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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openhab.binding.nikohomecontrol.handler.NikoHomeControlBridgeHandler;
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
 * <li>Execute Niko Home Control commands.
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

    public class NhcLocation {

        private String name;

        NhcLocation(String name) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

    private final NhcSystemInfo systemInfo = new NhcSystemInfo();
    private final Map<Integer, NhcLocation> locations = new HashMap<>();
    private final Map<Integer, NhcAction> actions = new HashMap<>();

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
     * Method to check if communication with Niko Home Control IP-interface is active
     *
     * @return True if active
     */
    public boolean communicationActive() {
        return nhcSocket != null;
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
        gsonBuilder.registerTypeAdapter(NhcMessageBase.class, new NikoHomeControlMessageDeserializer());
        Gson gson = gsonBuilder.create();

        try {
            NhcMessageBase nhcMessageGson = gson.fromJson(nhcMessage, NhcMessageBase.class);

            String cmd = nhcMessageGson.getCmd();
            String event = nhcMessageGson.getEvent();

            if ("systeminfo".equals(cmd)) {
                cmdSystemInfo(((NhcMessageMap) nhcMessageGson).getData());
            } else if ("startevents".equals(cmd)) {
                cmdStartEvents(((NhcMessageMap) nhcMessageGson).getData());
            } else if ("listlocations".equals(cmd)) {
                cmdListLocations(((NhcMessageListMap) nhcMessageGson).getData());
            } else if ("listactions".equals(cmd)) {
                cmdListActions(((NhcMessageListMap) nhcMessageGson).getData());
            } else if ("executeactions".equals(cmd)) {
                cmdExecuteActions(((NhcMessageMap) nhcMessageGson).getData());
            } else if ("listactions".equals(event)) {
                eventListActions(((NhcMessageListMap) nhcMessageGson).getData());
            } else {
                logger.debug("Niko Home Control: not acted on json {}", nhcMessage);
            }
        } catch (JsonParseException e) {
            logger.debug("Niko Home Control: not acted on unsupported json {}", nhcMessage);
        }

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
        sendMessage(new NhcMessageCmd(command));
        readMessage(nhcIn.readLine());
    }

    private void cmdSystemInfo(Map<String, String> data) {

        logger.debug("Niko Home Control: systeminfo");

        if (data.containsKey("swversion")) {
            systemInfo.setSwVersion(data.get("swversion"));
        }
        if (data.containsKey("api")) {
            systemInfo.setApi(data.get("api"));
        }
        if (data.containsKey("time")) {
            systemInfo.setTime(data.get("time"));
        }
        if (data.containsKey("language")) {
            systemInfo.setLanguage(data.get("language"));
        }
        if (data.containsKey("currency")) {
            systemInfo.setCurrency(data.get("currency"));
        }
        if (data.containsKey("units")) {
            systemInfo.setUnits(data.get("units"));
        }
        if (data.containsKey("DST")) {
            systemInfo.setDst(data.get("DST"));
        }
        if (data.containsKey("TZ")) {
            systemInfo.setTz(data.get("TZ"));
        }
        if (data.containsKey("lastenergyerase")) {
            systemInfo.setLastEnergyErase(data.get("lastenergyerase"));
        }
        if (data.containsKey("lastconfig")) {
            systemInfo.setLastConfig(data.get("lastconfig"));
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

        this.locations.clear();

        for (HashMap<String, String> location : data) {
            int id = Integer.valueOf(location.get("id"));
            String name = location.get("name");
            NhcLocation nhcLocation = new NhcLocation(name);
            this.locations.put(id, nhcLocation);
        }
    }

    private void cmdListActions(List<HashMap<String, String>> data) {

        logger.debug("Niko Home Control: list actions");

        this.actions.clear();

        for (HashMap<String, String> action : data) {

            int id = Integer.valueOf(action.get("id"));
            String name = action.get("name");
            Integer type = Integer.valueOf(action.get("type"));
            Integer locationId = Integer.valueOf(action.get("location"));
            String location = null;
            if (locationId != null) {
                location = this.locations.get(locationId).getName();
            }
            Integer state = Integer.valueOf(action.get("value1"));

            NhcAction nhcAction = new NhcAction(id, name, type, location, state);
            this.actions.put(id, nhcAction);
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
            this.actions.get(id).setState(Integer.valueOf(action.get("value1")));
        }
    }

    /**
     * Called by other methods to send json cmd to Niko Home Control.
     *
     * @param nhcMessage
     */
    public void sendMessage(Object nhcMessage) {
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
     * Return the object with system info as read from the Niko Home Control controller.
     *
     * @return the systemInfo
     */
    public NhcSystemInfo getSystemInfo() {
        return systemInfo;
    }

    /**
     * Return all locations in the Niko Home Control Controller.
     *
     * @return <code>Map&ltInteger, {@link NhcLocation}></code>
     */
    public Map<Integer, NhcLocation> getLocations() {
        return this.locations;
    }

    /**
     * Return all actions in the Niko Home Control Controller.
     *
     * @return <code>Map&ltInteger, {@link NhcAction}></code>
     */
    public Map<Integer, NhcAction> getActions() {
        return this.actions;
    }

}
