/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.souliss.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.handler.SoulissGenericTypical.typicalCommonMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SoulissT1AHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Luca Remigio - Initial contribution
 */
public class SoulissT1AHandler extends SoulissGenericTypical implements typicalCommonMethods {
    private Logger logger = LoggerFactory.getLogger(SoulissT1AHandler.class);

    public SoulissT1AHandler(Thing _thing) {
        super(_thing);
        thing = _thing;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void setState(PrimitiveType _state) {
        super.setLastStatusStored();
        if (_state != null) {
            int valore = Integer.parseInt(_state.toString());
            this.updateState(SoulissBindingConstants.T1A_1_CHANNEL, getTypeFromBool(getBitState(valore, 0)));
            this.updateState(SoulissBindingConstants.T1A_2_CHANNEL, getTypeFromBool(getBitState(valore, 1)));
            this.updateState(SoulissBindingConstants.T1A_3_CHANNEL, getTypeFromBool(getBitState(valore, 2)));
            this.updateState(SoulissBindingConstants.T1A_4_CHANNEL, getTypeFromBool(getBitState(valore, 3)));
            this.updateState(SoulissBindingConstants.T1A_5_CHANNEL, getTypeFromBool(getBitState(valore, 4)));
            this.updateState(SoulissBindingConstants.T1A_6_CHANNEL, getTypeFromBool(getBitState(valore, 5)));
            this.updateState(SoulissBindingConstants.T1A_7_CHANNEL, getTypeFromBool(getBitState(valore, 6)));
            this.updateState(SoulissBindingConstants.T1A_8_CHANNEL, getTypeFromBool(getBitState(valore, 7)));
        }
    }

    private OnOffType getTypeFromBool(boolean value) {
        if (value == false) {
            return OnOffType.OFF;
        }
        return OnOffType.ON;
    }

    private boolean getBitState(int value, int bit) {
        if ((value & (1L << bit)) == 0) {
            return false;
        }
        return true;
    }
}