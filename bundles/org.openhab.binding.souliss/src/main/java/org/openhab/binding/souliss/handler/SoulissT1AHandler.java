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

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.souliss.SoulissBindingConstants;

/**
 * The {@link SoulissT1AHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Luca Remigio - Initial contribution
 */
public class SoulissT1AHandler extends SoulissGenericHandler {
    Configuration gwConfigurationMap;
    // private Logger logger = LoggerFactory.getLogger(SoulissT1AHandler.class);
    byte T1nRawState;

    public SoulissT1AHandler(Thing _thing) {
        super(_thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    private OpenClosedType getTypeFromBool(boolean value) {
        if (value == false) {
            return OpenClosedType.CLOSED;
        }
        return OpenClosedType.OPEN;
    }

    private boolean getBitState(int value, int bit) {

        if ((value & (1L << bit)) == 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void setRawState(byte _rawState) {

        // update Last Status stored time
        super.setLastStatusStored();
        // update item state only if it is different from previous
        if (T1nRawState != _rawState) {
            this.updateState(SoulissBindingConstants.T1A_1_CHANNEL, getTypeFromBool(getBitState(_rawState, 0)));
            this.updateState(SoulissBindingConstants.T1A_2_CHANNEL, getTypeFromBool(getBitState(_rawState, 1)));
            this.updateState(SoulissBindingConstants.T1A_3_CHANNEL, getTypeFromBool(getBitState(_rawState, 2)));
            this.updateState(SoulissBindingConstants.T1A_4_CHANNEL, getTypeFromBool(getBitState(_rawState, 3)));
            this.updateState(SoulissBindingConstants.T1A_5_CHANNEL, getTypeFromBool(getBitState(_rawState, 4)));
            this.updateState(SoulissBindingConstants.T1A_6_CHANNEL, getTypeFromBool(getBitState(_rawState, 5)));
            this.updateState(SoulissBindingConstants.T1A_7_CHANNEL, getTypeFromBool(getBitState(_rawState, 6)));
            this.updateState(SoulissBindingConstants.T1A_8_CHANNEL, getTypeFromBool(getBitState(_rawState, 7)));
        }
        T1nRawState = _rawState;
    }

    @Override
    public byte getRawState() {
        return T1nRawState;
    }

    @Override
    public byte getExpectedRawState(byte bCommand) {
        // Secure Send is disabled
        return -1;
    }
}