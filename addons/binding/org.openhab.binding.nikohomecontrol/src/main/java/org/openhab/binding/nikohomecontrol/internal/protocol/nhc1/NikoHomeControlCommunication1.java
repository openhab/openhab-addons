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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcAction;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcControllerEvent;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcThermostat;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * The {@link NikoHomeControlCommunication1} class is able to do the following tasks with Niko Home Control I
 * systems:
 * <ul>
 * <li>Start and stop TCP socket connection with Niko Home Control IP-interface.
 * <li>Read all setup and status information from the Niko Home Control Controller.
 * <li>Execute Niko Home Control commands.
 * <li>Listen to events from Niko Home Control.
 * </ul>
 *
 * Only switch, dimmer, rollershutter and thermostat actions are currently implemented.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NikoHomeControlCommunication1 extends NikoHomeControlCommunication {

    private Logger logger = LoggerFactory.getLogger(NikoHomeControlCommunication1.class);

    private final NhcSystemInfo1 systemInfo = new NhcSystemInfo1();
    private final Map<String, NhcLocation1> locations = new ConcurrentHashMap<>();

    @Nullable
    private Socket nhcSocket;
    @Nullable
    private PrintWriter nhcOut;
    @Nullable
    private BufferedReader nhcIn;

    private volatile boolean listenerStopped;
    private volatile boolean nhcEventsRunning;

    // We keep only 2 gson adapters used to serialize and deserialize all messages sent and received
    protected final Gson gsonOut = new Gson();
    protected Gson gsonIn;

    /**
     * Constructor for Niko Home Control I communication object, manages communication with
     * Niko Home Control IP-interface.
     *
     */
    public NikoHomeControlCommunication1(NhcControllerEvent handler) {
        // When we set up this object, we want to get the proper gson adapter set up once
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(NhcMessageBase1.class, new NikoHomeControlMessageDeserializer1());
        this.gsonIn = gsonBuilder.create();
        this.handler = handler;
    }

    @Override
    public synchronized void startCommunication() {
        try {
            for (int i = 1; nhcEventsRunning && (i <= 5); i++) {
                // the events listener thread did not finish yet, so wait max 5000ms before restarting
                Thread.sleep(1000);
            }
            if (nhcEventsRunning) {
                logger.error("Niko Home Control: starting but previous connection still active after 5000ms");
                throw new IOException();
            }

            InetAddress addr = handler.getAddr();
            @SuppressWarnings("null") // default value specified, so cannot be null
            int port = handler.getPort();

            this.nhcSocket = new Socket(addr, port);
            Socket socket = this.nhcSocket;
            this.nhcOut = new PrintWriter(socket.getOutputStream(), true);
            this.nhcIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            logger.debug("Niko Home Control: connected via local port {}", socket.getLocalPort());

            // initialize all info in local fields
            initialize();

            // Start Niko Home Control event listener. This listener will act on all messages coming from
            // IP-interface.
            (new Thread(nhcEvents)).start();

        } catch (IOException | InterruptedException e) {
            logger.warn("Niko Home Control: error initializing communication");
            stopCommunication();
            this.handler.controllerOffline();
        }
    }

    /**
     * Cleanup socket when the communication with Niko Home Control IP-interface is closed.
     *
     */
    @Override
    public synchronized void stopCommunication() {
        this.listenerStopped = true;

        if (this.nhcSocket != null) {
            try {
                this.nhcSocket.close();
            } catch (IOException ignore) {
                // ignore IO Error when trying to close the socket if the intention is to close it anyway
            }
        }
        this.nhcSocket = null;

        logger.debug("Niko Home Control: communication stopped");
    }

    @Override
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
    @SuppressWarnings("null")
    private Runnable nhcEvents = () -> {
        @Nullable
        String nhcMessage;

        logger.debug("Niko Home Control: listening for events");
        listenerStopped = false;
        nhcEventsRunning = true;

        try {
            while (!listenerStopped & ((nhcMessage = this.nhcIn.readLine()) != null)) {
                readMessage(nhcMessage);
            }
        } catch (IOException e) {
            if (!listenerStopped) {
                nhcEventsRunning = false;
                // this is a socket error, not a communication stop triggered from outside this runnable
                logger.warn("Niko Home Control: IO error in listener");
                // the IO has stopped working, so we need to close cleanly and try to restart
                restartCommunication();
                return;
            }
        }

        nhcEventsRunning = false;
        // this is a stop from outside the runnable, so just log it and stop
        logger.debug("Niko Home Control: event listener thread stopped");
    };

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

    @SuppressWarnings("null")
    private void sendAndReadMessage(String command) throws IOException {
        sendMessage(new NhcMessageCmd1(command));
        readMessage(this.nhcIn.readLine());
    }

    /**
     * Called by other methods to send json cmd to Niko Home Control.
     *
     * @param nhcMessage
     */
    @SuppressWarnings("null")
    private synchronized void sendMessage(Object nhcMessage) {
        String json = gsonOut.toJson(nhcMessage);
        logger.debug("Niko Home Control: send json {}", json);
        this.nhcOut.println(json);
        if (this.nhcOut.checkError()) {
            logger.warn("Niko Home Control: error sending message, trying to restart communication");
            restartCommunication();
            // retry sending after restart
            logger.debug("Niko Home Control: resend json {}", json);
            this.nhcOut.println(json);
            if (this.nhcOut.checkError()) {
                logger.warn("Niko Home Control: error resending message");
                this.handler.controllerOffline();
            }
        }
    }

    /**
     * Method that interprets all feedback from Niko Home Control and calls appropriate handling methods.
     *
     * @param nhcMessage message read from Niko Home Control.
     */
    private void readMessage(@Nullable String nhcMessage) {
        logger.debug("Niko Home Control: received json {}", nhcMessage);

        try {
            NhcMessageBase1 nhcMessageGson = this.gsonIn.fromJson(nhcMessage, NhcMessageBase1.class);

            String cmd = nhcMessageGson.getCmd();
            String event = nhcMessageGson.getEvent();

            if ("systeminfo".equals(cmd)) {
                cmdSystemInfo(((NhcMessageMap1) nhcMessageGson).getData());
            } else if ("startevents".equals(cmd)) {
                cmdStartEvents(((NhcMessageMap1) nhcMessageGson).getData());
            } else if ("listlocations".equals(cmd)) {
                cmdListLocations(((NhcMessageListMap1) nhcMessageGson).getData());
            } else if ("listactions".equals(cmd)) {
                cmdListActions(((NhcMessageListMap1) nhcMessageGson).getData());
            } else if (("listthermostat").equals(cmd)) {
                cmdListThermostat(((NhcMessageListMap1) nhcMessageGson).getData());
            } else if ("executeactions".equals(cmd)) {
                cmdExecuteActions(((NhcMessageMap1) nhcMessageGson).getData());
            } else if ("executethermostat".equals(cmd)) {
                cmdExecuteThermostat(((NhcMessageMap1) nhcMessageGson).getData());
            } else if ("listactions".equals(event)) {
                eventListActions(((NhcMessageListMap1) nhcMessageGson).getData());
            } else if ("listthermostat".equals(event)) {
                eventListThermostat(((NhcMessageListMap1) nhcMessageGson).getData());
            } else if ("getalarms".equals(event)) {
                eventGetAlarms(((NhcMessageMap1) nhcMessageGson).getData());
            } else {
                logger.debug("Niko Home Control: not acted on json {}", nhcMessage);
            }
        } catch (JsonParseException e) {
            logger.debug("Niko Home Control: not acted on unsupported json {}", nhcMessage);
        }
    }

    private synchronized void cmdSystemInfo(Map<String, String> data) {
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

    /**
     * Return the object with system info as read from the Niko Home Control controller.
     *
     * @return the systemInfo
     */
    public synchronized NhcSystemInfo1 getSystemInfo() {
        return this.systemInfo;
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
            String id = location.get("id");
            String name = location.get("name");
            NhcLocation1 nhcLocation1 = new NhcLocation1(name);
            this.locations.put(id, nhcLocation1);
        }
    }

    private void cmdListActions(List<Map<String, String>> data) {
        logger.debug("Niko Home Control: list actions");

        for (Map<String, String> action : data) {

            String id = action.get("id");
            Integer state = Integer.valueOf(action.get("value1"));
            String value2 = action.get("value2");
            int closeTime = ((value2 == null) || value2.isEmpty() ? 0 : Integer.valueOf(value2));
            String value3 = action.get("value3");
            int openTime = ((value3 == null) || value3.isEmpty() ? 0 : Integer.valueOf(value3));

            if (!this.actions.containsKey(id)) {
                // Initial instantiation of NhcAction class for action object
                String name = action.get("name");
                String type = action.get("type");
                ActionType actionType = ActionType.GENERIC;
                switch (type) {
                    case "0":
                        actionType = ActionType.TRIGGER;
                        break;
                    case "1":
                        actionType = ActionType.RELAY;
                        break;
                    case "2":
                        actionType = ActionType.DIMMER;
                        break;
                    case "4":
                    case "5":
                        actionType = ActionType.ROLLERSHUTTER;
                        break;
                    default:
                        logger.debug("Niko Home Control: unknown action type {} for action {}", type, id);
                        continue;
                }
                String locationId = action.get("location");
                String location = "";
                if (!locationId.isEmpty()) {
                    location = this.locations.get(locationId).getName();
                }
                NhcAction nhcAction = new NhcAction1(id, name, actionType, location);
                if (actionType == ActionType.ROLLERSHUTTER) {
                    nhcAction.setShutterTimes(openTime, closeTime);
                }
                nhcAction.setNhcComm(this);
                nhcAction.setState(state);
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

            String id = thermostat.get("id");
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
                String locationId = thermostat.get("location");
                String location = "";
                if (!locationId.isEmpty()) {
                    location = this.locations.get(locationId).getName();
                }
                NhcThermostat nhcThermostat = new NhcThermostat1(id, name, location);
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
            String id = action.get("id");
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
            String id = thermostat.get("id");
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
        Integer type = Integer.valueOf(data.get("type"));
        String alarmText = data.get("text");
        switch (type) {
            case 0:
                logger.debug("Niko Home Control: alarm - {}", alarmText);
                handler.alarmEvent(alarmText);
                break;
            case 1:
                logger.debug("Niko Home Control: notice - {}", alarmText);
                handler.noticeEvent(alarmText);
                break;
            default:
                logger.debug("Niko Home Control: unexpected message type {}", type);
        }
    }

    @Override
    public void executeAction(String actionId, String value) {
        NhcMessageCmd1 nhcCmd = new NhcMessageCmd1("executeactions", Integer.valueOf(actionId), Integer.valueOf(value));
        sendMessage(nhcCmd);
    }

    @Override
    public void executeThermostat(String thermostatId, String mode) {
        NhcMessageCmd1 nhcCmd = new NhcMessageCmd1("executethermostat", Integer.valueOf(thermostatId))
                .withMode(Integer.valueOf(mode));
        sendMessage(nhcCmd);
    }

    @Override
    public void executeThermostat(String thermostatId, int overruleTemp, int overruleTime) {
        String overruletimeString = String.format("%1$02d:%2$02d", overruleTime / 60, overruleTime % 60);
        NhcMessageCmd1 nhcCmd = new NhcMessageCmd1("executethermostat", Integer.valueOf(thermostatId))
                .withOverrule(overruleTemp).withOverruletime(overruletimeString);
        sendMessage(nhcCmd);
    }
}
