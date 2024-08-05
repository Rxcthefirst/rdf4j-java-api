package com.example.rdf4j.service;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.BindingSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Service
public class RDF4JService {

    @Autowired
    private Repository repository;

    @Autowired
    private ResourceLoader resourceLoader;

    public void executeQuery() {
        // Read the SPARQL query from the file
        String queryString = loadSparqlQuery("classpath:sparql/query1.sparql");

        if (queryString == null) {
            System.err.println("Failed to load the SPARQL query.");
            return;
        }

        try (RepositoryConnection conn = repository.getConnection()) {
            // Prepare and execute the query
            TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            try (TupleQueryResult result = query.evaluate()) {
                // Iterate over the result and print each binding set
                while (result.hasNext()) {
                    BindingSet bindingSet = result.next();
                    System.out.println(bindingSet);
                }
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    private String loadSparqlQuery(String filePath) {
        Resource resource = resourceLoader.getResource(filePath);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}