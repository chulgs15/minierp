package com.financial.ledger.repository.resultset;

import com.financial.ledger.enums.FinancialAccounts;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class JournalDrCrTotalByFinancialAccountsDto {
  private BigDecimal totalEnteredDr;
  private BigDecimal totalEnteredCr;
  private BigDecimal totalAccountedDr;
  private BigDecimal totalAccountedCr;
  private FinancialAccounts financialAccounts;

  public JournalDrCrTotalByFinancialAccountsDto(BigDecimal totalEnteredDr,
                                                BigDecimal totalEnteredCr,
                                                BigDecimal totalAccountedDr,
                                                BigDecimal totalAccountedCr,
                                                FinancialAccounts financialAccounts) {
    this.totalEnteredDr = totalEnteredDr;
    this.totalEnteredCr = totalEnteredCr;
    this.totalAccountedDr = totalAccountedDr;
    this.totalAccountedCr = totalAccountedCr;
    this.financialAccounts = financialAccounts;
  }
}
