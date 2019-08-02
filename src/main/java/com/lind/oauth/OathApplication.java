package com.lind.oauth;

import java.security.Principal;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class OathApplication {

  public static void main(String[] args) {
    SpringApplication.run(OathApplication.class, args);
  }

  @GetMapping("/user")
  public Principal user(Principal user) {
    return user;
  }

  @GetMapping("/index")
  public String index() {
    return "index";
  }

  @GetMapping("/oauth/callback")
  public String callback() {
    return "hello oauth2";
  }

}
