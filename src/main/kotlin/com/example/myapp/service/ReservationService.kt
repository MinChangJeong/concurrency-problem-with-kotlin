package com.example.myapp.service

import com.example.myapp.domain.Event
import com.example.myapp.domain.Reservation
import com.example.myapp.repository.EventRepository
import com.example.myapp.repository.ReservationRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.random.Random
import org.springframework.transaction.annotation.Transactional

@Service
class ReservationService(
    private val eventRepository: EventRepository,
    private val reservationRepository: ReservationRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun reserve(eventId: Long, username: String): Boolean {
        // 비관적 락으로 이벤트 행 잠금
        val event = eventRepository.findForUpdate(eventId) ?: throw IllegalArgumentException("Event not found")

        val current = reservationRepository.countByEvent(event)
        if (current >= event.capacity) {
            log.debug("Capacity exceeded for event ${'$'}eventId (current=${'$'}current)")
            return false
        }

        // 기존 경쟁 유도 지연은 그대로 둠 (하지만 락 덕분에 초과는 방지됨)
        val extra = Random.nextLong(50, 250)
        Thread.sleep(150 + extra)

        reservationRepository.save(Reservation(event, username))
        log.debug("Reserved by ${'$'}username (event=${'$'}eventId)")
        return true
    }

    // 락을 사용하지 않는 버전 (경쟁 조건 재현용)
    @Transactional
    fun reserveWithoutLock(eventId: Long, username: String): Boolean {
        val event = eventRepository.findById(eventId).orElseThrow()
        val current = reservationRepository.countByEvent(event)
        if (current >= event.capacity) {
            return false
        }
        val extra = Random.nextLong(50, 250)
        Thread.sleep(150 + extra)
        reservationRepository.save(Reservation(event, username))
        return true
    }

    @Transactional(readOnly = true)
    fun count(eventId: Long): Long {
        val event = eventRepository.findById(eventId).orElseThrow()
        return reservationRepository.countByEvent(event)
    }

    @Transactional
    fun createEvent(name: String, capacity: Int): Event {
        return eventRepository.save(Event(name, capacity))
    }
}
