/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal.protocol;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.domintell.internal.config.BridgeConfig;
import org.openhab.binding.domintell.internal.protocol.message.*;
import org.openhab.binding.domintell.internal.protocol.model.group.ItemGroup;
import org.openhab.binding.domintell.internal.protocol.model.module.Module;
import org.openhab.binding.domintell.internal.protocol.model.type.ActionType;
import org.openhab.binding.domintell.internal.protocol.model.type.ModuleType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.*;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * {@link DomintellConnection} class implements the communication protocol provided by the Domintell DETH02 ethernet
 * module.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class DomintellConnection {
    //static Domintell commands
    private static final String CMD_HELLO = "HELLO";
    private static final String CMD_LOGIN = "LOGIN";
    private static final String CMD_LOGOUT = "LOGOUT";
    private static final String CMD_APPINFO = "APPINFO";

    /**
     * Time between retries
     */
    private static final int RECONNECT_TIMEOUT = 10; //s

    /**
     * Time between HELLO commands
     */
    private static final int HELLO_TIMEOUT = 50; //s

    /**
     * Timeout on the reader thread i.e. we must receive a packet within that delay since the last read. There
     * should be a clock update every minute from the Domintell master, so using 61s for this should be safe.
     */
    private static final int SOCKET_READ_TIMEOUT = 61; //s

    /**
     * Delay to reconnect
     */
    private static final int READER_CHECK_DELAY = 5;

    /**
     * Class logger.
     */
    private final Logger logger = LoggerFactory.getLogger(DomintellConnection.class);

    /**
     * Domintell system date
     */
    private Date domintellSysdate;

    /**
     * Bridge network configuration
     */
    private BridgeConfig config;

    /**
     * Login state listener
     */
    private final StateListener stateListener;

    /**
     * DomintellConnection state
     */
    private StateListener.State currentState;

    /**
     * Scheduler
     */
    private ScheduledExecutorService scheduler;

    /**
     * Scheduled HELLO job
     */
    private ScheduledFuture<?> helloJob;

    /**
     * Reader thread monitoring
     */
    private ScheduledFuture<?> readerCheckJob;

    /**
     * Thread for receiving messages from Domintell system (message producer for readQueue).
     */
    private MessageReceiverThread readerThread;

    /**
     * Socket
     */
    private DatagramSocket socket;

    /**
     * Module/group registry
     */
    private DomintellRegistry registry = new DomintellRegistry(this);

    /**
     * Constructor.
     *
     * @param config Configuration.
     * @param configEventListener Configuration event listener.
     * @param scheduler Scheduler
     */
    public DomintellConnection(BridgeConfig config, StateListener stateListener, ConfigurationEventHandler configEventListener, ScheduledExecutorService scheduler) {
        this.config = config;
        this.stateListener = stateListener;
        this.scheduler = scheduler;
        registry.setConfigEventListener(configEventListener);
    }

    /**
     * Getter.
     *
     * @return Module/group registry.
     */
    public DomintellRegistry getRegistry() {
        return registry;
    }

    private void updateState(StateListener.State state, String msg) {
        StateListener.State oldState = this.currentState;
        this.currentState = state;

        //get all module status when changing status to ONLINE
        if (oldState != StateListener.State.ONLINE && state == StateListener.State.ONLINE) {
            registry.getModules().forEach(m -> sendCommand(ActionMessageBuilder.create().withModuleKey(m.getModuleKey()).withAction(ActionType.STATUS).build()));
        }
        if (state != oldState) {
            if (msg != null) {
                logger.debug("Domintell connection status: {}({})", state, msg);
            } else {
                logger.debug("Domintell connection status: {}", state);
            }
        }
        stateListener.stateChanged(state, msg);
    }

    public void startGateway(BridgeConfig config) {
        this.config = config;
        if (config.isValid()) {
            startBackgroundThreads();
            readerCheckJob = scheduler.scheduleWithFixedDelay(this::checkReader, 0, READER_CHECK_DELAY, TimeUnit.SECONDS);
        } else {
            updateState(StateListener.State.ERROR, "Invalid parameters.");
        }
    }

    private void checkReader() {
        try {
            if (!readerThread.isAlive()) {
                stopBackgroundThreads();
                Thread.sleep(RECONNECT_TIMEOUT * 1000);
                startBackgroundThreads();
            }
        } catch (InterruptedException e) {
            logger.debug("Reader check was interrupted.");
        }
    }

    public void stopGateway() {
        //stop monitoring job
        if (readerCheckJob != null && !readerCheckJob.isCancelled()) {
            readerCheckJob.cancel(true);
            readerCheckJob = null;
        }
        //shut down the connection
        stopBackgroundThreads();
        logger.debug("Stop Domintell connection.");
    }

    public Date getDomintellSysdate() {
        return domintellSysdate;
    }

    /**
     * DomintellConnection status check.
     *
     * @return True if online.
     */
    public boolean isOnline() {
        return currentState == StateListener.State.ONLINE;
    }

    /**
     * Called from discovery service to scan Domintell network
     */
    public void scan() {
        sendCommand(CMD_APPINFO);
    }

    /**
     * Start the socket and the two read/write threads.
     */
    private void startBackgroundThreads() {
        try {
            updateState(StateListener.State.INITIALIZING, null);
            socket = new DatagramSocket();
            socket.setSoTimeout(SOCKET_READ_TIMEOUT * 1000);

            logger.debug("Connecting to Domintell system: {}", config.toString());
            socket.connect(config.getInternetAddress(), config.getPort());
            logger.debug("Socket connected");

            readerThread = new MessageReceiverThread(socket);
            readerThread.start();

            //send HELLO command after every 50s according to the Domintell protocol requirements to keep the connection open
            helloJob = scheduler.scheduleWithFixedDelay(() -> {
                if (currentState == StateListener.State.ONLINE) {
                    sendCommand(CMD_HELLO);
                }
            }, 0, HELLO_TIMEOUT, TimeUnit.SECONDS);

            updateState(StateListener.State.STARTING_SESSION, null);
            sendCommand(CMD_LOGIN, true);
        } catch (SocketException | UnknownHostException e) {
            logger.debug("Configuration error", e);
        }
    }

    /**
     * Stop reader thread and close the socket.
     */
    private void stopBackgroundThreads() {
        updateState(StateListener.State.STOPPING, null);

        if (helloJob != null && !helloJob.isCancelled()) {
            helloJob.cancel(true);
            helloJob = null;
        }

        sendCommand(CMD_LOGOUT, true);

        //stop the reader
        if (readerThread != null && readerThread.isAlive()) {
            readerThread.interrupt();
        }
        readerThread = null;

        //close the socket
        if (socket != null) {
            if (socket.isConnected()) {
                socket.disconnect();
            }
            if (!socket.isClosed()) {
                socket.close();
            }
            socket = null;
        }

        updateState(StateListener.State.OFFLINE, null);
    }

    public void sendCommand(String command) {
        sendCommand(command, false);
    }

    /**
     * Send command to Domintell system
     *
     * @param cmd Command to send
     * @param force Normally command are sent only if the connection is online. This can be forced by this flag.
     */
    private void sendCommand(String cmd, boolean force) {
        logger.trace("Sending message: >{}<", cmd);
        if (socket != null && (force || currentState == StateListener.State.ONLINE)) {
            byte[] buf = cmd.getBytes();
            try {
                DatagramPacket p = new DatagramPacket(buf, buf.length, config.getInternetAddress(), config.getPort());
                socket.send(p);
            } catch (IOException e) {
                logger.trace("Could not send message: >{}<", cmd);
            }
        }
    }

    /**
     * Receiver thread class
     */
    private class MessageReceiverThread extends Thread {
        /**
         * Communication message encoding
         */
        private static final String CHARSET = "ISO-8859-1";

        /**
         * SOcker reader buffer size
         */
        private static final int READ_BUFFER_SIZE = 256;

        /**
         * Connected socket
         */
        private DatagramSocket socket;

        /**
         * Message parser
         */
        private MessageParser parser;

        MessageReceiverThread(DatagramSocket socket) {
            super();
            this.socket = socket;
            this.parser = new MessageParser();
        }

        @Override
        public void run() {
            logger.debug("Receiver thread started");
            try {
                while (!isInterrupted()) {
                    try {
                        byte[] buffer = new byte[READ_BUFFER_SIZE];
                        DatagramPacket p = new DatagramPacket(buffer, buffer.length);
                        socket.receive(p);
                        String txt = new String(p.getData(), 0, p.getLength(), CHARSET);

                        // Get rid of line ending
                        txt = txt.trim();
                        String[] msgArray = txt.split("\r\n");
                        if (!handleMessageArray(msgArray)) {
                            //we need to stop the reader thread
                            break;
                        }
                    } catch (SocketTimeoutException se) {
                        if (currentState == StateListener.State.ONLINE) {
                            updateState(StateListener.State.STALE, null);
                            logger.trace("No messages received in the last {} seconds.", SOCKET_READ_TIMEOUT);
                        } else {
                            logger.trace("Still no messages received. Stopping the reader thread.");
                            updateState(StateListener.State.ERROR, "Cannot connect to to Domintell system.");
                            break;
                        }
                    } catch (InterruptedIOException ie) {
                        logger.debug("Reader thread interrupted.");
                        break;
                    } catch (IOException e) {
                        logger.debug("Error receiving packet from Domintell system", e);
                        updateState(StateListener.State.ERROR, "I/O Exception");
                        break;
                    }
                }
            } finally {
                //reader thread exited
                updateState(StateListener.State.OFFLINE, null);
            }
            logger.debug("Receiver thread stopped");
        }

        /**
         * Process array of messages received bz a single read
         *
         * @param msgArray Array of messages
         * @return False case the processing failed and if the reader need to be stopped
         */
        private boolean handleMessageArray(String[] msgArray) {
            for (String msg : msgArray) {
                logger.trace("Receiver thread got packet >{}<", msg);
                @Nullable BaseMessage baseMessage = parser.parseMessage(msg);
                if (baseMessage != null) {
                    return handleMessage(baseMessage);
                }
            }
            return true;
        }

        /**
         * Handling message based on type
         *
         * @param message Parsed message
         * @return False if the reader thread need to be stopped
         */
        private boolean handleMessage(@NonNull BaseMessage message) {
            try {
                switch (message.getType()) {
                    case DATA:
                        processModuleStatusMessage((StatusMessage) message);
                        break;
                    case SYSTEM_TIME:
                        domintellSysdate = ((SystemTimeMessage) message).getDateTime();
                        updateState(StateListener.State.ONLINE, null);
                        break;
                    case SESSION_OPENED:
                        updateState(StateListener.State.ONLINE, null);
                        break;
                    case AUTH_FAILED:
                        updateState(StateListener.State.FATAL, "Authentication failed");
                        return false;
                    case ACCESS_DENIED:
                        updateState(StateListener.State.FATAL, "Access denied");
                        return false;
                    case SESSION_TIMEOUT:
                    case SESSION_CLOSED:
                        updateState(StateListener.State.OFFLINE, null);
                        return false;
                    case WORLD:
                        updateState(StateListener.State.ONLINE, null);
                        logger.trace("Domintell system replied to HELLO");
                        break;
                    case PONG:
                        updateState(StateListener.State.ONLINE, null);
                        logger.trace("Domintell system replied to PING");
                        break;
                    case END_APPINFO:
                        logger.trace("APPINFO received - update things and channels");
                        updateDescriptions();
                        break;
                    case START_APPINFO:
                        logger.trace("APPINFO starting");
                        break;
                    case APPINFO:
                        logger.trace("APPINFO message: {}", message.getMessage());
                        break;
                }
            } catch (Exception e) {
                logger.debug("Failed to process message: {}", message, e);
            }
            return true;
        }

        /**
         * Called after an APPINFO cycle to update item descriptions for all groups and modules.
         */
        private void updateDescriptions() {
            registry.getGroups().forEach(ItemGroup::notifyItemsTranslated);
            registry.getModules().forEach(Module::notifyItemsTranslated);
        }

        /**
         * Call the module to process status message
         *
         * @param message Parsed status message
         */
        private void processModuleStatusMessage(StatusMessage message) {
            try {
                //logger.trace("Module status update: {}", message.getMessage());
                ModuleType moduleType = message.getModuleType();
                if (moduleType != null && moduleType.isModuleSupported()) {
                    Module module = registry.getDomintellModule(moduleType, message.getSerialNumber());
                    module.processStateUpdate(message);
                } else {
                    logger.trace("Module type not supported. Dropping message", message);
                }
            } catch (Exception e) {
                logger.debug("Error processing the message: {}", message.getMessage(), e);
            }
        }
    }
}
