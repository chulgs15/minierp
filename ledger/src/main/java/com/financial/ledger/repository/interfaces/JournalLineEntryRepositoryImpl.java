package com.financial.ledger.repository.interfaces;

import com.financial.ledger.domain.GLPeriod;
import com.financial.ledger.domain.JournalEntry;
import com.financial.ledger.domain.JournalLineEntry;
import com.financial.ledger.domain.JournalLineEntry.PostFlag;
import com.financial.ledger.repository.resultset.JournalDrCrTotalDto;
import java.util.List;
import javax.persistence.EntityManager;

public class JournalLineEntryRepositoryImpl implements JournalLineEntryRepositoryCustom {

  private final EntityManager entityManager;

  public JournalLineEntryRepositoryImpl(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public JournalDrCrTotalDto sumDrCrAmountByJournal(JournalEntry journalEntry) {
    String sql = "select new com.financial.ledger.repository.resultset.JournalDrCrTotalDto(" +
        "sum(l.enteredDr), sum(l.enteredCr), sum(l.accountedDr), sum(l.accountedCr)" +
        ") from JournalLineEntry l where l.journalEntry = :journalEntry";
    return entityManager.createQuery(sql, JournalDrCrTotalDto.class)
        .setParameter("journalEntry", journalEntry)
        .getSingleResult();
  }

  @Override
  public List<JournalLineEntry> findUnpostLinesByJournal(JournalEntry journalEntry) {
    String sql = "select l from JournalLineEntry l where 1=1 " +
            "AND l.journalEntry = :journalEntry " +
            "AND l.postFlag = :postedFlag ";
    return entityManager.createQuery(sql, JournalLineEntry.class)
        .setParameter("journalEntry", journalEntry)
        .setParameter("postedFlag", JournalLineEntry.PostFlag.NEW)
        .getResultList();
  }

  @Override
  public List<JournalLineEntry> findPostedLines(GLPeriod glPeriod) {
    String sql = "select l from JournalLineEntry l where 1=1 "
        + "And l.glPeriod = :glPeriod "
        + "And l.postFlag = :postFlag ";

    return entityManager.createQuery(sql, JournalLineEntry.class)
        .setParameter("glPeriod", glPeriod)
        .setParameter("postFlag", PostFlag.POSTED)
        .getResultList();
  }

  @Override
  public List<JournalLineEntry> findUnpostedLines(GLPeriod glPeriod) {
    String sql = "select l from JournalLineEntry l join fetch l.journalEntry where 1=1 "
        + "And l.glPeriod = :glPeriod "
        + "And l.postFlag = :postFlag ";

    return entityManager.createQuery(sql, JournalLineEntry.class)
        .setParameter("glPeriod", glPeriod)
        .setParameter("postFlag", PostFlag.NEW)
        .getResultList();
  }
}
