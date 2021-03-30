package com.financial.ledger.dto;

import com.financial.ledger.domain.JournalLineEntry;
import com.financial.ledger.enums.FinancialAccounts;
import com.sun.istack.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public abstract class JournalLineEntryVO {
  @NotNull
  private JournalLineEntry.DrCrDeliminator deliminator = JournalLineEntry.DrCrDeliminator.CR;

  @NotNull
  private BigDecimal amount;

  @NotNull
  private LocalDate accountingDate;

  @NotNull
  private FinancialAccounts financialAccounts;

  public JournalLineEntryVO(JournalLineEntry.DrCrDeliminator deliminator,
                            BigDecimal amount,
                            LocalDate accountingDate,
                            FinancialAccounts financialAccounts) {
    this.deliminator = deliminator;
    this.amount = amount;
    this.accountingDate = accountingDate == null ? LocalDate.now() : accountingDate;
    this.financialAccounts = financialAccounts;
  }
}
