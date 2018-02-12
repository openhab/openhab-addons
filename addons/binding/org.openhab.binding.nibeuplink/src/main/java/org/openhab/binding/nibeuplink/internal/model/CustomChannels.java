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
public enum CustomChannels implements Channel {

    // Custom Channels
    CH_CH01("00000", "CH01", ChannelType.SENSOR, ChannelGroup.CUSTOM, String.class),
    CH_CH02("00000", "CH02", ChannelType.SENSOR, ChannelGroup.CUSTOM, String.class),
    CH_CH03("00000", "CH03", ChannelType.SENSOR, ChannelGroup.CUSTOM, String.class),
    CH_CH04("00000", "CH04", ChannelType.SENSOR, ChannelGroup.CUSTOM, String.class),
    CH_CH05("00000", "CH05", ChannelType.SENSOR, ChannelGroup.CUSTOM, String.class),
    CH_CH06("00000", "CH06", ChannelType.SENSOR, ChannelGroup.CUSTOM, String.class),
    CH_CH07("00000", "CH07", ChannelType.SENSOR, ChannelGroup.CUSTOM, String.class),
    CH_CH08("00000", "CH08", ChannelType.SENSOR, ChannelGroup.CUSTOM, String.class),

    /* END */
    ;

    private String id;
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
    CustomChannels(String id, String name, ChannelType channelType, ChannelGroup channelGroup, Class<?> javaType) {
        this.id = id;
        this.name = name;
        this.channelType = channelType;
        this.channelGroup = channelGroup;
        this.javaType = javaType;
    }

    public static CustomChannels fromId(String id) {
        for (CustomChannels channel : CustomChannels.values()) {
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

    public final void setId(Integer id) {
        this.id = id.toString();
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
        return getChannelGroup().toString().toLowerCase() + "#" + getName();
    }

}
