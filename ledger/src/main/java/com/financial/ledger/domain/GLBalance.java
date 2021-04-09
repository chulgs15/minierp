package com.financial.ledger.domain;


import java.math.BigDecimal;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;

@Entity
@SequenceGenerator(name = "gl_balance_s", sequenceName = "gl_balance_s", allocationSize = 1)
@Table(name = "gl_balances", indexes = {
    @Index(name = "GL_BALANCE_U1", unique = true, columnList = "gl_period_id, acct_code")})
@Getter
public class GLBalance {

  @Id
  @GeneratedValue(generator = "gl_balance_s", strategy = GenerationType.SEQUENCE)
  @Column(name = "gl_balance_id")
  private Long balanceId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "gl_period_id")
  private GLPeriod glPeriod;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "acct_code")
  private FinancialAccount financialAccount;

  @Column(name = "begin_dr")
  private BigDecimal beginDr = BigDecimal.ZERO;

  @Column(name = "begin_cr")
  private BigDecimal beginCr = BigDecimal.ZERO;

  @Column(name = "period_dr")
  private BigDecimal periodDr = BigDecimal.ZERO;

  @Column(name = "period_cr")
  private BigDecimal periodCr = BigDecimal.ZERO;

  @Column(name = "balance_dr")
  private BigDecimal balanceDr = BigDecimal.ZERO;

  @Column(name = "balance_cr")
  private BigDecimal balanceCr = BigDecimal.ZERO;

  @Column(name = "balance_amount")
  private BigDecimal balanceAmount = BigDecimal.ZERO;

  public GLBalance() {
  }

  public GLBalance(GLPeriod glPeriod, FinancialAccount financialAccount) {
    this.glPeriod = glPeriod;
    this.financialAccount = financialAccount;
  }

  public void addCr(BigDecimal accountedCr) {
    this.periodCr = this.periodCr.add(accountedCr);
    applyBalanceAmount();
  }

  public void addDr(BigDecimal accountedDr) {
    this.periodDr = this.periodDr.add(accountedDr);
    applyBalanceAmount();
  }

  public void addBeginCr(BigDecimal beginCr) {
    this.beginCr = this.beginCr.add(beginCr);
    applyBalanceAmount();
  }

  public void addBeginDr(BigDecimal beginDr) {
    this.beginDr = this.beginDr.add(beginDr);
    applyBalanceAmount();
  }

  private void applyBalanceAmount() {
    this.balanceDr = this.beginDr.add(this.periodDr);
    this.balanceCr = this.beginCr.add(this.periodCr);

    this.balanceAmount = this.balanceDr.subtract(this.balanceCr);
  }

  public void post(List<JournalLineEntry> journalLineEntries) {
    journalLineEntries.forEach(x -> {
      addCr(x.getAccountedCr());
      addDr(x.getAccountedDr());
      x.markAsPosted();
    });
  }
}
