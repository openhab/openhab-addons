/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.powermax.internal.message;

import org.openhab.binding.powermax.internal.state.PowermaxState;

/**
 * A class for PowerMaster message handling
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxPowerMasterMessage extends PowermaxBaseMessage {

    /**
     * Constructor
     *
     * @param message
     *            the received message as a buffer of bytes
     */
    public PowermaxPowerMasterMessage(byte[] message) {
        super(message);
    }

    @Override
    public PowermaxState handleMessage(PowermaxCommManager commManager) {
        super.handleMessage(commManager);

        if (commManager == null) {
            return null;
        }

        byte[] message = getRawData();
        byte msgType = message[2];
        byte subType = message[3];

        if ((msgType == 0x03) && (subType == 0x39)) {
            commManager.sendMessage(PowermaxSendType.POWERMASTER_ZONE_STAT1);
            commManager.sendMessage(PowermaxSendType.POWERMASTER_ZONE_STAT2);
        }

        return null;
    }

    @Override
    public String toString() {
        String str = super.toString();

        byte[] message = getRawData();
        byte msgType = message[2];
        byte subType = message[3];
        byte msgLen = message[4];

        str += "\n - type = " + String.format("%02X", msgType);
        str += "\n - subtype = " + String.format("%02X", subType);
        str += "\n - msgLen = " + String.format("%02X", msgLen);

        return str;
    }

}
