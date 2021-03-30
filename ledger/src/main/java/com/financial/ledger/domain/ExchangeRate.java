package com.financial.ledger.domain;

import lombok.Getter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

@Embeddable
@Getter
@ToString
public class ExchangeRate {

  @Column(name = "currency")
  private String currency;

  @Column(name = "exchange_rate")
  private BigDecimal exchangeRate;

  public ExchangeRate() {
    this.currency = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
    this.exchangeRate = BigDecimal.ONE;
  }

  public ExchangeRate(String currency, BigDecimal exchangeRate) {
    this.currency = currency;
    this.exchangeRate = exchangeRate;
  }
}
