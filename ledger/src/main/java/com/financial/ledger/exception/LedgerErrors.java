package com.financial.ledger.exception;

import lombok.Getter;

@Getter
public enum LedgerErrors {
  LEDGER_00001("POSTED Journal cannot add Journal LIne", "Create Another Journal"),
  LEDGER_00002("Period Name Pattern is YYYY-MM", "Change Period Name."),
  LEDGER_00003("The date and Period do not match.", "Change Period or Accounting Date"),
  LEDGER_00004("You cannot reverse unless the status is POST.", "You have to delete it when it's NEW."),
  LEDGER_00005("You cannot reverse twice.", "Create new Journal"),
  LEDGER_00006("The amount does not match the debit and credit.", "Please match the debit and credit total."),
  LEDGER_00007("Period is not in Open.", "Open Period first."),
  LEDGER_00008("Invalid debit side type specified.", "Use enum DrCrDeliminatodkr."),
  ;

  private String message;
  private String action;

  LedgerErrors(String message, String action) {
    this.message = message;
    this.action = action;
  }
}
