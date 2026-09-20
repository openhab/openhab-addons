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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
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
 * @author Ronny Grun - Monitoring API V2 support
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
    private final Object authorizationLock = new Object();
    private volatile String authorizationUrl = "";
    private final AtomicLong v2PollingCycle = new AtomicLong();
    private final PublicApiV2Data v2Data;

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
        this.v2Data = new PublicApiV2Data(this, this::updateChannelStatus,
                () -> getConfiguration().getBatteryCriticalLevel());
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
    public void updatePublicApiV2Production(long cycleId, Map<Channel, State> values, @Nullable Double production) {
        v2Data.updateProduction(cycleId, values, production);
    }

    @Override
    public void updatePublicApiV2Grid(long cycleId, Map<Channel, State> values, @Nullable Double imported,
            @Nullable Double exported, @Nullable Double consumption) {
        v2Data.updateGrid(cycleId, values, imported, exported, consumption);
    }

    @Override
    public void updatePublicApiV2Storage(long cycleId, Map<Channel, State> values, @Nullable Double charged,
            @Nullable Double discharged, @Nullable Double level) {
        v2Data.updateStorage(cycleId, values, charged, discharged, level);
    }

    @Override
    public void updatePublicApiV2AggregateProduction(long cycleId, AggregatePeriod period, Map<Channel, State> values,
            @Nullable Double production) {
        v2Data.updateAggregateProduction(cycleId, period, values, production);
    }

    @Override
    public void updatePublicApiV2AggregateGrid(long cycleId, AggregatePeriod period, Map<Channel, State> values,
            @Nullable Double imported, @Nullable Double exported, @Nullable Double consumption) {
        v2Data.updateAggregateGrid(cycleId, period, values, imported, exported, consumption);
    }

    @Override
    public void updatePublicApiV2AggregateStorage(long cycleId, AggregatePeriod period, Map<Channel, State> values,
            @Nullable Double charged, @Nullable Double discharged) {
        v2Data.updateAggregateStorage(cycleId, period, values, charged, discharged);
    }

    @Override
    public void initialize() {
        logger.debug("About to initialize SolarEdge");
        synchronized (authorizationLock) {
            setAuthorizationUrl("");
        }
        SolarEdgeConfiguration config = getConfiguration();
        logger.debug("SolarEdge initialized with configuration: {}", config);
        updatePublicApiV2RequestCountProperty(publicApiV2RequestCounter.getRequestCount());

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, STATUS_WAITING_FOR_LOGIN);
        if (isOAuthConfigured() && !oAuthClient.hasRefreshToken(config) && !config.getOAuthClientId().isBlank()
                && !config.getOAuthClientSecret().isBlank()) {
            synchronized (authorizationLock) {
                setAuthorizationUrl(oAuthServlet.register(this, config.getOAuthClientId()));
            }
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, authorizationDescription());
        } else {
            oAuthServlet.unregister(this);
            synchronized (authorizationLock) {
                setAuthorizationUrl("");
            }
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

        if (!getConfiguration().isUsePrivateApi()
                && PublicApiVersion.V2.equals(getConfiguration().getPublicApiVersion())) {
            updateJobReference(yearlyAggregateDataPollingJobReference,
                    scheduler.scheduleWithFixedDelay(this::yearlyAggregateDataPollingRun,
                            YEARLY_AGGREGATE_POLLING_INITIAL_DELAY, YEARLY_AGGREGATE_POLLING_INTERVAL,
                            TimeUnit.MINUTES));
        } else {
            cancelJobReference(yearlyAggregateDataPollingJobReference);
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
            long cycleId = v2PollingCycle.incrementAndGet();
            ldu = new LiveDataUpdatePublicApiV2(this, cycleId, this::updateOnlineStatus);
            getWebInterface().enqueueCommand(ldu);
            getWebInterface().enqueueCommand(
                    new LiveDeviceTelemetryUpdatePublicApiV2(this, cycleId, false, this::updateOnlineStatus));
            getWebInterface().enqueueCommand(
                    new LiveDeviceTelemetryUpdatePublicApiV2(this, cycleId, true, this::updateOnlineStatus));
            return;
        } else {
            if (getConfiguration().isMeterInstalled()) {
                ldu = new LiveDataUpdatePublicApi(this, this::updateOnlineStatus);
            } else {
                ldu = new LiveDataUpdateMeterless(this, this::updateOnlineStatus);
            }
        }
        getWebInterface().enqueueCommand(ldu);
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
                long cycleId = v2PollingCycle.incrementAndGet();
                commands.add(new AggregateDataUpdatePublicApiV2(this, cycleId, false, this::updateOnlineStatus));
                commands.add(new AggregateDeviceTelemetryUpdatePublicApiV2(this, cycleId, false, false,
                        this::updateOnlineStatus));
                commands.add(new AggregateDeviceTelemetryUpdatePublicApiV2(this, cycleId, true, false,
                        this::updateOnlineStatus));
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
        if (getConfiguration().isUsePrivateApi() || !hasPublicApiV2Credential()
                || !PublicApiVersion.V2.equals(getConfiguration().getPublicApiVersion())) {
            return;
        }
        logger.debug("polling SolarEdge yearly aggregate data {}", getConfiguration());
        long cycleId = v2PollingCycle.incrementAndGet();
        getWebInterface()
                .enqueueCommand(new AggregateDataUpdatePublicApiV2(this, cycleId, true, this::updateOnlineStatus));
        getWebInterface().enqueueCommand(
                new AggregateDeviceTelemetryUpdatePublicApiV2(this, cycleId, false, true, this::updateOnlineStatus));
        getWebInterface().enqueueCommand(
                new AggregateDeviceTelemetryUpdatePublicApiV2(this, cycleId, true, true, this::updateOnlineStatus));
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
        synchronized (authorizationLock) {
            setAuthorizationUrl("");
        }

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
                synchronized (authorizationLock) {
                    if (authorizationUrl.isBlank() && isOAuthConfigured()) {
                        setAuthorizationUrl(oAuthServlet.register(this, config.getOAuthClientId()));
                    }
                }
            }
            String description = e.isAuthorizationRequired() && !authorizationUrl.isBlank() ? authorizationDescription()
                    : e.getMessage();
            updateStatus(ThingStatus.OFFLINE, oauthFailureStatusDetail(e), description);
            return "";
        }
    }

    static ThingStatusDetail oauthFailureStatusDetail(SolarEdgeOAuthException e) {
        return e.isAuthorizationRequired() ? ThingStatusDetail.CONFIGURATION_PENDING
                : ThingStatusDetail.COMMUNICATION_ERROR;
    }

    @Override
    public boolean hasPublicApiV2Credential() {
        return !isOAuthConfigured() || oAuthClient.hasRefreshToken(getConfiguration());
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
            @Nullable String retryAfter, boolean rateLimited) {
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
        if (rateLimited) {
            webInterface.pausePublicApiV2Requests(retryAfter);
        }
    }

    private String displayHeader(@Nullable String value) {
        return value == null || value.isBlank() ? "<not provided>" : value;
    }

    public void onOAuthAuthorized(String code, String siteId) throws SolarEdgeOAuthException {
        String activeAuthorizationUrl;
        synchronized (authorizationLock) {
            activeAuthorizationUrl = authorizationUrl;
            if (!isOAuthConfigured() || activeAuthorizationUrl.isBlank()) {
                throw new SolarEdgeOAuthException("SolarEdge OAuth authorization is no longer configured");
            }
        }
        SolarEdgeConfiguration config = getConfiguration();
        if (!config.getSolarId().equals(siteId)) {
            throw new SolarEdgeOAuthException(
                    "Authorized site " + siteId + " does not match configured site " + config.getSolarId());
        }
        oAuthClient.exchangeAuthorizationCode(config, code, persistence -> {
            synchronized (authorizationLock) {
                if (!activeAuthorizationUrl.equals(authorizationUrl) || !isOAuthConfigured()
                        || !config.getSolarId().equals(getConfiguration().getSolarId())
                        || !config.getOAuthClientId().equals(getConfiguration().getOAuthClientId())
                        || !config.getOAuthClientSecret().equals(getConfiguration().getOAuthClientSecret())) {
                    throw new SolarEdgeOAuthException("SolarEdge OAuth authorization is no longer configured");
                }
                persistence.run();
            }
        });
        synchronized (authorizationLock) {
            if (!activeAuthorizationUrl.equals(authorizationUrl)) {
                throw new SolarEdgeOAuthException("SolarEdge OAuth authorization is no longer configured");
            }
            setAuthorizationUrl("");
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, STATUS_WAITING_FOR_LOGIN);
        }
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
