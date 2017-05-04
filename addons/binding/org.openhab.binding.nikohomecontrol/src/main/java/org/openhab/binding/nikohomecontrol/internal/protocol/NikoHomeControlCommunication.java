/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nikohomecontrol.internal.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.nikohomecontrol.handler.NikoHomeControlBridgeHandler;
import org.openhab.binding.nikohomecontrol.handler.NikoHomeControlHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * The {@link NikoHomeControlCommunication} class is able to do the following tasks with Niko Home Control
 * systems:
 * <ul>
 * <li>Start and stop TCP socket connection with Niko Home Control IP-interface.
 * <li>Read all setup and status information from the Niko Home Control Controller.
 * <li>Execute Niko Home Control Actions.
 * <li>Listen to events from Niko Home Control.
 * </ul>
 *
 * Only switch, dimmer and rollershutter actions are currently implemented.
 *
 * A class instance is instantiated from the {@link NikoHomeControlBridgeHandler} class initialization.
 *
 * @author Mark Herwege
 */

public class NikoHomeControlCommunication {

    private Logger logger = LoggerFactory.getLogger(NikoHomeControlCommunication.class);

    private InetAddress nhcAddress;
    private int nhcPort;

    private Socket nhcSocket;
    private PrintWriter nhcOut;
    private BufferedReader nhcIn;

    private Boolean listenerStopped;

    // Initialize with empty strings. If null, downstream methods may throw null pointer exceptions. These exceptions
    // cause threads to stop without warning, no way to catch the exception. This behavior happened when trying to write
    // null to properties of things. So this avoids having to check before the call for null pointers each time as the
    // null exceptions cannot be caught.
    private String swVersion = "";
    private String api = "";
    private String time = "";
    private String language = "";
    private String currency = "";
    private String units = "";
    private String dst = "";
    private String tz = "";
    private String lastEnergyErase = "";
    private String lastConfig = "";

    private final List<Integer> locations = new ArrayList<>();
    private final Map<Integer, String> locationNames = new HashMap<>();

    private final List<Integer> actions = new ArrayList<>();
    private final Map<Integer, String> actionNames = new HashMap<>();
    private final Map<Integer, Integer> actionTypes = new HashMap<>();
    private final Map<Integer, Integer> actionLocations = new HashMap<>();
    private final Map<Integer, Integer> actionStates = new HashMap<>();

    private final Map<Integer, NikoHomeControlHandler> actionThingHandlers = new HashMap<>();

    /**
     * Constructor for Niko Home Control communication object, manages communication with
     * Niko Home Control IP-interface.
     *
     * @param addr Can be null or omitted, will attempt to discover IP address.
     * @param port
     */
    public NikoHomeControlCommunication(InetAddress addr, int port) {

        startCommunication(addr, port);
    }

    public NikoHomeControlCommunication(int port) {

        startCommunication(null, port);
    }

    /**
     * Get Niko Home Control IP-interface IP address.
     * <p>
     * The method sends a UDP packet with content 0x44 to the local network on port 10000.
     * The Niko Home Control IP-interface responds to this UDP packet.
     * The IP-address from the Niko Home Control IP-interface is then extracted from the response packet.
     *
     * @param IP address used to get local broadcast address, will be broadcast in local subnet if null
     * @return IP address
     * @throws IOException
     */
    private InetAddress nikoHomeControlFindAddr(InetAddress broadcastaddr) throws IOException {

        byte[] discoverbuffer = { (byte) 0x44 };
        int broadcastport = 10000;

        if (broadcastaddr == null) {
            broadcastaddr = InetAddress.getLocalHost();
        }
        byte[] ipaddr = broadcastaddr.getAddress();
        ipaddr[3] = (byte) 0xff;
        broadcastaddr = InetAddress.getByAddress(ipaddr);

        DatagramPacket discoveryPacket = new DatagramPacket(discoverbuffer, discoverbuffer.length, broadcastaddr,
                broadcastport);
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        try (DatagramSocket datagramSocket = new DatagramSocket(null)) {
            datagramSocket.setBroadcast(true);
            datagramSocket.setSoTimeout(500);
            datagramSocket.send(discoveryPacket);
            datagramSocket.receive(packet);
            InetAddress addr = packet.getAddress();
            logger.debug("Niko Home Control: IP address is {}", addr);
            return addr;
        }

    }

