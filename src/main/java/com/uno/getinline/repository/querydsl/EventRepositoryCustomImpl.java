package com.uno.getinline.repository.querydsl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.uno.getinline.constant.ErrorCode;
import com.uno.getinline.constant.EventStatus;
import com.uno.getinline.domain.Event;
import com.uno.getinline.domain.QEvent;
import com.uno.getinline.dto.EventViewResponse;
import com.uno.getinline.exception.GeneralException;
import com.uno.getinline.repository.EventRepository;
import io.micrometer.core.instrument.util.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//QueryDSL를 이용한 구현체를 만들어줘야한다. -> QuerydslRespositorySupport 상속 필요 -> 해당 리포지토리의 도메인을 전달한 부모의 생성자 호출
//: QuerydslRepositorySupport안에 엔티티 매니저가 있기 때문에 직접 엔티티 매니저 주입할 필요 없다.
public class EventRepositoryCustomImpl extends QuerydslRepositorySupport implements EventRepositoryCustom {
    /**
     * Creates a new {@link org.springframework.data.jpa.repository.support.QuerydslRepositorySupport} instance for the given domain type.
     *
     * @param domainClass must not be {@literal null}.
     */
    public EventRepositoryCustomImpl() {
//    public EventRepositoryCustomImpl(Class<?> domainClass) {
//        super(domainClass);
        super(Event.class);
    }

    @Override
    public Page<EventViewResponse> findEventViewPageBySearchParams(
            String placeName,
            String eventName,
            EventStatus eventStatus,
            LocalDateTime eventStartDatetime,
            LocalDateTime eventEndDatetime,
            Pageable pageable) {
        //큐클래스 이용
        QEvent event = QEvent.event;

        //타입 세이프티하고 스프링 데이터 JPA가 지원하는 JPQLQuery (JPAQueryFactory도 사용가능하지만 JPA와 무관)
        //:QueryDSL이 지원하는 기능 사용
        JPQLQuery<EventViewResponse> query = from(event)
                .select(Projections.constructor(
                        //타입 지정 필요
                        EventViewResponse.class,
                        //원하는 필드 주입
                        event.id,
                        event.place.placeName,
                        event.eventName,
                        event.eventStatus,
                        event.eventStartDatetime,
                        event.eventEndDatetime,
                        event.currentNumberOfPeople,
                        event.capacity,
                        event.memo
                ));
        //메서드 체이닝으로 쿼리 생성 가능
        //: 다이내믹하게 where 절을 추가 -> 해당 필드가 있을 때만 검색하도록

        //StringUtils를 이용해서 한방에 표현도 가능하다.
//        if(StringUtils.isNotBlank(placeName)) {
        //독립적인 IF로 존재할 때 추가하도록 한다.
        if (placeName != null && !placeName.isBlank()) {
            //like가 아닌 기능이 더 많은 contains 이용 -> 대소문자 구분하지 않도록 설정
            query.where(event.place.placeName.containsIgnoreCase(placeName));
        }
        if (eventName != null && !eventName.isBlank()) {
            //like가 아닌 기능이 더 많은 contains 이용 -> 대소문자 구분하지 않도록 설정
            query.where(event.eventName.containsIgnoreCase(eventName));
        }
        if (eventStatus != null) { //Enum이므로 똑같은지만 확인
            //eq? equals? 뭔차이지
            query.where(event.eventStatus.eq(eventStatus));
        }
        //날짜범위는 goe 이상, loe 이하 사용
        if (eventStartDatetime != null) {
            query.where(event.eventStartDatetime.goe(eventStartDatetime));
        }
        if (eventEndDatetime!=null) {
            query.where(event.eventEndDatetime.loe(eventEndDatetime));
        }

        //queryDSL 클래스의 getQueryDsl()가 인스턴스를 돌려주고, 실제로 원하는 타입을 리턴
        //이후 페이징 진행 -> fetch()해서 반환

//        List<EventViewResponse> events=getQuerydsl()
//                .applyPagination(pageable, query)
//                .fetch();
        //getQuerydsl아 null이 가능하므로 null 처리 수행
        List<EventViewResponse> events= Optional.ofNullable(getQuerydsl())
                .orElseThrow(()->new GeneralException(ErrorCode.DATA_ACCESS_ERROR, "Spring Data JPA로부터 Querydsl 인스턴스를 못가져옴"))
                .applyPagination(pageable, query)
                .fetch();

        //페이지 방식으로 변환하기 위한 파라미터 : 리스트, 페이징 정보, 페이징 처리하지 않는 총 사이즈 (쿼리의 카운트 쿼리)
        return new PageImpl<>(events, pageable, query.fetchCount());
    }
}
