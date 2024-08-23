/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcAction;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcControllerEvent;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcMeter;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcThermostat;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.ActionType;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.MeterType;
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

    private static final int TIMEOUT_MILLIS = 2000;
    private static final int TIMOUT_LONG_MILLIS = 10000;
    private static final String DATE_TIME_PATTERN = "yyyyMMddHHmm";
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);

    private final NhcSystemInfo1 systemInfo = new NhcSystemInfo1();
    private final Map<String, NhcLocation1> locations = new ConcurrentHashMap<>();

    private @Nullable Socket nhcSocket;
    private @Nullable PrintWriter nhcOut;
    private @Nullable BufferedReader nhcIn;

    private @Nullable Socket nhcEnergySocket; // dedicated socket for energy data to avoid blocking main communication
    private @Nullable PrintWriter nhcEnergyOut;
    private @Nullable BufferedReader nhcEnergyIn;

    private volatile boolean listenerStopped;
    private volatile boolean nhcEventsRunning;

    // Synchronization of send/receive, used to block sending new commands when response to previous command has not
    // been received
    private volatile @Nullable CompletableFuture<Boolean> cmdResponseFuture;

    private Object executeMeterLock = new Object(); // only allow a single send and read cycle at the same time

    // The meter reading response does not contain the channel and end date, so we have to store it when giving a meter
    // reading command
    private volatile String meterReadingChannel = "";
    private @Nullable volatile LocalDateTime meterReadingEnd;
    private volatile boolean meterReadingInit;

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
        super(handler, scheduler);
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

            handler.controllerOnline();
        } catch (InterruptedException e) {
            handler.controllerOffline("@text/offline.communication-error");
        } catch (IOException e) {
            handler.controllerOffline("@text/offline.communication-error");
            scheduleRestartCommunication();
        }
    }

    /**
     * Cleanup socket when the communication with Niko Home Control IP-interface is closed.
     *
     */
    @Override
    public synchronized void resetCommunication() {
        listenerStopped = true;

        socketClose(nhcSocket);
        nhcSocket = null;
        socketClose(nhcEnergySocket);
        nhcEnergySocket = null;

        CompletableFuture<Boolean> future = cmdResponseFuture;
        if (future != null) {
            future.complete(false);
        }
        cmdResponseFuture = null;

        logger.debug("communication stopped");
    }

    private void socketClose(@Nullable Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignore) {
                // ignore IO Error when trying to close the socket if the intention is to close it anyway
            }
        }
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
            BufferedReader in = nhcIn;
            if (in != null) {
                while (!listenerStopped && ((nhcMessage = in.readLine()) != null)) {
                    readMessage(nhcMessage);
                }
            }
        } catch (IOException e) {
            if (!listenerStopped) {
                nhcEventsRunning = false;
                // this is a socket error, not a communication stop triggered from outside this runnable
                logger.debug("IO error in listener");
                // the IO has stopped working, so we need to close cleanly and try to restart
                scheduleRestartCommunication();
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
     *
     * @throws IOException
     */
    private void initialize() throws IOException {
        Socket socket = nhcSocket;
        PrintWriter out = nhcOut;
        BufferedReader in = nhcIn;
        if ((socket != null) && (in != null) && (out != null)) {
            sendAndReadMessage(new NhcMessageCmd1("systeminfo"), socket, out, in);
            sendAndReadMessage(new NhcMessageCmd1("listlocations"), socket, out, in);
            sendAndReadMessage(new NhcMessageCmd1("listactions"), socket, out, in);
            sendAndReadMessage(new NhcMessageCmd1("listthermostat"), socket, out, in);
            sendAndReadMessage(new NhcMessageCmd1("listthermostatHVAC"), socket, out, in);
            sendAndReadMessage(new NhcMessageCmd1("listenergy"), socket, out, in);
            sendAndReadMessage(new NhcMessageCmd1("readtariffdata"), socket, out, in);
            sendAndReadMessage(new NhcMessageCmd1("getalarms"), socket, out, in);
            sendAndReadMessage(new NhcMessageCmd1("startevents"), socket, out, in);
        } else {
            throw (new IOException("socket not initialized"));
        }
    }

    /**
     * Send a command and read the response. This should only be used when there is no listener active on the socket to
     * listen to responses. In that case send and received would be decoupled.
     *
     * @param command
     * @param out
     * @param in
     * @throws IOException
     */
    private void sendAndReadMessage(Object command, Socket socket, PrintWriter out, BufferedReader in)
            throws IOException {
        socket.setSoTimeout(TIMOUT_LONG_MILLIS);
        try {
            sendMessage(command, out, false);
            readMessage(in.readLine(), false);
        } catch (SocketTimeoutException e) {
            logger.debug("Did not receive a response in {} ms", TIMOUT_LONG_MILLIS);
            throw (e);
        }
        socket.setSoTimeout(0);
    }

    /**
     * Method that interprets all feedback from Niko Home Control and calls appropriate handling methods.
     *
     * @param nhcMessage message read from Niko Home Control.
     */
    private void readMessage(@Nullable String nhcMessage) {
        readMessage(nhcMessage, true);
    }

    private void readMessage(@Nullable String nhcMessage, boolean hasEventLoop) {
        NhcMessageBase1 nhcMessageGson;
        String cmd;
        String event;

        if (nhcMessage == null) {
            logger.debug("empty message, nothing to interpret");
            return;
        }

        try {
            nhcMessageGson = gsonIn.fromJson(nhcMessage, NhcMessageBase1.class);

            if (nhcMessageGson == null) {
                return;
            }
            cmd = nhcMessageGson.getCmd();
            event = nhcMessageGson.getEvent();
        } catch (JsonParseException e) {
            logger.debug("not acted on unsupported json {}", nhcMessage);
            return;
        }

        logger.trace("received json {}", nhcMessage);

        // We received a command response from the listener, so can allow the next cmd to be sent
        if (hasEventLoop && !cmd.isEmpty()) {
            CompletableFuture<Boolean> responseFuture = cmdResponseFuture;
            if (responseFuture != null) {
                responseFuture.complete(true);
            }
            cmdResponseFuture = null;
        }

        try {
            if ("systeminfo".equals(cmd)) {
                cmdSystemInfo(((NhcMessageMap1) nhcMessageGson).getData());
            } else if ("startevents".equals(cmd)) {
                cmdStartEvents(((NhcMessageMap1) nhcMessageGson).getData());
            } else if ("stoplive".equals(cmd)) {
                cmdStopLive(((NhcMessageMap1) nhcMessageGson).getData());
            } else if ("getlive".equals(cmd)) {
                cmdGetLive(((NhcMessageMap1) nhcMessageGson).getData());
            } else if ("listlocations".equals(cmd)) {
                cmdListLocations(((NhcMessageListMap1) nhcMessageGson).getData());
            } else if ("listactions".equals(cmd)) {
                cmdListActions(((NhcMessageListMap1) nhcMessageGson).getData());
            } else if (("listthermostat").equals(cmd)) {
                cmdListThermostat(((NhcMessageListMap1) nhcMessageGson).getData());
            } else if ("listenergy".equals(cmd)) {
                cmdListEnergy(((NhcMessageListMap1) nhcMessageGson).getData());
            } else if ("executeactions".equals(cmd)) {
                cmdExecuteActions(((NhcMessageMap1) nhcMessageGson).getData());
            } else if ("executethermostat".equals(cmd)) {
                cmdExecuteThermostat(((NhcMessageMap1) nhcMessageGson).getData());
            } else if ("getenergydata".equals(cmd)) {
                cmdGetEnergyData(((NhcMessageList1) nhcMessageGson).getData(), meterReadingChannel, meterReadingEnd,
                        meterReadingInit);
            } else if ("listactions".equals(event)) {
                eventListActions(((NhcMessageListMap1) nhcMessageGson).getData());
            } else if ("listthermostat".equals(event)) {
                eventListThermostat(((NhcMessageListMap1) nhcMessageGson).getData());
            } else if ("getlive".equals(event)) {
                eventGetLive(((NhcMessageMap1) nhcMessageGson).getData());
            } else if ("getalarms".equals(event)) {
                eventGetAlarms(((NhcMessageMap1) nhcMessageGson).getData());
            } else {
                logger.debug("not acted on json {}", nhcMessage);
            }
        } catch (ClassCastException e) {
            readError(nhcMessage, nhcMessageGson);
        }
    }

    private void readError(String nhcMessage, NhcMessageBase1 nhcMessageGson) {
        try {
            Map<String, String> data = ((NhcMessageMap1) nhcMessageGson).getData();
            if (data.containsKey("error")) {
                logger.warn("received error message {} from controller", data.get("error"));
                logger.debug("received error with json {}", nhcMessage);
            } else {
                logger.debug("received unsupported format in json {}", nhcMessage);
            }
        } catch (ClassCastException e) {
            logger.debug("received unsupported format in json {}", nhcMessage);
        }
    }

    private void sendMessage(Object nhcMessage, PrintWriter out) {
        sendMessage(nhcMessage, out, true);
    }

    /**
     * Called by other methods to send json cmd to Niko Home Control.
     *
     * @param nhcMessage
     * @param out
     * @param hasEventLoop Should be true for asynchronous send/receive
     */
    private synchronized void sendMessage(Object nhcMessage, PrintWriter out, boolean hasEventLoop) {
        String json = gsonOut.toJson(nhcMessage);
        logger.trace("request to send json {}", json);

        boolean responseReceived = true;
        boolean sendFailure = false;

        // When the event loop is running, we need to make sure we received the previous response before sending
        // anything
        if (hasEventLoop) {
            CompletableFuture<Boolean> responseFuture = cmdResponseFuture;
            if ((responseFuture != null) && !responseFuture.isDone()) {
                try {
                    // Wait until we have received the full response to the previous command.
                    responseFuture.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    responseReceived = false;
                    sendFailure = true;
                    logger.debug("exception waiting cmd response");
                }
            }
            cmdResponseFuture = new CompletableFuture<>();
        }

        if (responseReceived) {
            logger.debug("send json {}", json);
            out.println(json);
            if (out.checkError()) {
                sendFailure = true;
            }
        }

        if (sendFailure) {
            // This will make sure no further request are being sent until we are back up
            nhcEnergyOut = null;
            nhcOut = null;
            restartOnSendFailure(out, json);
        }
    }

    private void restartOnSendFailure(PrintWriter out, String json) {
        logger.debug("error sending message, trying to restart communication");
        restartCommunication();
        // retry sending after restart
        cmdResponseFuture = new CompletableFuture<>();
        logger.debug("resend json {}", json);
        out.println(json);
        if (out.checkError()) {
            // This will make sure no further request are being sent until we are back up
            nhcOut = null;

            handler.controllerOffline("@text/offline.communication-error");
            // Keep on trying to restart, but don't send message anymore
            scheduleRestartCommunication();
        }
    }

    private void setIfPresent(Map<String, String> data, String key, Consumer<String> consumer) {
        String val = data.get(key);
        if (val != null) {
            consumer.accept(val);
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

    private void cmdSystemInfo(Map<String, String> data) {
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
    public NhcSystemInfo1 getSystemInfo() {
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

    private void cmdStopLive(Map<String, String> data) {
        String errorCodeString = data.get("error");
        if (errorCodeString != null) {
            int errorCode = Integer.parseInt(errorCodeString);
            if (errorCode == 0) {
                logger.debug("Stop live meter events success");
            } else {
                logger.debug("error code {} returned on stop live", errorCode);
            }
        } else {
            logger.debug("could not determine error code returned on stop live");
        }
    }

    private void cmdGetLive(Map<String, String> data) {
        eventGetLive(data);
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

            if (!getActions().containsKey(id)) {
                // Initial instantiation of NhcAction class for action object
                NhcAction nhcAction = new NhcAction1(id, name, actionType, location, this, scheduler);
                if (actionType == ActionType.ROLLERSHUTTER) {
                    nhcAction.setShutterTimes(openTime, closeTime);
                }
                nhcAction.setState(state);
                actions.put(id, nhcAction);
            } else {
                // Action object already exists, so only update state, name and location.
                // If we would re-instantiate action, we would lose pointer back from action to thing handler that was
                // set in thing handler initialize().
                NhcAction nhcAction = getActions().get(id);
                if (nhcAction != null) {
                    nhcAction.setName(name);
                    nhcAction.setLocation(location);
                    nhcAction.setState(state);
                }
            }
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

                String name = thermostat.get("name");
                String locationId = thermostat.get("location");
                NhcLocation1 nhcLocation = null;
                if (!((locationId == null) || locationId.isEmpty())) {
                    nhcLocation = locations.get(locationId);
                }
                String location = (nhcLocation != null) ? nhcLocation.getName() : null;

                NhcThermostat t = getThermostats().computeIfAbsent(id, i -> {
                    // Initial instantiation of NhcThermostat class for thermostat object
                    if (name != null) {
                        return new NhcThermostat1(i, name, location, this);
                    }
                    throw new IllegalArgumentException();
                });
                if (t != null) {
                    if (name != null) {
                        t.setName(name);
                    }
                    t.setLocation(location);
                    t.setState(measured, setpoint, mode, overrule, overruletime, ecosave, demand);
                }
            } catch (IllegalArgumentException e) {
                // do nothing
            }
        }
    }

    private void cmdListEnergy(List<Map<String, String>> data) {
        logger.debug("list energy");

        DateTimeFormatter format = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN).withZone(getTimeZone());

        for (Map<String, String> meter : data) {
            String id = meter.get("channel");
            if (id == null) {
                logger.debug("skipping energy meter {}, id not found", meter);
                continue;
            }

            String name = meter.get("name");
            if (name == null) {
                logger.debug("name not found in meter {}", meter);
                continue;
            }
            String type = meter.getOrDefault("type", "");
            String energy = meter.getOrDefault("energy", "");
            String live = meter.getOrDefault("live", "");
            String referenceDateString = meter.getOrDefault("startdate", "");
            LocalDateTime referenceDate;
            try {
                referenceDate = LocalDateTime.parse(referenceDateString, format);
            } catch (DateTimeParseException e) {
                logger.debug("cannot parse reference date {} for meter {}", referenceDateString, id);
                referenceDate = null;
            }
            MeterType meterType = MeterType.GENERIC;
            switch (energy) {
                case "0":
                    if ("1".equals(live)) {
                        meterType = MeterType.ENERGY_LIVE;
                    } else {
                        meterType = MeterType.ENERGY;
                    }
                    break;
                case "1":
                    meterType = MeterType.GAS;
                    break;
                case "2":
                    meterType = MeterType.WATER;
                    break;
                default:
                    logger.debug("unknown meter energy {} for meter {}", energy, id);
                    continue;
            }

            String locationId = meter.get("location");
            NhcLocation1 nhcLocation = null;
            if (!((locationId == null) || locationId.isEmpty())) {
                nhcLocation = locations.get(locationId);
            }
            String location = (nhcLocation != null) ? nhcLocation.getName() : null;
            if (!getMeters().containsKey(id)) {
                // Initial instantiation of NhcMeter class for meter object
                NhcMeter nhcMeter = new NhcMeter1(id, name, meterType, location, type, referenceDate, this, scheduler);
                meters.put(id, nhcMeter);
            } else {
                // Meter object already exists, so only name and location.
                // If we would re-instantiate meter, we would lose pointer back from meter to thing handler that was
                // set in thing handler initialize().
                NhcMeter nhcMeter = getMeters().get(id);
                if (nhcMeter != null) {
                    nhcMeter.setName(name);
                    nhcMeter.setLocation(location);
                }
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

    private void cmdGetEnergyData(List<String> data, String id, @Nullable LocalDateTime meterReadingEnd, boolean init) {
        if (id.isEmpty()) {
            logger.debug("received meter data but none requested, ignoring");
            return;
        }
        NhcMeter meter = getMeters().get(id);
        if (meter == null) {
            logger.debug("received meter data for {} but no meter found, ignoring", id);
            return;
        }
        if (meterReadingEnd == null) {
            logger.debug("received meter reading data for {} but request did not have end date, ignoring", id);
            return;
        }

        LocalDateTime lastReading = meter.getLastReading();
        if (lastReading == null) {
            lastReading = meter.getReferenceDate();
            if (lastReading == null) {
                logger.debug("error getting meter data for {}, no meter reference date available", id);
                return;
            }
        }

        int reading;
        int dayReading;
        ZonedDateTime lastReadingCurrentZone = lastReading.atZone(ZoneOffset.UTC).withZoneSameInstant(getTimeZone());
        ZonedDateTime meterReadingEndCurrentZone = meterReadingEnd.atZone(ZoneOffset.UTC)
                .withZoneSameInstant(getTimeZone());
        boolean dayChange = meterReadingEndCurrentZone.truncatedTo(ChronoUnit.DAYS).isAfter(lastReadingCurrentZone);
        long beforeDayStart = Math.max(0, ChronoUnit.MINUTES.between(lastReadingCurrentZone,
                meterReadingEndCurrentZone.truncatedTo(ChronoUnit.DAYS)) / 10);

        logger.trace("received {} individual meter readings for {}, summing up", data.size(), id);

        try {
            if (init) {
                reading = data.stream().mapToInt(Integer::parseInt).sum();
                dayReading = data.stream().skip(beforeDayStart).mapToInt(Integer::parseInt).sum();
            } else {
                int value = data.stream().skip(1).mapToInt(Integer::parseInt).sum();
                reading = meter.getReadingInt() + value;
                logger.trace("adding {} to meter {} reading, new reading {}", value, id, reading);
                if (dayChange) {
                    dayReading = data.stream().skip(1 + beforeDayStart).mapToInt(Integer::parseInt).sum();
                    logger.trace("meter {} day reading, it's a new day, new reading {}", id, dayReading);
                } else {
                    dayReading = meter.getDayReadingInt() + value;
                    logger.trace("adding {} to meter {} day reading, new reading {}", value, id, dayReading);
                }
            }
        } catch (NumberFormatException e) {
            logger.debug("error in meter readings received for {}", id);
            return;
        }

        logger.debug("received meter reading for {}: total {}, day {}", id, reading, dayReading);
        meter.setReading(reading, dayReading, meterReadingEnd);
    }

    private void eventListActions(List<Map<String, String>> data) {
        for (Map<String, String> action : data) {
            String id = action.get("id");
            if (id == null || !getActions().containsKey(id)) {
                logger.warn("action in controller not known {}", id);
                return;
            }
            String stateString = action.get("value1");
            if (stateString != null) {
                int state = Integer.parseInt(stateString);
                logger.debug("event execute action {} with state {}", id, state);
                NhcAction action1 = getActions().get(id);
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
                if (!getThermostats().containsKey(id)) {
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
                        "event execute thermostat {} with measured {}, setpoint {}, mode {}, overrule {}, overruletime {}, ecosave {}, demand {}",
                        id, measured, setpoint, mode, overrule, overruletime, ecosave, demand);
                NhcThermostat t = getThermostats().get(id);
                if (t != null) {
                    t.setState(measured, setpoint, mode, overrule, overruletime, ecosave, demand);
                }
            } catch (IllegalArgumentException e) {
                // do nothing
            }
        }
    }

    private void eventGetLive(Map<String, String> data) {
        try {
            String channel = data.get("channel");
            int v = parseIntOrThrow(data.get("v"));
            logger.debug("event live power channel {} with v {}", channel, v);
            NhcMeter e = getMeters().get(channel);
            if (e != null) {
                e.setPower(v);
            }
        } catch (IllegalArgumentException e) {
            // do nothing
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
        PrintWriter out = nhcOut;
        if (out != null) {
            NhcMessageCmd1 nhcCmd = new NhcMessageCmd1("executeactions", Integer.parseInt(actionId),
                    Integer.parseInt(value));
            sendMessage(nhcCmd, out);
        }
    }

    @Override
    public void executeThermostat(String thermostatId, String mode) {
        PrintWriter out = nhcOut;
        if (out != null) {
            NhcMessageCmd1 nhcCmd = new NhcMessageCmd1("executethermostat", Integer.parseInt(thermostatId))
                    .withMode(Integer.parseInt(mode));
            sendMessage(nhcCmd, out);
        }
    }

    @Override
    public void executeThermostat(String thermostatId, int overruleTemp, int overruleTime) {
        PrintWriter out = nhcOut;
        if (out != null) {
            String overruletimeString = String.format("%1$02d:%2$02d", overruleTime / 60, overruleTime % 60);
            NhcMessageCmd1 nhcCmd = new NhcMessageCmd1("executethermostat", Integer.parseInt(thermostatId))
                    .withOverrule(overruleTemp).withOverruletime(overruletimeString);
            sendMessage(nhcCmd, out);
        }
    }

    @Override
    public void executeMeter(String meterId) {
        NhcMeter meter = getMeters().get(meterId);
        if (meter == null) {
            return;
        }

        if (!communicationActive()) {
            logger.debug("Communication not active, not getting meter data for {}", meterId);
            return;
        }

        // only update one meter at a time and do it on a dedicated socket
        synchronized (executeMeterLock) {
            Socket socket = nhcEnergySocket;
            BufferedReader in = nhcEnergyIn;
            PrintWriter out = nhcEnergyOut;

            try {
                if ((socket == null) || socket.isClosed() || (in == null) || (out == null)) {
                    InetAddress addr = handler.getAddr();
                    int port = handler.getPort();

                    socketClose(socket);
                    socket = new Socket(addr, port);
                    nhcEnergySocket = socket;
                    out = new PrintWriter(socket.getOutputStream(), true);
                    nhcEnergyOut = out;
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    nhcEnergyIn = in;
                    logger.debug("connected via local port {} for energy data", socket.getLocalPort());
                }
            } catch (IOException e) {
                logger.debug("not able to establish communication to read meter data");
                return;
            }

            try {
                meterReadingInit = false;

                LocalDateTime start = meter.getLastReading();
                if (start == null) {
                    meterReadingInit = true;
                    start = meter.getReferenceDate();
                    if (start == null) {
                        logger.debug("error getting meter data, no meter reference date available");
                        return;
                    }
                }

                String readingStart = start.format(DATE_TIME_FORMAT);
                LocalDateTime end = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
                String readingEnd = end.format(DATE_TIME_FORMAT);

                meterReadingChannel = meterId;
                meterReadingEnd = end;

                NhcMessageCmd1 nhcCmd = new NhcMessageCmd1("getenergydata").withChannel(Integer.parseInt(meterId))
                        .withInterval(readingStart, readingEnd);
                if (logger.isTraceEnabled()) {
                    long interval = ChronoUnit.MINUTES.between(start, end);
                    long number = interval / 10;
                    int minute = start.getMinute();
                    if ((number > 0) || (((minute + interval) / 10) > (start.getMinute() / 10))) {
                        number += 1;
                    }
                    logger.trace("expecting {} readings between {} and {} for {}", number, readingStart, readingEnd,
                            meterId);
                }
                sendAndReadMessage(nhcCmd, socket, out, in);
            } catch (IOException e) {
                logger.debug("error getting meter data for {}", meterId);
            }
        }
    }

    @Override
    public synchronized void retriggerMeterLive(String meterId) {
        if (!communicationActive()) {
            logger.debug("Communication not active, not live getting meter data for {}", meterId);
            return;
        }

        PrintWriter out = nhcOut;
        if (out != null) {
            NhcMessageCmd1 nhcCmd = new NhcMessageCmd1("stoplive").withChannel(Integer.parseInt(meterId));
            sendMessage(nhcCmd, out);
            nhcCmd = new NhcMessageCmd1("getlive").withChannel(Integer.parseInt(meterId));
            sendMessage(nhcCmd, out);
        }
    }
}
