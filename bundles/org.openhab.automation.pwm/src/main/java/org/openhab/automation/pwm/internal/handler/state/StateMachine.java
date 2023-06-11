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

import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The context of all states.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class StateMachine {
    private ScheduledExecutorService scheduler;
    private Consumer<Boolean> controlOutput;
    private State state;
    private long periodMs;
    private double dutycycle;

    public StateMachine(ScheduledExecutorService scheduler, Consumer<Boolean> controlOutput, long periodMs) {
        this.scheduler = scheduler;
        this.controlOutput = controlOutput;
        this.periodMs = periodMs;
        this.state = new AlwaysOffState(this);
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public void setDutycycle(double newDutycycle) {
        if (dutycycle != newDutycycle) {
            this.dutycycle = newDutycycle;
            state.dutyCycleChanged();
        }

        state.dutyCycleUpdated();
    }

    public double getDutycycle() {
        return dutycycle;
    }

    public long getPeriodMs() {
        return periodMs;
    }

    public State getState() {
        return state;
    }

    public void setState(State current) {
        this.state = current;
    }

    public void controlOutput(boolean on) {
        controlOutput.accept(on);
    }

    public void reset() {
        state.nextState(OnState::new);
    }

    public void stop() {
        state.nextState(AlwaysOffState::new);
    }
}
