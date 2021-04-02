package com.financial.ledger.service;

import com.financial.ledger.domain.FinancialAccount;
import com.financial.ledger.domain.FinancialAccount.AccountEnabledFlag;
import com.financial.ledger.domain.GLBalance;
import com.financial.ledger.domain.GLPeriod;
import com.financial.ledger.domain.JournalEntry;
import com.financial.ledger.exception.LedgerApplicationException;
import com.financial.ledger.exception.LedgerErrors;
import com.financial.ledger.repository.FinancialAccountsRepository;
import com.financial.ledger.repository.GLBalanceRepository;
import com.financial.ledger.repository.GLPeriodRepository;
import com.financial.ledger.repository.JournalEntryRepository;
import com.financial.ledger.repository.JournalLineEntryRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LedgerService {

  private final JournalEntryRepository journalEntryRepository;
  private final GLPeriodRepository glPeriodRepository;
  private final GLBalanceRepository glBalanceRepository;
  private final FinancialAccountsRepository financialAccountsRepository;

  public void post(JournalEntry journalEntry) {
    if (!journalEntry.isSameDrCrAmountInJournal()) {
      throw new LedgerApplicationException(LedgerErrors.LEDGER_00006);
    }

    var unpostedJournalLinesGroupByPeriodAndAccounts
        = Optional.ofNullable(journalEntry.getUnpostedJournalLinesGroupByPeriodAndAccounts())
        .orElseThrow(() -> new RuntimeException("Posting 대상이 없습니다."));

    unpostedJournalLinesGroupByPeriodAndAccounts.keySet().stream()
        .flatMap(x -> unpostedJournalLinesGroupByPeriodAndAccounts.get(x).keySet().stream()
            .map(y -> glBalanceRepository.findByglPeriodAndFinancialAccount(x, y)))
        .forEach(glBalance -> glBalance.post(unpostedJournalLinesGroupByPeriodAndAccounts
            .get(glBalance.getGlPeriod()).get(glBalance.getFinancialAccount())));

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

  public GLPeriod openPeriod(String periodName) {
    GLPeriod glPeriod = Optional.ofNullable(glPeriodRepository.findByPeriodName(periodName))
        .orElseGet(()-> {
          GLPeriod tmpGLPeriod = glPeriodRepository.save(new GLPeriod(periodName));
          financialAccountsRepository.findAll()
              .stream().filter(x -> x.getEnabledFlag() == AccountEnabledFlag.YES)
              .forEach(x -> addFinacialAccount(tmpGLPeriod, x));
          return tmpGLPeriod;
        });

    glPeriod.markAsOpened();
    return glPeriod;
  }

  public GLBalance addFinacialAccount(GLPeriod glPeriod, FinancialAccount financialAccount) {
    GLBalance glBalance = Optional.ofNullable(glBalanceRepository
        .findByglPeriodAndFinancialAccount(glPeriod, financialAccount))
        .orElse(new GLBalance(glPeriod, financialAccount));

    return glBalanceRepository.save(glBalance);
  }

  public void closePeriod(GLPeriod glPeriod) {
    List<JournalEntry> unpostJournal = journalEntryRepository.findUnpostJournal(glPeriod);

    for (JournalEntry journalEntry : unpostJournal) {
      post(journalEntry);
    }

    glPeriod.markAsClosed();
  }
}
