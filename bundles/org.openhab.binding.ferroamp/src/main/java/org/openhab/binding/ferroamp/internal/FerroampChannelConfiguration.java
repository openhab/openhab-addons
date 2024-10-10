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
package org.openhab.binding.ferroamp.internal;

import java.util.ArrayList;
import java.util.List;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.Units;

/**
 * The {@link FerroampChannelConfiguration} class defines methods, which set up channel configuration for the binding.
 *
 * @author Örjan Backsell - Initial contribution
 *
 */

@NonNullByDefault
public class FerroampChannelConfiguration {
    public String id;
    public Unit<?> unit;

    public FerroampChannelConfiguration(String id, Unit<?> unit) {
        this.id = id;
        this.unit = unit;
    }

    public static List<FerroampChannelConfiguration> getChannelConfigurationEhub() {
        final List<FerroampChannelConfiguration> channelConfigurationEhub = new ArrayList<>();
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_GRIDFREQUENCY, Units.HERTZ));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ACECURRENTL1, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ACECURRENTL2, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ACECURRENTL3, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EXTERNALVOLTAGEL1, Units.VOLT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EXTERNALVOLTAGEL2, Units.VOLT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EXTERNALVOLTAGEL3, Units.VOLT));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_INVERTERRMSCURRENTL1, Units.AMPERE));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_INVERTERRMSCURRENTL2, Units.AMPERE));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_INVERTERRMSCURRENTL3, Units.AMPERE));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_INVERTERCURRENTREACTIVEL1, Units.AMPERE));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_INVERTERCURRENTREACTIVEL2, Units.AMPERE));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_INVERTERCURRENTREACTIVEL3, Units.AMPERE));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_INVERTERCURRENTACTIVEL1, Units.AMPERE));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_INVERTERCURRENTACTIVEL2, Units.AMPERE));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_INVERTERCURRENTACTIVEL3, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_GRIDCURRENTL1, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_GRIDCURRENTL2, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_GRIDCURRENTL3, Units.AMPERE));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_GRIDCURRENTREACTIVEL1, Units.AMPERE));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_GRIDCURRENTREACTIVEL2, Units.AMPERE));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_GRIDCURRENTREACTIVEL3, Units.AMPERE));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_GRIDCURRENTACTIVEL1, Units.AMPERE));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_GRIDCURRENTACTIVEL2, Units.AMPERE));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_GRIDCURRENTACTIVEL3, Units.AMPERE));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_INVERTERREACTIVECURRENTL1, Units.AMPERE));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_INVERTERREACTIVECURRENTL2, Units.AMPERE));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_INVERTERREACTIVECURRENTL3, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_INVERTERLOADL1, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_INVERTERLOADL2, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_INVERTERLOADL3, Units.AMPERE));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_APPARENTPOWER, Units.VOLT_AMPERE));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_GRIDPOWERACTIVEL1, Units.AMPERE));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_GRIDPOWERACTIVEL2, Units.AMPERE));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_GRIDPOWERACTIVEL3, Units.AMPERE));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_GRIDPOWERREACTIVEL1, Units.WATT));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_GRIDPOWERREACTIVEL2, Units.WATT));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_GRIDPOWERREACTIVEL3, Units.WATT));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_INVERTERPOWERACTIVEL1, Units.VOLT));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_INVERTERPOWERACTIVEL2, Units.VOLT));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_INVERTERPOWERACTIVEL3, Units.VOLT));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_INVERTERPOWERREACTIVEL1, Units.VOLT));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_INVERTERPOWERREACTIVEL2, Units.VOLT));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_INVERTERPOWERREACTIVEL3, Units.VOLT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_CONSUMPTIONPOWERL1, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_CONSUMPTIONPOWERL2, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_CONSUMPTIONPOWERL3, Units.WATT));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_CONSUMPTIONPOWERREACTIVEL1, Units.WATT));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_CONSUMPTIONPOWERREACTIVEL2, Units.WATT));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_CONSUMPTIONPOWERREACTIVEL3, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SOLARPV, Units.WATT));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_POSITIVEDCLINKVOLTAGE, Units.VOLT));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_NEGATIVEDCLINKVOLTAGE, Units.VOLT));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_EXTERNALENERGYPRODUCEDL1, Units.WATT));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_EXTERNALENERGYPRODUCEDL2, Units.WATT));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_EXTERNALENERGYPRODUCEDL3, Units.WATT));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_EXTERNALENERGYCONSUMEDL1, Units.WATT));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_EXTERNALENERGYCONSUMEDL2, Units.WATT));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_EXTERNALENERGYCONSUMEDL3, Units.WATT));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_INVERTERENERGYPRODUCEDL1, Units.WATT));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_INVERTERENERGYPRODUCEDL2, Units.WATT));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_INVERTERENERGYPRODUCEDL3, Units.WATT));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_INVERTERENERGYCONSUMEDL1, Units.WATT));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_INVERTERENERGYCONSUMEDL2, Units.WATT));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_INVERTERENERGYCONSUMEDL3, Units.WATT));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_LOADENERGYPRODUCEDL1, Units.WATT));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_LOADENERGYPRODUCEDL2, Units.WATT));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_LOADENERGYPRODUCEDL3, Units.WATT));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_LOADENERGYCONSUMEDL1, Units.WATT));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_LOADENERGYCONSUMEDL2, Units.WATT));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_LOADENERGYCONSUMEDL3, Units.WATT));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_EXTERNALENERGYPRODUCED3P, Units.WATT));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_EXTERNALENERGYCONSUMED3P, Units.WATT));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_INVERTERENERGYPRODUCED3P, Units.WATT));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_INVERTERENERGYCONSUMED3P, Units.WATT));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_LOADENERGYPRODUCED3P, Units.WATT));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_LOADENERGYCONSUMED3P, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_TOTALSOLARENERGY, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_STATE, Units.ONE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_TIMESTAMP, Units.ONE));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_BATTERYENERGYPRODUCED, Units.WATT));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_BATTERYENERGYCONSUMED, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SOC, Units.PERCENT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SOH, Units.PERCENT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_POWERBATTERY, Units.WATT));
        channelConfigurationEhub.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_TOTALRATEDCAPACITYALLBATTERIES, Units.WATT_HOUR));
        return channelConfigurationEhub;
    }

    public static List<FerroampChannelConfiguration> getChannelConfigurationSsoS0() {
        final List<FerroampChannelConfiguration> channelConfigurationSsoS0 = new ArrayList<>();
        channelConfigurationSsoS0
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S0ID, Units.ONE));
        channelConfigurationSsoS0.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_S0MEASUREDVOLTAGEPVSTRING, Units.VOLT));
        channelConfigurationSsoS0.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_S0MEASUREDCURRENTPVSTRING, Units.AMPERE));
        channelConfigurationSsoS0
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S0TOTALSOLARENERGY, Units.WATT));
        channelConfigurationSsoS0
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S0RELAYSTATUS, Units.ONE));
        channelConfigurationSsoS0
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S0TEMPERATURE, Units.ONE));
        channelConfigurationSsoS0
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S0FAULTCODE, Units.ONE));
        channelConfigurationSsoS0
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S0DCLINKVOLTAGE, Units.VOLT));
        channelConfigurationSsoS0
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S0TIMESTAMP, Units.ONE));
        return channelConfigurationSsoS0;
    }

    public static List<FerroampChannelConfiguration> getChannelConfigurationSsoS1() {
        final List<FerroampChannelConfiguration> channelConfigurationSsoS1 = new ArrayList<>();
        channelConfigurationSsoS1
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S1ID, Units.ONE));
        channelConfigurationSsoS1.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_S1MEASUREDVOLTAGEPVSTRING, Units.VOLT));
        channelConfigurationSsoS1.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_S1MEASUREDCURRENTPVSTRING, Units.AMPERE));
        channelConfigurationSsoS1
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S1TOTALSOLARENERGY, Units.WATT));
        channelConfigurationSsoS1
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S1RELAYSTATUS, Units.ONE));
        channelConfigurationSsoS1
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S1TEMPERATURE, Units.ONE));
        channelConfigurationSsoS1
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S1FAULTCODE, Units.ONE));
        channelConfigurationSsoS1
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S1DCLINKVOLTAGE, Units.VOLT));
        channelConfigurationSsoS1
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S1TIMESTAMP, Units.ONE));
        return channelConfigurationSsoS1;
    }

    public static List<FerroampChannelConfiguration> getChannelConfigurationSsoS2() {
        final List<FerroampChannelConfiguration> channelConfigurationSsoS2 = new ArrayList<>();
        channelConfigurationSsoS2
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S2ID, Units.ONE));
        channelConfigurationSsoS2.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_S2MEASUREDVOLTAGEPVSTRING, Units.VOLT));
        channelConfigurationSsoS2.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_S2MEASUREDCURRENTPVSTRING, Units.AMPERE));
        channelConfigurationSsoS2
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S2TOTALSOLARENERGY, Units.WATT));
        channelConfigurationSsoS2
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S2RELAYSTATUS, Units.ONE));
        channelConfigurationSsoS2
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S2TEMPERATURE, Units.ONE));
        channelConfigurationSsoS2
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S2FAULTCODE, Units.ONE));
        channelConfigurationSsoS2
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S2DCLINKVOLTAGE, Units.VOLT));
        channelConfigurationSsoS2
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S2TIMESTAMP, Units.ONE));
        return channelConfigurationSsoS2;
    }

    public static List<FerroampChannelConfiguration> getChannelConfigurationSsoS3() {
        final List<FerroampChannelConfiguration> channelConfigurationSsoS3 = new ArrayList<>();
        channelConfigurationSsoS3
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S3ID, Units.ONE));
        channelConfigurationSsoS3.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_S3MEASUREDVOLTAGEPVSTRING, Units.VOLT));
        channelConfigurationSsoS3.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_S3MEASUREDCURRENTPVSTRING, Units.AMPERE));
        channelConfigurationSsoS3
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S3TOTALSOLARENERGY, Units.WATT));
        channelConfigurationSsoS3
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S3RELAYSTATUS, Units.ONE));
        channelConfigurationSsoS3
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S3TEMPERATURE, Units.ONE));
        channelConfigurationSsoS3
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S3FAULTCODE, Units.ONE));
        channelConfigurationSsoS3
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S3DCLINKVOLTAGE, Units.VOLT));
        channelConfigurationSsoS3
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_S3TIMESTAMP, Units.ONE));
        return channelConfigurationSsoS3;
    }

    public static List<FerroampChannelConfiguration> getChannelConfigurationEso() {
        final List<FerroampChannelConfiguration> channelConfigurationEso = new ArrayList<>();
        channelConfigurationEso
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESOUNIQUEIDENTIFIER, Units.ONE));
        channelConfigurationEso.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_ESOMEASUREDVOLTAGEBATTERY, Units.VOLT));
        channelConfigurationEso.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_ESOMEASUREDCURRENTBATTERY, Units.AMPERE));
        channelConfigurationEso.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_ESOBATTERYENERGYPRODUCED, Units.WATT));
        channelConfigurationEso.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_ESOBATTERYENERGYCONSUMED, Units.WATT));
        channelConfigurationEso
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESOSOC, Units.PERCENT));
        channelConfigurationEso
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESORELAYSTATUS, Units.ONE));
        channelConfigurationEso
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESOTEMPERATURE, Units.ONE));
        channelConfigurationEso
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESOFAULTCODE, Units.ONE));
        channelConfigurationEso
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESODCLINKVOLTAGE, Units.VOLT));
        channelConfigurationEso
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESOTIMESTAMP, Units.ONE));
        return channelConfigurationEso;
    }

    public static List<FerroampChannelConfiguration> getChannelConfigurationEsm() {
        final List<FerroampChannelConfiguration> channelConfigurationEsm = new ArrayList<>();
        channelConfigurationEsm
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESMUNIQUEIDENTIFIER, Units.ONE));
        channelConfigurationEsm
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESMSOH, Units.PERCENT));
        channelConfigurationEsm
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESMSOC, Units.PERCENT));
        channelConfigurationEsm.add(new FerroampChannelConfiguration(
                FerroampBindingConstants.CHANNEL_ESMTOTALRATEDCAPACITYALLBATTERIES, Units.WATT_HOUR));
        channelConfigurationEsm.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESMRATEDPOWERBATTERY, Units.WATT));
        channelConfigurationEsm
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESMSTATUS, Units.ONE));
        channelConfigurationEsm
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESMTIMESTAMP, Units.ONE));
        return channelConfigurationEsm;
    }

    public static List<FerroampChannelConfiguration> getChannelConfigurationRequest() {
        final List<FerroampChannelConfiguration> channelConfigurationRequest = new ArrayList<>();
        channelConfigurationRequest
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_REQUESTCHARGE, Units.ONE));
        channelConfigurationRequest
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_REQUESTDISCHARGE, Units.ONE));
        channelConfigurationRequest
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_AUTO, Units.ONE));
        channelConfigurationRequest.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_REQUESTEXTAPIVERSION, Units.ONE));
        return channelConfigurationRequest;
    }
}