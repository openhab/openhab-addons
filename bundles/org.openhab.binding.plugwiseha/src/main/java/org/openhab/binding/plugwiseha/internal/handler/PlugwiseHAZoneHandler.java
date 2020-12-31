/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.plugwiseha.internal.handler;

import static org.openhab.binding.plugwiseha.internal.PlugwiseHABindingConstants.*;
import static org.openhab.core.thing.ThingStatus.*;
import static org.openhab.core.thing.ThingStatusDetail.COMMUNICATION_ERROR;
import static org.openhab.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plugwiseha.internal.PlugwiseHABindingConstants;
import org.openhab.binding.plugwiseha.internal.api.exception.PlugwiseHAException;
import org.openhab.binding.plugwiseha.internal.api.model.PlugwiseHAController;
import org.openhab.binding.plugwiseha.internal.api.model.dto.Location;
import org.openhab.binding.plugwiseha.internal.config.PlugwiseHAThingConfig;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PlugwiseHAZoneHandler} class is responsible for handling commands
 * and status updates for the Plugwise Home Automation zones/locations.
 * Extends @{link PlugwiseHABaseHandler}
 *
 * @author Bas van Wetten - Initial contribution
 * @author Leo Siepel - finish initial contribution
 *
 */

@NonNullByDefault
public class PlugwiseHAZoneHandler extends PlugwiseHABaseHandler<Location, PlugwiseHAThingConfig> {

    private @Nullable Location location;
    private final Logger logger = LoggerFactory.getLogger(PlugwiseHAZoneHandler.class);

    // Constructor

    public PlugwiseHAZoneHandler(Thing thing) {
        super(thing);
    }

    public static boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return PlugwiseHABindingConstants.THING_TYPE_ZONE.equals(thingTypeUID);
    }

    // Overrides

    @Override
    protected synchronized void initialize(PlugwiseHAThingConfig config) {
        if (thing.getStatus() == INITIALIZING) {
            logger.debug("Initializing Plugwise Home Automation zone handler with config = {}", config);
            if (!config.isValid()) {
                updateStatus(OFFLINE, CONFIGURATION_ERROR,
                        "Invalid configuration for Plugwise Home Automation zone handler.");
                return;
            }

            try {
                PlugwiseHABridgeHandler bridge = this.getPlugwiseHABridge();
                if (bridge != null) {
                    PlugwiseHAController controller = bridge.getController();
                    if (controller != null) {
                        this.location = getEntity(controller, true);

                        setLocationProperties();
                        updateStatus(ONLINE);
                    }
                }
            } catch (PlugwiseHAException e) {
                updateStatus(OFFLINE, COMMUNICATION_ERROR, STATUS_DESCRIPTION_COMMUNICATION_ERROR);
            }
        }
    }

    @Override
    protected @Nullable Location getEntity(PlugwiseHAController controller, Boolean forceRefresh)
            throws PlugwiseHAException {
        PlugwiseHAThingConfig config = getPlugwiseThingConfig();
        Location location = controller.getLocation(config.getId(), forceRefresh);

        return location;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void handleCommand(Location entity, ChannelUID channelUID, Command command) throws PlugwiseHAException {
        String channelID = channelUID.getIdWithoutGroup();

        switch (channelID) {
            case ZONE_SETPOINT_CHANNEL:
                if (command instanceof QuantityType) {
                    QuantityType<Temperature> state = (QuantityType<Temperature>) command;

                    PlugwiseHABridgeHandler bridge = this.getPlugwiseHABridge();
                    if (bridge != null) {
                        PlugwiseHAController controller = bridge.getController();
                        if (controller != null) {
                            try {
                                controller.setLocationThermostat(entity, state.doubleValue());
                                updateState(ZONE_SETPOINT_CHANNEL, (State) command);
                            } catch (PlugwiseHAException e) {
                                logger.warn("Unable to update setpoint for zone '{}': {} -> {}", entity.getName(),
                                        entity.getSetpointTemperature().orElse(null), state.doubleValue());
                            }
                        }
                    }
                }
                break;
            default:
                logger.warn("Ignoring unsupported command = {} for channel = {}", command, channelUID);
        }
    }

    private State getDefaultState(String channelID) {
        State state = UnDefType.NULL;
        switch (channelID) {
            case ZONE_PRESETSCENE_CHANNEL:
            case ZONE_SETPOINT_CHANNEL:
            case ZONE_TEMPERATURE_CHANNEL:
                state = UnDefType.NULL;
                break;
        }
        return state;
    }

    @Override
    protected void refreshChannel(Location entity, ChannelUID channelUID) {
        String channelID = channelUID.getIdWithoutGroup();
        State state = getDefaultState(channelID);

        // TODO Fetch location from API to force refresh - use synchronized block to prevent multiple threads from
        // calling

        switch (channelID) {
            case ZONE_PRESETSCENE_CHANNEL:
                state = new StringType(entity.getPreset());
                break;
            case ZONE_SETPOINT_CHANNEL:
                if (entity.getSetpointTemperature().isPresent()) {
                    state = new DecimalType(entity.getSetpointTemperature().get());
                }
                break;
            case ZONE_TEMPERATURE_CHANNEL:
                if (entity.getTemperature().isPresent()) {
                    state = new DecimalType(entity.getTemperature().get());
                }
                break;
            default:
                break;
        }

        if (state != UnDefType.NULL) {
            updateState(channelID, state);
        }
    }

    protected void setLocationProperties() {
        if (this.location != null) {
            Map<String, String> properties = editProperties();

            Optional.ofNullable(this.location.getActuatorFunctionalities()).ifPresent(actuatorFunctionalities -> {
                properties.put("functionalities", actuatorFunctionalities.keySet().stream().map(e -> e.toString())
                        .collect(Collectors.joining(", ")));

            });

            properties.put("description", Optional.ofNullable(this.location.getDescription()).orElse(""));
            properties.put("type", Optional.ofNullable(this.location.getType()).orElse(""));

            updateProperties(properties);
        }
    }
}
