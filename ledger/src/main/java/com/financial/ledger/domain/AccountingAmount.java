package com.financial.ledger.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import lombok.Getter;
import lombok.ToString;

@Embeddable
@ToString
@Getter
public class AccountingAmount {

  @Embedded
  private ExchangeRate exchangeRate;

  @Column(name = "amount")
  private BigDecimal amount;

  @Column(name = "functional_amount")
  private BigDecimal functionalAmount;

  public AccountingAmount() {
  }

  public AccountingAmount(ExchangeRate exchangeRate, BigDecimal amount) {
    this.exchangeRate = exchangeRate;
    this.amount = amount;
    this.functionalAmount = _getCalculatedFunctionalAmount(amount);
  }

  private AccountingAmount(ExchangeRate exchangeRate, BigDecimal amount,
      BigDecimal functionalAmount) {
    this.exchangeRate = exchangeRate;
    this.amount = amount;
    this.functionalAmount = functionalAmount;
  }

  public AccountingAmount getNegateAmount() {
    return new AccountingAmount(this.exchangeRate,
        amount.negate(),
        functionalAmount.negate());
  }

  private BigDecimal _getCalculatedFunctionalAmount(BigDecimal amount) {
    NumberFormat currencyInstance = NumberFormat.getCurrencyInstance();

    int roundPoint = currencyInstance.getMaximumFractionDigits();
    RoundingMode roundingMode = currencyInstance.getRoundingMode();

    return this.exchangeRate.getExchangeRate().multiply(amount)
        .setScale(roundPoint, roundingMode);
  }
}
