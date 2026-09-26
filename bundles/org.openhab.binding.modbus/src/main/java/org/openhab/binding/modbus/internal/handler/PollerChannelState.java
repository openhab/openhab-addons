/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and accompanying materials are made available under the terms of the
 * Eclipse Public License 2.0 which is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.modbus.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * State contract for a poller-owned channel.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
final class PollerChannelState {

    private final PollerReadFailurePolicy failurePolicy;
    private State state = UnDefType.UNDEF;

    public PollerChannelState(PollerReadFailurePolicy failurePolicy) {
        this.failurePolicy = failurePolicy;
    }

    public synchronized void acceptSuccessfulState(State state) {
        this.state = state;
    }

    public synchronized void acceptReadFailure() {
        if (failurePolicy == PollerReadFailurePolicy.UNDEF) {
            state = UnDefType.UNDEF;
        }
    }

    public synchronized State currentState() {
        return state;
    }
}
