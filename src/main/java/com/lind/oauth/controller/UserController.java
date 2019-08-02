package com.lind.oauth.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
public class UserController {
  @RequestMapping(value = "/callback", method = RequestMethod.GET)
  public ResponseEntity<String> callback(@RequestParam("code") String code) throws JsonProcessingException, IOException {
    ResponseEntity<String> response = null;
    System.out.println("Authorization Ccode------" + code);

    RestTemplate restTemplate = new RestTemplate();

    String access_token_url = "http://localhost:8081/oauth/token";
    access_token_url += "?client_id=android1&code=" + code;
    access_token_url += "&grant_type=authorization_code";
    access_token_url += "&redirect_uri=http://localhost:8081/callback";
    access_token_url += "&client_secret=android1";
    System.out.println("access_token_url " + access_token_url);

    response = restTemplate.exchange(access_token_url, HttpMethod.POST, null, String.class);

    System.out.println("Access Token Response ---------" + response.getBody());

    // Get the Access Token From the recieved JSON response
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(response.getBody());
    String token = node.path("access_token").asText();

    String url = "http://localhost:8081/index";

    // Use the access token for authentication
    HttpHeaders headers1 = new HttpHeaders();
    headers1.add("Authorization", "Bearer " + token);
    HttpEntity<String> entity = new HttpEntity<>(headers1);

    ResponseEntity<String> result = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    return result;
  }
}
