package com.example.myapp

import com.example.myapp.service.ReservationService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.junit.jupiter.api.extension.ExtendWith
import com.example.myapp.support.TimingExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest
@ExtendWith(TimingExtension::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationConcurrencyTest {

    @Autowired
    lateinit var reservationService: ReservationService

    @Test
    fun `비관적 락 적용 시 초과 예약 방지`() {
        val capacity = 5
        val event = reservationService.createEvent("Concert", capacity = capacity)

        val threads = 40
        val rounds = 3 // 라운드 반복으로 레이스 재현 확률 증가
    var overbooked = false

    val successUsers = java.util.concurrent.ConcurrentLinkedQueue<String>()
    val failUsers = java.util.concurrent.ConcurrentLinkedQueue<String>()

    repeat(rounds) { round ->
            val pool = Executors.newFixedThreadPool(threads)
            val startLatch = CountDownLatch(1)
            val doneLatch = CountDownLatch(threads)
            repeat(threads) { idx ->
                pool.submit {
                    try {
                        startLatch.await()
                        val user = "lock-r${'$'}round-u${'$'}idx"
                        val success = reservationService.reserve(event.id!!, user)
                        if (success) successUsers += user else failUsers += user
                    } finally {
                        doneLatch.countDown()
                    }
                }
            }
            startLatch.countDown()
            doneLatch.await()
            pool.shutdown()
            val countNow = reservationService.count(event.id!!)
                println("[round=$round] 현재 예약 수 = $countNow / capacity=$capacity"); System.out.flush()
            if (countNow > capacity) {
                overbooked = true // 비관적 락이 제대로라면 발생하지 않아야 함
            }
        }

        val finalCount = reservationService.count(event.id!!)
    println("[Locked] 성공=${successUsers.size} 실패=${failUsers.size} 최종=$finalCount 정원=$capacity"); System.out.flush()
    assertThat(overbooked).describedAs("비관적 락 사용 시 정원 초과가 발생하면 안 됩니다.").isFalse
    assertThat(finalCount).isLessThanOrEqualTo(capacity.toLong())
    }
}
