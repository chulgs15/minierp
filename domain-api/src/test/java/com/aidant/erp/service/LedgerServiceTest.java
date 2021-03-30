package com.aidant.erp.service;

import com.financial.ledger.domain.ExchangeRate;
import com.financial.ledger.domain.GLBalance;
import com.financial.ledger.domain.GLPeriod;
import com.financial.ledger.domain.IdClass.LedgerFXKey;
import com.financial.ledger.domain.JournalEntry;
import com.financial.ledger.domain.JournalEntry.JournalEntryStatus;
import com.financial.ledger.domain.JournalLineEntry;
import com.financial.ledger.domain.LedgerForeignExchange;
import com.financial.ledger.dto.JournalCrLineEntryVO;
import com.financial.ledger.dto.JournalDrLineEntryVO;
import com.financial.ledger.enums.FinancialAccounts;
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
import java.util.Optional;
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

  String periodName = null;
  GLPeriod glPeriod = null;

  @BeforeEach
  public void setupExchangeRateBeforeAllTest() {
    periodName = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    glPeriod = Optional.ofNullable(glPeriodRepository.findByPeriodName(periodName)).orElse(
        new GLPeriod(periodName));

    glPeriod.markAsOpened();

    glPeriodRepository.save(glPeriod);

    LedgerFXKey fxKey = LedgerFXKey.builder().exchangeDate(LocalDate.now()).fromCurrency("USD")
        .toCurrency(
            "KRW").build();

    LedgerForeignExchange exchangeRate = LedgerForeignExchange.builder().fxKey(fxKey).exchangeRate(
        new BigDecimal("1234.56")).build();

    ledgerForeignExchangeRepo.save(exchangeRate);
  }

  private JournalEntry getJournalEntry() {
    ExchangeRate exchangeRate = new ExchangeRate();

    JournalDrLineEntryVO dr = JournalDrLineEntryVO.builder()
        .financialAccounts(FinancialAccounts.E_0001).amount(
            new BigDecimal("1000")).build();

    JournalCrLineEntryVO cr = JournalCrLineEntryVO.builder()
        .financialAccounts(FinancialAccounts.A_0001).amount(
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

    journalEntry.reverse(LocalDate.now());

    List<JournalLineEntry> journalLineEntries = journalEntry.getJournalLineEntries();

    BigDecimal totalEnteredDr = BigDecimal.ZERO;
    for (JournalLineEntry journalLineEntry : journalLineEntries) {
      totalEnteredDr = totalEnteredDr.add(journalLineEntry.getEnteredDr());
    }

    Assertions.assertEquals(totalEnteredDr.compareTo(new BigDecimal("0")), 0);

    BigDecimal totalAccountedDr = BigDecimal.ZERO;
    for (JournalLineEntry journalLineEntry : journalLineEntries) {
      totalAccountedDr = totalAccountedDr.add(journalLineEntry.getAccountedDr());
    }

    Assertions.assertEquals(totalAccountedDr.compareTo(new BigDecimal("0")), 0);

    BigDecimal totalEnteredCr = BigDecimal.ZERO;
    for (JournalLineEntry journalLineEntry : journalLineEntries) {
      totalEnteredCr = totalEnteredCr.add(journalLineEntry.getEnteredCr());
    }

    Assertions.assertEquals(totalEnteredCr.compareTo(new BigDecimal("0")), 0);

    BigDecimal totalAccountedCr = BigDecimal.ZERO;
    for (JournalLineEntry journalLineEntry : journalLineEntries) {
      totalAccountedCr = totalAccountedCr.add(journalLineEntry.getAccountedCr());
    }

    Assertions.assertEquals(totalAccountedCr.compareTo(new BigDecimal("0")), 0);

    Assertions.assertEquals(journalEntry.getStatus(), JournalEntryStatus.REVERSE);
  }


  @Test
  @Transactional
  public void GLPost_beforePostingIsCrDrSame_thenTrue() {
    ExchangeRate exchangeRate = new ExchangeRate();

    JournalDrLineEntryVO dr = JournalDrLineEntryVO.builder()
        .financialAccounts(FinancialAccounts.E_0001).amount(
            new BigDecimal("1000")).build();

    JournalCrLineEntryVO cr = JournalCrLineEntryVO.builder()
        .financialAccounts(FinancialAccounts.A_0001).amount(
            new BigDecimal("1000")).build();

    JournalEntry journalEntry = JournalEntry.builder().description("hello world").exchangeRate(
        exchangeRate).glPeriod(glPeriod).build().addDrLine(dr).addCrLine(cr);

    journalEntryRepository.save(journalEntry);

    journalEntry = journalEntryRepository.findById(journalEntry.getJournalId()).orElseThrow(
        () -> new RuntimeException("에러"));

    Assertions.assertTrue(ledgerService.isSameDrCrAmountInJournal(journalEntry));
  }

  @Test
  @Transactional
  public void Journal_whenDrCrDifferent_errorOccured() {
    // NPE 를 피하기 위해서 사용.
    JournalEntry journalEntry = getJournalEntry();
    ledgerService.post(journalEntry);

    List<JournalLineEntry> postLines = journalLineEntryRepository.findPostedLines(glPeriod);

    Map<FinancialAccounts, BigDecimal> totalCrByAccounts = postLines.stream()
        .collect(Collectors.groupingBy(JournalLineEntry::getFinancialAccounts,
            Collectors
                .reducing(BigDecimal.ZERO, JournalLineEntry::getAccountedCr, BigDecimal::add)));

    Map<FinancialAccounts, BigDecimal> totalDrByAccounts = postLines.stream()
        .collect(Collectors.groupingBy(JournalLineEntry::getFinancialAccounts,
            Collectors
                .reducing(BigDecimal.ZERO, JournalLineEntry::getAccountedDr, BigDecimal::add)));

    for (FinancialAccounts financialAccounts : totalDrByAccounts.keySet()) {
      GLBalance balance = glBalanceRepository
          .findByglPeriodAndFinancialAccounts(glPeriod, financialAccounts);

      Assertions.assertEquals(balance.getPeriodDr()
              .compareTo(totalDrByAccounts.get(financialAccounts)), 0,
          String.format("%s DR Amount is different", financialAccounts));
    }

    for (FinancialAccounts financialAccounts : totalCrByAccounts.keySet()) {
      GLBalance balance = glBalanceRepository
          .findByglPeriodAndFinancialAccounts(glPeriod, financialAccounts);

      Assertions.assertEquals(balance.getPeriodCr()
              .compareTo(totalCrByAccounts.get(financialAccounts)), 0,
          String.format("%s CR Amount is different", financialAccounts));
    }
  }

  @Test
  @Transactional
  public void Journal_whenPeriodClose_closeOk() {
    for (int i = 0; i < 10; i++) {
      JournalEntry journalEntry = getJournalEntry();
    }

    String periodName = "2021-04";
    GLPeriod glPeriod = Optional.ofNullable(glPeriodRepository.findByPeriodName(periodName)).orElse(
        new GLPeriod(periodName));

    glPeriod.markAsOpened();

    glPeriodRepository.save(glPeriod);


    for (int i = 0; i < 10; i++) {
      ExchangeRate exchangeRate = new ExchangeRate();

      JournalDrLineEntryVO dr = JournalDrLineEntryVO.builder()
          .accountingDate(LocalDate.of(2021, 4, 1))
          .financialAccounts(FinancialAccounts.E_0001).amount(
              new BigDecimal("1000")).build();

      JournalCrLineEntryVO cr = JournalCrLineEntryVO.builder()
          .accountingDate(LocalDate.of(2021, 4, 1))
          .financialAccounts(FinancialAccounts.A_0001).amount(
              new BigDecimal("1000")).build();

      JournalEntry journalEntry = JournalEntry.builder().description("hello world")
          .glPeriod(glPeriod)
          .exchangeRate(
              exchangeRate).build().addDrLine(dr).addCrLine(cr);

      journalEntryRepository.save(journalEntry);
    }


    ledgerService.closePeriod(this.glPeriod);
  }

}
