/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.connector;

/**
 * This interface defines interface to receive data from RFXCOM controller.
 *
 * @author Pauli Anttila - Initial contribution
 */
public interface RFXComEventListener {

    /**
     * Procedure for receive raw data from RFXCOM controller.
     *
     * @param data
     *            Received raw data.
     */
    void packetReceived(byte[] data);

    /**
     * Procedure for receiving information fatal error.
     *
     * @param error
     *            Error occurred.
     */
    void errorOccurred(String error);

}
