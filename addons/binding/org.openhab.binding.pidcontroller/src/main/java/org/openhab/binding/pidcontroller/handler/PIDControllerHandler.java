/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pidcontroller.handler;

import static org.openhab.binding.pidcontroller.PIDControllerBindingConstants.*;

import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.pidcontroller.internal.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PIDControllerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author George Erhan - Initial contribution
 */
public class PIDControllerHandler extends BaseThingHandler {
    /*
     * Logger for class
     */
    private Logger logger = LoggerFactory.getLogger(PIDControllerHandler.class);
    /*
     * Variable for the channel input
     */
    private BigDecimal PIDinput = BigDecimal.valueOf(0);
    /*
     * Variable for the setpoint
     */
    private BigDecimal PIDsetpoint = BigDecimal.valueOf(0);
    /*
     * Optional variable for the loop duration. If not set defaults to 1000 millisecond
     */
    private int PIDLoopTime = LoopTimeDefault;
    /*
     * variables for the output of the controller
     */
    private BigDecimal PIDOutput = BigDecimal.valueOf(0);
    private BigDecimal PIDOutputLowerLimit = BigDecimal.valueOf(0);
    private BigDecimal PIDOutputUpperLimit = BigDecimal.valueOf(0);
    /*
     * variables for the tuning of the controller
     */
    private BigDecimal Kpadjuster = BigDecimal.valueOf(1);
    private BigDecimal Kiadjuster = BigDecimal.valueOf(1);
    private BigDecimal Kdadjuster = BigDecimal.valueOf(1);
    /*
     * variable for the loop of controller
     */
    private ScheduledFuture<?> controllerjob;
    /*
     * variable retainer of the channelUID of the output channel
     */
    private ChannelUID outputChannelUID;
    /*
     * Instantiation object of the controller calculator
     * /org.openhab.binding.pidcontroller/src/main/java/org/openhab/binding/pidcontroller/internal/Controller.java
     */
    private Controller controller = new Controller();

