/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.boschindego.handler;

import static org.openhab.binding.boschindego.BoschIndegoBindingConstants.*;
import static org.openhab.binding.boschindego.internal.IndegoStateConstants.*;

import java.math.BigDecimal;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.zazaz.iot.bosch.indego.DeviceCommand;
import de.zazaz.iot.bosch.indego.DeviceStateInformation;
import de.zazaz.iot.bosch.indego.DeviceStatus;
import de.zazaz.iot.bosch.indego.IndegoAuthenticationException;
import de.zazaz.iot.bosch.indego.IndegoController;
import de.zazaz.iot.bosch.indego.IndegoException;

/**
 * The {@link BoschIndegoHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jonas Fleck - Initial contribution
 */
public class BoschIndegoHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(BoschIndegoHandler.class);
    private int commandToSend;

    private Thread pollingThread;
    private boolean running;

    public BoschIndegoHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // Currently manual refrshing is not possible in the moment
            return;
        } else if (channelUID.getId().equals(STATE) && command instanceof DecimalType) {
            if (command instanceof DecimalType) {
                synchronized (this) {
                    commandToSend = ((DecimalType) command).intValue();
                    this.notifyAll();
                }
            } else {
            }

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void updateConfiguration(Configuration configuration) {
        // TODO Auto-generated method stub
        super.updateConfiguration(configuration);

    }

    private synchronized void poll() throws InterruptedException {
        // Create controller instance
        try {
            IndegoController controller = new IndegoController(getConfig().get("username").toString(),
                    getConfig().get("password").toString());
            // Connect to server
            controller.connect();
            // Query the device state
            DeviceStateInformation state = controller.getState();
            DeviceStatus statusWithMessage = DeviceStatus.decodeStatusCode(state.getState());
            int eshStatus = getEshStatusFromCommand(statusWithMessage.getAssociatedCommand());
            int mowed = state.getMowed();
            int error = state.getError();
            int statecode = state.getState();
            boolean ready = isReadyToMow(state.getState(), state.getError());

            if (verifyCommand(commandToSend, eshStatus, state.getState(), error)) {
                logger.debug("Sending command...");
                updateState(TEXTUAL_STATE, new StringType("Refreshing..."));
                controller.sendCommand(getCommandFromEshStatus(commandToSend));
                commandToSend = 0;
                try {
                    for (int i = 0; i < 30; i++) {
                        DeviceStateInformation stateTmp = controller.getState();
                        if (state.getState() != stateTmp.getState()) {
                            state = stateTmp;
                            statusWithMessage = DeviceStatus.decodeStatusCode(state.getState());
                            eshStatus = getEshStatusFromCommand(statusWithMessage.getAssociatedCommand());
                            mowed = state.getMowed();
                            error = state.getError();
                            statecode = state.getState();
                            ready = isReadyToMow(state.getState(), state.getError());
                            break;
                        }
                        Thread.sleep(1000);
                    }

                } catch (InterruptedException e) {
                    controller.disconnect();
                    throw e;
                }
            }
            controller.disconnect();
            updateStatus(ThingStatus.ONLINE);
            updateState(STATECODE, new DecimalType(statecode));
            updateState(READY, new DecimalType(ready ? 1 : 0));
            updateState(ERRORCODE, new DecimalType(error));
            updateState(MOWED, new PercentType(mowed));
            updateState(STATE, new DecimalType(eshStatus));
            updateState(TEXTUAL_STATE, new StringType(statusWithMessage.getMessage()));

        } catch (IndegoAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        } catch (IndegoException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    private boolean isReadyToMow(int statusCode, int error) {
        // I don´t know why bosch uses different state codes for the same state.
        return (statusCode == STATE_DOCKED_1 || statusCode == STATE_DOCKED_2 || statusCode == STATE_DOCKED_3
                || statusCode == STATE_PAUSED || statusCode == STATE_IDLE_IN_LAWN) && error == 0;
    }

    private boolean verifyCommand(int command, int eshStatus, int statusCode, int errorCode) {
        // Mower reported an error
        if (errorCode != 0) {
            logger.error("The mower reported an error.");
            return false;
        }
        // Command out of range
        if (command < 1 || command > 3) {
            logger.debug("Command out of range");
            return false;
        }
        // Command is equal to current state
        if (command == eshStatus) {
            logger.debug("Command is equal to state");
            return false;
        }
        // Cant pause while the mower is docked
        if (command == 3 && eshStatus == 2) {
            logger.debug("Can´t pause the mower while it´s docked or docking");
            return false;
        }
        // Command means "MOW" but mower is not ready
        if (command == 1 && !isReadyToMow(statusCode, errorCode)) {
            logger.debug("The mower is not ready to mow in the moment");
            return false;
        }
        return true;
    }

    private DeviceCommand getCommandFromEshStatus(int eshStatus) {
        DeviceCommand command;
        switch (eshStatus) {
            case 1:
                command = DeviceCommand.MOW;
                break;
            case 2:
                command = DeviceCommand.RETURN;
                break;
            case 3:
                command = DeviceCommand.PAUSE;
                break;
            default:
                command = null;
        }
        return command;
    }

    private int getEshStatusFromCommand(DeviceCommand command) {
        int eshStatus;
        switch (command) {
            case MOW:
                eshStatus = 1;
                break;
            case RETURN:
                eshStatus = 2;
                break;
            case PAUSE:
                eshStatus = 3;
                break;
            default:
                eshStatus = 0;
        }
        return eshStatus;
    }

    @Override
    public void dispose() {
        super.dispose();
        logger.debug("removing thing..");
        running = false;
        pollingThread.interrupt();
    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        running = true;
        pollingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    while (running) {
                        synchronized (BoschIndegoHandler.this) {
                            poll();
                            BoschIndegoHandler.this.wait(((BigDecimal) getConfig().get("refresh")).intValue() * 1000);

                        }
                    }
                } catch (InterruptedException e) {
                    logger.debug("Binding closed");
                }

            }
        });
        pollingThread.start();

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }
}
