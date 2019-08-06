package com.lind.oauth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.security.Principal;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@RestController
public class OathApplication {

  public static void main(String[] args) {
    SpringApplication.run(OathApplication.class, args);
  }

  @GetMapping("/user")
  public String user(Principal user) {
    return "user list";
  }

  @GetMapping("/index")
  public String index() {
    return "index";
  }

  @GetMapping("/")
  public String home() {
    return "home";
  }

  @RequestMapping(value = "/callback", method = RequestMethod.GET)
  public ResponseEntity<String> callback(@RequestParam("code") String code) throws JsonProcessingException, IOException {
    // 访问/oauth/authorize返回一个唯一的code
    System.out.println("Authorization Ccode------" + code);

    // 带上code和client_id及client_secret来获取一个access_token
    RestTemplate restTemplate = new RestTemplate();
    String access_token_url = "http://localhost:8081/oauth/token";
    access_token_url += "?client_id=android1&code=" + code;
    access_token_url += "&grant_type=authorization_code";
    access_token_url += "&redirect_uri=http://localhost:8081/callback";
    access_token_url += "&client_secret=android1";
    System.out.println("access_token_url " + access_token_url);
    ResponseEntity<String> response = restTemplate.exchange(access_token_url, HttpMethod.POST, null, String.class);
    System.out.println("Access Token Response ---------" + response.getBody());

    // 通过access_token来获取真实的资源
    ObjectMapper mapper = new ObjectMapper();
    JsonNode node = mapper.readTree(response.getBody());
    String token = node.path("access_token").asText();
    return response;
  }
}
