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
package org.openhab.binding.nibeheatpump.internal.connection;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.nibeheatpump.internal.NibeHeatPumpException;
import org.openhab.binding.nibeheatpump.internal.message.MessageFactory;
import org.openhab.binding.nibeheatpump.internal.message.NibeHeatPumpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NibeHeatPumpBaseConnector} define abstract class for Nibe connectors. All connector implementations should
 * extend this class.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public abstract class NibeHeatPumpBaseConnector implements NibeHeatPumpConnector {
    private final Logger logger = LoggerFactory.getLogger(NibeHeatPumpBaseConnector.class);

    private final List<NibeHeatPumpEventListener> listeners = new ArrayList<>();
    public boolean connected = false;

    @Override
    public synchronized void addEventListener(NibeHeatPumpEventListener listener) {
        if (!listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    @Override
    public synchronized void removeEventListener(NibeHeatPumpEventListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    public void sendMsgToListeners(byte[] data) {
        try {
            NibeHeatPumpMessage msg = MessageFactory.getMessage(data);
            sendMsgToListeners(msg);
        } catch (NibeHeatPumpException e) {
            logger.debug("Invalid message received, exception {}", e.getMessage());
        }
    }

    public void sendMsgToListeners(NibeHeatPumpMessage msg) {
        if (msg != null) {
            for (NibeHeatPumpEventListener listener : listeners) {
                try {
                    listener.msgReceived(msg);
                } catch (Exception e) {
                    logger.error("Event listener invoking error", e);
                }
            }
        }
    }

    public void sendErrorToListeners(String error) {
        for (NibeHeatPumpEventListener listener : listeners) {
            try {
                listener.errorOccurred(error);
            } catch (Exception e) {
                logger.error("Event listener invoking error", e);
            }
        }
    }
}
