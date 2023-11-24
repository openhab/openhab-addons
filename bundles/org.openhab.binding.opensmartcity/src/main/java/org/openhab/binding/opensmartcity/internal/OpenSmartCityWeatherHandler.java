/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.opensmartcity.internal;

import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link OpenSmartCityWeatherHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class OpenSmartCityWeatherHandler extends BaseThingHandler {

    private static final String QUERY_LOCATIONS_ONLINE = "/v1.1/Locations?%24expand=Things&%24filter=substringof(%27online%27%2C%20Things%2Fproperties%2Fstatus)";

    private final Logger logger = LoggerFactory.getLogger(OpenSmartCityWeatherHandler.class);

    private @NonNullByDefault({}) OpenSmartCityWeatherConfiguration config;

    private @Nullable ScheduledFuture<?> refreshJob;

    private @NonNullByDefault({}) OpenSmartCityCityHandler bridgeHandler;

    private Gson gson = new Gson();

    private String myLoc = "";

    private @Nullable String nearestStationId;
    private String nearestStationIdStatus = "offline";

    private Map<String, Double> onlineLocations = new HashMap<>();
    private Map<String, String> locationNames = new HashMap<>();
    private Map<String, String> onlineThings = new HashMap<>();

    public OpenSmartCityWeatherHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateSensorValues();
        }
    }

    @Override
    public void initialize() {
        boolean validConfig = true;
        config = getConfigAs(OpenSmartCityWeatherConfiguration.class);
        String thingUid = getThing().getUID().toString();
        myLoc = config.location;
        if (myLoc.isEmpty()) {
            logger.debug("Geo Location is mandatory and has to be set in system settings, disabling thing {}",
                    thingUid);
            validConfig = false;
        }
        if (validConfig) {
            updateStatus(ThingStatus.ONLINE);
            bridgeHandler = (OpenSmartCityCityHandler) getBridge().getHandler();

            ScheduledFuture<?> localRefreshJob = refreshJob;
            if (localRefreshJob == null || localRefreshJob.isCancelled()) {
                logger.debug("Start refresh job at interval {} seconds.", bridgeHandler.config.refreshInterval);
                refreshJob = scheduler.scheduleWithFixedDelay(this::updateSensorValues, 0,
                        bridgeHandler.config.refreshInterval, TimeUnit.SECONDS);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
    }

    private void updateSensorValues() {

        if (nearestStationId != null) {
            String stationOnlineCheckUrl = "/v1.1/Things(" + nearestStationId + ")?$select=properties/status";
            logger.debug("Checking if station {} is online", nearestStationId);
            String stationOnlineCheck = apiRequest(stationOnlineCheckUrl);
            logger.debug("Station status: {}", stationOnlineCheck);
            if (stationOnlineCheck != null && !stationOnlineCheck.contains("online")) {
                nearestStationIdStatus = "offline";
            }
        }
        if (nearestStationId == null || !("online".equals(nearestStationIdStatus))) {
            logger.debug("Checking for nearest station");
            nearestStationId = getNearestOnlineLocation();
            nearestStationIdStatus = "online";
        }

        String queryNearestStation = "/v1.1/Datastreams?%24expand=Thing%2C%20Observations(%24top%3D1%3B%24orderby%3DphenomenonTime%20desc)&%24filter=Thing%2F%40iot.id%20eq%20"
                + nearestStationId
                + "%20and%20phenomenonTime%20ne%20null%20and%20(substringof(%27lufttemperatur%27%2Cname)%20or%20substringof(%27relative_luftfeuchte%27%2Cname))";

        String content = apiRequest(queryNearestStation);
        JsonObject jsonResponse = gson.fromJson(content, JsonObject.class);
        if (jsonResponse != null) {
            logger.debug("Response: {}", jsonResponse.toString());
            JsonElement observations = jsonResponse.get("value").getAsJsonArray().get(0);
            String rs = observations.toString();
            JsonObject jsonResult = gson.fromJson(rs, JsonObject.class);
            if (jsonResult != null) {
                JsonElement result = jsonResult.get("Observations").getAsJsonArray().get(0);
                JsonObject valueObj = gson.fromJson(result.toString(), JsonObject.class);
                if (valueObj != null) {
                    JsonElement value = valueObj.get("result");
                    logger.debug("Temperature of nearest station: {}", value);
                    Double temperature = value.getAsBigDecimal().doubleValue();
                    updateState(OpenSmartCityBindingConstants.CHANNEL_TEMPERATURE,
                            new QuantityType<Temperature>(temperature, SIUnits.CELSIUS));
                }
            }
            observations = jsonResponse.get("value").getAsJsonArray().get(1);
            rs = observations.toString();
            jsonResult = gson.fromJson(rs, JsonObject.class);
            if (jsonResult != null) {
                JsonElement result = jsonResult.get("Observations").getAsJsonArray().get(0);
                JsonObject valueObj = gson.fromJson(result.toString(), JsonObject.class);
                if (valueObj != null) {
                    JsonElement value = valueObj.get("result");
                    logger.debug("Humidity of nearest station: {}", value);
                    Double humidity = value.getAsBigDecimal().doubleValue();
                    updateState(OpenSmartCityBindingConstants.CHANNEL_HUMIDITY, new DecimalType(humidity));
                }
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Dispose handler '{}'.", getThing().getUID());
        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob != null && !localRefreshJob.isCancelled()) {
            logger.debug("Stop refresh job.");
            if (localRefreshJob.cancel(true)) {
                refreshJob = null;
            }
        }
    }

    public class JsonResponse {
    }

    private @Nullable String apiRequest(String queryPath) {
        try {
            String url = bridgeHandler.basePath + queryPath;
            logger.debug("Requesting {}", url);

            Request request = bridgeHandler.httpClient.newRequest(url);
            // TODO: remove hard coded credentials
            request.getHeaders().add("Authorization",
                    "Basic " + Base64.getEncoder().encodeToString(("smarthomeuser:Solingen2030!").getBytes()));
            ContentResponse response = request.send();

            if (response.getStatus() == 200) {
                String content = response.getContentAsString();
                logger.debug("Requesting URL {}", url);
                logger.debug("Retreived Content {}", content);
                updateStatus(ThingStatus.ONLINE);
                return content;
            } else {
                // TODO: check exact problem
                updateStatus(ThingStatus.OFFLINE);
                logger.debug("HTTP request failed with response code {}: {}", response.getStatus(),
                        response.getReason());
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
        return null;
    }

    private @Nullable String getNearestOnlineLocation() {
        String content = apiRequest(QUERY_LOCATIONS_ONLINE);
        if (content != null && !content.isEmpty()) {
            JsonObject jsonResponse = gson.fromJson(content, JsonObject.class);
            if (jsonResponse != null) {
                int iotCount = Integer.parseInt(jsonResponse.get("@iot.count").toString());
                for (int i = 0; i < iotCount - 1; i++) {
                    JsonElement locations = jsonResponse.get("value").getAsJsonArray().get(i);
                    String rs = locations.toString();
                    JsonObject jsonResult = gson.fromJson(rs, JsonObject.class);
                    if (jsonResult != null) {
                        String id = jsonResult.get("@iot.id").getAsString();
                        String locName = jsonResult.get("name").getAsString();
                        locationNames.put(id, locName);
                        JsonObject location = gson.fromJson(jsonResult.get("location").toString(), JsonObject.class);
                        if (location != null) {
                            JsonArray coordinates = location.get("coordinates").getAsJsonArray();
                            String position = coordinates.get(1).toString() + "," + coordinates.get(0).toString();
                            Double distance = getDistance(position);
                            onlineLocations.put(id, distance);
                        }
                        JsonElement thingsElement = jsonResult.get("Things").getAsJsonArray().get(0);
                        if (thingsElement != null) {
                            JsonObject thingsObject = gson.fromJson(thingsElement, JsonObject.class);
                            if (thingsObject != null) {
                                String thingId = thingsObject.get("@iot.id").getAsString();
                                onlineThings.put(id, thingId);
                            }
                        }
                    }
                }
                Map<String, Double> sortedMap = onlineLocations.entrySet().stream().sorted(Map.Entry.comparingByValue())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
                                LinkedHashMap::new));
                logger.debug("Nearest Station @iot.id:{} / {}", sortedMap.keySet().iterator().next(),
                        locationNames.get(sortedMap.keySet().iterator().next()));
                logger.debug("Thing @iot.id:{}", onlineThings.get(sortedMap.keySet().iterator().next()));
                logger.debug("Distance: {}", onlineLocations.get(sortedMap.keySet().iterator().next()));
                String thingIotId = onlineThings.get(sortedMap.keySet().iterator().next());
                if (thingIotId != null) {
                    return thingIotId;
                }
            }
        }
        return null;
    }

    private Double getDistance(String position) {
        PointType myPos = new PointType(myLoc);
        PointType stationPos = new PointType(position);
        DecimalType distance = stationPos.distanceFrom(myPos);
        return distance.doubleValue();
    }
}
