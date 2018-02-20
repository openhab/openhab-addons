/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio.controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.net.SocketSessionListener;
import org.openhab.binding.russound.internal.rio.AbstractRioProtocol;
import org.openhab.binding.russound.internal.rio.RioConstants;
import org.openhab.binding.russound.internal.rio.RioHandlerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the protocol handler for the Russound controller. This handler will issue the protocol commands and will
 * process the responses from the Russound system.
 *
 * @author Tim Roberts
 *
 */
class RioControllerProtocol extends AbstractRioProtocol {
    // logger
    private final Logger logger = LoggerFactory.getLogger(RioControllerProtocol.class);

    /**
     * The controller identifier
     */
    private final int controller;

    // Protocol constants
    private static final String CTL_TYPE = "type";
    private static final String CTL_IPADDRESS = "ipaddress";
    private static final String CTL_MACADDRESS = "macaddress";

    // Response patterns
    private static final Pattern RSP_CONTROLLERNOTIFICATION = Pattern
            .compile("(?i)^[SN] C\\[(\\d+)\\]\\.(\\w+)=\"(.*)\"$");

    /**
     * Constructs the protocol handler from given parameters
     *
     * @param controller the controller identifier
     * @param session a non-null {@link SocketSession} (may be connected or disconnected)
     * @param callback a non-null {@link RioHandlerCallback} to callback
     */
    RioControllerProtocol(int controller, SocketSession session, RioHandlerCallback callback) {
        super(session, callback);
        this.controller = controller;
    }

    /**
     * Helper method to issue post online commands
     */
    void postOnline() {
        refreshControllerType();
        refreshControllerIpAddress();
        refreshControllerMacAddress();
    }

    /**
     * Issues a get command for the controller given the keyname
     *
     * @param keyName a non-null, non-empty keyname to get
     * @throws IllegalArgumentException if name is null or an empty string
     */
    private void refreshControllerKey(String keyName) {
        if (keyName == null || keyName.trim().length() == 0) {
            throw new IllegalArgumentException("keyName cannot be null or empty");
        }
        sendCommand("GET C[" + controller + "]." + keyName);
    }

    /**
     * Refreshes the controller IP address
     */
    void refreshControllerIpAddress() {
        refreshControllerKey(CTL_IPADDRESS);
    }

    /**
     * Refreshes the controller MAC address
     */
    void refreshControllerMacAddress() {
        refreshControllerKey(CTL_MACADDRESS);
    }

    /**
     * Refreshes the controller Model Type
     */
    void refreshControllerType() {
        refreshControllerKey(CTL_TYPE);
    }

    /**
     * Handles any controller notifications returned by the russound system
     *
     * @param m a non-null matcher
     * @param resp a possibly null, possibly empty response
     */
    private void handleControllerNotification(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }
        if (m.groupCount() == 3) {
            try {
                final int notifyController = Integer.parseInt(m.group(1));
                if (notifyController != controller) {
                    return;
                }

                final String key = m.group(2).toLowerCase();
                final String value = m.group(3);

                switch (key) {
                    case CTL_TYPE:
                        setProperty(RioConstants.PROPERTY_CTLTYPE, value);
                        break;

                    case CTL_IPADDRESS:
                        setProperty(RioConstants.PROPERTY_CTLIPADDRESS, value);
                        break;

                    case CTL_MACADDRESS:
                        setProperty(RioConstants.PROPERTY_CTLMACADDRESS, value);
                        break;

                    default:
                        logger.debug("Unknown controller notification: '{}'", resp);
                        break;
                }
            } catch (NumberFormatException e) {
                logger.debug("Invalid Controller Notification (controller not a parsable integer): '{}')", resp);
            }
        } else {
            logger.debug("Invalid Controller Notification response: '{}'", resp);
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
        if (StringUtils.isEmpty(response)) {
            return;
        }

        final Matcher m = RSP_CONTROLLERNOTIFICATION.matcher(response);
        if (m.matches()) {
            handleControllerNotification(m, response);
            return;
        }
    }
}
