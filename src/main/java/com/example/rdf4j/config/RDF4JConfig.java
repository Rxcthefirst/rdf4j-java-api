package com.example.rdf4j.config;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RDF4JConfig {

    @Bean
    public Repository rdf4jRepository() {
        String sparqlEndpoint = "http://localhost:8080/rdf4j-server/repositories/1"; // Replace with your SPARQL endpoint
        Repository repo = new SPARQLRepository(sparqlEndpoint);
        repo.init();
        return repo;
    }
}