package com.example.rdf4j.controller;

import com.example.rdf4j.service.PathFindingService;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PathFindingController {

    private final PathFindingService pathFindingService;

    @Autowired
    public PathFindingController(PathFindingService pathFindingService) {
        this.pathFindingService = pathFindingService;
    }

    @GetMapping("/find")
    public ResponseEntity<List<List<IRI>>> findPaths(
            @RequestParam("start") String start,
            @RequestParam("end") String end) {
        IRI startIRI = SimpleValueFactory.getInstance().createIRI(start);
        IRI endIRI = SimpleValueFactory.getInstance().createIRI(end);

        List<List<IRI>> paths = pathFindingService.findPaths(startIRI, endIRI);
        return ResponseEntity.ok(paths);
    }
}
