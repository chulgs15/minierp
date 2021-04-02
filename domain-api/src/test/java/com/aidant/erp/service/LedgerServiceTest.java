package com.aidant.erp.service;

import com.financial.ledger.domain.ExchangeRate;
import com.financial.ledger.domain.FinancialAccount;
import com.financial.ledger.domain.FinancialAccount.AccountType;
import com.financial.ledger.domain.GLBalance;
import com.financial.ledger.domain.GLPeriod;
import com.financial.ledger.domain.IdClass.LedgerFXKey;
import com.financial.ledger.domain.JournalEntry;
import com.financial.ledger.domain.JournalEntry.JournalEntryStatus;
import com.financial.ledger.domain.JournalLineEntry;
import com.financial.ledger.domain.LedgerForeignExchange;
import com.financial.ledger.dto.JournalCrLineEntryVO;
import com.financial.ledger.dto.JournalDrLineEntryVO;
import com.financial.ledger.repository.FinancialAccountsRepository;
import com.financial.ledger.repository.GLBalanceRepository;
import com.financial.ledger.repository.GLPeriodRepository;
import com.financial.ledger.repository.JournalEntryRepository;
import com.financial.ledger.repository.JournalLineEntryRepository;
import com.financial.ledger.repository.LedgerForeignExchangeRepository;
import com.financial.ledger.service.LedgerService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

@SpringBootTest
@Rollback(value = false)
public class LedgerServiceTest {

  @Autowired
  private LedgerService ledgerService;

  @Autowired
  private JournalEntryRepository journalEntryRepository;

  @Autowired
  private GLPeriodRepository glPeriodRepository;

  @Autowired
  private LedgerForeignExchangeRepository ledgerForeignExchangeRepo;

  @Autowired
  private JournalLineEntryRepository journalLineEntryRepository;

  @Autowired
  private GLBalanceRepository glBalanceRepository;

  @Autowired
  private FinancialAccountsRepository financialAccountsRepository;

  String periodName = null;
  GLPeriod glPeriod = null;

