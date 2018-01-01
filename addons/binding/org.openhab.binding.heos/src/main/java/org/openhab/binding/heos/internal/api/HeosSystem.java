/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.heos.internal.api;

import static org.openhab.binding.heos.internal.resources.HeosConstants.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.heos.internal.resources.HeosCommands;
import org.openhab.binding.heos.internal.resources.HeosGroup;
import org.openhab.binding.heos.internal.resources.HeosJsonParser;
import org.openhab.binding.heos.internal.resources.HeosPlayer;
import org.openhab.binding.heos.internal.resources.HeosResponse;
import org.openhab.binding.heos.internal.resources.HeosSendCommand;
import org.openhab.binding.heos.internal.resources.Telnet;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosSystem} is handling the main commands, which are
 * send and received by the HEOS system.
 *
 * @author Johannes Einig - Initial contribution
 */

public class HeosSystem {

    private String connectionIP = "";
    private int connectionPort = 0;

    private Telnet commandLine;
    private Telnet eventLine;
    private HeosCommands heosCommand = new HeosCommands();
    private HeosResponse response = new HeosResponse();
    private HeosJsonParser parser = new HeosJsonParser(response);
    private HeosEventController eventController = new HeosEventController(response, heosCommand, this);
    private HeosSendCommand sendCommand = new HeosSendCommand(commandLine, parser, response, eventController);
    private HashMap<String, HeosPlayer> playerMapNew;
    private HashMap<String, HeosGroup> groupMapNew;
    private HashMap<String, HeosPlayer> playerMapOld;
    private HashMap<String, HeosGroup> groupMapOld;
    private HashMap<String, HeosPlayer> removedPlayerMap;
    private HashMap<String, HeosGroup> removedGroupMap;
    private HeosFacade heosApi = new HeosFacade(this, eventController);

    private Logger logger = LoggerFactory.getLogger(HeosSystem.class);

    boolean sendSuccess = false;

    private ScheduledExecutorService keepAlive;
    private KeepALiveRunnable keepAliveRunnable = new KeepALiveRunnable();

    private final int START_DELAY = 30;

    private final int WAIT_TIME_AFTER_RECONNECT = 15000;

    public HeosSystem() {
    }

    /**
     * Method to be used to send a command to the HEOS system.
     *
     * If a ReadException is detected during reading the response
     * to the send command the method tries to send the command
     * a second time.
     * Method returns true if sending and reading the response was
     * successful. It returns false if either the client is not connected
     * or a failure during reading the response occurs.
     * A response reading failure is returned after a timeout during the
     * read command was detected. (catch by a ReadException)
     *
     * @param command the command to be send
     * @return true if sending and reading successful
     */

