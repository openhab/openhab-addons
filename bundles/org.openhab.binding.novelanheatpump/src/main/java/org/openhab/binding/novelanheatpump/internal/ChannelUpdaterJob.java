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
package org.openhab.binding.novelanheatpump.internal;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.scheduler.SchedulerRunnable;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public class ChannelUpdaterJob implements SchedulerRunnable, Runnable {

    private final Thing thing;
    private final NovelanHeatpumpConfiguration config;
    private final Logger logger = LoggerFactory.getLogger(ChannelUpdaterJob.class);
    private final SimpleDateFormat sdateformat = new SimpleDateFormat("dd.MM.yy HH:mm"); //$NON-NLS-1$

    /** Parameter code for heating operation mode */
    public static final int PARAM_HEATING_OPERATION_MODE = 3;
    /** Parameter code for heating temperature */
    public static final int PARAM_HEATING_TEMPERATURE = 1;
    /** Parameter code for warmwater operation mode */
    public static final int PARAM_WARMWATER_OPERATION_MODE = 4;
    /** Parameter code for warmwater temperature */
    public static final int PARAM_WARMWATER_TEMPERATURE = 2;
    /** Parameter code for cooling operation mode */
    public static final int PARAM_COOLING_OPERATION_MODE = 108;
    /** Parameter code for cooling release temperature */
    public static final int PARAM_COOLING_RELEASE_TEMP = 110;
    /** Parameter code for target temp MK1 */
    public static final int PARAM_COOLING_INLET_TEMP = 132;
    /** Parameter code for start cooling after hours */
    public static final int PARAM_COOLING_START = 850;
    /** Parameter code for stop cooling after hours */
    public static final int PARAM_COOLING_STOP = 851;

    public ChannelUpdaterJob(Thing thing) {
        this.thing = thing;
        this.config = thing.getConfiguration().as(NovelanHeatpumpConfiguration.class);
    }

    public Thing getThing() {
        return thing;
    }

    @Override
    public void run() {
        // connect to heatpump and check if values can be fetched
        HeatpumpConnector connector = new HeatpumpConnector(config.ipAddress, config.port);

        try {
            connector.connect();

            // read all available values
            int[] heatpumpValues = connector.getValues();

            // all temperatures are 0.2 degree Celsius exact
            // but use int to save values
            // example 124 is 12.4 degree Celsius

            // workaround for thermal energies
            // the thermal energies can be unreasonably high in some cases, probably due to a sign bug in the firmware
            // trying to correct this issue here
            if (heatpumpValues[151] >= 214748364)
                heatpumpValues[151] -= 214748364;
            if (heatpumpValues[152] >= 214748364)
                heatpumpValues[152] -= 214748364;
            if (heatpumpValues[153] >= 214748364)
                heatpumpValues[153] -= 214748364;
            if (heatpumpValues[154] >= 214748364)
                heatpumpValues[154] -= 214748364;

            handleEventType(new QuantityType<>((double) heatpumpValues[10] / 10, SIUnits.CELSIUS),
                    HeatpumpChannel.CHANNEL_TEMPERATURE_SUPPLAY);
            handleEventType(new QuantityType<>((double) heatpumpValues[11] / 10, SIUnits.CELSIUS),
                    HeatpumpChannel.CHANNEL_TEMPERATURE_RETURN);
            handleEventType(new QuantityType<>((double) heatpumpValues[12] / 10, SIUnits.CELSIUS),
                    HeatpumpChannel.CHANNEL_TEMPERATURE_REFERENCE_RETURN);
            handleEventType(new QuantityType<>((double) heatpumpValues[13] / 10, SIUnits.CELSIUS),
                    HeatpumpChannel.CHANNEL_TEMPERATURE_OUT_EXTERNAL);
            handleEventType(new QuantityType<>((double) heatpumpValues[14] / 10, SIUnits.CELSIUS),
                    HeatpumpChannel.CHANNEL_TEMPERATURE_HOT_GAS);
            handleEventType(new QuantityType<>((double) heatpumpValues[15] / 10, SIUnits.CELSIUS),
                    HeatpumpChannel.CHANNEL_TEMPERATURE_OUTSIDE);
            handleEventType(new QuantityType<>((double) heatpumpValues[16] / 10, SIUnits.CELSIUS),
                    HeatpumpChannel.CHANNEL_TEMPERATURE_OUTSIDE_AVG);
            handleEventType(new QuantityType<>((double) heatpumpValues[17] / 10, SIUnits.CELSIUS),
                    HeatpumpChannel.CHANNEL_TEMPERATURE_SERVICEWATER);
            handleEventType(new QuantityType<>((double) heatpumpValues[18] / 10, SIUnits.CELSIUS),
                    HeatpumpChannel.CHANNEL_TEMPERATURE_SERVICEWATER_REFERENCE);
            handleEventType(new QuantityType<>((double) heatpumpValues[19] / 10, SIUnits.CELSIUS),
                    HeatpumpChannel.CHANNEL_TEMPERATURE_PROBE_IN);
            handleEventType(new QuantityType<>((double) heatpumpValues[20] / 10, SIUnits.CELSIUS),
                    HeatpumpChannel.CHANNEL_TEMPERATURE_PROBE_OUT);
            handleEventType(new QuantityType<>((double) heatpumpValues[21] / 10, SIUnits.CELSIUS),
                    HeatpumpChannel.CHANNEL_TEMPERATURE_MK1);
            handleEventType(new QuantityType<>((double) heatpumpValues[22] / 10, SIUnits.CELSIUS),
                    HeatpumpChannel.CHANNEL_TEMPERATURE_MK1_REFERENCE);
            handleEventType(new QuantityType<>((double) heatpumpValues[24] / 10, SIUnits.CELSIUS),
                    HeatpumpChannel.CHANNEL_TEMPERATURE_MK2);
            handleEventType(new QuantityType<>((double) heatpumpValues[25] / 10, SIUnits.CELSIUS),
                    HeatpumpChannel.CHANNEL_TEMPERATURE_MK2_REFERENCE);
            handleEventType(new QuantityType<>((double) heatpumpValues[26] / 10, SIUnits.CELSIUS),
                    HeatpumpChannel.CHANNEL_HEATPUMP_SOLAR_COLLECTOR);
            handleEventType(new QuantityType<>((double) heatpumpValues[27] / 10, SIUnits.CELSIUS),
                    HeatpumpChannel.CHANNEL_HEATPUMP_SOLAR_STORAGE);
            handleEventType(new QuantityType<>((double) heatpumpValues[28] / 10, SIUnits.CELSIUS),
                    HeatpumpChannel.CHANNEL_TEMPERATURE_EXTERNAL_SOURCE);
            handleEventType(new QuantityType<>((double) heatpumpValues[56], Units.SECOND),
                    HeatpumpChannel.CHANNEL_HOURS_COMPRESSOR1);
            handleEventType(new DecimalType((double) heatpumpValues[57]), HeatpumpChannel.CHANNEL_STARTS_COMPRESSOR1);
            handleEventType(new QuantityType<>((double) heatpumpValues[58], Units.SECOND),
                    HeatpumpChannel.CHANNEL_HOURS_COMPRESSOR2);
            handleEventType(new DecimalType((double) heatpumpValues[59]), HeatpumpChannel.CHANNEL_STARTS_COMPRESSOR2);
            handleEventType(new QuantityType<>((double) heatpumpValues[60], Units.SECOND),
                    HeatpumpChannel.CHANNEL_HOURS_ZWE1);
            handleEventType(new QuantityType<>((double) heatpumpValues[61], Units.SECOND),
                    HeatpumpChannel.CHANNEL_HOURS_ZWE2);
            handleEventType(new QuantityType<>((double) heatpumpValues[62], Units.SECOND),
                    HeatpumpChannel.CHANNEL_HOURS_ZWE3);
            handleEventType(new QuantityType<>((double) heatpumpValues[63], Units.SECOND),
                    HeatpumpChannel.CHANNEL_HOURS_HETPUMP);
            handleEventType(new QuantityType<>((double) heatpumpValues[64], Units.SECOND),
                    HeatpumpChannel.CHANNEL_HOURS_HEATING);
            handleEventType(new QuantityType<>((double) heatpumpValues[65], Units.SECOND),
                    HeatpumpChannel.CHANNEL_HOURS_WARMWATER);
            handleEventType(new QuantityType<>((double) heatpumpValues[66], Units.SECOND),
                    HeatpumpChannel.CHANNEL_HOURS_COOLING);
            handleEventType(new QuantityType<>((double) heatpumpValues[151] / 10, Units.KILOWATT_HOUR),
                    HeatpumpChannel.CHANNEL_THERMALENERGY_HEATING);
            handleEventType(new QuantityType<>((double) heatpumpValues[152] / 10, Units.KILOWATT_HOUR),
                    HeatpumpChannel.CHANNEL_THERMALENERGY_WARMWATER);
            handleEventType(new QuantityType<>((double) heatpumpValues[153] / 10, Units.KILOWATT_HOUR),
                    HeatpumpChannel.CHANNEL_THERMALENERGY_POOL);
            handleEventType(new QuantityType<>((double) heatpumpValues[154] / 10, Units.KILOWATT_HOUR),
                    HeatpumpChannel.CHANNEL_THERMALENERGY_TOTAL);
            handleEventType(new DecimalType((double) heatpumpValues[155]), HeatpumpChannel.CHANNEL_MASSFLOW);

            String heatpumpSimpleState = HeatpumpState.getStateByNumber(heatpumpValues[117]).toString();
            String heatpumpState = heatpumpSimpleState + ": " + getStateTime(heatpumpValues); //$NON-NLS-1$
            handleEventType(new StringType(heatpumpState), HeatpumpChannel.CHANNEL_HEATPUMP_STATE);
            handleEventType(new StringType(heatpumpSimpleState), HeatpumpChannel.CHANNEL_HEATPUMP_SIMPLE_STATE);
            handleEventType(new DecimalType(heatpumpValues[117]), HeatpumpChannel.CHANNEL_HEATPUMP_SIMPLE_STATE_NUM);

            handleEventType(new DecimalType(heatpumpValues[106]), HeatpumpChannel.CHANNEL_HEATPUMP_SWITCHOFF_REASON_0);

            handleEventType(new DecimalType(heatpumpValues[100]), HeatpumpChannel.CHANNEL_HEATPUMP_SWITCHOFF_CODE_0);

            String heatpumpExtendedState = HeatpumpState.getExtendedStateByNumber(heatpumpValues[119]).toString() + ": " //$NON-NLS-1$
                    + formatHours(heatpumpValues[120]);
            handleEventType(new StringType(heatpumpExtendedState), HeatpumpChannel.CHANNEL_HEATPUMP_EXTENDED_STATE);

            // read all parameters
            int[] heatpumpParams = connector.getParams();

            handleEventType(new QuantityType<>(heatpumpParams[PARAM_HEATING_TEMPERATURE] / 10, SIUnits.CELSIUS),
                    HeatpumpChannel.CHANNEL_HEATING_TEMPERATURE);
            handleEventType(new DecimalType(heatpumpParams[PARAM_HEATING_OPERATION_MODE]),
                    HeatpumpChannel.CHANNEL_HEATING_OPERATION_MODE);
            handleEventType(new QuantityType<>(heatpumpParams[PARAM_WARMWATER_TEMPERATURE] / 10, SIUnits.CELSIUS),
                    HeatpumpChannel.CHANNEL_WARMWATER_TEMPERATURE);
            handleEventType(new DecimalType(heatpumpParams[PARAM_WARMWATER_OPERATION_MODE]),
                    HeatpumpChannel.CHANNEL_WARMWATER_OPERATION_MODE);
            handleEventType(new DecimalType(heatpumpParams[PARAM_COOLING_OPERATION_MODE]),
                    HeatpumpChannel.CHANNEL_COOLING_OPERATION_MODE);
            handleEventType(new QuantityType<>(heatpumpParams[PARAM_COOLING_RELEASE_TEMP] / 10, SIUnits.CELSIUS),
                    HeatpumpChannel.CHANNEL_COOLING_RELEASE_TEMPERATURE);
            handleEventType(new QuantityType<>(heatpumpParams[PARAM_COOLING_INLET_TEMP] / 10, SIUnits.CELSIUS),
                    HeatpumpChannel.CHANNEL_COOLING_INLET_TEMP);
            handleEventType(new DecimalType(heatpumpParams[PARAM_COOLING_START] / 10),
                    HeatpumpChannel.CHANNEL_COOLING_START_AFTER_HOURS);
            handleEventType(new DecimalType(heatpumpParams[PARAM_COOLING_STOP] / 10),
                    HeatpumpChannel.CHANNEL_COOLING_STOP_AFTER_HOURS);

            // read all boolean output signals
            handleEventType((heatpumpValues[37] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_AV);
            handleEventType((heatpumpValues[38] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_BUP);
            handleEventType((heatpumpValues[39] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_HUP);
            handleEventType((heatpumpValues[40] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_MA1);
            handleEventType((heatpumpValues[41] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_MZ1);
            handleEventType((heatpumpValues[42] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_VEN);
            handleEventType((heatpumpValues[43] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_VBO);
            handleEventType((heatpumpValues[44] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_VD1);
            handleEventType((heatpumpValues[45] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_VD2);
            handleEventType((heatpumpValues[46] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_ZIP);
            handleEventType((heatpumpValues[47] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_ZUP);
            handleEventType((heatpumpValues[48] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_ZW1);
            handleEventType((heatpumpValues[49] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_ZW2SST);
            handleEventType((heatpumpValues[50] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_ZW3SST);
            handleEventType((heatpumpValues[51] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_FP2);
            handleEventType((heatpumpValues[52] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_SLP);
            handleEventType((heatpumpValues[53] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_SUP);
            handleEventType((heatpumpValues[54] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_MZ2);
            handleEventType((heatpumpValues[55] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_MA2);
            handleEventType((heatpumpValues[138] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_MZ3);
            handleEventType((heatpumpValues[139] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_MA3);
            handleEventType((heatpumpValues[140] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_FP3);
            handleEventType((heatpumpValues[166] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_VSK);
            handleEventType((heatpumpValues[167] == 0) ? OnOffType.OFF : OnOffType.ON,
                    HeatpumpChannel.CHANNEL_OUTPUT_FRH);

            if (heatpumpValues.length > 213) {
                handleEventType((heatpumpValues[213] == 0) ? OnOffType.OFF : OnOffType.ON,
                        HeatpumpChannel.CHANNEL_OUTPUT_AV2);
                handleEventType((heatpumpValues[214] == 0) ? OnOffType.OFF : OnOffType.ON,
                        HeatpumpChannel.CHANNEL_OUTPUT_VBO2);
                handleEventType((heatpumpValues[215] == 0) ? OnOffType.OFF : OnOffType.ON,
                        HeatpumpChannel.CHANNEL_OUTPUT_VD12);
                handleEventType((heatpumpValues[216] == 0) ? OnOffType.OFF : OnOffType.ON,
                        HeatpumpChannel.CHANNEL_OUTPUT_VDH2);
            }

        } catch (Exception e) {
            logger.warn("Could not connect to heatpump (uuid={}, ip={}, port={}): {}", thing.getUID(), config.ipAddress,
                    config.port, e.getStackTrace());
        } finally {
            connector.disconnect();
        }
    }

    private void handleEventType(org.openhab.core.types.State state, HeatpumpChannel heatpumpCommandType) {
        NovelanHeatpumpHandler handler = NovelanHeatpumpHandlerFactory.getHandler(thing.getUID().toString());
        handler.updateState(heatpumpCommandType.command, state);
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

    private String formatHours(int value) {
        String returnValue = "";
        returnValue += String.format("%02d:", new Object[] { Integer.valueOf(value / 3600) }); //$NON-NLS-1$
        value %= 3600;
        returnValue += String.format("%02d:", new Object[] { Integer.valueOf(value / 60) }); //$NON-NLS-1$
        value %= 60;
        returnValue += String.format("%02d", new Object[] { Integer.valueOf(value) }); //$NON-NLS-1$
        return returnValue;
    }
}
