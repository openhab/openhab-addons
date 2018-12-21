/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.veluxklf200.internal.engine;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.openhab.binding.veluxklf200.internal.commands.BaseKLFCommand;
import org.openhab.binding.veluxklf200.internal.commands.CommandStatus;
import org.openhab.binding.veluxklf200.internal.commands.KlfCmdLogin;
import org.openhab.binding.veluxklf200.internal.commands.KlfCmdPing;
import org.openhab.binding.veluxklf200.internal.commands.KlfCmdTerminate;
import org.openhab.binding.veluxklf200.internal.commands.structure.KLFCommandCodes;
import org.openhab.binding.veluxklf200.internal.components.VeluxErrorResponse;
import org.openhab.binding.veluxklf200.internal.utility.KLFUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the work-horse of the KLF200 interface. Specifically, it is
 * responsible for establishing connections to the unit, sending commands and
 * then delegating responses. Two queue's are maintained, one with a list of
 * commands that are awaiting processing and a second with a list of commands
 * that are being processed. A producer consumer pattern is implemented whereby
 * clients can submit a command and then notified when the processing has
 * completed.
 *
 * @author MFK - Initial Contribution
 * @author Guenther Schreiner - Re-use of some utility functions created for dealing with SLIP byte streams
 */
public class KLFCommandProcessor {

    /** Logging. */
    private final Logger logger = LoggerFactory.getLogger(KLFCommandProcessor.class);

    /**
     * Default amount of time to wait for a command to execute (in milliseconds)
     * if an explicit timeout is not specified. The default is set to 60 seconds
     * as it can take some time for a command to execute. For example, moving a
     * blind from open to closed could take several seconds, particularly if the
     * user mode is set to a comfort setting.
     */
    private static final long DEFAULT_COMMAND_TIMEOUT = 60000;

    /** How often (in minutes) to send a keep alive Ping to the KLF200 unit. */
    private static final int KEEPALIVE_PING_FREQ = 10;

    /** Time delay in miliseconds to wait for a duplicate in progress command to complete */
    private static final long WAIT_DUPLICATE_COMPLETION = 500;

    /** Time delay between sending commands to the KLF200 unit */
    private static final long COMMAND_EXEC_DELAY = 250;

    /**
     * Maximum number of commands that can be waiting to be processed in the
     * queue. In theory, should never be exceeded, but if it is, clients will
     * block until space is available in the queue.
     */
    public static final int MAX_QUEUE_SIZE = 20;

    /** The hostname or IP address of the KLF200 unit. */
    private String host;

    /** The socket port on the KLF200 unit. */
    private int port;

    /** The password for the KLF200 unit. */
    private String password;

    /** Timer for keepalive ping. */
    private Timer keepaliveTimer;

    /**
     * Amount of time to wait for a command to execute (in miliseconds) if an
     * explicit timeout is not specified.
     */
    private long commandTimeout = DEFAULT_COMMAND_TIMEOUT;

    /**
     * Set to indicate that a successful login to the unit has occurred and is
     * also used to ensure that a command that requires authentication is not
     * executed if successful login has not been achieved or executed.
     */
    private boolean isLoggedIn;

    /**
     * Indicates that initialisation has completed successfully as a result of
     * calling {@link initialise}.
     */
    private boolean isInitialised;

    /**
     * Flag to indicate whether or not processing queues are operational or have
     * been shutdown. Typically is set to true when {@link shutdown} is called.
     */
    private boolean queueShutdown;

    /** Queue of commands that are waiting to be processed by the KLF200 unit. */
    private LinkedBlockingDeque<BaseKLFCommand> commandQueue;

    /**
     * List of commands that are in the process of being processed by the KLF200
     * unit.
     */
    private CopyOnWriteArrayList<BaseKLFCommand> processingList;

    /**
     * Reference to class to handle unsolicoted events. Specifically, events
     * that have occurred within the KLF/Velux eco-system that did not originate
     * as a result of a command initiated by us! For example, if someone uses a
     * remote control to open a blind.
     */
    private KLFEventNotification eventNotification;

    /** Maximum size of a response from the KLF200 unit. */
    private static final int CONNECTION_BUFFER_SIZE = 4096;

    /** Socket connection to the KLF200 unit. */
    private SSLSocket klfRawSocket = null;

