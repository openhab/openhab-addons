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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.message.Msg;
import org.openhab.binding.insteon.internal.message.MsgListener;
import org.openhab.core.io.transport.serial.SerialPortManager;

/**
 * The driver class manages the modem port.
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public class Driver {
    private Port port;
    private String portName;
    private DriverListener listener;
    private Map<InsteonAddress, ModemDBEntry> modemDBEntries = new HashMap<>();
    private ReentrantLock modemDBEntriesLock = new ReentrantLock();

    public Driver(String portName, DriverListener listener, @Nullable SerialPortManager serialPortManager,
            ScheduledExecutorService scheduler) {
        this.listener = listener;
        this.portName = portName;

        port = new Port(portName, this, serialPortManager, scheduler);
    }

    public boolean isReady() {
        return port.isRunning();
    }

    public Map<InsteonAddress, ModemDBEntry> lockModemDBEntries() {
        modemDBEntriesLock.lock();
        return modemDBEntries;
    }

    public void unlockModemDBEntries() {
        modemDBEntriesLock.unlock();
    }

    public void addMsgListener(MsgListener listener) {
        port.addListener(listener);
    }

    public void removeListener(MsgListener listener) {
        port.removeListener(listener);
    }

    public void start() {
        port.start();
    }

    public void stop() {
        port.stop();
    }

    public void writeMessage(Msg m) throws IOException {
        port.writeMessage(m);
    }

    public String getPortName() {
        return portName;
    }

    public boolean isRunning() {
        return port.isRunning();
    }

    public boolean isMsgForUs(@Nullable InsteonAddress toAddr) {
        return port.getAddress().equals(toAddr);
    }

    public void modemDBComplete(Port port) {
        if (isModemDBComplete()) {
            listener.driverCompletelyInitialized();
        }
    }

    public boolean isModemDBComplete() {
        return port.isModemDBComplete();
    }

    public void disconnected() {
        listener.disconnected();
    }
}
