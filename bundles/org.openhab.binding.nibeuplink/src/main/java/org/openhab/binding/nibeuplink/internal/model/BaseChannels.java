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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.openhab.binding.nibeuplink.internal.model.ScaledChannel.ScaleFactor;

/**
 * this class contains all base channels which are used by all heatpump models
 *
 * @author Alexander Friese - initial contribution
 */
public class BaseChannels extends AbstractChannels {

    /**
     * singleton
     */
    private static final BaseChannels INSTANCE = new BaseChannels();

    /**
     * Returns the unique instance of this class.
     *
     * @return the Units instance.
     */
    protected static BaseChannels getInstance() {
        return INSTANCE;
    }

    /**
     * singleton should not be instantiated from outside
     */
    protected BaseChannels() {
    }

    /**
     * returns the matching channel, null if no match was found.
     *
     * @param channelCode the channelCode which identifies the channel
     * @return channel which belongs to the code. might be null if there is no channel found.
     */
    @Override
    public Channel fromCode(String channelCode) {
        Channel channel = super.fromCode(channelCode);

        // also check channels in this class if called from an inherited class
        if (channel == null && this != INSTANCE) {
            return INSTANCE.fromCode(channelCode);
        } else {
            return channel;
        }
    }

    /**
     * returns an unmodifiable set containing all available channels.
     *
     * @return
     */
    @Override
    public Set<Channel> getChannels() {
        Set<Channel> allChannels = new HashSet<>();
        allChannels.addAll(channels);

        // also add channels contained in this class if called from an inherited class
        if (this != INSTANCE) {
            allChannels.addAll(INSTANCE.channels);
        }

        return Collections.unmodifiableSet(allChannels);
    }

