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
package org.openhab.binding.homeconnect.internal.handler;

import static org.openhab.binding.homeconnect.internal.HomeConnectBindingConstants.*;

import java.util.Map;
import java.util.Optional;

import javax.measure.UnconvertibleException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.homeconnect.internal.client.HomeConnectApiClient;
import org.openhab.binding.homeconnect.internal.client.exception.ApplianceOfflineException;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;
import org.openhab.binding.homeconnect.internal.client.model.Data;
import org.openhab.binding.homeconnect.internal.type.HomeConnectDynamicStateDescriptionProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomeConnectFridgeFreezerHandler} is responsible for handling commands, which are
 * sent to one of the channels of a fridge/freezer.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class HomeConnectFridgeFreezerHandler extends AbstractHomeConnectThingHandler {

    private final Logger logger = LoggerFactory.getLogger(HomeConnectFridgeFreezerHandler.class);

    public HomeConnectFridgeFreezerHandler(Thing thing,
            HomeConnectDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing, dynamicStateDescriptionProvider);
    }

    @Override
    protected void configureChannelUpdateHandlers(Map<String, ChannelUpdateHandler> handlers) {
        // register default update handlers
        handlers.put(CHANNEL_DOOR_STATE, defaultDoorStateChannelUpdateHandler());

        // register fridge/freezer specific handlers
        handlers.put(CHANNEL_FREEZER_SETPOINT_TEMPERATURE,
                (channelUID, cache) -> updateState(channelUID, cache.putIfAbsentAndGet(channelUID, () -> {
                    Optional<HomeConnectApiClient> apiClient = getApiClient();
                    if (apiClient.isPresent()) {
                        Data data = apiClient.get().getFreezerSetpointTemperature(getThingHaId());
                        if (data.getValue() != null) {
                            return new QuantityType<>(data.getValueAsInt(), mapTemperature(data.getUnit()));
                        } else {
                            return UnDefType.UNDEF;
                        }
                    }
                    return UnDefType.UNDEF;
                })));
        handlers.put(CHANNEL_REFRIGERATOR_SETPOINT_TEMPERATURE,
                (channelUID, cache) -> updateState(channelUID, cache.putIfAbsentAndGet(channelUID, () -> {
                    Optional<HomeConnectApiClient> apiClient = getApiClient();
                    if (apiClient.isPresent()) {
                        Data data = apiClient.get().getFridgeSetpointTemperature(getThingHaId());
                        if (data.getValue() != null) {
                            return new QuantityType<>(data.getValueAsInt(), mapTemperature(data.getUnit()));
                        } else {
                            return UnDefType.UNDEF;
                        }
                    }
                    return UnDefType.UNDEF;
                })));
        handlers.put(CHANNEL_REFRIGERATOR_SUPER_MODE,
                (channelUID, cache) -> updateState(channelUID, cache.putIfAbsentAndGet(channelUID, () -> {
                    Optional<HomeConnectApiClient> apiClient = getApiClient();
                    if (apiClient.isPresent()) {
                        Data data = apiClient.get().getFridgeSuperMode(getThingHaId());
                        if (data.getValue() != null) {
                            return OnOffType.from(data.getValueAsBoolean());
                        } else {
                            return UnDefType.UNDEF;
                        }
                    }
                    return UnDefType.UNDEF;
                })));
        handlers.put(CHANNEL_FREEZER_SUPER_MODE,
                (channelUID, cache) -> updateState(channelUID, cache.putIfAbsentAndGet(channelUID, () -> {
                    Optional<HomeConnectApiClient> apiClient = getApiClient();

                    if (apiClient.isPresent()) {
                        Data data = apiClient.get().getFreezerSuperMode(getThingHaId());
                        if (data.getValue() != null) {
                            return OnOffType.from(data.getValueAsBoolean());
                        } else {
                            return UnDefType.UNDEF;
                        }
                    }
                    return UnDefType.UNDEF;
                })));
    }

    @Override
    protected void configureEventHandlers(Map<String, EventHandler> handlers) {
        // register default event handlers
        handlers.put(EVENT_DOOR_STATE, defaultDoorStateEventHandler());
        handlers.put(EVENT_FREEZER_SUPER_MODE, defaultBooleanEventHandler(CHANNEL_FREEZER_SUPER_MODE));
        handlers.put(EVENT_FRIDGE_SUPER_MODE, defaultBooleanEventHandler(CHANNEL_REFRIGERATOR_SUPER_MODE));

        // register fridge/freezer specific event handlers
        handlers.put(EVENT_FREEZER_SETPOINT_TEMPERATURE,
                event -> getLinkedChannel(CHANNEL_FREEZER_SETPOINT_TEMPERATURE)
                        .ifPresent(channel -> updateState(channel.getUID(),
                                new QuantityType<>(event.getValueAsInt(), mapTemperature(event.getUnit())))));
        handlers.put(EVENT_FRIDGE_SETPOINT_TEMPERATURE,
                event -> getLinkedChannel(CHANNEL_REFRIGERATOR_SETPOINT_TEMPERATURE)
                        .ifPresent(channel -> updateState(channel.getUID(),
                                new QuantityType<>(event.getValueAsInt(), mapTemperature(event.getUnit())))));
    }

    @Override
    protected void handleCommand(final ChannelUID channelUID, final Command command,
            final HomeConnectApiClient apiClient)
            throws CommunicationException, AuthorizationException, ApplianceOfflineException {
        super.handleCommand(channelUID, command, apiClient);

        try {
            if (CHANNEL_REFRIGERATOR_SETPOINT_TEMPERATURE.equals(channelUID.getId())
                    || CHANNEL_FREEZER_SETPOINT_TEMPERATURE.equals(channelUID.getId())) {
                handleTemperatureCommand(channelUID, command, apiClient);
            } else if (command instanceof OnOffType) {
                if (CHANNEL_FREEZER_SUPER_MODE.equals(channelUID.getId())) {
                    apiClient.setFreezerSuperMode(getThingHaId(), OnOffType.ON.equals(command));
                } else if (CHANNEL_REFRIGERATOR_SUPER_MODE.equals(channelUID.getId())) {
                    apiClient.setFridgeSuperMode(getThingHaId(), OnOffType.ON.equals(command));
                }
            }
        } catch (UnconvertibleException e) {
            logger.debug("Could not set setpoint! haId={}, error={}", getThingHaId(), e.getMessage());
        }
    }

    @Override
    protected void updateSelectedProgramStateDescription() {
        // not used
    }

    @Override
    protected void removeSelectedProgramStateDescription() {
        // not used
    }

    @Override
    public String toString() {
        return "HomeConnectFridgeFreezerHandler [haId: " + getThingHaId() + "]";
    }
}
