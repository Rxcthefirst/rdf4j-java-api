package com.example.rdf4j.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.resultio.QueryResultFormat;
import org.eclipse.rdf4j.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.eclipse.rdf4j.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.eclipse.rdf4j.query.resultio.text.csv.SPARQLResultsCSVWriter;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Controller
@RequestMapping("/sparql")
public class SparqlProxyController {

    private final Repository rdf4jRepository;

    @Autowired
    public SparqlProxyController(Repository rdf4jRepository) {
        this.rdf4jRepository = rdf4jRepository;
    }

    @GetMapping
    public ResponseEntity<?> executeGetQuery(@RequestParam("query") String encodedQuery,
                                             @RequestParam(value = "format", required = false) String format) {


        try {
            // Decode the query parameter
            String decodedQuery = URLDecoder.decode(encodedQuery, StandardCharsets.UTF_8);
            System.out.println("Requested Format: " + (format != null ? format : "default"));

            // Process the query (e.g., send to RDF database)
            String result = processSparqlQuery(decodedQuery, format);

            // Debug statement
            System.out.println("Result: " + result);

            // Return the result to the client

            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing query: " + e.getMessage());
        }
    }

    @PostMapping(consumes = {"application/sparql-query", "application/x-www-form-urlencoded"}, produces = {
            "application/sparql-results+xml",
            "application/sparql-results+json",
            "text/csv"
    })
    public ResponseEntity<String> proxySparqlQuery(HttpServletRequest request, @RequestBody(required = false) String sparqlQuery) {

        String query;
        String contentType = request.getContentType();

         System.out.println("Query: " + sparqlQuery);
        QueryResultFormat resultFormat;
        String acceptHeader = null;

        if ("application/x-www-form-urlencoded".equalsIgnoreCase(contentType)) {
            // Extract the query parameter from URL-encoded content
            query = request.getParameter("query");
            if (query == null) {
                return ResponseEntity.badRequest().body("Missing 'query' parameter");
            }
            query = URLDecoder.decode(query, StandardCharsets.UTF_8);
        } else if ("application/sparql-query".equalsIgnoreCase(contentType)) {
            // Use raw query directly from the request body
            acceptHeader = request.getHeader("Accept");
            query = sparqlQuery;
        } else {
            return ResponseEntity.status(415).body("Unsupported Content-Type: " + contentType);
        }


        if (acceptHeader != null && acceptHeader.contains("application/sparql-results+json")) {
            resultFormat = new QueryResultFormat(
                    "SPARQL/JSON",                            // Name
                    "application/sparql-results+json", // MIME type
                    StandardCharsets.UTF_8,                  // Charset
                    Collections.singletonList(".json")                          // Is binary
            );
        } else if (acceptHeader != null && acceptHeader.contains("application/sparql-results+xml")) {
            resultFormat = new QueryResultFormat(
                    "XML",                            // Name
                    "application/sparql-results+xml", // MIME type
                    StandardCharsets.UTF_8,                  // Charset
                    Collections.singletonList(".xml")                          // Is binary
            );
        } else if (acceptHeader != null && acceptHeader.contains("text/csv")) {
            resultFormat = new QueryResultFormat(
                    "CSV",                            // Name
                    "text/csv", // MIME type
                    StandardCharsets.UTF_8,                  // Charset
                    Collections.singletonList(".csv")                          // Is binary
            );
        } else {
            resultFormat = new QueryResultFormat(
                    "JSON",                            // Name
                    "application/custom-sparql-results+json", // MIME type
                    StandardCharsets.UTF_8,                  // Charset
                    Collections.singletonList(".json")                          // Is binary
            ); // Default to XML
        }

        try (RepositoryConnection connection = rdf4jRepository.getConnection()) {
            TupleQuery tupleQuery = connection.prepareTupleQuery(query);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        if (acceptHeader != null && acceptHeader.contains("application/sparql-results+xml")) {
            tupleQuery.evaluate(new SPARQLResultsXMLWriter(outputStream));
        } else if (acceptHeader != null && acceptHeader.contains("application/sparql-results+json")) {
            tupleQuery.evaluate(new SPARQLResultsJSONWriter(outputStream));
        } else if (acceptHeader != null && acceptHeader.contains("text/csv")) {
            tupleQuery.evaluate(new SPARQLResultsCSVWriter(outputStream));
        } else {
            // Default to XML if no valid Accept header is provided
            tupleQuery.evaluate(new SPARQLResultsJSONWriter(outputStream));
        }


            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(resultFormat.getDefaultMIMEType()))
                    .body(outputStream.toString(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing SPARQL query: " + e.getMessage());
        }
    }

    private String processSparqlQuery(String query, String format) {
        // Implement the logic to send the query to your RDF database

        try (RepositoryConnection connection = rdf4jRepository.getConnection()) {
            TupleQuery tupleQuery = connection.prepareTupleQuery(query);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            if (format != null && format.contains("xml")) {
                tupleQuery.evaluate(new SPARQLResultsXMLWriter(outputStream));
            } else if (format != null && (format.contains("json"))) {
                tupleQuery.evaluate(new SPARQLResultsJSONWriter(outputStream));
            } else if (format != null && format.contains("csv")) {
                tupleQuery.evaluate(new SPARQLResultsCSVWriter(outputStream));
            } else {
                // Default to XML if no valid Accept header is provided
                tupleQuery.evaluate(new SPARQLResultsXMLWriter(outputStream));
            }


            return outputStream.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}




