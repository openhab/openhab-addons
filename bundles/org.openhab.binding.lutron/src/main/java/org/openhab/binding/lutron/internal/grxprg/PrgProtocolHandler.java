/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.grxprg;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the protocol handler for the GRX-PRG/GRX-CI-PRG. This handler will issue the protocol commands and will
 * process the responses from the interface. This handler was written to respond to any response that can be sent from
 * the TCP/IP session (either in response to our own commands or in response to external events [other TCP/IP sessions,
 * web GUI, etc]).
 *
 * @author Tim Roberts - Initial contribution
 *
 */
class PrgProtocolHandler {
    private Logger logger = LoggerFactory.getLogger(PrgProtocolHandler.class);

    /**
     * The {@link SocketSession} used by this protocol handler
     */
    private final SocketSession session;

    /**
     * The {@link PrgBridgeHandler} to call back to update status and state
     */
    private final PrgHandlerCallback phCallback;

    // ------------------------------------------------------------------------------------------------
    // The following are the various command formats specified by the
    // http://www.lutron.com/TechnicalDocumentLibrary/RS232ProtocolCommandSet.040196d.pdf
    private static final String CMD_SCENE = "A";
    private static final String CMD_SCENELOCK = "SL";
    private static final String CMD_SCENESTATUS = "G";
    private static final String CMD_SCENESEQ = "SQ";
    private static final String CMD_ZONELOCK = "ZL";
    private static final String CMD_ZONELOWER = "D";
    private static final String CMD_ZONELOWERSTOP = "E";
    private static final String CMD_ZONERAISE = "B";
    private static final String CMD_ZONERAISESTOP = "C";
    private static final String CMD_ZONEINTENSITY = "szi";
    private static final String CMD_ZONEINTENSITYSTATUS = "rzi";
    private static final String CMD_SETTIME = "ST";
    private static final String CMD_READTIME = "RT";
    private static final String CMD_SELECTSCHEDULE = "SS";
    private static final String CMD_REPORTSCHEDULE = "RS";
    private static final String CMD_SUNRISESUNSET = "RA";
    private static final String CMD_SUPERSEQUENCESTART = "QS";
    private static final String CMD_SUPERSEQUENCEPAUSE = "QP";
    private static final String CMD_SUPERSEQUENCERESUME = "QC";
    private static final String CMD_SUPERSEQUENCESTATUS = "Q?";

    // ------------------------------------------------------------------------------------------------
    // The following are the various responses specified by the
    // http://www.lutron.com/TechnicalDocumentLibrary/RS232ProtocolCommandSet.040196d.pdf
    private static final Pattern RSP_FAILED = Pattern.compile("^~ERROR # (\\d+) (\\d+) OK");
    private static final Pattern RSP_OK = Pattern.compile("^~(\\d+) OK");
    private static final Pattern RSP_RESETTING = Pattern.compile("^~:Reseting Device... (\\d+) OK");
    private static final Pattern RSP_RMU = Pattern
            .compile("^~:mu (\\d) (\\d+) (\\w+) (\\w+) (\\w+) (\\w+) (\\w+) (\\w+) (\\w+)");
    private static final Pattern RSP_SCENESTATUS = Pattern.compile("^~?:ss (\\w{8,8})( (\\d+) OK)?");
    private static final Pattern RSP_ZONEINTENSITY = Pattern.compile(
            "^~:zi (\\d) (\\w{1,3}) (\\w{1,3}) (\\w{1,3}) (\\w{1,3}) (\\w{1,3}) (\\w{1,3}) (\\w{1,3}) (\\w{1,3}) (\\d+) OK");
    private static final Pattern RSP_REPORTIME = Pattern
            .compile("^~:rt (\\d{1,2}) (\\d{1,2}) (\\d{1,2}) (\\d{1,2}) (\\d{1,2}) (\\d) (\\d+) OK");
    private static final Pattern RSP_REPORTSCHEDULE = Pattern.compile("^~:rs (\\d) (\\d+) OK");
    private static final Pattern RSP_SUNRISESUNSET = Pattern
            .compile("^~:ra (\\d{1,3}) (\\d{1,3}) (\\d{1,3}) (\\d{1,3}) (\\d+) OK");
    private static final Pattern RSP_SUPERSEQUENCESTATUS = Pattern
            .compile("^~:s\\? (\\w) (\\d+) (\\d{1,2}) (\\d{1,2}) (\\d+) OK");
    private static final Pattern RSP_BUTTON = Pattern.compile("^[^~:].*");
    private static final String RSP_CONNECTION_ESTABLISHED = "connection established";

    /**
     * A lookup between a 0-100 percentage and corresponding hex value. Note: this specifically matches the liason
     * software setup
     */
    private static final Map<Integer, String> INTENSITY_MAP = new HashMap<>();

    /**
     * The reverse lookup for the {{@link #INTENSITY_MAP}
     */
    private static final Map<String, Integer> REVERSE_INTENSITY_MAP = new HashMap<>();

    /**
     * A lookup between returned shade hex intensity to corresponding shade values
     */
    private static final Map<String, Integer> SHADE_INTENSITY_MAP = new HashMap<>();

