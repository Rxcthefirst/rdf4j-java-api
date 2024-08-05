package com.example.rdf4j.service;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Service
public class PFT2 {

    private final Repository repository;
    private final ValueFactory vf = SimpleValueFactory.getInstance();

    @Autowired
    public PFT2(Repository repository) {
        this.repository = repository;
    }

    public void findPaths(IRI start, IRI end) {
        List<List<IRI>> allPaths = new LinkedList<>();
        try (RepositoryConnection connection = repository.getConnection()) {
            allPaths = findPaths(connection, start, end, new LinkedList<>(), new HashSet<>());
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (List<IRI> path : allPaths) {
            System.out.println(" -> " + String.join(" -> ", path.stream().map(IRI::toString).toArray(String[]::new)));
        }
    }

    private List<List<IRI>> findPaths(RepositoryConnection connection, IRI start, IRI end, List<IRI> path, Set<IRI> visited) {
        path.add(start);
        visited.add(start);

        List<List<IRI>> paths = new LinkedList<>();
        if (start.equals(end)) {
            paths.add(new LinkedList<>(path));
        } else {
            String query = "SELECT ?o WHERE { <" + start.stringValue() + "> ?p ?o }";
            try (TupleQueryResult result = connection.prepareTupleQuery(QueryLanguage.SPARQL, query).evaluate()) {
                while (result.hasNext()) {
                    BindingSet bindingSet = result.next();
                    Value o = bindingSet.getValue("o");

                    if (o instanceof IRI && !visited.contains(o)) {
                        List<IRI> newPath = new LinkedList<>(path);
                        Set<IRI> newVisited = new HashSet<>(visited);
                        List<List<IRI>> newPaths = findPaths(connection, (IRI) o, end, newPath, newVisited);
                        for (List<IRI> newPathResult : newPaths) {
                            paths.add(newPathResult);
                        }
                    }
                }
            }
        }
        return paths;
    }
}
