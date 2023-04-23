## Spring Data Jpa 학습을 위한 리포지토리

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