package com.financial.ledger.repository;

import com.financial.ledger.domain.JournalEntry;
import com.financial.ledger.repository.interfaces.JournalEntryRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long>,
    JournalEntryRepositoryCustom {

}
