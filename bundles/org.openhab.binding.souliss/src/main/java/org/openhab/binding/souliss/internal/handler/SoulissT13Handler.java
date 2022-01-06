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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.souliss.internal.SoulissBindingConstants;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.PrimitiveType;
import org.openhab.core.types.RefreshType;

/**
 * The {@link SoulissT13Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */

@NonNullByDefault
public class SoulissT13Handler extends SoulissGenericHandler {

    private byte t1nRawState = 0xF;

    public SoulissT13Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        updateStatus(ThingStatus.UNKNOWN);
    }

    public void setState(@Nullable PrimitiveType state) {
        super.setLastStatusStored();
        if (state != null) {
            if (state instanceof OnOffType) {
                this.updateState(SoulissBindingConstants.STATEONOFF_CHANNEL, (OnOffType) state);
            }

            if (state instanceof OpenClosedType) {
                this.updateState(SoulissBindingConstants.STATEOPENCLOSE_CHANNEL, (OpenClosedType) state);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case SoulissBindingConstants.STATEONOFF_CHANNEL:
                    OnOffType valonOff = getOhStateOnOffFromSoulissVal(t1nRawState);
                    if (valonOff != null) {
                        updateState(channelUID, valonOff);
                    }
                    break;
                case SoulissBindingConstants.STATEOPENCLOSE_CHANNEL:
                    OpenClosedType valOpenClose = getOhStateOpenCloseFromSoulissVal(t1nRawState);
                    if (valOpenClose != null) {
                        updateState(channelUID, valOpenClose);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void setRawState(byte rawState) {
        // update Last Status stored time
        super.setLastStatusStored();
        // update item state only if it is different from previous
        if (t1nRawState != rawState) {
            this.setState(getOhStateOpenCloseFromSoulissVal(rawState));
            this.setState(getOhStateOnOffFromSoulissVal(rawState));
        }
        t1nRawState = rawState;
    }

    @Override
    public byte getRawState() {
        return 0;
    }

    @Override
    public byte getExpectedRawState(byte bCommand) {
        // Secure Send is disabled
        return -1;
    }
}
