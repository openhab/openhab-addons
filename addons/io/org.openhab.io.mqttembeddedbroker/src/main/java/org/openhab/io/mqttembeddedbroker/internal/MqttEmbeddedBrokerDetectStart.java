/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.mqttembeddedbroker.internal;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.moquette.server.Server;

/**
 * Unfortunately there is no listener interface for the Moquette MQTT Broker
 * to get notified when it is started and ready to accept connections.
 * We therefore try to connect to the socket with a Socket object until a timeout is reached.
 *
 * @author David Graeff - Inital contriution
 */
@NonNullByDefault
public class MqttEmbeddedBrokerDetectStart {
    protected @Nullable Server server;
    protected final MqttEmbeddedBrokerStartedListener startedListener;
    protected long startTime;
    protected int port;
    protected int timeout = 2000;
    protected @Nullable ScheduledExecutorService scheduler;
    protected @Nullable ScheduledFuture<?> schedule;

    /**
     * Implement this interface to be notified if a connection to the given tcp port can be established.
     */
    public static interface MqttEmbeddedBrokerStartedListener {
        public void mqttEmbeddedBrokerStarted(boolean timeout);
    }

    /**
     * Registers the given listener. Start with {@link #startBrokerStartedDetection(int, ScheduledExecutorService)}.
     *
     * @param startedListener A listener
     */
    public MqttEmbeddedBrokerDetectStart(MqttEmbeddedBrokerStartedListener startedListener) {
        this.startedListener = startedListener;
    }

    /**
     * Performs a tcp socket open/close process. Will notify the registered listener on success
     * and retry until a timeout is reached otherwise.
     */
    protected void servicePing() {
        ScheduledExecutorService scheduler = this.scheduler;
        if (scheduler == null) {
            return;
        }

        try {
            SocketAddress socketAddress = new InetSocketAddress("127.0.0.1", port);
            Socket socket = new Socket();
            socket.connect(socketAddress, 500);
            socket.close();
            schedule = null;
            startedListener.mqttEmbeddedBrokerStarted(false);
            return;
        } catch (IOException ignored) {
        }
        if (System.currentTimeMillis() - startTime < timeout) {
            schedule = scheduler.schedule(() -> servicePing(), 100, TimeUnit.MILLISECONDS);
        } else {
            startedListener.mqttEmbeddedBrokerStarted(true);
        }
    }

    /**
     * Start the broker server reachable detection
     *
     * @param port The Mqtt Server port
     * @param scheduler A scheduler
     */
    public void startBrokerStartedDetection(int port, ScheduledExecutorService scheduler) {
        this.port = port;
        this.scheduler = scheduler;
        this.startTime = System.currentTimeMillis();
        this.schedule = null;
        servicePing();
    }

    /**
     * Stops the broker server reachable detection if it is still running.
     */
    public void stopBrokerStartDetection() {
        if (schedule != null) {
            schedule.cancel(true);
            schedule = null;
        }
    }
}
