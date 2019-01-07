/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
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
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.handler.NikoHomeControlBridgeHandler;
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
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public final class NikoHomeControlCommunication {

    private final Logger logger = LoggerFactory.getLogger(NikoHomeControlCommunication.class);

    @Nullable
    private Socket nhcSocket;
    @Nullable
    private PrintWriter nhcOut;
    @Nullable
    private BufferedReader nhcIn;

    private boolean listenerStopped;
    private boolean nhcEventsRunning;

    // We keep only 2 gson adapters used to serialize and deserialize all messages sent and received
    private Gson gsonOut = new Gson();
    private Gson gsonIn;

    private final NhcSystemInfo systemInfo = new NhcSystemInfo();
    private final Map<Integer, NhcLocation> locations = new HashMap<>();
    private final Map<Integer, NhcAction> actions = new HashMap<>();
    private final Map<Integer, NhcThermostat> thermostats = new HashMap<>();

    @Nullable
    private NikoHomeControlBridgeHandler bridgeCallBack;

    /**
     * Constructor for Niko Home Control communication object, manages communication with
     * Niko Home Control IP-interface.
     *
     */
    public NikoHomeControlCommunication() {
        // When we set up this object, we want to get the proper gson adapter set up once
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(NhcMessageBase.class, new NikoHomeControlMessageDeserializer());
        this.gsonIn = gsonBuilder.create();
    }

    /**
     * Start communication with Niko Home Control IP-interface, run through initialization and start thread listening
     * to all messages coming from Niko Home Control.
     *
     * @param addr IP-address of Niko Home Control IP-interface
     * @param port port
     *
     */
    public synchronized void startCommunication() {
        NikoHomeControlBridgeHandler handler = this.bridgeCallBack;
        try {
            for (int i = 1; nhcEventsRunning && (i <= 5); i++) {
                // the events listener thread did not finish yet, so wait max 5000ms before restarting
                Thread.sleep(1000);
            }
            if (nhcEventsRunning) {
                logger.error(
                        "Niko Home Control: starting from thread {}, but previous connection still active after 5000ms",
                        Thread.currentThread().getId());
                throw new IOException();
            }

            if (handler == null) {
                throw new IOException();
            }
            InetAddress addr = handler.getAddr();
            int port = handler.getPort();

            Socket socket = new Socket(addr, port);
            this.nhcSocket = socket;
            this.nhcOut = new PrintWriter(socket.getOutputStream(), true);
            this.nhcIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            logger.debug("Niko Home Control: connected via local port {} from thread {}", socket.getLocalPort(),
                    Thread.currentThread().getId());

            // initialize all info in local fields
            initialize();

            // Start Niko Home Control event listener. This listener will act on all messages coming from
            // IP-interface.
            (new Thread(nhcEvents)).start();

        } catch (IOException | InterruptedException e) {
            logger.warn("Niko Home Control: error initializing communication from thread {}",
                    Thread.currentThread().getId());
            stopCommunication();
            if (handler != null) {
                handler.bridgeOffline();
            }
        }
    }

    /**
     * Cleanup socket when the communication with Niko Home Control IP-interface is closed.
     *
     */
    public synchronized void stopCommunication() {
        this.listenerStopped = true;

        Socket socket = this.nhcSocket;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignore) {
                // ignore IO Error when trying to close the socket if the intention is to close it anyway
            }
        }
        this.nhcSocket = null;

        logger.debug("Niko Home Control: communication stopped from thread {}", Thread.currentThread().getId());
    }

    /**
     * Close and restart communication with Niko Home Control IP-interface.
     *
     */
    public synchronized void restartCommunication() {
        stopCommunication();

        logger.debug("Niko Home Control: restart communication from thread {}", Thread.currentThread().getId());

        startCommunication();
    }

    /**
     * Method to check if communication with Niko Home Control IP-interface is active
     *
     * @return True if active
     */
    public boolean communicationActive() {
        return (this.nhcSocket != null);
    }

    /**
     * Runnable that handles inbound communication from Niko Home Control.
     * <p>
     * The thread listens to the TCP socket opened at instantiation of the {@link NikoHomeControlCommunication} class
     * and interprets all inbound json messages. It triggers state updates for active channels linked to the Niko Home
     * Control actions. It is started after initialization of the communication.
     *
     */
    private Runnable nhcEvents = () -> {
        String nhcMessage;

        logger.debug("Niko Home Control: listening for events on thread {}", Thread.currentThread().getId());
        listenerStopped = false;
        nhcEventsRunning = true;

        BufferedReader reader = this.nhcIn;

        try {
            if (reader == null) {
                throw new IOException();
            }
            while (!listenerStopped & ((nhcMessage = reader.readLine()) != null)) {
                if (nhcMessage != null) {
                    readMessage(nhcMessage);
                }
            }
        } catch (IOException e) {
            if (!listenerStopped) {
                nhcEventsRunning = false;
                // this is a socket error, not a communication stop triggered from outside this runnable
                logger.warn("Niko Home Control: IO error in listener on thread {}", Thread.currentThread().getId());
                // the IO has stopped working, so we need to close cleanly and try to restart
                restartCommunication();
                return;
            }
        }

        nhcEventsRunning = false;
        // this is a stop from outside the runnable, so just log it and stop
        logger.debug("Niko Home Control: event listener thread stopped on thread {}", Thread.currentThread().getId());
    };

    /**
     * Method that interprets all feedback from Niko Home Control and calls appropriate handling methods.
     *
     * @param nhcMessage message read from Niko Home Control.
     */
    private void readMessage(String nhcMessage) {
        logger.debug("Niko Home Control: received json {} on thread {}", nhcMessage, Thread.currentThread().getId());

        try {
            NhcMessageBase nhcMessageGson = this.gsonIn.fromJson(nhcMessage, NhcMessageBase.class);

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
            } else if (("listthermostat").equals(cmd)) {
                cmdListThermostat(((NhcMessageListMap) nhcMessageGson).getData());
            } else if ("executeactions".equals(cmd)) {
                cmdExecuteActions(((NhcMessageMap) nhcMessageGson).getData());
            } else if ("executethermostat".equals(cmd)) {
                cmdExecuteThermostat(((NhcMessageMap) nhcMessageGson).getData());
            } else if ("listactions".equals(event)) {
                eventListActions(((NhcMessageListMap) nhcMessageGson).getData());
            } else if ("listthermostat".equals(event)) {
                eventListThermostat(((NhcMessageListMap) nhcMessageGson).getData());
            } else if ("getalarms".equals(event)) {
                eventGetAlarms(((NhcMessageMap) nhcMessageGson).getData());
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
        BufferedReader reader = this.nhcIn;
        if (reader == null) {
            throw new IOException("Cannot read from socket, reader not connected.");
        }
        readMessage(reader.readLine());
    }

    private void cmdSystemInfo(Map<String, String> data) {
        logger.debug("Niko Home Control: systeminfo");

        if (data.containsKey("swversion")) {
            this.systemInfo.setSwVersion(data.get("swversion"));
        }
        if (data.containsKey("api")) {
            this.systemInfo.setApi(data.get("api"));
        }
        if (data.containsKey("time")) {
            this.systemInfo.setTime(data.get("time"));
        }
        if (data.containsKey("language")) {
            this.systemInfo.setLanguage(data.get("language"));
        }
        if (data.containsKey("currency")) {
            this.systemInfo.setCurrency(data.get("currency"));
        }
        if (data.containsKey("units")) {
            this.systemInfo.setUnits(data.get("units"));
        }
        if (data.containsKey("DST")) {
            this.systemInfo.setDst(data.get("DST"));
        }
        if (data.containsKey("TZ")) {
            this.systemInfo.setTz(data.get("TZ"));
        }
        if (data.containsKey("lastenergyerase")) {
            this.systemInfo.setLastEnergyErase(data.get("lastenergyerase"));
        }
        if (data.containsKey("lastconfig")) {
            this.systemInfo.setLastConfig(data.get("lastconfig"));
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

    private void cmdListLocations(List<Map<String, String>> data) {
        logger.debug("Niko Home Control: list locations");

        this.locations.clear();

        for (Map<String, String> location : data) {
            int id = Integer.valueOf(location.get("id"));
            String name = location.get("name");
            NhcLocation nhcLocation = new NhcLocation(name);
            this.locations.put(id, nhcLocation);
        }
    }

    private void cmdListActions(List<Map<String, String>> data) {
        logger.debug("Niko Home Control: list actions");

        for (Map<String, String> action : data) {

            int id = Integer.parseInt(action.get("id"));
            Integer state = Integer.valueOf(action.get("value1"));
            String value2 = action.get("value2");
            @SuppressWarnings("null")
            Integer closeTime = (value2 == null ? null : Integer.valueOf(value2));
            String value3 = action.get("value3");
            @SuppressWarnings("null")
            Integer openTime = (value3 == null ? null : Integer.valueOf(value3));

            if (!this.actions.containsKey(id)) {
                // Initial instantiation of NhcAction class for action object
                String name = action.get("name");
                Integer type = Integer.valueOf(action.get("type"));
                Integer locationId = Integer.valueOf(action.get("location"));
                String location = "";
                if (locationId != null) {
                    location = this.locations.get(locationId).getName();
                }
                NhcAction nhcAction = new NhcAction(id, name, type, location, closeTime, openTime);
                nhcAction.setState(state);
                nhcAction.setNhcComm(this);
                this.actions.put(id, nhcAction);
            } else {
                // Action object already exists, so only update state.
                // If we would re-instantiate action, we would lose pointer back from action to thing handler that was
                // set in thing handler initialize().
                this.actions.get(id).setState(state);
            }
        }
    }

    private void cmdListThermostat(List<Map<String, String>> data) {
        logger.debug("Niko Home Control: list thermostats");

        for (Map<String, String> thermostat : data) {

            int id = Integer.parseInt(thermostat.get("id"));
            Integer measured = Integer.valueOf(thermostat.get("measured"));
            Integer setpoint = Integer.valueOf(thermostat.get("setpoint"));
            Integer mode = Integer.valueOf(thermostat.get("mode"));
            Integer overrule = Integer.valueOf(thermostat.get("overrule"));
            // overruletime received in "HH:MM" format
            String[] overruletimeStrings = thermostat.get("overruletime").split(":");
            Integer overruletime = 0;
            if (overruletimeStrings.length == 2) {
                overruletime = Integer.valueOf(overruletimeStrings[0]) * 60 + Integer.valueOf(overruletimeStrings[1]);
            }
            Integer ecosave = Integer.valueOf(thermostat.get("ecosave"));

            if (!this.thermostats.containsKey(id)) {
                // Initial instantiation of NhcThermostat class for thermostat object
                String name = thermostat.get("name");
                Integer locationId = Integer.valueOf(thermostat.get("location"));
                String location = "";
                if (locationId != null) {
                    location = this.locations.get(locationId).getName();
                }
                NhcThermostat nhcThermostat = new NhcThermostat(id, name, location);
                nhcThermostat.updateState(measured, setpoint, mode, overrule, overruletime, ecosave);
                nhcThermostat.setNhcComm(this);
                this.thermostats.put(id, nhcThermostat);
            } else {
                // Thermostat object already exists, so only update state.
                // If we would re-instantiate thermostat, we would lose pointer back from thermostat to thing handler
                // that was set in thing handler initialize().
                this.thermostats.get(id).updateState(measured, setpoint, mode, overrule, overruletime, ecosave);
            }
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

    private void cmdExecuteThermostat(Map<String, String> data) {
        Integer errorCode = Integer.valueOf(data.get("error"));
        if (errorCode.equals(0)) {
            logger.debug("Niko Home Control: execute thermostats success");
        } else {
            logger.warn("Niko Home Control: error code {} returned on command execution", errorCode);
        }
    }

    private void eventListActions(List<Map<String, String>> data) {
        for (Map<String, String> action : data) {
            int id = Integer.valueOf(action.get("id"));
            if (!this.actions.containsKey(id)) {
                logger.warn("Niko Home Control: action in controller not known {}", id);
                return;
            }
            Integer state = Integer.valueOf(action.get("value1"));
            logger.debug("Niko Home Control: event execute action {} with state {}", id, state);
            this.actions.get(id).setState(state);
        }
    }

    private void eventListThermostat(List<Map<String, String>> data) {
        for (Map<String, String> thermostat : data) {
            int id = Integer.parseInt(thermostat.get("id"));
            if (!this.thermostats.containsKey(id)) {
                logger.warn("Niko Home Control: thermostat in controller not known {}", id);
                return;
            }

            Integer measured = Integer.valueOf(thermostat.get("measured"));
            Integer setpoint = Integer.valueOf(thermostat.get("setpoint"));
            Integer mode = Integer.valueOf(thermostat.get("mode"));
            Integer overrule = Integer.valueOf(thermostat.get("overrule"));
            // overruletime received in "HH:MM" format
            String[] overruletimeStrings = thermostat.get("overruletime").split(":");
            Integer overruletime = 0;
            if (overruletimeStrings.length == 2) {
                overruletime = Integer.valueOf(overruletimeStrings[0]) * 60 + Integer.valueOf(overruletimeStrings[1]);
            }
            Integer ecosave = Integer.valueOf(thermostat.get("ecosave"));

            logger.debug(
                    "Niko Home Control: event execute thermostat {} with measured {}, setpoint {}, mode {}, overrule {}, overruletime {}, ecosave {}",
                    id, measured, setpoint, mode, overrule, overruletime, ecosave);
            this.thermostats.get(id).updateState(measured, setpoint, mode, overrule, overruletime, ecosave);
        }
    }

    private void eventGetAlarms(Map<String, String> data) {
        NikoHomeControlBridgeHandler handler = this.bridgeCallBack;

        Integer type = Integer.valueOf(data.get("type"));
        String alarmText = data.get("text");

        if (handler == null) {
            logger.error("Niko Home Control: triggering alarm '{}' but no bridge handler", alarmText);
            return;
        }

        switch (type) {
            case 0:
                logger.debug("Niko Home Control: alarm - {}", alarmText);
                handler.triggerAlarm(alarmText);
                break;
            case 1:
                logger.debug("Niko Home Control: notice - {}", alarmText);
                handler.triggerNotice(alarmText);
                break;
            default:
                logger.debug("Niko Home Control: unexpected message type {}", type);
        }
    }

    /**
     * Called by other methods to send json cmd to Niko Home Control.
     *
     * @param nhcMessage
     */
    synchronized void sendMessage(Object nhcMessage) {
        PrintWriter writer = this.nhcOut;
        String json = gsonOut.toJson(nhcMessage);
        logger.debug("Niko Home Control: send json {} from thread {}", json, Thread.currentThread().getId());
        if (writer != null) {
            writer.println(json);
        }
        if ((writer == null) || (writer.checkError())) {
            logger.warn("Niko Home Control: error sending message, trying to restart communication");
            restartCommunication();
            // retry sending after restart
            logger.debug("Niko Home Control: resend json {} from thread {}", json, Thread.currentThread().getId());
            writer = this.nhcOut;
            if (writer != null) {
                writer.println(json);
            }
            if ((writer == null) || (writer.checkError())) {
                logger.warn("Niko Home Control: error resending message");

            }
        }
    }

    /**
     * Return the object with system info as read from the Niko Home Control controller.
     *
     * @return the systemInfo
     */
    public NhcSystemInfo getSystemInfo() {
        return this.systemInfo;
    }

    /**
     * Return all actions in the Niko Home Control Controller.
     *
     * @return <code>Map&ltInteger, {@link NhcAction}></code>
     */
    public Map<Integer, NhcAction> getActions() {
        return this.actions;
    }

    /**
     * Return all thermostats in the Niko Home Control Controller.
     *
     * @return <code>Map&ltInteger, {@link NhcThermostat}></code>
     */
    public Map<Integer, NhcThermostat> getThermostats() {
        return this.thermostats;
    }

    /**
     * @param bridgeCallBack the bridgeCallBack to set
     */
    public void setBridgeCallBack(NikoHomeControlBridgeHandler bridgeCallBack) {
        this.bridgeCallBack = bridgeCallBack;
    }
}
