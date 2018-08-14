/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.eep.F6_02;

import static org.openhab.binding.enocean.EnOceanBindingConstants.*;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.enocean.internal.eep.Base._RPSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class F6_02_01_Virtual extends _RPSMessage {

    final byte AI = 0;
    final byte A0 = 1;
    final byte BI = 2;
    final byte B0 = 3;
    final byte PRESSED = 16;

    public F6_02_01_Virtual() {
        super();
    }

    public F6_02_01_Virtual(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected void convertFromCommandImpl(Command command, String channelId, State currentState, Configuration config) {

        if (command instanceof StringType) {

            StringType s = (StringType) command;

            if (s.equals(CommonTriggerEvents.DIR1_RELEASED) || s.equals(CommonTriggerEvents.DIR2_RELEASED)) {
                setStatus(_RPSMessage.T21Flag);
                setData((byte) 0x00);
                return;
            }

            switch (channelId) {
                case CHANNEL_VIRTUALROCKERSWITCH_CHANNELA:
                    if (s.equals(CommonTriggerEvents.DIR1_PRESSED)) {
                        setStatus((byte) (_RPSMessage.T21Flag | _RPSMessage.NUFlag));
                        setData((byte) ((A0 << 5) | PRESSED));
                    } else if (s.equals(CommonTriggerEvents.DIR2_PRESSED)) {
                        setStatus((byte) (_RPSMessage.T21Flag | _RPSMessage.NUFlag));
                        setData((byte) ((AI << 5) | PRESSED));
                    }
                    break;

                case CHANNEL_VIRTUALROCKERSWITCH_CHANNELB:
                    if (s.equals(CommonTriggerEvents.DIR1_PRESSED)) {
                        setStatus((byte) (_RPSMessage.T21Flag | _RPSMessage.NUFlag));
                        setData((byte) ((B0 << 5) | PRESSED));
                    } else if (s.equals(CommonTriggerEvents.DIR2_PRESSED)) {
                        setStatus((byte) (_RPSMessage.T21Flag | _RPSMessage.NUFlag));
                        setData((byte) ((BI << 5) | PRESSED));
                    }
                    break;

            }
        }

    }
}
