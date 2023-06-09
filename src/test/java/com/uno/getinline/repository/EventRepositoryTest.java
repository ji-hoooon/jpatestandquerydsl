package com.uno.getinline.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.uno.getinline.constant.EventStatus;
import com.uno.getinline.constant.PlaceType;
import com.uno.getinline.domain.Event;
import com.uno.getinline.domain.Place;
import com.uno.getinline.dto.EventViewResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

//@Disabled("Jooq 테스트시엔 비활성화 필요")
@DisplayName("DB-이벤트")
@DataJpaTest
class EventRepositoryTest {
    //DataJpaTest 기본 세팅
    //: 의존성 주입 방법 중 생성자 주입 시에 @Autowired 필수
    private final EventRepository eventRepository;
    private final TestEntityManager testEntityManager;

    public EventRepositoryTest(
            @Autowired EventRepository eventRepository,
            @Autowired TestEntityManager testEntityManager) {
        this.eventRepository = eventRepository;
        this.testEntityManager = testEntityManager;
    }


    //테스트를 위한 더미 데이터
    private Event createEvent(Place place) {
        return createEvent(
//                1L,
//                1L,
                place,
                "test event",
                EventStatus.ABORTED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

    }

    private Event createEvent(
//            long id,
//            long placeId,
            Place place,
            String eventName,
            EventStatus eventStatus,
            LocalDateTime eventStartDateTime,
            LocalDateTime eventEndDateTime
    ) {
        Event event = Event.of(
                place,
                eventName,
                eventStatus,
                eventStartDateTime,
                eventEndDateTime,
                0,
                24,
                "마스크 꼭 착용하세요"
        );
        // 스프링 부트 테스트에서 리플렉션 사용할 수 있도록 도와주는 유틸 클래스로 setter를 만들지 않았기 때문에 사용
        //: 영속성 컨텍스트에 넣기 때문에 제외
//        ReflectionTestUtils.setField(event, "id", id);

        return event;
    }

    //이 엔티티도 영속화를 해야한다.
    private Place createPlace() {
        Place place = Place.of(PlaceType.COMMON, "test place", "test address", "010-1234-1234", 10, null);
        //: 영속성 컨텍스트에 넣기 때문에 제외
//        ReflectionTestUtils.setField(place, "id", 1L);
        return place;
    }

    //테스트 메서드 기본 세팅
    @DisplayName("기본 테스트")
    @Test
    public void eventDependsPlaceEntity_test() throws Exception {
        //given
        //: 필요한 데이터를 테스트 엔티티매니저로 추가
        //: EventServiceTest에서 만들어둔 더미 오브젝트 복사해와서 사용

        Place place = createPlace();
        Event event= createEvent(place);
        //이벤트가 플레이스에 의존하는 형태로 만들어야한다. -> 오버라이딩
        //-> 사용할 엔티티는 모두 영속화 필요

        testEntityManager.persist(place);
        testEntityManager.persist(event);


        //when
        //: EventRepository의 QueryPredicateExecutor 테스트
        //인터페이스인 Predicate를 구현한 BooleanBuilder
        Iterable<Event> events = eventRepository.findAll(new BooleanBuilder());

        //then
        assertThat(events).hasSize(27);
    }

    @DisplayName("이벤트 뷰 데이터를 검색 파라미터와 함께 조회하면, 조건에 맞는 데이터를 페이징 처리하여 리턴한다.")
    @Test
    void givenSearchParams_whenFindingEventViewPage_thenReturnsEventViewResponsePage() {
        //given
        //:Data.sql에 테스트할 데이터를 추가해놨다.

        //when
        Page<EventViewResponse> eventPage = eventRepository.findEventViewPageBySearchParams(
                "배드민턴",
                "운동1",
                EventStatus.OPENED,
                LocalDateTime.of(2021, 1, 1, 0, 0, 0),
                LocalDateTime.of(2021, 1, 2, 0, 0, 0),
                //Pageable 요청 부분과 응답부분이 별도로 구현되어있다.
                PageRequest.of(0, 5)
        );//일부 일치, 정확히 일치

        //then
        assertThat(eventPage.getTotalPages()).isEqualTo(1);
        //총 페이지 수 1
        assertThat(eventPage.getNumberOfElements()).isEqualTo(1);
        //현재 페이지에 요소 수 1
        assertThat(eventPage.getTotalElements()).isEqualTo(1);
        //총 요소의 수 1
        assertThat(eventPage.getContent().get(0))
                .hasFieldOrPropertyWithValue("placeName", "서울 배드민턴장")
                .hasFieldOrPropertyWithValue("eventName", "운동1")
                .hasFieldOrPropertyWithValue("eventStatus", EventStatus.OPENED)
                .hasFieldOrPropertyWithValue("eventStartDatetime", LocalDateTime.of(2021, 1, 1, 9, 0, 0))
                .hasFieldOrPropertyWithValue("eventEndDatetime", LocalDateTime.of(2021, 1, 1, 12, 0, 0));
    }



    @DisplayName("이벤트 뷰 데이터 검색어에 따른 조회 결과가 없으면, 빈 데이터를 페이징 정보와 함께 리턴한다.")
    @Test
    void givenSearchParams_whenFindingNonexistentEventViewPage_thenReturnsEmptyEventViewResponsePage() {
        // Given

        // When
        Page<EventViewResponse> eventPage = eventRepository.findEventViewPageBySearchParams(
                "없은 장소",
                "없는 이벤트",
                null,
                LocalDateTime.of(1000, 1, 1, 1, 1, 1),
                LocalDateTime.of(1000, 1, 1, 1, 1, 0),
                PageRequest.of(0, 5)
        );

        // Then
        assertThat(eventPage).hasSize(0);
    }

    @DisplayName("이벤트 뷰 데이터를 검색 파라미터 없이 페이징 값만 주고 조회하면, 전체 데이터를 페이징 처리하여 리턴한다.")
    @Test
    void givenPagingInfoOnly_whenFindingEventViewPage_thenReturnsEventViewResponsePage() {
        // Given

        // When
        Page<EventViewResponse> eventPage = eventRepository.findEventViewPageBySearchParams(
                null,
                null,
                null,
                null,
                null,
                PageRequest.of(0, 5)
        );

        // Then
        assertThat(eventPage).hasSize(5);
    }

    @DisplayName("이벤트 뷰 데이터를 페이징 정보 없이 조회하면, 에러를 리턴한다.")
    @Test
    void givenNothing_whenFindingEventViewPage_thenThrowsError() {
        // Given

        // When
        Throwable t = catchThrowable(() -> eventRepository.findEventViewPageBySearchParams(
                null,
                null,
                null,
                null,
                null,
                null
        ));

        // Then
        assertThat(t).isInstanceOf(InvalidDataAccessApiUsageException.class);
    }

    //EAGER 사용하는 경우 독립적으로 동작하는지 테스트
    //: findAll() 테스트 -> 반드시 Join을 보장하지 않는다.

    @Test
    public void eagerJoinTest_test() throws Exception{
        //given


        //when
        List<Event> list = eventRepository.findAll();

        //then

    }
}