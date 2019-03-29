/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.net.internal.handler;

import static org.openhab.binding.net.internal.NetBindingConstants.CHANNEL_DATA_RECEIVED;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.net.internal.DataConverter;
import org.openhab.binding.net.internal.DataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractServerBridge extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(AbstractServerBridge.class);

    public AbstractServerBridge(Bridge bridge) {
        super(bridge);
    }

    private List<DataListener> dataListeners = new CopyOnWriteArrayList<>();

    protected boolean registerDataListener(DataListener dataListener) {
        if (dataListener == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null dataListener.");
        }
        return dataListeners.contains(dataListener) ? false : dataListeners.add(dataListener);
    }

    protected boolean unregisterDataListener(DataListener dataListener) {
        if (dataListener == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null dataListener.");
        }
        return dataListeners.remove(dataListener);
    }

    protected void sendMessageToListeners(Object data) {
        for (DataListener listener : dataListeners) {
            try {
                listener.dataReceived(getThing().getUID(), data);
            } catch (RuntimeException e) {
                // catch all exceptions give all handlers a fair chance of handling the messages
                // logger.error("An exception occurred while calling the DeviceStatusListener", e);
            }
        }
    }

    protected void sendData(String convertTo, byte[] bytes) {
        if (logger.isDebugEnabled()) {
            logger.debug("Received datagram: {}", HexUtils.bytesToHex(bytes));
        }
        try {
            Object data = new DataConverter(convertTo).convertBytes(bytes);
            sendMessageToListeners(data);
            if (data instanceof String) {
                triggerChannel(CHANNEL_DATA_RECEIVED, (String) data);
            }
            if (data instanceof byte[]) {
                String hexaString = HexUtils.bytesToHex(bytes);
                triggerChannel(CHANNEL_DATA_RECEIVED, hexaString);
            }
        } catch (UnsupportedEncodingException e) {
            logger.debug("UnsupportedEncodingException: reason {}.", e.getMessage(), e);
        }
    }
}