    public synchronized boolean send(String command) {
        logger.debug("Sending Command: {}", command);
        try {
            if (sendCommand.send(command)) {
                return true;
            } else {
                logger.warn("Could not send message. Client is not connected");
                return false;
            }
        } catch (ReadException e) {
            logger.warn("HEOS System read failure during response. message: {}", e.getMessage());
            logger.debug("HEOS failed command: {}", command);
            logger.debug("HEOS System trys to send command again....");

            try {
                if (sendCommand.send(command)) {
                    return true;
                } else {
                    logger.warn("Could not send message. Client is not connected");
                    return false;
                }
            } catch (ReadException | IOException e1) {
                logger.warn("HEOS System second try sending command not successful");
                // e1.printStackTrace();
                return false;
            }
        } catch (IOException e) {
            logger.warn("IO Exception during send HEOS command with message: {}", e.getMessage());
            // e.printStackTrace();
            return false;
        }
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
            logger.warn("Could not send message. Client is not connected");
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
     * set.
     *
     * @param connectionDelay if set to true system waits for 10 seconds before connecting to event line
     *
     * @return {@code true} if connection is established else returns {@code false}
     */

    public boolean establishConnection(boolean connectionDelay) {
        this.playerMapNew = new HashMap<String, HeosPlayer>();
        this.groupMapNew = new HashMap<String, HeosGroup>();
        this.playerMapOld = new HashMap<String, HeosPlayer>();
        this.groupMapOld = new HashMap<String, HeosGroup>();
        this.removedGroupMap = new HashMap<String, HeosGroup>();
        this.commandLine = new Telnet();
        this.eventLine = new Telnet();

        boolean commandLineConnected = false;
        boolean eventLineConnected = false;

        try {
            commandLineConnected = commandLine.connect(connectionIP, connectionPort);
        } catch (SocketException e) {
            if (e.getMessage().equals("Connection timed out: connect")) {
                retryEstablishConnection();
            }
        } catch (IOException e) {
            logger.warn("IOException - connection trouble {}", e.getMessage());
        }
        if (commandLineConnected) {
            logger.info("HEOS command line connected at IP {} @ port {}", connectionIP, connectionPort);
        } else {
            logger.warn("Could not connect HEOS command line at IP {} @ port {}", connectionIP, connectionPort);
        }
        sendCommand.setTelnetClient(commandLine);
        sendSuccess = send(command().registerChangeEventOFF()); // should be their to clean up starting procedure

        try {
            eventLineConnected = eventLine.connect(connectionIP, connectionPort);
        } catch (SocketException e) {
            if (e.getMessage().equals("Connection timed out: connect")) {
                retryEstablishConnection();
            }
        } catch (IOException e) {
            logger.warn("IOException - connection trouble {}", e.getMessage());
        }
        if (eventLineConnected) {
            logger.info("HEOS event line connected at IP {} @ port {}", connectionIP, connectionPort);
        } else {
            logger.warn("Could not connect HEOS event line at IP {} @ port {}", connectionIP, connectionPort);
        }
        if (connectionDelay) { // Allows the HEOS system to find all need things internally.
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
        sendSuccess = send(command().registerChangeEventOFF());
        sendCommand.setTelnetClient(commandLine);

        if (commandLine.isConnected() && eventLine.isConnected()) {
            return true;
        }
        return false;
    }

    private void retryEstablishConnection() {
        logger.warn("Connection to HEOS-System timed out. Trying again....");
        try {
            Thread.sleep(5000);
            if (commandLine.isConnected()) {
                commandLine.disconnect();
            }
            if (eventLine.isConnected()) {
                eventLine.disconnect();
            }
        } catch (InterruptedException | IOException e) {
            logger.warn("Interrupt Exception {}", e.getMessage());
        }
        establishConnection(true);
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
     *
     */

    public void startHeartBeat(int heartBeatPulse) {
        keepAlive = Executors.newScheduledThreadPool(1);
        keepAliveRunnable = new KeepALiveRunnable();

        @SuppressWarnings("unused")
        ScheduledFuture<?> keepAliveHandler = keepAlive.scheduleAtFixedRate(this.keepAliveRunnable, START_DELAY,
                heartBeatPulse, TimeUnit.SECONDS);
    }

    public synchronized void startEventListener() {
        logger.info("HEOS System Event Listener is booting....");

        if (sendCommand.setTelnetClient(eventLine)) {
            sendSuccess = send(command().registerChangeEventOn());
            logger.info("HEOS System Event Listener is starting....");
            eventLine.startInputListener();
        }

        sendCommand.setTelnetClient(commandLine);
        logger.info("HEOS System Event Listener succsessfully started");

        eventLine.getReadResultListener().addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                parser.parseResult((String) evt.getNewValue());
                eventController.handleEvent(1);
            }
        });
    }

