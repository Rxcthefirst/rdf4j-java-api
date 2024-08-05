package com.example.rdf4j.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Controller
public class SparqlProxyController {

    @Autowired
    private RestTemplate restTemplate;

    private final String rdf4jSparqlEndpoint = "http://localhost:8080/rdf4j-server/repositories/1";

    @PostMapping(value = "/sparql", consumes = "application/sparql-query", produces = "application/sparql-results+json")
    public ResponseEntity<String> proxySparqlQuery(@RequestBody String sparqlQuery) {
        // Set the headers to specify the content type and accept type
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/sparql-query"));
        headers.setAccept(List.of(MediaType.parseMediaType("application/sparql-results+json")));

        // Create an HttpEntity containing the query and headers
        HttpEntity<String> requestEntity = new HttpEntity<>(sparqlQuery, headers);

        // Forward the query to the RDF4J SPARQL endpoint
        ResponseEntity<String> response = restTemplate.exchange(
                rdf4jSparqlEndpoint,
                HttpMethod.POST,
                requestEntity,
                String.class
        );

        // Return the response body and headers from RDF4J
        return ResponseEntity.status(response.getStatusCode())
                .headers(response.getHeaders())
                .body(response.getBody());
    }
}
