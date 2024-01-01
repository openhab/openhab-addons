/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import org.openhab.binding.powermax.internal.state.PowermaxPanelType;
import org.openhab.binding.powermax.internal.state.PowermaxState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for INFO message handling
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
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
    protected @Nullable PowermaxState handleMessageInternal(@Nullable PowermaxCommManager commManager) {
        if (commManager == null) {
            return null;
        }

        PowermaxState updatedState = commManager.createNewState();

        byte[] message = getRawData();
        byte panelTypeNr = message[7];
        String panelTypeStr;

        PowermaxPanelType panelType = null;
        try {
            panelType = PowermaxPanelType.fromCode(panelTypeNr);
            panelTypeStr = panelType.toString();
        } catch (IllegalArgumentException e) {
            panelType = null;
            panelTypeStr = "UNKNOWN";
        }

        debug("Panel type", panelTypeNr, panelTypeStr);

        logger.debug("Reading panel settings");
        updatedState.downloadMode.setValue(true);
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
}
