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
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.casokitchen.internal.config.TwoZonesWinecoolerConfiguration;
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
 * The {@link TwoZonesWinecoolerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TwoZonesWinecoolerHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TwoZonesWinecoolerHandler.class);

    private final TimeZoneProvider timeZoneProvider;
    private final HttpClient httpClient;
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private Optional<StatusResult> cachedResult = Optional.empty();
    private TwoZonesWinecoolerConfiguration configuration = new TwoZonesWinecoolerConfiguration();

    public TwoZonesWinecoolerHandler(Thing thing, HttpClient hc, TimeZoneProvider tzp) {
        super(thing);
        httpClient = hc;
        timeZoneProvider = tzp;
    }

    @Override
    public void initialize() {
        configuration = getConfigAs(TwoZonesWinecoolerConfiguration.class);
        if (checkConfig()) {
            startSchedule();
        }
    }

    private boolean checkConfig() {
        String reason = "Config Empty";
        if (configuration.apiKey != EMPTY) {
            if (configuration.deviceId != EMPTY) {
                if (configuration.refreshInterval > 0) {
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE,
                            "@text/casokitchen.winecooler-2z.status.wait-for-response");
                    return true;
                } else {
                    reason = "@text/casokitchen.winecooler-2z.status.refresh-interval [\""
                            + configuration.refreshInterval + "\"]";
                }
            } else {
                reason = "@text/casokitchen.winecooler-2z.status.device-id-missing";
            }
        } else {
            reason = "@text/casokitchen.winecooler-2z.status.api-key-missing";
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, reason);
        return false;
    }

    private void startSchedule() {
        refreshJob.ifPresent(job -> {
            job.cancel(false);
        });
        refreshJob = Optional.of(
                scheduler.scheduleWithFixedDelay(this::dataUpdate, 0, configuration.refreshInterval, TimeUnit.MINUTES));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String group = channelUID.getGroupId();
        if (group == null) {
            return; // no channels without group defined!
        }
        if (command instanceof RefreshType) {
            cachedResult.ifPresent(result -> {
                // update channels from cached result if available
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
                    case BOTTOM:
                        switch (channel) {
                            case LIGHT:
                                updateState(new ChannelUID(thing.getUID(), group, LIGHT),
                                        OnOffType.from(result.light2));
                                break;
                            case POWER:
                                updateState(new ChannelUID(thing.getUID(), group, POWER),
                                        OnOffType.from(result.power2));
                                break;
                            case TEMPERATURE:
                                updateState(new ChannelUID(thing.getUID(), group, TEMPERATURE),
                                        QuantityType.valueOf(result.temperature2, SIUnits.CELSIUS));
                                break;
                            case TARGET_TEMPERATURE:
                                updateState(new ChannelUID(thing.getUID(), group, TARGET_TEMPERATURE),
                                        QuantityType.valueOf(result.targetTemperature2, SIUnits.CELSIUS));
                                break;
                        }
                        break;
                }
            });
        } else if (LIGHT.equals(channelUID.getIdWithoutGroup())) {
            LightRequest lr = new LightRequest();
            lr.technicalDeviceId = configuration.deviceId;
            if (command instanceof OnOffType) {
                lr.lightOn = OnOffType.ON.equals(command);
                switch (group) {
                    case GENERIC:
                        lr.zone = 0;
                        break;
                    case TOP:
                        lr.zone = 1;
                        break;
                    case BOTTOM:
                        lr.zone = 2;
                        break;
                }
                logger.info("Send {} with command {}", LIGHT_URL, GSON.toJson(lr));
                Request req = httpClient.POST(LIGHT_URL);
                req.header(HttpHeader.CONTENT_TYPE, "application/json");
                req.header(HTTP_HEADER_API_KEY, configuration.apiKey);
                req.content(new StringContentProvider(GSON.toJson(lr)));
                try {
                    ContentResponse response = req.timeout(60, TimeUnit.SECONDS).send();
                    logger.debug("Call to {} responded with status {} reason {}", LIGHT_URL, response.getStatus(),
                            response.getReason());
                } catch (InterruptedException | TimeoutException | ExecutionException e) {
                    logger.debug("Call to {} failed with reason {}", LIGHT_URL, e.getMessage());
                }
                // force data update
                scheduler.schedule(this::dataUpdate, 2, TimeUnit.SECONDS);
            }
        } else {
            logger.debug("Cannot handle command {}", command);
        }
    }

    @Override
    public void dispose() {
        refreshJob.ifPresent(job -> {
            job.cancel(true);
        });
    }

    private void dataUpdate() {
        StatusRequest requestContent = new StatusRequest(configuration.deviceId);
        Request req = httpClient.POST(STATUS_URL);
        req.header(HttpHeader.CONTENT_TYPE, "application/json");
        req.header(HTTP_HEADER_API_KEY, configuration.apiKey);
        req.content(new StringContentProvider(GSON.toJson(requestContent)));
        try {
            ContentResponse contentResponse = req.timeout(60, TimeUnit.SECONDS).send();
            int responseStatus = contentResponse.getStatus();
            String contentResult = contentResponse.getContentAsString();
            if (responseStatus != 200) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/casokitchen.winecooler-2z.status.http-status [\"" + responseStatus + " - "
                                + contentResult + "\"]");
            } else {
                updateStatus(ThingStatus.ONLINE);
                StatusResult statusResult = GSON.fromJson(contentResult, StatusResult.class);
                if (statusResult != null) {
                    updateChannels(statusResult);
                }
            }
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/casokitchen.winecooler-2z.status.http-status [\"" + e.getMessage() + "\"]");
        }
    }

    private void updateChannels(StatusResult result) {
        cachedResult = Optional.of(result);
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
