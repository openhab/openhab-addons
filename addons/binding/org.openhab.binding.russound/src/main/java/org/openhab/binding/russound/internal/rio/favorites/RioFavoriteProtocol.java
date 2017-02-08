/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio.favorites;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.net.SocketSessionListener;
import org.openhab.binding.russound.internal.rio.AbstractRioProtocol;
import org.openhab.binding.russound.internal.rio.RioConstants;
import org.openhab.binding.russound.internal.rio.RioHandlerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the protocol handler for the Russound Favorite. This handler will issue the protocol commands and will
 * process the responses from the Russound system. This handler operates at two levels: system level or zone level.
 *
 * @author Tim Roberts
 *
 */
class RioFavoriteProtocol extends AbstractRioProtocol {
    // Logger
    private Logger logger = LoggerFactory.getLogger(RioFavoriteProtocol.class);

    /**
     * The favorite identifier
     */
    private final int _favorite;

    /**
     * The zone identifier (will be -1 if operating at the system level).
     */
    private final int _zone;

    /**
     * The controller identifier (will be -1 if operating at the system level).
     */
    private final int _controller;

    /**
     * The name of the favorite - only is applied when a saveXXXFavorite event is sent
     */
    private String _name;

    // Protocol constants
    private final static String FAV_NAME = "name";
    private final static String FAV_VALID = "valid";

    // Response patterns
    private final Pattern RSP_SYSTEMNOTIFICATION = Pattern
            .compile("^[SN] System.favorite\\[(\\d+)\\].(\\w+)=\"(.*)\"$");
    private final Pattern RSP_ZONENOTIFICATION = Pattern
            .compile("^[SN] C\\[(\\d+)\\].Z\\[(\\d+)\\].favorite\\[(\\d+)\\].(\\w+)=\"(.*)\"$");

    /**
     * Constructs the protocol handler from given parameters
     *
     * @param favorite the favorite identifier
     * @param zone the zone identifier (or -1 if at the system level)
     * @param controller the controller identifier (or -1 if at the system level)
     * @param session a non-null {@link SocketSession} (may be connected or disconnected)
     * @param callback a non-null {@link RioHandlerCallback} to callback
     */
    RioFavoriteProtocol(int favorite, int zone, int controller, SocketSession session, RioHandlerCallback callback) {
        super(session, callback);
        _favorite = favorite;
        _zone = zone;
        _controller = controller;
        setName("Favorite " + favorite);
    }

    /**
     * Helper method to deterime if we are at the system or zone level
     *
     * @return true if system level, false if zone level
     */
    private boolean isSystemFavorite() {
        return _controller <= 0;
    }

    /**
     * Helper method to refresh a given key. System or zone is determined by {@link #isSystemFavorite()}
     *
     * @param keyName a non-null, non-empty keyname to get
     * @throws IllegalArgumentException if name is null or an empty string
     */
    private void refreshKey(String keyName) {
        refreshKey(keyName, isSystemFavorite());
    }

    /**
     * Helper method to refresh a given key and issues a system or zone command
     *
     * @param keyName a non-null, non-empty keyname to get
     * @param systemCommand true if a system command, false if zone
     * @throws IllegalArgumentException if name is null or an empty string
     */
    private void refreshKey(String keyName, boolean systemCommand) {
        if (keyName == null || keyName.trim().length() == 0) {
            throw new IllegalArgumentException("keyName cannot be null or empty");
        }

        if (systemCommand) {
            sendCommand("GET System.favorite[" + _favorite + "]." + keyName);
        } else {
            sendCommand("GET C[" + _controller + "].Z[" + _zone + "].favorite[" + _favorite + "]." + keyName);
        }

    }

    /**
     * Refresh the favorite name
     */
    void refreshName() {
        refreshKey(FAV_NAME);
    }

    /**
     * Refresh whether the favorite is valid or not
     */
    void refreshValid() {
        refreshKey(FAV_VALID);
    }

    /**
     * Sets the name of the favorite. Please note that the name will only be committed when the favorite is saved.
     *
     * @param name a non-null, non-empty name
     * @throws IllegalArgumentException if name is null or empty
     */
    void setName(String name) {
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("name cannot be null or empty");
        }

