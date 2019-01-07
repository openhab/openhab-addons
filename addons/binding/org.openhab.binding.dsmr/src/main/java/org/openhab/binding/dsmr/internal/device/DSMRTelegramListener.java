/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dsmr.internal.device;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dsmr.internal.device.connector.DSMRConnectorErrorEvent;
import org.openhab.binding.dsmr.internal.device.connector.DSMRConnectorListener;
import org.openhab.binding.dsmr.internal.device.cosem.CosemObject;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1Telegram.TelegramState;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1TelegramListener;
import org.openhab.binding.dsmr.internal.device.p1telegram.P1TelegramParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper listener to receive telegram data from the connector, send it to the parser and forward data or errors from
 * the parser to the DSMR Device.
 *
 * @author M. Volaart - Initial contribution
 * @author Hilbrand Bouwkamp - Moved this code out of the DSMRPort class, fixed some issues and reduced code
 */
@NonNullByDefault
class DSMRTelegramListener implements P1TelegramListener, DSMRConnectorListener {

    private final Logger logger = LoggerFactory.getLogger(DSMRTelegramListener.class);
    private final P1TelegramParser parser;

    private DSMREventListener dsmrEventListener;

    /**
     * Constructor.
     *
     * @param eventListener listener to send received errors or messages to
     */
    public DSMRTelegramListener(DSMREventListener eventListener) {
        dsmrEventListener = eventListener;
        parser = new P1TelegramParser(this);
    }

    /**
     * Set the DSMR event listener.
     *
     * @param eventListener the listener to set
     */
    public void setDsmrEventListener(DSMREventListener eventListener) {
        this.dsmrEventListener = eventListener;
    }

    @Override
    public void handleData(byte[] data, int length) {
        parser.parseData(data, 0, length);
    }

    @Override
    public void handleErrorEvent(DSMRConnectorErrorEvent portEvent) {
        dsmrEventListener.handleErrorEvent(portEvent);
        parser.reset();
    }

    /**
     * Handler for cosemObjects received in a P1 telegram
     *
     * @param cosemObjects. List of received {@link CosemObject} objects
     * @param telegramState. {@link TelegramState} describing the state of the received telegram.
     */
    @Override
    public void telegramReceived(P1Telegram telegram) {
        TelegramState telegramState = telegram.getTelegramState();
        List<CosemObject> cosemObjects = telegram.getCosemObjects();
        if (logger.isTraceEnabled()) {
            logger.trace("Received {} Cosem Objects with state: '{}'", cosemObjects.size(), telegramState);
        }

        if (telegramState == TelegramState.OK) {
            dsmrEventListener.handleTelegramReceived(telegram);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Telegram received with error state '{}': {}", telegramState,
                        cosemObjects.stream().map(CosemObject::toString).collect(Collectors.joining(",")));
            }
        }
    }

    /**
     * @param lenientMode the lenientMode to set
     */
    public void setLenientMode(boolean lenientMode) {
        parser.setLenientMode(lenientMode);
    }
}
