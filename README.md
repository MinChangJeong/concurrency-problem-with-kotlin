# My App - Kotlin Spring Boot Application

Spring Boot와 Kotlin으로 개발된 기본 웹 애플리케이션입니다.

## 🚀 기술 스택

- **Kotlin** 1.9.20
- **Spring Boot** 3.2.0
- **Gradle** 8.5
- **Java** 17

## 📋 주요 기능

- RESTful API 엔드포인트
- Spring Boot Actuator를 통한 헬스 체크
- 기본 CRUD 구조

## 🛠️ 설치 및 실행

### 사전 요구사항

- Java 17 이상
- Gradle (wrapper 포함)

### 로컬 실행

1. 프로젝트 클론 및 디렉토리 이동
```bash
cd my-app2
```

2. 애플리케이션 빌드
```bash
./gradlew build
```

3. 애플리케이션 실행
```bash
./gradlew bootRun
```

애플리케이션이 `http://localhost:8081`에서 실행됩니다.

### VS Code Tasks

- **Build**: `Ctrl+Shift+P` → `Tasks: Run Task` → `Build`
- **Run**: `Ctrl+Shift+P` → `Tasks: Run Task` → `Run Spring Boot App`

## 📡 API 엔드포인트

### 기본 엔드포인트

- `GET /api/hello` - Hello 메시지 반환
- `GET /api/health` - 애플리케이션 상태 확인

### 예제 요청

```bash
# Hello 엔드포인트 테스트
curl http://localhost:8081/api/hello

# 응답
{
  "message": "Hello, Kotlin Spring Boot!"
}

# Health 엔드포인트 테스트
curl http://localhost:8081/api/health

# 응답
{
  "status": "UP",
  "timestamp": "1697097628000"
}
```

### Actuator 엔드포인트

- `GET /actuator/health` - 애플리케이션 헬스 체크
- `GET /actuator/info` - 애플리케이션 정보

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

## 🧪 테스트

```bash
# 테스트 실행
./gradlew test
```

## 📝 개발 노트

- 포트 번호: 8081 (기본값)
- 패키지 구조: `com.example.myapp`
- 테스트 프레임워크: JUnit 5

## 🔧 설정

애플리케이션 설정은 `src/main/resources/application.properties`에서 관리됩니다.

```properties
server.port=8081
spring.application.name=my-app
management.endpoints.web.exposure.include=health,info
```

## 📞 연락처

프로젝트에 대한 문의사항이 있으시면 언제든지 연락해 주세요.