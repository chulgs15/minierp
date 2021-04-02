package com.financial.ledger.repository;

import com.financial.ledger.domain.FinancialAccount;
import com.financial.ledger.domain.GLBalance;
import com.financial.ledger.domain.GLPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GLBalanceRepository extends JpaRepository<GLBalance, Long> {

  GLBalance findByglPeriodAndFinancialAccount(GLPeriod glPeriod, FinancialAccount financialAccount);

}
