/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio.bank;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.net.SocketSessionListener;
import org.openhab.binding.russound.internal.rio.AbstractRioProtocol;
import org.openhab.binding.russound.internal.rio.RioConstants;
import org.openhab.binding.russound.internal.rio.RioHandlerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the protocol handler for the Russound Bank. This handler will issue the protocol commands and will
 * process the responses from the Russound system.
 *
 * @author Tim Roberts
 *
 */
class RioBankProtocol extends AbstractRioProtocol {
    // our logger
    private Logger logger = LoggerFactory.getLogger(RioBankProtocol.class);

    /**
     * The bank identifier for the handler
     */
    private final int _bank;

    /**
     * The source identifier for the handler
     */
    private final int _source;

    // Protocol constants
    private final static String BANK_NAME = "name";

    // Protocol notification patterns
    private final Pattern RSP_BANKNOTIFICATION = Pattern.compile("^[SN] S\\[(\\d+)\\].B\\[(\\d+)\\].(\\w+)=\"(.*)\"$");

    /**
     * Constructs the protocol handler from given parameters
     *
     * @param bank the bank identifier
     * @param source the source identifier
     * @param session a non-null {@link SocketSession} (may be connected or disconnected)
     * @param callback a non-null {@link RioHandlerCallback} to callback
     */
    RioBankProtocol(int bank, int source, SocketSession session, RioHandlerCallback callback) {
        super(session, callback);
        _bank = bank;
        _source = source;
    }

    /**
     * Request a refresh of the bank name
     */
    void refreshName() {
        sendCommand("GET S[" + _source + "].B[" + _bank + "].name");
    }

    /**
     * Sets the name of the bank
     *
     * @param name a non-null, non-empty bank name to set
     * @throws IllegalArgumentException if name is null or an empty string
     */
    void setName(String name) {
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }
        sendCommand("SET S[" + _source + "].B[" + _bank + "].name = \"" + name + "\"");
    }

    /**
     * Handles any bank notifications returned by the russound system
     *
     * @param m a non-null matcher
     * @param resp a possibly null, possibly empty response
     */
    private void handleBankNotification(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }

        // System notification
        if (m.groupCount() == 4) {
            try {
                final int bank = Integer.parseInt(m.group(1));
                if (bank != _bank) {
                    return;
                }

                final int source = Integer.parseInt(m.group(2));
                if (source != _source) {
                    return;
                }

                final String key = m.group(3);
                final String value = m.group(4);

                switch (key) {
                    case BANK_NAME:
                        stateChanged(RioConstants.CHANNEL_BANKNAME, new StringType(value));
                        break;

                    default:
                        logger.warn("Unknown bank name notification: '{}'", resp);
                        break;
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid Bank Name Notification (bank/source not a parsable integer): '{}')", resp);
            }

        } else {
            logger.warn("Invalid Bank Notification: '{}')", resp);
        }
    }

    /**
     * Implements {@link SocketSessionListener#responseReceived(String)} to try to process the response from the
     * russound system. This response may be for other protocol handler - so ignore if we don't recognize the response.
     *
     * @param a possibly null, possibly empty response
     */
    @Override
    public void responseReceived(String response) {
        if (response == null || response == "") {
            return;
        }

        final Matcher m = RSP_BANKNOTIFICATION.matcher(response);
        if (m.matches()) {
            handleBankNotification(m, response);
            return;
        }
    }
}
