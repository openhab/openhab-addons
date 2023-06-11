/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.qbus.internal.QbusBridgeHandler;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

/**
 * The {@link QbusCommunication} class is able to do the following tasks with Qbus
 * CTD controllers:
 * <ul>
 * <li>Start and stop TCP socket connection with Qbus Server.
 * <li>Read all the outputs and their status from the Qbus Controller.
 * <li>Execute Qbus commands.
 * <li>Listen to events from Qbus.
 * </ul>
 *
 * A class instance is instantiated from the {@link QbusBridgeHandler} class initialization.
 *
 * @author Koen Schockaert - Initial Contribution
 */

@NonNullByDefault
public final class QbusCommunication extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(QbusCommunication.class);

    private @Nullable Socket qSocket;
    private @Nullable PrintWriter qOut;
    private @Nullable BufferedReader qIn;

    private boolean listenerStopped;
    private boolean qbusListenerRunning;

    private Gson gsonOut = new Gson();
    private Gson gsonIn;

    private @Nullable String ctd;
    private boolean ctdConnected;

    private List<Map<String, String>> outputs = new ArrayList<>();
    private final Map<Integer, QbusBistabiel> bistabiel = new HashMap<>();
    private final Map<Integer, QbusScene> scene = new HashMap<>();
    private final Map<Integer, QbusDimmer> dimmer = new HashMap<>();
    private final Map<Integer, QbusRol> rol = new HashMap<>();
    private final Map<Integer, QbusThermostat> thermostat = new HashMap<>();
    private final Map<Integer, QbusCO2> co2 = new HashMap<>();

    private final ExecutorService threadExecutor = Executors
            .newSingleThreadExecutor(new NamedThreadFactory(getThing().getUID().getAsString(), true));

    private @Nullable QbusBridgeHandler bridgeCallBack;

    public QbusCommunication(Thing thing) {
        super(thing);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(QbusMessageBase.class, new QbusMessageDeserializer());
        gsonIn = gsonBuilder.create();
    }

    /**
     * Starts main communication thread.
     * <ul>
     * <li>Connect to Qbus server
     * <li>Requests outputs
     * <li>Start listener
     * </ul>
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public synchronized void startCommunication() throws IOException, InterruptedException {
        QbusBridgeHandler handler = bridgeCallBack;
        ctdConnected = false;

        if (qbusListenerRunning) {
            throw new IOException("Previous listening thread is still active.");
        }

        if (handler == null) {
            throw new IOException("No Bridge handler initialised.");
        }

        InetAddress addr = InetAddress.getByName(handler.getAddress());
        Integer port = handler.getPort();

        if (port == null) {
            handler.bridgeOffline(ThingStatusDetail.CONFIGURATION_ERROR, "Please set a correct port.");
            return;
        }

        if (addr == null) {
            handler.bridgeOffline(ThingStatusDetail.CONFIGURATION_ERROR, "Please set the hostname of the Qbus server.");
            return;
        }

        Socket socket = null;

        try {
            socket = new Socket(addr, port);
            qSocket = socket;
            qOut = new PrintWriter(socket.getOutputStream(), true);
            qIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            String msg = e.getMessage();
            handler.bridgeOffline(ThingStatusDetail.COMMUNICATION_ERROR, "No communication with Qbus Server. " + msg);
            return;
        }

        setSN();
        getSN();

        // Connect to Qbus server
        connect();

        // Then start thread to listen to incoming updates from Qbus.
        threadExecutor.execute(() -> {
            try {
                qbusListener();
            } catch (IOException e) {
                String msg = e.getMessage();
                logger.warn("Could not start listening thread, IOException: {}", msg);
            } catch (InterruptedException e) {
                String msg = e.getMessage();
                logger.warn("Could not start listening thread, InterruptedException: {}", msg);
            }
        });

        if (!ctdConnected) {
            handler.bridgePending("Waiting for CTD to come online...");
        }
    }

    /**
     * Cleanup socket when the communication with Qbus Server is closed.
     *
     * @throws IOException
     *
     */
    public synchronized void stopCommunication() throws IOException {
        listenerStopped = true;

        Socket socket = qSocket;

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignore) {
                // ignore IO Error when trying to close the socket if the intention is to close it anyway
            }
        }

        BufferedReader reader = this.qIn;
        if (reader != null) {
            reader.close();
        }

        PrintWriter writer = this.qOut;
        if (writer != null) {
            writer.close();
        }

        qSocket = null;
        qbusListenerRunning = false;
        ctdConnected = false;

        logger.trace("Communication stopped from thread {}", Thread.currentThread().getId());
    }

    /**
     * Close and restart communication with Qbus Server.
     *
     * @throws InterruptedException
     * @throws IOException
     */
    public synchronized void restartCommunication() throws InterruptedException, IOException {
        stopCommunication();

        startCommunication();
    }

    /**
     * Thread that handles incoming messages from Qbus client.
     * <p>
     * The thread listens to the TCP socket opened at instantiation of the {@link QbusCommunication} class
     * and interprets all incomming json messages. It triggers state updates for active channels linked to the
     * Qbus outputs. It is started after initialization of the communication.
     *
     * @return
     * @throws IOException
     * @throws InterruptedException
     *
     *
     */
    private void qbusListener() throws IOException, InterruptedException {
        String qMessage;

        listenerStopped = false;
        qbusListenerRunning = true;

        BufferedReader reader = this.qIn;

        if (reader == null) {
            throw new IOException("Bufferreader for incoming messages not initialized.");
        }

        try {
            while (!Thread.currentThread().isInterrupted() && ((qMessage = reader.readLine()) != null)) {
                readMessage(qMessage);

            }
        } catch (IOException e) {
            if (!listenerStopped) {
                qbusListenerRunning = false;
                // the IO has stopped working, so we need to close cleanly and try to restart
                restartCommunication();
                return;
            }
        } finally {
            qbusListenerRunning = false;
        }

        if (!listenerStopped) {
            qbusListenerRunning = false;

            QbusBridgeHandler handler = bridgeCallBack;

            if (handler != null) {
                ctdConnected = false;
                handler.bridgeOffline(ThingStatusDetail.COMMUNICATION_ERROR, "No communication with Qbus server");
            }
        }

        qbusListenerRunning = false;
        logger.trace("Event listener thread stopped on thread {}", Thread.currentThread().getId());
    };

    /**
     * Called by other methods to send json data to Qbus.
     *
     * @param qMessage
     * @throws InterruptedException
     * @throws IOException
     */
    synchronized void sendMessage(Object qMessage) throws InterruptedException, IOException {
        PrintWriter writer = qOut;
        String json = gsonOut.toJson(qMessage);

        if (writer != null) {
            writer.println(json);
            // Delay after sending data to improve scene execution
            TimeUnit.MILLISECONDS.sleep(250);
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
     * Method that interprets all feedback from Qbus Server application and calls appropriate handling methods.
     * <ul>
     * <li>Get request & update states for Bistabiel/Timers/Intervals/Mono outputs
     * <li>Get request & update states for the Scenes
     * <li>Get request & update states for Dimmers 1T and 2T
     * <li>Get request & update states for Shutters
     * <li>Get request & update states for Thermostats
     * <li>Get request & update states for CO2
     * </ul>
     *
     * @param qMessage message read from Qbus.
     * @throws InterruptedException
     * @throws IOException
     *
     */
    private void readMessage(String qMessage) {
        String sn = null;
        String cmd = "";
        String ctd = null;
        Integer id = null;
        Integer state = null;
        Integer mode = null;
        Double setpoint = null;
        Double measured = null;
        Integer slats = null;

        QbusMessageBase qMessageGson;
        try {
            qMessageGson = gsonIn.fromJson(qMessage, QbusMessageBase.class);

            if (qMessageGson != null) {
                ctd = qMessageGson.getSn();
                cmd = qMessageGson.getCmd();
                id = qMessageGson.getId();
                state = qMessageGson.getState();
                mode = qMessageGson.getMode();
                setpoint = qMessageGson.getSetPoint();
                measured = qMessageGson.getMeasured();
                slats = qMessageGson.getSlatState();
            }
        } catch (JsonParseException e) {
            String msg = e.getMessage();
            logger.trace("Not acted on unsupported json {} : {}", qMessage, msg);
            return;
        }
        QbusBridgeHandler handler = bridgeCallBack;

        if (handler != null) {
            sn = handler.getSn();
        }

        if (sn != null && ctd != null) {
            try {
                if (sn.equals(ctd) && qMessageGson != null) { // Check if commands are for this Bridge
                    // Handle all outputs from Qbus
                    if ("returnOutputs".equals(cmd)) {
                        outputs = ((QbusMessageListMap) qMessageGson).getOutputs();

                        for (Map<String, String> ctdOutputs : outputs) {

                            String ctdType = ctdOutputs.get("type");
                            String ctdIdStr = ctdOutputs.get("id");
                            Integer ctdId = null;

                            if (ctdIdStr != null) {
                                ctdId = Integer.parseInt(ctdIdStr);
                            } else {
                                return;
                            }

                            if (ctdType != null) {
                                String ctdState = ctdOutputs.get("state");
                                String ctdMmode = ctdOutputs.get("regime");
                                String ctdSetpoint = ctdOutputs.get("setpoint");
                                String ctdMeasured = ctdOutputs.get("measured");
                                String ctdSlats = ctdOutputs.get("slats");

                                Integer ctdStateI = null;
                                if (ctdState != null) {
                                    ctdStateI = Integer.parseInt(ctdState);
                                }

                                Integer ctdSlatsI = null;
                                if (ctdSlats != null) {
                                    ctdSlatsI = Integer.parseInt(ctdSlats);
                                }

                                Integer ctdMmodeI = null;
                                if (ctdMmode != null) {
                                    ctdMmodeI = Integer.parseInt(ctdMmode);
                                }

                                Double ctdSetpointD = null;
                                if (ctdSetpoint != null) {
                                    ctdSetpointD = Double.parseDouble(ctdSetpoint);
                                }

                                Double ctdMeasuredD = null;
                                if (ctdMeasured != null) {
                                    ctdMeasuredD = Double.parseDouble(ctdMeasured);
                                }

                                if (ctdState != null) {
                                    if (ctdType.equals("bistabiel")) {
                                        QbusBistabiel output = new QbusBistabiel(ctdId);
                                        if (!bistabiel.containsKey(ctdId)) {
                                            output.setQComm(this);
                                            output.updateState(ctdStateI);
                                            bistabiel.put(ctdId, output);
                                        } else {
                                            output.updateState(ctdStateI);
                                        }
                                    } else if (ctdType.equals("dimmer")) {
                                        QbusDimmer output = new QbusDimmer(ctdId);
                                        if (!dimmer.containsKey(ctdId)) {
                                            output.setQComm(this);
                                            output.updateState(ctdStateI);
                                            dimmer.put(ctdId, output);
                                        } else {
                                            output.updateState(ctdStateI);
                                        }
                                    } else if (ctdType.equals("CO2")) {
                                        QbusCO2 output = new QbusCO2();
                                        if (!co2.containsKey(ctdId)) {
                                            output.updateState(ctdStateI);
                                            co2.put(ctdId, output);
                                        } else {
                                            output.updateState(ctdStateI);
                                        }
                                    } else if (ctdType.equals("scene")) {
                                        QbusScene output = new QbusScene(ctdId);
                                        if (!scene.containsKey(ctdId)) {
                                            output.setQComm(this);
                                            scene.put(ctdId, output);
                                        }
                                    } else if (ctdType.equals("rol")) {
                                        QbusRol output = new QbusRol(ctdId);
                                        if (!rol.containsKey(ctdId)) {
                                            output.setQComm(this);
                                            output.updateState(ctdStateI);
                                            if (ctdSlats != null) {
                                                output.updateSlats(ctdSlatsI);
                                            }
                                            rol.put(ctdId, output);
                                        } else {
                                            output.updateState(ctdStateI);
                                            if (ctdSlats != null) {
                                                output.updateSlats(ctdSlatsI);
                                            }
                                        }
                                    }
                                } else if (ctdMeasuredD != null && ctdSetpointD != null && ctdMmodeI != null) {
                                    if (ctdType.equals("thermostat")) {
                                        QbusThermostat output = new QbusThermostat(ctdId);
                                        if (!thermostat.containsKey(ctdId)) {
                                            output.setQComm(this);
                                            output.updateState(ctdMeasuredD, ctdSetpointD, ctdMmodeI);
                                            thermostat.put(ctdId, output);
                                        } else {
                                            output.updateState(ctdMeasuredD, ctdSetpointD, ctdMmodeI);
                                        }
                                    }
                                }
                            }
                        }
                        // Handle update commands from Qbus
                    } else if ("updateBistabiel".equals(cmd)) {
                        if (id != null && state != null) {
                            updateBistabiel(id, state);
                        }
                    } else if ("updateDimmer".equals(cmd)) {
                        if (id != null && state != null) {
                            updateDimmer(id, state);
                        }
                    } else if ("updateDimmer".equals(cmd)) {
                        if (id != null && state != null) {
                            updateDimmer(id, state);
                        }
                    } else if ("updateCo2".equals(cmd)) {
                        if (id != null && state != null) {
                            updateCO2(id, state);
                        }
                    } else if ("updateThermostat".equals(cmd)) {
                        if (id != null && measured != null && setpoint != null && mode != null) {
                            updateThermostat(id, mode, setpoint, measured);
                        }
                    } else if ("updateRol02p".equals(cmd)) {
                        if (id != null && state != null) {
                            updateRol(id, state);
                        }
                    } else if ("updateRol02pSlat".equals(cmd)) {
                        if (id != null && state != null && slats != null) {
                            updateRolSlats(id, state, slats);
                        }
                        // Incomming commands from Qbus server to verify the client connection
                    } else if ("noconnection".equals(cmd)) {
                        eventDisconnect();
                    } else if ("connected".equals(cmd)) {
                        // threadExecutor.execute(() -> {
                        try {
                            requestOutputs();
                        } catch (InterruptedException e) {
                            String msg = e.getMessage();
                            logger.warn("Could not request outputs. InterruptedException: {}", msg);
                        } catch (IOException e) {
                            String msg = e.getMessage();
                            logger.warn("Could not request outputs. IOException: {}", msg);
                        }
                    }
                }
            } catch (JsonParseException e) {
                String msg = e.getMessage();
                logger.warn("Not acted on unsupported json {}, {}", qMessage, msg);
            }
        }
    }

    /**
     * Initialize the communication object
     */
    @Override
    public void initialize() {
    }

    /**
     * Initial connection to Qbus Server to open a communication channel
     *
     * @throws InterruptedException
     * @throws IOException
     */
    private void connect() throws InterruptedException, IOException {
        String snr = getSN();

        if (snr != null) {
            QbusMessageCmd qCmd = new QbusMessageCmd(snr, "openHAB");

            sendMessage(qCmd);

            BufferedReader reader = qIn;

            if (reader == null) {
                throw new IOException("Cannot read from socket, reader not connected.");
            }
            readMessage(reader.readLine());

        } else {
            QbusBridgeHandler handler = bridgeCallBack;
            if (handler != null) {
                handler.bridgeOffline(ThingStatusDetail.CONFIGURATION_ERROR, "No serial nr defined");
            }
        }
    }

    /**
     * Send a request for all available outputs and initializes them via readMessage
     *
     * @throws InterruptedException
     * @throws IOException
     */
    private void requestOutputs() throws InterruptedException, IOException {
        String snr = getSN();
        QbusBridgeHandler handler = bridgeCallBack;

        if (snr != null) {
            QbusMessageCmd qCmd = new QbusMessageCmd(snr, "all");
            sendMessage(qCmd);

            BufferedReader reader = qIn;
            if (reader == null) {
                throw new IOException("Cannot read from socket, reader not connected.");
            }
            readMessage(reader.readLine());
            ctdConnected = true;

            if (handler != null) {
                handler.bridgeOnline();
            }

        } else {
            if (handler != null) {
                handler.bridgeOffline(ThingStatusDetail.CONFIGURATION_ERROR, "No serial nr defined");
            }
        }
    }

    /**
     * Event on incoming Bistabiel/Timer/Mono/Interval updates
     *
     * @param id
     * @param state
     */
    private void updateBistabiel(Integer id, Integer state) {
        QbusBistabiel qBistabiel = this.bistabiel.get(id);

        if (qBistabiel != null) {
            qBistabiel.updateState(state);
        } else {
            logger.trace("Bistabiel in controller not known {}", id);
        }
    }

    /**
     * Event on incoming Dimmer updates
     *
     * @param id
     * @param state
     */
    private void updateDimmer(Integer id, Integer state) {
        QbusDimmer qDimmer = this.dimmer.get(id);

        if (qDimmer != null) {
            qDimmer.updateState(state);
        } else {
            logger.trace("Dimmer in controller not known {}", id);
        }
    }

    /**
     * Event on incoming thermostat updates
     *
     * @param id
     * @param mode
     * @param sp
     * @param ct
     */
    private void updateThermostat(Integer id, int mode, double sp, double ct) {
        QbusThermostat qThermostat = this.thermostat.get(id);

        if (qThermostat != null) {
            qThermostat.updateState(ct, sp, mode);
        } else {
            logger.trace("Thermostat in controller not known {}", id);
        }
    }

    /**
     * Event on incoming CO2 updates
     *
     * @param id
     * @param state
     */
    private void updateCO2(Integer id, Integer state) {
        QbusCO2 qCO2 = this.co2.get(id);

        if (qCO2 != null) {
            qCO2.updateState(state);
        } else {
            logger.trace("CO2 in controller not known {}", id);
        }
    }

    /**
     * Event on incoming screen updates
     *
     * @param id
     * @param state
     */
    private void updateRol(Integer id, Integer state) {
        QbusRol qRol = this.rol.get(id);

        if (qRol != null) {
            qRol.updateState(state);
        } else {
            logger.trace("ROL02P in controller not known {}", id);
        }
    }

    /**
     * Event on incoming screen with slats updates
     *
     * @param id
     * @param state
     * @param slats
     */
    private void updateRolSlats(Integer id, Integer state, Integer slats) {
        QbusRol qRol = this.rol.get(id);

        if (qRol != null) {
            qRol.updateState(state);
            qRol.updateSlats(slats);
        } else {
            logger.trace("ROL02P with slats in controller not known {}", id);
        }
    }

    /**
     * Put Bridge offline when there is no connection from the QbusClient
     *
     */
    private void eventDisconnect() {
        QbusBridgeHandler handler = bridgeCallBack;

        if (handler != null) {
            handler.bridgePending("Waiting for CTD connection");
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
     * Return all Scenes in the Qbus Controller
     *
     * @return
     */
    public Map<Integer, QbusScene> getScene() {
        return this.scene;
    }

    /**
     * Return all Dimmers outputs in the Qbus Controller.
     *
     * @return
     */
    public Map<Integer, QbusDimmer> getDimmer() {
        return this.dimmer;
    }

    /**
     * Return all rollershutter/screen outputs in the Qbus Controller.
     *
     * @return
     */
    public Map<Integer, QbusRol> getRol() {
        return this.rol;
    }

    /**
     * Return all Thermostats outputs in the Qbus Controller.
     *
     * @return
     */
    public Map<Integer, QbusThermostat> getThermostat() {
        return this.thermostat;
    }

    /**
     * Return all CO2 outputs in the Qbus Controller.
     *
     * @return
     */
    public Map<Integer, QbusCO2> getCo2() {
        return this.co2;
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
        return ctdConnected;
    }

    /**
     * @param bridgeCallBack the bridgeCallBack to set
     */
    public void setBridgeCallBack(QbusBridgeHandler bridgeCallBack) {
        this.bridgeCallBack = bridgeCallBack;
    }

    /**
     * Get the serial number of the CTD as configured in the Bridge.
     *
     * @return serial number of controller
     */
    public @Nullable String getSN() {
        return this.ctd;
    }

    /**
     * Sets the serial number of the CTD, as configured in the Bridge.
     */
    public void setSN() {
        QbusBridgeHandler qBridgeHandler = bridgeCallBack;

        if (qBridgeHandler != null) {
            this.ctd = qBridgeHandler.getSn();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }
}
