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
package org.openhab.binding.mercedesme.internal.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.mercedesme.internal.Constants;
import org.openhab.binding.mercedesme.internal.handler.AccountHandler;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.daimler.mbcarkit.proto.Client.ClientMessage;
import com.daimler.mbcarkit.proto.Protos.AcknowledgeAssignedVehicles;
import com.daimler.mbcarkit.proto.VehicleEvents;
import com.daimler.mbcarkit.proto.VehicleEvents.AcknowledgeVEPUpdatesByVIN;
import com.daimler.mbcarkit.proto.VehicleEvents.PushMessage;
import com.daimler.mbcarkit.proto.Vehicleapi.AcknowledgeAppTwinCommandStatusUpdatesByVIN;
import com.daimler.mbcarkit.proto.Vehicleapi.AppTwinCommandStatusUpdatesByVIN;
import com.daimler.mbcarkit.proto.Vehicleapi.AppTwinPendingCommandsRequest;

/**
 * {@link MBWebsocket} as socket endpoint to communicate with Mercedes
 *
 * @author Bernd Weymann - Initial contribution
 */
@WebSocket
@NonNullByDefault
public class MBWebsocket {
    // timeout 14 Minutes - just below scheduling of 15 Minutes by AccountHandler
    private static final int CONNECT_TIMEOUT_MS = 14 * 60 * 1000;
    // standard runtime of Websocket
    private static final int WS_RUNTIME_MS = 60 * 1000;
    // addon time of 1 minute for a new send command
    private static final int ADDON_MESSAGE_TIME_MS = 60 * 1000;
    // check Socket time elapsed each second
    private static final int CHECK_INTERVAL_MS = 1000;
    // additional 5 minutes after keep alive
    private static final int KEEP_ALIVE_ADDON = 5 * 60 * 1000;

    private final Logger logger = LoggerFactory.getLogger(MBWebsocket.class);
    private AccountHandler accountHandler;
    private boolean running = false;
    private Instant runTill = Instant.now();
    private @Nullable Session session;
    private List<ClientMessage> commandQueue = new ArrayList<>();

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
            logger.trace("Websocket start {}", websocketURL);
            if (Constants.JUNIT_TOKEN.equals(request.getHeader("Authorization"))) {
                // avoid unit test requesting real websocket - simply return
                return;
            }
            client.start();
            client.connect(this, new URI(websocketURL), request);
            while (keepAlive || Instant.now().isBefore(runTill)) {
                try {
                    Thread.sleep(CHECK_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    logger.trace("Websocket interrupted during sleeping - stop executing");
                    runTill = Instant.MIN;
                }
                // sends one message per second
                if (sendMessage()) {
                    // add additional runtime to execute and finish command
                    runTill = runTill.plusMillis(ADDON_MESSAGE_TIME_MS);
                }
            }
            logger.trace("Websocket stop");
            client.stop();
            client.destroy();
        } catch (Throwable t) {
            // catch Exceptions of start stop and declare communication error
            accountHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/mercedesme.account.status.websocket-failure");
            logger.warn("Websocket handling exception: {}", t.getMessage());
        }
        synchronized (this) {
            running = false;
        }
    }

    public void setCommand(ClientMessage cm) {
        commandQueue.add(cm);
    }

    private boolean sendMessage() {
        if (!commandQueue.isEmpty()) {
            ClientMessage message = commandQueue.remove(0);
            logger.trace("Send Message {}", message.getAllFields());
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                message.writeTo(baos);
                if (session != null) {
                    session.getRemote().sendBytes(ByteBuffer.wrap(baos.toByteArray()));
                }
                return true;
            } catch (IOException e) {
                logger.warn("Error sending message {} : {}", message.getAllFields(), e.getMessage());
            }
            logger.info("Send Message {} done", message.getAllFields());
        }
        return false;
    }

    private void sendAcknowledgeMessage(ClientMessage message) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            message.writeTo(baos);
            if (session != null) {
                session.getRemote().sendBytes(ByteBuffer.wrap(baos.toByteArray()));
            }
        } catch (IOException e) {
            logger.warn("Error sending acknowledge {} : {}", message.getAllFields(), e.getMessage());
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void interrupt() {
        synchronized (this) {
            runTill = Instant.MIN;
            keepAlive = false;
        }
    }

    public void keepAlive(boolean b) {
        if (!keepAlive) {
            if (b) {
                logger.trace("WebSocket - keep alive start");
            }
        } else {
            if (!b) {
                // after keep alive is finished add 5 minutes to cover e.g. door events after trip is finished
                runTill = Instant.now().plusMillis(KEEP_ALIVE_ADDON);
                logger.trace("Websocket - keep alive stop - run till {}", runTill.toString());
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
                    sendAcknowledgeMessage(cm);
                }
            } else if (pm.hasAssignedVehicles()) {
                for (int i = 0; i < pm.getAssignedVehicles().getVinsCount(); i++) {
                    String vin = pm.getAssignedVehicles().getVins(0);
                    accountHandler.discovery(vin);
                }
                AcknowledgeAssignedVehicles ack = AcknowledgeAssignedVehicles.newBuilder().build();
                ClientMessage cm = ClientMessage.newBuilder().setAcknowledgeAssignedVehicles(ack).build();
                sendAcknowledgeMessage(cm);
            } else if (pm.hasApptwinCommandStatusUpdatesByVin()) {
                AppTwinCommandStatusUpdatesByVIN csubv = pm.getApptwinCommandStatusUpdatesByVin();
                accountHandler.commandStatusUpdate(csubv.getUpdatesByVinMap());
                AcknowledgeAppTwinCommandStatusUpdatesByVIN ack = AcknowledgeAppTwinCommandStatusUpdatesByVIN
                        .newBuilder().setSequenceNumber(csubv.getSequenceNumber()).build();
                ClientMessage cm = ClientMessage.newBuilder().setAcknowledgeApptwinCommandStatusUpdateByVin(ack)
                        .build();
                sendAcknowledgeMessage(cm);
            } else if (pm.hasApptwinPendingCommandRequest()) {
                AppTwinPendingCommandsRequest pending = pm.getApptwinPendingCommandRequest();
                if (!pending.getAllFields().isEmpty()) {
                    logger.trace("Pending Command {}", pending.getAllFields());
                }
            } else if (pm.hasDebugMessage()) {
                logger.trace("MB Debug Message: {}", pm.getDebugMessage().getMessage());
            } else {
                logger.trace("MB Message: {} not handled", pm.getAllFields());
            }
        } catch (IOException e) {
            // don't report thing status errors here.
            // Sometimes messages cannot be decoded which doesn't effect the overall functionality
            logger.trace("IOException {}", e.getMessage());
        } catch (Error err) {
            logger.trace("Error caught {}", err.getMessage());
        }
    }

    @OnWebSocketClose
    public void onDisconnect(Session session, int statusCode, String reason) {
        logger.debug("Disconnected from server. Status {} Reason {}", statusCode, reason);
        this.session = null;
        // ensure execution stop if disconnect was triggered from server side
        interrupt();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        accountHandler.updateStatus(ThingStatus.ONLINE);
        this.session = session;
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        logger.warn("onError {}", t.getMessage());
        accountHandler.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "@text/mercedesme.account.status.websocket-failure");
    }
}
