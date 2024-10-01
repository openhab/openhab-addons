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
package org.openhab.binding.warmup.internal.handler;

import static org.openhab.binding.warmup.internal.WarmupBindingConstants.*;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.warmup.internal.WarmupBindingConstants.RoomMode;
import org.openhab.binding.warmup.internal.action.WarmupActions;
import org.openhab.binding.warmup.internal.api.MyWarmupApi;
import org.openhab.binding.warmup.internal.api.MyWarmupApiException;
import org.openhab.binding.warmup.internal.model.query.LocationDTO;
import org.openhab.binding.warmup.internal.model.query.QueryResponseDTO;
import org.openhab.binding.warmup.internal.model.query.RoomDTO;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author James Melville - Initial contribution
 */
@NonNullByDefault
public class RoomHandler extends WarmupThingHandler implements WarmupRefreshListener {

    private final Logger logger = LoggerFactory.getLogger(RoomHandler.class);
    private @Nullable RoomConfigurationDTO config;

    public RoomHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        config = getConfigAs(RoomConfigurationDTO.class);
        if (config.getSerialNumber().length() == 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Serial Number not configured");
        } else {
            super.refreshFromServer();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);
        if (CHANNEL_TARGET_TEMPERATURE.equals(channelUID.getId())
                && command instanceof QuantityType<?> quantityCommand) {
            setOverride((QuantityType<Temperature>) quantityCommand);
        }
        if (CHANNEL_FIXED_TEMPERATURE.equals(channelUID.getId())
                && command instanceof QuantityType<?> quantityCommand) {
            setFixed((QuantityType<Temperature>) quantityCommand);
        }
        if (CHANNEL_FROST_PROTECTION_MODE.equals(channelUID.getId()) && command instanceof OnOffType onOffCommand) {
            toggleFrostProtectionMode(onOffCommand);
        }
        if (CHANNEL_RUN_MODE.equals(channelUID.getId()) && command instanceof StringType stringCommand) {
            setRoomMode(stringCommand);
        }
    }

    /**
     * Process device list and populate room properties, status and state
     *
     * @param domain Data model representing all devices
     */
    @Override
    public void refresh(@Nullable QueryResponseDTO domain) {
        if (domain == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "No data from bridge");
        } else if (config != null) {
            final String serialNumber = config.getSerialNumber();
            for (LocationDTO location : domain.data().user().locations()) {
                for (RoomDTO room : location.rooms()) {
                    if (room.thermostat4ies() != null && !room.thermostat4ies().isEmpty()
                            && room.thermostat4ies().get(0).deviceSN().equals(serialNumber)) {
                        if (room.thermostat4ies().get(0).lastPoll() > 10) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "Thermostat has not polled for 10 minutes");
                        } else {
                            updateStatus(ThingStatus.ONLINE);

                            updateProperty(PROPERTY_ROOM_ID, room.getId());
                            updateProperty(PROPERTY_ROOM_NAME, room.roomName());
                            updateProperty(PROPERTY_LOCATION_ID, location.getId());
                            updateProperty(PROPERTY_LOCATION_NAME, location.name());

                            updateState(CHANNEL_CURRENT_TEMPERATURE, parseTemperature(room.currentTemp()));
                            updateState(CHANNEL_TARGET_TEMPERATURE, parseTemperature(room.targetTemp()));
                            updateState(CHANNEL_FIXED_TEMPERATURE, parseTemperature(room.fixedTemp()));
                            updateState(CHANNEL_ENERGY, parseEnergy(room.energy()));
                            updateState(CHANNEL_AIR_TEMPERATURE,
                                    parseTemperature(room.thermostat4ies().get(0).airTemp()));
                            updateState(CHANNEL_FLOOR1_TEMPERATURE,
                                    parseTemperature(room.thermostat4ies().get(0).floor1Temp()));
                            updateState(CHANNEL_FLOOR2_TEMPERATURE,
                                    parseTemperature(room.thermostat4ies().get(0).floor2Temp()));
                            updateState(CHANNEL_OVERRIDE_DURATION, parseDuration(room.overrideDur()));
                            updateState(CHANNEL_RUN_MODE, parseString(room.runMode()));
                            updateState(CHANNEL_FROST_PROTECTION_MODE,
                                    OnOffType.from(room.runMode().equals(FROST_PROTECTION_MODE)));
                        }
                        return;
                    }
                }
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Room not found");
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Room not configured");
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(WarmupActions.class);
    }

    private void setOverride(final QuantityType<Temperature> command) {
        setOverride(command, new QuantityType<>(config.getOverrideDuration(), Units.MINUTE));
    }

    public void setOverride(final QuantityType<Temperature> temperature, final QuantityType<Time> duration) {
        setOverride(formatTemperature(temperature), duration.toUnit(Units.MINUTE).intValue());
    }

    private void setOverride(final int temperature, final int duration) {
        if (duration > 1440 || duration <= 0) {
            logger.warn("Set Override failed: duration must be between 0 and 1440 minutes");
        }
        if (temperature > 600 || temperature < 50) {
            logger.warn("Set Override failed: temperature must be between 0.5 and 60 degrees C");
        } else {
            try {
                RoomCallout rc = getCallout();
                rc.api.setOverride(rc.locationId, rc.roomId, temperature, duration);
                refreshFromServer();
            } catch (MyWarmupApiException e) {
                logger.warn("Set Override failed: {}", e.getMessage());
            }
        }
    }

    private void setFixed(final QuantityType<Temperature> command) {
        try {
            RoomCallout rc = getCallout();
            rc.api.setFixed(rc.locationId, rc.roomId, formatTemperature(command));
            refreshFromServer();
        } catch (MyWarmupApiException e) {
            logger.warn("Set Fixed failed: {}", e.getMessage());
        }
    }

    private void toggleFrostProtectionMode(OnOffType command) {
        try {
            RoomCallout rc = getCallout();
            rc.api.toggleFrostProtectionMode(rc.locationId, rc.roomId, command);
            refreshFromServer();
        } catch (MyWarmupApiException e) {
            logger.warn("Toggle Frost Protection failed: {}", e.getMessage());
        }
    }

    private void setRoomMode(StringType command) {
        try {
            RoomCallout rc = getCallout();
            RoomMode mode = RoomMode.valueOf(command.toString().trim().toUpperCase());
            rc.api.setRoomMode(rc.locationId, rc.roomId, mode);
            refreshFromServer();
        } catch (MyWarmupApiException e) {
            logger.warn("Set Room Mode failed: {}", e.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.warn("Unable to set room mode: {}", command.toString());
        }
    }

    private RoomCallout getCallout() throws MyWarmupApiException {
        Map<String, String> props = getThing().getProperties();
        String locationId = props.get(PROPERTY_LOCATION_ID);
        String roomId = props.get(PROPERTY_ROOM_ID);
        final MyWarmupAccountHandler bridgeHandler = getBridgeHandler();

        if (bridgeHandler != null && locationId != null && roomId != null) {
            return new RoomCallout(roomId, locationId, bridgeHandler.getApi());
        } else {
            throw new MyWarmupApiException("Misconfigured thing.");
        }
    }

    record RoomCallout(String roomId, String locationId, MyWarmupApi api) {
    }
}
