package com.finger.hand_backend.mockAPI;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/test")
@RequiredArgsConstructor
public class TestDataController {

    private final TestDataSeeder seeder;

    @PostMapping("/seed")
    public TestDataSeeder.SeedResult seed(
            @RequestParam(defaultValue = "100") int users,
            @RequestParam(defaultValue = "3") int diariesPerUser
    ) {
        return seeder.seed(users, diariesPerUser);
    }
}


