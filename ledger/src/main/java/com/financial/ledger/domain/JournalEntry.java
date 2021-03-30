package com.financial.ledger.domain;

import com.financial.ledger.dto.JournalCrLineEntryVO;
import com.financial.ledger.dto.JournalDrLineEntryVO;
import com.financial.ledger.dto.JournalLineEntryVO;
import com.financial.ledger.exception.LedgerApplicationException;
import com.financial.ledger.exception.LedgerErrors;
import java.time.LocalDate;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@SequenceGenerator(name = "gl_journal_header_s", sequenceName = "gl_journal_header_s", allocationSize = 1)
@Table(name = "gl_journal_header_all", indexes = {@Index(name = "gl_journal_header_u1", unique = true, columnList = "header_name")})
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

  public JournalEntry() {
    this.journalEntryName = "Journal " + System.nanoTime();
    this.status = JournalEntryStatus.NEW;
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

    public boolean isReversed() {return false;}

    public boolean isPosted() {
      return true;
    }
  }

  @Builder
  public JournalEntry(GLPeriod glPeriod, String description, ExchangeRate exchangeRate) {
    this();
    this.glPeriod = glPeriod;
    this.description = description;
    this.exchangeRate = exchangeRate;
  }

  public void markAsPosted() {
    this.status = this.status == JournalEntryStatus.NEW ? JournalEntryStatus.POSTED : this.status;
  }

  private void markAsReversed() {
    this.status = JournalEntryStatus.REVERSE;
  }

  public void reverse(LocalDate accountingDate) {
    if (!this.status.isPosted()) {
      throw new LedgerApplicationException(LedgerErrors.LEDGER_00004);
    }

    if (this.status.isReversed()) {
      throw new LedgerApplicationException(LedgerErrors.LEDGER_00005);
    }

    this.journalLineEntries.addAll(_createReverseJournalLines());

    markAsReversed();
  }

  private List<JournalLineEntry> _createReverseJournalLines() {
    return this.journalLineEntries.stream()
        .map(JournalLineEntry::getNewReverseLine)
        .collect(Collectors.toList());
  }
}