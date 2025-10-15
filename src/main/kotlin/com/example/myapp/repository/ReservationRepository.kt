package com.example.myapp.repository

import com.example.myapp.domain.Event
import com.example.myapp.domain.Reservation
import org.springframework.data.jpa.repository.JpaRepository

interface ReservationRepository : JpaRepository<Reservation, Long> {
    fun countByEvent(event: Event): Long
}
