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
package org.openhab.binding.powermax.internal.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.openhab.binding.powermax.internal.message.PowermaxBaseMessage;
import org.openhab.binding.powermax.internal.message.PowermaxMessageEvent;
import org.openhab.binding.powermax.internal.message.PowermaxMessageEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An abstract class for the communication with the Visonic alarm panel that
 * handles stuff common to all communication types
 *
 * @author Laurent Garnier - Initial contribution
 */
public abstract class PowermaxConnector implements PowermaxConnectorInterface {

    private final Logger logger = LoggerFactory.getLogger(PowermaxConnector.class);

    private InputStream input;
    private OutputStream output;
    private boolean connected;
    private Thread readerThread;
    private long waitingForResponse;
    private List<PowermaxMessageEventListener> listeners = new ArrayList<>();

    @Override
    public abstract void open();

    @Override
    public abstract void close();

    /**
     * Cleanup everything; to be called when closing the communication
     */
    protected void cleanup() {
        logger.debug("cleanup(): cleaning up Connection");

        if (readerThread != null) {
            readerThread.interrupt();
            try {
                readerThread.join();
            } catch (InterruptedException e) {
            }
        }

        if (output != null) {
            IOUtils.closeQuietly(output);
        }

        if (input != null) {
            IOUtils.closeQuietly(input);
        }

        readerThread = null;
        input = null;
        output = null;

        logger.debug("cleanup(): Connection Cleanup");
    }

    /**
     * Handles an incoming message
     *
     * @param incomingMessage the received message as a table of bytes
     */
    public void handleIncomingMessage(byte[] incomingMessage) {
        PowermaxMessageEvent event = new PowermaxMessageEvent(this,
                PowermaxBaseMessage.getMessageHandler(incomingMessage));

        // send message to event listeners
        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).onNewMessageEvent(event);
        }
    }

    @Override
    public void sendMessage(byte[] data) {
        try {
            output.write(data);
            output.flush();
        } catch (IOException e) {
            logger.debug("sendMessage(): Writing error: {}", e.getMessage(), e);
            setConnected(false);
        }
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        return input.read(buffer);
    }

    @Override
    public void addEventListener(PowermaxMessageEventListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeEventListener(PowermaxMessageEventListener listener) {
        listeners.remove(listener);
    }

    /**
     * @return the input stream
     */
    public InputStream getInput() {
        return input;
    }

    /**
     * Set the input stream
     *
     * @param input the input stream
     */
    public void setInput(InputStream input) {
        this.input = input;
    }

    /**
     * @return the output stream
     */
    public OutputStream getOutput() {
        return output;
    }

    /**
     * Set the output stream
     *
     * @param output the output stream
     */
    public void setOutput(OutputStream output) {
        this.output = output;
    }

    /**
     * @return true if connected or false if not
     */
    @Override
    public boolean isConnected() {
        return connected;
    }

    /**
     * Set the connection state
     *
     * @param connected true if connected or false if not
     */
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    /**
     * @return the thread that handles the message reading
     */
    public Thread getReaderThread() {
        return readerThread;
    }

    /**
     * Set the thread that handles the message reading
     *
     * @param readerThread the thread
     */
    public void setReaderThread(Thread readerThread) {
        this.readerThread = readerThread;
    }

    /**
     * @return the start time of the time frame to receive a response
     */
    public synchronized long getWaitingForResponse() {
        return waitingForResponse;
    }

    /**
     * Set the start time of the time frame to receive a response
     *
     * @param timeLastReceive the time in milliseconds
     */
    public synchronized void setWaitingForResponse(long waitingForResponse) {
        this.waitingForResponse = waitingForResponse;
    }
}
