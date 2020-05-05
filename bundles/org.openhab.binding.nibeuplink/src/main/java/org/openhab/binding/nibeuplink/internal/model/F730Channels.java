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

import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.openhab.binding.nibeuplink.internal.model.ScaledChannel.ScaleFactor;

/**
 * list of all available channels
 *
 * @author Alexander Friese - initial contribution
 */
public final class F730Channels extends BaseChannels {

    /**
     * singleton
     */
    private static final F730Channels INSTANCE = new F730Channels();

    /**
     * Returns the unique instance of this class.
     *
     * @return the Units instance.
     */
    public static F730Channels getInstance() {
        return INSTANCE;
    }

    /**
     * singleton should not be instantiated from outside
     */
    private F730Channels() {
    }

    // General
    // currently no general channels

    // Compressor
    public static final Channel CH_43181 = INSTANCE.addChannel(
            new QuantityChannel("43181", "Chargepump speed", ChannelGroup.COMPRESSOR, SmartHomeUnits.PERCENT));
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
    public static final Channel CH_40020 = INSTANCE.addChannel(new QuantityChannel("40020",
            "EB100-EP14-BT16 Evaporator", ChannelGroup.COMPRESSOR, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_43136 = INSTANCE.addChannel(new QuantityChannel("43136",
            "Compressor Frequency, Actual", ChannelGroup.COMPRESSOR, ScaleFactor.DIV_10, SmartHomeUnits.HERTZ));
    public static final Channel CH_43122 = INSTANCE.addChannel(
            new QuantityChannel("43122", "Compr. current min.freq.", ChannelGroup.COMPRESSOR, SmartHomeUnits.HERTZ));
    public static final Channel CH_43123 = INSTANCE.addChannel(
            new QuantityChannel("43123", "Compr. current max.freq.", ChannelGroup.COMPRESSOR, SmartHomeUnits.HERTZ));
    public static final Channel CH_43066 = INSTANCE.addChannel(
            new QuantityChannel("43066", "Defrosting time", ChannelGroup.COMPRESSOR, SmartHomeUnits.SECOND));

    // Airsupply
    public static final Channel CH_10001 = INSTANCE.addChannel(
            new QuantityChannel("10001", "Fan speed current", ChannelGroup.AIRSUPPLY, SmartHomeUnits.PERCENT));
    public static final Channel CH_40025 = INSTANCE.addChannel(new QuantityChannel("40025", "BT20 Exhaust air temp. 1",
            ChannelGroup.AIRSUPPLY, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_40026 = INSTANCE.addChannel(new QuantityChannel("40026", "BT21 Vented air temp. 1",
            ChannelGroup.AIRSUPPLY, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_43124 = INSTANCE
            .addChannel(new ScaledChannel("43124", "Air flow ref.", ChannelGroup.AIRSUPPLY, ScaleFactor.DIV_10));
    public static final Channel CH_41026 = INSTANCE
            .addChannel(new Channel("41026", "EB100-Adjusted BS1 Air flow", ChannelGroup.AIRSUPPLY));
    public static final Channel CH_43125 = INSTANCE
            .addChannel(new Channel("43125", "Air flow reduction", ChannelGroup.AIRSUPPLY));
    public static final Channel CH_40919 = INSTANCE.addChannel(
            new QuantityChannel("40919", "Air mix", ChannelGroup.AIRSUPPLY, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_40101 = INSTANCE.addChannel(new QuantityChannel("40101", "BT28 Air mix Temp",
            ChannelGroup.AIRSUPPLY, ScaleFactor.DIV_10, SIUnits.CELSIUS));
}
