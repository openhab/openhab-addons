/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
public final class VVM310Channels extends BaseChannels {

    /**
     * singleton
     */
    private static final VVM310Channels INSTANCE = new VVM310Channels();

    /**
     * Returns the unique instance of this class.
     *
     * @return the Units instance.
     */
    public static VVM310Channels getInstance() {
        return INSTANCE;
    }

    /**
     * singleton should not be instantiated from outside
     */
    private VVM310Channels() {
    }

    // General
    public static final Channel CH_44270 = INSTANCE.addChannel(new QuantityChannel("44270", "Calc. Cooling Supply S1",
            ChannelGroup.GENERAL, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_40121 = INSTANCE.addChannel(new QuantityChannel("40121", "BT63 Add Supply Temp",
            ChannelGroup.GENERAL, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_43437 = INSTANCE.addChannel(
            new QuantityChannel("43437", "Supply Pump Speed EP14", ChannelGroup.GENERAL, SmartHomeUnits.PERCENT));

    public static final Channel CH_44302 = INSTANCE
            .addChannel(new QuantityChannel("44302", "Heat Meter - Cooling Cpr EP14", ChannelGroup.GENERAL,
                    ScaleFactor.DIV_10, MetricPrefix.KILO(SmartHomeUnits.WATT_HOUR)));

    public static final Channel CH_47011 = INSTANCE.addChannel(
            new Channel("47011", "Heat Offset S1", ChannelGroup.GENERAL, "/Manage/1.9.1.1-S1", "[-1]*[0-9]"));
    public static final Channel CH_47394 = INSTANCE
            .addChannel(new SwitchChannel("47394", "Room Sensor", ChannelGroup.GENERAL, "/Manage/1.9.4"));
    public static final Channel CH_47402 = INSTANCE.addChannel(new ScaledChannel("47402", "Room sensor factor",
            ChannelGroup.GENERAL, ScaleFactor.DIV_10, "/Manage/1.9.4", "[0123456]*[0-9]"));
    public static final Channel CH_48793 = INSTANCE.addChannel(new ScaledChannel("48793", "Room sensor cool factor",
            ChannelGroup.GENERAL, ScaleFactor.DIV_10, "/Manage/1.9.4", "[0123456]*[0-9]"));

    // Compressor
    public static final Channel CH_44362 = INSTANCE.addChannel(new QuantityChannel("44362",
            "EB101-EP14-BT28 Outdoor Temp", ChannelGroup.COMPRESSOR, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_44396 = INSTANCE.addChannel(
            new QuantityChannel("44396", "EB101 Speed charge pump", ChannelGroup.COMPRESSOR, SmartHomeUnits.PERCENT));
    public static final Channel CH_44703 = INSTANCE
            .addChannel(new Channel("44703", "EB101-EP14 Defrosting Outdoor Unit", ChannelGroup.COMPRESSOR));
    public static final Channel CH_44073 = INSTANCE.addChannel(
            new QuantityChannel("44073", "EB101-EP14 Tot. HW op.time compr", ChannelGroup.COMPRESSOR, SIUnits.HOUR));
    public static final Channel CH_40737 = INSTANCE.addChannel(new QuantityChannel("40737",
            "EB101-EP14 Tot. Cooling op.time compr", ChannelGroup.COMPRESSOR, SIUnits.HOUR));
    public static final Channel CH_44071 = INSTANCE.addChannel(
            new QuantityChannel("44071", "EB101-EP14 Tot. op.time compr", ChannelGroup.COMPRESSOR, SIUnits.HOUR));
    public static final Channel CH_44069 = INSTANCE
            .addChannel(new Channel("44069", "EB101-EP14 Compressor starts", ChannelGroup.COMPRESSOR));
    public static final Channel CH_44061 = INSTANCE.addChannel(new QuantityChannel("44061", "EB101-EP14-BT17 Suction",
            ChannelGroup.COMPRESSOR, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_44060 = INSTANCE.addChannel(new QuantityChannel("44060",
            "EB101-EP14-BT15 Liquid Line", ChannelGroup.COMPRESSOR, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_44059 = INSTANCE.addChannel(new QuantityChannel("44059",
            "EB101-EP14-BT14 Hot Gas Temp", ChannelGroup.COMPRESSOR, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_44058 = INSTANCE.addChannel(new QuantityChannel("44058",
            "EB101-EP14-BT12 Condensor Out", ChannelGroup.COMPRESSOR, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_44055 = INSTANCE.addChannel(new QuantityChannel("44055",
            "EB101-EP14-BT3 Return Temp.", ChannelGroup.COMPRESSOR, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_44363 = INSTANCE.addChannel(new QuantityChannel("44363",
            "EB101-EP14-BT16 Evaporator", ChannelGroup.COMPRESSOR, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_44699 = INSTANCE.addChannel(new QuantityChannel("44699",
            "EB101-EP14-BP4 Pressure Sensor", ChannelGroup.COMPRESSOR, ScaleFactor.DIV_10, SmartHomeUnits.BAR));
    public static final Channel CH_40782 = INSTANCE.addChannel(new QuantityChannel("40782",
            "EB101 Cpr Frequency Desired F2040", ChannelGroup.COMPRESSOR, SmartHomeUnits.HERTZ));
    public static final Channel CH_44701 = INSTANCE
            .addChannel(new QuantityChannel("44701", "EB101-EP14 Actual Cpr Frequency Outdoor Unit",
                    ChannelGroup.COMPRESSOR, ScaleFactor.DIV_10, SmartHomeUnits.HERTZ));
    public static final Channel CH_44702 = INSTANCE.addChannel(
            new SwitchChannel("44702", "EB101-EP14 Protection Status Register Outdoor Unit", ChannelGroup.COMPRESSOR));
    public static final Channel CH_44700 = INSTANCE
            .addChannel(new QuantityChannel("44700", "EB101-EP14 Low Pressure Sensor Outdoor Unit",
                    ChannelGroup.COMPRESSOR, ScaleFactor.DIV_10, SmartHomeUnits.BAR));
    public static final Channel CH_44457 = INSTANCE
            .addChannel(new Channel("44457", "EB101-EP14 Compressor State", ChannelGroup.COMPRESSOR));

    // Airsupply
    public static final Channel CH_40025 = INSTANCE.addChannel(new QuantityChannel("40025", "BT20 Exhaust air temp. 1",
            ChannelGroup.AIRSUPPLY, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_40026 = INSTANCE.addChannel(new QuantityChannel("40026", "BT21 Vented air temp. 1",
            ChannelGroup.AIRSUPPLY, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_40075 = INSTANCE.addChannel(new QuantityChannel("40075", "BT22 Supply air temp.",
            ChannelGroup.AIRSUPPLY, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_40183 = INSTANCE.addChannel(new QuantityChannel("40183",
            "AZ30-BT23 Outdoor temp. ERS", ChannelGroup.AIRSUPPLY, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_40311 = INSTANCE.addChannel(new QuantityChannel("40311",
            "External ERS accessory GQ2 speed", ChannelGroup.AIRSUPPLY, SmartHomeUnits.PERCENT));
    public static final Channel CH_40312 = INSTANCE.addChannel(new QuantityChannel("40312",
            "External ERS accessory GQ3speed", ChannelGroup.AIRSUPPLY, SmartHomeUnits.PERCENT));
    public static final Channel CH_40942 = INSTANCE
            .addChannel(new SwitchChannel("40942", "External ERS accessory blockstatus", ChannelGroup.AIRSUPPLY));
    public static final Channel CH_47260 = INSTANCE
            .addChannel(new Channel("47260", "Selected Fan speed", ChannelGroup.AIRSUPPLY, "/Manage/1.2", "[01234]"));
}
