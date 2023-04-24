# Jpa Test와 QueryDSL 학습을 위한 리포지토리

## Spring Data JPA Test

1. Spring Data Jpa Test
   - @DataJpaTest
     - 프로퍼티를 가질 수 있다.
     - sql 공개 유무 (기본값 트루)
     - 부트스트랩 모드
     - SpringBootTest 공통 옵션
       - useDefaultFilter
       - includeFilters
       - excludeFilters
       - excludeAutoConfiguration
     - 메타 어노테이션
       - @ExtendWith(SpringExtension.class) - JUnit5
          - Junit4의 경우 @RunWith -> SpringRunner 클래스
       - @Transactional
2. 스프링 부트 테스트에서 리플렉션 사용할 수 있도록 도와주는 유틸 클래스로 setter를 만들지 않았기 때문에 사용  - Null인채로 들어갈 경우 Equals, HashCode 구현하는 코드에서 문제가 발생한다.
   - 롬복의 @EqualsAndHashCode를 이용하면 영속성 컨텍스트에 들어가기전 id가 Null인 경우 동등성 비교가 힘들다.
     - 동등성 비교를 하지않고 false를 리턴해서 영속성 컨텍스트에 들어가지 않았지만 데이터가 다른 엔티티들을 Set 자료구조에 넣는다.
     - Equals 오버라이딩에는 id가 null이면 false, id가 null이 아니면 id만 이용하도록 비교
     - HashCode 오버라이딩할 때 상수만 들어가도록 작성한다. (검색할 때 사용하는 인덱스 걸어둔 컬럼들들을 활용)
   ```java
       @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return id != null && id.equals(((Place) obj).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(placeName, address, phoneNumber, createdAt, modifiedAt);
    }
   ```
3. @Transactional를 가지고 있고 테스트 스코프에서 동작하는 메서드는 모두 기본동작이 롤백이다.
4. Spring Data Rest
   - Spring Data 프로젝트의 일부로, Spring 애플리케이션에서 RESTful 웹 서비스를 만드는 데 사용되는 라이브러리
   - Spring Data 리포지토리를 기반으로 RESTful API를 자동으로 노출하는 기능을 제공
   - Spring Data JPA, MongoDB, Redis 등의 저장소를 사용하는 애플리케이션에서 RESTful API를 만들기 위해 별도의 컨트롤러 클래스를 작성하지 않아도 된다. 
   - Spring Data 리포지토리에서 제공되는 CRUD(Create, Read, Update, Delete) 연산을 HTTP 메서드와 매핑해주어 RESTful API를 제공
   - HAL(Hypertext Application Language)과 같은 하이퍼미디어 형식을 지원해서 자기 서술적인 특성을 강화
5. Spring Data Rest 만들 때 QueryDSL PredicateExcutor를 이용


## QueryDSL


### QueryDSL과 Jooq
1. QueryDSL
    - 자바 코드로 부터 DB 쿼리를 생성해주는 도구
    - 타입 세이프가 지켜지지 않는 HQL의 가독성이 떨어지는 Criteria 대체제
    - Type-Safety하지 않아 불안정한 JPQL, em을 직접 사용 해야하고 어려운 Criteria Query
2. Q클래스
    - QueryDSL이 자동으로 생성해주는 클래스
    - Q클래스를 이용해 가독성 좋은 쿼리 작성 가능
    - 편리한 Join과 스프링이 제공해주는 Pagealbe 등 스프링과의 연동성 보장
    - 다이내믹하게 where절을 추가 가능
    - fetchCount()를 이용해 쉽게 카운트 쿼리 호출 가능
    - QueryDsl 인스턴스를 가져와 에러처리 가능하고 쉽게 조인과 페이징이 가능
        - 복잡한 페이징을 구현할 때에도 Pageable만 잘 만들어두면 쉽게 페이징 가능
