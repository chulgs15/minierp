package com.financial.ledger.repository;

import com.financial.ledger.domain.FinancialAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FinancialAccountsRepository extends JpaRepository<FinancialAccount, String> {



}
