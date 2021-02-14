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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
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

    private final Logger logger = LoggerFactory.getLogger(NovelanHeatpumpHandler.class);
    private static final SimpleDateFormat sdateformat = new SimpleDateFormat("dd.MM.yy HH:mm"); //$NON-NLS-1$

    private @Nullable ScheduledFuture<?> refreshJob;
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

    private String formatHours(int value) {
        String returnValue = "";
        returnValue += String.format("%02d:", new Object[] { Integer.valueOf(value / 3600) }); //$NON-NLS-1$
        value %= 3600;
        returnValue += String.format("%02d:", new Object[] { Integer.valueOf(value / 60) }); //$NON-NLS-1$
        value %= 60;
        returnValue += String.format("%02d", new Object[] { Integer.valueOf(value) }); //$NON-NLS-1$
        return returnValue;
    }

    /**
     * generate a readable string containing the time since the heatpump is in
     * the state.
     *
     * @param heatpumpValues
     *            the internal state array of the heatpump
     * @return a human readable time string
     */
    private String getStateTime(int[] heatpumpValues) {
        String returnValue = ""; //$NON-NLS-1$
        // for a long time create a date
        if (heatpumpValues[118] == 2) {
            long value = heatpumpValues[95];
            if (value < 0) {
                value = 0;
            }
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(value * 1000L);
            returnValue += sdateformat.format(cal.getTime());
        } else {
            // for a shorter time use the counted time (HH:mm:ss)
            int value = heatpumpValues[120];
            returnValue = formatHours(value);
        }
        return returnValue;
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
            case STATE_DURATION:
                return new StringType(getStateTime(heatpumpResponse.getHeatpumpValues()));
            case SIMPLE_STATE:
                int simpleState = heatpumpResponse.getHeatpumpValues()[117];
                return new StringType(String.valueOf(simpleState));
            case SIMPLE_STATE_NUM:
                int simpleStateNum = heatpumpResponse.getHeatpumpValues()[117];
                return new DecimalType(simpleStateNum);
            case EXTENDED_STATE:
                int extendedState = heatpumpResponse.getHeatpumpValues()[120];
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
                int hoursCompressor1 = heatpumpResponse.getHeatpumpValues()[56];
                return new StringType(formatHours(hoursCompressor1));
            case STARTS_COMPRESSOR1:
                int startsCompressor1 = heatpumpResponse.getHeatpumpValues()[57];
                return new DecimalType(startsCompressor1);
            case HOURS_COMPRESSOR2:
                int hoursCompressor2 = heatpumpResponse.getHeatpumpValues()[58];
                return new StringType(formatHours(hoursCompressor2));
            case STARTS_COMPRESSOR2:
                int startsCompressor2 = heatpumpResponse.getHeatpumpValues()[59];
                return new DecimalType(startsCompressor2);
            case HOURS_HEATPUMP:
                int hoursHeatpump = heatpumpResponse.getHeatpumpValues()[63];
                return new StringType(formatHours(hoursHeatpump));
            case HOURS_HEATING:
                int hoursHeating = heatpumpResponse.getHeatpumpValues()[64];
                return new StringType(formatHours(hoursHeating));
            case HOURS_WARMWATER:
                int hoursWarmwater = heatpumpResponse.getHeatpumpValues()[65];
                return new StringType(formatHours(hoursWarmwater));
            case HOURS_COOLING:
                int hoursCooling = heatpumpResponse.getHeatpumpValues()[66];
                return new StringType(formatHours(hoursCooling));
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
            case TEMPERATURE_SOLAR_STORAGE:
                double temperatureSolarStorage = (double) heatpumpResponse.getHeatpumpValues()[27] / 10;
                return new QuantityType<>(temperatureSolarStorage, API_TEMPERATURE_UNIT);
            case HEATING_OPERATION_MODE:
                int heatingOperationMode = heatpumpResponse.getHeatpumpParams()[PARAM_HEATING_OPERATION_MODE];
                return new DecimalType(heatingOperationMode);
            case HEATING_TEMPERATURE:
                double heatingTemperature = (double) heatpumpResponse.getHeatpumpParams()[PARAM_HEATING_TEMPERATURE]
                        / 10;
                return new QuantityType<>(heatingTemperature, API_TEMPERATURE_UNIT);
            case WARMWATER_OPERATION_MODE:
                int warmwaterOperationMode = heatpumpResponse.getHeatpumpParams()[PARAM_WARMWATER_OPERATION_MODE];
                return new DecimalType(warmwaterOperationMode);
            case WARMWATER_TEMPERATURE:
                double warmwaterTemperature = (double) heatpumpResponse.getHeatpumpParams()[PARAM_WARMWATER_TEMPERATURE]
                        / 10;
                return new QuantityType<>(warmwaterTemperature, API_TEMPERATURE_UNIT);
            case COOLING_OPERATION_MODE:
                int coolingOperationMode = heatpumpResponse.getHeatpumpParams()[PARAM_COOLING_OPERATION_MODE];
                return new DecimalType(coolingOperationMode);
            case COOLING_RELEASE_TEMPERATURE:
                double coolingReleaseTemperature = (double) heatpumpResponse
                        .getHeatpumpParams()[PARAM_COOLING_RELEASE_TEMP] / 10;
                return new QuantityType<>(coolingReleaseTemperature, API_TEMPERATURE_UNIT);
            case COOLING_INLET_TEMPERATURE:
                double coolingInletTemperature = (double) heatpumpResponse.getHeatpumpParams()[PARAM_COOLING_INLET_TEMP]
                        / 10;
                return new QuantityType<>(coolingInletTemperature, API_TEMPERATURE_UNIT);
            case COOLING_START_HOURS:
                double coolingStartHours = (double) heatpumpResponse.getHeatpumpParams()[PARAM_COOLING_START] / 10;
                return new DecimalType(coolingStartHours);
            case COOLING_STOP_HOURS:
                double coolingStopHours = (double) heatpumpResponse.getHeatpumpParams()[PARAM_COOLING_STOP] / 10;
                return new DecimalType(coolingStopHours);
            case OUTPUT_AV:
                return heatpumpResponse.getHeatpumpValues()[37] == 0 ? OnOffType.OFF : OnOffType.ON;
            case OUTPUT_BUP:
                return heatpumpResponse.getHeatpumpValues()[38] == 0 ? OnOffType.OFF : OnOffType.ON;
            case OUTPUT_HUP:
                return heatpumpResponse.getHeatpumpValues()[39] == 0 ? OnOffType.OFF : OnOffType.ON;
            case OUTPUT_VEN:
                return heatpumpResponse.getHeatpumpValues()[42] == 0 ? OnOffType.OFF : OnOffType.ON;
            case OUTPUT_VD1:
                return heatpumpResponse.getHeatpumpValues()[44] == 0 ? OnOffType.OFF : OnOffType.ON;
            case OUTPUT_VD2:
                return heatpumpResponse.getHeatpumpValues()[45] == 0 ? OnOffType.OFF : OnOffType.ON;
            case OUTPUT_ZIP:
                return heatpumpResponse.getHeatpumpValues()[46] == 0 ? OnOffType.OFF : OnOffType.ON;
            case OUTPUT_ZUP:
                return heatpumpResponse.getHeatpumpValues()[47] == 0 ? OnOffType.OFF : OnOffType.ON;
            case OUTPUT_ZW1:
                return heatpumpResponse.getHeatpumpValues()[48] == 0 ? OnOffType.OFF : OnOffType.ON;
            case OUTPUT_ZW2_SST:
                return heatpumpResponse.getHeatpumpValues()[49] == 0 ? OnOffType.OFF : OnOffType.ON;
            case OUTPUT_ZW3_SST:
                return heatpumpResponse.getHeatpumpValues()[50] == 0 ? OnOffType.OFF : OnOffType.ON;
            default:
                return UnDefType.UNDEF;
        }
    }
}
