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
 * The {@link SoulissTopicsHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Luca Remigio - Initial contribution
 */

public class SoulissTopicsHandler extends SoulissGenericActionMessage implements TypicalCommonMethods {

    // private Logger logger = LoggerFactory.getLogger(SoulissTopicsHandler.class);
    // private DecimalType _setPointValue = DecimalType.ZERO;
    private float fSetPointValue;

    public SoulissTopicsHandler(Thing _thing) {
        super(_thing);
        thing = _thing;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        // status online
        updateStatus(ThingStatus.ONLINE);
    }

    public void setState(PrimitiveType _state) {
        if (_state != null) {
            this.updateState(SoulissBindingConstants.T5N_VALUE_CHANNEL, (DecimalType) _state);
        }
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
    public void setRawState(byte _rawState) {
        throw new NotImplementedException();
    }

    @Override
    public byte getRawState() {
        throw new NotImplementedException();
    }

    @Override
    public byte getExpectedRawState(byte bCommand) {
        // TODO Auto-generated method stub
        return -1;
    }
}
