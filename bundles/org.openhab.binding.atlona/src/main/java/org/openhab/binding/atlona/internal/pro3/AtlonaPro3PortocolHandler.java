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
package org.openhab.binding.atlona.internal.pro3;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.atlona.internal.AtlonaHandlerCallback;
import org.openhab.binding.atlona.internal.net.SocketSession;
import org.openhab.binding.atlona.internal.net.SocketSessionListener;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the protocol handler for the PRO3 product line. This handler will issue the protocol commands and will
 * process the responses from the PRO3 switch. This handler was written to respond to any response that can be sent from
 * the TCP/IP session (either in response to our own commands or in response to external events [other TCP/IP sessions,
 * web GUI, front panel keystrokes, etc]).
 *
 * @author Tim Roberts - Initial contribution
 * @author Michael Lobstein - Add support for AT-PRO3HD66M
 */
class AtlonaPro3PortocolHandler {
    private final Logger logger = LoggerFactory.getLogger(AtlonaPro3PortocolHandler.class);

    /**
     * The {@link SocketSession} used by this protocol handler
     */
    private final SocketSession session;

    /**
     * The {@link AtlonaPro3Config} configuration used by this handler
     */
    private final AtlonaPro3Config config;

    /**
     * The {@link AtlonaPro3Capabilities} of the PRO3 model
     */
    private final AtlonaPro3Capabilities capabilities;

    /**
     * The {@link AtlonaPro3Handler} to call back to update status and state
     */
    private final AtlonaHandlerCallback callback;

    /**
     * The model type identified by the switch. We save it for faster refreshes since it will not change
     */
    private String modelType;

    /**
     * The version (firmware) identified by the switch. We save it for faster refreshes since it will not change between
     * sessions
     */
    private String version;

    /**
     * A special (invalid) command used internally by this handler to identify whether the switch wants a login or not
     * (see {@link #login()})
     */
    private static final String NOTVALID_USER_OR_CMD = "notvalid$934%912";

    // ------------------------------------------------------------------------------------------------
    // The following are the various command formats specified by the Atlona protocol
    private static final String CMD_POWERON = "PWON";
    private static final String CMD_POWEROFF = "PWOFF";
    private static final String CMD_POWER_STATUS = "PWSTA";
    private static final String CMD_VERSION = "Version";
    private static final String CMD_TYPE = "Type";
    private static final String CMD_PANELLOCK = "Lock";
    private static final String CMD_PANELUNLOCK = "Unlock";
    private static final String CMD_PORT_RESETALL = "All#";
    private static final String CMD_PORT_POWER_FORMAT = "x%d$ %s";
    private static final String CMD_PORT_ALL_FORMAT = "x%dAll";
    private static final String CMD_PORT_SWITCH_FORMAT = "x%dAVx%d";
    private static final String CMD_PORT_MIRROR_FORMAT = "MirrorHdmi%d Out%d";
    private static final String CMD_PORT_MIRROR_STATUS_FORMAT = "MirrorHdmi%d sta";
    private static final String CMD_PORT_UNMIRROR_FORMAT = "UnMirror%d";
    private static final String CMD_VOLUME_FORMAT = "VOUT%d %s";
    private static final String CMD_VOLUME_MUTE_FORMAT = "VOUTMute%d %s";
    private static final String CMD_IROFF = "IROFF";
    private static final String CMD_IRON = "IRON";
    private static final String CMD_PORT_STATUS = "Status";
    private static final String CMD_PORT_STATUS_FORMAT = "Statusx%d";
    private static final String CMD_SAVEIO_FORMAT = "Save%d";
    private static final String CMD_RECALLIO_FORMAT = "Recall%d";
    private static final String CMD_CLEARIO_FORMAT = "Clear%d";
    private static final String CMD_MATRIX_RESET = "Mreset";
    private static final String CMD_BROADCAST_ON = "Broadcast on";

    // ------------------------------------------------------------------------------------------------
    // The following are the various responses specified by the Atlona protocol
    private static final String RSP_FAILED = "Command FAILED:";

    private static final String RSP_LOGIN = "Login";
    private static final String RSP_PASSWORD = "Password";

    private final Pattern powerStatusPattern = Pattern.compile("PW(\\w+)");
    private final Pattern versionPattern = Pattern.compile("Firmware (.*)");
    private final Pattern typePattern = Pattern.compile("AT-UHD-PRO3-(\\d+)M");
    private static final String RSP_ALL = "All#";
    private static final String RSP_LOCK = "Lock";
    private static final String RSP_UNLOCK = "Unlock";
    private final Pattern portStatusPattern = Pattern.compile("x(\\d+)AVx(\\d+),?+");
    private final Pattern portPowerPattern = Pattern.compile("x(\\d+)\\$ (\\w+)");
    private final Pattern portAllPattern = Pattern.compile("x(\\d+)All");
    private final Pattern portMirrorPattern = Pattern.compile("MirrorHdmi(\\d+) (\\p{Alpha}+)(\\d*)");
    private final Pattern portUnmirrorPattern = Pattern.compile("UnMirror(\\d+)");
    private final Pattern volumePattern = Pattern.compile("VOUT(\\d+) (-?\\d+)");
    private final Pattern volumeMutePattern = Pattern.compile("VOUTMute(\\d+) (\\w+)");
    private static final String RSP_IROFF = "IROFF";
    private static final String RSP_IRON = "IRON";
    private final Pattern saveIoPattern = Pattern.compile("Save(\\d+)");
    private final Pattern recallIoPattern = Pattern.compile("Recall(\\d+)");
    private final Pattern clearIoPattern = Pattern.compile("Clear(\\d+)");
    private final Pattern broadCastPattern = Pattern.compile("Broadcast (\\w+)");
    private static final String RSP_MATRIX_RESET = "Mreset";