3. Jooq
    - DB 스키마를 자바 클래스로 바꿔주는 도구
    - ORM 프레임워크가 아니며 SQL을 잘 활용하기 위한 도구로 JPA와 함께 사용하기 어렵다. (이름 충돌 문제)
    - 때문에 Spring Data JPA의 트랜잭션 연동이 불가능하다. (Jooq는 엔티티 매니저를 사용하지 않으므로)
    - 즉 애플리케이션 레벨에서의 트랜잭션 관리가 어렵다. (Spring Dat JPA의 트랜잭션 관리 불가능, 즉 jooq를 통한 트랜잭션 관리만 가능)
    - 스프링과 JooqAutoConfiguration으로만 연동이 가능 -> pageable 사용 불가능 -> sort를 직접 작성해야한다.
    - 대신 잘 만든 스키마가 필요하다.
    - 즉, 잘 만든 스키마가 있으면 QueryDSL이 아닌 Jooq를 이용해 동적쿼리를 작성할 수 있다.
    - 운영시에도 바인딩 파라미터를 직접 알 수 있어서 문제를 직접 수행할 수 있는 장점이 있다. 
    - QueryDSL의 경우 바인딩 파라미터를 trace하지 않는 이상 알 수 없다.

### QueryDSL 다루기
1. Querydsl의 특징
    - JPQL 작성 라이브러리로, Spring Data JPA와 조합이 좋다.
    - 외래키가 아닌 커스텀 키 조인이 가능하고, query projection이 자유롭다.
    - 쿼리메서드로 가져오면 테이블과 1:1 매핑관계이지만, QueryDSL로 원하는 필드만 가져와 데이터 사이즈를 줄일 수 있다.
        - QuerydslRepositorySupport
            - EntityManager를 노출하지 않고, Querydsl 필요 기능 직접 지원
        - QuerydslPredicateExecutor
            - Predicate를 이용한 dynamic select, Spring Data REST 지원
        - QuerydslBinderCustomizer
            - 파라미터 바인딩의 세부 기능 조절 지원
2. 검색어를 동적으로 받아서 존재하는 검색어마다 다르게 쿼리를 작성하는 동적 쿼리
    - QueryDsl를 간접적으로 이용해서 쉽게 구현된 코드
        - QuerydslPredicateExecutor
        - QuerydslBinderCustomizer
   ```java
   public interface EventRepository extends
        JpaRepository<Event, Long>,
        QuerydslPredicateExecutor<Event>,
        QuerydslBinderCustomizer<QEvent> {

    @Override
    default void customize(QuerydslBindings bindings, QEvent root) {
        bindings.excludeUnlistedProperties(true);
        bindings.including(root.place.placeName, root.eventName, root.eventStatus, root.eventStartDatetime, root.eventEndDatetime);
        bindings.bind(root.place.placeName).first(StringExpression::containsIgnoreCase);
        bindings.bind(root.eventName).first(StringExpression::containsIgnoreCase);
        bindings.bind(root.eventStartDatetime).first(ComparableExpression::goe);
        bindings.bind(root.eventEndDatetime).first(ComparableExpression::loe);
    }
   }
   ```
   - 위 코드를 QueryDsl로 변경하기 위한 조건
     1. getEvents()메서드를 활용해 직접 구현할 것이다.
       ```java
      public List<EventDto> getEvents(
               Long placeId,
               String eventName,
               EventStatus eventStatus,
               LocalDateTime eventStartDatetime,
               LocalDateTime eventEndDatetime
      ) {
          try {
              return null;
          } catch (Exception e) {
              throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
         }
      }
      ```
     2. 위 코드를 리팩토링해서 장소 이름으로, 이벤트이름, 이벤트 상태, 날짜범위로 검색한다.
3. 구현 순서
   1. QueryDSL 세팅
   2. 쿼리 작성
   3. 서비스 로직에 적용


## 구형
1. QueryDSL 세팅
    - 의존성 추가
    - 필요한 옵션 설정
      - 테스트시에 인텔리제이의 기능을 이용하기 위한 옵션 (그레이들이 빌드한 큐클래스와 인텔리제이가 빌드한 큐클래스가 겹치지않도록)
      - 수동으로 큐클래스의 위치를 지정했고, 이러한 클래스를 그레이들이 관리할 수 있도록 설정
      - 큐 클래스를 형상관리에서 제외한다.
   ```yaml
    // queryDSL 설정
    implementation "com.querydsl:querydsl-jpa" //코어를 포함한다.
    //implementation "com.querydsl:querydsl-core"
    implementation "com.querydsl:querydsl-collections"
    annotationProcessor "com.querydsl:querydsl-apt:${dependencyManagement.importedProperties['querydsl.version']}:jpa" // querydsl JPAAnnotationProcessor 사용 지정
    annotationProcessor "jakarta.annotation:jakarta.annotation-api" // java.lang.NoClassDefFoundError (javax.annotation.Generated) 발생 대응
   
   ```
   ```yaml 
    // querydsl 적용
    def generated='src/main/generated'
    
    // java source set 에 querydsl QClass 위치 추가
    sourceSets {
        main.java.srcDirs += [ generated ]
    }
    
    // querydsl QClass 파일 위치를 잡아주는 설정
    tasks.withType(JavaCompile) {
        options.getGeneratedSourceOutputDirectory().set(file(generated))
    }
    
    // gradle clean 시에 QClass 디렉토리 삭제
    clean {
        delete file(generated)
    }
   ```
