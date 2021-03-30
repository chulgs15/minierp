package com.aidant.erp.domain;

import com.financial.ledger.domain.ExchangeRate;
import com.financial.ledger.domain.GLPeriod;
import com.financial.ledger.domain.IdClass.LedgerFXKey;
import com.financial.ledger.domain.JournalEntry;
import com.financial.ledger.domain.JournalLineEntry;
import com.financial.ledger.domain.LedgerForeignExchange;
import com.financial.ledger.dto.JournalCrLineEntryVO;
import com.financial.ledger.dto.JournalDrLineEntryVO;
import com.financial.ledger.enums.FinancialAccounts;
import com.financial.ledger.exception.LedgerApplicationException;
import com.financial.ledger.exception.LedgerErrors;
import com.financial.ledger.repository.GLPeriodRepository;
import com.financial.ledger.repository.JournalEntryRepository;
import com.financial.ledger.repository.JournalLineEntryRepository;
import com.financial.ledger.repository.LedgerForeignExchangeRepository;
import com.financial.ledger.repository.resultset.JournalDrCrTotalDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import javax.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

@DataJpaTest
@Rollback(value = false)
public class LedgerDomainTest {

  @Autowired
  private JournalEntryRepository journalEntryRepository;

  @Autowired
  private LedgerForeignExchangeRepository ledgerForeignExchangeRepo;

  @Autowired
  private JournalLineEntryRepository journalLineEntryRepository;

  @Autowired
  private GLPeriodRepository periodRepository;

  String periodName = null;
  GLPeriod glPeriod = null;


  @BeforeEach
  public void setupExchangeRateBeforeAllTest() {

    periodName = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    glPeriod = Optional.ofNullable(periodRepository.findByPeriodName(periodName))
        .orElse(new GLPeriod(periodName));

    glPeriod.markAsOpened();

    periodRepository.save(glPeriod);

    LedgerFXKey fxKey = LedgerFXKey.builder().exchangeDate(LocalDate.now()).fromCurrency("USD")
        .toCurrency(
            "KRW").build();

    LedgerForeignExchange exchangeRate = LedgerForeignExchange.builder().fxKey(fxKey).exchangeRate(
        new BigDecimal("1234.56")).build();

    ledgerForeignExchangeRepo.save(exchangeRate);
  }


  @Test
  @Transactional
  public void Journal_WhenJournalCreated_JournalNormallyCreated() {
    ExchangeRate exchangeRate = new ExchangeRate();

    JournalDrLineEntryVO dr = JournalDrLineEntryVO.builder()
        .financialAccounts(FinancialAccounts.E_0001).amount(
            new BigDecimal("1000")).build();

    JournalCrLineEntryVO cr = JournalCrLineEntryVO.builder()
        .financialAccounts(FinancialAccounts.A_0001).amount(
            new BigDecimal("1000")).build();

    JournalEntry journalEntry = JournalEntry.builder()
        .description("hello world")
        .exchangeRate(exchangeRate)
        .glPeriod(glPeriod)
        .build()
        .addDrLine(dr)
        .addCrLine(cr);

    journalEntryRepository.save(journalEntry);

    journalEntry = journalEntryRepository.findById(journalEntry.getJournalId()).orElseThrow(
        () -> new RuntimeException("에러"));

    List<JournalLineEntry> journalLineEntries = journalEntry.getJournalLineEntries();

    BigDecimal totalEnteredDr = BigDecimal.ZERO;
    for (JournalLineEntry journalLineEntry : journalLineEntries) {
      totalEnteredDr = totalEnteredDr.add(journalLineEntry.getEnteredDr());
    }

    Assertions.assertEquals(totalEnteredDr.compareTo(new BigDecimal("1000")), 0);

    BigDecimal totalAccountedDr = BigDecimal.ZERO;
    for (JournalLineEntry journalLineEntry : journalLineEntries) {
      totalAccountedDr = totalAccountedDr.add(journalLineEntry.getAccountedDr());
    }

    Assertions.assertEquals(totalAccountedDr.compareTo(new BigDecimal("1000")), 0);

    BigDecimal totalEnteredCr = BigDecimal.ZERO;
    for (JournalLineEntry journalLineEntry : journalLineEntries) {
      totalEnteredCr = totalEnteredCr.add(journalLineEntry.getEnteredCr());
    }

    Assertions.assertEquals(totalEnteredCr.compareTo(new BigDecimal("1000")), 0);

    BigDecimal totalAccountedCr = BigDecimal.ZERO;
    for (JournalLineEntry journalLineEntry : journalLineEntries) {
      totalAccountedCr = totalAccountedCr.add(journalLineEntry.getAccountedCr());
    }

    Assertions.assertEquals(totalAccountedCr.compareTo(new BigDecimal("1000")), 0);
  }

  @Test
  public void Journal_WhenJournalPost_AmountEqualJournal() {
    ExchangeRate exchangeRate = new ExchangeRate();

    JournalDrLineEntryVO dr = JournalDrLineEntryVO.builder()
        .financialAccounts(FinancialAccounts.E_0001).amount(
            new BigDecimal("1000")).build();

    JournalCrLineEntryVO cr = JournalCrLineEntryVO.builder()
        .financialAccounts(FinancialAccounts.A_0001).amount(
            new BigDecimal("1000")).build();

    JournalEntry journalEntry = JournalEntry.builder().description("hello world").glPeriod(glPeriod)
        .exchangeRate(
            exchangeRate).build().addDrLine(dr).addCrLine(cr);

    journalEntryRepository.save(journalEntry);

    journalEntry = journalEntryRepository.findById(journalEntry.getJournalId()).orElseThrow(
        () -> new RuntimeException("에러"));

    JournalDrCrTotalDto sumResult = journalLineEntryRepository.sumDrCrAmountByJournal(journalEntry);

    boolean result = (sumResult.getTotalEnteredDr().compareTo(
        sumResult.getTotalEnteredDr()) == 0 && sumResult.getTotalAccountedDr().compareTo(
        sumResult.getTotalAccountedCr()) == 0);

    Assertions.assertTrue(result);
  }



  @Test
  @Transactional
  public void Journal_WhenGLPeriodAndAccountingDateMisMatch_ErrorEccours() {
    try {
      ExchangeRate exchangeRate = new ExchangeRate();

      JournalDrLineEntryVO dr = JournalDrLineEntryVO.builder()
          .financialAccounts(FinancialAccounts.E_0001).amount(
              new BigDecimal("1000")).build();

      JournalCrLineEntryVO cr = JournalCrLineEntryVO.builder()
          .financialAccounts(FinancialAccounts.A_0001).amount(
              new BigDecimal("1000")).build();

      JournalEntry journalEntry = JournalEntry.builder().description("hello world").glPeriod(
          new GLPeriod("2000-01")).exchangeRate(exchangeRate).build().addDrLine(dr).addCrLine(cr);

      journalEntryRepository.save(journalEntry);

    } catch (LedgerApplicationException e) {
      Assertions.assertEquals(e.getCode(), LedgerErrors.LEDGER_00003);
    }
  }
}