    // Constants added to support the HD models
    private static final String RSP_WELCOME = "Welcome to TELNET";
    private static final String RSP_LOGIN_PLEASE = "Login Please";
    private static final String RSP_USERNAME = "Username";
    private static final String RSP_TRY_AGAIN = "Please Try Again";
    private final Pattern versionHdPattern = Pattern.compile("V(.*)");
    private final Pattern typeHdPattern = Pattern.compile("AT-PRO3HD(\\d+)M");

    // ------------------------------------------------------------------------------------------------
    // The following isn't part of the atlona protocol and is generated by us
    private static final String CMD_PING = "ping";
    private static final String RSP_PING = "Command FAILED: (ping)";

    /**
     * Constructs the protocol handler from given parameters
     *
     * @param session a non-null {@link SocketSession} (may be connected or disconnected)
     * @param config a non-null {@link AtlonaPro3Config}
     * @param capabilities a non-null {@link AtlonaPro3Capabilities}
     * @param callback a non-null {@link AtlonaHandlerCallback} to update state and status
     */
    AtlonaPro3PortocolHandler(SocketSession session, AtlonaPro3Config config, AtlonaPro3Capabilities capabilities,
            AtlonaHandlerCallback callback) {
        if (session == null) {
            throw new IllegalArgumentException("session cannot be null");
        }

        if (config == null) {
            throw new IllegalArgumentException("config cannot be null");
        }

        if (capabilities == null) {
            throw new IllegalArgumentException("capabilities cannot be null");
        }

        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }

