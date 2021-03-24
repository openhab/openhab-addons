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

import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.*;
import static org.openhab.core.thing.ThingStatus.OFFLINE;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homeconnect.internal.client.exception.ApplianceOfflineException;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;
import org.openhab.binding.homeconnect.internal.client.model.Data;
import org.openhab.binding.homeconnect.internal.type.HomeConnectDynamicStateDescriptionProvider;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomeConnectDishwasherHandler} is responsible for handling commands, which are
 * sent to one of the channels of a dishwasher.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class HomeConnectDishwasherHandler extends AbstractHomeConnectThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HomeConnectDishwasherHandler.class);

    public HomeConnectDishwasherHandler(Thing thing,
            HomeConnectDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, dynamicStateDescriptionProvider);
    }

    @Override
    protected void configureChannelUpdateHandlers(Map<String, ChannelUpdateHandler> handlers) {
        // register default update handlers
        handlers.put(CHANNEL_DOOR_STATE, defaultDoorStateChannelUpdateHandler());
        handlers.put(CHANNEL_POWER_STATE, defaultPowerStateChannelUpdateHandler());
        handlers.put(CHANNEL_OPERATION_STATE, defaultOperationStateChannelUpdateHandler());
        handlers.put(CHANNEL_REMOTE_CONTROL_ACTIVE_STATE, defaultRemoteControlActiveStateChannelUpdateHandler());
        handlers.put(CHANNEL_REMOTE_START_ALLOWANCE_STATE, defaultRemoteStartAllowanceChannelUpdateHandler());
        handlers.put(CHANNEL_SELECTED_PROGRAM_STATE, defaultSelectedProgramStateUpdateHandler());
        handlers.put(CHANNEL_ACTIVE_PROGRAM_STATE, defaultActiveProgramStateUpdateHandler());
        handlers.put(CHANNEL_AMBIENT_LIGHT_STATE, defaultAmbientLightChannelUpdateHandler());
    }

    @Override
    protected void configureEventHandlers(Map<String, EventHandler> handlers) {
        // register default event handlers
        handlers.put(EVENT_DOOR_STATE, defaultDoorStateEventHandler());
        handlers.put(EVENT_REMOTE_CONTROL_ACTIVE, defaultBooleanEventHandler(CHANNEL_REMOTE_CONTROL_ACTIVE_STATE));
        handlers.put(EVENT_REMOTE_CONTROL_START_ALLOWED,
                defaultBooleanEventHandler(CHANNEL_REMOTE_START_ALLOWANCE_STATE));
        handlers.put(EVENT_REMAINING_PROGRAM_TIME, defaultRemainingProgramTimeEventHandler());
        handlers.put(EVENT_PROGRAM_PROGRESS, defaultPercentEventHandler(CHANNEL_PROGRAM_PROGRESS_STATE));
        handlers.put(EVENT_SELECTED_PROGRAM, defaultSelectedProgramStateEventHandler());
        handlers.put(EVENT_ACTIVE_PROGRAM, defaultActiveProgramEventHandler());
        handlers.put(EVENT_POWER_STATE, defaultPowerStateEventHandler());
        handlers.put(EVENT_OPERATION_STATE, defaultOperationStateEventHandler());
        handlers.put(EVENT_AMBIENT_LIGHT_STATE, defaultBooleanEventHandler(CHANNEL_AMBIENT_LIGHT_STATE));
        handlers.put(EVENT_AMBIENT_LIGHT_BRIGHTNESS_STATE,
                defaultPercentEventHandler(CHANNEL_AMBIENT_LIGHT_BRIGHTNESS_STATE));
        handlers.put(EVENT_AMBIENT_LIGHT_COLOR_STATE, defaultAmbientLightColorStateEventHandler());
        handlers.put(EVENT_AMBIENT_LIGHT_CUSTOM_COLOR_STATE, defaultAmbientLightCustomColorStateEventHandler());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (isThingReadyToHandleCommand()) {
            super.handleCommand(channelUID, command);

            getApiClient().ifPresent(client -> {
                try {
                    if (command instanceof OnOffType) {
                        if (CHANNEL_POWER_STATE.equals(channelUID.getId())) {
                            client.setPowerState(getThingHaId(),
                                    OnOffType.ON.equals(command) ? STATE_POWER_ON : STATE_POWER_OFF);
                        } else if (CHANNEL_AMBIENT_LIGHT_STATE.equals(channelUID.getId())) {
                            client.setAmbientLightState(getThingHaId(), OnOffType.ON.equals(command));
                        }
                    } else if (command instanceof QuantityType
                            && CHANNEL_AMBIENT_LIGHT_BRIGHTNESS_STATE.equals(channelUID.getId())) {
                        Data ambientLightState = client.getAmbientLightState(getThingHaId());
                        if (!ambientLightState.getValueAsBoolean()) {
                            // turn on
                            client.setAmbientLightState(getThingHaId(), true);
                        }
                        int value = ((QuantityType<?>) command).intValue();
                        if (value < 10) {
                            value = 10;
                        } else if (value > 100) {
                            value = 100;
                        }
                        client.setAmbientLightBrightnessState(getThingHaId(), value);
                    } else if (command instanceof StringType
                            && CHANNEL_AMBIENT_LIGHT_COLOR_STATE.equals(channelUID.getId())) {
                        Data ambientLightState = client.getAmbientLightState(getThingHaId());
                        if (!ambientLightState.getValueAsBoolean()) {
                            // turn on
                            client.setAmbientLightState(getThingHaId(), true);
                        }
                        client.setAmbientLightColorState(getThingHaId(), command.toFullString());
                    } else if (CHANNEL_AMBIENT_LIGHT_CUSTOM_COLOR_STATE.equals(channelUID.getId())) {
                        Data ambientLightState = client.getAmbientLightState(getThingHaId());
                        if (!ambientLightState.getValueAsBoolean()) {
                            // turn on
                            client.setAmbientLightState(getThingHaId(), true);
                        }
                        Data ambientLightColorState = client.getAmbientLightColorState(getThingHaId());
                        if (!STATE_AMBIENT_LIGHT_COLOR_CUSTOM_COLOR.equals(ambientLightColorState.getValue())) {
                            // set color to custom color
                            client.setAmbientLightColorState(getThingHaId(), STATE_AMBIENT_LIGHT_COLOR_CUSTOM_COLOR);
                        }

                        if (command instanceof HSBType) {
                            client.setAmbientLightCustomColorState(getThingHaId(), mapColor((HSBType) command));
                        } else if (command instanceof StringType) {
                            client.setAmbientLightCustomColorState(getThingHaId(), command.toFullString());
                        }
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
        return "HomeConnectDishwasherHandler [haId: " + getThingHaId() + "]";
    }

    @Override
    protected void resetProgramStateChannels() {
        super.resetProgramStateChannels();
        getThingChannel(CHANNEL_REMAINING_PROGRAM_TIME_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        getThingChannel(CHANNEL_PROGRAM_PROGRESS_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
        getThingChannel(CHANNEL_ACTIVE_PROGRAM_STATE).ifPresent(c -> updateState(c.getUID(), UnDefType.UNDEF));
    }
}
