# DY2BIT_Server
<img width="1054" alt="image" src="https://user-images.githubusercontent.com/39155520/161428278-4594233d-dc60-4d55-84db-ba2a5a46a96e.png">

트레이딩 알고리즘 한달 수동 테스트
- 2월19일 2020만원
- 2월20일 2029만원
- 2월24일 2032만원
- 2월28일 2049만원
- 3월01일 2037만원
- 3월05일 2071만원
- 3월07일 2086만원
- 3월13일 2103만원

## 서버 기획 스펙
- 업비트, 바이낸스, 환율 api를 3~5초 주기로 가격을 받아와 김프가를 계산합니다.
- 해당 날짜의 최대, 최소 코인 시세 기록을 자동 저장합니다.(최저가, 최대가 기록을 위한 작업)
- 사용자는 원하는 김프 가격에 예약 매매 주문을 걸어둘 수 있습니다.
- 서버에서는 해당 김프 가격에 도달하면 자동으로 업비트와 바이낸스에서 동시에 매매를 진행합니다.

## 기술적 고려 사항
- 호출하는 API가 많아 비동기 처리 신경쓰기 (Coroutine)
- 3초~5초마다 DB에 접근하므로 Latancy를 줄여줘야함 (Redis)
- API의 장애를 생각해서 예외처리 잘 해주기

## TODO-LIST
### 완료
- 스프링 프로젝트 세팅하기
- Liquibase 연결해서 테이블 생성하기
- 업비트 API로 현재 가격 정보 가져오기
- 바이낸스 API로 현재 가격 정보 가져오기
- 환율정보 API로 현재 환율 정보 가져오기
- Couroutine으로 API 비동기 처리
- 받아온 정보들로 김프 가격 계산
- 조건 설정 후 김프 가격과 비교해서 조건에 따라 MIN/MAX 김프가 갱신하기
- & 관련 Controller, Service 로직 개발

### 진행 예정
- Redis 캐시 DB 이용하도록 변경
- 스케줄러 설정해서 3~5초마다 잡 돌도록 하기
- 프론트엔드와 연결해서 예약 가격 설정하고 DB에 저장하기
- 사용자 설정 예약 가격 정보를 바탕으로 예약 매매 시스템 구축하기

<hr>

## 기술스택
- Kotlin
- Springboot
- Mysql
- JPA (Hibernate)
- Liquibase
- Coroutine
- Redis

## DB
엔티티
- DailyPrice(시세저장)
아이디, 종목명, 최소값, 최대값, 최소값 갱신 시각, 최대값 갱신 시각, 생성일

- ReservationOrder(예약 주문)
아이디, 종목명, 예약수량, 김프 설정가, 매수or매도(업비트 기준) 여부, 생성일, 종료일(취소 포함)

<hr>
매수 시세 계산 로직: 종목별로 평균단가 구하기 = {(물량1 * 가격1) + (물량2 * 가격2)+...+(물량n * 가격n)}/ 물량1+물량2+..+물량n
청산가격: 평균단가 - (100/레버리지 배수 * 평균단가)
평균 김프 = {(물량1 * 김프1) + (물량2 * 김프2)+...+(물량n * 김프n)}/ 물량1+물량2+..+물량n
