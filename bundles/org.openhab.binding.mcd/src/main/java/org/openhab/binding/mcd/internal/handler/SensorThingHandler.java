/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.mcd.internal.util.Callback;
import org.openhab.binding.mcd.internal.util.SensorEventDef;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
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
import com.google.gson.JsonObject;

/**
 * Handler for the SensorThing of the MCD Binding.
 * 
 * @author Simon Dengler - Initial contribution
 */
@NonNullByDefault
public class SensorThingHandler extends BaseThingHandler {

    private static final int REQUEST_TIMEOUT_MS = 10_000;
    private final Logger logger = LoggerFactory.getLogger(SensorThingHandler.class);

    private final HttpClient httpClient;
    private final @Nullable Gson gson;
    private @Nullable McdBridgeHandler mcdBridgeHandler;
    private @Nullable String serialNumber = "";
    private @Nullable SensorThingConfiguration config;
    private int maxSensorEventId = 0;
    private boolean initIsDone = false;

    public SensorThingHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
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
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            refreshChannelValue();
        } else if (mcdBridgeHandler != null) {
            String channelId = channelUID.getId();
            // check for the right channel id
            if (channelId.equals(SEND_EVENT)) {
                String commandString = command.toString();
                int sensorEventId = SensorEventDef.getSensorEventId(commandString);
                if (sensorEventId < 1 || sensorEventId > maxSensorEventId) {
                    // check, if an id is passed as number
                    try {
                        sensorEventId = Integer.parseInt(commandString);
                        if (sensorEventId < 1 || sensorEventId > maxSensorEventId) {
                            logger.warn("Invalid Command!");
                        } else {
                            sendSensorEvent(serialNumber, sensorEventId);
                        }
                    } catch (Exception e) {
                        logger.warn("Invalid Command!");
                    }
                } else {
                    // command was valid (and id is between 1 and max)
                    sendSensorEvent(serialNumber, sensorEventId);
                }
            } else {
                logger.warn("Received command for unexpected channel!");
            }
            refreshChannelValue();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge is offline.");
        }
    }

    // this is called from initialize()
    private void init() {
        SensorThingConfiguration localConfig = config;
        if (localConfig != null) {
            serialNumber = localConfig.getSerialNumber();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Cannot access config data.");
        }
        McdBridgeHandler localMcdBridgeHandler = mcdBridgeHandler;
        if (localMcdBridgeHandler != null) {
            updateStatus(ThingStatus.ONLINE);
            if (!initIsDone) {
                // build and register listener
                localMcdBridgeHandler.register(() -> {
                    try {
                        // determine, if thing is specified correctly and if it is online
                        fetchDeviceInfo(res -> {
                            if (res != null) {
                                JsonObject result = res.getAsJsonObject();
                                if (result.has("SerialNumber")) {
                                    // check for serial number in MCD cloud
                                    if (result.get("SerialNumber").isJsonNull()) {
                                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                                "Serial number does not exist in MCD!");
                                    } else {
                                        // refresh channel values and set thing status to ONLINE
                                        refreshChannelValue();
                                        updateStatus(ThingStatus.ONLINE);
                                    }
                                }
                            }
                        });
                        fetchEventDef(jsonElement -> {
                            if (jsonElement != null) {
                                JsonArray eventDefArray = jsonElement.getAsJsonArray();
                                maxSensorEventId = eventDefArray.size();
                            }
                        });
                    } catch (Exception e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    }
                });
                initIsDone = true;
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "unable to access bridge");
        }
    }

    /**
     * This method uses the things serial number in order to obtain the latest
     * sensor event, that was registered in the
     * C&S MCD cloud, and then updates the channels with this latest value.
     */
    private void refreshChannelValue() {
        try {
            /*
             * First, the device info for the given serial number is requested from the
             * cloud, which is then used fetch
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
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
            });
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * Updates the channels of the sensor thing with the latest value.
     * 
     * @param latestValue the latest value as JsonObject as obtained from the REST
     *            API
     */
    private void updateChannels(@Nullable JsonObject latestValue) {
        if (latestValue != null) {
            String event = latestValue.get("EventDef").getAsString();
            String dateString = latestValue.get("DateEntry").getAsString();
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateString);
                dateString = new SimpleDateFormat("dd.MM.yyyy', 'HH:mm:ss").format(date);
            } catch (Exception e) {
                logger.debug("{}", e.getMessage());
            }
            updateState(LAST_VALUE, new StringType(event + ", " + dateString));
        }
    }

    /**
     * Make asynchronous HTTP request to fetch the sensors last value as JsonObject.
     * 
     * @param urlString Contains the request URI as String
     * @param callback Implementation of interface Callback
     *            (org.openhab.binding.mcd.internal.util), that includes
     *            the proceeding of the obtained JsonObject.
     * @throws Exception Throws HTTP related Exceptions.
     */
    private void fetchLatestValue(String urlString, Callback callback) throws Exception {
        McdBridgeHandler localMcdBridgeHandler = mcdBridgeHandler;
        if (localMcdBridgeHandler != null) {
            String accessToken = localMcdBridgeHandler.getAccessToken();
            Request request = httpClient.newRequest(urlString).method(HttpMethod.GET)
                    .header(HttpHeader.HOST, "cunds-syncapi.azurewebsites.net")
                    .header(HttpHeader.ACCEPT, "application/json")
                    .header(HttpHeader.AUTHORIZATION, "Bearer " + accessToken)
                    .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS);

            request.send(new BufferingResponseListener() {
                @NonNullByDefault({})
                @Override
                public void onComplete(Result result) {
                    String contentString = getContentAsString();
                    Gson localGson = gson;
                    if (localGson != null) {
                        JsonArray content = localGson.fromJson(contentString, JsonArray.class);
                        callback.jsonElementTypeCallback(content);
                    }
                }
            });
        }
    }

    /**
     * get device info as json via http request
     * 
     * @param callback instance of callback interface
     * @throws Exception throws http related exceptions
     */
    private void fetchDeviceInfo(Callback callback) throws Exception {
        McdBridgeHandler localMcdBridgeHandler = mcdBridgeHandler;
        if (localMcdBridgeHandler != null) {
            String accessToken = localMcdBridgeHandler.getAccessToken();
            Request request = httpClient
                    .newRequest("https://cunds-syncapi.azurewebsites.net/api/Device?serialNumber=" + serialNumber)
                    .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).method(HttpMethod.GET)
                    .header(HttpHeader.HOST, "cunds-syncapi.azurewebsites.net")
                    .header(HttpHeader.ACCEPT, "application/json")
                    .header(HttpHeader.AUTHORIZATION, "Bearer " + accessToken);
            request.send(new BufferingResponseListener() {
                @NonNullByDefault({})
                @Override
                public void onComplete(Result result) {
                    String contentString = getContentAsString();
                    Gson localGson = gson;
                    if (localGson != null) {
                        JsonObject content = localGson.fromJson(contentString, JsonObject.class);
                        callback.jsonElementTypeCallback(content);
                    }
                }
            });
        }
    }

    /**
     * Sends a GET request to the C&S REST API to receive the list of sensor event
     * definitions.
     * 
     * @param callback Implementation of interface Callback
     *            (org.openhab.binding.mcd.internal.util), that includes
     *            the proceeding of the obtained JsonObject.
     * @throws Exception Throws HTTP related Exceptions.
     */
    private void fetchEventDef(Callback callback) throws Exception {
        McdBridgeHandler localMcdBridgeHandler = mcdBridgeHandler;
        if (localMcdBridgeHandler != null) {
            String accessToken = localMcdBridgeHandler.getAccessToken();
            Request request = httpClient.newRequest("https://cunds-syncapi.azurewebsites.net/api/ApiSensor/GetEventDef")
                    .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).method(HttpMethod.GET)
                    .header(HttpHeader.HOST, "cunds-syncapi.azurewebsites.net")
                    .header(HttpHeader.ACCEPT, "application/json")
                    .header(HttpHeader.AUTHORIZATION, "Bearer " + accessToken);
            request.send(new BufferingResponseListener() {
                @NonNullByDefault({})
                @Override
                public void onComplete(Result result) {
                    String contentString = getContentAsString();
                    Gson localGson = gson;
                    if (localGson != null) {
                        JsonArray content = localGson.fromJson(contentString, JsonArray.class);
                        callback.jsonElementTypeCallback(content);
                    }
                }
            });
        }
    }

    /**
     * Builds the URI String for requesting the latest sensor event from the API. In
     * order to do that, the parameter
     * deviceInfo is needed.
     * 
     * @param deviceInfo JsonObject that contains the device info as received from
     *            the C&S API
     * @return returns the URI as String or null, if no patient or organisation unit
     *         is assigned to the sensor in the
     *         MCD cloud
     */
    @Nullable
    String getUrlStringFromDeviceInfo(@Nullable JsonObject deviceInfo) {
        if (deviceInfo != null) {
            if (deviceInfo.has("SerialNumber") && deviceInfo.get("SerialNumber").getAsString().equals(serialNumber)) {
                if (deviceInfo.has("PatientDevices") && deviceInfo.getAsJsonArray("PatientDevices").size() != 0) {
                    JsonArray array = deviceInfo.getAsJsonArray("PatientDevices");
                    JsonObject patient = array.get(0).getAsJsonObject();
                    if (patient.has("UuidPerson") && !patient.get("UuidPerson").isJsonNull()) {
                        return """
                                https://cunds-syncapi.azurewebsites.net/api/ApiSensor/GetLatestApiSensorEvents\
                                ?UuidPatient=\
                                """ + patient.get("UuidPerson").getAsString() + "&SerialNumber=" + serialNumber
                                + "&Count=1";
                    }
                } else if (deviceInfo.has("OrganisationUnitDevices")
                        && deviceInfo.getAsJsonArray("OrganisationUnitDevices").size() != 0) {
                    JsonArray array = deviceInfo.getAsJsonArray("OrganisationUnitDevices");
                    JsonObject orgUnit = array.get(0).getAsJsonObject();
                    if (orgUnit.has("UuidOrganisationUnit") && !orgUnit.get("UuidOrganisationUnit").isJsonNull()) {
                        return """
                                https://cunds-syncapi.azurewebsites.net/api/ApiSensor/GetLatestApiSensorEvents\
                                ?UuidOrganisationUnit=\
                                """ + orgUnit.get("UuidOrganisationUnit").getAsString() + "&SerialNumber="
                                + serialNumber + "&Count=1";
                    }
                }
            } else {
                init();
            }
        }
        return null;
    }

    /**
     * Extracts the latest value from the JsonArray, that is obtained by the C&S
     * SensorApi.
     * 
     * @param jsonArray the array that contains the latest value
     * @return the latest value as JsonObject or null.
     */
    @Nullable
    static JsonObject getLatestValueFromJsonArray(@Nullable JsonArray jsonArray) {
        if (jsonArray != null) {
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
        }
        return null;
    }

    /**
     * Sends data to the cloud via POST request and switches the channel states from
     * ON to OFF for a number of channels.
     * 
     * @param serialNumber serial number of the sensor in the MCD cloud
     * @param sensorEventDef specifies the type of sensor event, that will be sent
     */
    private void sendSensorEvent(@Nullable String serialNumber, int sensorEventDef) {
        try {
            McdBridgeHandler localMcdBridgeHandler = mcdBridgeHandler;
            if (localMcdBridgeHandler != null) {
                String accessToken = localMcdBridgeHandler.getAccessToken();
                Date date = new Date();
                String dateString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(date);
                Request request = httpClient.newRequest("https://cunds-syncapi.azurewebsites.net/api/ApiSensor")
                        .timeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS).method(HttpMethod.POST)
                        .header(HttpHeader.CONTENT_TYPE, "application/json")
                        .header(HttpHeader.ACCEPT, "application/json")
                        .header(HttpHeader.AUTHORIZATION, "Bearer " + accessToken);
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("SerialNumber", serialNumber);
                jsonObject.addProperty("IdApiSensorEventDef", sensorEventDef);
                jsonObject.addProperty("DateEntry", dateString);
                jsonObject.addProperty("DateSend", dateString);
                request.content(
                        new StringContentProvider("application/json", jsonObject.toString(), StandardCharsets.UTF_8));
                request.send(new BufferingResponseListener() {
                    @NonNullByDefault({})
                    @Override
                    public void onComplete(Result result) {
                        if (result.getResponse().getStatus() != 201) {
                            logger.debug("Unable to send sensor event:\n{}", result.getResponse().toString());
                        } else {
                            logger.debug("Sensor event was stored successfully.");
                            refreshChannelValue();
                        }
                    }
                });
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }
}
