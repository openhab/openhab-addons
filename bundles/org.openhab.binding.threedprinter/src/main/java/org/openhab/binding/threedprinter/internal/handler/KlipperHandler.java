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
package org.openhab.binding.threedprinter.internal.handler;

import static org.openhab.binding.threedprinter.internal.ThreedprinterBindingConstants.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.threedprinter.internal.config.KlipperConfiguration;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperMetadataResponse;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperMetadataResponse.KlipperMetadataResult;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperMetadataResponse.KlipperThumbnail;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperObjectsResponse;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperObjectsResponse.KlipperDisplayStatus;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperObjectsResponse.KlipperFan;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperObjectsResponse.KlipperGcodeMove;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperObjectsResponse.KlipperHeater;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperObjectsResponse.KlipperPrintStats;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperObjectsResponse.KlipperResult;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperObjectsResponse.KlipperStatus;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * Handler for Klipper printers accessed via the Moonraker REST API.
 *
 * <p>
 * Moonraker API reference: https://moonraker.readthedocs.io/
 * Default port is 7125. API key is optional on local networks.
 *
 * @author Scott Hanson - Initial contribution
 */
@NonNullByDefault
public class KlipperHandler extends AbstractPrinterHandler {

    private static final String QUERY_URL_SUFFIX = "/printer/objects/query?extruder&heater_bed&print_stats&display_status&fan&gcode_move";

    private @Nullable KlipperConfiguration config;
    private String lastPreviewFilename = "";

    public KlipperHandler(Thing thing, HttpClient httpClient) {
        super(thing, httpClient);
    }

    @Override
    public void initialize() {
        KlipperConfiguration cfg = getConfigAs(KlipperConfiguration.class);
        if (cfg.hostname.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-hostname");
            return;
        }
        config = cfg;
        super.initialize();
    }

    @Override
    protected int getRefreshInterval() {
        KlipperConfiguration cfg = config;
        return cfg != null ? cfg.refreshInterval : 30;
    }

    @Override
    protected void refresh() {
        KlipperConfiguration cfg = config;
        if (cfg == null) {
            return;
        }
        String baseUrl = "http://" + cfg.hostname + ":" + cfg.port;
        String json = httpGet(baseUrl + QUERY_URL_SUFFIX, cfg.apiKey);
        if (json == null) {
            markOffline("@text/offline.comm-error-unreachable");
            return;
        }

        KlipperObjectsResponse response = fromJson(json, KlipperObjectsResponse.class);
        if (response == null) {
            markOffline("@text/offline.comm-error-json");
            return;
        }

        KlipperResult result = response.result;
        if (result == null) {
            markOffline("@text/offline.comm-error-json");
            return;
        }

        updateStatus(ThingStatus.ONLINE);

        KlipperStatus status = result.status;
        if (status == null) {
            return;
        }

        KlipperHeater extruder = status.extruder;
        if (extruder != null) {
            updateState(CHANNEL_NOZZLE_TEMPERATURE,
                    new QuantityType<Temperature>(extruder.temperature, SIUnits.CELSIUS));
            updateState(CHANNEL_NOZZLE_TEMPERATURE_SETPOINT,
                    new QuantityType<Temperature>(extruder.target, SIUnits.CELSIUS));
        }

        KlipperHeater bed = status.heaterBed;
        if (bed != null) {
            updateState(CHANNEL_BED_TEMPERATURE, new QuantityType<Temperature>(bed.temperature, SIUnits.CELSIUS));
            updateState(CHANNEL_BED_TEMPERATURE_SETPOINT, new QuantityType<Temperature>(bed.target, SIUnits.CELSIUS));
        }

        KlipperPrintStats stats = status.printStats;
        if (stats != null) {
            String mappedState = mapKlipperState(stats.state);
            updateState(CHANNEL_PRINTER_STATE, new StringType(mappedState));
            updateState(CHANNEL_PAUSE_RESUME, OnOffType.from(STATE_PAUSED.equals(mappedState)));
            updateState(CHANNEL_JOB_NAME, new StringType(stats.filename));
            updateState(CHANNEL_TIME_ELAPSED, new DecimalType((long) stats.printDuration));

            if (!stats.filename.isBlank()) {
                if (!stats.filename.equals(lastPreviewFilename)) {
                    lastPreviewFilename = stats.filename;
                    fetchAndUpdatePreview(baseUrl, cfg.apiKey, stats.filename);
                }
            } else {
                lastPreviewFilename = "";
            }
        }

        KlipperDisplayStatus display = status.displayStatus;
        if (display != null) {
            updateState(CHANNEL_JOB_PROGRESS, new DecimalType(display.progress * 100.0));
        }

        KlipperFan fan = status.fan;
        if (fan != null) {
            updateState(CHANNEL_FAN_SPEED, new DecimalType(fan.speed * 100.0));
        }

        KlipperGcodeMove gcodeMove = status.gcodeMove;
        if (gcodeMove != null) {
            updateState(CHANNEL_PRINT_SPEED, new DecimalType(gcodeMove.speedFactor * 100.0));
        }

        // Estimate time remaining from progress and elapsed if available
        if (display != null && stats != null && display.progress > 0) {
            long elapsed = (long) stats.printDuration;
            long remaining = display.progress < 1.0 ? (long) (elapsed / display.progress - elapsed) : 0L;
            updateState(CHANNEL_TIME_REMAINING, new DecimalType(remaining));
        }
    }

