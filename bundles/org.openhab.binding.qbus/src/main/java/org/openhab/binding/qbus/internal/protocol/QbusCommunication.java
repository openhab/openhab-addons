/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.qbus.internal.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.qbus.internal.QbusBridgeHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * The {@link QbusCommunication} class is able to do the following tasks with Qbus
 * systems:
 * <ul>
 * <li>Start and stop TCP socket connection with Qbus Server.
 * <li>Read all setup and status information from the Qbus Controller.
 * <li>Execute Qbus commands.
 * <li>Listen to events from Qbus.
 * </ul>
 *
 * A class instance is instantiated from the {@link QbusBridgeHandler} class initialization.
 *
 * @author Koen Schockaert - Initial Contribution
 */
@NonNullByDefault
public final class QbusCommunication {

    private final Logger logger = LoggerFactory.getLogger(QbusCommunication.class);

    @Nullable
    private Socket qSocket;
    @Nullable
    private PrintWriter qOut;
    @Nullable
    private BufferedReader qIn;

    private boolean listenerStopped;
    private boolean qEventsRunning;

    private Gson gsonOut = new Gson();
    private Gson gsonIn;

    private final Map<Integer, QbusScene> scenes = new HashMap<>();
    private final Map<Integer, QbusBistabiel> bistabiel = new HashMap<>();
    private final Map<Integer, QbusDimmer> dimmer = new HashMap<>();
    private final Map<Integer, QThermostat> thermostats = new HashMap<>();
    private final Map<Integer, QbusCO2> co2 = new HashMap<>();
    private final Map<Integer, QbusRol> Rol = new HashMap<>();
    // private final Map<String, String> Disconnect = new HashMap<>();

    @Nullable
    private QbusBridgeHandler bridgeCallBack;

