/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.souliss.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.souliss.internal.SoulissBindingConstants;
import org.openhab.binding.souliss.internal.protocol.HalfFloatUtils;
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
@NonNullByDefault
public class SoulissT6nHandler extends SoulissGenericHandler {

    private float fSetPointValue = 0xFFFF;

    public SoulissT6nHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof DecimalType) {
            int uu = HalfFloatUtils.fromFloat(((DecimalType) command).floatValue());
            byte b2 = (byte) (uu >> 8);
            byte b1 = (byte) uu;
            // setpoint command
            commandSEND(b1, b2);
        }
    }

    @Override
    public void initialize() {
        super.initialize();

        updateStatus(ThingStatus.UNKNOWN);
    }

    public void setState(PrimitiveType state) {
        this.updateState(SoulissBindingConstants.T6N_VALUE_CHANNEL, (DecimalType) state);
    }

    @Override
    public void setRawState(byte rawState) {
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
