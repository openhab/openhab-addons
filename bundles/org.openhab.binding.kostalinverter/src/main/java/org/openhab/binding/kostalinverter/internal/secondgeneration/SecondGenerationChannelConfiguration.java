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
package org.openhab.binding.kostalinverter.internal.secondgeneration;

import java.util.ArrayList;
import java.util.List;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * The {@link SecondGenerationChannelConfiguration} class defines methods, which set up channel configuration,
 * used in the second generation part of the binding.
 *
 *
 * @author Christian Schneider - Initial contribution
 * @author Christoph Weitkamp - Incorporated new QuantityType (Units of Measurement)
 * @author Örjan Backsell - Added channels for (Piko1020, Piko New Generation)
 */

@NonNullByDefault
public class SecondGenerationChannelConfiguration {
    public String id;
    public String tag;
    public int num;
    public Unit<?> unit;
    public String dxsEntries;

    public SecondGenerationChannelConfiguration(String id, String tag, int num, Unit<?> unit, String dxsEntries) {
        this.id = id;
        this.tag = tag;
        this.num = num;
        this.unit = unit;
        this.dxsEntries = dxsEntries;
    }

    public static List<SecondGenerationChannelConfiguration> getChannelConfiguration() {
        final List<SecondGenerationChannelConfiguration> channelConfiguration = new ArrayList<>();

        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_GRIDOUTPUTPOWER, "td", 4, Units.WATT, "67109120"));
        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_YIELD_DAY_SECOND_GEN, "td", 7, Units.WATT_HOUR, "251658754"));
        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_YIELD_TOTAL_SECOND_GEN, "td", 10, Units.KILOWATT_HOUR,
                "251658753"));
        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_OPERATING_STATUS, "td", 13, Units.ONE, "16780032"));
        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_GRIDVOLTAGEL1, "td", 16, Units.VOLT, "67109378"));
        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_GRIDCURRENTL1, "td", 19, Units.AMPERE, "67109377"));
        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_GRIDPOWERL1, "td", 22, Units.WATT, "67109379"));
        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_GRIDVOLTAGEL2, "td", 25, Units.VOLT, "67109634"));
        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_GRIDCURRENTL2, "td", 28, Units.AMPERE, "67109633"));
        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_GRIDPOWERL2, "td", 31, Units.WATT, "67109635"));
        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_GRIDVOLTAGEL3, "td", 34, Units.VOLT, "67109890"));
        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_GRIDCURRENTL3, "td", 37, Units.AMPERE, "67109889"));
        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_GRIDPOWERL3, "td", 40, Units.WATT, "67109891"));
        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_DCPOWERPV, "td", 43, Units.WATT, "33556736"));
        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_DC1VOLTAGE, "td", 46, Units.VOLT, "33555202"));
        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_DC1CURRENT, "td", 49, Units.AMPERE, "33555201"));
        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_DC1POWER, "td", 52, Units.WATT, "33555203"));
        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_DC2VOLTAGE, "td", 55, Units.VOLT, "33555458"));
        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_DC2CURRENT, "td", 58, Units.AMPERE, "33555457"));
        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_DC2POWER, "td", 61, Units.WATT, "33555459"));
        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_DC3VOLTAGE, "td", 64, Units.VOLT, "33555714"));
        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_DC3CURRENT, "td", 67, Units.AMPERE, "33555713"));
        channelConfiguration.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_DC3POWER, "td", 70, Units.WATT, "33555715"));

        return channelConfiguration;
    }

    public static List<SecondGenerationChannelConfiguration> getChannelConfigurationExt() {
        final List<SecondGenerationChannelConfiguration> channelConfigurationExt = new ArrayList<>();

        channelConfigurationExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_AKTHOMECONSUMTIONSOLAR, "td", 73, Units.WATT, "83886336"));
        channelConfigurationExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_AKTHOMECONSUMPTIONBAT, "td", 76, Units.WATT, "83886592"));
        channelConfigurationExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_AKTHOMECONSUMPTIONGRID, "td", 79, Units.WATT, "83886848"));
        channelConfigurationExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_PHASESELHOMECONSUMPL1, "td", 82, Units.WATT, "83887106"));
        channelConfigurationExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_PHASESELHOMECONSUMPL2, "td", 85, Units.WATT, "83887362"));
        channelConfigurationExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_PHASESELHOMECONSUMPL3, "td", 88, Units.WATT, "83887618"));
        channelConfigurationExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_GRIDFREQ, "td", 91, Units.HERTZ, "67110400"));
        channelConfigurationExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_GRIDCOSPHI, "td", 94, Units.DEGREE_ANGLE, "67110656"));
        channelConfigurationExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_HOMECONSUMPTION_DAY, "td", 97, Units.WATT_HOUR, "251659010"));
        channelConfigurationExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_OWNCONSUMPTION_DAY, "td", 100, Units.WATT_HOUR, "251659266"));
        channelConfigurationExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_OWNCONSRATE_DAY, "td", 103, Units.PERCENT, "251659278"));
        channelConfigurationExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_AUTONOMYDEGREE_DAY, "td", 106, Units.PERCENT, "251659279"));
        channelConfigurationExt.add(
                new SecondGenerationChannelConfiguration(SecondGenerationBindingConstants.CHANNEL_HOMECONSUMPTION_TOTAL,
                        "td", 109, Units.WATT_HOUR, "251659009"));
        channelConfigurationExt.add(
                new SecondGenerationChannelConfiguration(SecondGenerationBindingConstants.CHANNEL_OWNCONSUMPTION_TOTAL,
                        "td", 112, Units.WATT_HOUR, "251659265"));
        channelConfigurationExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_OPERATINGTIME_TOTAL, "td", 115, Units.HOUR, "251658496"));
        channelConfigurationExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_CURRENT, "td", 118, Units.AMPERE, "33556238"));
        channelConfigurationExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_CURRENTDIR, "td", 121, Units.ONE, "33556230"));
        channelConfigurationExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_CHARGECYCLES, "td", 124, Units.ONE, "33556228"));
        channelConfigurationExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_BATTERYTEMPERATURE, "td", 127, SIUnits.CELSIUS, "33556227"));
        channelConfigurationExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_LOGINTERVAL, "td", 130, Units.MINUTE, "150995968"));
        channelConfigurationExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_S0INPULSECNT, "td", 133, Units.ONE, "184549632"));
        channelConfigurationExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_OWNCONSRATE_TOTAL, "td", 136, Units.PERCENT, "251659280"));
        channelConfigurationExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_AUTONOMYDEGREE_TOTAL, "td", 139, Units.PERCENT, "251659281"));

        return channelConfigurationExt;
    }

    public static List<SecondGenerationChannelConfiguration> getChannelConfigurationExtExt() {
        final List<SecondGenerationChannelConfiguration> channelConfigurationExtExt = new ArrayList<>();

        channelConfigurationExtExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_BATTERYVOLTAGE, "td", 142, Units.VOLT, "33556226"));
        channelConfigurationExtExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_BATSTATEOFCHARGE, "td", 145, Units.PERCENT, "33556229"));
        channelConfigurationExtExt.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_SELFCONSUMPTION, "td", 148, Units.WATT, "83888128"));
        return channelConfigurationExtExt;
    }

    public static List<SecondGenerationChannelConfiguration> getChannelConfigurationConfigurable() {
        final List<SecondGenerationChannelConfiguration> channelConfigurationConfigurable = new ArrayList<>();
        channelConfigurationConfigurable.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_BATTERYUSAGECONSUMPTION, "td", 151, Units.WATT, "33556249"));
        channelConfigurationConfigurable.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_SMARTBATTERYCONTROL, "td", 154, Units.ONE, "33556484"));
        channelConfigurationConfigurable.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_MAXDEPTHOFDISCHARGE, "td", 157, Units.ONE, "33556247"));
        channelConfigurationConfigurable.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_SHADOWMANAGEMENT, "td", 160, Units.ONE, "33556483"));
        channelConfigurationConfigurable.add(new SecondGenerationChannelConfiguration(
                SecondGenerationBindingConstants.CHANNEL_EXTERNALMODULECONTROL, "td", 163, Units.ONE, "33556482"));
        return channelConfigurationConfigurable;
    }
}
