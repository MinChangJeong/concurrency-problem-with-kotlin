package com.example.myapp.repository

import com.example.myapp.domain.Event
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import jakarta.persistence.LockModeType

interface EventRepository : JpaRepository<Event, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select e from Event e where e.id = :id")
	fun findForUpdate(@Param("id") id: Long): Event?
}
