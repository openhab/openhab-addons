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
package org.openhab.binding.fineoffsetweatherstation.internal.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.fineoffsetweatherstation.internal.FineOffsetGatewayConfiguration;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.SensorGatewayBinding;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.MeasuredValue;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.SensorDevice;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.response.SystemInfo;
import org.openhab.binding.fineoffsetweatherstation.internal.handler.ThingStatusListener;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;

/**
 * Queries a gateway via the Ecowitt HTTP API.
 * <p>
 * The endpoints return the data pre-decoded as JSON; {@link EcowittDataParser} converts them into the same domain
 * objects the TCP services produce, so the handler does not need to know which transport is in use.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class EcowittHttpGatewayQueryService extends GatewayQueryService {

    private static final int TIMEOUT_MS = 5000;
    private static final int MAX_SENSOR_PAGES = 8;

    private final EcowittDataParser parser = new EcowittDataParser();

    public EcowittHttpGatewayQueryService(FineOffsetGatewayConfiguration config,
            @Nullable ThingStatusListener thingStatusListener) {
        super(config, thingStatusListener);
    }

    @Override
    public @Nullable String getFirmwareVersion() {
        String response = get("get_version");
        return response == null ? null : parser.parseFirmwareVersion(response);
    }

    @Override
    public Map<SensorGatewayBinding, SensorDevice> getRegisteredSensors() {
        String version = get("get_version");
        if (version == null) {
            return Map.of();
        }
        int pageCount = Math.min(parser.parseSensorPageCount(version), MAX_SENSOR_PAGES);
        @Nullable
        SystemInfo systemInfo = fetchSystemInfo();
        boolean useWh24 = systemInfo != null && systemInfo.isUseWh24();
        List<String> pages = new ArrayList<>();
        for (int page = 1; page <= pageCount; page++) {
            String response = get("get_sensors_info?page=" + page);
            if (response != null) {
                pages.add(response);
            }
        }
        return parser.parseSensors(pages, useWh24);
    }

    @Override
    public @Nullable SystemInfo fetchSystemInfo() {
        String response = get("get_device_info");
        return response == null ? null : parser.parseSystemInfo(response);
    }

    @Override
    public Collection<MeasuredValue> getMeasuredValues() {
        String response = get("get_livedata_info");
        if (response == null) {
            return List.of();
        }
        return parser.parseLiveData(response);
    }

    /**
     * @return {@code true} if the configured device answers {@code get_version} as an Ecowitt HTTP API gateway
     */
    public boolean isEcowittGateway() {
        String response = get("get_version");
        return response != null && parser.isEcowittPlatform(response);
    }

    private @Nullable String get(String endpoint) {
        String url = "http://" + config.ip + "/" + endpoint;
        try {
            String response = HttpUtil.executeUrl("GET", url, TIMEOUT_MS);
            updateThingStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
            logger.trace("GET {} -> {}", url, response);
            return response;
        } catch (IOException e) {
            logger.debug("GET {} failed: {}", url, e.getMessage());
            updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return null;
        }
    }
}
