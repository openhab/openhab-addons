/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink.internal.model;

/**
 * list of all available channels
 *
 * @author Alexander Friese - initial contribution
 *
 */
public enum VVM310Channels implements Channel {

    // General
    CH_40004("40004", "BT1 Outdoor Temperature", ChannelType.SENSOR, ChannelGroup.GENERAL, Double.class),
    CH_40067("40067", "BT1 Average", ChannelType.SENSOR, ChannelGroup.GENERAL, Double.class),
    CH_43005("43005", "Degree Minutes (16 bit)", ChannelType.SETTING, ChannelGroup.GENERAL, Double.class),
    CH_43009("43009", "Calc. Supply S1", ChannelType.SENSOR, ChannelGroup.GENERAL, Double.class),
    CH_40033("40033", "BT50 Room Temp S1", ChannelType.SENSOR, ChannelGroup.GENERAL, Double.class),
    CH_43161("43161", "External adjustment activated via input S1", ChannelType.SENSOR, ChannelGroup.GENERAL,
            String.class),
    CH_40008("40008", "BT2 Supply temp S1", ChannelType.SENSOR, ChannelGroup.GENERAL, Double.class),
    CH_40012("40012", "EB100-EP14-BT3 Return temp", ChannelType.SENSOR, ChannelGroup.GENERAL, Double.class),
    CH_40072("40072", "BF1 EP14 Flow", ChannelType.SENSOR, ChannelGroup.GENERAL, Double.class),

    CH_44270("44270", "Calc. Cooling Supply S1", ChannelType.SENSOR, ChannelGroup.GENERAL, Double.class),
    CH_43081("43081", "Tot. op.time add.", ChannelType.SENSOR, ChannelGroup.GENERAL, Double.class),
    CH_43084("43084", "Int. el.add. Power", ChannelType.SENSOR, ChannelGroup.GENERAL, Double.class),
    CH_47212("47212", "Max int add. power", ChannelType.SETTING, ChannelGroup.GENERAL, Double.class),
    CH_48914("48914", "Max int add. power, SG Ready", ChannelType.SETTING, ChannelGroup.GENERAL, Double.class),
    CH_40121("40121", "BT63 Add Supply Temp", ChannelType.SENSOR, ChannelGroup.GENERAL, Double.class),

