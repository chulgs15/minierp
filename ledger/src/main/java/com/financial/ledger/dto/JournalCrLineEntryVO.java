package com.financial.ledger.dto;

import com.financial.ledger.domain.JournalLineEntry;
import com.financial.ledger.enums.FinancialAccounts;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class JournalCrLineEntryVO extends JournalLineEntryVO {

  @Builder
  public JournalCrLineEntryVO(
      BigDecimal amount,
      LocalDate accountingDate,
      FinancialAccounts financialAccounts) {
    super(JournalLineEntry.DrCrDeliminator.CR, amount, accountingDate, financialAccounts);
  }

}
