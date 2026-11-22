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
package org.openhab.binding.tibber.internal.handler;

import static org.openhab.binding.tibber.internal.TibberBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
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
import org.openhab.binding.tibber.internal.history.TibberHistory;
import org.openhab.binding.tibber.internal.history.TibberHistoryAggregator;
import org.openhab.binding.tibber.internal.history.TibberHistoryCurrentPeriods;
import org.openhab.binding.tibber.internal.history.TibberHistoryCurrentPeriods.Values;
import org.openhab.binding.tibber.internal.history.TibberHistoryListener;
import org.openhab.binding.tibber.internal.history.TibberHistorySeries;
import org.openhab.binding.tibber.internal.websocket.TibberWebsocket;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.CurrencyUnits;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.scheduler.ScheduledCompletableFuture;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
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
 * @author Bernd Weymann - Rework initialization
 * @author Bernd Weymann - Add history support
 * @author Bernd Weymann - Add history channel group
 * @author Bernd Weymann - Add stale history refresh and live history values
 */
@NonNullByDefault
public class TibberHandler extends BaseThingHandler implements TibberHistoryListener {
    private static final int REQUEST_TIMEOUT_SEC = 10;
    /** Maximum number of hourly stale-history fetch attempts per window and day before backing off. */
    private static final int MAX_STALE_HISTORY_ATTEMPTS = 6;
    private static final Duration STALE_HISTORY_RETRY_INTERVAL = Duration.ofHours(6);
    /** Minimum interval between two live updates of the history channels. */
    private static final Duration HISTORY_LIVE_UPDATE_INTERVAL = Duration.ofMinutes(1);

    private final Logger logger = LoggerFactory.getLogger(TibberHandler.class);
    private final Random random = new Random();
    private final BundleContext bundleContext;
    private final HttpClient httpClient;
    private final CronScheduler cron;
    private final Map<String, String> templates = new HashMap<>();
    private final TimeZoneProvider timeZoneProvider;
    private final StorageService storageService;

    private final ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();
    private TibberConfiguration tibberConfig = new TibberConfiguration();
    private @Nullable ScheduledFuture<?> watchdog;
    private @Nullable ScheduledCompletableFuture<?> cronDaily;
    private @Nullable ScheduledCompletableFuture<?> cronHistory;
    private @Nullable TibberWebsocket webSocket;
    private @Nullable Boolean realtimeEnabled;
    private @Nullable String currencyUnit;
    private Object calculatorLock = new Object();
    private int retryCounter = 0;
    private boolean isDisposed = true;
    private boolean historyStarted;
    protected @Nullable TibberHistory history;

    private final Map<TibberHistory.TimeWindow, Integer> staleHistoryAttempts = new EnumMap<>(
            TibberHistory.TimeWindow.class);
    private final Map<TibberHistory.TimeWindow, Instant> staleHistoryLastAttempt = new EnumMap<>(
            TibberHistory.TimeWindow.class);
    private LocalDate staleHistoryAttemptsDate = LocalDate.MIN;
    private volatile @Nullable Values currentPeriodValues;
    private volatile Instant lastHistoryLiveUpdate = Instant.MIN;

    private TimeSeries priceCache = new TimeSeries(TimeSeries.Policy.REPLACE);
    private TimeSeries energyCache = new TimeSeries(TimeSeries.Policy.REPLACE);
    private TimeSeries taxCache = new TimeSeries(TimeSeries.Policy.REPLACE);
    private TimeSeries levelCache = new TimeSeries(TimeSeries.Policy.REPLACE);
    private TimeSeries averageCache = new TimeSeries(TimeSeries.Policy.REPLACE);

    protected @Nullable PriceCalculator calculator;

