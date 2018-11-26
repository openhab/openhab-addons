/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.eep.A5_38;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.CHANNEL_DIMMER;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class A5_38_08_Dimming extends _4BSMessage {

    static final byte CommandId = 0x02;
    static final byte SwitchOff = 0x00;
    static final byte SwitchOn = 0x01;
    static final byte Switch100Percent = 0x64;

    public A5_38_08_Dimming() {
        super();
    }

    public A5_38_08_Dimming(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command outputCommand,
            State currentState, Configuration config) {

        if (outputCommand instanceof DecimalType) {
            if (((DecimalType) outputCommand).equals(DecimalType.ZERO)) {
                setData(CommandId, Zero, Zero, (byte) (TeachInBit | SwitchOff));
            } else {
                setData(CommandId, ((DecimalType) outputCommand).byteValue(), Zero, (byte) (TeachInBit | SwitchOn));
            }
        } else if ((OnOffType) outputCommand == OnOffType.ON) {
            setData(CommandId, Switch100Percent, Zero, (byte) (TeachInBit | SwitchOn));
        } else {
            setData(CommandId, Zero, Zero, (byte) (TeachInBit | SwitchOff));
        }
    }

    @Override
    public State convertToStateImpl(String channelId, String channelTypeId, State currentState, Configuration config) {
        if (!isValid()) {
            return UnDefType.UNDEF;
        }

        if (channelId == CHANNEL_DIMMER) {
            if (getDB_0() == (TeachInBit | SwitchOff)) {
                return new PercentType(0);
            } else {
                return new PercentType(getDB_2Value());
            }
        }

        return UnDefType.UNDEF;
    }
}
