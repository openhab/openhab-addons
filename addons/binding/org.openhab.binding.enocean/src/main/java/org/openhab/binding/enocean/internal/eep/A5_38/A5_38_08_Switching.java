/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.eep.A5_38;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.enocean.internal.eep.Base._4BSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class A5_38_08_Switching extends _4BSMessage {

    static final byte CommandId = 0x01;
    static final byte SwitchOff = 0x00;
    static final byte SwitchOn = 0x01;

    public A5_38_08_Switching() {
        super();
    }

    public A5_38_08_Switching(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected void convertFromCommandImpl(String channelId, String channelTypeId, Command outputCommand,
            State currentState, Configuration config) {

        if ((OnOffType) outputCommand == OnOffType.ON) {
            setData(CommandId, Zero, Zero, (byte) (TeachInBit | SwitchOn));
        } else {
            setData(CommandId, Zero, Zero, (byte) (TeachInBit | SwitchOff));
        }
    }
}
