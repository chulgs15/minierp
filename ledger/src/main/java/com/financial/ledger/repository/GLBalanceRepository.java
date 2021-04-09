package com.financial.ledger.repository;

import com.financial.ledger.domain.FinancialAccount;
import com.financial.ledger.domain.GLBalance;
import com.financial.ledger.domain.GLPeriod;
import java.util.List;
import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

public interface GLBalanceRepository extends JpaRepository<GLBalance, Long> {

  @Lock(LockModeType.PESSIMISTIC_READ)
  @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "3000")})
  GLBalance findByglPeriodAndFinancialAccount(GLPeriod glPeriod, FinancialAccount financialAccount);

  List<GLBalance> findByglPeriod(GLPeriod glPeriod);
}
