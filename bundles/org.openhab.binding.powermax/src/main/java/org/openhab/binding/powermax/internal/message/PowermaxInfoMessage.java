/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.openhab.binding.powermax.internal.state.PowermaxPanelType;
import org.openhab.binding.powermax.internal.state.PowermaxState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for INFO message handling
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxInfoMessage extends PowermaxBaseMessage {

    private final Logger logger = LoggerFactory.getLogger(PowermaxInfoMessage.class);

    /**
     * Constructor
     *
     * @param message
     *            the received message as a buffer of bytes
     */
    public PowermaxInfoMessage(byte[] message) {
        super(message);
    }

    @Override
    public PowermaxState handleMessage(PowermaxCommManager commManager) {
        super.handleMessage(commManager);

        if (commManager == null) {
            return null;
        }

        PowermaxState updatedState = commManager.createNewState();

        byte[] message = getRawData();

        PowermaxPanelType panelType = null;
        try {
            panelType = PowermaxPanelType.fromCode(message[7]);
        } catch (IllegalArgumentException e) {
            logger.debug("Powermax alarm binding: unknwon panel type for code {}", message[7] & 0x000000FF);
            panelType = null;
        }

        logger.debug("Reading panel settings");
        updatedState.setDownloadMode(true);
        commManager.sendMessage(PowermaxSendType.DL_PANELFW);
        commManager.sendMessage(PowermaxSendType.DL_SERIAL);
        commManager.sendMessage(PowermaxSendType.DL_ZONESTR);
        commManager.sendSetTime();
        if ((panelType != null) && panelType.isPowerMaster()) {
            commManager.sendMessage(PowermaxSendType.DL_MR_SIRKEYZON);
        }
        commManager.sendMessage(PowermaxSendType.START);
        commManager.sendMessage(PowermaxSendType.EXIT);

        return updatedState;
    }

    @Override
    public String toString() {
        String str = super.toString();

        byte[] message = getRawData();
        byte panelTypeNr = message[7];

        str += "\n - panel type number = " + String.format("%02X", panelTypeNr);

        return str;
    }
}
