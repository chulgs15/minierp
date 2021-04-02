package com.financial.ledger.dto;

import com.financial.ledger.domain.FinancialAccount;
import com.financial.ledger.domain.JournalLineEntry;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

@Getter
public class JournalCrLineEntryVO extends JournalLineEntryVO {

  @Builder
  public JournalCrLineEntryVO(
      BigDecimal amount,
      LocalDate accountingDate,
      FinancialAccount financialAccount) {
    super(JournalLineEntry.DrCrDeliminator.CR, amount, accountingDate, financialAccount);
  }

}
