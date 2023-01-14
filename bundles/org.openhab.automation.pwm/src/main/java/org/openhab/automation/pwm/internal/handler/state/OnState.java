/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.automation.pwm.internal.handler.state;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Active when, the output is currently ON and the duty cycle is between 0% and 100% (exclusively).
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class OnState extends State {
    private @NonNullByDefault({}) ScheduledFuture<?> offTimer;
    private Instant enabledAt = Instant.now();

    public OnState(StateMachine context) {
        super(context);

        context.controlOutput(true);

        startOnTimer(calculateOnTimeMs(context.getDutycycle()));
    }

    private void startOnTimer(long timeMs) {
        offTimer = scheduler.schedule(() -> {
            if (Math.round(context.getDutycycle()) >= 100) {
                nextState(DutycycleHundredState::new);
            } else {
                nextState(OffState::new);
            }
        }, timeMs, TimeUnit.MILLISECONDS);
    }

    @Override
    public void dutyCycleChanged() {
        // end current ON phase prematurely or extend it if the new duty cycle demands it
        offTimer.cancel(false);

        long newOnTimeMs = calculateOnTimeMs(context.getDutycycle());
        long elapsedMs = enabledAt.until(Instant.now(), ChronoUnit.MILLIS);

        if (elapsedMs - newOnTimeMs > 0) {
            nextState(OffState::new);
        } else {
            startOnTimer(newOnTimeMs - elapsedMs);
        }
    }

    @Override
    protected void dutyCycleUpdated() {
        // nothing
    }

    @Override
    public void dispose() {
        offTimer.cancel(false);
    }
}
