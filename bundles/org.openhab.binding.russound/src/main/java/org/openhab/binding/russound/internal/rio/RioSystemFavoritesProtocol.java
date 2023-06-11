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
package org.openhab.binding.russound.internal.rio;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.net.SocketSessionListener;
import org.openhab.binding.russound.internal.rio.models.GsonUtilities;
import org.openhab.binding.russound.internal.rio.models.RioFavorite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * This {@link AbstractRioProtocol} implementation provides the implementation for managing Russound system favorites.
 * Since refreshing all 32 system favorites requires 64 calls to russound (for name/valid), we limit how often we can
 * refresh to {@link #UPDATE_TIME_SPAN}.
 *
 * @author Tim Roberts - Initial contribution
 */
public class RioSystemFavoritesProtocol extends AbstractRioProtocol {

    // logger
    private final Logger logger = LoggerFactory.getLogger(RioSystemFavoritesProtocol.class);

    // Helper names in the protocol
    private static final String FAV_NAME = "name";
    private static final String FAV_VALID = "valid";

    /**
     * The pattern representing system favorite notifications
     */
    private static final Pattern RSP_SYSTEMFAVORITENOTIFICATION = Pattern
            .compile("(?i)^[SN] System.favorite\\[(\\d+)\\].(\\w+)=\"(.*)\"$");

    /**
     * The current state of all 32 system favorites
     */
    private final RioFavorite[] systemFavorites = new RioFavorite[32];

    /**
     * The {@link Gson} used for all JSON operations
     */
    private final Gson gson;

    /**
     * The {@link ReentrantLock} used to control access to {@link #lastUpdateTime}
     */
    private final Lock lastUpdateLock = new ReentrantLock();

    /**
     * The last time we did a full refresh of system favorites via {@link #refreshSystemFavorites()}
     */
    private long lastUpdateTime;

    /**
     * The minimum timespan between full refreshes of system favorites (via {@link #refreshSystemFavorites()})
     */
    private static final long UPDATE_TIME_SPAN = 60000;

    /**
     * The list of listeners that will be called when system favorites have changed
     */
    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Constructs the system favorite protocol from the given session and callback. Note: the passed callback is not
     * currently used
     *
     * @param session a non null {@link SocketSession} to use
     * @param callback a non-null {@link RioHandlerCallback} to use
     */
    public RioSystemFavoritesProtocol(SocketSession session, RioHandlerCallback callback) {
        super(session, callback);

        gson = GsonUtilities.createGson();

        for (int x = 1; x <= 32; x++) {
            systemFavorites[x - 1] = new RioFavorite(x);
        }
    }

    /**
     * Adds the specified listener to changes in system favorites
     *
     * @param listener a non-null listener to add
     * @throws IllegalArgumentException if listener is null
     */
    public void addListener(Listener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        listeners.add(listener);
    }

    /**
     * Removes the specified listener from change notifications
     *
     * @param listener a possibly null listener to remove (null is ignored)
     * @return true if removed, false otherwise
     */
    public boolean removeListener(Listener listener) {
        return listeners.remove(listener);
    }

    /**
     * Fires the systemFavoritesUpdated method on all listeners with the results of {@link #getJson()}
     */
    private void fireUpdate() {
        final String json = getJson();
        for (Listener l : listeners) {
            l.systemFavoritesUpdated(json);
        }
    }

    /**
     * Helper method to request the specified system favorite id information (name/valid). Please note that this does
     * NOT change the {@link #lastUpdateTime}
     *
     * @param favIds a non-null, possibly empty list of system favorite ids to request (any id < 1 or > 32 will be
     *            ignored)
     * @throws IllegalArgumentException if favIds is null
     */
    private void requestSystemFavorites(List<Integer> favIds) {
        if (favIds == null) {
            throw new IllegalArgumentException("favIds cannot be null");
        }
        for (final Integer favId : favIds) {
            if (favId >= 1 && favId <= 32) {
                sendCommand("GET System.favorite[" + favId + "].name");
                sendCommand("GET System.favorite[" + favId + "].valid");
            }
        }
    }

    /**
     * Refreshes ALL system favorites if they have not been refreshed within the last
     * {@link #UPDATE_TIME_SPAN}. This method WILL change the {@link #lastUpdateTime}
     */
    public void refreshSystemFavorites() {
        lastUpdateLock.lock();
        try {
            final long now = System.currentTimeMillis();
            if (now > lastUpdateTime + UPDATE_TIME_SPAN) {
                lastUpdateTime = now;
                for (int x = 1; x <= 32; x++) {
                    sendCommand("GET System.favorite[" + x + "].valid");
                    sendCommand("GET System.favorite[" + x + "].name");
                }
            }
        } finally {
            lastUpdateLock.unlock();
        }
    }

    /**
     * Returns the JSON representation of all the system favorites and their state.
     *
     * @return A non-null, non-empty JSON representation of {@link #systemFavorites}
     */
    public String getJson() {
        final List<RioFavorite> favs = new ArrayList<>();
        for (final RioFavorite fav : systemFavorites) {
            if (fav.isValid()) {
                favs.add(fav);
            }
        }
        return gson.toJson(favs);
    }

    /**
     * Sets the system favorites for a controller/zone. For each system favorite found in the favJson parameter, this
     * method will either save the system favorite (if it's status changed from not valid to valid) or save the system
     * favorite name (if only the name changed) or delete the system favorite (if status changed from valid to invalid).
     *
     * @param controller the controller number between 1 and 6
     * @param zone the zone number between 1 and 8
     * @param favJson the possibly empty, possibly null JSON representation of system favorites
     * @throws IllegalArgumentException if controller is < 1 or > 6
     * @throws IllegalArgumentException if zone is < 1 or > 8
     */
    public void setSystemFavorites(int controller, int zone, @Nullable String favJson) {
        if (controller < 1 || controller > 6) {
            throw new IllegalArgumentException("Controller must be between 1 and 6");
        }

        if (zone < 1 || zone > 8) {
            throw new IllegalArgumentException("Zone must be between 1 and 8");
        }

        if (favJson == null || favJson.isEmpty()) {
            return;
        }

        final List<Integer> updateFavIds = new ArrayList<>();
        try {
            final RioFavorite[] favs;
            favs = gson.fromJson(favJson, RioFavorite[].class);
            for (int x = favs.length - 1; x >= 0; x--) {
                final RioFavorite fav = favs[x];
                if (fav == null) {
                    continue; // caused by {id,valid,name},,{id,valid,name}
                }

                final int favId = fav.getId();
                if (favId < 1 || favId > 32) {
                    logger.debug("Invalid favorite id (not between 1 and 32) - ignoring: {}:{}", favId, favJson);
                } else {
                    final RioFavorite myFav = systemFavorites[favId - 1];
                    final boolean favValid = fav.isValid();
                    final String favName = fav.getName();

                    // re-retrieve to see if the save/delete worked (saving on a zone that's off - valid won't be set to
                    // true)
                    if (myFav.isValid() != favValid) {
                        myFav.setValid(favValid);
                        if (favValid) {
                            myFav.setName(favName);
                            sendCommand("EVENT C[" + controller + "].Z[" + zone + "]!saveSystemFavorite \"" + favName
                                    + "\" " + favId);
                            updateFavIds.add(favId);
                        } else {
                            sendCommand("EVENT C[" + controller + "].Z[" + zone + "]!deleteSystemFavorite " + favId);
                        }
                    } else if (!Objects.equals(myFav.getName(), favName)) {
                        myFav.setName(favName);
                        sendCommand("SET System.favorite[" + favId + "]." + FAV_NAME + "=\"" + favName + "\"");
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Invalid JSON: {}", e.getMessage(), e);
        }

        // Refresh the favorites that changed (verifies if the favorite was actually saved)
        requestSystemFavorites(updateFavIds);

        // Refresh any listeners immediately to reset the channel
        fireUpdate();
    }

    /**
     * Handles any system notifications returned by the russound system
     *
     * @param m a non-null matcher
     * @param resp a possibly null, possibly empty response
     */
    private void handleSystemFavoriteNotification(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }
        if (m.groupCount() == 3) {
            try {
                final int favoriteId = Integer.parseInt(m.group(1));

                if (favoriteId >= 1 && favoriteId <= 32) {
                    final RioFavorite fav = systemFavorites[favoriteId - 1];

                    final String key = m.group(2).toLowerCase();
                    final String value = m.group(3);

                    switch (key) {
                        case FAV_NAME:
                            fav.setName(value);
                            fireUpdate();
                            break;
                        case FAV_VALID:
                            fav.setValid(!"false".equalsIgnoreCase(value));
                            fireUpdate();
                            break;

                        default:
                            logger.warn("Unknown system favorite notification: '{}'", resp);
                            break;
                    }
                } else {
                    logger.warn("Invalid System Favorite Notification (favorite < 1 or > 32): '{}')", resp);
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid System Favorite Notification (favorite not a parsable integer): '{}')", resp);
            }
        } else {
            logger.warn("Invalid System Notification response: '{}'", resp);
        }
    }

    /**
     * Implements {@link SocketSessionListener#responseReceived(String)} to try to process the response from the
     * russound system. This response may be for other protocol handler - so ignore if we don't recognize the response.
     *
     * @param a possibly null, possibly empty response
     */
    @Override
    public void responseReceived(@Nullable String response) {
        if (response == null || response.isEmpty()) {
            return;
        }

        final Matcher m = RSP_SYSTEMFAVORITENOTIFICATION.matcher(response);
        if (m.matches()) {
            handleSystemFavoriteNotification(m, response);
        }
    }

    /**
     * Defines the listener implementation to list for system favorite updates
     *
     * @author Tim Roberts
     *
     */
    public interface Listener {
        /**
         * Called when system favorites have changed. The jsonString will contain the current representation of all
         * valid system favorites.
         *
         * @param jsonString a non-null, non-empty json representation of {@link RioFavorite}
         */
        void systemFavoritesUpdated(String jsonString);
    }
}
