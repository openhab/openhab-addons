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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.tibber.internal.Utils;
import org.openhab.binding.tibber.internal.action.TibberActions;
import org.openhab.binding.tibber.internal.calculator.PriceCalculator;
import org.openhab.binding.tibber.internal.config.TibberConfiguration;
import org.openhab.binding.tibber.internal.exception.PriceCalculationException;
import org.openhab.binding.tibber.internal.websocket.TibberWebsocket;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.CurrencyUnits;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.scheduler.ScheduledCompletableFuture;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.UnDefType;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
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
 * @author Bernd Weymann - Use common HttpClient, rework of Nullable fields
 */
@NonNullByDefault
public class TibberHandler extends BaseThingHandler {
    private static final int REQUEST_TIMEOUT_SEC = 10;
    private final Logger logger = LoggerFactory.getLogger(TibberHandler.class);
    private final Random random = new Random();
    private final BundleContext bundleContext;
    private final HttpClient httpClient;
    private final CronScheduler cron;
    private final Map<String, String> templates = new HashMap<>();
    private final TimeZoneProvider timeZoneProvider;

    private final ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();
    private TibberConfiguration tibberConfig = new TibberConfiguration();
    private @Nullable ScheduledFuture<?> watchdog;
    private @Nullable ScheduledCompletableFuture<?> cronDaily;
    private @Nullable TibberWebsocket webSocket;
    private @Nullable Boolean realtimeEnabled;
    private @Nullable String currencyUnit;
    private Object calculatorLock = new Object();
    private int retryCounter = 0;
    private boolean isDisposed = true;

    private TimeSeries priceCache = new TimeSeries(TimeSeries.Policy.REPLACE);
    private TimeSeries energyCache = new TimeSeries(TimeSeries.Policy.REPLACE);
    private TimeSeries taxCache = new TimeSeries(TimeSeries.Policy.REPLACE);
    private TimeSeries levelCache = new TimeSeries(TimeSeries.Policy.REPLACE);
    private TimeSeries averageCache = new TimeSeries(TimeSeries.Policy.REPLACE);

    protected @Nullable PriceCalculator calculator;

