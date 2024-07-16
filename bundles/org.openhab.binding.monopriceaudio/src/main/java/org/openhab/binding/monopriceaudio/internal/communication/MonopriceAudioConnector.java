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
package org.openhab.binding.monopriceaudio.internal.communication;

import static org.openhab.binding.monopriceaudio.internal.MonopriceAudioBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
 * @author Michael Lobstein - Add support for additional amplifier types
 */
@NonNullByDefault
public abstract class MonopriceAudioConnector {
    // Message types
    public static final String KEY_ZONE_UPDATE = "zone_update";
    public static final String KEY_PING = "ping";

    private final Logger logger = LoggerFactory.getLogger(MonopriceAudioConnector.class);

    /** The output stream */
    protected @Nullable OutputStream dataOut;

    /** The input stream */
    protected @Nullable InputStream dataIn;

    /** true if the connection is established, false if not */
    private boolean connected;
    private boolean pingResponseOnly;

    protected AmplifierModel amp = AmplifierModel.MONOPRICE;

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
        this.pingResponseOnly = false;
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
        this.pingResponseOnly = false;
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
                logger.debug("Error joining readerThread: {}", e.getMessage());
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
     * Get only ping success events from the connector. If amplifier does not have keypads or supports
     * unsolicited updates, the use of this method will cause the connector to only send ping success events until the
     * next time the connection is reset.
     *
     * @throws MonopriceAudioException - In case of any problem
     */
    public void sendPing() throws MonopriceAudioException {
        pingResponseOnly = true;
        // poll zone 1 status only to see if the amp responds
        queryZone(amp.getZoneIds().iterator().next());
    }

    /**
     * Get the status of a zone
     *
     * @param zoneId the zone to query for current status
     *
     * @throws MonopriceAudioException - In case of any problem
     */
    public void queryZone(String zoneId) throws MonopriceAudioException {
        sendCommand(amp.getQueryPrefix() + zoneId + amp.getQuerySuffix());
    }

    /**
     * Monoprice 31028 and OSD Audio PAM1270 amps do not report treble, bass and balance with the main status inquiry,
     * so we must send three extra commands to retrieve those values
     *
     * @param zoneId the zone to query for current treble, bass and balance status
     *
     * @throws MonopriceAudioException - In case of any problem
     */
    public void queryTrebBassBalance(String zoneId) throws MonopriceAudioException {
        sendCommand(amp.getQueryPrefix() + zoneId + amp.getTrebleCmd());
        sendCommand(amp.getQueryPrefix() + zoneId + amp.getBassCmd());
        sendCommand(amp.getQueryPrefix() + zoneId + amp.getBalanceCmd());
    }

    /**
     * Request the MonopriceAudio amplifier to execute a raw command
     *
     * @param cmd the command to execute
     *
     * @throws MonopriceAudioException - In case of any problem
     */
    public void sendCommand(String cmd) throws MonopriceAudioException {
        sendCommand(null, cmd, null);
    }

    /**
     * Request the MonopriceAudio amplifier to execute a command
     *
     * @param zoneId the zone for which the command is to be run
     * @param cmd the command to execute
     * @param value the integer value to consider for power, volume, bass, treble, etc. adjustment
     *
     * @throws MonopriceAudioException - In case of any problem
     */
    public void sendCommand(@Nullable String zoneId, String cmd, @Nullable Integer value)
            throws MonopriceAudioException {
        String messageStr;

        if (zoneId != null && value != null) {
            // if the command passed a value, build messageStr with prefix, zoneId, command, value and suffix
            messageStr = amp.getCmdPrefix() + zoneId + cmd + amp.getFormattedValue(value) + amp.getCmdSuffix();
        } else {
            // otherwise send the raw cmd from the query() methods
            messageStr = cmd + amp.getCmdSuffix();
        }
        logger.debug("Send command {}", messageStr);

        OutputStream dataOut = this.dataOut;
        if (dataOut == null) {
            throw new MonopriceAudioException("Send command \"" + messageStr + "\" failed: output stream is null");
        }
        try {
            dataOut.write(messageStr.getBytes(StandardCharsets.US_ASCII));
            dataOut.flush();
        } catch (IOException e) {
            throw new MonopriceAudioException("Send command \"" + messageStr + "\" failed: " + e.getMessage(), e);
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
        if (pingResponseOnly) {
            dispatchKeyValue(KEY_PING, EMPTY);
            return;
        }

        String message = new String(incomingMessage, StandardCharsets.US_ASCII).trim();

        if (EMPTY.equals(message)) {
            return;
        }

        if (message.startsWith(amp.getRespPrefix())) {
            logger.debug("handleIncomingMessage: {}", message);
            dispatchKeyValue(KEY_ZONE_UPDATE, message);
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
