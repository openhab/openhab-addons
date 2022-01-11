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
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.PrimitiveType;

/**
 * The {@link SoulissTopicsHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Luca Remigio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */
@NonNullByDefault
public class SoulissTopicsHandler extends SoulissGenericActionMessage implements TypicalCommonMethods {

    private float fSetPointValue = 0xFFFF;

    public SoulissTopicsHandler(Thing pThing) {
        super(pThing);
        thingGenActMsg = pThing;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        // status online
        updateStatus(ThingStatus.ONLINE);
    }

    public void setState(PrimitiveType state) {
        this.updateState(SoulissBindingConstants.T5N_VALUE_CHANNEL, (DecimalType) state);
    }

    public void setFloatValue(float valueOf) {
        this.updateState(SoulissBindingConstants.LASTSTATUSSTORED_CHANNEL, this.getLastUpdateTime());
        if (fSetPointValue != valueOf) {
            this.setState(DecimalType.valueOf(Float.toString(valueOf)));
            fSetPointValue = valueOf;
        }
    }

    public float getFloatState() {
        return fSetPointValue;
    }

    @Override
    public void setRawState(byte rawState) {
        throw new UnsupportedOperationException("Not Implemented, yet.");
    }

    @Override
    public byte getRawState() {
        throw new UnsupportedOperationException("Not Implemented, yet.");
    }

    @Override
    public byte getExpectedRawState(byte bCommand) {
        return -1;
    }
}
