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
package org.openhab.binding.omnilink.internal.handler;

import static org.openhab.binding.omnilink.internal.AreaAlarm.*;
import static org.openhab.binding.omnilink.internal.OmnilinkBindingConstants.*;

import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.omnilink.internal.AreaAlarm;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;

import com.digitaldan.jomnilinkII.MessageTypes.CommandMessage;

/**
 * The {@link OmniAreaHandler} defines some methods that are used to
 * interface with an OmniLink OmniPro Area. This by extension also defines the
 * OmniPro Area thing that openHAB will be able to pick up and interface with.
 *
 * @author Craig Hamilton - Initial contribution
 * @author Ethan Dye - openHAB3 rewrite
 */
@NonNullByDefault
public class OmniAreaHandler extends AbstractAreaHandler {
    private static final EnumSet<AreaAlarm> OMNI_ALARMS = EnumSet.of(BURGLARY, FIRE, GAS, AUXILIARY, FREEZE, WATER,
            DURESS, TEMPERATURE);
    public @Nullable String number;

    public OmniAreaHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected int getMode(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case CHANNEL_AREA_SECURITY_MODE_DISARM:
                return CommandMessage.CMD_SECURITY_OMNI_DISARM;
            case CHANNEL_AREA_SECURITY_MODE_DAY:
                return CommandMessage.CMD_SECURITY_OMNI_DAY_MODE;
            case CHANNEL_AREA_SECURITY_MODE_NIGHT:
                return CommandMessage.CMD_SECURITY_OMNI_NIGHT_MODE;
            case CHANNEL_AREA_SECURITY_MODE_AWAY:
                return CommandMessage.CMD_SECURITY_OMNI_AWAY_MODE;
            case CHANNEL_AREA_SECURITY_MODE_VACATION:
                return CommandMessage.CMD_SECURITY_OMNI_VACATION_MODE;
            case CHANNEL_AREA_SECURITY_MODE_DAY_INSTANT:
                return CommandMessage.CMD_SECURITY_OMNI_DAY_INSTANT_MODE;
            case CHANNEL_AREA_SECURITY_MODE_NIGHT_DELAYED:
                return CommandMessage.CMD_SECURITY_OMNI_NIGHT_DELAYED_MODE;
            default:
                throw new IllegalStateException("Unknown channel for area thing " + channelUID);
        }
    }

    @Override
    protected EnumSet<AreaAlarm> getAlarms() {
        return OMNI_ALARMS;
    }
}