        _name = name;
        stateChanged(RioConstants.CHANNEL_FAVNAME, new StringType(name));
    }

    /**
     * Save the favorite as a system or zone favorite - this can only be done from a zone level. If called on a
     * system level, a debug warning will be issued and the call ignored. The name will be saved as well.
     *
     * @param system true if save to system favorite, false to save to zone favorite
     */
    void saveFavorite(boolean system) {
        if (isSystemFavorite()) {
            logger.warn("Trying to save a system favorite outside of a zone");
        } else {
            sendCommand("EVENT C[" + _controller + "].Z[" + _zone + "]!"
                    + (system ? "saveSystemFavorite" : "saveZoneFavorite") + " \"" + _name + "\" " + _favorite);

            refreshKey(FAV_NAME, true);
            refreshKey(FAV_VALID, true);
            if (!system) {
                refreshKey(FAV_NAME, false);
                refreshKey(FAV_VALID, false);
            }
        }
    }

    /**
     * Restore a system or zone favorite - this can only be done from a zone level. If called on a
     * system level, a debug warning will be issued and the call ignored.
     *
     * @param system true if restore a system favorite, false to restore a zone favorite
     */
    void restoreFavorite(boolean system) {
        if (isSystemFavorite()) {
            logger.warn("Trying to restore a system favorite outside of a zone");
        } else {
            sendCommand("EVENT C[" + _controller + "].Z[" + _zone + "]!"
                    + (system ? "restoreSystemFavorite" : "restoreZoneFavorite") + " " + _favorite);
        }
    }

    /**
     * Delete a system or zone favorite - this can only be done from a zone level. If called on a
     * system level, a debug warning will be issued and the call ignored.
     *
     * @param system true if delete a system favorite, false to delete a zone favorite
     */
    void deleteFavorite(boolean system) {
        if (isSystemFavorite()) {
            logger.warn("Trying to delete a system favorite outside of a zone");
        } else {
            sendCommand("EVENT C[" + _controller + "].Z[" + _zone + "]!"
                    + (system ? "deleteSystemFavorite" : "deleteZoneFavorite") + " " + _favorite);

            refreshKey(FAV_VALID, true);
            if (!system) {
                refreshKey(FAV_VALID, false);
            }
        }
    }

    /**
     * Handles any system level favorite notifications returned by the russound system
     *
     * @param m a non-null matcher
     * @param resp a possibly null, possibly empty response
     */
    private void handleSystemNotification(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }

        // System notification
        if (m.groupCount() == 3) {
            try {
                final int favorite = Integer.parseInt(m.group(1));
                if (favorite != _favorite) {
                    return;
                }

                final String key = m.group(2);
                final String value = m.group(3);
                switch (key) {
                    case FAV_NAME:
                        setName(value);
                        break;

                    case FAV_VALID:
                        stateChanged(RioConstants.CHANNEL_FAVVALID,
                                "false".equalsIgnoreCase(value) ? OnOffType.OFF : OnOffType.ON);
                        break;

                    default:
                        logger.warn("Unknown system favorite notification: '{}'", resp);
                        break;
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid System Favorite Notification (favorite not a parsable integer): '{}')", resp);
            }

        } else {
            logger.warn("Invalid System Favorite Notification: '{}')", resp);
        }
    }

    /**
     * Handles any zone level favorite notifications returned by the russound system
     *
     * @param m a non-null matcher
     * @param resp a possibly null, possibly empty response
     */
    private void handleZoneNotification(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }

        if (m.groupCount() == 5) {
            try {
                final int controller = Integer.parseInt(m.group(1));
                if (controller != _controller) {
                    return;
                }

                final int zone = Integer.parseInt(m.group(2));
                if (zone != _zone) {
                    return;
                }

                final int favorite = Integer.parseInt(m.group(3));
                if (favorite != _favorite) {
                    return;
                }

                final String key = m.group(4);
                final String value = m.group(5);

                switch (key) {
                    case FAV_NAME:
                        setName(value);
                        break;

                    case FAV_VALID:
                        stateChanged(RioConstants.CHANNEL_FAVVALID,
                                "false".equalsIgnoreCase(value) ? OnOffType.OFF : OnOffType.ON);
                        break;

                    default:
                        logger.warn("Unknown zone favorite notification: '{}'", resp);
                        break;
                }
            } catch (NumberFormatException e) {
                logger.warn(
                        "Invalid zone favorite Notification (controller/zone/favorite not a parsable integer): '{}')",
                        resp);
            }
        } else {
            logger.warn("Invalid Zone Favorite Notification: '{}')", resp);
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

        Matcher m = RSP_SYSTEMNOTIFICATION.matcher(response);
        if (m.matches()) {
            handleSystemNotification(m, response);
            return;
        }

        m = RSP_ZONENOTIFICATION.matcher(response);
        if (m.matches()) {
            handleZoneNotification(m, response);
            return;
        }
    }
}
