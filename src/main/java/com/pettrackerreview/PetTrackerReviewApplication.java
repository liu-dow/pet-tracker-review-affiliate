package com.pettrackerreview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class PetTrackerReviewApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetTrackerReviewApplication.class, args);
    }
}