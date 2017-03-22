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

import org.openhab.binding.nikohomecontrol.handler.NikoHomeControlBridgeHandler;
import org.openhab.binding.nikohomecontrol.handler.NikoHomeControlHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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
 * Only switch, dimmer and rollershutter actions are currently implemented. There are placeholder methods for some other
 * types.
 * This is most likely incomplete because I don't have all possible equipment and don't have access to protocol
 * documentation.
 *
 * A class instance is instantiated from the {@link NikoHomeControlBridgeHandler} class initialization.
 *
 * @author Mark Herwege
 */

public class NikoHomeControlCommunication {

    private Logger logger = LoggerFactory.getLogger(NikoHomeControlCommunication.class);

    private InetAddress nhcAddress = null;
    private Integer nhcPort = null;

    private Socket nhcSocket = null;
    private PrintWriter nhcOut = null;
    private BufferedReader nhcIn = null;

    private Boolean listenerStopped = false;

    private String swversion = null;
    private String api = null;
    private String time = null;
    private String language = null;
    private String currency = null;
    private String units = null;
    private String dst = null;
    private String tz = null;
    private String lastenergyerase = null;
    private String lastconfig = null;

    private final ArrayList<Integer> locations = new ArrayList<Integer>();
    private final HashMap<Integer, String> locationNames = new HashMap<Integer, String>();

    private final ArrayList<Integer> actions = new ArrayList<Integer>();
    private final HashMap<Integer, String> actionNames = new HashMap<Integer, String>();
    private final HashMap<Integer, Integer> actionTypes = new HashMap<Integer, Integer>();
    private final HashMap<Integer, Integer> actionLocations = new HashMap<Integer, Integer>();
    private final HashMap<Integer, Integer> actionStates = new HashMap<Integer, Integer>();

    private final HashMap<Integer, NikoHomeControlHandler> actionThingHandlers = new HashMap<Integer, NikoHomeControlHandler>();

    /**
     * Class {@link NHCCmd} used as input to gson to send commands to Niko Home Control.
     * <p>
     * Example: <code>{"cmd":"executeactions","id":1,"value1":0}</code>
     *
     * @author Mark Herwege
     */
    private class NHCCmd {
        @SuppressWarnings("unused")
        private String cmd = "cmd";
        @SuppressWarnings("unused")
        private Integer id = null;
        @SuppressWarnings("unused")
        private Integer value1 = null;
        @SuppressWarnings("unused")
        private Integer value2 = null;
        @SuppressWarnings("unused")
        private Integer value3 = null;
        @SuppressWarnings("unused")
        private Integer startValue = null;
        @SuppressWarnings("unused")
        private Integer endValue = null;

        NHCCmd() {
        }
    }

    /**
     * Class {@link NHCCmdAck} used as output from gson for cmd or event feedback from Niko Home Control where the data
     * part is enclosed by [] and contains a list of json strings.
     * <p>
     * Example: <code>{"cmd":"listactions","data":[{"id":1,"name":"Garage","type":1,"location":1,"value1":0},
     * {"id":25,"name":"Frontdoor","type":2,"location":2,"value1":0}]}</code>
     *
     * @author Mark Herwege
     */
    private class NHCCmdAck {
        private String cmd;
        private String event;
        private ArrayList<HashMap<String, String>> data = new ArrayList<HashMap<String, String>>();

        NHCCmdAck() {
        }
    }

    /**
     * Class {@link NHCSimpleCmdAck} used as output from gson for cmd or event feedback from Niko Home Control where the
     * data part is a simple json string.
     * <p>
     * Example: <code>{"cmd":"executeactions", "data":{"error":0}}</code>
     *
     * @author Mark Herwege
     */
    private class NHCSimpleCmdAck {
        private String cmd;
        @SuppressWarnings("unused")
        private String event;
        private HashMap<String, String> data = new HashMap<String, String>();

        NHCSimpleCmdAck() {
        }
    }

    /**
     * Constructor for Niko Home Control communication object, manages communication with
     * Niko Home Control IP-interface.
     *
     * @param port
     * @param addr
     */
    public NikoHomeControlCommunication(int port, InetAddress addr) {

        startCommunication(addr, port);
    }

