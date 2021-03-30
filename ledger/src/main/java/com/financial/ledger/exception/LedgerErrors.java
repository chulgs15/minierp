package com.financial.ledger.exception;

import lombok.Getter;

@Getter
public enum LedgerErrors {
  LEDGER_00001("POSTED Journal cannot add Journal LIne", "Create Another Journal"),
  LEDGER_00002("Period Name Pattern is YYYY-MM", "Change Period Name."),
  LEDGER_00003("날짜와 Period 가 맞지 않습니다.", "Change Period Name."),
  LEDGER_00004("상태가 Post가 아니면 Reverse 할 수 없습니다.", "New 상태일 때는 삭제하셔야 합니다"),
  LEDGER_00005("2번 Reverse 할 수 없습니다.", "새로운 Journal 을 만드세요"),
  LEDGER_00006("차변 대변 금액이 일치하지 않습니다.", "차변과 대변금액 Total을 맞춰주세요."),
  LEDGER_00007("Period가 Open 상태가 아닙니다.", "먼저 Period를 Open하세요."),
  ;

  private String message;
  private String action;

  LedgerErrors(String message, String action) {
    this.message = message;
    this.action = action;
  }
}
