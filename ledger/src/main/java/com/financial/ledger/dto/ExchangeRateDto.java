package com.financial.ledger.dto;

import java.time.LocalDate;
import java.util.Map;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
public class ExchangeRateDto {

  private Map<String, Object> rates;
  private String base;

  @DateTimeFormat(pattern = "yyyy-MM-dd")
  private LocalDate date;

  public ExchangeRateDto() {
  }

  @Override
  public String toString() {
    return "ExchangeRateDto{" +
        "rates='" + rates.keySet().size() + '\'' +
        ", base='" + base + '\'' +
        ", date=" + date +
        '}';
  }
}