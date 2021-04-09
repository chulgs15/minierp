package com.financial.ledger.repository;

import com.financial.ledger.domain.GLPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GLPeriodRepository extends JpaRepository<GLPeriod, String> {

  GLPeriod findByPeriodName(String periodName);
}
