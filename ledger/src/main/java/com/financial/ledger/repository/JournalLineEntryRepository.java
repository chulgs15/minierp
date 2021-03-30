package com.financial.ledger.repository;

import com.financial.ledger.domain.JournalLineEntry;
import com.financial.ledger.repository.interfaces.JournalLineEntryRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JournalLineEntryRepository extends JpaRepository<JournalLineEntry, Long>, JournalLineEntryRepositoryCustom {

}