    /**
     * Cache of current zone intensities
     */
    private final int[] zoneIntensities = new int[8];

    /**
     * Static method to setup the intensity lookup maps
     */
    static {
        INTENSITY_MAP.put(0, "0");
        INTENSITY_MAP.put(1, "2");
        INTENSITY_MAP.put(2, "3");
        INTENSITY_MAP.put(3, "4");
        INTENSITY_MAP.put(4, "6");
        INTENSITY_MAP.put(5, "7");
        INTENSITY_MAP.put(6, "8");
        INTENSITY_MAP.put(7, "9");
        INTENSITY_MAP.put(8, "B");
        INTENSITY_MAP.put(9, "C");
        INTENSITY_MAP.put(10, "D");
        INTENSITY_MAP.put(11, "F");
        INTENSITY_MAP.put(12, "10");
        INTENSITY_MAP.put(13, "11");
        INTENSITY_MAP.put(14, "12");
        INTENSITY_MAP.put(15, "14");
        INTENSITY_MAP.put(16, "15");
        INTENSITY_MAP.put(17, "16");
        INTENSITY_MAP.put(18, "18");
        INTENSITY_MAP.put(19, "19");
        INTENSITY_MAP.put(20, "1A");
        INTENSITY_MAP.put(21, "1B");
        INTENSITY_MAP.put(22, "1D");
        INTENSITY_MAP.put(23, "1E");
        INTENSITY_MAP.put(24, "1F");
        INTENSITY_MAP.put(25, "20");
        INTENSITY_MAP.put(26, "22");
        INTENSITY_MAP.put(27, "23");
        INTENSITY_MAP.put(28, "24");
        INTENSITY_MAP.put(29, "26");
        INTENSITY_MAP.put(30, "27");
        INTENSITY_MAP.put(31, "28");
        INTENSITY_MAP.put(32, "29");
        INTENSITY_MAP.put(33, "2B");
        INTENSITY_MAP.put(34, "2C");
        INTENSITY_MAP.put(35, "2D");
        INTENSITY_MAP.put(36, "2F");
        INTENSITY_MAP.put(37, "30");
        INTENSITY_MAP.put(38, "31");
        INTENSITY_MAP.put(39, "32");
        INTENSITY_MAP.put(40, "34");
        INTENSITY_MAP.put(41, "35");
        INTENSITY_MAP.put(42, "36");
        INTENSITY_MAP.put(43, "38");
        INTENSITY_MAP.put(44, "39");
        INTENSITY_MAP.put(45, "3A");
        INTENSITY_MAP.put(46, "3B");
        INTENSITY_MAP.put(47, "3D");
        INTENSITY_MAP.put(48, "3E");
        INTENSITY_MAP.put(49, "3F");
        INTENSITY_MAP.put(50, "40");
        INTENSITY_MAP.put(51, "42");
        INTENSITY_MAP.put(52, "43");
        INTENSITY_MAP.put(53, "44");
        INTENSITY_MAP.put(54, "46");
        INTENSITY_MAP.put(55, "47");
        INTENSITY_MAP.put(56, "48");
        INTENSITY_MAP.put(57, "49");
        INTENSITY_MAP.put(58, "4B");
        INTENSITY_MAP.put(59, "4C");
        INTENSITY_MAP.put(60, "4D");
        INTENSITY_MAP.put(61, "4F");
        INTENSITY_MAP.put(62, "50");
        INTENSITY_MAP.put(63, "51");
        INTENSITY_MAP.put(64, "52");
        INTENSITY_MAP.put(65, "54");
        INTENSITY_MAP.put(66, "55");
        INTENSITY_MAP.put(67, "56");
        INTENSITY_MAP.put(68, "58");
        INTENSITY_MAP.put(69, "59");
        INTENSITY_MAP.put(70, "5A");
        INTENSITY_MAP.put(71, "5B");
        INTENSITY_MAP.put(72, "5D");
        INTENSITY_MAP.put(73, "5E");
        INTENSITY_MAP.put(74, "5F");
        INTENSITY_MAP.put(75, "60");
        INTENSITY_MAP.put(76, "62");
        INTENSITY_MAP.put(77, "63");
        INTENSITY_MAP.put(78, "64");
        INTENSITY_MAP.put(79, "66");
        INTENSITY_MAP.put(80, "67");
        INTENSITY_MAP.put(81, "68");
        INTENSITY_MAP.put(82, "69");
        INTENSITY_MAP.put(83, "6B");
        INTENSITY_MAP.put(84, "6C");
        INTENSITY_MAP.put(85, "6D");
        INTENSITY_MAP.put(86, "6F");
        INTENSITY_MAP.put(87, "70");
        INTENSITY_MAP.put(88, "71");
        INTENSITY_MAP.put(89, "72");
        INTENSITY_MAP.put(90, "74");
        INTENSITY_MAP.put(91, "75");
        INTENSITY_MAP.put(92, "76");
        INTENSITY_MAP.put(93, "78");
        INTENSITY_MAP.put(94, "79");
        INTENSITY_MAP.put(95, "7A");
        INTENSITY_MAP.put(96, "7B");
        INTENSITY_MAP.put(97, "7D");
        INTENSITY_MAP.put(98, "7E");
        INTENSITY_MAP.put(99, "7F");
        INTENSITY_MAP.put(100, "7F");

        for (int key : INTENSITY_MAP.keySet()) {
            String value = INTENSITY_MAP.get(key);
            REVERSE_INTENSITY_MAP.put(value, key);
        }

        SHADE_INTENSITY_MAP.put("0", 0);
        SHADE_INTENSITY_MAP.put("5E", 0);
        SHADE_INTENSITY_MAP.put("15", 1);
        SHADE_INTENSITY_MAP.put("2D", 2);
        SHADE_INTENSITY_MAP.put("71", 3);
        SHADE_INTENSITY_MAP.put("72", 4);
        SHADE_INTENSITY_MAP.put("73", 5);
        SHADE_INTENSITY_MAP.put("5F", 1);
        SHADE_INTENSITY_MAP.put("60", 2);
        SHADE_INTENSITY_MAP.put("61", 3);
        SHADE_INTENSITY_MAP.put("62", 4);
        SHADE_INTENSITY_MAP.put("63", 5);
    }

