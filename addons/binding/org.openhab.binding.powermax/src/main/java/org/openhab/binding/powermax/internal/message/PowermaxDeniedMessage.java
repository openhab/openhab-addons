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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class for DENIED message handling
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxDeniedMessage extends PowermaxBaseMessage {

    private final Logger logger = LoggerFactory.getLogger(PowermaxDeniedMessage.class);

    /**
     * Constructor
     *
     * @param message
     *            the received message as a buffer of bytes
     */
    public PowermaxDeniedMessage(byte[] message) {
        super(message);
    }

    @Override
    public PowermaxState handleMessage(PowermaxCommManager commManager) {
        super.handleMessage(commManager);

        if (commManager == null) {
            return null;
        }

        PowermaxState updatedState = null;

        PowermaxSendType lastSendType = commManager.getLastSendMsg().getSendType();
        if (lastSendType == PowermaxSendType.EVENTLOG || lastSendType == PowermaxSendType.ARM
                || lastSendType == PowermaxSendType.BYPASS) {
            logger.debug("Powermax alarm binding: invalid PIN code");
        } else if (lastSendType == PowermaxSendType.DOWNLOAD) {
            logger.debug("Powermax alarm binding: openHAB Powerlink not enrolled");
            updatedState = commManager.createNewState();
            updatedState.setPowerlinkMode(false);
        }

        return updatedState;
    }

}
