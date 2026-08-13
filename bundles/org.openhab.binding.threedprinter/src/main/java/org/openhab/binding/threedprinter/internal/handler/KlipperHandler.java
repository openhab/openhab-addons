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
import java.util.Locale;
import java.util.stream.Collectors;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.threedprinter.internal.config.KlipperConfiguration;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperMetadataResponse;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperMetadataResponse.KlipperMetadataResult;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperMetadataResponse.KlipperThumbnail;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperObjectsResponse;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperObjectsResponse.KlipperFan;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperObjectsResponse.KlipperGcodeMove;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperObjectsResponse.KlipperHeater;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperObjectsResponse.KlipperPrintStats;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperObjectsResponse.KlipperResult;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperObjectsResponse.KlipperStatus;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperObjectsResponse.KlipperVirtualSdcard;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperObjectsResponse.KlipperWebhooks;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;

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

    private static final String QUERY_URL_SUFFIX = "/printer/objects/query?extruder&heater_bed&print_stats&virtual_sdcard&webhooks&fan&gcode_move";

    private @Nullable KlipperConfiguration config;
    private String lastPreviewFilename = "";
    private @Nullable RawType lastPreviewState;

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
        HttpGetResult getResult = httpGet(baseUrl + QUERY_URL_SUFFIX, cfg.apiKey);
        String json = getResult.body;
        if (json == null) {
            markHttpFailure(getResult.status);
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

        KlipperStatus status = result.status;
        if (status == null) {
            markOffline("@text/offline.comm-error-json");
            return;
        }

        KlipperWebhooks webhooks = status.webhooks;
        if (webhooks == null || !"ready".equals(webhooks.state)) {
            markOffline("@text/offline.comm-error-klippy-not-ready");
            return;
        }

        updateStatus(ThingStatus.ONLINE);

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
        KlipperVirtualSdcard virtualSdcard = status.virtualSdcard;
        if (stats != null) {
            String mappedState = mapKlipperState(stats.state);
            updateState(CHANNEL_PRINTER_STATE, new StringType(mappedState));
            updateState(CHANNEL_PAUSE_RESUME, OnOffType.from(STATE_PAUSED.equals(mappedState)));

            if (!stats.filename.isBlank()) {
                updateState(CHANNEL_JOB_NAME, new StringType(stats.filename));
                updateState(CHANNEL_TIME_ELAPSED, new QuantityType<>(stats.printDuration, Units.SECOND));

                if (virtualSdcard != null) {
                    double progress = virtualSdcard.progress;
                    updateState(CHANNEL_JOB_PROGRESS, new QuantityType<>(progress * 100.0, Units.PERCENT));
                    if (progress > 0) {
                        double elapsed = stats.printDuration;
                        double remaining = progress < 1.0 ? (elapsed / progress - elapsed) : 0.0;
                        updateState(CHANNEL_TIME_REMAINING, new QuantityType<>(remaining, Units.SECOND));
                    }
                }

                if (!stats.filename.equals(lastPreviewFilename)) {
                    logger.debug("Fetching preview for {} (last was '{}')", stats.filename, lastPreviewFilename);
                    fetchAndUpdatePreview(baseUrl, cfg.apiKey, stats.filename);
                } else {
                    // Re-push the cached image every cycle rather than only on filename change, so a channel
                    // linked to an item after the initial fetch (or a UI reconnecting) still gets the preview.
                    RawType cached = lastPreviewState;
                    if (cached != null) {
                        updateState(CHANNEL_JOB_PREVIEW, cached);
                    }
                }
            } else {
                clearJobState();
                clearPreview();
            }
        }

        KlipperFan fan = status.fan;
        if (fan != null) {
            updateState(CHANNEL_FAN_SPEED, new QuantityType<>(fan.speed * 100.0, Units.PERCENT));
        }

        KlipperGcodeMove gcodeMove = status.gcodeMove;
        if (gcodeMove != null) {
            updateState(CHANNEL_PRINT_SPEED, new QuantityType<>(gcodeMove.speedFactor * 100.0, Units.PERCENT));
        }
    }

    private void fetchAndUpdatePreview(String baseUrl, String apiKey, String filename) {
        // The previous job's thumbnail must not linger while a new job's preview is being resolved; only a
        // successful fetch below repopulates it.
        clearPreview();
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8);
        String metaJson = httpGet(baseUrl + "/server/files/metadata?filename=" + encodedFilename, apiKey).body;
        if (metaJson == null) {
            logger.debug("Preview for {}: metadata request failed (see prior GET log)", filename);
            return;
        }
        KlipperMetadataResponse meta = fromJson(metaJson, KlipperMetadataResponse.class);
        if (meta == null) {
            logger.debug("Preview for {}: metadata response was not valid JSON", filename);
            return;
        }
        KlipperMetadataResult metaResult = meta.result;
        if (metaResult == null) {
            logger.debug("Preview for {}: metadata response had no 'result' field", filename);
            return;
        }
        List<KlipperThumbnail> thumbnails = metaResult.thumbnails;
        if (thumbnails == null || thumbnails.isEmpty()) {
            logger.debug("Preview for {}: metadata has no thumbnails", filename);
            return;
        }
        @Nullable
        KlipperThumbnail best = thumbnails.stream().max(Comparator.comparingInt(t -> t.size)).orElse(null);
        if (best == null || best.relativePath.isBlank()) {
            logger.debug("Preview for {}: no usable thumbnail entry (relative_path blank)", filename);
            return;
        }
        // relative_path is relative to the gcode file's own directory, not the gcodes root
        int lastSlash = filename.lastIndexOf('/');
        String dir = lastSlash >= 0 ? filename.substring(0, lastSlash + 1) : "";
        String fullPath = dir + best.relativePath;
        // Encode each path segment individually to preserve the directory separator
        String encodedPath = Arrays.stream(fullPath.split("/"))
                .map(s -> URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20"))
                .collect(Collectors.joining("/"));
        String previewUrl = baseUrl + "/server/files/gcodes/" + encodedPath;
        byte @Nullable [] bytes = httpGetBytes(previewUrl, apiKey);
        if (bytes == null) {
            logger.debug("Preview for {}: image GET {} failed (see prior GET log)", filename, previewUrl);
            return;
        }
        if (bytes.length == 0) {
            logger.debug("Preview for {}: image GET {} returned an empty body", filename, previewUrl);
            return;
        }
        logger.debug("Preview for {}: fetched {} bytes from {}", filename, bytes.length, previewUrl);
        RawType state = new RawType(bytes, "image/png");
        updateState(CHANNEL_JOB_PREVIEW, state);
        lastPreviewFilename = filename;
        lastPreviewState = state;
    }

    private void clearPreview() {
        updateState(CHANNEL_JOB_PREVIEW, UnDefType.UNDEF);
        lastPreviewFilename = "";
        lastPreviewState = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        KlipperConfiguration cfg = config;
        if (cfg == null) {
            return;
        }
        if (command instanceof RefreshType) {
            scheduler.execute(this::refresh);
            return;
        }
        // Command handling performs blocking HTTP I/O; keep it off the framework callback thread.
        scheduler.execute(() -> handleCommandAsync(channelUID, command, cfg));
    }

    private void handleCommandAsync(ChannelUID channelUID, Command command, KlipperConfiguration cfg) {
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
                    int status = httpPost(baseUrl + "/printer/print/cancel", cfg.apiKey, "");
                    if (!HttpStatus.isSuccess(status)) {
                        logger.debug("Failed to cancel print: HTTP {}", status);
                        markHttpFailure(status);
                    }
                    updateState(CHANNEL_CANCEL, OnOffType.OFF);
                }
                break;

            case CHANNEL_NOZZLE_TEMPERATURE_SETPOINT: {
                Integer temp = toCelsius(command);
                if (temp != null) {
                    sendGcode(baseUrl, cfg.apiKey, "M104 S" + temp);
                } else {
                    logger.warn("Unsupported command type {} for channel {}", command, channelUID);
                }
                break;
            }
            case CHANNEL_BED_TEMPERATURE_SETPOINT: {
                Integer temp = toCelsius(command);
                if (temp != null) {
                    sendGcode(baseUrl, cfg.apiKey, "M140 S" + temp);
                } else {
                    logger.warn("Unsupported command type {} for channel {}", command, channelUID);
                }
                break;
            }
            case CHANNEL_PRINT_SPEED: {
                Integer speed = toPercent(command);
                if (speed != null) {
                    sendGcode(baseUrl, cfg.apiKey, "M220 S" + speed);
                } else {
                    logger.warn("Unsupported command type {} for channel {}", command, channelUID);
                }
                break;
            }
            case CHANNEL_FAN_SPEED: {
                Integer speed = toPercent(command);
                if (speed != null) {
                    int s255 = (int) Math.round(speed * 2.55);
                    sendGcode(baseUrl, cfg.apiKey, "M106 S" + s255);
                } else {
                    logger.warn("Unsupported command type {} for channel {}", command, channelUID);
                }
                break;
            }
            default:
                logger.debug("Unhandled command {} for channel {}", command, channelUID);
        }
    }

    private void sendGcode(String baseUrl, String apiKey, String script) {
        int status = httpPost(baseUrl + "/printer/gcode/script", apiKey, "{\"script\":\"" + script + "\"}");
        if (!HttpStatus.isSuccess(status)) {
            logger.debug("G-code script '{}' failed: HTTP {}", script, status);
            markHttpFailure(status);
        }
    }

    private String mapKlipperState(String klipperState) {
        return switch (klipperState.toLowerCase(Locale.ROOT)) {
            case "printing" -> STATE_PRINTING;
            case "paused" -> STATE_PAUSED;
            case "complete" -> STATE_FINISHED;
            case "error" -> STATE_ERROR;
            case "cancelled" -> STATE_IDLE;
            default -> STATE_IDLE;
        };
    }
}
