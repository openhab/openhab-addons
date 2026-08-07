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

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.threedprinter.internal.config.OctoPrintConfiguration;
import org.openhab.binding.threedprinter.internal.dto.octoprint.OctoPrintJobResponse;
import org.openhab.binding.threedprinter.internal.dto.octoprint.OctoPrintJobResponse.OctoPrintJob;
import org.openhab.binding.threedprinter.internal.dto.octoprint.OctoPrintJobResponse.OctoPrintProgress;
import org.openhab.binding.threedprinter.internal.dto.octoprint.OctoPrintPrinterResponse;
import org.openhab.binding.threedprinter.internal.dto.octoprint.OctoPrintPrinterResponse.OctoPrintState;
import org.openhab.binding.threedprinter.internal.dto.octoprint.OctoPrintPrinterResponse.OctoPrintTemperature;
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

/**
 * Handler for printers running OctoPrint.
 *
 * <p>
 * OctoPrint REST API reference: https://docs.octoprint.org/en/master/api/
 * Authentication via X-Api-Key header.
 * Thumbnails require the PrusaSlicer Thumbnails plugin.
 *
 * @author Scott Hanson - Initial contribution
 */
@NonNullByDefault
public class OctoPrintHandler extends AbstractPrinterHandler {

    private @Nullable OctoPrintConfiguration config;
    private String lastPreviewFilename = "";
    private @Nullable RawType lastPreviewState;

    public OctoPrintHandler(Thing thing, HttpClient httpClient) {
        super(thing, httpClient);
    }

    @Override
    public void initialize() {
        OctoPrintConfiguration cfg = getConfigAs(OctoPrintConfiguration.class);
        if (cfg.hostname.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-hostname");
            return;
        }
        if (cfg.apiKey.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-missing-apikey");
            return;
        }
        config = cfg;
        super.initialize();
    }

    @Override
    protected int getRefreshInterval() {
        OctoPrintConfiguration cfg = config;
        return cfg != null ? cfg.refreshInterval : 30;
    }

