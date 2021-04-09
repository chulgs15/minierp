package com.financial.ledger.domain;

import com.financial.ledger.dto.JournalCrLineEntryVO;
import com.financial.ledger.dto.JournalDrLineEntryVO;
import com.financial.ledger.dto.JournalLineEntryVO;
import com.financial.ledger.exception.LedgerApplicationException;
import com.financial.ledger.exception.LedgerErrors;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
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
import javax.persistence.Transient;
import lombok.Builder;
import lombok.Getter;

@Entity
@SequenceGenerator(name = "gl_journal_header_s", sequenceName = "gl_journal_header_s", allocationSize = 1)
@Table(name = "gl_journal_header_all", indexes = {
    @Index(name = "gl_journal_header_u1", unique = true, columnList = "header_name")})
@Getter
public class JournalEntry {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "gl_journal_header_s")
  @Column(name = "journal_header_id")
  @Getter
  private Long journalId;

  @Column(name = "header_name")
  @GeneratedValue(generator = "JournalEntryNameGen")
  private String journalEntryName;

  @Column(name = "description")
  private String description;

  @Column(name = "status_code")
  @Enumerated(EnumType.STRING)
  private JournalEntryStatus status;

  @Embedded
  private ExchangeRate exchangeRate;

  @Transient
  private GLPeriod glPeriod;

  @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL)
  private List<JournalLineEntry> journalLineEntries = new ArrayList<>();

  public JournalEntry() {
    this.journalEntryName = "Journal " + System.nanoTime();
    this.status = JournalEntryStatus.NEW;
  }

  @Builder
  public JournalEntry(GLPeriod glPeriod, String description, ExchangeRate exchangeRate) {
    this();
    this.glPeriod = glPeriod;
    this.description = description;
    this.exchangeRate = exchangeRate;
  }

  public JournalEntry addCrLine(JournalCrLineEntryVO crDto) {
    _createAndAddJournalLine(crDto);
    return this;
  }

  public JournalEntry addDrLine(JournalDrLineEntryVO drDto) {
    _createAndAddJournalLine(drDto);
    return this;
  }

  private void _createAndAddJournalLine(JournalLineEntryVO dto) {
    if (this.status.isPosted()) {
      throw new LedgerApplicationException(LedgerErrors.LEDGER_00001);
    }
    this.journalLineEntries.add(new JournalLineEntry(this, dto));
  }

  public boolean isSameDrCrAmountInJournal() {
    BigDecimal totalEnterdCr = BigDecimal.ZERO;
    BigDecimal totalEnterdDr = BigDecimal.ZERO;
    BigDecimal totalAccountedCr = BigDecimal.ZERO;
    BigDecimal totalAccountedDr = BigDecimal.ZERO;

    for (JournalLineEntry journalLineEntry : journalLineEntries) {
      totalEnterdDr = totalEnterdDr.add(journalLineEntry.getEnteredDr());
      totalEnterdCr = totalEnterdCr.add(journalLineEntry.getEnteredCr());
      totalAccountedDr = totalAccountedDr.add(journalLineEntry.getAccountedDr());
      totalAccountedCr = totalAccountedCr.add(journalLineEntry.getAccountedCr());
    }

    return (totalEnterdDr.compareTo(totalEnterdCr) == 0) && (
        totalAccountedDr.compareTo(totalAccountedCr) == 0);
  }

  public void markAsPosted() {
    this.status = this.status == JournalEntryStatus.NEW ? JournalEntryStatus.POSTED : this.status;
  }

  public void reverse(LocalDate reverseDate) {
    if (!this.status.isPosted()) {
      throw new LedgerApplicationException(LedgerErrors.LEDGER_00004);
    }

    if (this.status.isReversed()) {
      throw new LedgerApplicationException(LedgerErrors.LEDGER_00005);
    }

    this.journalLineEntries.addAll(_createReverseJournalLines(reverseDate));

    markAsReversed();
  }

  private List<JournalLineEntry> _createReverseJournalLines(LocalDate accountingDate) {
    return this.journalLineEntries.stream()
        .map(x -> x.getNewReverseLine(accountingDate))
        .collect(Collectors.toList());
  }

  private void markAsReversed() {
    this.status = JournalEntryStatus.REVERSE;
  }

  public Map<GLPeriod, Map<FinancialAccount, List<JournalLineEntry>>> getUnpostedJournalLinesGroupByPeriodAndAccounts() {
    return this.journalLineEntries.stream()
        .filter(x -> x.getGlPeriod().isOpened() && !x.isPosted())
        .collect(Collectors.groupingBy(JournalLineEntry::getGlPeriod,
            Collectors
                .groupingBy(JournalLineEntry::getFinancialAccount, Collectors.toList())));
  }

  public enum JournalEntryStatus {
    NEW {
      @Override
      public boolean isPosted() {
        return false;
      }
    }, POSTED {

    }, REVERSE {
      @Override
      public boolean isReversed() {
        return true;
      }
    };

    public boolean isReversed() {
      return false;
    }

    public boolean isPosted() {
      return true;
    }
  }
}