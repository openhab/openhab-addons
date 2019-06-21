/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.concurrent.ConcurrentHashMap;

import javax.measure.IncommensurableException;
import javax.measure.UnconvertibleException;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.homeconnect.internal.client.HomeConnectApiClient;
import org.openhab.binding.homeconnect.internal.client.exception.AuthorizationException;
import org.openhab.binding.homeconnect.internal.client.exception.CommunicationException;
import org.openhab.binding.homeconnect.internal.client.model.Data;
import org.openhab.binding.homeconnect.internal.logger.EmbeddedLoggingService;
import org.openhab.binding.homeconnect.internal.logger.LogWriter;
import org.openhab.binding.homeconnect.internal.type.HomeConnectDynamicStateDescriptionProvider;

/**
 * The {@link HomeConnectFridgeFreezerHandler} is responsible for handling commands, which are
 * sent to one of the channels of a fridge/freezer.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class HomeConnectFridgeFreezerHandler extends AbstractHomeConnectThingHandler {

    private final LogWriter logger;

    public HomeConnectFridgeFreezerHandler(Thing thing,
            HomeConnectDynamicStateDescriptionProvider dynamicStateDescriptionProvider,
            EmbeddedLoggingService loggingService) {
        super(thing, dynamicStateDescriptionProvider, loggingService);
        logger = loggingService.getLogger(HomeConnectFridgeFreezerHandler.class);
    }

    @Override
    protected void configureChannelUpdateHandlers(ConcurrentHashMap<String, ChannelUpdateHandler> handlers) {
        // register default update handlers
        handlers.put(CHANNEL_DOOR_STATE, defaultDoorStateChannelUpdateHandler());

        // register fridge/freezer specific handlers
        handlers.put(CHANNEL_FREEZER_SETPOINT_TEMPERATURE, (channelUID, cache) -> {
            updateState(channelUID, cachePutIfAbsentAndGet(channelUID, cache, () -> {
                HomeConnectApiClient apiClient = getApiClient();
                if (apiClient != null) {
                    Data data = apiClient.getFreezerSetpointTemperature(getThingHaId());
                    if (data.getValue() != null) {
                        return new QuantityType<>(data.getValueAsInt(), mapTemperature(data.getUnit()));
                    } else {
                        return UnDefType.NULL;
                    }
                }
                return UnDefType.NULL;
            }));
        });
        handlers.put(CHANNEL_REFRIGERATOR_SETPOINT_TEMPERATURE, (channelUID, cache) -> {
            updateState(channelUID, cachePutIfAbsentAndGet(channelUID, cache, () -> {
                HomeConnectApiClient apiClient = getApiClient();
                if (apiClient != null) {
                    Data data = apiClient.getFridgeSetpointTemperature(getThingHaId());
                    if (data.getValue() != null) {
                        return new QuantityType<>(data.getValueAsInt(), mapTemperature(data.getUnit()));
                    } else {
                        return UnDefType.NULL;
                    }
                }
                return UnDefType.NULL;
            }));
        });
        handlers.put(CHANNEL_REFRIGERATOR_SUPER_MODE, (channelUID, cache) -> {
            updateState(channelUID, cachePutIfAbsentAndGet(channelUID, cache, () -> {
                HomeConnectApiClient apiClient = getApiClient();
                if (apiClient != null) {
                    Data data = apiClient.getFridgeSuperMode(getThingHaId());
                    if (data.getValue() != null) {
                        return data.getValueAsBoolean() ? OnOffType.ON : OnOffType.OFF;
                    } else {
                        return UnDefType.NULL;
                    }
                }
                return UnDefType.NULL;
            }));
        });
        handlers.put(CHANNEL_FREEZER_SUPER_MODE, (channelUID, cache) -> {
            updateState(channelUID, cachePutIfAbsentAndGet(channelUID, cache, () -> {
                HomeConnectApiClient apiClient = getApiClient();
                if (apiClient != null) {
                    Data data = apiClient.getFreezerSuperMode(getThingHaId());
                    if (data.getValue() != null) {
                        return data.getValueAsBoolean() ? OnOffType.ON : OnOffType.OFF;
                    } else {
                        return UnDefType.NULL;
                    }
                }
                return UnDefType.NULL;
            }));
        });
    }

    @Override
    protected void configureEventHandlers(ConcurrentHashMap<String, EventHandler> handlers) {
        // register default event handlers
        handlers.put(EVENT_DOOR_STATE, defaultDoorStateEventHandler());
        handlers.put(EVENT_FREEZER_SUPER_MODE, defaultBooleanEventHandler(CHANNEL_FREEZER_SUPER_MODE));
        handlers.put(EVENT_FRIDGE_SUPER_MODE, defaultBooleanEventHandler(CHANNEL_REFRIGERATOR_SUPER_MODE));

        // register fridge/freezer specific event handlers
        handlers.put(EVENT_FREEZER_SETPOINT_TEMPERATURE, event -> {
            getThingChannel(CHANNEL_FREEZER_SETPOINT_TEMPERATURE).ifPresent(channel -> updateState(channel.getUID(),
                    new QuantityType<>(event.getValueAsInt(), mapTemperature(event.getUnit()))));
        });
        handlers.put(EVENT_FRIDGE_SETPOINT_TEMPERATURE, event -> {
            getThingChannel(CHANNEL_REFRIGERATOR_SETPOINT_TEMPERATURE)
                    .ifPresent(channel -> updateState(channel.getUID(),
                            new QuantityType<>(event.getValueAsInt(), mapTemperature(event.getUnit()))));
        });
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (isThingReadyToHandleCommand()) {
            super.handleCommand(channelUID, command);
            HomeConnectApiClient apiClient = getApiClient();

            try {
                if (apiClient != null && command instanceof QuantityType
                        && (CHANNEL_REFRIGERATOR_SETPOINT_TEMPERATURE.equals(channelUID.getId())
                                || CHANNEL_FREEZER_SETPOINT_TEMPERATURE.equals(channelUID.getId()))) {
                    @SuppressWarnings("unchecked")
                    QuantityType<Temperature> quantity = ((QuantityType<Temperature>) command);

                    String value;
                    String unit;

                    if (quantity.getUnit().equals(SIUnits.CELSIUS)
                            || quantity.getUnit().equals(ImperialUnits.FAHRENHEIT)) {
                        unit = quantity.getUnit().toString();
                        value = String.valueOf(quantity.intValue());
                    } else {
                        logger.infoWithHaId(getThingHaId(),
                                "Converting target setpoint temperature from {}{} to °C value.", quantity.intValue(),
                                quantity.getUnit().toString());
                        unit = "°C";
                        value = String.valueOf(
                                quantity.getUnit().getConverterToAny(SIUnits.CELSIUS).convert(quantity).intValue());
                        logger.infoWithHaId(getThingHaId(), "{}{}", value, unit);
                    }

                    logger.debugWithHaId(getThingHaId(), "Set setpoint temperature to {} {}.", value, unit);

                    if (CHANNEL_REFRIGERATOR_SETPOINT_TEMPERATURE.equals(channelUID.getId())) {
                        apiClient.setFridgeSetpointTemperature(getThingHaId(), value, unit);
                    } else if (CHANNEL_FREEZER_SETPOINT_TEMPERATURE.equals(channelUID.getId())) {
                        apiClient.setFreezerSetpointTemperature(getThingHaId(), value, unit);
                    }

                } else if (command instanceof OnOffType && apiClient != null) {
                    if (CHANNEL_FREEZER_SUPER_MODE.equals(channelUID.getId())) {
                        apiClient.setFreezerSuperMode(getThingHaId(), OnOffType.ON.equals(command));
                    } else if (CHANNEL_REFRIGERATOR_SUPER_MODE.equals(channelUID.getId())) {
                        apiClient.setFridgeSuperMode(getThingHaId(), OnOffType.ON.equals(command));
                    }
                }
            } catch (CommunicationException e) {
                logger.warnWithHaId(getThingHaId(), "Could not handle command {}. API communication problem! error: {}",
                        command.toFullString(), e.getMessage());
            } catch (AuthorizationException e) {
                logger.warnWithHaId(getThingHaId(), "Could not handle command {}. Authorization problem! error: {}",
                        command.toFullString(), e.getMessage());

                handleAuthenticationError(e);
            } catch (IncommensurableException | UnconvertibleException e) {
                logger.errorWithHaId(getThingHaId(), "Could not set setpoint! {}", e.getMessage());
            }
        }
    }

    @Override
    public String toString() {
        return "HomeConnectFridgeFreezerHandler [haId: " + getThingHaId() + "]";
    }
}
