/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.tibber.internal.config.TibberConfiguration;
import org.openhab.binding.tibber.internal.websocket.TibberWebsocket;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.scheduler.ScheduledCompletableFuture;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.TimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link TibberHandler} is responsible for handling queries to/from Tibber API.
 *
 * @author Stian Kjoglum - Initial contribution
 * @author Bernd Weymann - Use common HttpClient
 */
@NonNullByDefault
public class TibberHandler extends BaseThingHandler {
    private static final int REQUEST_TIMEOUT_SEC = 10;
    private final Logger logger = LoggerFactory.getLogger(TibberHandler.class);
    private final Random random = new Random();
    private final TreeMap<Instant, Double> spotPriceMap = new TreeMap<>();
    private final TreeMap<Instant, String> spotPriceLevelMap = new TreeMap<>();
    private final HttpClient httpClient;
    private final Storage<String> storage;
    private final CronScheduler cron;

    private TibberConfiguration tibberConfig = new TibberConfiguration();
    private Optional<ScheduledFuture<?>> websocketWatchdog = Optional.empty();
    private Optional<ScheduledCompletableFuture<?>> cronDaily = Optional.empty();
    private Optional<TibberWebsocket> webSocket = Optional.empty();
    private Optional<Boolean> realtimeEnabled = Optional.empty();
    private int retryCounter = 0;

    public TibberHandler(Thing thing, HttpClient httpClient, CronScheduler cron, Storage<String> storage) {
        super(thing);
        this.httpClient = httpClient;
        this.cron = cron;
        this.storage = storage;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands handled
    }

    @Override
    public void initialize() {
        tibberConfig = getConfigAs(TibberConfiguration.class);
        if (EMPTY.equals(tibberConfig.homeid) || EMPTY.equals(tibberConfig.token)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            scheduler.execute(this::doInitialize);
        }
    }

    @Override
    public void dispose() {
        cronDaily.ifPresent(job -> {
            job.cancel(true);
        });
        cronDaily = Optional.empty();
        websocketWatchdog.ifPresent(job -> {
            job.cancel(true);
        });
        websocketWatchdog = Optional.empty();
        webSocket.ifPresent(socket -> {
            socket.stop();
        });
        webSocket = Optional.empty();
        super.dispose();
    }

    public Request getRequest() {
        Request req = httpClient.POST(BASE_URL).timeout(REQUEST_TIMEOUT_SEC, TimeUnit.SECONDS);
        req.header(HttpHeader.AUTHORIZATION, "Bearer " + tibberConfig.token);
        req.header(HttpHeader.USER_AGENT, AGENT_VERSION);

        req.header(HttpHeader.CONTENT_TYPE, JSON_CONTENT_TYPE);
        req.header("cache-control", "no-cache");
        return req;
    }

