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
package org.openhab.binding.dominoswiss.internal;

import static org.openhab.binding.dominoswiss.internal.DominoswissBindingConstants.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EGateHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Frieso Aeschbacher - Initial contribution
 */
@NonNullByDefault
public class EGateHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(EGateHandler.class);
    private @Nullable Socket egateSocket;

    private int port;
    private @Nullable String host;
    private static final int SOCKET_TIMEOUT_MILLISEC = 1000;
    private final Object lock = new Object();
    private @Nullable BufferedWriter writer;
    private @Nullable BufferedReader reader;
    private @Nullable Future<?> refreshJob;
    private Map<String, ThingUID> registeredBlinds;
    private @Nullable ScheduledFuture<?> pollingJob;

    public EGateHandler(Bridge thing) {
        super(thing);
        registeredBlinds = new HashMap<>();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(GETCONFIG)) {
            sendCommand("EthernetGet;\r");
        }
    }

    @Override
    public void initialize() {
        DominoswissConfiguration config;
        config = this.getConfigAs(DominoswissConfiguration.class);
        host = config.ipAddress;
        port = config.port;

        if (host != null && port > 0) {
            // Create a socket to eGate

            InetSocketAddress socketAddress = new InetSocketAddress(host, port);
            Socket localEgateSocket = new Socket();
            try {
                localEgateSocket.connect(socketAddress, SOCKET_TIMEOUT_MILLISEC);
                writer = new BufferedWriter(new OutputStreamWriter(localEgateSocket.getOutputStream()));
                egateSocket = localEgateSocket;
                updateStatus(ThingStatus.ONLINE);
                logger.debug("Egate successfully connected {}", egateSocket.toString());
            } catch (IOException e) {
                logger.debug("IOException in initialize: {} host {} port {}", e.toString(), host, port);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.toString());
                egateSocket = null;
            }
            pollingJob = scheduler.scheduleWithFixedDelay(this::pollingConfig, 0, 30, TimeUnit.SECONDS);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Cannot connect to dominoswiss eGate gateway. host IP address or port are not set.");
        }
    }

    @Override
    public void dispose() {
        try {
            Socket localEgateSocket = egateSocket;
            if (localEgateSocket != null) {
                localEgateSocket.close();
            }
            Future<?> localRefreshJob = refreshJob;
            if (localRefreshJob != null) {
                localRefreshJob.cancel(true);
            }
            BufferedReader localReader = reader;
            if (localReader != null) {
                localReader.close();
            }

            BufferedWriter localWriter = writer;
            if (localWriter != null) {
                localWriter.close();
            }
            ScheduledFuture<?> localPollingJob = pollingJob;
            if (localPollingJob != null) {
                localPollingJob.cancel(true);
                localPollingJob = null;
            }
            logger.debug("EGate Handler connection closed, disposing");
        } catch (IOException e) {
            logger.debug("EGate Handler Error on dispose: {} ", e.toString());
        }
    }

    public synchronized boolean isConnected() {
        Socket localEGateSocket = egateSocket;
        if (localEGateSocket == null) {
            logger.debug("EGate is not connected, Socket is null");
            return false;
        }

        // NOTE: isConnected() returns true once a connection is made and will
        // always return true even after the socket is closed
        // http://stackoverflow.com/questions/10163358/
        logger.debug("EGate isconnected() {}, isClosed() {}", localEGateSocket.isConnected(),
                localEGateSocket.isClosed());

        return localEGateSocket.isConnected() && !localEGateSocket.isClosed();
    }

    /**
     * Possible Instructions are:
     * FssTransmit 1 Kommandoabsetzung (Controller > eGate > Dominoswiss)
     * FssReceive 2 Empfangenes Funkpaket (Dominoswiss > eGate > Controller)
     *
     * @throws InterruptedException
     *
     */

    public void tiltUp(String id) throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            pulseUp(id);
            Thread.sleep(150); // sleep to not confuse the blinds

        }
    }

    public void tiltDown(String id) throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            pulseDown(id);
            Thread.sleep(150);// sleep to not confuse the blinds
        }
    }

    public void pulseUp(String id) {
        sendCommand("Instruction=1;ID=" + id + ";Command=1;Priority=1;CheckNr=3415347;" + CR);
    }

    public void pulseDown(String id) {
        sendCommand("Instruction=1;ID=" + id + ";Command=2;Priority=1;CheckNr=2764516;" + CR);
    }

    public void continuousUp(String id) {
        sendCommand("Instruction=1;ID=" + id + ";Command=3;Priority=1;CheckNr=2867016;" + CR, 20000);
    }

    public void continuousDown(String id) {
        sendCommand("Instruction=1;ID=" + id + ";Command=4;Priority=1;CheckNr=973898;" + CR, 20000);
    }

    public void stop(String id) {
        sendCommand("Instruction=1;ID=" + id + ";Command=5;Priority=1;CheckNr=5408219;" + CR);
    }

    public void registerBlind(String id, ThingUID uid) {
        logger.debug("Registring Blind id {} with thingUID {}", id, uid);
        registeredBlinds.put(id, uid);
    }

    /**
     * Send a command to the eGate Server.
     */

    private void sendCommand(String command) {
        sendCommand(command, SOCKET_TIMEOUT_MILLISEC);
    }

    private synchronized void sendCommand(String command, int timeout) {
        logger.debug("EGate got command: {}", command);
        Socket localEGateSocket = egateSocket;
        BufferedWriter localWriter = writer;
        if (localEGateSocket == null || localWriter == null) {
            logger.debug("Error eGateSocket null, writer null, returning...");
            return;
        }
        if (!isConnected()) {
            logger.debug("no connection to Dominoswiss eGate server when trying to send command, returning...");
            return;
        }

        // Send plain string to eGate Server,
        try {
            localEGateSocket.setSoTimeout(timeout);
            localWriter.write(command);
            localWriter.flush();
        } catch (IOException e) {
            logger.debug("Error while sending command {} to Dominoswiss eGate Server {} ", command, e.toString());
        }
    }

    private void pollingConfig() {
        if (!isConnected()) {
            logger.debug("PollingConfig Run, is not connected so let's connect");
            Socket localEGateSocket = egateSocket;
            BufferedWriter localWriter = writer;
            if (localEGateSocket == null || localWriter == null) {
                logger.debug("Error eGateSocket null, writer null in pollingConfig(), returning...");
                return;
            }

            synchronized (lock) {
                try {
                    localEGateSocket.connect(new InetSocketAddress(host, port), SOCKET_TIMEOUT_MILLISEC);
                    logger.debug("pollingConfig() successsully connected {}", localEGateSocket.isClosed());
                    localWriter.write("SilenceModeSet;Value=0;" + CR);
                    localWriter.flush();
                } catch (IOException e) {
                    logger.debug("IOException in pollingConfig: {} host {} port {}", e.toString(), host, port);
                    try {
                        localEGateSocket.close();
                        egateSocket = null;
                        logger.debug("EGate closed");
                    } catch (IOException e1) {
                        logger.debug("EGate Socket not closed {}", e1.toString());
                    }
                    egateSocket = null;
                }
                if (egateSocket != null) {
                    updateStatus(ThingStatus.ONLINE);
                    startAutomaticRefresh();
                    logger.debug("EGate Handler started automatic refresh, status: {} ",
                            getThing().getStatus().toString());
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
            }
        }
    }

    private void startAutomaticRefresh() {
        Runnable runnable = () -> {
            try {
                Socket localSocket = egateSocket;
                if (localSocket == null) {
                    return;
                }
                BufferedReader localReader = reader;
                if (localReader == null) {
                    reader = new BufferedReader(new InputStreamReader(localSocket.getInputStream()));
                }
                if (localReader != null && localReader.ready()) {
                    String input = localReader.readLine();
                    logger.debug("Reader got from EGATE: {}", input);
                    onData(input);
                }
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "Error while reading command from Dominoswiss eGate Server " + e.toString());
            }
        };
        refreshJob = scheduler.submit(runnable);
    }

    /**
     * Finds and returns a child thing for a given UID of this bridge.
     *
     * @param uid uid of the child thing
     * @return child thing with the given uid or null if thing was not found
     */
    public @Nullable Thing getThingByUID(ThingUID uid) {
        Bridge bridge = getThing();

        List<Thing> things = bridge.getThings();

        for (Thing thing : things) {
            if (thing.getUID().equals(uid)) {
                return thing;
            }
        }

        return null;
    }

    protected void onData(String input) {
        // Instruction=2;ID=19;Command=1;Value=0;Priority=0;
        Map<String, String> map = new HashMap<>();
        // split on ;
        String[] parts = input.split(";");
        if (parts.length >= 2) {
            for (int i = 0; i < parts.length; i += 2) {
                map.put(parts[i], parts[i + 1]);
            }
        }
    }
}
