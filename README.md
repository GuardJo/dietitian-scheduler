# dietitian-scheduler
영양사 근무 스케줄 기록 앱

# 영양사 근무 스케줄러

영양사가 기존에 사용하던 **Excel 기반 근무 스케줄 데이터를 웹에 등록하고, 스마트폰에서 캘린더 형태로 편리하게 조회**할 수 있도록 하는 스케줄 관리 서비스

## 프로젝트 목적
병원 및 기관에서 관리되는 영양사 근무 스케줄은 Excel 등의 문서 형태로 관리되는 경우가 많음
이 프로젝트는 이러한 스케줄 데이터를 웹 서비스에 저장하여 아래의 기능들을 제공할 예정

- 스마트폰에서 쉽게 확인
    
- 월별 캘린더 형태로 조회
    
- 개인별 로그인 및 스케줄 관리
    
- 향후 Excel 자동 등록 지원

---

## MVP 범위


|기능|설명|
|---|---|
|로그인|사용자 인증 및 세션 관리|
|스케줄 등록|근무일, 근무 유형 등의 스케줄 등록|
|스케줄 조회|월별 근무 스케줄 조회|
|모바일 UI|스마트폰에서 보기 편한 반응형 UI|

### MVP 사용자 흐름

```mermaid
flowchart LR
    A[로그인] --> B[월별 스케줄 조회]
    B --> C{스케줄 등록 필요?}
    C -->|Yes| D[스케줄 등록]
    D --> B
    C -->|No| E[스케줄 확인]
```

---

# 전체 시스템 구조

Spring Boot 기반의 REST API 서버와 모바일 웹 기반의 프론트엔드로 구성

```mermaid
flowchart TB
    User["👤 영양사"]

    subgraph Frontend["Frontend"]
        Web["Web App"]
        Calendar["월별 캘린더"]
        ScheduleForm["스케줄 등록"]
        Login["로그인"]
    end

    subgraph Backend["☕ Backend - Spring Boot"]
        Auth["인증 / 인가"]
        Schedule["스케줄 API"]
        UserAPI["사용자 API"]
    end

    subgraph Database["Database"]
        DB[("Schedule DB")]
    end

    User --> Web

    Web --> Login
    Web --> Calendar
    Web --> ScheduleForm

    Login --> Auth
    Calendar --> Schedule
    ScheduleForm --> Schedule

    Auth --> UserAPI
    Schedule --> DB
    UserAPI --> DB
```

---

# 로그인 프로세스

사용자는 자신의 계정으로 로그인한 후 스케줄을 조회하고 등록

```mermaid
sequenceDiagram
    actor User as 영양사
    participant Web as Web App
    participant API as Spring Boot
    participant DB as Database

    User->>Web: 아이디 / 비밀번호 입력
    Web->>API: POST /api/auth/login
    API->>DB: 사용자 조회
    DB-->>API: 사용자 정보

    alt 인증 성공
        API-->>Web: Access Token
        Web-->>User: 스케줄 화면 이동
    else 인증 실패
        API-->>Web: 401 Unauthorized
        Web-->>User: 로그인 실패 메시지
    end
```

---

# 월별 스케줄 조회 프로세스

로그인 이후 기본 화면은 **현재 월의 스케줄을 보여주는 캘린더 화면**으로 구성

```mermaid
sequenceDiagram
    actor User as 영양사
    participant Web as Web App
    participant API as Spring Boot
    participant DB as Database

    User->>Web: 월별 스케줄 화면 진입
    Web->>API: GET /api/schedules?year=2026&month=9
    API->>DB: 해당 월 스케줄 조회
    DB-->>API: Schedule List
    API-->>Web: 월별 스케줄 JSON
    Web-->>User: 캘린더 화면 표시
```

### API 예시

```
GET /api/schedules?year=2026&month=9
Authorization: Bearer {accessToken}
```

Response:

```json
{
  "year": 2026,
  "month": 9,
  "schedules": [
    {
      "date": "2026-09-01",
      "type": "DAY",
      "startTime": "09:00",
      "endTime": "18:00"
    },
    {
      "date": "2026-09-02",
      "type": "OFF"
    }
  ]
}
```

---

# 스케줄 등록 프로세스

사용자는 특정 날짜의 근무 정보를 등록하거나 수정
- *우선은 수정은 통째로 해당 월 엎어칠 예정*

```mermaid
sequenceDiagram
    actor User as 영양사
    participant Web as Web App
    participant API as Spring Boot
    participant DB as Database

    User->>Web: 날짜 선택
    Web-->>User: 스케줄 등록 화면

    User->>Web: 근무 정보 입력
    Web->>API: POST /api/schedules

    API->>API: 입력값 검증
    API->>DB: 스케줄 저장
    DB-->>API: 저장 완료

    API-->>Web: 201 Created
    Web-->>User: 등록 완료
    Web-->>User: 월별 캘린더 갱신
```

---

# 전체 사용자 프로세스

MVP의 전체적인 프로세스 흐름

```mermaid
flowchart TD
    Start(["서비스 접속"])

    Start --> Login{"로그인 여부"}

    Login -->|No| LoginPage["로그인"]
    LoginPage --> Auth{"인증 성공?"}

    Auth -->|No| LoginPage
    Auth -->|Yes| Calendar["월별 스케줄 조회"]

    Login -->|Yes| Calendar

    Calendar --> SelectDate["날짜 선택"]

    SelectDate --> ScheduleExists{"스케줄 존재?"}

    ScheduleExists -->|Yes| Detail["스케줄 상세 확인"]
    ScheduleExists -->|No| Register["스케줄 등록"]

    Detail --> Edit{"수정 필요?"}

    Edit -->|Yes| Register
    Edit -->|No| Calendar

    Register --> Save["스케줄 저장"]
    Save --> Calendar

    Calendar --> ChangeMonth{"다른 월 조회?"}
    ChangeMonth -->|Yes| Calendar
    ChangeMonth -->|No| End(["종료"])
```
