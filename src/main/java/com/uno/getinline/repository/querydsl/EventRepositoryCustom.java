package com.uno.getinline.repository.querydsl;

import com.uno.getinline.constant.EventStatus;
import com.uno.getinline.dto.EventViewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

//QueryDSL를 간접적으로 이용해 만든 EventRepository를 QueryDSL를 직접 구현하기 위한 리포지토리
public interface EventRepositoryCustom{
    //EventViewResponse DTO 작성
    //: 엔티티를 직접 의존하지 않고, 필요한 필드만 가져온다.
    Page<EventViewResponse> findEventViewPageBySearchParams(
            //EventService에 getEvents을 리팩토링해서 사용
//            Long placeId,
            String placeName,
            String eventName,
            EventStatus eventStatus,
            LocalDateTime eventStartDatetime,
            LocalDateTime eventEndDatetime,
            //+) 페이징 객체
            Pageable pageable
    );
}
