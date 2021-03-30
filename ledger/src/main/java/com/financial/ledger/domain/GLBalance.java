package com.financial.ledger.domain;


import com.financial.ledger.enums.FinancialAccounts;
import lombok.Getter;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@SequenceGenerator(name = "gl_balance_s", sequenceName = "gl_balance_s", allocationSize = 1)
@Table(name = "gl_balances", indexes = {
    @Index(name = "GL_BALANCE_U1", unique = true, columnList = "gl_period_id, financial_account")})
@Getter
public class GLBalance {

    @Id
    @GeneratedValue(generator = "gl_balance_s", strategy = GenerationType.SEQUENCE)
    @Column(name = "gl_balance_id")
    private Long balanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gl_period_id")
    private GLPeriod glPeriod;

    @Column(name = "financial_account")
    @Enumerated(EnumType.STRING)
    private FinancialAccounts financialAccounts;

    @Column(name = "begin_dr")
    private BigDecimal beginDr = BigDecimal.ZERO;

    @Column(name = "begin_cr")
    private BigDecimal beginCr = BigDecimal.ZERO;

    @Column(name = "period_dr")
    private BigDecimal periodDr = BigDecimal.ZERO;

    @Column(name = "period_cr")
    private BigDecimal periodCr = BigDecimal.ZERO;

    @Column(name = "balance_amount")
    private BigDecimal balanceAmount = BigDecimal.ZERO;

    public GLBalance() {
    }

    public GLBalance(GLPeriod glPeriod, FinancialAccounts financialAccounts) {
        this.glPeriod = glPeriod;
        this.financialAccounts = financialAccounts;
    }

    public void addCr(BigDecimal accountedCr) {
        this.periodCr = this.periodCr.add(accountedCr);
        applyBalanceAmount();
    }

    public void addDr(BigDecimal accountedDr) {
        this.periodDr = this.periodDr.add(accountedDr);
        applyBalanceAmount();
    }

    private void applyBalanceAmount() {
        this.balanceAmount = this.beginDr.add(this.periodDr).subtract(this.beginCr).subtract(this.periodCr);
    }
}
