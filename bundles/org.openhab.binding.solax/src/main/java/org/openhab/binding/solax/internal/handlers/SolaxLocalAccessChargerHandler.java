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
import org.openhab.binding.solax.internal.exceptions.SolaxUpdateException;
import org.openhab.binding.solax.internal.model.local.EvChargerData;
import org.openhab.binding.solax.internal.model.local.LocalData;
import org.openhab.binding.solax.internal.model.local.RawDataParser;
import org.openhab.binding.solax.internal.model.local.evchargers.EvChargerDataParser;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;

/**
 * The {@link SolaxLocalAccessChargerHandler} the handler for the charger
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
public class SolaxLocalAccessChargerHandler extends SolaxLocalAccessAbstractHandler {

    private static final EvChargerDataParser parser = new EvChargerDataParser();

    public SolaxLocalAccessChargerHandler(Thing thing, TranslationProvider i18nProvider,
            TimeZoneProvider timeZoneProvider) {
        super(thing, i18nProvider, timeZoneProvider);
    }

    @Override
    public void initialize() {
        removeUnsupportedChannels(parser.getSupportedChannels());
        super.initialize();
    }

    @Override
    protected void updateFromData(String rawJsonData) throws SolaxUpdateException {
        LocalConnectRawDataBean rawDataBean = parseJson(rawJsonData);
        EvChargerData data = parser.getData(rawDataBean);
        updateChannels(parser, data);
        updateProperties(data);
    }

    private void updateProperties(LocalData data) {
    }

    private void updateChannels(RawDataParser parser, EvChargerData data) {
        Set<String> supportedChannels = parser.getSupportedChannels();

        // States/modes
        updateState(SolaxBindingConstants.CHANNEL_CHARGER_MODE, new StringType(data.getDeviceMode()));
        updateState(SolaxBindingConstants.CHANNEL_CHARGER_STATE, new StringType(data.getDeviceState()));

        // Energy
        updateChannel(SolaxBindingConstants.CHANNEL_CHARGER_EQ_SINGLE_SESSION, data.getEqSingle(), Units.KILOWATT_HOUR,
                supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_CHARGER_EQ_TOTAL, data.getEqTotal(), Units.KILOWATT_HOUR,
                supportedChannels);

        // Output data
        updateChannel(SolaxBindingConstants.CHANNEL_CHARGER_OUTPUT_CURRENT_PHASE1, data.getCurrentPhase1(),
                Units.AMPERE, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_CHARGER_OUTPUT_CURRENT_PHASE2, data.getCurrentPhase2(),
                Units.AMPERE, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_CHARGER_OUTPUT_CURRENT_PHASE3, data.getCurrentPhase3(),
                Units.AMPERE, supportedChannels);

        updateChannel(SolaxBindingConstants.CHANNEL_CHARGER_OUTPUT_VOLTAGE_PHASE1, data.getVoltagePhase1(), Units.VOLT,
                supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_CHARGER_OUTPUT_VOLTAGE_PHASE2, data.getVoltagePhase2(), Units.VOLT,
                supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_CHARGER_OUTPUT_VOLTAGE_PHASE3, data.getVoltagePhase3(), Units.VOLT,
                supportedChannels);

        updateChannel(SolaxBindingConstants.CHANNEL_CHARGER_OUTPUT_POWER_PHASE1, data.getOutputPowerPhase1(),
                Units.WATT, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_CHARGER_OUTPUT_POWER_PHASE2, data.getOutputPowerPhase2(),
                Units.WATT, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_CHARGER_OUTPUT_POWER_PHASE3, data.getOutputPowerPhase3(),
                Units.WATT, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_CHARGER_TOTAL_OUTPUT_POWER, data.getTotalChargePower(), Units.WATT,
                supportedChannels);

        // Provider data
        updateChannel(SolaxBindingConstants.CHANNEL_CHARGER_EXTERNAL_CURRENT_PHASE1, data.getExternalCurrentPhase1(),
                Units.AMPERE, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_CHARGER_EXTERNAL_CURRENT_PHASE2, data.getExternalCurrentPhase2(),
                Units.AMPERE, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_CHARGER_EXTERNAL_CURRENT_PHASE3, data.getExternalCurrentPhase3(),
                Units.AMPERE, supportedChannels);

        updateChannel(SolaxBindingConstants.CHANNEL_CHARGER_EXTERNAL_POWER_PHASE1, data.getExternalPowerPhase1(),
                Units.WATT, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_CHARGER_EXTERNAL_POWER_PHASE2, data.getExternalPowerPhase2(),
                Units.WATT, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_CHARGER_EXTERNAL_POWER_PHASE3, data.getExternalPowerPhase3(),
                Units.WATT, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_CHARGER_TOTAL_EXTERNAL_POWER, data.getExternalTotalPower(),
                Units.WATT, supportedChannels);

        // Temperatures
        updateChannel(SolaxBindingConstants.CHANNEL_CHARGER_PLUG_TEMPERATURE, data.getPlugTemperature(),
                SIUnits.CELSIUS, supportedChannels);
        updateChannel(SolaxBindingConstants.CHANNEL_CHARGER_INTERNAL_TEMPERATURE, data.getInternalTemperature(),
                SIUnits.CELSIUS, supportedChannels);

        // Binding provided data
        updateState(SolaxBindingConstants.CHANNEL_TIMESTAMP, new DateTimeType(ZonedDateTime.now()));
    }
}