    public void closeConnection() {
        logger.info("Shutting down HEOS Heart Beat");
        keepAlive.shutdown();
        logger.info("Stopping HEOS event line listener");
        eventLine.stopInputListener();
        if (eventLine.isConnected()) {
            logger.debug("HEOS event line is still open closing it....");
            if (eventLine.isConnectionAlive()) {
                sendCommand.setTelnetClient(eventLine);
                send(command().registerChangeEventOFF());
                try {
                    Thread.sleep(300);
                    sendCommand.setTelnetClient(commandLine);
                    logger.info("Disconnecting HEOS event line");
                    eventLine.disconnect();
                    logger.info("Disconnecting HEOS command line");
                    commandLine.disconnect();
                } catch (IOException | InterruptedException e) {
                    logger.error("Failure during closing connection to HEOS with message: {}", e.getMessage());//
                    return;
                }
            }
            logger.info("Connection to HEOS system closed");
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
     *
     */

    public synchronized HeosPlayer getPlayerState(String pid) {
        HeosPlayer heosPlayer = new HeosPlayer();
        send(command().getPlayerInfo(pid));
        while (response.getEvent().getResult().equals(FAIL)) {
            try {
                for (int i = 2; i > 0; i--) {
                    logger.info("HEOS System waiting for player with PID: '{}' to be available. Open tries: {}", pid,
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

        heosPlayer.updatePlayerInfo(response.getPayload().getPayloadList().get(0));
        heosPlayer = updatePlayerState(heosPlayer);
        heosPlayer.setOnline(true);
        return heosPlayer;
    }

    /**
     * This method returns a {@codeHashMap<String pid, HeosPlayer heosPlayer>} with
     * all player found on the network after an connection to the system is
     * established via a bridge.
     *
     * @return a HashMap with all HEOS Player in the network
     */

    public synchronized HashMap<String, HeosPlayer> getAllPlayer() {
        playerMapNew.clear();

        send(command().getPlayers());
        boolean resultIsEmpty = response.getPayload().getPayloadList().isEmpty();

        while (resultIsEmpty) {
            send(command().getPlayers());
            resultIsEmpty = response.getPayload().getPayloadList().isEmpty();
            logger.warn("HEOS System found no players.");
        }
        List<HashMap<String, String>> playerList = response.getPayload().getPayloadList();

        for (HashMap<String, String> player : playerList) {
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
        heosPlayer.setState(response.getEvent().getMessagesMap().get("state"));
        send(command().getMute(pid));
        heosPlayer.setMute(response.getEvent().getMessagesMap().get("state"));
        send(command().getVolume(pid));
        heosPlayer.setLevel(response.getEvent().getMessagesMap().get("level"));
        send(command().getNowPlayingMedia(pid));
        heosPlayer.updateMediaInfo(response.getPayload().getPayloadList().get(0));

        return heosPlayer;
    }

    /**
     * This method searches for all groups which are on the HEOS network
     * and returns a {@code HashMap<String gid, HeosGroup heosGroup>}.
     * Before calling this method a connection via a bridge has to be
     * established
     *
     * @return a HashMap with all HEOS groups
     */

    public synchronized HashMap<String, HeosGroup> getGroups() {
        groupMapNew.clear();

        send(command().getGroups());

        if (response.getPayload().getPayloadList().isEmpty()) {
            removedGroupMap = compareGroupMaps(groupMapNew, groupMapOld);
            groupMapOld.putAll(groupMapNew);
            return groupMapNew;
        }
        List<HashMap<String, String>> groupList = response.getPayload().getPayloadList();
        int groupCounter = 0;

        for (HashMap<String, String> group : groupList) {
            HeosGroup heosGroup = new HeosGroup();
            heosGroup.updateGroupInfo(group);
            heosGroup.updateGroupPlayers(response.getPayload().getPlayerList().get(groupCounter));

            logger.info("Found: Group {} with {} Players", heosGroup.getName(),
                    response.getPayload().getPlayerList().get(groupCounter).size());

            int playerCount = response.getPayload().getPlayerList().get(groupCounter).size();

            // Defining the Group leader

            for (int i = 0; i < playerCount; i++) {
                HashMap<String, String> playerMap = new HashMap<>();
                playerMap = response.getPayload().getPlayerList().get(groupCounter).get(i);
                for (String key : playerMap.keySet()) {
                    if (key.equals("role")) {
                        if (playerMap.get(key).equals("leader")) {
                            String leader = playerMap.get("pid");
                            heosGroup.setLeader(leader);
                        }
                    }
                }
            }

            // Switched to NameHash value
            heosGroup.generateGroupUID();

            groupMapNew.put(heosGroup.getNameHash(), heosGroup);
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
     *
     */

    public synchronized HeosGroup getGroupState(HeosGroup heosGroup) {
        // HeosGroup heosGroup = new HeosGroup();
        String gid = heosGroup.getGid();
        send(command().getGroupInfo(gid));

        // During start up sometimes the system has not collected all information
        // and sends a failure.

        while (response.getEvent().getResult().equals(FAIL)) {
            try {
                for (int i = 2; i > 0; i--) {
                    logger.info("HEOS System waiting for group with PID: '{}' to be available. Open tries: {}", gid, i);
                    Thread.sleep(3000);
                    send(command().getGroupInfo(gid));
                }
                heosGroup.setOnline(false);
                return heosGroup;
            } catch (InterruptedException e) {
                logger.debug("Interrupted Exception - Message: {}", e.getMessage());
            }
        }

        heosGroup.setOnline(true);
        heosGroup.updateGroupInfo(response.getPayload().getPayloadList().get(0));
        heosGroup.updateGroupPlayers((response.getPayload().getPlayerList().get(0)));
        send(command().getPlayState(gid));
        heosGroup.setState(response.getEvent().getMessagesMap().get("state"));
        send(command().getGroupMute(gid));
        heosGroup.setMute(response.getEvent().getMessagesMap().get("state"));
        send(command().getGroupVolume(gid));
        heosGroup.setLevel(response.getEvent().getMessagesMap().get("level"));
        send(command().getNowPlayingMedia(gid));
        heosGroup.updateMediaInfo(response.getPayload().getPayloadList().get(0));

        return heosGroup;
    }

    private HashMap<String, HeosGroup> compareGroupMaps(HashMap<String, HeosGroup> mapNew,
            HashMap<String, HeosGroup> mapOld) {
        HashMap<String, HeosGroup> removedItems = new HashMap<String, HeosGroup>();
        for (String key : mapOld.keySet()) {
            if (!mapNew.containsKey(key)) {
                removedItems.put(key, mapOld.get(key));
            }
        }
        return removedItems;
    }

    private HashMap<String, HeosPlayer> comparePlayerMaps(HashMap<String, HeosPlayer> mapNew,
            HashMap<String, HeosPlayer> mapOld) {
        HashMap<String, HeosPlayer> removedItems = new HashMap<String, HeosPlayer>();
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
     *
     * @param map a HashMap with {@code heosGroup.getNameHash(), heosGroup}
     */

    public void addHeosGroupToOldGroupMap(HashMap<String, HeosGroup> map) {
        groupMapOld.putAll(map);
    }

    public List<HashMap<String, String>> getFavorits() {
        send(command().browseSource(FAVORIT_SID));
        return response.getPayload().getPayloadList();
    }

    public List<String> getPlaylists() {
        List<String> playlistsList = new ArrayList<String>();
        send(command().browseSource(PLAYLISTS_SID));
        List<HashMap<String, String>> payload = response.getPayload().getPayloadList();
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

    public HashMap<String, HeosPlayer> getPlayerMap() {
        return playerMapNew;
    }

    public HashMap<String, HeosGroup> getGroupMap() {
        return groupMapNew;
    }

    public HashMap<String, HeosGroup> getGroupsRemoved() {
        return removedGroupMap;
    }

    public HashMap<String, HeosPlayer> getPlayerRemoved() {
        return removedPlayerMap;
    }

    /**
     * A class which provides a runnable for the HEOS Heart Beat
     *
     * @author Johannes
     *
     */

    public class KeepALiveRunnable implements Runnable {
        @Override
        public void run() {
            try {
                if (sendCommand.isConnectionAlive()) {
                    logger.debug("Sending Heos Heart Beat");
                    if (!sendCommand.send(command().heartBeat())) {
                        logger.warn("Connection to HEOS Network lost!");
                        restartConnection();
                    }
                } else {
                    logger.warn("Connection to HEOS Network lost!");
                    restartConnection();
                }
                // catches a failure during a heart beat send message if connection was
                // getting lost between last Heart Beat but Bridge is online again and not
                // detected by isConnectionAlive()
            } catch (ReadException | IOException e) {
                logger.warn("Failure during HEOS Heart Beat command with message: {}", e.getMessage());
                restartConnection();
            }
        }

        private void restartConnection() {
            closeConnection();
            eventController.connectionToSystemLost();

            try {
                while (!sendCommand.isConnectionAlive()) {
                    logger.info("Trying to reconnect to HEOS Network...");
                    Thread.sleep(5000);
                }
                logger.info("Reconnecting to Bridge with IP {} @ port {}", connectionIP, connectionPort);
                Thread.sleep(15000); // Waiting time is needed because System needs some time to start up
            } catch (InterruptedException e) {
                logger.warn("Failure during restart procedure. Trying again simplified....");
                eventController.connectionToSystemRestored();
                return;
            }
            eventController.connectionToSystemRestored();
        }
    }
}
