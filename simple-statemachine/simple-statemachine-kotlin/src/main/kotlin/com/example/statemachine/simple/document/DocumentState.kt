package com.example.statemachine.simple.document

enum class DocumentState {
  DRAFT, // 초안.
  UNDER_MEDIATION, // 검토 혹은 조정 진행중.
  WAITING_FOR_PUBLIC_DISCLOSURE, // 개시 대기중.
  PUBLIC_DISCLOSURE // 대외 공개 진행중.
}
