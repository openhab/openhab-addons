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
package org.openhab.binding.insteon.internal.driver;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.driver.hub.HubIOStream;
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
 */
@NonNullByDefault
public abstract class IOStream {
    private static final Logger logger = LoggerFactory.getLogger(IOStream.class);
    protected @Nullable InputStream in = null;
    protected @Nullable OutputStream out = null;
    private volatile boolean stopped = false;

    public void start() {
        stopped = false;
    }

    public void stop() {
        stopped = true;
    }

    /**
     * read data from iostream
     *
     * @param b byte array (output)
     * @param offset offset for placement into byte array
     * @param readSize size to read
     * @return number of bytes read
     */
    public int read(byte[] b, int offset, int readSize) throws InterruptedException, IOException {
        int len = 0;
        while (!stopped && len < 1) {
            InputStream in = this.in;
            if (in != null) {
                len = in.read(b, offset, readSize);
            } else {
                throw new IOException("in is null");
            }
            if (len == -1) {
                throw new EOFException();
            }

            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
        }
        return (len);
    }

    /**
     * Write data to iostream
     *
     * @param b byte array to write
     */
    public void write(byte @Nullable [] b) throws IOException {
        OutputStream out = this.out;
        if (out != null) {
            out.write(b);
        } else {
            throw new IOException("out is null");
        }
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
    public abstract void close();

    /**
     * Creates an IOStream from an allowed config string:
     *
     * /dev/ttyXYZ (serial port like e.g. usb: /dev/ttyUSB0 or alias /dev/insteon)
     *
     * /hub2/user:password@myinsteonhub.mydomain.com:25105,poll_time=1000 (insteon hub2 (2014))
     *
     * /hub/myinsteonhub.mydomain.com:9761
     *
     * /tcp/serialportserver.mydomain.com:port (serial port exposed via tcp, eg. ser2net)
     *
     * @param config
     * @return reference to IOStream
     */

    public static IOStream create(@Nullable SerialPortManager serialPortManager, String config) {
        if (config.startsWith("/hub2/")) {
            return makeHub2014Stream(config);
        } else if (config.startsWith("/hub/") || config.startsWith("/tcp/")) {
            return makeTCPStream(config);
        } else {
            return new SerialIOStream(serialPortManager, config);
        }
    }

    private static HubIOStream makeHub2014Stream(String config) {
        @Nullable
        String user = null;
        @Nullable
        String pass = null;
        int pollTime = 1000; // poll time in milliseconds

        // Get rid of the /hub2/ part and split off options at the end
        String[] parts = config.substring(6).split(",");

        // Parse the first part, the address
        String[] adr = parts[0].split("@");
        String[] hostPort;
        if (adr.length > 1) {
            String[] userPass = adr[0].split(":");
            user = userPass[0];
            pass = userPass[1];
            hostPort = adr[1].split(":");
        } else {
            hostPort = parts[0].split(":");
        }
        HostPort hp = new HostPort(hostPort, 25105);
        // check if additional options are given
        if (parts.length > 1) {
            if (parts[1].trim().startsWith("poll_time")) {
                pollTime = Integer.parseInt(parts[1].split("=")[1].trim());
            }
        }
        return new HubIOStream(hp.host, hp.port, pollTime, user, pass);
    }

    private static TcpIOStream makeTCPStream(String config) {
        // Get rid of the /hub/ part and split off options at the end, if any
        String[] parts = config.substring(5).split(",");
        String[] hostPort = parts[0].split(":");
        HostPort hp = new HostPort(hostPort, 9761);
        return new TcpIOStream(hp.host, hp.port);
    }

    private static class HostPort {
        public String host = "localhost";
        public int port = -1;

        HostPort(String[] hostPort, int defaultPort) {
            port = defaultPort;
            host = hostPort[0];
            try {
                if (hostPort.length > 1) {
                    port = Integer.parseInt(hostPort[1]);
                }
            } catch (NumberFormatException e) {
                logger.warn("bad format for port {} ", hostPort[1], e);
            }
        }
    }
}
