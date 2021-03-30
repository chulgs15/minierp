package com.financial.ledger.repository.interfaces;

import com.financial.ledger.domain.GLPeriod;
import com.financial.ledger.domain.JournalEntry;

import java.util.List;

public interface JournalEntryRepositoryCustom {

  List<JournalEntry> findUnpostJournal(GLPeriod glPeriod);

}