    public NikoHomeControlCommunication(int port) {

        InetAddress addr = null;
        startCommunication(addr, port);
    }

    /**
     * Get Niko Home Control IP-interface IP address.
     * <p>
     * The method sends a UDP packet with content 0x44 to the local network on port 10000.
     * The Niko Home Control IP-interface responds to this UDP packet.
     * The IP-address from the Niko Home Control IP-interface is then extracted from the response packet.
     *
     * @param IP address used to get local broadcast address, will be broadcast in local subnet if omitted
     * @return IP address
     * @throws IOException
     */
    private InetAddress nikoHomeControlFindAddr() throws IOException {
        return nikoHomeControlFindAddr(null);
    }

    private InetAddress nikoHomeControlFindAddr(InetAddress broadcastaddr) throws IOException {

        byte[] discoverbuffer = { (byte) 0x44 };
        int broadcastport = 10000;

        if (broadcastaddr == null) {
            broadcastaddr = InetAddress.getLocalHost();
        }
        byte[] ipaddr = broadcastaddr.getAddress();
        ipaddr[3] = (byte) 0xff;
        broadcastaddr = InetAddress.getByAddress(ipaddr);

        DatagramPacket discoveryPacket = null;
        DatagramPacket packet = null;
        DatagramSocket datagramSocket = null;
        InetAddress addr = null;
        byte[] buffer = null;

        try {
            discoveryPacket = new DatagramPacket(discoverbuffer, discoverbuffer.length, broadcastaddr, broadcastport);
            buffer = new byte[1024];
            packet = new DatagramPacket(buffer, buffer.length);
            datagramSocket = new DatagramSocket(null);
            datagramSocket.setBroadcast(true);
            datagramSocket.bind(null);
            datagramSocket.setSoTimeout(500);
            datagramSocket.send(discoveryPacket);
            datagramSocket.receive(packet);
            addr = packet.getAddress();
            logger.debug("Niko Home Control: IP address is {}", addr);

        } finally {
            if (datagramSocket != null) {
                datagramSocket.close();
            }
        }

        return addr;
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
            if (addr == null) {
                nhcAddress = nikoHomeControlFindAddr();
            } else {
                nhcAddress = nikoHomeControlFindAddr(addr);
            }

            if (nhcAddress == null) {
                logger.warn("Niko Home Control: did not find real IP of gateway");
                return;
            }

            nhcSocket = new Socket(nhcAddress, nhcPort);
            nhcOut = new PrintWriter(nhcSocket.getOutputStream(), true);
            nhcIn = new BufferedReader(new InputStreamReader(nhcSocket.getInputStream()));
            logger.info("Niko Home Control: connected");

            // initialize all info in local variables
            initialize();

            // Start Niko Home Control event listener. This listener will act on all messages coming from
            // IP-interface.
            (new Thread(nhcEvents)).start();

        } catch (IOException e) {
            // if the error occurs in the initialization, don't try to restart
            logger.warn("Niko Home Control: error initializing communication");
            stopCommunication(false);
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
                    stopCommunication(true);
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

        Gson gson = new Gson();
        NHCCmdAck nhcCmdAck = null;
        NHCSimpleCmdAck nhcSimpleCmdAck = null;

        logger.debug("Niko Home Control: received json {}", nhcMessage);

        try {
            // first try with simple reply form, a hashmap in the data field
            nhcSimpleCmdAck = new NHCSimpleCmdAck();
            nhcSimpleCmdAck = gson.fromJson(nhcMessage, NHCSimpleCmdAck.class);
            if (nhcSimpleCmdAck.cmd != null) {
                if (nhcSimpleCmdAck.cmd.equals("systeminfo")) {
                    systemInfo(nhcSimpleCmdAck);
                } else if (nhcSimpleCmdAck.cmd.equals("startevents")) {
                    startEvents(nhcSimpleCmdAck);
                } else if ((nhcSimpleCmdAck.cmd.equals("executeactions"))
                        && (Integer.valueOf(nhcSimpleCmdAck.data.get("error")).equals(0))) {
                    logger.debug("Niko Home Control: execute action success");
                } else {
                    logger.debug("Niko Home Control: not acted on json {}", nhcMessage);
                }
            } else {
                logger.debug("Niko Home Control: not acted on json {}", nhcMessage);
            }
        } catch (JsonSyntaxException je) {
            // if that JSON cannot be converted, it should be an array of hashmaps in the data field
            nhcCmdAck = new NHCCmdAck();
            nhcCmdAck = gson.fromJson(nhcMessage, NHCCmdAck.class);
            if (nhcCmdAck.cmd != null) {
                if (nhcCmdAck.cmd.equals("listlocations")) {
                    listLocations(nhcCmdAck);
                } else if (nhcCmdAck.cmd.equals("listactions")) {
                    listActions(nhcCmdAck);
                } else if (nhcCmdAck.cmd.equals("listthermostat")) {
                    listThermostat(nhcCmdAck);
                } else if (nhcCmdAck.cmd.equals("listthermostatHVAC")) {
                    listThermostatHVAC(nhcCmdAck);
                } else if (nhcCmdAck.cmd.equals("readtariffdata")) {
                    readTariffData(nhcCmdAck);
                } else if (nhcCmdAck.cmd.equals("getalarms")) {
                    getAlarms(nhcCmdAck);
                } else {
                    logger.debug("Niko Home Control: not acted on json {}", nhcMessage);
                }
            } else if (nhcCmdAck.event != null) {
                if (nhcCmdAck.event.equals("listactions")) {
                    for (HashMap<String, String> action : nhcCmdAck.data) {
                        int id = Integer.valueOf(action.get("id"));
                        setActionState(id, Integer.valueOf(action.get("value1")));
                    }
                } else {
                    logger.debug("Niko Home Control: not acted on json {}", nhcMessage);
                }
            } else {
                logger.debug("Niko Home Control: not acted on json {}", nhcMessage);
            }
        }

    }

