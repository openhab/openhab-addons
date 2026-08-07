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

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.threedprinter.internal.config.PrusaLinkConfiguration;
import org.openhab.binding.threedprinter.internal.dto.prusa.PrusaJobResponse;
import org.openhab.binding.threedprinter.internal.dto.prusa.PrusaJobResponse.PrusaJobFile;
import org.openhab.binding.threedprinter.internal.dto.prusa.PrusaStatusResponse;
import org.openhab.binding.threedprinter.internal.dto.prusa.PrusaStatusResponse.PrusaJobData;
import org.openhab.binding.threedprinter.internal.dto.prusa.PrusaStatusResponse.PrusaPrinterData;
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
 * Handler for Prusa printers using the PrusaLink v1 REST API.
 *
 * <p>
 * PrusaLink API reference: https://github.com/prusa3d/Prusa-Link-Web
 * Authentication: X-Api-Key header (newer firmware) or HTTP Digest (older firmware).
 * This handler uses the X-Api-Key header approach.
 *
 * @author Scott Hanson - Initial contribution
 */
@NonNullByDefault
public class PrusaLinkHandler extends AbstractPrinterHandler {

    private @Nullable PrusaLinkConfiguration config;
    private String lastPreviewFilename = "";
    private @Nullable RawType lastPreviewState;
    private int lastJobId = -1;

    public PrusaLinkHandler(Thing thing, HttpClient httpClient) {
        super(thing, httpClient);
    }

    @Override
    public void initialize() {
        PrusaLinkConfiguration cfg = getConfigAs(PrusaLinkConfiguration.class);
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
        PrusaLinkConfiguration cfg = config;
        return cfg != null ? cfg.refreshInterval : 30;
    }

    @Override
    protected void refresh() {
        PrusaLinkConfiguration cfg = config;
        if (cfg == null) {
            return;
        }
        String baseUrl = "http://" + cfg.hostname + ":" + cfg.port;
        String json = httpGet(baseUrl + "/api/v1/status", cfg.apiKey);
        if (json == null) {
            markOffline("@text/offline.comm-error-unreachable");
            return;
        }

        PrusaStatusResponse response = fromJson(json, PrusaStatusResponse.class);
        if (response == null) {
            markOffline("@text/offline.comm-error-json");
            return;
        }

        updateStatus(ThingStatus.ONLINE);

        PrusaPrinterData printer = response.printer;
        if (printer != null) {
            updateState(CHANNEL_PRINTER_STATE, new StringType(mapPrusaState(printer.state)));
            updateState(CHANNEL_NOZZLE_TEMPERATURE, new QuantityType<Temperature>(printer.tempNozzle, SIUnits.CELSIUS));
            updateState(CHANNEL_NOZZLE_TEMPERATURE_SETPOINT,
                    new QuantityType<Temperature>(printer.targetNozzle, SIUnits.CELSIUS));
            updateState(CHANNEL_BED_TEMPERATURE, new QuantityType<Temperature>(printer.tempBed, SIUnits.CELSIUS));
            updateState(CHANNEL_BED_TEMPERATURE_SETPOINT,
                    new QuantityType<Temperature>(printer.targetBed, SIUnits.CELSIUS));
            updateState(CHANNEL_PRINT_SPEED, new QuantityType<>(printer.speed, Units.PERCENT));
            // PrusaLink reports fan_print as RPM; normalise to 0-100% (max ~8000 RPM)
            int fanPct = printer.fanPrint > 0 ? Math.min(100, printer.fanPrint / 80) : 0;
            updateState(CHANNEL_FAN_SPEED, new QuantityType<>(fanPct, Units.PERCENT));
            updateState(CHANNEL_PAUSE_RESUME, OnOffType.from("PAUSED".equalsIgnoreCase(printer.state)));
        }

        PrusaJobData job = response.job;
        if (job != null) {
            lastJobId = job.id;
            updateState(CHANNEL_JOB_PROGRESS, new QuantityType<>(job.progress, Units.PERCENT));
            updateState(CHANNEL_TIME_ELAPSED, new QuantityType<>(job.timePrinting, Units.SECOND));
            updateState(CHANNEL_TIME_REMAINING, new QuantityType<>(job.timeRemaining, Units.SECOND));
        } else {
            lastJobId = -1;
            clearJobState();
        }

        // /api/v1/status does not include file name or thumbnail info; fetch /api/v1/job for that
        updateJobFile(baseUrl, cfg.apiKey);
    }

