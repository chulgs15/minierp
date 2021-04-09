package com.financial.ledger.service;

import static com.financial.ledger.exception.LedgerErrors.LEDGER_00009;

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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
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
        // TODO: 오류로 바꿀 필요가 있음.
        .orElseThrow(() -> new LedgerApplicationException(LEDGER_00009));

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
        .orElseGet(() -> {
          GLPeriod tmpGLPeriod = glPeriodRepository.save(new GLPeriod(periodName));
          financialAccountsRepository.findAll()
              .stream().filter(x -> x.getEnabledFlag() == AccountEnabledFlag.YES)
              .forEach(x -> addFinacialAccount(tmpGLPeriod, x));
          return tmpGLPeriod;
        });

    _openNextPeriod(periodName);

    glPeriod.markAsOpened();
    return glPeriod;
  }

  private void _openNextPeriod(String periodName) {
    LocalDate nextPeriod = LocalDate
        .parse(periodName + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusMonths(1);
    String nextPeriodName = DateTimeFormatter.ofPattern("yyyy-MM").format(nextPeriod);

    Optional.ofNullable(glPeriodRepository.findByPeriodName(nextPeriodName))
        .orElseGet(() -> {
          GLPeriod tmpGLPeriod = glPeriodRepository.save(new GLPeriod(nextPeriodName));
          financialAccountsRepository.findAll()
              .stream().filter(x -> x.getEnabledFlag() == AccountEnabledFlag.YES)
              .forEach(x -> addFinacialAccount(tmpGLPeriod, x));
          return tmpGLPeriod;
        });
  }

  public GLBalance addFinacialAccount(GLPeriod glPeriod, FinancialAccount financialAccount) {
    GLBalance glBalance = Optional.ofNullable(glBalanceRepository
        .findByglPeriodAndFinancialAccount(glPeriod, financialAccount))
        .orElse(new GLBalance(glPeriod, financialAccount));

    return glBalanceRepository.save(glBalance);
  }

  public void closePeriod(GLPeriod glPeriod) {
    _postCurrentPeriodLinesByGLPeriod(glPeriod);
    _updateNextGLBalanceBeginAmount(glPeriod);
    glPeriod.markAsClosed();
  }

  private void _updateNextGLBalanceBeginAmount(GLPeriod glPeriod) {
    GLPeriod nextGLPeriod = glPeriodRepository.findByPeriodName(glPeriod.getNextPeriodName());

    List<GLBalance> glBalances = glBalanceRepository.findByglPeriod(glPeriod);
    for (GLBalance glBalance : glBalances) {
      GLBalance nextPeriodGLBalance = glBalanceRepository
          .findByglPeriodAndFinancialAccount(nextGLPeriod, glBalance.getFinancialAccount());

      nextPeriodGLBalance.addBeginDr(glBalance.getBalanceDr());
      nextPeriodGLBalance.addBeginCr(glBalance.getBalanceCr());

      glBalanceRepository.save(nextPeriodGLBalance);
    }
  }

  private void _postCurrentPeriodLinesByGLPeriod(GLPeriod glPeriod) {
    List<JournalEntry> unpostJournal = journalEntryRepository.findUnpostJournal(glPeriod);

    for (JournalEntry journalEntry : unpostJournal) {
      post(journalEntry);
    }
  }
}
