package com.financial.ledger.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum FinancialAccounts {
  A_0001("CASH", AccountType.ASSET),
  A_0002("PREPAY", AccountType.ASSET),
  E_0001("MEAL", AccountType.EXPENSE),
  Q_0001("CAPITAL", AccountType.EQUITY),
  ;

  private String AccountName;
  private AccountType accountType;

  FinancialAccounts(String accountName, AccountType accountType) {
    AccountName = accountName;
    this.accountType = accountType;
  }

  public List<FinancialAccounts> getFinancialAccountsByAccountType(FinancialAccounts.AccountType accountType) {
    return Arrays.stream(FinancialAccounts.values())
        .filter(x -> x.getAccountType().equals(accountType))
        .collect(Collectors.toList());
  }

  public List<FinancialAccounts> getFinancialAccountsByAccountName(String accountName) {
    return Arrays.stream(FinancialAccounts.values())
        .filter(x -> x.getAccountName().equals(accountName))
        .collect(Collectors.toList());
  }

  public enum AccountType {
    ASSET, EXPENSE, EQUITY, EXPENSES, REVENUE;
  }
}
