/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.solax.internal;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solax.internal.connectivity.LocalHttpConnector;
import org.openhab.binding.solax.internal.connectivity.rawdata.LocalConnectRawDataBean;
import org.openhab.binding.solax.internal.model.InverterData;
import org.openhab.binding.solax.internal.model.InverterType;
import org.openhab.binding.solax.internal.model.SinglePhaseInverterData;
import org.openhab.binding.solax.internal.model.ThreePhaseInverterData;
import org.openhab.binding.solax.internal.model.parsers.RawDataParser;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;

/**
 * The {@link SolaxLocalAccessHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class SolaxLocalAccessHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SolaxLocalAccessHandler.class);

    private static final int INITIAL_SCHEDULE_DELAY_SECONDS = 5;

    private @NonNullByDefault({}) LocalHttpConnector localHttpConnector;

    private @Nullable ScheduledFuture<?> schedule;

    private boolean alreadyRemovedUnsupportedChannels;

    public SolaxLocalAccessHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);

        SolaxConfiguration config = getConfigAs(SolaxConfiguration.class);
        localHttpConnector = new LocalHttpConnector(config.password, config.hostname);
        int refreshInterval = config.refreshInterval;
        TimeUnit timeUnit = TimeUnit.SECONDS;

        logger.debug("Scheduling regular interval retrieval every {} {}", refreshInterval, timeUnit);
        schedule = scheduler.scheduleWithFixedDelay(this::retrieveData, INITIAL_SCHEDULE_DELAY_SECONDS, refreshInterval,
                timeUnit);
    }

    private void retrieveData() {
        try {
            String rawJsonData = localHttpConnector.retrieveData();
            logger.debug("Raw data retrieved = {}", rawJsonData);

            if (rawJsonData != null && !rawJsonData.isEmpty()) {
                updateFromData(rawJsonData);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        SolaxBindingConstants.I18N_KEY_OFFLINE_COMMUNICATION_ERROR_JSON_CANNOT_BE_RETRIEVED);
            }
        } catch (IOException e) {
            logger.debug("Exception received while attempting to retrieve data via HTTP", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void updateFromData(String rawJsonData) {
        try {
            LocalConnectRawDataBean rawDataBean = parseJson(rawJsonData);
            InverterType inverterType = calculateInverterType(rawDataBean);
            RawDataParser parser = inverterType.getParser();
            if (parser != null) {
                if (!alreadyRemovedUnsupportedChannels) {
                    removeUnsupportedChannels(inverterType.getSupportedChannels());
                    alreadyRemovedUnsupportedChannels = true;
                }

                updateChannels(rawDataBean, parser);

                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }
            } else {
                cancelSchedule();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.configuration-error.parser-not-implemented [\"" + inverterType.name() + "\"]");
            }
        } catch (JsonParseException e) {
            logger.debug("Unable to deserialize from JSON.", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private LocalConnectRawDataBean parseJson(String rawJsonData) {
        LocalConnectRawDataBean inverterParsedData = LocalConnectRawDataBean.fromJson(rawJsonData);
        logger.debug("Received a new inverter JSON object. Data = {}", inverterParsedData.toString());
        return inverterParsedData;
    }

    private InverterType calculateInverterType(LocalConnectRawDataBean rawDataBean) {
        int type = rawDataBean.getType();
        return InverterType.fromIndex(type);
    }

    private void updateChannels(LocalConnectRawDataBean rawDataBean, RawDataParser parser) {
        InverterData genericInverterData = parser.getData(rawDataBean);
        updateProperty(Thing.PROPERTY_SERIAL_NUMBER, genericInverterData.getWifiSerial());
        updateProperty(SolaxBindingConstants.PROPERTY_INVERTER_TYPE, genericInverterData.getInverterType().name());

        updateCommonChannels(parser, genericInverterData);

        if (genericInverterData instanceof SinglePhaseInverterData singlePhaseData) {
            updateSinglePhaseSpecificData(singlePhaseData);
        } else if (genericInverterData instanceof ThreePhaseInverterData threePhaseData) {
            updateThreePhaseSpecificData(threePhaseData);
        }
    }

    private void updateCommonChannels(RawDataParser parser, InverterData genericInverterData) {
        updateState(SolaxBindingConstants.CHANNEL_RAW_DATA, new StringType(genericInverterData.getRawData()));

        Set<String> supportedChannels = parser.getSupportedChannels();
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV1_POWER,
                new QuantityType<>(genericInverterData.getPV1Power(), Units.WATT), supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV1_CURRENT,
                new QuantityType<>(genericInverterData.getPV1Current(), Units.AMPERE), supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV1_VOLTAGE,
                new QuantityType<>(genericInverterData.getPV1Voltage(), Units.VOLT), supportedChannels);

        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV2_POWER,
                new QuantityType<>(genericInverterData.getPV2Power(), Units.WATT), supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV2_CURRENT,
                new QuantityType<>(genericInverterData.getPV2Current(), Units.AMPERE), supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV2_VOLTAGE,
                new QuantityType<>(genericInverterData.getPV2Voltage(), Units.VOLT), supportedChannels);

        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV_TOTAL_POWER,
                new QuantityType<>(genericInverterData.getPVTotalPower(), Units.WATT), supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV_TOTAL_CURRENT,
                new QuantityType<>(genericInverterData.getPVTotalCurrent(), Units.AMPERE), supportedChannels);

        updateChannel(SolaxBindingConstants.CHANNEL_BATTERY_POWER,
                new QuantityType<>(genericInverterData.getBatteryPower(), Units.WATT), supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_BATTERY_CURRENT,
                new QuantityType<>(genericInverterData.getBatteryCurrent(), Units.AMPERE), supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_BATTERY_VOLTAGE,
                new QuantityType<>(genericInverterData.getBatteryVoltage(), Units.VOLT), supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_BATTERY_TEMPERATURE,
                new QuantityType<>(genericInverterData.getBatteryTemperature(), SIUnits.CELSIUS), supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_BATTERY_STATE_OF_CHARGE,
                new QuantityType<>(genericInverterData.getBatteryLevel(), Units.PERCENT), supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_TIMESTAMP, new DateTimeType(ZonedDateTime.now()),
                supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_FEED_IN_POWER,
                new QuantityType<>(genericInverterData.getFeedInPower(), Units.WATT), supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_POWER_USAGE,
                new QuantityType<>(genericInverterData.getPowerUsage(), Units.WATT), supportedChannels);

        // Totals
        updateChannel(SolaxBindingConstants.CHANNEL_TOTAL_ENERGY,
                new QuantityType<>(genericInverterData.getTotalEnergy(), Units.KILOWATT_HOUR), supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_TOTAL_BATTERY_DISCHARGE_ENERGY,
                new QuantityType<>(genericInverterData.getTotalBatteryDischargeEnergy(), Units.KILOWATT_HOUR),
                supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_TOTAL_BATTERY_CHARGE_ENERGY,
                new QuantityType<>(genericInverterData.getTotalBatteryChargeEnergy(), Units.KILOWATT_HOUR),
                supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_TOTAL_PV_ENERGY,
                new QuantityType<>(genericInverterData.getTotalPVEnergy(), Units.KILOWATT_HOUR), supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_TOTAL_FEED_IN_ENERGY,
                new QuantityType<>(genericInverterData.getTotalFeedInEnergy(), Units.KILOWATT_HOUR), supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_TOTAL_CONSUMPTION,
                new QuantityType<>(genericInverterData.getTotalConsumption(), Units.KILOWATT_HOUR), supportedChannels);

        // Today's
        updateChannel(SolaxBindingConstants.CHANNEL_TODAY_ENERGY,
                new QuantityType<>(genericInverterData.getTodayEnergy(), Units.KILOWATT_HOUR), supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_TODAY_BATTERY_DISCHARGE_ENERGY,
                new QuantityType<>(genericInverterData.getTodayBatteryDischargeEnergy(), Units.KILOWATT_HOUR),
                supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_TODAY_BATTERY_CHARGE_ENERGY,
                new QuantityType<>(genericInverterData.getTodayBatteryChargeEnergy(), Units.KILOWATT_HOUR),
                supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_TODAY_FEED_IN_ENERGY,
                new QuantityType<>(genericInverterData.getTodayFeedInEnergy(), Units.KILOWATT_HOUR), supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_TODAY_CONSUMPTION,
                new QuantityType<>(genericInverterData.getTodayConsumption(), Units.KILOWATT_HOUR), supportedChannels);
    }

    private void updateSinglePhaseSpecificData(SinglePhaseInverterData singlePhaseData) {
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_POWER,
                new QuantityType<>(singlePhaseData.getInverterOutputPower(), Units.WATT));
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_CURRENT,
                new QuantityType<>(singlePhaseData.getInverterCurrent(), Units.AMPERE));
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_VOLTAGE,
                new QuantityType<>(singlePhaseData.getInverterVoltage(), Units.VOLT));
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_FREQUENCY,
                new QuantityType<>(singlePhaseData.getInverterFrequency(), Units.HERTZ));
    }

    private void updateThreePhaseSpecificData(ThreePhaseInverterData threePhaseData) {
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_POWER_PHASE1,
                new QuantityType<>(threePhaseData.getOutputPowerPhase1(), Units.WATT));
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_POWER_PHASE2,
                new QuantityType<>(threePhaseData.getOutputPowerPhase2(), Units.WATT));
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_POWER_PHASE3,
                new QuantityType<>(threePhaseData.getOutputPowerPhase3(), Units.WATT));
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_TOTAL_OUTPUT_POWER,
                new QuantityType<>(threePhaseData.getTotalOutputPower(), Units.WATT));

        updateState(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_CURRENT_PHASE1,
                new QuantityType<>(threePhaseData.getCurrentPhase1(), Units.AMPERE));
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_CURRENT_PHASE2,
                new QuantityType<>(threePhaseData.getCurrentPhase2(), Units.AMPERE));
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_CURRENT_PHASE3,
                new QuantityType<>(threePhaseData.getCurrentPhase3(), Units.AMPERE));

        updateState(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_VOLTAGE_PHASE1,
                new QuantityType<>(threePhaseData.getVoltagePhase1(), Units.VOLT));
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_VOLTAGE_PHASE2,
                new QuantityType<>(threePhaseData.getVoltagePhase2(), Units.VOLT));
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_VOLTAGE_PHASE3,
                new QuantityType<>(threePhaseData.getVoltagePhase3(), Units.VOLT));

        updateState(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_FREQUENCY_PHASE1,
                new QuantityType<>(threePhaseData.getFrequencyPhase1(), Units.HERTZ));
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_FREQUENCY_PHASE2,
                new QuantityType<>(threePhaseData.getFrequencyPhase2(), Units.HERTZ));
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_FREQUENCY_PHASE3,
                new QuantityType<>(threePhaseData.getFrequencyPhase3(), Units.HERTZ));
    }

    private void removeUnsupportedChannels(Set<String> supportedChannels) {
        if (supportedChannels.isEmpty()) {
            return;
        }
        List<Channel> channels = getThing().getChannels();
        List<Channel> channelsToRemove = channels.stream()
                .filter(channel -> !supportedChannels.contains(channel.getUID().getId())).collect(Collectors.toList());

        if (!channelsToRemove.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logRemovedChannels(channelsToRemove);
            }
            updateThing(editThing().withoutChannels(channelsToRemove).build());
        }
    }

    private void logRemovedChannels(List<Channel> channelsToRemove) {
        List<String> channelsToRemoveForLog = channelsToRemove.stream().map(channel -> channel.getUID().getId())
                .toList();
        logger.debug("Detected not supported channels for the current inverter. Channels to be removed:{}",
                channelsToRemoveForLog);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Nothing to do here as of now. Maybe implement a REFRESH command in the future.
    }

    @Override
    public void dispose() {
        super.dispose();
        cancelSchedule();
    }

    private void cancelSchedule() {
        ScheduledFuture<?> schedule = this.schedule;
        if (schedule != null) {
            schedule.cancel(true);
            this.schedule = null;
        }
    }

    private void updateChannel(String channelID, State state, Set<String> supportedChannels) {
        if (supportedChannels.contains(channelID)) {
            updateState(channelID, state);
        }
    }
}
