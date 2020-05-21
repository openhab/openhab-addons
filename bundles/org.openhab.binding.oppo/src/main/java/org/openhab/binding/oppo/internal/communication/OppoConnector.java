/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.oppo.internal.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.oppo.internal.OppoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for communicating with the Oppo player
 *
 * @author Laurent Garnier - Initial contribution
 * @author Michael Lobstein - Adapted for the Oppo binding
 */
@NonNullByDefault
public abstract class OppoConnector {
    private final Logger logger = LoggerFactory.getLogger(OppoConnector.class);

    public String END_CMD = "\r";    
    private String  BEGIN_CMD = "#";

    /** The output stream */
    protected @Nullable OutputStream dataOut;

    /** The input stream */
    protected @Nullable InputStream dataIn;

    /** true if the connection is established, false if not */
    private boolean connected;

    private @Nullable Thread readerThread;

    private List<OppoMessageEventListener> listeners = new ArrayList<>();
    
    /**
     * Called when using direct IP connection for 83/93/95/103/105
     * overrides the command message preamble and removes the CR at the end
     */
    public void overrideCmdPreamble(Boolean override) {
        if (override) {
            this.BEGIN_CMD = "REMOTE ";
            this.END_CMD = "";
        }
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
     * Open the connection with the Oppo player
     *
     * @throws OppoException - In case of any problem
     */
    public abstract void open() throws OppoException;

    /**
     * Close the connection with the Oppo player
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
     * @throws OppoException - If the input stream is null, if the first byte cannot be read for any reason
     *             other than the end of the file, if the input stream has been closed, or if some other I/O error
     *             occurs.
     * @throws InterruptedIOException - if the thread was interrupted during the reading of the input stream
     */
    protected int readInput(byte[] dataBuffer) throws OppoException, InterruptedIOException {
        InputStream dataIn = this.dataIn;
        if (dataIn == null) {
            throw new OppoException("readInput failed: input stream is null");
        }
        try {
            return dataIn.read(dataBuffer);
        } catch (IOException e) {
            logger.debug("readInput failed: {}", e.getMessage());
            throw new OppoException("readInput failed: " + e.getMessage());
        }
    }
    
    /**
     * Request the Oppo controller to execute a command and pass in a value
     *
     * @param cmd the command to execute
     * @param value the string value to pass with the command
     *
     * @throws OppoException - In case of any problem
     */
    public void sendCommand(OppoCommand cmd, @Nullable String value) throws OppoException {
        sendCommand(cmd.getValue() + " " + value);
    }
    
    /**
     * Request the Oppo controller to execute a command that does not specify a value
     *
     * @param cmd the command to execute
     *
     * @throws OppoException - In case of any problem
     */
    public void sendCommand(OppoCommand cmd) throws OppoException {
        sendCommand(cmd.getValue());
    }
    
    /**
     * Request the Oppo controller to execute a raw command string
     *
     * @param command the command string to run
     *
     * @throws OppoException - In case of any problem
     */
    public void sendCommand(String command) throws OppoException {
        String messageStr = BEGIN_CMD + command + END_CMD;
        
        logger.debug("sending command: {}", messageStr);

        byte[] message = messageStr.getBytes(StandardCharsets.US_ASCII);
        logger.debug("Send command {}", messageStr);      

        OutputStream dataOut = this.dataOut;
        if (dataOut == null) {
            throw new OppoException("Send command \"" + messageStr + "\" failed: output stream is null");
        }
        try {
            dataOut.write(message);
            dataOut.flush();
        } catch (IOException e) {
            logger.debug("Send command \"{}\" failed: {}", messageStr, e.getMessage());
            throw new OppoException("Send command \"" + command + "\" failed: " + e.getMessage());
        }
        logger.debug("Send command \"{}\" succeeded", messageStr);
    }

    /**
     * Add a listener to the list of listeners to be notified with events
     *
     * @param listener the listener
     */
    public void addEventListener(OppoMessageEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener from the list of listeners to be notified with events
     *
     * @param listener the listener
     */
    public void removeEventListener(OppoMessageEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * Analyze an incoming message and dispatch corresponding (key, value) to the event listeners
     *
     * @param incomingMessage the received message
     */
    public void handleIncomingMessage(byte[] incomingMessage) {
        String message = new String(incomingMessage).trim();
        
        logger.debug("handleIncomingMessage: {}", message);
        
        if ("@NOP OK".equals(message)) {
            dispatchKeyValue("NOP", "OK");
            return;
        }
        
        Pattern p;
        
        // Player sent an OK response to a query: @QDT OK DVD-VIDEO or a volume update @VUP OK 100
        p=Pattern.compile("^@(Q[A-Z0-9]{2}|VUP|VDN) OK (.*)$");
        
        try {
            Matcher matcher=p.matcher(message);
            matcher.find();
            // pull out the inquiry type and the remainder of the message
            dispatchKeyValue(matcher.group(1), matcher.group(2));
            return;
        } catch (IllegalStateException e){
            logger.debug("no match on message: {}", message);
        }
        
        // Player sent a status update ie: @UTC 000 000 T 00:00:01
        p=Pattern.compile("^@(U[A-Z0-9]{2}) (.*)$");
        
        try {
            Matcher matcher=p.matcher(message);
            matcher.find();
            // pull out the update type and the remainder of the message
            dispatchKeyValue(matcher.group(1), matcher.group(2));
            return;
        } catch (IllegalStateException e){
            logger.debug("no match on message: {}", message);
        }
        
        logger.debug("unhandled message: {}", message);
    }

    /**
     * Dispatch an event (type, key, value) to the event listeners
     *
     * @param type the type
     * @param key the key
     * @param value the value
     */
    private void dispatchKeyValue(String key, String value) {
        OppoMessageEvent event = new OppoMessageEvent(this, key, value);
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onNewMessageEvent(event);
        }
    }
}
