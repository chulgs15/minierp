package com.financial.ledger.service;

import com.financial.ledger.domain.GLBalance;
import com.financial.ledger.domain.GLPeriod;
import com.financial.ledger.domain.JournalEntry;
import com.financial.ledger.domain.JournalLineEntry;
import com.financial.ledger.enums.FinancialAccounts;
import com.financial.ledger.exception.LedgerApplicationException;
import com.financial.ledger.exception.LedgerErrors;
import com.financial.ledger.repository.GLBalanceRepository;
import com.financial.ledger.repository.GLPeriodRepository;
import com.financial.ledger.repository.JournalEntryRepository;
import com.financial.ledger.repository.JournalLineEntryRepository;
import com.financial.ledger.repository.resultset.JournalDrCrTotalDto;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LedgerService {

  private final JournalEntryRepository journalEntryRepository;
  private final JournalLineEntryRepository journalLineEntryRepository;
  private final GLPeriodRepository glPeriodRepository;
  private final GLBalanceRepository glBalanceRepository;

  public boolean isSameDrCrAmountInJournal(JournalEntry journalEntry) {
    JournalDrCrTotalDto result = journalLineEntryRepository.sumDrCrAmountByJournal(journalEntry);
    return (result.getTotalEnteredDr().compareTo(
        result.getTotalEnteredCr()) == 0) && (result.getTotalAccountedDr().compareTo(
        result.getTotalAccountedCr()) == 0);
  }

  public void post(JournalEntry journalEntry) {
    if (!isSameDrCrAmountInJournal(journalEntry)) {
      throw new LedgerApplicationException(LedgerErrors.LEDGER_00006);
    }

    List<JournalLineEntry> unpostLinesByJournal = journalLineEntryRepository
        .findUnpostLinesByJournal(journalEntry);
    _postJournalLineEntries(unpostLinesByJournal);
    journalEntry.markAsPosted();
  }

  public void reverse(JournalEntry journalEntry, LocalDate accountingDate) {
    GLPeriod glPeriod =
        Optional.of(glPeriodRepository
            .findByPeriodName(DateTimeFormatter.ofPattern("yyyy-MM").format(accountingDate)))
            .orElseThrow(() -> new LedgerApplicationException(LedgerErrors.LEDGER_00007));

      if (!glPeriod.isOpened()) {
          throw new LedgerApplicationException(LedgerErrors.LEDGER_00007);
      }

      journalEntry.reverse(accountingDate);
  }

  public void closePeriod(GLPeriod glPeriod) {
    List<JournalEntry> unpostJournal = journalEntryRepository.findUnpostJournal(glPeriod);

    for (JournalEntry journalEntry : unpostJournal) {
      post(journalEntry);
    }

    glPeriod.markAsClosed();
  }


  private void _postJournalLineEntries(List<JournalLineEntry> unpostLinesByJournal) {
    Map<GLPeriod, Map<FinancialAccounts, List<JournalLineEntry>>> GroupByPeriodAndAccountUnpostLines =
        unpostLinesByJournal.stream()
            .filter(x -> x.getGlPeriod().isOpened())
            .collect(Collectors.groupingBy(JournalLineEntry::getGlPeriod,
                Collectors
                    .groupingBy(JournalLineEntry::getFinancialAccounts, Collectors.toList())));

    for (GLPeriod glPeriod : GroupByPeriodAndAccountUnpostLines.keySet()) {
      Map<FinancialAccounts, List<JournalLineEntry>> groupByFinancialAccounts =
          GroupByPeriodAndAccountUnpostLines.get(glPeriod);

      for (FinancialAccounts financialAccounts : groupByFinancialAccounts.keySet()) {
        // GL Balance 를 조회한다.
        GLBalance glBalance = Optional.ofNullable(
            glBalanceRepository.findByglPeriodAndFinancialAccounts(glPeriod, financialAccounts))
            .orElse(
                new GLBalance(glPeriod, financialAccounts));

        List<JournalLineEntry> journalLineEntries = groupByFinancialAccounts.get(financialAccounts);
        journalLineEntries.forEach(x -> {
          glBalance.addCr(x.getAccountedCr());
          glBalance.addDr(x.getAccountedDr());
          x.markAsPosted();
        });

        glBalanceRepository.save(glBalance);
      }
    }
  }
}
