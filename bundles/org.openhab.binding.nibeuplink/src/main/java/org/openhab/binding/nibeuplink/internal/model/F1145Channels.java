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
package org.openhab.binding.nibeuplink.internal.model;

import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.openhab.binding.nibeuplink.internal.model.ScaledChannel.ScaleFactor;

/**
 * list of all available channels
 *
 * @author Alexander Friese - initial contribution
 */
public final class F1145Channels extends BaseChannels {

    /**
     * singleton
     */
    private static final F1145Channels INSTANCE = new F1145Channels();

    /**
     * Returns the unique instance of this class.
     *
     * @return the Units instance.
     */
    public static F1145Channels getInstance() {
        return INSTANCE;
    }

    /**
     * singleton should not be instantiated from outside
     */
    private F1145Channels() {
    }

    // General
    public static final Channel CH_44302 = INSTANCE
            .addChannel(new QuantityChannel("44302", "Heat Meter - Cooling Cpr EP14", ChannelGroup.GENERAL,
                    ScaleFactor.DIV_10, MetricPrefix.KILO(SmartHomeUnits.WATT_HOUR)));
    public static final Channel CH_44270 = INSTANCE.addChannel(new QuantityChannel("44270",
            "Calculated Cooling Supply Temperature S1", ChannelGroup.GENERAL, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_43103 = INSTANCE
            .addChannel(new Channel("43103", "HPAC state", ChannelGroup.GENERAL));

    // Compressor
    public static final Channel CH_43424 = INSTANCE.addChannel(new QuantityChannel("43424",
            "EB100-EP14 Tot. HW op.time compr", ChannelGroup.COMPRESSOR, SmartHomeUnits.HOUR));
    public static final Channel CH_43420 = INSTANCE.addChannel(new QuantityChannel("43420",
            "EB100-EP14 Tot. op.time compr", ChannelGroup.COMPRESSOR, SmartHomeUnits.HOUR));
    public static final Channel CH_43416 = INSTANCE
            .addChannel(new Channel("43416", "EB100-EP14 Compressor starts", ChannelGroup.COMPRESSOR));
    public static final Channel CH_40022 = INSTANCE.addChannel(new QuantityChannel("40022", "EB100-EP14-BT17 Suction",
            ChannelGroup.COMPRESSOR, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_40019 = INSTANCE.addChannel(new QuantityChannel("40019",
            "EB100-EP14-BT15 Liquid Line", ChannelGroup.COMPRESSOR, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_40018 = INSTANCE.addChannel(new QuantityChannel("40018",
            "EB100-EP14-BT14 Hot Gas Temp", ChannelGroup.COMPRESSOR, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_40017 = INSTANCE.addChannel(new QuantityChannel("40017",
            "EB100-EP14-BT12 Condensor Out", ChannelGroup.COMPRESSOR, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_40015 = INSTANCE.addChannel(new QuantityChannel("40015",
            "EB100-EP14-BT10 Brine In Temperature", ChannelGroup.COMPRESSOR, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_40016 = INSTANCE.addChannel(new QuantityChannel("40016",
            "EB100-EP14-BT11 Brine Out Temperature", ChannelGroup.COMPRESSOR, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_43439 = INSTANCE.addChannel(
            new QuantityChannel("43439", "EP14-GP2 Brine Pump Speed", ChannelGroup.COMPRESSOR, SmartHomeUnits.PERCENT));

    // Airsupply
    public static final Channel CH_40025 = INSTANCE.addChannel(new QuantityChannel("40025", "BT20 Exhaust air temp. 1",
            ChannelGroup.AIRSUPPLY, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_40026 = INSTANCE.addChannel(new QuantityChannel("40026", "BT21 Vented air temp. 1",
            ChannelGroup.AIRSUPPLY, ScaleFactor.DIV_10, SIUnits.CELSIUS));
}
