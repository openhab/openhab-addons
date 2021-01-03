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
 * <li>Read all the outputs and their status information from the Qbus Controller.
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

    private String CTD = "";
    // @Nullable
    private Boolean CTDConnected = false;

    private final Map<Integer, QbusBistabiel> bistabiel = new HashMap<>();
    private final Map<Integer, QbusScene> scene = new HashMap<>();
    private final Map<Integer, QbusDimmer> dimmer = new HashMap<>();
    private final Map<Integer, QbusRol> rol = new HashMap<>();
    private final Map<Integer, QbusThermostat> thermostat = new HashMap<>();
    private final Map<Integer, QbusCO2> co2 = new HashMap<>();

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
        gsonIn = gsonBuilder.create();
    }

    /**
     * Start communication with Qbus Server, run through initialization and start thread listening
     * to all messages coming from Qbus.
     *
     * @param addr : IP-address of Qbus Server
     * @param port : Communication port of Qbus server
     *
     */

    public synchronized void startCommunication() {
        QbusBridgeHandler handler = bridgeCallBack;

        try {

            for (int i = 1; qEventsRunning && (i <= 5); i++) {
                Thread.sleep(1000);

            }
            if (qEventsRunning) {
                logger.error("Starting from thread {}, but previous connection still active after 5000ms",
                        Thread.currentThread().getId());
                throw new IOException();
            }

            if (handler == null) {
                throw new IOException();
            }

            InetAddress addr = handler.getAddr();
            int port = handler.getPort();

            Socket socket = new Socket(addr, port);
            qSocket = socket;
            qOut = new PrintWriter(socket.getOutputStream(), true);
            qIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            logger.info("Connected via local port {} from thread {}", socket.getLocalPort(),
                    Thread.currentThread().getId());
            CTDConnected = false;

            // Connect to Qbus server
            Connect();

            // If Qbus Client is connected then initialize, else put Bridge offline
            if (CTDConnected == true) {
                initialize();
                (new Thread(qEvents)).start();
            } else {
                handler.bridgeOffline("No communication with Qbus client");
            }
        } catch (IOException | InterruptedException e) {
            logger.warn("Error initializing communication from thread {}", Thread.currentThread().getId());
            // No connection with Qbus server, put Bridge offline
            stopCommunication();
            if (handler != null) {
                handler.bridgeOffline("No communication with Qbus server");
            }

        }
    }

    /**
     * Cleanup socket when the communication with Qbus Server is closed.
     *
     * @throws IOException
     *
     */
    public synchronized void stopCommunication() {
        listenerStopped = true;

        Socket socket = qSocket;

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignore) {
                // ignore IO Error when trying to close the socket if the intention is to close it anyway
            }
        }

        qSocket = null;

        CTDConnected = false;
        logger.debug("Communication stopped from thread {}", Thread.currentThread().getId());
    }

    /**
     * Close and restart communication with Qbus Server.
     */
    public synchronized void restartCommunication() {
        stopCommunication();

        logger.debug("Qbus: restart communication from thread {}", Thread.currentThread().getId());

        startCommunication();
    }

    /**
     * Method to check if communication with Qbus Server is active
     *
     * @return True if active
     */
    public boolean communicationActive() {

        return (qSocket != null);
    }

    /**
     * Method to check if communication with Qbus Client is active
     *
     * @return True if active
     */

    public boolean clientConnected() {
        return (CTDConnected);
    }

    /**
     * Runnable that handles incomming communication from Qbus server.
     * <p>
     * The thread listens to the TCP socket opened at instantiation of the {@link QbusCommunication} class
     * and interprets all incomming json messages. It triggers state updates for active channels linked to the
     * Qbus outputs. It is started after initialization of the communication.
     *
     */
    private Runnable qEvents = () -> {
        String qMessage;

        logger.info("Listening for events on thread {}", Thread.currentThread().getId());
        listenerStopped = false;
        qEventsRunning = true;

        BufferedReader reader = qIn;

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

                QbusBridgeHandler handler = bridgeCallBack;

                if (handler != null) {
                    CTDConnected = false;
                    handler.bridgeOffline("No communication with Qbus server");
                }

                return;
            }
        }

        qEventsRunning = false;

        logger.warn("Event listener thread stopped on thread {}", Thread.currentThread().getId());

    };

    /**
     * Called by other methods to send json data to Qbus.
     *
     * @param qMessage
     */
    synchronized void sendMessage(Object qMessage) {
        PrintWriter writer = qOut;
        String json = gsonOut.toJson(qMessage);
        logger.debug("Send json from thread {}", Thread.currentThread().getId());

        if (writer != null) {
            writer.println(json);
            // Delay after sending data to improve scene execution
            try {
                TimeUnit.MILLISECONDS.sleep(250);
            } catch (InterruptedException e) {
                // No reaction on error is required
            }

        }
        if ((writer == null) || (writer.checkError())) {
            logger.warn("Error sending message, trying to restart communication");
            restartCommunication();
            // retry sending after restart
            logger.debug("Resend json from thread {}", Thread.currentThread().getId());
            writer = qOut;
            if (writer != null) {
                writer.println(json);
            }
            if ((writer == null) || (writer.checkError())) {
                logger.warn("Error resending message");

            }
        }
    }

    /**
     * Called by other methods to Qbus server and read response
     */
    private void sendAndReadMessage(String command) throws IOException, InterruptedException {
        QbusMessageCmd qCmd = new QbusMessageCmd(CTD, command);

        sendMessage(qCmd);

        BufferedReader reader = qIn;
        if (reader == null) {
            throw new IOException("Cannot read from socket, reader not connected.");
        }
        readMessage(reader.readLine());
    }

    /**
     * Method that interprets all feedback from Qbus Server application and calls appropriate handling methods.
     *
     * @param qMessage message read from Qbus.
     */
    // @SuppressWarnings("null")
    private void readMessage(String qMessage) {
        logger.debug("Received json on thread {}", Thread.currentThread().getId());
        String confsn = "";
        String cmd = "";
        String CTD = "";
        QbusMessageBase qMessageGson;

        qMessageGson = gsonIn.fromJson(qMessage, QbusMessageBase.class);
        if (qMessageGson != null) {
            CTD = qMessageGson.getSn();
            cmd = qMessageGson.getCmd();
        }

        if (bridgeCallBack != null) {
            confsn = bridgeCallBack.getSn();
        }

        try {
            if (Integer.parseInt(confsn) == Integer.parseInt(CTD) && qMessageGson != null) {
                // Get the compatible outputs from the Qbus server
                if ("returnBistabiel".equals(cmd)) {
                    cmdListBistabiel(((QbusMessageListMap) qMessageGson).getOutputs());
                } else if ("returnDimmer".equals(cmd)) {
                    cmdListDimmers(((QbusMessageListMap) qMessageGson).getOutputs());
                } else if (("returnThermostat").equals(cmd)) {
                    cmdListThermostat(((QbusMessageListMap) qMessageGson).getOutputs());
                } else if (("returnScene").equals(cmd)) {
                    cmdlistscenes(((QbusMessageListMap) qMessageGson).getOutputs());
                } else if (("returnCo2").equals(cmd)) {
                    cmdlistco2(((QbusMessageListMap) qMessageGson).getOutputs());
                } else if (("returnRol02p").equals(cmd)) {
                    cmdlistrol(((QbusMessageListMap) qMessageGson).getOutputs());
                } else if (("returnSlat").equals(cmd)) {
                    cmdlistrolslats(((QbusMessageListMap) qMessageGson).getOutputs());
                }

                // Incoming commands from Qbus Client to openHAB (event)
                else if ("updateBistabiel".equals(cmd)) {
                    updateBistabiel(((QbusMessageListMap) qMessageGson).getOutputs());
                } else if ("updateDimmer".equals(cmd)) {
                    eventListDimmers(((QbusMessageListMap) qMessageGson).getOutputs());
                } else if ("updateThermostat".equals(cmd)) {
                    eventListThermostat(((QbusMessageListMap) qMessageGson).getOutputs());
                } else if ("updateScene".equals(cmd)) {
                    eventListScenes(((QbusMessageListMap) qMessageGson).getOutputs());
                } else if ("updateCo2".equals(cmd)) {
                    eventListCO2(((QbusMessageListMap) qMessageGson).getOutputs());
                } else if ("updateRol02p".equals(cmd)) {
                    eventListRol(((QbusMessageListMap) qMessageGson).getOutputs());
                } else if ("updateRol02pSlat".equals(cmd)) {
                    eventListRolslats(((QbusMessageListMap) qMessageGson).getOutputs());
                }

                // Incomming commands from Qbus server to verify the client connection
                else if ("disconnect".equals(cmd)) {
                    eventDisconnect();
                } else if ("notConnected".equals(cmd)) {
                    noConnection();
                } else if ("connected".equals(cmd)) {
                    connection();
                }
            }
        } catch (JsonParseException e) {
            logger.warn("Not acted on unsupported json {}", qMessage);
        }
    }

    /**
     * After setting up the communication with the Qbus Server, send all initialization messages.
     * <p>
     * First send connect to connect with the Qbus Server application
     * Get request for Bistabiel/Timers/Intervals/Mono outputs
     * Get request for the Scenes
     * Get request for Dimmers 1T and 2T
     * Get request for Shutters
     * Get request for Thermostats
     * Get request for CO2
     *
     *
     * @throws IOException
     * @throws InterruptedException
     */

    private void initialize() throws IOException, InterruptedException {
        if (bridgeCallBack != null) {
            if (CTDConnected) {
                logger.info("Requesting Bistabiel outputs from client");
                sendAndReadMessage("getBistabiel");
                logger.info("Requesting Scenes from client");
                sendAndReadMessage("getScene");
                logger.info("Requesting Dimmers from client");
                sendAndReadMessage("getDimmer");
                logger.info("Requesting Shutters from client");
                sendAndReadMessage("getRol02p");
                logger.info("Requesting Shutters whith slat control from client");
                sendAndReadMessage("getRol02pSlat");
                logger.info("Requesting Thermostats from client");
                sendAndReadMessage("getThermostat");
                logger.info("Requesting CO2 outputs from client");
                sendAndReadMessage("getCo2");
            } else {
                logger.warn("No CTD client connected to server with sn {}", CTD);
                CTDConnected = false;

                QbusBridgeHandler handler = bridgeCallBack;

                if (handler != null) {
                    handler.bridgeOffline("No communication with Qbus client");
                }

                return;
            }
        } else {
            logger.error("Initialization error");
        }
    }

    /**
     * Initial connection to Qbus Server to open a communication channel
     *
     * @throws IOException
     *
     */
    private void Connect() throws InterruptedException, IOException {
        if (bridgeCallBack != null) {
            CTD = bridgeCallBack.getSn();
        }
        logger.info("Connecting to server");
        QbusMessageCmd QCmd = new QbusMessageCmd(CTD, "openHAB");

        sendMessage(QCmd);
        BufferedReader reader = qIn;

        if (reader == null) {
            throw new IOException("Cannot read from socket, reader not connected.");
        }
        readMessage(reader.readLine());
    }

    /**
     * Get all the Bistabiel/Timer/Mono/Interval outputs from the Qbus client
     *
     * @param data
     */
    @SuppressWarnings("null")
    private void cmdListBistabiel(@Nullable List<Map<String, String>> outputs) {
        logger.info("Bistabiel/Timers/Monos/Intervals received from Qbus server");

        if (outputs != null) {
            for (Map<String, String> bistabiel : outputs) {
                String idStr = bistabiel.get("id");
                String stateStr = bistabiel.get("state");

                if (idStr != null && stateStr != null) {
                    int id = Integer.parseInt(idStr);
                    Integer state = Integer.parseInt(stateStr);
                    if (!this.bistabiel.containsKey(id)) {
                        QbusBistabiel qBistabiel = new QbusBistabiel(idStr);
                        qBistabiel.setState(state);
                        qBistabiel.setQComm(this);
                        this.bistabiel.put(id, qBistabiel);
                        this.bistabiel.get(id).setState(state);
                    } else {
                        this.bistabiel.get(id).setState(state);
                    }
                } else {
                    logger.error("Error in json for BistabBistabiel/Timers/Monos/Intervals");
                }
            }
        }
    }

    /**
     * Get all the scenes from the Qbus server
     *
     * @param outputs
     */
    private void cmdlistscenes(@Nullable List<Map<String, String>> outputs) {
        logger.info("Scenes received from Qbus server");

        if (outputs != null) {
            for (Map<String, String> scene : outputs) {
                String idStr = scene.get("id");
                if (idStr != null) {
                    int id = Integer.parseInt(idStr);
                    QbusScene Scene = new QbusScene(idStr);
                    Scene.setQComm(this);
                    this.scene.put(id, Scene);
                } else {
                    logger.error("Error in json for Scenes");
                }
            }
        }
    }

    /**
     * Get all the Dimmer outputs from the Qbus client
     *
     * @param outputs
     */
    @SuppressWarnings("null")
    private void cmdListDimmers(@Nullable List<Map<String, String>> outputs) {
        logger.info("Dimmers received from the Qbus server");

        if (outputs != null) {
            for (Map<String, String> dimmer : outputs) {
                String idStr = dimmer.get("id");
                String stateStr = dimmer.get("state");
                if (idStr != null && stateStr != null) {
                    int id = Integer.parseInt(idStr);
                    Integer state = Integer.parseInt(stateStr);
                    if (!this.dimmer.containsKey(id)) {
                        QbusDimmer qDimmer = new QbusDimmer(idStr);
                        qDimmer.updateState(state);
                        qDimmer.setQComm(this);
                        this.dimmer.put(id, qDimmer);
                    } else {
                        this.dimmer.get(id).updateState(state);
                    }
                } else {
                    logger.error("Error in json for Dimmer");
                }
            }
        }
    }

    /**
     * Get all the screens with slat control outputs from the Qbus client
     *
     * @param data
     */
    @SuppressWarnings("null")
    private void cmdlistrol(@Nullable List<Map<String, String>> outputs) {

        logger.info("ROL02P received from Qbus server");
        if (outputs != null) {
            for (Map<String, String> rol : outputs) {
                String idStr = rol.get("id");
                String stateStr = rol.get("state");
                if (idStr != null && stateStr != null) {
                    int id = Integer.parseInt(idStr);
                    Integer rolpos = Integer.valueOf(stateStr);
                    if (!this.rol.containsKey(id)) {
                        QbusRol Rol = new QbusRol(idStr);
                        Rol.setQComm(this);
                        this.rol.put(id, Rol);
                        this.rol.get(id).setState(rolpos);
                    } else {
                        this.rol.get(id).setState(rolpos);
                    }
                } else {
                    logger.error("Error in json for ROL02P");
                }
            }
        }
    }

    /**
     * Get all the screen outputs from the Qbus client
     *
     * @param data
     */
    @SuppressWarnings("null")
    private void cmdlistrolslats(@Nullable List<Map<String, String>> outputs) {
        logger.info("ROL02PSLATS received from Qbus server");

        if (outputs != null) {
            for (Map<String, String> rol : outputs) {

                String idStr = rol.get("id");
                String rolPos = rol.get("rolPos");
                String slatPos = rol.get("slatPos");
                if (idStr != null && rolPos != null && slatPos != null) {
                    int id = Integer.parseInt(idStr);
                    Integer rolpos = Integer.valueOf(rolPos);
                    Integer rolposslats = Integer.valueOf(slatPos);
                    rolpos = Integer.valueOf(rolpos);
                    rolposslats = Integer.valueOf(rolposslats);
                    if (!this.rol.containsKey(id)) {
                        QbusRol Rol = new QbusRol(idStr);
                        Rol.setQComm(this);
                        this.rol.put(id, Rol);
                        this.rol.get(id).setState(rolpos);
                        this.rol.get(id).setSlats(rolposslats);
                    } else {
                        this.rol.get(id).setState(rolpos);
                        this.rol.get(id).setSlats(rolposslats);
                    }
                } else {
                    logger.error("Error in json for ROL02P_Slats");
                }
            }
        }
    }

    /**
     * Get all the CO2 outputs from the Qbus client
     *
     * @param data
     */
    @SuppressWarnings("null")
    private void cmdlistco2(@Nullable List<Map<String, String>> outputs) {
        logger.info("CO2 received from Qbus server");

        if (outputs != null) {
            for (Map<String, String> co2 : outputs) {
                String idStr = co2.get("id");
                String stateStr = co2.get("state");
                if (idStr != null && stateStr != null) {

                    int id = Integer.parseInt(idStr);
                    int state = Integer.parseInt(stateStr);

                    if (!this.co2.containsKey(id)) {
                        QbusCO2 CO2 = new QbusCO2(idStr);
                        this.co2.put(id, CO2);
                        this.co2.get(id).setState(state);
                    } else {
                        this.co2.get(id).setState(state);
                    }

                } else {
                    logger.error("Error in json for CO2");
                }

            }
        }
    }

    /**
     * Get all the Thermostat outputs from the Qbus client
     *
     * @param data
     */
    @SuppressWarnings("null")
    private void cmdListThermostat(@Nullable List<Map<String, String>> outputs) {
        logger.info("Thermostats received from the Qbus server");

        if (outputs != null) {
            for (Map<String, String> thermostat : outputs) {
                String idStr = thermostat.get("id");
                String measuredStr = thermostat.get("measured");
                String setpointStr = thermostat.get("SetPoint");
                String modeStr = thermostat.get("Mode");
                if (idStr != null && measuredStr != null && setpointStr != null && modeStr != null) {
                    int id = Integer.parseInt(idStr);
                    Double measured = Double.valueOf(measuredStr);
                    Double setpoint = Double.valueOf(setpointStr);
                    Integer mode = Integer.valueOf(modeStr);

                    if (!this.thermostat.containsKey(id)) {
                        QbusThermostat qThermostat = new QbusThermostat(idStr);
                        qThermostat.updateState(measured, setpoint, mode);
                        qThermostat.setQComm(this);
                        this.thermostat.put(id, qThermostat);
                    } else {
                        this.thermostat.get(id).updateState(measured, setpoint, mode);
                    }
                } else {
                    logger.error("Error in json for Thermostats");
                }
            }
        }
    }

    /**
     * Event on incoming Bistabiel/Timer/Mono/Interval updates
     *
     * @param data
     */
    @SuppressWarnings("null")
    private void updateBistabiel(List<Map<String, String>> output) {
        for (Map<String, String> bistabiel : output) {
            String idStr = bistabiel.get("id");
            String stateStr = bistabiel.get("state");
            if (idStr != null && stateStr != null) {
                int id = Integer.valueOf(idStr);
                int value1 = Integer.valueOf(stateStr);
                if (!this.bistabiel.containsKey(id)) {
                    logger.warn("Bistabiel in controller not known {}", id);
                    return;
                }
                logger.info("Event execute bistabiel {} with state {}", id, value1);
                this.bistabiel.get(id).setState(value1);
            }
        }
    }

    /**
     * Event on incoming Scene updates
     *
     * @param data
     */
    @SuppressWarnings("null")
    private void eventListScenes(List<Map<String, String>> data) {
        for (Map<String, String> scene : data) {
            String idStr = scene.get("id");
            String value1Str = scene.get("state");
            if (idStr != null && value1Str != null) {
                int id = Integer.valueOf(idStr);
                int value1 = Integer.valueOf(value1Str);
                if (!this.scene.containsKey(id)) {
                    logger.warn("Scene in controller not known {}", id);
                    return;
                }

                logger.info("Event execute scene {} with state {}", id, value1);
                this.scene.get(id).setState(value1);
            }
        }
    }

    /**
     * Event on incoming Dimmer updates
     *
     * @param data
     */
    @SuppressWarnings("null")
    private void eventListDimmers(List<Map<String, String>> data) {
        for (Map<String, String> dimmer : data) {
            String idStr = dimmer.get("id");
            String value1Str = dimmer.get("state");
            if (idStr != null && value1Str != null) {
                int id = Integer.valueOf(idStr);
                int value1 = Integer.valueOf(value1Str);

                if (!this.dimmer.containsKey(id)) {
                    logger.warn("Dimmer in controller not known {}", id);
                    return;
                }

                logger.info("Event execute dimmer {} with state {}", id, value1);
                this.dimmer.get(id).setState(value1);
            }
        }
    }

    /**
     * Event on incoming screen updates
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

                if (!this.rol.containsKey(id)) {
                    logger.warn("Rol02p in controller not known {}", id);
                    return;
                }
                logger.info("Event execute Rol02P {} with pos {}", id, value1);
                this.rol.get(id).setState(value1);

            }
        }
    }

    /**
     * Event on incoming screen with slats updates
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
                if (!this.rol.containsKey(id)) {
                    logger.warn("Rol02p in controller not known {}", id);
                    return;
                }

                logger.info("Event execute ROL02P_Slats {} with pos {} and slats {}", id, value1, value2);
                this.rol.get(id).setState(value1);
                this.rol.get(id).setSlats(value2);
            }
        }
    }

    /**
     * Event on incoming thermostat updates
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

                if (!this.thermostat.containsKey(id)) {
                    logger.warn("Thermostat in controller not known {}", id);
                    return;
                }

                logger.info("Event execute thermostat {} with measured {}, setpoint {}, mode {}", id, measured,
                        setpoint, mode);
                this.thermostat.get(id).updateState(measured, setpoint, mode);
            }
        }
    }

    /**
     * Event on incoming CO2 updates
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
                    logger.warn("Co2 in controller not known {}", id);
                    return;
                }
                logger.info("Event execute co2 {} with state {}", id, value1);
                this.co2.get(id).setState(value1);
            }
        }
    }

    private void eventDisconnect() {
        logger.info("Disconnect received from client. Putting Bridge offline");
        CTDConnected = false;
        QbusBridgeHandler handler = bridgeCallBack;
        if (handler != null) {
            handler.bridgeOffline("No communication with Qbus client");
        }
    }

    private void noConnection() {
        logger.info("No CTD connected to Qbus server");
        CTDConnected = false;
        QbusBridgeHandler handler = bridgeCallBack;
        if (handler != null) {
            handler.bridgeOffline("No communication with Qbus client");
        }
    }

    private void connection() {
        logger.info("CTD connected to Qbus server");
        CTDConnected = true;
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
     * Return all Scenes in the Qbus Controller
     *
     * @return
     */
    public Map<Integer, QbusScene> getScenes() {
        return this.scene;
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
     * Return all screen outûts in the Qbus Controller.
     *
     * @return
     */
    public Map<Integer, QbusRol> getRol() {
        return this.rol;
    }

    /**
     * Return all Thermostats in the Qbus Controller.
     *
     * @return
     */
    public Map<Integer, QbusThermostat> getThermostats() {
        return this.thermostat;
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
     * @param bridgeCallBack the bridgeCallBack to set
     */
    public void setBridgeCallBack(QbusBridgeHandler bridgeCallBack) {
        this.bridgeCallBack = bridgeCallBack;
    }
}