    /**
     * Constructor for Qbus communication object, manages communication with
     * Qbus Server.
     *
     */
    public QbusCommunication() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(QbusMessageBase.class, new QbusMessageDeserializer());
        this.gsonIn = gsonBuilder.create();
    }

    /**
     * Start communication with Qbus Server, run through initialization and start thread listening
     * to all messages coming from Qbus.
     *
     * @param addr : IP-address of Qbus Server
     * @param port : Communication port of Qbus server
     * @param sn : Serial number of the controller
     *
     */
    public synchronized void startCommunication() {
        QbusBridgeHandler handler = this.bridgeCallBack;

        try {
            for (int i = 1; qEventsRunning && (i <= 5); i++) {
                Thread.sleep(1000);
            }
            if (qEventsRunning) {
                logger.error("Qbus: starting from thread {}, but previous connection still active after 5000ms",
                        Thread.currentThread().getId());
                throw new IOException();
            }

            if (handler == null) {
                throw new IOException();
            }

            InetAddress addr = handler.getAddr();
            int port = handler.getPort();

            Socket socket = new Socket(addr, port);
            this.qSocket = socket;
            this.qOut = new PrintWriter(socket.getOutputStream(), true);
            this.qIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            logger.debug("Qbus: connected via local port {} from thread {}", socket.getLocalPort(),
                    Thread.currentThread().getId());

            initialize();

            (new Thread(qEvents)).start();

        } catch (IOException | InterruptedException e) {
            logger.warn("Qbus: error initializing communication from thread {}", Thread.currentThread().getId());
            if (handler != null) {
                handler.bridgeOffline();
            }
            stopCommunication();

        }
    }

    /**
     * Cleanup socket when the communication with Qbus Server is closed.
     *
     * @throws IOException
     *
     */
    public synchronized void stopCommunication() {
        this.listenerStopped = true;

        Socket socket = this.qSocket;

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignore) {
                // ignore IO Error when trying to close the socket if the intention is to close it anyway
            }
        }

        this.qSocket = null;
        // restartCommunication();
        logger.debug("Qbus: communication stopped from thread {}", Thread.currentThread().getId());
    }

    /**
     * Close and restart communication with Qbus Server.
     *
     */
    public synchronized void restartCommunication() {
        QbusBridgeHandler handler = this.bridgeCallBack;
        stopCommunication();

        // handler.bridgeOffline();

        if (handler != null) {
            handler.bridgeOffline();
        }

        logger.debug("Qbus: restart communication from thread {}", Thread.currentThread().getId());

        startCommunication();
    }

    /**
     * Method to check if communication with Qbus Server is active
     *
     * @return True if active
     */
    public boolean communicationActive() {
        return (this.qSocket != null);
    }

    /**
     * Runnable that handles inbound communication from Qbus server.
     * <p>
     * The thread listens to the TCP socket opened at instantiation of the {@link QbusCommunication} class
     * and interprets all inbound json messages. It triggers state updates for active channels linked to the
     * Qbus outputs. It is started after initialization of the communication.
     *
     */
    private Runnable qEvents = () -> {
        String qMessage;
        // QbusBridgeHandler handler = this.bridgeCallBack;

        logger.debug("Qbus: listening for events on thread {}", Thread.currentThread().getId());
        listenerStopped = false;
        qEventsRunning = true;

        BufferedReader reader = this.qIn;

        try {
            if (reader == null) {
                throw new IOException();
            }
            while (!listenerStopped & ((qMessage = reader.readLine()) != null)) {
                if (qMessage != null) {
                    readMessage(qMessage);
                }
            }
        } catch (IOException e) {
            if (!listenerStopped) {
                qEventsRunning = false;
                logger.warn("Qbus: IO error in listener on thread {}", Thread.currentThread().getId());
                restartCommunication();
                return;
            }
        }

        qEventsRunning = false;

        stopCommunication();

        logger.debug("Qbus: event listener thread stopped on thread {}", Thread.currentThread().getId());

    };

    /**
     * Method that interprets all feedback from Qbus Server application and calls appropriate handling methods.
     *
     * @param qMessage message read from Qbus.
     */
    private void readMessage(String qMessage) {
        logger.debug("Qbus: received json on thread {}", Thread.currentThread().getId());

        try {
            QbusMessageBase qMessageGson = this.gsonIn.fromJson(qMessage, QbusMessageBase.class);
            String cmd = "";
            String event = "";

            @SuppressWarnings("null")
            String confsn = this.bridgeCallBack.getSn();
            @SuppressWarnings("null")
            String sn = qMessageGson.getSn();
            cmd = qMessageGson.getCmd();
            event = qMessageGson.getEvent();

            if (Integer.parseInt(confsn) == Integer.parseInt(sn)) {
                // Get the compatible outputs from the Qbus server
                if ("listbistabiel".equals(cmd)) {
                    cmdListBistabiel(((QMessageListMap) qMessageGson).getData());
                } else if ("listdimmers".equals(cmd)) {
                    cmdListDimmers(((QMessageListMap) qMessageGson).getData());
                } else if (("listthermostats").equals(cmd)) {
                    cmdListThermostat(((QMessageListMap) qMessageGson).getData());
                } else if (("listscenes").equals(cmd)) {
                    cmdlistscenes(((QMessageListMap) qMessageGson).getData());
                } else if (("listco2").equals(cmd)) {
                    cmdlistco2(((QMessageListMap) qMessageGson).getData());
                } else if (("listrol").equals(cmd)) {
                    cmdlistrol(((QMessageListMap) qMessageGson).getData());
                } else if (("listrol02pslats").equals(cmd)) {
                    cmdlistrolslats(((QMessageListMap) qMessageGson).getData());
                }
                // Commands to execute from openHAB to Qbus
                else if ("executebistabiel".equals(cmd)) {
                    cmdExecuteBistabiel(((QMessageMap) qMessageGson).getData());
                } else if ("executedimmers".equals(cmd)) {
                    cmdExecuteDimmer(((QMessageMap) qMessageGson).getData());
                } else if ("executethermostat".equals(cmd)) {
                    cmdExecuteThermostat(((QMessageMap) qMessageGson).getData());
                } else if ("executescene".equals(cmd)) {
                    cmdExecuteScene(((QMessageMap) qMessageGson).getData());
                } else if ("executeslats".equals(cmd)) {
                    cmdExecuteSlats(((QMessageMap) qMessageGson).getData());
                } else if ("executerol".equals(cmd)) {
                    cmdExecuteRol(((QMessageMap) qMessageGson).getData());
                } else if ("executerol02pslats".equals(cmd)) {
                    cmdExecuteRolslats(((QMessageMap) qMessageGson).getData());
                }
                // Incoming commands from Qbus Server to openHAB (event)
                else if ("listbistabiel".equals(event)) {
                    eventListBistabiel(((QMessageListMap) qMessageGson).getData());
                } else if ("listdimmers".equals(event)) {
                    eventListDimmers(((QMessageListMap) qMessageGson).getData());
                } else if ("listthermostat".equals(event)) {
                    eventListThermostat(((QMessageListMap) qMessageGson).getData());
                } else if ("listscenes".equals(event)) {
                    eventListScenes(((QMessageListMap) qMessageGson).getData());
                } else if ("listco2".equals(event)) {
                    eventListCO2(((QMessageListMap) qMessageGson).getData());
                } else if ("listrol".equals(event)) {
                    eventListRol(((QMessageListMap) qMessageGson).getData());
                } else if ("listrol02pslats".equals(event)) {
                    eventListRolslats(((QMessageListMap) qMessageGson).getData());
                }
                //
                else if ("disconnect".equals(event)) {
                    eventFunction(((QMessageListMap) qMessageGson).getData());
                }
            }
        } catch (JsonParseException e) {
            logger.debug("Qbus: not acted on unsupported json {}", qMessage);
        }
    }

    /**
     * After setting up the communication with the Qbus Server, send all initialization messages.
     * <p>
     * First send connect to connect with the Qbus Server application
     * Get request for the Scenes
     * Get request for Bistabiel/Timers/Intervals/Mono outputs
     * Get request for Dimmers 1T and 2T
     * Get request for Thermostats
     * Get request for CO2
     * Get request for Shutters
     *
     * @throws IOException
     * @throws InterruptedException
     */
    private void initialize() throws IOException, InterruptedException {
        Connect();
        sendAndReadMessage("listrol");
        sendAndReadMessage("listrol02pslats");
        sendAndReadMessage("listscenes");
        sendAndReadMessage("listbistabiel");
        sendAndReadMessage("listdimmers");
        sendAndReadMessage("listthermostats");
        sendAndReadMessage("listco2");
    }

    /**
     * Initial connection to Qbus Server to open a communication channel
     *
     * @throws InterruptedException
     */
    private void Connect() throws InterruptedException {
        @SuppressWarnings("null")
        String confsn = this.bridgeCallBack.getSn();
        QMessageCmd qCmd = new QMessageCmd("openHAB").withSn(confsn);
        sendMessage(qCmd);
        TimeUnit.MILLISECONDS.sleep(250);
    }

    /**
     * Send message to Qbus server and read response
     */
    private void sendAndReadMessage(String command) throws IOException, InterruptedException {
        @SuppressWarnings("null")
        String confsn = this.bridgeCallBack.getSn();
        QMessageCmd qCmd = new QMessageCmd(command).withSn(confsn);

        sendMessage(qCmd);

        BufferedReader reader = this.qIn;
        if (reader == null) {
            throw new IOException("Cannot read from socket, reader not connected.");
        }
        readMessage(reader.readLine());
    }

    /**
     * Get all the scenes from the Qbus server
     *
     * @param data
     */
    private void cmdlistscenes(@Nullable List<Map<String, String>> data) {
        logger.debug("Qbus: Scenes received from Qbus server");

        if (data != null) {
            for (Map<String, String> scene : data) {
                String idStr = scene.get("id");
                if (idStr != null) {
                    try {
                        int id = Integer.parseInt(idStr);
                        QbusScene Scene = new QbusScene(id);
                        Scene.setQComm(this);
                        this.scenes.put(id, Scene);
                    } catch (Exception e) {
                        logger.debug("Qbus: Error in json for Scenes");
                    }
                } else {
                    logger.debug("Qbus: Error in json for Scenes Nullvalue");
                }
            }
        }
    }

    /**
     * Get all the CO2 outputs from the Qbus server
     *
     * @param data
     */
    @SuppressWarnings("null")
    private void cmdlistco2(@Nullable List<Map<String, String>> data) {
        logger.debug("Qbus: CO2 received from Qbus server");

        if (data != null) {
            for (Map<String, String> co2 : data) {
                String idStr = co2.get("id");
                String value1Str = co2.get("value1");
                if (idStr != null && value1Str != null) {

                    int id = Integer.parseInt(idStr);
                    Integer co = Integer.parseInt(value1Str);

                    if (!this.co2.containsKey(id)) {
                        QbusCO2 CO2 = new QbusCO2(id);
                        this.co2.put(id, CO2);
                        this.co2.get(id).setState(co);
                    } else {
                        this.co2.get(id).setState(co);
                    }

                } else {
                    logger.debug("Qbus: Error in json for CO2 Nullvalue");
                }

            }
        }
    }

    /**
     * Get all the Positioning module outputs from the Qbus server
     *
     * @param data
     */
    @SuppressWarnings("null")
    private void cmdlistrol(@Nullable List<Map<String, String>> data) {
        logger.debug("Qbus: ROL02P received from Qbus server");

        if (data != null) {
            for (Map<String, String> rol : data) {
                String idStr = rol.get("id");
                String value1Str = rol.get("value1");
                if (idStr != null && value1Str != null) {
                    int id = Integer.parseInt(idStr);
                    Integer rolpos = Integer.valueOf(value1Str);
                    if (!this.Rol.containsKey(id)) {
                        QbusRol Rol = new QbusRol(id);
                        Rol.setQComm(this);
                        this.Rol.put(id, Rol);
                        this.Rol.get(id).setState(rolpos);
                    } else {
                        this.Rol.get(id).setState(rolpos);
                    }
                } else {
                    logger.debug("Qbus: Error in json for ROL02P Nullvalue");
                }
            }
        }
    }

    /**
     * Get all the Positioning module outputs from the Qbus server
     *
     * @param data
     */
    @SuppressWarnings("null")
    private void cmdlistrolslats(@Nullable List<Map<String, String>> data) {
        logger.debug("Qbus: ROL02PSLATS received from Qbus server");

        if (data != null) {
            for (Map<String, String> rol : data) {

                String idStr = rol.get("id");
                String value1Str = rol.get("value1");
                String value2Str = rol.get("value2");
                if (idStr != null && value1Str != null && value2Str != null) {
                    int id = Integer.parseInt(idStr);
                    Integer rolpos = Integer.valueOf(value1Str);
                    Integer rolposslats = Integer.valueOf(value2Str);
                    rolpos = Integer.valueOf(rolpos);
                    rolposslats = Integer.valueOf(rolposslats);
                    if (!this.Rol.containsKey(id)) {
                        QbusRol Rol = new QbusRol(id);
                        Rol.setQComm(this);
                        this.Rol.put(id, Rol);
                        this.Rol.get(id).setState(rolpos);
                        this.Rol.get(id).setSlats(rolposslats);
                    } else {
                        this.Rol.get(id).setState(rolpos);
                        this.Rol.get(id).setSlats(rolposslats);
                    }
                } else {
                    logger.debug("Qbus: Error in json for ROL02P_Slats Nullvalue");
                }
            }
        }
    }

    /**
     * Get all the Bistabiel/Timers/Mono/Interval from the Qbus server
     *
     * @param data
     */
    @SuppressWarnings("null")
    private void cmdListBistabiel(@Nullable List<Map<String, String>> data) {
        logger.debug("Qbus: Bistabiel/Timers/Monos/Intervals received from Qbus server");

        if (data != null) {
            for (Map<String, String> bistabiel : data) {
                String idStr = bistabiel.get("id");
                String value1Str = bistabiel.get("value1");
                if (idStr != null && value1Str != null) {
                    int id = Integer.parseInt(idStr);
                    Integer state = Integer.parseInt(value1Str);
                    if (!this.bistabiel.containsKey(id)) {
                        QbusBistabiel qBistabiel = new QbusBistabiel(id);
                        qBistabiel.setState(state);
                        qBistabiel.setQComm(this);
                        this.bistabiel.put(id, qBistabiel);
                    } else {
                        this.bistabiel.get(id).setState(state);
                    }
                }
            }
        }
    }

    /**
     * Get all the Dimmer outputs from the Qbus server
     *
     * @param data
     */
    @SuppressWarnings("null")
    private void cmdListDimmers(@Nullable List<Map<String, String>> data) {
        logger.debug("Qbus: Dimmers received from the Qbus server");

        if (data != null) {
            for (Map<String, String> dimmer : data) {
                String idStr = dimmer.get("id");
                String value1Str = dimmer.get("value1");
                if (idStr != null && value1Str != null) {
                    int id = Integer.parseInt(idStr);
                    Integer state = Integer.parseInt(value1Str);
                    if (!this.dimmer.containsKey(id)) {
                        QbusDimmer qDimmer = new QbusDimmer(id);
                        qDimmer.updateState(state);
                        qDimmer.setQComm(this);
                        this.dimmer.put(id, qDimmer);
                    } else {
                        this.dimmer.get(id).updateState(state);
                    }
                }
            }
        }
    }

    /**
     * Get all the Thermostat outputs from the Qbus server
     *
     * @param data
     */
    @SuppressWarnings("null")
    private void cmdListThermostat(@Nullable List<Map<String, String>> data) {
        logger.debug("Qbus: thermostats received from the Qbus server");

        if (data != null) {
            for (Map<String, String> thermostat : data) {
                String idStr = thermostat.get("id");
                String measuredStr = thermostat.get("measured");
                String setpointStr = thermostat.get("setpoint");
                String modeStr = thermostat.get("mode");
                if (idStr != null && measuredStr != null && setpointStr != null && modeStr != null) {
                    int id = Integer.parseInt(idStr);
                    Double measured = Double.valueOf(measuredStr);
                    Double setpoint = Double.valueOf(setpointStr);
                    Integer mode = Integer.valueOf(modeStr);

                    if (!this.thermostats.containsKey(id)) {
                        QThermostat qThermostat = new QThermostat(id);
                        qThermostat.updateState(measured, setpoint, mode);
                        qThermostat.setQComm(this);
                        this.thermostats.put(id, qThermostat);
                    } else {
                        this.thermostats.get(id).updateState(measured, setpoint, mode);
                    }
                }
            }
        }
    }

    /**
     * Execute Bistabiel/Timers/Monos/Intervals
     *
     * @param data
     */
    private void cmdExecuteBistabiel(Map<String, String> data) {
        String errorCodeStr = data.get("error");
        if (errorCodeStr != null) {
            Integer errorCode = Integer.valueOf(errorCodeStr);
            if (errorCode.equals(0)) {
                logger.debug("Qbus: execute bistabiel success");
            } else {
                logger.warn("Qbus: error code {} returned on command execution", errorCode);
            }
        }
    }

    /**
     * Execute Scenes
     *
     * @param data
     */
    private void cmdExecuteScene(Map<String, String> data) {
        String errorCodeStr = data.get("error");
        if (errorCodeStr != null) {
            Integer errorCode = Integer.valueOf(errorCodeStr);
            if (errorCode.equals(0)) {
                logger.debug("Qbus: execute scene success");
            } else {
                logger.warn("Qbus: error code {} returned on command execution", errorCode);
            }
        }
    }

    /**
     * Execute Dimmers
     *
     * @param data
     */
    private void cmdExecuteDimmer(Map<String, String> data) {
        String errorCodeStr = data.get("error");
        if (errorCodeStr != null) {
            Integer errorCode = Integer.valueOf(errorCodeStr);
            if (errorCode.equals(0)) {
                logger.debug("Qbus: execute dimmer success");
            } else {
                logger.warn("Qbus: error code {} returned on command execution", errorCode);
            }
        }
    }

    /**
     * Execute Slats
     *
     * @param data
     */
    private void cmdExecuteSlats(Map<String, String> data) {
        String errorCodeStr = data.get("error");
        if (errorCodeStr != null) {
            Integer errorCode = Integer.valueOf(errorCodeStr);
            if (errorCode.equals(0)) {
                logger.debug("Qbus: execute slats success");
            } else {
                logger.warn("Qbus: error code {} returned on command execution", errorCode);
            }
        }
    }

    /**
     * Execute Shutter
     *
     * @param data
     */
    private void cmdExecuteRol(Map<String, String> data) {
        String errorCodeStr = data.get("error");
        if (errorCodeStr != null) {
            Integer errorCode = Integer.valueOf(errorCodeStr);
            if (errorCode.equals(0)) {
                logger.debug("Qbus: execute shutter success");
            } else {
                logger.warn("Qbus: error code {} returned on command execution", errorCode);
            }
        }
    }

    /**
     * Execute Shutter with slats
     *
     * @param data
     */
    private void cmdExecuteRolslats(Map<String, String> data) {
        String errorCodeStr = data.get("error");
        if (errorCodeStr != null) {
            Integer errorCode = Integer.valueOf(errorCodeStr);
            if (errorCode.equals(0)) {
                logger.debug("Qbus: execute shutter with slats success");
            } else {
                logger.warn("Qbus: error code {} returned on command execution", errorCode);
            }
        }
    }

    /**
     * Execute Thermostats
     *
     * @param data
     */
    private void cmdExecuteThermostat(Map<String, String> data) {
        String errorCodeStr = data.get("error");
        if (errorCodeStr != null) {
            Integer errorCode = Integer.valueOf(errorCodeStr);
            if (errorCode.equals(0)) {
                logger.debug("Qbus: execute thermostat success");
            } else {
                logger.warn("Qbus: error code {} returned on command execution", errorCode);
            }
        }
    }

    /**
     * Event on incomming Bistabiel/Timer/Mono/Interval updates
     *
     * @param data
     */
    @SuppressWarnings("null")
    private void eventListBistabiel(List<Map<String, String>> data) {
        for (Map<String, String> bistabiel : data) {
            String idStr = bistabiel.get("id");
            String value1Str = bistabiel.get("value1Str");
            if (idStr != null && value1Str != null) {
                int id = Integer.valueOf(idStr);
                int value1 = Integer.valueOf(value1Str);
                if (!this.bistabiel.containsKey(id)) {
                    logger.warn("Qbus: bistabiel in controller not known {}", id);
                    return;
                }
                logger.debug("Qbus: event execute bistabiel {} with state {}", id, value1);
                this.bistabiel.get(id).setState(value1);
            }
        }
    }

    /**
     * Event on incomming CO2 updates
     *
     * @param data
     */
    @SuppressWarnings("null")
    private void eventListCO2(List<Map<String, String>> data) {
        for (Map<String, String> co2 : data) {
            String idStr = co2.get("id");
            String value1Str = co2.get("value1Str");
            if (idStr != null && value1Str != null) {
                int id = Integer.valueOf(idStr);
                int value1 = Integer.valueOf(value1Str);
                if (!this.co2.containsKey(id)) {
                    logger.warn("Qbus: co2 in controller not known {}", id);
                    return;
                }
                logger.debug("Qbus: event execute co2 {} with state {}", id, value1);
                this.co2.get(id).setState(value1);
            }
        }
    }

    /**
     * Event on incomming ROL02P without updates
     *
     * @param data
     */
    @SuppressWarnings("null")
    private void eventListRol(List<Map<String, String>> data) {
        for (Map<String, String> rol : data) {

            String idStr = rol.get("id");
            String value1Str = rol.get("value1Str");
            if (idStr != null && value1Str != null) {
                int id = Integer.valueOf(idStr);
                int value1 = Integer.valueOf(value1Str);

                if (!this.Rol.containsKey(id)) {
                    logger.warn("Qbus: Rol02p in controller not known {}", id);
                    return;
                }
                logger.debug("Qbus: event execute Rol02P {} with pos {}", id, value1);
                this.Rol.get(id).setState(value1);

            }
        }
    }

    /**
     * Event on incomming ROL02P with slats updates
     *
     * @param data
     */
    @SuppressWarnings("null")
    private void eventListRolslats(List<Map<String, String>> data) {
        for (Map<String, String> rol : data) {
            String idStr = rol.get("id");
            String value1Str = rol.get("pos");
            String value2Str = rol.get("slats");
            if (idStr != null && value1Str != null && value2Str != null) {
                int id = Integer.valueOf(idStr);
                int value1 = Integer.valueOf(value1Str);
                int value2 = Integer.valueOf(value2Str);
                if (!this.Rol.containsKey(id)) {
                    logger.warn("Qbus: Rol02p in controller not known {}", id);
                    return;
                }

                logger.debug("Qbus: event execute ROL02P_Slats {} with pos {} and slats {}", id, value1, value2);
                this.Rol.get(id).setState(value1);
                this.Rol.get(id).setSlats(value2);
            }
        }
    }

    /**
     * Event on incomming Scene updates
     *
     * @param data
     */
    @SuppressWarnings("null")
    private void eventListScenes(List<Map<String, String>> data) {
        for (Map<String, String> scene : data) {
            String idStr = scene.get("id");
            String value1Str = scene.get("value1");
            if (idStr != null && value1Str != null) {
                int id = Integer.valueOf(idStr);
                int value1 = Integer.valueOf(value1Str);
                if (!this.scenes.containsKey(id)) {
                    logger.warn("Qbus: scene in controller not known {}", id);
                    return;
                }

                logger.debug("Qbus: event execute scene {} with state {}", id, value1);
                this.scenes.get(id).setState(value1);
            }
        }
    }

    /**
     * Event on incomming Dimmer updates
     *
     * @param data
     */
    @SuppressWarnings("null")
    private void eventListDimmers(List<Map<String, String>> data) {
        for (Map<String, String> dimmer : data) {
            String idStr = dimmer.get("id");
            String value1Str = dimmer.get("value1");
            if (idStr != null && value1Str != null) {
                int id = Integer.valueOf(idStr);
                int value1 = Integer.valueOf(value1Str);

                if (!this.dimmer.containsKey(id)) {
                    logger.warn("Qbus: dimmer in controller not known {}", id);
                    return;
                }

                logger.debug("Qbus: event execute dimmer {} with state {}", id, value1);
                this.dimmer.get(id).setState(value1);
            }
        }
    }

    /**
     * Event on incomming thermostat updates
     *
     * @param data
     */
    @SuppressWarnings("null")
    private void eventListThermostat(List<Map<String, String>> data) {
        for (Map<String, String> thermostat : data) {
            String idStr = thermostat.get("id");
            String measuredStr = thermostat.get("measured");
            String setpointdStr = thermostat.get("setpoint");
            String modedStr = thermostat.get("mode");
            if (idStr != null && measuredStr != null && setpointdStr != null && modedStr != null) {
                int id = Integer.valueOf(idStr);
                Double measured = Double.valueOf(measuredStr);
                Double setpoint = Double.valueOf(setpointdStr);
                Integer mode = Integer.valueOf(modedStr);

                if (!this.thermostats.containsKey(id)) {
                    logger.warn("Qbus: thermostat in controller not known {}", id);
                    return;
                }

                logger.debug("Qbus: event execute thermostat {} with measured {}, setpoint {}, mode {}", id, measured,
                        setpoint, mode);
                this.thermostats.get(id).updateState(measured, setpoint, mode);
            }
        }
    }

    /*
     * /**
     * Event on Disconnect
     *
     * @param data
     */
    private void eventFunction(List<Map<String, String>> data) {
        // for (Map<String, String> function : data) {
        logger.debug("Disconnect");
        // String ctd = function.get("ctd");
        // String funcion = function.get("function");

        QbusBridgeHandler handler = this.bridgeCallBack;

        if (handler != null) {
            stopCommunication();
            handler.bridgeOffline();

        }
    }

    /**
     * Called by other methods to send json cmd to Qbus.
     *
     * @param qMessage
     */
    synchronized void sendMessage(Object qMessage) {
        PrintWriter writer = this.qOut;
        String json = gsonOut.toJson(qMessage);
        logger.debug("Qbus: send json from thread {}", Thread.currentThread().getId());

        if (writer != null) {
            writer.println(json);

            try {
                TimeUnit.MILLISECONDS.sleep(250);
            } catch (InterruptedException e) {
                // No reaction on error is required
            }

        }
        if ((writer == null) || (writer.checkError())) {
            logger.warn("Qbus: error sending message, trying to restart communication");
            restartCommunication();
            // retry sending after restart
            logger.debug("Qbus: resend json from thread {}", Thread.currentThread().getId());
            writer = this.qOut;
            if (writer != null) {
                writer.println(json);
            }
            if ((writer == null) || (writer.checkError())) {
                logger.warn("Qbus: error resending message");

            }
        }
    }

    /**
     * Return all Bistabiel/Timers/Mono/Intervals in the Qbus Controller.
     *
     * @return
     */
    public Map<Integer, QbusBistabiel> getBistabiel() {
        return this.bistabiel;
    }

    /**
     * Return all Dimmers in the Qbus Controller.
     *
     * @return
     */
    public Map<Integer, QbusDimmer> getDimmer() {
        return this.dimmer;
    }

    /**
     * Return all Scenes in the Qbus Controller
     *
     * @return
     */
    public Map<Integer, QbusScene> getScenes() {
        return this.scenes;
    }

    /**
     * Return all Thermostats in the Qbus Controller.
     *
     * @return
     */
    public Map<Integer, QThermostat> getThermostats() {
        return this.thermostats;
    }

    /**
     * Return all CO2 in the Qbus Controller.
     *
     * @return
     */
    public Map<Integer, QbusCO2> getCo2() {
        return this.co2;
    }

    /**
     * Return all ROL02P outûts in the Qbus Controller.
     *
     * @return
     */
    public Map<Integer, QbusRol> getRol() {
        return this.Rol;
    }

    /**
     * @param bridgeCallBack the bridgeCallBack to set
     */
    public void setBridgeCallBack(QbusBridgeHandler bridgeCallBack) {
        this.bridgeCallBack = bridgeCallBack;
    }
}
