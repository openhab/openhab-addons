/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.bravia;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.sony.internal.net.SocketSession;
import org.openhab.binding.sony.internal.net.SocketSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the protocol handler for the Bravia System. This handler will issue the protocol commands and will
 * process the responses from the Bravia system. The Bravia system is a little flacky and doesn't seem to handle
 * multiple commands in a single session. For this reason, we create a single {@link SocketSession} to listen for any
 * notifications (whose lifetime matches that of this handler) and then create separate {@link SocketSession} for each
 * request. Special care must be taken to differentiate between a Control request result and the Enquiry/Notification
 * results to avoid misinterpreting the result (the control "success" message will have all zeroes - which has a form
 * that matches some enquery/notification results (like volume could be interpreted as 0!).
 *
 * @author Tim Roberts
 * @version $Id: $Id
 *
 */
class BraviaProtocol implements SocketSessionListener {
    // Logger
    private Logger logger = LoggerFactory.getLogger(BraviaProtocol.class);

    // Protocol Constants
    private final char TYPE_CONTROL = 'C';
    private final char TYPE_ENQUIRY = 'E';

    private final static String IRCC = "IRCC";
    private final static String POWER = "POWR";
    private final static String VOLUME = "VOLU";
    private final static String AUDIO_MUTE = "AMUT";
    private final static String CHANNEL = "CHNN";
    private final static String TRIPLET_CHANNEL = "TCHN";
    private final static String INPUT_SOURCE = "ISRC";
    private final static String INPUT = "INPT";
    private final static String PICTURE_MUTE = "PMUT";
    private final static String TOGGLE_PICTURE_MUTE = "TPMU";
    private final static String PICTURE_IN_PICTURE = "PIPI";
    private final static String TOGGLE_PICTURE_IN_PICTURE = "TPIP";
    private final static String TOGGLE_PIP_POSITION = "TPPP";
    private final static String BROADCAST_ADDRESS = "BADR";
    private final static String MAC_ADDRESS = "MADR";
    private final static String NO_PARM = "################";

    // Size of the parameter area as defined by the spec
    private final static int PARM_SIZE = 16;

    // Response strings/patterns
    private final String RSP_SUCCESS = "0000000000000000";
    private final String RSP_ERROR = "FFFFFFFFFFFFFFFF";
    private final String RSP_NOSUCHTHING = "NNNNNNNNNNNNNNNN";
    private final Pattern RSP_NOTIFICATION = Pattern.compile("^\\*S([AN])(\\w{4})(.*{16})");

    /**
     * The {@link SocketSession} that will listen for notifications
     */
    private final SocketSession _listeningSession;

    /**
     * The {@link BraviaConfig} for creating new {@link SocketSession}
     */
    private final BraviaConfig _config;

    /**
     * The {@link BraviaHandlerCallback} that we can callback to set state and status
     */
    private final BraviaHandlerCallback _callback;

    /**
     * Represents a lookup between the known input source types and their corresponding bravia identifier
     */
    private final static Map<Integer, String> _inputTypes = new HashMap<Integer, String>();
    static {
        _inputTypes.put(0, "TV");
        _inputTypes.put(10000, "HDMI");
        _inputTypes.put(20000, "SCART");
        _inputTypes.put(30000, "Composite");
        _inputTypes.put(40000, "Component");
        _inputTypes.put(50000, "Screen Mirroring");
        _inputTypes.put(60000, "PC RGB Input");
    }

    /**
     * Constructs the protocol handler from given parameters. This constructor will create the
     * {@link #_listeningSession} to listen to notifications sent by the Bravia device (adding ourselfs as the
     * listener).
     *
     * @param config a non-null {@link BraviaConfig} (may be connected or disconnected)
     * @param callback a non-null {@link RioHandlerCallback} to callback
     * @throws IOException
     */
    BraviaProtocol(BraviaConfig config, BraviaHandlerCallback callback) throws IOException {
        _config = config;
        _callback = callback;

        _listeningSession = new SocketSession(config.getIpAddress(), 20060);
        _listeningSession.addListener(this);
    }

    /**
     * Attempts to log into the system. The login will connect the {@link #_listeningSession} and immediately call
     * {@link #postLogin()} since there is no authentication mechanisms
     *
     * @return always null to indicate a successful login
     * @throws IOException if an exception occurs trying to connect our {@link #_listeningSession}
     */
    String login() throws IOException {
        _listeningSession.connect();
        postLogin();
        return null;
    }

    /**
     * Post successful login stuff - mark us online!
     */
    private void postLogin() {
        logger.info("Bravia TV System now connected");
        _callback.statusChanged(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
    }

    /**
     * Called to refresh some of the state of the bravia system (mainly state that either we won't get notifications for
     * or whose state may commonly change due to remote actions
     */
    void refreshState() {
        refreshVolume();
        refreshChannel();
        refreshTripletChannel();
        refreshInputSource();
        refreshPictureInPicture();
    }

    /**
     * Sends the command and puts the thing into {@link ThingStatus#OFFLINE} if an IOException occurs. This method will
     * create a new {@link SocketSession} for the command and a anonymous {@link SocketSessionListener} to listen for
     * the result. The connection will then be closed/disposed of when a valid response is received (if the listener is
     * more than 10 seconds old, it will be disposed of to avoid memory leaks).
     *
     * @param type the type of command ({@link #TYPE_CONTROL} or {@link #TYPE_ENQUIRY})
     * @param command a non-null, non-empty command to send
     * @param parm the parameters for the command. Must be exactly {@link #PARM_SIZE} in length
     * @throws IllegalArgumentException if command is null or empty
     * @throws IllegalArgumentException if parm is null or not {@link #PARM_SIZE} in length
     */
    protected void sendCommand(final char type, final String command, String parm) {
        if (command == null || command.trim().length() == 0) {
            throw new IllegalArgumentException("command cannot be null or empty");
        }
        if (parm == null || parm.length() != PARM_SIZE) {
            throw new IllegalArgumentException(
                    "parm cannot be null and must be exactly " + PARM_SIZE + " in length: " + parm);
        }

        // Create our command
        final String cmd = "*S" + type + command + parm;

        // Create the socket session for this request
        final SocketSession cmdSocket = new SocketSession(_config.getIpAddress(), 20060);

        // Add our listener to it
        cmdSocket.addListener(new SocketSessionListener() {
            private long createTime = System.currentTimeMillis();

            // Disconnects (must be in a different thread to avoid locking issues)
            private void disconnect(final SocketSessionListener listener) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            cmdSocket.removeListener(listener);
                            cmdSocket.disconnect();
                        } catch (IOException e) {

                        }

                    }
                }).start();
            }

            @Override
            public void responseReceived(String response) {
                // If request is more than 10 seconds, cancel this listener/socket
                if (createTime + 10000 < System.currentTimeMillis()) {
                    logger.debug("No valid response received within 10 seconds - removing listener");
                    disconnect(this);
                    return;
                }

                // See if the response is valid
                final Matcher m = RSP_NOTIFICATION.matcher(response);
                if (m.matches() && m.groupCount() == 3) {

                    // make sure we only process responses for our command
                    if (m.group(1).equals("A") && m.group(2).equals(command)) {
                        if (type == TYPE_CONTROL) {
                            handleResponse(m, response, cmd);
                        } else if (type == TYPE_ENQUIRY) {
                            handleNotification(m, response);
                        } else {
                            logger.error("Unknown command type: " + type);
                        }
                        disconnect(this);
                    }
                } else {
                    logger.warn("Unparsable response '{}' to command '{}'", response, cmd);
                }

            }

            @Override
            public void responseException(Exception e) {
                BraviaProtocol.this.responseException(e);
                disconnect(this);
            }
        });

        // Connect to the socket and send out command
        try {
            cmdSocket.connect();
            cmdSocket.sendCommand(cmd);
        } catch (IOException e) {
            _callback.statusChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Exception occurred sending command: " + e);
        }
    }

    /**
     * Pings the server with out ping command to keep the connection alive
     */
    void ping() {
        try {
            _listeningSession.sendCommand("*SEPING################");
        } catch (IOException e) {
            _callback.statusChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Exception occurred sending command: PING");
        }
    }

    /**
     * Refreshes the power status
     */
    void refreshPower() {
        sendCommand(TYPE_ENQUIRY, POWER, NO_PARM);
    }

    /**
     * Refreshes the volume status
     */
    void refreshVolume() {
        sendCommand(TYPE_ENQUIRY, VOLUME, NO_PARM);
    }

    /**
     * Refreshes the audio mute status
     */
    void refreshAudioMute() {
        sendCommand(TYPE_ENQUIRY, AUDIO_MUTE, NO_PARM);
    }

    /**
     * Refreshes the channel
     */
    void refreshChannel() {
        sendCommand(TYPE_ENQUIRY, CHANNEL, NO_PARM);
    }

    /**
     * Refreshes the channel triplet
     */
    void refreshTripletChannel() {
        sendCommand(TYPE_ENQUIRY, TRIPLET_CHANNEL, NO_PARM);
    }

    /**
     * Refreshes the input source
     */
    void refreshInputSource() {
        sendCommand(TYPE_ENQUIRY, INPUT_SOURCE, NO_PARM);
    }

    /**
     * Refreshes the input
     */
    void refreshInput() {
        sendCommand(TYPE_ENQUIRY, INPUT, NO_PARM);
    }

    /**
     * Refreshes the picture mute
     */
    void refreshPictureMute() {
        sendCommand(TYPE_ENQUIRY, PICTURE_MUTE, NO_PARM);
    }

    /**
     * Refreshes the PIP
     */
    void refreshPictureInPicture() {
        sendCommand(TYPE_ENQUIRY, PICTURE_IN_PICTURE, NO_PARM);
    }

    /**
     * Refreshes the broadcast address
     *
     * @param netInterface the possibly null network interface to inquire
     */
    void refreshBroadcastAddress(String netInterface) {
        if (netInterface == null) {
            netInterface = "eth0";
        }
        sendCommand(TYPE_ENQUIRY, BROADCAST_ADDRESS, StringUtils.rightPad(netInterface, PARM_SIZE, '#'));
    }

    /**
     * Refreshes the mac address of the given network interface
     *
     * @param netInterface the possibly null mac interface to inquire
     */
    void refreshMacAddress(String interfce) {
        if (interfce == null) {
            interfce = "eth0";
        }
        sendCommand(TYPE_ENQUIRY, MAC_ADDRESS, StringUtils.rightPad(interfce, PARM_SIZE, '#'));
    }

    /**
     * Sets the IRCC code
     *
     * @param ircc a non negative IR code
     * @throws IllegalArgumentException if ircc is negative
     */
    void setIR(int ircc) {
        if (ircc < 0) {
            throw new IllegalArgumentException("ircc must be positive");

        }
        sendCommand(TYPE_CONTROL, IRCC, StringUtils.leftPad(Integer.toString(ircc), PARM_SIZE, '0'));
    }

    /**
     * Sets the power on/off to the device
     *
     * @param on true if on, false off
     */
    void setPower(boolean on) {
        sendCommand(TYPE_CONTROL, POWER, StringUtils.leftPad(on ? "1" : "0", PARM_SIZE, '0'));
    }

    /**
     * Sets the volume level
     *
     * @param volume a volume between 0-100
     * @throws IllegalArgumentException if < 0 or > 100
     */
    void setAudioVolume(int volume) {
        if (volume < 0 || volume > 100) {
            throw new IllegalArgumentException("volume must be between 0-100");
        }
        sendCommand(TYPE_CONTROL, VOLUME, StringUtils.leftPad(Integer.toString(volume), PARM_SIZE, '0'));
    }

    /**
     * Sets the audio mute
     *
     * @param on true for muted, false otherwise
     */
    void setAudioMute(boolean on) {
        sendCommand(TYPE_CONTROL, AUDIO_MUTE, StringUtils.leftPad(on ? "1" : "0", PARM_SIZE, '0'));
    }

    /**
     * Sets the channel. Channel must be in the form of "x.x" or simply "x" where x must be numeric
     *
     * @param channel the non-null, non-empty channel in the form of "x.x" or "x" (such as 50.1 or 6)
     * @throws IllegalArgumentException if channel is null or empty
     * @throws IllegalArgumentException if channel is not in the form of "x.x" or "x" where x is numeric
     */
    void setChannel(String channel) {
        if (channel == null || channel.trim().length() == 0) {
            throw new IllegalArgumentException("channel cannot be null or empty");
        }
        final int period = channel.indexOf('.');

        final String pre = period < 0 ? channel : channel.substring(0, period);
        final String post = period < 0 ? "0" : channel.substring(period + 1);
        try {
            final int preNum = Integer.parseInt(pre);
            final int postNum = Integer.parseInt(post);
            final String cmd = StringUtils.leftPad(Integer.toString(preNum), 8, '0') + "."
                    + StringUtils.rightPad(Integer.toString(postNum), 7, '0');
            sendCommand(TYPE_CONTROL, CHANNEL, cmd);

            refreshTripletChannel();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("channel could not be parsed: " + channel);
        }
    }

    /**
     * Sets the triplet channel. Channel must be in the form of "x.x.x" where x must be numeric
     *
     * @param channel the non-null, non-empty channel in the form of "x.x.x" (such as 32736.32736.1024)
     * @throws IllegalArgumentException if channel is null or empty
     * @throws IllegalArgumentException if channel is not in the form of "x.x.x"
     */
    void setTripletChannel(String channel) {
        if (channel == null || channel.trim().length() == 0) {
            throw new IllegalArgumentException("channel cannot be null or empty");
        }
        final int firstPeriod = channel.indexOf('.');
        if (firstPeriod < 0) {
            throw new IllegalArgumentException(
                    "Could not find the number of the first part of the triplet channel: " + channel);
        }

        final int secondPeriod = channel.indexOf(',', firstPeriod + 1);
        if (firstPeriod < 0) {
            throw new IllegalArgumentException(
                    "Could not find the number of the second part of the triplet channel: " + channel);
        }

        final String first = channel.substring(0, firstPeriod);
        final String second = channel.substring(firstPeriod + 1, secondPeriod);
        final String third = channel.substring(secondPeriod + 1);
        try {
            final int firstNum = Integer.parseInt(first);
            final int secondNum = Integer.parseInt(second);
            final int thirdNum = Integer.parseInt(third);

            final String firstHex = StringUtils.leftPad(Integer.toHexString(firstNum), 4, '0');
            final String secondHex = StringUtils.leftPad(Integer.toHexString(secondNum), 4, '0');
            final String thirdHex = StringUtils.leftPad(Integer.toHexString(thirdNum), 4, '0');

            sendCommand(TYPE_CONTROL, CHANNEL, firstHex + secondHex + thirdHex + "####");
            refreshChannel();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("channel could not be parsed: " + channel);
        }
    }

    /**
     * Sets the input source and will refresh channel and triplet channel afterwards. This must be a valid string
     * recognized by the bravia device.
     *
     * @param source a non-null, non-empty input source
     * @throws IllegalArgumentException if source is null or empty
     */
    void setInputSource(String source) {
        if (source == null || source.trim().length() == 0) {
            throw new IllegalArgumentException("source cannot be null or empty");
        }
        sendCommand(TYPE_CONTROL, INPUT_SOURCE, StringUtils.rightPad(source, PARM_SIZE, '#'));

        refreshChannel();
        refreshTripletChannel();
    }

    /**
     * Sets the input port and will refresh input source, channel and triplet channel afterwards. This must be a valid
     * in the form of "xxxxyyyy" where xxxx is the name of the input and yyyy is the port number (like "hdmi1"). The
     * valid input names are "TV", "HDMI", "SCART", "Composite", "Component", "Screen Mirroring", and "PC RGB Input"
     * (case doesn't matter). The port number does NOT apply to "TV".
     *
     * @param input a non-null, non-empty input port
     * @throws IllegalArgumentException if input is null or empty
     */
    void setInput(String input) {
        if (input == null || input.trim().length() == 0) {
            throw new IllegalArgumentException("input cannot be null or empty");
        }
        int typeCode = -1;
        int portNbr = -1;
        final String lowerInput = input.toLowerCase();
        for (Entry<Integer, String> entry : _inputTypes.entrySet()) {
            if (lowerInput.startsWith(entry.getValue().toLowerCase())) {
                typeCode = entry.getKey();
                if (typeCode > 0) {
                    try {
                        portNbr = Integer.parseInt(input.substring(entry.getValue().length()));
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException(
                                "The port number on the input is invalid (not an integer): " + input);
                    }
                } else {
                    portNbr = 0;
                }
                break;
            }
        }
        if (typeCode == -1) {
            throw new IllegalArgumentException("Unknown input: " + input);
        }
        sendCommand(TYPE_CONTROL, INPUT, StringUtils.leftPad(Integer.toString(typeCode), 12, '0')
                + StringUtils.leftPad(Integer.toString(portNbr), 4, '0'));

        refreshChannel();
        refreshTripletChannel();
        refreshInputSource();
    }

    /**
     * Sets the picture mute
     *
     * @param on true for muted, false otherwise
     */
    void setPictureMute(boolean on) {
        sendCommand(TYPE_CONTROL, PICTURE_MUTE, StringUtils.leftPad(on ? "1" : "0", PARM_SIZE, '0'));
    }

    /**
     * Toggles the picture mute
     */
    void togglePictureMute() {
        sendCommand(TYPE_CONTROL, TOGGLE_PICTURE_MUTE, NO_PARM);
    }

    /**
     * Sets the PIP enabling
     *
     * @param on true to enable, false otherwise
     */
    void setPictureInPicture(boolean on) {
        sendCommand(TYPE_CONTROL, PICTURE_IN_PICTURE, StringUtils.leftPad(on ? "1" : "0", PARM_SIZE, '0'));
    }

    /**
     * Toggles PIP enabling
     */
    void togglePictureInPicture() {
        sendCommand(TYPE_CONTROL, TOGGLE_PICTURE_IN_PICTURE, NO_PARM);
    }

    /**
     * Toggles the PIP position
     */
    void togglePipPosition() {
        sendCommand(TYPE_CONTROL, TOGGLE_PIP_POSITION, NO_PARM);
    }

    /**
     * Handles control responses from commands (*SC->*SA)
     *
     * @param m a non-null matcher
     * @param resp a possibly null, possibly empty response
     * @param command the possibly null, possibly empty command that triggered this response
     */
    private void handleResponse(Matcher m, String response, String command) {
        if (m.groupCount() == 3) {
            final String cmd = m.group(2);
            final String parms = m.group(3);

            if (IRCC.equalsIgnoreCase(cmd)) {
                handleIRResponse(parms);
            } else if (POWER.equalsIgnoreCase(cmd)) {
                handlePowerResponse(parms);
            } else if (VOLUME.equalsIgnoreCase(cmd)) {
                handleAudioVolumeResponse(parms);
            } else if (AUDIO_MUTE.equalsIgnoreCase(cmd)) {
                handleAudioMuteResponse(parms);
            } else if (CHANNEL.equalsIgnoreCase(cmd)) {
                handleChannelResponse(parms);
            } else if (TRIPLET_CHANNEL.equalsIgnoreCase(cmd)) {
                handleTripletChannelResponse(parms);
            } else if (INPUT_SOURCE.equalsIgnoreCase(cmd)) {
                handleInputSourceResponse(parms);
            } else if (INPUT.equalsIgnoreCase(cmd)) {
                handleInputResponse(parms);
            } else if (PICTURE_MUTE.equalsIgnoreCase(cmd)) {
                handlePictureMuteResponse(parms);
            } else if (TOGGLE_PICTURE_MUTE.equalsIgnoreCase(cmd)) {
                handleTogglePictureMuteResponse(parms);
            } else if (PICTURE_IN_PICTURE.equalsIgnoreCase(cmd)) {
                handlePictureInPictureResponse(parms);
            } else if (TOGGLE_PICTURE_IN_PICTURE.equalsIgnoreCase(cmd)) {
                handleTogglePictureInPictureResponse(parms);
            } else if (TOGGLE_PIP_POSITION.equalsIgnoreCase(cmd)) {
                handleTogglePIPPosition(parms);
            } else {
                logger.warn("Unknown command response '{}' to command '{}' ", response, command);
            }
        }

    }

    /**
     * Handles notification messages (*SN) and enquiry responses (*SE->*SA)
     *
     * @param m a non-null matcher
     * @param resp a possibly null, possibly empty response
     */
    private void handleNotification(Matcher m, String response) {
        if (m.groupCount() == 3) {
            final String cmd = m.group(2);
            final String parms = m.group(3);

            if (POWER.equalsIgnoreCase(cmd)) {
                handlePowerNotification(parms);
            } else if (VOLUME.equalsIgnoreCase(cmd)) {
                handleAudioVolumeNotification(parms);
            } else if (AUDIO_MUTE.equalsIgnoreCase(cmd)) {
                handleAudioMuteNotification(parms);
            } else if (CHANNEL.equalsIgnoreCase(cmd)) {
                handleChannelNotification(parms);
            } else if (TRIPLET_CHANNEL.equalsIgnoreCase(cmd)) {
                handleTripletChannelResponse(parms);
            } else if (INPUT_SOURCE.equalsIgnoreCase(cmd)) {
                handleInputSourceResponse(parms);
            } else if (INPUT.equalsIgnoreCase(cmd)) {
                handleInputNotification(parms);
            } else if (PICTURE_MUTE.equalsIgnoreCase(cmd)) {
                handlePictureMuteNotification(parms);
            } else if (PICTURE_IN_PICTURE.equalsIgnoreCase(cmd)) {
                handlePictureInPictureNotification(parms);
            } else if (BROADCAST_ADDRESS.equalsIgnoreCase(cmd)) {
                handleBroadcastAddressResponse(parms);
            } else if (MAC_ADDRESS.equalsIgnoreCase(cmd)) {
                handleMacAddressResponse(parms);
            } else {
                logger.warn("Unknown notification: " + response);
            }
        }

    }

    /**
     * Handles the IRCC commadn response
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleIRResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.warn(IRCC + " command failed: " + parms);
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else {
            logger.warn("Unknown " + IRCC + " response: " + parms);
        }
    }

    /**
     * Handles the POWR command response
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handlePowerResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.warn(POWER + " command failed: " + parms);
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else {
            logger.warn("Unknown " + POWER + " response: " + parms);
        }
    }

    /**
     * Handles the power notification/enquiry response
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handlePowerNotification(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.warn("Error issuing power");
        } else {
            try {
                final int power = Integer.parseInt(parms);
                if (power == 0) {
                    _callback.stateChanged(BraviaConstants.CHANNEL_POWER, OnOffType.OFF);
                } else if (power == 1) {
                    _callback.stateChanged(BraviaConstants.CHANNEL_POWER, OnOffType.ON);
                } else {
                    logger.warn("Unknown power response: " + parms);
                }
            } catch (NumberFormatException e) {
                logger.warn("Unparsable power response: " + parms);
            }

            refreshState();
        }
    }

    /**
     * Handles the audio volume command response
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleAudioVolumeResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.warn(VOLUME + " command failed: " + parms);
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else {
            logger.warn("Unknown " + VOLUME + " response: " + parms);
        }
    }

    /**
     * Handles the audio volume notification/enquiry response
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleAudioVolumeNotification(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.warn("Error issuing audio volume");
        } else {
            try {
                final int volume = Integer.parseInt(parms);
                _callback.stateChanged(BraviaConstants.CHANNEL_VOLUME, new PercentType(volume));
            } catch (NumberFormatException e) {
                logger.warn("Unparsable audio volume response: " + parms);
            }
        }
    }

    /**
     * Handles the audio mute command response
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleAudioMuteResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.warn(AUDIO_MUTE + " command failed: " + parms);
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else {
            logger.warn("Unknown " + AUDIO_MUTE + " response: " + parms);
        }
    }

    /**
     * Handles the audio mute notification/enquiry response
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleAudioMuteNotification(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.warn("Error issuing channel");
        } else {
            try {
                final int mute = Integer.parseInt(parms);
                if (mute == 0) {
                    _callback.stateChanged(BraviaConstants.CHANNEL_AUDIOMUTE, OnOffType.OFF);
                } else if (mute == 1) {
                    _callback.stateChanged(BraviaConstants.CHANNEL_AUDIOMUTE, OnOffType.ON);
                } else {
                    logger.warn("Unknown audio mute response: " + parms);
                }
            } catch (NumberFormatException e) {
                logger.warn("Unparsable audio mute response: " + parms);
            }
        }
    }

    /**
     * Handles the channel command response
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleChannelResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            _callback.stateChanged(BraviaConstants.CHANNEL_CHANNEL, new StringType(""));
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else if (RSP_NOSUCHTHING.equals(parms)) {
            _callback.stateChanged(BraviaConstants.CHANNEL_CHANNEL, new StringType(""));
        } else {
            logger.warn("Unknown " + CHANNEL + " response: " + parms);
        }
    }

    /**
     * Handles the channel notification/enquiry response
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleChannelNotification(String parms) {
        if (RSP_ERROR.equals(parms)) {
            _callback.stateChanged(BraviaConstants.CHANNEL_CHANNEL, new StringType(""));
        } else {
            try {
                final int pre = Integer.parseInt(parms.substring(0, 8));
                final int post = Integer.parseInt(StringUtils.stripEnd(parms.substring(9), "0"));
                _callback.stateChanged(BraviaConstants.CHANNEL_CHANNEL, new StringType(pre + "." + post));
            } catch (NumberFormatException e) {
                logger.warn("Unparsable channel response: " + parms);
            }
        }
    }

    /**
     * Handles the triplet channel command response
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleTripletChannelResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            _callback.stateChanged(BraviaConstants.CHANNEL_TRIPLETCHANNEL, new StringType(""));
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else if (RSP_NOSUCHTHING.equals(parms)) {
            _callback.stateChanged(BraviaConstants.CHANNEL_TRIPLETCHANNEL, new StringType(""));
            logger.warn(TRIPLET_CHANNEL + " response is no such channel: " + parms);
        } else {
            try {
                final int first = Integer.parseInt(parms.substring(0, 4), 16);
                final int second = Integer.parseInt(parms.substring(4, 8), 16);
                final int third = Integer.parseInt(StringUtils.stripEnd(parms.substring(9, 13), "#"), 16);

                _callback.stateChanged(BraviaConstants.CHANNEL_TRIPLETCHANNEL,
                        new StringType(first + "." + second + "." + third));
            } catch (NumberFormatException e) {
                logger.warn("Unparsable triplet channel response: " + parms);
            }
        }
    }

    /**
     * Handles the input source command response/notification
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleInputSourceResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            _callback.stateChanged(BraviaConstants.CHANNEL_INPUTSOURCE, new StringType(""));
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else if (RSP_NOSUCHTHING.equals(parms)) {
            _callback.stateChanged(BraviaConstants.CHANNEL_INPUTSOURCE, new StringType(""));
        } else {
            final int del = parms.indexOf('#');
            if (del >= 0) {
                parms = parms.substring(0, del);
            }
            _callback.stateChanged(BraviaConstants.CHANNEL_INPUTSOURCE, new StringType(parms));
        }
    }

    /**
     * Handles the input command response
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleInputResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.warn(INPUT + " command failed: " + parms);
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else if (RSP_NOSUCHTHING.equals(parms)) {
            logger.warn(INPUT + " response is no such input: " + parms);
        } else {
            logger.warn("Unknown " + INPUT + " response: " + parms);
        }
    }

    /**
     * Handles the input notification/enquiry response
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleInputNotification(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.warn("Error issuing input");
        } else {
            try {
                final int inputType = Integer.parseInt(parms.substring(0, 12));
                final int portNbr = Integer.parseInt(parms.substring(13));

                final String inputName = _inputTypes.get(inputType);
                if (inputName == null) {
                    logger.warn("Unknown input name for code: " + parms);
                } else {
                    _callback.stateChanged(BraviaConstants.CHANNEL_INPUT,
                            new StringType(inputName + (portNbr > 0 ? portNbr : "")));

                }
            } catch (NumberFormatException e) {
                logger.warn("Unparsable input response: " + parms);
            }
        }
    }

    /**
     * Handles the picture mute command response
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handlePictureMuteResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.warn(PICTURE_MUTE + " command failed: " + parms);
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else {
            logger.warn("Unknown " + PICTURE_MUTE + " response: " + parms);
        }
    }

    /**
     * Handles the picture mute notification/enquiry response
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handlePictureMuteNotification(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.warn("Error picture mute channel");
        } else {
            try {
                final int mute = Integer.parseInt(parms);
                if (mute == 0) {
                    _callback.stateChanged(BraviaConstants.CHANNEL_PICTUREMUTE, OnOffType.OFF);
                } else if (mute == 1) {
                    _callback.stateChanged(BraviaConstants.CHANNEL_PICTUREMUTE, OnOffType.ON);
                } else {
                    logger.warn("Unknown picture mute response: " + parms);
                }
            } catch (NumberFormatException e) {
                logger.warn("Unparsable picture mute response: " + parms);
            }
        }
    }

    /**
     * Handles the toggle picture mute command response
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleTogglePictureMuteResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.warn(TOGGLE_PICTURE_MUTE + " command failed: " + parms);
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else {
            logger.warn("Unknown " + TOGGLE_PICTURE_MUTE + " response: " + parms);
        }
    }

    /**
     * Handles the PIP command response
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handlePictureInPictureResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.warn(PICTURE_IN_PICTURE + " command failed: " + parms);
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else {
            logger.warn("Unknown " + PICTURE_IN_PICTURE + " response: " + parms);
        }
    }

    /**
     * Handles the PIP notification/enquiry response
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handlePictureInPictureNotification(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.warn("Error PIP channel");
        } else {
            try {
                final int enabled = Integer.parseInt(parms);
                if (enabled == 0) {
                    _callback.stateChanged(BraviaConstants.CHANNEL_PICTUREINPICTURE, OnOffType.OFF);
                } else if (enabled == 1) {
                    _callback.stateChanged(BraviaConstants.CHANNEL_PICTUREINPICTURE, OnOffType.ON);
                } else {
                    logger.warn("Unknown PIP response: " + parms);
                }
            } catch (NumberFormatException e) {
                logger.warn("Unparsable PIP response: " + parms);
            }
        }
    }

    /**
     * Handles the toggle PIP command response
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleTogglePictureInPictureResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.warn(TOGGLE_PICTURE_IN_PICTURE + " command failed: " + parms);
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else {
            logger.warn("Unknown " + TOGGLE_PICTURE_IN_PICTURE + " response: " + parms);
        }
    }

    /**
     * Handles the toggle PIP position command response
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleTogglePIPPosition(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.warn(TOGGLE_PIP_POSITION + " command failed: " + parms);
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else {
            logger.warn("Unknown " + TOGGLE_PIP_POSITION + " response: " + parms);
        }
    }

    /**
     * Handles the broadcast enquiry response
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleBroadcastAddressResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.warn("Error broadcast address");
        } else {
            final int del = parms.indexOf('#');
            if (del >= 0) {
                parms = parms.substring(0, del);
            }
            _callback.stateChanged(BraviaConstants.CHANNEL_BROADCASTADDRESS, new StringType(parms));
        }
    }

    /**
     * Handles the mac address enquiry response
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleMacAddressResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.warn("Error mac address");
        } else {
            final StringBuffer sb = new StringBuffer();

            final int max = parms.length();
            for (int x = 0; x < max; x++) {
                char myChar = parms.charAt(x);
                if (myChar == '#') {
                    break;
                }
                if (x > 0 && x % 2 == 0) {
                    sb.append(':');
                }
                sb.append(myChar);
            }
            _callback.stateChanged(BraviaConstants.CHANNEL_MACADDRESS, new StringType(sb.toString()));
        }
    }

    /**
     * Implements {@link SocketSessionListener#responseReceived(String)} to try to process the notification from the
     * bravia system.
     *
     * @param a possibly null, possibly empty response
     */
    @Override
    public void responseReceived(String response) {
        if (response == null || response.equals("")) {
            return;
        }

        final Matcher m = RSP_NOTIFICATION.matcher(response);
        if (m.matches()) {
            handleNotification(m, response);
            return;
        }

        logger.warn("Unparsable notification: " + response);
    }

    /**
     * Implements {@link SocketSessionListener#responseException(Exception)} to try to process the exception from the
     * bravia system. Will simply take the thing offline because of the exception
     *
     * @param a possibly null, possibly empty response
     */
    @Override
    public void responseException(Exception e) {
        _callback.statusChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Exception occurred reading from the socket: " + e);
    }

    /**
     * Disposes of this protocol handler by removing ourselves from the
     * {@link SocketSession#removeListener(SocketSessionListener)} and disconnecting the {@link #_listeningSession}
     *
     * @throws IOException
     */
    public void dispose() throws IOException {
        _listeningSession.removeListener(this);
        _listeningSession.disconnect();
    }
}
