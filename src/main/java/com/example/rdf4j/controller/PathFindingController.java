package com.example.rdf4j.controller;

import com.example.rdf4j.service.PathFindingService;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PathFindingController {

    private final PathFindingService pathFindingService;

    @Autowired
    public PathFindingController(PathFindingService pathFindingService) {
        this.pathFindingService = pathFindingService;
    }

    @GetMapping("/findPaths")
    public void findPaths(@RequestParam String start, @RequestParam String end) {
        SimpleValueFactory vf = SimpleValueFactory.getInstance();
        IRI startIRI = vf.createIRI(start);
        IRI endIRI = vf.createIRI(end);
        pathFindingService.findPaths(startIRI, endIRI);
    }
}
