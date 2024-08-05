package com.example.rdf4j.service;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Service
public class PathFindingService {

    @Autowired
    private Repository repository;
    private final ValueFactory vf = SimpleValueFactory.getInstance();

    private static final int MAX_DEPTH = 5;  // Set a maximum depth to avoid deep cycles

    // Define your specific predicates (change the namespace as necessary)
    private final IRI FRIEND_OF = vf.createIRI("http://example.org/friendOf");
    private final IRI PARENT_OF = vf.createIRI("http://example.org/parentOf");

    public void findPaths(IRI start, IRI end) {
        List<List<IRI>> allPaths = findPaths(start, end, new LinkedList<>(), new HashSet<>(), 0);
        for (List<IRI> path : allPaths) {
            System.out.println(" -> " + String.join(" -> ", path.stream().map(IRI::toString).toArray(String[]::new)));
        }
    }

    private List<List<IRI>> findPaths(IRI start, IRI end, List<IRI> path, Set<IRI> visited, int depth) {
        path.add(start);
        visited.add(start);

        List<List<IRI>> paths = new LinkedList<>();
        if (start.equals(end)) {
            paths.add(new LinkedList<>(path));
        } else if (depth < MAX_DEPTH) {
            // SPARQL query to find specific relationships
            String queryString =
                    "SELECT ?o WHERE { " +
                            "{ <" + start.stringValue() + "> <" + FRIEND_OF.stringValue() + "> ?o . } " +
                            "UNION " +
                            "{ <" + start.stringValue() + "> <" + PARENT_OF.stringValue() + "> ?o . } " +
                            "UNION " +
                            "{ ?o <" + PARENT_OF.stringValue() + "> <" + start.stringValue() + "> . } " +
                            "UNION " +
                            "{ ?o <" + FRIEND_OF.stringValue() + "> <" + start.stringValue() + "> . } " +
                            "}";

            try (RepositoryConnection conn = repository.getConnection()) {
                TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
                try (TupleQueryResult result = query.evaluate()) {
                    while (result.hasNext()) {
                        BindingSet bindingSet = result.next();
                        Value o = bindingSet.getValue("o");

                        if (o instanceof IRI && !visited.contains(o)) {
                            List<IRI> newPath = new LinkedList<>(path);
                            Set<IRI> newVisited = new HashSet<>(visited);
                            List<List<IRI>> newPaths = findPaths((IRI) o, end, newPath, newVisited, depth + 1);
                            paths.addAll(newPaths);
                        }
                    }
                }
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }

        path.remove(start);  // Remove the start node from the current path
        visited.remove(start);  // Allow the start node to be revisited in different paths
        return paths;
    }
}
