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
package org.openhab.binding.tibber.internal.handler;

import static org.openhab.binding.tibber.internal.TibberBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.tibber.internal.config.TibberConfiguration;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link TibberHandler} is responsible for handling queries to/from Tibber API.
 *
 * @author Stian Kjoglum - Initial contribution
 */
@NonNullByDefault
public class TibberHandler extends BaseThingHandler {
    private static final int REQUEST_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(20);
    private final Logger logger = LoggerFactory.getLogger(TibberHandler.class);
    private final Properties httpHeader = new Properties();
    private TibberConfiguration tibberConfig = new TibberConfiguration();
    private @Nullable SslContextFactory sslContextFactory;
    private @Nullable TibberWebSocketListener socket;
    private @Nullable Session session;
    private @Nullable WebSocketClient client;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable Future<?> sessionFuture;
    private String rtEnabled = "false";

    public TibberHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        tibberConfig = getConfigAs(TibberConfiguration.class);

        getTibberParameters();
        startRefresh(tibberConfig.getRefresh());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            startRefresh(tibberConfig.getRefresh());
        } else {
            logger.debug("Tibber API is read-only and does not handle commands");
        }
    }

    public void getTibberParameters() {
        String response = "";
        try {
            httpHeader.put("cache-control", "no-cache");
            httpHeader.put("content-type", JSON_CONTENT_TYPE);
            httpHeader.put("Authorization", "Bearer " + tibberConfig.getToken());

            TibberPriceConsumptionHandler tibberQuery = new TibberPriceConsumptionHandler();
            InputStream connectionStream = tibberQuery.connectionInputStream(tibberConfig.getHomeid());
            response = HttpUtil.executeUrl("POST", BASE_URL, httpHeader, connectionStream, null, REQUEST_TIMEOUT);

            if (!response.contains("error") && !response.contains("<html>")) {
                updateStatus(ThingStatus.ONLINE);
                getURLInput(BASE_URL);

                InputStream inputStream = tibberQuery.getRealtimeInputStream(tibberConfig.getHomeid());
                String jsonResponse = HttpUtil.executeUrl("POST", BASE_URL, httpHeader, inputStream, null,
                        REQUEST_TIMEOUT);

                JsonObject object = (JsonObject) JsonParser.parseString(jsonResponse);
                rtEnabled = object.getAsJsonObject("data").getAsJsonObject("viewer").getAsJsonObject("home")
                        .getAsJsonObject("features").get("realTimeConsumptionEnabled").toString();

                if ("true".equals(rtEnabled)) {
                    logger.debug("Pulse associated with HomeId: Live stream will be started");
                    open();
                } else {
                    logger.debug("No Pulse associated with HomeId: No live stream will be started");
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Problems connecting/communicating with server: " + response);
            }
        } catch (IOException | JsonSyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    public void getURLInput(String url) throws IOException {
        String jsonResponse = "";
        TibberPriceConsumptionHandler tibberQuery = new TibberPriceConsumptionHandler();

        InputStream inputStream = tibberQuery.getInputStream(tibberConfig.getHomeid());
        jsonResponse = HttpUtil.executeUrl("POST", url, httpHeader, inputStream, null, REQUEST_TIMEOUT);
        logger.debug("API response: {}", jsonResponse);

        if (!jsonResponse.contains("error") && !jsonResponse.contains("<html>")) {
            if (getThing().getStatus() == ThingStatus.OFFLINE || getThing().getStatus() == ThingStatus.INITIALIZING) {
                updateStatus(ThingStatus.ONLINE);
            }

            JsonObject object = (JsonObject) JsonParser.parseString(jsonResponse);

            if (jsonResponse.contains("total")) {
                try {
                    JsonObject myObject = object.getAsJsonObject("data").getAsJsonObject("viewer")
                            .getAsJsonObject("home").getAsJsonObject("currentSubscription").getAsJsonObject("priceInfo")
                            .getAsJsonObject("current");

                    updateState(CURRENT_TOTAL, new DecimalType(myObject.get("total").toString()));
                    String timestamp = myObject.get("startsAt").toString().substring(1, 20);
                    updateState(CURRENT_STARTSAT, new DateTimeType(timestamp));
                    updateState(CURRENT_LEVEL,
                            new StringType(myObject.get("level").toString().replaceAll("^\"|\"$", "")));

                } catch (JsonSyntaxException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Error communicating with Tibber API: " + e.getMessage());
                }
            }
            if (jsonResponse.contains("daily") && !jsonResponse.contains("\"daily\":{\"nodes\":[]")
                    && !jsonResponse.contains("\"daily\":null")) {
                try {
                    JsonObject myObject = (JsonObject) object.getAsJsonObject("data").getAsJsonObject("viewer")
                            .getAsJsonObject("home").getAsJsonObject("daily").getAsJsonArray("nodes").get(0);

                    String timestampDailyFrom = myObject.get("from").toString().substring(1, 20);
                    updateState(DAILY_FROM, new DateTimeType(timestampDailyFrom));

                    String timestampDailyTo = myObject.get("to").toString().substring(1, 20);
                    updateState(DAILY_TO, new DateTimeType(timestampDailyTo));

                    updateChannel(DAILY_COST, myObject.get("cost").toString());
                    updateChannel(DAILY_CONSUMPTION, myObject.get("consumption").toString());

                } catch (JsonSyntaxException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Error communicating with Tibber API: " + e.getMessage());
                }
            }
            if (jsonResponse.contains("hourly") && !jsonResponse.contains("\"hourly\":{\"nodes\":[]")
                    && !jsonResponse.contains("\"hourly\":null")) {
                try {
                    JsonObject myObject = (JsonObject) object.getAsJsonObject("data").getAsJsonObject("viewer")
                            .getAsJsonObject("home").getAsJsonObject("hourly").getAsJsonArray("nodes").get(0);

                    String timestampHourlyFrom = myObject.get("from").toString().substring(1, 20);
                    updateState(HOURLY_FROM, new DateTimeType(timestampHourlyFrom));

                    String timestampHourlyTo = myObject.get("to").toString().substring(1, 20);
                    updateState(HOURLY_TO, new DateTimeType(timestampHourlyTo));

                    updateChannel(HOURLY_COST, myObject.get("cost").toString());
                    updateChannel(HOURLY_CONSUMPTION, myObject.get("consumption").toString());

                } catch (JsonSyntaxException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Error communicating with Tibber API: " + e.getMessage());
                }
            }
        } else if (jsonResponse.contains("error")) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Error in response from Tibber API: " + jsonResponse);
            try {
                Thread.sleep(300 * 1000);
                return;
            } catch (InterruptedException e) {
                logger.debug("Tibber OFFLINE, attempting thread sleep: {}", e.getMessage());
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unexpected response from Tibber: " + jsonResponse);
            try {
                Thread.sleep(300 * 1000);
                return;
            } catch (InterruptedException e) {
                logger.debug("Tibber OFFLINE, attempting thread sleep: {}", e.getMessage());
            }
        }
    }

    public void startRefresh(int refresh) {
        if (pollingJob == null) {
            pollingJob = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    updateRequest();
                } catch (IOException e) {
                    logger.warn("IO Exception: {}", e.getMessage());
                }
            }, 1, refresh, TimeUnit.MINUTES);
        }
    }

    public void updateRequest() throws IOException {
        getURLInput(BASE_URL);
        if ("true".equals(rtEnabled) && !isConnected()) {
            logger.debug("Attempting to reopen Websocket connection");
            open();
        }
    }

    public void updateChannel(String channelID, String channelValue) {
        if (!channelValue.contains("null")) {
            if (channelID.contains("consumption") || channelID.contains("Consumption")
                    || channelID.contains("accumulatedProduction")) {
                updateState(channelID, new QuantityType<>(new BigDecimal(channelValue), Units.KILOWATT_HOUR));
            } else if (channelID.contains("power") || channelID.contains("Power")) {
                updateState(channelID, new QuantityType<>(new BigDecimal(channelValue), Units.WATT));
            } else if (channelID.contains("voltage")) {
                updateState(channelID, new QuantityType<>(new BigDecimal(channelValue), Units.VOLT));
            } else if (channelID.contains("current")) {
                updateState(channelID, new QuantityType<>(new BigDecimal(channelValue), Units.AMPERE));
            } else {
                updateState(channelID, new DecimalType(channelValue));
            }
        }
    }

    public void thingStatusChanged(ThingStatusInfo thingStatusInfo) {
        logger.debug("Thing Status updated to {} for device: {}", thingStatusInfo.getStatus(), getThing().getUID());
        if (thingStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Unable to communicate with Tibber API");
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
            this.pollingJob = null;
        }
        if (isConnected()) {
            close();
            WebSocketClient client = this.client;
            if (client != null) {
                try {
                    logger.debug("DISPOSE - Stopping and Terminating Websocket connection");
                    client.stop();
                } catch (Exception e) {
                    logger.warn("Websocket Client Stop Exception: {}", e.getMessage());
                }
                client.destroy();
                this.client = null;
            }
        }
        super.dispose();
    }

    public void open() {
        WebSocketClient client = this.client;
        if (client == null || !client.isRunning() || !isConnected()) {
            if (client != null) {
                try {
                    client.stop();
                } catch (Exception e) {
                    logger.warn("OPEN FRAME - Failed to stop websocket client: {}", e.getMessage());
                }
                client.destroy();
            }
            sslContextFactory = new SslContextFactory.Client(true);
            sslContextFactory.setTrustAll(true);
            sslContextFactory.setEndpointIdentificationAlgorithm(null);

            client = new WebSocketClient(sslContextFactory);
            client.setMaxIdleTimeout(30 * 1000);
            this.client = client;

            TibberWebSocketListener socket = this.socket;
            if (socket == null) {
                logger.debug("New socket being created");
                socket = new TibberWebSocketListener();
                this.socket = socket;
            }

            ClientUpgradeRequest newRequest = new ClientUpgradeRequest();
            newRequest.setHeader("Authorization", "Bearer " + tibberConfig.getToken());
            newRequest.setSubProtocols("graphql-subscriptions");

            try {
                logger.debug("Starting Websocket connection");
                client.start();
            } catch (Exception e) {
                logger.warn("Websocket Start Exception: {}", e.getMessage());
            }
            try {
                logger.debug("Connecting Websocket connection");
                sessionFuture = client.connect(socket, new URI(SUBSCRIPTION_URL), newRequest);
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                }
                Session session = this.session;
                if (!session.isOpen()) {
                    close();
                    logger.warn("Unable to establish websocket session");
                } else {
                    logger.debug("Websocket session established");
                }
            } catch (IOException e) {
                logger.warn("Websocket Connect Exception: {}", e.getMessage());
            } catch (URISyntaxException e) {
                logger.warn("Websocket URI Exception: {}", e.getMessage());
            }
        } else {
            logger.warn("Open: Websocket client already running");
        }
    }

    public void close() {
        Session session = this.session;
        if (session != null) {
            String disconnect = "{\"type\":\"connection_terminate\",\"payload\":null}";
            try {
                TibberWebSocketListener socket = this.socket;
                if (socket != null) {
                    logger.debug("Sending websocket disconnect message");
                    socket.sendMessage(disconnect);
                } else {
                    logger.warn("Socket unable to send disconnect message: Socket is null");
                }
            } catch (IOException e) {
                logger.warn("Websocket Close Exception: {}", e.getMessage());
            }
            try {
                session.close();
            } catch (Exception e) {
                logger.warn("Unable to disconnect session");
            }
            this.session = null;
            this.socket = null;
        }
        Future<?> sessionFuture = this.sessionFuture;
        if (sessionFuture != null && !sessionFuture.isDone()) {
            sessionFuture.cancel(true);
        }
        WebSocketClient client = this.client;
        if (client != null) {
            try {
                client.stop();
            } catch (Exception e) {
                logger.warn("CLOSE FRAME - Failed to stop websocket client: {}", e.getMessage());
            }
            client.destroy();
        }
    }

    public boolean isConnected() {
        Session session = this.session;
        return session != null && session.isOpen();
    }

    @WebSocket
    @NonNullByDefault
    public class TibberWebSocketListener {

        @OnWebSocketConnect
        public void onConnect(Session wssession) {
            TibberHandler.this.session = wssession;
            TibberWebSocketListener socket = TibberHandler.this.socket;
            String connection = "{\"type\":\"connection_init\", \"payload\":\"token=" + tibberConfig.getToken() + "\"}";
            try {
                if (socket != null) {
                    logger.debug("Sending websocket connect message");
                    socket.sendMessage(connection);
                } else {
                    logger.debug("Socket unable to send connect message: Socket is null");
                }
            } catch (IOException e) {
                logger.warn("Send Message Exception: {}", e.getMessage());
            }
        }

        @OnWebSocketClose
        public void onClose(int statusCode, String reason) {
            logger.debug("Closing a WebSocket due to {}", reason);
            WebSocketClient client = TibberHandler.this.client;
            if (client != null && client.isRunning()) {
                try {
                    logger.debug("ONCLOSE - Stopping and Terminating Websocket connection");
                    client.stop();
                } catch (Exception e) {
                    logger.warn("Websocket Client Stop Exception: {}", e.getMessage());
                }
            }
        }

        @OnWebSocketError
        public void onWebSocketError(Throwable e) {
            String message = e.getMessage();
            logger.debug("Error during websocket communication: {}", message);
            close();
        }

        @OnWebSocketMessage
        public void onMessage(String message) {
            if (message.contains("connection_ack")) {
                logger.debug("Connected to Server");
                startSubscription();
            } else if (message.contains("error") || message.contains("terminate")) {
                logger.debug("Error/terminate received from server: {}", message);
                close();
            } else if (message.contains("liveMeasurement")) {
                JsonObject object = (JsonObject) JsonParser.parseString(message);
                JsonObject myObject = object.getAsJsonObject("payload").getAsJsonObject("data")
                        .getAsJsonObject("liveMeasurement");
                if (myObject.has("timestamp")) {
                    String liveTimestamp = myObject.get("timestamp").toString().substring(1, 20);
                    updateState(LIVE_TIMESTAMP, new DateTimeType(liveTimestamp));
                }
                if (myObject.has("power")) {
                    updateChannel(LIVE_POWER, myObject.get("power").toString());
                }
                if (myObject.has("lastMeterConsumption")) {
                    updateChannel(LIVE_LASTMETERCONSUMPTION, myObject.get("lastMeterConsumption").toString());
                }
                if (myObject.has("accumulatedConsumption")) {
                    updateChannel(LIVE_ACCUMULATEDCONSUMPTION, myObject.get("accumulatedConsumption").toString());
                }
                if (myObject.has("accumulatedCost")) {
                    updateChannel(LIVE_ACCUMULATEDCOST, myObject.get("accumulatedCost").toString());
                }
                if (myObject.has("currency")) {
                    updateState(LIVE_CURRENCY, new StringType(myObject.get("currency").toString()));
                }
                if (myObject.has("minPower")) {
                    updateChannel(LIVE_MINPOWER, myObject.get("minPower").toString());
                }
                if (myObject.has("averagePower")) {
                    updateChannel(LIVE_AVERAGEPOWER, myObject.get("averagePower").toString());
                }
                if (myObject.has("maxPower")) {
                    updateChannel(LIVE_MAXPOWER, myObject.get("maxPower").toString());
                }
                if (myObject.has("voltagePhase1")) {
                    updateChannel(LIVE_VOLTAGE1, myObject.get("voltagePhase1").toString());
                }
                if (myObject.has("voltagePhase2")) {
                    updateChannel(LIVE_VOLTAGE2, myObject.get("voltagePhase2").toString());
                }
                if (myObject.has("voltagePhase3")) {
                    updateChannel(LIVE_VOLTAGE3, myObject.get("voltagePhase3").toString());
                }
                if (myObject.has("currentL1")) {
                    updateChannel(LIVE_CURRENT1, myObject.get("currentL1").toString());
                }
                if (myObject.has("currentL2")) {
                    updateChannel(LIVE_CURRENT2, myObject.get("currentL2").toString());
                }
                if (myObject.has("currentL3")) {
                    updateChannel(LIVE_CURRENT3, myObject.get("currentL3").toString());
                }
                if (myObject.has("powerProduction")) {
                    updateChannel(LIVE_POWERPRODUCTION, myObject.get("powerProduction").toString());
                }
                if (myObject.has("accumulatedProduction")) {
                    updateChannel(LIVE_ACCUMULATEDPRODUCTION, myObject.get("accumulatedProduction").toString());
                }
                if (myObject.has("minPowerProduction")) {
                    updateChannel(LIVE_MINPOWERPRODUCTION, myObject.get("minPowerProduction").toString());
                }
                if (myObject.has("maxPowerProduction")) {
                    updateChannel(LIVE_MAXPOWERPRODUCTION, myObject.get("maxPowerProduction").toString());
                }
            } else {
                logger.debug("Unknown live response from Tibber");
            }
        }

        private void sendMessage(String message) throws IOException {
            logger.debug("Send message: {}", message);
            Session session = TibberHandler.this.session;
            if (session != null) {
                session.getRemote().sendString(message);
            }
        }

        public void startSubscription() {
            String query = "{\"id\":\"1\",\"type\":\"start\",\"payload\":{\"variables\":{},\"extensions\":{},\"operationName\":null,\"query\":\"subscription {\\n liveMeasurement(homeId:\\\""
                    + tibberConfig.getHomeid()
                    + "\\\") {\\n timestamp\\n power\\n lastMeterConsumption\\n accumulatedConsumption\\n accumulatedCost\\n currency\\n minPower\\n averagePower\\n maxPower\\n"
                    + "voltagePhase1\\n voltagePhase2\\n voltagePhase3\\n currentL1\\n currentL2\\n currentL3\\n powerProduction\\n accumulatedProduction\\n minPowerProduction\\n maxPowerProduction\\n }\\n }\\n\"}}";
            try {
                TibberWebSocketListener socket = TibberHandler.this.socket;
                if (socket != null) {
                    socket.sendMessage(query);
                } else {
                    logger.debug("Socket unable to send subscription message: Socket is null");
                }
            } catch (IOException e) {
                logger.warn("Send Message Exception: {}", e.getMessage());
            }
        }
    }
}