    public TibberHandler(Thing thing, HttpClient httpClient, CronScheduler cron, BundleContext bundleContext,
            TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.httpClient = httpClient;
        this.cron = cron;
        this.bundleContext = bundleContext;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            String group = channelUID.getGroupId();
            if (CHANNEL_GROUP_PRICE.equals(group)) {
                switch (channelUID.getIdWithoutGroup()) {
                    case CHANNEL_TOTAL_PRICE:
                        sendTimeSeries(new ChannelUID(thing.getUID(), CHANNEL_GROUP_PRICE, CHANNEL_TOTAL_PRICE),
                                priceCache);
                        sendTimeSeries(
                                new ChannelUID(thing.getUID(), CHANNEL_GROUP_PRICE, CHANNEL_DEPRECATED_SPOT_PRICE),
                                priceCache);
                        break;
                    case CHANNEL_SPOT_PRICE:
                        sendTimeSeries(new ChannelUID(thing.getUID(), CHANNEL_GROUP_PRICE, CHANNEL_SPOT_PRICE),
                                energyCache);
                        break;
                    case CHANNEL_TAX:
                        sendTimeSeries(new ChannelUID(thing.getUID(), CHANNEL_GROUP_PRICE, CHANNEL_TAX), taxCache);
                        break;
                    case CHANNEL_PRICE_LEVELS:
                        sendTimeSeries(new ChannelUID(thing.getUID(), CHANNEL_GROUP_PRICE, CHANNEL_PRICE_LEVELS),
                                levelCache);
                        break;
                    case CHANNEL_AVERAGE:
                        sendTimeSeries(new ChannelUID(thing.getUID(), CHANNEL_GROUP_PRICE, CHANNEL_AVERAGE),
                                averageCache);
                        break;
                }
            }
        }
    }

    @Override
    public void initialize() {
        isDisposed = false;
        tibberConfig = getConfigAs(TibberConfiguration.class);
        if (tibberConfig.homeid.isBlank() || tibberConfig.token.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/status.configuration-error");
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            scheduler.execute(this::doInitialize);
        }
    }

    @Override
    public void dispose() {
        isDisposed = true;

        ScheduledCompletableFuture<?> cronDaily = this.cronDaily;
        if (cronDaily != null) {
            cronDaily.cancel(true);
        }
        this.cronDaily = null;

        ScheduledFuture<?> watchdog = this.watchdog;
        if (watchdog != null) {
            watchdog.cancel(true);
        }
        this.watchdog = null;

        TibberWebsocket webSocket = this.webSocket;
        if (webSocket != null) {
            webSocket.stop();
        }
        this.webSocket = null;

        super.dispose();
    }

    public Request getRequest() {
        Request req = httpClient.POST(BASE_URL).timeout(REQUEST_TIMEOUT_SEC, TimeUnit.SECONDS);
        req.header(HttpHeader.AUTHORIZATION, "Bearer " + tibberConfig.token);
        req.header(HttpHeader.USER_AGENT, Utils.getUserAgent(this));

        req.header(HttpHeader.CONTENT_TYPE, JSON_CONTENT_TYPE);
        req.header("cache-control", "no-cache");
        return req;
    }

    private void doInitialize() {
        Request currencyRequest = getRequest();
        String body = String.format(QUERY_CONTAINER,
                String.format(getTemplate(CURRENCY_QUERY_RESOURCE_PATH), tibberConfig.homeid));
        logger.trace("Query with body {}", body);
        currencyRequest.content(new StringContentProvider(body, "utf-8"));
        try {
            ContentResponse cr = currencyRequest.send();
            int responseStatus = cr.getStatus();
            String currencyResponse = cr.getContentAsString();
            logger.trace("doInitialze response {} - {}", responseStatus, currencyResponse);
            if (responseStatus == HttpStatus.OK_200) {
                JsonObject jsonResponse = (JsonObject) JsonParser.parseString(currencyResponse);
                JsonObject currency = Utils.getJsonObject(jsonResponse, CURRENCY_QUERY_JSON_PATH);
                if (!currency.isEmpty()) {
                    updateStatus(ThingStatus.ONLINE);

                    // check if currency is supported
                    String currencyCode = currency.get("currency").getAsString();
                    Unit<?> unit = CurrencyUnits.getInstance().getUnit(currencyCode);
                    if (unit != null) {
                        currencyUnit = currencyCode;
                        logger.trace("Currency is set to {}", unit.getSymbol());
                    } else {
                        logger.trace("Currency {} is unknown, falling back to DecimalType", currencyCode);
                    }

                    // create websocket and watchdog
                    webSocket = new TibberWebsocket(this, tibberConfig, httpClient);
                    watchdog = scheduler.scheduleWithFixedDelay(this::watchdog, 0, 1, TimeUnit.MINUTES);

                    // start cron update for new spot prices
                    scheduler.schedule(this::updateSpotPrices, 0, TimeUnit.MINUTES);
                    int hour = tibberConfig.updateHour;
                    String cronHour = (hour < 0) ? "*" : String.valueOf(hour);
                    cronDaily = cron.schedule(this::updateSpotPrices, String.format(CRON_DAILY_AT, cronHour));
                    return;
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/status.initial-call-failed  [\"" + responseStatus + " - " + currencyResponse + "\"]");
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/status.initial-call-failed  [\"" + e.getMessage() + "\"]");
        }
        watchdog = scheduler.schedule(this::doInitialize, 1, TimeUnit.MINUTES);
    }

    private void watchdog() {
        TibberWebsocket webSocket = this.webSocket;
        if (liveChannelsLinked() && isRealtimeEnabled()) {
            if (webSocket != null) {
                if (!webSocket.isConnected()) {
                    webSocket.start();
                } else {
                    webSocket.ping();
                }
            } else {
                this.webSocket = webSocket = new TibberWebsocket(this, tibberConfig, httpClient);
                webSocket.start();
            }
        } else {
            if (webSocket != null) {
                if (webSocket.isConnected()) {
                    webSocket.stop();
                }
            } else {
                logger.trace("Websocket is kept offline - either feature disabled ({}) or channel mot linked ({})",
                        isRealtimeEnabled(), liveChannelsLinked());
            }
        }
    }

    private void updateSpotPrices() {
        Request priceRequest = getRequest();
        String body = String.format(QUERY_CONTAINER,
                String.format(getTemplate(PRICE_QUERY_RESOURCE_PATH), tibberConfig.homeid));
        priceRequest.content(new StringContentProvider(body, "utf-8"));
        try {
            ContentResponse cr = priceRequest.send();
            int responseStatus = cr.getStatus();
            String jsonResponse = cr.getContentAsString();
            logger.trace("updatePrices response {} - {}", responseStatus, jsonResponse);
            if (responseStatus != HttpStatus.OK_200) {
                updatePriceInfoRetry();
                return;
            }
            if (!jsonResponse.contains("error") && !jsonResponse.contains("<html>")) {
                JsonObject rootJsonObject = (JsonObject) JsonParser.parseString(jsonResponse);
                JsonObject priceInfo = Utils.getJsonObject(rootJsonObject, PRICE_INFO_JSON_PATH);

                if (!priceInfo.isEmpty()) {
                    try {
                        // now check if tomorrows prices are updated for the given hour
                        if (tibberConfig.updateHour <= Instant.now().atZone(timeZoneProvider.getTimeZone()).getHour()) {
                            JsonArray tomorrowPrices = priceInfo.getAsJsonArray("tomorrow");
                            if (tomorrowPrices.isEmpty()) {
                                logger.debug("No update for tomorrow found - retry");
                                updatePriceInfoRetry();
                                return;
                            } else {
                                logger.debug("update found - continue");
                                retryCounter = 0;
                            }
                        }
                        JsonArray spotPrices = new JsonArray();
                        spotPrices.addAll(priceInfo.getAsJsonArray("today"));
                        spotPrices.addAll(priceInfo.getAsJsonArray("tomorrow"));

                        TimeSeries timeSeriesTotalPrices = new TimeSeries(TimeSeries.Policy.REPLACE);
                        TimeSeries timeSeriesEnergyPrices = new TimeSeries(TimeSeries.Policy.REPLACE);
                        TimeSeries timeSeriesTaxPrices = new TimeSeries(TimeSeries.Policy.REPLACE);
                        TimeSeries timeSeriesLevels = new TimeSeries(TimeSeries.Policy.REPLACE);
                        for (JsonElement entry : spotPrices) {
                            JsonObject entryObject = entry.getAsJsonObject();
                            Instant startsAt = Instant.parse(entryObject.get("startsAt").getAsString());
                            String totalPriceString = entryObject.get("total").getAsString();
                            timeSeriesTotalPrices.add(startsAt, getEnergyPrice(totalPriceString));
                            String energyPriceString = entryObject.get("energy").getAsString();
                            timeSeriesEnergyPrices.add(startsAt, getEnergyPrice(energyPriceString));
                            String taxPriceString = entryObject.get("tax").getAsString();
                            timeSeriesTaxPrices.add(startsAt, getEnergyPrice(taxPriceString));

                            String levelString = entryObject.get("level").getAsString();
                            timeSeriesLevels.add(startsAt, Utils.mapToState(levelString));
                        }
                        priceCache = timeSeriesTotalPrices;
                        sendTimeSeries(new ChannelUID(thing.getUID(), CHANNEL_GROUP_PRICE, CHANNEL_TOTAL_PRICE),
                                timeSeriesTotalPrices);
                        sendTimeSeries(
                                new ChannelUID(thing.getUID(), CHANNEL_GROUP_PRICE, CHANNEL_DEPRECATED_SPOT_PRICE),
                                timeSeriesTotalPrices);
                        energyCache = timeSeriesEnergyPrices;
                        sendTimeSeries(new ChannelUID(thing.getUID(), CHANNEL_GROUP_PRICE, CHANNEL_SPOT_PRICE),
                                timeSeriesEnergyPrices);
                        taxCache = timeSeriesTaxPrices;
                        sendTimeSeries(new ChannelUID(thing.getUID(), CHANNEL_GROUP_PRICE, CHANNEL_TAX),
                                timeSeriesTaxPrices);

                        levelCache = timeSeriesLevels;
                        sendTimeSeries(new ChannelUID(thing.getUID(), CHANNEL_GROUP_PRICE, CHANNEL_PRICE_LEVELS),
                                timeSeriesLevels);
                        PriceCalculator calculator;
                        synchronized (calculatorLock) {
                            this.calculator = calculator = new PriceCalculator(spotPrices);
                        }

                        TreeMap<Instant, Double> averagePrices = calculator.calculateAveragePrices();
                        TimeSeries avgSeries = new TimeSeries(TimeSeries.Policy.REPLACE);
                        averagePrices.forEach((key, value) -> {
                            String priceString = String.valueOf(value);
                            avgSeries.add(key, getEnergyPrice(priceString));
                        });
                        averageCache = avgSeries;
                        sendTimeSeries(new ChannelUID(thing.getUID(), CHANNEL_GROUP_PRICE, CHANNEL_AVERAGE), avgSeries);
                        updateStatus(ThingStatus.ONLINE);
                    } catch (JsonSyntaxException | DateTimeParseException e) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "@text/status.price-update-failed  [\"" + e.getMessage() + "\"]");
                    }
                }
            } else if (jsonResponse.contains("error")) {
                updatePriceInfoRetry();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/status.price-update-failed  [\"" + responseStatus + "\"]");
            } else {
                updatePriceInfoRetry();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/status.price-update-failed  [\"" + jsonResponse + "\"]");
            }
        } catch (TimeoutException | ExecutionException e) {
            updatePriceInfoRetry();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/status.price-update-failed  [\"" + e.getMessage() + "\"]");
        } catch (InterruptedException e1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/status.price-update-failed  [\"" + e1.getMessage() + "\"]");
            Thread.currentThread().interrupt();
        }
    }

    public PriceCalculator getPriceCalculator() throws PriceCalculationException {
        synchronized (calculatorLock) {
            PriceCalculator calculator = this.calculator;
            if (calculator != null) {
                return calculator;
            } else {
                throw new PriceCalculationException(
                        "No PriceCalculator available! Maybe OFFLINE or Thing deactivated.");
            }
        }
    }

    private State getEnergyPrice(String priceString) {
        State priceState;
        String currencyUnit = this.currencyUnit;
        if (currencyUnit != null) {
            priceState = QuantityType.valueOf(priceString + " " + currencyUnit + "/kWh");
        } else {
            priceState = DecimalType.valueOf(priceString);
        }
        return priceState;
    }

    private boolean isRealtimeEnabled() {
        Boolean realtimeEnabled = this.realtimeEnabled;
        if (realtimeEnabled != null) {
            return realtimeEnabled.booleanValue();
        } else {
            Request realtimeRequest = getRequest();
            String body = String.format(QUERY_CONTAINER,
                    String.format(getTemplate(REALTIME_QUERY_RESOURCE_PATH), tibberConfig.homeid));
            realtimeRequest.content(new StringContentProvider(body, "utf-8"));

            try {
                ContentResponse cr = realtimeRequest.send();
                int responseStatus = cr.getStatus();
                String jsonResponse = cr.getContentAsString();
                logger.trace("isRealtimeEnabled response {} - {}", responseStatus, jsonResponse);
                JsonObject object = (JsonObject) JsonParser.parseString(jsonResponse);
                JsonObject featuresObject = Utils.getJsonObject(object, REALTIME_FEATURE_JSON_PATH);
                if (!featuresObject.isEmpty()) {
                    String rtEnabled = featuresObject.get("realTimeConsumptionEnabled").toString();
                    this.realtimeEnabled = realtimeEnabled = Boolean.valueOf(rtEnabled);
                    return realtimeEnabled.booleanValue();
                }
            } catch (JsonSyntaxException | InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("Realtime feature query failed {}", e.getMessage());
            }
        }
        return false;
    }

    private boolean liveChannelsLinked() {
        return getThing().getChannels().stream().map(Channel::getUID)
                .filter((channelUID -> channelUID.getAsString().contains(CHANNEL_GROUP_LIVE)
                        || channelUID.getAsString().contains(CHANNEL_GROUP_STATISTICS)))
                .anyMatch(this::isLinked);
    };

    private void updatePriceInfoRetry() {
        if (isDisposed) {
            logger.trace("Retry rejected due to disposed thing");
            return;
        }
        // fulfill https://developer.tibber.com/docs/guides/calling-api
        // Clients must implement jitter and exponential backoff when retrying queries.
        if (retryCounter == 0) {
            retryCounter = 1;
        } else {
            retryCounter = Math.min(60, retryCounter * 2);
        }

        // return increasing time retry + random jitter, max 20 minutes
        int retryMs = Math.min(1000 * retryCounter + random.nextInt(1000), 20 * 60 * 1000);
        logger.trace("Try to update prices in {} ms", retryMs);
        scheduler.schedule(this::updateSpotPrices, retryMs, TimeUnit.MILLISECONDS);
    }

    public void newMessage(String message) {
        if (message.contains("error") || message.contains("terminate")) {
            logger.debug("Error/terminate received from server: {}", message);
        } else if (message.contains("liveMeasurement")) {
            messageQueue.add(message);
            scheduler.schedule(this::handleNewMessage, 0, TimeUnit.SECONDS);
        }
    }

    @SuppressWarnings("all")
    private void handleNewMessage() {
        String message = messageQueue.poll();
        // This is not dead code, value can be null!
        if (message == null) {
            return;
        }
        JsonObject jsonMessage = (JsonObject) JsonParser.parseString(message);
        JsonObject jsonData = Utils.getJsonObject(jsonMessage, SOCKET_MESSAGE_JSON_PATH);
        if (!jsonData.isEmpty()) {
            String value = Utils.getJsonValue(jsonData, "lastMeterConsumption");
            updateChannel(CHANNEL_GROUP_STATISTICS, CHANNEL_TOTAL_CONSUMPTION, value, "kWh");
            value = Utils.getJsonValue(jsonData, "accumulatedConsumption");
            updateChannel(CHANNEL_GROUP_STATISTICS, CHANNEL_DAILY_CONSUMPTION, value, "kWh");
            value = Utils.getJsonValue(jsonData, "accumulatedConsumptionLastHour");
            updateChannel(CHANNEL_GROUP_STATISTICS, CHANNEL_LAST_HOUR_CONSUMPTION, value, "kWh");
            value = Utils.getJsonValue(jsonData, "accumulatedCost");
            if (!EMPTY_VALUE.equals(value)) {
                State costState;
                if (!NULL_VALUE.equals(value)) {
                    String currencyUnit = this.currencyUnit;
                    if (currencyUnit != null) {
                        costState = QuantityType.valueOf(value + " " + currencyUnit);
                    } else {
                        costState = DecimalType.valueOf(value);
                    }
                } else {
                    costState = UnDefType.UNDEF;
                }
                updateState(new ChannelUID(thing.getUID(), CHANNEL_GROUP_STATISTICS, CHANNEL_DAILY_COST), costState);
            }
            value = Utils.getJsonValue(jsonData, "lastMeterProduction");
            updateChannel(CHANNEL_GROUP_STATISTICS, CHANNEL_TOTAL_PRODUCTION, value, "kWh");
            value = Utils.getJsonValue(jsonData, "accumulatedProduction");
            updateChannel(CHANNEL_GROUP_STATISTICS, CHANNEL_DAILY_PRODUCTION, value, "kWh");
            value = Utils.getJsonValue(jsonData, "accumulatedProductionLastHour");
            updateChannel(CHANNEL_GROUP_STATISTICS, CHANNEL_LAST_HOUR_PRODUCTION, value, "kWh");
            value = Utils.getJsonValue(jsonData, "minPower");
            updateChannel(CHANNEL_GROUP_LIVE, CHANNEL_MIN_COSNUMPTION, value, "W");
            value = Utils.getJsonValue(jsonData, "maxPower");
            updateChannel(CHANNEL_GROUP_LIVE, CHANNEL_PEAK_CONSUMPTION, value, "W");
            value = Utils.getJsonValue(jsonData, "averagePower");
            updateChannel(CHANNEL_GROUP_LIVE, CHANNEL_AVERAGE_CONSUMPTION, value, "W");
            value = Utils.getJsonValue(jsonData, "minPowerProduction");
            updateChannel(CHANNEL_GROUP_LIVE, CHANNEL_MIN_PRODUCTION, value, "W");
            value = Utils.getJsonValue(jsonData, "maxPowerProduction");
            updateChannel(CHANNEL_GROUP_LIVE, CHANNEL_PEAK_PRODUCTION, value, "W");
            value = Utils.getJsonValue(jsonData, "voltagePhase1");
            updateChannel(CHANNEL_GROUP_LIVE, CHANNEL_VOLTAGE_1, value, "V");
            value = Utils.getJsonValue(jsonData, "voltagePhase2");
            updateChannel(CHANNEL_GROUP_LIVE, CHANNEL_VOLTAGE_2, value, "V");
            value = Utils.getJsonValue(jsonData, "voltagePhase3");
            updateChannel(CHANNEL_GROUP_LIVE, CHANNEL_VOLTAGE_3, value, "V");
            value = Utils.getJsonValue(jsonData, "currentL1");
            updateChannel(CHANNEL_GROUP_LIVE, CHANNEL_CURRENT_1, value, "A");
            value = Utils.getJsonValue(jsonData, "currentL2");
            updateChannel(CHANNEL_GROUP_LIVE, CHANNEL_CURRENT_2, value, "A");
            value = Utils.getJsonValue(jsonData, "currentL3");
            updateChannel(CHANNEL_GROUP_LIVE, CHANNEL_CURRENT_3, value, "A");

            String consumption = Utils.getJsonValue(jsonData, "power");
            String production = Utils.getJsonValue(jsonData, "powerProduction");
            updateChannel(CHANNEL_GROUP_LIVE, CHANNEL_CONSUMPTION, consumption, "W");
            updateChannel(CHANNEL_GROUP_LIVE, CHANNEL_PRODUCTION, production, "W");
            double consumptionValue = parseValueSafely(consumption, "consumption");
            double productionValue = parseValueSafely(production, "production");

            updateChannel(CHANNEL_GROUP_LIVE, CHANNEL_POWER_BALANCE, String.valueOf(consumptionValue - productionValue),
                    "W");
        }
    }

    private double parseValueSafely(@Nullable String value, String valueType) {
        if (value == null || value.isBlank() || EMPTY_VALUE.equals(value) || NULL_VALUE.equals(value)) {
            return 0.0;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            logger.error("Unable to parse {} value: {}. Assuming 0.", valueType, value, e);
            return 0.0;
        }
    }

    private void updateChannel(String group, String channelId, String value, String unit) {
        if (EMPTY_VALUE.equals(value)) {
            // value not present - don't update
            return;
        }
        // value is present
        if (!NULL_VALUE.equals(value)) {
            // value isn't null
            updateState(new ChannelUID(thing.getUID(), group, channelId), QuantityType.valueOf(value + " " + unit));
        } else {
            // value is null
            updateState(new ChannelUID(thing.getUID(), group, channelId), UnDefType.NULL);
        }
    }

    public String getTemplate(String name) {
        String template = templates.get(name);
        if (template == null) {
            template = getResourceFile(name);
            if (!template.isBlank()) {
                templates.put(name, template);
            } else {
                template = EMPTY_VALUE;
            }
        }
        return template;
    }

    private String getResourceFile(String fileName) {
        try {
            Bundle myself = bundleContext.getBundle();
            // do this check for unit tests to avoid NullPointerException
            if (myself != null) {
                URL url = myself.getResource(fileName);
                logger.debug("try to get {}", url);
                InputStream input = url.openStream();
                // https://www.baeldung.com/java-scanner-usedelimiter
                try (Scanner scanner = new Scanner(input).useDelimiter("\\A")) {
                    String result = scanner.hasNext() ? scanner.next() : "";
                    String resultReplaceAll = result.replaceAll("[\\n\\r]", "");
                    scanner.close();
                    return resultReplaceAll;
                }
            } else {
                // only unit testing
                return Files.readString(Paths.get("src/main/resources" + fileName));
            }
        } catch (IOException e) {
            logger.warn("no resource found for path {}", fileName);
        }
        return EMPTY_VALUE;
    }

    /**
     * Tibber Actions
     */
    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(TibberActions.class);
    }
}
