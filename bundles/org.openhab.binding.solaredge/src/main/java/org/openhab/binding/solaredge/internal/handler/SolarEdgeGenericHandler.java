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
package org.openhab.binding.solaredge.internal.handler;

import static org.openhab.binding.solaredge.internal.SolarEdgeBindingConstants.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.solaredge.internal.AtomicReferenceTrait;
import org.openhab.binding.solaredge.internal.command.AggregateDataUpdatePrivateApi;
import org.openhab.binding.solaredge.internal.command.AggregateDataUpdatePublicApi;
import org.openhab.binding.solaredge.internal.command.AggregateDataUpdatePublicApiV2;
import org.openhab.binding.solaredge.internal.command.AggregateDeviceTelemetryUpdatePublicApiV2;
import org.openhab.binding.solaredge.internal.command.LiveDataUpdateMeterless;
import org.openhab.binding.solaredge.internal.command.LiveDataUpdatePrivateApi;
import org.openhab.binding.solaredge.internal.command.LiveDataUpdatePublicApi;
import org.openhab.binding.solaredge.internal.command.LiveDataUpdatePublicApiV2;
import org.openhab.binding.solaredge.internal.command.LiveDeviceTelemetryUpdatePublicApiV2;
import org.openhab.binding.solaredge.internal.command.SolarEdgeCommand;
import org.openhab.binding.solaredge.internal.config.PublicApiAuthentication;
import org.openhab.binding.solaredge.internal.config.PublicApiVersion;
import org.openhab.binding.solaredge.internal.config.SolarEdgeConfiguration;
import org.openhab.binding.solaredge.internal.connector.CommunicationStatus;
import org.openhab.binding.solaredge.internal.connector.PublicApiV2RequestCounter;
import org.openhab.binding.solaredge.internal.connector.WebInterface;
import org.openhab.binding.solaredge.internal.model.AggregatePeriod;
import org.openhab.binding.solaredge.internal.oauth.SolarEdgeOAuthClient;
import org.openhab.binding.solaredge.internal.oauth.SolarEdgeOAuthException;
import org.openhab.binding.solaredge.internal.oauth.SolarEdgeOAuthServlet;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SolarEdgeGenericHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class SolarEdgeGenericHandler extends BaseThingHandler implements SolarEdgeHandler, AtomicReferenceTrait {
    private final Logger logger = LoggerFactory.getLogger(SolarEdgeGenericHandler.class);

    private static final long LIVE_POLLING_INITIAL_DELAY = 1;
    private static final long AGGREGATE_POLLING_INITIAL_DELAY = 2;
    private static final long YEARLY_AGGREGATE_POLLING_INITIAL_DELAY = 3;
    private static final long YEARLY_AGGREGATE_POLLING_INTERVAL = TimeUnit.DAYS.toMinutes(1);

    /**
     * Interface object for querying the Solaredge web interface
     */
    private WebInterface webInterface;
    private final SolarEdgeOAuthClient oAuthClient;
    private final PublicApiV2RequestCounter publicApiV2RequestCounter;
    private final SolarEdgeOAuthServlet oAuthServlet;
    private String authorizationUrl = "";
    private static final Duration V2_BALANCE_MAX_AGE = Duration.ofMinutes(2);
    private @Nullable TimedPower v2Production;
    private @Nullable TimedPower v2Import;
    private @Nullable TimedPower v2Export;
    private @Nullable TimedPower v2Charge;
    private @Nullable TimedPower v2Discharge;
    private final Map<AggregatePeriod, AggregateBalance> v2AggregateBalances = new EnumMap<>(AggregatePeriod.class);

    private record TimedPower(double value, Instant timestamp) {
    }

    private static class AggregateBalance {
        private @Nullable TimedPower production;
        private @Nullable TimedPower imported;
        private @Nullable TimedPower exported;
        private @Nullable TimedPower charged;
        private @Nullable TimedPower discharged;
    }

    /**
     * Schedule for polling live data
     */
    private final AtomicReference<@Nullable Future<?>> liveDataPollingJobReference;

    /**
     * Schedule for polling aggregate data
     */
    private final AtomicReference<@Nullable Future<?>> aggregateDataPollingJobReference;

    /** Schedule for polling yearly Monitoring API V2 aggregate data. */
    private final AtomicReference<@Nullable Future<?>> yearlyAggregateDataPollingJobReference;

    public SolarEdgeGenericHandler(Thing thing, HttpClient httpClient, SolarEdgeOAuthClient oAuthClient,
            PublicApiV2RequestCounter publicApiV2RequestCounter, SolarEdgeOAuthServlet oAuthServlet) {
        super(thing);
        this.webInterface = new WebInterface(scheduler, this, httpClient);
        this.oAuthClient = oAuthClient;
        this.publicApiV2RequestCounter = publicApiV2RequestCounter;
        this.oAuthServlet = oAuthServlet;
        this.liveDataPollingJobReference = new AtomicReference<>(null);
        this.aggregateDataPollingJobReference = new AtomicReference<>(null);
        this.yearlyAggregateDataPollingJobReference = new AtomicReference<>(null);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("command for {}: {}", channelUID, command);
        // write access is not supported.
    }

    @Override
    public synchronized void updatePublicApiV2Production(@Nullable Double production) {
        v2Production = timed(production);
        updatePublicApiV2LiveValues();
    }

    @Override
    public synchronized void updatePublicApiV2Grid(@Nullable Double imported, @Nullable Double exported) {
        v2Import = timed(imported);
        v2Export = timed(exported);
        updatePublicApiV2LiveValues();
    }

    @Override
    public synchronized void updatePublicApiV2Storage(@Nullable Double charged, @Nullable Double discharged,
            @Nullable Double level) {
        v2Charge = timed(charged);
        v2Discharge = timed(discharged);
        Map<Channel, State> values = new HashMap<>();
        putState(values, CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_CRITICAL,
                batteryCriticalState(level, getConfiguration().getBatteryCriticalLevel()));
        updateChannelStatus(values);
        updatePublicApiV2LiveValues();
    }

    @Override
    public synchronized void updatePublicApiV2AggregateProduction(AggregatePeriod period, @Nullable Double production) {
        AggregateBalance balance = aggregateBalance(period);
        balance.production = timed(production);
        updatePublicApiV2AggregateConsumption(period, balance);
    }

    @Override
    public synchronized void updatePublicApiV2AggregateGrid(AggregatePeriod period, @Nullable Double imported,
            @Nullable Double exported) {
        AggregateBalance balance = aggregateBalance(period);
        balance.imported = timed(imported);
        balance.exported = timed(exported);
        updatePublicApiV2AggregateConsumption(period, balance);
    }

    @Override
    public synchronized void updatePublicApiV2AggregateStorage(AggregatePeriod period, @Nullable Double charged,
            @Nullable Double discharged) {
        AggregateBalance balance = aggregateBalance(period);
        balance.charged = timed(charged);
        balance.discharged = timed(discharged);
        updatePublicApiV2AggregateConsumption(period, balance);
    }

    private AggregateBalance aggregateBalance(AggregatePeriod period) {
        AggregateBalance balance = v2AggregateBalances.get(period);
        if (balance == null) {
            balance = new AggregateBalance();
            v2AggregateBalances.put(period, balance);
        }
        return balance;
    }

    private void updatePublicApiV2AggregateConsumption(AggregatePeriod period, AggregateBalance balance) {
        Instant oldestAllowed = Instant.now().minus(V2_BALANCE_MAX_AGE);
        if (isCurrent(balance.production, oldestAllowed) && isCurrent(balance.imported, oldestAllowed)
                && isCurrent(balance.exported, oldestAllowed) && isCurrent(balance.charged, oldestAllowed)
                && isCurrent(balance.discharged, oldestAllowed)) {
            double consumption = calculateConsumption(valueOf(balance.production), valueOf(balance.imported),
                    valueOf(balance.exported), valueOf(balance.charged), valueOf(balance.discharged));
            double selfConsumption = calculateSelfConsumption(valueOf(balance.production), valueOf(balance.exported),
                    valueOf(balance.charged));
            Map<Channel, State> values = new HashMap<>();
            putState(values, aggregateGroup(period), CHANNEL_ID_CONSUMPTION,
                    new QuantityType<>(consumption, Units.WATT_HOUR));
            putState(values, aggregateGroup(period), CHANNEL_ID_SELF_CONSUMPTION_FOR_CONSUMPTION,
                    new QuantityType<>(selfConsumption, Units.WATT_HOUR));
            putState(values, aggregateGroup(period), CHANNEL_ID_SELF_CONSUMPTION_COVERAGE,
                    new QuantityType<>(calculateCoverage(selfConsumption, consumption), Units.PERCENT));
            updateChannelStatus(values);
        }
    }

    private @Nullable TimedPower timed(@Nullable Double value) {
        return value == null ? null : new TimedPower(value, Instant.now());
    }

    private void updatePublicApiV2LiveValues() {
        Instant oldestAllowed = Instant.now().minus(V2_BALANCE_MAX_AGE);
        TimedPower production = v2Production;
        TimedPower imported = v2Import;
        TimedPower exported = v2Export;
        TimedPower charge = v2Charge;
        TimedPower discharge = v2Discharge;
        if (isCurrent(production, oldestAllowed) && isCurrent(imported, oldestAllowed)
                && isCurrent(exported, oldestAllowed) && isCurrent(charge, oldestAllowed)
                && isCurrent(discharge, oldestAllowed)) {
            double consumption = calculateConsumption(valueOf(production), valueOf(imported), valueOf(exported),
                    valueOf(charge), valueOf(discharge));
            Map<Channel, State> values = new HashMap<>();
            putState(values, CHANNEL_GROUP_LIVE, CHANNEL_ID_CONSUMPTION, new QuantityType<>(consumption, Units.WATT));
            putState(values, CHANNEL_GROUP_LIVE, CHANNEL_ID_PV_STATUS,
                    new StringType(activeStatus(valueOf(production))));
            putState(values, CHANNEL_GROUP_LIVE, CHANNEL_ID_LOAD_STATUS, new StringType(activeStatus(consumption)));
            putState(values, CHANNEL_GROUP_LIVE, CHANNEL_ID_GRID_STATUS,
                    new StringType(activeStatus(valueOf(imported), valueOf(exported))));
            putState(values, CHANNEL_GROUP_LIVE, CHANNEL_ID_BATTERY_STATUS,
                    new StringType(activeStatus(valueOf(charge), valueOf(discharge))));
            updateChannelStatus(values);
        }
    }

    private void putState(Map<Channel, State> values, String group, String channelId, State state) {
        Channel channel = getChannel(group, channelId);
        if (channel != null) {
            values.put(channel, state);
        }
    }

    private boolean isCurrent(@Nullable TimedPower power, Instant oldestAllowed) {
        return power != null && !power.timestamp.isBefore(oldestAllowed);
    }

    private double valueOf(@Nullable TimedPower power) {
        return Objects.requireNonNull(power).value;
    }

    static double calculateConsumption(double production, double imported, double exported, double charged,
            double discharged) {
        return Math.max(0, production + imported + discharged - exported - charged);
    }

    static double calculateSelfConsumption(double production, double exported, double charged) {
        return Math.max(0, production - exported - charged);
    }

    static double calculateCoverage(double selfConsumption, double consumption) {
        return consumption > 0 ? selfConsumption / consumption * 100 : 0;
    }

    static String activeStatus(double... powers) {
        for (double power : powers) {
            if (power > 0) {
                return "Active";
            }
        }
        return "Idle";
    }

    static State batteryCriticalState(@Nullable Double level, int threshold) {
        return level == null ? UnDefType.UNDEF : new StringType(Boolean.toString(level < threshold));
    }

    private String aggregateGroup(AggregatePeriod period) {
        return switch (period) {
            case DAY -> CHANNEL_GROUP_AGGREGATE_DAY;
            case WEEK -> CHANNEL_GROUP_AGGREGATE_WEEK;
            case MONTH -> CHANNEL_GROUP_AGGREGATE_MONTH;
            case YEAR -> CHANNEL_GROUP_AGGREGATE_YEAR;
        };
    }

    @Override
    public void initialize() {
        logger.debug("About to initialize SolarEdge");
        SolarEdgeConfiguration config = getConfiguration();
        logger.debug("SolarEdge initialized with configuration: {}", config);
        updatePublicApiV2RequestCountProperty(publicApiV2RequestCounter.getRequestCount());

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, STATUS_WAITING_FOR_LOGIN);
        if (isOAuthConfigured() && !oAuthClient.hasRefreshToken() && !config.getOAuthClientId().isBlank()
                && !config.getOAuthClientSecret().isBlank()) {
            setAuthorizationUrl(oAuthServlet.register(this, config.getOAuthClientId()));
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, authorizationDescription());
        } else {
            setAuthorizationUrl("");
        }
        webInterface.start();
        startPolling();
    }

    /**
     * Start the polling.
     */
    private void startPolling() {
        updateJobReference(liveDataPollingJobReference, scheduler.scheduleWithFixedDelay(this::liveDataPollingRun,
                LIVE_POLLING_INITIAL_DELAY, getConfiguration().getLiveDataPollingInterval(), TimeUnit.MINUTES));

        updateJobReference(aggregateDataPollingJobReference,
                scheduler.scheduleWithFixedDelay(this::aggregateDataPollingRun, AGGREGATE_POLLING_INITIAL_DELAY,
                        getConfiguration().getAggregateDataPollingInterval(), TimeUnit.MINUTES));

        if (PublicApiVersion.V2.equals(getConfiguration().getPublicApiVersion())) {
            updateJobReference(yearlyAggregateDataPollingJobReference,
                    scheduler.scheduleWithFixedDelay(this::yearlyAggregateDataPollingRun,
                            YEARLY_AGGREGATE_POLLING_INITIAL_DELAY, YEARLY_AGGREGATE_POLLING_INTERVAL,
                            TimeUnit.MINUTES));
        }
    }

    /**
     * Poll the SolarEdge Webservice one time per call to retrieve live data.
     */
    void liveDataPollingRun() {
        if (!hasPublicApiV2Credential()) {
            return;
        }
        logger.debug("polling SolarEdge live data {}", getConfiguration());
        SolarEdgeCommand ldu;

        if (getConfiguration().isUsePrivateApi()) {
            ldu = new LiveDataUpdatePrivateApi(this, this::updateOnlineStatus);
        } else if (PublicApiVersion.V2.equals(getConfiguration().getPublicApiVersion())) {
            ldu = new LiveDataUpdatePublicApiV2(this, this::updateOnlineStatus);
        } else {
            if (getConfiguration().isMeterInstalled()) {
                ldu = new LiveDataUpdatePublicApi(this, this::updateOnlineStatus);
            } else {
                ldu = new LiveDataUpdateMeterless(this, this::updateOnlineStatus);
            }
        }
        getWebInterface().enqueueCommand(ldu);
        if (PublicApiVersion.V2.equals(getConfiguration().getPublicApiVersion())) {
            getWebInterface()
                    .enqueueCommand(new LiveDeviceTelemetryUpdatePublicApiV2(this, false, this::updateOnlineStatus));
            getWebInterface()
                    .enqueueCommand(new LiveDeviceTelemetryUpdatePublicApiV2(this, true, this::updateOnlineStatus));
        }
    }

    /**
     * Poll the SolarEdge Webservice one time per call to retrieve aggregate data.
     */
    void aggregateDataPollingRun() {
        if (!hasPublicApiV2Credential()) {
            return;
        }
        // V1 meterless aggregate data is part of the overview response. V2 exposes it through the energy endpoint.
        if (getConfiguration().isMeterInstalled()
                || PublicApiVersion.V2.equals(getConfiguration().getPublicApiVersion())) {
            logger.debug("polling SolarEdge aggregate data {}", getConfiguration());
            List<SolarEdgeCommand> commands = new ArrayList<>();

            if (getConfiguration().isUsePrivateApi()) {
                commands.add(new AggregateDataUpdatePrivateApi(this, AggregatePeriod.DAY, this::updateOnlineStatus));
                commands.add(new AggregateDataUpdatePrivateApi(this, AggregatePeriod.WEEK, this::updateOnlineStatus));
                commands.add(new AggregateDataUpdatePrivateApi(this, AggregatePeriod.MONTH, this::updateOnlineStatus));
                commands.add(new AggregateDataUpdatePrivateApi(this, AggregatePeriod.YEAR, this::updateOnlineStatus));
            } else if (PublicApiVersion.V2.equals(getConfiguration().getPublicApiVersion())) {
                commands.add(new AggregateDataUpdatePublicApiV2(this, false, this::updateOnlineStatus));
                commands.add(
                        new AggregateDeviceTelemetryUpdatePublicApiV2(this, false, false, this::updateOnlineStatus));
                commands.add(
                        new AggregateDeviceTelemetryUpdatePublicApiV2(this, true, false, this::updateOnlineStatus));
            } else {
                commands.add(new AggregateDataUpdatePublicApi(this, AggregatePeriod.DAY, this::updateOnlineStatus));
                commands.add(new AggregateDataUpdatePublicApi(this, AggregatePeriod.WEEK, this::updateOnlineStatus));
                commands.add(new AggregateDataUpdatePublicApi(this, AggregatePeriod.MONTH, this::updateOnlineStatus));
                commands.add(new AggregateDataUpdatePublicApi(this, AggregatePeriod.YEAR, this::updateOnlineStatus));
            }

            for (SolarEdgeCommand command : commands) {
                getWebInterface().enqueueCommand(command);
            }
        }
    }

    /** Poll yearly Monitoring API V2 aggregate data once per day. */
    void yearlyAggregateDataPollingRun() {
        if (!hasPublicApiV2Credential() || !PublicApiVersion.V2.equals(getConfiguration().getPublicApiVersion())) {
            return;
        }
        logger.debug("polling SolarEdge yearly aggregate data {}", getConfiguration());
        getWebInterface().enqueueCommand(new AggregateDataUpdatePublicApiV2(this, true, this::updateOnlineStatus));
        getWebInterface().enqueueCommand(
                new AggregateDeviceTelemetryUpdatePublicApiV2(this, false, true, this::updateOnlineStatus));
        getWebInterface().enqueueCommand(
                new AggregateDeviceTelemetryUpdatePublicApiV2(this, true, true, this::updateOnlineStatus));
    }

    private void updateOnlineStatus(CommunicationStatus status) {
        String detailMessage = status.getUserFacingMessage();
        switch (status.getHttpCode()) {
            case SERVICE_UNAVAILABLE:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, detailMessage);
                break;
            case OK:
                updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                break;
            default:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, detailMessage);
        }
    }

    /**
     * Disposes the bridge.
     */
    @Override
    public void dispose() {
        logger.debug("Handler disposed.");

        cancelJobReference(liveDataPollingJobReference);
        cancelJobReference(aggregateDataPollingJobReference);
        cancelJobReference(yearlyAggregateDataPollingJobReference);

        webInterface.dispose();
        oAuthServlet.unregister(this);
    }

    @Override
    public WebInterface getWebInterface() {
        return webInterface;
    }

    /**
     * will update all channels provided in the map
     */
    @Override
    public void updateChannelStatus(Map<Channel, State> values) {
        logger.debug("Handling channel update.");

        for (Channel channel : values.keySet()) {
            if (getChannels().contains(channel)) {
                State value = values.get(channel);
                if (value != null) {
                    logger.debug("Channel is to be updated: {}: {}", channel.getUID().getAsString(), value);
                    updateState(channel.getUID(), value);
                } else {
                    logger.debug("Value is null or not provided by solaredge (channel: {})",
                            channel.getUID().getAsString());
                    updateState(channel.getUID(), UnDefType.UNDEF);
                }
            } else {
                logger.debug("Could not identify channel: {} for model {}", channel.getUID().getAsString(),
                        getThing().getThingTypeUID().getAsString());
            }
        }
    }

    @Override
    public void setStatusInfo(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    public SolarEdgeConfiguration getConfiguration() {
        return this.getConfigAs(SolarEdgeConfiguration.class);
    }

    @Override
    public String getPublicApiV2Credential() {
        SolarEdgeConfiguration config = getConfiguration();
        if (!isOAuthConfigured()) {
            return config.getTokenOrApiKey();
        }
        try {
            return oAuthClient.getAccessToken(config);
        } catch (SolarEdgeOAuthException e) {
            logger.debug("Unable to obtain SolarEdge OAuth access token: {}", e.getMessage());
            if (e.isAuthorizationRequired() && authorizationUrl.isBlank() && !config.getOAuthClientId().isBlank()) {
                setAuthorizationUrl(oAuthServlet.register(this, config.getOAuthClientId()));
            }
            String description = authorizationUrl.isBlank() ? e.getMessage() : authorizationDescription();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, description);
            return "";
        }
    }

    @Override
    public boolean hasPublicApiV2Credential() {
        return !isOAuthConfigured() || oAuthClient.hasRefreshToken();
    }

    @Override
    public void invalidatePublicApiV2Credential() {
        if (isOAuthConfigured()) {
            oAuthClient.invalidateAccessToken();
        }
    }

    @Override
    public void recordPublicApiV2Request() {
        updatePublicApiV2RequestCountProperty(publicApiV2RequestCounter.recordRequest());
    }

    private void updatePublicApiV2RequestCountProperty(int count) {
        getThing().setProperty(PROPERTY_API_CALLS_LAST_30_DAYS, Integer.toString(count));
    }

    @Override
    public void updatePublicApiV2RateLimit(@Nullable String limit, @Nullable String remaining,
            @Nullable String retryAfter) {
        logger.debug("SolarEdge API rate limit: minute={}, remaining={}, retryAfter={}", displayHeader(limit),
                displayHeader(remaining), displayHeader(retryAfter));
        if (limit != null && !limit.isBlank()) {
            getThing().setProperty(PROPERTY_API_RATE_LIMIT_MINUTE, limit);
        }
        if (remaining != null && !remaining.isBlank()) {
            getThing().setProperty(PROPERTY_API_RATE_LIMIT_REMAINING_MINUTE, remaining);
        }
        getThing().setProperty(PROPERTY_API_RATE_LIMIT_RETRY_AFTER,
                retryAfter == null || retryAfter.isBlank() ? null : retryAfter);
    }

    private String displayHeader(@Nullable String value) {
        return value == null || value.isBlank() ? "<not provided>" : value;
    }

    public void onOAuthAuthorized(String code, String siteId) throws SolarEdgeOAuthException {
        SolarEdgeConfiguration config = getConfiguration();
        if (!config.getSolarId().equals(siteId)) {
            throw new SolarEdgeOAuthException(
                    "Authorized site " + siteId + " does not match configured site " + config.getSolarId());
        }
        oAuthClient.exchangeAuthorizationCode(config, code);
        setAuthorizationUrl("");
        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, STATUS_WAITING_FOR_LOGIN);
    }

    private void setAuthorizationUrl(String url) {
        authorizationUrl = url;
        getThing().setProperty(PROPERTY_OAUTH_AUTHORIZATION_URL, url.isBlank() ? null : url);
    }

    private String authorizationDescription() {
        return "SolarEdge authorization required: <a class=\"external\" href=\""
                + authorizationUrl.replace("&", "&amp;")
                + "\" target=\"_blank\" rel=\"noopener noreferrer\">Click here to authorize access</a>";
    }

    private boolean isOAuthConfigured() {
        SolarEdgeConfiguration config = getConfiguration();
        return !config.isUsePrivateApi() && PublicApiVersion.V2.equals(config.getPublicApiVersion())
                && PublicApiAuthentication.OAUTH.equals(config.getPublicApiAuthentication());
    }

    @Override
    public List<Channel> getChannels() {
        return getThing().getChannels();
    }

    @Override
    public @Nullable Channel getChannel(String groupId, String channelId) {
        ThingUID thingUID = this.getThing().getUID();
        ChannelGroupUID channelGroupUID = new ChannelGroupUID(thingUID, groupId);
        return getThing().getChannel(new ChannelUID(channelGroupUID, channelId));
    }
}
