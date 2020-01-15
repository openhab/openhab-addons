/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.tibber.internal.handler;

import static org.openhab.binding.tibber.internal.TibberBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.tibber.internal.config.TibberConfiguration;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link TibberHandler} is responsible for handling queries to/from Tibber API.
 *
 * @author Stian Kjoglum - Initial contribution
 */

public class TibberHandler extends BaseThingHandler {
    private static final int REQUEST_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(20);
    private org.slf4j.Logger logger = LoggerFactory.getLogger(TibberHandler.class);
    private final Properties httpHeader = new Properties();
    private WebSocketClient client;
    private SslContextFactory sslContextFactory;
    private TibberWebSocketListener socket;
    private Session session;
    private ClientUpgradeRequest request;
    private boolean connected = false;
    private String rtEnabled = "false";

    private TibberConfiguration configuration;
    private TibberPriceConsumptionHandler priceInfo = new TibberPriceConsumptionHandler();
    private ScheduledFuture<?> pollingJob;

    public TibberHandler(Thing thing) {
        super(thing);

        httpHeader.put("cache-control", "no-cache");
        httpHeader.put("content-type", JSON_CONTENT_TYPE);
    }

    @Override
    public void initialize() {
        logger.info("Initializing Tibber API");
        updateStatus(ThingStatus.UNKNOWN);

        try {
            configuration = getConfigAs(TibberConfiguration.class);
            String token = new StringBuilder("Bearer ").append(configuration.getToken()).toString();
            httpHeader.put("Authorization", token);

            InputStream connectionStream = priceInfo.connectionInputStream(configuration.getHomeid());
            String response = HttpUtil.executeUrl("POST", BASE_URL, httpHeader, connectionStream, null,
                    REQUEST_TIMEOUT);

            if (!response.contains("error")) {
                logger.info("Initialized ID: {}", configuration.getHomeid());
                updateStatus(ThingStatus.ONLINE);

                getURLPriceInfo(BASE_URL);
                getURLDaily(BASE_URL);
                getURLHourly(BASE_URL);

                InputStream inputStream = priceInfo.getRealtimeInputStream(configuration.getHomeid());
                String jsonResponse = HttpUtil.executeUrl("POST", BASE_URL, httpHeader, inputStream, null,
                        REQUEST_TIMEOUT);

                JsonObject Object = (JsonObject) new JsonParser().parse(jsonResponse);
                rtEnabled = Object.getAsJsonObject("data").getAsJsonObject("viewer").getAsJsonObject("home")
                        .getAsJsonObject("features").get("realTimeConsumptionEnabled").toString();
                if (rtEnabled.equals("true")) {
                    logger.info("Pulse associated with HomeId: Live stream will be started");
                    try {
                        open();
                    } catch (Exception e) {
                        logger.error("Exception: {}", e);
                    }
                } else {
                    logger.info("No Pulse associated with HomeId: No live stream will be started");
                }
                startRefresh(configuration.getRefresh());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Incorrect token/homeid");
                return;
            }

        } catch (IOException | JsonSyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Tibber API is read-only and does not handle commands");
        return;
    }

    public void getURLPriceInfo(String url) throws IOException {
        InputStream inputStream = priceInfo.getInputStream(configuration.getHomeid());
        String jsonResponse = HttpUtil.executeUrl("POST", url, httpHeader, inputStream, null, REQUEST_TIMEOUT);
        logger.debug("API response (Price): {}", jsonResponse);

        if (!jsonResponse.contains("error") && jsonResponse.contains("total")) {
            try {
                JsonObject Object = (JsonObject) new JsonParser().parse(jsonResponse);
                JsonObject myObject = Object.getAsJsonObject("data").getAsJsonObject("viewer").getAsJsonObject("home")
                        .getAsJsonObject("currentSubscription").getAsJsonObject("priceInfo").getAsJsonObject("current");

                String Total = myObject.get("total").toString();
                BigDecimal currentTotal = new BigDecimal(Total);

                String startsAt = myObject.get("startsAt").toString();

                updateState(CURRENT_TOTAL, new DecimalType(currentTotal));
                updateState(CURRENT_STARTSAT, new StringType(startsAt));

                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }

                return;

            } catch (JsonSyntaxException e) {
                logger.error("JsonException: {}", e);
            }
        } else if (jsonResponse.contains("error")) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error communicating with Tibber API");
            return;

        } else {
            logger.debug("Unexpected response from Tibber: {}", jsonResponse);
            return;
        }
    }

    public void getURLDaily(String url) throws IOException {
        InputStream inputStream = priceInfo.getDailyInputStream(configuration.getHomeid());
        String jsonResponse = HttpUtil.executeUrl("POST", url, httpHeader, inputStream, null, REQUEST_TIMEOUT);
        logger.debug("API response (Daily): {}", jsonResponse);

        if (!jsonResponse.contains("error") && jsonResponse.contains("cost")) {
            try {
                JsonObject Object = (JsonObject) new JsonParser().parse(jsonResponse);
                JsonObject myObject = (JsonObject) Object.getAsJsonObject("data").getAsJsonObject("viewer")
                        .getAsJsonObject("home").getAsJsonObject("daily").getAsJsonArray("nodes").get(0);

                String dailyFrom = myObject.get("from").toString();
                String dailyTo = myObject.get("to").toString();
                updateState(DAILY_FROM, new StringType(dailyFrom));
                updateState(DAILY_TO, new StringType(dailyTo));

                String cost = myObject.get("cost").toString();
                updateChannel(DAILY_COST, cost);

                String consumption = myObject.get("consumption").toString();
                updateChannel(DAILY_CONSUMPTION, consumption);

                return;

            } catch (JsonSyntaxException e) {
                logger.error("JsonException: {}", e);
            }
        } else if (jsonResponse.contains("error")) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error communicating with Tibber API");
            return;

        } else {
            logger.debug("Unexpected response from Tibber: {}", jsonResponse);
            return;
        }
    }

    public void getURLHourly(String url) throws IOException {
        InputStream inputStream = priceInfo.getHourlyInputStream(configuration.getHomeid());
        String jsonResponse = HttpUtil.executeUrl("POST", url, httpHeader, inputStream, null, REQUEST_TIMEOUT);
        logger.debug("API response (Hourly): {}", jsonResponse);

        if (!jsonResponse.contains("error") && jsonResponse.contains("cost")) {
            try {
                JsonObject Object = (JsonObject) new JsonParser().parse(jsonResponse);
                JsonObject myObject = (JsonObject) Object.getAsJsonObject("data").getAsJsonObject("viewer")
                        .getAsJsonObject("home").getAsJsonObject("hourly").getAsJsonArray("nodes").get(0);

                String hourlyFrom = myObject.get("from").toString();
                String hourlyTo = myObject.get("to").toString();
                updateState(HOURLY_FROM, new StringType(hourlyFrom));
                updateState(HOURLY_TO, new StringType(hourlyTo));

                String cost = myObject.get("cost").toString();
                updateChannel(HOURLY_COST, cost);

                String consumption = myObject.get("consumption").toString();
                updateChannel(HOURLY_CONSUMPTION, consumption);

                return;

            } catch (JsonSyntaxException e) {
                logger.error("JsonException: {}", e);
            }
        } else if (jsonResponse.contains("error")) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error communicating with Tibber API");
            return;

        } else {
            logger.debug("Unexpected response from Tibber: {}", jsonResponse);
            return;
        }
    }

    public void startRefresh(int Refresh) {
        if (pollingJob == null) {
            pollingJob = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    updateRequest();
                } catch (IOException e) {
                    logger.error("IO Exception: {}", e);
                }
            }, 1, Refresh, TimeUnit.MINUTES);
        }
        return;
    }

    public void updateRequest() throws IOException {
        getURLPriceInfo(BASE_URL);
        getURLDaily(BASE_URL);
        getURLHourly(BASE_URL);

        if (rtEnabled.equals("true") && !isConnected()) {
            try {
                open();
            } catch (Exception e) {
                logger.error("Exception: {}", e);
            }
        }

        return;
    }

    public void updateChannel(String channelID, String channelValue) {
        if (!channelValue.contains("null")) {
            BigDecimal state = new BigDecimal(channelValue);
            updateState(channelID, new DecimalType(state));
            return;
        } else {
            return;
        }
    }

    public void thingStatusChanged(ThingStatusInfo thingStatusInfo) {
        logger.warn("Thing Status updated to {} for device: {}", thingStatusInfo.getStatus(), getThing().getUID());
        if (thingStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to communicate with Tibber API");
        }
    }

    @Override
    public void dispose() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
        }
        if (isConnected()) {
            close();
        }
        super.dispose();
    }

    public void open() throws Exception {
        if (isConnected()) {
            logger.warn("Open: connection is already open");
        }
        logger.info("Connecting to: {}", SUBSCRIPTION_URL);

        sslContextFactory = new SslContextFactory(true);
        sslContextFactory.setTrustAll(true);
        sslContextFactory.setEndpointIdentificationAlgorithm(null);

        client = new WebSocketClient(sslContextFactory);
        client.setMaxIdleTimeout(360 * 1000);

        socket = new TibberWebSocketListener();

        request = new ClientUpgradeRequest();
        String token = new StringBuilder("Bearer ").append(configuration.getToken()).toString();
        request.setHeader("Authorization", token);
        request.setSubProtocols("graphql-subscriptions");

        client.start();
        client.connect(socket, new URI(SUBSCRIPTION_URL), request);
    }

    public void close() {
        if (session != null) {
            String disconnect = "{\"type\":\"connection_terminate\",\"payload\":null}";
            try {
                socket.sendMessage(disconnect);
            } catch (IOException e) {
                logger.error("Closing websocket stream");
            }
            session.close();
            session = null;
        }
    }

    public boolean isConnected() {
        if (session == null || !session.isOpen()) {
            return false;
        }
        return connected;
    }

    @WebSocket
    public class TibberWebSocketListener {

        @OnWebSocketConnect
        public void onConnect(Session wssession) {
            session = wssession;
            connected = true;
            logger.info("Connected to Server");

            String connection = "{\"type\":\"connection_init\", \"payload\":\"token=" + configuration.getToken()
                    + "\"}";
            try {
                sendMessage(connection);
            } catch (IOException e) {
                logger.error("Send Message Exception: ", e);
            }
        }

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            logger.warn("Closing a WebSocket due to {}", reason);
            session = null;
            connected = false;
        }

        @OnWebSocketError
        public void onWebSocketError(Throwable e) {
            logger.error("Error during websocket communication: {}", e.getMessage(), e);
            onClose(0, e.getMessage());
        }

        @OnWebSocketMessage
        public void onMessage(String message) {
            if (message.contains("connection_ack")) {
                startSubscription();
            } else if (message.contains("error")) {
                close();
                logger.warn("WebSocket Connection closed due to Connection error: {}", message);
            } else if (!message.contains("error") && message.contains("liveMeasurement")) {
                JsonObject Object = (JsonObject) new JsonParser().parse(message);
                JsonObject myObject = Object.getAsJsonObject("payload").getAsJsonObject("data")
                        .getAsJsonObject("liveMeasurement");
                if (message.contains("timestamp")) {
                    String timestamp = myObject.get("timestamp").toString();
                    updateState(LIVE_TIMESTAMP, new StringType(timestamp));
                }
                if (message.contains("power")) {
                    String power = myObject.get("power").toString();
                    updateChannel(LIVE_POWER, power);
                }
                if (message.contains("lastMeterConsumption")) {
                    String lastMeterConsumption = myObject.get("lastMeterConsumption").toString();
                    updateChannel(LIVE_LASTMETERCONSUMPTION, lastMeterConsumption);
                }
                if (message.contains("accumulatedConsumption")) {
                    String accumulatedConsumption = myObject.get("accumulatedConsumption").toString();
                    updateChannel(LIVE_ACCUMULATEDCONSUMPTION, accumulatedConsumption);
                }
                if (message.contains("accumulatedCost")) {
                    String accumulatedCost = myObject.get("accumulatedCost").toString();
                    updateChannel(LIVE_ACCUMULATEDCOST, accumulatedCost);
                }
                if (message.contains("currency")) {
                    String currency = myObject.get("currency").toString();
                    updateState(LIVE_CURRENCY, new StringType(currency));
                }
                if (message.contains("minPower")) {
                    String minpower = myObject.get("minPower").toString();
                    updateChannel(LIVE_MINPOWER, minpower);
                }
                if (message.contains("averagePower")) {
                    String averagepower = myObject.get("averagePower").toString();
                    updateChannel(LIVE_AVERAGEPOWER, averagepower);
                }
                if (message.contains("maxPower")) {
                    String maxpower = myObject.get("maxPower").toString();
                    updateChannel(LIVE_MAXPOWER, maxpower);
                }
                if (message.contains("voltagePhase1")) {
                    String voltage1 = myObject.get("voltagePhase1").toString();
                    updateChannel(LIVE_VOLTAGE1, voltage1);
                }
                if (message.contains("voltagePhase2")) {
                    String voltage2 = myObject.get("voltagePhase2").toString();
                    updateChannel(LIVE_VOLTAGE2, voltage2);
                }
                if (message.contains("voltagePhase3")) {
                    String voltage3 = myObject.get("voltagePhase3").toString();
                    updateChannel(LIVE_VOLTAGE3, voltage3);
                }
                if (message.contains("currentPhase1")) {
                    String current1 = myObject.get("currentPhase1").toString();
                    updateChannel(LIVE_CURRENT1, current1);
                }
                if (message.contains("currentPhase2")) {
                    String current2 = myObject.get("currentPhase2").toString();
                    updateChannel(LIVE_CURRENT2, current2);
                }
                if (message.contains("currentPhase3")) {
                    String current3 = myObject.get("currentPhase3").toString();
                    updateChannel(LIVE_CURRENT3, current3);
                }
                if (message.contains("powerProduction")) {
                    String production = myObject.get("powerProduction").toString();
                    updateChannel(LIVE_POWERPRODUCTION, production);
                }
                if (message.contains("accumulatedProduction")) {
                    String accumulatedProduction = myObject.get("accumulatedProduction").toString();
                    updateChannel(LIVE_ACCUMULATEDPRODUCTION, accumulatedProduction);
                }
                if (message.contains("minPowerProduction")) {
                    String minpowerproduction = myObject.get("minPowerProduction").toString();
                    updateChannel(LIVE_MINPOWERPRODUCTION, minpowerproduction);
                }
                if (message.contains("maxPowerProduction")) {
                    String maxpowerproduction = myObject.get("maxPowerProduction").toString();
                    updateChannel(LIVE_MAXPOWERPRODUCTION, maxpowerproduction);
                }
            } else {
                logger.debug("Unknown live response from Tibber");
            }
        }

        private void sendMessage(String message) throws IOException {
            logger.debug("Send message: {}", message);
            session.getRemote().sendString(message);
        }

        public void onWebSocketBinary(byte[] payload, int offset, int len) {
            logger.debug("WebSocketBinary({}, {}, '{}')", offset, len, Arrays.toString(payload));
        }

        public void onWebSocketText(String message) {
            logger.debug("WebSocketText('{}')", message);

        }

        public void startSubscription() {

            String query = "{\"id\":\"1\",\"type\":\"start\",\"payload\":{\"variables\":{},\"extensions\":{},\"operationName\":null,\"query\":\"subscription {\\n liveMeasurement(homeId:\\\""
                    + configuration.getHomeid()
                    + "\\\") {\\n timestamp\\n power\\n lastMeterConsumption\\n accumulatedConsumption\\n accumulatedCost\\n currency\\n minPower\\n averagePower\\n maxPower\\n voltagePhase1\\n voltagePhase2\\n voltagePhase3\\n currentPhase1\\n currentPhase2\\n currentPhase3\\n powerProduction\\n accumulatedProduction\\n minPowerProduction\\n maxPowerProduction\\n }\\n }\\n\"}}";
            try {
                sendMessage(query);
            } catch (IOException e) {
                logger.error("Send Message Exception: ", e);
            }
        }
    }
}