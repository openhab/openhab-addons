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
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.atmofrance.internal.api.dto.AtmoFranceDto.BaseProperties;
import org.openhab.binding.atmofrance.internal.api.dto.AtmoFranceDto.IndexProperties;
import org.openhab.binding.atmofrance.internal.api.dto.AtmoFranceDto.PollensProperties;
import org.openhab.binding.atmofrance.internal.api.dto.Pollutant;
import org.openhab.binding.atmofrance.internal.api.dto.Taxon;
import org.openhab.binding.atmofrance.internal.configuration.CityConfiguration;
import org.openhab.binding.atmofrance.internal.configuration.ConfigurationLevel;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
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
    private final Map<String, ScheduledFuture<?>> jobs = new ConcurrentHashMap<>();

    private @Nullable CityConfiguration config;

    public CityHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        var localConfig = getConfigAs(CityConfiguration.class);
        ConfigurationLevel configLevel = localConfig.check();

        if (configLevel != ConfigurationLevel.COMPLETED) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configLevel.message);
            return;
        }

        config = localConfig;

        updateStatus(ThingStatus.UNKNOWN);
        schedule(AQ_JOB.formatted(thing.getUID()), this::getAtmoIndex, Duration.ofSeconds(2));
        schedule(POLLENS_JOB.formatted(thing.getUID()), this::getPollens, Duration.ofSeconds(3));
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
                updateState(GROUP_AQ, CHANNEL_INDEX, properties.codeQual.value);
                Pollutant.AS_SET.forEach(pollutant -> {
                    String channelName = pollutant.name().toLowerCase(Locale.ROOT) + "-index";
                    updateState(GROUP_AQ, channelName, properties.getPollutantIndex(pollutant));
                });
                updateCommon(properties, GROUP_AQ);
                updateStatus(ThingStatus.ONLINE);
            } else {
                if (getBridgeHandler(AtmoFranceApiHandler.class) != null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/no-data");
                } else {
                    // the call to getBridgeHandler will already have put me on OFFLINE/BRIDGE_OFFLINE
                    return;
                }
            }
        } else {
            delay = 10;
        }
        schedule(AQ_JOB.formatted(thing.getUID()), this::getAtmoIndex, Duration.ofSeconds(delay));
    }

    private void updateCommon(BaseProperties properties, String group) {
        updateState(group, CHANNEL_EFFECTIVE_DATE, properties.getEffectiveDate());
        updateState(group, CHANNEL_RELEASE_DATE, properties.getDiffusionDate());

        updateProperty("%s-source".formatted(group), properties.source);
    }

    private void getPollens() {
        @Nullable
        AtmoFranceApiHandler apiHandler = getBridgeHandler(AtmoFranceApiHandler.class);
        @Nullable
        CityConfiguration local = config;
        long delay = 3600;
        if (apiHandler != null && local != null) {
            PollensProperties properties = apiHandler.getPollens(local.codeInsee);
            if (properties != null) {
                updateState(GROUP_POLLENS, CHANNEL_INDEX, properties.getGlobal());
                Taxon.AS_SET.forEach(taxon -> {
                    String channelName = taxon.name().toLowerCase(Locale.ROOT) + "-";
                    updateState(GROUP_POLLENS, channelName + "level", properties.getTaxonIndex(taxon));
                    updateState(GROUP_POLLENS, channelName + "conc", properties.getTaxonConc(taxon));
                });
                updateCommon(properties, GROUP_POLLENS);
                updateStatus(ThingStatus.ONLINE);
            } else {
                if (getBridgeHandler(AtmoFranceApiHandler.class) != null) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/no-pollens");
                } else {
                    // the call to getBridgeHandler will already have put me on OFFLINE/BRIDGE_OFFLINE
                    return;
                }
            }
        } else {
            delay = 10;
        }
        schedule(POLLENS_JOB.formatted(thing.getUID()), this::getPollens, Duration.ofSeconds(delay));
    }

    private void updateState(String group, String channel, int value) {
        updateState(group, channel, new DecimalType(value));
    }

    private void updateState(String group, String channel, Instant date) {
        updateState(group, channel, new DateTimeType(date));
    }

    private void updateState(String group, String channel, State state) {
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), group, channel);
        if (isLinked(channelUID)) {
            updateState(channelUID, state);
        }
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
