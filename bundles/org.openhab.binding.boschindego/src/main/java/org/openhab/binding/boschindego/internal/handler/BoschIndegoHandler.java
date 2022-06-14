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

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.boschindego.internal.DeviceStatus;
import org.openhab.binding.boschindego.internal.IndegoController;
import org.openhab.binding.boschindego.internal.config.BoschIndegoConfiguration;
import org.openhab.binding.boschindego.internal.dto.DeviceCommand;
import org.openhab.binding.boschindego.internal.dto.response.DeviceStateResponse;
import org.openhab.binding.boschindego.internal.exceptions.IndegoAuthenticationException;
import org.openhab.binding.boschindego.internal.exceptions.IndegoException;
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

/**
 * The {@link BoschIndegoHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jonas Fleck - Initial contribution
 * @author Jacob Laursen - Refactoring, bugfixing and removal of dependency towards abandoned library
 */
@NonNullByDefault
public class BoschIndegoHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(BoschIndegoHandler.class);
    private final HttpClient httpClient;

    private @NonNullByDefault({}) IndegoController controller;
    private @Nullable ScheduledFuture<?> pollFuture;
    private long refreshRate;
    private boolean propertiesInitialized;

    public BoschIndegoHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Indego handler");
        BoschIndegoConfiguration config = getConfigAs(BoschIndegoConfiguration.class);
        String username = config.username;
        String password = config.password;

        if (username == null || username.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.missing-username");
            return;
        }
        if (password == null || password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.missing-password");
            return;
        }

        controller = new IndegoController(httpClient, username, password);
        refreshRate = config.refresh;

        updateStatus(ThingStatus.UNKNOWN);
        this.pollFuture = scheduler.scheduleWithFixedDelay(this::refreshState, 0, refreshRate, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Indego handler");
        ScheduledFuture<?> pollFuture = this.pollFuture;
        if (pollFuture != null) {
            pollFuture.cancel(true);
        }
        this.pollFuture = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            scheduler.submit(() -> this.refreshState());
            return;
        }
        try {
            if (command instanceof DecimalType && channelUID.getId().equals(STATE)) {
                sendCommand(((DecimalType) command).intValue());
            }
        } catch (IndegoAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.authentication-failure");
        } catch (IndegoException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void sendCommand(int commandInt) throws IndegoException {
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
                logger.warn("Invalid command {}", commandInt);
                return;
        }

        DeviceStateResponse state = controller.getState();
        DeviceStatus deviceStatus = DeviceStatus.fromCode(state.state);
        if (!verifyCommand(command, deviceStatus, state.error)) {
            return;
        }
        logger.debug("Sending command {}", command);
        updateState(TEXTUAL_STATE, UnDefType.UNDEF);
        controller.sendCommand(command);
        state = controller.getState();
        updateStatus(ThingStatus.ONLINE);
        updateState(state);
    }

    private void refreshState() {
        try {
            if (!propertiesInitialized) {
                getThing().setProperty(Thing.PROPERTY_SERIAL_NUMBER, controller.getSerialNumber());
                propertiesInitialized = true;
            }

            DeviceStateResponse state = controller.getState();
            updateStatus(ThingStatus.ONLINE);
            updateState(state);
        } catch (IndegoAuthenticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/offline.comm-error.authentication-failure");
        } catch (IndegoException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void updateState(DeviceStateResponse state) {
        DeviceStatus deviceStatus = DeviceStatus.fromCode(state.state);
        int status = getStatusFromCommand(deviceStatus.getAssociatedCommand());
        int mowed = state.mowed;
        int error = state.error;
        int statecode = state.state;
        boolean ready = isReadyToMow(deviceStatus, state.error);

        updateState(STATECODE, new DecimalType(statecode));
        updateState(READY, new DecimalType(ready ? 1 : 0));
        updateState(ERRORCODE, new DecimalType(error));
        updateState(MOWED, new PercentType(mowed));
        updateState(STATE, new DecimalType(status));
        updateState(TEXTUAL_STATE, new StringType(deviceStatus.getMessage()));
    }

    private boolean isReadyToMow(DeviceStatus deviceStatus, int error) {
        return deviceStatus.isReadyToMow() && error == 0;
    }

    private boolean verifyCommand(DeviceCommand command, DeviceStatus deviceStatus, int errorCode) {
        // Mower reported an error
        if (errorCode != 0) {
            logger.error("The mower reported an error.");
            return false;
        }

        // Command is equal to current state
        if (command == deviceStatus.getAssociatedCommand()) {
            logger.debug("Command is equal to state");
            return false;
        }
        // Cant pause while the mower is docked
        if (command == DeviceCommand.PAUSE && deviceStatus.getAssociatedCommand() == DeviceCommand.RETURN) {
            logger.debug("Can't pause the mower while it's docked or docking");
            return false;
        }
        // Command means "MOW" but mower is not ready
        if (command == DeviceCommand.MOW && !isReadyToMow(deviceStatus, errorCode)) {
            logger.debug("The mower is not ready to mow at the moment");
            return false;
        }
        return true;
    }

    private int getStatusFromCommand(@Nullable DeviceCommand command) {
        if (command == null) {
            return 0;
        }
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
}
