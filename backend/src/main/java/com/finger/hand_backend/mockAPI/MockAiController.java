package com.finger.hand_backend.mockAPI;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Profile("local")
public class MockAiController {

    @PostMapping("/diary/summary")
    public ResponseEntity<?> summary(
            @RequestParam(defaultValue = "3000") long delayMs,
            @RequestParam(defaultValue = "0") double failRate
    ) throws InterruptedException {
        Thread.sleep(delayMs);
        if (failRate > 0 && Math.random() < failRate) {
            return ResponseEntity.status(500).body(Map.of("error", "mock failure"));
        }
        return ResponseEntity.ok(Map.of(
                "user_id", 1,
                "result", Map.of(
                        "score", 42.0,
                        "sentiment", Map.of("기쁨", 0.3, "당황", 0.1, "분노", 0.1, "불안", 0.1, "상처", 0.1, "슬픔", 0.3),
                        "short_summary", "mock short",
                        "long_summary", "mock long",
                        "short_advice", "mock advice"
                )
        ));
    }
}
