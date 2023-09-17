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
package org.openhab.binding.mercedesme.internal.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.mercedesme.internal.handler.AccountHandler;
import org.openhab.binding.mercedesme.internal.proto.Client.ClientMessage;
import org.openhab.binding.mercedesme.internal.proto.Protos.AcknowledgeAssignedVehicles;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents.AcknowledgeVEPUpdatesByVIN;
import org.openhab.binding.mercedesme.internal.proto.VehicleEvents.PushMessage;
import org.openhab.binding.mercedesme.internal.proto.Vehicleapi.AcknowledgeAppTwinCommandStatusUpdatesByVIN;
import org.openhab.binding.mercedesme.internal.proto.Vehicleapi.AppTwinCommandStatusUpdatesByVIN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MBWebsocket} class provides authentication callback endpoint
 *
 * @author Bernd Weymann - Initial contribution
 */

@WebSocket
public class MBWebsocket {
    private final Logger logger = LoggerFactory.getLogger(MBWebsocket.class);

    // timeout 14 Minutes - just below scheduling of 15 Minutes by Accounthandler
    private final int CONNECT_TIMEOUT_MS = 14 * 60 * 1000;
    // standard runtime of Websocket
    private final int WS_RUNTIME_MS = 60 * 1000;
    // addon time of 10 seconds for a new send command
    private final int ADDON_MESSAGE_TIME_MS = 10 * 1000;
    // check Socket time elapsed each second
    private final int CHECK_INTERVAL_MS = 1000;
    // additional 5 minutes after keep alive
    private final int KEEP_ALIVE_ADDON = 5 * 60 * 1000;

    private AccountHandler accountHandler;
    private boolean running = false;
    private Instant runTill = Instant.now();
    private @Nullable Future<?> sessionFuture;
    private @Nullable Session session;
    private List<ClientMessage> commandQueue = new ArrayList<ClientMessage>();

    private static int fileCounter = 1;
    private boolean keepAlive = false;

    public MBWebsocket(AccountHandler ah) {
        accountHandler = ah;
    }

    /**
     * Is called by
     * - scheduler every 15 minutes
     * - handler sending a command
     * - handler requesting refresh
     */
    public void run() {
        synchronized (this) {
            if (running) {
                return;
            } else {
                running = true;
                runTill = Instant.now().plusMillis(WS_RUNTIME_MS);
            }
        }
        try {
            WebSocketClient client = new WebSocketClient();
            client.setMaxIdleTimeout(CONNECT_TIMEOUT_MS);
            client.setStopTimeout(CONNECT_TIMEOUT_MS);
            ClientUpgradeRequest request = accountHandler.getClientUpgradeRequest();
            String websocketURL = accountHandler.getWSUri();
            logger.debug("Websocket start {}", websocketURL);
            client.start();
            sessionFuture = client.connect(this, new URI(websocketURL), request);
            while (keepAlive || Instant.now().isBefore(runTill)) {
                // sends one message per second
                if (sendMessage()) {
                    // add additional runtime to execute and finish command
                    runTill = runTill.plusMillis(ADDON_MESSAGE_TIME_MS);
                }
                try {
                    Thread.sleep(CHECK_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    logger.trace("Websocket interrupted during sleeping - stop executing");
                    runTill = Instant.MIN;
                }
            }
            logger.debug("Websocket stop");
            client.stop();
            client.destroy();
        } catch (Throwable t) {
            logger.warn("Websocket handling exception: {}", t.getMessage());
            StackTraceElement[] ste = t.getStackTrace();
            for (int i = 0; i < ste.length; i++) {
                logger.warn("{}", ste[i].toString());
            }
        }
        synchronized (this) {
            running = false;
        }
    }

    public void setCommand(@NonNull ClientMessage cm) {
        commandQueue.add(cm);
    }

    private boolean sendMessage() {
        if (commandQueue.size() > 0 && this.session != null) {
            ClientMessage message = commandQueue.remove(0);
            logger.info("Send Message {}", message.getAllFields());
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                message.writeTo(baos);
                session.getRemote().sendBytes(ByteBuffer.wrap(baos.toByteArray()));
                return true;
            } catch (IOException e) {
                logger.warn("Error sending message {} : {}", message.getAllFields(), e.getMessage());
            }
            logger.info("Send Message {} done", message.getAllFields());
        } else {
            // logger.info("Message {} or Session is null", commandQueue.size());
        }
        return false;
    }

