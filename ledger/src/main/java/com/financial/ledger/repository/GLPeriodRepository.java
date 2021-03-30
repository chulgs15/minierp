package com.financial.ledger.repository;

import com.financial.ledger.domain.GLPeriod;
import com.financial.ledger.domain.JournalEntry;
import com.financial.ledger.repository.interfaces.JournalEntryRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GLPeriodRepository extends JpaRepository<GLPeriod, String> {
  GLPeriod findByPeriodName(String periodName);
}
