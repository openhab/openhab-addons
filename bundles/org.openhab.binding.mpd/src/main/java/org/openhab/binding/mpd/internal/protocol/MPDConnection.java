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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mpd.internal.handler.MPDEventListener;
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
public class MPDConnection implements MPDResponseListener {

    private static final int DISPOSE_TIMEOUT_MS = 1000;

    private final Logger logger = LoggerFactory.getLogger(MPDConnection.class);

    private final MPDEventListener listener;

    private @Nullable MPDConnectionThread connectionThread = null;

    /**
     * Constructor
     *
     * @param address the IP address of the music player daemon
     * @param port the TCP port to be used
     * @param password the password to connect to the music player daemon
     */
    public MPDConnection(MPDEventListener listener) {
        this.listener = listener;
    }

    /**
     * start the connection
     *
     * @param address the IP address of the music player daemon
     * @param port the TCP port to be used
     * @param password the password to connect to the music player daemon
     * @param threadName the name of the thread
     */
    public void start(String address, Integer port, String password, String threadName) {
        if (connectionThread == null) {
            final MPDConnectionThread connectionThread = new MPDConnectionThread(this, address, port, password);
            connectionThread.setName(threadName);
            connectionThread.start();
            this.connectionThread = connectionThread;
        }
    }

    /**
     * dispose the connection
     */
    public void dispose() {
        final MPDConnectionThread connectionThread = this.connectionThread;
        if (connectionThread != null) {
            connectionThread.dispose();
            connectionThread.interrupt();
            try {
                connectionThread.join(DISPOSE_TIMEOUT_MS);
            } catch (InterruptedException ignore) {
            }
            this.connectionThread = null;
        }
    }

    /**
     * send a command to the music player daemon
     *
     * @param command command to send
     * @param parameter parameter of command
     */
    public void sendCommand(String command, String... parameter) {
        addCommand(new MPDCommand(command, parameter));
    }

    /**
     * play
     */
    public void play() {
        sendCommand("play");
    }

    /**
     * pause the music player daemon
     */
    public void pause() {
        addCommand(new MPDCommand("pause", 1));
    }

    /**
     * play next track
     */
    public void playNext() {
        sendCommand("next");
    }

    /**
     * play previous track
     */
    public void playPrevious() {
        sendCommand("previous");
    }

    /**
     * stop the music player daemon
     */
    public void stop() {
        sendCommand("stop");
    }

    /**
     * update status
     */
    public void updateStatus() {
        sendCommand("status");
    }

    /**
     * update information regarding current song
     */
    public void updateCurrentSong() {
        sendCommand("currentsong");
    }

    /**
     * set volume
     *
     * @param volume set new volume
     */
    public void setVolume(int volume) {
        addCommand(new MPDCommand("setvol", volume));
    }

    private void addCommand(MPDCommand command) {
        MPDConnectionThread connectionThread = this.connectionThread;
        if (connectionThread != null) {
            connectionThread.addCommand(command);
        } else {
            logger.debug("could not add command {} since thing offline", command.getCommand());
        }
    }

    @Override
    public void updateThingStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String cause) {
        listener.updateThingStatus(status, statusDetail, cause);
    }

    @Override
    public void onResponse(MPDResponse response) {
        switch (response.getCommand()) {
            case "idle":
                handleResponseIdle(response);
                break;
            case "status":
                handleResponseStatus(response);
                break;
            case "currentsong":
                handleResponseCurrentSong(response);
                break;
            default:
                break;
        }
    }

    private void handleResponseCurrentSong(MPDResponse response) {
        MPDSong song = new MPDSong(response);
        listener.updateMPDSong(song);
    }

    private void handleResponseIdle(MPDResponse response) {
        boolean updateStatus = false;
        boolean updateCurrentSong = false;
        for (String line : response.getLines()) {
            if (line.startsWith("changed:")) {
                line = line.substring(8).trim();
                switch (line) {
                    case "player":
                        updateStatus = true;
                        updateCurrentSong = true;
                        break;
                    case "mixer":
                        updateStatus = true;
                        break;
                    case "playlist":
                        updateCurrentSong = true;
                        break;
                }
            }
        }

        if (updateStatus) {
            updateStatus();
        }
        if (updateCurrentSong) {
            updateCurrentSong();
        }
    }

    private void handleResponseStatus(MPDResponse response) {
        MPDStatus song = new MPDStatus(response);
        listener.updateMPDStatus(song);
    }
}
