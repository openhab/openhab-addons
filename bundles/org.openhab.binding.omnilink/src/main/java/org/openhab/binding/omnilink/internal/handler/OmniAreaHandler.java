/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
                return OmniLinkCmd.CMD_SECURITY_OMNI_DISARM.getNumber();
            case CHANNEL_AREA_SECURITY_MODE_DAY:
                return OmniLinkCmd.CMD_SECURITY_OMNI_DAY_MODE.getNumber();
            case CHANNEL_AREA_SECURITY_MODE_NIGHT:
                return OmniLinkCmd.CMD_SECURITY_OMNI_NIGHT_MODE.getNumber();
            case CHANNEL_AREA_SECURITY_MODE_AWAY:
                return OmniLinkCmd.CMD_SECURITY_OMNI_AWAY_MODE.getNumber();
            case CHANNEL_AREA_SECURITY_MODE_VACATION:
                return OmniLinkCmd.CMD_SECURITY_OMNI_VACATION_MODE.getNumber();
            case CHANNEL_AREA_SECURITY_MODE_DAY_INSTANT:
                return OmniLinkCmd.CMD_SECURITY_OMNI_DAY_INSTANCE_MODE.getNumber();
            case CHANNEL_AREA_SECURITY_MODE_NIGHT_DELAYED:
                return OmniLinkCmd.CMD_SECURITY_OMNI_NIGHT_DELAYED_MODE.getNumber();
            default:
                throw new IllegalStateException("Unknown channel for area thing " + channelUID);
        }
    }

    @Override
    protected EnumSet<AreaAlarm> getAlarms() {
        return OMNI_ALARMS;
    }
}
