/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.simpleip;

import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.ConfigConstants;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.transform.TransformationException;
import org.eclipse.smarthome.core.transform.TransformationHelper;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.openhab.binding.sony.internal.ThingCallback;
import org.openhab.binding.sony.internal.net.NetUtilities;
import org.openhab.binding.sony.internal.net.SocketChannelSession;
import org.openhab.binding.sony.internal.net.SocketSession;
import org.openhab.binding.sony.internal.net.SocketSessionListener;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Auto-generated Javadoc
/**
 * This is the protocol handler for the Simple IP System. This handler will issue the protocol commands and will
 * process the responses from the Simple IP system. The Simple IP system is a little flacky and doesn't seem to handle
 * multiple commands in a single session. For this reason, we create a single {@link SocketSession} to listen for any
 * notifications (whose lifetime matches that of this handler) and then create separate {@link SocketSession} for each
 * request. Special care must be taken to differentiate between a Control request result and the Inquiry/Notification
 * results to avoid misinterpreting the result (the control "success" message will have all zeroes - which has a form
 * that matches some inquiry/notification results (like volume could be interpreted as 0!).
 *
 * @author Tim Roberts - Initial contribution
 *
 *
 */
class SimpleIpProtocol implements SocketSessionListener, AutoCloseable {

    /** The logger. */
    // Logger
    private Logger logger = LoggerFactory.getLogger(SimpleIpProtocol.class);

    /** The type control. */
    // Protocol Constants
    private final char TYPE_CONTROL = 'C';

    /** The type enquiry. */
    private final char TYPE_ENQUIRY = 'E';

    /** The Constant IRCC. */
    private final static String IRCC = "IRCC";

    /** The Constant POWER. */
    private final static String POWER = "POWR";

    /** The Constant VOLUME. */
    private final static String VOLUME = "VOLU";

    /** The Constant AUDIO_MUTE. */
    private final static String AUDIO_MUTE = "AMUT";

    /** The Constant CHANNEL. */
    private final static String CHANNEL = "CHNN";

    /** The Constant TRIPLET_CHANNEL. */
    private final static String TRIPLET_CHANNEL = "TCHN";

    /** The Constant INPUT_SOURCE. */
    private final static String INPUT_SOURCE = "ISRC";

    /** The Constant INPUT. */
    private final static String INPUT = "INPT";

    /** The Constant PICTURE_MUTE. */
    private final static String PICTURE_MUTE = "PMUT";

    /** The Constant TOGGLE_PICTURE_MUTE. */
    private final static String TOGGLE_PICTURE_MUTE = "TPMU";

    /** The Constant PICTURE_IN_PICTURE. */
    private final static String PICTURE_IN_PICTURE = "PIPI";

    /** The Constant TOGGLE_PICTURE_IN_PICTURE. */
    private final static String TOGGLE_PICTURE_IN_PICTURE = "TPIP";

    /** The Constant TOGGLE_PIP_POSITION. */
    private final static String TOGGLE_PIP_POSITION = "TPPP";

    /** The Constant BROADCAST_ADDRESS. */
    private final static String BROADCAST_ADDRESS = "BADR";

    /** The Constant MAC_ADDRESS. */
    private final static String MAC_ADDRESS = "MADR";

    /** The Constant NO_PARM. */
    private final static String NO_PARM = "################";

    /** The Constant PARM_SIZE. */
    // Size of the parameter area as defined by the spec
    private final static int PARM_SIZE = 16;

    /** The rsp success. */
    // Response strings/patterns
    private final String RSP_SUCCESS = "0000000000000000";

    /** The rsp error. */
    private final String RSP_ERROR = "FFFFFFFFFFFFFFFF";

    /** The rsp nosuchthing. */
    private final String RSP_NOSUCHTHING = "NNNNNNNNNNNNNNNN";

    /** The rsp notification. */
    private final Pattern RSP_NOTIFICATION = Pattern.compile("^\\*S([AN])(\\w{4})(.*{16})");

    /** The {@link SocketSession} that will listen for notifications. */
    private final SocketSession _listeningSession;

    /** The {@link SimpleIpConfig} for creating new {@link SocketSession}. */
    private final SimpleIpConfig _config;

    /** The {@link Simple IPHandlerCallback} that we can callback to set state and status. */
    private final ThingCallback<String> _callback;

    /** Constant representing the TV input id. */
    private final static int INPUT_TV = 0;

