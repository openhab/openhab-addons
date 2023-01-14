/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for POWERLINK message handling
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class PowermaxPowerlinkMessage extends PowermaxBaseMessage {

    private final Logger logger = LoggerFactory.getLogger(PowermaxPowerlinkMessage.class);

    /**
     * Constructor
     *
     * @param message
     *            the received message as a buffer of bytes
     */
    public PowermaxPowerlinkMessage(byte[] message) {
        super(message);
    }

    @Override
    protected @Nullable PowermaxState handleMessageInternal(@Nullable PowermaxCommManager commManager) {
        if (commManager == null) {
            return null;
        }

        PowermaxState updatedState = null;

        byte[] message = getRawData();
        byte subType = message[2];

        if (subType == 0x03) {
            // keep alive message

            debug("Subtype", subType, "Keep Alive");

            commManager.sendAck(this, (byte) 0x02);
            updatedState = commManager.createNewState();
            updatedState.lastKeepAlive.setValue(System.currentTimeMillis());
        } else if (subType == 0x0A) {
            byte enroll = message[4];

            debug("Subtype", subType, "Enroll");
            debug("Enroll", enroll);

            if (enroll == 0x01) {
                logger.debug("Powermax alarm binding: Enrolling Powerlink");
                commManager.enrollPowerlink();
                updatedState = commManager.createNewState();
                updatedState.downloadSetupRequired.setValue(true);
            } else {
                commManager.sendAck(this, (byte) 0x02);
            }
        } else {
            debug("Subtype", subType, "UNKNOWN");
            commManager.sendAck(this, (byte) 0x02);
        }

        return updatedState;
    }
}
