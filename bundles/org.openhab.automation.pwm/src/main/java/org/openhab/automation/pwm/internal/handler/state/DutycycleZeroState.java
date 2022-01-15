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
 * Active when, the PWM period ended with a duty cycle set to 0%.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class DutycycleZeroState extends State {
    private ScheduledFuture<?> periodTimer;

    public DutycycleZeroState(StateMachine context) {
        super(context);

        controlOutput(false);

        periodTimer = scheduler.schedule(this::periodEnded, context.getPeriodMs(), TimeUnit.MILLISECONDS);
    }

    private void periodEnded() {
        long dutycycleRounded = Math.round(context.getDutycycle());

        if (dutycycleRounded <= 0) {
            nextState(AlwaysOffState::new);
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
        periodTimer.cancel(false);
    }
}
