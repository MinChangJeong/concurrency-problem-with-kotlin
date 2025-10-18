
## 📂 프로젝트 구조

```
my-app2/
├── src/
│   ├── main/
│   │   ├── kotlin/
│   │   │   └── com/example/myapp/
│   │   │       ├── MyAppApplication.kt
│   │   │       └── controller/
│   │   │           └── HelloController.kt
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── kotlin/
│           └── com/example/myapp/
│               └── MyAppApplicationTests.kt
├── build.gradle.kts
├── gradlew
├── gradle/
│   └── wrapper/
└── README.md
```

## 동시성 및 @Transactional 한계 설명

### 왜 @Transactional 만으로 동시성 문제가 해결되지 않는가

`@Transactional` 은 다음을 보장합니다: 원자성(Atomicity), 일관성(Consistency), 격리(Isolation), 지속성(Durability). 그러나 여기서 격리(Isolation)는 데이터베이스의 격리 수준에 의존하며 기본값(대부분 `READ_COMMITTED`)에서는 다음과 같은 레이스 조건(race condition)을 막지 못합니다.

대표적인 패턴은 Check-Then-Act 문제입니다.

1. 트랜잭션 A와 B가 거의 동시에 시작한다.
2. 둘 다 현재 예약 수(count)를 조회한다. (예: 4)
3. 둘 다 정원(capacity=5) 미만이라고 판단한다.
4. 둘 다 예약 레코드(insert)를 시도한다.
5. 최종적으로 정원이 초과된다. (예: 6, 7, 10 등 환경/타이밍에 따라)

이 상황은 READ_COMMITTED 수준에서 자연스럽게 발생할 수 있으며 @Transactional 자체는 “순차 실행”을 강제하지 않습니다. 즉 같은 테이블/행을 읽고 수정하는 여러 트랜잭션이 동시에 열리고, 커밋 시점 혹은 락 충돌이 없는 한 그대로 반영됩니다.

### 비관적 락(Pessimistic Lock) 적용

우리는 다음과 같은 리포지토리 메서드를 통해 비관적 락을 적용했습니다.

```kotlin
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select e from Event e where e.id = :id")
fun findForUpdate(@Param("id") id: Long): Event?
```

이에 따라 `ReservationService.reserve` 내부에서 이벤트 행을 먼저 `SELECT ... FOR UPDATE` 로 잠금 → 다른 트랜잭션이 동일 행을 잠그려 할 때 대기 → count 검사와 insert 사이에 다른 트랜잭션이 끼어들지 못해 과잉 예약(overbooking)이 방지됩니다.

### 비교: 락 전후 스레드 흐름

| 구분 | 락 없이 (`reserveWithoutLock`) | 락 적용 (`reserve`) |
|------|-------------------------------|---------------------|
| count 조회 | 여러 스레드 동시 | 첫 스레드만 즉시, 나머지 대기 |
| 조건 검사 | 모두 같은 이전 값 기반 | 직렬화된 최신 값 기반 |
| 초과 발생 | 쉽게 발생 | 방지 (capacity 이상 insert 차단) |
| 대기 시간 | 낮지만 불안정 | 대기 증가 가능, 정합성 확보 |

### 대안적 전략

| 전략 | 장점 | 단점 | 사용 예 |
|------|------|------|--------|
| 낙관적 락(@Version) | 경합 적을 때 고성능 | 재시도 로직 필요 | 재고 낮은 변경 빈도 |
| 조건부 원자 UPDATE | 단일 SQL로 동시성 제어 | SQL 의존 | 카운터/재고 감소 |
| Redis/분산 락 | 분산 환경 확장 | 인프라 필요 | 멀티 서비스 공유 자원 |
| 메시지 큐 직렬화 | 완전 순차 처리 | 지연 증가 | 결제/정산 등 강한 일관성 |

## 동시성 관련 테스트 코드 설명

### 1) `ReservationOverbookingTest` (락 없이 과잉 예약 재현)
- `reserveWithoutLock` 사용.
- 여러 스레드가 동시에 count → 조건 통과 → insert 하도록 인위적 지연(`Thread.sleep`) 포함.
- 최종 예약 수가 정원을 초과할 것을 기대(`assertThat(finalCount).isGreaterThan(capacity)`).
- 성공/실패 사용자 식별: 성공한 사용자 ID를 `ConcurrentLinkedQueue` 에 누적, 실패 사용자도 별도 큐에 저장 후 요약 출력.

### 2) `ReservationConcurrencyTest` (비관적 락 적용, 초과 방지)
- `reserve` 사용 (내부에서 `findForUpdate` 호출).
- 동일한 경쟁 환경(스레드 수/지연)을 유지하지만 초과가 발생하지 않아야 테스트 통과.
- 최종 예약 수가 `capacity` 이하인지 검증(`<= capacity`).
- 성공/실패 스레드 수를 함께 출력하여 락으로 인해 실패(용량 초과 조기 차단)된 요청을 관찰.

### 공통 포인트
- 인위적 지연은 경쟁 조건을 “확대”하기 위한 실험 장치이며 실제 서비스에서는 제거하거나 비동기 큐/락으로 대체.
- 두 테스트의 차이는 “행 잠금 유무” 뿐이며 이것이 결과(정합성 vs 과잉)의 차이를 만든 핵심.

### 추가 개선 가능성
- 낙관적 락 버전의 추가 테스트(@Version) 및 재시도 전략.
- 조건부 원자 업데이트(e.g. `update event set used = used + 1 where id=? and used < capacity`).
- 재현 실패 시 자동 재시도 Extension(JUnit) 도입.

## 요약
- `@Transactional` 은 트랜잭션 경계를 정의하고 롤백/커밋을 관리하지만 “동시성 순차화” 를 제공하지 않는다.
- 정원(capacity) 같은 비즈니스 불변식을 보존하려면 별도의 동시성 제어(비관적 락, 낙관적 락, 원자적 업데이트, 큐 등)가 필요하다.

## 🔧 설정

애플리케이션 설정은 `src/main/resources/application.properties`에서 관리됩니다.

```properties
server.port=8081
spring.application.name=my-app
management.endpoints.web.exposure.include=health,info
```