    /**
     * Cleanup socket when the communication with Niko Home Control IP-interface is closed.
     */
    private void stopCommunication() {

        listenerStopped = true;

        if (nhcSocket != null) {
            try {
                nhcSocket.close();
            } catch (IOException e) {
            }
            nhcSocket = null;
        }
        logger.warn("Niko Home Control: communication stopped");
    }

    /**
     * Cleanup socket when the communication with Niko Home Control IP-interface is closed.
     *
     * @param restart true if it is the intention to try to restart, should not be true if the event listener has not
     *            successfully been started before
     */
    public synchronized void stopCommunication(Boolean restart) {
        stopCommunication();

        // try restarting the communication
        if (restart) {
            logger.info("Niko Home Control: restart communication");
            startCommunication(nhcAddress, nhcPort.intValue());
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

        NHCCmd nhcCmd = new NHCCmd();

        nhcCmd.cmd = "systeminfo";
        sendMessage(nhcCmd);
        readMessage(nhcIn.readLine());

        nhcCmd.cmd = "startevents";
        sendMessage(nhcCmd);
        readMessage(nhcIn.readLine());

        nhcCmd.cmd = "listlocations";
        sendMessage(nhcCmd);
        readMessage(nhcIn.readLine());

        nhcCmd.cmd = "listactions";
        sendMessage(nhcCmd);
        readMessage(nhcIn.readLine());

        nhcCmd.cmd = "listthermostat";
        sendMessage(nhcCmd);
        readMessage(nhcIn.readLine());

        nhcCmd.cmd = "listthermostatHVAC";
        sendMessage(nhcCmd);
        readMessage(nhcIn.readLine());

        nhcCmd.cmd = "readtariffdata";
        sendMessage(nhcCmd);
        readMessage(nhcIn.readLine());

        nhcCmd.cmd = "getalarms";
        sendMessage(nhcCmd);
        readMessage(nhcIn.readLine());

    }

    private void systemInfo(NHCSimpleCmdAck nhcCmdAck) {

        logger.debug("Niko Home Control: systeminfo");

        if (nhcCmdAck.data.containsKey("swversion")) {
            setSwversion(nhcCmdAck.data.get("swversion"));
        }
        if (nhcCmdAck.data.containsKey("api")) {
            setApi(nhcCmdAck.data.get("api"));
        }
        if (nhcCmdAck.data.containsKey("time")) {
            setTime(nhcCmdAck.data.get("time"));
        }
        if (nhcCmdAck.data.containsKey("language")) {
            setLanguage(nhcCmdAck.data.get("language"));
        }
        if (nhcCmdAck.data.containsKey("currency")) {
            setCurrency(nhcCmdAck.data.get("currency"));
        }
        if (nhcCmdAck.data.containsKey("units")) {
            setUnits(nhcCmdAck.data.get("units"));
        }
        if (nhcCmdAck.data.containsKey("DST")) {
            setDst(nhcCmdAck.data.get("DST"));
        }
        if (nhcCmdAck.data.containsKey("TZ")) {
            setTz(nhcCmdAck.data.get("TZ"));
        }
        if (nhcCmdAck.data.containsKey("lastenergyerase")) {
            setLastenergyerase(nhcCmdAck.data.get("lastenergyerase"));
        }
        if (nhcCmdAck.data.containsKey("lastconfig")) {
            setLastconfig(nhcCmdAck.data.get("lastconfig"));
        }
    }

    private void startEvents(NHCSimpleCmdAck nhcCmdAck) {

        logger.debug("Niko Home Control: startevents");
        if (Integer.valueOf(nhcCmdAck.data.get("error")).equals(0)) {
            logger.debug("Niko Home Control: start events success");
        }

    }

    private void listLocations(NHCCmdAck nhcCmdAck) {

        logger.debug("Niko Home Control: list locations");

        locations.clear();
        locationNames.clear();

        for (HashMap<String, String> location : nhcCmdAck.data) {
            int id = Integer.parseInt(location.get("id"));
            locations.add(id);
            locationNames.put(id, location.get("name"));
        }

    }

    private void listActions(NHCCmdAck nhcCmdAck) {

        logger.debug("Niko Home Control: list actions");

        actions.clear();
        actionNames.clear();
        actionTypes.clear();
        actionLocations.clear();
        actionStates.clear();

        for (HashMap<String, String> action : nhcCmdAck.data) {
            int id = Integer.parseInt(action.get("id"));
            actions.add(id);
            actionNames.put(id, action.get("name"));
            actionTypes.put(id, Integer.parseInt(action.get("type")));
            actionLocations.put(id, Integer.parseInt(action.get("location")));
            actionStates.put(id, Integer.parseInt(action.get("value1")));
            if (!actionThingHandlers.containsKey(id)) {
                actionThingHandlers.put(id, null);
            }
        }

    }

    private void listThermostat(NHCCmdAck nhcCmdAck) {

        logger.debug("Niko Home Control: list thermostat");

    }

    private void listThermostatHVAC(NHCCmdAck nhcCmdAck) {

        logger.debug("Niko Home Control: list thermostatHVAC");

    }

    private void readTariffData(NHCCmdAck nhcCmdAck) {

        logger.debug("Niko Home Control: read tariff data");

    }

    private void getAlarms(NHCCmdAck nhcCmdAck) {

        logger.debug("Niko Home Control: get alarms");

    }

    /**
     * Sends action to Niko Home Control.
     *
     * @param actionId
     * @param percent - The allowed values depend on the action type.
     *            switch action: 0 or 100
     *            dimmer action: between 0 and 100, 254 for on, 255 for off
     *            rollershutter action: between 0 (closed) and 100 (open), 253 to open, 254 to close, 255 to stop
     */
    public void executeAction(int actionId, int percent) {

        int actionType = getActionType(actionId);

        logger.debug("Niko Home Control: execute action {} of type {} for {}", percent, actionType, actionId);

        NHCCmd nhcCmd = new NHCCmd();
        nhcCmd.cmd = "executeactions";
        nhcCmd.id = actionId;
        nhcCmd.value1 = percent;

        // rollershutters have extra fields in the command
        if ((actionType == 4) || (actionType == 5)) {
            switch (percent) {
                case 253: // open
                    nhcCmd.endValue = 100;
                    break;
                case 254: // close
                    nhcCmd.startValue = 100;
                    break;
                case 255: // stop
                    nhcCmd.startValue = getActionState(actionId);
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
            stopCommunication(true);
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
     * @return swversion
     */
    public String getSwversion() {
        return swversion;
    }

    private void setSwversion(String swversion) {
        this.swversion = swversion;
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
     * Return current time from Niko Home Control Controller, retrieved when connecting.
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
     * Return timezone from Niko Home Control Controller, retrieved when connecting.
     *
     * @return timezone
     */
    public String getTz() {
        return tz;
    }

    private void setTz(String tz) {
        this.tz = tz;
    }

    /**
     * Return daylight saving time from Niko Home Control Controller, retrieved when connecting.
     *
     * @return dst
     */
    public String getDst() {
        return dst;
    }

    private void setDst(String dst) {
        this.dst = dst;
    }

    /**
     * Return last energy data erase time from Niko Home Control Controller, retrieved when connecting.
     *
     * @return lastenergyerase
     */
    public String getLastenergyerase() {
        return lastenergyerase;
    }

    private void setLastenergyerase(String lastenergyerase) {
        this.lastenergyerase = lastenergyerase;
    }

    /**
     * Return last configuration upload time from Niko Home Control Controller, retrieved when connecting.
     *
     * @return lastconfig
     */
    public String getLastconfig() {
        return lastconfig;
    }

    private void setLastconfig(String lastconfig) {
        this.lastconfig = lastconfig;
    }

    /**
     * Return the location name identified by locationID
     *
     * @param locationID
     * @return location name
     */
    public String getLocationName(int locationID) {
        return this.locationNames.get(locationID);
    }

    /**
     * Return the list of all action IDs in the Niko Home Control Controller.
     *
     * @return <code>ArrayList&ltInteger></code> of action IDs
     */
    public ArrayList<Integer> getActions() {
        return this.actions;
    }

    /**
     * Get name of action identified by actionID.
     *
     * @param actionID
     * @return action name
     */
    public String getActionName(int actionID) {
        if (this.actionNames.containsKey(actionID)) {
            return this.actionNames.get(actionID);
        } else {
            return null;
        }
    }

    /**
     * Get type of action identified by actionID.
     * <p>
     * Action type is 1 for a switch and 2 for a dimmer.
     *
     * @param actionID
     * @return action type
     */
    public Integer getActionType(int actionID) {
        if (this.actionNames.containsKey(actionID)) {
            return this.actionTypes.get(actionID);
        } else {
            return null;
        }
    }

    /**
     * Get location ID of action identified by actionID.
     *
     * @param actionID
     * @return location ID
     */
    public Integer getActionLocation(int actionID) {
        if (this.actionNames.containsKey(actionID)) {
            return this.actionLocations.get(actionID);
        } else {
            return null;
        }
    }

    /**
     * Get state of action identified by actionID.
     * <p>
     * State is a value between 0 and 100 for a dimmer or rollershutter.
     * State is 0 or 100 for a switch.
     *
     * @param actionID
     * @return action state
     */
    public Integer getActionState(int actionID) {
        if (this.actionNames.containsKey(actionID)) {
            return this.actionStates.get(actionID);
        } else {
            return null;
        }
    }

    private void setActionState(int actionID, int value) {
        this.actionStates.put(actionID, value);
        NikoHomeControlHandler nhcHandler = getActionThingHandler(actionID);
        if (nhcHandler != null) {
            logger.debug("Niko Home Control: update channel state for {} with {}", actionID, value);
            nhcHandler.handleStateUpdate(getActionType(actionID), value);
        }
    }

    /**
     * This method should be called if the ThingHandler for thing is initialized.
     * It keeps a record of the thing handler in this object so the thing can be updated when
     * an action receives an update from the Niko Home Control IP-interface.
     *
     * @param actionID
     * @param handler
     */
    public void setActionThingHandler(int actionID, NikoHomeControlHandler handler) {
        this.actionThingHandlers.put(actionID, handler);
    }

    /**
     * Retrieves the ThingHandler from the internal record of the thing handler in this object.
     *
     * @param actionID
     * @return Niko Home Control Thing Handler
     */
    private NikoHomeControlHandler getActionThingHandler(int actionID) {
        if (this.actionNames.containsKey(actionID)) {
            return actionThingHandlers.get(actionID);
        } else {
            return null;
        }
    }

    /**
     * Method to check if communication with Niko Home Control IP-interface is active
     *
     * @return True if active
     */
    public boolean communicationActive() {
        return ((nhcSocket == null) ? false : true);
    }
}
