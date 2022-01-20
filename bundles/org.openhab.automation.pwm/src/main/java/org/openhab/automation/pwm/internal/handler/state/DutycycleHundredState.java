/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.eclipse.jdt.annotation.Nullable;

/**
 * Active when, the PWM period ended with a duty cycle set to 100%.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class DutycycleHundredState extends State {
    private ScheduledFuture<?> periodTimer;
    private @Nullable ScheduledFuture<?> offTimer;
    private Instant enabledAt = Instant.now();
    private boolean dutyCycleChanged;

    public DutycycleHundredState(StateMachine context) {
        super(context);

        controlOutput(true);

        periodTimer = scheduler.schedule(this::periodEnded, context.getPeriodMs(), TimeUnit.MILLISECONDS);
    }

    private void periodEnded() {
        long dutycycleRounded = Math.round(context.getDutycycle());

        if (!dutyCycleChanged && dutycycleRounded <= 0) {
            nextState(AlwaysOffState::new);
        } else if (!dutyCycleChanged && dutycycleRounded >= 100) {
            nextState(AlwaysOnState::new);
        } else {
            nextState(OnState::new);
        }
    }

    @Override
    public void dutyCycleChanged() {
        dutyCycleChanged = true;

        long newOnTimeMs = calculateOnTimeMs(context.getDutycycle());
        long elapsedMs = enabledAt.until(Instant.now(), ChronoUnit.MILLIS);

        if (elapsedMs - newOnTimeMs > 0) {
            controlOutput(false);
        } else {
            ScheduledFuture<?> timer = offTimer;
            if (timer != null) {
                timer.cancel(false);
            }
            offTimer = scheduler.schedule(() -> controlOutput(false), newOnTimeMs - elapsedMs, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    protected void dutyCycleUpdated() {
        // nothing
    }

    @Override
    public void dispose() {
        periodTimer.cancel(false);

        ScheduledFuture<?> timer = offTimer;
        if (timer != null) {
            timer.cancel(false);
        }
    }
}
