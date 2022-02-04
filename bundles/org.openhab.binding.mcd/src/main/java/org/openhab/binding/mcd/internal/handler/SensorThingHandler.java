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
package org.openhab.binding.mcd.internal.handler;

import static org.openhab.binding.mcd.internal.McdBindingConstants.*;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.mcd.internal.util.Callback;
import org.openhab.binding.mcd.internal.util.HelperMethods;
import org.openhab.binding.mcd.internal.util.SensorEventDef;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.*;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Handler for the SensorThing of the MCD Binding.
 * 
 * @author Simon Dengler - Initial contribution
 */
public class SensorThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SensorThingHandler.class);
    private final HttpClient httpClient;
    private final Gson gson;
    private McdBridgeHandler mcdBridgeHandler;
    private JsonObject eventDef = null;
    private String serialNumber = "";
    private @Nullable SensorThingConfiguration config;

    public SensorThingHandler(Thing thing) {
        super(thing);
        SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
        httpClient = new HttpClient(sslContextFactory);
        httpClient.setFollowRedirects(false);
        gson = new Gson();
    }

    @Override
    public void initialize() {
        config = getConfigAs(SensorThingConfiguration.class);
        Bridge bridge = getBridge();
        if (bridge != null) {
            mcdBridgeHandler = (McdBridgeHandler) bridge.getHandler();
        } else {
            mcdBridgeHandler = null;
        }
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(this::init);
    }

    @Override
    @NonNullByDefault
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refreshChannelValue();
        } else if (command instanceof OnOffType && mcdBridgeHandler != null) {
            String channelId = channelUID.getId();
            // send sensor event for the given channel id
            switch (channelId) {
                case SIT_STATUS:
                    switch ((OnOffType) command) {
                        case ON:
                            sendSensorEvent(serialNumber, SensorEventDef.SIT_DOWN);
                            break;
                        case OFF:
                            sendSensorEvent(serialNumber, SensorEventDef.STAND_UP);
                            break;
                    }
                    break;
                case BED_STATUS:
                    switch ((OnOffType) command) {
                        case ON:
                            sendSensorEvent(serialNumber, SensorEventDef.BED_ENTRY);
                            break;
                        case OFF:
                            sendSensorEvent(serialNumber, SensorEventDef.BED_EXIT);
                            break;
                    }
                    break;
                case LIGHT:
                    switch ((OnOffType) command) {
                        case ON:
                            sendSensorEvent(serialNumber, SensorEventDef.ON);
                            break;
                        case OFF:
                            sendSensorEvent(serialNumber, SensorEventDef.OFF);
                            break;
                    }
                    break;
                case PRESENCE:
                    switch ((OnOffType) command) {
                        case ON:
                            sendSensorEvent(serialNumber, SensorEventDef.ROOM_ENTRY);
                            break;
                        case OFF:
                            sendSensorEvent(serialNumber, SensorEventDef.ROOM_EXIT);
                            break;
                    }
                    break;
                case OPEN_SHUT:
                    switch ((OnOffType) command) {
                        case ON:
                            sendSensorEvent(serialNumber, SensorEventDef.OPEN);
                            break;
                        case OFF:
                            sendSensorEvent(serialNumber, SensorEventDef.CLOSE);
                            break;
                    }
                    break;
                case FALL:
                    if (command == OnOffType.ON) {
                        sendSensorEvent(serialNumber, SensorEventDef.FALL);
                    }
                    break;
                case CHANGE_POSITION:
                    if (command == OnOffType.ON) {
                        sendSensorEvent(serialNumber, SensorEventDef.CHANGE_POSITION);
                    }
                    break;
                case BATTERY_STATE:
                    if (command == OnOffType.ON) {
                        sendSensorEvent(serialNumber, SensorEventDef.BATTERY_STATE);
                    }
                    break;
                case INACTIVITY:
                    if (command == OnOffType.ON) {
                        sendSensorEvent(serialNumber, SensorEventDef.INACTIVITY);
                    }
                    break;
                case ALARM:
                    if (command == OnOffType.ON) {
                        sendSensorEvent(serialNumber, SensorEventDef.ALARM);
                    }
                    break;
                case ACTIVITY:
                    if (command == OnOffType.ON) {
                        sendSensorEvent(serialNumber, SensorEventDef.ACTIVITY);
                    }
                    break;
                case GAS:
                    if (command == OnOffType.ON) {
                        sendSensorEvent(serialNumber, SensorEventDef.GAS);
                    }
                    break;
                case REMOVED_SENSOR:
                    if (command == OnOffType.ON) {
                        sendSensorEvent(serialNumber, SensorEventDef.REMOVE_SENSOR);
                    }
                    break;
                case INACTIVITY_ROOM:
                    if (command == OnOffType.ON) {
                        sendSensorEvent(serialNumber, SensorEventDef.INACTIVITY_ROOM);
                    }
                    break;
                case SMOKE_ALARM:
                    if (command == OnOffType.ON) {
                        sendSensorEvent(serialNumber, SensorEventDef.SMOKE_ALARM);
                    }
                    break;
                case HEAT:
                    if (command == OnOffType.ON) {
                        sendSensorEvent(serialNumber, SensorEventDef.HEAT);
                    }
                    break;
                case COLD:
                    if (command == OnOffType.ON) {
                        sendSensorEvent(serialNumber, SensorEventDef.COLD);
                    }
                    break;
                case ALARM_AIR:
                    if (command == OnOffType.ON) {
                        sendSensorEvent(serialNumber, SensorEventDef.ALARM_AIR);
                    }
                    break;
                case URINE:
                    if (command == OnOffType.ON) {
                        sendSensorEvent(serialNumber, SensorEventDef.URINE);
                    }
                    break;
                default:
                    logger.warn("no matching channel");
                    break;
            }
            refreshChannelValue();
        } else {
            logger.warn("handleCommand: received unexpected command");
        }
    }

    // this is called from initialize()
    private void init() {
        if (mcdBridgeHandler != null) {
            try {
                // wait until login is finished
                mcdBridgeHandler.waitForAccessToken();
                if (config != null) {
                    serialNumber = config.getSerialNumber();
                }
                // set status to offline
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "You are not authorized!");
                // determine, if thing is specified correctly and if it is online
                fetchDeviceInfo(res -> {
                    JsonObject result = res.getAsJsonObject();
                    if (result.has("SerialNumber")) {
                        // check for serial number in MCD cloud
                        if (result.get("SerialNumber").isJsonNull()) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                    "Serial number does not exist in MCD!");
                        } else {
                            try {
                                // get event definitions from REST API and build JsonObject with that list
                                fetchEventDef(jsonElement -> {
                                    JsonArray eventDefArray = jsonElement.getAsJsonArray();
                                    eventDef = new JsonObject();
                                    for (JsonElement elem : eventDefArray) {
                                        JsonObject obj = elem.getAsJsonObject();
                                        // fill object with pairs of key (name of sensor event) and value (id of event)
                                        eventDef.addProperty(obj.get("NameApiSensorEventDef").getAsString(),
                                                obj.get("IdApiSensorEventDef").getAsInt());
                                    }
                                });
                            } catch (Exception e) {
                                logger.warn("{}", e.getMessage());
                            }
                            // refresh channel values and set thing status to ONLINE
                            refreshChannelValue();
                            updateStatus(ThingStatus.ONLINE);
                        }
                    }
                });

            } catch (Exception e) {
                logger.warn("{}", e.getMessage());
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "unable to access bridge");
        }
    }

    /**
     * This method uses the things serial number in order to obtain the latest sensor event, that was registered in the
     * C&S MCD cloud, and then updates the channels with this latest value.
     */
    private void refreshChannelValue() {
        try {
            /*
             * First, the device info for the given serial number is requested from the cloud, which is then used fetch
             * the latest sensor event and update the channels.
             */
            fetchDeviceInfo(deviceInfo -> {
                // build request URI String
                String requestUrl = getUrlStringFromDeviceInfo((JsonObject) deviceInfo);
                try {
                    if (requestUrl != null) {
                        // get latest sensor event
                        fetchLatestValue(requestUrl, result -> {
                            JsonObject latestValue = getLatestValueFromJsonArray((JsonArray) result);
                            // update channels
                            updateChannels(latestValue);
                        });
                    } else {
                        logger.warn(
                                "Unable to synchronize! Please assign sensor to patient or organization unit in MCD!");
                    }
                } catch (Exception e) {
                    logger.warn("{}", e.getMessage());
                }
            });
        } catch (Exception e) {
            logger.warn("{}", e.getMessage());
        }
    }

    /**
     * Make asynchronous HTTP request to fetch the sensors last value as JsonObject.
     * 
     * @param urlString Contains the request URI as String
     * @param callback Implementation of interface Callback (org.openhab.binding.mcd.internal.util), that includes
     *            the proceeding of the obtained JsonObject.
     * @throws Exception Throws HTTP related Exceptions.
     */
    private void fetchLatestValue(String urlString, Callback callback) throws Exception {
        String accessToken = mcdBridgeHandler.getAccessToken();
        httpClient.start();
        Request request = httpClient.newRequest(urlString).method(HttpMethod.GET)
                .header(HttpHeader.HOST, "cunds-syncapi.azurewebsites.net")
                .header(HttpHeader.ACCEPT, "application/json")
                .header(HttpHeader.AUTHORIZATION, "Bearer " + accessToken);
        request.send(new BufferingResponseListener() {
            @NonNullByDefault({})
            @Override
            public void onComplete(Result result) {
                String contentString = getContentAsString();
                JsonArray content = gson.fromJson(contentString, JsonArray.class);
                callback.jsonElementTypeCallback(content);
            }
        });
    }

    /**
     * get device info as json via http request
     * 
     * @param callback instance of callback interface
     * @throws Exception throws http related exceptions
     */
    private void fetchDeviceInfo(Callback callback) throws Exception {
        String accessToken = mcdBridgeHandler.getAccessToken();
        httpClient.start();
        Request request = httpClient
                .newRequest("https://cunds-syncapi.azurewebsites.net/api/Device?serialNumber=" + serialNumber)
                .method(HttpMethod.GET).header(HttpHeader.HOST, "cunds-syncapi.azurewebsites.net")
                .header(HttpHeader.ACCEPT, "application/json")
                .header(HttpHeader.AUTHORIZATION, "Bearer " + accessToken);
        request.send(new BufferingResponseListener() {
            @NonNullByDefault({})
            @Override
            public void onComplete(Result result) {
                String contentString = getContentAsString();
                JsonObject content = gson.fromJson(contentString, JsonObject.class);
                callback.jsonElementTypeCallback(content);
            }
        });
    }

    /**
     * Sends a GET request to the C&S REST API to receive the list of sensor event definitions.
     * 
     * @param callback Implementation of interface Callback (org.openhab.binding.mcd.internal.util), that includes
     *            the proceeding of the obtained JsonObject.
     * @throws Exception Throws HTTP related Exceptions.
     */
    private void fetchEventDef(Callback callback) throws Exception {
        String accessToken = mcdBridgeHandler.getAccessToken();
        httpClient.start();
        Request request = httpClient.newRequest("https://cunds-syncapi.azurewebsites.net/api/ApiSensor/GetEventDef")
                .method(HttpMethod.GET).header(HttpHeader.HOST, "cunds-syncapi.azurewebsites.net")
                .header(HttpHeader.ACCEPT, "application/json")
                .header(HttpHeader.AUTHORIZATION, "Bearer " + accessToken);
        request.send(new BufferingResponseListener() {
            @NonNullByDefault({})
            @Override
            public void onComplete(Result result) {
                String contentString = getContentAsString();
                JsonArray content = gson.fromJson(contentString, JsonArray.class);
                callback.jsonElementTypeCallback(content);
            }
        });
    }

    /**
     * Builds the URI String for requesting the latest sensor event from the API. In order to do that, the parameter
     * deviceInfo is needed.
     * 
     * @param deviceInfo JsonObject that contains the device info as received from the C&S API
     * @return returns the URI as String or null, if no patient or organisation unit is assigned to the sensor in the
     *         MCD cloud
     */
    @Nullable
    String getUrlStringFromDeviceInfo(JsonObject deviceInfo) {
        if (deviceInfo.has("SerialNumber") && deviceInfo.get("SerialNumber").getAsString().equals(serialNumber)) {
            if (deviceInfo.has("PatientDevices") && deviceInfo.getAsJsonArray("PatientDevices").size() != 0) {
                JsonArray array = deviceInfo.getAsJsonArray("PatientDevices");
                JsonObject patient = array.get(0).getAsJsonObject();
                if (patient.has("UuidPerson") && !patient.get("UuidPerson").isJsonNull()) {
                    return "https://cunds-syncapi.azurewebsites.net/api/ApiSensor/GetLatestApiSensorEvents"
                            + "?UuidPatient=" + patient.get("UuidPerson").getAsString() + "&SerialNumber="
                            + serialNumber + "&Count=1";
                }
            } else if (deviceInfo.has("OrganisationUnitDevices")
                    && deviceInfo.getAsJsonArray("OrganisationUnitDevices").size() != 0) {
                JsonArray array = deviceInfo.getAsJsonArray("OrganisationUnitDevices");
                JsonObject orgUnit = array.get(0).getAsJsonObject();
                if (orgUnit.has("UuidOrganisationUnit") && !orgUnit.get("UuidOrganisationUnit").isJsonNull()) {
                    return "https://cunds-syncapi.azurewebsites.net/api/ApiSensor/GetLatestApiSensorEvents"
                            + "?UuidOrganisationUnit=" + orgUnit.get("UuidOrganisationUnit").getAsString()
                            + "&SerialNumber=" + serialNumber + "&Count=1";
                }
            }
        } else {
            init();
        }
        return null;
    }

    /**
     * Extracts the latest value from the JsonArray, that is obtained by the C&S SensorApi.
     * 
     * @param jsonArray the array that contains the latest value
     * @return the latest value as JsonObject or null.
     */
    JsonObject getLatestValueFromJsonArray(JsonArray jsonArray) {
        if (jsonArray.size() != 0) {
            JsonObject patientObject = jsonArray.get(0).getAsJsonObject();
            JsonArray devicesArray = patientObject.getAsJsonArray("Devices");
            if (devicesArray.size() != 0) {
                JsonObject deviceObject = devicesArray.get(0).getAsJsonObject();
                if (deviceObject.has("Events")) {
                    JsonArray eventsArray = deviceObject.getAsJsonArray("Events");
                    if (eventsArray.size() != 0) {
                        return eventsArray.get(0).getAsJsonObject();
                    }
                }
            }
        }
        return null;
    }

    /**
     * Updates the channels of the sensor thing with the latest value.
     * 
     * @param latestValue the latest value as JsonObject as obtained from the REST API
     */
    private void updateChannels(JsonObject latestValue) {
        String event = latestValue.get("EventDef").getAsString();
        String dateString = latestValue.get("DateEntry").getAsString();
        int id = eventDef.get(event).getAsInt();
        State state = HelperMethods.getSwitchStateByEventId(id);
        String channelID = HelperMethods.getChannelByEventId(id);
        // this will only happen for channels, that can send two different events
        if (state != null && channelID != null) {
            updateState(channelID, state);
        }
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateString);
            dateString = new SimpleDateFormat("yyyy-MM-dd', 'HH:mm:ss").format(date);
        } catch (Exception e) {
            logger.warn("{}", e.getMessage());
        }
        updateState(LAST_VALUE, new StringType(event + ", " + dateString));
    }

    /**
     * Sets the serial number to "001". This method should only be called for unit tests.
     */
    void setSerialNumber() {
        this.serialNumber = "001";
    }

    /**
     * Sends data to the cloud via POST request and switches the channel states from ON to OFF for a number of channels.
     * 
     * @param serialNumber serial number of the sensor in the MCD cloud
     * @param sensorEventDef specifies the type of sensor event, that will be sent
     */
    private void sendSensorEvent(@Nullable String serialNumber, SensorEventDef sensorEventDef) {
        try {
            httpClient.start();
            String accessToken = mcdBridgeHandler.getAccessToken();
            Date date = new Date();
            String dateString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(date);
            Request request = httpClient.newRequest("https://cunds-syncapi.azurewebsites.net/api/ApiSensor")
                    .method(HttpMethod.POST).header(HttpHeader.CONTENT_TYPE, "application/json")
                    .header(HttpHeader.HOST, "cunds-syncapi.azurewebsites.net")
                    .header(HttpHeader.ACCEPT, "application/json")
                    .header(HttpHeader.AUTHORIZATION, "Bearer " + accessToken);
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("SerialNumber", serialNumber);
            jsonObject.addProperty("IdApiSensorEventDef", sensorEventDef.ordinal());
            jsonObject.addProperty("DateEntry", dateString);
            jsonObject.addProperty("DateSend", dateString);
            request.content(new StringContentProvider(jsonObject.toString()), "application/json");

            request.send(new BufferingResponseListener() {
                @NonNullByDefault({})
                @Override
                public void onComplete(Result result) {
                    if (result.getResponse().getStatus() != 201) {
                        logger.warn("Unable to send sensor event!");
                    } else {
                        logger.debug("Sensor event was stored successfully.");
                        String channelID = HelperMethods.getChannelByEventId(sensorEventDef.ordinal());
                        if (channelID != null) {
                            // switch will be reset for channels, that can only send one event type
                            switch (sensorEventDef) {
                                case FALL:
                                case CHANGE_POSITION:
                                case BATTERY_STATE:
                                case INACTIVITY:
                                case ALARM:
                                case ACTIVITY:
                                case URINE:
                                case GAS:
                                case REMOVE_SENSOR:
                                case INACTIVITY_ROOM:
                                case SMOKE_ALARM:
                                case HEAT:
                                case COLD:
                                case ALARM_AIR:
                                    updateState(channelID, OnOffType.OFF);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            });
        } catch (Exception e) {
            logger.warn("{}", e.getMessage());
        }
    }
}
