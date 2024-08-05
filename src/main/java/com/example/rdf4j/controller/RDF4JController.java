package com.example.rdf4j.controller;

import com.example.rdf4j.service.RDF4JService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RDF4JController {

    @Autowired
    private RDF4JService rdf4jService;

    @GetMapping("/execute-query")
    public String executeQuery() {
        rdf4jService.executeQuery();
        return "Query executed. Check the console for results.";
    }
}