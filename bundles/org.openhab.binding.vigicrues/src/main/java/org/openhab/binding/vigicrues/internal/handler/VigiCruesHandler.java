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

import static org.openhab.binding.vigicrues.internal.VigiCruesBindingConstants.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.apache.commons.io.IOUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.vigicrues.internal.VigiCruesConfiguration;
import org.openhab.binding.vigicrues.internal.json.OpenDatasoftResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import tec.uom.se.unit.Units;

/**
 * The {@link VigiCruesHandler} is responsible for updating channels
 * and querying the API
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class VigiCruesHandler extends BaseThingHandler {
    private static final String URL = OPENDATASOFT_URL + "?dataset=vigicrues&sort=timestamp&q=";
    private final Logger logger = LoggerFactory.getLogger(VigiCruesHandler.class);
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class, (JsonDeserializer<ZonedDateTime>) (json, type,
                    jsonDeserializationContext) -> ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString()))
            .create();

    // Time zone provider representing time zone configured in openHAB configuration
    private final TimeZoneProvider timeZoneProvider;

    private @NonNullByDefault({}) ScheduledFuture<?> refreshJob;
    private @NonNullByDefault({}) String queryUrl;

    public VigiCruesHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing VigiCrues handler.");

        VigiCruesConfiguration config = getConfigAs(VigiCruesConfiguration.class);
        logger.debug("config station = {}", config.id);
        logger.debug("config refresh = {} mn", config.refresh);

        updateStatus(ThingStatus.UNKNOWN);
        queryUrl = URL + config.id;
        refreshJob = scheduler.scheduleWithFixedDelay(this::updateAndPublish, 0, config.refresh, TimeUnit.MINUTES);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the VigiCrues handler.");

        if (refreshJob != null && !refreshJob.isCancelled()) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateAndPublish();
        } else {
            logger.debug("The VigiCrues binding is read-only and can not handle command {}", command);
        }
    }

    private void updateAndPublish() {
        try {
            URL url = new URL(queryUrl);
            try {
                URLConnection connection = url.openConnection();
                String response = IOUtils.toString(connection.getInputStream());
                IOUtils.closeQuietly(connection.getInputStream());
                updateStatus(ThingStatus.ONLINE);
                OpenDatasoftResponse apiResponse = gson.fromJson(response, OpenDatasoftResponse.class);
                Arrays.stream(apiResponse.getRecords()).findFirst().ifPresent(record -> {
                    record.getFields().ifPresent(field -> {
                        field.getHauteur().ifPresent(height -> updateQuantity(HEIGHT, height, Units.METRE));
                        field.getDebit()
                                .ifPresent(flow -> updateQuantity(FLOW, flow, SmartHomeUnits.CUBICMETRE_PER_SECOND));
                        field.getTimestamp().ifPresent(date -> updateDate(OBSERVATION_TIME, date));
                    });
                });
            } catch (IOException e) {
                logger.warn("Error opening connection to VigiCrues webservice : {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } catch (MalformedURLException e) {
            logger.error("Malformed URL in VigiCrues request : {}", queryUrl);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
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
