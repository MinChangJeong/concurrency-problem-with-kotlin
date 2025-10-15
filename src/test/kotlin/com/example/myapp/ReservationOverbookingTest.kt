package com.example.myapp

import com.example.myapp.service.ReservationService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import com.example.myapp.support.TimingExtension
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@ExtendWith(TimingExtension::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ReservationOverbookingTest {

    @Autowired
    lateinit var reservationService: ReservationService

    @Test
    fun `락 미사용 시 과잉 예약 발생`() {
        val capacity = 5
        val event = reservationService.createEvent("Concert-NoLock", capacity)

        val threads = 40
        val rounds = 2
        val pool = Executors.newFixedThreadPool(threads)
        val startLatch = CountDownLatch(1)
        val successUsers = ConcurrentLinkedQueue<String>()
        val failUsers = ConcurrentLinkedQueue<String>()

        repeat(rounds) { round ->
            repeat(threads) { idx ->
                pool.submit {
                    val user = "nolock-r${'$'}round-u${'$'}idx"
                    try {
                        startLatch.await()
                        val ok = reservationService.reserveWithoutLock(event.id!!, user)
                        if (ok) successUsers += user else failUsers += user
                    } catch (e: InterruptedException) {
                        Thread.currentThread().interrupt()
                    }
                }
            }
        }

        startLatch.countDown()
        pool.shutdown()
        pool.awaitTermination(30, TimeUnit.SECONDS)

        val finalCount = reservationService.count(event.id!!)
        println("[NoLock] 성공=${successUsers.size} 실패=${failUsers.size} 최종=${finalCount} 정원=${capacity}")
        if (finalCount <= capacity) {
            println("[WARN] 이번 실행에서는 과잉 예약이 재현되지 않았습니다. (실패 사용자 수=${failUsers.size})")
        }
        // 재현 목적: 초과되길 기대 (환경 따라 간헐적으로 초과 안될 수 있음)
        assertThat(finalCount).isGreaterThan(capacity.toLong())
    }
}
