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
package org.openhab.binding.alarmdecoder.internal.handler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.alarmdecoder.internal.config.IPBridgeConfig;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler responsible for communicating via TCP with the Nu Tech Alarm Decoder device.
 * Based on and including code from the original OH1 alarmdecoder binding.
 *
 * @author Bernd Pfrommer - Initial contribution (OH1 version)
 * @author Bob Adair - Re-factored into OH2 binding
 */
@NonNullByDefault
public class IPBridgeHandler extends ADBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(IPBridgeHandler.class);

    private IPBridgeConfig config = new IPBridgeConfig();

    private @Nullable Socket socket = null;

    public IPBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing IP bridge handler");
        config = getConfigAs(IPBridgeConfig.class);
        discovery = config.discovery;

        if (config.hostname == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "hostname not configured");
            return;
        }
        if (config.tcpPort <= 0 || config.tcpPort > 65535) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "invalid port number configured");
            return;
        }

        // set the thing status to UNKNOWN temporarily and let the background connect task decide the real status.
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.submit(this::connect); // start the async connect task
    }

    @Override
    protected synchronized void connect() {
        disconnect(); // make sure we are disconnected
        writeException = false;
        try {
            Socket socket = new Socket(config.hostname, config.tcpPort);
            this.socket = socket;
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), AD_CHARSET));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), AD_CHARSET));
            logger.debug("connected to {}:{}", config.hostname, config.tcpPort);
            panelReadyReceived = false;
            startMsgReader();
            updateStatus(ThingStatus.ONLINE);

            // Start connection check job
            logger.debug("Scheduling connection check job with interval {} minutes.", config.reconnect);
            lastReceivedTime = new Date();
            connectionCheckJob = scheduler.scheduleWithFixedDelay(this::connectionCheck, config.reconnect,
                    config.reconnect, TimeUnit.MINUTES);
        } catch (UnknownHostException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "unknown host");
            disconnect();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            disconnect();
            scheduleConnectRetry(config.reconnect); // Possibly a retryable error. Try again later.
        }
    }

    protected synchronized void connectionCheck() {
        logger.trace("Connection check job running");

        Thread mrThread = msgReaderThread;
        if (mrThread != null && !mrThread.isAlive()) {
            logger.debug("Reader thread has exited abnormally. Restarting.");
            scheduler.submit(this::connect);
        } else if (writeException) {
            logger.debug("Write exception encountered. Resetting connection.");
            scheduler.submit(this::connect);
        } else {
            Date now = new Date();
            Date last = lastReceivedTime;
            if (last != null && config.timeout > 0
                    && ((last.getTime() + (config.timeout * 60 * 1000)) < now.getTime())) {
                logger.warn("Last valid message received at {}. Resetting connection.", last);
                scheduler.submit(this::connect);
            }
        }
    }

    @Override
    protected synchronized void disconnect() {
        logger.trace("Disconnecting");
        // stop scheduled connection check and retry jobs
        ScheduledFuture<?> crJob = connectRetryJob;
        if (crJob != null) {
            // use cancel(false) so we don't kill ourselves when connect retry job calls disconnect()
            crJob.cancel(false);
            connectRetryJob = null;
        }
        ScheduledFuture<?> ccJob = connectionCheckJob;
        if (ccJob != null) {
            // use cancel(false) so we don't kill ourselves when reconnect job calls disconnect()
            ccJob.cancel(false);
            connectionCheckJob = null;
        }

        // Must close the socket first so the message reader thread will exit properly.
        // The BufferedReader.readLine() call used in readerThread() is not interruptable.
        Socket s = socket;
        if (s != null) {
            try {
                s.close();
            } catch (IOException e) {
                logger.debug("error closing socket: {}", e.getMessage());
            }
        }
        socket = null;

        stopMsgReader();

        try {
            BufferedWriter bw = writer;
            if (bw != null) {
                bw.close();
            }
            BufferedReader br = reader;
            if (br != null) {
                br.close();
            }
        } catch (IOException e) {
            logger.debug("error closing reader/writer: {}", e.getMessage());
        }
        writer = null;
        reader = null;
    }
}
