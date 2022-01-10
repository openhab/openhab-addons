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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc1;

import static org.openhab.binding.nikohomecontrol.internal.NikoHomeControlBindingConstants.THREAD_NAME_PREFIX;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

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
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NikoHomeControlCommunication1 extends NikoHomeControlCommunication {

    private Logger logger = LoggerFactory.getLogger(NikoHomeControlCommunication1.class);

    private String eventThreadName = THREAD_NAME_PREFIX;

    private final NhcSystemInfo1 systemInfo = new NhcSystemInfo1();
    private final Map<String, NhcLocation1> locations = new ConcurrentHashMap<>();

    private @Nullable Socket nhcSocket;
    private @Nullable PrintWriter nhcOut;
    private @Nullable BufferedReader nhcIn;

    private volatile boolean listenerStopped;
    private volatile boolean nhcEventsRunning;

    private ScheduledExecutorService scheduler;

    // We keep only 2 gson adapters used to serialize and deserialize all messages sent and received
    protected final Gson gsonOut = new Gson();
    protected Gson gsonIn;

    /**
     * Constructor for Niko Home Control I communication object, manages communication with
     * Niko Home Control IP-interface.
     *
     */
    public NikoHomeControlCommunication1(NhcControllerEvent handler, ScheduledExecutorService scheduler,
            String eventThreadName) {
        super(handler);
        this.scheduler = scheduler;
        this.eventThreadName = eventThreadName;

        // When we set up this object, we want to get the proper gson adapter set up once
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(NhcMessageBase1.class, new NikoHomeControlMessageDeserializer1());
        gsonIn = gsonBuilder.create();
    }

    @Override
    public synchronized void startCommunication() {
        try {
            for (int i = 1; nhcEventsRunning && (i <= 5); i++) {
                // the events listener thread did not finish yet, so wait max 5000ms before restarting
                Thread.sleep(1000);
            }
            if (nhcEventsRunning) {
                logger.debug("starting but previous connection still active after 5000ms");
                throw new IOException();
            }

            InetAddress addr = handler.getAddr();
            int port = handler.getPort();

            Socket socket = new Socket(addr, port);
            nhcSocket = socket;
            nhcOut = new PrintWriter(socket.getOutputStream(), true);
            nhcIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            logger.debug("connected via local port {}", socket.getLocalPort());

            // initialize all info in local fields
            initialize();

            // Start Niko Home Control event listener. This listener will act on all messages coming from
            // IP-interface.
            (new Thread(this::runNhcEvents, eventThreadName)).start();

        } catch (IOException | InterruptedException e) {
            stopCommunication();
            handler.controllerOffline("@text/offline.communication-error");
        }
    }

    /**
     * Cleanup socket when the communication with Niko Home Control IP-interface is closed.
     *
     */
    @Override
    public synchronized void stopCommunication() {
        listenerStopped = true;

        Socket socket = nhcSocket;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignore) {
                // ignore IO Error when trying to close the socket if the intention is to close it anyway
            }
        }
        nhcSocket = null;

        logger.debug("communication stopped");
    }

    @Override
    public boolean communicationActive() {
        return (nhcSocket != null);
    }

    /**
     * Method that handles inbound communication from Niko Home Control, to be called on a separate thread.
     * <p>
     * The thread listens to the TCP socket opened at instantiation of the {@link NikoHomeControlCommunication} class
     * and interprets all inbound json messages. It triggers state updates for active channels linked to the Niko Home
     * Control actions. It is started after initialization of the communication.
     *
     */
    private void runNhcEvents() {
        String nhcMessage;

        logger.debug("listening for events");
        listenerStopped = false;
        nhcEventsRunning = true;

        try {
            while (!listenerStopped & (nhcIn != null) & ((nhcMessage = nhcIn.readLine()) != null)) {
                readMessage(nhcMessage);
            }
        } catch (IOException e) {
            if (!listenerStopped) {
                nhcEventsRunning = false;
                // this is a socket error, not a communication stop triggered from outside this runnable
                logger.debug("IO error in listener");
                // the IO has stopped working, so we need to close cleanly and try to restart
                restartCommunication();
                return;
            }
        } finally {
            nhcEventsRunning = false;
        }

        nhcEventsRunning = false;
        // this is a stop from outside the runnable, so just log it and stop
        logger.debug("event listener thread stopped");
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

    @SuppressWarnings("null")
    private void sendAndReadMessage(String command) throws IOException {
        sendMessage(new NhcMessageCmd1(command));
        readMessage(nhcIn.readLine());
    }

    /**
     * Called by other methods to send json cmd to Niko Home Control.
     *
     * @param nhcMessage
     */
    @SuppressWarnings("null")
    private synchronized void sendMessage(Object nhcMessage) {
        String json = gsonOut.toJson(nhcMessage);
        logger.debug("send json {}", json);
        nhcOut.println(json);
        if (nhcOut.checkError()) {
            logger.debug("error sending message, trying to restart communication");
            restartCommunication();
            // retry sending after restart
            logger.debug("resend json {}", json);
            nhcOut.println(json);
            if (nhcOut.checkError()) {
                handler.controllerOffline("@text/offline.communication-error");
            }
        }
    }

    /**
     * Method that interprets all feedback from Niko Home Control and calls appropriate handling methods.
     *
     * @param nhcMessage message read from Niko Home Control.
     */
    private void readMessage(@Nullable String nhcMessage) {
        logger.debug("received json {}", nhcMessage);

        try {
            NhcMessageBase1 nhcMessageGson = gsonIn.fromJson(nhcMessage, NhcMessageBase1.class);

            if (nhcMessageGson == null) {
                return;
            }
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
                logger.debug("not acted on json {}", nhcMessage);
            }
        } catch (JsonParseException e) {
            logger.debug("not acted on unsupported json {}", nhcMessage);
        }
    }

    private void setIfPresent(Map<String, String> data, String key, Consumer<String> consumer) {
        String val = data.get(key);
        if (val != null) {
            consumer.accept(val);
        }
    }

    private synchronized void cmdSystemInfo(Map<String, String> data) {
        logger.debug("systeminfo");

        setIfPresent(data, "swversion", systemInfo::setSwVersion);
        setIfPresent(data, "api", systemInfo::setApi);
        setIfPresent(data, "time", systemInfo::setTime);
        setIfPresent(data, "language", systemInfo::setLanguage);
        setIfPresent(data, "currency", systemInfo::setCurrency);
        setIfPresent(data, "units", systemInfo::setUnits);
        setIfPresent(data, "DST", systemInfo::setDst);
        setIfPresent(data, "TZ", systemInfo::setTz);
        setIfPresent(data, "lastenergyerase", systemInfo::setLastEnergyErase);
        setIfPresent(data, "lastconfig", systemInfo::setLastConfig);
    }

    /**
     * Return the object with system info as read from the Niko Home Control controller.
     *
     * @return the systemInfo
     */
    public synchronized NhcSystemInfo1 getSystemInfo() {
        return systemInfo;
    }

    private void cmdStartEvents(Map<String, String> data) {
        String errorCodeString = data.get("error");
        if (errorCodeString != null) {
            int errorCode = Integer.parseInt(errorCodeString);
            if (errorCode == 0) {
                logger.debug("start events success");
            } else {
                logger.debug("error code {} returned on start events", errorCode);
            }
        } else {
            logger.debug("could not determine error code returned on start events");
        }
    }

    private void cmdListLocations(List<Map<String, String>> data) {
        logger.debug("list locations");

        locations.clear();

        for (Map<String, String> location : data) {
            String id = location.get("id");
            String name = location.get("name");
            if (id == null || name == null) {
                logger.debug("id or name null, ignoring entry");
                continue;
            }
            NhcLocation1 nhcLocation1 = new NhcLocation1(name);
            locations.put(id, nhcLocation1);
        }
    }

    private void cmdListActions(List<Map<String, String>> data) {
        logger.debug("list actions");

        for (Map<String, String> action : data) {
            String id = action.get("id");
            if (id == null) {
                logger.debug("id not found in action {}", action);
                continue;
            }
            String value1 = action.get("value1");
            int state = ((value1 == null) || value1.isEmpty() ? 0 : Integer.parseInt(value1));
            String value2 = action.get("value2");
            int closeTime = ((value2 == null) || value2.isEmpty() ? 0 : Integer.parseInt(value2));
            String value3 = action.get("value3");
            int openTime = ((value3 == null) || value3.isEmpty() ? 0 : Integer.parseInt(value3));

            if (!actions.containsKey(id)) {
                // Initial instantiation of NhcAction class for action object
                String name = action.get("name");
                if (name == null) {
                    logger.debug("name not found in action {}", action);
                    continue;
                }
                String type = Optional.ofNullable(action.get("type")).orElse("");
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
                        logger.debug("unknown action type {} for action {}", type, id);
                        continue;
                }
                String locationId = action.get("location");
                String location = "";
                if (locationId != null && !locationId.isEmpty()) {
                    location = locations.getOrDefault(locationId, new NhcLocation1("")).getName();
                }
                NhcAction nhcAction = new NhcAction1(id, name, actionType, location, this, scheduler);
                if (actionType == ActionType.ROLLERSHUTTER) {
                    nhcAction.setShutterTimes(openTime, closeTime);
                }
                nhcAction.setState(state);
                actions.put(id, nhcAction);
            } else {
                // Action object already exists, so only update state.
                // If we would re-instantiate action, we would lose pointer back from action to thing handler that was
                // set in thing handler initialize().
                NhcAction nhcAction = actions.get(id);
                if (nhcAction != null) {
                    nhcAction.setState(state);
                }
            }
        }
    }

    private int parseIntOrThrow(@Nullable String str) throws IllegalArgumentException {
        if (str == null) {
            throw new IllegalArgumentException("String is null");
        }
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void cmdListThermostat(List<Map<String, String>> data) {
        logger.debug("list thermostats");

        for (Map<String, String> thermostat : data) {
            try {
                String id = thermostat.get("id");
                if (id == null) {
                    logger.debug("skipping thermostat {}, id not found", thermostat);
                    continue;
                }
                int measured = parseIntOrThrow(thermostat.get("measured"));
                int setpoint = parseIntOrThrow(thermostat.get("setpoint"));
                int mode = parseIntOrThrow(thermostat.get("mode"));
                int overrule = parseIntOrThrow(thermostat.get("overrule"));
                // overruletime received in "HH:MM" format
                String[] overruletimeStrings = thermostat.getOrDefault("overruletime", "").split(":");
                int overruletime = 0;
                if (overruletimeStrings.length == 2) {
                    overruletime = Integer.parseInt(overruletimeStrings[0]) * 60
                            + Integer.parseInt(overruletimeStrings[1]);
                }
                int ecosave = parseIntOrThrow(thermostat.get("ecosave"));

                // For parity with NHC II, assume heating/cooling if thermostat is on and setpoint different from
                // measured
                int demand = (mode != 3) ? (setpoint > measured ? 1 : (setpoint < measured ? -1 : 0)) : 0;

                NhcThermostat t = thermostats.computeIfAbsent(id, i -> {
                    // Initial instantiation of NhcThermostat class for thermostat object
                    String name = thermostat.get("name");
                    String locationId = thermostat.get("location");
                    String location = "";
                    if (!((locationId == null) || locationId.isEmpty())) {
                        NhcLocation1 nhcLocation = locations.get(locationId);
                        if (nhcLocation != null) {
                            location = nhcLocation.getName();
                        }
                    }
                    if (name != null) {
                        return new NhcThermostat1(i, name, location, this);
                    }
                    throw new IllegalArgumentException();
                });
                if (t != null) {
                    t.updateState(measured, setpoint, mode, overrule, overruletime, ecosave, demand);
                }
            } catch (IllegalArgumentException e) {
                // do nothing
            }
        }
    }

    private void cmdExecuteActions(Map<String, String> data) {
        try {
            int errorCode = parseIntOrThrow(data.get("error"));
            if (errorCode == 0) {
                logger.debug("execute action success");
            } else {
                logger.debug("error code {} returned on command execution", errorCode);
            }
        } catch (IllegalArgumentException e) {
            logger.debug("no error code returned on command execution");
        }
    }

    private void cmdExecuteThermostat(Map<String, String> data) {
        try {
            int errorCode = parseIntOrThrow(data.get("error"));
            if (errorCode == 0) {
                logger.debug("execute thermostats success");
            } else {
                logger.debug("error code {} returned on command execution", errorCode);
            }
        } catch (IllegalArgumentException e) {
            logger.debug("no error code returned on command execution");
        }
    }

    private void eventListActions(List<Map<String, String>> data) {
        for (Map<String, String> action : data) {
            String id = action.get("id");
            if (id == null || !actions.containsKey(id)) {
                logger.warn("action in controller not known {}", id);
                return;
            }
            String stateString = action.get("value1");
            if (stateString != null) {
                int state = Integer.parseInt(stateString);
                logger.debug("event execute action {} with state {}", id, state);
                NhcAction action1 = actions.get(id);
                if (action1 != null) {
                    action1.setState(state);
                }
            }
        }
    }

    private void eventListThermostat(List<Map<String, String>> data) {
        for (Map<String, String> thermostat : data) {
            try {
                String id = thermostat.get("id");
                if (!thermostats.containsKey(id)) {
                    logger.warn("thermostat in controller not known {}", id);
                    return;
                }

                int measured = parseIntOrThrow(thermostat.get("measured"));
                int setpoint = parseIntOrThrow(thermostat.get("setpoint"));
                int mode = parseIntOrThrow(thermostat.get("mode"));
                int overrule = parseIntOrThrow(thermostat.get("overrule"));
                // overruletime received in "HH:MM" format
                String[] overruletimeStrings = thermostat.getOrDefault("overruletime", "").split(":");
                int overruletime = 0;
                if (overruletimeStrings.length == 2) {
                    overruletime = Integer.parseInt(overruletimeStrings[0]) * 60
                            + Integer.parseInt(overruletimeStrings[1]);
                }
                int ecosave = parseIntOrThrow(thermostat.get("ecosave"));

                int demand = (mode != 3) ? (setpoint > measured ? 1 : (setpoint < measured ? -1 : 0)) : 0;

                logger.debug(
                        "Niko Home Control: event execute thermostat {} with measured {}, setpoint {}, mode {}, overrule {}, overruletime {}, ecosave {}, demand {}",
                        id, measured, setpoint, mode, overrule, overruletime, ecosave, demand);
                NhcThermostat t = thermostats.get(id);
                if (t != null) {
                    t.updateState(measured, setpoint, mode, overrule, overruletime, ecosave, demand);
                }
            } catch (IllegalArgumentException e) {
                // do nothing
            }
        }
    }

    private void eventGetAlarms(Map<String, String> data) {
        String alarmText = data.get("text");
        if (alarmText == null) {
            logger.debug("message does not contain alarmtext: {}", data);
            return;
        }
        switch (data.getOrDefault("type", "")) {
            case "0":
                logger.debug("alarm - {}", alarmText);
                handler.alarmEvent(alarmText);
                break;
            case "1":
                logger.debug("notice - {}", alarmText);
                handler.noticeEvent(alarmText);
                break;
            default:
                logger.debug("unexpected message type {}", data.get("type"));
        }
    }

    @Override
    public void executeAction(String actionId, String value) {
        NhcMessageCmd1 nhcCmd = new NhcMessageCmd1("executeactions", Integer.parseInt(actionId),
                Integer.parseInt(value));
        sendMessage(nhcCmd);
    }

    @Override
    public void executeThermostat(String thermostatId, String mode) {
        NhcMessageCmd1 nhcCmd = new NhcMessageCmd1("executethermostat", Integer.parseInt(thermostatId))
                .withMode(Integer.parseInt(mode));
        sendMessage(nhcCmd);
    }

    @Override
    public void executeThermostat(String thermostatId, int overruleTemp, int overruleTime) {
        String overruletimeString = String.format("%1$02d:%2$02d", overruleTime / 60, overruleTime % 60);
        NhcMessageCmd1 nhcCmd = new NhcMessageCmd1("executethermostat", Integer.parseInt(thermostatId))
                .withOverrule(overruleTemp).withOverruletime(overruletimeString);
        sendMessage(nhcCmd);
    }
}
