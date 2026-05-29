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
import java.util.stream.Collectors;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.threedprinter.internal.config.PrusaLinkConfiguration;
import org.openhab.binding.threedprinter.internal.dto.prusa.PrusaStatusResponse;
import org.openhab.binding.threedprinter.internal.dto.prusa.PrusaStatusResponse.PrusaJobData;
import org.openhab.binding.threedprinter.internal.dto.prusa.PrusaStatusResponse.PrusaPrinterData;
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
            updateState(CHANNEL_PRINT_SPEED, new DecimalType(printer.speed));
            // PrusaLink reports fan_print as RPM; normalise to 0-100% (max ~8000 RPM)
            int fanPct = printer.fanPrint > 0 ? Math.min(100, printer.fanPrint / 80) : 0;
            updateState(CHANNEL_FAN_SPEED, new DecimalType(fanPct));
            updateState(CHANNEL_PAUSE_RESUME, OnOffType.from("PAUSED".equalsIgnoreCase(printer.state)));
        }

        PrusaJobData job = response.job;
        if (job != null) {
            updateState(CHANNEL_JOB_PROGRESS, new DecimalType(job.progress));
            updateState(CHANNEL_TIME_ELAPSED, new DecimalType(job.timePrinting));
            updateState(CHANNEL_TIME_REMAINING, new DecimalType(job.timeRemaining));
            PrusaJobData.PrusaFileData file = job.file;
            if (file != null) {
                String name = file.displayName.isBlank() ? file.name : file.displayName;
                updateState(CHANNEL_JOB_NAME, new StringType(name));

                if (!file.name.isBlank() && !file.name.equals(lastPreviewFilename)) {
                    lastPreviewFilename = file.name;
                    // Prefer the path from the API (e.g. "/usb/benchy.gcode"); fall back to "usb/{name}"
                    String thumbPath = file.path.isBlank() ? "usb/" + file.name
                            : file.path.startsWith("/") ? file.path.substring(1) : file.path;
                    String encodedPath = Arrays.stream(thumbPath.split("/"))
                            .map(s -> URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20"))
                            .collect(Collectors.joining("/"));
                    byte[] bytes = httpGetBytes(baseUrl + "/thumb/l/" + encodedPath, cfg.apiKey);
                    if (bytes != null && bytes.length > 0) {
                        updateState(CHANNEL_JOB_PREVIEW, new RawType(bytes, "image/png"));
                    }
                }
            }
        } else {
            updateState(CHANNEL_JOB_PROGRESS, new DecimalType(0));
            updateState(CHANNEL_JOB_NAME, new StringType(""));
            lastPreviewFilename = "";
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        PrusaLinkConfiguration cfg = config;
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
                    String action = OnOffType.ON.equals(onOff) ? "pause" : "resume";
                    httpPut(baseUrl + "/api/v1/job", cfg.apiKey, "{\"action\":\"" + action + "\"}");
                }
                break;

            case CHANNEL_CANCEL:
                if (OnOffType.ON.equals(command)) {
                    httpDelete(baseUrl + "/api/v1/job", cfg.apiKey);
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

    // PrusaLink supports the OctoPrint-compatible gcode endpoint
    private void sendGcode(String baseUrl, String apiKey, String gcode) {
        httpPost(baseUrl + "/api/printer/command", apiKey, "{\"command\":\"" + gcode + "\"}");
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
