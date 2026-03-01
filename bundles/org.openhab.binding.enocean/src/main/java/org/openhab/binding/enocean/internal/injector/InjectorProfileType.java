/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.injector;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_CONTACT;
import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_GENERAL_SWITCHING;
import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_MOTIONDETECTION;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.enocean.internal.eep.EEPType;

/**
 * Registry for send-only datagram profiles.
 */
@NonNullByDefault
public enum InjectorProfileType {
    FTK_D5_00_01("FTK_D5_00_01", EEPType.ContactAndSwitch01, "switch", CHANNEL_GENERAL_SWITCHING),
    MOTION_A5_07_01("MOTION_A5_07_01", EEPType.OCCUPANCYSENSOR_A5_07_01, "switch", CHANNEL_GENERAL_SWITCHING);

    private final String id;
    private final EEPType sendingEEPType;
    private final String channelId;
    private final String channelTypeId;

    InjectorProfileType(String id, EEPType sendingEEPType, String channelId, String channelTypeId) {
        this.id = id;
        this.sendingEEPType = sendingEEPType;
        this.channelId = channelId;
        this.channelTypeId = channelTypeId;
    }

    public EEPType getSendingEEPType() {
        return sendingEEPType;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getChannelTypeId() {
        return channelTypeId;
    }

    public static boolean isProfileChannelId(String channelId) {
        for (InjectorProfileType type : values()) {
            if (type.channelId.equals(channelId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isChannelSupported(String channelId, String channelTypeId) {
        return switch (this) {
            case FTK_D5_00_01 -> this.channelId.equals(channelId)
                    && (this.channelTypeId.equals(channelTypeId) || CHANNEL_CONTACT.equals(channelTypeId));
            case MOTION_A5_07_01 -> this.channelId.equals(channelId)
                    && (this.channelTypeId.equals(channelTypeId) || CHANNEL_MOTIONDETECTION.equals(channelTypeId));
        };
    }

    public static InjectorProfileType getType(String id) {
        for (InjectorProfileType type : values()) {
            if (type.id.equalsIgnoreCase(id)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported sending profile: " + id);
    }
}
