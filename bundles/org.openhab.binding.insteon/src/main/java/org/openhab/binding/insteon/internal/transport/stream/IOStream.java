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
package org.openhab.binding.insteon.internal.transport.stream;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.insteon.internal.config.InsteonBridgeConfiguration;
import org.openhab.binding.insteon.internal.config.InsteonHub1Configuration;
import org.openhab.binding.insteon.internal.config.InsteonHub2Configuration;
import org.openhab.binding.insteon.internal.config.InsteonPLMConfiguration;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class for implementation for I/O stream with anything that looks
 * like a PLM (e.g. the insteon hubs, serial/usb connection etc)
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Daniel Pfrommer - openHAB 1 insteonplm binding
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public abstract class IOStream {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected @Nullable InputStream in;
    protected @Nullable OutputStream out;
    private final int rateLimitTime;

    protected IOStream(int rateLimitTime) {
        this.rateLimitTime = rateLimitTime;
    }

    public int getRateLimitTime() {
        return rateLimitTime;
    }

    /**
     * Reads data from IOStream
     *
     * @param b byte array (output)
     * @return number of bytes read
     * @throws InterruptedException
     * @throws IOException
     */
    public int read(byte @Nullable [] b) throws InterruptedException, IOException {
        InputStream in = this.in;
        if (in == null) {
            throw new IOException("input stream not defined");
        }
        int len = 0;
        while (len == 0) {
            len = in.read(b);

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }

            if (len == -1) {
                throw new EOFException();
            }
        }
        return len;
    }

    /**
     * Writes data to IOStream
     *
     * @param b byte array to write
     * @throws IOException
     */
    public void write(byte @Nullable [] b) throws IOException {
        OutputStream out = this.out;
        if (out == null) {
            throw new IOException("output stream not defined");
        }
        out.write(b);
    }

    /**
     * Opens the IOStream
     *
     * @return true if open was successful, false if not
     */
    public abstract boolean open();

    /**
     * Closes the IOStream
     */
    public void close() {
        InputStream in = this.in;
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                logger.debug("failed to close input stream", e);
            }
            this.in = null;
        }

        OutputStream out = this.out;
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                logger.debug("failed to close output stream", e);
            }
            this.out = null;
        }
    }

    /**
     * Creates an IOStream from an insteon bridge config object
     *
     * @param config
     * @param httpClient
     * @param scheduler
     * @param serialPortManager
     * @return reference to IOStream
     */
    public static IOStream create(InsteonBridgeConfiguration config, HttpClient httpClient,
            ScheduledExecutorService scheduler, SerialPortManager serialPortManager) {
        if (config instanceof InsteonHub1Configuration hub1Config) {
            return makeTcpIOStream(hub1Config);
        } else if (config instanceof InsteonHub2Configuration hub2Config) {
            return makeHubIOStream(hub2Config, httpClient, scheduler);
        } else if (config instanceof InsteonPLMConfiguration plmConfig) {
            return makeSerialIOStream(plmConfig, serialPortManager);
        } else {
            throw new UnsupportedOperationException("unsupported bridge configuration");
        }
    }

    private static HubIOStream makeHubIOStream(InsteonHub2Configuration config, HttpClient httpClient,
            ScheduledExecutorService scheduler) {
        String host = config.getHostname();
        int port = config.getPort();
        String user = config.getUsername();
        String pass = config.getPassword();
        int pollInterval = config.getHubPollInterval();
        return new HubIOStream(host, port, user, pass, pollInterval, httpClient, scheduler);
    }

    private static SerialIOStream makeSerialIOStream(InsteonPLMConfiguration config,
            SerialPortManager serialPortManager) {
        String name = config.getSerialPort();
        int baudRate = config.getBaudRate();
        return new SerialIOStream(name, baudRate, serialPortManager);
    }

    private static TcpIOStream makeTcpIOStream(InsteonHub1Configuration config) {
        String host = config.getHostname();
        int port = config.getPort();
        return new TcpIOStream(host, port);
    }
}
