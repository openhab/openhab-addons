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
package org.openhab.binding.onkyo.internal;

import org.openhab.binding.onkyo.internal.eiscp.EiscpMessage;

/**
 * This interface defines interface to receive status updates from Onkyo receiver.
 *
 * @author Pauli Anttila - Initial contribution
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
     * @param errorMsg
     *            Reason for error.
     */
    void connectionError(String ip, String errorMsg);
}
