package com.example.statemachine.simple.document

enum class DocumentEvent {
  DOCUMENT_PUBLISHED_BY_USER, // 일반 유저가 문서를 개시함.
  DOCUMENT_PUBLISHED_BY_ADMINISTRATOR, // 관리자 유저가 문서를 게시함.
  APPROVED_DOCUMENT_IS_REVEALED_BY_ADMINISTRATOR, // 관리자 유저가, 공개 승인받은 문서를 게시함.
  NEEDS_TO_BE_ADJUSTED_OR_REJECTED, // 문서가 (수정이 필요하거나, 승인 규정상 문제가 있는 등의 사유로) 개시 승인을 받지 못함.
  APPROVED, // 문서가 게시 승인됨.
  EXPIRED // 공개 기간 만료
}