    private void fetchAndUpdatePreview(String baseUrl, String apiKey, String filename) {
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        String metaJson = httpGet(baseUrl + "/server/files/metadata?filename=" + encodedFilename, apiKey);
        if (metaJson == null) {
            return;
        }
        KlipperMetadataResponse meta = fromJson(metaJson, KlipperMetadataResponse.class);
        if (meta == null) {
            return;
        }
        KlipperMetadataResult metaResult = meta.result;
        if (metaResult == null) {
            return;
        }
        List<KlipperThumbnail> thumbnails = metaResult.thumbnails;
        if (thumbnails == null || thumbnails.isEmpty()) {
            return;
        }
        @Nullable
        KlipperThumbnail best = thumbnails.stream().max(Comparator.comparingInt(t -> t.size)).orElse(null);
        if (best == null || best.relativePath.isBlank()) {
            return;
        }
        // Encode each path segment individually to preserve the directory separator
        String encodedPath = Arrays.stream(best.relativePath.split("/"))
                .map(s -> URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20"))
                .collect(Collectors.joining("/"));
        byte @Nullable [] bytes = httpGetBytes(baseUrl + "/server/files/gcodes/" + encodedPath, apiKey);
        if (bytes != null && bytes.length > 0) {
            updateState(CHANNEL_JOB_PREVIEW, new RawType(bytes, "image/png"));
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        KlipperConfiguration cfg = config;
        if (cfg == null) {
            return;
        }
        if (command instanceof RefreshType) {
            refresh();
            return;
        }

        String baseUrl = "http://" + cfg.hostname + ":" + cfg.port;

        switch (channelUID.getId()) {
            case CHANNEL_PAUSE_RESUME:
                if (command instanceof OnOffType onOff) {
                    String script = OnOffType.ON.equals(onOff) ? "PAUSE" : "RESUME";
                    sendGcode(baseUrl, cfg.apiKey, script);
                }
                break;

            case CHANNEL_CANCEL:
                if (OnOffType.ON.equals(command)) {
                    httpPost(baseUrl + "/printer/print/cancel", cfg.apiKey, "");
                    updateState(CHANNEL_CANCEL, OnOffType.OFF);
                }
                break;

            case CHANNEL_NOZZLE_TEMPERATURE_SETPOINT:
                sendGcode(baseUrl, cfg.apiKey, "M104 S" + toGcodeTemp(command));
                break;

            case CHANNEL_BED_TEMPERATURE_SETPOINT:
                sendGcode(baseUrl, cfg.apiKey, "M140 S" + toGcodeTemp(command));
                break;

            case CHANNEL_PRINT_SPEED:
                if (command instanceof DecimalType dec) {
                    sendGcode(baseUrl, cfg.apiKey, "M220 S" + dec.intValue());
                }
                break;

            case CHANNEL_FAN_SPEED:
                if (command instanceof DecimalType dec) {
                    int s255 = (int) Math.round(dec.doubleValue() * 2.55);
                    sendGcode(baseUrl, cfg.apiKey, "M106 S" + s255);
                }
                break;

            default:
                logger.debug("Unhandled command {} for channel {}", command, channelUID);
        }
    }

    private void sendGcode(String baseUrl, String apiKey, String script) {
        httpPost(baseUrl + "/printer/gcode/script", apiKey, "{\"script\":\"" + script + "\"}");
    }

    private int toGcodeTemp(Command command) {
        if (command instanceof QuantityType<?> qty) {
            QuantityType<?> celsius = qty.toUnit(SIUnits.CELSIUS);
            return celsius != null ? celsius.intValue() : qty.intValue();
        }
        if (command instanceof DecimalType dec) {
            return dec.intValue();
        }
        return 0;
    }

    private String mapKlipperState(String klipperState) {
        return switch (klipperState.toLowerCase()) {
            case "printing" -> STATE_PRINTING;
            case "paused" -> STATE_PAUSED;
            case "complete" -> STATE_FINISHED;
            case "error" -> STATE_ERROR;
            case "cancelled" -> STATE_IDLE;
            default -> STATE_IDLE;
        };
    }
}
