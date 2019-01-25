package com.lind.oath;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class OathApplication {

  public static void main(String[] args) {
    SpringApplication.run(OathApplication.class, args);
  }

  @GetMapping("/login")
  public String login() {
    return "login";
  }

  @GetMapping("/index")
  public String index() {
    return "index";
  }
}

