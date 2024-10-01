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
package org.openhab.binding.ecovacs.internal.api.impl;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ecovacs.internal.api.EcovacsDevice;
import org.openhab.binding.ecovacs.internal.api.EcovacsDevice.EventListener;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json.BatteryReport;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json.ChargeReport;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json.CleanReport;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json.CleanReportV2;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json.ErrorReport;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json.StatsReport;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json.WaterInfoReport;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandJsonResponse.JsonResponsePayloadWrapper;
import org.openhab.binding.ecovacs.internal.api.model.CleanMode;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * @author Danny Baumann - Initial contribution
 */
@NonNullByDefault
class JsonReportParser implements ReportParser {
    private final EcovacsDevice device;
    private final EventListener listener;
    private final Gson gson;
    private final Logger logger;
    private String lastFirmwareVersion = "";

    JsonReportParser(EcovacsDevice device, EventListener listener, ProtocolVersion version, Gson gson, Logger logger) {
        this.device = device;
        this.listener = listener;
        this.gson = gson;
        this.logger = logger;
    }

    @Override
    public void handleMessage(String eventName, String payload) throws DataParsingException {
        JsonResponsePayloadWrapper response;
        try {
            response = gson.fromJson(payload, JsonResponsePayloadWrapper.class);
        } catch (JsonSyntaxException e) {
            // The onFwBuryPoint-bd_sysinfo sends a JSON array instead of the expected JsonResponsePayloadBody object.
            // Since we don't do anything with it anyway, just ignore it
            logger.debug("{}: Got invalid JSON message payload, ignoring: {}", device.getSerialNumber(), payload, e);
            response = null;
        }
        if (response == null) {
            return;
        }
        if (!lastFirmwareVersion.equals(response.header.firmwareVersion)) {
            lastFirmwareVersion = response.header.firmwareVersion;
            listener.onFirmwareVersionChanged(device, lastFirmwareVersion);
        }
        if (eventName.startsWith("on")) {
            eventName = eventName.substring(2);
        } else if (eventName.startsWith("report")) {
            eventName = eventName.substring(6);
        }
        switch (eventName) {
            case "battery": {
                BatteryReport report = payloadAs(response, BatteryReport.class);
                listener.onBatteryLevelUpdated(device, report.percent);
                break;
            }
            case "chargestate": {
                ChargeReport report = payloadAs(response, ChargeReport.class);
                listener.onChargingStateUpdated(device, report.isCharging != 0);
                break;
            }
            case "cleaninfo": {
                CleanReport report = payloadAs(response, CleanReport.class);
                CleanMode mode = report.determineCleanMode(gson);
                if (mode == null) {
                    throw new DataParsingException("Could not get clean mode from response " + payload);
                }
                String area = report.cleanState != null ? report.cleanState.areaDefinition : null;
                handleCleanModeChange(mode, area);
                break;
            }
            case "cleaninfo_v2": {
                CleanReportV2 report = payloadAs(response, CleanReportV2.class);
                CleanMode mode = report.determineCleanMode(gson);
                if (mode == null) {
                    throw new DataParsingException("Could not get clean mode from response " + payload);
                }
                String area = report.cleanState != null && report.cleanState.content != null
                        ? report.cleanState.content.areaDefinition
                        : null;
                handleCleanModeChange(mode, area);
                break;
            }
            case "error": {
                ErrorReport report = payloadAs(response, ErrorReport.class);
                if (report.errorCodes.isEmpty()) {
                    listener.onErrorReported(device, 0);
                } else {
                    for (Integer code : report.errorCodes) {
                        listener.onErrorReported(device, code);
                    }
                }
            }
            case "stats": {
                StatsReport report = payloadAs(response, StatsReport.class);
                listener.onCleaningStatsUpdated(device, report.area, report.timeInSeconds);
                break;
            }
            case "waterinfo": {
                WaterInfoReport report = payloadAs(response, WaterInfoReport.class);
                listener.onWaterSystemPresentUpdated(device, report.waterPlatePresent != 0);
                break;
            }
            // more possible events (unused for now):
            // - "evt" -> EventReport
            // - "lifespan" -> ComponentLifeSpanReport
            // - "speed" -> SpeedReport
        }
    }

    private void handleCleanModeChange(CleanMode mode, @Nullable String areaDefinition) {
        if (mode == CleanMode.CUSTOM_AREA) {
            logger.debug("{}: Custom area cleaning stated with area definition {}", device.getSerialNumber(),
                    areaDefinition);
        }
        listener.onCleaningModeUpdated(device, mode, Optional.ofNullable(areaDefinition));
    }

    private <T> T payloadAs(JsonResponsePayloadWrapper response, Class<T> clazz) throws DataParsingException {
        @Nullable
        T payload = gson.fromJson(response.body.payload, clazz);
        if (payload == null) {
            throw new DataParsingException("Null payload in response " + response);
        }
        return payload;
    }
}
