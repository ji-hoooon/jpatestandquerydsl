package com.uno.getinline.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.uno.getinline.constant.EventStatus;
import com.uno.getinline.constant.PlaceType;
import com.uno.getinline.domain.Event;
import com.uno.getinline.domain.Place;
import com.uno.getinline.dto.EventViewResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DB-이벤트")
@DataJpaTest
class EventRepositoryTest {
    //DataJpaTest 기본 세팅
    //: 의존성 주입 방법 중 생성자 주입 시에 @Autowired 필수
    private final EventRepository sut;
    private final TestEntityManager testEntityManager;

    public EventRepositoryTest(
            @Autowired EventRepository sut,
            @Autowired TestEntityManager testEntityManager) {
        this.sut = sut;
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
    @DisplayName("asdf")
    @Test
    public void _test() throws Exception {
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
        Iterable<Event> events = sut.findAll(new BooleanBuilder());

        //then
        assertThat(events).hasSize(7);

    }


    @Test
    public void givenSearchParams_wehnFindingEventViewResponse_thenReturnsEventViewResponsePage_test() throws Exception{
        //given
        //:Data.sql에 테스트할 데이터를 추가해놨다.

        //when
        Page<EventViewResponse> eventPage = sut.findEventViewPageBySearchParams(
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
                .hasFieldOrPropertyWithValue("placeName","서울 배드민턴장")
                .hasFieldOrPropertyWithValue("eventName","운동1")
                .hasFieldOrPropertyWithValue("eventStatus",EventStatus.OPENED)
                .hasFieldOrPropertyWithValue("eventStartDateTime",LocalDateTime.of(2021, 1, 1, 9, 0, 0))
                .hasFieldOrPropertyWithValue("eventEndDateTime", LocalDateTime.of(2021, 1, 2, 12, 0, 0));
    }


}