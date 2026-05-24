package org.resourceserver.common.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserJoinedEvent extends ApplicationEvent {
    private final Long roomId;
    private final String userId;

    public UserJoinedEvent(Object source, Long roomId, String userId) {
        super(source);
        this.roomId = roomId;
        this.userId = userId;
    }
}
