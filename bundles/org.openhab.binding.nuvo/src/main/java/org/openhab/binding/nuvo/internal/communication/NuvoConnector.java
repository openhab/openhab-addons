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
package org.openhab.binding.nuvo.internal.communication;

import static org.openhab.binding.nuvo.internal.NuvoBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nuvo.internal.NuvoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for communicating with the Nuvo device
 *
 * @author Laurent Garnier - Initial contribution
 * @author Michael Lobstein - Adapted for the Nuvo binding
 */
@NonNullByDefault
public abstract class NuvoConnector {
    private static final String COMMAND_OK = "#OK";
    private static final String BEGIN_CMD = "*";
    private static final String END_CMD = "\r";
    private static final String QUERY = "?";
    private static final String VER_STR_E6 = "#VER\"NV-E6G";
    private static final String VER_STR_GC = "#VER\"NV-I8G";
    private static final String ALL_OFF = "#ALLOFF";
    private static final String MUTE = "#MUTE";
    private static final String PAGE = "#PAGE";
    private static final String RESTART = "#RESTART\"NuVoNet\"";
    private static final String PING = "#PING";
    private static final String PING_RESPONSE = "PING";

    private static final byte[] WAKE_STR = "\r".getBytes(StandardCharsets.US_ASCII);

    private static final Pattern SRC_PATTERN = Pattern.compile("^#S(\\d{1})(.*)$");
    private static final Pattern ZONE_PATTERN = Pattern.compile("^#Z(\\d{1,2}),(.*)$");
    private static final Pattern ZONE_SOURCE_PATTERN = Pattern.compile("^#Z(\\d{1,2})S(\\d{1})(.*)$");
    private static final Pattern NN_MENUREQ_PATTERN = Pattern.compile("^#Z(\\d{1,2})S(\\d{1})MENUREQ(.*)$");
    private static final Pattern NN_BUTTON_PATTERN = Pattern.compile("^#Z(\\d{1,2})S(\\d{1})BUTTON(.*)$");
    private static final Pattern NN_BUTTONTWO_PATTERN = Pattern.compile("^#Z(\\d{1,2})S(\\d{1})BUTTONTWO(.*)$");

    private static final Pattern ZONE_CFG_PATTERN = Pattern.compile("^#ZCFG(\\d{1,2}),(.*)$");

    // S2ALBUMARTREQ0x620FD879,80,80,2,0x00C0C0C0,0,0,0,0,1
    private static final Pattern NN_ALBUM_ART_REQ = Pattern.compile("^#S(\\d{1})ALBUMARTREQ(.*)$");

    // S2ALBUMARTFRAGREQ0x620FD879,0,750
    private static final Pattern NN_ALBUM_ART_FRAG_REQ = Pattern.compile("^#S(\\d{1})ALBUMARTFRAGREQ(.*)$");

    // S6FAVORITE0x000003E8
    private static final Pattern NN_FAVORITE_PATTERN = Pattern.compile("^#S(\\d{1})FAVORITE0x(.*)$");

    private final Logger logger = LoggerFactory.getLogger(NuvoConnector.class);

    protected static final String COMMAND_ERROR = "#?";

    /** The output stream */
    protected @Nullable OutputStream dataOut;

    /** The input stream */
    protected @Nullable InputStream dataIn;

    /** true if the connection is established, false if not */
    private boolean connected;

    private @Nullable Thread readerThread;

    private List<NuvoMessageEventListener> listeners = new ArrayList<>();

    private boolean isEssentia = true;
    private boolean isAnyOhNuvoNet = false;

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
     * Tell the connector if the device is an Essentia G or not
     *
     * @param true if the device is an Essentia G
     */
    public void setEssentia(boolean isEssentia) {
        this.isEssentia = isEssentia;
    }

