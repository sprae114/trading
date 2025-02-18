package com.backend.post.model;

public enum TradeStatus {
    SALE,       // 판매 중
    RESERVED,   // 예약 중
    SOLD_OUT,   // 판매 완료 (거래 완료)
    CANCELED,   // 판매 취소 (또는 거래 취소)
    HIDDEN,      // 숨김 (게시글 비공개)
    REPORTED    // 신고됨
}
