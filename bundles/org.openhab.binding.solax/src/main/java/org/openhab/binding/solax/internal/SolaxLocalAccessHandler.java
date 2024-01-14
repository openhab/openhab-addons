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
package org.openhab.binding.solax.internal;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.solax.internal.connectivity.LocalHttpConnector;
import org.openhab.binding.solax.internal.connectivity.rawdata.LocalConnectRawDataBean;
import org.openhab.binding.solax.internal.model.InverterData;
import org.openhab.binding.solax.internal.model.InverterType;
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
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
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

    private final Set<String> unsupportedExistingChannels = new HashSet<>();

    private final ReentrantLock retrieveDataCallLock = new ReentrantLock();

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
        if (retrieveDataCallLock.tryLock()) {
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
            } finally {
                retrieveDataCallLock.unlock();
            }
        } else {
            logger.debug("Unable to retrieve data because a request is already in progress.");
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

                InverterData genericInverterData = parser.getData(rawDataBean);
                updateChannels(parser, genericInverterData);
                updateProperties(genericInverterData);

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

    private void updateProperties(InverterData genericInverterData) {
        updateProperty(Thing.PROPERTY_SERIAL_NUMBER, genericInverterData.getWifiSerial());
        updateProperty(SolaxBindingConstants.PROPERTY_INVERTER_TYPE, genericInverterData.getInverterType().name());
    }

    private void updateChannels(RawDataParser parser, InverterData inverterData) {
        updateState(SolaxBindingConstants.CHANNEL_RAW_DATA, new StringType(inverterData.getRawData()));

        Set<String> supportedChannels = parser.getSupportedChannels();
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV1_POWER, inverterData.getPV1Power(), Units.WATT,
                supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV1_CURRENT, inverterData.getPV1Current(), Units.AMPERE,
                supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV1_VOLTAGE, inverterData.getPV1Voltage(), Units.VOLT,
                supportedChannels);

        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV2_POWER, inverterData.getPV2Power(), Units.WATT,
                supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV2_CURRENT, inverterData.getPV2Current(), Units.AMPERE,
                supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV2_VOLTAGE, inverterData.getPV2Voltage(), Units.VOLT,
                supportedChannels);

        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV_TOTAL_POWER, inverterData.getPVTotalPower(), Units.WATT,
                supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_PV_TOTAL_CURRENT, inverterData.getPVTotalCurrent(),
                Units.AMPERE, supportedChannels);

        updateChannel(SolaxBindingConstants.CHANNEL_BATTERY_POWER, inverterData.getBatteryPower(), Units.WATT,
                supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_BATTERY_CURRENT, inverterData.getBatteryCurrent(), Units.AMPERE,
                supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_BATTERY_VOLTAGE, inverterData.getBatteryVoltage(), Units.VOLT,
                supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_BATTERY_TEMPERATURE, inverterData.getBatteryTemperature(),
                SIUnits.CELSIUS, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_BATTERY_STATE_OF_CHARGE, inverterData.getBatteryLevel(),
                Units.PERCENT, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_FEED_IN_POWER, inverterData.getFeedInPower(), Units.WATT,
                supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_POWER_USAGE, inverterData.getPowerUsage(), Units.WATT,
                supportedChannels);

        // Totals
        updateChannel(SolaxBindingConstants.CHANNEL_TOTAL_ENERGY, inverterData.getTotalEnergy(), Units.KILOWATT_HOUR,
                supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_TOTAL_BATTERY_DISCHARGE_ENERGY,
                inverterData.getTotalBatteryDischargeEnergy(), Units.KILOWATT_HOUR, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_TOTAL_BATTERY_CHARGE_ENERGY,
                inverterData.getTotalBatteryChargeEnergy(), Units.KILOWATT_HOUR, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_TOTAL_PV_ENERGY, inverterData.getTotalPVEnergy(),
                Units.KILOWATT_HOUR, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_TOTAL_FEED_IN_ENERGY, inverterData.getTotalFeedInEnergy(),
                Units.KILOWATT_HOUR, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_TOTAL_CONSUMPTION, inverterData.getTotalConsumption(),
                Units.KILOWATT_HOUR, supportedChannels);

        // Today's
        updateChannel(SolaxBindingConstants.CHANNEL_TODAY_ENERGY, inverterData.getTodayEnergy(), Units.KILOWATT_HOUR,
                supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_TODAY_BATTERY_DISCHARGE_ENERGY,
                inverterData.getTodayBatteryDischargeEnergy(), Units.KILOWATT_HOUR, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_TODAY_BATTERY_CHARGE_ENERGY,
                inverterData.getTodayBatteryChargeEnergy(), Units.KILOWATT_HOUR, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_TODAY_FEED_IN_ENERGY, inverterData.getTodayFeedInEnergy(),
                Units.KILOWATT_HOUR, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_TODAY_CONSUMPTION, inverterData.getTodayConsumption(),
                Units.KILOWATT_HOUR, supportedChannels);

        // Single phase specific channels
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_POWER, inverterData.getInverterOutputPower(),
                Units.WATT, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_CURRENT, inverterData.getInverterCurrent(),
                Units.AMPERE, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_VOLTAGE, inverterData.getInverterVoltage(),
                Units.VOLT, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_FREQUENCY, inverterData.getInverterFrequency(),
                Units.HERTZ, supportedChannels);

        // Three phase specific channels
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_POWER_PHASE1, inverterData.getOutputPowerPhase1(),
                Units.WATT, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_POWER_PHASE2, inverterData.getOutputPowerPhase2(),
                Units.WATT, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_POWER_PHASE3, inverterData.getOutputPowerPhase3(),
                Units.WATT, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_TOTAL_OUTPUT_POWER, inverterData.getTotalOutputPower(),
                Units.WATT, supportedChannels);

        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_CURRENT_PHASE1, inverterData.getCurrentPhase1(),
                Units.AMPERE, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_CURRENT_PHASE2, inverterData.getCurrentPhase2(),
                Units.AMPERE, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_CURRENT_PHASE3, inverterData.getCurrentPhase3(),
                Units.AMPERE, supportedChannels);

        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_VOLTAGE_PHASE1, inverterData.getVoltagePhase1(),
                Units.VOLT, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_VOLTAGE_PHASE2, inverterData.getVoltagePhase2(),
                Units.VOLT, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_VOLTAGE_PHASE3, inverterData.getVoltagePhase3(),
                Units.VOLT, supportedChannels);

        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_FREQUENCY_PHASE1, inverterData.getFrequencyPhase1(),
                Units.HERTZ, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_FREQUENCY_PHASE2, inverterData.getFrequencyPhase2(),
                Units.HERTZ, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_INVERTER_OUTPUT_FREQUENCY_PHASE3, inverterData.getFrequencyPhase3(),
                Units.HERTZ, supportedChannels);

        // Binding provided data
        updateState(SolaxBindingConstants.CHANNEL_TIMESTAMP, new DateTimeType(ZonedDateTime.now()));
    }

    private void removeUnsupportedChannels(Set<String> supportedChannels) {
        if (supportedChannels.isEmpty()) {
            return;
        }
        List<Channel> channels = getThing().getChannels();
        List<Channel> channelsToRemove = channels.stream()
                .filter(channel -> !supportedChannels.contains(channel.getUID().getId())).toList();

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
        logger.debug("Detected unsupported channels for the current inverter. Channels to be removed: {}",
                channelsToRemoveForLog);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(this::retrieveData);
        } else {
            logger.debug("Binding {} only supports refresh command", SolaxBindingConstants.BINDING_ID);
        }
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

    private <T extends Quantity<T>> void updateChannel(String channelID, double value, Unit<T> unit,
            Set<String> supportedChannels) {
        if (supportedChannels.contains(channelID)) {
            if (value > Short.MIN_VALUE) {
                updateState(channelID, new QuantityType<>(value, unit));
            } else if (!unsupportedExistingChannels.contains(channelID)) {
                updateState(channelID, UnDefType.UNDEF);
                unsupportedExistingChannels.add(channelID);
                logger.warn(
                        "Channel {} is marked as supported, but its value is out of the defined range. Value = {}. This is unexpected behaviour. Please file a bug.",
                        channelID, value);
            }
        }
    }
}
