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
package org.openhab.binding.pulseaudio.internal;

import java.io.IOException;
import java.net.Socket;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.pulseaudio.internal.handler.PulseaudioHandler;
import org.openhab.core.library.types.PercentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A connection to a pulseaudio Simple TCP Protocol
 *
 * @author Gwendal Roulleau - Initial contribution
 * @author Miguel Álvarez - Refactor some code from PulseAudioAudioSink here
 *
 */
@NonNullByDefault
public abstract class PulseaudioSimpleProtocolStream {

    private final Logger logger = LoggerFactory.getLogger(PulseaudioSimpleProtocolStream.class);

    protected PulseaudioHandler pulseaudioHandler;
    protected ScheduledExecutorService scheduler;

    protected @Nullable Socket clientSocket;

    private ReentrantLock countClientLock = new ReentrantLock();
    private Integer countClient = 0;

    private @Nullable ScheduledFuture<?> scheduledDisconnection;

    public PulseaudioSimpleProtocolStream(PulseaudioHandler pulseaudioHandler, ScheduledExecutorService scheduler) {
        this.pulseaudioHandler = pulseaudioHandler;
        this.scheduler = scheduler;
    }

    /**
     * Connect to pulseaudio with the simple protocol
     * Will schedule an attempt for disconnection after timeout
     *
     * @throws IOException
     * @throws InterruptedException when interrupted during the loading module wait
     */
    public void connectIfNeeded() throws IOException, InterruptedException {
        Socket clientSocketLocal = clientSocket;
        if (clientSocketLocal == null || !clientSocketLocal.isConnected() || clientSocketLocal.isClosed()) {
            logger.debug("Simple TCP Stream connecting for {}", getLabel(null));
            String host = pulseaudioHandler.getHost();
            int port = pulseaudioHandler.getSimpleTcpPortAndLoadModuleIfNecessary();
            var clientSocketFinal = new Socket(host, port);
            clientSocketFinal.setSoTimeout(pulseaudioHandler.getBasicProtocolSOTimeout());
            clientSocket = clientSocketFinal;
            scheduleDisconnectIfNoClient();
        }
    }

    /**
     * Disconnect the socket to pulseaudio simple protocol
     */
    public void disconnect() {
        final Socket clientSocketLocal = clientSocket;
        if (clientSocketLocal != null) {
            logger.debug("Simple TCP Stream disconnecting for {}", getLabel(null));
            try {
                clientSocketLocal.close();
            } catch (IOException ignored) {
            }
        } else {
            logger.debug("Stream still running or socket not open");
        }
    }

    private void scheduleDisconnectIfNoClient() {
        countClientLock.lock();
        try {
            if (countClient <= 0) {
                var scheduledDisconnectionFinal = scheduledDisconnection;
                if (scheduledDisconnectionFinal != null) {
                    logger.debug("Aborting next disconnect");
                    scheduledDisconnectionFinal.cancel(true);
                }
                int idleTimeout = pulseaudioHandler.getIdleTimeout();
                if (idleTimeout > -1) {
                    if (idleTimeout == 0) {
                        this.disconnect();
                    } else {
                        logger.debug("Scheduling next disconnect");
                        scheduledDisconnection = scheduler.schedule(this::disconnect, idleTimeout,
                                TimeUnit.MILLISECONDS);
                    }
                }
            }
        } finally {
            countClientLock.unlock();
        }
    }

    public PercentType getVolume() {
        return new PercentType(pulseaudioHandler.getLastVolume());
    }

    public void setVolume(PercentType volume) {
        pulseaudioHandler.setVolume(volume.intValue());
    }

    public String getId() {
        return pulseaudioHandler.getThing().getUID().toString();
    }

    public String getLabel(@Nullable Locale locale) {
        var label = pulseaudioHandler.getThing().getLabel();
        return label != null ? label : pulseaudioHandler.getThing().getUID().getId();
    }

    protected void addClientCount() {
        countClientLock.lock();
        try {
            countClient += 1;
            logger.debug("Adding new client for pulseaudio sink/source {}. Current count: {}", getLabel(null),
                    countClient);
            if (countClient <= 0) { // safe against misuse
                countClient = 1;
            }
            var scheduledDisconnectionFinal = scheduledDisconnection;
            if (scheduledDisconnectionFinal != null) {
                logger.debug("Aborting next disconnect");
                scheduledDisconnectionFinal.cancel(true);
            }
        } finally {
            countClientLock.unlock();
        }
    }

    protected void minusClientCount() {
        countClientLock.lock();
        countClient -= 1;
        logger.debug("Removing client for pulseaudio sink/source {}. Current count: {}", getLabel(null), countClient);
        if (countClient < 0) { // safe against misuse
            countClient = 0;
        }
        countClientLock.unlock();
        if (countClient <= 0) {
            scheduleDisconnectIfNoClient();
        }
    }
}
