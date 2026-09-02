/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.dahuadoor.internal.media;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Immutable SIP session snapshot for one HTTP session.
 *
 * @author Sven Schad - Initial contribution
 */
@NonNullByDefault
public final class SipBackchannelSession {

    private final String sessionId;
    private final String clientId;
    private final String thingUid;
    private final String callerId;
    private final String callState;
    private final @Nullable String inviteSdp;
    private final long createdAtMs;
    private final long updatedAtMs;

    public SipBackchannelSession(String sessionId, String clientId, String thingUid, String callerId, String callState,
            @Nullable String inviteSdp, long createdAtMs, long updatedAtMs) {
        this.sessionId = sessionId;
        this.clientId = clientId;
        this.thingUid = thingUid;
        this.callerId = callerId;
        this.callState = callState;
        this.inviteSdp = inviteSdp;
        this.createdAtMs = createdAtMs;
        this.updatedAtMs = updatedAtMs;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getThingUid() {
        return thingUid;
    }

    public String getCallerId() {
        return callerId;
    }

    public String getCallState() {
        return callState;
    }

    public @Nullable String getInviteSdp() {
        return inviteSdp;
    }

    public long getCreatedAtMs() {
        return createdAtMs;
    }

    public long getUpdatedAtMs() {
        return updatedAtMs;
    }
}
