package com.financial.ledger.repository.resultset;

import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class JournalDrCrTotalDto {
  private BigDecimal totalEnteredDr;
  private BigDecimal totalEnteredCr;
  private BigDecimal totalAccountedDr;
  private BigDecimal totalAccountedCr;

  public JournalDrCrTotalDto(BigDecimal totalEnteredDr, BigDecimal totalEnteredCr, BigDecimal totalAccountedDr, BigDecimal totalAccountedCr) {
    this.totalEnteredDr = totalEnteredDr;
    this.totalEnteredCr = totalEnteredCr;
    this.totalAccountedDr = totalAccountedDr;
    this.totalAccountedCr = totalAccountedCr;
  }
}
