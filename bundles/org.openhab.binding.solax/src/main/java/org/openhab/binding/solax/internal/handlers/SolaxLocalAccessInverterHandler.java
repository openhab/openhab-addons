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
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solax.internal.SolaxBindingConstants;
import org.openhab.binding.solax.internal.connectivity.rawdata.local.LocalConnectRawDataBean;
import org.openhab.binding.solax.internal.model.InverterType;
import org.openhab.binding.solax.internal.model.local.LocalData;
import org.openhab.binding.solax.internal.model.local.RawDataParser;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;

/**
 * The {@link SolaxLocalAccessInverterHandler} the handler for the inverter
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class SolaxLocalAccessInverterHandler extends SolaxLocalAccessAbstractHandler {

    private final Logger logger = LoggerFactory.getLogger(SolaxLocalAccessInverterHandler.class);

    public SolaxLocalAccessInverterHandler(Thing thing, TranslationProvider i18nProvider,
            TimeZoneProvider timeZoneProvider) {
        super(thing, i18nProvider, timeZoneProvider);
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

                LocalData genericInverterData = parser.getData(rawDataBean);
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

    private InverterType calculateInverterType(LocalConnectRawDataBean rawDataBean) {
        int type = rawDataBean.getType();
        return InverterType.fromIndex(type);
    }

    private void updateProperties(LocalData genericInverterData) {
        updateProperty(Thing.PROPERTY_SERIAL_NUMBER, genericInverterData.getWifiSerial());
        updateProperty(SolaxBindingConstants.PROPERTY_INVERTER_TYPE, genericInverterData.getInverterType().name());
    }

    private void updateChannels(RawDataParser parser, LocalData inverterData) {
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
}
