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
 * @Author Joel Backsell - Defined channels
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
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWLOADCONSQL1, Units.JOULE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWLOADCONSQL2, Units.JOULE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWLOADCONSQL3, Units.JOULE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBILOADDL1, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBILOADDL2, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBILOADDL3, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWINVCONSQ_3P, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWEXTCONSQL1, Units.JOULE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWEXTCONSQL2, Units.JOULE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWEXTCONSQL3, Units.JOULE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWINVPRODQ_3P, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWINVCONSQL1, Units.JOULE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWINVCONSQL2, Units.JOULE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWINVCONSQL3, Units.JOULE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBIEXTL1, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBIEXTL2, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBIEXTL3, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBILOADQL1, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBILOADQL2, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBILOADQL3, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWLOADPRODQ_3P, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBIACEL1, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBIACEL2, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBIACEL3, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBPLOADL1, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBPLOADL2, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBPLOADL3, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBPINVREACTIVEL1, Units.VOLT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBPINVREACTIVEL2, Units.VOLT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBPINVREACTIVEL3, Units.VOLT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBTS, Units.ONE));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBPLOADREACTIVEL1, Units.WATT));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBPLOADREACTIVEL2, Units.WATT));
        channelConfigurationEhub.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBPLOADREACTIVEL3, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBSTATE, Units.ONE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWLOADPRODQL1, Units.JOULE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWLOADPRODQL2, Units.JOULE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWLOADPRODQL3, Units.JOULE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBPPV, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBPINVL1, Units.VOLT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBPINVL2, Units.VOLT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBPINVL3, Units.VOLT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBIEXTQL1, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBIEXTQL2, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBIEXTQL3, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBPEXTL1, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBPEXTL2, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBPEXTL3, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWEXTPRODQL1, Units.JOULE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWEXTPRODQL2, Units.JOULE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWEXTPRODQL3, Units.JOULE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWPV, Units.JOULE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBPEXTREACTIVEL1, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBPEXTREACTIVEL2, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBPEXTREACTIVEL3, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBUDCPOS, Units.VOLT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBUDCNEG, Units.VOLT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBSEXT, Units.VOLT_AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBIEXTDL1, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBIEXTDL2, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBIEXTDL3, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWEXTCONSQ_3P, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBILDL1, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBILDL2, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBILDL3, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBGRIDFREQ, Units.HERTZ));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWLOADCONSQ_3P, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBULL1, Units.VOLT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBULL2, Units.VOLT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBULL3, Units.VOLT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWEXTPRODQ_3P, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBILQL1, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBILQL2, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBILQL3, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWINVPRODQL1, Units.JOULE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWINVPRODQL2, Units.JOULE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWINVPRODQL3, Units.JOULE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBILL1, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBILL2, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBILL3, Units.AMPERE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWBATPROD, Units.JOULE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBWBATCONS, Units.JOULE));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBSOC, Units.PERCENT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBSOH, Units.PERCENT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBPBAT, Units.WATT));
        channelConfigurationEhub
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_EHUBRATEDCAP, Units.WATT_HOUR));
        return channelConfigurationEhub;
    }

    public static List<FerroampChannelConfiguration> getChannelConfigurationSsoS0() {
        final List<FerroampChannelConfiguration> channelConfigurationSsoS0 = new ArrayList<>();

        channelConfigurationSsoS0
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS0RELAYSTATUS, Units.ONE));
        channelConfigurationSsoS0
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS0TEMP, Units.ONE));
        channelConfigurationSsoS0
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS0WPV, Units.JOULE));
        channelConfigurationSsoS0
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS0TS, Units.ONE));
        channelConfigurationSsoS0
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS0UDC, Units.VOLT));
        channelConfigurationSsoS0
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS0FAULTCODE, Units.ONE));
        channelConfigurationSsoS0
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS0IPV, Units.AMPERE));
        channelConfigurationSsoS0
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS0UPV, Units.VOLT));
        channelConfigurationSsoS0
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS0ID, Units.ONE));
        return channelConfigurationSsoS0;
    }

    public static List<FerroampChannelConfiguration> getChannelConfigurationSsoS1() {
        final List<FerroampChannelConfiguration> channelConfigurationSsoS1 = new ArrayList<>();

        channelConfigurationSsoS1
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS1RELAYSTATUS, Units.ONE));
        channelConfigurationSsoS1
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS1TEMP, Units.ONE));
        channelConfigurationSsoS1
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS1WPV, Units.JOULE));
        channelConfigurationSsoS1
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS1TS, Units.ONE));
        channelConfigurationSsoS1
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS1UDC, Units.VOLT));
        channelConfigurationSsoS1
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS1FAULTCODE, Units.ONE));
        channelConfigurationSsoS1
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS1IPV, Units.AMPERE));
        channelConfigurationSsoS1
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS1UPV, Units.VOLT));
        channelConfigurationSsoS1
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS1ID, Units.ONE));
        return channelConfigurationSsoS1;
    }

    public static List<FerroampChannelConfiguration> getChannelConfigurationSsoS2() {
        final List<FerroampChannelConfiguration> channelConfigurationSsoS2 = new ArrayList<>();

        channelConfigurationSsoS2
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS2RELAYSTATUS, Units.ONE));
        channelConfigurationSsoS2
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS2TEMP, Units.ONE));
        channelConfigurationSsoS2
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS2WPV, Units.JOULE));
        channelConfigurationSsoS2
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS2TS, Units.ONE));
        channelConfigurationSsoS2
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS2UDC, Units.VOLT));
        channelConfigurationSsoS2
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS2FAULTCODE, Units.ONE));
        channelConfigurationSsoS2
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS2IPV, Units.AMPERE));
        channelConfigurationSsoS2
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS2UPV, Units.VOLT));
        channelConfigurationSsoS2
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS2ID, Units.ONE));
        return channelConfigurationSsoS2;
    }

    public static List<FerroampChannelConfiguration> getChannelConfigurationSsoS3() {
        final List<FerroampChannelConfiguration> channelConfigurationSsoS3 = new ArrayList<>();

        channelConfigurationSsoS3
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS3RELAYSTATUS, Units.ONE));
        channelConfigurationSsoS3
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS3TEMP, Units.ONE));
        channelConfigurationSsoS3
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS3WPV, Units.JOULE));
        channelConfigurationSsoS3
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS3TS, Units.ONE));
        channelConfigurationSsoS3
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS3UDC, Units.VOLT));
        channelConfigurationSsoS3
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS3FAULTCODE, Units.ONE));
        channelConfigurationSsoS3
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS3IPV, Units.AMPERE));
        channelConfigurationSsoS3
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS3UPV, Units.VOLT));
        channelConfigurationSsoS3
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_SSOS3ID, Units.ONE));
        return channelConfigurationSsoS3;
    }

    public static List<FerroampChannelConfiguration> getChannelConfigurationEso() {
        final List<FerroampChannelConfiguration> channelConfigurationEso = new ArrayList<>();

        channelConfigurationEso
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESOFAULTCODE, Units.ONE));
        channelConfigurationEso
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESOID, Units.ONE));
        channelConfigurationEso
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESOIBAT, Units.AMPERE));
        channelConfigurationEso
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESOUBAT, Units.VOLT));
        channelConfigurationEso
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESORELAYSTATUS, Units.ONE));
        channelConfigurationEso
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESOSOC, Units.PERCENT));
        channelConfigurationEso
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESOTEMP, Units.ONE));
        channelConfigurationEso
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESOWBATCONS, Units.JOULE));
        channelConfigurationEso
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESOWBATPROD, Units.JOULE));
        channelConfigurationEso
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESOUDC, Units.VOLT));
        channelConfigurationEso
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESOTS, Units.ONE));
        return channelConfigurationEso;
    }

    public static List<FerroampChannelConfiguration> getChannelConfigurationEsm() {
        final List<FerroampChannelConfiguration> channelConfigurationEsm = new ArrayList<>();

        channelConfigurationEsm
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESMSOH, Units.PERCENT));
        channelConfigurationEsm
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESMSOC, Units.PERCENT));
        channelConfigurationEsm.add(
                new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESMRATEDCAPACITY, Units.WATT_HOUR));
        channelConfigurationEsm
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESMSID, Units.ONE));
        channelConfigurationEsm
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESMRATEDPOWER, Units.WATT));
        channelConfigurationEsm
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESMSTATUS, Units.ONE));
        channelConfigurationEsm
                .add(new FerroampChannelConfiguration(FerroampBindingConstants.CHANNEL_ESMTS, Units.ONE));
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
