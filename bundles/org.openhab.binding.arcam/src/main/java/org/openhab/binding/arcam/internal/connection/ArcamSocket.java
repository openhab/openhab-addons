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

    public synchronized void connect(String hostname) {
        String prevHostname = this.hostname;
        if (hostname.equals(prevHostname)) {
            logger.trace("Skip connect as we're already connected to {}", hostname);
            return;
        }

        this.hostname = hostname;
        try {
            connect();
        } catch (IOException e) {
            handleConnectionIssue();
        }

        pollingTask = scheduler.scheduleWithFixedDelay(this::sendHeartbeat, ALIVE_INTERVAL, ALIVE_INTERVAL,
                TimeUnit.SECONDS);
        lastResponseTime = Instant.now();
    }

    private synchronized void connect() throws UnknownHostException, IOException {
        logger.debug("connecting to: {} {}", hostname, PORT);
        Socket s = new Socket(hostname, PORT);
        socket = s;

        outputStream = s.getOutputStream();
        ArcamConnectionReader acr = new ArcamConnectionReader(s, this);
        acr.setName("OH-binding-" + thingUID);
        acr.setDaemon(true);
        acr.start();
        this.acr = acr;

        socketListener.onConnection();
        connectionState = ArcamConnectionState.CONNECTED;
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
            scheduler.schedule(this::reconnect, 10, TimeUnit.SECONDS);
        }
    }

    // This method should be called from a synchronized method in order to be thread safe
    private void handleConnectionIssue() {
        logger.debug("handleConnectionIssue");
        var x = System.out;
        if (x != null) {
            new Exception().printStackTrace(x);
        }

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

        logger.debug("Sending heartbeat bytes: {}", ArcamUtil.bytesToHex(heartbeatCommand));
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
    public synchronized void onResponse(ArcamResponse response) {
        lastResponseTime = Instant.now();
        socketListener.onResponse(response);
    }

    @Override
    public synchronized void onConnReadError() {
        handleConnectionIssue();
    }
}
