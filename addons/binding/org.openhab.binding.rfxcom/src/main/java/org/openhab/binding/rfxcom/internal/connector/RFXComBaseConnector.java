/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.connector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RFXCOM connector for serial port communication.
 *
 * @author James Hewitt-Thomas
 */
public abstract class RFXComBaseConnector implements RFXComConnectorInterface {

    private static final Logger logger = LoggerFactory.getLogger(RFXComBaseConnector.class);

    private static List<RFXComEventListener> _listeners = new ArrayList<RFXComEventListener>();

    public RFXComBaseConnector() {
    }

    @Override
    public synchronized void addEventListener(RFXComEventListener rfxComEventListener) {
        if (!_listeners.contains(rfxComEventListener)) {
            _listeners.add(rfxComEventListener);
        }
    }

    @Override
    public synchronized void removeEventListener(RFXComEventListener listener) {
        _listeners.remove(listener);
    }

    void sendMsgToListeners(byte[] msg) {
        try {
            Iterator<RFXComEventListener> iterator = _listeners.iterator();

            while (iterator.hasNext()) {
                iterator.next().packetReceived(msg);
            }

        } catch (Exception e) {
            logger.error("Event listener invoking error", e);
        }
    }

    void sendErrorToListeners(String error) {
        try {
            Iterator<RFXComEventListener> iterator = _listeners.iterator();

            while (iterator.hasNext()) {
                iterator.next().errorOccurred(error);
            }

        } catch (Exception e) {
            logger.error("Event listener invoking error", e);
        }
    }
}