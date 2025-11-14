package com.team.updevic001.model.mappers;

import com.team.updevic001.dao.entities.EventEntity;
import com.team.updevic001.model.dtos.request.EventRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventMapper {

    public EventEntity toEntity(EventRequest request) {
        return new EventEntity(

        );
    }

}
