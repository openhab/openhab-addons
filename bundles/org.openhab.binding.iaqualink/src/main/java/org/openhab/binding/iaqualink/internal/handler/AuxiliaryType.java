/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.iaqualink.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.iaqualink.internal.IAqualinkBindingConstants;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * AuxiliaryType maps iAquaLink Auxiliary Devices to binding channel types
 *
 * @author Dan Cunningham - Initial contribution
 *
 */
@NonNullByDefault
public enum AuxiliaryType {
    SWITCH("0", "0", IAqualinkBindingConstants.CHANNEL_TYPE_UID_AUX_SWITCH),
    DIMMER("1", "NA", IAqualinkBindingConstants.CHANNEL_TYPE_UID_AUX_DIMMER),
    JANDYCOLOR("2", "1", IAqualinkBindingConstants.CHANNEL_TYPE_UID_AUX_JANDYCOLOR),
    PENTAIRSAM("2", "2", IAqualinkBindingConstants.CHANNEL_TYPE_UID_AUX_PENTAIRSAM),
    JANDYLED("2", "4", IAqualinkBindingConstants.CHANNEL_TYPE_UID_AUX_JANDYLED),
    PENTAIRIB("2", "5", IAqualinkBindingConstants.CHANNEL_TYPE_UID_AUX_PENTAIRIB),
    HAYWARD("2", "6", IAqualinkBindingConstants.CHANNEL_TYPE_UID_AUX_HAYWARD);

    private String type;
    private String subType;
    private ChannelTypeUID channelTypeUID;

    AuxiliaryType(String type, String subType, ChannelTypeUID channelTypeUID) {
        this.type = type;
        this.subType = subType;
        this.channelTypeUID = channelTypeUID;
    }

    public String getSubType() {
        return subType;
    }

    public String getType() {
        return type;
    }

    public ChannelTypeUID getChannelTypeUID() {
        return channelTypeUID;
    }

    public static AuxiliaryType fromSubType(String subType) {
        for (AuxiliaryType at : AuxiliaryType.values()) {
            if (at.subType.equals(subType)) {
                return at;
            }
        }
        return AuxiliaryType.SWITCH;
    }

    public static AuxiliaryType fromChannelTypeUID(@Nullable ChannelTypeUID channelTypeUID) {
        for (AuxiliaryType at : AuxiliaryType.values()) {
            if (at.channelTypeUID.equals(channelTypeUID)) {
                return at;
            }
        }
        return AuxiliaryType.SWITCH;
    }
}
