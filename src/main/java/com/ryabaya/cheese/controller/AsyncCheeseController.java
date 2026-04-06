package com.ryabaya.cheese.controller;

import com.ryabaya.cheese.dto.request.CheeseBulkRequestDto;
import com.ryabaya.cheese.entity.AsyncTask;
import com.ryabaya.cheese.service.CheeseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/cheeses/async")
@RequiredArgsConstructor
public class AsyncCheeseController {

    private final CheeseService cheeseService;

    @PostMapping("/{shopId}/{producerId}")
    public ResponseEntity<Map<String, String>> createCheesesAsync(
            @PathVariable Long shopId,
            @PathVariable Long producerId,
            @RequestBody CheeseBulkRequestDto request) {

        String taskId = cheeseService.createCheesesAsync(shopId, producerId, request);
        Map<String, String> responseBody = Map.of("taskId", taskId);

        return ResponseEntity.accepted().body(responseBody);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<AsyncTask> getTaskStatus(@PathVariable String taskId) {
        AsyncTask task = cheeseService.getCheeseTaskStatus(taskId);

        if (task == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(task);
    }

    @GetMapping("/tasks")
    public ResponseEntity<Map<String, AsyncTask>> getAllAsyncTasks() {
        return ResponseEntity.ok(cheeseService.getAllAsyncTasks());
    }
}