    private void sendAchnowledgeMessage(ClientMessage message) {
        if (this.session != null) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                message.writeTo(baos);
                session.getRemote().sendBytes(ByteBuffer.wrap(baos.toByteArray()));
            } catch (IOException e) {
                logger.warn("Error sending acknowledge {} : {}", message.getAllFields(), e.getMessage());
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void interrupt() {
        synchronized (this) {
            runTill = Instant.MIN;
        }
        logger.info("Kill Websocket!");
    }

    public void keepAlive(boolean b) {
        if (!keepAlive) {
            if (b) {
                logger.info("WebSocket - keep alive start");
            }
        } else {
            if (!b) {
                // after keep alive is finished add 5 minutes to cover e.g. door events after trip is finished
                runTill = Instant.now().plusMillis(KEEP_ALIVE_ADDON);
                logger.info("Wbesocket - keep alive stop - run till {}", runTill.toString());
            }
        }
        keepAlive = b;
    }

    /**
     * endpoints
     */

    @OnWebSocketMessage
    public void onBytes(InputStream is) {
        try {
            PushMessage pm = VehicleEvents.PushMessage.parseFrom(is);
            if (pm.hasVepUpdates()) {
                boolean distributed = accountHandler.distributeVepUpdates(pm.getVepUpdates().getUpdatesMap());
                if (distributed) {
                    AcknowledgeVEPUpdatesByVIN ack = AcknowledgeVEPUpdatesByVIN.newBuilder()
                            .setSequenceNumber(pm.getVepUpdates().getSequenceNumber()).build();
                    ClientMessage cm = ClientMessage.newBuilder().setAcknowledgeVepUpdatesByVin(ack).build();
                    sendAchnowledgeMessage(cm);
                    logger.trace("Vehicle update acknowledged {}", cm.getAllFields());
                }
            } else if (pm.hasAssignedVehicles()) {
                for (int i = 0; i < pm.getAssignedVehicles().getVinsCount(); i++) {
                    String vin = pm.getAssignedVehicles().getVins(0);
                    accountHandler.discovery(vin);
                }
                AcknowledgeAssignedVehicles ack = AcknowledgeAssignedVehicles.newBuilder().build();
                ClientMessage cm = ClientMessage.newBuilder().setAcknowledgeAssignedVehicles(ack).build();
                sendAchnowledgeMessage(cm);
                logger.trace("Vehicle assignments acknowledged {}", cm.getAllFields());
            } else if (pm.hasApptwinCommandStatusUpdatesByVin()) {
                logger.debug("Command Status {}", pm.getApptwinCommandStatusUpdatesByVin().getAllFields());
                AppTwinCommandStatusUpdatesByVIN csubv = pm.getApptwinCommandStatusUpdatesByVin();
                accountHandler.commandStatusUpdate(csubv.getUpdatesByVinMap());
                AcknowledgeAppTwinCommandStatusUpdatesByVIN ack = AcknowledgeAppTwinCommandStatusUpdatesByVIN
                        .newBuilder().setSequenceNumber(csubv.getSequenceNumber()).build();
                ClientMessage cm = ClientMessage.newBuilder().setAcknowledgeApptwinCommandStatusUpdateByVin(ack)
                        .build();
                sendAchnowledgeMessage(cm);
                logger.trace("Command Status acknowledged {}" + cm.getAllFields());
            } else if (pm.hasApptwinPendingCommandRequest()) {
                // logger.trace("Pending Command {}", pm.getApptwinPendingCommandRequest().getAllFields());
            } else if (pm.hasDebugMessage()) {
                logger.trace("MB Debug Message: {}", pm.getDebugMessage().getMessage());
            } else {
                logger.debug("MB Message: {} not handeled", pm.getAllFields());
            }

        } catch (

        IOException e) {
            logger.warn("IOEXception {}", e.getMessage());
        } catch (Error err) {
            logger.warn("Error caught {}", err.getMessage());
            StackTraceElement[] stack = err.getStackTrace();
            for (int i = 0; i < stack.length; i++) {
                logger.warn("{}", stack[i]);
            }
        }
    }

    @OnWebSocketClose
    public void onDisconnect(Session session, int statusCode, String reason) {
        logger.debug("Disonnected from server. Status {} Reason {}", statusCode, reason);
        this.session = null;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        logger.debug("Connected to server");
        this.session = session;
        sendMessage();
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        logger.warn("Error {}", t.getMessage());
        StackTraceElement[] stack = t.getStackTrace();
        for (int i = 0; i < stack.length; i++) {
            logger.warn("{}", stack[i]);
        }
    }
}
