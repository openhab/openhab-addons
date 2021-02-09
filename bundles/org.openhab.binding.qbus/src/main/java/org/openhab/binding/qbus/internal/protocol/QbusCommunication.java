/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
    @Nullable
    private String CTD;

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
        CTDConnected = false;
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

            InetAddress addr = InetAddress.getByName(handler.getAddress());
            Integer port = handler.getPort();

            if (port != null) {
                Socket socket = new Socket(addr, port);
                qSocket = socket;
                qOut = new PrintWriter(socket.getOutputStream(), true);
                qIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                logger.info("Connected via local port {} from thread {}", socket.getLocalPort(),
                        Thread.currentThread().getId());
            } else {
                return;
            }

            setSN();
            getSN();

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

        return qSocket != null;
    }

    /**
     * Method to check if communication with Qbus Client is active
     *
     * @return True if active
     */

    public boolean clientConnected() {
        return CTDConnected;
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
        @Nullable
        String snr = getSN();
        if (snr != null) {

            QbusMessageCmd qCmd = new QbusMessageCmd(snr, command);

            sendMessage(qCmd);

            BufferedReader reader = qIn;
            if (reader == null) {
                throw new IOException("Cannot read from socket, reader not connected.");
            }
            readMessage(reader.readLine());
        } else {
            QbusBridgeHandler handler = bridgeCallBack;
            if (handler != null) {
                handler.bridgeOffline("No serial nr defined");
            }
        }
    }

    /**
     * Method that interprets all feedback from Qbus Server application and calls appropriate handling methods.
     *
     * @param qMessage message read from Qbus.
     */
    private void readMessage(String qMessage) {
        String cmd = "";
        String CTD = "";
        String sn = null;
        QbusMessageBase qMessageGson;

        qMessageGson = gsonIn.fromJson(qMessage, QbusMessageBase.class);
        if (qMessageGson != null) {
            CTD = qMessageGson.getSn();
            cmd = qMessageGson.getCmd();
        }

        if (bridgeCallBack != null) {
            sn = bridgeCallBack.getSn();
        }

        if (sn != null && CTD != null) {
            try {

                if (Integer.parseInt(sn) == Integer.parseInt(CTD) && qMessageGson != null) {
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
                        updateDimmers(((QbusMessageListMap) qMessageGson).getOutputs());
                    } else if ("updateThermostat".equals(cmd)) {
                        updateThermostat(((QbusMessageListMap) qMessageGson).getOutputs());
                    } else if ("updateCo2".equals(cmd)) {
                        updateCO2(((QbusMessageListMap) qMessageGson).getOutputs());
                    } else if ("updateRol02p".equals(cmd)) {
                        updateRol(((QbusMessageListMap) qMessageGson).getOutputs());
                    } else if ("updateRol02pSlat".equals(cmd)) {
                        updateRolslats(((QbusMessageListMap) qMessageGson).getOutputs());
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
                sendAndReadMessage("getBistabiel");
                sendAndReadMessage("getScene");
                sendAndReadMessage("getDimmer");
                sendAndReadMessage("getRol02p");
                sendAndReadMessage("getRol02pSlat");
                sendAndReadMessage("getThermostat");
                sendAndReadMessage("getCo2");
            } else {
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

    public @Nullable String getSN() {
        return this.CTD;
    }

    public void setSN() {
        QbusBridgeHandler QBridgeHandler = bridgeCallBack;
        if (QBridgeHandler != null) {
            this.CTD = QBridgeHandler.getSn();
        }
    }

    /**
     * Initial connection to Qbus Server to open a communication channel
     *
     * @throws IOException
     *
     */
    private void Connect() throws InterruptedException, IOException {
        @Nullable
        String snr = getSN();

        if (snr != null) {
            QbusMessageCmd QCmd = new QbusMessageCmd(snr, "openHAB");

            sendMessage(QCmd);
            BufferedReader reader = qIn;

            if (reader == null) {
                throw new IOException("Cannot read from socket, reader not connected.");
            }
            readMessage(reader.readLine());
        } else {
            QbusBridgeHandler handler = bridgeCallBack;
            if (handler != null) {
                handler.bridgeOffline("No serial nr defined");
            }
        }

        return;
    }

    /**
     * Get all the Bistabiel/Timer/Mono/Interval outputs from the Qbus client
     *
     * @param data
     */
    private void cmdListBistabiel(@Nullable List<Map<String, String>> outputs) {
        if (outputs != null) {
            for (Map<String, String> bistabiel : outputs) {
                String idStr = bistabiel.get("id");
                String stateStr = bistabiel.get("state");
                if (idStr != null && stateStr != null) {
                    int id = Integer.parseInt(idStr);
                    Integer state = Integer.parseInt(stateStr);
                    QbusBistabiel qBistabiel = new QbusBistabiel(idStr);
                    if (!this.bistabiel.containsKey(id)) {
                        qBistabiel.setState(state);
                        qBistabiel.setQComm(this);
                        this.bistabiel.put(id, qBistabiel);
                        qBistabiel.setState(state);
                    } else {
                        qBistabiel.setState(state);
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
    private void cmdListDimmers(@Nullable List<Map<String, String>> outputs) {
        if (outputs != null) {
            for (Map<String, String> dimmer : outputs) {
                String idStr = dimmer.get("id");
                String stateStr = dimmer.get("state");
                if (idStr != null && stateStr != null) {
                    int id = Integer.parseInt(idStr);
                    Integer state = Integer.parseInt(stateStr);
                    QbusDimmer qDimmer = new QbusDimmer(idStr);
                    if (!this.dimmer.containsKey(id)) {
                        qDimmer.setQComm(this);
                        this.dimmer.put(id, qDimmer);
                        qDimmer.updateState(state);
                    } else {
                        qDimmer.updateState(state);
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
    private void cmdlistrol(@Nullable List<Map<String, String>> outputs) {
        if (outputs != null) {
            for (Map<String, String> rol : outputs) {
                String idStr = rol.get("id");
                String stateStr = rol.get("state");
                if (idStr != null && stateStr != null) {
                    int id = Integer.parseInt(idStr);
                    Integer rolpos = Integer.valueOf(stateStr);
                    QbusRol Qrol = new QbusRol(idStr);
                    if (!this.rol.containsKey(id)) {
                        Qrol.setQComm(this);
                        this.rol.put(id, Qrol);
                        Qrol.setState(rolpos);
                    } else {
                        Qrol.setState(rolpos);
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
    private void cmdlistrolslats(@Nullable List<Map<String, String>> outputs) {
        logger.debug("Qbus: ROL02PSLATS received from Qbus server");
        if (outputs != null) {
            for (Map<String, String> rol : outputs) {
                String idStr = rol.get("id");
                String rolPos = rol.get("rolPos");
                String slatPos = rol.get("slatPos");
                if (idStr != null && rolPos != null && slatPos != null) {
                    int id = Integer.parseInt(idStr);
                    Integer rolpos = Integer.parseInt(rolPos);
                    Integer rolposslats = Integer.parseInt(slatPos);
                    QbusRol Qrol = new QbusRol(idStr);
                    if (!this.rol.containsKey(id)) {
                        Qrol.setQComm(this);
                        this.rol.put(id, Qrol);
                        Qrol.setState(rolpos);
                        Qrol.setSlats(rolposslats);
                    } else {
                        Qrol.setState(rolpos);
                        Qrol.setSlats(rolposslats);
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
    private void cmdlistco2(@Nullable List<Map<String, String>> outputs) {
        if (outputs != null) {
            for (Map<String, String> co2 : outputs) {
                String idStr = co2.get("id");
                String stateStr = co2.get("state");
                if (idStr != null && stateStr != null) {
                    int id = Integer.parseInt(idStr);
                    int state = Integer.parseInt(stateStr);
                    QbusCO2 CO2 = new QbusCO2();
                    if (!this.co2.containsKey(id)) {
                        this.co2.put(id, CO2);
                        CO2.setState(state);
                    } else {
                        CO2.setState(state);
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
    private void cmdListThermostat(@Nullable List<Map<String, String>> outputs) {
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
                    QbusThermostat qThermostat = new QbusThermostat(idStr);
                    if (!this.thermostat.containsKey(id)) {
                        qThermostat.updateState(measured, setpoint, mode);
                        qThermostat.setQComm(this);
                        this.thermostat.put(id, qThermostat);
                    } else {
                        qThermostat.updateState(measured, setpoint, mode);
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
    private void updateBistabiel(List<Map<String, String>> output) {
        for (Map<String, String> bistabiel : output) {
            String idStr = bistabiel.get("id");
            String stateStr = bistabiel.get("state");
            if (idStr != null && stateStr != null) {
                int id = Integer.parseInt(idStr);
                int value1 = Integer.parseInt(stateStr);
                QbusBistabiel Bistabiel = this.bistabiel.get(id);
                if (Bistabiel != null) {
                    if (!this.bistabiel.containsKey(id)) {
                        logger.warn("Bistabiel in controller not known {}", id);
                        return;
                    }
                    logger.info("Event execute bistabiel {} with state {}", id, value1);
                    Bistabiel.setState(value1);
                }
            }
        }
    }

    /**
     * Event on incoming Dimmer updates
     *
     * @param data
     */
    private void updateDimmers(List<Map<String, String>> data) {
        for (Map<String, String> dimmer : data) {
            String idStr = dimmer.get("id");
            String stateStr = dimmer.get("state");
            if (idStr != null && stateStr != null) {
                int id = Integer.valueOf(idStr);
                int value = Integer.valueOf(stateStr);
                QbusDimmer Qdimmer = this.dimmer.get(id);
                if (!this.dimmer.containsKey(id)) {
                    logger.warn("Dimmer in controller not known {}", id);
                    return;
                }
                if (Qdimmer != null) {
                    logger.info("Event execute dimmer {} with state {}", id, value);
                    Qdimmer.setState(value);
                }
            }
        }
    }

    /**
     * Event on incoming screen updates
     *
     * @param data
     */
    private void updateRol(List<Map<String, String>> data) {
        for (Map<String, String> rol : data) {
            String idStr = rol.get("id");
            String posStr = rol.get("value1Str");
            if (idStr != null && posStr != null) {
                int id = Integer.valueOf(idStr);
                int pos = Integer.valueOf(posStr);
                QbusRol Qrol = this.rol.get(id);
                if (!this.rol.containsKey(id)) {
                    logger.warn("Rol02p in controller not known {}", id);
                    return;
                }
                if (Qrol != null) {
                    logger.info("Event execute Rol02P {} with pos {}", id, pos);
                    Qrol.setState(pos);
                }

            }
        }
    }

    /**
     * Event on incoming screen with slats updates
     *
     * @param data
     */
    private void updateRolslats(List<Map<String, String>> data) {
        for (Map<String, String> rol : data) {
            String idStr = rol.get("id");
            String posStr = rol.get("pos");
            String slatsStr = rol.get("slats");
            if (idStr != null && posStr != null && slatsStr != null) {
                int id = Integer.valueOf(idStr);
                int pos = Integer.valueOf(posStr);
                int slats = Integer.valueOf(slatsStr);
                QbusRol Qrol = this.rol.get(id);
                if (!this.rol.containsKey(id)) {
                    logger.warn("Rol02p in controller not known {}", id);
                    return;
                }
                if (Qrol != null) {
                    logger.info("Event execute ROL02P_Slats {} with pos {} and slats {}", id, pos, slats);
                    Qrol.setState(pos);
                    Qrol.setSlats(slats);
                }
            }
        }
    }

    /**
     * Event on incoming thermostat updates
     *
     * @param data
     */
    private void updateThermostat(List<Map<String, String>> data) {
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
                QbusThermostat Qthermostat = this.thermostat.get(id);
                if (!this.thermostat.containsKey(id)) {
                    logger.warn("Thermostat in controller not known {}", id);
                    return;
                }
                if (Qthermostat != null) {
                    logger.info("Event execute thermostat {} with measured {}, setpoint {}, mode {}", id, measured,
                            setpoint, mode);
                    Qthermostat.updateState(measured, setpoint, mode);
                }
            }
        }
    }

    /**
     * Event on incoming CO2 updates
     *
     * @param data
     */
    private void updateCO2(List<Map<String, String>> data) {
        for (Map<String, String> co2 : data) {
            String idStr = co2.get("id");
            String value1Str = co2.get("value1Str");
            if (idStr != null && value1Str != null) {
                int id = Integer.valueOf(idStr);
                int value1 = Integer.valueOf(value1Str);
                QbusCO2 Qco2 = this.co2.get(id);
                if (!this.co2.containsKey(id)) {
                    logger.warn("Co2 in controller not known {}", id);
                    return;
                }
                if (Qco2 != null) {
                    logger.info("Event execute co2 {} with state {}", id, value1);
                    Qco2.setState(value1);
                }
            }
        }
    }

    /**
     * Put Bridge offline when QbusClient disconnects
     */
    private void eventDisconnect() {
        CTDConnected = false;
        QbusBridgeHandler handler = bridgeCallBack;
        if (handler != null) {
            handler.bridgeOffline("No communication with Qbus client");
        }
    }

    /**
     * Put Bridge offline when there is no connection from the QbusClient
     */
    private void noConnection() {
        CTDConnected = false;
        QbusBridgeHandler handler = bridgeCallBack;
        if (handler != null) {
            handler.bridgeOffline("No communication with Qbus client");
        }
    }

    /**
     * Set connection state true if there is a connection from QbusClient
     */
    private void connection() {
        CTDConnected = true;
    }

    /**
     * Return all Bistabiel/Timers/Mono/Intervals in the Qbus Controller.
     *
     * @return
     */
    public @Nullable Map<Integer, QbusBistabiel> getBistabiel() {
        return this.bistabiel;
    }

    /**
     * Return all Scenes in the Qbus Controller
     *
     * @return
     */
    public @Nullable Map<Integer, QbusScene> getScene() {
        return this.scene;
    }

    /**
     * Return all Dimmers in the Qbus Controller.
     *
     * @return
     */
    public @Nullable Map<Integer, QbusDimmer> getDimmer() {
        return this.dimmer;
    }

    /**
     * Return all screen outûts in the Qbus Controller.
     *
     * @return
     */
    public @Nullable Map<Integer, QbusRol> getRol() {
        return this.rol;
    }

    /**
     * Return all Thermostats in the Qbus Controller.
     *
     * @return
     */
    public @Nullable Map<Integer, QbusThermostat> getThermostat() {
        return this.thermostat;
    }

    /**
     * Return all CO2 in the Qbus Controller.
     *
     * @return
     */
    public @Nullable Map<Integer, QbusCO2> getCo2() {
        return this.co2;
    }

    /**
     * @param bridgeCallBack the bridgeCallBack to set
     */
    public void setBridgeCallBack(QbusBridgeHandler bridgeCallBack) {
        this.bridgeCallBack = bridgeCallBack;
    }
}
