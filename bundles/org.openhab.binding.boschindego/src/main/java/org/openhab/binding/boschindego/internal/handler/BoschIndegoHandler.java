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
package org.openhab.binding.boschindego.internal.handler;

import static org.openhab.binding.boschindego.internal.BoschIndegoBindingConstants.*;
import static org.openhab.binding.boschindego.internal.IndegoStateConstants.*;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
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

    private final Logger logger = LoggerFactory.getLogger(BoschIndegoHandler.class);
    private final Queue<DeviceCommand> commandQueue = new LinkedList<>();

    private ScheduledFuture<?> pollFuture;

    // If false the request is already scheduled.
    private boolean shouldReschedule;

    public BoschIndegoHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            // Currently manual refreshing is not possible in the moment
            return;
        } else if (channelUID.getId().equals(STATE) && command instanceof DecimalType) {
            if (command instanceof DecimalType) {
                sendCommand(((DecimalType) command).intValue());
            }
        }
    }

    private void sendCommand(int commandInt) {
        DeviceCommand command;
        switch (commandInt) {
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
                logger.error("Invalid command");
                return;
        }
        synchronized (commandQueue) {
            // Add command to queue to avoid blocking
            commandQueue.offer(command);
            if (shouldReschedule) {
                shouldReschedule = false;
                reschedule();
            }
        }
    }

    private synchronized void poll() {
        // Create controller instance
        try {
            IndegoController controller = new IndegoController(getConfig().get("username").toString(),
                    getConfig().get("password").toString());
            // Connect to server
            controller.connect();
            // Query the device state
            DeviceStateInformation state = controller.getState();
            DeviceStatus statusWithMessage = DeviceStatus.decodeStatusCode(state.getState());
            int status = getStatusFromCommand(statusWithMessage.getAssociatedCommand());
            int mowed = state.getMowed();
            int error = state.getError();
            int statecode = state.getState();
            boolean ready = isReadyToMow(state.getState(), state.getError());
            DeviceCommand commandToSend = null;
            synchronized (commandQueue) {
                // Discard older commands
                while (!commandQueue.isEmpty()) {
                    commandToSend = commandQueue.poll();
                }
                // For newer commands a new request is needed
                shouldReschedule = true;
            }
            if (commandToSend != null && verifyCommand(commandToSend, statusWithMessage.getAssociatedCommand(),
                    state.getState(), error)) {
                logger.debug("Sending command...");
                updateState(TEXTUAL_STATE, UnDefType.UNDEF);
                controller.sendCommand(commandToSend);
                try {
                    for (int i = 0; i < 30 && !Thread.interrupted(); i++) {
                        DeviceStateInformation stateTmp = controller.getState();
                        if (state.getState() != stateTmp.getState()) {
                            state = stateTmp;
                            statusWithMessage = DeviceStatus.decodeStatusCode(state.getState());
                            status = getStatusFromCommand(statusWithMessage.getAssociatedCommand());
                            mowed = state.getMowed();
                            error = state.getError();
                            statecode = state.getState();
                            ready = isReadyToMow(state.getState(), state.getError());
                            break;
                        }
                        Thread.sleep(1000);
                    }

                } catch (InterruptedException e) {
                    // Nothing to do here
                }
            }
            controller.disconnect();
            updateStatus(ThingStatus.ONLINE);
            updateState(STATECODE, new DecimalType(statecode));
            updateState(READY, new DecimalType(ready ? 1 : 0));
            updateState(ERRORCODE, new DecimalType(error));
            updateState(MOWED, new PercentType(mowed));
            updateState(STATE, new DecimalType(status));
            updateState(TEXTUAL_STATE, new StringType(statusWithMessage.getMessage()));

        } catch (IndegoAuthenticationException e) {
            String message = "The login credentials are wrong or another client connected to your Indego account";
            logger.warn(message, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
        } catch (IndegoException e) {
            logger.warn("An error occurred", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    private boolean isReadyToMow(int statusCode, int error) {
        // I don´t know why bosch uses different state codes for the same state.
        return (statusCode == STATE_DOCKED_1 || statusCode == STATE_DOCKED_2 || statusCode == STATE_DOCKED_3
                || statusCode == STATE_PAUSED || statusCode == STATE_IDLE_IN_LAWN) && error == 0;
    }

    private boolean verifyCommand(DeviceCommand command, DeviceCommand state, int statusCode, int errorCode) {
        // Mower reported an error
        if (errorCode != 0) {
            logger.error("The mower reported an error.");
            return false;
        }

        // Command is equal to current state
        if (command == state) {
            logger.debug("Command is equal to state");
            return false;
        }
        // Cant pause while the mower is docked
        if (command == DeviceCommand.PAUSE && state == DeviceCommand.RETURN) {
            logger.debug("Can´t pause the mower while it´s docked or docking");
            return false;
        }
        // Command means "MOW" but mower is not ready
        if (command == DeviceCommand.MOW && !isReadyToMow(statusCode, errorCode)) {
            logger.debug("The mower is not ready to mow in the moment");
            return false;
        }
        return true;
    }

    private int getStatusFromCommand(DeviceCommand command) {
        int status;
        switch (command) {
            case MOW:
                status = 1;
                break;
            case RETURN:
                status = 2;
                break;
            case PAUSE:
                status = 3;
                break;
            default:
                status = 0;
        }
        return status;
    }

    @Override
    public void dispose() {
        super.dispose();
        logger.debug("removing thing..");
        if (pollFuture != null) {
            pollFuture.cancel(true);
        }
    }

    private void reschedule() {
        logger.debug("rescheduling");

        if (pollFuture != null) {
            pollFuture.cancel(false);
        }

        int refreshRate = ((BigDecimal) getConfig().get("refresh")).intValue();
        pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 0, refreshRate, TimeUnit.SECONDS);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);
        reschedule();
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.OFFLINE);
        reschedule();
    }
}
