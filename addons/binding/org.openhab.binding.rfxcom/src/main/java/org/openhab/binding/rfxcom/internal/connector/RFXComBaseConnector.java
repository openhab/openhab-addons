/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.connector;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RFXCOM connector for serial port communication.
 *
 * @author James Hewitt-Thomas
 */
public abstract class RFXComBaseConnector implements RFXComConnectorInterface {
    private final Logger logger = LoggerFactory.getLogger(RFXComBaseConnector.class);

    private List<RFXComEventListener> listeners = new ArrayList<>();
    protected InputStream in;

    @Override
    public synchronized void addEventListener(RFXComEventListener rfxComEventListener) {
        if (!listeners.contains(rfxComEventListener)) {
            listeners.add(rfxComEventListener);
        }
    }

    @Override
    public synchronized void removeEventListener(RFXComEventListener listener) {
        listeners.remove(listener);
    }

    void sendMsgToListeners(byte[] msg) {
        try {
            for (RFXComEventListener listener : listeners) {
                listener.packetReceived(msg);
            }
        } catch (Exception e) {
            logger.error("Event listener invoking error", e);
        }
    }

    void sendErrorToListeners(String error) {
        try {
            for (RFXComEventListener listener : listeners) {
                listener.errorOccurred(error);
            }
        } catch (Exception e) {
            logger.error("Event listener invoking error", e);
        }
    }

    int read(byte[] buffer, int offset, int length) throws IOException {
        return in.read(buffer, offset, length);
    }
}