    public PIDControllerHandler(Thing thing) {
        super(thing);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.eclipse.smarthome.core.thing.binding.ThingHandler#handleCommand(org.eclipse.smarthome.core.thing.ChannelUID,
     * org.eclipse.smarthome.core.types.Command)
     */

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case input:
                if (command != null) {
                    PIDinput = BigDecimal.valueOf(Double.parseDouble(command.toString()));
                    logger.debug("input value {} for channel {}", PIDinput, channelUID);
                } else {
                    logger.debug("Channel value can not be null");
                }
                break;
            case setpoint:
                if (command != null) {
                    PIDsetpoint = BigDecimal.valueOf(Double.parseDouble(command.toString()));
                    logger.debug("setpoint value {} for channel {}", PIDsetpoint, channelUID);
                } else {
                    logger.debug("Channel value can not be null");
                }
                break;
            case LoopTime:
                if (command != null) {
                    PIDLoopTime = Integer.parseInt(command.toString());
                    logger.debug("Initializing loop time for controller with value: {}", PIDLoopTime);
                    disposejob();
                    initialize();

                } else {
                    logger.debug("Channel value can not be null");
                }
                break;
            case kpadjuster:
                if (command != null) {
                    Kpadjuster = BigDecimal.valueOf(Double.parseDouble(command.toString()));
                    logger.debug("Kp adjust value {} for channel {}", Kpadjuster, channelUID);
                } else {
                    logger.debug("Channel value can not be null");
                }
                break;
            case kiadjuster:
                if (command != null) {
                    Kiadjuster = BigDecimal.valueOf(Double.parseDouble(command.toString()));
                    logger.debug("Ki adjust {} for channel {}", Kiadjuster, channelUID);
                } else {
                    logger.debug("Channel value can not be null");
                }
                break;
            case kdadjuster:
                if (command != null) {
                    Kdadjuster = BigDecimal.valueOf(Double.parseDouble(command.toString()));
                    logger.debug("Kd adjust value {} for channel {}", Kdadjuster, channelUID);
                } else {
                    logger.debug("Channel value can not be null");
                }
                break;
            case pidlowerlimit:
                if (command != null) {
                    PIDOutputLowerLimit = BigDecimal.valueOf(Double.parseDouble(command.toString()));
                    logger.debug("PIDLowerLimit value {} for channel {}", PIDOutputLowerLimit, channelUID);
                } else {
                    logger.debug("Channel value can not be null");
                }
                break;
            case pidupperlimit:
                if (command != null) {
                    PIDOutputUpperLimit = BigDecimal.valueOf(Double.parseDouble(command.toString()));
                    logger.debug("PIDLowerLimit value {} for channel {}", PIDOutputUpperLimit, channelUID);
                } else {
                    logger.debug("Channel value can not be null");
                }
                break;

        }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#handleUpdate(org.eclipse.smarthome.core.thing.
     * ChannelUID, org.eclipse.smarthome.core.types.State)
     */
    @Override
    public void handleUpdate(ChannelUID channelUID, State channelstate) {

        switch (channelUID.getId()) {
            case input:
                if (channelstate != null) {
                    PIDinput = BigDecimal.valueOf(Double.parseDouble(channelstate.toString()));
                    logger.debug("input value{} for channel {}", PIDinput, channelUID);
                } else {
                    logger.debug("Channel value can not be null");
                }
                break;
            case setpoint:
                if (channelstate != null) {
                    PIDsetpoint = BigDecimal.valueOf(Double.parseDouble(channelstate.toString()));
                    logger.debug("setpoint value {} for channel {}", PIDsetpoint, channelUID);
                } else {
                    logger.debug("Channel value can not be null");
                }
                break;
            case LoopTime:
                if (channelstate != null) {
                    PIDLoopTime = Integer.parseInt(channelstate.toString());
                    logger.debug("Initializing controller loop in update handler");
                    disposejob();
                    initialize();

                    logger.debug(" de loop time value {} for channel {}", PIDLoopTime, channelUID);
                } else {
                    logger.debug("Channel value can not be null");
                }
                break;
            case kpadjuster:
                if (channelstate != null) {
                    Kpadjuster = BigDecimal.valueOf(Double.parseDouble(channelstate.toString()));
                    logger.debug("Kp adjust value {} for channel {}", PIDsetpoint, channelUID);
                } else {
                    logger.debug("Channel value can not be null");
                }
                break;
            case kiadjuster:
                if (channelstate != null) {
                    Kiadjuster = BigDecimal.valueOf(Double.parseDouble(channelstate.toString()));
                    logger.debug("Ki adjust {} for channel {}", PIDsetpoint, channelUID);
                } else {
                    logger.debug("Channel value can not be null");
                }
                break;
            case kdadjuster:
                if (channelstate != null) {
                    Kdadjuster = BigDecimal.valueOf(Double.parseDouble(channelstate.toString()));
                    logger.debug("valoare de Kd adjust {} for channel {}", PIDsetpoint, channelUID);
                } else {
                    logger.debug("Channel value can not be null");
                }
                break;
            case pidlowerlimit:
                if (channelstate != null) {
                    PIDOutputLowerLimit = BigDecimal.valueOf(Double.parseDouble(channelstate.toString()));
                    logger.debug("valoare de PIDLowerLimit adjust {} for channel {}", PIDOutputLowerLimit, channelUID);
                } else {
                    logger.debug("Channel value can not be null");
                }
                break;
            case pidupperlimit:
                if (channelstate != null) {
                    PIDOutputUpperLimit = BigDecimal.valueOf(Double.parseDouble(channelstate.toString()));
                    logger.debug("valoare de PIDLowerLimit adjust {} for channel {}", PIDOutputUpperLimit, channelUID);
                } else {
                    logger.debug("Channel value can not be null");
                }
                break;

        }

    }

    /*
     * for alpha version this is the call for the PID calculations and posting of the output to the channel output
     */
    public void postToOutput() {
        if (PIDOutputLowerLimit.compareTo(BigDecimal.valueOf(0)) == 0
                && PIDOutputUpperLimit.compareTo(BigDecimal.valueOf(0)) == 0) {
            PIDOutputLowerLimit = BigDecimal.valueOf(-255);
            PIDOutputUpperLimit = BigDecimal.valueOf(255);
        }
        logger.debug("Upper Limit :{}", PIDOutputUpperLimit);

        PIDOutput = controller.PIDCalculation(PIDinput, PIDsetpoint, PIDLoopTime, PIDOutputLowerLimit,
                PIDOutputUpperLimit, Kpadjuster, Kiadjuster, Kdadjuster);

        StringBuilder tempstring2 = new StringBuilder();
        tempstring2.append(PIDOutput);
        updateState(outputChannelUID.getId(), DecimalType.valueOf(tempstring2.toString()));
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#channelLinked(org.eclipse.smarthome.core.thing.
     * ChannelUID)
     * For now only the output channel is retained in a variable accessible to the class
     */
    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("channelLinked: {}", channelUID);
        if (channelUID.getId().equals(output))

        {
            outputChannelUID = channelUID;
        }

    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.smarthome.core.thing.binding.BaseThingHandler#initialize()
     */
    @Override
    public void initialize() {

        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.ONLINE);
        /*
         * The runnable is a polling type of initialization
         */
        Runnable calculations = new Runnable() {
            @Override

            public void run() {

                if (PIDsetpoint.subtract(PIDinput).compareTo(BigDecimal.valueOf(0)) == 0) {
                    logger.debug(
                            "No calculations are done for controller because there is no difference between input and setpoint");

                } else {
                    postToOutput();
                }
            }
        };

        if (PIDLoopTime == 0) {
            PIDLoopTime = LoopTimeDefault;
        }
        StringBuilder strPIDLoopTime = new StringBuilder();
        strPIDLoopTime.append(PIDLoopTime);
        logger.debug(strPIDLoopTime.toString());
        /*
         * Scheduling of the runnable
         */
        controllerjob = scheduler.scheduleAtFixedRate(calculations, 0, PIDLoopTime, TimeUnit.MILLISECONDS);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    /*
     * Dispose the runnable
     */
    private void disposejob() {
        controllerjob.cancel(true);
        controllerjob = null;

    }

}
