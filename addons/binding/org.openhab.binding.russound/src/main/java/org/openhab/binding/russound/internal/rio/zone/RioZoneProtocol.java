/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.russound.internal.rio.zone;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.openhab.binding.russound.internal.net.SocketSession;
import org.openhab.binding.russound.internal.net.SocketSessionListener;
import org.openhab.binding.russound.internal.rio.AbstractRioProtocol;
import org.openhab.binding.russound.internal.rio.RioConstants;
import org.openhab.binding.russound.internal.rio.RioHandlerCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the protocol handler for the Russound Zone. This handler will issue the protocol commands and will
 * process the responses from the Russound system.
 *
 * @author Tim Roberts
 *
 */
class RioZoneProtocol extends AbstractRioProtocol {
    // logger
    private Logger logger = LoggerFactory.getLogger(RioZoneProtocol.class);

    /**
     * The controller identifier
     */
    private int _controller;

    /**
     * The zone identifier
     */
    private int _zone;

    // Zone constants
    private final static String ZONE_NAME = "name"; // 12 max
    private final static String ZONE_SOURCE = "currentSource"; // 1-8 or 1-12
    private final static String ZONE_BASS = "bass"; // -10 to 10
    private final static String ZONE_TREBLE = "treble"; // -10 to 10
    private final static String ZONE_BALANCE = "balance"; // -10 to 10
    private final static String ZONE_LOUDNESS = "loudness"; // OFF/ON
    private final static String ZONE_TURNONVOLUME = "turnOnVolume"; // 0 to 50
    private final static String ZONE_DONOTDISTURB = "doNotDisturb"; // OFF/ON/SLAVE
    private final static String ZONE_PARTYMODE = "partyMode"; // OFF/ON/MASTER
    private final static String ZONE_STATUS = "status"; // OFF/ON/MASTER
    private final static String ZONE_VOLUME = "volume"; // 0 to 50
    private final static String ZONE_MUTE = "mute"; // OFF/ON/MASTER
    private final static String ZONE_PAGE = "page"; // OFF/ON/MASTER
    private final static String ZONE_SHAREDSOURCE = "sharedSource"; // OFF/ON/MASTER
    private final static String ZONE_SLEEPTIMEREMAINING = "sleepTimeRemaining"; // OFF/ON/MASTER
    private final static String ZONE_LASTERROR = "lastError"; // OFF/ON/MASTER
    private final static String ZONE_ENABLED = "enabled"; // OFF/ON

    // Respone patterns
    private final Pattern RSP_ZONENOTIFICATION = Pattern
            .compile("^[SN] C\\[(\\d+)\\]\\.Z\\[(\\d+)\\]\\.(\\w+)=\"(.*)\"$");

