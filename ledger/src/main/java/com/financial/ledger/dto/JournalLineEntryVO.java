package com.financial.ledger.dto;

import com.financial.ledger.domain.FinancialAccount;
import com.financial.ledger.domain.JournalLineEntry;
import com.sun.istack.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;

@Getter
public abstract class JournalLineEntryVO {

  @NotNull
  private JournalLineEntry.DrCrDeliminator deliminator;

  @NotNull
  private BigDecimal amount;

  @NotNull
  private LocalDate accountingDate;

  @NotNull
  private FinancialAccount financialAccount;

  public JournalLineEntryVO(JournalLineEntry.DrCrDeliminator deliminator,
      BigDecimal amount,
      LocalDate accountingDate,
      FinancialAccount financialAccount) {
    this.deliminator = deliminator;
    this.amount = amount;
    this.accountingDate = accountingDate == null ? LocalDate.now() : accountingDate;
    this.financialAccount = financialAccount;
  }
}
