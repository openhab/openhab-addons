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
package org.openhab.binding.kvv.internal;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * KVVBridgeHandler encapsulates the communication with the KVV API.
 *
 * @author Maximilian Hess - Initial contribution
 */
@NonNullByDefault
public class KVVBridgeHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(KVVBridgeHandler.class);

    private final Cache cache;

    private KVVBridgeConfig config;

    private boolean wasOffline;

    public KVVBridgeHandler(final Bridge bridge) {
        super(bridge);
        this.config = new KVVBridgeConfig();
        this.cache = new Cache();
        this.wasOffline = false;
    }

    public KVVBridgeConfig getBridgeConfig() {
        return this.config;
    }

    @Override
    public void initialize() {
        this.config = getConfigAs(KVVBridgeConfig.class);
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // There is nothing to handle in the bridge handler
    }

    /**
     * Returns the latest {@link DepartureResult}. Returns {@code null} if the result could not be retrieved.
     *
     * @return the latest {@link DepartureResult}.
     */
    public synchronized @Nullable DepartureResult queryKVV(final KVVStopConfig stopConfig) {
        // is there an up-to-date value in the cache?
        final DepartureResult cr = this.cache.get(stopConfig.stopId);
        if (cr != null) {
            return cr;
        }

        final String url = String.format(KVVBindingConstants.API_FORMAT, stopConfig.stopId, config.maxTrains);

        String data;
        try {
            data = HttpUtil.executeUrl("GET", url, KVVBindingConstants.TIMEOUT_IN_SECONDS * 1000);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Failed to connect to KVV API");
            logger.debug("Failed to get departures from '{}'", url, e);
            this.wasOffline = true;
            return null;
        }

        DepartureResult result;
        try {
            result = new Gson().fromJson(data, DepartureResult.class);
        } catch (JsonSyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Failed to connect to KVV API");
            logger.debug("Failed to parse departure data", e);
            logger.debug("Server returned '{}'", data);
            this.wasOffline = true;
            return null;
        }

        if (result == null) {
            return null;
        }

        if (this.wasOffline) {
            updateStatus(ThingStatus.ONLINE);
        }

        this.cache.update(stopConfig.stopId, result);
        return result;
    }

    @NonNullByDefault
    public static class Cache {

        private int updateInterval;

        private final Map<String, CacheLine> cache;

        /**
         * Creates a new @{link Cache}.
         *
         */
        public Cache() {
            this.updateInterval = KVVBindingConstants.CACHE_DEFAULT_UPDATEINTERVAL;
            this.cache = new HashMap<>();
        }

        /*
         * Updates the @{code updateInterval}.
         *
         * @param updateInterval the new @{code updateInterval}
         */
        public void setUpdateInterval(final int updateInterval) {
            this.updateInterval = updateInterval;
        }

        /**
         * Returns the result of the latest API call for a given stop. Returns @{code null} if the latest result is
         * out dated or the @{link CacheLine} does not exist. Not distinguishing between those two cases is sufficient,
         * because it leads to the same handling of @{link KVVBridgeHandler}.
         * 
         * @param stopId
         * @return the result of the latest API call for a given stop.
         */
        @Nullable
        public DepartureResult get(final String stopId) {
            if (!this.cache.containsKey(stopId)) {
                return null;
            }

            final CacheLine cl = this.cache.get(stopId);
            if (cl.getEvictAfter().before(new Date())) {
                return null;
            }

            return cl.getPayload();
        }

        public void update(final String stopId, final DepartureResult payload) {
            if (!this.cache.containsKey(stopId)) {
                this.cache.put(stopId, new CacheLine(payload, new Date()));
            }

            final CacheLine cl = this.cache.get(stopId);

            // the eviction time is calculated by adding an offset of 60 percent of the regular update interval of
            // the bridge handler
            cl.update(payload, new Date(System.currentTimeMillis() + (long) (0.6 * this.updateInterval * 1000)));
        }
    }

    @NonNullByDefault
    public static class CacheLine {

        private Date evictAfter;

        private DepartureResult payload;

        public CacheLine(final DepartureResult payload, final Date evictAfter) {
            this.payload = payload;
            this.evictAfter = evictAfter;
        }

        public Date getEvictAfter() {
            return this.evictAfter;
        }

        public DepartureResult getPayload() {
            return this.payload;
        }

        public void update(final DepartureResult payload, final Date evictAfter) {
            this.payload = payload;
            this.evictAfter = evictAfter;
        }
    }
}
