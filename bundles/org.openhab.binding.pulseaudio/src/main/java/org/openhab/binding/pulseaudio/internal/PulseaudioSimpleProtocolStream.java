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
package org.openhab.binding.pulseaudio.internal;

import java.io.IOException;
import java.net.Socket;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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

    private boolean isIdle = true;

    private @Nullable ScheduledFuture<?> scheduledDisconnection;

    public PulseaudioSimpleProtocolStream(PulseaudioHandler pulseaudioHandler, ScheduledExecutorService scheduler) {
        this.pulseaudioHandler = pulseaudioHandler;
        this.scheduler = scheduler;
    }

    /**
     * Connect to pulseaudio with the simple protocol
     *
     * @throws IOException
     * @throws InterruptedException when interrupted during the loading module wait
     */
    public void connectIfNeeded() throws IOException, InterruptedException {
        Socket clientSocketLocal = clientSocket;
        if (clientSocketLocal == null || !clientSocketLocal.isConnected() || clientSocketLocal.isClosed()) {
            logger.debug("Simple TCP Stream connecting");
            String host = pulseaudioHandler.getHost();
            int port = pulseaudioHandler.getSimpleTcpPortAndLoadModuleIfNecessary();
            var clientSocketFinal = new Socket(host, port);
            clientSocketFinal.setSoTimeout(pulseaudioHandler.getBasicProtocolSOTimeout());
            clientSocket = clientSocketFinal;
        }
    }

    /**
     * Disconnect the socket to pulseaudio simple protocol
     */
    public void disconnect() {
        final Socket clientSocketLocal = clientSocket;
        if (clientSocketLocal != null && isIdle) {
            logger.debug("Simple TCP Stream disconnecting");
            try {
                clientSocketLocal.close();
            } catch (IOException ignored) {
            }
        } else {
            logger.debug("Stream still running or socket not open");
        }
    }

    public void scheduleDisconnect() {
        var scheduledDisconnectionFinal = scheduledDisconnection;
        if (scheduledDisconnectionFinal != null) {
            scheduledDisconnectionFinal.cancel(true);
        }
        int idleTimeout = pulseaudioHandler.getIdleTimeout();
        if (idleTimeout > -1) {
            logger.debug("Scheduling disconnect");
            scheduledDisconnection = scheduler.schedule(this::disconnect, idleTimeout, TimeUnit.MILLISECONDS);
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

    public void setIdle(boolean idle) {
        isIdle = idle;
    }
}
