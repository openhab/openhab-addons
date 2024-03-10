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
package org.openhab.binding.solax.internal.handlers;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solax.internal.SolaxBindingConstants;
import org.openhab.binding.solax.internal.SolaxConfiguration;
import org.openhab.binding.solax.internal.connectivity.LocalHttpConnector;
import org.openhab.binding.solax.internal.connectivity.SolaxConnector;
import org.openhab.binding.solax.internal.connectivity.rawdata.local.LocalConnectRawDataBean;
import org.openhab.binding.solax.internal.model.InverterType;
import org.openhab.binding.solax.internal.model.local.LocalInverterData;
import org.openhab.binding.solax.internal.model.local.parsers.RawDataParser;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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
public class SolaxLocalAccessHandler extends AbstractSolaxHandler {

    private final Logger logger = LoggerFactory.getLogger(SolaxLocalAccessHandler.class);

    private boolean alreadyRemovedUnsupportedChannels;

    private final Set<String> unsupportedExistingChannels = new HashSet<>();

    public SolaxLocalAccessHandler(Thing thing, TranslationProvider i18nProvider, TimeZoneProvider timeZoneProvider) {
        super(thing, i18nProvider, timeZoneProvider);
    }

    @Override
    protected SolaxConnector createConnector(SolaxConfiguration config) {
        return new LocalHttpConnector(config.password, config.hostname);
    }

    @Override
    protected void updateFromData(String rawJsonData) {
        try {
            LocalConnectRawDataBean rawDataBean = parseJson(rawJsonData);
            InverterType inverterType = calculateInverterType(rawDataBean);
            RawDataParser parser = inverterType.getParser();
            if (parser != null) {
                if (!alreadyRemovedUnsupportedChannels) {
                    removeUnsupportedChannels(inverterType.getSupportedChannels());
                    alreadyRemovedUnsupportedChannels = true;
                }

                LocalInverterData genericInverterData = parser.getData(rawDataBean);
                updateChannels(parser, genericInverterData);
                updateProperties(genericInverterData);
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
        LocalConnectRawDataBean fromJson = LocalConnectRawDataBean.fromJson(rawJsonData);
        logger.debug("Received a new inverter JSON object. Data = {}", fromJson.toString());
        return fromJson;
    }

    private InverterType calculateInverterType(LocalConnectRawDataBean rawDataBean) {
        int type = rawDataBean.getType();
        return InverterType.fromIndex(type);
    }

    private void updateProperties(LocalInverterData genericInverterData) {
        updateProperty(Thing.PROPERTY_SERIAL_NUMBER, genericInverterData.getWifiSerial());
        updateProperty(SolaxBindingConstants.PROPERTY_INVERTER_TYPE, genericInverterData.getInverterType().name());
    }

    private void updateChannels(RawDataParser parser, LocalInverterData inverterData) {
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
        updateState(SolaxBindingConstants.CHANNEL_INVERTER_WORKMODE,
                new StringType(inverterData.getInverterWorkMode()));

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
