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
 * A class for ACK message handling
 *
 * @author Laurent Garnier - Initial contribution
 */
public class PowermaxAckMessage extends PowermaxBaseMessage {

    /**
     * Constructor
     *
     * @param message
     *            the received message as a buffer of bytes
     */
    public PowermaxAckMessage(byte[] message) {
        super(message);
    }

    @Override
    public PowermaxState handleMessage(PowermaxCommManager commManager) {
        super.handleMessage(commManager);

        if (commManager == null) {
            return null;
        }

        PowermaxState updatedState = null;

        if (commManager.getLastSendMsg().getSendType() == PowermaxSendType.EXIT) {
            updatedState = commManager.createNewState();
            updatedState.setPowerlinkMode(true);
            updatedState.setDownloadMode(false);
        }

        return updatedState;
    }

}
