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
package org.openhab.binding.mpd.internal.protocol;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.mpd.internal.MPDException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for communicating with the music player daemon through a IP connection
 *
 * @author Stefan Röllin - Initial contribution
 */
@NonNullByDefault
public class MPDConnectionThread extends Thread {

    private static final int RECONNECTION_TIMEOUT = 60000; // 60 seconds

    private final Logger logger = LoggerFactory.getLogger(MPDConnectionThread.class);

    private final MPDResponseListener listener;

    private final String address;
    private final Integer port;
    private final String password;

    private @Nullable Socket socket = null;
    private @Nullable BufferedReader reader = null;
    private @Nullable DataOutputStream writer = null;

    private final List<MPDCommand> pendingCommands = new ArrayList<>();
    private AtomicBoolean isInIdle = new AtomicBoolean(false);
    private AtomicBoolean disposed = new AtomicBoolean(false);

    public MPDConnectionThread(MPDResponseListener listener, String address, Integer port, String password) {
        this.listener = listener;

        this.address = address;
        this.port = port;
        this.password = password;
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
                } catch (IOException e) {
                    updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                } catch (MPDException e) {
                    updateThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                }

                isInIdle.set(false);
                closeSocket();

                if (!disposed.get()) {
                    sleep(RECONNECTION_TIMEOUT);
                }
            }
        } catch (InterruptedException e) {
        }
    }

    /**
     * dispose the connection
     */
    public void dispose() {
        disposed.set(true);
        synchronized (pendingCommands) {
            pendingCommands.add(new MPDCommand("close"));
            sendNoIdleIfInIdle();
            interrupt();
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
        logger.trace("insert command '{}' at position {}", command.getCommand(), position);
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
            }
        }
    }

    private void establishConnection() throws IOException, MPDException {
        openSocket();

        MPDCommand currentCommand = new MPDCommand("connect");
        MPDResponse response = readResponse(currentCommand);

        if (!response.isOk()) {
            throw new MPDException("could not connect");
        }

        if (!password.isEmpty()) {
            currentCommand = new MPDCommand("password", password);
            sendCommand(currentCommand);
            response = readResponse(currentCommand);
            if (!response.isOk()) {
                throw new MPDException("could not authenticate");
            }
        }
    }

    private void openSocket() throws IOException {
        logger.debug("opening connection to {} port {}", this.address, this.port);
        Socket socket = new Socket(this.address, this.port);

        reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        writer = new DataOutputStream(socket.getOutputStream());

        this.socket = socket;
    }

    private void processPendingCommands() throws IOException {
        MPDCommand currentCommand;

        while (!disposed.get()) {
            synchronized (pendingCommands) {
                if (!pendingCommands.isEmpty()) {
                    currentCommand = pendingCommands.remove(0);
                } else {
                    currentCommand = new MPDCommand("idle");
                }

                sendCommand(currentCommand);
                if (currentCommand.getCommand().equals("idle")) {
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
            } catch (IOException e) {
            }
            this.reader = null;
        }

        DataOutputStream writer = this.writer;
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
            }
            this.writer = null;
        }

        Socket socket = this.socket;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
            }
            this.socket = null;
            this.reader = null;
            this.writer = null;
        }
    }

    private void sendCommand(MPDCommand command) throws IOException {
        logger.trace("send command '{}'", command.asLine());
        final DataOutputStream writer = this.writer;
        if (writer != null) {
            String line = command.asLine();
            byte[] bytes = line.getBytes(StandardCharsets.UTF_8);
            writer.write(bytes);
            writer.write('\n');
        } else {
            throw new IOException("writer is null");
        }
    }

    private MPDResponse readResponse(MPDCommand command) throws IOException {
        logger.trace("read response for command '{}'", command.getCommand());
        MPDResponse response = new MPDResponse(command.getCommand());
        boolean done = false;

        final BufferedReader reader = this.reader;
        if (reader != null) {
            while (!done) {
                String line = reader.readLine();
                logger.trace("received line '{}'", line);

                if (line != null) {
                    if (line.startsWith("ACK")) {
                        logger.warn("command '{}' failed with '{}'", command.asLine(), line);
                        response.setFailed();
                        done = true;
                    } else if (line.startsWith("OK")) {
                        done = true;
                    } else {
                        response.addLine(line.trim());
                    }
                } else {
                    isInIdle.set(false);
                    throw new IOException("Receive failed.");
                }
            }
        } else {
            isInIdle.set(false);
            throw new IOException("Reader is null.");
        }

        isInIdle.set(false);

        return response;
    }
}
