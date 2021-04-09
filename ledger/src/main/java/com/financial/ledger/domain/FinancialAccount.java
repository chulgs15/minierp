package com.financial.ledger.domain;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(name = "gl_financial_accounts")
@Getter
public class FinancialAccount {

  @Id
  @Column(name = "acct_code")
  private String accountCode;

  @Column(name = "acct_name")
  private String accountName;

  @Column(name = "enabled_flag")
  @Enumerated(EnumType.STRING)
  private AccountEnabledFlag enabledFlag = AccountEnabledFlag.YES;

  @Column(name = "acct_type")
  @Enumerated(EnumType.STRING)
  private AccountType accountType;

  @OneToMany(mappedBy = "financialAccount")
  private List<GLBalance> glBalances = new ArrayList<>();

  public FinancialAccount() {
  }

  @Builder
  public FinancialAccount(String accountCode, String accountName,
      AccountType accountType) {
    this.accountCode = accountCode;
    this.accountName = accountName;
    this.accountType = accountType;
  }

  public enum AccountEnabledFlag {
    YES, NO
  }

  public enum AccountType {
    ASSET, EXPENSE, EQUITY, EXPENSES, REVENUE;
  }
}
