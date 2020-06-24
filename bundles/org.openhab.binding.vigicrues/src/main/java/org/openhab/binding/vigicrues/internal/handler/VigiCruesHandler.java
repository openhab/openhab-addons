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
package org.openhab.binding.vigicrues.internal.handler;

import static org.eclipse.smarthome.core.library.unit.SmartHomeUnits.CUBICMETRE_PER_SECOND;
import static org.openhab.binding.vigicrues.internal.VigiCruesBindingConstants.*;
import static tec.uom.se.unit.Units.METRE;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.vigicrues.internal.VigiCruesConfiguration;
import org.openhab.binding.vigicrues.internal.json.OpenDatasoftResponse;
import org.openhab.binding.vigicrues.internal.json.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link VigiCruesHandler} is responsible for querying the API and
 * updating channels
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VigiCruesHandler extends BaseThingHandler {
    private static final String URL = OPENDATASOFT_URL + "?dataset=vigicrues&sort=timestamp&q=";
    private static final int TIMEOUT_MS = 30000;
    private final Logger logger = LoggerFactory.getLogger(VigiCruesHandler.class);

    // Time zone provider representing time zone configured in openHAB configuration
    private final TimeZoneProvider timeZoneProvider;
    private final Gson gson;
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable String queryUrl;

    public VigiCruesHandler(Thing thing, TimeZoneProvider timeZoneProvider, Gson gson) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
        this.gson = gson;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing VigiCrues handler.");

        VigiCruesConfiguration config = getConfigAs(VigiCruesConfiguration.class);
        logger.debug("config station = {}", config.id);
        logger.debug("config refresh = {} min", config.refresh);

        updateStatus(ThingStatus.UNKNOWN);
        queryUrl = URL + config.id;
        refreshJob = scheduler.scheduleWithFixedDelay(this::updateAndPublish, 0, config.refresh, TimeUnit.MINUTES);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the VigiCrues handler.");

        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
        this.refreshJob = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateAndPublish();
        }
    }

    private void updateAndPublish() {
        try {
            if (queryUrl != null) {
                String response = HttpUtil.executeUrl("GET", queryUrl, TIMEOUT_MS);
                updateStatus(ThingStatus.ONLINE);
                OpenDatasoftResponse apiResponse = gson.fromJson(response, OpenDatasoftResponse.class);
                Arrays.stream(apiResponse.getRecords()).findFirst().flatMap(Record::getFields).ifPresent(field -> {
                    field.getHeight().ifPresent(height -> updateQuantity(HEIGHT, height, METRE));
                    field.getFlow().ifPresent(flow -> updateQuantity(FLOW, flow, CUBICMETRE_PER_SECOND));
                    field.getTimestamp().ifPresent(date -> updateDate(OBSERVATION_TIME, date));
                });
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.DISABLED,
                        "queryUrl should never be null, but it is !");
            }
        } catch (MalformedURLException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    String.format("Querying '%s' raised : %s", queryUrl, e.getMessage()));
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    String.format("Error opening connection to VigiCrues webservice : {}", e.getMessage()));
        }
    }

    private void updateQuantity(String channelId, Double value, Unit<?> unit) {
        if (isLinked(channelId)) {
            updateState(channelId, new QuantityType<>(value, unit));
        }
    }

    public void updateDate(String channelId, ZonedDateTime zonedDateTime) {
        if (isLinked(channelId)) {
            ZonedDateTime localDateTime = zonedDateTime.withZoneSameInstant(timeZoneProvider.getTimeZone());
            updateState(channelId, new DateTimeType(localDateTime));
        }
    }

}