    /** The {@link BundleContext} used to retrieve the {@link TransformationService}. */
    private final BundleContext _bundleContext;

    /** Represents a lookup between the known input source types and their corresponding simple IP identifier. */
    private final static Map<Integer, String> _inputTypes = new HashMap<Integer, String>();
    static {
        _inputTypes.put(INPUT_TV, "TV");
        _inputTypes.put(10000, "HDMI");
        _inputTypes.put(20000, "SCART");
        _inputTypes.put(30000, "Composite");
        _inputTypes.put(40000, "Component");
        _inputTypes.put(50000, "Screen Mirroring");
        _inputTypes.put(60000, "PC RGB Input");
    }

    /**
     * Constructs the protocol handler from given parameters. This constructor will create the
     * {@link #_listeningSession} to listen to notifications sent by the Simple IP device (adding ourselfs as the
     * listener).
     *
     * @param config a non-null {@link SimpleIpConfig} (may be connected or disconnected)
     * @param bundleContext a non-null {@link BundleContext}
     * @param callback a non-null {@link RioHandlerCallback} to callback
     * @throws IOException Signals that an I/O exception has occurred.
     */
    SimpleIpProtocol(SimpleIpConfig config, BundleContext bundleContext, ThingCallback<String> callback)
            throws IOException {
        _config = config;
        _bundleContext = bundleContext;
        _callback = callback;

        _listeningSession = new SocketChannelSession(config.getIpAddress(), SimpleIpConstants.PORT);
        _listeningSession.addListener(this);
    }

    /**
     * Attempts to log into the system. The login will connect the {@link #_listeningSession} and immediately call
     * {@link #postLogin()} since there is no authentication mechanisms
     *
     * @return always null to indicate a successful login
     * @throws IOException if an exception occurs trying to connect our {@link #_listeningSession} or writing the
     *             command file
     */
    String login() throws IOException {
        _listeningSession.connect();
        return null;
    }

    /**
     * Post successful login stuff - mark us online!.
     *
     * @throws IOException if an IO exception occurs writing the map file
     */
    void postLogin() throws IOException {
        writeCommands();
        refreshBroadcastAddress(_config.getNetInterface());
        refreshMacAddress(_config.getNetInterface());

    }

    /**
     * Writes the commands to the commands map file if it doesn't exist.
     *
     * @throws IOException if an IO exception occurs writing the map file
     */
    private void writeCommands() throws IOException {
        final String cmdMap = _config.getCommandsMapFile();
        if (StringUtils.isEmpty(cmdMap)) {
            logger.debug("No command map defined - ignoring");
            return;
        }

        final String filePath = ConfigConstants.getConfigFolder() + File.separator
                + TransformationService.TRANSFORM_FOLDER_NAME + File.separator + cmdMap;
        Path file = Paths.get(filePath);
        if (file.toFile().exists()) {
            logger.info("Command map already defined - ignoring: {}", file);
            return;
        }

        logger.info("Writing remote commands to {}", file);
        Files.write(file, getDefaultCommands(), Charset.forName("UTF-8"));
    }

    /**
     * Called to refresh some of the state of the simple IP system (mainly state that either we won't get notifications
     * for
     * or whose state may commonly change due to remote actions.
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

        // SimpleIP seems to need each request on it's own socket - so provide that here.
        try {
            logger.debug("Sending '{}'", cmd);
            NetUtilities.sendSocketRequest(_config.getIpAddress(), SimpleIpConstants.PORT, cmd,
                    new SocketSessionListener() {
                        @Override
                        public boolean responseReceived(String response) {
                            // See if the response is valid
                            final Matcher m = RSP_NOTIFICATION.matcher(response);
                            if (m.matches() && m.groupCount() == 3) {

                                // make sure we only process responses for our command
                                if (m.group(1).equals("A") && m.group(2).equals(command)) {
                                    logger.debug("Send '{}' result: '{}'", cmd, response);
                                    if (type == TYPE_CONTROL) {
                                        handleResponse(m, response, cmd);
                                    } else if (type == TYPE_ENQUIRY) {
                                        handleNotification(m, response);
                                    } else {
                                        logger.error("Unknown command type: {}", cmd);
                                    }
                                    return true;
                                }
                            } else if (StringUtils.isEmpty(response)) {
                                logger.debug("Empty reponse (or unsupported command): '{}'", response, cmd);
                            } else {
                                logger.warn("Unparsable response '{}' to command '{}'", response, cmd);
                            }
                            return false;
                        }

                        @Override
                        public void responseException(Exception e) {
                            if (e instanceof SocketTimeoutException) {
                                logger.debug("(SocketTimeoutException) Response took too long - ignoring");
                            } else {
                                SimpleIpProtocol.this.responseException(e);
                            }

                        }
                    });
            // _listeningSession.sendCommand(cmd);
        } catch (IOException e) {
            _callback.statusChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Exception occurred sending command: " + e);
        }
    }

    /**
     * Pings the server with our ping command to keep the connection alive.
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
     * Refreshes the power status.
     */
    void refreshPower() {
        sendCommand(TYPE_ENQUIRY, POWER, NO_PARM);
    }

