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

import org.apache.commons.lang.NotImplementedException;
import org.openhab.binding.souliss.SoulissBindingConstants;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.PrimitiveType;

/**
 * The {@link SoulissT5nHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Tonino Fazio - Initial contribution
 * @author Luca Calcaterra - Refactor for OH3
 */
public class SoulissT5nHandler extends SoulissGenericHandler {

    // private Logger logger = LoggerFactory.getLogger(SoulissT5nHandler.class);
    float fVal;

    public SoulissT5nHandler(Thing _thing) {
        super(_thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    public void setState(PrimitiveType state) {
        if (state != null) {
            this.updateState(SoulissBindingConstants.T5N_VALUE_CHANNEL, (DecimalType) state);
        }
    }

    @Override
    public void setRawState(byte _rawState) {
        throw new NotImplementedException();
    }

    public void setFloatValue(float valueOf) {
        super.setLastStatusStored();
        if (fVal != valueOf) {
            this.setState(DecimalType.valueOf(Float.toString(valueOf)));
            fVal = valueOf;
        }
    }

    @Override
    public byte getRawState() {
        throw new NotImplementedException();
    }

    public float getFloatState() {
        return fVal;
    }

    @Override
    public byte getExpectedRawState(byte bCommand) {
        // Secure Send is disabled
        return -1;
    }
}
