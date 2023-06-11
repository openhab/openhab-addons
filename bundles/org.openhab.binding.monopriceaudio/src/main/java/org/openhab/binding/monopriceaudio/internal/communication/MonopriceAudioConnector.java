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
package org.openhab.binding.monopriceaudio.internal.communication;

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
import org.openhab.binding.monopriceaudio.internal.MonopriceAudioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for communicating with the MonopriceAudio device
 *
 * @author Laurent Garnier - Initial contribution
 * @author Michael Lobstein - Adapted for the MonopriceAudio binding
 */
@NonNullByDefault
public abstract class MonopriceAudioConnector {
    public static final String READ_ERROR = "Command Error.";

    // Message types
    public static final String KEY_ZONE_UPDATE = "zone_update";
    // Special keys used by the binding
    public static final String KEY_ERROR = "error";
    public static final String MSG_VALUE_ON = "on";

    private static final Pattern PATTERN = Pattern.compile("^.*#>(\\d{22})$", Pattern.DOTALL);
    private static final String BEGIN_CMD = "<";
    private static final String END_CMD = "\r";

    private final Logger logger = LoggerFactory.getLogger(MonopriceAudioConnector.class);

    /** The output stream */
    protected @Nullable OutputStream dataOut;

    /** The input stream */
    protected @Nullable InputStream dataIn;

    /** true if the connection is established, false if not */
    private boolean connected;

    private @Nullable Thread readerThread;

    private final List<MonopriceAudioMessageEventListener> listeners = new ArrayList<>();

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
     * Open the connection with the MonopriceAudio device
     *
     * @throws MonopriceAudioException - In case of any problem
     */
    public abstract void open() throws MonopriceAudioException;

    /**
     * Close the connection with the MonopriceAudio device
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
            try {
                readerThread.join(3000);
            } catch (InterruptedException e) {
                logger.warn("Error joining readerThread: {}", e.getMessage());
            }
            this.readerThread = null;
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
     * @throws MonopriceAudioException - If the input stream is null, if the first byte cannot be read for any reason
     *             other than the end of the file, if the input stream has been closed, or if some other I/O error
     *             occurs.
     */
    protected int readInput(byte[] dataBuffer) throws MonopriceAudioException {
        InputStream dataIn = this.dataIn;
        if (dataIn == null) {
            throw new MonopriceAudioException("readInput failed: input stream is null");
        }
        try {
            return dataIn.read(dataBuffer);
        } catch (IOException e) {
            throw new MonopriceAudioException("readInput failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get the status of a zone
     *
     * @param zone the zone to query for current status
     *
     * @throws MonopriceAudioException - In case of any problem
     */
    public void queryZone(MonopriceAudioZone zone) throws MonopriceAudioException {
        sendCommand(zone, MonopriceAudioCommand.QUERY, null);
    }

    /**
     * Request the MonopriceAudio controller to execute a command
     *
     * @param zone the zone for which the command is to be run
     * @param cmd the command to execute
     * @param value the integer value to consider for volume, bass, treble, etc. adjustment
     *
     * @throws MonopriceAudioException - In case of any problem
     */
    public void sendCommand(MonopriceAudioZone zone, MonopriceAudioCommand cmd, @Nullable Integer value)
            throws MonopriceAudioException {
        String messageStr = "";

        if (cmd == MonopriceAudioCommand.QUERY) {
            // query special case (ie: ? + zoneId)
            messageStr = cmd.getValue() + zone.getZoneId();
        } else if (value != null) {
            // if the command passed a value, append it to the messageStr
            messageStr = BEGIN_CMD + zone.getZoneId() + cmd.getValue() + String.format("%02d", value);
        } else {
            throw new MonopriceAudioException("Send command \"" + messageStr + "\" failed: passed in value is null");
        }
        messageStr += END_CMD;
        logger.debug("Send command {}", messageStr);

        OutputStream dataOut = this.dataOut;
        if (dataOut == null) {
            throw new MonopriceAudioException("Send command \"" + messageStr + "\" failed: output stream is null");
        }
        try {
            dataOut.write(messageStr.getBytes(StandardCharsets.US_ASCII));
            dataOut.flush();
        } catch (IOException e) {
            throw new MonopriceAudioException("Send command \"" + cmd.getValue() + "\" failed: " + e.getMessage(), e);
        }
    }

    /**
     * Add a listener to the list of listeners to be notified with events
     *
     * @param listener the listener
     */
    public void addEventListener(MonopriceAudioMessageEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener from the list of listeners to be notified with events
     *
     * @param listener the listener
     */
    public void removeEventListener(MonopriceAudioMessageEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Analyze an incoming message and dispatch corresponding (key, value) to the event listeners
     *
     * @param incomingMessage the received message
     */
    public void handleIncomingMessage(byte[] incomingMessage) {
        String message = new String(incomingMessage, StandardCharsets.US_ASCII).trim();

        logger.debug("handleIncomingMessage: {}", message);

        if (READ_ERROR.equals(message)) {
            dispatchKeyValue(KEY_ERROR, MSG_VALUE_ON);
            return;
        }

        // Amp controller sends status string: #>1200010000130809100601
        Matcher matcher = PATTERN.matcher(message);
        if (matcher.find()) {
            // pull out just the digits and send them as an event
            dispatchKeyValue(KEY_ZONE_UPDATE, matcher.group(1));
        } else {
            logger.debug("no match on message: {}", message);
        }
    }

    /**
     * Dispatch an event (key, value) to the event listeners
     *
     * @param key the key
     * @param value the value
     */
    private void dispatchKeyValue(String key, String value) {
        MonopriceAudioMessageEvent event = new MonopriceAudioMessageEvent(this, key, value);
        listeners.forEach(l -> l.onNewMessageEvent(event));
    }
}
