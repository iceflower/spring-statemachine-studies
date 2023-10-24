package com.example.statemachine.simple.document.config

import com.example.statemachine.simple.document.config.exception.PersistenceDataIsNotExistException
import org.springframework.jdbc.core.JdbcTemplate

class DocumentIdChecker(private val jdbcTemplate: JdbcTemplate) {

  fun isExist(documentId: Int): Boolean {
    val cnt = jdbcTemplate.queryForObject(
      "SELECT COUNT(id) AS cnt FROM documents WHERE id = ?",
      Int::class.java,
      documentId
    ).toInt()


    if (cnt != 1) {
      throw PersistenceDataIsNotExistException(
        "주어진 documentId '$documentId' 는 데이터베이스에 존재하지 않는 값입니다"
      )
    }
    return true
  }
}
