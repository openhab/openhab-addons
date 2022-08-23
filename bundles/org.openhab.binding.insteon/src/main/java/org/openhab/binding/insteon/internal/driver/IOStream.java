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
package org.openhab.binding.insteon.internal.driver;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.config.InsteonBridgeConfiguration;
import org.openhab.binding.insteon.internal.config.InsteonHubConfiguration;
import org.openhab.binding.insteon.internal.config.InsteonHubLegacyConfiguration;
import org.openhab.binding.insteon.internal.config.InsteonPLMConfiguration;
import org.openhab.binding.insteon.internal.driver.hub.HubIOStream;
import org.openhab.core.io.transport.serial.SerialPortManager;

/**
 * Abstract class for implementation for I/O stream with anything that looks
 * like a PLM (e.g. the insteon hubs, serial/usb connection etc)
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Daniel Pfrommer - openHAB 1 insteonplm binding
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public abstract class IOStream {
    protected @Nullable InputStream in;
    protected @Nullable OutputStream out;

    /**
     * Reads data from IOStream
     *
     * @param b byte array (output)
     * @return number of bytes read
     */
    public int read(byte @Nullable [] b) throws InterruptedException, IOException {
        int len = 0;
        while (len == 0) {
            if (!isOpen()) {
                throw new IOException("io stream not open");
            }

            InputStream in = this.in;
            if (in != null) {
                len = in.read(b);
            } else {
                throw new IOException("input stream not defined");
            }

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
     */
    public void write(byte @Nullable [] b) throws InterruptedException, IOException {
        if (!isOpen()) {
            throw new IOException("io stream not open");
        }

        OutputStream out = this.out;
        if (out != null) {
            out.write(b);
        } else {
            throw new IOException("output stream not defined");
        }
    }

    /**
     * Returns if IOStream is open
     *
     * @return true if stream is open, false if not
     */
    public abstract boolean isOpen();

    /**
     * Opens the IOStream
     *
     * @return true if open was successful, false if not
     */
    public abstract boolean open();

    /**
     * Closes the IOStream
     */
    public abstract void close();

    /**
     * Creates an IOStream from an insteon bridge config object
     *
     * @param config
     * @param serialPortManager
     * @return reference to IOStream
     */

    public static IOStream create(InsteonBridgeConfiguration config, ScheduledExecutorService scheduler,
            @Nullable SerialPortManager serialPortManager) {
        if (config instanceof InsteonHubConfiguration) {
            return makeHubIOStream((InsteonHubConfiguration) config, scheduler);
        } else if (config instanceof InsteonHubLegacyConfiguration) {
            return makeHubLegacyIOStream((InsteonHubLegacyConfiguration) config);
        } else if (config instanceof InsteonPLMConfiguration && serialPortManager != null) {
            return makePLMIOStream((InsteonPLMConfiguration) config, serialPortManager);
        } else {
            throw new UnsupportedOperationException("Unsupported bridge configuration");
        }
    }

    private static HubIOStream makeHubIOStream(InsteonHubConfiguration config, ScheduledExecutorService scheduler) {
        String host = config.getHostname();
        int port = config.getPort();
        String user = config.getUsername();
        String pass = config.getPassword();
        int pollInterval = config.getHubPollInterval();
        return new HubIOStream(host, port, user, pass, pollInterval, scheduler);
    }

    private static TcpIOStream makeHubLegacyIOStream(InsteonHubLegacyConfiguration config) {
        String host = config.getHostname();
        int port = config.getPort();
        return new TcpIOStream(host, port);
    }

    private static SerialIOStream makePLMIOStream(InsteonPLMConfiguration config, SerialPortManager serialPortManager) {
        String name = config.getSerialPort();
        int baudRate = config.getBaudRate();
        return new SerialIOStream(name, baudRate, serialPortManager);
    }
}
