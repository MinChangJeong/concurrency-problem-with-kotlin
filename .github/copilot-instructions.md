<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->

# Kotlin Spring Boot Project

이 프로젝트는 Kotlin과 Spring Boot를 사용한 웹 애플리케이션입니다.

## 프로젝트 구조
- Kotlin 및 Spring Boot로 구축
- 의존성 관리를 위한 Gradle 사용
- RESTful API 구조
- 기본 설정 및 설치 포함

## 개발 참고사항
- 주요 언어: Kotlin
- 웹 애플리케이션을 위한 Spring Boot 프레임워크
- Gradle 빌드 시스템
- 표준 Spring Boot 프로젝트 구조

## API 엔드포인트
- `GET /api/hello` - Hello 메시지 반환
- `GET /api/health` - 애플리케이션 상태 확인
- `GET /actuator/health` - Spring Boot Actuator 헬스 체크

## 실행 방법
```bash
./gradlew bootRun
```
애플리케이션은 http://localhost:8081에서 실행됩니다.