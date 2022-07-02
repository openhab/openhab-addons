/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mercedesme.internal;

import static org.openhab.binding.mercedesme.internal.Constants.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.WWWAuthenticationProtocolHandler;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.MultiMap;
import org.eclipse.jetty.util.UrlEncoded;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.mercedesme.internal.utils.ChannelStateMap;
import org.openhab.binding.mercedesme.internal.utils.Mapper;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VehicleHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class VehicleHandler extends BaseThingHandler {
    private static final DateTimeFormatter DATE_INPUT_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final String EXT_IMG_RES = "ExtImageResources_";
    private static final String INITIALIZE_COMMAND = "Initialze";

    private final Logger logger = LoggerFactory.getLogger(VehicleHandler.class);
    private final Map<String, Long> timeHash = new HashMap<String, Long>();
    private final MercedesMeCommandOptionProvider mmcop;
    private final MercedesMeStateOptionProvider mmsop;
    private final StorageService storageService;
    private final HttpClient hc;
    private final String uid;

    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private Optional<AccountHandler> accountHandler = Optional.empty();
    private Optional<QuantityType<?>> rangeElectric = Optional.empty();
    private Optional<Storage<String>> imageStorage = Optional.empty();
    private Optional<VehicleConfiguration> config = Optional.empty();
    private Optional<QuantityType<?>> rangeFuel = Optional.empty();
    private LocalDateTime nextRefresh;
    private boolean online = false;

    public VehicleHandler(Thing thing, HttpClientFactory hcf, String uid, StorageService storageService,
            MercedesMeCommandOptionProvider mmcop, MercedesMeStateOptionProvider mmsop) {
        super(thing);
        hc = hcf.getCommonHttpClient();
        this.uid = uid;
        this.mmcop = mmcop;
        this.mmsop = mmsop;
        this.storageService = storageService;
        // https://github.com/jetty-project/jetty-reactive-httpclient/issues/33
        hc.getProtocolHandlers().remove(WWWAuthenticationProtocolHandler.NAME);
        nextRefresh = LocalDateTime.now();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.trace("Received {} {} {}", channelUID.getAsString(), command.toFullString(), channelUID.getId());
        if (command instanceof RefreshType) {
            if (LocalDateTime.now().isAfter(nextRefresh)) {
                nextRefresh = LocalDateTime.now().plus(Duration.ofSeconds(10));
                logger.trace("Refresh granted - next at {}", nextRefresh);
                getData();
            } else {
                logger.trace("Refresh rejected {}", nextRefresh);
            }
        } else if (channelUID.getIdWithoutGroup().equals("image-view")) {
            if (imageStorage.isPresent()) {
                if (command.toFullString().equals(INITIALIZE_COMMAND)) {
                    getImageResources();
                }
                String key = command.toFullString() + "_" + config.get().vin;
                String encodedImage = EMPTY;
                if (imageStorage.get().containsKey(key)) {
                    encodedImage = imageStorage.get().get(key);
                    logger.trace("Image {} found in storage", key);
                } else {
                    logger.trace("Request Image {} ", key);
                    encodedImage = getImage(command.toFullString());
                    if (!encodedImage.equals(EMPTY)) {
                        imageStorage.get().put(key, encodedImage);
                    }
                }
                if (encodedImage != null && !EMPTY.equals(encodedImage)) {
                    RawType image = new RawType(Base64.getDecoder().decode(encodedImage),
                            MIME_PREFIX + config.get().format);
                    updateState(new ChannelUID(thing.getUID(), GROUP_IMAGE, "image-data"), image);
                } else {
                    logger.debug("Image {} is empty", key);
                }
            }
        } else if (channelUID.getIdWithoutGroup().equals("clear-cache") && command.equals(OnOffType.ON)) {
            List<String> removals = new ArrayList<String>();
            imageStorage.get().getKeys().forEach(entry -> {
                if (entry.contains("_" + config.get().vin)) {
                    removals.add(entry);
                }
            });
            removals.forEach(entry -> {
                imageStorage.get().remove(entry);
            });
            updateState(new ChannelUID(thing.getUID(), GROUP_IMAGE, "clear-cache"), OnOffType.OFF);
            getImageResources();
        }
    }

    @Override
    public void initialize() {
        config = Optional.of(getConfigAs(VehicleConfiguration.class));
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, null);
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                accountHandler = Optional.of((AccountHandler) handler);
                startSchedule(config.get().refreshInterval);
                if (!config.get().vin.equals(NOT_SET)) {
                    imageStorage = Optional.of(storageService.getStorage(BINDING_ID + "_" + config.get().vin));
                    if (!imageStorage.get().containsKey(EXT_IMG_RES + config.get().vin)) {
                        getImageResources();
                    }
                    setImageOtions();
                }
                // check Image resources
                updateState(new ChannelUID(thing.getUID(), GROUP_IMAGE, "clear-cache"), OnOffType.OFF);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "BridgeHanlder missing");
                logger.warn("Bridge Handler null");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bridge not set");
            logger.warn("Bridge null");
        }
    }

    private void startSchedule(int interval) {
        refreshJob.ifPresentOrElse(job -> {
            if (job.isCancelled()) {
                refreshJob = Optional
                        .of(scheduler.scheduleWithFixedDelay(this::getData, 0, interval, TimeUnit.MINUTES));
            } // else - scheduler is already running!
        }, () -> {
            refreshJob = Optional.of(scheduler.scheduleWithFixedDelay(this::getData, 0, interval, TimeUnit.MINUTES));
        });
    }

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> job.cancel(true));
    }

    public void getData() {
        if (!accountHandler.isEmpty()) {
            String token = accountHandler.get().getToken();
            if (EMPTY.equals(token)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Check Bridge Authorization");
                return;
            } else if (!online) { // only update if thing isn't already ONLINE
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
            }

            // Mileage for all cars
            String odoUrl = String.format(ODO_URL, config.get().vin);
            if (accountConfigAvailable()) {
                if (accountHandler.get().config.get().odoScope) {
                    call(odoUrl);
                } else {
                    logger.debug("{} Odo scope not activated", this.getThing().getLabel());
                }
            } else {
                logger.debug("{} Account not properly configured", this.getThing().getLabel());
            }

            // Electric status for hybrid and electric
            if (uid.equals(BEV) || uid.equals(HYBRID)) {
                String evUrl = String.format(EV_URL, config.get().vin);
                if (accountConfigAvailable()) {
                    if (accountHandler.get().config.get().evScope) {
                        call(evUrl);
                    } else {
                        logger.debug("{} Electric Status scope not activated", this.getThing().getLabel());
                    }
                } else {
                    logger.debug("{} Account not properly configured", this.getThing().getLabel());
                }
            }

            // Fuel for hybrid and combustion
            if (uid.equals(COMBUSTION) || uid.equals(HYBRID)) {
                String fuelUrl = String.format(FUEL_URL, config.get().vin);
                if (accountConfigAvailable()) {
                    if (accountHandler.get().config.get().fuelScope) {
                        call(fuelUrl);
                    } else {
                        logger.debug("{} Fuel scope not activated", this.getThing().getLabel());
                    }
                } else {
                    logger.debug("{} Account not properly configured", this.getThing().getLabel());
                }
            }

            // Status and Lock for all
            String statusUrl = String.format(STATUS_URL, config.get().vin);
            if (accountConfigAvailable()) {
                if (accountHandler.get().config.get().vehicleScope) {
                    call(statusUrl);
                } else {
                    logger.debug("{} Vehicle Status scope not activated", this.getThing().getLabel());
                }
            } else {
                logger.debug("{} Account not properly configured", this.getThing().getLabel());
            }
            String lockUrl = String.format(LOCK_URL, config.get().vin);
            if (accountConfigAvailable()) {
                if (accountHandler.get().config.get().lockScope) {
                    call(lockUrl);
                } else {
                    logger.debug("{} Lock scope not activated", this.getThing().getLabel());
                }
            } else {
                logger.debug("{} Account not properly configured", this.getThing().getLabel());
            }

            // [todo] remove test for call to one specific resource
            // String lightUrl = BASE_URL + "/vehicles/%s/resources/interiorLightsFront";
            // call(String.format(lightUrl, config.get().vin));

            // Range radius for all types
            updateRadius();
        } else {
            logger.warn("AccountHandler not set");
        }
    }

    private boolean accountConfigAvailable() {
        if (accountHandler.isPresent()) {
            if (accountHandler.get().config.isPresent()) {
                return true;
            }
        }
        return false;
    }

    private void getImageResources() {
        if (accountHandler.get().getImageApiKey().equals(NOT_SET)) {
            logger.debug("Image API key not set");
            return;
        }
        // add config parameters
        MultiMap<String> parameterMap = new MultiMap<String>();
        parameterMap.add("background", Boolean.toString(config.get().background));
        parameterMap.add("night", Boolean.toString(config.get().night));
        parameterMap.add("cropped", Boolean.toString(config.get().cropped));
        parameterMap.add("roofOpen", Boolean.toString(config.get().roofOpen));
        parameterMap.add("fileFormat", config.get().format);
        String params = UrlEncoded.encode(parameterMap, StandardCharsets.UTF_8, false);
        String url = String.format(IMAGE_EXTERIOR_RESOURCE_URL, config.get().vin) + "?" + params;
        logger.debug("Get Image resources {} {} ", accountHandler.get().getImageApiKey(), url);
        Request req = hc.newRequest(url);
        req.header("x-api-key", accountHandler.get().getImageApiKey());
        req.header(HttpHeader.ACCEPT, "application/json");
        try {
            ContentResponse cr = req.send();
            if (cr.getStatus() == 200) {
                imageStorage.get().put(EXT_IMG_RES + config.get().vin, cr.getContentAsString());
                setImageOtions();
            } else {
                logger.debug("Failed to get image resources {} {}", cr.getStatus(), cr.getContentAsString());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Error getting image resources {}", e.getMessage());
        }
    }

    private void setImageOtions() {
        List<String> entries = new ArrayList<String>();
        if (imageStorage.get().containsKey(EXT_IMG_RES + config.get().vin)) {
            String resources = imageStorage.get().get(EXT_IMG_RES + config.get().vin);
            JSONObject jo = new JSONObject(resources);
            jo.keySet().forEach(entry -> {
                entries.add(entry);
            });
        }
        Collections.sort(entries);
        List<CommandOption> commandOptions = new ArrayList<CommandOption>();
        List<StateOption> stateOptions = new ArrayList<StateOption>();
        entries.forEach(entry -> {
            CommandOption co = new CommandOption(entry, null);
            commandOptions.add(co);
            StateOption so = new StateOption(entry, null);
            stateOptions.add(so);
        });
        if (commandOptions.isEmpty()) {
            commandOptions.add(new CommandOption("Initilaze", null));
            stateOptions.add(new StateOption("Initilaze", null));
        }
        mmcop.setCommandOptions(new ChannelUID(thing.getUID(), GROUP_IMAGE, "image-view"), commandOptions);
        mmsop.setStateOptions(new ChannelUID(thing.getUID(), GROUP_IMAGE, "image-view"), stateOptions);
    }

    private String getImage(String key) {
        if (accountHandler.get().getImageApiKey().equals(NOT_SET)) {
            logger.debug("Image API key not set");
            return EMPTY;
        }
        String imageId = EMPTY;
        if (imageStorage.get().containsKey(EXT_IMG_RES + config.get().vin)) {
            String resources = imageStorage.get().get(EXT_IMG_RES + config.get().vin);
            JSONObject jo = new JSONObject(resources);
            if (jo.has(key)) {
                imageId = jo.getString(key);
            }
        } else {
            getImageResources();
            return EMPTY;
        }

        String url = IMAGE_BASE_URL + "/images/" + imageId;
        Request req = hc.newRequest(url);
        req.header("x-api-key", accountHandler.get().getImageApiKey());
        req.header(HttpHeader.ACCEPT, "*/*");
        ContentResponse cr;
        try {
            cr = req.send();
            byte[] response = cr.getContent();
            return Base64.getEncoder().encodeToString(response);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Get Image {} error {}", url, e.getMessage());
        }
        return EMPTY;
    }

    private void call(String url) {
        String requestUrl = String.format(url, config.get().vin);
        // Calculate endpoint for debugging
        String[] endpoint = requestUrl.split("/");
        String finalEndpoint = endpoint[endpoint.length - 1];
        // debug prefix contains Thing label and call endpoint for propper debugging
        String debugPrefix = this.getThing().getLabel() + Constants.COLON + finalEndpoint;

        Request req = hc.newRequest(requestUrl);
        req.header(HttpHeader.AUTHORIZATION, "Bearer " + accountHandler.get().getToken());
        try {
            ContentResponse cr = req.send();
            logger.debug("{} Response {} {}", debugPrefix, cr.getStatus(), cr.getContentAsString());
            if (cr.getStatus() == 200) {
                distributeContent(cr.getContentAsString().trim());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("{} Error getting data {}", debugPrefix, e.getMessage());
            fallbackCall(requestUrl);
        }
    }

    /**
     * Fallback solution with Java11 classes
     * Performs try with Java11 HttpClient - https://zetcode.com/java/getpostrequest/ to identify Community problem
     * https://community.openhab.org/t/mercedes-me-binding/136852/21
     *
     * @param requestUrl
     */
    private void fallbackCall(String requestUrl) {
        // Calculate endpoint for debugging
        String[] endpoint = requestUrl.split("/");
        String finalEndpoint = endpoint[endpoint.length - 1];
        // debug prefix contains Thing label and call endpoint for propper debugging
        String debugPrefix = this.getThing().getLabel() + Constants.COLON + finalEndpoint;

        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(requestUrl))
                .header(HttpHeader.AUTHORIZATION.toString(), "Bearer " + accountHandler.get().getToken()).GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.debug("{} Fallback Response {} {}", debugPrefix, response.statusCode(), response.body());
            if (response.statusCode() == 200) {
                distributeContent(response.body().trim());
            }
        } catch (IOException | InterruptedException e) {
            logger.warn("{} Error getting data via fallback {}", debugPrefix, e.getMessage());
        }
    }

    private void distributeContent(String json) {
        if (json.startsWith("[") && json.endsWith("]")) {
            JSONArray ja = new JSONArray(json);
            for (Iterator<Object> iterator = ja.iterator(); iterator.hasNext();) {
                JSONObject jo = (JSONObject) iterator.next();
                ChannelStateMap csm = Mapper.getChannelStateMap(jo);
                if (csm.isValid()) {
                    updateChannel(csm);
                    // store ChannelMap for range radius calculation
                    if (csm.getChannel().equals("range-electric")) {
                        rangeElectric = Optional.of((QuantityType<?>) csm.getState());
                    } else if (csm.getChannel().equals("range-fuel")) {
                        rangeFuel = Optional.of((QuantityType<?>) csm.getState());
                    }
                } else {
                    logger.warn("Unable to deliver state for {}", jo);
                }
            }
        } else {
            logger.info("JSON Array expected but received {}", json);
        }
    }

    private void updateRadius() {
        if (rangeElectric.isPresent()) {
            // update electric radius
            ChannelStateMap radiusElectric = new ChannelStateMap("radius-electric", GROUP_RANGE,
                    guessRangeRadius(rangeElectric.get()), 0);
            updateChannel(radiusElectric);
            if (rangeFuel.isPresent()) {
                // update fuel & hybrid radius
                ChannelStateMap radiusFuel = new ChannelStateMap("radius-fuel", GROUP_RANGE,
                        guessRangeRadius(rangeFuel.get()), 0);
                updateChannel(radiusFuel);
                int hybridKm = rangeElectric.get().intValue() + rangeFuel.get().intValue();
                QuantityType<Length> hybridRangeState = QuantityType.valueOf(hybridKm, KILOMETRE_UNIT);
                ChannelStateMap rangeHybrid = new ChannelStateMap("range-hybrid", GROUP_RANGE, hybridRangeState, 0);
                updateChannel(rangeHybrid);
                ChannelStateMap radiusHybrid = new ChannelStateMap("radius-hybrid", GROUP_RANGE,
                        guessRangeRadius(hybridRangeState), 0);
                updateChannel(radiusHybrid);
            }
        } else if (rangeFuel.isPresent()) {
            // update fuel & hybrid radius
            ChannelStateMap radiusFuel = new ChannelStateMap("radius-fuel", GROUP_RANGE,
                    guessRangeRadius(rangeFuel.get()), 0);
            updateChannel(radiusFuel);
        }
    }

    /**
     * Easy function but there's some measures behind:
     * Guessing the range of the Vehicle on Map. If you can drive x kilometers with your Vehicle it's not feasible to
     * project this x km Radius on Map. The roads to be taken are causing some overhead because they are not a straight
     * line from Location A to B.
     * I've taken some measurements to calculate the overhead factor based on Google Maps
     * Berlin - Dresden: Road Distance: 193 air-line Distance 167 = Factor 87%
     * Kassel - Frankfurt: Road Distance: 199 air-line Distance 143 = Factor 72%
     * After measuring more distances you'll find out that the outcome is between 70% and 90%. So
     *
     * This depends also on the roads of a concrete route but this is only a guess without any Route Navigation behind
     *
     * @param range
     * @return mapping from air-line distance to "real road" distance
     */
    public static State guessRangeRadius(QuantityType<?> s) {
        double radius = s.intValue() * 0.8;
        return QuantityType.valueOf(Math.round(radius), KILOMETRE_UNIT);
    }

    protected void updateChannel(ChannelStateMap csm) {
        updateTime(csm.getGroup(), csm.getTimestamp());
        updateState(new ChannelUID(thing.getUID(), csm.getGroup(), csm.getChannel()), csm.getState());
    }

    private void updateTime(String group, long timestamp) {
        boolean updateTime = false;
        Long l = timeHash.get(group);
        if (l != null) {
            if (l.longValue() < timestamp) {
                updateTime = true;
            }
        } else {
            updateTime = true;
        }
        if (updateTime) {
            timeHash.put(group, timestamp);
            Date d = new Date(timestamp);
            LocalDateTime ld = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            DateTimeType dtt = DateTimeType.valueOf(ld.format(DATE_INPUT_PATTERN));
            updateState(new ChannelUID(thing.getUID(), group, "last-update"), dtt);
            logger.trace("{} last update {}", group, dtt);
        }
    }

    @Override
    public void updateStatus(ThingStatus ts, ThingStatusDetail tsd, @Nullable String details) {
        online = ts.equals(ThingStatus.ONLINE);
        super.updateStatus(ts, tsd, details);
    }
}