    /**
     * Constructs the protocol handler from given parameters
     *
     * @param zone the zone identifier
     * @param controller the controller identifier
     * @param session a non-null {@link SocketSession} (may be connected or disconnected)
     * @param callback a non-null {@link RioHandlerCallback} to callback
     */
    RioZoneProtocol(int zone, int controller, SocketSession session, RioHandlerCallback callback) {
        super(session, callback);

        if (controller < 1 || controller > 6) {
            throw new IllegalArgumentException("Controller must be between 1-6: " + controller);
        }
        if (zone < 1 || zone > 8) {
            throw new IllegalArgumentException("Zone must be between 1-6: " + zone);
        }

        _controller = controller;
        _zone = zone;
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

        sendCommand("GET C[" + _controller + "].Z[" + _zone + "]." + keyname);
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
     * Turns on/off watching for zone notifications
     *
     * @param on true to turn on, false to turn off
     */
    void watchZone(boolean watch) {
        sendCommand("WATCH C[" + _controller + "].Z[" + _zone + "] " + (watch ? "ON" : "OFF"));
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
        sendCommand("SET C[" + _controller + "].Z[" + _zone + "]." + ZONE_BASS + "=\"" + bass + "\"");
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
        sendCommand("SET C[" + _controller + "].Z[" + _zone + "]." + ZONE_TREBLE + "=\"" + treble + "\"");
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
        sendCommand("SET C[" + _controller + "].Z[" + _zone + "]." + ZONE_BALANCE + "=\"" + balance + "\"");
    }

    /**
     * Set's the zone's loudness
     *
     * @param on true to turn on loudness, false to turn off
     */
    void setZoneLoudness(boolean on) {
        sendCommand(
                "SET C[" + _controller + "].Z[" + _zone + "]." + ZONE_LOUDNESS + "=\"" + (on ? "ON" : "OFF") + "\"");
    }

    /**
     * Set's the zone turn on volume (from 0 to 50)
     *
     * @param volume the turn on volume (from 0 to 50)
     * @throws IllegalArgumentException if volume < 0 or > 50
     */
    void setZoneTurnOnVolume(int volume) {
        if (volume < 0 || volume > 50) {
            throw new IllegalArgumentException("Volume must be between 0 and 50: " + volume);
        }
        sendCommand("SET C[" + _controller + "].Z[" + _zone + "]." + ZONE_TURNONVOLUME + "=\"" + volume + "\"");
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
        sendCommand(
                "SET C[" + _controller + "].Z[" + _zone + "]." + ZONE_SLEEPTIMEREMAINING + "=\"" + sleepTime + "\"");
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
        sendCommand("EVENT C[" + _controller + "].Z[" + _zone + "]!SelectSource " + source);
    }

    /**
     * Set's the zone's status
     *
     * @param on true to turn on, false otherwise
     */
    void setZoneStatus(boolean on) {
        sendCommand("EVENT C[" + _controller + "].Z[" + _zone + "]!Zone" + (on ? "On" : "Off"));
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
        sendCommand("EVENT C[" + _controller + "].Z[" + _zone + "]!PartyMode " + partyMode);
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
        sendCommand("EVENT C[" + _controller + "].Z[" + _zone + "]!DoNotDisturb "
                + ("off".equals(doNotDisturb) ? "OFF" : "ON")); // translate "slave" to "on"
    }

    /**
     * Sets the zone's volume level (0-50)
     *
     * @param volume the volume level (0-50)
     * @throws IllegalArgumentException if volume is < 0 or > 50
     */
    void setZoneVolume(int volume) {
        if (volume < 0 || volume > 50) {
            throw new IllegalArgumentException("Volume must be between 0 and 50");
        }
        sendKeyPress("Volume " + volume);
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
        sendCommand("EVENT C[" + _controller + "].Z[" + _zone + "]!Shuffle");
    }

    /**
     * Toggles the zone's repeat if the source supports repeat mod
     */
    void toggleZoneRepeat() {
        sendCommand("EVENT C[" + _controller + "].Z[" + _zone + "]!Repeat");
    }

    /**
     * Assign a rating to the current song if the source supports a rating
     *
     * @param like true to like, false to dislike
     */
    void setZoneRating(boolean like) {
        sendCommand("EVENT C[" + _controller + "].Z[" + _zone + "]!MMRate " + (like ? "hi" : "low"));
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
        sendCommand("EVENT C[" + _controller + "].Z[" + _zone + "]!KeyPress " + keyPress);
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
        sendCommand("EVENT C[" + _controller + "].Z[" + _zone + "]!KeyRelease " + keyRelease);
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
        sendCommand("EVENT C[" + _controller + "].Z[" + _zone + "]!KeyHold " + keyHold);
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
        sendCommand("EVENT C[" + _controller + "].Z[" + _zone + "]!KeyCode " + keyCode);
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
        sendCommand("EVENT C[" + _controller + "].Z[" + _zone + "]!" + event);
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
                final int controller = Integer.parseInt(m.group(1));
                if (controller != _controller) {
                    return;
                }
                final int zone = Integer.parseInt(m.group(2));
                if (zone != _zone) {
                    return;
                }
                final String key = m.group(3);
                final String value = m.group(4);

                switch (key) {
                    case ZONE_NAME:
                        stateChanged(RioConstants.CHANNEL_ZONENAME, new StringType(value));
                        break;

                    case ZONE_SOURCE:
                        try {
                            final int nbr = Integer.parseInt(value);
                            stateChanged(RioConstants.CHANNEL_ZONESOURCE, new DecimalType(nbr));
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
                            stateChanged(RioConstants.CHANNEL_ZONETURNONVOLUME, new DecimalType(nbr));
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

        final Matcher m = RSP_ZONENOTIFICATION.matcher(response);
        if (m.matches()) {
            handleZoneNotification(m, response);
        }
    }

    /**
     * Overrides the default implementation to turn watch off ({@link #watchZone(boolean)}) before calling the dispose
     */
    @Override
    public void dispose() {
        watchZone(false);
        super.dispose();
    }
}
