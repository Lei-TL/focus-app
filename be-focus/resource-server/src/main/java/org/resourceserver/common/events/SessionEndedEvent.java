package org.resourceserver.common.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SessionEndedEvent extends ApplicationEvent {
    private final Long roomId;
    private final String sessionId;

    public SessionEndedEvent(Object source, Long roomId, String sessionId) {
        super(source);
        this.roomId = roomId;
        this.sessionId = sessionId;
    }
}