    /**
     * Start communication with Niko Home Control IP-interface, run through initialization and start thread listening
     * to all messages coming from Niko Home Control.
     *
     * @param addr address used as a basis to calculate broadcast address, automatically set to previous address used
     * @param port port number for TCP communication, 8000 by default
     */
    private void startCommunication(InetAddress addr, int port) {

        try {
            nhcPort = port;
            nhcAddress = nikoHomeControlFindAddr(addr);

            if (nhcAddress == null) {
                logger.warn("Niko Home Control: did not find real IP of gateway");
                return;
            }

            nhcSocket = new Socket(nhcAddress, nhcPort);
            nhcOut = new PrintWriter(nhcSocket.getOutputStream(), true);
            nhcIn = new BufferedReader(new InputStreamReader(nhcSocket.getInputStream()));
            logger.info("Niko Home Control: connected");

            // initialize all info in local fields
            initialize();

            // Start Niko Home Control event listener. This listener will act on all messages coming from
            // IP-interface.
            (new Thread(nhcEvents)).start();

        } catch (IOException e) {
            // if the error occurs in the initialization, don't try to restart
            logger.warn("Niko Home Control: error initializing communication");
            stopCommunication();
        }

    }

    /**
     * Runnable that handles inbound communication from Niko Home Control.
     * <p>
     * The thread listens to the TCP socket opened at instantiation of the {@link NikoHomeControlCommunication} class
     * and interprets all inbound json messages. It triggers state updates for active channels linked to the Niko Home
     * Control actions. It is started after initialization of the communication.
     *
     */
    private Runnable nhcEvents = new Runnable() {

        @Override
        public void run() {
            String nhcMessage;

            logger.debug("Niko Home Control: listening for events");
            listenerStopped = false;

            try {
                while (!listenerStopped & ((nhcMessage = nhcIn.readLine()) != null)) {
                    readMessage(nhcMessage);
                }
            } catch (IOException e) {
                if (!listenerStopped) {
                    // this is not an communication stop triggered from outside this runnable
                    logger.warn("Niko Home Control: IO error in listener");
                    // the IO has stopped working, so we need to close cleanly and try to restart
                    restartCommunication();
                }
            }
        }

    };

    /**
     * Method that interprets all feedback from Niko Home Control and calls appropriate handling methods.
     *
     * @param nhcMessage message read from Niko Home Control.
     */
    private void readMessage(String nhcMessage) {

        logger.debug("Niko Home Control: received json {}", nhcMessage);

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(NHCBaseMessage.class, new NikoHomeControlMessageDeserializer());
        Gson gson = gsonBuilder.create();

        try {
            NHCBaseMessage nhcMessageGson = gson.fromJson(nhcMessage, NHCBaseMessage.class);

            String cmd = nhcMessageGson.getCmd();
            String event = nhcMessageGson.getEvent();

            if ("systeminfo".equals(cmd)) {
                cmdSystemInfo(((NHCMessageMap) nhcMessageGson).getData());
            } else if ("startevents".equals(cmd)) {
                cmdStartEvents(((NHCMessageMap) nhcMessageGson).getData());
            } else if ("listlocations".equals(cmd)) {
                cmdListLocations(((NHCMessageListMap) nhcMessageGson).getData());
            } else if ("listactions".equals(cmd)) {
                cmdListActions(((NHCMessageListMap) nhcMessageGson).getData());
            } else if ("executeactions".equals(cmd)) {
                cmdExecuteActions(((NHCMessageMap) nhcMessageGson).getData());
            } else if ("listactions".equals(event)) {
                eventListActions(((NHCMessageListMap) nhcMessageGson).getData());
            } else {
                logger.debug("Niko Home Control: not acted on json {}", nhcMessage);
            }
        } catch (JsonParseException e) {
            logger.debug("Niko Home Control: not acted on unsupported json {}", nhcMessage);
        }

    }