    @Override
    protected void refresh() {
        OctoPrintConfiguration cfg = config;
        if (cfg == null) {
            return;
        }
        String baseUrl = "http://" + cfg.hostname + ":" + cfg.port;

        // Fetch printer state + temperatures
        String printerJson = httpGet(baseUrl + "/api/printer", cfg.apiKey);
        if (printerJson == null) {
            markOffline("@text/offline.comm-error-unreachable");
            return;
        }

        OctoPrintPrinterResponse printerResponse = fromJson(printerJson, OctoPrintPrinterResponse.class);
        if (printerResponse == null) {
            markOffline("@text/offline.comm-error-json");
            return;
        }

        updateStatus(ThingStatus.ONLINE);

        OctoPrintTemperature temps = printerResponse.temperature;
        if (temps != null) {
            OctoPrintTemperature.OctoPrintTempReading tool0 = temps.tool0;
            if (tool0 != null) {
                updateState(CHANNEL_NOZZLE_TEMPERATURE, new QuantityType<Temperature>(tool0.actual, SIUnits.CELSIUS));
                updateState(CHANNEL_NOZZLE_TEMPERATURE_SETPOINT,
                        new QuantityType<Temperature>(tool0.target, SIUnits.CELSIUS));
            }
            OctoPrintTemperature.OctoPrintTempReading bed = temps.bed;
            if (bed != null) {
                updateState(CHANNEL_BED_TEMPERATURE, new QuantityType<Temperature>(bed.actual, SIUnits.CELSIUS));
                updateState(CHANNEL_BED_TEMPERATURE_SETPOINT,
                        new QuantityType<Temperature>(bed.target, SIUnits.CELSIUS));
            }
        }

        OctoPrintState stateObj = printerResponse.state;
        if (stateObj != null) {
            String mappedState = mapOctoPrintState(stateObj);
            updateState(CHANNEL_PRINTER_STATE, new StringType(mappedState));
            updateState(CHANNEL_PAUSE_RESUME, OnOffType.from(STATE_PAUSED.equals(mappedState)));
        }

        // Fetch job info
        String jobJson = httpGet(baseUrl + "/api/job", cfg.apiKey);
        if (jobJson == null) {
            return;
        }

        OctoPrintJobResponse jobResponse = fromJson(jobJson, OctoPrintJobResponse.class);
        if (jobResponse == null) {
            return;
        }

        OctoPrintJob job = jobResponse.job;
        OctoPrintJob.OctoPrintFile file = job != null ? job.file : null;
        String filename = file != null ? file.name : "";

        if (file == null || filename.isBlank()) {
            clearJobState();
            lastPreviewFilename = "";
            lastPreviewState = null;
            return;
        }

        String name = file.display.isBlank() ? file.name : file.display;
        updateState(CHANNEL_JOB_NAME, new StringType(name));

        OctoPrintProgress progress = jobResponse.progress;
        if (progress != null) {
            updateState(CHANNEL_JOB_PROGRESS,
                    new QuantityType<>(progress.completion > 0 ? progress.completion : 0, Units.PERCENT));
            updateState(CHANNEL_TIME_ELAPSED, new QuantityType<>(progress.printTime, Units.SECOND));
            updateState(CHANNEL_TIME_REMAINING, new QuantityType<>(progress.printTimeLeft, Units.SECOND));
        }

        // Use the raw filename (not display name) as the key for the thumbnail URL
        if (!filename.equals(lastPreviewFilename)) {
            String encodedName = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
            byte @Nullable [] bytes = httpGetBytes(baseUrl + "/plugin/prusaslicerthumbnails/thumbnail/" + encodedName,
                    cfg.apiKey);
            if (bytes != null && bytes.length > 0) {
                RawType state = new RawType(bytes, "image/png");
                updateState(CHANNEL_JOB_PREVIEW, state);
                lastPreviewFilename = filename;
                lastPreviewState = state;
            }
        } else {
            // Re-push the cached image every cycle rather than only on change, so a channel
            // linked to an item after the initial fetch (or a UI reconnecting) still gets it.
            RawType cached = lastPreviewState;
            if (cached != null) {
                updateState(CHANNEL_JOB_PREVIEW, cached);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        OctoPrintConfiguration cfg = config;
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

    private void handleCommandAsync(ChannelUID channelUID, Command command, OctoPrintConfiguration cfg) {
        String baseUrl = "http://" + cfg.hostname + ":" + cfg.port;

        switch (channelUID.getId()) {
            case CHANNEL_PAUSE_RESUME:
                if (command instanceof OnOffType onOff) {
                    String action = OnOffType.ON.equals(onOff) ? "pause" : "resume";
                    int status = httpPost(baseUrl + "/api/job", cfg.apiKey,
                            "{\"command\":\"pause\",\"action\":\"" + action + "\"}");
                    if (!HttpStatus.isSuccess(status)) {
                        logger.warn("Failed to {} print: HTTP {}", action, status);
                    }
                }
                break;

            case CHANNEL_CANCEL:
                if (OnOffType.ON.equals(command)) {
                    int status = httpPost(baseUrl + "/api/job", cfg.apiKey, "{\"command\":\"cancel\"}");
                    if (!HttpStatus.isSuccess(status)) {
                        logger.warn("Failed to cancel print: HTTP {}", status);
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

    private void sendGcode(String baseUrl, String apiKey, String gcode) {
        int status = httpPost(baseUrl + "/api/printer/command", apiKey, "{\"command\":\"" + gcode + "\"}");
        if (!HttpStatus.isSuccess(status)) {
            logger.warn("G-code command '{}' failed: HTTP {}", gcode, status);
        }
    }

    private String mapOctoPrintState(OctoPrintState state) {
        OctoPrintState.OctoPrintStateFlags flags = state.flags;
        if (flags != null) {
            if (flags.printing) {
                return STATE_PRINTING;
            }
            if (flags.paused) {
                return STATE_PAUSED;
            }
            if (flags.error) {
                return STATE_ERROR;
            }
            if (flags.cancelling) {
                return STATE_BUSY;
            }
        }
        String text = state.text.toUpperCase();
        if (text.contains("PRINT")) {
            return STATE_PRINTING;
        }
        if (text.contains("PAUS")) {
            return STATE_PAUSED;
        }
        if (text.contains("FINISH") || text.contains("COMPLET")) {
            return STATE_FINISHED;
        }
        if (text.contains("ERROR")) {
            return STATE_ERROR;
        }
        return STATE_IDLE;
    }
}
