package com.financial.ledger.domain;


import com.financial.ledger.dto.JournalLineEntryVO;
import com.financial.ledger.exception.LedgerApplicationException;
import com.financial.ledger.exception.LedgerErrors;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;

@Entity
@SequenceGenerator(name = "gl_journal_line_s", sequenceName = "gl_journal_line_s", allocationSize = 1)
@Table(name = "gl_journal_line_all")
public class JournalLineEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gl_journal_line_s")
  @Column(name = "journal_line_id")
  @Getter
  private Long journalLineId;

  @ManyToOne
  @JoinColumn(name = "journal_header_id")
  @Getter
  private JournalEntry journalEntry;

  @ManyToOne
  @JoinColumn(name = "gl_period_id")
  @Getter
  private GLPeriod glPeriod;

  @Embedded
  @Getter
  private AccountingAmount accountingAmount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "acct_code")
  @Getter
  private FinancialAccount financialAccount;

  @Column(name = "entered_dr")
  private BigDecimal enteredDr;

  @Column(name = "entered_cr")
  private BigDecimal enteredCr;

  @Column(name = "accounted_dr")
  private BigDecimal accountedDr;

  @Column(name = "accounted_cr")
  private BigDecimal accountedCr;

  @Enumerated(EnumType.STRING)
  @Column(name = "post_flag")
  private PostFlag postFlag = PostFlag.NEW;

  @Column(name = "accounting_date")
  @Getter
  private LocalDate accountingDate;

  public JournalLineEntry() {
  }

  public JournalLineEntry(JournalEntry journalEntry, JournalLineEntryVO dto) {
    this.journalEntry = journalEntry;
    this.glPeriod = journalEntry.getGlPeriod();

    if (!this.glPeriod.isValidAccountingDate(dto.getAccountingDate())) {
      throw new LedgerApplicationException(LedgerErrors.LEDGER_00003);
    }

    this.accountingDate = dto.getAccountingDate();
    this.accountingAmount = new AccountingAmount(this.journalEntry.getExchangeRate(),
        dto.getAmount());
    this.financialAccount = dto.getFinancialAccount();
    setDrCrAmount(dto.getDeliminator());
  }

  private JournalLineEntry(JournalEntry journalEntry, GLPeriod glPeriod,
      AccountingAmount accountingAmount,
      FinancialAccount financialAccount, BigDecimal enteredDr, BigDecimal enteredCr,
      BigDecimal accountedDr, BigDecimal accountedCr, PostFlag postFlag,
      LocalDate accountingDate) {
    this.journalEntry = journalEntry;
    this.glPeriod = glPeriod;
    this.accountingAmount = accountingAmount;
    this.financialAccount = financialAccount;
    this.enteredDr = enteredDr;
    this.enteredCr = enteredCr;
    this.accountedDr = accountedDr;
    this.accountedCr = accountedCr;
    this.postFlag = postFlag;
    this.accountingDate = accountingDate;
  }

  private void setDrCrAmount(DrCrDeliminator deliminator) {
    if (deliminator.equals(DrCrDeliminator.DR)) {
      this.enteredDr = this.accountingAmount.getAmount();
      this.accountedDr = this.accountingAmount.getFunctionalAmount();
    } else if (deliminator.equals(DrCrDeliminator.CR)) {
      this.enteredCr = this.accountingAmount.getAmount();
      this.accountedCr = this.accountingAmount.getFunctionalAmount();
    } else {
      throw new LedgerApplicationException(LedgerErrors.LEDGER_00008);
    }
  }

  public BigDecimal getEnteredDr() {
    return Optional.ofNullable(enteredDr).orElse(BigDecimal.ZERO);
  }

  public BigDecimal getEnteredCr() {
    return Optional.ofNullable(enteredCr).orElse(BigDecimal.ZERO);
  }

  public BigDecimal getAccountedDr() {
    return Optional.ofNullable(accountedDr).orElse(BigDecimal.ZERO);
  }

  public BigDecimal getAccountedCr() {
    return Optional.ofNullable(accountedCr).orElse(BigDecimal.ZERO);
  }

  public void markAsPosted() {
    this.postFlag = PostFlag.POSTED;
  }

  public boolean isPosted() {
    return this.postFlag.equals(PostFlag.POSTED);
  }

  JournalLineEntry getNewReverseLine(LocalDate accountingDate) {
    BigDecimal enteredDr = this.enteredDr == null ? null : this.enteredDr.negate();
    BigDecimal enteredCr = this.enteredCr == null ? null : this.enteredCr.negate();
    BigDecimal accountedDr = this.accountedDr == null ? null : this.accountedDr.negate();
    BigDecimal accountedCr = this.accountedCr == null ? null : this.accountedCr.negate();
    accountingDate = accountingDate == null ? LocalDate.now() : accountingDate;

    return new JournalLineEntry(
        this.journalEntry,
        this.glPeriod,
        this.accountingAmount.getNegateAmount(),
        this.financialAccount,
        enteredDr,
        enteredCr,
        accountedDr,
        accountedCr,
        PostFlag.NEW,
        accountingDate
    );
  }

  public enum DrCrDeliminator {
    DR, CR
  }

  public enum PostFlag {
    POSTED, NEW
  }
}
