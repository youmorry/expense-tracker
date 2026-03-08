package com.youmorry.expensetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** 支出記録アプリケーションのエントリーポイント。 */
@SpringBootApplication
public class ExpenseTrackerApplication {

  /** アプリケーションを起動する。 */
  public static void main(String[] args) {
    SpringApplication.run(ExpenseTrackerApplication.class, args);
  }
}