    /**
     * Refreshes the volume status.
     */
    void refreshVolume() {
        sendCommand(TYPE_ENQUIRY, VOLUME, NO_PARM);
    }

    /**
     * Refreshes the audio mute status.
     */
    void refreshAudioMute() {
        sendCommand(TYPE_ENQUIRY, AUDIO_MUTE, NO_PARM);
    }

    /**
     * Refreshes the channel.
     */
    void refreshChannel() {
        sendCommand(TYPE_ENQUIRY, CHANNEL, NO_PARM);
    }

    /**
     * Refreshes the channel triplet.
     */
    void refreshTripletChannel() {
        sendCommand(TYPE_ENQUIRY, TRIPLET_CHANNEL, NO_PARM);
    }

    /**
     * Refreshes the input source.
     */
    void refreshInputSource() {
        sendCommand(TYPE_ENQUIRY, INPUT_SOURCE, NO_PARM);
    }

    /**
     * Refreshes the input.
     */
    void refreshInput() {
        sendCommand(TYPE_ENQUIRY, INPUT, NO_PARM);
    }

    /**
     * Refreshes the picture mute.
     */
    void refreshPictureMute() {
        sendCommand(TYPE_ENQUIRY, PICTURE_MUTE, NO_PARM);
    }

    /**
     * Refreshes the PIP.
     */
    void refreshPictureInPicture() {
        sendCommand(TYPE_ENQUIRY, PICTURE_IN_PICTURE, NO_PARM);
    }

    /**
     * Refreshes the broadcast address.
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
     * Refreshes the mac address of the given network interface.
     *
     * @param interfce the interfce
     */
    void refreshMacAddress(String interfce) {
        if (interfce == null) {
            interfce = "eth0";
        }
        sendCommand(TYPE_ENQUIRY, MAC_ADDRESS, StringUtils.rightPad(interfce, PARM_SIZE, '#'));
    }

    /**
     * Sets the IRCC code.
     *
     * @param irCmd the new ir
     * @throws IllegalArgumentException if ircc is negative
     */
    void setIR(String irCmd) {
        if (StringUtils.isEmpty(irCmd)) {
            throw new IllegalArgumentException("irCmd must be specified");
        }

        final String cmdMap = _config.getCommandsMapFile();
        final TransformationService transformService = TransformationHelper.getTransformationService(_bundleContext,
                "MAP");
        if (transformService == null) {
            logger.error("Failed to get MAP transformation service; is bundle installed?");
            return;
        }

        String code;
        try {
            code = transformService.transform(cmdMap, irCmd);
        } catch (TransformationException e) {
            logger.error("Failed to transform {} using map file '{}', exception={}", irCmd, cmdMap, e.getMessage());
            return;
        }

        if (StringUtils.isEmpty(code)) {
            logger.warn("No entry for {} in map file '{}'", irCmd, cmdMap);
            return;
        }

        try {
            Integer.parseInt(code);
        } catch (NumberFormatException e) {
            logger.warn("The resulting code {} was not an integer - transformed from {}", code, irCmd);
        }

        logger.debug("Transformed {} with map file '{}' to {}", irCmd, cmdMap, code);

        sendCommand(TYPE_CONTROL, IRCC, StringUtils.leftPad(code, PARM_SIZE, '0'));
        refreshPower();
        refreshState();
    }

    /**
     * Sets the power on/off to the device.
     *
     * @param on true if on, false off
     */
    void setPower(boolean on) {
        sendCommand(TYPE_CONTROL, POWER, StringUtils.leftPad(on ? "1" : "0", PARM_SIZE, '0'));
    }