        this.session = session;
        this.config = config;
        this.capabilities = capabilities;
        this.callback = callback;
    }

    /**
     * Attempts to log into the switch when prompted by the switch. Please see code comments on the exact protocol for
     * this.
     *
     * @return a null if logged in successfully (or if switch didn't require login). Non-null if an exception occurred.
     * @throws IOException an IO exception occurred during login
     */
    String loginUHD() throws Exception {
        logger.debug("Logging into atlona switch");
        // Void to make sure we retrieve them
        modelType = null;
        version = null;

        NoDispatchingCallback callback = new NoDispatchingCallback();
        session.addListener(callback);

        // Burn the initial (empty) return
        String response;
        try {
            response = callback.getResponse();
            if (!"".equals(response)) {
                logger.debug("Atlona protocol violation - didn't start with an inital empty response: '{}'", response);
            }
        } catch (Exception e) {
            // ignore - may not having given us an initial ""
        }

        // At this point - we are not sure if it's:
        // 1) waiting for a command input
        // or 2) has sent a "Login: " prompt
        // By sending a string that doesn't exist as a command or user
        // we can tell which by the response to the invalid command
        session.sendCommand(NOTVALID_USER_OR_CMD);

        // Command failed - Atlona not configured with IPLogin - return success
        response = callback.getResponse();
        if (response.startsWith(RSP_FAILED)) {
            logger.debug("Atlona didn't require a login");
            postLogin();
            return null;
        }

        // We should have been presented with a new "\r\nLogin: "
        response = callback.getResponse();
        if (!"".equals(response)) {
            logger.debug("Atlona protocol violation - didn't start with an inital empty response: '{}'", response);
        }

        // Get the new "Login: " prompt response
        response = callback.getResponse();
        if (RSP_LOGIN.equals(response)) {
            if (config.getUserName() == null || config.getUserName().trim().length() == 0) {
                return "Atlona PRO3 has enabled Telnet/IP Login but no username was provided in the configuration.";
            }

            // Send the username and wait for a ": " response
            session.sendCommand(config.getUserName());
        } else {
            return "Atlona protocol violation - wasn't initially a command failure or login prompt: " + response;
        }

        // We should have gotten the password response
        response = callback.getResponse();

        // Burn the empty response if we got one (
        if ("".equals(response)) {
            response = callback.getResponse();
        }
        if (!RSP_PASSWORD.equals(response)) {
            // If we got another login response, username wasn't valid
            if (RSP_LOGIN.equals(response)) {
                return "Username " + config.getUserName() + " is not a valid user on the atlona";
            }
            return "Atlona protocol violation - invalid response to a login: " + response;
        }

        // Make sure we have a password
        if (config.getPassword() == null || config.getPassword().trim().length() == 0) {
            return "Atlona PRO3 has enabled Telnet/IP Login but no password was provided in the configuration.";
        }

        // Send the password
        session.sendCommand(config.getPassword());
        response = callback.getResponse();

        // At this point, we don't know if we received a
        // 1) "\r\n" and waiting for a command
        // or 2) "\r\nLogin: " if the password is invalid
        // Send an invalid command to see if we get the failed command response

        // First make sure we had an empty response (the "\r\n" part)
        if (!"".equals(response)) {
            logger.debug("Atlona protocol violation - not an empty response after password: '{}'", response);
        }

        // Now send an invalid command
        session.sendCommand(NOTVALID_USER_OR_CMD);

        // If we get an invalid command response - we are logged in
        response = callback.getResponse();
        if (response.startsWith(RSP_FAILED)) {
            postLogin();
            return null;
        }

        // Nope - password invalid
        return "Password was invalid - please check your atlona setup";
    }

    /**
     * Attempts to log into the older HD model switches using a slightly different protocol
     *
     * @return a null if logged in successfully (or if switch didn't require login). Non-null if an exception occurred.
     * @throws IOException an IO exception occurred during login
     */
    String loginHD() throws Exception {
        logger.debug("Logging into atlona switch");
        // Void to make sure we retrieve them
        modelType = null;
        version = null;

        NoDispatchingCallback callback = new NoDispatchingCallback();
        session.addListener(callback);

        // Burn the initial (empty) return
        String response;
        try {
            response = callback.getResponse();
            if (!"".equals(response)) {
                logger.debug("Atlona protocol violation - didn't start with an inital empty response: '{}'", response);
            }
        } catch (Exception e) {
            // ignore - may not having given us an initial ""
        }

        response = callback.getResponse();
        if (response.startsWith(RSP_WELCOME)) {
            logger.debug("Atlona AT-PRO3HD66M didn't require a login");
            postLogin();
            return null;
        } else {
            if (!response.startsWith(RSP_LOGIN_PLEASE)) {
                logger.debug("Atlona protocol violation - didn't start with login prompt '{}'", response);
            }
            // Since we were not logged in automatically, a user name is required from the configuration
            if (config.getUserName() == null || config.getUserName().trim().length() == 0) {
                return "Atlona PRO3 has enabled Telnet/IP Login but no username was provided in the configuration.";
            }

            // Make sure we have a password too
            if (config.getPassword() == null || config.getPassword().trim().length() == 0) {
                return "Atlona PRO3 has enabled Telnet/IP Login but no password was provided in the configuration.";
            }

            // Check for an empty response after the login prompt (the "\r\n" part)
            response = callback.getResponse();
            if (!"".equals(response)) {
                logger.debug("Atlona protocol violation - not an empty response after password: '{}'", response);
            }

            // Send the username and wait for a ": " response
            session.sendCommand(config.getUserName());

            // We should have gotten the username response
            response = callback.getResponse();
            if (!response.startsWith(RSP_USERNAME)) {
                logger.debug("Atlona protocol violation - invalid response to username: '{}'", response);
            }

            // Send the password
            try {
                session.sendCommand(config.getPassword());
                response = callback.getResponse();
            } catch (Exception e) {
                return "Password was invalid - please check your atlona setup";
            }

            if (response.startsWith(RSP_TRY_AGAIN)) {
                return "Username " + config.getUserName() + " is not a valid user on the atlona";
            }

            if (response.startsWith(RSP_PASSWORD)) {
                // After the correct password is sent, several empty responses are sent before the welcome message
                for (int i = 0; i < 8; i++) {
                    response = callback.getResponse();

                    // If we get a welcome message, login was successful
                    if (response.startsWith(RSP_WELCOME)) {
                        postLogin();
                        return null;
                    }
                }
            }
        }
        return "Authentication failed - please check your atlona setup";
    }

    /**
     * Post successful login stuff - mark us online and refresh from the switch
     */
    private void postLogin() {
        logger.debug("Atlona switch now connected");
        session.clearListeners();
        session.addListener(new NormalResponseCallback());
        callback.statusChanged(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

        if (capabilities.isUHDModel()) {
            // Set broadcast to on to receive notifications when
            // routing changes (via the webpage, or presets or IR, etc)
            sendCommand(CMD_BROADCAST_ON);
        }

        // setup the most likely state of these switches (there is no protocol to get them)
        refreshAll();
    }

    /**
     * Returns the callback being used by this handler
     *
     * @return a non-null {@link AtlonaHandlerCallback}
     */
    AtlonaHandlerCallback getCallback() {
        return callback;
    }

    /**
     * Pings the server with an (invalid) ping command to keep the connection alive
     */
    void ping() {
        if (capabilities.isUHDModel()) {
            sendCommand(CMD_PING);
        } else {
            // the HD model does not reflect the invalid command string back in the response for us to match later
            sendCommand(CMD_VERSION);
        }
    }

    /**
     * Refreshes the state from the switch itself. This will retrieve all the state (that we can get) from the switch.
     */
    void refreshAll() {
        logger.debug("Refreshing matrix state");
        if (version == null) {
            refreshVersion();
        } else {
            callback.setProperty(AtlonaPro3Constants.PROPERTY_VERSION, version);
        }

        if (modelType == null) {
            refreshType();
        } else {
            callback.setProperty(AtlonaPro3Constants.PROPERTY_TYPE, modelType);
        }

        refreshPower();
        if (capabilities.isUHDModel()) {
            refreshAllPortStatuses();
        }

        final int nbrPowerPorts = capabilities.getNbrPowerPorts();
        for (int x = 1; x <= nbrPowerPorts; x++) {
            refreshPortPower(x);
        }

        final int nbrAudioPorts = capabilities.getNbrAudioPorts();
        for (int x = 1; x <= nbrAudioPorts; x++) {
            refreshVolumeStatus(x);
            refreshVolumeMute(x);
        }

        for (int x : capabilities.getHdmiPorts()) {
            refreshPortStatus(x);
        }
    }

    /**
     * Sets the power to the switch
     *
     * @param on true if on, false otherwise
     */
    void setPower(boolean on) {
        sendCommand(on ? CMD_POWERON : CMD_POWEROFF);
    }

    /**
     * Queries the switch about it's power state
     */
    void refreshPower() {
        sendCommand(CMD_POWER_STATUS);
    }

    /**
     * Queries the switch about it's version (firmware)
     */
    void refreshVersion() {
        sendCommand(CMD_VERSION);
    }

    /**
     * Queries the switch about it's type (model)
     */
    void refreshType() {
        sendCommand(CMD_TYPE);
    }

    /**
     * Sets whether the front panel is locked or not
     *
     * @param locked true if locked, false otherwise
     */
    void setPanelLock(boolean locked) {
        sendCommand(locked ? CMD_PANELLOCK : CMD_PANELUNLOCK);
    }

    /**
     * Resets all ports back to their default state.
     */
    void resetAllPorts() {
        sendCommand(CMD_PORT_RESETALL);
    }

    /**
     * Sets whether the specified port is powered (i.e. outputing).
     *
     * @param portNbr a greater than zero port number
     * @param on true if powered.
     */
    void setPortPower(int portNbr, boolean on) {
        if (portNbr <= 0) {
            throw new IllegalArgumentException("portNbr must be greater than 0");
        }
        sendCommand(String.format(CMD_PORT_POWER_FORMAT, portNbr, on ? "on" : "off"));
    }

    /**
     * Refreshes whether the specified port is powered (i.e. outputing).
     *
     * @param portNbr a greater than zero port number
     */
    void refreshPortPower(int portNbr) {
        if (portNbr <= 0) {
            throw new IllegalArgumentException("portNbr must be greater than 0");
        }
        sendCommand(String.format(CMD_PORT_POWER_FORMAT, portNbr, "sta"));
    }

    /**
     * Sets all the output ports to the specified input port.
     *
     * @param portNbr a greater than zero port number
     */
    void setPortAll(int portNbr) {
        if (portNbr <= 0) {
            throw new IllegalArgumentException("portNbr must be greater than 0");
        }
        sendCommand(String.format(CMD_PORT_ALL_FORMAT, portNbr));
    }

    /**
     * Sets the input port number to the specified output port number.
     *
     * @param inPortNbr a greater than zero port number
     * @param outPortNbr a greater than zero port number
     */
    void setPortSwitch(int inPortNbr, int outPortNbr) {
        if (inPortNbr <= 0) {
            throw new IllegalArgumentException("inPortNbr must be greater than 0");
        }
        if (outPortNbr <= 0) {
            throw new IllegalArgumentException("outPortNbr must be greater than 0");
        }
        sendCommand(String.format(CMD_PORT_SWITCH_FORMAT, inPortNbr, outPortNbr));
    }

    /**
     * Sets the hdmi port number to mirror the specified output port number.
     *
     * @param hdmiPortNbr a greater than zero port number
     * @param outPortNbr a greater than zero port number
     */
    void setPortMirror(int hdmiPortNbr, int outPortNbr) {
        if (hdmiPortNbr <= 0) {
            throw new IllegalArgumentException("hdmiPortNbr must be greater than 0");
        }
        if (outPortNbr <= 0) {
            throw new IllegalArgumentException("outPortNbr must be greater than 0");
        }

        if (capabilities.getHdmiPorts().contains(hdmiPortNbr)) {
            sendCommand(String.format(CMD_PORT_MIRROR_FORMAT, hdmiPortNbr, outPortNbr));
        } else {
            logger.info("Trying to set port mirroring on a non-hdmi port: {}", hdmiPortNbr);
        }
    }

    /**
     * Disabled mirroring on the specified hdmi port number.
     *
     * @param hdmiPortNbr a greater than zero port number
     * @param outPortNbr a greater than zero port number
     */
    void removePortMirror(int hdmiPortNbr) {
        if (hdmiPortNbr <= 0) {
            throw new IllegalArgumentException("hdmiPortNbr must be greater than 0");
        }

        if (capabilities.getHdmiPorts().contains(hdmiPortNbr)) {
            sendCommand(String.format(CMD_PORT_UNMIRROR_FORMAT, hdmiPortNbr));
        } else {
            logger.info("Trying to remove port mirroring on a non-hdmi port: {}", hdmiPortNbr);
        }
    }

    /**
     * Sets the volume level on the specified audio port.
     *
     * @param portNbr a greater than zero port number
     * @param level a volume level in decibels (must range from -79 to +15)
     */
    void setVolume(int portNbr, int level) {
        if (portNbr <= 0) {
            throw new IllegalArgumentException("portNbr must be greater than 0");
        }
        if (level < -79 || level > 15) {
            throw new IllegalArgumentException("level must be between -79 to +15");
        }
        sendCommand(String.format(CMD_VOLUME_FORMAT, portNbr, level));
    }

    /**
     * Refreshes the volume level for the given audio port.
     *
     * @param portNbr a greater than zero port number
     */
    void refreshVolumeStatus(int portNbr) {
        if (portNbr <= 0) {
            throw new IllegalArgumentException("portNbr must be greater than 0");
        }
        sendCommand(String.format(CMD_VOLUME_FORMAT, portNbr, "sta"));
    }

    /**
     * Refreshes the specified hdmi port's mirroring status
     *
     * @param hdmiPortNbr a greater than zero hdmi port number
     */
    void refreshPortMirror(int hdmiPortNbr) {
        if (hdmiPortNbr <= 0) {
            throw new IllegalArgumentException("hdmiPortNbr must be greater than 0");
        }
        sendCommand(String.format(CMD_PORT_MIRROR_STATUS_FORMAT, hdmiPortNbr));
    }

    /**
     * Mutes/Unmutes the specified audio port.
     *
     * @param portNbr a greater than zero port number
     * @param mute true to mute, false to unmute
     */
    void setVolumeMute(int portNbr, boolean mute) {
        if (portNbr <= 0) {
            throw new IllegalArgumentException("portNbr must be greater than 0");
        }
        sendCommand(String.format(CMD_VOLUME_MUTE_FORMAT, portNbr, mute ? "on" : "off"));
    }

    /**
     * Refreshes the volume mute for the given audio port.
     *
     * @param portNbr a greater than zero port number
     */
    void refreshVolumeMute(int portNbr) {
        if (portNbr <= 0) {
            throw new IllegalArgumentException("portNbr must be greater than 0");
        }
        sendCommand(String.format(CMD_VOLUME_MUTE_FORMAT, portNbr, "sta"));
    }

    /**
     * Turn on/off the front panel IR.
     *
     * @param on true for on, false otherwise
     */
    void setIrOn(boolean on) {
        sendCommand(on ? CMD_IRON : CMD_IROFF);
    }

    /**
     * Refreshes the input port setting on the specified output port.
     *
     * @param portNbr a greater than zero port number
     */
    void refreshPortStatus(int portNbr) {
        if (portNbr <= 0) {
            throw new IllegalArgumentException("portNbr must be greater than 0");
        }
        sendCommand(String.format(CMD_PORT_STATUS_FORMAT, portNbr));
    }

    /**
     * Refreshes all of the input port settings for all of the output ports.
     */
    private void refreshAllPortStatuses() {
        sendCommand(CMD_PORT_STATUS);
    }

    /**
     * Saves the current Input/Output scheme to the specified preset number.
     *
     * @param presetNbr a greater than 0 preset number
     */
    void saveIoSettings(int presetNbr) {
        if (presetNbr <= 0) {
            throw new IllegalArgumentException("presetNbr must be greater than 0");
        }
        sendCommand(String.format(CMD_SAVEIO_FORMAT, presetNbr));
    }

    /**
     * Recalls the Input/Output scheme for the specified preset number.
     *
     * @param presetNbr a greater than 0 preset number
     */
    void recallIoSettings(int presetNbr) {
        if (presetNbr <= 0) {
            throw new IllegalArgumentException("presetNbr must be greater than 0");
        }
        sendCommand(String.format(CMD_RECALLIO_FORMAT, presetNbr));
    }

    /**
     * Clears the Input/Output scheme for the specified preset number.
     *
     * @param presetNbr a greater than 0 preset number
     */
    void clearIoSettings(int presetNbr) {
        if (presetNbr <= 0) {
            throw new IllegalArgumentException("presetNbr must be greater than 0");
        }
        sendCommand(String.format(CMD_CLEARIO_FORMAT, presetNbr));
    }

    /**
     * Resets the matrix back to defaults.
     */
    void resetMatrix() {
        sendCommand(CMD_MATRIX_RESET);
    }

    /**
     * Sends the command and puts the thing into {@link ThingStatus#OFFLINE} if an IOException occurs
     *
     * @param command a non-null, non-empty command to send
     */
    private void sendCommand(String command) {
        if (command == null) {
            throw new IllegalArgumentException("command cannot be null");
        }
        if (command.trim().length() == 0) {
            throw new IllegalArgumentException("command cannot be empty");
        }
        try {
            session.sendCommand(command);
        } catch (IOException e) {
            callback.statusChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Exception occurred sending to Atlona: " + e);
        }
    }

    /**
     * Handles the switch power response. The first matching group should be "on" or "off"
     *
     * @param m the non-null {@link Matcher} that matched the response
     * @param resp the possibly null, possibly empty actual response
     */
    private void handlePowerResponse(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }
        if (m.groupCount() == 1) {
            switch (m.group(1)) {
                case "ON":
                    callback.stateChanged(AtlonaPro3Utilities.createChannelID(AtlonaPro3Constants.GROUP_PRIMARY,
                            AtlonaPro3Constants.CHANNEL_POWER), OnOffType.ON);
                    break;
                case "OFF":
                    callback.stateChanged(AtlonaPro3Utilities.createChannelID(AtlonaPro3Constants.GROUP_PRIMARY,
                            AtlonaPro3Constants.CHANNEL_POWER), OnOffType.OFF);
                    break;
                default:
                    logger.debug("Invalid power response: '{}'", resp);
            }
        } else {
            logger.debug("Invalid power response: '{}'", resp);
        }
    }

    /**
     * Handles the version (firmware) response. The first matching group should be the version
     *
     * @param m the non-null {@link Matcher} that matched the response
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleVersionResponse(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }
        if (m.groupCount() == 1) {
            version = m.group(1);
            callback.setProperty(AtlonaPro3Constants.PROPERTY_VERSION, version);
        } else {
            logger.debug("Invalid version response: '{}'", resp);
        }
    }

    /**
     * Handles the type (model) response. The first matching group should be the type.
     *
     * @param m the non-null {@link Matcher} that matched the response
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleTypeResponse(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }
        if (m.groupCount() == 1) {
            modelType = resp;
            callback.setProperty(AtlonaPro3Constants.PROPERTY_TYPE, modelType);
        } else {
            logger.debug("Invalid Type response: '{}'", resp);
        }
    }

    /**
     * Handles the panel lock response. The response is only on or off.
     *
     * @param resp the possibly null, possibly empty actual response
     */
    private void handlePanelLockResponse(String resp) {
        callback.stateChanged(AtlonaPro3Utilities.createChannelID(AtlonaPro3Constants.GROUP_PRIMARY,
                AtlonaPro3Constants.CHANNEL_PANELLOCK), OnOffType.from(RSP_LOCK.equals(resp)));
    }

    /**
     * Handles the port power response. The first two groups should be the port nbr and either "on" or "off"
     *
     * @param m the non-null {@link Matcher} that matched the response
     * @param resp the possibly null, possibly empty actual response
     */
    private void handlePortPowerResponse(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }
        if (m.groupCount() == 2) {
            try {
                int portNbr = Integer.parseInt(m.group(1));
                switch (m.group(2)) {
                    case "on":
                        callback.stateChanged(AtlonaPro3Utilities.createChannelID(AtlonaPro3Constants.GROUP_PORT,
                                portNbr, AtlonaPro3Constants.CHANNEL_PORTPOWER), OnOffType.ON);
                        break;
                    case "off":
                        callback.stateChanged(AtlonaPro3Utilities.createChannelID(AtlonaPro3Constants.GROUP_PORT,
                                portNbr, AtlonaPro3Constants.CHANNEL_PORTPOWER), OnOffType.OFF);
                        break;
                    default:
                        logger.debug("Invalid port power response: '{}'", resp);
                }
            } catch (NumberFormatException e) {
                logger.debug("Invalid port power (can't parse number): '{}'", resp);
            }
        } else {
            logger.debug("Invalid port power response: '{}'", resp);
        }
    }

    /**
     * Handles the port all response. Simply calls {@link #refreshAllPortStatuses()}
     *
     * @param resp ignored
     */
    private void handlePortAllResponse(String resp) {
        refreshAllPortStatuses();
    }

    /**
     * Handles the port output response. This matcher can have multiple groups separated by commas. Find each group and
     * that group should have two groups within - an input port nbr and an output port number
     *
     * @param m the non-null {@link Matcher} that matched the response
     * @param resp the possibly null, possibly empty actual response
     */
    private void handlePortOutputResponse(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }

        m.reset();
        while (m.find()) {
            try {
                int inPort = Integer.parseInt(m.group(1));
                int outPort = Integer.parseInt(m.group(2));

                callback.stateChanged(AtlonaPro3Utilities.createChannelID(AtlonaPro3Constants.GROUP_PORT, outPort,
                        AtlonaPro3Constants.CHANNEL_PORTOUTPUT), new DecimalType(inPort));
            } catch (NumberFormatException e) {
                logger.debug("Invalid port output response (can't parse number): '{}'", resp);
            }
        }
    }

    /**
     * Handles the mirror response. The matcher should have two groups - an hdmi port number and an output port number.
     *
     * @param m the non-null {@link Matcher} that matched the response
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleMirrorResponse(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }
        if (m.groupCount() == 3) {
            try {
                int hdmiPortNbr = Integer.parseInt(m.group(1));

                // could be "off" (if mirror off), "on"/"Out" (with 3rd group representing out)
                String oper = (m.group(2) == null ? "" : m.group(2).trim()).toLowerCase();

                if ("off".equals(oper)) {
                    callback.stateChanged(AtlonaPro3Utilities.createChannelID(AtlonaPro3Constants.GROUP_MIRROR,
                            hdmiPortNbr, AtlonaPro3Constants.CHANNEL_PORTMIRRORENABLED), OnOffType.OFF);
                } else {
                    int outPortNbr = Integer.parseInt(m.group(3));
                    callback.stateChanged(AtlonaPro3Utilities.createChannelID(AtlonaPro3Constants.GROUP_MIRROR,
                            hdmiPortNbr, AtlonaPro3Constants.CHANNEL_PORTMIRROR), new DecimalType(outPortNbr));
                    callback.stateChanged(AtlonaPro3Utilities.createChannelID(AtlonaPro3Constants.GROUP_MIRROR,
                            hdmiPortNbr, AtlonaPro3Constants.CHANNEL_PORTMIRRORENABLED), OnOffType.ON);
                }
            } catch (NumberFormatException e) {
                logger.debug("Invalid mirror response (can't parse number): '{}'", resp);
            }
        } else {
            logger.debug("Invalid mirror response: '{}'", resp);
        }
    }

    /**
     * Handles the unmirror response. The first group should contain the hdmi port number
     *
     * @param m the non-null {@link Matcher} that matched the response
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleUnMirrorResponse(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }
        if (m.groupCount() == 1) {
            try {
                int hdmiPortNbr = Integer.parseInt(m.group(1));
                callback.stateChanged(AtlonaPro3Utilities.createChannelID(AtlonaPro3Constants.GROUP_MIRROR, hdmiPortNbr,
                        AtlonaPro3Constants.CHANNEL_PORTMIRROR), new DecimalType(0));
            } catch (NumberFormatException e) {
                logger.debug("Invalid unmirror response (can't parse number): '{}'", resp);
            }
        } else {
            logger.debug("Invalid unmirror response: '{}'", resp);
        }
    }

    /**
     * Handles the volume response. The first two group should be the audio port number and the level
     *
     * @param m the non-null {@link Matcher} that matched the response
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleVolumeResponse(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }
        if (m.groupCount() == 2) {
            try {
                int portNbr = Integer.parseInt(m.group(1));
                double level = Double.parseDouble(m.group(2));
                callback.stateChanged(AtlonaPro3Utilities.createChannelID(AtlonaPro3Constants.GROUP_VOLUME, portNbr,
                        AtlonaPro3Constants.CHANNEL_VOLUME), new DecimalType(level));
            } catch (NumberFormatException e) {
                logger.debug("Invalid volume response (can't parse number): '{}'", resp);
            }
        } else {
            logger.debug("Invalid volume response: '{}'", resp);
        }
    }

    /**
     * Handles the volume mute response. The first two group should be the audio port number and either "on" or "off
     *
     * @param m the non-null {@link Matcher} that matched the response
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleVolumeMuteResponse(Matcher m, String resp) {
        if (m == null) {
            throw new IllegalArgumentException("m (matcher) cannot be null");
        }
        if (m.groupCount() == 2) {
            try {
                int portNbr = Integer.parseInt(m.group(1));
                switch (m.group(2)) {
                    case "on":
                        callback.stateChanged(AtlonaPro3Utilities.createChannelID(AtlonaPro3Constants.GROUP_VOLUME,
                                portNbr, AtlonaPro3Constants.CHANNEL_VOLUME_MUTE), OnOffType.ON);
                        break;
                    case "off":
                        callback.stateChanged(AtlonaPro3Utilities.createChannelID(AtlonaPro3Constants.GROUP_VOLUME,
                                portNbr, AtlonaPro3Constants.CHANNEL_VOLUME_MUTE), OnOffType.OFF);
                        break;
                    default:
                        logger.debug("Invalid volume mute response: '{}'", resp);
                }
            } catch (NumberFormatException e) {
                logger.debug("Invalid volume mute (can't parse number): '{}'", resp);
            }
        } else {
            logger.debug("Invalid volume mute response: '{}'", resp);
        }
    }

    /**
     * Handles the IR Response. The response is either on or off
     *
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleIrLockResponse(String resp) {
        callback.stateChanged(AtlonaPro3Utilities.createChannelID(AtlonaPro3Constants.GROUP_PRIMARY,
                AtlonaPro3Constants.CHANNEL_IRENABLE), OnOffType.from(RSP_IRON.equals(resp)));
    }

    /**
     * Handles the Save IO Response. Should have one group specifying the preset number
     *
     * @param m the non-null {@link Matcher} that matched the response
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleSaveIoResponse(Matcher m, String resp) {
        // nothing to handle
    }

    /**
     * Handles the Recall IO Response. Should have one group specifying the preset number. After updating the Recall
     * State, we refresh all the ports via {@link #refreshAllPortStatuses()}.
     *
     * @param m the non-null {@link Matcher} that matched the response
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleRecallIoResponse(Matcher m, String resp) {
        refreshAllPortStatuses();
    }

    /**
     * Handles the Clear IO Response. Should have one group specifying the preset number.
     *
     * @param m the non-null {@link Matcher} that matched the response
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleClearIoResponse(Matcher m, String resp) {
        // nothing to handle
    }

    /**
     * Handles the broadcast Response. Should have one group specifying the status.
     *
     * @param m the non-null {@link Matcher} that matched the response
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleBroadcastResponse(Matcher m, String resp) {
        // nothing to handle
    }

    /**
     * Handles the matrix reset response. The matrix will go offline immediately on a reset.
     *
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleMatrixResetResponse(String resp) {
        if (RSP_MATRIX_RESET.equals(resp)) {
            callback.statusChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "System is rebooting due to matrix reset");
        }
    }

    /**
     * Handles a command failure - we simply log the response as an error
     *
     * @param resp the possibly null, possibly empty actual response
     */
    private void handleCommandFailure(String resp) {
        logger.debug("{}", resp);
    }

    /**
     * This callback is our normal response callback. Should be set into the {@link SocketSession} after the login
     * process to handle normal responses.
     *
     * @author Tim Roberts
     *
     */
    private class NormalResponseCallback implements SocketSessionListener {

        @Override
        public void responseReceived(String response) {
            if (response.isEmpty()) {
                return;
            }

            if (RSP_PING.equals(response)) {
                // ignore
                return;
            }

            Matcher m;

            m = portStatusPattern.matcher(response);
            if (m.find()) {
                handlePortOutputResponse(m, response);
                return;
            }

            m = powerStatusPattern.matcher(response);
            if (m.matches()) {
                handlePowerResponse(m, response);
                return;
            }

            m = versionPattern.matcher(response);
            if (m.matches()) {
                handleVersionResponse(m, response);
                return;
            }

            m = versionHdPattern.matcher(response);
            if (!capabilities.isUHDModel() && m.matches()) {
                handleVersionResponse(m, response);
                return;
            }

            m = typePattern.matcher(response);
            if (m.matches()) {
                handleTypeResponse(m, response);
                return;
            }

            m = typeHdPattern.matcher(response);
            if (m.matches()) {
                handleTypeResponse(m, response);
                return;
            }

            m = portPowerPattern.matcher(response);
            if (m.matches()) {
                handlePortPowerResponse(m, response);
                return;
            }

            m = volumePattern.matcher(response);
            if (m.matches()) {
                handleVolumeResponse(m, response);
                return;
            }

            m = volumeMutePattern.matcher(response);
            if (m.matches()) {
                handleVolumeMuteResponse(m, response);
                return;
            }

            m = portAllPattern.matcher(response);
            if (m.matches()) {
                handlePortAllResponse(response);
                return;
            }

            m = portMirrorPattern.matcher(response);
            if (m.matches()) {
                handleMirrorResponse(m, response);
                return;
            }

            m = portUnmirrorPattern.matcher(response);
            if (m.matches()) {
                handleUnMirrorResponse(m, response);
                return;
            }

            m = saveIoPattern.matcher(response);
            if (m.matches()) {
                handleSaveIoResponse(m, response);
                return;
            }

            m = recallIoPattern.matcher(response);
            if (m.matches()) {
                handleRecallIoResponse(m, response);
                return;
            }

            m = clearIoPattern.matcher(response);
            if (m.matches()) {
                handleClearIoResponse(m, response);
                return;
            }

            m = broadCastPattern.matcher(response);
            if (m.matches()) {
                handleBroadcastResponse(m, response);
                return;
            }

            if (RSP_IRON.equals(response) || RSP_IROFF.equals(response)) {
                handleIrLockResponse(response);
                return;
            }

            if (RSP_ALL.equals(response)) {
                handlePortAllResponse(response);
                return;
            }

            if (RSP_LOCK.equals(response) || RSP_UNLOCK.equals(response)) {
                handlePanelLockResponse(response);
                return;
            }

            if (RSP_MATRIX_RESET.equals(response)) {
                handleMatrixResetResponse(response);
                return;
            }

            if (response.startsWith(RSP_FAILED)) {
                handleCommandFailure(response);
                return;
            }

            logger.debug("Unhandled response: {}", response);
        }

        @Override
        public void responseException(Exception e) {
            callback.statusChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Exception occurred reading from Atlona: " + e);
        }
    }

    /**
     * Special callback used during the login process to not dispatch the responses to this class but rather give them
     * back at each call to {@link NoDispatchingCallback#getResponse()}
     *
     * @author Tim Roberts
     *
     */
    private class NoDispatchingCallback implements SocketSessionListener {

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
            if (lastResponse instanceof String stringResponse) {
                return stringResponse;
            } else if (lastResponse instanceof Exception exceptionResponse) {
                throw exceptionResponse;
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