    // General
    public static final Channel CH_40004 = INSTANCE.addChannel(new QuantityChannel("40004", "BT1 Outdoor Temperature",
            ChannelGroup.BASE, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_40067 = INSTANCE.addChannel(
            new QuantityChannel("40067", "BT1 Average", ChannelGroup.BASE, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_43005 = INSTANCE.addChannel(new ScaledChannel("43005", "Degree Minutes (16 bit)",
            ChannelGroup.BASE, ScaleFactor.DIV_10, "/Manage/4.9.3", "-?[0-9]+"));
    public static final Channel CH_43009 = INSTANCE.addChannel(
            new QuantityChannel("43009", "Calc. Supply S1", ChannelGroup.BASE, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_40071 = INSTANCE.addChannel(
            new QuantityChannel("40071", "BT25 Ext. Supply", ChannelGroup.BASE, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_40033 = INSTANCE.addChannel(
            new QuantityChannel("40033", "BT50 Room Temp S1", ChannelGroup.BASE, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_43161 = INSTANCE
            .addChannel(new SwitchChannel("43161", "External adjustment activated via input S1", ChannelGroup.BASE));
    public static final Channel CH_40008 = INSTANCE.addChannel(
            new QuantityChannel("40008", "BT2 Supply temp S1", ChannelGroup.BASE, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_40012 = INSTANCE.addChannel(new QuantityChannel("40012",
            "EB100-EP14-BT3 Return temp", ChannelGroup.BASE, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_40072 = INSTANCE.addChannel(new QuantityChannel("40072", "BF1 EP14 Flow",
            ChannelGroup.BASE, ScaleFactor.DIV_10, SmartHomeUnits.LITRE.divide(SIUnits.METRE)));
    public static final Channel CH_43437 = INSTANCE.addChannel(
            new QuantityChannel("43437", "Supply Pump Speed EP14", ChannelGroup.BASE, SmartHomeUnits.PERCENT));
    public static final Channel CH_40079 = INSTANCE.addChannel(new QuantityChannel("40079", "EB100-BE3 Current",
            ChannelGroup.BASE, ScaleFactor.DIV_10, SmartHomeUnits.AMPERE));
    public static final Channel CH_40081 = INSTANCE.addChannel(new QuantityChannel("40081", "EB100-BE2 Current",
            ChannelGroup.BASE, ScaleFactor.DIV_10, SmartHomeUnits.AMPERE));
    public static final Channel CH_40083 = INSTANCE.addChannel(new QuantityChannel("40083", "EB100-BE1 Current",
            ChannelGroup.BASE, ScaleFactor.DIV_10, SmartHomeUnits.AMPERE));
    public static final Channel CH_10033 = INSTANCE
            .addChannel(new SwitchChannel("10033", "Int. el.add. blocked", ChannelGroup.BASE));

    // additional heater
    public static final Channel CH_43081 = INSTANCE.addChannel(new QuantityChannel("43081", "Tot. op.time add.",
            ChannelGroup.BASE, ScaleFactor.DIV_10, SmartHomeUnits.HOUR));
    public static final Channel CH_43084 = INSTANCE.addChannel(new QuantityChannel("43084", "Int. el.add. Power",
            ChannelGroup.BASE, ScaleFactor.DIV_100, MetricPrefix.KILO(SmartHomeUnits.WATT)));
    public static final Channel CH_47212 = INSTANCE.addChannel(new QuantityChannel("47212", "Max int add. power",
            ChannelGroup.BASE, ScaleFactor.DIV_100, MetricPrefix.KILO(SmartHomeUnits.WATT)));
    public static final Channel CH_48914 = INSTANCE
            .addChannel(new QuantityChannel("48914", "Max int add. power, SG Ready", ChannelGroup.BASE,
                    ScaleFactor.DIV_100, MetricPrefix.KILO(SmartHomeUnits.WATT)));

    // heat meters
    public static final Channel CH_44308 = INSTANCE
            .addChannel(new QuantityChannel("44308", "Heat Meter - Heat Cpr EP14", ChannelGroup.BASE,
                    ScaleFactor.DIV_10, MetricPrefix.KILO(SmartHomeUnits.WATT_HOUR)));
    public static final Channel CH_44304 = INSTANCE
            .addChannel(new QuantityChannel("44304", "Heat Meter - Pool Cpr EP14", ChannelGroup.BASE,
                    ScaleFactor.DIV_10, MetricPrefix.KILO(SmartHomeUnits.WATT_HOUR)));
    public static final Channel CH_44300 = INSTANCE
            .addChannel(new QuantityChannel("44300", "Heat Meter - Heat Cpr and Add EP14", ChannelGroup.BASE,
                    ScaleFactor.DIV_10, MetricPrefix.KILO(SmartHomeUnits.WATT_HOUR)));

    public static final Channel CH_48043 = INSTANCE
            .addChannel(new SwitchChannel("48043", "Holiday Mode", ChannelGroup.BASE, 0, 10, "/Manage/4.7"));

    // Hotwater
    public static final Channel CH_40013 = INSTANCE.addChannel(
            new QuantityChannel("40013", "BT7 HW Top", ChannelGroup.HOTWATER, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_40014 = INSTANCE.addChannel(
            new QuantityChannel("40014", "BT6 HW Load", ChannelGroup.HOTWATER, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_44306 = INSTANCE.addChannel(new QuantityChannel("44306", "Heat Meter - HW Cpr EP14",
            ChannelGroup.HOTWATER, ScaleFactor.DIV_10, MetricPrefix.KILO(SmartHomeUnits.WATT_HOUR)));
    public static final Channel CH_44298 = INSTANCE
            .addChannel(new QuantityChannel("44298", "Heat Meter - HW Cpr and Add EP14", ChannelGroup.HOTWATER,
                    ScaleFactor.DIV_10, MetricPrefix.KILO(SmartHomeUnits.WATT_HOUR)));
    public static final Channel CH_48132 = INSTANCE
            .addChannel(new Channel("48132", "Temporary Lux", ChannelGroup.HOTWATER, "/Manage/2.1", "[01234]"));
    public static final Channel CH_47041 = INSTANCE
            .addChannel(new Channel("47041", "Hot water mode", ChannelGroup.HOTWATER, "/Manage/2.2", "[012]"));
    public static final Channel CH_47045 = INSTANCE.addChannel(new QuantityChannel("47045",
            "Start temperature HW Economy", ChannelGroup.HOTWATER, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_47049 = INSTANCE.addChannel(new QuantityChannel("47049",
            "Stop temperature HW Economy", ChannelGroup.HOTWATER, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_47044 = INSTANCE.addChannel(new QuantityChannel("47044",
            "Start temperature HW Normal", ChannelGroup.HOTWATER, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_47048 = INSTANCE.addChannel(new QuantityChannel("47048",
            "Stop temperature HW Normal", ChannelGroup.HOTWATER, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_47043 = INSTANCE.addChannel(new QuantityChannel("47043",
            "Start temperature HW Luxury", ChannelGroup.HOTWATER, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_47047 = INSTANCE.addChannel(new QuantityChannel("47047",
            "Stop temperature HW Luxury", ChannelGroup.HOTWATER, ScaleFactor.DIV_10, SIUnits.CELSIUS));
    public static final Channel CH_47046 = INSTANCE.addChannel(new QuantityChannel("47046",
            "Stop temperature periodic HW", ChannelGroup.HOTWATER, ScaleFactor.DIV_10, SIUnits.CELSIUS));

    // Compressor
    public static final Channel CH_10012 = INSTANCE
            .addChannel(new SwitchChannel("10012", "Compressor blocked", ChannelGroup.BASE));
}
