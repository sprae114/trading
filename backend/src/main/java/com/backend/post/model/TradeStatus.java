package com.backend.post.model;

public enum TradeStatus {
    SALE,       // 판매 중
    RESERVED,   // 예약 중
    SOLD_OUT,   // 판매 완료 (거래 완료)
    HIDDEN,      // 숨김 (게시글 비공개)
}
