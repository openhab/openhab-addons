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
public enum F1145Channels implements Channel {

    // General
    CH_40004("40004", "BT1 Outdoor Temperature", ChannelGroup.GENERAL, ValueType.NUMBER),
    CH_40067("40067", "BT1 Average", ChannelGroup.GENERAL, ValueType.NUMBER),
    CH_43005("43005", "Degree Minutes (16 bit)", ChannelGroup.GENERAL, ValueType.NUMBER),
    CH_43009("43009", "Calc. Supply S1", ChannelGroup.GENERAL, ValueType.NUMBER),
    CH_40071("40071", "BT25 Ext. Supply", ChannelGroup.GENERAL, ValueType.NUMBER),
    CH_40033("40033", "BT50 Room Temp S1", ChannelGroup.GENERAL, ValueType.NUMBER),
    CH_43161("43161", "External adjustment activated via input S1", ChannelGroup.GENERAL, ValueType.STRING),
    CH_40008("40008", "BT2 Supply temp S1", ChannelGroup.GENERAL, ValueType.NUMBER),
    CH_40012("40012", "EB100-EP14-BT3 Return temp", ChannelGroup.GENERAL, ValueType.NUMBER),
    CH_40072("40072", "BF1 EP14 Flow", ChannelGroup.GENERAL, ValueType.NUMBER),
    CH_40079("40079", "EB100-BE3 Current", ChannelGroup.GENERAL, ValueType.NUMBER_10),
    CH_40081("40081", "EB100-BE2 Current", ChannelGroup.GENERAL, ValueType.NUMBER_10),
    CH_40083("40083", "EB100-BE1 Current", ChannelGroup.GENERAL, ValueType.NUMBER_10),
    CH_10033("10033", "Int. el.add. blocked", ChannelGroup.GENERAL, ValueType.STRING),

    CH_43081("43081", "Tot. op.time add.", ChannelGroup.GENERAL, ValueType.NUMBER),
    CH_43084("43084", "Int. el.add. Power", ChannelGroup.GENERAL, ValueType.NUMBER),
    CH_47212("47212", "Max int add. power", ChannelGroup.GENERAL, ValueType.NUMBER),

    CH_44308("44308", "Heat Meter - Heat Cpr EP14", ChannelGroup.GENERAL, ValueType.NUMBER),
    CH_44304("44304", "Heat Meter - Pool Cpr EP14", ChannelGroup.GENERAL, ValueType.NUMBER),
    CH_44302("44302", "Heat Meter - Cooling Cpr EP14", ChannelGroup.GENERAL, ValueType.NUMBER),
    CH_44300("44300", "Heat Meter - Heat Cpr and Add EP14", ChannelGroup.GENERAL, ValueType.NUMBER),

    // Hotwater
    CH_40013("40013", "BT7 HW Top", ChannelGroup.HOTWATER, ValueType.NUMBER),
    CH_40014("40014", "BT6 HW Load", ChannelGroup.HOTWATER, ValueType.NUMBER),
    CH_44306("44306", "Heat Meter - HW Cpr EP14", ChannelGroup.HOTWATER, ValueType.NUMBER),
    CH_44298("44298", "Heat Meter - HW Cpr and Add EP14", ChannelGroup.HOTWATER, ValueType.NUMBER),
    CH_48132("48132", "Temporary Lux", ChannelGroup.HOTWATER, ValueType.STRING),
    CH_47041("47041", "Hot water mode", ChannelGroup.HOTWATER, ValueType.STRING),

    // Compressor
    CH_43424("43424", "EB100-EP14 Tot. HW op.time compr", ChannelGroup.COMPRESSOR, ValueType.NUMBER),
    CH_43420("43420", "EB100-EP14 Tot. op.time compr", ChannelGroup.COMPRESSOR, ValueType.NUMBER),
    CH_43416("43416", "EB100-EP14 Compressor starts", ChannelGroup.COMPRESSOR, ValueType.NUMBER),
    CH_40022("40022", "EB100-EP14-BT17 Suction", ChannelGroup.COMPRESSOR, ValueType.NUMBER),
    CH_40019("40019", "EB100-EP14-BT15 Liquid Line", ChannelGroup.COMPRESSOR, ValueType.NUMBER),
    CH_40018("40018", "EB100-EP14-BT14 Hot Gas Temp", ChannelGroup.COMPRESSOR, ValueType.NUMBER),
    CH_40017("40017", "EB100-EP14-BT12 Condensor Out", ChannelGroup.COMPRESSOR, ValueType.NUMBER),
    CH_10012("10012", "Compressor blocked", ChannelGroup.COMPRESSOR, ValueType.STRING),

    // Airsupply
    CH_40025("40025", "BT20 Exhaust air temp. 1", ChannelGroup.AIRSUPPLY, ValueType.NUMBER),
    CH_40026("40026", "BT21 Vented air temp. 1", ChannelGroup.AIRSUPPLY, ValueType.NUMBER),

    /* END */
    ;

    private final String id;
    private final String name;
    private final ChannelGroup channelGroup;
    private final ValueType valueType;
    private final String writeApiUrl;
    private final String validationExpression;

    /**
     * constructor for channels with wrote access enabled
     *
     * @param id
     * @param name
     * @param channelType
     * @param channelGroup
     * @param javaType
     * @param writeApiUrl
     * @param validationExpression
     */
    F1145Channels(String id, String name, ChannelGroup channelGroup, ValueType valueType, String writeApiUrl,
            String validationExpression) {
        this.id = id;
        this.name = name;
        this.channelGroup = channelGroup;
        this.valueType = valueType;
        this.writeApiUrl = writeApiUrl;
        this.validationExpression = validationExpression;
    }

    /**
     * constructor for channels without write access
     *
     * @param id
     * @param name
     * @param channelType
     * @param channelGroup
     * @param javaType
     */
    F1145Channels(String id, String name, ChannelGroup channelGroup, ValueType valueType) {
        this(id, name, channelGroup, valueType, null, null);
    }

    public static F1145Channels fromId(String id) {
        for (F1145Channels channel : F1145Channels.values()) {
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
    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }

    @Override
    public final ValueType getValueType() {
        return valueType;
    }

    @Override
    public String getFQName() {
        return getChannelGroup().toString().toLowerCase() + "#" + getId();
    }

    @Override
    public String getWriteApiUrlSuffix() {
        return writeApiUrl;
    }

    @Override
    public boolean isReadOnly() {
        return writeApiUrl == null || writeApiUrl.isEmpty();
    }

    @Override
    public String getValidationExpression() {
        return validationExpression;
    }
}