    public TibberHandler(Thing thing, HttpClient httpClient, CronScheduler cron, BundleContext bundleContext,
            TimeZoneProvider timeZoneProvider, StorageService storageService) {
        super(thing);
        this.httpClient = httpClient;
        this.cron = cron;
        this.bundleContext = bundleContext;
        this.timeZoneProvider = timeZoneProvider;
        this.storageService = storageService;
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
            } else if (CHANNEL_GROUP_HISTORY.equals(group)) {
                TibberHistory localHistory = this.history;
                if (localHistory != null) {
                    switch (channelUID.getIdWithoutGroup()) {
                        case CHANNEL_YEARLY_CONSUMPTION:
                        case CHANNEL_YEARLY_COST:
                        case CHANNEL_YEARLY_PRODUCTION:
                            localHistory.updateHistory(TibberHistory.TimeWindow.ANNUAL, false);
                            break;
                        case CHANNEL_MONTHLY_CONSUMPTION:
                        case CHANNEL_MONTHLY_COST:
                        case CHANNEL_MONTHLY_PRODUCTION:
                            localHistory.updateHistory(TibberHistory.TimeWindow.MONTHLY, false);
                            break;
                        case CHANNEL_WEEKLY_CONSUMPTION:
                        case CHANNEL_WEEKLY_COST:
                        case CHANNEL_WEEKLY_PRODUCTION:
                            localHistory.updateHistory(TibberHistory.TimeWindow.WEEKLY, false);
                            break;
                        case CHANNEL_HISTORY_DAILY_CONSUMPTION:
                        case CHANNEL_HISTORY_DAILY_COST:
                        case CHANNEL_HISTORY_DAILY_PRODUCTION:
                            localHistory.updateHistory(TibberHistory.TimeWindow.DAILY, false);
                            break;
                        default:
                            logger.debug("Unhandled history channel: {}", channelUID.getIdWithoutGroup());
                    }
                }
            }
        }
    }

    /**
     * Creates the {@link TibberHistory} instance used by this handler.
     * Called from {@link #initialize()} so that StorageService injection has already completed.
     * Overridden in unit tests to inject a history stub.
     */
    protected TibberHistory createHistory() {
        return new TibberHistory(storageService, tibberConfig.homeid, this);
    }

    @Override
    public void initialize() {
        isDisposed = false;
        tibberConfig = getConfigAs(TibberConfiguration.class);
        if (tibberConfig.homeid.isBlank() || tibberConfig.token.isBlank()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/status.configuration-error");
        } else {
            // Create TibberHistory here (not in constructor) to ensure StorageService OSGi injection is complete
            TibberHistory history = createHistory();
            this.history = history;
            history.dispose(false); // mark as active (default state is disposed=true)
            historyStarted = false;
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

        ScheduledCompletableFuture<?> cronHistory = this.cronHistory;
        if (cronHistory != null) {
            cronHistory.cancel(true);
        }
        this.cronHistory = null;

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

        TibberHistory history = this.history;
        if (history != null) {
            history.dispose(true);
        }
        this.history = null;

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
        if (isDisposed) {
            logger.trace("doInitialize rejected due to disposed thing");
            return;
        }
        Request initializeRequest = getRequest();
        String body = String.format(QUERY_CONTAINER,
                String.format(getTemplate(CURRENCY_QUERY_RESOURCE_PATH), tibberConfig.homeid));
        logger.trace("Query with body {}", body);
        initializeRequest.content(new StringContentProvider(body, "utf-8"));
        try {
            ContentResponse cr = initializeRequest.send();
            int responseStatus = cr.getStatus();
            String initialResponse = cr.getContentAsString();
            logger.trace("doInitialze response {} - {}", responseStatus, initialResponse);
            if (responseStatus == HttpStatus.OK_200) {
                JsonObject jsonResponse = (JsonObject) JsonParser.parseString(initialResponse);
                if (jsonResponse.has("errors")) {
                    // case: error
                    // if response is ok_200 and an error occurs homeid or token is wrong so raise configuration error
                    updateStatus(ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/status.initial-call-failed  [\"" + responseStatus + " - " + initialResponse + "\"]");
                } else {
                    JsonObject initalJson = Utils.getJsonObject(jsonResponse, INITIAL_QUERY_JSON_PATH);
                    if (initalJson.isEmpty()) {
                        handleInitializeError(responseStatus + " - " + initialResponse, false);
                    } else {
                        if (initalJson.get("currentSubscription").isJsonNull()) {
                            initializePulseUser();
                        } else {
                            JsonObject currentJson = Utils.getJsonObject(jsonResponse, CURRENCY_QUERY_JSON_PATH);
                            if (!currentJson.isEmpty()) {
                                initializeTibberCustomer(currentJson);
                            } else {
                                handleInitializeError(responseStatus + " - " + initialResponse, false);
                            }
                        }
                    }
                }
            } else {
                handleInitializeError(responseStatus + " - " + initialResponse, true);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            handleInitializeError(e.getMessage(), true);
        }
    }

    private void initializeTibberCustomer(JsonObject initalJson) {
        managePriceChannels(true);
        updateStatus(ThingStatus.ONLINE);

        // check if currency is supported
        String currencyCode = initalJson.get("currency").getAsString();
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

        // An existing channel link is not replayed to a handler which is initialized after the link
        // (re-)appeared, so history updates must also start from here and not only from channelLinked().
        startHistoryProcessing();
    }

    private void initializePulseUser() {
        managePriceChannels(false);
        updateStatus(ThingStatus.ONLINE);
        // create websocket and watchdog
        webSocket = new TibberWebsocket(this, tibberConfig, httpClient);
        watchdog = scheduler.scheduleWithFixedDelay(this::watchdog, 0, 1, TimeUnit.MINUTES);

        // An existing channel link is not replayed to a handler which is initialized after the link
        // (re-)appeared, so history updates must also start from here and not only from channelLinked().
        startHistoryProcessing();
    }

    private void handleInitializeError(@Nullable String message, boolean retry) {
        updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, "@text/status.initial-call-failed  [\"" + message + "\"]");
        if (retry) {
            scheduler.schedule(this::doInitialize, 60, TimeUnit.SECONDS);
        }
    }

    protected void managePriceChannels(boolean add) {
        boolean hasPriceChannels = thing
                .getChannel(new ChannelUID(thing.getUID(), CHANNEL_GROUP_PRICE, CHANNEL_SPOT_PRICE)) != null;
        logger.trace("Add channels? {}. Has spot price channel? {}", add, hasPriceChannels);
        if ((add && hasPriceChannels) || (!add && !hasPriceChannels)) {
            // don't add channel if already available OR remove channel if not available
            return;
        }
        // Collect channels to add/remove
        List<Channel> channels = new ArrayList<>();
        PRICE_COST_CHANNELS.forEach((channelId, channelType) -> {
            Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), channelId), CoreItemFactory.NUMBER)
                    .withType(new ChannelTypeUID(BINDING_ID, channelType)).build();
            logger.trace("Add/Remove channel {} ", channel.getUID().getAsString());
            channels.add(channel);
        });
        // update thing with / without channels
        ThingBuilder thingBuilder = editThing();
        if (add) {
            for (Channel channel : channels) {
                thingBuilder = thingBuilder.withChannel(channel);
            }
            updateThing(thingBuilder.build());
        } else {
            updateThing(thingBuilder.withoutChannels(channels).build());
        }
    }

    private void watchdog() {
        TibberWebsocket webSocket = this.webSocket;
        if (realtimeChannelsLinked() && isRealtimeEnabled()) {
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
                logger.trace("Websocket is kept offline - either feature disabled ({}) or channel not linked ({})",
                        isRealtimeEnabled(), realtimeChannelsLinked());
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
                        boolean updateTrigger = false;
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
                                updateTrigger = true;
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
                        if (updateTrigger) {
                            triggerChannel(new ChannelUID(thing.getUID(), CHANNEL_GROUP_PRICE, CHANNEL_EVENT),
                                    EVENT_DAY_AHEAD_AVAILABLE);
                        }
                        updateStatus(ThingStatus.ONLINE);
                    } catch (JsonSyntaxException | DateTimeParseException e) {
                        updateStatus(ThingStatusDetail.COMMUNICATION_ERROR,
                                "@text/status.price-update-failed  [\"" + e.getMessage() + "\"]");
                    }
                }
            } else if (jsonResponse.contains("error")) {
                updatePriceInfoRetry();
                updateStatus(ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/status.price-update-failed  [\"" + responseStatus + "\"]");
            } else {
                updatePriceInfoRetry();
                updateStatus(ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/status.price-update-failed  [\"" + jsonResponse + "\"]");
            }
        } catch (TimeoutException | ExecutionException e) {
            updatePriceInfoRetry();
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/status.price-update-failed  [\"" + e.getMessage() + "\"]");
        } catch (InterruptedException e1) {
            updateStatus(ThingStatusDetail.COMMUNICATION_ERROR,
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
        Boolean localRealtimeEnabled = realtimeEnabled;
        if (localRealtimeEnabled != null) {
            return localRealtimeEnabled.booleanValue();
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
                    localRealtimeEnabled = Boolean.valueOf(rtEnabled);
                    realtimeEnabled = localRealtimeEnabled;
                    return localRealtimeEnabled.booleanValue();
                }
            } catch (JsonSyntaxException | InterruptedException | TimeoutException | ExecutionException e) {
                logger.warn("Realtime feature query failed {}", e.getMessage());
            }
        }
        return false;
    }

    /**
     * Returns true when at least one channel which requires the live websocket is linked: live,
     * statistics or history. A history channel uses the live measurement to include the running day.
     */
    private boolean realtimeChannelsLinked() {
        return getThing().getChannels().stream().map(Channel::getUID)
                .filter(uid -> CHANNEL_GROUP_LIVE.equals(uid.getGroupId())
                        || CHANNEL_GROUP_STATISTICS.equals(uid.getGroupId())
                        || CHANNEL_GROUP_HISTORY.equals(uid.getGroupId()))
                .anyMatch(this::isLinked);
    }

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

            updateHistoryLiveValues(jsonData);
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

    /**
     * Adds the live values of the running day (received via the websocket) to the completed part of the
     * current week/month/year and publishes the result to the history channels.
     * <p>
     * The completed part is the sum of the stored DAILY entries strictly before today
     * ({@link TibberHistoryCurrentPeriods}); the live values are the Tibber accumulators since midnight.
     * The result is independent of the forecast restore state because {@code updateState} is used directly.
     * Updates are throttled and skipped while the history store does not contain yesterday yet, so the
     * displayed value never drops at midnight before the daily history fetch has completed.
     */
    private void updateHistoryLiveValues(JsonObject liveMeasurement) {
        Values currentValues = this.currentPeriodValues;
        if (isDisposed || currentValues == null) {
            return;
        }
        Instant now = Instant.now();
        if (!currentValues.isFresh(timeZoneProvider.getTimeZone(), now)) {
            return;
        }
        if (now.isBefore(lastHistoryLiveUpdate.plus(HISTORY_LIVE_UPDATE_INTERVAL))) {
            return;
        }
        State todayConsumption = getLiveState(liveMeasurement, "accumulatedConsumption", "kWh");
        State todayCost = getLiveCostState(Utils.getJsonValue(liveMeasurement, "accumulatedCost"));
        State todayProduction = getLiveState(liveMeasurement, "accumulatedProduction", "kWh");
        if (todayConsumption == null && todayCost == null && todayProduction == null) {
            return;
        }
        lastHistoryLiveUpdate = now;
        for (TibberHistory.TimeWindow window : List.of(TibberHistory.TimeWindow.WEEKLY,
                TibberHistory.TimeWindow.MONTHLY, TibberHistory.TimeWindow.ANNUAL)) {
            updateHistoryLiveChannel(window, TibberHistorySeries.PURPOSE_CONSUMPTION, todayConsumption, currentValues);
            updateHistoryLiveChannel(window, TibberHistorySeries.PURPOSE_COST, todayCost, currentValues);
            updateHistoryLiveChannel(window, TibberHistorySeries.PURPOSE_PRODUCTION, todayProduction, currentValues);
        }
    }

    private void updateHistoryLiveChannel(TibberHistory.TimeWindow window, String purpose, @Nullable State today,
            Values currentValues) {
        if (today == null) {
            return;
        }
        State completed = currentValues.get(window, purpose);
        State state = completed == null ? today : TibberHistoryCurrentPeriods.add(completed, today);
        if (state != null) {
            updateState(new ChannelUID(thing.getUID(), CHANNEL_GROUP_HISTORY, window.channelPrefix() + "-" + purpose),
                    state);
        }
    }

    private @Nullable State getLiveState(JsonObject liveMeasurement, String key, String unit) {
        String value = Utils.getJsonValue(liveMeasurement, key);
        if (EMPTY_VALUE.equals(value) || NULL_VALUE.equals(value)) {
            return null;
        }
        return QuantityType.valueOf(value + " " + unit);
    }

    private @Nullable State getLiveCostState(String value) {
        if (EMPTY_VALUE.equals(value) || NULL_VALUE.equals(value)) {
            return null;
        }
        String currencyUnit = this.currencyUnit;
        return currencyUnit != null ? QuantityType.valueOf(value + " " + currencyUnit) : DecimalType.valueOf(value);
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
     * {@link TibberHistoryListener} implementation.
     * Called by {@link TibberHistory} when a time window has been fetched.
     * When {@code series} is null the fetch is still in progress (status update only).
     * When {@code series} is non-null the result is sent to all three history channels of the window,
     * followed by a bottom-up aggregation pass to fill the current (incomplete) period gap.
     */
    @Override
    public void historyUpdated(TibberHistory.HistoryRequest request, @Nullable TibberHistorySeries series) {
        TibberHistory.TimeWindow window = request.window();
        if (series == null) {
            // Fetch started — update status to show which window is loading
            String description = "@text/status.history-update [\"" + window.name() + "\"]";
            ThingStatusInfo statusInfo = thing.getStatusInfo();
            ThingStatus status = statusInfo.getStatus();
            if (status == ThingStatus.ONLINE || status == ThingStatus.OFFLINE || status == ThingStatus.UNKNOWN) {
                ThingStatusDetail detail = statusInfo.getStatusDetail();
                if (detail == ThingStatusDetail.NONE) {
                    updateStatus(status, description);
                } else {
                    updateStatus(detail, description);
                }
            }
            return;
        }

        Instant start = Instant.MIN;
        if (!request.fullUpdate()) {
            start = Instant.now().minus(window.daysInWindow(), ChronoUnit.DAYS);
        }

        TibberHistory localHistory = this.history;
        if (localHistory == null) {
            logger.warn("historyUpdated called but history is null (disposed?)");
            return;
        }

        sendHistorySeries(localHistory, window, start);

        // After sending raw data: fill current-period gap bottom-up (non-blocking)
        scheduler.execute(() -> enrichCurrentPeriods(localHistory));

        // Restore status (clear the "loading" message)
        ThingStatusInfo statusInfo = thing.getStatusInfo();
        ThingStatus status = statusInfo.getStatus();
        if (status == ThingStatus.ONLINE || status == ThingStatus.OFFLINE || status == ThingStatus.UNKNOWN) {
            updateStatus(status, statusInfo.getStatusDetail());
        }
        logger.info("History update for {} completed", window.name());
    }

    /**
     * Sends the three history channels (consumption, cost, production) for the given window
     * from the persistent store, filtered to entries at or after {@code start}.
     */
    private void sendHistorySeries(TibberHistory history, TibberHistory.TimeWindow window, Instant start) {
        TibberHistorySeries stored = history.getStoredSeries(window);

        sendHistoryChannel(window, TibberHistorySeries.PURPOSE_CONSUMPTION,
                stored.getTimeSeries(start, TibberHistorySeries.PURPOSE_CONSUMPTION));
        sendHistoryChannel(window, TibberHistorySeries.PURPOSE_COST,
                stored.getTimeSeries(start, TibberHistorySeries.PURPOSE_COST));
        sendHistoryChannel(window, TibberHistorySeries.PURPOSE_PRODUCTION,
                stored.getTimeSeries(start, TibberHistorySeries.PURPOSE_PRODUCTION));
    }

    /**
     * Performs a bottom-up aggregation pass to inject synthetic "current period" entries
     * into each coarser resolution that would otherwise have a gap for the running period:
     * <ol>
     * <li>DAILY → WEEKLY (current ISO week so far)</li>
     * <li>DAILY → MONTHLY (current month so far)</li>
     * <li>MONTHLY → ANNUAL (current year so far, including the synthetic current month)</li>
     * </ol>
     * The current month is aggregated from DAILY data because WEEKLY entries are dated at the start of
     * a completed ISO week and therefore only rarely fall into the running month.
     * Synthetic entries are transient — they are never written to the Tibber history store.
     */
    private void enrichCurrentPeriods(TibberHistory localHistory) {
        if (isDisposed) {
            return;
        }
        TibberHistoryAggregator agg = new TibberHistoryAggregator(timeZoneProvider.getTimeZone());

        TibberHistorySeries daily = localHistory.getStoredSeries(TibberHistory.TimeWindow.DAILY);
        TibberHistorySeries weekly = localHistory.getStoredSeries(TibberHistory.TimeWindow.WEEKLY);
        TibberHistorySeries monthly = localHistory.getStoredSeries(TibberHistory.TimeWindow.MONTHLY);
        TibberHistorySeries annual = localHistory.getStoredSeries(TibberHistory.TimeWindow.ANNUAL);

        // cache the completed part of the running periods for the live updates of the history channels
        currentPeriodValues = TibberHistoryCurrentPeriods.compute(daily, timeZoneProvider.getTimeZone(), Instant.now());

        sendEnrichedSeries(
                agg.fillCurrentPeriod(TibberHistory.TimeWindow.DAILY, TibberHistory.TimeWindow.WEEKLY, daily, weekly),
                weekly, TibberHistory.TimeWindow.WEEKLY);
        TibberHistorySeries enrichedMonthly = agg.fillCurrentPeriod(TibberHistory.TimeWindow.DAILY,
                TibberHistory.TimeWindow.MONTHLY, daily, monthly);
        sendEnrichedSeries(enrichedMonthly, monthly, TibberHistory.TimeWindow.MONTHLY);
        sendEnrichedSeries(agg.fillCurrentPeriod(TibberHistory.TimeWindow.MONTHLY, TibberHistory.TimeWindow.ANNUAL,
                enrichedMonthly, annual), annual, TibberHistory.TimeWindow.ANNUAL);
    }

    /**
     * Sends all three history channels for the given window from an enriched (in-memory) series.
     * Unlike {@link #sendHistorySeries}, this uses the provided series directly rather than
     * reading from the persistent store, so that synthetic aggregation entries are included.
     * Nothing is sent if the aggregator did not add an entry.
     */
    private void sendEnrichedSeries(TibberHistorySeries enriched, TibberHistorySeries stored,
            TibberHistory.TimeWindow window) {
        if (enriched.equals(stored)) {
            return;
        }
        // REPLACE policy ensures the channels receive the complete enriched series
        sendHistoryChannel(window, TibberHistorySeries.PURPOSE_CONSUMPTION,
                enriched.getTimeSeries(Instant.MIN, TibberHistorySeries.PURPOSE_CONSUMPTION));
        sendHistoryChannel(window, TibberHistorySeries.PURPOSE_COST,
                enriched.getTimeSeries(Instant.MIN, TibberHistorySeries.PURPOSE_COST));
        sendHistoryChannel(window, TibberHistorySeries.PURPOSE_PRODUCTION,
                enriched.getTimeSeries(Instant.MIN, TibberHistorySeries.PURPOSE_PRODUCTION));
    }

    /**
     * Sends the time series of one history channel and publishes its newest past entry as item state.
     * <p>
     * openHAB core applies past entries of a {@link TimeSeries} to the item state only if the entry
     * timestamp is newer than the item's last state update (see {@code PersistenceManagerImpl}). History
     * entries are always dated at the start of their period, so they are silently ignored for items whose
     * state was already restored. The explicit {@link #updateState(ChannelUID, State)} makes the channel
     * show the current (or most recently completed) period regardless of the restore state.
     */
    private void sendHistoryChannel(TibberHistory.TimeWindow window, String purpose, TimeSeries series) {
        if (series.size() == 0) {
            return;
        }
        ChannelUID channelUID = new ChannelUID(thing.getUID(), CHANNEL_GROUP_HISTORY,
                window.channelPrefix() + "-" + purpose);
        sendTimeSeries(channelUID, series);
        logger.debug("History {} {} series size: {}", window, purpose, series.size());
        Instant now = Instant.now();
        series.getStates().filter(entry -> !entry.timestamp().isAfter(now)).reduce((first, second) -> second)
                .ifPresent(entry -> updateState(channelUID, entry.state()));
    }

    // -------------------------------------------------------------------------
    // History: channel linking lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
        if (CHANNEL_GROUP_HISTORY.equals(channelUID.getGroupId())) {
            logger.debug("History channel linked: {} — starting history updates", channelUID);
            startHistoryProcessing();
        }
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        super.channelUnlinked(channelUID);
        if (CHANNEL_GROUP_HISTORY.equals(channelUID.getGroupId()) && !historyChannelsLinked()) {
            logger.debug("Last history channel unlinked — stopping history cron");
            stopHistoryCron();
        }
    }

    /**
     * Returns true when at least one channel in the history group is linked to an item.
     */
    private boolean historyChannelsLinked() {
        return getThing().getChannels().stream().map(Channel::getUID)
                .filter(uid -> CHANNEL_GROUP_HISTORY.equals(uid.getGroupId())).anyMatch(this::isLinked);
    }

    /**
     * Starts the daily history refresh and the initial history load once per handler lifetime.
     * <p>
     * This is called from {@link #channelLinked(ChannelUID)} and after a successful initialization.
     * The latter is required because openHAB core only replays link notifications to handlers which are
     * already initialized at that moment ({@code ChannelLinkNotifier}); a handler initialized afterwards
     * never receives {@code channelLinked} for its existing links and would therefore neither fetch the
     * history nor start the daily refresh.
     */
    private synchronized void startHistoryProcessing() {
        if (isDisposed || !historyChannelsLinked()) {
            return;
        }
        startHistoryCron();
        if (!historyStarted) {
            historyStarted = true;
            // On first start: full paginated fetch to load complete history.
            // On subsequent starts (store already populated): partial fetch is sufficient.
            scheduler.execute(this::initialHistoryLoad);
        }
    }

    /**
     * Starts the hourly history refresh if not already running.
     * Only schedules if at least one history channel is linked.
     */
    private void startHistoryCron() {
        if (this.cronHistory != null || !historyChannelsLinked()) {
            return;
        }
        // Check every hour whether a completed period is missing; a fetch is only issued if needed
        cronHistory = cron.schedule(this::refreshStaleHistory, "0 5 * * * ? *");
        logger.debug("History refresh cron scheduled");
    }

    /**
     * Stops and clears the history cron job.
     */
    private void stopHistoryCron() {
        ScheduledCompletableFuture<?> cronHistory = this.cronHistory;
        if (cronHistory != null) {
            cronHistory.cancel(true);
        }
        this.cronHistory = null;
        logger.debug("History cron stopped");
    }

    /**
     * Called once per handler lifetime when the history updates are started.
     * Issues a full update (paginated) for windows whose persistent store is still empty,
     * so the complete available history is fetched. Windows that already have stored data
     * get a partial update (most recent page only) instead.
     */
    private void initialHistoryLoad() {
        if (isDisposed || !historyChannelsLinked()) {
            return;
        }
        TibberHistory localHistory = this.history;
        if (localHistory == null) {
            logger.debug("initialHistoryLoad: history not yet initialised");
            return;
        }
        for (TibberHistory.TimeWindow window : TibberHistory.TimeWindow.values()) {
            boolean hasStoredData = !localHistory.getStoredSeries(window).isEmpty();
            boolean fullUpdate = !hasStoredData;
            logger.debug("initialHistoryLoad: {} → {}", window, fullUpdate ? "fullUpdate" : "partialUpdate");
            localHistory.updateHistory(window, fullUpdate);
        }
    }

    /**
     * Triggered hourly by the history cron.
     * <p>
     * Requests an update only for those windows whose stored series does not yet contain the newest period the
     * Tibber API can have completed (see {@link TibberHistory.TimeWindow#lastCompletedPeriodStart(ZonedDateTime)}).
     * On a normal day this causes no API request at all, while a missed or too early daily fetch (API lag, restart,
     * communication error) is retried automatically. A window whose store is still empty is retried once per day
     * with a full fetch, so a failed initial load recovers. Retries per window are capped and then continue at a
     * reduced interval to keep API load bounded.
     */
    protected void refreshStaleHistory() {
        if (isDisposed || !historyChannelsLinked()) {
            return;
        }
        TibberHistory localHistory = this.history;
        if (localHistory == null) {
            return;
        }
        ZonedDateTime now = ZonedDateTime.now(timeZoneProvider.getTimeZone());
        resetStaleHistoryAttemptsOnNewDay(now.toLocalDate());
        for (TibberHistory.TimeWindow window : TibberHistory.TimeWindow.values()) {
            TibberHistorySeries stored = localHistory.getStoredSeries(window);
            if (stored.isEmpty()) {
                // Retry a failed initial load once per day. A permanently empty window (for example DAILY data
                // for a non-pulse user) must not cause hourly requests.
                if (shouldRetryStaleHistory(window, now.toInstant(), 1)) {
                    logger.debug("No stored {} history, retrying full fetch", window);
                    localHistory.updateHistory(window, true);
                }
                continue;
            }
            Instant expected = window.lastCompletedPeriodStart(now);
            if (!stored.lastKey().isBefore(expected)) {
                continue;
            }
            if (!shouldRetryStaleHistory(window, now.toInstant(), MAX_STALE_HISTORY_ATTEMPTS)) {
                continue;
            }
            logger.debug("Stored {} history is stale (last entry {}, expected {}), requesting partial update", window,
                    stored.lastKey(), expected);
            localHistory.updateHistory(window, false);
        }
    }

    private void resetStaleHistoryAttemptsOnNewDay(LocalDate today) {
        synchronized (staleHistoryAttempts) {
            if (!today.equals(staleHistoryAttemptsDate)) {
                staleHistoryAttempts.clear();
                staleHistoryLastAttempt.clear();
                staleHistoryAttemptsDate = today;
            }
        }
    }

    private boolean shouldRetryStaleHistory(TibberHistory.TimeWindow window, Instant now, int maxAttempts) {
        synchronized (staleHistoryAttempts) {
            int attempts = staleHistoryAttempts.getOrDefault(window, 0);
            Instant lastAttempt = staleHistoryLastAttempt.get(window);
            if (attempts >= maxAttempts && lastAttempt != null
                    && now.isBefore(lastAttempt.plus(STALE_HISTORY_RETRY_INTERVAL))) {
                return false;
            }
            staleHistoryAttempts.put(window, attempts + 1);
            staleHistoryLastAttempt.put(window, now);
            return true;
        }
    }

    /**
     * Explicitly fetches (full update) a single time window.
     * Called by the {@code fetchHistory} ThingAction.
     *
     * @param window the time window to fetch
     */
    public void fetchHistory(TibberHistory.TimeWindow window) {
        TibberHistory localHistory = this.history;
        if (localHistory == null) {
            logger.warn("fetchHistory called but history is not initialised (Thing OFFLINE?)");
            return;
        }
        logger.info("ThingAction fetchHistory triggered for window {}", window);
        localHistory.updateHistory(window, true);
    }

    // -------------------------------------------------------------------------
    // Tibber Actions
    // -------------------------------------------------------------------------

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(TibberActions.class);
    }
}
