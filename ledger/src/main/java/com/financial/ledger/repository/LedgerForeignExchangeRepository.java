package com.financial.ledger.repository;

import com.financial.ledger.domain.IdClass.LedgerFXKey;
import com.financial.ledger.domain.LedgerForeignExchange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LedgerForeignExchangeRepository extends
    JpaRepository<LedgerForeignExchange, LedgerFXKey> {

}
