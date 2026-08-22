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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperObjectsListResponse;
import org.openhab.binding.threedprinter.internal.dto.klipper.KlipperObjectsListResponse.KlipperObjectsListResult;
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
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

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

    /**
     * Matches Klipper's own extruder section naming: {@code extruder} for the first toolhead, {@code extruder1},
     * {@code extruder2}, ... for additional ones (Klipper itself allows up to 99; see
     * {@code kinematics/extruder.py add_printer_objects}).
     */
    private static final Pattern EXTRUDER_OBJECT_PATTERN = Pattern.compile("^extruder([1-9][0-9]*)?$");

    private @Nullable KlipperConfiguration config;
    private String lastPreviewFilename = "";
    private @Nullable RawType lastPreviewState;

    /** Whether {@link #discoverExtraExtruders} has completed at least once. */
    private boolean extrudersDiscovered = false;
    /** Klipper object names (e.g. {@code extruder1}) for toolheads beyond the primary {@code extruder}. */
    private List<String> extraExtruders = List.of();
    /** The object-query URL suffix, extended with any discovered extra extruders. */
    private String queryUrlSuffix = QUERY_URL_SUFFIX;
    /** Maps a dynamically added per-tool setpoint channel ID to the Klipper heater object name it controls. */
    private Map<String, String> extraSetpointHeaterByChannel = new LinkedHashMap<>();

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

        if (!extrudersDiscovered && !discoverExtraExtruders(baseUrl, cfg.apiKey)) {
            return;
        }

        HttpGetResult getResult = httpGet(baseUrl + queryUrlSuffix, cfg.apiKey);
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

        if (!extraExtruders.isEmpty()) {
            Map<String, KlipperHeater> extraHeaters = parseExtraExtruderHeaters(json, extraExtruders);
            for (String extruderId : extraExtruders) {
                KlipperHeater extraHeater = extraHeaters.get(extruderId);
                if (extraHeater != null) {
                    int toolNumber = extruderToolNumber(extruderId);
                    updateState(nozzleTemperatureChannelId(toolNumber),
                            new QuantityType<Temperature>(extraHeater.temperature, SIUnits.CELSIUS));
                    updateState(nozzleSetpointChannelId(toolNumber),
                            new QuantityType<Temperature>(extraHeater.target, SIUnits.CELSIUS));
                }
            }
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
                    } else {
                        updateState(CHANNEL_TIME_REMAINING, UnDefType.UNDEF);
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

    /**
     * Queries Moonraker once for the set of all available printer objects to discover which additional extruders
     * (beyond the primary {@code extruder}) this machine has, then adds the corresponding dynamic channels to the
     * Thing. Klipper names them {@code extruder1}, {@code extruder2}, ... with no fixed upper bound, so the
     * printer's own object list - not a hardcoded count - determines how many toolheads are added.
     *
     * @return false if the discovery request itself failed, in which case the Thing status has already been
     *         updated to reflect the failure and the caller should abort this refresh cycle and retry on the next
     *         one.
     */
    private boolean discoverExtraExtruders(String baseUrl, String apiKey) {
        HttpGetResult listResult = httpGet(baseUrl + "/printer/objects/list", apiKey);
        String json = listResult.body;
        if (json == null) {
            markHttpFailure(listResult.status);
            return false;
        }

        KlipperObjectsListResponse listResponse = fromJson(json, KlipperObjectsListResponse.class);
        KlipperObjectsListResult listBody = listResponse != null ? listResponse.result : null;
        List<String> objects = listBody != null ? listBody.objects : null;
        if (objects == null) {
            markOffline("@text/offline.comm-error-json");
            return false;
        }

        List<Integer> extraIndexes = new ArrayList<>();
        for (String object : objects) {
            Matcher matcher = EXTRUDER_OBJECT_PATTERN.matcher(object);
            if (matcher.matches() && matcher.group(1) != null) {
                extraIndexes.add(Integer.parseInt(matcher.group(1)));
            }
        }
        Collections.sort(extraIndexes);

        List<String> found = new ArrayList<>();
        StringBuilder suffix = new StringBuilder(QUERY_URL_SUFFIX);
        for (Integer index : extraIndexes) {
            String objectId = "extruder" + index;
            found.add(objectId);
            suffix.append('&').append(objectId);
        }

        extraExtruders = found;
        queryUrlSuffix = suffix.toString();
        if (!found.isEmpty()) {
            logger.debug("Discovered {} additional extruder(s): {}", found.size(), found);
            addExtraExtruderChannels(found);
        }
        extrudersDiscovered = true;
        return true;
    }

    /**
     * Adds a temperature/setpoint channel pair for each discovered extra extruder, reusing the same channel types
     * as the primary nozzle channels. Tool numbering follows Klipper's own convention: {@code extruder} is tool 1,
     * {@code extruder1} is tool 2, {@code extruder2} is tool 3, and so on.
     */
    private void addExtraExtruderChannels(List<String> extruderIds) {
        ThingBuilder builder = null;
        Map<String, String> heaterByChannel = new LinkedHashMap<>();
        for (String extruderId : extruderIds) {
            int toolNumber = extruderToolNumber(extruderId);
            String tempChannelId = nozzleTemperatureChannelId(toolNumber);
            String setpointChannelId = nozzleSetpointChannelId(toolNumber);

            if (thing.getChannel(tempChannelId) == null) {
                builder = builder != null ? builder : editThing();
                builder.withChannel(
                        ChannelBuilder.create(new ChannelUID(thing.getUID(), tempChannelId), "Number:Temperature")
                                .withType(new ChannelTypeUID(BINDING_ID, "nozzle-temperature"))
                                .withLabel("Nozzle " + toolNumber + " Temperature").build());
            }
            if (thing.getChannel(setpointChannelId) == null) {
                builder = builder != null ? builder : editThing();
                builder.withChannel(
                        ChannelBuilder.create(new ChannelUID(thing.getUID(), setpointChannelId), "Number:Temperature")
                                .withType(new ChannelTypeUID(BINDING_ID, "nozzle-temperature-setpoint"))
                                .withLabel("Nozzle " + toolNumber + " Setpoint").build());
            }
            heaterByChannel.put(setpointChannelId, extruderId);
        }
        extraSetpointHeaterByChannel = heaterByChannel;
        if (builder != null) {
            updateThing(builder.build());
        }
    }

    /**
     * Parses only the requested extruder objects out of the raw object-query response, since they are not part of
     * the fixed {@link KlipperObjectsResponse} shape (their names/count vary per machine).
     */
    private Map<String, KlipperHeater> parseExtraExtruderHeaters(String json, List<String> extruderIds) {
        Map<String, KlipperHeater> heaters = new LinkedHashMap<>();
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            JsonObject result = root.getAsJsonObject("result");
            JsonObject status = result != null ? result.getAsJsonObject("status") : null;
            if (status == null) {
                return heaters;
            }
            for (String extruderId : extruderIds) {
                JsonElement element = status.get(extruderId);
                if (element != null && element.isJsonObject()) {
                    KlipperHeater heater = gson.fromJson(element, KlipperHeater.class);
                    if (heater != null) {
                        heaters.put(extruderId, heater);
                    }
                }
            }
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.debug("Failed to parse additional extruder objects: {}", e.getMessage());
        }
        return heaters;
    }

    private int extruderToolNumber(String extruderId) {
        String suffix = extruderId.substring("extruder".length());
        int index = suffix.isEmpty() ? 0 : Integer.parseInt(suffix);
        return index + 1;
    }

    private String nozzleTemperatureChannelId(int toolNumber) {
        return CHANNEL_NOZZLE_TEMPERATURE + "-" + toolNumber;
    }

    private String nozzleSetpointChannelId(int toolNumber) {
        return CHANNEL_NOZZLE_TEMPERATURE_SETPOINT + "-" + toolNumber;
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
        String channelId = channelUID.getId();

        String extraHeaterName = extraSetpointHeaterByChannel.get(channelId);
        if (extraHeaterName != null) {
            Integer temp = toCelsius(command);
            if (temp != null) {
                sendGcode(baseUrl, cfg.apiKey, "SET_HEATER_TEMPERATURE HEATER=" + extraHeaterName + " TARGET=" + temp);
            } else {
                logger.warn("Unsupported command type {} for channel {}", command, channelUID);
            }
            return;
        }

        switch (channelId) {
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
                        markCommandFailure(status);
                    }
                    updateState(CHANNEL_CANCEL, OnOffType.OFF);
                }
                break;

            case CHANNEL_NOZZLE_TEMPERATURE_SETPOINT: {
                Integer temp = toCelsius(command);
                if (temp != null) {
                    sendGcode(baseUrl, cfg.apiKey, "SET_HEATER_TEMPERATURE HEATER=extruder TARGET=" + temp);
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
            markCommandFailure(status);
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
