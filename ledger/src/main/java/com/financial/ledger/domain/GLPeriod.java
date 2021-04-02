package com.financial.ledger.domain;

import com.financial.ledger.exception.LedgerApplicationException;
import com.financial.ledger.exception.LedgerErrors;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.ToString;

@Entity
@Table(name = "gl_periods", indexes = {@Index(name = "GL_PERIOD_U1", unique = true, columnList = "period_name")})
@SequenceGenerator(name = "gl_period_s", sequenceName = "gl_period_s", allocationSize = 1)
@Getter
@ToString
public class GLPeriod {

  @Id
  @GeneratedValue(generator = "gl_period_s", strategy = GenerationType.SEQUENCE)
  @Column(name = "gl_period_id")
  private Long periodId;

  @Column(name = "period_name")
  private String periodName;

  @Column(name = "period_start_date")
  private LocalDate periodStartDate;

  @Column(name = "period_end_date")
  private LocalDate periodEndDate;

  @Column(name = "status")
  @Enumerated(value = EnumType.STRING)
  private PeriodStatus status;

  @OneToMany(mappedBy = "glPeriod")
  private List<GLBalance> glBalances = new ArrayList<>();

  @OneToMany(mappedBy = "glPeriod")
  private List<JournalLineEntry> journalLineEntries = new ArrayList<>();

  public enum PeriodStatus {
    OPEN, CLOSE, NEVER_OPENED
  }

  public GLPeriod() {
  }

  public GLPeriod(String periodName) {
    _checkValidPeriodName(periodName);
    this.periodName = periodName;
    this.periodStartDate = LocalDate.parse(periodName + "-01", DateTimeFormatter.ISO_DATE);
    this.periodEndDate = this.periodStartDate.plusMonths(1).minusDays(1);
    this.status = PeriodStatus.NEVER_OPENED;
  }

  public boolean isOpened() {
    return PeriodStatus.OPEN == status;
  }

  public boolean isClosed() {
    return PeriodStatus.CLOSE == status;
  }

  public boolean isValidAccountingDate(LocalDate accountingDate) {
//    return accountingDate.isAfter(this.periodStartDate) && accountingDate.isBefore(this.periodEndDate);
    return (accountingDate.compareTo(this.periodStartDate) >= 0) &&
        (accountingDate.compareTo(this.periodEndDate) <= 0);
  }

  public void markAsOpened() {
    this.status = PeriodStatus.OPEN;
  }

  public void markAsClosed() {
    this.status = PeriodStatus.CLOSE;
  }

  private void _checkValidPeriodName(String periodName) {
    if (periodName == null || !periodName.matches("^(\\d{4}([-]\\d{2})?)?$")) {
      throw new LedgerApplicationException(LedgerErrors.LEDGER_00002);
    }
  }
}