2. 쿼리 작성
   - DTO 만들기 
     - 이벤트 엔티티가 플레이스 엔티티를 직접 의존하는 기존 형태
     - DTO가 아닌 사용할 필드에만 의존하는 형태로 전환
  ```java
  public record EventResponse(
       Long id,
       PlaceDto place,
       String eventName,
       EventStatus eventStatus,
       LocalDateTime eventStartDatetime,
       LocalDateTime eventEndDatetime,
       Integer currentNumberOfPeople,
       Integer capacity,
       String memo
  )
  ```
  ```java
  public record EventViewResponse(
    Long id,
    String placeName,
    String eventName,
    EventStatus eventStatus,
    LocalDateTime eventStartDatetime,
    LocalDateTime eventEndDatetime,
    Integer currentNumberOfPeople,
    Integer capacity,
    String memo
  )
  ```
  1. QueryDSL를 이용한 구현체를 작성
     - QuerydslRespositorySupport 상속
     - 해당 리포지토리의 도메인을 전달한 부모의 생성자 호출
  2. DTO를 반환하는 메서드 오버라이딩
     - 큐클래스를 이용해 직접 구현
     - 스프링 데이터 JPA가 지원하는 JPQLQuery 이용 (from으로 시작 (문법))
     - JPQL쿼리를 이용해 event 엔티티의 필드와 place에 있는 필드를 가져온다. (이벤트만 가져올때는 select 생략 가능)
  3. 직접 커스텀 프로젝션 하는 방법
     1. setter 주입
     2. 생성자 주입
     3. 쿼리 프로젝션 어노테이션 사용
  4. 생성자 주입을 이용해 작성
     - 동적으로 where 절을 추가한다.
     - where(큐클래스.검색할 이름.검색할 방법.검색할 이름)
  5. where절로 만든 검색 조건을 완성시킨다.
     - 페이지 방식으로 변환하기 위한 파라미터 : 리스트, 페이징 정보, 페이징 처리하지 않는 총 사이즈 (쿼리의 카운트 쿼리)
     - PageImpl<>(events, pageable, query.fetchCount());
  6. 직접 작성한 쿼리DSL를 상속
  7. EventRepository를 테스트
     - Pageable 요청 부분과 응답부분이 별도로 구현되어있다.
  8. properties 설정으로 sql 출력하고 sql을 가독성 좋게 출력                                                                
     - spring.jpa.show-sql=true
     - spring.jpa.properties.hibernate.format_sql=true

3. 서비스 로직에 적용
   1. ApiEventController를 직접 작성한 QueryDSL를 사용하는 코드로 변경 
      - eventService의 getEvents를 사용하는데 세부 스펙이 다름 (Place 엔티티 -> Place 엔티티의 placeName)
   2. EventController에 작성한 코드를 테스트하기 위한 임시 메서드 작성
      - @Validated 적용
   3. 뷰를 알맞게 변경
      - 실제로 place 엔티티를 직접 접근하는게 아니라, plcaeName만 받는다.
      - 타임리프 라이브러리를 이용해 날짜 형식 포멧팅
   4. 테스트 코드에 반영
      - 이벤트 뷰 데이터를 검색하면, 페이징된 결과를 출력하여 보여주는 서비스 테스트
      - [GET] 이벤트 리스트 페이지 - 커스텀 데이터
      - [GET] 이벤트 리스트 페이지 - 커스텀 데이터 + 검색 파라미터
      - [GET] 이벤트 리스트 페이지 - 커스텀 데이터 + 검색 파라미터 (장소명, 이벤트명 잘못된 입력)")
   5. 페이지 사이즈 변경 가능
      - spring.data.web.pageable.default-page-size
      - 기본값이 20
