//package com.uno.getinline.repository.jooq;
//
//import com.uno.getinline.constant.EventStatus;
//import com.uno.getinline.dto.EventViewResponse;
//import com.uno.getinline.tables.Event;
//import com.uno.getinline.tables.Place;
//import lombok.RequiredArgsConstructor;
//import org.jooq.Condition;
//import org.jooq.DSLContext;
//import org.jooq.SelectField;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.stereotype.Repository;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//import static org.jooq.impl.DSL.trueCondition;
//
//@RequiredArgsConstructor
//@Repository
//public class EventRepositoryJooqImpl implements EventRepositoryJooq {
//    //starter-jooq에 포함된 빈 주입
//    private final DSLContext dslContext;
//    //: 도메인에도 엔티티가 존재하고, Jooq가 만든 엔티티가 존재하는데, Jooq가 만든 엔티티를 사용한다.
//
//    @Override
//    public Page<EventViewResponse> findEventViewPageBySearchParams(
//            String placeName,
//            String eventName,
//            EventStatus eventStatus,
//            LocalDateTime eventStartDatetime,
//            LocalDateTime eventEndDatetime,
//            Pageable pageable) {
//        //클린 후 빌드하면 원하는 엔티티 발견가능
//        //: QueryDSL 사용불가, 하이버네이트의 기능을 사용하지 못한다.
//        final Event EVENT = Event.EVENT;
//        final Place PLACE = Place.PLACE;
//
//        //동적쿼리 작성시 사용하는 주크에서 제공하는 인터페이스로 무조건 실행하는 조건
//        Condition condition = trueCondition();
//
//        //조인시 원하는 필드의 타입을 정해주는 주크에서 제공하는 제네릭클래스를 이용해 만든다.
//        SelectField<?>[] select = {
//                EVENT.ID,
//                PLACE.PLACE_NAME,
//                EVENT.EVENT_NAME,
//                EVENT.EVENT_STATUS,
//                EVENT.EVENT_START_DATETIME,
//                EVENT.EVENT_END_DATETIME,
//                EVENT.CURRENT_NUMBER_OF_PEOPLE,
//                EVENT.CAPACITY,
//                EVENT.MEMO
//        };
//        //StringUtils를 이용해서 한방에 표현도 가능하다.
////        if(StringUtils.isNotBlank(placeName)) {
//        //독립적인 IF로 존재할 때 추가하도록 한다.
//        if (placeName != null && !placeName.isBlank()) {
//            //like가 아닌 기능이 더 많은 contains 이용 -> 대소문자 구분하지 않도록 설정
//            condition = condition.and(PLACE.PLACE_NAME.containsIgnoreCase(placeName));
//        }
//        if (eventName != null && !eventName.isBlank()) {
//            //like가 아닌 기능이 더 많은 contains 이용 -> 대소문자 구분하지 않도록 설정
//            condition = condition.and(EVENT.EVENT_NAME.contains(eventName));
//        }
//        if (eventStatus != null) { //Enum이므로 똑같은지만 확인
//            //eq? equals? 뭔차이지
//            condition = condition.and(EVENT.EVENT_STATUS.eq(eventStatus));
//        }
//        //날짜범위는 goe 이상, loe 이하 사용
//        if (eventStartDatetime != null) {
//            condition = condition.and(EVENT.EVENT_START_DATETIME.ge(eventStartDatetime));
//        }
//        if (eventEndDatetime != null) {
//            condition = condition.and(EVENT.EVENT_END_DATETIME.le(eventEndDatetime));
//        }
//
//        //마지막으로 카운트 쿼리 직접 작성
//        int count = dslContext
//                .selectCount()
//                .from(EVENT)
//                .innerJoin(PLACE)
//                //placeId로 FK키 주입
//                .onKey()
//                .where(condition)
//                //fetch종류 중에 결정
//                .fetchOneInto(int.class);
//
//        //리스트 쿼리로 페이징 수행
//        List<EventViewResponse> pagedList = dslContext
//                .select(select)
//                .from(EVENT)
//                .innerJoin(PLACE)
//                //placeId로 FK키 주입
//                .onKey()
//                .where(condition)
//                //페이징 조건 설정 -> 오프셋과 페이지 사이즈 전달
//                .limit(pageable.getOffset(), pageable.getPageSize())
//                //fetch종류 중에 결정
//                .fetchInto(EventViewResponse.class);
//
//        return new PageImpl<>(pagedList, pageable, count);
//    }
//}