    /**
     * Lookup of valid scene numbers (H is also sometimes returned - no idea what it is however)
     */
    private static final String VALID_SCENES = "0123456789ABCDEFG";

    /**
     * Constructs the protocol handler from given parameters
     *
     * @param session a non-null {@link SocketSession} (may be connected or disconnected)
     * @param config a non-null {@link PrgHandlerCallback}
     */
    PrgProtocolHandler(SocketSession session, PrgHandlerCallback callback) {
        if (session == null) {
            throw new IllegalArgumentException("session cannot be null");
        }

        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }

        this.session = session;
        this.phCallback = callback;
    }

    /**
     * Attempts to log into the interface.
     *
     * @return a null if logged in successfully. Non-null if an exception occurred.
     * @throws IOException an IO exception occurred during login
     */
    String login(String username) throws Exception {
        logger.info("Logging into the PRG interface");
        final NoDispatchingCallback callback = new NoDispatchingCallback();
        session.setCallback(callback);

        String response = callback.getResponse();
        if ("login".equals(response)) {
            session.sendCommand(username);
        } else {
            return "Protocol violation - wasn't initially a command failure or login prompt: " + response;
        }

        // We should have received back a connection established response
        response = callback.getResponse();

        // Burn the empty response if we got one (
        if ("".equals(response)) {
            response = callback.getResponse();
        }

        if (RSP_CONNECTION_ESTABLISHED.equals(response)) {
            postLogin();
            return null;
        } else {
            return "login failed";
        }
    }

    /**
     * Post successful login stuff - mark us online and refresh from the switch
     *
     * @throws IOException
     */
    private void postLogin() throws IOException {
        logger.info("PRG interface now connected");
        session.setCallback(new NormalResponseCallback());
        phCallback.statusChanged(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
    }

    /**
     * Refreshes the state of the specified control unit
     *
     * @param controlUnit the control unit to refresh
     */
    void refreshState(int controlUnit) {
        logger.debug("Refreshing control unit ({}) state", controlUnit);
        refreshScene();
        refreshTime();
        refreshSchedule();
        refreshSunriseSunset();
        reportSuperSequenceStatus();

        // The RMU would return the zone lock, scene lock and scene seq state
        // Unfortunately, if any of those are true - the PRG interface locks up
        // the response until turned off - so comment out

        // Get the current state of the zone/scene lock
        // sendCommand("spm");
        // sendCommand("rmu " + controlUnit);
        // sendCommand("epm");

        refreshZoneIntensity(controlUnit);
    }

    /**
     * Validate the control unit parameter
     *
     * @param controlUnit a control unit between 1-8
     * @throws IllegalArgumentException if controlUnit is < 0 or > 8
     */
    private void validateControlUnit(int controlUnit) {
        if (controlUnit < 1 || controlUnit > 8) {
            throw new IllegalArgumentException("Invalid control unit (must be between 1 and 8): " + controlUnit);
        }
    }

    /**
     * Validates the scene and converts it to the corresponding hex value
     *
     * @param scene a scene between 0 and 16
     * @return the valid hex value of the scene
     * @throws IllegalArgumentException if scene is < 0 or > 16
     */
    private char convertScene(int scene) {
        if (scene < 0 || scene > VALID_SCENES.length()) {
            throw new IllegalArgumentException(
                    "Invalid scene (must be between 0 and " + VALID_SCENES.length() + "): " + scene);
        }
        return VALID_SCENES.charAt(scene);
    }

    /**
     * Validates the zone
     *
     * @param zone the zone to validate
     * @throws IllegalArgumentException if zone < 1 or > 8
     */
    private void validateZone(int zone) {
        if (zone < 1 || zone > 8) {
            throw new IllegalArgumentException("Invalid zone (must be between 1 and 8): " + zone);
        }
    }

    /**
     * Validates the fade and converts it to hex
     *
     * @param fade the fade
     * @return a valid fade value
     * @throws IllegalArgumentException if fade < 0 or > 120
     */
    private String convertFade(int fade) {
        if (fade < 0 || fade > 120) {
            throw new IllegalArgumentException("Invalid fade (must be between 1 and 120): " + fade);
        }
        if (fade > 59) {
            fade = (fade / 60) + 59;
        }
        return Integer.toHexString(fade).toUpperCase();
    }

    /**
     * Validates a zone intensity and returns the hex corresponding value (handles shade intensity zones as well)
     *
     * @param controlUnit the control unit
     * @param zone the zone
     * @param intensity the new intensity level
     * @return a valid hex representation
     * @throws IllegalArgumentException if controlUnit, zone or intensity are invalid
     */
    private String convertIntensity(int controlUnit, int zone, int intensity) {
        validateControlUnit(controlUnit);
        validateZone(zone);

        if (intensity < 0 || intensity > 100) {
            throw new IllegalArgumentException("Invalid intensity (must be between 0 and 100): " + intensity);
        }

        final boolean isShade = phCallback.isShade(controlUnit, zone);
        if (isShade) {
            if (intensity > 5) {
                throw new IllegalArgumentException("Invalid SHADE intensity (must be between 0 and 5): " + intensity);
            }
            return Integer.toString(intensity);
        } else {
            final String hexNbr = INTENSITY_MAP.get(intensity);
            if (hexNbr == null) { // this should be impossible as all 100 values are in table
                logger.warn("Unknown zone intensity ({})", intensity);
                return Integer.toHexString(intensity).toUpperCase();
            }
            return hexNbr;
        }
    }

    /**
     * Converts a hex zone intensity back to an integer - handles shade zones as well
     *
     * @param controlUnit the control unit
     * @param zone the zone
     * @param intensity the hex intensity value
     * @return the new intensity (between 0-100)
     * @throws IllegalArgumentException if controlUnit, zone or intensity are invalid
     */
    private int convertIntensity(int controlUnit, int zone, String intensity) {
        validateControlUnit(controlUnit);
        validateZone(zone);

        final boolean isShade = phCallback.isShade(controlUnit, zone);

        if (isShade) {
            final Integer intNbr = SHADE_INTENSITY_MAP.get(intensity);
            if (intNbr == null) {
                logger.warn("Unknown shade intensity ({})", intensity);
                return Integer.parseInt(intensity, 16);
            }
            return intNbr;
        } else {
            final Integer intNbr = REVERSE_INTENSITY_MAP.get(intensity);
            if (intNbr == null) {
                logger.warn("Unknown zone intensity ({})", intensity);
                return Integer.parseInt(intensity, 16);
            }
            zoneIntensities[zone] = intNbr;
            return intNbr;
        }
    }

    /**
     * Selects a specific scene on a control unit
     *
     * @param controlUnit the control unit
     * @param scene the new scene
     * @throws IllegalArgumentException if controlUnit or scene are invalid
     */
    void selectScene(int controlUnit, int scene) {
        validateControlUnit(controlUnit);
        sendCommand(CMD_SCENE + convertScene(scene) + controlUnit);
    }

    /**
     * Queries the interface for the current scene status on all control units
     */
    void refreshScene() {
        sendCommand(CMD_SCENESTATUS);
    }

    /**
     * Sets the scene locked/unlocked for the specific control unit
     *
     * @param controlUnit the control unit
     * @param locked true for locked, false otherwise
     * @throws IllegalArgumentException if controlUnit is invalid
     */
    void setSceneLock(int controlUnit, boolean locked) {
        validateControlUnit(controlUnit);
        sendCommand(CMD_SCENELOCK + (locked ? "+" : "-") + controlUnit);
    }

    /**
     * Sets the scene sequence on/off for the specific control unit
     *
     * @param controlUnit the control unit
     * @param on true for sequencing on, false otherwise
     * @throws IllegalArgumentException if controlUnit is invalid
     */
    void setSceneSequence(int controlUnit, boolean on) {
        validateControlUnit(controlUnit);
        sendCommand(CMD_SCENESEQ + (on ? "+" : "-") + controlUnit);
    }

    /**
     * Sets the zone locked/unlocked for the specific control unit
     *
     * @param controlUnit the control unit
     * @param locked true for locked, false otherwise
     * @throws IllegalArgumentException if controlUnit is invalid
     */
    void setZoneLock(int controlUnit, boolean locked) {
        validateControlUnit(controlUnit);
        sendCommand(CMD_ZONELOCK + (locked ? "+" : "-") + controlUnit);
    }

    /**
     * Sets the zone to lowering for the specific control unit
     *
     * @param controlUnit the control unit
     * @param zone the zone to lower
     * @throws IllegalArgumentException if controlUnit or zone is invalid
     */
    void setZoneLower(int controlUnit, int zone) {
        validateControlUnit(controlUnit);
        validateZone(zone);
        sendCommand(CMD_ZONELOWER + controlUnit + zone);
    }

    /**
     * Stops the zone lowering on all control units
     */
    void setZoneLowerStop() {
        sendCommand(CMD_ZONELOWERSTOP);
    }

    /**
     * Sets the zone to raising for the specific control unit
     *
     * @param controlUnit the control unit
     * @param zone the zone to raise
     * @throws IllegalArgumentException if controlUnit or zone is invalid
     */
    void setZoneRaise(int controlUnit, int zone) {
        validateControlUnit(controlUnit);
        validateZone(zone);
        sendCommand(CMD_ZONERAISE + controlUnit + zone);
    }

    /**
     * Stops the zone raising on all control units
     */
    void setZoneRaiseStop() {
        sendCommand(CMD_ZONERAISESTOP);
    }

    /**
     * Sets the zone intensity up/down by 1 with the corresponding fade time on the specific zone/control unit. Does
     * nothing if already at floor or ceiling. If the specified zone is a shade, does nothing.
     *
     * @param controlUnit the control unit
     * @param zone the zone
     * @param fade the fade time (0-59 seconds, 60-3600 seconds converted to minutes)
     * @param increase true to increase by 1, false otherwise
     * @throws IllegalArgumentException if controlUnit, zone or fade is invalid
     */
    void setZoneIntensity(int controlUnit, int zone, int fade, boolean increase) {
        if (phCallback.isShade(controlUnit, zone)) {
            return;
        }

        validateControlUnit(controlUnit);
        validateZone(zone);

        int newInt = zoneIntensities[zone] += (increase ? 1 : -1);
        if (newInt < 0) {
            newInt = 0;
        }
        if (newInt > 100) {
            newInt = 100;
        }

        setZoneIntensity(controlUnit, zone, fade, newInt);
    }

    /**
     * Sets the zone intensity to a specific number with the corresponding fade time on the specific zone/control unit.
     * If a shade, only deals with intensities from 0 to 5 (stop, open close, preset 1, preset 2, preset 3).
     *
     * @param controlUnit the control unit
     * @param zone the zone
     * @param fade the fade time (0-59 seconds, 60-3600 seconds converted to minutes)
     * @param increase true to increase by 1, false otherwise
     * @throws IllegalArgumentException if controlUnit, zone, fade or intensity is invalid
     */
    void setZoneIntensity(int controlUnit, int zone, int fade, int intensity) {
        validateControlUnit(controlUnit);
        validateZone(zone);

        final String hexFade = convertFade(fade);
        final String hexIntensity = convertIntensity(controlUnit, zone, intensity);

        final StringBuilder sb = new StringBuilder(16);
        for (int z = 1; z <= 8; z++) {
            sb.append(' ');
            sb.append(zone == z ? hexIntensity : "*");
        }

        sendCommand(CMD_ZONEINTENSITY + " " + controlUnit + " " + hexFade + sb);
    }

    /**
     * Refreshes the current zone intensities for the control unit
     *
     * @param controlUnit the control unit
     * @throws IllegalArgumentException if control unit is invalid
     */
    void refreshZoneIntensity(int controlUnit) {
        validateControlUnit(controlUnit);
        sendCommand(CMD_ZONEINTENSITYSTATUS + " " + controlUnit);
    }

    /**
     * Sets the time on the PRG interface
     *
     * @param calendar a non-null calendar to set the time to
     * @throws IllegalArgumentException if calendar is null
     */
    void setTime(Calendar calendar) {
        if (calendar == null) {
            throw new IllegalArgumentException("calendar cannot be null");
        }
        final String cmd = String.format("%1 %2$tk %2$tM %2$tm %2$te %2ty %3", CMD_SETTIME, calendar,
                calendar.get(Calendar.DAY_OF_WEEK));
        sendCommand(cmd);
    }

    /**
     * Refreshes the time from the PRG interface
     */
    void refreshTime() {
        sendCommand(CMD_READTIME);
    }

    /**
     * Selects the specific schedule (0=none, 1=weekday, 2=weekend)
     *
     * @param schedule the new schedule
     * @throws IllegalArgumentException if schedule is < 0 or > 32
     */
    void selectSchedule(int schedule) {
        if (schedule < 0 || schedule > 2) {
            throw new IllegalArgumentException("Schedule invalid (must be between 0 and 2): " + schedule);
        }
        sendCommand(CMD_SELECTSCHEDULE + " " + schedule);
    }

    /**
     * Refreshes the current schedule
     */
    void refreshSchedule() {
        sendCommand(CMD_REPORTSCHEDULE);
    }

    /**
     * Refreshs the current sunrise/sunset
     */
    void refreshSunriseSunset() {
        sendCommand(CMD_SUNRISESUNSET);
    }

    /**
     * Starts the super sequence
     */
    void startSuperSequence() {
        sendCommand(CMD_SUPERSEQUENCESTART);
        reportSuperSequenceStatus();
    }

    /**
     * Pauses the super sequence
     */
    void pauseSuperSequence() {
        sendCommand(CMD_SUPERSEQUENCEPAUSE);
    }

    /**
     * Resumes the super sequence
     */
    void resumeSuperSequence() {
        sendCommand(CMD_SUPERSEQUENCERESUME);
    }

    /**
     * Refreshes the status of the super sequence
     */
    void reportSuperSequenceStatus() {
        sendCommand(CMD_SUPERSEQUENCESTATUS);
    }

    /**
     * Sends the command and puts the thing into {@link ThingStatus#OFFLINE} if an IOException occurs
     *
     * @param command a non-null, non-empty command to send
     * @throws IllegalArgumentException if command is null or empty
     */
    private void sendCommand(String command) {
        if (command == null) {
            throw new IllegalArgumentException("command cannot be null");
        }
        if (command.trim().length() == 0) {
            throw new IllegalArgumentException("command cannot be empty");
        }
        try {
            logger.debug("SendCommand: {}", command);
            session.sendCommand(command);
        } catch (IOException e) {
            phCallback.statusChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Exception occurred sending to PRG: " + e);
        }
    }

    /**
     * Handles a command failure - we simply log the response as an error (trying to convert the error number to a
     * legible error message)
     *
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleCommandFailure(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }
        if (m.groupCount() == 2) {
            try {
                final int errorNbr = Integer.parseInt(m.group(1));
                String errorMsg = "ErrorCode: " + errorNbr;
                switch (errorNbr) {
                    case 1: {
                        errorMsg = "Control Unit Raise/Lower error";
                        break;
                    }
                    case 2: {
                        errorMsg = "Invalid scene selected";
                        break;
                    }
                    case 6: {
                        errorMsg = "Bad command was sent";
                        break;
                    }
                    case 13: {
                        errorMsg = "Not a timeclock unit (GRX-ATC or GRX-PRG)";
                        break;
                    }
                    case 14: {
                        errorMsg = "Illegal time was entered";
                        break;
                    }
                    case 15: {
                        errorMsg = "Invalid schedule";
                        break;
                    }
                    case 16: {
                        errorMsg = "No Super Sequence has been loaded";
                        break;
                    }
                    case 20: {
                        errorMsg = "Command was missing Control Units";
                        break;
                    }
                    case 21: {
                        errorMsg = "Command was missing data";
                        break;
                    }
                    case 22: {
                        errorMsg = "Error in command argument (improper hex value)";
                        break;
                    }
                    case 24: {
                        errorMsg = "Invalid Control Unit";
                        break;
                    }
                    case 25: {
                        errorMsg = "Invalid value, outside range of acceptable values";
                        break;
                    }
                    case 26: {
                        errorMsg = "Invalid Accessory Control";
                        break;
                    }
                    case 31: {
                        errorMsg = "Network address illegally formatted; 4 octets required (xxx.xxx.xxx.xxx)";
                        break;
                    }
                    case 80: {
                        errorMsg = "Time-out error, no response received";
                        break;
                    }
                    case 100: {
                        errorMsg = "Invalid Telnet login number";
                        break;
                    }
                    case 101: {
                        errorMsg = "Invalid Telnet login";
                        break;
                    }
                    case 102: {
                        errorMsg = "Telnet login name exceeds 8 characters";
                        break;
                    }
                    case 103: {
                        errorMsg = "INvalid number of arguments";
                        break;
                    }
                    case 255: {
                        errorMsg = "GRX-PRG must be in programming mode for specific commands";
                        break;
                    }
                }
                logger.error("Error response: {} ({})", errorMsg, errorNbr);
            } catch (NumberFormatException e) {
                logger.error("Invalid failure response (can't parse error number): '{}'", resp);
            }
        } else {
            logger.error("Invalid failure response: '{}'", resp);
        }
    }

    /**
     * Handles the scene status response
     *
     * @param m the non-null {@link Matcher} that matched the response
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleSceneStatus(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }
        if (m.groupCount() >= 2) {
            try {
                final String sceneStatus = m.group(1);
                for (int i = 1; i <= 8; i++) {
                    char status = sceneStatus.charAt(i - 1);
                    if (status == 'M') {
                        continue; // no control unit
                    }

                    int scene = VALID_SCENES.indexOf(status);
                    if (scene < 0) {
                        logger.warn("Unknown scene status returned for zone {}: {}", i, status);
                    } else {
                        phCallback.stateChanged(i, PrgConstants.CHANNEL_SCENE, new DecimalType(scene));
                        refreshZoneIntensity(i); // request to get new zone intensities
                    }
                }
            } catch (NumberFormatException e) {
                logger.error("Invalid scene status (can't parse scene #): '{}'", resp);
            }
        } else {
            logger.error("Invalid scene status response: '{}'", resp);
        }
    }

    /**
     * Handles the report time response
     *
     * @param m the non-null {@link Matcher} that matched the response
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleReportTime(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }
        if (m.groupCount() == 7) {
            try {
                final Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group(1)));
                c.set(Calendar.MINUTE, Integer.parseInt(m.group(2)));
                c.set(Calendar.MONDAY, Integer.parseInt(m.group(3)));
                c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(m.group(4)));

                final int yr = Integer.parseInt(m.group(5));
                c.set(Calendar.YEAR, yr + (yr < 50 ? 1900 : 2000));

                phCallback.stateChanged(PrgConstants.CHANNEL_TIMECLOCK,
                        new DateTimeType(ZonedDateTime.ofInstant(c.toInstant(), ZoneId.systemDefault())));
            } catch (NumberFormatException e) {
                logger.error("Invalid time response (can't parse number): '{}'", resp);
            }
        } else {
            logger.error("Invalid time response: '{}'", resp);
        }
    }

    /**
     * Handles the report schedule response
     *
     * @param m the non-null {@link Matcher} that matched the response
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleReportSchedule(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }
        if (m.groupCount() == 2) {
            try {
                int schedule = Integer.parseInt(m.group(1));
                phCallback.stateChanged(PrgConstants.CHANNEL_SCHEDULE, new DecimalType(schedule));
            } catch (NumberFormatException e) {
                logger.error("Invalid schedule response (can't parse number): '{}'", resp);
            }
        } else {
            logger.error("Invalid schedule volume response: '{}'", resp);
        }
    }

    /**
     * Handles the sunrise/sunset response
     *
     * @param m the non-null {@link Matcher} that matched the response
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleSunriseSunset(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }
        if (m.groupCount() == 5) {
            if ("255".equals(m.group(1))) {
                logger.warn("Sunrise/Sunset needs to be enabled via Liason Software");
                return;
            }
            try {
                final Calendar sunrise = Calendar.getInstance();
                sunrise.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group(1)));
                sunrise.set(Calendar.MINUTE, Integer.parseInt(m.group(2)));
                phCallback.stateChanged(PrgConstants.CHANNEL_SUNRISE,
                        new DateTimeType(ZonedDateTime.ofInstant(sunrise.toInstant(), ZoneId.systemDefault())));

                final Calendar sunset = Calendar.getInstance();
                sunset.set(Calendar.HOUR_OF_DAY, Integer.parseInt(m.group(3)));
                sunset.set(Calendar.MINUTE, Integer.parseInt(m.group(4)));
                phCallback.stateChanged(PrgConstants.CHANNEL_SUNSET,
                        new DateTimeType(ZonedDateTime.ofInstant(sunset.toInstant(), ZoneId.systemDefault())));
            } catch (NumberFormatException e) {
                logger.error("Invalid sunrise/sunset response (can't parse number): '{}'", resp);
            }
        } else {
            logger.error("Invalid sunrise/sunset response: '{}'", resp);
        }
    }

    /**
     * Handles the super sequence response
     *
     * @param m the non-null {@link Matcher} that matched the response
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleSuperSequenceStatus(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }
        if (m.groupCount() == 5) {
            try {
                final int nextStep = Integer.parseInt(m.group(2));
                final int nextMin = Integer.parseInt(m.group(3));
                final int nextSec = Integer.parseInt(m.group(4));
                phCallback.stateChanged(PrgConstants.CHANNEL_SUPERSEQUENCESTATUS, new StringType(m.group(1)));
                phCallback.stateChanged(PrgConstants.CHANNEL_SUPERSEQUENCENEXTSTEP, new DecimalType(nextStep));
                phCallback.stateChanged(PrgConstants.CHANNEL_SUPERSEQUENCENEXTMIN, new DecimalType(nextMin));
                phCallback.stateChanged(PrgConstants.CHANNEL_SUPERSEQUENCENEXTSEC, new DecimalType(nextSec));
            } catch (NumberFormatException e) {
                logger.error("Invalid volume response (can't parse number): '{}'", resp);
            }
        } else {
            logger.error("Invalid format volume response: '{}'", resp);
        }
    }

    /**
     * Handles the zone intensity response
     *
     * @param m the non-null {@link Matcher} that matched the response
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleZoneIntensity(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }

        if (m.groupCount() == 10) {
            try {
                final int controlUnit = Integer.parseInt(m.group(1));
                for (int z = 1; z <= 8; z++) {
                    final String zi = m.group(z + 1);
                    if ("*".equals(zi) || zi.equals(Integer.toString(z - 1))) {
                        continue; // not present
                    }
                    final int zid = convertIntensity(controlUnit, z, zi);

                    phCallback.stateChanged(controlUnit, PrgConstants.CHANNEL_ZONEINTENSITY + z, new PercentType(zid));
                }
            } catch (NumberFormatException e) {
                logger.error("Invalid volume response (can't parse number): '{}'", resp);
            }
        } else {
            logger.error("Invalid format volume response: '{}'", resp);
        }
    }

    /**
     * Handles the controller information response (currently not used).
     *
     * @param m the non-null {@link Matcher} that matched the response
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleControlInfo(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }
        if (m.groupCount() == 9) {
            int controlUnit = 0;
            try {
                controlUnit = Integer.parseInt(m.group(1));

                final String q4 = m.group(8);
                final String q4bits = new StringBuilder(Integer.toBinaryString(Integer.parseInt(q4, 16))).reverse()
                        .toString();
                // final boolean seqType = (q4bits.length() > 0 ? q4bits.charAt(0) : '0') == '1';
                final boolean seqMode = (q4bits.length() > 1 ? q4bits.charAt(1) : '0') == '1';
                final boolean zoneLock = (q4bits.length() > 2 ? q4bits.charAt(2) : '0') == '1';
                final boolean sceneLock = (q4bits.length() > 3 ? q4bits.charAt(4) : '0') == '1';

                phCallback.stateChanged(controlUnit, PrgConstants.CHANNEL_SCENESEQ, OnOffType.from(seqMode));
                phCallback.stateChanged(controlUnit, PrgConstants.CHANNEL_SCENELOCK, OnOffType.from(sceneLock));
                phCallback.stateChanged(controlUnit, PrgConstants.CHANNEL_ZONELOCK, OnOffType.from(zoneLock));
            } catch (NumberFormatException e) {
                logger.error("Invalid controller information response: '{}'", resp);
            }
        } else {
            logger.error("Invalid controller information response: '{}'", resp);
        }
    }

    /**
     * Handles the interface being reset
     *
     * @param m the non-null {@link Matcher} that matched the response
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleResetting(Matcher m, String resp) {
        phCallback.statusChanged(ThingStatus.OFFLINE, ThingStatusDetail.DUTY_CYCLE, "Device resetting");
    }

    /**
     * Handles the button press response
     *
     * @param m the non-null {@link Matcher} that matched the response
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleButton(Matcher m, String resp) {
        phCallback.stateChanged(PrgConstants.CHANNEL_BUTTONPRESS, new StringType(resp));
    }

    /**
     * Handles an unknown response (simply logs it)
     *
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleUnknownCommand(String response) {
        logger.info("Unhandled response: {}", response);
    }

    /**
     * This callback is our normal response callback. Should be set into the {@link SocketSession} after the login
     * process to handle normal responses.
     *
     * @author Tim Roberts
     *
     */
    private class NormalResponseCallback implements SocketSessionCallback {

        @Override
        public void responseReceived(String response) {
            // logger.debug("Response received: " + response);

            if (response == null || response.trim().length() == 0) {
                return; // simple blank - do nothing
            }

            Matcher m = RSP_OK.matcher(response);
            if (m.matches()) {
                // logger.debug(response);
                return; // nothing to do on an OK! response
            }

            m = RSP_FAILED.matcher(response);
            if (m.matches()) {
                handleCommandFailure(m, response);
                return; // nothing really to do on an error response either
            }

            m = RSP_SCENESTATUS.matcher(response);
            if (m.matches()) {
                handleSceneStatus(m, response);
                return;
            }

            m = RSP_REPORTIME.matcher(response);
            if (m.matches()) {
                handleReportTime(m, response);
                return;
            }

            m = RSP_REPORTSCHEDULE.matcher(response);
            if (m.matches()) {
                handleReportSchedule(m, response);
                return;
            }

            m = RSP_SUNRISESUNSET.matcher(response);
            if (m.matches()) {
                handleSunriseSunset(m, response);
                return;
            }

            m = RSP_SUPERSEQUENCESTATUS.matcher(response);
            if (m.matches()) {
                handleSuperSequenceStatus(m, response);
                return;
            }

            m = RSP_ZONEINTENSITY.matcher(response);
            if (m.matches()) {
                handleZoneIntensity(m, response);
                return;
            }

            m = RSP_RMU.matcher(response);
            if (m.matches()) {
                handleControlInfo(m, response);
                return;
            }

            m = RSP_RESETTING.matcher(response);
            if (m.matches()) {
                handleResetting(m, response);
                return;
            }

            m = RSP_BUTTON.matcher(response);
            if (m.matches()) {
                handleButton(m, response);
                return;
            }

            if (RSP_CONNECTION_ESTABLISHED.equals(response)) {
                return; // nothing to do on connection established
            }

            handleUnknownCommand(response);
        }

        @Override
        public void responseException(Exception exception) {
            phCallback.statusChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Exception occurred reading from PRG: " + exception);
        }
    }

    /**
     * Special callback used during the login process to not dispatch the responses to this class but rather give them
     * back at each call to {@link NoDispatchingCallback#getResponse()}
     *
     * @author Tim Roberts
     *
     */
    private class NoDispatchingCallback implements SocketSessionCallback {

        /**
         * Cache of responses that have occurred
         */
        private BlockingQueue<Object> responses = new ArrayBlockingQueue<>(5);

        /**
         * Will return the next response from {@link #responses}. If the response is an exception, that exception will
         * be thrown instead.
         *
         * @return a non-null, possibly empty response
         * @throws Exception an exception if one occurred during reading
         */
        String getResponse() throws Exception {
            final Object lastResponse = responses.poll(5, TimeUnit.SECONDS);
            if (lastResponse instanceof String str) {
                return str;
            } else if (lastResponse instanceof Exception exception) {
                throw exception;
            } else if (lastResponse == null) {
                throw new Exception("Didn't receive response in time");
            } else {
                return lastResponse.toString();
            }
        }

        @Override
        public void responseReceived(String response) {
            try {
                responses.put(response);
            } catch (InterruptedException e) {
            }
        }

        @Override
        public void responseException(Exception e) {
            try {
                responses.put(e);
            } catch (InterruptedException e1) {
            }
        }
    }
}
