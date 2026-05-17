package org.resourceserver.realtime.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.resourceserver.common.response.AppEvent;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WsMessage<T> {
    private AppEvent event;
    private T data;
}
