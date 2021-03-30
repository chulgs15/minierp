package com.financial.ledger.repository;

import com.financial.ledger.domain.GLBalance;
import com.financial.ledger.domain.GLPeriod;
import com.financial.ledger.enums.FinancialAccounts;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GLBalanceRepository extends JpaRepository<GLBalance, Long> {

  GLBalance findByglPeriodAndFinancialAccounts(GLPeriod glPeriod, FinancialAccounts financialAccounts);

}
