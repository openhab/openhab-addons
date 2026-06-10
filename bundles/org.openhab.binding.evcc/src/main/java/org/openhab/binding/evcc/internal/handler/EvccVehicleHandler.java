/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.evcc.internal.handler;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;

import java.util.Locale;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

/**
 * The {@link EvccVehicleHandler} is responsible for fetching the data from the API response for Vehicle things
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccVehicleHandler extends EvccBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EvccVehicleHandler.class);

    private String endpoint = "";

    public EvccVehicleHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, channelTypeRegistry);
        type = PROPERTY_TYPE_VEHICLE;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof State state) {
            String datapoint = Utils.getKeyFromChannelUID(channelUID).toLowerCase(Locale.ROOT);
            String value = state.toString();
            if (value.contains(" ")) {
                value = value.substring(0, state.toString().indexOf(" "));
            }
            String url = endpoint + "/" + getPropertyOrConfigValue(PROPERTY_VEHICLE_ID) + "/" + datapoint + "/" + value;
            logger.debug("Sending command to this url: {}", url);
            performApiRequest(url, POST, JsonNull.INSTANCE);
        } else {
            super.handleCommand(channelUID, command);
        }
    }

    @Override
    public void prepareApiResponseForChannelStateUpdate(JsonObject state) {
        state = state.getAsJsonObject(JSON_KEY_VEHICLES).getAsJsonObject(getPropertyOrConfigValue(PROPERTY_VEHICLE_ID));
        updateStatesFromApiResponse(state);
    }

    @Override
    public void initialize() {
        Configuration config = thing.getConfiguration();

        Object oldId = config.get(PROPERTY_ID);
        Object newId = config.get(PROPERTY_VEHICLE_ID);

        if (oldId != null && (newId == null || newId.toString().isBlank())) {
            String migrated = oldId.toString();

            config.put(PROPERTY_VEHICLE_ID, migrated);
            config.remove(PROPERTY_ID);

            updateConfiguration(config);
            logger.info("Migrated evcc vehicle Thing property 'id' -> 'vehicleId'");
        } else if (oldId != null && newId != null) {
            config.remove(PROPERTY_ID);
            updateConfiguration(config);
        }

        if (getPropertyOrConfigValue(PROPERTY_VEHICLE_ID).isEmpty()) {
            logger.warn("No vehicle ID given");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }

        super.initialize();
        Optional.ofNullable(bridgeHandler).ifPresent(handler -> {
            endpoint = String.join("/", handler.getBaseURL(), API_PATH_VEHICLES);
            JsonObject stateOpt = handler.getCachedEvccState().deepCopy();
            if (stateOpt.isEmpty()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                return;
            }

            JsonObject state = stateOpt.getAsJsonObject(JSON_KEY_VEHICLES)
                    .getAsJsonObject(getPropertyOrConfigValue(PROPERTY_VEHICLE_ID));
            commonInitialize(state);
        });
    }

    @Override
    public JsonObject getStateFromCachedState(JsonObject state) {
        return state.has(JSON_KEY_VEHICLES) ? state.getAsJsonObject(JSON_KEY_VEHICLES)
                .getAsJsonObject(getPropertyOrConfigValue(PROPERTY_VEHICLE_ID)) : new JsonObject();
    }
}
