package com.youmorry.expensetracker.auth.infrastructure;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class SecurityTestController {

  @GetMapping("/api/v1/test")
  public String test() {
    return "ok";
  }
}
