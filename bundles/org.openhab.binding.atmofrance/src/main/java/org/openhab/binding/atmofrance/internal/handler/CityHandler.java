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
package org.openhab.binding.atmofrance.internal.handler;

import static org.openhab.binding.atmofrance.internal.AtmoFranceBindingConstants.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atmofrance.internal.api.dto.AtmoFranceDto.IndexProperties;
import org.openhab.binding.atmofrance.internal.api.dto.AtmoFranceDto.PollensProperties;
import org.openhab.binding.atmofrance.internal.api.dto.Taxon;
import org.openhab.binding.atmofrance.internal.configuration.CityConfiguration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CityHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class CityHandler extends BaseThingHandler implements HandlerUtils {
    private static final String AQ_JOB = "%s Air Quality";
    private static final String POLLENS_JOB = "%s Pollens";

    private final Logger logger = LoggerFactory.getLogger(CityHandler.class);
    private final Map<String, ScheduledFuture<?>> jobs = new HashMap<>();

    private @Nullable CityConfiguration config;

    public CityHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(CityConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);
        schedule(AQ_JOB.formatted(config.codeInsee), this::getAtmoIndex, Duration.ofSeconds(2));
        schedule(POLLENS_JOB.formatted(config.codeInsee), this::getPollens, Duration.ofSeconds(3));
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Atmo France city handler");
        cleanJobs();
    }

    private void getAtmoIndex() {
        AtmoFranceApiHandler apiHandler = getBridgeHandler(AtmoFranceApiHandler.class);
        CityConfiguration local = config;
        long delay = 3600;
        if (apiHandler != null && local != null) {
            IndexProperties properties = apiHandler.getAtmoIndex(local.codeInsee);
            if (properties != null) {
                updateState(CHANNEL_TIMESTAMP, new DateTimeType(properties.dateMaj));
                updateState(CHANNEL_DATE_ECH, new DateTimeType(properties.dateEch));
                updateState(CHANNEL_DATE_DIF, new DateTimeType(properties.dateDif));
                updateState(CHANNEL_COMMENT, new StringType(properties.libQual));
                updateState(CHANNEL_INDEX, new DecimalType(properties.codeQual.value));
                updateState(CHANNEL_INDEX_NO2, new DecimalType(properties.no2.value));
                updateState(CHANNEL_INDEX_SO2, new DecimalType(properties.so2.value));
                updateState(CHANNEL_INDEX_O3, new DecimalType(properties.o3.value));
                updateState(CHANNEL_INDEX_PM10, new DecimalType(properties.pm10.value));
                updateState(CHANNEL_INDEX_PM25, new DecimalType(properties.pm25.value));
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "No data provided !!");
            }

        } else {
            delay = 10;
        }
        schedule(AQ_JOB.formatted(local.codeInsee), this::getAtmoIndex, Duration.ofSeconds(delay));
    }

    private void getPollens() {
        AtmoFranceApiHandler apiHandler = getBridgeHandler(AtmoFranceApiHandler.class);
        CityConfiguration local = config;
        long delay = 3600;
        if (apiHandler != null && local != null) {
            PollensProperties properties = apiHandler.getPollens(local.codeInsee);
            if (properties != null) {
                updateState(CHANNEL_ALDER_LVL, new DecimalType(properties.getTaxon(Taxon.ALDER).value));
                updateState(CHANNEL_BIRCH_LVL, new DecimalType(properties.getTaxon(Taxon.BIRCH).value));
                updateState(CHANNEL_OLIVE_LVL, new DecimalType(properties.getTaxon(Taxon.OLIVE).value));
                updateState(CHANNEL_GRASSES_LVL, new DecimalType(properties.getTaxon(Taxon.GRASSES).value));
                updateState(CHANNEL_WORMWOOD_LVL, new DecimalType(properties.getTaxon(Taxon.WORMWOOD).value));
                updateState(CHANNEL_RAGWEED_LVL, new DecimalType(properties.getTaxon(Taxon.RAGWEED).value));
                // updateState(CHANNEL_TIMESTAMP, new DateTimeType(properties.dateMaj));
                // updateState(CHANNEL_DATE_ECH, new DateTimeType(properties.dateEch));
                // updateState(CHANNEL_DATE_DIF, new DateTimeType(properties.dateDif));
                // updateState(CHANNEL_COMMENT, new StringType(properties.libQual));
                // updateState(CHANNEL_INDEX, new DecimalType(properties.codeQual.value));
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "No data provided !!");
            }

        } else {
            delay = 10;
        }
        schedule(POLLENS_JOB.formatted(local.codeInsee), this::getAtmoIndex, Duration.ofSeconds(delay));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("This thing does not handle commands");
    }

    @Override
    public @Nullable Bridge getBridge() {
        return super.getBridge();
    }

    @Override
    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public Map<String, ScheduledFuture<?>> getJobs() {
        return jobs;
    }
}