```java
        List<EventResponse> eventResponses = eventService.getEvents(
                placeId,
                eventName,
                eventStatus,
                eventStartDatetime,
                eventEndDatetime
        ).stream().map(EventResponse::from).toList();

        return ApiDataResponse.of(eventResponses);
```
```java
        return ApiDataResponse.of(List.of(EventResponse.of(
                1L,
                PlaceDto.of(
                        1L,
                        PlaceType.SPORTS,
                        "배드민턴장",
                        "서울시 가나구 다라동",
                        "010-1111-2222",
                        0,
                        null,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                ),
                "오후 운동",
                EventStatus.OPENED,
                LocalDateTime.of(2021, 1, 1, 13, 0, 0),
                LocalDateTime.of(2021, 1, 1, 16, 0, 0),
                0,
                24,
                "마스크 꼭 착용하세요"
        )));
```
```java
public List<EventDto> getEvents(
            Long placeId,
            String eventName,
            EventStatus eventStatus,
            LocalDateTime eventStartDatetime,
            LocalDateTime eventEndDatetime
    ) {
        try {
            return null;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }
```

```java
    public Page<EventViewResponse> getEventViewResponse(
            String placeName,
            String eventName,
            EventStatus eventStatus,
            LocalDateTime eventStartDatetime,
            LocalDateTime eventEndDatetime,
            Pageable pageable
    ) {
        try {
            return eventRepository.findEventViewPageBySearchParams(
                    placeName,
                    eventName,
                    eventStatus,
                    eventStartDatetime,
                    eventEndDatetime,
                    pageable
            );
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }
```
```java
    @GetMapping("/custom")
    public ModelAndView customEvents(
            @Size(min = 2) String placeName,
            @Size(min = 2) String eventName,
            EventStatus eventStatus,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime eventStartDatetime,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime eventEndDatetime,
            Pageable pageable
    ) {
        Map<String, Object> map = new HashMap<>();
        Page<EventViewResponse> events = eventService.getEventViewResponse(
                placeName,
                eventName,
                eventStatus,
                eventStartDatetime,
                eventEndDatetime,
                pageable
        );

        map.put("events", events);

        return new ModelAndView("event/index", map);
    }
```


## Jooq 다루기
1. Jooq 설정
   - build.gradle
   - JooqConfig 
     - JooqAutoConfiguration에서 제공하는 pretty formatting
2. QueryDSL하던 동적쿼리 대체
   - 하이버네이트 사용 불가, QueryDSL 사용 불가
   - 따라서 spring.jpa.hibernate.ddl-auto=none 설정 필요
   - 도메인에도 엔티티가 존재하고, Jooq가 만든 엔티티가 존재하는데, Jooq가 만든 엔티티를 사용한다. 
     - starter-jooq에 포함된 빈 주입 
     - private final DSLContext dslContext;
   - `orderBy()` 구현은 Jooq 기술 선택을 하지 않은 상태에선 구현이 어렵다.
   ```java
   orderBy(event.ID.asc())
   pageable. getSort().getOrderFor("placeName").getProperty() //placeName
   pageable. getSort().getOrderFor("placeName").getDirection() //asc
   //order가 list로 있으므로 리플렉션으로 관련된 jooq 객체를 순회하면서 매칭해야한다.
   ```
   - Spring Data JPA 컬럼명을 `camelCase`로 쓰고 있는데 이를 `PASCAL_CASE`로 바꿔야 한다
   - 조인 테이블이라면 무슨 테이블 컬럼인지 알 수 없다
   - 컬럼 찾아내고 매칭하는 과정의 예외 처리도 해야 한다 
   - 모든 것을 스프링 지원 없이 해내야 한다.
   - gradle 플러그인에 DB정보가 build.gradle에 엉킬 가능성이 높다.
   - JPA는 프로퍼티로 설정하지만, build.gradle로만 해야하므로, 굉장히 불편하고 어렵다.
   - 스프링 부트나 Spring Data JPA와 레퍼런스도 적고 호환성이 떨어지기 때문에 함께 쓰기 어렵다.

## Fetch와 N+1 문제

1. Fetch
   - 애플리케이션이 DB로부터 데이터를 가져오는 것
   - DB에 통신하는 것에는 네트워크 I/O 비용, 데이터 크기에 대한 비용이 소모된다.
