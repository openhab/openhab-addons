/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge.internal.model;

/**
 * list of all available channels
 *
 * @author afriese
 *
 */
public enum AggregateDataChannels implements Channel {

    PRODUCTION("production", "production", ChannelType.Aggregate, ChannelGroup.Aggregate, Double.class),

    CONSUMPTION("consumption", "consumption", ChannelType.Aggregate, ChannelGroup.Aggregate, Double.class),

    SELFCONSUMPTIONFORCONSUMPTION("selfConsumptionForConsumption", "self consumption", ChannelType.Aggregate,
            ChannelGroup.Aggregate, Double.class),

    BATTERYSELFCONSUMPTION("batterySelfConsumption", "battery self consumption", ChannelType.Aggregate,
            ChannelGroup.Aggregate, Double.class),

    IMPORT("import", "import", ChannelType.Aggregate, ChannelGroup.Aggregate, Double.class),

    EXPORT("export", "export", ChannelType.Aggregate, ChannelGroup.Aggregate, Double.class),

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
    AggregateDataChannels(String id, String name, ChannelType channelType, ChannelGroup channelGroup,
            Class<?> javaType) {
        this.id = id;
        this.name = name;
        this.channelType = channelType;
        this.channelGroup = channelGroup;
        this.javaType = javaType;
    }

    public static AggregateDataChannels fromFQName(String fqName) {
        for (AggregateDataChannels channel : AggregateDataChannels.values()) {
            if (channel.getFQName().equals(fqName)) {
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
