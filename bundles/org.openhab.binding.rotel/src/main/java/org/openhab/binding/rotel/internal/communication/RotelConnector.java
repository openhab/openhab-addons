/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.rotel.internal.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rotel.internal.RotelException;
import org.openhab.binding.rotel.internal.RotelModel;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for communicating with the Rotel device
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public abstract class RotelConnector {

    private final Logger logger = LoggerFactory.getLogger(RotelConnector.class);

    public static final byte[] READ_ERROR = "read_error".getBytes(StandardCharsets.US_ASCII);

    protected static final byte START = (byte) 0xFE;

    // Message types
    public static final byte PRIMARY_CMD = (byte) 0x10;
    public static final byte MAIN_ZONE_CMD = (byte) 0x14;
    public static final byte RECORD_SRC_CMD = (byte) 0x15;
    public static final byte ZONE2_CMD = (byte) 0x16;
    public static final byte ZONE3_CMD = (byte) 0x17;
    public static final byte ZONE4_CMD = (byte) 0x18;
    public static final byte VOLUME_CMD = (byte) 0x30;
    public static final byte ZONE2_VOLUME_CMD = (byte) 0x32;
    public static final byte ZONE3_VOLUME_CMD = (byte) 0x33;
    public static final byte ZONE4_VOLUME_CMD = (byte) 0x34;
    private static final byte TRIGGER_CMD = (byte) 0x40;
    protected static final byte STANDARD_RESPONSE = (byte) 0x20;
    private static final byte TRIGGER_STATUS = (byte) 0x21;
    private static final byte SMART_DISPLAY_DATA_1 = (byte) 0x22;
    private static final byte SMART_DISPLAY_DATA_2 = (byte) 0x23;

    // Keys used by the HEX protocol
    private static final String KEY1_HEX_VOLUME = "volume ";
    private static final String KEY2_HEX_VOLUME = "vol ";
    private static final String KEY_HEX_MUTE = "mute ";
    private static final String KEY1_HEX_BASS = "bass ";
    private static final String KEY2_HEX_BASS = "lf ";
    private static final String KEY1_HEX_TREBLE = "treble ";
    private static final String KEY2_HEX_TREBLE = "hf ";
    private static final String KEY_HEX_MULTI_IN = "multi in ";
    private static final String KEY_HEX_STEREO = "stereo";
    private static final String KEY1_HEX_3CH = "3 stereo";
    private static final String KEY2_HEX_3CH = "dolby 3 stereo";
    private static final String KEY_HEX_5CH = "5ch stereo";
    private static final String KEY_HEX_7CH = "7ch stereo";
    private static final String KEY_HEX_MUSIC1 = "music 1";
    private static final String KEY_HEX_MUSIC2 = "music 2";
    private static final String KEY_HEX_MUSIC3 = "music 3";
    private static final String KEY_HEX_MUSIC4 = "music 4";
    private static final String KEY_HEX_DSP1 = "dsp 1";
    private static final String KEY_HEX_DSP2 = "dsp 2";
    private static final String KEY_HEX_DSP3 = "dsp 3";
    private static final String KEY_HEX_DSP4 = "dsp 4";
    private static final String KEY1_HEX_PROLOGIC = "prologic  emu";
    private static final String KEY2_HEX_PROLOGIC = "dolby pro logic";
    private static final String KEY1_HEX_PLII_CINEMA = "prologic  cin";
    private static final String KEY2_HEX_PLII_CINEMA = "dolby pl  c";
    private static final String KEY1_HEX_PLII_MUSIC = "prologic  mus";
    private static final String KEY2_HEX_PLII_MUSIC = "dolby pl  m";
    private static final String KEY1_HEX_PLII_GAME = "prologic  gam";
    private static final String KEY2_HEX_PLII_GAME = "dolby pl  g";
    private static final String KEY1_HEX_PLIIX_CINEMA = "pl x cinema";
    private static final String KEY2_HEX_PLIIX_CINEMA = "dolby pl x c";
    private static final String KEY1_HEX_PLIIX_MUSIC = "pl x music";
    private static final String KEY2_HEX_PLIIX_MUSIC = "dolby pl x m";
    private static final String KEY1_HEX_PLIIX_GAME = "pl x game";
    private static final String KEY2_HEX_PLIIX_GAME = "dolby pl x g";
    private static final String KEY_HEX_PLIIZ = "dolby pl z";
    private static final String KEY1_HEX_DTS_NEO6_CINEMA = "neo 6 cinema";
    private static final String KEY2_HEX_DTS_NEO6_CINEMA = "dts neo:6 c";
    private static final String KEY1_HEX_DTS_NEO6_MUSIC = "neo 6 music";
    private static final String KEY2_HEX_DTS_NEO6_MUSIC = "dts neo:6 m";
    private static final String KEY_HEX_DTS = "dts";
    private static final String KEY_HEX_DTS_ES = "dts-es";
    private static final String KEY_HEX_DTS_96 = "dts 96";
    private static final String KEY_HEX_DD = "dolby digital";
    private static final String KEY_HEX_DD_EX = "dolby d ex";
    private static final String KEY_HEX_PCM = "pcm";
    private static final String KEY_HEX_LPCM = "lpcm";
    private static final String KEY_HEX_MPEG = "mpeg";
    private static final String KEY_HEX_BYPASS = "bypass";
    private static final String KEY1_HEX_ZONE2 = "zone ";
    private static final String KEY2_HEX_ZONE2 = "zone2 ";
    private static final String KEY_HEX_ZONE3 = "zone3 ";
    private static final String KEY_HEX_ZONE4 = "zone4 ";
    private static final String KEY_HEX_RECORD = "rec ";

    // Keys used by the ASCII protocol
    public static final String KEY_UPDATE_MODE = "update_mode";
    public static final String KEY_DISPLAY_UPDATE = "display_update";
    public static final String KEY_POWER = "power";
    public static final String KEY_VOLUME_MIN = "volume_min";
    public static final String KEY_VOLUME_MAX = "volume_max";
    public static final String KEY_VOLUME = "volume";
    public static final String KEY_MUTE = "mute";
    public static final String KEY_TONE_MAX = "tone_max";
    public static final String KEY_BASS = "bass";
    public static final String KEY_TREBLE = "treble";
    public static final String KEY_SOURCE = "source";
    public static final String KEY1_PLAY_STATUS = "play_status";
    public static final String KEY2_PLAY_STATUS = "status";
    public static final String KEY_TRACK = "track";
    public static final String KEY_DSP_MODE = "dsp_mode";
    public static final String KEY_DIMMER = "dimmer";
    public static final String KEY_FREQ = "freq";

    // Special keys used by the binding
    public static final String KEY_LINE1 = "line1";
    public static final String KEY_LINE2 = "line2";
    public static final String KEY_RECORD = "record";
    public static final String KEY_RECORD_SEL = "record_sel";
    public static final String KEY_ZONE = "zone";
    public static final String KEY_POWER_ZONE2 = "power_zone2";
    public static final String KEY_POWER_ZONE3 = "power_zone3";
    public static final String KEY_POWER_ZONE4 = "power_zone4";
    public static final String KEY_SOURCE_ZONE2 = "source_zone2";
    public static final String KEY_SOURCE_ZONE3 = "source_zone3";
    public static final String KEY_SOURCE_ZONE4 = "source_zone4";
    public static final String KEY_VOLUME_ZONE2 = "volume_zone2";
    public static final String KEY_VOLUME_ZONE3 = "volume_zone3";
    public static final String KEY_VOLUME_ZONE4 = "volume_zone4";
    public static final String KEY_MUTE_ZONE2 = "mute_zone2";
    public static final String KEY_MUTE_ZONE3 = "mute_zone3";
    public static final String KEY_MUTE_ZONE4 = "mute_zone4";
    public static final String KEY_ERROR = "error";

    public static final String MSG_VALUE_OFF = "off";
    public static final String MSG_VALUE_ON = "on";
    public static final String POWER_ON = "on";
    public static final String STANDBY = "standby";
    public static final String POWER_OFF_DELAYED = "off_delayed";
    protected static final String AUTO = "auto";
    protected static final String MANUAL = "manual";
    public static final String MSG_VALUE_MIN = "min";
    public static final String MSG_VALUE_MAX = "max";
    public static final String MSG_VALUE_FIX = "fix";
    public static final String PLAY = "play";
    public static final String PAUSE = "pause";
    public static final String STOP = "stop";
    private static final String SOURCE = "source";

    private RotelModel model;
    private RotelProtocol protocol;
    protected Map<RotelSource, String> sourcesLabels;
    private boolean simu;

    /** The output stream */
    protected @Nullable OutputStream dataOut;

    /** The input stream */
    protected @Nullable InputStream dataIn;

    /** true if the connection is established, false if not */
    private boolean connected;

    protected String readerThreadName;
    private @Nullable Thread readerThread;

    private List<RotelMessageEventListener> listeners = new ArrayList<>();

    /** Special characters that can be found in the feedback messages for several devices using the ASCII protocol */
    public static final byte[][] SPECIAL_CHARACTERS = { { (byte) 0xEE, (byte) 0x82, (byte) 0x85 },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x84 }, { (byte) 0xEE, (byte) 0x82, (byte) 0x92 },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x87 }, { (byte) 0xEE, (byte) 0x82, (byte) 0x8E },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x89 }, { (byte) 0xEE, (byte) 0x82, (byte) 0x93 },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x8C }, { (byte) 0xEE, (byte) 0x82, (byte) 0x8F },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x8A }, { (byte) 0xEE, (byte) 0x82, (byte) 0x8B },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x81 }, { (byte) 0xEE, (byte) 0x82, (byte) 0x82 },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x83 }, { (byte) 0xEE, (byte) 0x82, (byte) 0x94 },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x97 }, { (byte) 0xEE, (byte) 0x82, (byte) 0x98 },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x80 }, { (byte) 0xEE, (byte) 0x82, (byte) 0x99 },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x9A }, { (byte) 0xEE, (byte) 0x82, (byte) 0x88 },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x95 }, { (byte) 0xEE, (byte) 0x82, (byte) 0x96 },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x90 }, { (byte) 0xEE, (byte) 0x82, (byte) 0x91 },
            { (byte) 0xEE, (byte) 0x82, (byte) 0x8D }, { (byte) 0xEE, (byte) 0x80, (byte) 0x80, (byte) 0xEE,
                    (byte) 0x80, (byte) 0x81, (byte) 0xEE, (byte) 0x80, (byte) 0x82 } };

    /** Special characters that can be found in the feedback messages for the RCD-1572 */
    public static final byte[][] SPECIAL_CHARACTERS_RCD1572 = { { (byte) 0xC2, (byte) 0x8C },
            { (byte) 0xC2, (byte) 0x54 }, { (byte) 0xC2, (byte) 0x81 }, { (byte) 0xC2, (byte) 0x82 },
            { (byte) 0xC2, (byte) 0x83 } };

    /** Empty table of special characters */
    public static final byte[][] NO_SPECIAL_CHARACTERS = {};

    /**
     * Constructor
     *
     * @param model the Rotel model in use
     * @param protocol the protocol to be used
     * @param simu whether the communication is simulated or real
     * @param readerThreadName the name of thread to be created
     */
    public RotelConnector(RotelModel model, RotelProtocol protocol, Map<RotelSource, String> sourcesLabels,
            boolean simu, String readerThreadName) {
        this.model = model;
        this.protocol = protocol;
        this.sourcesLabels = sourcesLabels;
        this.simu = simu;
        this.readerThreadName = readerThreadName;
    }

    /**
     * Get the Rotel model
     *
     * @return the model
     */
    public RotelModel getModel() {
        return model;
    }

    /**
     * Get the protocol to be used
     *
     * @return the protocol
     */
    public RotelProtocol getProtocol() {
        return protocol;
    }

    /**
     * Get whether the connection is established or not
     *
     * @return true if the connection is established
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Set whether the connection is established or not
     *
     * @param connected true if the connection is established
     */
    protected void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * Set the thread that handles the feedback messages
     *
     * @param readerThread the thread
     */
    protected void setReaderThread(Thread readerThread) {
        this.readerThread = readerThread;
    }

    /**
     * Open the connection with the Rotel device
     *
     * @throws RotelException - In case of any problem
     */
    public abstract void open() throws RotelException;

    /**
     * Close the connection with the Rotel device
     */
    public abstract void close();

    /**
     * Stop the thread that handles the feedback messages and close the opened input and output streams
     */
    protected void cleanup() {
        Thread readerThread = this.readerThread;
        if (readerThread != null) {
            readerThread.interrupt();
            try {
                readerThread.join();
            } catch (InterruptedException e) {
            }
            this.readerThread = null;
        }
        OutputStream dataOut = this.dataOut;
        if (dataOut != null) {
            try {
                dataOut.close();
            } catch (IOException e) {
            }
            this.dataOut = null;
        }
        InputStream dataIn = this.dataIn;
        if (dataIn != null) {
            try {
                dataIn.close();
            } catch (IOException e) {
            }
            this.dataIn = null;
        }
    }

    /**
     * Reads some number of bytes from the input stream and stores them into the buffer array b. The number of bytes
     * actually read is returned as an integer.
     *
     * @param dataBuffer the buffer into which the data is read.
     *
     * @return the total number of bytes read into the buffer, or -1 if there is no more data because the end of the
     *         stream has been reached.
     *
     * @throws RotelException - If the input stream is null, if the first byte cannot be read for any reason
     *             other than the end of the file, if the input stream has been closed, or if some other I/O error
     *             occurs.
     * @throws InterruptedIOException - if the thread was interrupted during the reading of the input stream
     */
    protected int readInput(byte[] dataBuffer) throws RotelException, InterruptedIOException {
        if (simu) {
            throw new RotelException("readInput failed: should not be called in simu mode");
        }
        InputStream dataIn = this.dataIn;
        if (dataIn == null) {
            throw new RotelException("readInput failed: input stream is null");
        }
        try {
            return dataIn.read(dataBuffer);
        } catch (IOException e) {
            logger.debug("readInput failed: {}", e.getMessage());
            throw new RotelException("readInput failed", e);
        }
    }

    /**
     * Request the Rotel device to execute a command
     *
     * @param cmd the command to execute
     *
     * @throws RotelException - In case of any problem
     */
    public void sendCommand(RotelCommand cmd) throws RotelException {
        sendCommand(cmd, null);
    }

    /**
     * Request the Rotel device to execute a command
     *
     * @param cmd the command to execute
     * @param value the integer value to consider for volume, bass or treble adjustment
     *
     * @throws RotelException - In case of any problem
     */
    public void sendCommand(RotelCommand cmd, @Nullable Integer value) throws RotelException {
        String messageStr;
        byte[] message = new byte[0];
        switch (protocol) {
            case HEX:
                if (cmd.getHexType() == 0) {
                    logger.debug("Send comman \"{}\" ignored: not available for HEX protocol", cmd.getName());
                    return;
                } else {
                    final int size = 6;
                    message = new byte[size];
                    int idx = 0;
                    message[idx++] = START;
                    message[idx++] = 3;
                    message[idx++] = model.getDeviceId();
                    message[idx++] = cmd.getHexType();
                    message[idx++] = (value == null) ? cmd.getHexKey() : (byte) (value & 0x000000FF);
                    final byte checksum = computeCheckSum(message, idx - 1);
                    if ((checksum & 0x000000FF) == 0x000000FD || (checksum & 0x000000FF) == 0x000000FE) {
                        message = Arrays.copyOf(message, size + 1);
                        message[idx++] = (byte) 0xFD;
                        message[idx++] = ((checksum & 0x000000FF) == 0x000000FD) ? (byte) 0 : (byte) 1;
                    } else {
                        message[idx++] = checksum;
                    }
                    logger.debug("Send command \"{}\" => {}", cmd.getName(), HexUtils.bytesToHex(message));
                }
                break;
            case ASCII_V1:
                messageStr = cmd.getAsciiCommandV1();
                if (messageStr == null) {
                    logger.debug("Send comman \"{}\" ignored: not available for ASCII V1 protocol", cmd.getName());
                    return;
                } else {
                    if (value != null) {
                        switch (cmd) {
                            case VOLUME_SET:
                                messageStr += String.format("%d", value);
                                break;
                            case BASS_SET:
                            case TREBLE_SET:
                                if (value == 0) {
                                    messageStr += "000";
                                } else if (value > 0) {
                                    messageStr += String.format("+%02d", value);
                                } else {
                                    messageStr += String.format("-%02d", -value);
                                }
                                break;
                            case DIMMER_LEVEL_SET:
                                if (value > 0 && model.getDimmerLevelMin() < 0) {
                                    messageStr += String.format("+%d", value);
                                } else {
                                    messageStr += String.format("%d", value);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    if (!messageStr.endsWith("?")) {
                        messageStr += "!";
                    }
                    message = messageStr.getBytes(StandardCharsets.US_ASCII);
                    logger.debug("Send command \"{}\" => {}", cmd.getName(), messageStr);
                }
                break;
            case ASCII_V2:
                messageStr = cmd.getAsciiCommandV2();
                if (messageStr == null) {
                    logger.debug("Send comman \"{}\" ignored: not available for ASCII V2 protocol", cmd.getName());
                    return;
                } else {
                    if (value != null) {
                        switch (cmd) {
                            case VOLUME_SET:
                                messageStr += String.format("%02d", value);
                                break;
                            case BASS_SET:
                            case TREBLE_SET:
                                if (value == 0) {
                                    messageStr += "000";
                                } else if (value > 0) {
                                    messageStr += String.format("+%02d", value);
                                } else {
                                    messageStr += String.format("-%02d", -value);
                                }
                                break;
                            case DIMMER_LEVEL_SET:
                                if (value > 0 && model.getDimmerLevelMin() < 0) {
                                    messageStr += String.format("+%d", value);
                                } else {
                                    messageStr += String.format("%d", value);
                                }
                                break;
                            default:
                                break;
                        }
                    }
                    if (!messageStr.endsWith("?")) {
                        messageStr += "!";
                    }
                    message = messageStr.getBytes(StandardCharsets.US_ASCII);
                    logger.debug("Send command \"{}\" => {}", cmd.getName(), messageStr);
                }
                break;
        }
        if (simu) {
            return;
        }
        OutputStream dataOut = this.dataOut;
        if (dataOut == null) {
            throw new RotelException("Send command \"" + cmd.getName() + "\" failed: output stream is null");
        }
        try {
            dataOut.write(message);
            dataOut.flush();
        } catch (IOException e) {
            logger.debug("Send command \"{}\" failed: {}", cmd.getName(), e.getMessage());
            throw new RotelException("Send command \"" + cmd.getName() + "\" failed", e);
        }
        logger.debug("Send command \"{}\" succeeded", cmd.getName());
    }

    /**
     * Validate the content of a feedback message
     *
     * @param responseMessage the buffer containing the feedback message
     *
     * @throws RotelException - If the message has unexpected content
     */
    private void validateResponse(byte[] responseMessage) throws RotelException {
        if (protocol == RotelProtocol.HEX) {
            // Check minimum message length
            if (responseMessage.length < 6) {
                logger.debug("Unexpected message length: {}", responseMessage.length);
                throw new RotelException("Unexpected message length");
            }

            // Check START
            if (responseMessage[0] != START) {
                logger.debug("Unexpected START in response: {} rather than {}",
                        Integer.toHexString(responseMessage[0] & 0x000000FF), Integer.toHexString(START & 0x000000FF));
                throw new RotelException("Unexpected START in response");
            }

            // Check ID
            if (responseMessage[2] != model.getDeviceId()) {
                logger.debug("Unexpected ID in response: {} rather than {}",
                        Integer.toHexString(responseMessage[2] & 0x000000FF),
                        Integer.toHexString(model.getDeviceId() & 0x000000FF));
                throw new RotelException("Unexpected ID in response");
            }

            // Check TYPE
            if (responseMessage[3] != STANDARD_RESPONSE && responseMessage[3] != TRIGGER_STATUS
                    && responseMessage[3] != SMART_DISPLAY_DATA_1 && responseMessage[3] != SMART_DISPLAY_DATA_2
                    && responseMessage[3] != PRIMARY_CMD && responseMessage[3] != MAIN_ZONE_CMD
                    && responseMessage[3] != RECORD_SRC_CMD && responseMessage[3] != ZONE2_CMD
                    && responseMessage[3] != ZONE3_CMD && responseMessage[3] != ZONE4_CMD
                    && responseMessage[3] != VOLUME_CMD && responseMessage[3] != ZONE2_VOLUME_CMD
                    && responseMessage[3] != ZONE3_VOLUME_CMD && responseMessage[3] != ZONE4_VOLUME_CMD
                    && responseMessage[3] != TRIGGER_CMD) {
                logger.debug("Unexpected TYPE in response: {}", Integer.toHexString(responseMessage[3] & 0x000000FF));
                throw new RotelException("Unexpected TYPE in response");
            }

            int expectedLen = (responseMessage[3] == STANDARD_RESPONSE)
                    ? (5 + model.getRespNbChars() + model.getRespNbFlags())
                    : responseMessage.length;

            // Check COUNT
            if (responseMessage[1] != (expectedLen - 3)) {
                logger.debug("Unexpected COUNT in response: {} rather than {}",
                        Integer.toHexString(responseMessage[1] & 0x000000FF),
                        Integer.toHexString((expectedLen - 3) & 0x000000FF));
                throw new RotelException("Unexpected COUNT in response");
            }

            final byte checksum = computeCheckSum(responseMessage, expectedLen - 2);
            if ((checksum & 0x000000FF) == 0x000000FD || (checksum & 0x000000FF) == 0x000000FE) {
                expectedLen++;
            }

            // Check message length
            if (responseMessage.length != expectedLen) {
                logger.debug("Unexpected message length: {} rather than {}", responseMessage.length, expectedLen);
                throw new RotelException("Unexpected message length");
            }

            // Check sum
            if ((checksum & 0x000000FF) == 0x000000FD) {
                if ((responseMessage[responseMessage.length - 2] & 0x000000FF) != 0x000000FD
                        || (responseMessage[responseMessage.length - 1] & 0x000000FF) != 0) {
                    logger.debug("Invalid check sum in response: {} rather than FD00", HexUtils.bytesToHex(
                            Arrays.copyOfRange(responseMessage, responseMessage.length - 2, responseMessage.length)));
                    throw new RotelException("Invalid check sum in response");
                }
            } else if ((checksum & 0x000000FF) == 0x000000FE) {
                if ((responseMessage[responseMessage.length - 2] & 0x000000FF) != 0x000000FD
                        || (responseMessage[responseMessage.length - 1] & 0x000000FF) != 1) {
                    logger.debug("Invalid check sum in response: {} rather than FD01", HexUtils.bytesToHex(
                            Arrays.copyOfRange(responseMessage, responseMessage.length - 2, responseMessage.length)));
                    throw new RotelException("Invalid check sum in response");
                }
            } else if ((checksum & 0x000000FF) != (responseMessage[responseMessage.length - 1] & 0x000000FF)) {
                logger.debug("Invalid check sum in response: {} rather than {}",
                        Integer.toHexString(responseMessage[responseMessage.length - 1] & 0x000000FF),
                        Integer.toHexString(checksum & 0x000000FF));
                throw new RotelException("Invalid check sum in response");
            }
        } else {
            // Check minimum message length
            if (responseMessage.length < 1) {
                logger.debug("Unexpected message length: {}", responseMessage.length);
                throw new RotelException("Unexpected message length");
            }

            if (responseMessage[responseMessage.length - 1] != '!'
                    && responseMessage[responseMessage.length - 1] != '$') {
                logger.debug("Unexpected ending character in response: {}",
                        Integer.toHexString(responseMessage[responseMessage.length - 1] & 0x000000FF));
                throw new RotelException("Unexpected ending character in response");
            }
        }
    }

    /**
     * Compute the checksum of a message
     *
     * @param message the buffer containing the message
     * @param maxIdx the position in the buffer at which the sum has to be stopped
     *
     * @return the checksum as a byte
     */
    protected byte computeCheckSum(byte[] message, int maxIdx) {
        int result = 0;
        for (int i = 1; i <= maxIdx; i++) {
            result += (message[i] & 0x000000FF);
        }
        return (byte) (result & 0x000000FF);
    }

    /**
     * Add a listener to the list of listeners to be notified with events
     *
     * @param listener the listener
     */
    public void addEventListener(RotelMessageEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener from the list of listeners to be notified with events
     *
     * @param listener the listener
     */
    public void removeEventListener(RotelMessageEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Analyze an incoming message and dispatch corresponding (key, value) to the event listeners
     *
     * @param incomingMessage the received message
     */
    public void handleIncomingMessage(byte[] incomingMessage) {
        logger.debug("handleIncomingMessage: bytes {}", HexUtils.bytesToHex(incomingMessage));

        if (READ_ERROR.equals(incomingMessage)) {
            dispatchKeyValue(KEY_ERROR, MSG_VALUE_ON);
            return;
        }

        try {
            validateResponse(incomingMessage);
        } catch (RotelException e) {
            return;
        }

        if (protocol == RotelProtocol.HEX) {
            handleValidHexMessage(incomingMessage);
        } else {
            handleValidAsciiMessage(incomingMessage);
        }
    }

    /**
     * Analyze a valid HEX message and dispatch corresponding (key, value) to the event listeners
     *
     * @param incomingMessage the received message
     */
    private void handleValidHexMessage(byte[] incomingMessage) {
        if (incomingMessage[3] != STANDARD_RESPONSE) {
            return;
        }

        final int idxChars = model.isCharsBeforeFlags() ? 4 : (4 + model.getRespNbFlags());

        // Replace characters with code < 32 by a space before converting to a string
        for (int i = idxChars; i < (idxChars + model.getRespNbChars()); i++) {
            if (incomingMessage[i] < 0x20) {
                incomingMessage[i] = 0x20;
            }
        }

        String value = new String(incomingMessage, idxChars, model.getRespNbChars(), StandardCharsets.US_ASCII);
        logger.debug("handleValidHexMessage: chars *{}*", value);

        final int idxFlags = model.isCharsBeforeFlags() ? (4 + model.getRespNbChars()) : 4;
        final byte[] flags = Arrays.copyOfRange(incomingMessage, idxFlags, idxFlags + model.getRespNbFlags());
        if (logger.isTraceEnabled()) {
            for (int i = 1; i <= flags.length; i++) {
                try {
                    logger.trace("handleValidHexMessage: Flag {} = {} bits 7-0 = {} {} {} {} {} {} {} {}", i,
                            Integer.toHexString(flags[i - 1] & 0x000000FF), RotelFlagsMapping.isBitFlagOn(flags, i, 7),
                            RotelFlagsMapping.isBitFlagOn(flags, i, 6), RotelFlagsMapping.isBitFlagOn(flags, i, 5),
                            RotelFlagsMapping.isBitFlagOn(flags, i, 4), RotelFlagsMapping.isBitFlagOn(flags, i, 3),
                            RotelFlagsMapping.isBitFlagOn(flags, i, 2), RotelFlagsMapping.isBitFlagOn(flags, i, 1),
                            RotelFlagsMapping.isBitFlagOn(flags, i, 0));
                } catch (RotelException e1) {
                }
            }
        }
        try {
            dispatchKeyValue(KEY_POWER_ZONE2, model.isZone2On(flags) ? POWER_ON : STANDBY);
        } catch (RotelException e1) {
        }
        try {
            dispatchKeyValue(KEY_POWER_ZONE3, model.isZone3On(flags) ? POWER_ON : STANDBY);
        } catch (RotelException e1) {
        }
        try {
            dispatchKeyValue(KEY_POWER_ZONE4, model.isZone4On(flags) ? POWER_ON : STANDBY);
        } catch (RotelException e1) {
        }
        boolean checkMultiIn = false;
        boolean checkSource = true;
        try {
            if (model.isMultiInputOn(flags)) {
                checkSource = false;
                try {
                    RotelSource source = model.getSourceFromName(RotelSource.CAT1_MULTI.getName());
                    RotelCommand cmd = source.getCommand();
                    if (cmd != null) {
                        String value2 = cmd.getAsciiCommandV2();
                        if (value2 != null) {
                            dispatchKeyValue(KEY_SOURCE, value2);
                        }
                    }
                } catch (RotelException e1) {
                }
            }
        } catch (RotelException e1) {
            checkMultiIn = true;
        }
        boolean checkStereo = true;
        try {
            checkStereo = !model.isMoreThan2Channels(flags);
        } catch (RotelException e1) {
        }

        String valueLowerCase = value.trim().toLowerCase();
        if (!valueLowerCase.isEmpty() && !valueLowerCase.startsWith(KEY1_HEX_ZONE2)
                && !valueLowerCase.startsWith(KEY2_HEX_ZONE2) && !valueLowerCase.startsWith(KEY_HEX_ZONE3)
                && !valueLowerCase.startsWith(KEY_HEX_ZONE4)) {
            dispatchKeyValue(KEY_POWER, POWER_ON);
        }

        if (model.getRespNbChars() == 42) {
            // 2 lines of 21 characters with a left part and a right part

            // Line 1 left
            value = new String(incomingMessage, idxChars, 14, StandardCharsets.US_ASCII);
            logger.debug("handleValidHexMessage: line 1 left *{}*", value);
            parseText(value, checkSource, checkMultiIn, false, false, false, false, false, true);

            // Line 1 right
            value = new String(incomingMessage, idxChars + 14, 7, StandardCharsets.US_ASCII);
            logger.debug("handleValidHexMessage: line 1 right *{}*", value);
            parseText(value, false, false, false, false, false, false, false, true);

            // Full line 1
            value = new String(incomingMessage, idxChars, 21, StandardCharsets.US_ASCII);
            dispatchKeyValue(KEY_LINE1, value);

            // Line 2 right
            value = new String(incomingMessage, idxChars + 35, 7, StandardCharsets.US_ASCII);
            logger.debug("handleValidHexMessage: line 2 right *{}*", value);
            parseText(value, false, false, false, false, false, false, false, true);

            // Full line 2
            value = new String(incomingMessage, idxChars + 21, 21, StandardCharsets.US_ASCII);
            logger.debug("handleValidHexMessage: line 2 *{}*", value);
            parseText(value, false, false, true, true, false, true, true, true);
            dispatchKeyValue(KEY_LINE2, value);
        } else {
            value = new String(incomingMessage, idxChars, model.getRespNbChars(), StandardCharsets.US_ASCII);
            parseText(value, checkSource, checkMultiIn, true, false, true, true, checkStereo, false);
            dispatchKeyValue(KEY_LINE1, value);
        }

        if (valueLowerCase.isEmpty()) {
            dispatchKeyValue(KEY_POWER, POWER_OFF_DELAYED);
        }
    }

    /**
     * Analyze a valid ASCII message and dispatch corresponding (key, value) to the event listeners
     *
     * @param incomingMessage the received message
     */
    public void handleValidAsciiMessage(byte[] incomingMessage) {
        byte[] message = filterMessage(incomingMessage, model.getSpecialCharacters());

        // Replace characters with code < 32 by a space before converting to a string
        for (int i = 0; i < message.length; i++) {
            if (message[i] < 0x20) {
                message[i] = 0x20;
            }
        }

        String value = new String(message, 0, message.length - 1, StandardCharsets.US_ASCII);
        logger.debug("handleValidAsciiMessage: chars *{}*", value);
        value = value.trim();
        if (value.isEmpty()) {
            return;
        }
        try {
            String[] splittedValue = value.split("=");
            if (splittedValue.length != 2) {
                logger.debug("handleValidAsciiMessage: ignored message {}", value);
            } else {
                dispatchKeyValue(splittedValue[0].trim().toLowerCase(), splittedValue[1]);
            }
        } catch (PatternSyntaxException e) {
            logger.debug("handleValidAsciiMessage: ignored message {}", value);
        }
    }

    /**
     * Parse a text and dispatch appropriate (key, value) to the event listeners for found information
     *
     * @param text the text to be parsed
     * @param searchSource true if a source has to be searched in the text
     * @param searchMultiIn true if MULTI IN indication has to be searched in the text
     * @param searchZone true if a zone information has to be searched in the text
     * @param searchRecord true if a record source has to be searched in the text
     * @param searchRecordAfterSource true if a record source has to be searched in the text after the a found source
     * @param searchDsp true if a DSP mode has to be searched in the text
     * @param searchStereo true if a STEREO has to be considered in the search
     * @param multipleInfo true if source and volume/mute are provided separately
     */
    private void parseText(String text, boolean searchSource, boolean searchMultiIn, boolean searchZone,
            boolean searchRecord, boolean searchRecordAfterSource, boolean searchDsp, boolean searchStereo,
            boolean multipleInfo) {
        String value = text.trim();
        String valueLowerCase = value.toLowerCase();
        if (searchRecord) {
            dispatchKeyValue(KEY_RECORD_SEL, valueLowerCase.startsWith(KEY_HEX_RECORD) ? MSG_VALUE_ON : MSG_VALUE_OFF);
        }
        if (searchZone) {
            if (valueLowerCase.startsWith(KEY1_HEX_ZONE2) || valueLowerCase.startsWith(KEY2_HEX_ZONE2)) {
                dispatchKeyValue(KEY_ZONE, "2");
            } else if (valueLowerCase.startsWith(KEY_HEX_ZONE3)) {
                dispatchKeyValue(KEY_ZONE, "3");
            } else if (valueLowerCase.startsWith(KEY_HEX_ZONE4)) {
                dispatchKeyValue(KEY_ZONE, "4");
            } else {
                dispatchKeyValue(KEY_ZONE, "1");
            }
        }
        if (valueLowerCase.startsWith(KEY1_HEX_VOLUME) || valueLowerCase.startsWith(KEY2_HEX_VOLUME)) {
            value = extractNumber(value,
                    valueLowerCase.startsWith(KEY1_HEX_VOLUME) ? KEY1_HEX_VOLUME.length() : KEY2_HEX_VOLUME.length());
            dispatchKeyValue(KEY_VOLUME, value);
            dispatchKeyValue(KEY_MUTE, MSG_VALUE_OFF);
        } else if (valueLowerCase.startsWith(KEY_HEX_MUTE)) {
            value = value.substring(KEY_HEX_MUTE.length()).trim();
            if (MSG_VALUE_ON.equalsIgnoreCase(value)) {
                dispatchKeyValue(KEY_MUTE, MSG_VALUE_ON);
            } else {
                logger.debug("Invalid value {} for zone mute", value);
            }
        } else if (valueLowerCase.startsWith(KEY1_HEX_BASS) || valueLowerCase.startsWith(KEY2_HEX_BASS)) {
            value = extractNumber(value,
                    valueLowerCase.startsWith(KEY1_HEX_BASS) ? KEY1_HEX_BASS.length() : KEY2_HEX_BASS.length());
            dispatchKeyValue(KEY_BASS, value);
        } else if (valueLowerCase.startsWith(KEY1_HEX_TREBLE) || valueLowerCase.startsWith(KEY2_HEX_TREBLE)) {
            value = extractNumber(value,
                    valueLowerCase.startsWith(KEY1_HEX_TREBLE) ? KEY1_HEX_TREBLE.length() : KEY2_HEX_TREBLE.length());
            dispatchKeyValue(KEY_TREBLE, value);
        } else if (searchMultiIn && valueLowerCase.startsWith(KEY_HEX_MULTI_IN)) {
            value = value.substring(KEY_HEX_MULTI_IN.length()).trim();
            if (MSG_VALUE_ON.equalsIgnoreCase(value)) {
                try {
                    RotelSource source = model.getSourceFromName(RotelSource.CAT1_MULTI.getName());
                    RotelCommand cmd = source.getCommand();
                    if (cmd != null) {
                        String value2 = cmd.getAsciiCommandV2();
                        if (value2 != null) {
                            dispatchKeyValue(KEY_SOURCE, value2);
                        }
                    }
                } catch (RotelException e1) {
                }
            } else if (!MSG_VALUE_OFF.equalsIgnoreCase(value)) {
                logger.debug("Invalid value {} for MULTI IN", value);
            }
        } else if (searchDsp && valueLowerCase.startsWith(KEY_HEX_BYPASS)) {
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT4_BYPASS.getFeedback());
        } else if (searchDsp && searchStereo && valueLowerCase.startsWith(KEY_HEX_STEREO)) {
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT4_NONE.getFeedback());
        } else if (searchDsp && (valueLowerCase.startsWith(KEY1_HEX_3CH) || valueLowerCase.startsWith(KEY2_HEX_3CH))) {
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT4_STEREO3.getFeedback());
        } else if (searchDsp && valueLowerCase.startsWith(KEY_HEX_5CH)) {
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT4_STEREO5.getFeedback());
        } else if (searchDsp && valueLowerCase.startsWith(KEY_HEX_7CH)) {
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT4_STEREO7.getFeedback());
        } else if (searchDsp
                && (valueLowerCase.startsWith(KEY_HEX_MUSIC1) || valueLowerCase.startsWith(KEY_HEX_DSP1))) {
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT4_DSP1.getFeedback());
        } else if (searchDsp
                && (valueLowerCase.startsWith(KEY_HEX_MUSIC2) || valueLowerCase.startsWith(KEY_HEX_DSP2))) {
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT4_DSP2.getFeedback());
        } else if (searchDsp
                && (valueLowerCase.startsWith(KEY_HEX_MUSIC3) || valueLowerCase.startsWith(KEY_HEX_DSP3))) {
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT4_DSP3.getFeedback());
        } else if (searchDsp
                && (valueLowerCase.startsWith(KEY_HEX_MUSIC4) || valueLowerCase.startsWith(KEY_HEX_DSP4))) {
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT4_DSP4.getFeedback());
        } else if (searchDsp && (valueLowerCase.startsWith(KEY1_HEX_PLII_CINEMA)
                || valueLowerCase.startsWith(KEY2_HEX_PLII_CINEMA) || valueLowerCase.startsWith(KEY1_HEX_PLIIX_CINEMA)
                || searchDsp && valueLowerCase.startsWith(KEY2_HEX_PLIIX_CINEMA))) {
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT2_PLII_CINEMA.getFeedback());
        } else if (searchDsp && (valueLowerCase.startsWith(KEY1_HEX_PLII_MUSIC)
                || valueLowerCase.startsWith(KEY2_HEX_PLII_MUSIC) || valueLowerCase.startsWith(KEY1_HEX_PLIIX_MUSIC)
                || valueLowerCase.startsWith(KEY2_HEX_PLIIX_MUSIC))) {
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT2_PLII_MUSIC.getFeedback());
        } else if (searchDsp && (valueLowerCase.startsWith(KEY1_HEX_PLII_GAME)
                || valueLowerCase.startsWith(KEY2_HEX_PLII_GAME) || valueLowerCase.startsWith(KEY1_HEX_PLIIX_GAME)
                || valueLowerCase.startsWith(KEY2_HEX_PLIIX_GAME))) {
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT2_PLII_GAME.getFeedback());
        } else if (searchDsp && valueLowerCase.startsWith(KEY_HEX_PLIIZ)) {
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT4_PLIIZ.getFeedback());
        } else if (searchDsp
                && (valueLowerCase.startsWith(KEY1_HEX_PROLOGIC) || valueLowerCase.startsWith(KEY2_HEX_PROLOGIC))) {
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT4_PROLOGIC.getFeedback());
        } else if (searchDsp && (valueLowerCase.startsWith(KEY1_HEX_DTS_NEO6_CINEMA)
                || valueLowerCase.startsWith(KEY2_HEX_DTS_NEO6_CINEMA))) {
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT4_NEO6_CINEMA.getFeedback());
        } else if (searchDsp && (valueLowerCase.startsWith(KEY1_HEX_DTS_NEO6_MUSIC)
                || valueLowerCase.startsWith(KEY2_HEX_DTS_NEO6_MUSIC))) {
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT4_NEO6_MUSIC.getFeedback());
        } else if (searchDsp && valueLowerCase.startsWith(KEY_HEX_DTS_ES)) {
            logger.debug("DTS-ES");
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT4_NONE.getFeedback());
        } else if (searchDsp && valueLowerCase.startsWith(KEY_HEX_DTS_96)) {
            logger.debug("DTS 96");
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT4_NONE.getFeedback());
        } else if (searchDsp && valueLowerCase.startsWith(KEY_HEX_DTS)) {
            logger.debug("DTS");
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT4_NONE.getFeedback());
        } else if (searchDsp && valueLowerCase.startsWith(KEY_HEX_DD_EX)) {
            logger.debug("DD-EX");
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT4_NONE.getFeedback());
        } else if (searchDsp && valueLowerCase.startsWith(KEY_HEX_DD)) {
            logger.debug("DD");
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT4_NONE.getFeedback());
        } else if (searchDsp && valueLowerCase.startsWith(KEY_HEX_LPCM)) {
            logger.debug("LPCM");
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT4_NONE.getFeedback());
        } else if (searchDsp && valueLowerCase.startsWith(KEY_HEX_PCM)) {
            logger.debug("PCM");
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT4_NONE.getFeedback());
        } else if (searchDsp && valueLowerCase.startsWith(KEY_HEX_MPEG)) {
            logger.debug("MPEG");
            dispatchKeyValue(KEY_DSP_MODE, RotelDsp.CAT4_NONE.getFeedback());
        } else if (searchZone
                && (valueLowerCase.startsWith(KEY1_HEX_ZONE2) || valueLowerCase.startsWith(KEY2_HEX_ZONE2))) {
            value = value.substring(
                    valueLowerCase.startsWith(KEY1_HEX_ZONE2) ? KEY1_HEX_ZONE2.length() : KEY2_HEX_ZONE2.length());
            parseZone2(value, multipleInfo);
        } else if (searchZone && valueLowerCase.startsWith(KEY_HEX_ZONE3)) {
            parseZone3(value.substring(KEY_HEX_ZONE3.length()), multipleInfo);
        } else if (searchZone && valueLowerCase.startsWith(KEY_HEX_ZONE4)) {
            parseZone4(value.substring(KEY_HEX_ZONE4.length()), multipleInfo);
        } else if (searchRecord && valueLowerCase.startsWith(KEY_HEX_RECORD)) {
            parseRecord(value.substring(KEY_HEX_RECORD.length()));
        } else if (searchSource || searchRecordAfterSource) {
            parseSourceAndRecord(value, searchSource, searchRecordAfterSource, multipleInfo);
        }
    }

    /**
     * Parse a text to identify a source
     *
     * @param text the text to be parsed
     * @param acceptFollowMain true if follow main has to be considered in the search
     *
     * @return the identified source or null if no source is identified in the text
     */
    private @Nullable RotelSource parseSource(String text, boolean acceptFollowMain) {
        String value = text.trim();
        RotelSource source = null;
        if (!value.isEmpty()) {
            if (acceptFollowMain && SOURCE.equalsIgnoreCase(value)) {
                try {
                    source = model.getSourceFromName(RotelSource.CAT1_FOLLOW_MAIN.getName());
                } catch (RotelException e) {
                }
            } else {
                for (RotelSource src : sourcesLabels.keySet()) {
                    String label = sourcesLabels.get(src);
                    if (label != null && value.startsWith(label)) {
                        if (source == null || sourcesLabels.get(source).length() < label.length()) {
                            source = src;
                        }
                    }
                }
            }
        }
        return source;
    }

    private void parseSourceAndRecord(String text, boolean searchSource, boolean searchRecordAfterSource,
            boolean multipleInfo) {
        RotelSource source = parseSource(text, false);
        if (source != null) {
            if (searchSource) {
                RotelCommand cmd = source.getCommand();
                if (cmd != null) {
                    String value2 = cmd.getAsciiCommandV2();
                    if (value2 != null) {
                        dispatchKeyValue(KEY_SOURCE, value2);
                        if (!multipleInfo) {
                            dispatchKeyValue(KEY_MUTE, MSG_VALUE_OFF);
                        }
                    }
                }
            }

            if (searchRecordAfterSource) {
                String value = text.substring(getSourceLabel(source).length()).trim();
                source = parseSource(value, true);
                if (source != null) {
                    RotelCommand cmd = source.getRecordCommand();
                    if (cmd != null) {
                        value = cmd.getAsciiCommandV2();
                        if (value != null) {
                            dispatchKeyValue(KEY_RECORD, value);
                        }
                    }
                }
            }
        }
    }

    private String getSourceLabel(RotelSource source) {
        String label = sourcesLabels.get(source);
        return (label == null) ? source.getLabel() : label;
    }

    private void parseRecord(String text) {
        String value = text.trim();
        RotelSource source = parseSource(value, true);
        if (source != null) {
            RotelCommand cmd = source.getRecordCommand();
            if (cmd != null) {
                value = cmd.getAsciiCommandV2();
                if (value != null) {
                    dispatchKeyValue(KEY_RECORD, value);
                }
            }
        } else {
            logger.debug("Invalid value {} for record source", value);
        }
    }

    private void parseZone2(String text, boolean multipleInfo) {
        String value = text.trim();
        String valueLowerCase = value.toLowerCase();
        if (valueLowerCase.startsWith(KEY1_HEX_VOLUME) || valueLowerCase.startsWith(KEY2_HEX_VOLUME)) {
            value = extractNumber(value,
                    valueLowerCase.startsWith(KEY1_HEX_VOLUME) ? KEY1_HEX_VOLUME.length() : KEY2_HEX_VOLUME.length());
            dispatchKeyValue(KEY_VOLUME_ZONE2, value);
            dispatchKeyValue(KEY_MUTE_ZONE2, MSG_VALUE_OFF);
        } else if (valueLowerCase.startsWith(KEY_HEX_MUTE)) {
            value = value.substring(KEY_HEX_MUTE.length()).trim();
            if (MSG_VALUE_ON.equalsIgnoreCase(value)) {
                dispatchKeyValue(KEY_MUTE_ZONE2, MSG_VALUE_ON);
            } else {
                logger.debug("Invalid value {} for zone mute", value);
            }
        } else if (!MSG_VALUE_OFF.equalsIgnoreCase(value)) {
            RotelSource source = parseSource(value, true);
            if (source != null) {
                RotelCommand cmd = source.getZone2Command();
                if (cmd != null) {
                    value = cmd.getAsciiCommandV2();
                    if (value != null) {
                        dispatchKeyValue(KEY_SOURCE_ZONE2, value);
                        if (!multipleInfo) {
                            dispatchKeyValue(KEY_MUTE_ZONE2, MSG_VALUE_OFF);
                        }
                    }
                }
            } else {
                logger.debug("Invalid value {} for zone 2 source", value);
            }
        }
    }

    private void parseZone3(String text, boolean multipleInfo) {
        String value = text.trim();
        String valueLowerCase = value.toLowerCase();
        if (valueLowerCase.startsWith(KEY1_HEX_VOLUME) || valueLowerCase.startsWith(KEY2_HEX_VOLUME)) {
            value = extractNumber(value,
                    valueLowerCase.startsWith(KEY1_HEX_VOLUME) ? KEY1_HEX_VOLUME.length() : KEY2_HEX_VOLUME.length());
            dispatchKeyValue(KEY_VOLUME_ZONE3, value);
            dispatchKeyValue(KEY_MUTE_ZONE3, MSG_VALUE_OFF);
        } else if (valueLowerCase.startsWith(KEY_HEX_MUTE)) {
            value = value.substring(KEY_HEX_MUTE.length()).trim();
            if (MSG_VALUE_ON.equalsIgnoreCase(value)) {
                dispatchKeyValue(KEY_MUTE_ZONE3, MSG_VALUE_ON);
            } else {
                logger.debug("Invalid value {} for zone mute", value);
            }
        } else if (!MSG_VALUE_OFF.equalsIgnoreCase(value)) {
            RotelSource source = parseSource(value, true);
            if (source != null) {
                RotelCommand cmd = source.getZone3Command();
                if (cmd != null) {
                    value = cmd.getAsciiCommandV2();
                    if (value != null) {
                        dispatchKeyValue(KEY_SOURCE_ZONE3, value);
                        if (!multipleInfo) {
                            dispatchKeyValue(KEY_MUTE_ZONE3, MSG_VALUE_OFF);
                        }
                    }
                }
            } else {
                logger.debug("Invalid value {} for zone 3 source", value);
            }
        }
    }

    private void parseZone4(String text, boolean multipleInfo) {
        String value = text.trim();
        String valueLowerCase = value.toLowerCase();
        if (valueLowerCase.startsWith(KEY1_HEX_VOLUME) || valueLowerCase.startsWith(KEY2_HEX_VOLUME)) {
            value = extractNumber(value,
                    valueLowerCase.startsWith(KEY1_HEX_VOLUME) ? KEY1_HEX_VOLUME.length() : KEY2_HEX_VOLUME.length());
            dispatchKeyValue(KEY_VOLUME_ZONE4, value);
            dispatchKeyValue(KEY_MUTE_ZONE4, MSG_VALUE_OFF);
        } else if (valueLowerCase.startsWith(KEY_HEX_MUTE)) {
            value = value.substring(KEY_HEX_MUTE.length()).trim();
            if (MSG_VALUE_ON.equalsIgnoreCase(value)) {
                dispatchKeyValue(KEY_MUTE_ZONE4, MSG_VALUE_ON);
            } else {
                logger.debug("Invalid value {} for zone mute", value);
            }
        } else if (!MSG_VALUE_OFF.equalsIgnoreCase(value)) {
            RotelSource source = parseSource(value, true);
            if (source != null) {
                RotelCommand cmd = source.getZone4Command();
                if (cmd != null) {
                    value = cmd.getAsciiCommandV2();
                    if (value != null) {
                        dispatchKeyValue(KEY_SOURCE_ZONE4, value);
                        if (!multipleInfo) {
                            dispatchKeyValue(KEY_MUTE_ZONE4, MSG_VALUE_OFF);
                        }
                    }
                }
            } else {
                logger.debug("Invalid value {} for zone 4 source", value);
            }
        }
    }

    /**
     * Extract from a string a number
     *
     * @param value the string
     * @param startIndex the index in the string at which the integer has to be extracted
     *
     * @return the number as a string with its sign and no blank between the sign and the digits
     */
    private String extractNumber(String value, int startIndex) {
        String result = value.substring(startIndex).trim();
        // Delete possible blank(s) between the sign and the number
        if (result.startsWith("+") || result.startsWith("-")) {
            result = result.substring(0, 1) + result.substring(1, result.length()).trim();
        }
        return result;
    }

    /**
     * Suppress certain sequences of bytes from a message
     *
     * @param message the message as a table of bytes
     * @param bytesSequences the table containing the sequence of bytes to be ignored
     *
     * @return the message without the unexpected sequence of bytes
     */
    private byte[] filterMessage(byte[] message, byte[][] bytesSequences) {
        if (bytesSequences.length == 0) {
            return message;
        }
        byte[] filteredMsg = new byte[message.length];
        int srcIdx = 0;
        int dstIdx = 0;
        while (srcIdx < message.length) {
            int ignoredLength = 0;
            for (int i = 0; i < bytesSequences.length; i++) {
                int size = bytesSequences[i].length;
                if ((message.length - srcIdx) >= size) {
                    boolean match = true;
                    for (int j = 0; j < size; j++) {
                        if (message[srcIdx + j] != bytesSequences[i][j]) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        ignoredLength = size;
                        break;
                    }
                }
            }
            if (ignoredLength > 0) {
                srcIdx += ignoredLength;
            } else {
                filteredMsg[dstIdx++] = message[srcIdx++];
            }
        }
        return Arrays.copyOf(filteredMsg, dstIdx);
    }

    /**
     * Dispatch an event (key, value) to the event listeners
     *
     * @param key the key
     * @param value the value
     */
    private void dispatchKeyValue(String key, String value) {
        RotelMessageEvent event = new RotelMessageEvent(this, key, value);
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onNewMessageEvent(event);
        }
    }
}