    /** Output data stream related to the {@link klfRawSocket}. */
    private DataOutputStream klfOutputStream;

    /** Input data stream related to the {@link klfRawSocket}. */
    private DataInputStream klfInputStream;

    /**
     * Custom trust manager that accepts self-signed certs. The KLF200 unit uses
     * SSL for communication, but does have a valid commercial SSL cert. It uses
     * a self-signed cert.
     */
    private final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }
    } };

    /**
     * Instantiates a new KLF command processor.
     *
     * @param host
     *                     The IP address or hostname of the KLF200 unit.
     * @param port
     *                     The TCP port of the KLF200 unit.
     * @param password
     *                     The password for connecting to the KLF200 unit.
     */
    public KLFCommandProcessor(String host, int port, String password) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.isInitialised = false;
    }

    /**
     * Checks to ensure that the command processor has been properly initialised
     * and if not, throws a runtime exception.
     */
    private void checkInitialised() {
        if (!this.isInitialised) {
            logger.error("Attempt to perform an operation before the command processor was initialised.");
        }
    }

    /**
     * Gets the command timeout. Specifically, the amount of time to wait for a
     * command to execute (in seconds) if an explicit timeout is not specified.
     *
     * @return the command timeout
     */
    public long getCommandTimeout() {
        return commandTimeout / 1000;
    }

    /**
     * Sets the command timeout. Specifically, the amount of time to wait for a
     * command to execute (in seconds) if an explicit timeout is not specified.
     *
     * @param commandTimeout
     *                           the new command timeout
     */
    public void setCommandTimeout(long commandTimeout) {
        this.commandTimeout = commandTimeout * 1000;
    }

    /**
     * After constructing, initialize should be called to prepare for
     * processing. A connection to the KLF200 unit is established, login is
     * attempted and the processing queues are setup to wait for commands.
     *
     * @return true, If initialization is successful, false otherwise. If
     *         initialisation fails and processing operations are attempted,
     *         runtime exceptions will be thrown.
     */
    public boolean initialise() {
        logger.trace("Attempting connection to the KLF200 unit at {} on port {}", this.host, this.port);
        if (!this.setupKLFConnection(this.host, this.port)) {
            logger.error("Unable to setup a connection to the KLF200 unit at {} on port {}", this.host, this.port);
            return false;
        }
        logger.trace("Connection established to the KLF200 unit at {} on port {}", this.host, this.port);

        this.processingList = new CopyOnWriteArrayList<BaseKLFCommand>();
        commandQueue = new LinkedBlockingDeque<BaseKLFCommand>(MAX_QUEUE_SIZE);
        this.queueShutdown = false;
        this.startProcessingResponses();
        this.startProcessingRequests();
        this.eventNotification = new KLFEventNotification();
        KlfCmdLogin login = new KlfCmdLogin(this.password);
        logger.trace("Attempting to login to the KLF200 unit with password supplied.");
        if (executeCommand(login)) {
            if (CommandStatus.COMPLETE == login.getCommandStatus()) {
                logger.trace("Login to the KLF200 unit at {} on port {} was successful", this.host, this.port);

                startKeepAlivePing();

                this.isLoggedIn = true;
                this.isInitialised = true;
                return true;
            }
        }
        logger.error("Unable to login to the KLF200 unit with password supplied.");
        return false;
    }

    /**
     * Registers a third-party as a consumer of notification events that were generated by the KLF200 unit.
     *
     * @param listener A third-party event consumer that implements the {@link KLFEventListener} interface.
     */
    public void registerEventListener(KLFEventListener listener) {
        this.eventNotification.registerListener(listener);
    }

    /**
     * Setup a socket connection to the KLF200 unit.
     *
     * @param host
     *                 The IP address or hostname of the KLF200 unit.
     * @param port
     *                 The TCP port of the KLF200 unit.
     *
     * @return true, if successfully connected, false otherwise.
     */
    private boolean setupKLFConnection(String host, int port) {
        try {
            logger.trace("Attempting to create an SSL connection to KLF200 host {} on port {}.", host, port);
            SSLContext ctx = null;
            try {
                ctx = SSLContext.getInstance("SSL");
                ctx.init(null, trustAllCerts, null);
            } catch (Exception e) {
                logger.error("Unable to create an SSL context: {}", e.getMessage());
                return false;
            }

            klfRawSocket = (SSLSocket) ctx.getSocketFactory().createSocket(host, port);
            klfRawSocket.startHandshake();
            klfOutputStream = new DataOutputStream(klfRawSocket.getOutputStream());
            klfInputStream = new DataInputStream(klfRawSocket.getInputStream());

            logger.info("Successfully established an SSL connection to KLF200 host {} on port {}.", host, port);

            return true;
        } catch (ConnectException e) {
            logger.error("Unable to connect to KLF200 host {} on port {}. Reason: {}", host, port, e.getMessage());
        } catch (UnknownHostException e) {
            logger.error("Unable to connect to KLF200 host {} on port {}. Reason: The host could not be found.", host,
                    port);
        } catch (IOException e) {
            logger.error("Unable to connect to KLF200 host {} on port {}. Reason: {}", host, port, e.getMessage());
        }
        // Only get here in the event of an error
        return false;
    }

    /**
     * Close the socket connection to the KLF200 unit as well as stopping
     * processing queues and threads.
     */
    public void shutdown() {
        checkInitialised();
        logger.trace("Instructing all processing threads / queues to cease and shutdown.");
        dispatchCommand(new KlfCmdTerminate());
        try {
            logger.info("Closing the connection to the KLF200 unit.");
            klfInputStream.close();
            klfOutputStream.close();
            klfRawSocket.close();
        } catch (IOException e) {
            logger.warn("An error occurred when attempting to close the connection to the KLF200 unit: {}",
                    e.getMessage());
        }

        keepaliveTimer.cancel();

        // Prevent any further operations being executed.
        this.isInitialised = false;
    }

    /**
     * Process a command request. Specifically, validate is and then send to the
     * KLF200 unit. Once sent, the command is added to the
     * {@link processingList} list to await a response / responses from the
     * KLF200 unit.
     *
     * @param command
     *                    The command to execute.
     */
    private void processRequest(BaseKLFCommand command) {
        if (null != klfOutputStream) {
            if (command.isValid()) {
                if ((!command.getKLFCommandStructure().isAuthRequired())
                        || (command.getKLFCommandStructure().isAuthRequired() == this.isLoggedIn)) {
                    byte[] data = command.getRawKLFCommand();
                    try {
                        logger.debug("Executing command {} with Session: {} for Specific Node: {}",
                                command.getKLFCommandStructure().getDisplayCode(), command.formatSessionID(),
                                command.formatMainNode());
                        klfOutputStream.write(data, 0, data.length);
                        command.setCommandStatus(CommandStatus.PROCESSING);
                        processingList.add(command);

                        try {
                            // The KLF200 gets confused when you send commands to it too quickly. When sent too quickly,
                            // the unit can lock up and become unresponsive for long periods of time. As such, a brief
                            // delay between commands being sent appears to cure the problem.
                            TimeUnit.MILLISECONDS.sleep(COMMAND_EXEC_DELAY);
                        } catch (InterruptedException e) {
                            logger.warn("Command execution delay was interrupted: {}", e);
                        }
                        return;
                    } catch (IOException e) {
                        logger.error("Unable to commuinicate with the KLF200 unit: {}", e.getMessage());
                    }
                } else {
                    logger.error("Rejected command as command required login, but login has not been performed.");
                }
            } else {
                logger.error("Rejected command as command failed validation.");
            }
        } else {
            logger.error("Rejected command as connection with KLF200 was null.");
        }

        // If we get here, something has gone wrong, so we need to notify the
        // caller
        command.setCommandStatus(CommandStatus.ERROR);
        synchronized (command) {
            command.notifyAll();
        }
    }

    /**
     * When a response is received from the KLF200 unit, the response code
     * (command code) of the response is retrieved and compared with the
     * {@link processingList} list of commands that are in progress. One of the
     * commands in the list should be able to handle the specified response
     * message from the KLF200.
     *
     * @param commandCode The response code (command code) received from the KLF200
     *                        unit.
     * @param data        the data
     * @return The relevant command from the {@link processingList} list or null
     *         if no match is found.
     */
    private BaseKLFCommand findInProgressCommand(short commandCode, byte[] data) {
        BaseKLFCommand result = null;
        Iterator<BaseKLFCommand> iterator = this.processingList.iterator();
        while (iterator.hasNext()) {
            BaseKLFCommand match = iterator.next();
            if (match.canHandleResponse(commandCode, data)) {
                logger.trace("Found a matching command for the response code {}", String.format("0x%04X", commandCode));
                result = match;
            }
        }
        // Only report an error in the event that the command received is not on
        // our watch list. Commands that are on the watch list are notification
        // commands that may have originated as a result of a direct user
        // interaction with a velux device (such as using a remote control to
        // perform a task)
        if (!this.eventNotification.isOnWatchList(commandCode)) {
            if (null == result) {
                logger.error("No match found for the command with the response code {}",
                        String.format("0x%04X", commandCode));
            }
        }
        return result;
    }

    /**
     * Checks the list of commands that are currently in progress to see if any are similar to the current command
     * supplied. This function is used by the command consumer to determine if it is safe to execute the current
     * command. It would not be safe to execute the current command if that command does not support sessions and there
     * is already a similar command in progress. In this case, the KLF200 can get confused and return only a single
     * result rather than two results -- one for each command.
     *
     * @param current The current command that the command processor is ready to execute
     * @return True if there is a similar command in progress, false otherwise.
     */
    private boolean isSimilarInProgress(BaseKLFCommand current) {
        Iterator<BaseKLFCommand> iterator = this.processingList.iterator();
        while (iterator.hasNext()) {
            BaseKLFCommand inProgress = iterator.next();
            if (inProgress.getClass() == current.getClass()) {
                // A command similar to this is already in progress
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a string representation of all the items currently in various processing states. Used typically for debug or
     * trace logging purposes only.
     *
     * @return String representation of the contents of the queues
     */
    private String getQueueContents() {
        String result = "In Progress: [";
        for (BaseKLFCommand cmd : this.processingList) {
            String part[] = cmd.getClass().getName().split("\\.");
            result += part[part.length - 1];
            result += ", ";
        }
        result += "], Waiting: [";
        for (BaseKLFCommand cmd : this.commandQueue) {
            String part[] = cmd.getClass().getName().split("\\.");
            result += part[part.length - 1];
            result += ", ";
        }
        result += "]";
        return result;
    }

    /**
     * Start a consumer thread to monitor the {@link commandQueue} for incoming
     * commands to be processed.
     */
    private void startProcessingRequests() {
        new Thread() {
            @Override
            public void run() {
                logger.trace("Starting the command consumer.");
                while (!queueShutdown) {
                    BaseKLFCommand c;
                    try {
                        c = commandQueue.take();
                        boolean delayProcessing = false;
                        if ((c.getKLFCommandStructure().isNodeSpecific())
                                && (!c.getKLFCommandStructure().isSessionRequired())) {
                            // The current command is node specific, but does not utilise a session. As such, there is a
                            // risk that if there is another command similar to this already executing that we could end
                            // up with a collision and the KLF200 getting confused by which request to process / respond
                            // to.
                            if (isSimilarInProgress(c)) {
                                // There is a similar command in progress, so lets put the current command back on the
                                // queue and wait for a little while to allow the current one to complete
                                logger.debug("Similar node specific command already executing, delaying execution: {}",
                                        c);
                                commandQueue.putFirst(c);
                                logger.debug("QUEUE CONTENTS -> {}", getQueueContents());
                                delayProcessing = true;
                                TimeUnit.MILLISECONDS.sleep(WAIT_DUPLICATE_COMPLETION);
                            }
                        }
                        if (!delayProcessing) {
                            if (c instanceof KlfCmdTerminate) {
                                queueShutdown = true;
                            } else {
                                processRequest(c);
                            }
                        }
                    } catch (InterruptedException e) {
                        logger.warn("Operation of the command consumer interrupted: {}", e.getMessage());
                    }
                }
                logger.trace("The command consumer has been shutdown.");
            }
        }.start();
    }

    /**
     * Schedule a ping of the KLF200 every 10 minutes to prevent the socket from
     * shutting down.
     */
    private void startKeepAlivePing() {
        keepaliveTimer = new Timer();
        keepaliveTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                logger.trace("Sending keep-alive ping to the KLF 200 unit.");
                dispatchCommand(new KlfCmdPing());
            }
        }, 0, 1000 * 60 * KEEPALIVE_PING_FREQ);
    }

    /**
     * Checks to see if an error GW_ERROR_NTF frame has been returned from the
     * KLF200 unit to indicate that there was a problem with a command that was
     * sent.
     *
     * @param data
     *                 The decoded data frame that was returned from the KLF200 unit.
     * @return A VeluxErrorResponse object with details of the error that
     *         occurred in the event there was an error, null otherwise.
     */
    private VeluxErrorResponse getErrorFrame(byte[] data) {
        if (KLFCommandCodes.GW_ERROR_NTF == KLFUtils.decodeKLFCommand(data)) {
            return VeluxErrorResponse.create(data[BaseKLFCommand.FIRSTBYTE]);
        }
        return null;
    }

    /**
     * In the event that an error frame is received, it may or may not be
     * possible to determine which original command the error refers to. If we
     * only have a single command in the processing state, then it is likely
     * that the error pertains to this command.
     *
     * @param error
     *                  The details of the error that occurred.
     */
    private void handleGeneralError(VeluxErrorResponse error) {
        if (this.processingList.size() == 1) {
            // Likely that the offending Command is the only one in the list.
            BaseKLFCommand bad = this.processingList.iterator().next();
            this.processingList.remove(bad);
            bad.setCommandStatus(CommandStatus.ERROR.setErrorDetail(error.getErrorReason()));
            synchronized (bad) {
                bad.notifyAll();
            }
        }
    }

    /**
     * Start a thread to monitor for responses from the KLF200 unit and when one
     * is received find a delegate Command from the {@link processingList} to
     * handle the response.
     */
    private void startProcessingResponses() {
        new Thread() {
            @Override
            public void run() {
                logger.trace("Starting the response consumer.");
                while (!queueShutdown) {
                    try {
                        byte[] temp = new byte[CONNECTION_BUFFER_SIZE];
                        int messageLength = klfInputStream.read(temp, 0, temp.length);
                        if (messageLength == -1) {
                            // Most likely the stream has closed, so shutdown
                            // the queue
                            logger.warn("Invalid message recieved, shutting down response processing.");
                            queueShutdown = true;
                        } else {
                            byte[] data = new byte[messageLength];
                            System.arraycopy(temp, 0, data, 0, messageLength);
                            logger.trace("Recieved response: {} - {}",
                                    String.format("0x%04x",
                                            KLFUtils.decodeKLFCommand(KLFUtils.slipRFC1055decode(data))),
                                    KLFUtils.formatBytes(data));

                            byte decoded[] = KLFUtils.slipRFC1055decode(data);
                            if ((null != decoded) && (BaseKLFCommand.validateKLFResponse(decoded))) {
                                short command = KLFUtils.decodeKLFCommand(decoded);
                                if (KLFCommandCodes.GW_ERROR_NTF == command) {
                                    // General error notification received
                                    VeluxErrorResponse error = getErrorFrame(decoded);
                                    logger.error("Error Notification Recieved: {}", error);
                                    handleGeneralError(error);
                                } else {
                                    if (eventNotification.isOnWatchList(command)) {
                                        eventNotification.notifyEvent(command, decoded);
                                    }
                                    BaseKLFCommand cmd = findInProgressCommand(command, decoded);
                                    if (null != cmd) {
                                        cmd.handleResponse(decoded);
                                        switch (cmd.getCommandStatus()) {
                                            case ERROR:
                                                logger.trace("Response is in an error state.",
                                                        cmd.getKLFCommandStructure().getCommandCode());
                                            case COMPLETE:
                                                logger.trace(
                                                        "Response processed. No further responses expected for command {}, notifying observers.",
                                                        cmd.getKLFCommandStructure().getCommandCode());
                                                processingList.remove(cmd);
                                                synchronized (cmd) {
                                                    cmd.notifyAll();
                                                }
                                                break;
                                            case PROCESSING:
                                                // Do nothing, command expecting
                                                // further responses
                                                logger.trace(
                                                        "Response recieved for command {}, but expecting further responses.",
                                                        cmd.getKLFCommandStructure().getCommandCode());
                                                break;
                                            default:
                                                // Should never happen as no other
                                                // states are valid at this stage.
                                                logger.error(
                                                        "An unexpectid condition occurred. A command status of {} was found for {}, but not permitted at this time.",
                                                        cmd.getCommandStatus(),
                                                        cmd.getKLFCommandStructure().getCommandCode());
                                                break;
                                        }
                                    } else {
                                        // Only report an error in the event
                                        // that the command received is not on
                                        // our watch list. Commands that are on
                                        // the watch list are notification
                                        // commands that may have originated as
                                        // a result of a direct user interaction
                                        // with a velux device (such as using a
                                        // remote control to perform a task)
                                        if (!eventNotification.isOnWatchList(command)) {
                                            logger.error(
                                                    "Recieved response but unable to find a matching command to consume the response. Discarding response: {}",
                                                    KLFUtils.formatBytes(decoded));
                                        }
                                    }
                                }
                            } else {
                                logger.error("Unable to SLIP RFC1055 decode the payload recieved, discarding. {}",
                                        KLFUtils.formatBytes(data));
                            }
                        }
                    } catch (IOException e) {
                        // Ignore if queue has been shutdown.
                        if (!queueShutdown) {
                            logger.error("Unexpected error when awaiting a response from KLF200 unit: {}",
                                    e.getMessage());
                        }
                    }
                }
                logger.trace("The response consumer has been shutdown.");
            }
        }.start();
    }

    /**
     * Send a command to be processed, but do not block / wait for the command
     * to complete.
     *
     * @param c
     *              The command to be processed
     * @return true, If successfully added to the queue, false if it was not
     *         possible to add to the processing queue. False would typically
     *         indicate that the queue is full.
     */
    public boolean dispatchCommand(BaseKLFCommand c) {
        if (c.getKLFCommandStructure().getCommandCode() != KLFCommandCodes.GW_PASSWORD_ENTER_REQ) {
            checkInitialised();
        }
        logger.trace("Adding command {} to the command queue.", c.getKLFCommandStructure().getCommandCode());
        boolean ret = commandQueue.offer(c);
        if (ret) {
            logger.trace("Command {} queued, awaiting processing.", c.getKLFCommandStructure().getCommandCode());
            c.setCommandStatus(CommandStatus.QUEUED);
        } else {
            c.setCommandStatus(CommandStatus.ERROR);
            logger.error("Command {} could not be added to the queue.", c.getKLFCommandStructure().getCommandCode());
        }
        return ret;
    }

    /**
     * Similar to {@link dispatchCommand}, however, waits for the command to
     * execute fully before returning to the user.
     *
     * @param c
     *              The command to be processed
     * @return true, If processing is complete, false if processing is
     *         incomplete for any reason. Note: a return value of true only
     *         indicates that processing has finished, it does not signify that
     *         processing was successful. The command object itself needs to be
     *         queried to determine whether or not processing yielded a
     *         successful or expected result.
     */
    public boolean executeCommand(BaseKLFCommand c) {
        return executeCommand(c, this.commandTimeout);
    }

    /**
     * Similar to {@link dispatchCommand}, however, waits for the command to
     * execute fully before returning to the user.
     *
     * @param c
     *                    The command to be processed
     * @param timeout
     *                    How long to wait (in miliseconds) for the command to execute
     * @return true, If processing is complete, false if processing is
     *         incomplete for any reason. Note: a return value of true only
     *         indicates that processing has finished, it does not signify that
     *         processing was successful. The command object itself needs to be
     *         queried to determine whether or not processing yielded a
     *         successful or expected result.
     */
    public boolean executeCommand(BaseKLFCommand c, long timeout) {
        if (dispatchCommand(c)) {
            synchronized (c) {
                try {
                    logger.trace("Waiting for command {} to complete.", c.getKLFCommandStructure().getCommandCode());
                    c.wait(timeout);
                    if ((c.getCommandStatus() == CommandStatus.ERROR)
                            || (c.getCommandStatus() == CommandStatus.COMPLETE)) {
                        // Processing complete (or finished due to error)
                        logger.trace("Command {} has completed.", c.getKLFCommandStructure().getCommandCode());
                        return true;
                    } else {
                        // Processing not completed within the allocated time
                        logger.warn("The command did not complete within the {} second time allocated.",
                                timeout / 1000);
                        c.setCommandStatus(CommandStatus.ERROR
                                .setErrorDetail("The command did not complete within the time allocated."));
                        return false;
                    }
                } catch (InterruptedException e) {
                    logger.warn("An unexpected error occurred while waiting for a command to complete: {}",
                            e.getMessage());
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Used to check if the command processor has been initialised successfully and is therefore available.
     *
     * @return true if it has bee successfully initialised.
     */
    public boolean isAvailable() {
        return this.isInitialised;
    }
}
