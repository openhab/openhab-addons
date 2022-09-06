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
package org.openhab.binding.arcam.internal.connection;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.arcam.internal.ArcamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper around a Socket, including reconnection logic
 *
 * @author Joep Admiraal - Initial contribution
 */
@NonNullByDefault
public class ArcamSocket implements ArcamConnectionReaderListener {
    private final Logger logger = LoggerFactory.getLogger(ArcamSocket.class);
    private static final int ALIVE_INTERVAL = 10;
    private static final int PORT = 50000;

    @Nullable
    private Socket socket;
    @Nullable
    private ArcamConnectionReader acr;
    @Nullable
    private String hostname;
    @Nullable
    private OutputStream outputStream;
    @Nullable
    private ScheduledFuture<?> pollingTask;
    @Nullable
    private ScheduledFuture<?> reconnectTask;

    private String thingUID;
    private ScheduledExecutorService scheduler;
    private Instant lastResponseTime;
    private ArcamSocketListener socketListener;
    private byte[] heartbeatCommand;

    private ArcamConnectionState connectionState;

    public ArcamSocket(String thingUID, ScheduledExecutorService scheduler, byte[] heartbeatCommand,
            ArcamSocketListener socketListener) {
        this.thingUID = thingUID;
        this.scheduler = scheduler;
        this.lastResponseTime = Instant.MIN;
        this.connectionState = ArcamConnectionState.CONNECTING;
        this.socketListener = socketListener;
        this.heartbeatCommand = heartbeatCommand;
    }

    public void connect(String hostname) {
        synchronized (this) {
            String prevHostname = this.hostname;
            if (hostname.equals(prevHostname)) {
                logger.trace("Skip connect as we're already connected to {}", hostname);
                return;
            }

            this.hostname = hostname;
            lastResponseTime = Instant.now();
        }

        try {
            connect();
        } catch (IOException e) {
            synchronized (this) {
                handleConnectionIssue();
            }
        }

        pollingTask = scheduler.scheduleWithFixedDelay(this::sendHeartbeat, ALIVE_INTERVAL, ALIVE_INTERVAL,
                TimeUnit.SECONDS);
    }

    private void connect() throws UnknownHostException, IOException {
        logger.debug("connecting to: {} {}", hostname, PORT);
        synchronized (this) {
            Socket s = new Socket(hostname, PORT);
            socket = s;

            outputStream = s.getOutputStream();
            ArcamConnectionReader acr = new ArcamConnectionReader(s, this);
            acr.setName("OH-binding-" + thingUID);
            acr.setDaemon(true);
            acr.start();
            this.acr = acr;
            connectionState = ArcamConnectionState.CONNECTED;
        }

        socketListener.onConnection();
    }

    private void reconnect() {
        if (hostname == null) {
            return;
        }
        logger.debug("Atempting to reconnect to: {}", hostname);

        ArcamConnectionReader acr = this.acr;
        if (acr != null) {
            acr.dispose();
            try {
                acr.join();
            } catch (InterruptedException e) {
            }
            acr = null;
        }

        // inputStream and outputStream are closed by socket.close()

        Socket socket = this.socket;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // Since we're closing the socket anyways we're not interested in closing IO errors
            }
        }

        try {
            connect();
        } catch (IOException e) {
            reconnectTask = scheduler.schedule(this::reconnect, 10, TimeUnit.SECONDS);
        }
    }

    // This method should be called from a synchronized method in order to be thread safe
    private void handleConnectionIssue() {
        logger.debug("handleConnectionIssue");

        if (connectionState == ArcamConnectionState.RECONNECTING) {
            logger.debug("skip reconnect because already connecting");
            return;
        }

        connectionState = ArcamConnectionState.RECONNECTING;
        socketListener.onError();

        reconnect();
    }

    private synchronized void sendHeartbeat() {
        if (connectionState != ArcamConnectionState.CONNECTED) {
            return;
        }

        if (Instant.now().getEpochSecond() - lastResponseTime.getEpochSecond() > ALIVE_INTERVAL + 10) {
            handleConnectionIssue();
        }

        logger.trace("Sending heartbeat bytes: {}", ArcamUtil.bytesToHex(heartbeatCommand));
        sendCommand(heartbeatCommand);
    }

    public synchronized void sendCommand(byte[] data) {
        OutputStream os = outputStream;
        if (os == null) {
            return;
        }

        try {
            logger.debug("outputStream write: {}", ArcamUtil.bytesToHex(data));
            os.write(data);
        } catch (IOException e) {
            handleConnectionIssue();
        }
    }

    public synchronized void dispose() {
        final ScheduledFuture<?> pollingTask = this.pollingTask;
        if (pollingTask != null) {
            pollingTask.cancel(true);
            this.pollingTask = null;
        }

        final ScheduledFuture<?> reconnectTask = this.reconnectTask;
        if (reconnectTask != null) {
            reconnectTask.cancel(true);
            this.reconnectTask = null;
        }

        try {
            OutputStream os = outputStream;
            if (os != null) {
                os.close();
            }
            ArcamConnectionReader acr = this.acr;
            if (acr != null) {
                acr.dispose();
            }
            Socket s = socket;
            if (s != null) {
                s.close();
            }
        } catch (IOException e) {
            logger.debug("{}", e.getMessage());
        }
    }

    @Override
    public void onResponse(ArcamResponse response) {
        synchronized (this) {
            lastResponseTime = Instant.now();
        }
        socketListener.onResponse(response);
    }

    @Override
    public synchronized void onConnReadError() {
        handleConnectionIssue();
    }
}