    CH_44308("44308", "Heat Meter - Heat Cpr EP14", ChannelType.SENSOR, ChannelGroup.GENERAL, Double.class),
    CH_44304("44304", "Heat Meter - Pool Cpr EP14", ChannelType.SENSOR, ChannelGroup.GENERAL, Double.class),
    CH_44302("44302", "Heat Meter - Cooling Cpr EP14", ChannelType.SENSOR, ChannelGroup.GENERAL, Double.class),
    CH_44300("44300", "Heat Meter - Heat Cpr and Add EP14", ChannelType.SENSOR, ChannelGroup.GENERAL, Double.class),
    // Hotwater
    CH_40013("40013", "BT7 HW Top", ChannelType.SENSOR, ChannelGroup.HOTWATER, Double.class),
    CH_40014("40014", "BT6 HW Load", ChannelType.SENSOR, ChannelGroup.HOTWATER, Double.class),
    CH_44306("44306", "Heat Meter - HW Cpr EP14", ChannelType.SENSOR, ChannelGroup.HOTWATER, Double.class),
    CH_44298("44298", "Heat Meter - HW Cpr and Add EP14", ChannelType.SENSOR, ChannelGroup.HOTWATER, Double.class),
    CH_48132("48132", "Temporary Lux", ChannelType.SETTING, ChannelGroup.HOTWATER, String.class),
    CH_47041("47041", "Hot water mode", ChannelType.SETTING, ChannelGroup.HOTWATER, String.class),
    // Compressor
    CH_44362("44362", "EB101-EP14-BT28 Outdoor Temp", ChannelType.SENSOR, ChannelGroup.COMPRESSOR, Double.class),
    CH_44396("44396", "EB101 Speed charge pump", ChannelType.SENSOR, ChannelGroup.COMPRESSOR, Double.class),
    CH_44703("44703", "EB101-EP14 Defrosting Outdoor Unit", ChannelType.SENSOR, ChannelGroup.COMPRESSOR, String.class),
    CH_44073("44073", "EB101-EP14 Tot. HW op.time compr", ChannelType.SENSOR, ChannelGroup.COMPRESSOR, Double.class),
    CH_40737("40737", "EB101-EP14 Tot. Cooling op.time compr", ChannelType.SENSOR, ChannelGroup.COMPRESSOR,
            Double.class),
    CH_44071("44071", "EB101-EP14 Tot. op.time compr", ChannelType.SENSOR, ChannelGroup.COMPRESSOR, Double.class),
    CH_44069("44069", "EB101-EP14 Compressor starts", ChannelType.SENSOR, ChannelGroup.COMPRESSOR, Double.class),
    CH_44061("44061", "EB101-EP14-BT17 Suction", ChannelType.SENSOR, ChannelGroup.COMPRESSOR, Double.class),
    CH_44060("44060", "EB101-EP14-BT15 Liquid Line", ChannelType.SENSOR, ChannelGroup.COMPRESSOR, Double.class),
    CH_44059("44059", "EB101-EP14-BT14 Hot Gas Temp", ChannelType.SENSOR, ChannelGroup.COMPRESSOR, Double.class),
    CH_44058("44058", "EB101-EP14-BT12 Condensor Out", ChannelType.SENSOR, ChannelGroup.COMPRESSOR, Double.class),
    CH_44055("44055", "EB101-EP14-BT3 Return Temp.", ChannelType.SENSOR, ChannelGroup.COMPRESSOR, Double.class),
    CH_44363("44363", "EB101-EP14-BT16 Evaporator", ChannelType.SENSOR, ChannelGroup.COMPRESSOR, Double.class),
    CH_44699("44699", "EB101-EP14-BP4 Pressure Sensor", ChannelType.SENSOR, ChannelGroup.COMPRESSOR, Double.class),
    CH_40782("40782", "EB101 Cpr Frequency Desired F2040 #2", ChannelType.SENSOR, ChannelGroup.COMPRESSOR,
            Double.class),
    CH_44701("44701", "EB101-EP14 Actual Cpr Frequency Outdoor Unit", ChannelType.SENSOR, ChannelGroup.COMPRESSOR,
            Double.class),
    CH_44702("44702", "EB101-EP14 Protection Status Register Outdoor Unit", ChannelType.SENSOR, ChannelGroup.COMPRESSOR,
            String.class),
    CH_44700("44700", "EB101-EP14 Low Pressure Sensor Outdoor Unit", ChannelType.SENSOR, ChannelGroup.COMPRESSOR,
            Double.class),
    // Airsupply
    CH_40025("40025", "BT20 Exhaust air temp. 1", ChannelType.SENSOR, ChannelGroup.AIRSUPPLY, Double.class),
    CH_40026("40026", "BT21 Vented air temp. 1", ChannelType.SENSOR, ChannelGroup.AIRSUPPLY, Double.class),
    CH_40075("40075", "BT22 Supply air temp.", ChannelType.SENSOR, ChannelGroup.AIRSUPPLY, Double.class),
    CH_40183("40183", "AZ30-BT23 Outdoor temp. ERS", ChannelType.SENSOR, ChannelGroup.AIRSUPPLY, Double.class),
    CH_40311("40311", "External ERS accessory GQ2 speed", ChannelType.SENSOR, ChannelGroup.AIRSUPPLY, Double.class),
    CH_40312("40312", "External ERS accessory GQ3 speed", ChannelType.SENSOR, ChannelGroup.AIRSUPPLY, Double.class),
    CH_40942("40942", "External ERS accessory block status", ChannelType.SENSOR, ChannelGroup.AIRSUPPLY, String.class),

    /* END */
    ;

    private final String id;
    private final String name;
    private final ChannelType channelType;
    private final ChannelGroup channelGroup;
    private final Class<?> javaType;

    /**
     * Constructor
     *
     * @param id
     * @param name
     * @param type
     */
    VVM310Channels(String id, String name, ChannelType channelType, ChannelGroup channelGroup, Class<?> javaType) {
        this.id = id;
        this.name = name;
        this.channelType = channelType;
        this.channelGroup = channelGroup;
        this.javaType = javaType;
    }

    public static VVM310Channels fromId(String id) {
        for (VVM310Channels channel : VVM310Channels.values()) {
            if (channel.id.equals(id)) {
                return channel;
            }
        }
        return null;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final String getId() {
        return id;
    }

    @Override
    public final ChannelType getChannelType() {
        return channelType;
    }

    @Override
    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }

    @Override
    public final Class<?> getJavaType() {
        return javaType;
    }

    @Override
    public String getFQName() {
        String fQName = getChannelGroup().toString().toLowerCase() + "#" + getId();
        return fQName;
    }

}
