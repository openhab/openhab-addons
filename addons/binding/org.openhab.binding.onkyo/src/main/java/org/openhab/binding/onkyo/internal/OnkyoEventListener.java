/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.onkyo.internal;

import org.openhab.binding.onkyo.internal.eiscp.EiscpMessage;

/**
 * This interface defines interface to receive status updates from Onkyo receiver.
 *
 * @author Pauli Anttila
 */
public interface OnkyoEventListener {

    /**
     * Procedure for receive status update from Onkyo AV receiver.
     *
     * @param data
     *            Received data.
     */
    void statusUpdateReceived(String ip, EiscpMessage data);

    /**
     * Procedure for connection error events from Onkyo AV receiver.
     *
     */
    void connectionError(String ip);
}