    /**
     * Sets the volume level.
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
     * Sets the audio mute.
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
     * @throws IllegalArgumentException if channel is not in the form of "x.x" or "x" where x is numeric
     */
    void setChannel(String channel) {
        if (channel == null || channel.trim().length() == 0) {
            throw new IllegalArgumentException("channel cannot be null or empty");
        }
        final int period = channel.indexOf('.');

        final String pre = StringUtils.trimToNull(period < 0 ? channel : channel.substring(0, period));
        final String post = StringUtils.trimToNull(period < 0 ? "0" : channel.substring(period + 1));
        try {
            final int preNum = pre == null ? 0 : Integer.parseInt(pre);
            final int postNum = post == null ? 0 : Integer.parseInt(post);
            final String cmd = StringUtils.leftPad(Integer.toString(preNum), 8, '0') + "."
                    + StringUtils.rightPad(Integer.toString(postNum), 7, '0');
            sendCommand(TYPE_CONTROL, CHANNEL, cmd);

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("channel could not be parsed: " + channel);
        }
    }

    /**
     * Sets the triplet channel. Channel must be in the form of "x.x.x" where x must be numeric
     *
     * @param channel the non-null, non-empty channel in the form of "x.x.x" (such as 32736.32736.1024)
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

        final String first = StringUtils.trimToNull(channel.substring(0, firstPeriod));
        final String second = StringUtils.trimToNull(channel.substring(firstPeriod + 1, secondPeriod));
        final String third = StringUtils.trimToNull(channel.substring(secondPeriod + 1));
        try {
            final int firstNum = first == null ? 0 : Integer.parseInt(first);
            final int secondNum = second == null ? 0 : Integer.parseInt(second);
            final int thirdNum = third == null ? 0 : Integer.parseInt(third);

            final String firstHex = StringUtils.leftPad(Integer.toHexString(firstNum), 4, '0');
            final String secondHex = StringUtils.leftPad(Integer.toHexString(secondNum), 4, '0');
            final String thirdHex = StringUtils.leftPad(Integer.toHexString(thirdNum), 4, '0');

            sendCommand(TYPE_CONTROL, CHANNEL, firstHex + secondHex + thirdHex + "####");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("channel could not be parsed: " + channel);
        }
    }

    /**
     * Sets the input source and will refresh channel and triplet channel afterwards. This must be a valid string
     * recognized by the simple IP device.
     *
     * @param source a non-null, non-empty input source
     * @throws IllegalArgumentException if source is null or empty
     */
    void setInputSource(String source) {
        if (source == null || source.trim().length() == 0) {
            throw new IllegalArgumentException("source cannot be null or empty");
        }
        sendCommand(TYPE_CONTROL, INPUT_SOURCE, StringUtils.rightPad(source, PARM_SIZE, '#'));
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
                if (typeCode != INPUT_TV) {
                    try {
                        String portS = StringUtils.trimToNull(input.substring(entry.getValue().length()));
                        portNbr = portS == null ? 0 : Integer.parseInt(portS);
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
    }

    /**
     * Sets the picture mute.
     *
     * @param on true for muted, false otherwise
     */
    void setPictureMute(boolean on) {
        sendCommand(TYPE_CONTROL, PICTURE_MUTE, StringUtils.leftPad(on ? "1" : "0", PARM_SIZE, '0'));
    }

    /**
     * Toggles the picture mute.
     */
    void togglePictureMute() {
        sendCommand(TYPE_CONTROL, TOGGLE_PICTURE_MUTE, NO_PARM);
    }

    /**
     * Sets the PIP enabling.
     *
     * @param on true to enable, false otherwise
     */
    void setPictureInPicture(boolean on) {
        sendCommand(TYPE_CONTROL, PICTURE_IN_PICTURE, StringUtils.leftPad(on ? "1" : "0", PARM_SIZE, '0'));
    }

    /**
     * Toggles PIP enabling.
     */
    void togglePictureInPicture() {
        sendCommand(TYPE_CONTROL, TOGGLE_PICTURE_IN_PICTURE, NO_PARM);
    }

    /**
     * Toggles the PIP position.
     */
    void togglePipPosition() {
        sendCommand(TYPE_CONTROL, TOGGLE_PIP_POSITION, NO_PARM);
    }

    /**
     * Handles control responses from commands (*SC->*SA).
     *
     * @param m a non-null matcher
     * @param response the response
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
     * Handles notification messages (*SN) and enquiry responses (*SE->*SA).
     *
     * @param m a non-null matcher
     * @param response the response
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
                handleTripletChannelNotification(parms);
            } else if (INPUT_SOURCE.equalsIgnoreCase(cmd)) {
                handleInputSourceNotification(parms);
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
                logger.warn("Unknown notification: {}", response);
            }
        }

    }

    /**
     * Handles the IRCC commadn response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleIRResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.debug("{} command failed: {}", IRCC, parms);
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else {
            logger.warn("Unknown {} response: {}", IRCC, parms);
        }
    }

    /**
     * Handles the POWR command response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handlePowerResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.debug("{} command failed: {}", POWER, parms);
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else {
            logger.warn("Unknown {} response: {}", POWER, parms);
        }
    }

    /**
     * Handles the power notification/enquiry response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handlePowerNotification(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.debug("Error issuing power");
        } else {
            try {
                final int power = StringUtils.isEmpty(parms) ? 0 : Integer.parseInt(parms);
                if (power == 0) {
                    _callback.stateChanged(SimpleIpConstants.CHANNEL_POWER, OnOffType.OFF);
                } else if (power == 1) {
                    _callback.stateChanged(SimpleIpConstants.CHANNEL_POWER, OnOffType.ON);
                } else {
                    logger.warn("Unknown power response: {}", parms);
                }
            } catch (NumberFormatException e) {
                logger.warn("Unparsable power response: {}", parms);
            }

            refreshState();
        }
    }

    /**
     * Handles the audio volume command response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleAudioVolumeResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.debug("{} command failed: {}", VOLUME, parms);
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else {
            logger.warn("Unknown {} response: {}", VOLUME, parms);
        }
    }

    /**
     * Handles the audio volume notification/enquiry response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleAudioVolumeNotification(String parms) {
        if (RSP_ERROR.equals(parms)) {
            // you'll get error when tv is off/muted
            _callback.stateChanged(SimpleIpConstants.CHANNEL_VOLUME, new PercentType(0));
        } else {
            try {
                final int volume = StringUtils.isEmpty(parms) ? 0 : Integer.parseInt(parms);
                _callback.stateChanged(SimpleIpConstants.CHANNEL_VOLUME, new PercentType(volume));
            } catch (NumberFormatException e) {
                logger.warn("Unparsable audio volume response: {}", parms);
            }
        }
    }

    /**
     * Handles the audio mute command response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleAudioMuteResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.debug("{} command failed: {}", AUDIO_MUTE, parms);
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else {
            logger.warn("Unknown {} response: {}", AUDIO_MUTE, parms);
        }
    }

    /**
     * Handles the audio mute notification/enquiry response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleAudioMuteNotification(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.debug("Error issuing channel");
        } else {
            try {
                final int mute = StringUtils.isEmpty(parms) ? 0 : Integer.parseInt(parms);
                if (mute == 0) {
                    _callback.stateChanged(SimpleIpConstants.CHANNEL_AUDIOMUTE, OnOffType.OFF);
                } else if (mute == 1) {
                    _callback.stateChanged(SimpleIpConstants.CHANNEL_AUDIOMUTE, OnOffType.ON);
                } else {
                    logger.warn("Unknown audio mute response: {}", parms);
                }
            } catch (NumberFormatException e) {
                logger.warn("Unparsable audio mute response: {}", parms);
            }
        }
    }

    /**
     * Handles the channel command response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleChannelResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            _callback.stateChanged(SimpleIpConstants.CHANNEL_CHANNEL, new StringType(""));
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else if (RSP_NOSUCHTHING.equals(parms)) {
            _callback.stateChanged(SimpleIpConstants.CHANNEL_CHANNEL, new StringType(""));
        } else {
            logger.warn("Unknown {} response: {}", CHANNEL, parms);
        }
    }

    /**
     * Handles the channel notification/enquiry response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleChannelNotification(String parms) {
        if (RSP_ERROR.equals(parms)) {
            _callback.stateChanged(SimpleIpConstants.CHANNEL_CHANNEL, new StringType(""));
        } else {
            try {
                final int idx = parms.indexOf('.');
                if (idx >= 0) {
                    final String preS = StringUtils.trimToNull(StringUtils.stripStart(parms.substring(0, idx), "0"));
                    final String postS = StringUtils.trimToNull(StringUtils.stripEnd(parms.substring(idx + 1), "0"));
                    final int pre = preS == null ? 0 : Integer.parseInt(preS);
                    final int post = postS == null ? 0 : Integer.parseInt(postS);
                    _callback.stateChanged(SimpleIpConstants.CHANNEL_CHANNEL, new StringType(pre + "." + post));
                } else {
                    logger.warn("Unparsable channel response: {}", parms);
                }
            } catch (NumberFormatException e) {
                logger.warn("Unparsable channel response: {}", parms);
            }
        }
    }

    /**
     * Handles the triplet channel command response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleTripletChannelResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            _callback.stateChanged(SimpleIpConstants.CHANNEL_TRIPLETCHANNEL, new StringType(""));
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else if (RSP_NOSUCHTHING.equals(parms)) {
            _callback.stateChanged(SimpleIpConstants.CHANNEL_TRIPLETCHANNEL, new StringType(""));
        } else {
            logger.warn("Unknown {} response: {}", TRIPLET_CHANNEL, parms);
        }
    }

    /**
     * Handles the triplet channel command response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleTripletChannelNotification(String parms) {
        if (RSP_ERROR.equals(parms)) {
            _callback.stateChanged(SimpleIpConstants.CHANNEL_TRIPLETCHANNEL, new StringType(""));
        } else {
            if (parms.length() >= 12) {
                try {
                    final String firstS = StringUtils.trimToNull(parms.substring(0, 4));
                    final String secondS = StringUtils.trimToNull(parms.substring(4, 8));
                    final String thirdS = StringUtils.trimToNull(StringUtils.stripEnd(parms.substring(9, 13), "#"));
                    final int first = firstS == null ? 0 : Integer.parseInt(firstS, 16);
                    final int second = secondS == null ? 0 : Integer.parseInt(secondS, 16);
                    final int third = thirdS == null ? 0 : Integer.parseInt(thirdS, 16);

                    _callback.stateChanged(SimpleIpConstants.CHANNEL_TRIPLETCHANNEL,
                            new StringType(first + "." + second + "." + third));

                } catch (NumberFormatException e) {
                    logger.warn("Unparsable triplet channel response: {}", parms);
                }
            } else {
                logger.warn("Unparsable triplet channel response: {}", parms);
            }
        }
    }

    /**
     * Handles the input command response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleInputSourceResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.debug("{} command failed: {}", INPUT_SOURCE, parms);
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else if (RSP_NOSUCHTHING.equals(parms)) {
            logger.warn("{} response is no such input: {}", INPUT_SOURCE, parms);
        } else {
            logger.warn("Unknown {} response: {}", INPUT_SOURCE, parms);
        }
    }

    /**
     * Handles the input source command response/notification.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleInputSourceNotification(String parms) {
        if (RSP_ERROR.equals(parms)) {
            _callback.stateChanged(SimpleIpConstants.CHANNEL_INPUTSOURCE, new StringType(""));
        } else {
            final int del = parms.indexOf('#');
            if (del >= 0) {
                parms = parms.substring(0, del);
            }
            _callback.stateChanged(SimpleIpConstants.CHANNEL_INPUTSOURCE, new StringType(parms));

            refreshChannel();
            refreshTripletChannel();
        }
    }

    /**
     * Handles the input command response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleInputResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.debug("{} command failed: {}", INPUT, parms);
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else if (RSP_NOSUCHTHING.equals(parms)) {
            logger.warn("{} response is no such input: {}", INPUT, parms);
        } else {
            logger.warn("Unknown {} response: {}", INPUT, parms);
        }
    }

    /**
     * Handles the input notification/enquiry response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleInputNotification(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.debug("Error issuing input");
        } else {
            if (parms.length() >= 13) {
                try {
                    final String inputS = StringUtils.trimToNull(parms.substring(0, 12));
                    final String portS = StringUtils.trimToNull(parms.substring(13));
                    final int inputType = inputS == null ? 0 : Integer.parseInt(inputS);
                    final int portNbr = portS == null ? 0 : Integer.parseInt(portS);

                    final String inputName = _inputTypes.get(inputType);
                    if (inputName == null) {
                        logger.warn("Unknown input name for code: {}", parms);
                    } else {
                        _callback.stateChanged(SimpleIpConstants.CHANNEL_INPUT,
                                new StringType(inputName + (inputType != INPUT_TV ? portNbr : "")));

                        refreshChannel();
                        refreshTripletChannel();
                        refreshInputSource();
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Unparsable input response: {}", parms);
                }
            } else {
                logger.warn("Unparsable input response: {}", parms);
            }
        }
    }

    /**
     * Handles the picture mute command response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handlePictureMuteResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.debug("{} command failed: {}", PICTURE_MUTE, parms);
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else {
            logger.warn("Unknown {} response: {}", PICTURE_MUTE, parms);
        }
    }

    /**
     * Handles the picture mute notification/enquiry response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handlePictureMuteNotification(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.debug("Error picture mute channel");
        } else {
            try {
                final int mute = StringUtils.isEmpty(parms) ? 0 : Integer.parseInt(parms);
                if (mute == 0) {
                    _callback.stateChanged(SimpleIpConstants.CHANNEL_PICTUREMUTE, OnOffType.OFF);
                } else if (mute == 1) {
                    _callback.stateChanged(SimpleIpConstants.CHANNEL_PICTUREMUTE, OnOffType.ON);
                } else {
                    logger.warn("Unknown picture mute response: {}", parms);
                }
            } catch (NumberFormatException e) {
                logger.warn("Unparsable picture mute response: {}", parms);
            }
        }
    }

    /**
     * Handles the toggle picture mute command response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleTogglePictureMuteResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.debug("{} command failed: {}", TOGGLE_PICTURE_MUTE, parms);
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else {
            logger.warn("Unknown {} response: {}", TOGGLE_PICTURE_MUTE, parms);
        }
    }

    /**
     * Handles the PIP command response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handlePictureInPictureResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.debug("{} command failed: {}", PICTURE_IN_PICTURE, parms);
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else {
            logger.warn("Unknown {} response: {}", PICTURE_IN_PICTURE, parms);
        }
    }

    /**
     * Handles the PIP notification/enquiry response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handlePictureInPictureNotification(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.debug("Error PIP channel");
        } else {
            try {
                final int enabled = StringUtils.isEmpty(parms) ? 0 : Integer.parseInt(parms);
                if (enabled == 0) {
                    _callback.stateChanged(SimpleIpConstants.CHANNEL_PICTUREINPICTURE, OnOffType.OFF);
                } else if (enabled == 1) {
                    _callback.stateChanged(SimpleIpConstants.CHANNEL_PICTUREINPICTURE, OnOffType.ON);
                } else {
                    logger.warn("Unknown PIP response: {}", parms);
                }
            } catch (NumberFormatException e) {
                logger.warn("Unparsable PIP response: {}", parms);
            }
        }
    }

    /**
     * Handles the toggle PIP command response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleTogglePictureInPictureResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.debug("{} command failed: {}", TOGGLE_PICTURE_IN_PICTURE, parms);
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else {
            logger.warn("Unknown {} response: {}", TOGGLE_PICTURE_IN_PICTURE, parms);
        }
    }

    /**
     * Handles the toggle PIP position command response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleTogglePIPPosition(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.debug("{} command failed: {}", TOGGLE_PIP_POSITION, parms);
        } else if (RSP_SUCCESS.equals(parms)) {
            // success!
        } else {
            logger.warn("Unknown {} response: {}", TOGGLE_PIP_POSITION, parms);
        }
    }

    /**
     * Handles the broadcast enquiry response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleBroadcastAddressResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.debug("Error broadcast address");
        } else {
            final int del = parms.indexOf('#');
            if (del >= 0) {
                parms = parms.substring(0, del);
            }
            _callback.setProperty(SimpleIpConstants.PROP_BROADCASTADDRESS, parms);
        }
    }

    /**
     * Handles the mac address enquiry response.
     *
     * @param parms a possibly null, possibly empty response
     */
    private void handleMacAddressResponse(String parms) {
        if (RSP_ERROR.equals(parms)) {
            logger.debug("Error mac address");
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
            _callback.setProperty(SimpleIpConstants.PROP_MACADDRESS, sb.toString());
        }
    }

    /**
     * Implements {@link SocketSessionListener#responseReceived(String)} to try to process the notification from the
     * simple IP system.
     *
     * @param response the response
     * @return true, if successful
     */
    @Override
    public boolean responseReceived(String response) {
        if (response == null || response.equals("")) {
            return true;
        }

        final Matcher m = RSP_NOTIFICATION.matcher(response);
        if (m.matches()) {
            handleNotification(m, response);
            return true;
        }

        logger.warn("Unparsable notification: {}", response);
        return true;
    }

    /**
     * Implements {@link SocketSessionListener#responseException(Exception)} to try to process the exception from the
     * simple IP system. Will simply take the thing offline because of the exception
     *
     * @param e the e
     */
    @Override
    public void responseException(Exception e) {
        _callback.statusChanged(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "Exception occurred reading from the socket: " + e);
    }

    /**
     * Helper method to simply return the default commands for Simple IP control.
     *
     * @return a non-null, non-empty {@link List<String>} of commands
     */
    private List<String> getDefaultCommands() {
        final List<String> cmds = new ArrayList<String>();
        // cmds.add("Power-Off=0"); - doesn't seem to be power-off
        cmds.add("Input=1");
        cmds.add("Guide=2");
        cmds.add("EPG=3");
        cmds.add("Favorites=4");
        cmds.add("Display=5");
        cmds.add("Home=6");
        cmds.add("Options=7");
        cmds.add("Return=8");
        cmds.add("Up=9");
        cmds.add("Down=10");
        cmds.add("Right=11");
        cmds.add("Left=12");
        cmds.add("Confirm=13");
        cmds.add("Red=14");
        cmds.add("Green=15");
        cmds.add("Yellow=16");
        cmds.add("Blue=17");
        cmds.add("Num1=18");
        cmds.add("Num2=19");
        cmds.add("Num3=20");
        cmds.add("Num4=21");
        cmds.add("Num5=22");
        cmds.add("Num6=23");
        cmds.add("Num7=24");
        cmds.add("Num8=25");
        cmds.add("Num9=26");
        cmds.add("Num0=27");
        cmds.add("Num11=28");
        cmds.add("Num12=29");
        cmds.add("Volume-Up=30");
        cmds.add("Volume-Down=31");
        cmds.add("Mute=32");
        cmds.add("Channel-Up=33");
        cmds.add("Channel-Down=34");
        cmds.add("Subtitle=35");
        cmds.add("Closed-Caption=36");
        cmds.add("Enter=37");
        cmds.add("DOT=38");
        cmds.add("Analog=39");
        cmds.add("Teletext=40");
        cmds.add("Exit=41");
        cmds.add("Analog2=42");
        cmds.add("*AD=43");
        cmds.add("Digital=44");
        cmds.add("Analog?=45");
        cmds.add("BS=46");
        cmds.add("CS=47");
        cmds.add("BS/CS=48");
        cmds.add("Ddata=49");
        cmds.add("Pic-Off=50");
        cmds.add("Tv_Radio=51");
        cmds.add("Theater=52");
        cmds.add("SEN=53");
        cmds.add("Internet-Widgets=54");
        cmds.add("Internet-Video=55");
        cmds.add("Netflix=56");
        cmds.add("Scene-Select=57");
        cmds.add("Model3D=58");
        cmds.add("iManual=59");
        cmds.add("Audio=60");
        cmds.add("Wide=61");
        cmds.add("Jump=62");
        cmds.add("PAP=63");
        cmds.add("MyEPG=64");
        cmds.add("Program-Description=65");
        cmds.add("Write-Chapter=66");
        cmds.add("TrackID=67");
        cmds.add("Ten-Key=68");
        cmds.add("AppliCast=69");
        cmds.add("acTVila=70");
        cmds.add("Delete-Video=71");
        cmds.add("Photo-Frame=72");
        cmds.add("TV-Pause=73");
        cmds.add("Key-Pad=74");
        cmds.add("Media=75");
        cmds.add("Sync-Menu=76");
        cmds.add("Forward=77");
        cmds.add("Play=78");
        cmds.add("Rewind=79");
        cmds.add("Prev=80");
        cmds.add("Stop=81");
        cmds.add("Next=82");
        cmds.add("Rec=83");
        cmds.add("Pause=84");
        cmds.add("Eject=85");
        cmds.add("Flash-Plus=86");
        cmds.add("Flash-Minus=87");
        cmds.add("Top-Menus=88");
        cmds.add("Popup-Menu=89");
        cmds.add("Rakuraku-Start=90");
        cmds.add("One-Touch-Time-Rec=91");
        cmds.add("One-Touch-View=92");
        cmds.add("One-Touch-Rec=93");
        cmds.add("One-Touch-Stop=94");
        cmds.add("DUX=95");
        cmds.add("Football-Mode=96");
        cmds.add("Social=97");
        cmds.add("Power=98");
        cmds.add("Hdmi1=100");
        cmds.add("Power-On=103");
        cmds.add("Power-Off=104");
        cmds.add("Composite1=107");
        return cmds;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.AutoCloseable#close()
     */
    @Override
    public void close() throws Exception {
        _listeningSession.removeListener(this);
        _listeningSession.disconnect();
    }
}
