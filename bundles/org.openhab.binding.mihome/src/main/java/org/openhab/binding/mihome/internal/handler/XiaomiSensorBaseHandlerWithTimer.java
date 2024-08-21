/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mihome.internal.handler;

import java.util.Timer;
import java.util.TimerTask;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link XiaomiSensorBaseHandlerWithTimer} is an abstract class for sensor devices
 * which use a timer to update a certain channel. The user can configure the timer via an item
 * value.
 *
 * @author Dieter Schmidt - Initial contribution
 *
 */
public abstract class XiaomiSensorBaseHandlerWithTimer extends XiaomiSensorBaseHandler {

    private int defaultTimer;
    private int minTimer;
    private Integer timerSetpoint;
    private final String setpointChannel;
    private boolean timerIsRunning;
    private Timer trigger = new Timer();

    private final Logger logger = LoggerFactory.getLogger(XiaomiSensorBaseHandlerWithTimer.class);

    public XiaomiSensorBaseHandlerWithTimer(Thing thing, int defaultTimer, int minTimer, String setpointChannel) {
        super(thing);
        this.defaultTimer = defaultTimer;
        this.minTimer = minTimer;
        this.timerSetpoint = defaultTimer;
        this.setpointChannel = setpointChannel;
    }

    class TimerAction extends TimerTask {
        @Override
        public synchronized void run() {
            onTimer();
            timerIsRunning = false;
        }
    }

    synchronized void startTimer() {
        cancelRunningTimer();
        logger.debug("Setting timer to {}s", timerSetpoint);
        trigger.schedule(new TimerAction(), timerSetpoint * 1000);
        timerIsRunning = true;
    }

    synchronized void cancelRunningTimer() {
        if (timerIsRunning) {
            trigger.cancel();
            logger.debug("Cancelled running timer");
            trigger = new Timer();
            timerIsRunning = false;
        }
    }

    abstract void onTimer();

    void setTimerFromDecimalType(DecimalType value) {
        try {
            int newValue = value.intValue();
            timerSetpoint = newValue < minTimer ? minTimer : newValue;
            if (timerSetpoint == minTimer) {
                updateState(setpointChannel, new DecimalType(timerSetpoint));
            }
        } catch (NumberFormatException e) {
            logger.debug("Cannot parse the value {} to an Integer", value);
            timerSetpoint = defaultTimer;
        }
    }
}
