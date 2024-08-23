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
package org.openhab.binding.souliss.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.souliss.internal.SoulissBindingConstants;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;

/**
 * The {@link SoulissT5nHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */
@NonNullByDefault
public class SoulissT5nHandler extends SoulissGenericHandler {

    private float fVal = 0xF;

    public SoulissT5nHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        updateStatus(ThingStatus.UNKNOWN);
    }

    public void setState(QuantityType<?> state) {
        this.updateState(SoulissBindingConstants.T5N_VALUE_CHANNEL, state);
    }

    @Override
    public void setRawState(byte rawState) {
        throw new UnsupportedOperationException("Not Implemented, yet.");
    }

    public void setFloatValue(float valueOf) {
        super.setLastStatusStored();
        if (fVal != valueOf) {
            this.setState(QuantityType.valueOf(Float.toString(valueOf)));
            fVal = valueOf;
        }
    }

    @Override
    public byte getRawState() {
        throw new UnsupportedOperationException("Not Implemented, yet.");
    }

    public float getFloatState() {
        return fVal;
    }

    @Override
    public byte getExpectedRawState(byte bCommand) {
        // Secure Send is disabled
        return -1;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        throw new UnsupportedOperationException("Unsupported operation. Read Only");
    }
}