    private void updateJobFile(String baseUrl, String apiKey) {
        String jobJson = httpGet(baseUrl + "/api/v1/job", apiKey);
        PrusaJobResponse jobResponse = jobJson != null ? fromJson(jobJson, PrusaJobResponse.class) : null;
        PrusaJobFile file = jobResponse != null ? jobResponse.file : null;

        if (file == null) {
            updateState(CHANNEL_JOB_NAME, new StringType(""));
            updateState(CHANNEL_JOB_PREVIEW, UnDefType.UNDEF);
            lastPreviewFilename = "";
            lastPreviewState = null;
            return;
        }

        String name = file.displayName.isBlank() ? file.name : file.displayName;
        updateState(CHANNEL_JOB_NAME, new StringType(name));

        String thumbnailRef = file.refs != null ? file.refs.thumbnail : "";
        if (thumbnailRef.isBlank()) {
            return;
        }
        if (thumbnailRef.equals(lastPreviewFilename)) {
            // Re-push the cached image every cycle rather than only on change, so a channel linked to an
            // item after the initial fetch (or a UI reconnecting) still gets the preview.
            RawType cached = lastPreviewState;
            if (cached != null) {
                updateState(CHANNEL_JOB_PREVIEW, cached);
            }
            return;
        }
        byte[] bytes = httpGetBytes(baseUrl + thumbnailRef, apiKey);
        if (bytes != null && bytes.length > 0) {
            RawType state = new RawType(bytes, "image/png");
            updateState(CHANNEL_JOB_PREVIEW, state);
            lastPreviewFilename = thumbnailRef;
            lastPreviewState = state;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        PrusaLinkConfiguration cfg = config;
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

    private void handleCommandAsync(ChannelUID channelUID, Command command, PrusaLinkConfiguration cfg) {
        String baseUrl = "http://" + cfg.hostname + ":" + cfg.port;

        switch (channelUID.getId()) {
            case CHANNEL_PAUSE_RESUME:
                if (command instanceof OnOffType onOff) {
                    int jobId = lastJobId;
                    if (jobId < 0) {
                        logger.warn("Cannot pause/resume: no active PrusaLink job");
                        break;
                    }
                    String action = OnOffType.ON.equals(onOff) ? "pause" : "resume";
                    int status = httpPut(baseUrl + "/api/v1/job/" + jobId + "/" + action, cfg.apiKey, "");
                    if (!HttpStatus.isSuccess(status)) {
                        logger.warn("Failed to {} job {}: HTTP {}", action, jobId, status);
                    }
                }
                break;

            case CHANNEL_CANCEL:
                if (OnOffType.ON.equals(command)) {
                    int jobId = lastJobId;
                    if (jobId < 0) {
                        logger.warn("Cannot cancel: no active PrusaLink job");
                    } else {
                        int status = httpDelete(baseUrl + "/api/v1/job/" + jobId, cfg.apiKey);
                        if (!HttpStatus.isSuccess(status)) {
                            logger.warn("Failed to cancel job {}: HTTP {}", jobId, status);
                        }
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

    // PrusaLink supports the OctoPrint-compatible gcode endpoint
    private void sendGcode(String baseUrl, String apiKey, String gcode) {
        int status = httpPost(baseUrl + "/api/printer/command", apiKey, "{\"command\":\"" + gcode + "\"}");
        if (!HttpStatus.isSuccess(status)) {
            logger.warn("G-code command '{}' failed: HTTP {}", gcode, status);
        }
    }

    private String mapPrusaState(String prusaState) {
        return switch (prusaState.toUpperCase()) {
            case "PRINTING" -> STATE_PRINTING;
            case "PAUSED" -> STATE_PAUSED;
            case "FINISHED" -> STATE_FINISHED;
            case "ERROR" -> STATE_ERROR;
            case "ATTENTION" -> STATE_ERROR;
            case "BUSY" -> STATE_BUSY;
            default -> STATE_IDLE;
        };
    }
}
