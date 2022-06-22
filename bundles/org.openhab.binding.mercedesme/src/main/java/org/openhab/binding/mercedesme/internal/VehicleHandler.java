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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import org.openhab.core.types.UnDefType;
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
    private final Logger logger = LoggerFactory.getLogger(VehicleHandler.class);

    public static final String CONTENT_TYPE_URL_ENCODED = "application/x-www-form-urlencoded";
    private static final String EXT_IMG_RES = "ExtImageResources_";

    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private Optional<AccountHandler> accountHandler = Optional.empty();;
    private Optional<VehicleConfiguration> config = Optional.empty();
    private Optional<ChannelStateMap> rangeFuel = Optional.empty();
    private Optional<ChannelStateMap> rangeElectric = Optional.empty();
    private Optional<Storage<String>> imageStorage = Optional.empty();
    private final HttpClient hc;
    private final String uid;
    private final StorageService storageService;
    private final MercedesMeCommandOptionProvider mmcop;

    public VehicleHandler(Thing thing, HttpClientFactory hcf, String uid, StorageService storageService,
            MercedesMeCommandOptionProvider mmcop) {
        super(thing);
        hc = hcf.getCommonHttpClient();
        this.uid = uid;
        this.mmcop = mmcop;
        this.storageService = storageService;
        // https://github.com/jetty-project/jetty-reactive-httpclient/issues/33
        hc.getProtocolHandlers().remove(WWWAuthenticationProtocolHandler.NAME);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.info("Received {} {} {}", channelUID.getAsString(), command.toFullString(), channelUID.getId());
        if (command instanceof RefreshType) {

        } else {
            if (channelUID.getIdWithoutGroup().equals("image-view")) {
                if (imageStorage.isPresent()) {
                    if (command.equals("Initialze")) {
                        getImageResources();
                    }
                    String key = command.toFullString() + "_" + config.get().vin;
                    String encodedImage = EMPTY;
                    if (imageStorage.get().containsKey(key)) {
                        encodedImage = imageStorage.get().get(key);
                        logger.info("Image {} found in storage", key);
                    } else {
                        logger.info("Request Image {} ", key);
                        encodedImage = getImage(command.toFullString());
                        if (!encodedImage.equals(EMPTY)) {
                            imageStorage.get().put(key, encodedImage);
                        }
                    }
                    if (!encodedImage.equals(EMPTY)) {
                        logger.info("Update data channel");
                        RawType image = new RawType(Base64.getDecoder().decode(encodedImage), MIME_PNG);
                        updateState(new ChannelUID(thing.getUID(), GROUP_IMAGE, "image-data"), image);
                    } else {
                        logger.info("Empty image");
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
        }
    }

    @Override
    public void initialize() {
        config = Optional.of(getConfigAs(VehicleConfiguration.class));
        updateStatus(ThingStatus.UNKNOWN);
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                accountHandler = Optional.of((AccountHandler) handler);
                startSchedule(config.get().refreshInterval);
                updateStatus(ThingStatus.ONLINE);
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
            // Mileage for all cars
            String odoUrl = String.format(ODO_URL, config.get().vin);
            call(odoUrl);

            // Electric status for hybrid and electric
            if (uid.equals(BEV) || uid.equals(HYBRID)) {
                String evUrl = String.format(EV_URL, config.get().vin);
                call(evUrl);
            }

            // Fuel for hybrid and combustion
            if (uid.equals(COMBUSTION) || uid.equals(HYBRID)) {
                String evUrl = String.format(FUEL_URL, config.get().vin);
                call(evUrl);
            }

            // Status and Lock for all
            String statusUrl = String.format(STATUS_URL, config.get().vin);
            call(statusUrl);
            String lockUrl = String.format(LOCK_URL, config.get().vin);
            call(lockUrl);

            // Range radius for all types
            updateRadius();
        } else {
            logger.warn("AccountHandler not set");
        }

        // test image data
        // String image64 = imageStorage.get("image:test");
        // byte[] data = Base64.getDecoder().decode(image64);
        // if (image64 != null) {
        // updateState(new ChannelUID(thing.getUID(), GROUP_IMAGE, "image-data"), new RawType(data, "image/png"));
        // } else {
        // logger.warn("Image not found in storage");
        // }
    }

    private void getImageResources() {
        if (accountHandler.get().getImageApiKey().equals(NOT_SET)) {
            logger.info("Image API key not set");
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
        logger.info("Get Image resources {} {} ", accountHandler.get().getImageApiKey(), url);
        Request req = hc.newRequest(url);
        req.header("x-api-key", accountHandler.get().getImageApiKey());
        req.header(HttpHeader.ACCEPT, "application/json");
        // req.content(new StringContentProvider(CONTENT_TYPE_URL_ENCODED, params, StandardCharsets.UTF_8));
        ContentResponse cr;
        try {
            cr = req.send();
            if (cr.getStatus() == 200) {
                imageStorage.get().put(EXT_IMG_RES + config.get().vin, cr.getContentAsString());
                setImageOtions();
            } else {
                logger.info("Failed to get image resources {} {}", cr.getStatus(), cr.getContentAsString());
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Error getting data {}", e.getMessage());
        }
    }

    private String getImage(String key) {
        if (accountHandler.get().getImageApiKey().equals(NOT_SET)) {
            logger.info("Image API key not set");
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
            logger.info("No Image resource file found - send request");
            getImageResources();
            return EMPTY;
        }

        String url = IMAGE_BASE_URL + "/images/" + imageId;
        logger.info("Image URL {}", url);
        Request req = hc.newRequest(url);
        req.header("x-api-key", accountHandler.get().getImageApiKey());
        req.header(HttpHeader.ACCEPT, "*/*");
        ContentResponse cr;
        try {
            cr = req.send();
            byte[] response = cr.getContent();
            return Base64.getEncoder().encodeToString(response);
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Error getting data {}", e.getMessage());
        }
        return EMPTY;
    }

    private void setImageOtions() {
        List<CommandOption> options = new ArrayList<CommandOption>();
        if (imageStorage.get().containsKey(EXT_IMG_RES + config.get().vin)) {
            String resources = imageStorage.get().get(EXT_IMG_RES + config.get().vin);
            JSONObject jo = new JSONObject(resources);
            jo.keySet().forEach(entry -> {
                CommandOption co = new CommandOption(entry, null);
                options.add(co);
                // logger.info("Add command option {}", co.toString());
            });
        }
        if (options.size() == 0) {
            options.add(new CommandOption("Initilaze", null));
        }
        mmcop.setCommandOptions(new ChannelUID(thing.getUID(), GROUP_IMAGE, "image-view"), options);
    }

    private void call(String url) {
        Request req = hc.newRequest(String.format(url, config.get().vin));
        req.header(HttpHeader.AUTHORIZATION, "Bearer " + accountHandler.get().getToken());
        ContentResponse cr;
        try {
            cr = req.send();
            logger.info("{} Response {} {}", this.getThing().getLabel(), cr.getStatus(), cr.getContentAsString());
            if (cr.getStatus() == 200) {
                JSONArray ja = new JSONArray(cr.getContentAsString());
                ja.forEach(entry -> {
                    JSONObject jo = (JSONObject) entry;
                    ChannelStateMap csm = Mapper.getChannelStateMap(jo);
                    if (csm != null) {
                        updateChannel(csm);
                        if (csm.getChannel().equals("range-electric")) {
                            rangeElectric = Optional.of(csm);
                        } else if (csm.getChannel().equals("range-fuel")) {
                            rangeFuel = Optional.of(csm);
                        }
                    } else {
                        logger.warn("Unable to deliver state for {}", jo);
                    }
                });
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            logger.warn("Error getting data {}", e.getMessage());
        }
    }

    private void updateRadius() {
        if (rangeElectric.isPresent()) {
            // update electric radius
            ChannelStateMap radiusElectric = new ChannelStateMap("radius-electric", GROUP_RANGE,
                    guessRangeRadius(rangeElectric.get().getState().as(QuantityType.class)));
            updateChannel(radiusElectric);
            if (rangeFuel.isPresent()) {
                // update fuel & hybrid radius
                ChannelStateMap radiusFuel = new ChannelStateMap("radius-fuel", GROUP_RANGE,
                        guessRangeRadius(rangeFuel.get().getState().as(QuantityType.class)));
                updateChannel(radiusFuel);
                int hybridKm = rangeElectric.get().getState().as(QuantityType.class).intValue()
                        + rangeFuel.get().getState().as(QuantityType.class).intValue();
                ChannelStateMap rangeHybrid = new ChannelStateMap("range-hybrid", GROUP_RANGE,
                        QuantityType.valueOf(hybridKm, KILOMETRE_UNIT));
                updateChannel(rangeHybrid);
                ChannelStateMap radiusHybrid = new ChannelStateMap("radius-hybrid", GROUP_RANGE,
                        guessRangeRadius(rangeHybrid.getState().as(QuantityType.class)));
                updateChannel(radiusHybrid);
            }
        } else if (rangeFuel.isPresent()) {
            // update fuel & hybrid radius
            ChannelStateMap radiusFuel = new ChannelStateMap("radius-fuel", GROUP_RANGE,
                    guessRangeRadius((QuantityType) rangeFuel.get().getState()));
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
    public static State guessRangeRadius(@Nullable QuantityType s) {
        if (s == null) {
            return UnDefType.UNDEF;
        }
        double radius = s.intValue() * 0.8;
        return QuantityType.valueOf(radius, KILOMETRE_UNIT);
    }

    protected void updateChannel(ChannelStateMap csm) {
        updateState(new ChannelUID(thing.getUID(), csm.getGroup(), csm.getChannel()), csm.getState());
    }
}
