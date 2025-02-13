/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.smarthome;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link ChannelInfo} holds the information for a single channel
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class ChannelInfo {
    public final String propertyName;
    public final String channelId;
    public final String propertyNameSend;
    public final ChannelTypeUID channelTypeUID;
    public final @Nullable String label;

    public ChannelInfo(String propertyNameReceive, String propertyNameSend, String channelId,
            ChannelTypeUID channelTypeUID, @Nullable String label) {
        this.propertyName = propertyNameReceive;
        this.propertyNameSend = propertyNameSend;
        this.channelId = channelId;
        this.channelTypeUID = channelTypeUID;
        this.label = label;
    }

    public ChannelInfo(String propertyNameReceive, String propertyNameSend, String channelId,
            ChannelTypeUID channelTypeUID) {
        this(propertyNameReceive, propertyNameSend, channelId, channelTypeUID, null);
    }

    public ChannelInfo(String propertyName, String channelId, ChannelTypeUID channelTypeUID) {
        this(propertyName, propertyName, channelId, channelTypeUID);
    }

    public ChannelInfo(String propertyName, String channelId, ChannelTypeUID channelTypeUID, String label) {
        this(propertyName, propertyName, channelId, channelTypeUID, label);
    }

    @Override
    public String toString() {
        return "ChannelInfo{" + "propertyName='" + propertyName + "', channelId='" + channelId + "', propertyNameSend='"
                + propertyNameSend + "', channelTypeUID=" + channelTypeUID + "label='" + label + "'}";
    }
}
