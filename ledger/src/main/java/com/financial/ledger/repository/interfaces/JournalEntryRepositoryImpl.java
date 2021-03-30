package com.financial.ledger.repository.interfaces;

import com.financial.ledger.domain.GLPeriod;
import com.financial.ledger.domain.JournalEntry;

import com.financial.ledger.domain.JournalLineEntry.PostFlag;
import javax.persistence.EntityManager;
import java.util.List;

public class JournalEntryRepositoryImpl implements JournalEntryRepositoryCustom {

  private final EntityManager entityManager;

  public JournalEntryRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public List<JournalEntry> findUnpostJournal(GLPeriod glPeriod) {
    String sql = "select h from JournalEntry h where 1=1 "
        + "and exists (select l from h.journalLineEntries l "
        + "where 1=1 "
        + "and l.glPeriod = :glPeriod "
        + "and l.postFlag = :postFlag )";
    return entityManager.createQuery(sql, JournalEntry.class)
        .setParameter("glPeriod", glPeriod)
        .setParameter("postFlag", PostFlag.NEW)
        .getResultList();
  }

}