    /**
     * Cleanup socket when the communication with Niko Home Control IP-interface is closed.
     *
     */
    public synchronized void stopCommunication() {
        listenerStopped = true;

        if (nhcSocket != null) {
            try {
                nhcSocket.close();
            } catch (IOException ignore) {
                // ignore IO Error when trying to close the socket if the intention is to close it anyway
            }
            nhcSocket = null;
        }
        logger.warn("Niko Home Control: communication stopped");

    }

    /**
     * Close and restart communication with Niko Home Control IP-interface.
     *
     */
    public void restartCommunication() {
        stopCommunication();

        logger.info("Niko Home Control: restart communication");
        startCommunication(nhcAddress, nhcPort);

    }

    /**
     * After setting up the communication with the Niko Home Control IP-interface, send all initialization messages.
     * <p>
     * Only at first initialization, also set the return values. Otherwise use the runnable to get updated values.
     * While communication is set up for thermostats, tariff data and alarms, only info from locations and actions
     * is used beyond this point in openHAB. All other elements are for future extensions.
     *
     * @throws IOException
     */
    private void initialize() throws IOException {

        sendAndReadMessage("systeminfo");
        sendAndReadMessage("startevents");
        sendAndReadMessage("listlocations");
        sendAndReadMessage("listactions");
        sendAndReadMessage("listthermostat");
        sendAndReadMessage("listthermostatHVAC");
        sendAndReadMessage("readtariffdata");
        sendAndReadMessage("getalarms");

    }

    private void sendAndReadMessage(String command) throws IOException {
        sendMessage(new NHCCmd(command));
        readMessage(nhcIn.readLine());
    }

    private void cmdSystemInfo(Map<String, String> data) {

        logger.debug("Niko Home Control: systeminfo");

        if (data.containsKey("swversion")) {
            setSwVersion(data.get("swversion"));
        }
        if (data.containsKey("api")) {
            setApi(data.get("api"));
        }
        if (data.containsKey("time")) {
            setTime(data.get("time"));
        }
        if (data.containsKey("language")) {
            setLanguage(data.get("language"));
        }
        if (data.containsKey("currency")) {
            setCurrency(data.get("currency"));
        }
        if (data.containsKey("units")) {
            setUnits(data.get("units"));
        }
        if (data.containsKey("DST")) {
            setDst(data.get("DST"));
        }
        if (data.containsKey("TZ")) {
            setTz(data.get("TZ"));
        }
        if (data.containsKey("lastenergyerase")) {
            setLastEnergyErase(data.get("lastenergyerase"));
        }
        if (data.containsKey("lastconfig")) {
            setLastConfig(data.get("lastconfig"));
        }
    }

    private void cmdStartEvents(Map<String, String> data) {

        Integer errorCode = Integer.valueOf(data.get("error"));

        if (errorCode.equals(0)) {
            logger.debug("Niko Home Control: start events success");
        } else {
            logger.warn("Niko Home Control: error code {} returned on start events", errorCode);
        }
    }

    private void cmdListLocations(List<HashMap<String, String>> data) {

        logger.debug("Niko Home Control: list locations");

        locations.clear();
        locationNames.clear();

        for (HashMap<String, String> location : data) {
            int id = Integer.parseInt(location.get("id"));
            locations.add(id);
            locationNames.put(id, location.get("name"));
        }
    }

    private void cmdListActions(List<HashMap<String, String>> data) {

        logger.debug("Niko Home Control: list actions");

        actions.clear();
        actionNames.clear();
        actionTypes.clear();
        actionLocations.clear();
        actionStates.clear();

        for (HashMap<String, String> action : data) {
            int id = Integer.parseInt(action.get("id"));
            actions.add(id);
            actionNames.put(id, action.get("name"));
            actionTypes.put(id, Integer.parseInt(action.get("type")));
            actionLocations.put(id, Integer.parseInt(action.get("location")));
            actionStates.put(id, Integer.parseInt(action.get("value1")));
        }
    }

