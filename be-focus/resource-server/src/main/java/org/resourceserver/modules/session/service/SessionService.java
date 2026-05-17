package org.resourceserver.modules.session.service;

import org.resourceserver.modules.session.dto.SoloSessionRequest;

public interface SessionService {
    void saveSoloSession(SoloSessionRequest request);
}
