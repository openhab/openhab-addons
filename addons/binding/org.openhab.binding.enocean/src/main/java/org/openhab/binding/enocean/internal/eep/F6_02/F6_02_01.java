/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.eep.F6_02;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.openhab.binding.enocean.internal.eep.Base._RPSMessage;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class F6_02_01 extends _RPSMessage {

    final byte AI = 0;
    final byte A0 = 1;
    final byte BI = 2;
    final byte B0 = 3;
    final byte PRESSED = 16;

    int secondByte = -1;
    int secondStatus = -1;

    public F6_02_01() {
        super();
    }

    public F6_02_01(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected String convertToEventImpl(String channelId, String lastEvent, Configuration config) {
        if (!isValid()) {
            return null;
        }

        if (t21 && nu) {

            switch (channelId) {
                case CHANNEL_ROCKERSWITCH_CHANNELA:
                    if ((bytes[0] >>> 5) == A0) {
                        return ((bytes[0] & PRESSED) != 0) ? CommonTriggerEvents.DIR1_PRESSED
                                : CommonTriggerEvents.DIR1_RELEASED;
                    } else if ((bytes[0] >>> 5) == AI) {
                        return ((bytes[0] & PRESSED) != 0) ? CommonTriggerEvents.DIR2_PRESSED
                                : CommonTriggerEvents.DIR2_RELEASED;
                    }
                    return null;

                case CHANNEL_ROCKERSWITCH_CHANNELB:
                    if ((bytes[0] >>> 5) == B0) {
                        return ((bytes[0] & PRESSED) != 0) ? CommonTriggerEvents.DIR1_PRESSED
                                : CommonTriggerEvents.DIR1_RELEASED;
                    } else if ((bytes[0] >>> 5) == BI) {
                        return ((bytes[0] & PRESSED) != 0) ? CommonTriggerEvents.DIR2_PRESSED
                                : CommonTriggerEvents.DIR2_RELEASED;
                    }
                    return null;
            }
        } else if (t21 && !nu) {
            if (lastEvent != null && lastEvent.equals(CommonTriggerEvents.DIR1_PRESSED)) {
                return CommonTriggerEvents.DIR1_RELEASED;
            } else if (lastEvent != null && lastEvent.equals(CommonTriggerEvents.DIR2_PRESSED)) {
                return CommonTriggerEvents.DIR2_RELEASED;
            }
        }

        return null;
    }

    @Override
    protected boolean validateData(byte[] bytes) {
        return super.validateData(bytes) && !getBit(bytes[0], 7);
    }
}
