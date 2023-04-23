package com.uno.getinline.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.uno.getinline.constant.EventStatus;
import com.uno.getinline.constant.PlaceType;
import com.uno.getinline.domain.Event;
import com.uno.getinline.domain.Place;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EventRepositoryTest {
    //DataJpaTest 기본 세팅
    //: 의존성 주입 방법 중 생성자 주입 + @Autorwired
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
        Assertions.assertThat(events).hasSize(7);

    }

}