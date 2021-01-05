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
package org.openhab.binding.souliss.handler;

import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.binding.souliss.internal.HalfFloatUtils;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.PrimitiveType;

/**
 * The {@link SoulissT6nHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Luca Remigio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */

public class SoulissT6nHandler extends SoulissGenericHandler {

    // private Logger logger = LoggerFactory.getLogger(SoulissT6nHandler.class);
    private float fSetPointValue;

    public SoulissT6nHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof DecimalType) {
            int uu = HalfFloatUtils.fromFloat(((DecimalType) command).floatValue());
            byte B2 = (byte) (uu >> 8);
            byte B1 = (byte) uu;
            // setpoint command
            commandSEND(B1, B2);
        }
    }

    @Override
    public void initialize() {
        // status online
        updateStatus(ThingStatus.ONLINE);
    }

    public void setState(PrimitiveType _state) {
        if (_state != null) {
            this.updateState(SoulissBindingConstants.T6N_VALUE_CHANNEL, (DecimalType) _state);
        }
    }

    @Override
    public void setRawState(byte _rawState) {
        throw new UnsupportedOperationException("Not Implemented, yet.");
    }

    public void setFloatValue(float valueOf) {
        super.setLastStatusStored();
        if (fSetPointValue != valueOf) {
            this.setState(DecimalType.valueOf(Float.toString(valueOf)));
            fSetPointValue = valueOf;
        }
    }

    @Override
    public byte getRawState() {
        throw new UnsupportedOperationException("Not Implemented, yet.");
    }

    public float getFloatState() {
        return fSetPointValue;
    }

    @Override
    public byte getExpectedRawState(byte bCommand) {
        return -1;
    }
}