    private void doInitialize() {
        Request initRequest = getRequest();
        String body = String.format(HOME_ID_QUERY, tibberConfig.homeid);
        initRequest.content(new StringContentProvider(body, "utf-8"));
        try {
            ContentResponse cr = initRequest.send();
            int responseStatus = cr.getStatus();
            String initResponse = cr.getContentAsString();
            logger.trace("doInitialze response {} - {}", responseStatus, initResponse);
            if (responseStatus == 200) {
                if (!initResponse.contains("error") && !initResponse.contains("<html>")) {
                    updateStatus(ThingStatus.ONLINE);
                    // start websocket plus watchdog
                    webSocket = Optional.of(new TibberWebsocket(this, tibberConfig, httpClient));
                    websocketWatchdog = Optional
                            .of(scheduler.scheduleWithFixedDelay(this::watchdog, 0, 1, TimeUnit.MINUTES));

                    // start cron update for new spot prices
                    scheduler.schedule(this::updateSpotPrices, 0, TimeUnit.MINUTES);
                    String hour = storage.get(tibberConfig.homeid);
                    if (hour == null) {
                        cronDaily = Optional
                                .of(cron.schedule(this::updateSpotPrices, String.format(CRON_DAILY_AT, "*")));
                    } else {
                        cronDaily = Optional
                                .of(cron.schedule(this::updateSpotPrices, String.format(CRON_DAILY_AT, hour)));
                    }

                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Status: " + responseStatus + " - " + initResponse);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Status: " + responseStatus + " - " + initResponse);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void watchdog() {
        if (isRealtimeEnabled() && liveChannelsLinked()) {
            webSocket.ifPresentOrElse(socket -> {
                if (!socket.isConnected()) {
                    socket.start();
                } else {
                    socket.ping();
                }
            }, () -> {
                webSocket = Optional.of(new TibberWebsocket(this, tibberConfig, httpClient));
                webSocket.get().start();
            });
        } else {
            webSocket.ifPresentOrElse(socket -> {
                if (socket.isConnected()) {
                    socket.stop();
                }
            }, () -> {
                logger.trace("Websocket is kept offline - either feature disabled ({}) or channel mot linked ({})",
                        isRealtimeEnabled(), liveChannelsLinked());
            });
        }
    }

    private void updateSpotPrices() {
        Request priceRequest = getRequest();
        String body = String.format(PRICE_QUERY, tibberConfig.homeid);
        priceRequest.content(new StringContentProvider(body, "utf-8"));
        try {
            ContentResponse cr = priceRequest.send();
            int responseStatus = cr.getStatus();
            String jsonResponse = cr.getContentAsString();
            logger.trace("updatePrices response {} - {}", responseStatus, jsonResponse);
            if (responseStatus != 200) {
                updatePriceInfoRetry();
                return;
            } else {
                // reset counter after successful call
                retryCounter = 0;
            }
            if (!jsonResponse.contains("error") && !jsonResponse.contains("<html>")) {
                JsonObject rootJsonObject = (JsonObject) JsonParser.parseString(jsonResponse);

                if (jsonResponse.contains("total")) {
                    try {
                        JsonObject current = rootJsonObject.getAsJsonObject("data").getAsJsonObject("viewer")
                                .getAsJsonObject("home").getAsJsonObject("currentSubscription")
                                .getAsJsonObject("priceInfo").getAsJsonObject("current");

                        updateState(CURRENT_TOTAL, new DecimalType(current.get("total").toString()));
                        String timestamp = current.get("startsAt").toString().substring(1, 20);
                        updateState(CURRENT_STARTSAT, new DateTimeType(timestamp));
                        updateState(CURRENT_LEVEL,
                                new StringType(current.get("level").toString().replaceAll("^\"|\"$", "")));

                        JsonArray tomorrow = rootJsonObject.getAsJsonObject("data").getAsJsonObject("viewer")
                                .getAsJsonObject("home").getAsJsonObject("currentSubscription")
                                .getAsJsonObject("priceInfo").getAsJsonArray("tomorrow");
                        updateState(TOMORROW_PRICES, new StringType(tomorrow.toString()));
                        JsonArray today = rootJsonObject.getAsJsonObject("data").getAsJsonObject("viewer")
                                .getAsJsonObject("home").getAsJsonObject("currentSubscription")
                                .getAsJsonObject("priceInfo").getAsJsonArray("today");
                        updateState(TODAY_PRICES, new StringType(today.toString()));

                        TimeSeries timeSeries = buildTimeSeries(today, tomorrow);
                        sendTimeSeries(CURRENT_TOTAL, timeSeries);
                    } catch (JsonSyntaxException | DateTimeParseException e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "Error communicating with Tibber API: " + e.getMessage());
                    }
                }
                if (jsonResponse.contains("daily") && !jsonResponse.contains("\"daily\":{\"nodes\":[]")
                        && !jsonResponse.contains("\"daily\":null")) {
                    try {
                        JsonObject myObject = (JsonObject) rootJsonObject.getAsJsonObject("data")
                                .getAsJsonObject("viewer").getAsJsonObject("home").getAsJsonObject("daily")
                                .getAsJsonArray("nodes").get(0);

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
                        JsonObject myObject = (JsonObject) rootJsonObject.getAsJsonObject("data")
                                .getAsJsonObject("viewer").getAsJsonObject("home").getAsJsonObject("hourly")
                                .getAsJsonArray("nodes").get(0);

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
                updatePriceInfoRetry();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Error in response from Tibber API: " + jsonResponse);
            } else {
                updatePriceInfoRetry();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Unexpected response from Tibber: " + jsonResponse);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            updatePriceInfoRetry();
        }
    }

    private boolean isRealtimeEnabled() {
        if (realtimeEnabled.isPresent()) {
            return realtimeEnabled.get();
        } else {
            Request realtimeRequest = getRequest();
            String body = String.format(REALTIME_QUERY, tibberConfig.homeid);
            realtimeRequest.content(new StringContentProvider(body, "utf-8"));

            try {
                ContentResponse cr = realtimeRequest.send();
                int responseStatus = cr.getStatus();
                String jsonResponse = cr.getContentAsString();
                logger.trace("isRealtimeEnabled response {} - {}", responseStatus, jsonResponse);
                if (!jsonResponse.contains("error") && !jsonResponse.contains("<html>")) {
                    JsonObject object = (JsonObject) JsonParser.parseString(jsonResponse);
                    JsonObject dObject = object.getAsJsonObject("data");
                    if (dObject != null) {
                        JsonObject viewerObject = dObject.getAsJsonObject("viewer");
                        if (viewerObject != null) {
                            JsonObject homeObject = viewerObject.getAsJsonObject("home");
                            if (homeObject != null) {
                                JsonObject featuresObject = homeObject.getAsJsonObject("features");
                                if (featuresObject != null) {
                                    String rtEnabled = featuresObject.get("realTimeConsumptionEnabled").toString();
                                    realtimeEnabled = Optional.of(Boolean.valueOf(rtEnabled));
                                    return realtimeEnabled.get();
                                }
                            }
                        }
                    }
                }
            } catch (JsonSyntaxException | InterruptedException | TimeoutException | ExecutionException e) {
            }
        }
        return false;
    }

    /**
     * Builds the {@link TimeSeries} that represents the future tibber prices.
     *
     * @param today The prices for today
     * @param tomorrow The prices for tomorrow.
     * @return The {@link TimeSeries} with future values.
     */
    private TimeSeries buildTimeSeries(JsonArray today, JsonArray tomorrow) {
        final TimeSeries timeSeries = new TimeSeries(TimeSeries.Policy.REPLACE);
        TreeMap<Instant, Double> newSpotPrices = new TreeMap<>();
        mapTimeSeriesEntries(today, timeSeries, newSpotPrices);
        mapTimeSeriesEntries(tomorrow, timeSeries, newSpotPrices);
        if (spotPriceMap.equals(newSpotPrices)) {
            logger.trace("Same spot prices as before");
        } else {
            logger.trace("Different spot prices than before");
            spotPriceMap.clear();
            spotPriceMap.putAll(newSpotPrices);
        }
        return timeSeries;
    }

    private void mapTimeSeriesEntries(JsonArray prices, TimeSeries timeSeries, TreeMap<Instant, Double> newSpotPrices) {
        for (JsonElement entry : prices) {
            JsonObject entryObject = entry.getAsJsonObject();
            final Instant startsAt = Instant.parse(entryObject.get("startsAt").getAsString());
            final Double price = entryObject.get("total").getAsDouble();
            final DecimalType value = new DecimalType(price);
            newSpotPrices.put(startsAt, price);
            timeSeries.add(startsAt, value);
        }
    }

    private boolean liveChannelsLinked() {
        return getThing().getChannels().stream().map(Channel::getUID)
                .filter((channelUID -> channelUID.getAsString().contains("live_"))).anyMatch(this::isLinked);
    };

    private void updatePriceInfoRetry() {
        // fulfill https://developer.tibber.com/docs/guides/calling-api
        // Clients must implement jitter and exponential backoff when retrying queries.
        if (retryCounter == 0) {
            retryCounter = 1;
        } else {
            retryCounter *= 2;
        }

        // return increasing time retry + random jitter, max one minute
        int retryMs = Math.min(1000 * retryCounter + random.nextInt(1000), 60 * 1000);
        logger.trace("Try to update prices in {} ms", retryMs);
        scheduler.schedule(this::updateSpotPrices, retryMs, TimeUnit.MILLISECONDS);
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

    public void newMessage(String message) {
        if (message.contains("error") || message.contains("terminate")) {
            logger.debug("Error/terminate received from server: {}", message);
        } else if (message.contains("liveMeasurement")) {
            // logger.debug("Received liveMeasurement message.");
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
            if (myObject.has("lastMeterProduction")) {
                updateChannel(LIVE_LASTMETERPRODUCTION, myObject.get("lastMeterProduction").toString());
            }
            if (myObject.has("accumulatedConsumption")) {
                updateChannel(LIVE_ACCUMULATEDCONSUMPTION, myObject.get("accumulatedConsumption").toString());
            }
            if (myObject.has("accumulatedConsumptionLastHour")) {
                updateChannel(LIVE_ACCUMULATEDCONSUMPTION_THIS_HOUR,
                        myObject.get("accumulatedConsumptionLastHour").toString());
            }
            if (myObject.has("accumulatedCost")) {
                updateChannel(LIVE_ACCUMULATEDCOST, myObject.get("accumulatedCost").toString());
            }
            if (myObject.has("accumulatedReward")) {
                updateChannel(LIVE_ACCUMULATEREWARD, myObject.get("accumulatedReward").toString());
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
            if (myObject.has("accumulatedProductionLastHour")) {
                updateChannel(LIVE_ACCUMULATEDPRODUCTION_THIS_HOUR,
                        myObject.get("accumulatedProductionLastHour").toString());
            }
            if (myObject.has("minPowerProduction")) {
                updateChannel(LIVE_MINPOWERPRODUCTION, myObject.get("minPowerProduction").toString());
            }
            if (myObject.has("maxPowerProduction")) {
                updateChannel(LIVE_MAXPOWERPRODUCTION, myObject.get("maxPowerProduction").toString());
            }
        } else {
            logger.debug("Unknown live response from Tibber. Message: {}", message);
        }
    }
}
