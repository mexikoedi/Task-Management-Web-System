package io.github.mexikoedi.tmws.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthController {

  @GetMapping
  public ResponseEntity<Map<String, Object>> health() {
    Map<String, Object> response = new HashMap<>();
    response.put("status", "UP");
    response.put("message", "TMWS Backend is running");
    response.put("version", "0.0.1-SNAPSHOT");
    return ResponseEntity.ok(response);
  }
}
