/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio.zone;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.net.SocketSessionListener;
import org.openhab.binding.russound.internal.rio.AbstractRioProtocol;
import org.openhab.binding.russound.internal.rio.RioConstants;
import org.openhab.binding.russound.internal.rio.RioHandlerCallback;
import org.openhab.binding.russound.internal.rio.RioPresetsProtocol;
import org.openhab.binding.russound.internal.rio.RioSystemFavoritesProtocol;
import org.openhab.binding.russound.internal.rio.models.GsonUtilities;
import org.openhab.binding.russound.internal.rio.models.RioFavorite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * This is the protocol handler for the Russound Zone. This handler will issue the protocol commands and will
 * process the responses from the Russound system.
 *
 * @author Tim Roberts
 *
 */
class RioZoneProtocol extends AbstractRioProtocol
        implements RioSystemFavoritesProtocol.Listener, RioPresetsProtocol.Listener {
    // logger
    private final Logger logger = LoggerFactory.getLogger(RioZoneProtocol.class);

    /**
     * The controller identifier
     */
    private final int controller;

    /**
     * The zone identifier
     */
    private final int zone;

    // Zone constants
    private static final String ZONE_NAME = "name"; // 12 max
    private static final String ZONE_SOURCE = "currentsource"; // 1-8 or 1-12
    private static final String ZONE_BASS = "bass"; // -10 to 10
    private static final String ZONE_TREBLE = "treble"; // -10 to 10
    private static final String ZONE_BALANCE = "balance"; // -10 to 10
    private static final String ZONE_LOUDNESS = "loudness"; // OFF/ON
    private static final String ZONE_TURNONVOLUME = "turnonvolume"; // 0 to 50
    private static final String ZONE_DONOTDISTURB = "donotdisturb"; // OFF/ON/SLAVE
    private static final String ZONE_PARTYMODE = "partymode"; // OFF/ON/MASTER
    private static final String ZONE_STATUS = "status"; // OFF/ON/MASTER
    private static final String ZONE_VOLUME = "volume"; // 0 to 50
    private static final String ZONE_MUTE = "mute"; // OFF/ON/MASTER
    private static final String ZONE_PAGE = "page"; // OFF/ON/MASTER
    private static final String ZONE_SHAREDSOURCE = "sharedsource"; // OFF/ON/MASTER
    private static final String ZONE_SLEEPTIMEREMAINING = "sleeptimeremaining"; // OFF/ON/MASTER
    private static final String ZONE_LASTERROR = "lasterror"; // OFF/ON/MASTER
    private static final String ZONE_ENABLED = "enabled"; // OFF/ON

    // Multimedia functions
    private static final String ZONE_MMInit = "MMInit"; // button
    private static final String ZONE_MMContextMenu = "MMContextMenu"; // button

    // Favorites
    private static final String FAV_NAME = "name";
    private static final String FAV_VALID = "valid";

    // Respone patterns
    private static final Pattern RSP_ZONENOTIFICATION = Pattern
            .compile("(?i)^[SN] C\\[(\\d+)\\]\\.Z\\[(\\d+)\\]\\.(\\w+)=\"(.*)\"$");

    private static final Pattern RSP_ZONEFAVORITENOTIFICATION = Pattern
            .compile("(?i)^[SN] C\\[(\\d+)\\].Z\\[(\\d+)\\].favorite\\[(\\d+)\\].(\\w+)=\"(.*)\"$");

    // The zone favorites
    private final RioFavorite[] zoneFavorites = new RioFavorite[2];

    // The current source identifier (or -1 if none)
    private final AtomicInteger sourceId = new AtomicInteger(-1);

    // GSON object used for json
    private final Gson gson;

    // The favorites protocol
    private final RioSystemFavoritesProtocol favoritesProtocol;

    // The presets protocol
    private final RioPresetsProtocol presetsProtocol;

    /**
     * Constructs the protocol handler from given parameters
     *
     * @param zone the zone identifier
     * @param controller the controller identifier
     * @param favoritesProtocol a non-null {@link RioSystemFavoritesProtocol}
     * @param presetsProtocol a non-null {@link RioPresetsProtocol}
     * @param session a non-null {@link SocketSession} (may be connected or disconnected)
     * @param callback a non-null {@link RioHandlerCallback} to callback
     */
    RioZoneProtocol(int zone, int controller, RioSystemFavoritesProtocol favoritesProtocol,
            RioPresetsProtocol presetsProtocol, SocketSession session, RioHandlerCallback callback) {
        super(session, callback);

        if (controller < 1 || controller > 6) {
            throw new IllegalArgumentException("Controller must be between 1-6: " + controller);
        }
        if (zone < 1 || zone > 8) {
            throw new IllegalArgumentException("Zone must be between 1-6: " + zone);
        }

        this.controller = controller;
        this.zone = zone;

        this.favoritesProtocol = favoritesProtocol;
        this.favoritesProtocol.addListener(this);

        this.presetsProtocol = presetsProtocol;
        this.presetsProtocol.addListener(this);

        this.gson = GsonUtilities.createGson();

        this.zoneFavorites[0] = new RioFavorite(1);
        this.zoneFavorites[1] = new RioFavorite(2);

    }

    /**
     * Helper method to issue post online commands
     */
    void postOnline() {
        watchZone(true);
        refreshZoneSource();
        refreshZoneEnabled();
        refreshZoneName();

        systemFavoritesUpdated(favoritesProtocol.getJson());
    }

    /**
     * Helper method to refresh a system keyname
     *
     * @param keyname a non-null, non-empty keyname
     * @throws IllegalArgumentException if keyname is null or empty
     */
    private void refreshZoneKey(String keyname) {
        if (keyname == null || keyname.trim().length() == 0) {
            throw new IllegalArgumentException("keyName cannot be null or empty");
        }

        sendCommand("GET C[" + controller + "].Z[" + zone + "]." + keyname);
    }

    /**
     * Refresh a zone name
     */
    void refreshZoneName() {
        refreshZoneKey(ZONE_NAME);
    }

    /**
     * Refresh the zone's source
     */
    void refreshZoneSource() {
        refreshZoneKey(ZONE_SOURCE);
    }

    /**
     * Refresh the zone's bass setting
     */
    void refreshZoneBass() {
        refreshZoneKey(ZONE_BASS);
    }

    /**
     * Refresh the zone's treble setting
     */
    void refreshZoneTreble() {
        refreshZoneKey(ZONE_TREBLE);
    }

    /**
     * Refresh the zone's balance setting
     */
    void refreshZoneBalance() {
        refreshZoneKey(ZONE_BALANCE);
    }

    /**
     * Refresh the zone's loudness setting
     */
    void refreshZoneLoudness() {
        refreshZoneKey(ZONE_LOUDNESS);
    }

    /**
     * Refresh the zone's turn on volume setting
     */
    void refreshZoneTurnOnVolume() {
        refreshZoneKey(ZONE_TURNONVOLUME);
    }

    /**
     * Refresh the zone's do not disturb setting
     */
    void refreshZoneDoNotDisturb() {
        refreshZoneKey(ZONE_DONOTDISTURB);
    }

    /**
     * Refresh the zone's party mode setting
     */
    void refreshZonePartyMode() {
        refreshZoneKey(ZONE_PARTYMODE);
    }

    /**
     * Refresh the zone's status
     */
    void refreshZoneStatus() {
        refreshZoneKey(ZONE_STATUS);
    }

    /**
     * Refresh the zone's volume setting
     */
    void refreshZoneVolume() {
        refreshZoneKey(ZONE_VOLUME);
    }

    /**
     * Refresh the zone's mute setting
     */
    void refreshZoneMute() {
        refreshZoneKey(ZONE_MUTE);
    }

    /**
     * Refresh the zone's paging setting
     */
    void refreshZonePage() {
        refreshZoneKey(ZONE_PAGE);
    }

    /**
     * Refresh the zone's shared source setting
     */
    void refreshZoneSharedSource() {
        refreshZoneKey(ZONE_SHAREDSOURCE);
    }

    /**
     * Refresh the zone's sleep time remaining setting
     */
    void refreshZoneSleepTimeRemaining() {
        refreshZoneKey(ZONE_SLEEPTIMEREMAINING);
    }

    /**
     * Refresh the zone's last error
     */
    void refreshZoneLastError() {
        refreshZoneKey(ZONE_LASTERROR);
    }

    /**
     * Refresh the zone's enabled setting
     */
    void refreshZoneEnabled() {
        refreshZoneKey(ZONE_ENABLED);
    }

    /**
     * Refreshes the system favorites via {@link #favoritesProtocol}
     */
    void refreshSystemFavorites() {
        favoritesProtocol.refreshSystemFavorites();
    }

    /**
     * Refreshes the zone favorites
     */
    void refreshZoneFavorites() {
        for (int x = 1; x <= 2; x++) {
            sendCommand("GET C[" + controller + "].Z[" + zone + "].favorite[" + x + "].valid");
            sendCommand("GET C[" + controller + "].Z[" + zone + "].favorite[" + x + "].name");
        }
    }

    /**
     * Refresh the zone preset via {@link #presetsProtocol}
     */
    void refreshZonePresets() {
        presetsProtocol.refreshPresets();
    }

    /**
     * Turns on/off watching for zone notifications
     *
     * @param on true to turn on, false to turn off
     */
    void watchZone(boolean watch) {
        sendCommand("WATCH C[" + controller + "].Z[" + zone + "] " + (watch ? "ON" : "OFF"));
    }

    /**
     * Set's the zone bass setting (from -10 to 10)
     *
     * @param bass the bass setting from -10 to 10
     * @throws IllegalArgumentException if bass < -10 or > 10
     */
    void setZoneBass(int bass) {
        if (bass < -10 || bass > 10) {
            throw new IllegalArgumentException("Bass must be between -10 and 10: " + bass);
        }
        sendCommand("SET C[" + controller + "].Z[" + zone + "]." + ZONE_BASS + "=\"" + bass + "\"");
    }

    /**
     * Set's the zone treble setting (from -10 to 10)
     *
     * @param treble the treble setting from -10 to 10
     * @throws IllegalArgumentException if treble < -10 or > 10
     */
    void setZoneTreble(int treble) {
        if (treble < -10 || treble > 10) {
            throw new IllegalArgumentException("Treble must be between -10 and 10: " + treble);
        }
        sendCommand("SET C[" + controller + "].Z[" + zone + "]." + ZONE_TREBLE + "=\"" + treble + "\"");
    }

    /**
     * Set's the zone balance setting (from -10 [full left] to 10 [full right])
     *
     * @param balance the balance setting from -10 to 10
     * @throws IllegalArgumentException if balance < -10 or > 10
     */
    void setZoneBalance(int balance) {
        if (balance < -10 || balance > 10) {
            throw new IllegalArgumentException("Balance must be between -10 and 10: " + balance);
        }
        sendCommand("SET C[" + controller + "].Z[" + zone + "]." + ZONE_BALANCE + "=\"" + balance + "\"");
    }

    /**
     * Set's the zone's loudness
     *
     * @param on true to turn on loudness, false to turn off
     */
    void setZoneLoudness(boolean on) {
        sendCommand("SET C[" + controller + "].Z[" + zone + "]." + ZONE_LOUDNESS + "=\"" + (on ? "ON" : "OFF") + "\"");
    }

    /**
     * Set's the zone turn on volume (will be scaled between 0 and 50)
     *
     * @param volume the turn on volume (between 0 and 1)
     * @throws IllegalArgumentException if volume < 0 or > 1
     */
    void setZoneTurnOnVolume(double volume) {
        if (volume < 0 || volume > 1) {
            throw new IllegalArgumentException("Volume must be between 0 and 1: " + volume);
        }

        final int scaledVolume = (int) ((volume * 100) / 2);
        sendCommand("SET C[" + controller + "].Z[" + zone + "]." + ZONE_TURNONVOLUME + "=\"" + scaledVolume + "\"");
    }

    /**
     * Set's the zone sleep time remaining in seconds (from 0 to 60). Will be rounded to nearest 5 (37 will become 35,
     * 38 will become 40).
     *
     * @param sleepTime the sleeptime in seconds
     * @throws IllegalArgumentException if sleepTime < 0 or > 60
     */
    void setZoneSleepTimeRemaining(int sleepTime) {
        if (sleepTime < 0 || sleepTime > 60) {
            throw new IllegalArgumentException("Sleep Time Remaining must be between 0 and 60: " + sleepTime);
        }
        sleepTime = (int) (5 * Math.round(sleepTime / 5.0));
        sendCommand("SET C[" + controller + "].Z[" + zone + "]." + ZONE_SLEEPTIMEREMAINING + "=\"" + sleepTime + "\"");
    }

    /**
     * Set's the zone source (physical source from 1 to 12)
     *
     * @param source the source (1 to 12)
     * @throws IllegalArgumentException if source is < 1 or > 12
     */
    void setZoneSource(int source) {
        if (source < 1 || source > 12) {
            throw new IllegalArgumentException("Source must be between 1 and 12");
        }
        sendCommand("EVENT C[" + controller + "].Z[" + zone + "]!SelectSource " + source);
    }

    /**
     * Set's the zone's status
     *
     * @param on true to turn on, false otherwise
     */
    void setZoneStatus(boolean on) {
        sendCommand("EVENT C[" + controller + "].Z[" + zone + "]!Zone" + (on ? "On" : "Off"));
    }

    /**
     * Set's the zone's partymode (supports on/off/master). Case does not matter - will be
     * converted to uppercase for the system.
     *
     * @param partyMode a non-null, non-empty party mode
     * @throws IllegalArgumentException if partymode is null, empty or not (on/off/master).
     */
    void setZonePartyMode(String partyMode) {
        if (partyMode == null || partyMode.trim().length() == 0) {
            throw new IllegalArgumentException("PartyMode cannot be null or empty");
        }
        if ("|on|off|master|".indexOf("|" + partyMode + "|") == -1) {
            throw new IllegalArgumentException(
                    "Party mode can only be set to on, off or master: " + partyMode.toUpperCase());
        }
        sendCommand("EVENT C[" + controller + "].Z[" + zone + "]!PartyMode " + partyMode);
    }

    /**
     * Set's the zone's do not disturb (supports on/off/slave). Case does not matter - will be
     * converted to uppercase for the system. Please note that slave will be translated to "ON" but may be refreshed
     * back to "SLAVE" if a master zone has been designated
     *
     * @param doNotDisturb a non-null, non-empty do not disturb mode
     * @throws IllegalArgumentException if doNotDisturb is null, empty or not (on/off/slave).
     */
    void setZoneDoNotDisturb(String doNotDisturb) {
        if (doNotDisturb == null || doNotDisturb.trim().length() == 0) {
            throw new IllegalArgumentException("Do Not Disturb cannot be null or empty");
        }
        if ("|on|off|slave|".indexOf("|" + doNotDisturb + "|") == -1) {
            throw new IllegalArgumentException("Do Not Disturb can only be set to on, off or slave: " + doNotDisturb);
        }
        sendCommand("EVENT C[" + controller + "].Z[" + zone + "]!DoNotDisturb "
                + ("off".equals(doNotDisturb) ? "OFF" : "ON")); // translate "slave" to "on"
    }

    /**
     * Sets the zone's volume level (scaled to 0-50)
     *
     * @param volume the volume level
     * @throws IllegalArgumentException if volume is < 0 or > 1
     */
    void setZoneVolume(double volume) {
        if (volume < 0 || volume > 1) {
            throw new IllegalArgumentException("Volume must be between 0 and 1");
        }

        final int scaledVolume = (int) ((volume * 100) / 2);
        sendKeyPress("Volume " + scaledVolume);
    }

    /**
     * Sets the volume up or down by 1
     *
     * @param increase true to increase by 1, false to decrease
     */
    void setZoneVolume(boolean increase) {
        sendKeyPress("Volume" + (increase ? "Up" : "Down"));
    }

    /**
     * Toggles the zone's mute
     */
    void toggleZoneMute() {
        sendKeyRelease("Mute");
    }

    /**
     * Toggles the zone's shuffle if the source supports shuffle mode
     */
    void toggleZoneShuffle() {
        sendCommand("EVENT C[" + controller + "].Z[" + zone + "]!Shuffle");
    }

    /**
     * Toggles the zone's repeat if the source supports repeat mod
     */
    void toggleZoneRepeat() {
        sendCommand("EVENT C[" + controller + "].Z[" + zone + "]!Repeat");
    }

    /**
     * Assign a rating to the current song if the source supports a rating
     *
     * @param like true to like, false to dislike
     */
    void setZoneRating(boolean like) {
        sendCommand("EVENT C[" + controller + "].Z[" + zone + "]!MMRate " + (like ? "hi" : "low"));
    }

    /**
     * Sets the system favorite based on what is currently being played in the zone via {@link #favoritesProtocol}
     *
     * @param favJson a possibly null, possibly empty JSON of favorites to set
     */
    void setSystemFavorites(String favJson) {
        favoritesProtocol.setSystemFavorites(controller, zone, favJson);
    }

    /**
     * Sets the zone favorites to what is currently playing
     *
     * @param favJson a possibly null, possibly empty json for favorites to set
     * @return a non-null {@link Runnable} that should be run after the call
     */
    Runnable setZoneFavorites(String favJson) {
        if (StringUtils.isEmpty(favJson)) {
            return new Runnable() {
                @Override
                public void run() {
                }
            };
        }

        final List<Integer> updateFavIds = new ArrayList<Integer>();
        try {
            final RioFavorite[] favs = gson.fromJson(favJson, RioFavorite[].class);
            for (int x = favs.length - 1; x >= 0; x--) {
                final RioFavorite fav = favs[x];
                if (fav == null) {
                    continue;// caused by {id,valid,name},,{id,valid,name}
                }
                final int favId = fav.getId();
                if (favId < 1 || favId > 2) {
                    logger.debug("Invalid favorite id (not between 1 and 2) - ignoring: {}:{}", favId, favJson);
                } else {
                    final RioFavorite myFav = zoneFavorites[favId - 1];
                    final boolean favValid = fav.isValid();
                    final String favName = fav.getName();

                    if (!StringUtils.equals(myFav.getName(), favName) || myFav.isValid() != favValid) {
                        myFav.setName(favName);
                        myFav.setValid(favValid);
                        if (favValid) {
                            sendEvent("saveZoneFavorite \"" + favName + "\" " + favId);
                            updateFavIds.add(favId);
                        } else {
                            sendEvent("deleteZoneFavorite " + favId);
                        }
                    }
                }
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Invalid JSON: {}", e.getMessage(), e);
        }
        // regardless of what happens above - reupdate the channel
        // (to remove anything bad from it)
        return new Runnable() {
            @Override
            public void run() {
                for (Integer favId : updateFavIds) {
                    sendCommand("GET C[" + controller + "].Z[" + zone + "].favorite[" + favId + "].valid");
                    sendCommand("GET C[" + controller + "].Z[" + zone + "].favorite[" + favId + "].name");
                }
                updateZoneFavoritesChannel();
            }
        };
    }

    /**
     * Sets the zone presets for what is currently playing via {@link #presetsProtocol}
     *
     * @param presetJson a possibly empty, possibly null preset json
     */
    void setZonePresets(String presetJson) {
        presetsProtocol.setZonePresets(controller, zone, sourceId.get(), presetJson);
    }

    /**
     * Sends a KeyPress instruction to the zone
     *
     * @param keyPress a non-null, non-empty string to send
     * @throws IllegalArgumentException if keyPress is null or empty
     */
    void sendKeyPress(String keyPress) {
        if (keyPress == null || keyPress.trim().length() == 0) {
            throw new IllegalArgumentException("keyPress cannot be null or empty");
        }
        sendCommand("EVENT C[" + controller + "].Z[" + zone + "]!KeyPress " + keyPress);
    }

    /**
     * Sends a KeyRelease instruction to the zone
     *
     * @param keyRelease a non-null, non-empty string to send
     * @throws IllegalArgumentException if keyRelease is null or empty
     */
    void sendKeyRelease(String keyRelease) {
        if (keyRelease == null || keyRelease.trim().length() == 0) {
            throw new IllegalArgumentException("keyRelease cannot be null or empty");
        }
        sendCommand("EVENT C[" + controller + "].Z[" + zone + "]!KeyRelease " + keyRelease);
    }

    /**
     * Sends a KeyHold instruction to the zone
     *
     * @param keyHold a non-null, non-empty string to send
     * @throws IllegalArgumentException if keyHold is null or empty
     */
    void sendKeyHold(String keyHold) {
        if (keyHold == null || keyHold.trim().length() == 0) {
            throw new IllegalArgumentException("keyHold cannot be null or empty");
        }
        sendCommand("EVENT C[" + controller + "].Z[" + zone + "]!KeyHold " + keyHold);
    }

    /**
     * Sends a KeyCode instruction to the zone
     *
     * @param keyCode a non-null, non-empty string to send
     * @throws IllegalArgumentException if keyCode is null or empty
     */
    void sendKeyCode(String keyCode) {
        if (keyCode == null || keyCode.trim().length() == 0) {
            throw new IllegalArgumentException("keyCode cannot be null or empty");
        }
        sendCommand("EVENT C[" + controller + "].Z[" + zone + "]!KeyCode " + keyCode);
    }

    /**
     * Sends a EVENT instruction to the zone
     *
     * @param event a non-null, non-empty string to send
     * @throws IllegalArgumentException if event is null or empty
     */
    void sendEvent(String event) {
        if (event == null || event.trim().length() == 0) {
            throw new IllegalArgumentException("event cannot be null or empty");
        }
        sendCommand("EVENT C[" + controller + "].Z[" + zone + "]!" + event);
    }

    /**
     * Sends the MMInit [home screen] command
     */
    void sendMMInit() {
        sendEvent("MMVerbosity 2");
        sendEvent("MMIndex ABSOLUTE");
        sendEvent("MMFormat JSON");
        sendEvent("MMUseBlockInfo TRUE");
        sendEvent("MMUseForms FALSE");
        sendEvent("MMMaxItems 25");

        sendEvent(ZONE_MMInit);
    }

    /**
     * Requests a context menu
     */
    void sendMMContextMenu() {
        sendEvent("MMVerbosity 2");
        sendEvent("MMIndex ABSOLUTE");
        sendEvent("MMFormat JSON");
        sendEvent("MMUseBlockInfo TRUE");
        sendEvent("MMUseForms FALSE");
        sendEvent("MMMaxItems 25");

        sendEvent(ZONE_MMContextMenu);
    }

    /**
     * Handles any zone notifications returned by the russound system
     *
     * @param m a non-null matcher
     * @param resp a possibly null, possibly empty response
     */
    private void handleZoneNotification(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }
        if (m.groupCount() == 4) {
            try {
                final int notifyController = Integer.parseInt(m.group(1));
                if (notifyController != controller) {
                    return;
                }
                final int notifyZone = Integer.parseInt(m.group(2));
                if (notifyZone != zone) {
                    return;
                }
                final String key = m.group(3).toLowerCase();
                final String value = m.group(4);

                switch (key) {
                    case ZONE_NAME:
                        stateChanged(RioConstants.CHANNEL_ZONENAME, new StringType(value));
                        break;

                    case ZONE_SOURCE:
                        try {
                            final int nbr = Integer.parseInt(value);
                            stateChanged(RioConstants.CHANNEL_ZONESOURCE, new DecimalType(nbr));

                            if (nbr != sourceId.getAndSet(nbr)) {
                                sourceId.set(nbr);
                                presetsUpdated(nbr, presetsProtocol.getJson(nbr));
                            }
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid zone notification (source not parsable): '{}')", resp);
                        }
                        break;

                    case ZONE_BASS:
                        try {
                            final int nbr = Integer.parseInt(value);
                            stateChanged(RioConstants.CHANNEL_ZONEBASS, new DecimalType(nbr));
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid zone notification (bass not parsable): '{}')", resp);
                        }
                        break;

                    case ZONE_TREBLE:
                        try {
                            final int nbr = Integer.parseInt(value);
                            stateChanged(RioConstants.CHANNEL_ZONETREBLE, new DecimalType(nbr));
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid zone notification (treble not parsable): '{}')", resp);
                        }
                        break;

                    case ZONE_BALANCE:
                        try {
                            final int nbr = Integer.parseInt(value);
                            stateChanged(RioConstants.CHANNEL_ZONEBALANCE, new DecimalType(nbr));
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid zone notification (balance not parsable): '{}')", resp);
                        }
                        break;

                    case ZONE_LOUDNESS:
                        stateChanged(RioConstants.CHANNEL_ZONELOUDNESS,
                                "ON".equals(value) ? OnOffType.ON : OnOffType.OFF);
                        break;

                    case ZONE_TURNONVOLUME:
                        try {
                            final int nbr = Integer.parseInt(value);
                            stateChanged(RioConstants.CHANNEL_ZONETURNONVOLUME, new PercentType(nbr * 2));
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid zone notification (turnonvolume not parsable): '{}')", resp);
                        }
                        break;

                    case ZONE_DONOTDISTURB:
                        stateChanged(RioConstants.CHANNEL_ZONEDONOTDISTURB, new StringType(value));
                        break;

                    case ZONE_PARTYMODE:
                        stateChanged(RioConstants.CHANNEL_ZONEPARTYMODE, new StringType(value));
                        break;

                    case ZONE_STATUS:
                        stateChanged(RioConstants.CHANNEL_ZONESTATUS,
                                "ON".equals(value) ? OnOffType.ON : OnOffType.OFF);
                        break;
                    case ZONE_MUTE:
                        stateChanged(RioConstants.CHANNEL_ZONEMUTE, "ON".equals(value) ? OnOffType.ON : OnOffType.OFF);
                        break;

                    case ZONE_SHAREDSOURCE:
                        stateChanged(RioConstants.CHANNEL_ZONESHAREDSOURCE,
                                "ON".equals(value) ? OnOffType.ON : OnOffType.OFF);
                        break;

                    case ZONE_LASTERROR:
                        stateChanged(RioConstants.CHANNEL_ZONELASTERROR, new StringType(value));
                        break;

                    case ZONE_PAGE:
                        stateChanged(RioConstants.CHANNEL_ZONEPAGE, "ON".equals(value) ? OnOffType.ON : OnOffType.OFF);
                        break;

                    case ZONE_SLEEPTIMEREMAINING:
                        try {
                            final int nbr = Integer.parseInt(value);
                            stateChanged(RioConstants.CHANNEL_ZONESLEEPTIMEREMAINING, new DecimalType(nbr));
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid zone notification (sleeptimeremaining not parsable): '{}')", resp);
                        }
                        break;

                    case ZONE_ENABLED:
                        stateChanged(RioConstants.CHANNEL_ZONEENABLED,
                                "ON".equals(value) ? OnOffType.ON : OnOffType.OFF);
                        break;

                    case ZONE_VOLUME:
                        try {
                            final int nbr = Integer.parseInt(value);
                            stateChanged(RioConstants.CHANNEL_ZONEVOLUME, new PercentType(nbr * 2));
                        } catch (NumberFormatException e) {
                            logger.warn("Invalid zone notification (volume not parsable): '{}')", resp);
                        }
                        break;

                    default:
                        logger.warn("Unknown zone notification: '{}'", resp);
                        break;
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid Zone Notification (controller/zone not a parsable integer): '{}')", resp);
            }
        } else {
            logger.warn("Invalid Zone Notification response: '{}'", resp);
        }

    }

    /**
     * Handles any system notifications returned by the russound system
     *
     * @param m a non-null matcher
     * @param resp a possibly null, possibly empty response
     */
    void handleZoneFavoriteNotification(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }
        if (m.groupCount() == 5) {
            try {
                final int notifyController = Integer.parseInt(m.group(1));
                if (notifyController != controller) {
                    return;
                }
                final int notifyZone = Integer.parseInt(m.group(2));
                if (notifyZone != zone) {
                    return;
                }

                final int favoriteId = Integer.parseInt(m.group(3));

                if (favoriteId >= 1 && favoriteId <= 2) {
                    final RioFavorite fav = zoneFavorites[favoriteId - 1];

                    final String key = m.group(4);
                    final String value = m.group(5);

                    switch (key) {
                        case FAV_NAME:
                            fav.setName(value);
                            updateZoneFavoritesChannel();
                            break;
                        case FAV_VALID:
                            fav.setValid(!"false".equalsIgnoreCase(value));
                            updateZoneFavoritesChannel();
                            break;

                        default:
                            logger.warn("Unknown zone favorite notification: '{}'", resp);
                            break;
                    }
                } else {
                    logger.warn("Invalid Zone Favorite Notification (favorite < 1 or > 2): '{}')", resp);
                }
            } catch (NumberFormatException e) {
                logger.warn("Invalid Zone Favorite Notification (favorite not a parsable integer): '{}')", resp);
            }
        } else {
            logger.warn("Invalid Zone Notification response: '{}'", resp);
        }
    }

    /**
     * Will update the zone favorites channel with only valid favorites
     */
    private void updateZoneFavoritesChannel() {
        final List<RioFavorite> favs = new ArrayList<RioFavorite>();
        for (final RioFavorite fav : zoneFavorites) {
            if (fav.isValid()) {
                favs.add(fav);
            }
        }

        final String favJson = gson.toJson(favs);
        stateChanged(RioConstants.CHANNEL_ZONEFAVORITES, new StringType(favJson));
    }

    /**
     * Callback method when system favorites are updated. Simply issues a state change for the zone system favorites
     * channel using the jsonString as the value
     */
    @Override
    public void systemFavoritesUpdated(String jsonString) {
        stateChanged(RioConstants.CHANNEL_ZONESYSFAVORITES, new StringType(jsonString));
    }

    /**
     * Callback method when presets are updated. Simply issues a state change for the zone presets channel using the
     * jsonString as the value
     */
    @Override
    public void presetsUpdated(int sourceIdUpdated, String jsonString) {
        if (sourceIdUpdated != sourceId.get()) {
            return;
        }
        stateChanged(RioConstants.CHANNEL_ZONEPRESETS, new StringType(jsonString));
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

        Matcher m = RSP_ZONENOTIFICATION.matcher(response);
        if (m.matches()) {
            handleZoneNotification(m, response);
        }

        m = RSP_ZONEFAVORITENOTIFICATION.matcher(response);
        if (m.matches()) {
            handleZoneFavoriteNotification(m, response);
        }

    }

    /**
     * Overrides the default implementation to turn watch off ({@link #watchZone(boolean)}) before calling the dispose
     */
    @Override
    public void dispose() {
        watchZone(false);
        favoritesProtocol.removeListener(this);
        super.dispose();
    }

}