  @BeforeEach
  public void setupExchangeRateBeforeAllTest() {
    // 계정 추가
    FinancialAccount cash = FinancialAccount.builder()
        .accountCode("1000")
        .accountName("CASH")
        .accountType(AccountType.ASSET)
        .build();

    FinancialAccount expense = FinancialAccount.builder()
        .accountCode("2000")
        .accountName("MEAL")
        .accountType(AccountType.EXPENSE)
        .build();

    FinancialAccount equity = FinancialAccount.builder()
        .accountCode("3000")
        .accountName("EQUITY")
        .accountType(AccountType.EQUITY)
        .build();

    financialAccountsRepository.save(cash);
    financialAccountsRepository.save(expense);
    financialAccountsRepository.save(equity);

    periodName = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));

    glPeriod = ledgerService.openPeriod(periodName);

    LedgerFXKey fxKey = LedgerFXKey.builder().exchangeDate(LocalDate.now()).fromCurrency("USD")
        .toCurrency(
            "KRW").build();

    LedgerForeignExchange exchangeRate = LedgerForeignExchange.builder().fxKey(fxKey).exchangeRate(
        new BigDecimal("1234.56")).build();

    ledgerForeignExchangeRepo.save(exchangeRate);
  }

  private JournalEntry getJournalEntry() {
    ExchangeRate exchangeRate = new ExchangeRate();

    FinancialAccount cash = financialAccountsRepository.findById("1000")
        .orElseThrow();

    FinancialAccount expense = financialAccountsRepository.findById("2000")
        .orElseThrow();

    JournalDrLineEntryVO dr = JournalDrLineEntryVO.builder()
        .financialAccount(expense).amount(
            new BigDecimal("1000")).build();

    JournalCrLineEntryVO cr = JournalCrLineEntryVO.builder()
        .financialAccount(cash).amount(
            new BigDecimal("1000")).build();

    JournalEntry journalEntry = JournalEntry.builder().description("hello world")
        .glPeriod(glPeriod)
        .exchangeRate(
            exchangeRate).build().addDrLine(dr).addCrLine(cr);

    return journalEntryRepository.save(journalEntry);
  }

  @Test
  @Transactional
  public void GLPost_whenPostBySingleJournal_thenPostAmountApplied() {
    JournalEntry journalEntry = getJournalEntry();

    ledgerService.post(journalEntry);

    List<JournalLineEntry> journalLineEntries = journalEntry.getJournalLineEntries();
    Assertions.assertEquals(journalLineEntries.size(),
        journalLineEntries.stream().filter(JournalLineEntry::isPosted).count());
  }

  @Test
  @Transactional
  public void Journal_WhenReversJournal_AmountIsZero() {
    JournalEntry journalEntry = getJournalEntry();
    ledgerService.post(journalEntry);
    ledgerService.reverse(journalEntry, LocalDate.now());

    List<JournalLineEntry> journalLineEntries = journalEntry.getJournalLineEntries();

    BigDecimal totalEnteredDr = BigDecimal.ZERO;
    BigDecimal totalAccountedDr = BigDecimal.ZERO;
    BigDecimal totalEnteredCr = BigDecimal.ZERO;
    BigDecimal totalAccountedCr = BigDecimal.ZERO;

    for (JournalLineEntry journalLineEntry : journalLineEntries) {
      totalAccountedDr = totalAccountedDr.add(journalLineEntry.getAccountedDr());
      totalEnteredCr = totalEnteredCr.add(journalLineEntry.getEnteredCr());
      totalAccountedCr = totalAccountedCr.add(journalLineEntry.getAccountedCr());
      totalEnteredDr = totalEnteredDr.add(journalLineEntry.getEnteredDr());
    }

    Assertions.assertEquals(totalEnteredDr.compareTo(new BigDecimal("0")), 0);
    Assertions.assertEquals(totalAccountedDr.compareTo(new BigDecimal("0")), 0);
    Assertions.assertEquals(totalEnteredCr.compareTo(new BigDecimal("0")), 0);
    Assertions.assertEquals(totalAccountedCr.compareTo(new BigDecimal("0")), 0);
    Assertions.assertEquals(journalEntry.getStatus(), JournalEntryStatus.REVERSE);
  }


  @Test
  @Transactional
  public void GLPost_beforePostingIsCrDrSame_thenTrue() {
    JournalEntry journalEntry = getJournalEntry();
    Assertions.assertTrue(journalEntry.isSameDrCrAmountInJournal());
  }

  @Test
  @Transactional
  public void Journal_whenDrCrDifferent_errorOccured() {
    // NPE 를 피하기 위해서 사용.
    JournalEntry journalEntry = getJournalEntry();
    ledgerService.post(journalEntry);

    List<JournalLineEntry> postLines = journalLineEntryRepository.findPostedLines(glPeriod);

    Map<FinancialAccount, BigDecimal> totalCrByAccounts = postLines.stream()
        .collect(Collectors.groupingBy(JournalLineEntry::getFinancialAccount,
            Collectors
                .reducing(BigDecimal.ZERO, JournalLineEntry::getAccountedCr, BigDecimal::add)));

    Map<FinancialAccount, BigDecimal> totalDrByAccounts = postLines.stream()
        .collect(Collectors.groupingBy(JournalLineEntry::getFinancialAccount,
            Collectors
                .reducing(BigDecimal.ZERO, JournalLineEntry::getAccountedDr, BigDecimal::add)));

    for (FinancialAccount financialAccount : totalDrByAccounts.keySet()) {
      GLBalance balance = glBalanceRepository
          .findByglPeriodAndFinancialAccount(glPeriod, financialAccount);

      Assertions.assertEquals(balance.getPeriodDr()
              .compareTo(totalDrByAccounts.get(financialAccount)), 0,
          String.format("%s DR Amount is different", financialAccount));
    }

    for (FinancialAccount financialAccount : totalCrByAccounts.keySet()) {
      GLBalance balance = glBalanceRepository
          .findByglPeriodAndFinancialAccount(glPeriod, financialAccount);

      Assertions.assertEquals(balance.getPeriodCr()
              .compareTo(totalCrByAccounts.get(financialAccount)), 0,
          String.format("%s CR Amount is different", financialAccount));
    }
  }

  @Test
  @Transactional
  public void Journal_whenPeriodClose_closeOk() {
    for (int i = 0; i < 10; i++) {
      getJournalEntry();
    }

    GLPeriod nextGLPeriod = ledgerService.openPeriod("2021-05");
    glPeriodRepository.save(nextGLPeriod);

    for (int i = 0; i < 10; i++) {

      ExchangeRate exchangeRate = new ExchangeRate();

      FinancialAccount cash = financialAccountsRepository.findById("1000")
          .orElseThrow();

      FinancialAccount expense = financialAccountsRepository.findById("2000")
          .orElseThrow();

      JournalDrLineEntryVO dr = JournalDrLineEntryVO.builder()
          .accountingDate(LocalDate.of(2021, 5, 1))
          .financialAccount(expense).amount(
              new BigDecimal("1000")).build();

      JournalCrLineEntryVO cr = JournalCrLineEntryVO.builder()
          .accountingDate(LocalDate.of(2021, 5, 1))
          .financialAccount(cash).amount(
              new BigDecimal("1000")).build();

      JournalEntry journalEntry = JournalEntry.builder().description("hello world")
          .glPeriod(nextGLPeriod)
          .exchangeRate(
              exchangeRate).build().addDrLine(dr).addCrLine(cr);

      journalEntryRepository.save(journalEntry);
    }

    ledgerService.closePeriod(this.glPeriod);

    List<JournalLineEntry> unpostedLines = journalLineEntryRepository
        .findUnpostedLines(this.glPeriod);

    Assertions.assertEquals(unpostedLines.size(), 0);
    Assertions.assertTrue(this.glPeriod.isClosed());
  }

}
