/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.homeconnect.internal.handler;

import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_ACTIVE_PROGRAM_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_COFFEEMAKER_BEAN_CONTAINER_EMPTY_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_COFFEEMAKER_DRIP_TRAY_FULL_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_COFFEEMAKER_WATER_TANK_EMPTY_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_LOCAL_CONTROL_ACTIVE_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_OPERATION_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_POWER_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_PROGRAM_PROGRESS_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_REMOTE_START_ALLOWANCE_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.CHANNEL_SELECTED_PROGRAM_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_ACTIVE_PROGRAM;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_COFFEEMAKER_BEAN_CONTAINER_EMPTY;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_COFFEEMAKER_DRIP_TRAY_FULL;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_COFFEEMAKER_WATER_TANK_EMPTY;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_LOCAL_CONTROL_ACTIVE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_OPERATION_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_POWER_STATE;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_PROGRAM_PROGRESS;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_REMOTE_CONTROL_START_ALLOWED;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.EVENT_SELECTED_PROGRAM;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.STATE_POWER_ON;
import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.STATE_POWER_STANDBY;
import static org.openhab.core.thing.ThingStatus.OFFLINE;

import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homeconnect.internal.client.exception.ApplianceOfflineException;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;
import org.openhab.binding.homeconnect.internal.type.HomeConnectDynamicStateDescriptionProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomeConnectCoffeeMakerHandler} is responsible for handling commands, which are
 * sent to one of the channels of a coffee machine.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class HomeConnectCoffeeMakerHandler extends AbstractHomeConnectThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HomeConnectCoffeeMakerHandler.class);

    public HomeConnectCoffeeMakerHandler(Thing thing,
            HomeConnectDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, dynamicStateDescriptionProvider);
    }

    @Override
    protected void configureChannelUpdateHandlers(ConcurrentHashMap<String, ChannelUpdateHandler> handlers) {
        // register default update handlers
        handlers.put(CHANNEL_OPERATION_STATE, defaultOperationStateChannelUpdateHandler());
        handlers.put(CHANNEL_POWER_STATE, defaultPowerStateChannelUpdateHandler());
        handlers.put(CHANNEL_REMOTE_START_ALLOWANCE_STATE, defaultRemoteStartAllowanceChannelUpdateHandler());
        handlers.put(CHANNEL_LOCAL_CONTROL_ACTIVE_STATE, defaultLocalControlActiveStateChannelUpdateHandler());
        handlers.put(CHANNEL_SELECTED_PROGRAM_STATE, defaultSelectedProgramStateUpdateHandler());
        handlers.put(CHANNEL_ACTIVE_PROGRAM_STATE, defaultActiveProgramStateUpdateHandler());
    }

    @Override
    protected void configureEventHandlers(ConcurrentHashMap<String, EventHandler> handlers) {
        // register default SSE event handlers
        handlers.put(EVENT_REMOTE_CONTROL_START_ALLOWED,
                defaultBooleanEventHandler(CHANNEL_REMOTE_START_ALLOWANCE_STATE));
        handlers.put(EVENT_LOCAL_CONTROL_ACTIVE, defaultBooleanEventHandler(CHANNEL_LOCAL_CONTROL_ACTIVE_STATE));
        handlers.put(EVENT_SELECTED_PROGRAM, defaultSelectedProgramStateEventHandler());
        handlers.put(EVENT_COFFEEMAKER_BEAN_CONTAINER_EMPTY,
                defaultEventPresentStateEventHandler(CHANNEL_COFFEEMAKER_BEAN_CONTAINER_EMPTY_STATE));
        handlers.put(EVENT_COFFEEMAKER_DRIP_TRAY_FULL,
                defaultEventPresentStateEventHandler(CHANNEL_COFFEEMAKER_DRIP_TRAY_FULL_STATE));
        handlers.put(EVENT_COFFEEMAKER_WATER_TANK_EMPTY,
                defaultEventPresentStateEventHandler(CHANNEL_COFFEEMAKER_WATER_TANK_EMPTY_STATE));
        handlers.put(EVENT_ACTIVE_PROGRAM, defaultActiveProgramEventHandler());
        handlers.put(EVENT_POWER_STATE, defaultPowerStateEventHandler());
        handlers.put(EVENT_OPERATION_STATE, defaultOperationStateEventHandler());

        // register coffee maker specific SSE event handlers
        handlers.put(EVENT_PROGRAM_PROGRESS, event -> {
            if (event.getValue() == null || event.getValueAsInt() == 0) {
                getThingChannel(CHANNEL_PROGRAM_PROGRESS_STATE)
                        .ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
            } else {
                defaultPercentEventHandler(CHANNEL_PROGRAM_PROGRESS_STATE).handle(event);
            }
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (isThingReadyToHandleCommand()) {
            super.handleCommand(channelUID, command);

            getApiClient().ifPresent(apiClient -> {
                try {
                    // turn coffee maker on and standby
                    if (command instanceof OnOffType && CHANNEL_POWER_STATE.equals(channelUID.getId())) {
                        apiClient.setPowerState(getThingHaId(),
                                OnOffType.ON.equals(command) ? STATE_POWER_ON : STATE_POWER_STANDBY);
                    }
                } catch (ApplianceOfflineException e) {
                    logger.debug("Could not handle command {}. Appliance offline. thing={}, haId={}, error={}",
                            command.toFullString(), getThingLabel(), getThingHaId(), e.getMessage());
                    updateStatus(OFFLINE);
                    resetChannelsOnOfflineEvent();
                    resetProgramStateChannels();
                } catch (CommunicationException e) {
                    logger.debug("Could not handle command {}. API communication problem! haId={}, error={}",
                            command.toFullString(), getThingHaId(), e.getMessage());
                } catch (AuthorizationException e) {
                    logger.debug("Could not handle command {}. Authorization problem! haId={}, error={}",
                            command.toFullString(), getThingHaId(), e.getMessage());

                    handleAuthenticationError(e);
                }
            });
        }
    }

    @Override
    public String toString() {
        return "HomeConnectCoffeeMakerHandler [haId: " + getThingHaId() + "]";
    }

    @Override
    protected void resetProgramStateChannels() {
        super.resetProgramStateChannels();
        getThingChannel(CHANNEL_PROGRAM_PROGRESS_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        getThingChannel(CHANNEL_ACTIVE_PROGRAM_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
    }
}
