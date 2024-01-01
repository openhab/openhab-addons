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
package org.openhab.binding.boschshc.internal.devices.userdefinedstate;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.CHANNEL_USER_DEFINED_STATE;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.BoschSHCConfiguration;
import org.openhab.binding.boschshc.internal.devices.BoschSHCHandler;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.userstate.UserStateService;
import org.openhab.binding.boschshc.internal.services.userstate.dto.UserStateServiceState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * Handler for user defined states
 *
 * @author Patrick Gell - Initial contribution
 *
 */
@NonNullByDefault
public class UserStateHandler extends BoschSHCHandler {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final UserStateService userStateService;
    /**
     * Bosch SHC configuration loaded from openHAB configuration.
     */
    private @Nullable BoschSHCConfiguration config;

    public UserStateHandler(Thing thing) {
        super(thing);

        userStateService = new UserStateService();
    }

    @Override
    public void initialize() {
        var localConfig = this.config = getConfigAs(BoschSHCConfiguration.class);
        String stateId = localConfig.id;
        if (stateId == null || stateId.isBlank()) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.empty-state-id");
            return;
        }

        // Try to get state info to make sure the state exists
        try {
            var bridgeHandler = this.getBridgeHandler();
            var info = bridgeHandler.getUserStateInfo(stateId);
            logger.trace("User-defined state initialized:\n{}", info);
        } catch (TimeoutException | ExecutionException | BoschSHCException e) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }
        super.initialize();
    }

    @Override
    public @Nullable String getBoschID() {
        if (config != null) {
            return config.id;
        }

        return null;
    }

    @Override
    protected void initializeServices() throws BoschSHCException {
        super.initializeServices();

        logger.debug("Initializing service for UserStateHandler");
        this.registerService(userStateService, this::updateChannels, List.of(CHANNEL_USER_DEFINED_STATE), true);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        if (channelUID.getId().equals(CHANNEL_USER_DEFINED_STATE) && (command instanceof OnOffType onOffCommand)) {
            updateUserState(channelUID.getThingUID().getId(), onOffCommand);
        }
    }

    private void updateUserState(String stateId, OnOffType userState) {
        UserStateServiceState serviceState = new UserStateServiceState();
        serviceState.setState(userState == OnOffType.ON);
        try {
            getBridgeHandler().putState(stateId, "", serviceState);
        } catch (BoschSHCException | ExecutionException | TimeoutException e) {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    String.format("Error while putting user-defined state for %s", stateId));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    String.format("Error while putting user-defined state for %s", stateId));
        }
    }

    private void updateChannels(UserStateServiceState userState) {
        super.updateState(CHANNEL_USER_DEFINED_STATE, userState.toOnOffType());
    }

    @Override
    public void processUpdate(String serviceName, @Nullable JsonElement stateData) {
        super.processUpdate("UserDefinedState", stateData);
    }
}
