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
package org.openhab.binding.kvv.internal;

import java.io.IOException;

import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.RawType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * KVVBridgeHandler encapsulates the communication with the KVV API.
 *
 * @author Maximilian Hess - Initial contribution
 */
@NonNullByDefault
public class KVVBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(KVVBridgeHandler.class);

    public KVVBridgeHandler(final Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Returns the latest {@link DepartureResult}. Returns {@code null} if the result could not be retrieved.
     *
     * @return the latest {@link DepartureResult}.
     */
    public @Nullable DepartureResult queryKVV(final KVVStationConfig config) {
        final String url = KVVBindingConstants.API_URL + "/departures/bystop/" + config.stationId + "?key="
                + config.apiKey + "&maxInfos=" + config.maxTrains;

        String data;
        try {
            data = HttpUtil.executeUrl("GET", url, KVVBindingConstants.TIMEOUT_IN_SECONDS * 1000);
        } catch (IOException e) {
            logger.warn("Failed to get departures from '{}': {}", url, e);
            return null;
        }

        final DepartureResult result = new Gson().fromJson(data, DepartureResult.class);
        if (result.departures.size() != config.maxTrains) {
            logger.warn("Result size (={}) differs from maxTrain setting (={})", result.departures.size(),
                    config.maxTrains);
            return null;
        }
        return result;
    }
}
