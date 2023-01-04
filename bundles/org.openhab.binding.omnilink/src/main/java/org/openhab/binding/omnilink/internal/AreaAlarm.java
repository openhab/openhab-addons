/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.omnilink.internal;

import static org.openhab.binding.omnilink.internal.OmnilinkBindingConstants.*;

import java.math.BigInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AreaAlarm} class defines the different types of alarms supported
 * by the OmniLink Protocol.
 *
 * @author Craig Hamilton - Initial contribution
 */
@NonNullByDefault
public enum AreaAlarm {
    BURGLARY(CHANNEL_AREA_ALARM_BURGLARY, 0),
    FIRE(CHANNEL_AREA_ALARM_FIRE, 1),
    GAS(CHANNEL_AREA_ALARM_GAS, 2),
    AUXILIARY(CHANNEL_AREA_ALARM_AUXILIARY, 3),
    FREEZE(CHANNEL_AREA_ALARM_FREEZE, 4),
    WATER(CHANNEL_AREA_ALARM_WATER, 5),
    DURESS(CHANNEL_AREA_ALARM_DURESS, 6),
    TEMPERATURE(CHANNEL_AREA_ALARM_TEMPERATURE, 7);

    private final String channelUID;
    private final int bit;

    AreaAlarm(String channelUID, int bit) {
        this.channelUID = channelUID;
        this.bit = bit;
    }

    public boolean isSet(BigInteger alarmBits) {
        return alarmBits.testBit(bit);
    }

    public boolean isSet(int alarmBits) {
        return isSet(BigInteger.valueOf(alarmBits));
    }

    public String getChannelUID() {
        return channelUID;
    }
}
