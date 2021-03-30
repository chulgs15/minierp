package com.financial.ledger.dto;

import com.financial.ledger.domain.JournalLineEntry;
import com.financial.ledger.enums.FinancialAccounts;
import com.sun.istack.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class JournalDrLineEntryVO extends JournalLineEntryVO {


  @Builder
  public JournalDrLineEntryVO(
      BigDecimal amount,
      LocalDate accountingDate,
      FinancialAccounts financialAccounts) {
    super(JournalLineEntry.DrCrDeliminator.DR, amount, accountingDate, financialAccounts);
  }
}
