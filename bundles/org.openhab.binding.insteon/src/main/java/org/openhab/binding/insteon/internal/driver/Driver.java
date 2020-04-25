/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.message.Msg;
import org.openhab.binding.insteon.internal.message.MsgListener;
import org.openhab.binding.insteon.internal.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The driver class manages the modem ports.
 * XXX: at this time, only a single modem has ever been used. Expect
 * the worst if you connect multiple modems. When multiple modems
 * are required, this code needs to be tested and fixed.
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
@SuppressWarnings("null")
public class Driver {
    private final Logger logger = LoggerFactory.getLogger(Driver.class);

    // maps device name to serial port, i.e /dev/insteon -> Port object
    private Map<String, Port> ports = new HashMap<>();
    private @Nullable DriverListener listener = null; // single listener for notifications
    private Map<InsteonAddress, @Nullable ModemDBEntry> modemDBEntries = new HashMap<>();
    private ReentrantLock modemDBEntriesLock = new ReentrantLock();
    private int modemDBRetryTimeout = 120000; // in milliseconds

    public void setDriverListener(DriverListener listener) {
        this.listener = listener;
    }

    public void setModemDBRetryTimeout(int timeout) {
        modemDBRetryTimeout = timeout;
        for (Port p : ports.values()) {
            p.setModemDBRetryTimeout(modemDBRetryTimeout);
        }
    }

    public boolean isReady() {
        for (Port p : ports.values()) {
            if (!p.isRunning()) {
                return false;
            }
        }
        return true;
    }

    public Map<InsteonAddress, @Nullable ModemDBEntry> lockModemDBEntries() {
        modemDBEntriesLock.lock();
        return modemDBEntries;
    }

    public void unlockModemDBEntries() {
        modemDBEntriesLock.unlock();
    }

    /**
     * Add new port (modem) to the driver
     *
     * @param name the name of the port (from the config file, e.g. port_0, port_1, etc
     * @param port the device name, e.g. /dev/insteon, /dev/ttyUSB0 etc
     */
    public void addPort(String name, String port, @Nullable SerialPortManager serialPortManager) {
        if (ports.keySet().contains(port)) {
            logger.warn("ignored attempt to add duplicate port: {} {}", name, port);
        } else {
            Port p = new Port(port, this, serialPortManager);
            p.setModemDBRetryTimeout(modemDBRetryTimeout);
            ports.put(port, p);
            logger.debug("added new port: {} {}", name, Utils.redactPassword(port));
        }
    }

    /**
     * Register a message listener with a port
     *
     * @param listener the listener who wants to listen to port messages
     * @param port the port (e.g. /dev/ttyUSB0) to which the listener listens
     */
    public void addMsgListener(MsgListener listener, String port) {
        if (ports.keySet().contains(port)) {
            ports.get(port).addListener(listener);
        } else {
            logger.warn("referencing unknown port {}!", port);
        }
    }

    public void startAllPorts() {
        for (Port p : ports.values()) {
            p.start();
        }
    }

    public void stopAllPorts() {
        for (Port p : ports.values()) {
            p.stop();
        }
    }

    /**
     * Write message to a port
     *
     * @param port name of the port to write to (e.g. '/dev/ttyUSB0')
     * @param m the message to write
     * @throws IOException
     */
    public void writeMessage(String port, Msg m) throws IOException {
        Port p = getPort(port);
        if (p == null) {
            logger.warn("cannot write to unknown port {}", port);
            throw new IOException();
        }
        p.writeMessage(m);
    }

    public @Nullable String getDefaultPort() {
        return (ports.isEmpty() ? null : ports.keySet().iterator().next());
    }

    public int getNumberOfPorts() {
        int n = 0;
        for (Port p : ports.values()) {
            if (p.isRunning()) {
                n++;
            }
        }
        return n;
    }

    public boolean isMsgForUs(@Nullable InsteonAddress toAddr) {
        if (toAddr == null) {
            return false;
        }
        for (Port p : ports.values()) {
            if (p.getAddress().equals(toAddr)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get port object corresponding to device
     *
     * @param port device name of port (e.g. /dev/ttyUSB0)
     * @return corresponding Port object or null if not found
     */
    public @Nullable Port getPort(String port) {
        if (port.equalsIgnoreCase("DEFAULT")) {
            if (ports.isEmpty()) {
                logger.warn("no default port found!");
                return null;
            }
            return ports.values().iterator().next();
        }
        if (!ports.containsKey(port)) {
            logger.warn("no port of name {} found!", port);
            return null;
        }
        return ports.get(port);
    }

    public void modemDBComplete(Port port) {
        // check if all ports have a complete device list
        if (!isModemDBComplete()) {
            return;
        }
        // if yes, notify listener
        listener.driverCompletelyInitialized();
    }

    public boolean isModemDBComplete() {
        // check if all ports have a complete device list
        for (Port p : ports.values()) {
            if (!p.isModemDBComplete()) {
                return false;
            }
        }
        return true;
    }
}