2. eager fetch
   - 프로그램 코드가 쿼리를 날리는 시점에 데이터를 즉시 가져오기
   - 쿼리 한번 -> 데이터 크기 비용 많음
   ```sql
   select a.id from A a inner join B b on a.b_id = b.id
   ```
3. lazy fetch
   - 가져오려는 데이터를 애플리케이션에서 실제로 접근할 때 가져오기
   - 쿼리 두번 -> 네트워크 I/O 비용 많음
   - 잘못 사용하면 데이터 접근 에러 발생 (LazyInitializationException)
     - OSIV 패턴을 사용할 때에도 발생 가능한데,
     - Entity Manager는 일반적으로 Transaction 범위 내에서 사용되어야 하지만
   Transaction이 종료되면 영속성 컨텍스트도 닫히게 된다. 
     - 만약 Lazy Loading 프록시 객체에 접근하고자 하는 시점에 영속성 컨텍스트가 닫혀버리면 발생
   ```sql
   select a.id from A; (select b from B b where b.id=?)
   ```
4. JPA의 연관관계 기본 FETCH 전략
   - 효율적인 전략이 기본 Fetch 전략 
      - @OneToOne : EAGER
      - @ManyToOne : EAGER
      - @OneToMany : LAZY
      - @ManyToMany : LAZY
   - 효율성 -> 자신의 엔티티 관점에서 상대방 엔티티를 알고 있는지 여부
     - 알고 있다. -> EAGER, 모른다 -> LAZY
     - 자식 입장에서 부모는 필연적이지만, 부모 입장에서는 자식을 반드시 알아야하는 게 아니므로
     - 데이터에 접근할 가능성이 낮으면 LAZY, 데이터에 접근할 가능성이 높으면 EAGER
5. 효율성을 따져서 데이터가 어느 쪽으로 더 자주 사용될 것인가 예측
   - LAZY 사용하는 경우
     - 연관 관계가 있는 엔티티에서 자식 엔티티만 가져오는 경우
     - JOIN을 의식하지 않지만, LAZY 세팅이 후속 쿼리 발생 방지를 보장하는 건 아니다.
   ```java
   @Setter
    //    @ManyToOne(optional = false) // 기본전략은 Eager
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    //이벤트 가져올때 필요없으면 안 가져온다.
    //꼭 필요할 때 가져온다. -> Lazy Fetch 쿼리 발생
    private Place place;
     ```
   - EAGER 사용하는 경우 (기본적으로는 독립적인 쿼리 수행)
     - 연관관계 엔티티를 무조건 다 가져오는 경우
     - JOIN을 사용해야한다고 의식하는 상황 -> JOIN 동작을 보장하는 것은 아니다. -> findAll()
     - JPQL을 직접 작성해 영속성 컨텍스트에 JOIN을 알린다. -> querydsl
     - inQuery 옵션을 켜놓으면 inQuery로 발생
   ```sql
       select
        place0_.id as id1_3_0_,
        place0_.address as address2_3_0_,
        place0_.capacity as capacity3_3_0_,
        place0_.created_at as created_4_3_0_,
        place0_.memo as memo5_3_0_,
        place0_.modified_at as modified6_3_0_,
        place0_.phone_number as phone_nu7_3_0_,
        place0_.place_name as place_na8_3_0_,
        place0_.place_type as place_ty9_3_0_ 
    from
        place place0_ 
    where
        place0_.id in (
            ?, ?, ?
        )
   ```
   - inQuery 옵션 해제 -> 쿼리 4번 발생
   - -> eager fetch지만 JOIN을 수행하지 않는다. -> 매우 비효율적인 쿼리 발생가능
     - inQuery란
       - 데이터베이스에 대한 쿼리를 객체 지향적인 방식으로 작성하는 HQL에서 제공하는 옵션으로 IN절 사용시 적용
       - IN 절은 주어진 값들 중 하나와 일치하는 결과를 반환하는 쿼리를 작성할 때 사용 
       - IN 절에 지정된 값들을 한 번에 가져오는 대신, 지정된 배치 크기만큼의 값들을 가져와서 쿼리를 실행한다. 
       - 따라서 대량의 데이터를 처리할 때, inQuery 옵션을 사용하면 성능이 향상
   ```sql
       select
        event0_.id as id1_2_,
        event0_.capacity as capacity2_2_,
        event0_.created_at as created_3_2_,
        event0_.current_number_of_people as current_4_2_,
        event0_.event_end_datetime as event_en5_2_,
        event0_.event_name as event_na6_2_,
        event0_.event_start_datetime as event_st7_2_,
        event0_.event_status as event_st8_2_,
        event0_.memo as memo9_2_,
        event0_.modified_at as modifie10_2_,
        event0_.place_id as place_i11_2_ 
    from
        event event0_
       select
        place0_.id as id1_3_0_,
        place0_.address as address2_3_0_,
        place0_.capacity as capacity3_3_0_,
        place0_.created_at as created_4_3_0_,
        place0_.memo as memo5_3_0_,
        place0_.modified_at as modified6_3_0_,
        place0_.phone_number as phone_nu7_3_0_,
        place0_.place_name as place_na8_3_0_,
        place0_.place_type as place_ty9_3_0_ 
    from
        place place0_ 
    where
        place0_.id=?
       select
        place0_.id as id1_3_0_,
        place0_.address as address2_3_0_,
        place0_.capacity as capacity3_3_0_,
        place0_.created_at as created_4_3_0_,
        place0_.memo as memo5_3_0_,
        place0_.modified_at as modified6_3_0_,
        place0_.phone_number as phone_nu7_3_0_,
        place0_.place_name as place_na8_3_0_,
        place0_.place_type as place_ty9_3_0_ 
    from
        place place0_ 
    where
        place0_.id=?
       select
        place0_.id as id1_3_0_,
        place0_.address as address2_3_0_,
        place0_.capacity as capacity3_3_0_,
        place0_.created_at as created_4_3_0_,
        place0_.memo as memo5_3_0_,
        place0_.modified_at as modified6_3_0_,
        place0_.phone_number as phone_nu7_3_0_,
        place0_.place_name as place_na8_3_0_,
        place0_.place_type as place_ty9_3_0_ 
    from
        place place0_ 
    where
        place0_.id=?
   ```
