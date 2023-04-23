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
    - ORM 프레임워크가 아니며 SQL을 잘 활용하기 위한 도구로 JPA 함께 사용하면 좋다.

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
3. 서비스 로직에 적용

     