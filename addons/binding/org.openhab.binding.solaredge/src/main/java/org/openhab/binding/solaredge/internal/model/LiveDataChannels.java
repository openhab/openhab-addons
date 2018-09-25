/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
 * @author Alexander Friese - initial contribution
 */
public enum LiveDataChannels implements Channel {

    PRODUCTION("production", "production", ChannelType.LIVE, ChannelGroup.LIVE),
    PV_STATUS("pv_status", "PV status", ChannelType.LIVE, ChannelGroup.LIVE),

    CONSUMPTION("consumption", "consumption", ChannelType.LIVE, ChannelGroup.LIVE),
    LOAD_STATUS("load_status", "load status", ChannelType.LIVE, ChannelGroup.LIVE),

    BATTERY_CHARGE("battery_charge", "battery charge rate", ChannelType.LIVE, ChannelGroup.LIVE),
    BATTERY_DISCHARGE("battery_discharge", "battery discharge rate", ChannelType.LIVE, ChannelGroup.LIVE),
    BATTERY_CHARGE_DISCHARGE("battery_charge_discharge", "battery charge/discharge rate", ChannelType.LIVE,
            ChannelGroup.LIVE),
    BATTERY_LEVEL("battery_level", "battery level", ChannelType.LIVE, ChannelGroup.LIVE),
    BATTERY_STATUS("battery_status", "battery status", ChannelType.LIVE, ChannelGroup.LIVE),
    BATTERY_CRITICAL("battery_critical", "battery critical", ChannelType.LIVE, ChannelGroup.LIVE),

    IMPORT("import", "import", ChannelType.LIVE, ChannelGroup.LIVE),
    EXPORT("export", "export", ChannelType.LIVE, ChannelGroup.LIVE),
    GRID_STATUS("grid_status", "grid status", ChannelType.LIVE, ChannelGroup.LIVE),

    /* END */
    ;

    private final String id;
    private final String name;
    private final ChannelType channelType;
    private final ChannelGroup channelGroup;

    /**
     * Constructor
     *
     * @param id
     * @param name
     * @param type
     */
    LiveDataChannels(String id, String name, ChannelType channelType, ChannelGroup channelGroup) {
        this.id = id;
        this.name = name;
        this.channelType = channelType;
        this.channelGroup = channelGroup;
    }

    public static LiveDataChannels fromFQName(String fqName) {
        for (LiveDataChannels channel : LiveDataChannels.values()) {
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
    public String getFQName() {
        String fQName = getChannelGroup().toString().toLowerCase() + "#" + getId();
        return fQName;
    }
}
