package com.financial.ledger.domain;

import com.financial.ledger.domain.IdClass.LedgerFXKey;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@IdClass(LedgerFXKey.class)
@Table(name = "gl_daily_rates")
@ToString
@RequiredArgsConstructor
public class LedgerForeignExchange {

  @Id
  @Column(name = "exchange_date")
  private LocalDate exchangeDate;

  @Id
  @Column(name = "from_currency")
  private String fromCurrency;

  @Id
  @Column(name = "to_currency")
  private String toCurrency;

  @Column(name = "exchange_rate", nullable = false)
  private BigDecimal exchangeRate;

  @Builder
  public LedgerForeignExchange(LedgerFXKey fxKey,
                               BigDecimal exchangeRate) {
    this.exchangeDate = fxKey.getExchangeDate();
    this.fromCurrency = fxKey.getFromCurrency();
    this.toCurrency = fxKey.getToCurrency();
    this.exchangeRate = exchangeRate;
  }

  public ExchangeRate changeToExchangeRate() {
    return new ExchangeRate(this.fromCurrency, this.exchangeRate);
  }
}
