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
package org.openhab.binding.rfxcom.internal.connector;

import java.io.IOException;

import org.openhab.binding.rfxcom.internal.config.RFXComBridgeConfiguration;

/**
 * This interface defines interface to communicate RFXCOM controller.
 *
 * @author Pauli Anttila - Initial contribution
 */
public interface RFXComConnectorInterface {

    /**
     * Procedure for connecting to RFXCOM controller.
     *
     * @param device Controller connection parameters (e.g. serial port name or IP address).
     */
    void connect(RFXComBridgeConfiguration device) throws Exception;

    /**
     * Procedure for disconnecting to RFXCOM controller.
     *
     */
    void disconnect();

    /**
     * Procedure for send raw data to RFXCOM controller.
     *
     * @param data raw bytes.
     */
    void sendMessage(byte[] data) throws IOException;

    /**
     * Procedure for register event listener.
     *
     * @param listener Event listener instance to handle events.
     */
    void addEventListener(RFXComEventListener listener);

    /**
     * Procedure for remove event listener.
     *
     * @param listener Event listener instance to remove.
     */
    void removeEventListener(RFXComEventListener listener);
}
