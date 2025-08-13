/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link EvccStatisticsHandler} is responsible for fetching the data from the API response for Site things
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccStatisticsHandler extends EvccBaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EvccStatisticsHandler.class);

    public EvccStatisticsHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, channelTypeRegistry);
    }

    @Override
    public void initialize() {
        super.initialize();
        Optional.ofNullable(bridgeHandler).ifPresent(handler -> {
            handler.register(this);
            updateStatus(ThingStatus.ONLINE);
            isInitialized = true;
        });
    }

    @Override
    public JsonObject getStateFromCachedState(JsonObject state) {
        return new JsonObject();
    }

    @Override
    public void updateFromEvccState(JsonObject state) {
        state = state.has(JSON_MEMBER_STATISTICS) ? state.getAsJsonObject(JSON_MEMBER_STATISTICS) : new JsonObject();
        for (String statisticsKey : state.keySet()) {
            JsonObject statistic = state.getAsJsonObject(statisticsKey);
            logger.debug("Extracting statistics for {}", statisticsKey);
            for (Map.Entry<@Nullable String, @Nullable JsonElement> entry : statistic.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();
                if (null != key && null != value) {
                    ChannelGroupUID channelGroupUID = new ChannelGroupUID(thing.getUID(),
                            Utils.sanitizeChannelID(statisticsKey));
                    if ("chargedKWh".equals(key)) {
                        key = "chargedEnergy";
                    }
                    ChannelUID channelUID = new ChannelUID(channelGroupUID, Utils.sanitizeChannelID(key));
                    updateState(channelUID, new DecimalType(value.getAsDouble()));
                }
            }
        }
    }
}
