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
package org.openhab.binding.powermax.internal.message;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.powermax.internal.state.PowermaxState;

/**
 * A class for PowerMaster message handling
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
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
    protected @Nullable PowermaxState handleMessageInternal(@Nullable PowermaxCommManager commManager) {
        if (commManager == null) {
            return null;
        }

        byte[] message = getRawData();
        byte msgType = message[2];
        byte subType = message[3];
        byte msgLen = message[4];

        debug("Type", msgType);
        debug("Subtype", subType);
        debug("Message length", msgLen);

        if ((msgType == 0x03) && (subType == 0x39)) {
            commManager.sendMessage(PowermaxSendType.POWERMASTER_ZONE_STAT1);
            commManager.sendMessage(PowermaxSendType.POWERMASTER_ZONE_STAT2);
        }

        return null;
    }
}
