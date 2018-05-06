/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mihome.handler;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.State;
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
    };

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

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        if (setpointChannel.equals(channelUID.getId())) {
            if (newState instanceof DecimalType) {
                logger.debug("Received update for timer setpoint channel: {}", newState);
                timerSetpoint = ((DecimalType) newState).intValue();
            }
        }
    }
}