    private void cmdExecuteActions(Map<String, String> data) {

        Integer errorCode = Integer.valueOf(data.get("error"));
        if (errorCode.equals(0)) {
            logger.debug("Niko Home Control: execute action success");
        } else {
            logger.warn("Niko Home Control: error code {} returned on command execution", errorCode);
        }
    }

    private void eventListActions(List<HashMap<String, String>> data) {

        for (Map<String, String> action : data) {
            int id = Integer.valueOf(action.get("id"));
            setActionState(id, Integer.valueOf(action.get("value1")));
        }
    }

    /**
     * Sends action to Niko Home Control.
     *
     * @param actionId
     * @param percent - The allowed values depend on the action type.
     *            switch action: 0 or 100
     *            dimmer action: between 0 and 100, 254 for on, 255 for off
     *            rollershutter action: between 0 (closed) and 100 (open), 255 to open, 254 to close, 253 to stop
     */
    public void executeAction(int actionId, int percent) {

        int actionType = getActionType(actionId);

        logger.debug("Niko Home Control: execute action {} of type {} for {}", percent, actionType, actionId);

        NHCCmd nhcCmd = new NHCCmd("executeactions", actionId, percent);

        // rollershutters have extra fields in the command
        if ((actionType == 4) || (actionType == 5)) {
            switch (percent) {
                case 255: // open
                    nhcCmd.setEndValue(100);
                    break;
                case 254: // close
                    nhcCmd.setStartValue(100);
                    break;
                case 253: // stop
                    nhcCmd.setStartValue(getActionState(actionId));
            }
        }

        sendMessage(nhcCmd);

    }

    /**
     * Called by other methods to send json cmd to Niko Home Control.
     *
     * @param nhcMessage
     */
    private void sendMessage(Object nhcMessage) {
        Gson gson = new Gson();
        String json = gson.toJson(nhcMessage);
        logger.debug("Niko Home Control: send json {}", json);
        nhcOut.println(json);
        if (nhcOut.checkError()) {
            logger.warn("Niko Home Control: error sending message to gateway, trying to restart communication");
            restartCommunication();
            // retry sending after restart
            nhcOut.println(json);
        }
    }

    /**
     * Return IP address of Niko Home Control IP-interface.
     *
     * @return IP address
     */
    public InetAddress getAddr() {
        return nhcAddress;
    }

    /**
     * Return socket of Niko Home Control IP-interface.
     *
     * @return port
     */
    public int getPort() {
        return nhcSocket.getPort();
    }

    /**
     * Return software version of Niko Home Control Controller, retrieved when connecting.
     *
     * @return swVersion
     */
    public String getSwVersion() {
        return swVersion;
    }

    private void setSwVersion(String swversion) {
        this.swVersion = swversion;
    }

    /**
     * Return api version of Niko Home Control Controller, retrieved when connecting.
     *
     * @return api
     */
    public String getApi() {
        return api;
    }

    private void setApi(String api) {
        this.api = api;
    }

    /**
     * Return current local time from Niko Home Control Controller, retrieved when connecting.
     *
     * @return time
     */
    public String getTime() {
        return time;
    }

    private void setTime(String time) {
        this.time = time;
    }

    /**
     * Return language from Niko Home Control Controller, retrieved when connecting.
     *
     * @return language
     */
    public String getLanguage() {
        return language;
    }

    private void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Return currency from Niko Home Control Controller, retrieved when connecting.
     *
     * @return currency
     */
    public String getCurrency() {
        return currency;
    }

    private void setCurrency(String currency) {
        this.currency = currency;
    }

    /**
     * Return units of measure from Niko Home Control Controller, retrieved when connecting.
     *
     * @return units
     */
    public String getUnits() {
        return units;
    }

