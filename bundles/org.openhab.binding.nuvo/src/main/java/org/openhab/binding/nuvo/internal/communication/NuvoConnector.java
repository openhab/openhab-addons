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
package org.openhab.binding.nuvo.internal.communication;

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
    private final Logger logger = LoggerFactory.getLogger(NuvoConnector.class);
    
    public static final String COMMAND_ERROR = "#?";
    public static final String COMMAND_OK = "#OK";

    // Message types
    public static final String TYPE_VERSION = "version";
    public static final String TYPE_ALLOFF = "alloff";
    public static final String TYPE_ALLMUTE = "allmute";
    public static final String TYPE_PAGE = "page";
    public static final String TYPE_SOURCE_UPDATE = "source_update";
    public static final String TYPE_ZONE_UPDATE = "zone_update";
    public static final String TYPE_ZONE_BUTTON = "zone_button";
    public static final String TYPE_ZONE_CONFIG = "zone_config";

    /** The output stream */
    protected @Nullable OutputStream dataOut;

    /** The input stream */
    protected @Nullable InputStream dataIn;

    /** true if the connection is established, false if not */
    private boolean connected;

    private @Nullable Thread readerThread;

    private List<NuvoMessageEventListener> listeners = new ArrayList<>();
    
    private boolean isEssentia = true;

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
     * @throws NuvoException - If the input stream is null, if the first byte cannot be read for any reason
     *             other than the end of the file, if the input stream has been closed, or if some other I/O error
     *             occurs.
     * @throws InterruptedIOException - if the thread was interrupted during the reading of the input stream
     */
    protected int readInput(byte[] dataBuffer) throws NuvoException, InterruptedIOException {
        InputStream dataIn = this.dataIn;
        if (dataIn == null) {
            throw new NuvoException("readInput failed: input stream is null");
        }
        try {
            return dataIn.read(dataBuffer);
        } catch (IOException e) {
            logger.debug("readInput failed: {}", e.getMessage());
            throw new NuvoException("readInput failed: " + e.getMessage());
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
        sendCommand(zone.getId() + cmd.getValue() + NuvoCommand.QUERY.getValue());
    }

    /**
     * Request the Nuvo controller to execute a command for a zone that takes no arguments (ie power on, power off, etc.)
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
    public void sendCommand(String command) throws NuvoException {
        byte[] wakeString = "\r".getBytes(StandardCharsets.US_ASCII);
        
        String messageStr = NuvoCommand.BEGIN_CMD.getValue() + command + NuvoCommand.END_CMD.getValue();
        
        logger.debug("sending command: {}", messageStr);

        byte[] message = messageStr.getBytes(StandardCharsets.US_ASCII);
        logger.debug("Send command {}", messageStr);      

        OutputStream dataOut = this.dataOut;
        if (dataOut == null) {
            throw new NuvoException("Send command \"" + messageStr + "\" failed: output stream is null");
        }
        try {
            // Essentia G needs time to wake up when in standby mode
            // I don't want to track that in the binding, so just do this always
            if (this.isEssentia) {
                dataOut.write(wakeString);
                dataOut.flush();
                Thread.sleep(5);
            }
            dataOut.write(message);
            dataOut.flush();
        } catch (IOException | InterruptedException e) {
            logger.debug("Send command \"{}\" failed: {}", messageStr, e.getMessage());
            throw new NuvoException("Send command \"" + command + "\" failed: " + e.getMessage());
        }
        logger.debug("Send command \"{}\" succeeded", messageStr);
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
     * Analyze an incoming message and dispatch corresponding (type, key, value) to the event listeners
     *
     * @param incomingMessage the received message
     */
    public void handleIncomingMessage(byte[] incomingMessage) {
        String message = new String(incomingMessage).trim();
        
        logger.debug("handleIncomingMessage: {}", message);
        
        if (COMMAND_ERROR.equals(message) || COMMAND_OK.equals(message)) {
            //ignore
            return;
        }
  
        if (message.contains("#VER\"NV-")) {
            // example: #VER"NV-E6G FWv2.66 HWv0"
            // split on " and return the version number
            dispatchKeyValue(TYPE_VERSION, "", message.split("\"")[1]);
            return;
        }
        
        if (message.equals("#ALLOFF")) {
            dispatchKeyValue(TYPE_ALLOFF, "", "");
            return;
        }
        
        if (message.contains("#MUTE")) {
            dispatchKeyValue(TYPE_ALLMUTE, "", message.substring(message.length() - 1));
            return;
        }
        
        if (message.contains("#PAGE")) {
            dispatchKeyValue(TYPE_PAGE, "", message.substring(message.length() - 1));
            return;
        }
        
        Pattern p;
        
        // Amp controller send a source update ie: #S2DISPINFO,DUR3380,POS3090,STATUS2
        // or #S2DISPLINE1,"1 of 17"
        p=Pattern.compile("^#(S\\d{1})(.*)$");
        
        try {
            Matcher matcher=p.matcher(message);
            matcher.find();
            // pull out the source id and the remainder of the message
            dispatchKeyValue(TYPE_SOURCE_UPDATE, matcher.group(1), matcher.group(2));
            return;
        } catch (IllegalStateException e){
            logger.debug("no match on message: {}", message);
        }
        
        // Amp controller send a zone update ie: #Z11,ON,SRC3,VOL63,DND0,LOCK0
        p=Pattern.compile("^#(Z\\d{1,2}),(.*)$");
        
        try {
            Matcher matcher=p.matcher(message);
            matcher.find();
            // pull out the zone id and the remainder of the message
            dispatchKeyValue(TYPE_ZONE_UPDATE, matcher.group(1), matcher.group(2));
            return;
        } catch (IllegalStateException e){
            logger.debug("no match on message: {}", message);
        }
        
        // Amp controller send a zone button press event ie: #Z11S3PLAYPAUSE
        p=Pattern.compile("^#(Z\\d{1,2})(S\\d{1})(.*)$");
        
        try {
            Matcher matcher=p.matcher(message);
            matcher.find();
            // pull out the source id and the remainder of the message, ignore the zone id
            dispatchKeyValue(TYPE_ZONE_BUTTON, matcher.group(2), matcher.group(3));
            return;
        } catch (IllegalStateException e){
            logger.debug("no match on message: {}", message);
        }
        
        // Amp controller send a zone configuration response ie: #ZCFG1,BASS1,TREB-2,BALR2,LOUDCMP1
        p=Pattern.compile("^#ZCFG(\\d{1,2}),(.*)$");
        
        try {
            Matcher matcher=p.matcher(message);
            matcher.find();
            // pull out the zone id and the remainder of the message
            dispatchKeyValue(TYPE_ZONE_CONFIG, "Z"+matcher.group(1), matcher.group(2));
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
    private void dispatchKeyValue(String type, String key, String value) {
        NuvoMessageEvent event = new NuvoMessageEvent(this, type, key, value);
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onNewMessageEvent(event);
        }
    }
}
