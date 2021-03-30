package com.aidant.erp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.financial.ledger"})
@EntityScan(value = {"com.financial.ledger"})
@EnableJpaRepositories(value = {"com.financial.ledger"})
public class MiniERPApplication {

  public static void main(String[] args) {
    SpringApplication.run(MiniERPApplication.class, args);

  }
}
