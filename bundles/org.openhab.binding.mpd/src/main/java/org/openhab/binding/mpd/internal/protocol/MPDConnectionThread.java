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
package org.openhab.binding.mpd.internal.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mpd.internal.MPDException;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for communicating with the music player daemon through an IP connection
 *
 * @author Stefan Röllin - Initial contribution
 */
@NonNullByDefault
public class MPDConnectionThread extends Thread {

    private static final int RECONNECTION_TIMEOUT_SEC = 60;

    private final Logger logger = LoggerFactory.getLogger(MPDConnectionThread.class);

    private final MPDResponseListener listener;

    private final String address;
    private final Integer port;
    private final String password;

    private @Nullable Socket socket = null;
    private @Nullable InputStreamReader inputStreamReader = null;
    private @Nullable BufferedReader reader = null;

    private final List<MPDCommand> pendingCommands = new ArrayList<>();
    private AtomicBoolean isInIdle = new AtomicBoolean(false);
    private AtomicBoolean disposed = new AtomicBoolean(false);

    public MPDConnectionThread(MPDResponseListener listener, String address, Integer port, String password) {
        this.listener = listener;
        this.address = address;
        this.port = port;
        this.password = password;
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            while (!disposed.get()) {
                try {
                    synchronized (pendingCommands) {
                        pendingCommands.clear();
                        pendingCommands.add(new MPDCommand("status"));
                        pendingCommands.add(new MPDCommand("currentsong"));
                    }

                    establishConnection();
                    updateThingStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

                    processPendingCommands();
                } catch (UnknownHostException e) {
                    updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "Unknown host " + address);
                } catch (IOException e) {
                    updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                } catch (MPDException e) {
                    updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                }

                isInIdle.set(false);
                closeSocket();

                if (!disposed.get()) {
                    sleep(RECONNECTION_TIMEOUT_SEC * 1000);
                }
            }
        } catch (InterruptedException ignore) {
        }
    }

    /**
     * dispose the connection
     */
    public void dispose() {
        disposed.set(true);
        Socket socket = this.socket;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignore) {
            }
            this.socket = null;
        }
    }

    /**
     * add a command to the pending commands queue
     *
     * @param command command to add
     */
    public void addCommand(MPDCommand command) {
        insertCommand(command, -1);
    }

    private void insertCommand(MPDCommand command, int position) {
        logger.debug("insert command '{}' at position {}", command.getCommand(), position);
        int index = position;
        synchronized (pendingCommands) {
            if (index < 0) {
                index = pendingCommands.size();
            }
            pendingCommands.add(index, command);
            sendNoIdleIfInIdle();
        }
    }

    private void updateThingStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        if (!disposed.get()) {
            listener.updateThingStatus(status, statusDetail, description);
        }
    }

    private void sendNoIdleIfInIdle() {
        if (isInIdle.compareAndSet(true, false)) {
            try {
                sendCommand(new MPDCommand("noidle"));
            } catch (IOException e) {
                logger.debug("sendCommand(noidle) failed", e);
            }
        }
    }

    private void establishConnection() throws UnknownHostException, IOException, MPDException {
        openSocket();

        MPDCommand currentCommand = new MPDCommand("connect");
        MPDResponse response = readResponse(currentCommand);

        if (!response.isOk()) {
            throw new MPDException("Failed to connect to " + this.address + ":" + this.port);
        }

        if (!password.isEmpty()) {
            currentCommand = new MPDCommand("password", password);
            sendCommand(currentCommand);
            response = readResponse(currentCommand);
            if (!response.isOk()) {
                throw new MPDException("Could not authenticate, please validate your password");
            }
        }
    }

    private void openSocket() throws UnknownHostException, IOException, MPDException {
        logger.debug("opening connection to {} port {}", address, port);

        if (address.isEmpty()) {
            throw new MPDException("The parameter 'ipAddress' is missing.");
        }
        if (port < 1 || port > 65335) {
            throw new MPDException("The parameter 'port' has an invalid value.");
        }

        Socket socket = new Socket(address, port);

        inputStreamReader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
        reader = new BufferedReader(inputStreamReader);

        this.socket = socket;
    }

    private void processPendingCommands() throws IOException, MPDException {
        MPDCommand currentCommand;

        while (!disposed.get()) {
            synchronized (pendingCommands) {
                if (!pendingCommands.isEmpty()) {
                    currentCommand = pendingCommands.remove(0);
                } else {
                    currentCommand = new MPDCommand("idle");
                }

                sendCommand(currentCommand);
                if ("idle".equals(currentCommand.getCommand())) {
                    isInIdle.set(true);
                }
            }

            MPDResponse response = readResponse(currentCommand);
            if (!response.isOk()) {
                insertCommand(new MPDCommand("clearerror"), 0);
            }
            listener.onResponse(response);
        }
    }

    private void closeSocket() {
        logger.debug("Closing socket");
        BufferedReader reader = this.reader;
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException ignore) {
            }
            this.reader = null;
        }

        InputStreamReader inputStreamReader = this.inputStreamReader;
        if (inputStreamReader != null) {
            try {
                inputStreamReader.close();
            } catch (IOException ignore) {
            }
            this.inputStreamReader = null;
        }

        Socket socket = this.socket;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignore) {
            }
            this.socket = null;
        }
    }

    private void sendCommand(MPDCommand command) throws IOException {
        logger.trace("send command '{}'", command);
        final Socket socket = this.socket;
        if (socket != null) {
            String line = command.asLine();
            socket.getOutputStream().write(line.getBytes(StandardCharsets.UTF_8));
            socket.getOutputStream().write('\n');
        } else {
            throw new IOException("Connection closed unexpectedly.");
        }
    }

    private MPDResponse readResponse(MPDCommand command) throws IOException, MPDException {
        logger.trace("read response for command '{}'", command.getCommand());
        MPDResponse response = new MPDResponse(command.getCommand());
        boolean done = false;

        final BufferedReader reader = this.reader;
        if (reader != null) {
            while (!done) {
                String line = reader.readLine();
                logger.trace("received line '{}'", line);

                if (line != null) {
                    if (line.startsWith("ACK [4")) {
                        logger.warn("command '{}' failed with permission error '{}'", command, line);
                        isInIdle.set(false);
                        throw new MPDException(
                                "Please validate your password and/or your permissions on the Music Player Daemon.");
                    } else if (line.startsWith("ACK")) {
                        logger.warn("command '{}' failed with '{}'", command, line);
                        response.setFailed();
                        done = true;
                    } else if (line.startsWith("OK")) {
                        done = true;
                    } else {
                        response.addLine(line.trim());
                    }
                } else {
                    isInIdle.set(false);
                    throw new IOException("Communication failed unexpectedly.");
                }
            }
        } else {
            isInIdle.set(false);
            throw new IOException("Connection closed unexpectedly.");
        }

        isInIdle.set(false);

        return response;
    }
}
