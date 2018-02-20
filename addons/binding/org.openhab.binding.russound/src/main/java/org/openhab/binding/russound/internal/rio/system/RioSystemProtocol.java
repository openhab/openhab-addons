/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio.system;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.net.SocketSessionListener;
import org.openhab.binding.russound.internal.rio.AbstractRioProtocol;
import org.openhab.binding.russound.internal.rio.RioConstants;
import org.openhab.binding.russound.internal.rio.RioHandlerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the protocol handler for the Russound System. This handler will issue the protocol commands and will
 * process the responses from the Russound system.
 *
 * @author Tim Roberts
 *
 */
class RioSystemProtocol extends AbstractRioProtocol {
    // Logger
    private final Logger logger = LoggerFactory.getLogger(RioSystemProtocol.class);

    // Protocol Constants
    private static final String SYS_VERSION = "version"; // 12 max
    private static final String SYS_STATUS = "status"; // 12 max
    private static final String SYS_LANG = "language"; // 12 max

    // Response patterns
    private static final Pattern RSP_VERSION = Pattern.compile("(?i)^S VERSION=\"(.+)\"$");
    private static final Pattern RSP_FAILURE = Pattern.compile("(?i)^E (.*)");
    private static final Pattern RSP_SYSTEMNOTIFICATION = Pattern.compile("(?i)^[SN] System\\.(\\w+)=\"(.*)\"$");

    // all on state (there is no corresponding value)
    private final AtomicBoolean allOn = new AtomicBoolean(false);

    /**
     * This represents our ping command. There is no ping command in the protocol so we simply send an empty command to
     * keep things alive (and not generate any errors)
     */
    private static final String CMD_PING = "";

    /**
     * Constructs the protocol handler from given parameters
     *
     * @param session a non-null {@link SocketSession} (may be connected or disconnected)
     * @param callback a non-null {@link RioHandlerCallback} to callback
     */
    RioSystemProtocol(SocketSession session, RioHandlerCallback callback) {
        super(session, callback);
    }

    /**
     * Attempts to log into the system. The russound system requires no login, so we immediately execute any
     * {@link #postLogin()} commands.
     *
     * @return always null to indicate a successful login
     */
    String login() {
        postLogin();
        return null;
    }

    /**
     * Post successful login stuff - mark us online, start watching the system and refresh some attributes
     */
    private void postLogin() {
        logger.info("Russound System now connected");
        statusChanged(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
        watchSystem(true);

        refreshSystemStatus();
        refreshVersion();
        refreshSystemLanguage();
    }

    /**
     * Pings the server with out ping command to keep the connection alive
     */
    void ping() {
        sendCommand(CMD_PING);
    }

    /**
     * Refreshes the firmware version of the system
     */
    void refreshVersion() {
        sendCommand(SYS_VERSION);
    }

    /**
     * Helper method to refresh a system keyname
     *
     * @param keyName a non-null, non-empty keyname
     * @throws IllegalArgumentException if keyname is null or empty
     */
    private void refreshSystemKey(String keyName) {
        if (keyName == null || keyName.trim().length() == 0) {
            throw new IllegalArgumentException("keyName cannot be null or empty");
        }

        sendCommand("GET System." + keyName);
    }

    /**
     * Refresh the system status
     */
    void refreshSystemAllOn() {
        stateChanged(RioConstants.CHANNEL_SYSALLON, allOn.get() ? OnOffType.ON : OnOffType.OFF);
    }

    /**
     * Refresh the system language
     */
    void refreshSystemLanguage() {
        refreshSystemKey(SYS_LANG);
    }

    /**
     * Refresh the system status
     */
    void refreshSystemStatus() {
        refreshSystemKey(SYS_STATUS);
    }

    /**
     * Turns on/off watching for system notifications
     *
     * @param on true to turn on, false to turn off
     */
    void watchSystem(boolean on) {
        sendCommand("WATCH SYSTEM " + (on ? "ON" : "OFF"));
    }

    /**
     * Sets all zones on
     *
     * @param on true to turn all zones on, false otherwise
     */
    void setSystemAllOn(boolean on) {
        sendCommand("EVENT C[1].Z[1]!All" + (on ? "On" : "Off"));
        allOn.set(on);
        refreshSystemAllOn();
    }

    /**
     * Sets the system language (currently can only be english, chinese or russian). Case does not matter - will be
     * converted to uppercase for the system.
     *
     * @param language a non-null, non-empty language to set
     * @throws IllegalArgumentException if language is null, empty or not (english, chinese or russian).
     */
    void setSystemLanguage(String language) {
        if (language == null || language.trim().length() == 0) {
            throw new IllegalArgumentException("Language cannot be null or an empty string");
        }

        if ("|ENGLISH|CHINESE|RUSSIAN|".indexOf("|" + language + "|") == -1) {
            throw new IllegalArgumentException("Language can only be ENGLISH, CHINESE or RUSSIAN: " + language);
        }
        sendCommand("SET System." + SYS_LANG + " " + language.toUpperCase());
    }

    /**
     * Handles the version notification
     *
     * @param m a non-null matcher
     * @param resp a possibly null, possibly empty response
     */
    void handleVersionNotification(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }
        if (m.groupCount() == 1) {
            final String version = m.group(1);
            setProperty(RioConstants.PROPERTY_SYSVERSION, version);
        } else {
            logger.warn("Invalid System Notification response: '{}'", resp);
        }

    }

    /**
     * Handles any system notifications returned by the russound system
     *
     * @param m a non-null matcher
     * @param resp a possibly null, possibly empty response
     */
    void handleSystemNotification(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }
        if (m.groupCount() == 2) {
            final String key = m.group(1).toLowerCase();
            final String value = m.group(2);

            switch (key) {
                case SYS_LANG:
                    stateChanged(RioConstants.CHANNEL_SYSLANG, new StringType(value));
                    break;
                case SYS_STATUS:
                    stateChanged(RioConstants.CHANNEL_SYSSTATUS, "ON".equals(value) ? OnOffType.ON : OnOffType.OFF);
                    break;

                default:
                    logger.warn("Unknown system notification: '{}'", resp);
                    break;
            }
        } else {
            logger.warn("Invalid System Notification response: '{}'", resp);
        }

    }

    /**
     * Handles any error notifications returned by the russound system
     *
     * @param m a non-null matcher
     * @param resp a possibly null, possibly empty response
     */
    private void handleFailureNotification(Matcher m, String resp) {
        logger.debug("Error notification: {}", resp);
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

        Matcher m = RSP_VERSION.matcher(response);
        if (m.matches()) {
            handleVersionNotification(m, response);
            return;
        }

        m = RSP_SYSTEMNOTIFICATION.matcher(response);
        if (m.matches()) {
            handleSystemNotification(m, response);
            return;
        }

        m = RSP_FAILURE.matcher(response);
        if (m.matches()) {
            handleFailureNotification(m, response);
            return;
        }

    }

    /**
     * Overrides the default implementation to turn watch off ({@link #watchSystem(boolean)}) before calling the dispose
     */
    @Override
    public void dispose() {
        watchSystem(false);
        super.dispose();
    }
}