    private void setUnits(String units) {
        this.units = units;
    }

    /**
     * Return timezone offset from Niko Home Control Controller, retrieved when connecting.
     *
     * @return local timezone offset in 1/10 seconds
     */
    public String getTz() {
        return tz;
    }

    private void setTz(String tz) {
        this.tz = tz;
    }

    /**
     * Return daylight saving time offset from Niko Home Control Controller, retrieved when connecting.
     *
     * @return dst offset in 1/10 seconds for current time
     */
    public String getDst() {
        return dst;
    }

    private void setDst(String dst) {
        this.dst = dst;
    }

    /**
     * Return last energy data erase time (UTC) from Niko Home Control Controller, retrieved when connecting.
     *
     * @return lastEnergyErase
     */
    public String getLastEnergyErase() {
        return lastEnergyErase;
    }

    private void setLastEnergyErase(String lastEnergyErase) {
        this.lastEnergyErase = lastEnergyErase;
    }

    /**
     * Return last configuration upload time (UTC) from Niko Home Control Controller, retrieved when connecting.
     *
     * @return lastConfig
     */
    public String getLastConfig() {
        return lastConfig;
    }

    private void setLastConfig(String lastConfig) {
        this.lastConfig = lastConfig;
    }

    /**
     * Return the location name identified by locationId
     *
     * @param locationId
     * @return location name
     */
    public String getLocationName(int locationId) {
        return this.locationNames.get(locationId);
    }

    /**
     * Return the list of all action Ids in the Niko Home Control Controller.
     *
     * @return <code>ArrayList&ltInteger></code> of action Ids
     */
    public List<Integer> getActions() {
        return this.actions;
    }

    /**
     * Get name of action identified by actionId.
     *
     * @param actionId
     * @return action name
     */
    public String getActionName(int actionId) {
        return this.actionNames.get(actionId);
    }

    /**
     * Get type of action identified by actionId.
     * <p>
     * Action type is 1 for a switch and 2 for a dimmer.
     *
     * @param actionId
     * @return action type
     */
    public Integer getActionType(int actionId) {
        return this.actionTypes.get(actionId);
    }

    /**
     * Get location Id of action identified by actionId.
     *
     * @param actionId
     * @return location Id
     */
    public Integer getActionLocation(int actionId) {
        return this.actionLocations.get(actionId);
    }

    /**
     * Get state of action identified by actionId.
     * <p>
     * State is a value between 0 and 100 for a dimmer or rollershutter.
     * State is 0 or 100 for a switch.
     *
     * @param actionId
     * @return action state
     */
    public Integer getActionState(int actionId) {
        return this.actionStates.get(actionId);
    }

    private void setActionState(int actionId, int value) {
        this.actionStates.put(actionId, value);
        NikoHomeControlHandler nhcHandler = getActionThingHandler(actionId);
        if (nhcHandler != null) {
            logger.debug("Niko Home Control: update channel state for {} with {}", actionId, value);
            nhcHandler.handleStateUpdate(getActionType(actionId), value);
        }
    }

    /**
     * This method should be called if the ThingHandler for thing is initialized.
     * It keeps a record of the thing handler in this object so the thing can be updated when
     * an action receives an update from the Niko Home Control IP-interface.
     *
     * @param actionId
     * @param handler
     */
    public void setActionThingHandler(int actionId, NikoHomeControlHandler handler) {
        this.actionThingHandlers.put(actionId, handler);
    }

    /**
     * Retrieves the ThingHandler from the internal record of the thing handler in this object.
     *
     * @param actionId
     * @return Niko Home Control Thing Handler
     */
    private NikoHomeControlHandler getActionThingHandler(int actionId) {
        return actionThingHandlers.get(actionId);
    }

    /**
     * Method to check if communication with Niko Home Control IP-interface is active
     *
     * @return True if active
     */
    public boolean communicationActive() {
        return nhcSocket != null;
    }
}
