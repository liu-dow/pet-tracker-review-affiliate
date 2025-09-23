package com.pettrackerreview.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Service
public class SearchEngineService {

    private static final String BING_API_KEY = "0a1530a2305041dbaa781156c2ce4c64";
    private static final String BING_INDEXNOW_API_URL = "https://api.indexnow.org/IndexNow";
    
    private final RestTemplate restTemplate;

    public SearchEngineService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Submit URL to Bing IndexNow API for indexing
     * @param url The URL to be indexed
     * @return true if submission was successful, false otherwise
     */
    public boolean submitUrlToBing(String url) {
        try {
            // Prepare request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Prepare request body according to IndexNow API specification
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("host", "pettrackerreview.com");
            requestBody.put("key", BING_API_KEY);
            requestBody.put("keyLocation", "https://pettrackerreview.com/" + BING_API_KEY + ".txt");
            
            // Create URL list with single URL
            List<String> urlList = new ArrayList<>();
            urlList.add(url);
            requestBody.put("urlList", urlList);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // Make the API call to Bing IndexNow API
            ResponseEntity<String> response = restTemplate.exchange(
                BING_INDEXNOW_API_URL,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            // Check if the request was successful (200 or 202 are both considered successful)
            int statusCode = response.getStatusCode().value();
            System.out.println("Bing IndexNow API response status: " + statusCode);
            System.out.println("Bing IndexNow API response body: " + response.getBody());
            
            return statusCode == 200 || statusCode == 202;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Submit multiple URLs to Bing IndexNow API for indexing
     * @param urls The list of URLs to be indexed
     * @return true if submission was successful, false otherwise
     */
    public boolean submitUrlsToBing(List<String> urls) {
        try {
            // Prepare request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Prepare request body according to IndexNow API specification
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("host", "pettrackerreview.com");
            requestBody.put("key", BING_API_KEY);
            requestBody.put("keyLocation", "https://pettrackerreview.com/" + BING_API_KEY + ".txt");
            requestBody.put("urlList", urls);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            // Make the API call to Bing IndexNow API
            ResponseEntity<String> response = restTemplate.exchange(
                BING_INDEXNOW_API_URL,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            // Check if the request was successful (200 or 202 are both considered successful)
            int statusCode = response.getStatusCode().value();
            System.out.println("Bing IndexNow API response status: " + statusCode);
            System.out.println("Bing IndexNow API response body: " + response.getBody());
            
            return statusCode == 200 || statusCode == 202;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}