    /**
     * Tell the connector to listen for NuvoNet source messages
     *
     * @param true if any sources are configured as openHAB NuvoNet sources
     */
    public void setAnyOhNuvoNet(boolean isAnyOhNuvoNet) {
        this.isAnyOhNuvoNet = isAnyOhNuvoNet;
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
     * Open the connection with the Nuvo device
     *
     * @throws NuvoException - In case of any problem
     */
    public abstract void open() throws NuvoException;

    /**
     * Close the connection with the Nuvo device
     */
    public abstract void close();

    /**
     * Stop the thread that handles the feedback messages and close the opened input and output streams
     */
    protected void cleanup() {
        Thread readerThread = this.readerThread;
        OutputStream dataOut = this.dataOut;
        if (dataOut != null) {
            try {
                dataOut.close();
            } catch (IOException e) {
                logger.debug("Error closing dataOut: {}", e.getMessage());
            }
            this.dataOut = null;
        }
        InputStream dataIn = this.dataIn;
        if (dataIn != null) {
            try {
                dataIn.close();
            } catch (IOException e) {
                logger.debug("Error closing dataIn: {}", e.getMessage());
            }
            this.dataIn = null;
        }
        if (readerThread != null) {
            readerThread.interrupt();
            this.readerThread = null;
            try {
                readerThread.join(3000);
            } catch (InterruptedException e) {
                logger.warn("Error joining readerThread: {}", e.getMessage());
            }
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
     * @throws NuvoException - If the input stream is null, if the first byte cannot be read for any reason
     *             other than the end of the file, if the input stream has been closed, or if some other I/O error
     *             occurs.
     */
    protected int readInput(byte[] dataBuffer) throws NuvoException {
        InputStream dataIn = this.dataIn;
        if (dataIn == null) {
            throw new NuvoException("readInput failed: input stream is null");
        }
        try {
            return dataIn.read(dataBuffer);
        } catch (IOException e) {
            throw new NuvoException("readInput failed: " + e.getMessage(), e);
        }
    }

    /**
     * Request the Nuvo controller to execute an inquiry command
     *
     * @param zone the zone for which the command is to be run
     * @param cmd the command to execute
     *
     * @throws NuvoException - In case of any problem
     */
    public void sendQuery(NuvoEnum zone, NuvoCommand cmd) throws NuvoException {
        sendCommand(zone.getId() + cmd.getValue() + QUERY);
    }

    /**
     * Request the Nuvo controller to execute a command for a zone that takes no arguments (ie power on, power off,
     * etc.)
     *
     * @param zone the zone for which the command is to be run
     * @param cmd the command to execute
     *
     * @throws NuvoException - In case of any problem
     */
    public void sendCommand(NuvoEnum zone, NuvoCommand cmd) throws NuvoException {
        sendCommand(zone.getId() + cmd.getValue());
    }

    /**
     * Request the Nuvo controller to execute a command for a zone and pass in a value
     *
     * @param zone the zone for which the command is to be run
     * @param cmd the command to execute
     * @param value the string value to consider for volume, source, etc.
     *
     * @throws NuvoException - In case of any problem
     */
    public void sendCommand(NuvoEnum zone, NuvoCommand cmd, @Nullable String value) throws NuvoException {
        sendCommand(zone.getId() + cmd.getValue() + value);
    }

    /**
     * Request the Nuvo controller to execute a configuration command for a zone and pass in a value
     *
     * @param zone the zone for which the command is to be run
     * @param cmd the command to execute
     * @param value the string value to consider for bass, treble, balance, etc.
     *
     * @throws NuvoException - In case of any problem
     */
    public void sendCfgCommand(NuvoEnum zone, NuvoCommand cmd, @Nullable String value) throws NuvoException {
        sendCommand(zone.getConfigId() + cmd.getValue() + value);
    }

    /**
     * Request the Nuvo controller to execute a system command the does not specify a zone or value
     *
     * @param cmd the command to execute
     *
     * @throws NuvoException - In case of any problem
     */
    public void sendCommand(NuvoCommand cmd) throws NuvoException {
        sendCommand(cmd.getValue());
    }

    /**
     * Request the Nuvo controller to execute a raw command string
     *
     * @param command the command string to run
     *
     * @throws NuvoException - In case of any problem
     */
    public void sendCommand(@Nullable String command) throws NuvoException {
        String messageStr = BEGIN_CMD + command + END_CMD;

        logger.debug("sending command: {}", messageStr);

        OutputStream dataOut = this.dataOut;
        if (dataOut == null) {
            throw new NuvoException("Send command \"" + messageStr + "\" failed: output stream is null");
        }
        try {
            // Essentia G needs time to wake up when in standby mode
            // I don't want to track that in the binding, so just do this always
            if (this.isEssentia) {
                dataOut.write(WAKE_STR);
                dataOut.flush();
            }
            dataOut.write(messageStr.getBytes(StandardCharsets.US_ASCII));
            dataOut.flush();
        } catch (IOException e) {
            throw new NuvoException("Send command \"" + command + "\" failed: " + e.getMessage(), e);
        }
    }

    /**
     * Add a listener to the list of listeners to be notified with events
     *
     * @param listener the listener
     */
    public void addEventListener(NuvoMessageEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener from the list of listeners to be notified with events
     *
     * @param listener the listener
     */
    public void removeEventListener(NuvoMessageEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Analyze an incoming message and dispatch corresponding (type, zone, src, value) to the event listeners
     *
     * @param incomingMessage the received message
     */
    public void handleIncomingMessage(byte[] incomingMessage) {
        String message = new String(incomingMessage, StandardCharsets.US_ASCII).trim();

        logger.debug("handleIncomingMessage: {}", message);

        if (COMMAND_ERROR.equals(message) || COMMAND_OK.equals(message)) {
            // ignore
            return;
        }

        if (message.contains(PING)) {
            try {
                sendCommand(PING_RESPONSE);
            } catch (NuvoException e) {
                logger.debug("Error sending response to PING command");
            }
            dispatchKeyValue(TYPE_PING, BLANK);
            return;
        }

        if (RESTART.equals(message)) {
            dispatchKeyValue(TYPE_RESTART, BLANK);
            return;
        }

        if (message.contains(VER_STR_E6) || message.contains(VER_STR_GC)) {
            // example: #VER"NV-E6G FWv2.66 HWv0"
            // split on " and return the version number
            dispatchKeyValue(TYPE_VERSION, message.split("\"")[1]);
            return;
        }

        if (message.equals(ALL_OFF)) {
            dispatchKeyValue(TYPE_ALLOFF, BLANK);
            return;
        }

        if (message.contains(MUTE)) {
            dispatchKeyValue(TYPE_ALLMUTE, message.substring(message.length() - 1));
            return;
        }

        if (message.contains(PAGE)) {
            dispatchKeyValue(TYPE_PAGE, message.substring(message.length() - 1));
            return;
        }

        Matcher matcher;

        if (isAnyOhNuvoNet) {
            // Amp controller sent a NuvoNet album art request
            matcher = NN_ALBUM_ART_REQ.matcher(message);
            if (matcher.find()) {
                dispatchKeyValue(TYPE_NN_ALBUM_ART_REQ, BLANK, matcher.group(1), matcher.group(2));
                return;
            }

            // Amp controller sent a NuvoNet album art fragment request
            matcher = NN_ALBUM_ART_FRAG_REQ.matcher(message);
            if (matcher.find()) {
                dispatchKeyValue(TYPE_NN_ALBUM_ART_FRAG_REQ, BLANK, matcher.group(1), matcher.group(2));
                return;
            }

            // Amp controller sent a request for a NuvoNet source to play a favorite
            matcher = NN_FAVORITE_PATTERN.matcher(message);
            if (matcher.find()) {
                dispatchKeyValue(TYPE_NN_FAVORITE_REQ, BLANK, matcher.group(1), matcher.group(2));
                return;
            }
        }

        // Amp controller sent a source update ie: #S2DISPINFO,DUR3380,POS3090,STATUS2
        // or #S2DISPLINE1,"1 of 17"
        matcher = SRC_PATTERN.matcher(message);
        if (matcher.find()) {
            dispatchKeyValue(TYPE_SOURCE_UPDATE, BLANK, matcher.group(1), matcher.group(2));
            return;
        }

        // Amp controller sent a zone update ie: #Z11,ON,SRC3,VOL63,DND0,LOCK0
        matcher = ZONE_PATTERN.matcher(message);
        if (matcher.find()) {
            dispatchKeyValue(TYPE_ZONE_UPDATE, matcher.group(1), BLANK, matcher.group(2));
            return;
        }

        if (isAnyOhNuvoNet) {
            // Amp controller sent a NuvoNet zone/source BUTTONTWO press event ie: #Z11S3BUTTONTWO4,2,0,0,0
            matcher = NN_BUTTONTWO_PATTERN.matcher(message);
            if (matcher.find()) {
                // redundant - ignore
                return;
            }

            // Amp controller sent a NuvoNet zone/source BUTTON press event ie: #Z4S6BUTTON1,1,0xFFFFFFFF,1,3
            matcher = NN_BUTTON_PATTERN.matcher(message);
            if (matcher.find()) {
                // pull out the remainder of the message: button #, action, menuid, itemid, itemidx
                String[] buttonSplit = matcher.group(3).split(COMMA);

                // second field is button action, only send DOWNUP (0) or DOWN (1), ignore UP (2)
                if (ZERO.equals(buttonSplit[1]) || ONE.equals(buttonSplit[1])) {
                    // a button in a menu was pressed, send 'menuid,itemidx'
                    if (!ZERO.equals(buttonSplit[2])) {
                        dispatchKeyValue(TYPE_NN_MENU_ITEM_SELECTED, matcher.group(1), matcher.group(2),
                                buttonSplit[2] + COMMA + buttonSplit[3]);
                    } else {
                        // send the button # in the event, don't send extra fields menuid, itemid, etc..
                        dispatchKeyValue(TYPE_NN_BUTTON, matcher.group(1), matcher.group(2), buttonSplit[0]);
                    }
                }
                return;
            }

            // Amp controller sent a NuvoNet zone/source menu request event ie: #Z2S6MENUREQ0x0000000B,1,0,0
            matcher = NN_MENUREQ_PATTERN.matcher(message);
            if (matcher.find()) {
                dispatchKeyValue(TYPE_NN_MENUREQ, matcher.group(1), matcher.group(2), matcher.group(3));
                return;
            }
        }

        // Amp controller sent a zone/source button press event ie: #Z11S3PLAYPAUSE
        matcher = ZONE_SOURCE_PATTERN.matcher(message);
        if (matcher.find()) {
            dispatchKeyValue(TYPE_ZONE_SOURCE_BUTTON, matcher.group(1), matcher.group(2), matcher.group(3));
            return;
        }

        // Amp controller sent a zone configuration response ie: #ZCFG1,BASS1,TREB-2,BALR2,LOUDCMP1
        matcher = ZONE_CFG_PATTERN.matcher(message);
        if (matcher.find()) {
            // pull out the zone id and the remainder of the message
            dispatchKeyValue(TYPE_ZONE_CONFIG, matcher.group(1), BLANK, matcher.group(2));
            return;
        }

        logger.debug("unhandled message: {}", message);
    }

    /**
     * Dispatch a system level event without zone or src to the event listeners
     *
     * @param type the type
     * @param value the value
     */
    private void dispatchKeyValue(String type, String value) {
        dispatchKeyValue(type, BLANK, BLANK, value);
    }

    /**
     * Dispatch an event (type, zone, src, value) to the event listeners
     *
     * @param type the type
     * @param zone the zone id
     * @param src the source id
     * @param value the value
     */
    private void dispatchKeyValue(String type, String zone, String src, String value) {
        NuvoMessageEvent event = new NuvoMessageEvent(this, type, zone, src, value);
        listeners.forEach(l -> l.onNewMessageEvent(event));
    }
}
