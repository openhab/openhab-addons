/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.transport;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.insteon.internal.config.InsteonBridgeConfiguration;
import org.openhab.binding.insteon.internal.transport.message.Msg;
import org.openhab.binding.insteon.internal.transport.stream.IOStream;
import org.openhab.binding.insteon.internal.transport.stream.IOStreamListener;
import org.openhab.binding.insteon.internal.transport.stream.IOStreamReader;
import org.openhab.binding.insteon.internal.transport.stream.IOStreamWriter;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Port class represents a port, that is a connection to either an Insteon modem either through
 * a serial or USB port, or via an Insteon Hub.
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Daniel Pfrommer - openHAB 1 insteonplm binding
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class Port implements IOStreamListener {
    private final Logger logger = LoggerFactory.getLogger(Port.class);

    private final String name;
    private final ScheduledExecutorService scheduler;
    private final IOStream stream;
    private final IOStreamReader reader;
    private final IOStreamWriter writer;
    private @Nullable ScheduledFuture<?> readJob;
    private @Nullable ScheduledFuture<?> writeJob;
    private final Set<PortListener> listeners = new CopyOnWriteArraySet<>();
    private final AtomicBoolean connected = new AtomicBoolean(false);

    public Port(InsteonBridgeConfiguration config, HttpClient httpClient, ScheduledExecutorService scheduler,
            SerialPortManager serialPortManager) {
        this.name = config.getId();
        this.scheduler = scheduler;
        this.stream = IOStream.create(config, httpClient, scheduler, serialPortManager);
        this.reader = new IOStreamReader(stream, this);
        this.writer = new IOStreamWriter(stream, this);
    }

    public String getName() {
        return name;
    }

    public void registerListener(PortListener listener) {
        if (listeners.add(listener)) {
            logger.trace("added listener for {}", listener.getClass().getSimpleName());
        }
    }

    public void unregisterListener(PortListener listener) {
        if (listeners.remove(listener)) {
            logger.trace("removed listener for {}", listener.getClass().getSimpleName());
        }
    }

    /**
     * Starts threads necessary for reading and writing
     *
     * @return true if port is connected, otherwise false
     */
    public boolean start() {
        if (connected.get()) {
            logger.debug("port {} already connected, no need to start it", name);
            return true;
        }

        logger.debug("starting port {}", name);

        writer.clearQueue();

        if (!stream.open()) {
            logger.debug("failed to open port {}", name);
            return false;
        }

        readJob = scheduler.schedule(reader, 0, TimeUnit.SECONDS);
        writeJob = scheduler.schedule(writer, 0, TimeUnit.SECONDS);

        connected.set(true);

        logger.trace("all threads for port {} started.", name);

        return true;
    }

    /**
     * Stops all threads
     */
    public void stop() {
        logger.debug("stopping port {}", name);

        connected.set(false);

        stream.close();

        ScheduledFuture<?> readJob = this.readJob;
        if (readJob != null) {
            readJob.cancel(true);
            this.readJob = null;
        }

        ScheduledFuture<?> writeJob = this.writeJob;
        if (writeJob != null) {
            writeJob.cancel(true);
            this.writeJob = null;
        }

        logger.trace("all threads for port {} stopped.", name);
    }

    /**
     * Adds message to the write queue
     *
     * @param msg message to be added to the write queue
     */
    public void writeMessage(Msg msg) {
        try {
            writer.addMessage(msg);
            logger.trace("enqueued msg ({}): {}", writer.getQueueSize(), msg);
        } catch (IllegalStateException e) {
            logger.debug("cannot write message {}, write queue is full!", msg);
        }
    }

    /**
     * Notifies that the io stream has disconnected
     */
    @Override
    public void disconnected() {
        if (connected.getAndSet(false)) {
            logger.warn("port {} disconnected", name);
            listeners.forEach(PortListener::disconnected);
        }
    }

    /**
     * Notifies that the io stream has received a message
     *
     * @param msg the message received if valid, otherwise null
     */
    @Override
    public void messageReceived(Msg msg) {
        logger.debug("got msg: {}", msg);
        listeners.forEach(listener -> listener.messageReceived(msg));
        writer.messageReceived(msg);
    }

    /**
     * Notifies that the io stream has received bad data
     */
    @Override
    public void invalidMessageReceived() {
        logger.debug("got bad data back");
        writer.invalidMessageReceived();
    }

    /**
     * Notifies that the io stream has sent a message
     *
     * @param msg the message sent
     */
    @Override
    public void messageSent(Msg msg) {
        logger.debug("sent msg: {}", msg);
        listeners.forEach(listener -> listener.messageSent(msg));
    }
}
