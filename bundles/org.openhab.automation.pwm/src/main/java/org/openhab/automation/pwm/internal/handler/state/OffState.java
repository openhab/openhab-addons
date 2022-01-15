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

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Active when, the output is currently OFF and the duty cycle is between 0% and 100% (exclusively).
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class OffState extends State {
    ScheduledFuture<?> offTimer;

    public OffState(StateMachine context) {
        super(context);

        controlOutput(false);

        long offTimeMs = context.getPeriodMs() - calculateOnTimeMs(context.getDutycycle());
        offTimer = scheduler.schedule(this::periodEnded, offTimeMs, TimeUnit.MILLISECONDS);
    }

    private void periodEnded() {
        long dutycycleRounded = Math.round(context.getDutycycle());

        if (dutycycleRounded <= 0) {
            nextState(DutycycleZeroState::new);
        } else if (dutycycleRounded >= 100) {
            nextState(DutycycleHundredState::new);
        } else {
            nextState(OnState::new);
        }
    }

    @Override
    public void dutyCycleChanged() {
        // nothing
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
