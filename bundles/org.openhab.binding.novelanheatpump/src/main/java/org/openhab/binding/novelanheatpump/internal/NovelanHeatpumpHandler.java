/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.novelanheatpump.internal;

import static org.openhab.binding.novelanheatpump.internal.NovelanHeatpumpBindingConstants.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NovelanHeatpumpHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jan-Philipp Bolle - Initial contribution
 */
@NonNullByDefault
public class NovelanHeatpumpHandler extends BaseThingHandler {
    private @Nullable ScheduledFuture<?> refreshJob;

    private final Logger logger = LoggerFactory.getLogger(NovelanHeatpumpHandler.class);

    private @Nullable NovelanHeatpumpConfiguration config;
    private @Nullable HeatpumpConnector connector;

    public NovelanHeatpumpHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateAndPublishData();
        } else {
            logger.debug("The NovelanHeatpump binding is read-only and can not handle command {}", command);
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing NovelanHeatpump handler.");
        config = getConfigAs(NovelanHeatpumpConfiguration.class);
        logger.debug("config address = {}", config.address);
        logger.debug("config port = {}", config.port);
        logger.debug("config refresh = {}", config.refresh);

        List<String> errorMsg = new ArrayList<>();

        if (config.address.trim().isEmpty()) {
            errorMsg.add("Parameter 'address' is mandatory and must be configured");
        }

        connector = new HeatpumpConnector(config.address, config.port);

        if (errorMsg.isEmpty()) {
            ScheduledFuture<?> job = this.refreshJob;
            if (job == null || job.isCancelled()) {
                refreshJob = scheduler.scheduleWithFixedDelay(this::updateAndPublishData, 0, config.refresh,
                        TimeUnit.SECONDS);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String.join(", ", errorMsg));
        }
    }

    private void updateAndPublishData() {
        String errorMsg;
        try {
            connector.connect();

            NovelanHeatpumpResponse heatpumpResponse = connector.getResponse();
            getThing().getChannels().stream().filter(channel -> isLinked(channel.getUID().getId())).forEach(channel -> {
                String channelId = channel.getUID().getId();
                State state = getValue(channelId, heatpumpResponse);
                updateState(channelId, state);
            });

            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
            return;
        } catch (UnknownHostException e) {
            errorMsg = "Configuration is incorrect. the given hostname of the Novelan heatpump is unknown";
            logger.warn("Error running aqicn.org request: {}", errorMsg);
        } catch (IOException e) {
            errorMsg = e.getMessage();
        } finally {
            if (connector != null) {
                connector.disconnect();
            }
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, errorMsg);
    }

    private State getValue(String channelId, NovelanHeatpumpResponse heatpumpResponse) {
        // all temperatures are 0.2 degree Celsius exact
        // but use int to save values
        // example 124 is 12.4 degree Celsius
        switch (channelId) {
            case TEMPERATURE_OUTSIDE:
                double temperatureOutside = (double) heatpumpResponse.getHeatpumpValues()[15] / 10;
                return new QuantityType<>(temperatureOutside, API_TEMPERATURE_UNIT);
            case TEMPERATURE_OUTSIDE_AVG:
                double temperatureOutsideAvg = (double) heatpumpResponse.getHeatpumpValues()[16] / 10;
                return new QuantityType<>(temperatureOutsideAvg, API_TEMPERATURE_UNIT);
            case TEMPERATURE_RETURN:
                double temperatureReturn = (double) heatpumpResponse.getHeatpumpValues()[11] / 10;
                return new QuantityType<>(temperatureReturn, API_TEMPERATURE_UNIT);
            case TEMPERATURE_REFERENCE_RETURN:
                double temperatureReferenceReturn = (double) heatpumpResponse.getHeatpumpValues()[12] / 10;
                return new QuantityType<>(temperatureReferenceReturn, API_TEMPERATURE_UNIT);
            case TEMPERATURE_SUPPLAY:
                double temperatureSupplay = (double) heatpumpResponse.getHeatpumpValues()[10] / 10;
                return new QuantityType<>(temperatureSupplay, API_TEMPERATURE_UNIT);
            case TEMPERATURE_SERVICEWATER_REFERENCE:
                double temperatureServicewaterReference = (double) heatpumpResponse.getHeatpumpValues()[18] / 10;
                return new QuantityType<>(temperatureServicewaterReference, API_TEMPERATURE_UNIT);
            case TEMPERATURE_SERVICEWATER:
                double temperatureServicewater = (double) heatpumpResponse.getHeatpumpValues()[17] / 10;
                return new QuantityType<>(temperatureServicewater, API_TEMPERATURE_UNIT);
            case STATE:
                // @TODO
                return UnDefType.UNDEF;
            case SIMPLE_STATE:
                int simpleState = heatpumpResponse.getHeatpumpValues()[117];
                return new StringType(String.valueOf(simpleState));
            case SIMPLE_STATE_NUM:
                // @TODO
                return UnDefType.UNDEF;
            case EXTENDED_STATE:
                int extendedState = heatpumpResponse.getHeatpumpValues()[117];
                return new StringType(String.valueOf(extendedState));
            case TEMPERATURE_SOLAR_COLLECTOR:
                double temperatureSolarCollector = (double) heatpumpResponse.getHeatpumpValues()[26] / 10;
                return new QuantityType<>(temperatureSolarCollector, API_TEMPERATURE_UNIT);
            case TEMPERATURE_PROBE_IN:
                double temperatureProbeIn = (double) heatpumpResponse.getHeatpumpValues()[19] / 10;
                return new QuantityType<>(temperatureProbeIn, API_TEMPERATURE_UNIT);
            case TEMPERATURE_PROBE_OUT:
                double temperatureProbeOut = (double) heatpumpResponse.getHeatpumpValues()[20] / 10;
                return new QuantityType<>(temperatureProbeOut, API_TEMPERATURE_UNIT);
            case HOURS_COMPRESSOR1:
                // @TODO
                return UnDefType.UNDEF;
            case STARTS_COMPRESSOR1:
                // @TODO
                return UnDefType.UNDEF;
            case HOURS_COMPRESSOR2:
                // @TODO
                return UnDefType.UNDEF;
            case STARTS_COMPRESSOR2:
                // @TODO
                return UnDefType.UNDEF;
            case HOURS_HEATPUMP:
                // @TODO
                return UnDefType.UNDEF;
            case HOURS_HEATING:
                // @TODO
                return UnDefType.UNDEF;
            case HOURS_WARMWATER:
                // @TODO
                return UnDefType.UNDEF;
            case HOURS_COOLING:
                // @TODO
                return UnDefType.UNDEF;
            case THERMALENERGY_HEATING:
                double thermalenergyHeating = (double) heatpumpResponse.getHeatpumpValues()[151] / 10;
                return new QuantityType<>(thermalenergyHeating, API_POWER_UNIT);
            case THERMALENERGY_WARMWATER:
                double thermalenergyWarmwater = (double) heatpumpResponse.getHeatpumpValues()[152] / 10;
                return new QuantityType<>(thermalenergyWarmwater, API_POWER_UNIT);
            case THERMALENERGY_POOL:
                double thermalenergyPool = (double) heatpumpResponse.getHeatpumpValues()[153] / 10;
                return new QuantityType<>(thermalenergyPool, API_POWER_UNIT);
            case THERMALENERGY_TOTAL:
                double thermalenergyTotal = (double) heatpumpResponse.getHeatpumpValues()[154] / 10;
                return new QuantityType<>(thermalenergyTotal, API_POWER_UNIT);
            default:
                return UnDefType.UNDEF;
        }
    }
}
