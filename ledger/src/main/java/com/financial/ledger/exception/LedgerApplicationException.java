package com.financial.ledger.exception;

import lombok.Getter;

@Getter
public class LedgerApplicationException extends RuntimeException {
  private LedgerErrors code;
  private String message;
  private String action;

  public LedgerApplicationException(LedgerErrors ledgerErrors) {
    super();
    this.code = ledgerErrors;
    this.message = ledgerErrors.getMessage();
    this.action = ledgerErrors.getAction();
  }
}
