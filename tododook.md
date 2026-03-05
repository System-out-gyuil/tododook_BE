# 기술 스택

- BE : Spring Boot, JPA, JWT
- FE : HTML, CSS, React, Typescript
- DB : MySQL
- ETC : AWS EC2, AWS S3, GitHub, Docker

Websocket, Redis, OAuth2 고려

<br>
<br>

# 메인 색감
- 프론트엔드하면서 정하기

<br>
<br>

# DB 설계

### tbl_user

| 필드명 | 타입 | 설명 | - |
|--------|------|------|------|
| id | index | 인덱스번호 | PK |
| name | string | 유저 이름 | unique, NN |
| email | string | 이메일 | unique, NN |
| pw | string | 비밀번호 | NN |
| pnum | string | 폰번호 | unique |
| settings | dict | 유저설정모음 |  |
| agreements | dict | 유저동의항목 |  |


### tbl_todo_category

| 필드명 | 타입 | 설명 | - |
|--------|------|------|------|
| id | index | 인덱스번호 | PK |
| id | index | 유저 번호 | FK |
| name | string | 카테고리 명 | NN |
| color | string | 카테고리 색상 | default : white |
| order | int | 카테고리 순서 |  |
| reveal | boolean | 카테고리 공개 | default : true |


### tbl_todo_routine

| 필드명 | 타입 | 설명 | - |
|--------|------|------|------|
| id | index | 인덱스번호 | PK |
| id | index | 카테고리 번호 | FK |
| name | string | 루틴 명 | NN |
| start_date | String | 루틴 시작날짜 |  |
| end_date | string | 루틴 마감날짜 | |
| passivity | boolean | 루틴 수동으로 할일 추가 | default : false |
| repeat | dict | 루틴 반복설정 | 아래 `repeat` 구조 참고 |


### tbl_todo

| 필드명 | 타입 | 설명 | - |
|--------|------|------|------|
| id | index | 인덱스번호 | PK |
| id | index | 카테고리 번호 | FK |
| name | string | 할일 명 | unique, NN |
| date | string | 할일 날짜 | |
| done | boolean | 할일 완료 여부 | default : false |
| start_time | String | 할일 시작 시간 |  |
| end_time | String | 할일 종료 시간 |  |


<br>

**`repeat` dict 구조 (반복 유형별로 필요한 필드만 사용)**

| type | 의미 | 추가 필드 | 예시 |
|------|------|-----------|------|
| `"daily"` | 매일 | 없음 | `{ "type": "daily" }` |
| `"weekly"` | 매주 | `weekly_days` | `{ "type": "weekly", "weekly_days": [1, 3, 5] }` → 월·수·금 |
| `"monthly"` | 매달 | `monthly_days` | `{ "type": "monthly", "monthly_days": [1, 15] }` → 매월 1일, 15일 |
| `"yearly"` | 매년 | `yearly_dates` | `{ "type": "yearly", "yearly_dates": [{ "month": 3, "day": 5 }, { "month": 12, "day": 25 }] }` |

- **`weekly_days`**: 요일 배열. `0`=일요일 ~ `6`=토요일 (또는 팀 규칙으로 1=월 ~ 7=일 등 통일).
- **`monthly_days`**: 해당 월의 날짜(일) 배열. `1`~`31`. 없는 날(예: 2월 30일)은 생성 시 스킵.
- **`yearly_dates`**: `{ "month": 1~12, "day": 1~31 }` 객체 배열. 여러 날 저장 가능.