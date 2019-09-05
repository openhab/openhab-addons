/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.heos.internal.api;

import static org.openhab.binding.heos.internal.resources.HeosConstants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// import org.eclipse.smarthome.core.thing.ThingStatus;
// import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.openhab.binding.heos.internal.resources.HeosCommands;
import org.openhab.binding.heos.internal.resources.HeosGroup;
import org.openhab.binding.heos.internal.resources.HeosPlayer;
import org.openhab.binding.heos.internal.resources.HeosResponseDecoder;
import org.openhab.binding.heos.internal.resources.HeosSendCommand;
import org.openhab.binding.heos.internal.resources.Telnet;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosSystem} is handling the main commands, which are
 * sent and received by the HEOS system.
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosSystem {
    private final Logger logger = LoggerFactory.getLogger(HeosSystem.class);

    private static final int START_DELAY = 30;
    private static final int WAIT_TIME_AFTER_RECONNECT = 15000;

    private String connectionIP = "";
    private int connectionPort;

    private Telnet commandLine;
    private Telnet eventLine;
    private HeosCommands heosCommand = new HeosCommands();
    private HeosResponseDecoder heosDecoder = new HeosResponseDecoder();
    private HeosEventController eventController = new HeosEventController(heosDecoder, heosCommand, this);
    private HeosSendCommand sendCommand = new HeosSendCommand(commandLine, heosDecoder, eventController);

    private Map<String, HeosPlayer> playerMapNew = new HashMap<>();
    private Map<String, HeosGroup> groupMapNew = new HashMap<>();
    private Map<String, HeosPlayer> playerMapOld = new HashMap<>();
    private Map<String, HeosGroup> groupMapOld = new HashMap<>();
    private Map<String, HeosPlayer> removedPlayerMap = new HashMap<>();
    private Map<String, HeosGroup> removedGroupMap = new HashMap<>();
    private HeosFacade heosApi = new HeosFacade(this, eventController);

    private ScheduledExecutorService keepAlive;
    private KeepAliveRunnable keepAliveRunnable = new KeepAliveRunnable();

    /**
     * Method to be used to send a command to the HEOS system.
     *
     * If a ReadException is detected during reading the response
     * to the send command the method tries to send the command
     * a second time.
     * Method returns true if sending the message and reading the response was
     * successful. It returns false if either the client is not connected
     * or a failure during reading occurs.
     * A response reading failure is returned after a timeout during the
     * read command was detected. (catch by a ReadException)
     *
     * @param command the command to be send
     * @return true if sending and reading successful
     */
    public synchronized boolean send(String command) {
        for (int i = 0; i < 2;) {
            logger.debug("Sending Command: {}", command);
            try {
                if (sendCommand.send(command)) {
                    return true;
                } else {
                    logger.debug("Could not send message. HEOS bridge is not connected");
//                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Could not send message. HEOS bridge is not connected");
                    return false;
                }
            } catch (ReadException e) {
                logger.debug("HEOS System read failure during response. message: {}", e.getMessage());
                logger.debug("HEOS failed command: {}", command);
                i++;
                if (i < 2) {
                    logger.debug("HEOS System trys to send command again....");
                } else {
                    logger.debug("Could not send command. Please check the system connection");
                }
            } catch (IOException e) {
                logger.debug("IO Exception during send HEOS command with message: {}", e.getMessage());
//                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                return false;
            }
        }
//        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Could not send command. Please check the system connection");
        return false;
    }

    /**
     * This method shall only be used if no response from network
     * is expected. Else the read buffer is not cleared
     *
     * @param command
     * @return true if send was successful
     */
    public boolean sendWithoutResponse(String command) {
        if (sendCommand.sendWithoutResponse(command)) {
            return true;
        } else {
            logger.debug("Could not send message. Client is not connected");
            return false;
        }
    }

    /**
     * This method returns a class from type HeosCommands. Within this class all
     * possible commands to control the HEOS network are defined.
     *
     * @return a class with all HEOS Commands
     */
    public HeosCommands command() {
        return heosCommand;
    }

    /**
     * Establishes the connection to the HEOS-Network if IP and Port is
     * set. The caller has to handle the retry to establish the connection
     * if the method returns {@code false}.
     *
     * @param connectionDelay if set to true system waits for 10 seconds before connecting to event line
     * @return {@code true} if connection is established else returns {@code false}
     */
    public boolean establishConnection(boolean connectionDelay) {
        this.commandLine = new Telnet();
        this.eventLine = new Telnet();

        boolean commandLineConnected = false;
        boolean eventLineConnected = false;

        try {
            commandLineConnected = commandLine.connect(connectionIP, connectionPort);
        } catch (IOException e) {
            logger.debug("Exception during connection to bridge with message: {}", e.getMessage());
//            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.debug("Could not connect HEOS command line at IP {} @ port {}", connectionIP, connectionPort);
            commandLineConnected = false;
        }
        if (commandLineConnected) {
            logger.debug("HEOS command line connected at IP {} @ port {}", connectionIP, connectionPort);
            sendCommand.setTelnetClient(commandLine);
            send(command().registerChangeEventOFF());
        }

        try {
            eventLineConnected = eventLine.connect(connectionIP, connectionPort);
        } catch (IOException e) {
            logger.debug("Exception during connection to bridge with message: {}", e.getMessage());
//            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            logger.debug("Could not connect HEOS event line at IP {} @ port {}", connectionIP, connectionPort);
            eventLineConnected = false;
        }
        if (eventLineConnected) {
            logger.debug("HEOS event line connected at IP {} @ port {}", connectionIP, connectionPort);
            if (connectionDelay) {
                // Allows the HEOS system to find all needed things internally.
                // During the first connection after a restart or long sleep of the HEOS system,
                // the system needs time to activate all internal processes to provide the information
                // to the bridge
                try {
                    Thread.sleep(WAIT_TIME_AFTER_RECONNECT);
                } catch (InterruptedException e) {
                    logger.debug("Thread.sleep interrupt during waiting time for HEOS Network");
                }
            }
            sendCommand.setTelnetClient(eventLine);
            send(command().registerChangeEventOFF());
        }
        // Setting back the TelnetClient to the commandLine so that all
        // commands are send via the commandLine.
        sendCommand.setTelnetClient(commandLine);
        return commandLine.isConnected() && eventLine.isConnected();
    }

    /**
     * Starts the HEOS Heart Beat. This held the connection open even
     * if no data is transmitted. If the connection to the HEOS system
     * is lost, the method reconnects to the HEOS system by calling the
     * {@code establishConnection()} method. If the connection is lost or
     * reconnect the method fires a bridgeEvent via the {@code HeosEvenController.class}
     *
     * @see establishConnection()
     * @see HeosEvenController.class
     */
    public void startHeartBeat(int heartbeatPulse) {
        keepAlive = Executors.newScheduledThreadPool(1);
        keepAlive.scheduleWithFixedDelay(this.keepAliveRunnable, START_DELAY, heartbeatPulse, TimeUnit.SECONDS);
    }

    public synchronized void startEventListener() {
        logger.debug("HEOS System Event Listener is booting....");

        if (sendCommand.setTelnetClient(eventLine)) {
            send(command().registerChangeEventOn());
            logger.debug("HEOS System Event Listener is starting....");
            eventLine.startInputListener();
        }

        sendCommand.setTelnetClient(commandLine);
        logger.debug("HEOS System Event Listener succsessfully started");

        eventLine.getReadResultListener().addPropertyChangeListener(evt -> {
            heosDecoder.getHeosJsonParser().parseResult((String) evt.getNewValue());
            eventController.handleEvent(1);
        });
    }

    public void closeConnection() {
        logger.debug("Shutting down HEOS Heart Beat");
        if (keepAlive != null) {
            keepAlive.shutdown();
        }
        logger.debug("Stopping HEOS event line listener");
        eventLine.stopInputListener();
        if (eventLine.isConnected()) {
            logger.debug("HEOS event line is still open closing it....");
            if (eventLine.isConnectionAlive()) {
                sendCommand.setTelnetClient(eventLine);
                send(command().registerChangeEventOFF());
                try {
                    Thread.sleep(300);
                    sendCommand.setTelnetClient(commandLine);
                    logger.debug("Disconnecting HEOS event line");
                    eventLine.disconnect();
                    logger.debug("Disconnecting HEOS command line");
                    commandLine.disconnect();
                } catch (IOException | InterruptedException e) {
                    logger.error("Failure during closing connection to HEOS with message: {}", e.getMessage());//
                    return;
                }
            }
            logger.debug("Connection to HEOS system closed");
        }
    }

    /**
     * This method update the current state of the HEOS Player
     * like the play state title and so on. This method updates all
     * states and shall be used for initialization only. For ongoing
     * updates use the eventListener
     *
     * @param pid Player PID from the player
     * @return a HEOS Player with the updated states
     */
    public synchronized HeosPlayer getPlayerState(String pid) {
        HeosPlayer heosPlayer = new HeosPlayer();
        send(command().getPlayerInfo(pid));
        while (heosDecoder.getSendResult().equals(FAIL)) {
            try {
                for (int i = 2; i > 0; i--) {
                    logger.debug("HEOS System waiting for player with PID: '{}' to be available. Open tries: {}", pid,
                            i);
                    Thread.sleep(3000);
                    send(command().getPlayerInfo(pid));
                }
                heosPlayer.setOnline(false);
                return heosPlayer;
            } catch (InterruptedException e) {
                logger.debug("Interrupted Exception - Message: {}", e.getMessage());
            }
        }
        heosPlayer.updatePlayerInfo(heosDecoder.getPayloadList().get(0));
        heosPlayer = updatePlayerState(heosPlayer);
        heosPlayer.setOnline(true);
        return heosPlayer;
    }

    /**
     * This method returns a {@code Map<String pid, HeosPlayer heosPlayer>} with
     * all player found on the network after an connection to the system is
     * established via a bridge.
     *
     * @return a HashMap with all HEOS Player in the network
     */
    public synchronized Map<String, HeosPlayer> getAllPlayer() {
        playerMapNew.clear();

        send(command().getPlayers());
        boolean resultIsEmpty = heosDecoder.getPayloadList().isEmpty();

        while (resultIsEmpty) {
            send(command().getPlayers());
            resultIsEmpty = heosDecoder.getPayloadList().isEmpty();
            logger.debug("HEOS System found no players.");
        }
        List<Map<String, String>> playerList = heosDecoder.getPayloadList();

        for (Map<String, String> player : playerList) {
            HeosPlayer heosPlayer = new HeosPlayer();
            heosPlayer.updatePlayerInfo(player);
            playerMapNew.put(heosPlayer.getPid(), heosPlayer);
            removedPlayerMap = comparePlayerMaps(playerMapNew, playerMapOld);
            playerMapOld.clear();
            playerMapOld.putAll(playerMapNew);
        }
        return playerMapNew;
    }

    private synchronized HeosPlayer updatePlayerState(HeosPlayer heosPlayer) {
        String pid = heosPlayer.getPid();
        send(command().getPlayState(pid));
        heosPlayer.setState(heosDecoder.getPlayState());
        send(command().getMute(pid));
        heosPlayer.setMute(heosDecoder.getPlayerMuteState());
        send(command().getVolume(pid));
        heosPlayer.setLevel(heosDecoder.getPlayerVolume());
        send(command().getNowPlayingMedia(pid));
        heosPlayer.updateMediaInfo(heosDecoder.getNowPlayingMedia());
        send(command().getPlayMode(pid));
        heosPlayer.setShuffle(heosDecoder.getShuffleMode());
        heosPlayer.setRepeatMode(heosDecoder.getRepeateMode());
        return heosPlayer;
    }

    /**
     * This method searches for all groups which are on the HEOS network
     * and returns a {@code Map<String gid, HeosGroup heosGroup>}.
     * Before calling this method a connection via a bridge has to be
     * established
     *
     * @return a HashMap with all HEOS groups
     */
    public synchronized Map<String, HeosGroup> getGroups() {
        groupMapNew.clear();
        removedGroupMap.clear();
        send(command().getGroups());
        if (heosDecoder.payloadListIsEmpty()) {
            removedGroupMap = compareGroupMaps(groupMapNew, groupMapOld);
            groupMapOld.clear();
            return groupMapNew;
        }
        List<Map<String, String>> groupList = heosDecoder.getPayloadList();
        int groupCounter = 0;

        for (Map<String, String> group : groupList) {
            HeosGroup heosGroup = new HeosGroup();
            heosGroup.updateGroupInfo(group);
            heosGroup.updateGroupPlayers(heosDecoder.getPlayerList().get(groupCounter));
            logger.debug("Found: Group {} with {} Players", heosGroup.getName(),
                    heosDecoder.getPlayerList().get(groupCounter).size());
            groupMapNew.put(heosGroup.getGroupMemberHash(), heosGroup);
            removedGroupMap = compareGroupMaps(groupMapNew, groupMapOld);
            groupMapOld.clear(); // clear the old map so that only the currently available groups are added in the next
                                 // step.
            groupMapOld.putAll(groupMapNew);
            groupCounter++;
        }
        return groupMapNew;
    }

    /**
     * This method update the current state of the HEOS Group
     * like the play state title and so on. This method updates all
     * states and shall be used for initialization only. For ongoing
     * updates use the eventListener
     *
     * @param gid Group GID from the group
     * @return a HEOS group with the updated states
     */
    public synchronized HeosGroup getGroupState(HeosGroup heosGroup) {
        String gid = heosGroup.getGid();
        send(command().getGroupInfo(gid));

        // During start up sometimes the system has not collected all information
        // and sends a failure.

        while (heosDecoder.getSendResult().equals(FAIL)) {
            try {
                for (int i = 2; i > 0; i--) {
                    logger.debug("HEOS System waiting for group with PID: '{}' to be available. Open tries: {}", gid,
                            i);
                    Thread.sleep(1500);
                    send(command().getGroupInfo(gid));
                }
                heosGroup.setOnline(false);
                return heosGroup;
            } catch (InterruptedException e) {
                logger.debug("Interrupted Exception - Message: {}", e.getMessage());
            }
        }
        heosGroup.setOnline(true);
        heosGroup.updateGroupInfo(heosDecoder.getPayloadList().get(0));
        heosGroup.updateGroupPlayers((heosDecoder.getPlayerList().get(0)));
        send(command().getPlayState(gid));
        heosGroup.setState(heosDecoder.getPlayState());
        send(command().getGroupMute(gid));
        heosGroup.setMute(heosDecoder.getGroupMute());
        send(command().getGroupVolume(gid));
        heosGroup.setLevel(heosDecoder.getGroupVolume());
        send(command().getNowPlayingMedia(gid));
        heosGroup.updateMediaInfo(heosDecoder.getNowPlayingMedia());
        send(command().getPlayMode(gid));
        heosGroup.setShuffle(heosDecoder.getShuffleMode());
        heosGroup.setRepeatMode(heosDecoder.getRepeateMode());
        return heosGroup;
    }

    private Map<String, HeosGroup> compareGroupMaps(Map<String, HeosGroup> mapNew, Map<String, HeosGroup> mapOld) {
        Map<String, HeosGroup> removedItems = new HashMap<>();
        for (String key : mapOld.keySet()) {
            if (!mapNew.containsKey(key)) {
                removedItems.put(key, mapOld.get(key));
            }
        }
        return removedItems;
    }

    private Map<String, HeosPlayer> comparePlayerMaps(Map<String, HeosPlayer> mapNew, Map<String, HeosPlayer> mapOld) {
        Map<String, HeosPlayer> removedItems = new HashMap<>();
        for (String key : mapOld.keySet()) {
            if (!mapNew.containsKey(key)) {
                removedItems.put(key, mapOld.get(key));
            }
        }
        return removedItems;
    }

    /**
     * Be used to fill the map which contains old Groups at startup
     * with existing HEOS groups.
     *
     * @param map a Map with {@code heosGroup.getNameHash(), heosGroup}
     */
    public void addHeosGroupToOldGroupMap(String hashValue, HeosGroup heosGroup) {
        groupMapOld.put(hashValue, heosGroup);
    }

    public List<Map<String, String>> getFavorites() {
        send(command().browseSource(FAVORIT_SID));
        return heosDecoder.getPayloadList();
    }

    public List<String> getPlaylists() {
        List<String> playlistsList = new ArrayList<>();
        send(command().browseSource(PLAYLISTS_SID));
        List<Map<String, String>> payload = heosDecoder.getPayloadList();
        for (int i = 0; i < payload.size(); i++) {
            playlistsList.add(payload.get(i).get(CID));
        }
        return playlistsList;
    }

    public HeosFacade getAPI() {
        return heosApi;
    }

    public String getConnectionIP() {
        return connectionIP;
    }

    public void setConnectionIP(String connectionIP) {
        this.connectionIP = connectionIP;
    }

    public int getConnectionPort() {
        return connectionPort;
    }

    public void setConnectionPort(int connectionPort) {
        this.connectionPort = connectionPort;
    }

    public Map<String, HeosPlayer> getPlayerMap() {
        return playerMapNew;
    }

    public Map<String, HeosGroup> getGroupMap() {
        return groupMapNew;
    }

    public Map<String, HeosGroup> getGroupsRemoved() {
        return removedGroupMap;
    }

    public Map<String, HeosPlayer> getPlayerRemoved() {
        return removedPlayerMap;
    }

    /**
     * A class which provides a runnable for the HEOS Heart Beat
     *
     * @author Johannes Einig
     */
    public class KeepAliveRunnable implements Runnable {
        @Override
        public void run() {
            try {
                if (sendCommand.isConnectionAlive()) {
                    logger.debug("Sending Heos Heart Beat");
                    if (!sendCommand.send(command().heartbeat())) {
                        logger.debug("Connection to HEOS Network lost!");
                        restartConnection();
                    }
                } else {
                    logger.debug("Connection to HEOS Network lost!");
                    restartConnection();
                }
                // catches a failure during a heart beat send message if connection was
                // getting lost between last Heart Beat but Bridge is online again and not
                // detected by isConnectionAlive()
            } catch (ReadException | IOException e) {
                logger.debug("Failure during HEOS Heart Beat command with message: {}", e.getMessage());
                restartConnection();
            }
        }

        private void restartConnection() {
            closeConnection();
            eventController.connectionToSystemLost();
            try {
                while (!sendCommand.isConnectionAlive()) {
                    logger.debug("Trying to reconnect to HEOS Network...");
                    Thread.sleep(5000);
                }
                logger.debug("Reconnecting to Bridge with IP {} @ port {}", connectionIP, connectionPort);
                Thread.sleep(15000); // Waiting time is needed because System needs some time to start up
            } catch (InterruptedException e) {
                logger.debug("Failure during restart procedure. Trying again simplified....");
                eventController.connectionToSystemRestored();
                return;
            }
            eventController.connectionToSystemRestored();
        }
    }
}
