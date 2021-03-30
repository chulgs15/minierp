package com.financial.ledger.repository.interfaces;

import com.financial.ledger.domain.GLPeriod;
import com.financial.ledger.domain.JournalEntry;
import com.financial.ledger.domain.JournalLineEntry;
import com.financial.ledger.repository.resultset.JournalDrCrTotalByFinancialAccountsDto;
import com.financial.ledger.repository.resultset.JournalDrCrTotalDto;

import java.util.List;
import org.springframework.data.domain.Page;

public interface JournalLineEntryRepositoryCustom {

  JournalDrCrTotalDto sumDrCrAmountByJournal(JournalEntry journalEntry);

  List<JournalLineEntry> findUnpostLinesByJournal(JournalEntry journalEntry);

  List<JournalLineEntry> findPostedLines(GLPeriod glPeriod);

  List<JournalLineEntry> findUnpostedLines(GLPeriod glPeriod);
}
