package com.financial.ledger.domain.IdClass;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;


@Setter
@Getter
@ToString
@RequiredArgsConstructor
public class LedgerFXKey implements Serializable {
  private LocalDate exchangeDate;
  private String fromCurrency;
  private String toCurrency;

  @Builder
  public LedgerFXKey(LocalDate exchangeDate, String fromCurrency, String toCurrency) {
    this.exchangeDate = exchangeDate;
    this.fromCurrency = fromCurrency;
    this.toCurrency = toCurrency;
  }

  @Override
  public int hashCode() {
    return exchangeDate.hashCode() + fromCurrency.hashCode() + toCurrency.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    LedgerFXKey o = (LedgerFXKey) obj;

    return (exchangeDate.compareTo(o.getExchangeDate()) == 0 &&
        fromCurrency.equals(o.getFromCurrency()) &&
        toCurrency.equals(o.getToCurrency()));
  }
}