5. N+1문제

## 고민
1. eq? equals? 뭔차이지 
- query.where(event.eventStatus.eq(eventStatus));
- query.where(event.eventStatus.equals(eventStatus));
- 첫 번째 코드는 JPQL에서 사용되는 문법
- 두 번째 코드는 Java 객체 비교에 사용되는 문법

2. JPA와 함께 사용하기 적합한건 QueryDSL이고, JOOQ는 MyBatis와 함께 사용하는게 더 나아보이는데
- JPA, QueryDSL, JOOQ, MyBatis는 모두 SQL 쿼리를 생성하고 실행하는 데 사용되는 자바 기반의 ORM(Object-Relational Mapping) 라이브러리 및 프레임워크입니다. 이러한 도구들은 각각의 장단점이 있으며, 적합한 사용 시나리오가 있습니다.
- JPA는 ORM 기술 중 가장 널리 사용되는 기술 중 하나이며, 개발자가 데이터베이스 스키마를 직접 작성하지 않고도 객체 지향적인 방식으로 데이터베이스를 조작할 수 있도록 해줍니다. QueryDSL은 JPA와 함께 사용하기 적합한 라이브러리로, JPA의 Criteria API 보다 더 직관적이고 유연한 쿼리 작성이 가능합니다.
- 반면에 JOOQ는 SQL 기반의 코드를 생성하여 타입 안정성과 성능을 보장합니다. JOOQ는 쿼리 작성 시 자동 완성 기능을 지원하므로 개발자는 오타나 잘못된 쿼리 문법으로 인한 버그를 줄일 수 있습니다. 또한, JOOQ는 MyBatis와 함께 사용할 때 더 나은 상호운용성을 제공합니다.

- 따라서, JPA와 함께 사용하려면 QueryDSL을 사용하는 것이 좋습니다. 
- QueryDSL은 JPA의 Criteria API의 한계를 극복하고, JPA에서 제공하지 않는 유연한 쿼리 작성이 가능하도록 지원하기 때문입니다.
- JOOQ는 MyBatis와 함께 사용하여 SQL 기반의 코드를 생성하는 데 적합합니다. 
- JOOQ는 타입 안정성과 성능을 보장하며, MyBatis와 함께 사용하면 더 나은 상호운용성을 제공합니다.