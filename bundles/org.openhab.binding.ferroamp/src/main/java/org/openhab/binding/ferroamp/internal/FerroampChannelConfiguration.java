/**
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ferroamp.internal;

import java.util.ArrayList;
import java.util.List;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.Units;

/**
 * The {@link FerroampChannelConfiguration} class defines methods, that set the channel configuration for the binding.
 *
 * @author Örjan Backsell - Initial contribution
 *
 */

@NonNullByDefault
public class FerroampChannelConfiguration {

    public static final String id = "";

    public FerroampChannelConfiguration(String id, Unit<?> unit) {

    }

    private static FerroampChannelConfiguration cc(String id, Unit<?> unit) {
        return new FerroampChannelConfiguration(id, unit);
    }

    public static List<FerroampChannelConfiguration> getChannelConfigurationEhub() {
        final List<FerroampChannelConfiguration> list = new ArrayList<>();

        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDFREQUENCY, Units.HERTZ));
        list.add(cc(FerroampBindingConstants.CHANNEL_ACECURRENTL1, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_ACECURRENTL2, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_ACECURRENTL3, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDVOLTAGEL1, Units.VOLT));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDVOLTAGEL2, Units.VOLT));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDVOLTAGEL3, Units.VOLT));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERRMSCURRENTL1, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERRMSCURRENTL2, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERRMSCURRENTL3, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERREACTIVECURRENTL1, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERREACTIVECURRENTL2, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERREACTIVECURRENTL3, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERACTIVECURRENTL1, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERACTIVECURRENTL2, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERACTIVECURRENTL3, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDCURRENTL1, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDCURRENTL2, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDCURRENTL3, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDREACTIVECURRENTL1, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDREACTIVECURRENTL2, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDREACTIVECURRENTL3, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDACTIVECURRENTL1, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDACTIVECURRENTL2, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDACTIVECURRENTL3, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERLOADREACTIVECURRENTL1, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERLOADREACTIVECURRENTL2, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERLOADREACTIVECURRENTL3, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERLOADACTIVECURRENTL1, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERLOADACTIVECURRENTL2, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERLOADACTIVECURRENTL3, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_APPARENTPOWER, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDPOWERACTIVEL1, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDPOWERACTIVEL2, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDPOWERACTIVEL3, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDPOWERREACTIVEL1, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDPOWERREACTIVEL2, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDPOWERREACTIVEL3, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERPOWERACTIVEL1, Units.VOLT));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERPOWERACTIVEL2, Units.VOLT));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERPOWERACTIVEL3, Units.VOLT));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERPOWERREACTIVEL1, Units.VOLT));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERPOWERREACTIVEL2, Units.VOLT));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERPOWERREACTIVEL3, Units.VOLT));
        list.add(cc(FerroampBindingConstants.CHANNEL_CONSUMPTIONPOWERL1, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_CONSUMPTIONPOWERL2, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_CONSUMPTIONPOWERL3, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_CONSUMPTIONPOWERREACTIVEL1, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_CONSUMPTIONPOWERREACTIVEL2, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_CONSUMPTIONPOWERREACTIVEL3, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_SOLARPV, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_POSITIVEDCLINKVOLTAGE, Units.VOLT));
        list.add(cc(FerroampBindingConstants.CHANNEL_NEGATIVEDCLINKVOLTAGE, Units.VOLT));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDENERGYPRODUCEDL1, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDENERGYPRODUCEDL2, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDENERGYPRODUCEDL3, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDENERGYCONSUMEDL1, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDENERGYCONSUMEDL2, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDENERGYCONSUMEDL3, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERENERGYPRODUCEDL1, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERENERGYPRODUCEDL2, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERENERGYPRODUCEDL3, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERENERGYCONSUMEDL1, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERENERGYCONSUMEDL2, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERENERGYCONSUMEDL3, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_LOADENERGYPRODUCEDL1, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_LOADENERGYPRODUCEDL2, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_LOADENERGYPRODUCEDL3, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_LOADENERGYCONSUMEDL1, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_LOADENERGYCONSUMEDL2, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_LOADENERGYCONSUMEDL3, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDENERGYPRODUCEDTOTAL, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_GRIDENERGYCONSUMEDTOTAL, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERENERGYPRODUCEDTOTAL, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_INVERTERENERGYCONSUMEDTOTAL, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_LOADENERGYPRODUCEDTOTAL, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_LOADENERGYCONSUMEDTOTAL, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_TOTALSOLARENERGY, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_STATE, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_TIMESTAMP, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_BATTERYENERGYPRODUCED, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_BATTERYENERGYCONSUMED, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_SOC, Units.PERCENT));
        list.add(cc(FerroampBindingConstants.CHANNEL_SOH, Units.PERCENT));
        list.add(cc(FerroampBindingConstants.CHANNEL_POWERBATTERY, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_TOTALCAPACITYBATTERIES, Units.WATT_HOUR));
        return list;
    }

    public static List<FerroampChannelConfiguration> getChannelConfigurationSsoS1() {
        final List<FerroampChannelConfiguration> list = new ArrayList<>();
        list.add(cc(FerroampBindingConstants.CHANNEL_S1ID, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_S1PVVOLTAGE, Units.VOLT));
        list.add(cc(FerroampBindingConstants.CHANNEL_S1PVCURRENT, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_S1TOTALSOLARENERGY, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_S1RELAYSTATUS, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_S1TEMPERATURE, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_S1FAULTCODE, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_S1DCLINKVOLTAGE, Units.VOLT));
        list.add(cc(FerroampBindingConstants.CHANNEL_S1TIMESTAMP, Units.ONE));
        return list;
    }

    public static List<FerroampChannelConfiguration> getChannelConfigurationSsoS2() {
        final List<FerroampChannelConfiguration> list = new ArrayList<>();
        list.add(cc(FerroampBindingConstants.CHANNEL_S2ID, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_S2PVVOLTAGE, Units.VOLT));
        list.add(cc(FerroampBindingConstants.CHANNEL_S2PVCURRENT, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_S2TOTALSOLARENERGY, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_S2RELAYSTATUS, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_S2TEMPERATURE, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_S2FAULTCODE, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_S2DCLINKVOLTAGE, Units.VOLT));
        list.add(cc(FerroampBindingConstants.CHANNEL_S2TIMESTAMP, Units.ONE));
        return list;
    }

    public static List<FerroampChannelConfiguration> getChannelConfigurationSsoS3() {
        final List<FerroampChannelConfiguration> list = new ArrayList<>();
        list.add(cc(FerroampBindingConstants.CHANNEL_S3ID, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_S3PVVOLTAGE, Units.VOLT));
        list.add(cc(FerroampBindingConstants.CHANNEL_S3PVCURRENT, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_S3TOTALSOLARENERGY, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_S3RELAYSTATUS, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_S3TEMPERATURE, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_S3FAULTCODE, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_S3DCLINKVOLTAGE, Units.VOLT));
        list.add(cc(FerroampBindingConstants.CHANNEL_S3TIMESTAMP, Units.ONE));
        return list;
    }

    public static List<FerroampChannelConfiguration> getChannelConfigurationSsoS4() {
        final List<FerroampChannelConfiguration> list = new ArrayList<>();
        list.add(cc(FerroampBindingConstants.CHANNEL_S4ID, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_S4PVVOLTAGE, Units.VOLT));
        list.add(cc(FerroampBindingConstants.CHANNEL_S4PVCURRENT, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_S4TOTALSOLARENERGY, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_S4RELAYSTATUS, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_S4TEMPERATURE, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_S4FAULTCODE, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_S4DCLINKVOLTAGE, Units.VOLT));
        list.add(cc(FerroampBindingConstants.CHANNEL_S4TIMESTAMP, Units.ONE));
        return list;
    }

    public static List<FerroampChannelConfiguration> getChannelConfigurationEso() {
        final List<FerroampChannelConfiguration> list = new ArrayList<>();
        list.add(cc(FerroampBindingConstants.CHANNEL_ESOID, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_ESOVOLTAGEBATTERY, Units.VOLT));
        list.add(cc(FerroampBindingConstants.CHANNEL_ESOCURRENTBATTERY, Units.AMPERE));
        list.add(cc(FerroampBindingConstants.CHANNEL_ESOBATTERYENERGYPRODUCED, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_ESOBATTERYENERGYCONSUMED, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_ESOSOC, Units.PERCENT));
        list.add(cc(FerroampBindingConstants.CHANNEL_ESORELAYSTATUS, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_ESOTEMPERATURE, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_ESOFAULTCODE, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_ESODCLINKVOLTAGE, Units.VOLT));
        list.add(cc(FerroampBindingConstants.CHANNEL_ESOTIMESTAMP, Units.ONE));
        return list;
    }

    public static List<FerroampChannelConfiguration> getChannelConfigurationEsm() {
        final List<FerroampChannelConfiguration> list = new ArrayList<>();
        list.add(cc(FerroampBindingConstants.CHANNEL_ESMID, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_ESMSOH, Units.PERCENT));
        list.add(cc(FerroampBindingConstants.CHANNEL_ESMSOC, Units.PERCENT));
        list.add(cc(FerroampBindingConstants.CHANNEL_ESMTOTALCAPACITY, Units.WATT_HOUR));
        list.add(cc(FerroampBindingConstants.CHANNEL_ESMPOWERBATTERY, Units.WATT));
        list.add(cc(FerroampBindingConstants.CHANNEL_ESMSTATUS, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_ESMTIMESTAMP, Units.ONE));
        return list;
    }

    public static List<FerroampChannelConfiguration> getChannelConfigurationRequest() {
        final List<FerroampChannelConfiguration> list = new ArrayList<>();
        list.add(cc(FerroampBindingConstants.CHANNEL_REQUESTCHARGE, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_REQUESTDISCHARGE, Units.ONE));
        list.add(cc(FerroampBindingConstants.CHANNEL_AUTO, Units.ONE));
        return list;
    }
}
