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
package org.openhab.binding.homekit.internal.camera.go2rtc;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Decides when the camera media session should be opened or closed, based on whether go2rtc has consumers.
 * <p>
 * The session is closed only after demand has been absent for a grace period, so that a browser that briefly
 * reconnects does not restart the camera stream. A failed open is not retried until a retry delay has passed.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public final class Go2RtcDemand {

    /**
     * The action the caller should take after an {@link #update} call.
     */
    public enum Action {
        NONE,
        OPEN,
        CLOSE
    }

    static final Duration CLOSE_GRACE = Duration.ofSeconds(15);
    static final Duration OPEN_RETRY_DELAY = Duration.ofSeconds(30);

    private @Nullable Instant idleSince;
    private @Nullable Instant nextOpenAttempt;
    private @Nullable Instant holdUntil;

    /**
     * Evaluates the current demand.
     *
     * @param consumersPresent true if go2rtc has at least one consumer
     * @param sessionOpen true if the camera media session is currently open
     * @param now the current time
     * @return the action to take
     */
    public synchronized Action update(boolean consumersPresent, boolean sessionOpen, Instant now) {
        Instant hold = holdUntil;
        boolean wanted = consumersPresent || (hold != null && now.isBefore(hold));
        if (!sessionOpen) {
            idleSince = null;
            Instant retryAt = nextOpenAttempt;
            return wanted && (retryAt == null || !now.isBefore(retryAt)) ? Action.OPEN : Action.NONE;
        }
        if (wanted) {
            idleSince = null;
            return Action.NONE;
        }
        Instant idle = idleSince;
        if (idle == null) {
            idleSince = now;
            return Action.NONE;
        }
        return now.isBefore(idle.plus(CLOSE_GRACE)) ? Action.NONE : Action.CLOSE;
    }

    /**
     * Records that an open attempt failed, so that it is not repeated before the retry delay has passed.
     *
     * @param now the time of the failure
     */
    public synchronized void openFailed(Instant now) {
        nextOpenAttempt = now.plus(OPEN_RETRY_DELAY);
    }

    /**
     * Keeps the camera session wanted until the given time, even without go2rtc consumers. This gives a browser
     * that has just been redirected to go2rtc time to connect.
     *
     * @param until the time until which demand is held
     */
    public synchronized void hold(Instant until) {
        holdUntil = until;
    }

    /**
     * Clears the retry state, for example when the bridge is stopped.
     */
    public synchronized void reset() {
        idleSince = null;
        nextOpenAttempt = null;
        holdUntil = null;
    }
}
