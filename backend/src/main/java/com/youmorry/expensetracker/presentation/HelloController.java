package com.youmorry.expensetracker.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** ヘルスチェック用コントローラ。 */
@RestController
public class HelloController {

  /** ヘルスチェック用エンドポイント。 */
  @GetMapping("/api/v1/hello")
  public String hello() {
    return "Hello World";
  }
}
