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
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.deviceapi.json.WorkStateReport;
import org.openhab.binding.ecovacs.internal.api.impl.dto.response.portal.PortalIotCommandJsonResponse.JsonResponsePayloadWrapper;
import org.openhab.binding.ecovacs.internal.api.model.CleanMode;
import org.openhab.binding.ecovacs.internal.api.util.DataParsingException;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
        // Handle fwburypoint events separately - they have a different body structure
        // (fields directly in body, not nested in body.data)
        if (eventName.equals("onfwburypoint-bd_task-mow-auto-stop")) {
            handleMowTaskStop(payload);
            return;
        }

        JsonResponsePayloadWrapper response;
        try {
            response = gson.fromJson(payload, JsonResponsePayloadWrapper.class);
        } catch (JsonSyntaxException e) {
            // The onFwBuryPoint-bd_sysinfo sends a JSON array instead of the expected JsonResponsePayloadBody object.
            // Since we don't do anything with it anyway, just ignore it
            logger.debug("{}: Got invalid JSON data ({}), ignoring payload: {}", device.getSerialNumber(),
                    e.getMessage(), payload);
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
                String area = report.cleanState != null ? report.cleanState.getAreaDefinition() : null;
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
                if (report.mowedArea > 0) {
                    listener.onMowingStatsUpdated(device, report.mowedArea, report.timeInSeconds);
                } else {
                    listener.onCleaningStatsUpdated(device, report.area, report.timeInSeconds);
                }
                break;
            }
            case "waterinfo": {
                WaterInfoReport report = payloadAs(response, WaterInfoReport.class);
                listener.onWaterSystemPresentUpdated(device, report.waterPlatePresent != 0);
                break;
            }
            case "workstate": {
                WorkStateReport report = payloadAs(response, WorkStateReport.class);
                CleanMode mode = report.determineCleanMode(gson);
                if (mode == null) {
                    throw new DataParsingException("Could not get clean mode from response " + payload);
                }
                handleCleanModeChange(mode, null);
                break;
            }
            default:
                logger.trace("{}: Ignoring unhandled event '{}': {}", device.getSerialNumber(), eventName, payload);
                break;
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

    private void handleMowTaskStop(String payload) {
        try {
            JsonObject root = gson.fromJson(payload, JsonObject.class);
            if (root == null || !root.has("body")) {
                return;
            }
            JsonObject body = root.getAsJsonObject("body");
            if (body == null || !body.has("mowedArea")) {
                return;
            }
            // mowedArea is in m² (float), time is in seconds (float), ts is epoch milliseconds (string)
            double mowedAreaSqM = body.get("mowedArea").getAsDouble();
            double timeSeconds = body.has("time") ? body.get("time").getAsDouble() : 0;
            long startTimestampMs = body.has("ts") ? Long.parseLong(body.get("ts").getAsString()) : 0;
            long startTimestamp = startTimestampMs / 1000;

            // Convert area to cm² (int) and time to seconds (int) to match the existing callback signature
            int areaSqCm = (int) Math.round(mowedAreaSqM * 10000);
            int durationSeconds = (int) Math.round(timeSeconds);

            listener.onMowingSessionFinished(device, startTimestamp, durationSeconds, areaSqCm);
        } catch (Exception e) {
            logger.debug("{}: Could not parse mow task stop event: {}", device.getSerialNumber(), e.getMessage());
        }
    }
}
