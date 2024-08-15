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
package org.openhab.binding.casokitchen.internal.handler;

import static org.openhab.binding.casokitchen.internal.CasoKitchenBindingConstants.*;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.casokitchen.internal.config.CasoKitchenConfiguration;
import org.openhab.binding.casokitchen.internal.dto.LightRequest;
import org.openhab.binding.casokitchen.internal.dto.StatusRequest;
import org.openhab.binding.casokitchen.internal.dto.StatusResult;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
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

/**
 * The {@link CasoKitchenHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class CasoKitchenHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(CasoKitchenHandler.class);

    private Optional<CasoKitchenConfiguration> configuration = Optional.empty();
    private Optional<StatusResult> cachedResult = Optional.empty();
    private @Nullable ScheduledFuture<?> refreshJob;
    private final HttpClient httpClient;
    private final TimeZoneProvider timeZoneProvider;

    public CasoKitchenHandler(Thing thing, HttpClient hc, TimeZoneProvider tzp) {
        super(thing);
        httpClient = hc;
        timeZoneProvider = tzp;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            cachedResult.ifPresent(result -> {
                // update channels from cached result if available
                String group = channelUID.getGroupId();
                if (group == null) {
                    return; // no channels without group defined!
                }
                String channel = channelUID.getIdWithoutGroup();
                switch (group) {
                    case GENERIC:
                        switch (channel) {
                            case LIGHT:
                                updateState(new ChannelUID(thing.getUID(), GENERIC, LIGHT),
                                        OnOffType.from(result.light1 && result.light2));
                                break;
                            case LAST_UPDATE:
                                Instant timestamp = Instant.parse(result.logTimestampUtc);
                                updateState(new ChannelUID(thing.getUID(), GENERIC, LAST_UPDATE),
                                        new DateTimeType(timestamp.atZone(timeZoneProvider.getTimeZone())));
                                break;
                            case HINT:
                                updateState(new ChannelUID(thing.getUID(), GENERIC, HINT),
                                        StringType.valueOf(result.hint));
                                break;
                        }
                        break;
                    case TOP:
                        switch (channel) {
                            case LIGHT:
                                updateState(new ChannelUID(thing.getUID(), TOP, LIGHT), OnOffType.from(result.light1));
                                break;
                            case POWER:
                                updateState(new ChannelUID(thing.getUID(), TOP, POWER), OnOffType.from(result.power1));
                                break;
                            case TEMPERATURE:
                                updateState(new ChannelUID(thing.getUID(), TOP, TEMPERATURE),
                                        QuantityType.valueOf(result.temperature1, SIUnits.CELSIUS));
                                break;
                            case TARGET_TEMPERATURE:
                                updateState(new ChannelUID(thing.getUID(), TOP, TARGET_TEMPERATURE),
                                        QuantityType.valueOf(result.targetTemperature1, SIUnits.CELSIUS));
                                break;
                        }
                        break;
                    case BOTTOM:
                        switch (channel) {
                            case LIGHT:
                                updateState(new ChannelUID(thing.getUID(), BOTTOM, LIGHT),
                                        OnOffType.from(result.light2));
                                break;
                            case POWER:
                                updateState(new ChannelUID(thing.getUID(), BOTTOM, POWER),
                                        OnOffType.from(result.power2));
                                break;
                            case TEMPERATURE:
                                updateState(new ChannelUID(thing.getUID(), BOTTOM, TEMPERATURE),
                                        QuantityType.valueOf(result.temperature2, SIUnits.CELSIUS));
                                break;
                            case TARGET_TEMPERATURE:
                                updateState(new ChannelUID(thing.getUID(), BOTTOM, TARGET_TEMPERATURE),
                                        QuantityType.valueOf(result.targetTemperature2, SIUnits.CELSIUS));
                                break;
                        }
                        break;
                }
            });
        }
        if (LIGHT.equals(channelUID.getIdWithoutGroup())) {
            logger.info("{} request received for group {}", LIGHT, channelUID.getGroupId());
            LightRequest lr = new LightRequest();
            lr.technicalDeviceId = configuration.get().deviceId;
            if (command instanceof OnOffType) {
                lr.lightOn = OnOffType.ON.equals(command);
                if (TOP.equals(channelUID.getGroupId())) {
                    lr.zone = 1;
                } else if (BOTTOM.equals(channelUID.getGroupId())) {
                    lr.zone = 2;
                } else if (GENERIC.equals(channelUID.getGroupId())) {
                    // light for all zones
                    lr.zone = 0;
                }
            }
            if (lr.isValid()) {
                Request req = httpClient.POST(LIGHT_URL);
                req.header(HttpHeader.CONTENT_TYPE, "application/json");
                req.header(HTTP_HEADER_API_KEY, configuration.get().apiKey);
                req.content(new StringContentProvider(GSON.toJson(lr)));
                try {
                    req.send();
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    logger.warn("Call to {} failed with reason {}", LIGHT_URL, e.getMessage());
                }
                // force data update
                dataUpdate();
            }
        } else {
            logger.info("Request {} doesn't fit", command);
        }
    }

    @Override
    public void initialize() {
        configuration = Optional.of(getConfigAs(CasoKitchenConfiguration.class));
        if (checkConfig(configuration.get())) {
            updateStatus(ThingStatus.UNKNOWN);
            startSchedule();
        }
    }

    private void startSchedule() {
        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob != null && configuration.isPresent()) {
            if (localRefreshJob.isCancelled()) {
                refreshJob = scheduler.scheduleWithFixedDelay(this::dataUpdate, 0, configuration.get().refreshInterval,
                        TimeUnit.MINUTES);
            } // else - scheduler is already running!
        } else {
            refreshJob = scheduler.scheduleWithFixedDelay(this::dataUpdate, 0, configuration.get().refreshInterval,
                    TimeUnit.MINUTES);
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> localRefreshJob = refreshJob;
        if (localRefreshJob != null) {
            localRefreshJob.cancel(true);
        }
    }

    /**
     * Checks if config is valid - a) not null and b) sensorid is a number
     *
     * @param c
     * @return
     */
    private boolean checkConfig(@Nullable CasoKitchenConfiguration c) {
        String reason = "Config Empty";
        if (c != null) {
            if (c.apiKey != EMPTY) {
                if (c.deviceId != EMPTY) {
                    if (c.refreshInterval > 0) {
                        updateStatus(ThingStatus.ONLINE);
                        return true;
                    } else {
                        reason = "@text/casokitchen.winecooler-2z.status.refresh-interval [\"" + c.refreshInterval
                                + "\"]";
                    }
                } else {
                    reason = "@text/casokitchen.winecooler-2z.status.device-id-missing";
                }
            } else {
                reason = "@text/casokitchen.winecooler-2z.status.api-key-missing";
            }
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, reason);
        return false;
    }

    private void dataUpdate() {
        StatusRequest requestContent = new StatusRequest(configuration.get().deviceId);
        Request req = httpClient.POST(STATUS_URL);
        req.header(HttpHeader.CONTENT_TYPE, "application/json");
        req.header(HTTP_HEADER_API_KEY, configuration.get().apiKey);
        req.content(new StringContentProvider(GSON.toJson(requestContent)));
        req.timeout(15, TimeUnit.SECONDS).send(new BufferingResponseListener() {
            @NonNullByDefault({})
            @Override
            public void onComplete(org.eclipse.jetty.client.api.Result result) {
                int responseStatus = result.getResponse().getStatus();
                String resultContent = getContentAsString();
                if (responseStatus != 200) {
                    logger.info("Request to {} failed. Status: {} Reason: {}", STATUS_URL, responseStatus,
                            resultContent);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/casokitchen.winecooler-2z.status.http-status [\"" + responseStatus + " - "
                                    + resultContent + "\"]");
                } else {
                    updateStatus(ThingStatus.ONLINE);
                    logger.info("Request to {} delivered {}", STATUS_URL, resultContent);
                    if (resultContent != null) {
                        StatusResult statusResult = GSON.fromJson(resultContent, StatusResult.class);
                        if (statusResult != null) {
                            cachedResult = Optional.of(statusResult);
                            updateChannels(statusResult);
                        }
                    }
                }
            }
        });
    }

    private void updateChannels(StatusResult result) {
        updateState(new ChannelUID(thing.getUID(), GENERIC, HINT), StringType.valueOf(result.hint));
        updateState(new ChannelUID(thing.getUID(), GENERIC, LIGHT), OnOffType.from(result.light1 && result.light2));
        updateState(new ChannelUID(thing.getUID(), TOP, TEMPERATURE),
                QuantityType.valueOf(result.temperature1, SIUnits.CELSIUS));
        updateState(new ChannelUID(thing.getUID(), TOP, TARGET_TEMPERATURE),
                QuantityType.valueOf(result.targetTemperature1, SIUnits.CELSIUS));
        updateState(new ChannelUID(thing.getUID(), TOP, POWER), OnOffType.from(result.power1));
        updateState(new ChannelUID(thing.getUID(), TOP, LIGHT), OnOffType.from(result.light1));
        updateState(new ChannelUID(thing.getUID(), BOTTOM, TEMPERATURE),
                QuantityType.valueOf(result.temperature2, SIUnits.CELSIUS));
        updateState(new ChannelUID(thing.getUID(), BOTTOM, TARGET_TEMPERATURE),
                QuantityType.valueOf(result.targetTemperature2, SIUnits.CELSIUS));
        updateState(new ChannelUID(thing.getUID(), BOTTOM, POWER), OnOffType.from(result.power2));
        updateState(new ChannelUID(thing.getUID(), BOTTOM, LIGHT), OnOffType.from(result.light2));

        ZonedDateTime zdt = Instant.parse(result.logTimestampUtc).atZone(timeZoneProvider.getTimeZone());
        updateState(new ChannelUID(thing.getUID(), GENERIC, LAST_UPDATE), new DateTimeType(zdt));
    }
}
