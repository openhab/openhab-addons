/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.hive.internal.client;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents a session with the Hive API.
 *
 * @author Ross Brown - Initial contribution
 */
@NonNullByDefault
public final class Session {
    private final SessionId sessionId;
    private final UserId userId;

    public Session(
            final SessionId sessionId,
            final UserId userId
    ) {
        Objects.requireNonNull(sessionId);
        Objects.requireNonNull(userId);

        this.sessionId = sessionId;
        this.userId = userId;
    }

    public SessionId getSessionId() {
        return sessionId;
    }

    public UserId getUserId() {
        return userId;
    }

    @Override
    public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Session session = (Session) o;
        return sessionId.equals(session.sessionId) &&
                userId.equals(session.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, userId);
    }

    @Override
    public String toString() {
        return "Session{" +
                "sessionId=" + sessionId +
                ", userId=" + userId +
                '}';
    }
}
