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
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.config.InsteonBridgeConfiguration;
import org.openhab.binding.insteon.internal.device.DeviceLinker;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.device.InsteonDevice;
import org.openhab.binding.insteon.internal.device.Modem;
import org.openhab.binding.insteon.internal.device.database.LinkDBBuilder;
import org.openhab.binding.insteon.internal.device.database.ModemDB;
import org.openhab.binding.insteon.internal.device.database.ModemDBBuilder;
import org.openhab.binding.insteon.internal.message.FieldException;
import org.openhab.binding.insteon.internal.message.Msg;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The driver class manages the modem port.
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public class Driver implements PortListener {
    private final Logger logger = LoggerFactory.getLogger(Driver.class);

    private DriverListener listener;
    private Port port;
    private Modem modem;
    private LinkDBBuilder ldbb;
    private ModemDBBuilder mdbb;
    private DeviceLinker linker;
    private Poller poller;
    private RequestQueueManager requestQueue;
    private int x10HouseUnit = -1;

    public Driver(DriverListener listener, InsteonBridgeConfiguration config, ScheduledExecutorService scheduler,
            @Nullable SerialPortManager serialPortManager) {
        this.listener = listener;
        this.port = new Port(config, scheduler, serialPortManager);
        this.modem = new Modem(this);
        this.ldbb = new LinkDBBuilder(this, scheduler);
        this.mdbb = new ModemDBBuilder(this, scheduler);
        this.linker = new DeviceLinker(this, scheduler);
        this.poller = new Poller(config.getId());
        this.requestQueue = new RequestQueueManager(config.getId());
    }

    public DriverListener getListener() {
        return listener;
    }

    public String getName() {
        return port.getName();
    }

    public InsteonAddress getModemAddress() {
        return modem.getAddress();
    }

    public @Nullable InsteonDevice getModemDevice() {
        return modem.getDevice();
    }

    public ModemDB getModemDB() {
        return modem.getDB();
    }

    public boolean isMsgForUs(InsteonAddress toAddr) {
        return modem.getAddress().equals(toAddr);
    }

    public boolean isModemDBComplete() {
        return modem.getDB().isComplete();
    }

    public Poller getPoller() {
        return poller;
    }

    public RequestQueueManager getRequestQueueManager() {
        return requestQueue;
    }

    public void addPortListener(PortListener listener) {
        port.addListener(listener);
    }

    public void removePortListener(PortListener listener) {
        port.removeListener(listener);
    }

    public void writeMessage(Msg msg) throws IOException {
        port.writeMessage(msg);
    }

    public boolean start() {
        if (!port.start()) {
            return false;
        }

        modem.initialize();

        poller.start();
        requestQueue.start();

        return true;
    }

    public void stop() {
        if (ldbb.isRunning()) {
            ldbb.stop();
        }

        if (mdbb.isRunning()) {
            mdbb.stop();
        }

        if (linker.isRunning()) {
            linker.stop();
        }

        port.stop();
        requestQueue.stop();
        poller.stop();
    }

    public void reconnect() {
        port.stop();
        port.start();
    }

    /**
     * Builds the link db for a given device
     *
     * @param device device link db to build
     * @param delay downloading delay (in milliseconds)
     */
    public void buildLinkDB(InsteonDevice device, long delay) {
        if (ldbb.isRunning()) {
            if (logger.isDebugEnabled()) {
                logger.debug("link db builder is already running for {}", ldbb.getDevice().getAddress());
            }
        } else {
            ldbb.start(device, delay);
        }
    }

    /**
     * Builds the modem db
     */
    public void buildModemDB() {
        if (mdbb.isRunning()) {
            if (logger.isDebugEnabled()) {
                logger.debug("modem db builder is already running");
            }
        } else {
            mdbb.start();
        }
    }

    /**
     * Links a device
     *
     * @param address the device address to link
     */
    public void linkDevice(@Nullable InsteonAddress address) {
        if (linker.isRunning()) {
            if (logger.isDebugEnabled()) {
                logger.debug("device linker is already running");
            }
        } else {
            linker.link(address);
        }
    }

    /**
     * Unlinks a device
     *
     * @param address the device address to unlink
     */
    public void unlinkDevice(InsteonAddress address) {
        if (linker.isRunning()) {
            if (logger.isDebugEnabled()) {
                logger.debug("device linker is already running");
            }
        } else {
            linker.unlink(address);
        }
    }

    /**
     * Notifies that the modem database has completed
     */
    public void modemDBCompleted() {
        port.addListener(this);
        modem.startPolling();
        listener.modemDBCompleted();
    }

    /**
     * Notifies that the port has disconnected
     */
    @Override
    public void disconnected() {
        listener.disconnected();
    }

    /**
     * Notifies that the port has received a message
     *
     * @param msg the message received
     */
    @Override
    public void messageReceived(Msg msg) {
        if (msg.isPureNack()) {
            return;
        }
        if (msg.isX10()) {
            handleX10Message(msg);
        } else if (msg.isInsteonMessage()) {
            handleInsteonMessage(msg);
        } else {
            handleIMMessage(msg);
        }
    }

    /**
     * Notifies that the port has sent a message
     *
     * @param msg the message sent
     */
    @Override
    public void messageSent(Msg msg) {
        if (msg.isBroadcast()) {
            return;
        }
        InsteonAddress address = msg.getAddressOrNull("toAddress");
        long time = System.currentTimeMillis();
        if (address != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("request sent to {}", address);
            }
            listener.requestSent(address, time);
        } else {
            if (logger.isTraceEnabled()) {
                logger.trace("im request sent");
            }
            listener.imRequestSent(time);
        }
    }

    private void handleIMMessage(Msg msg) {
        listener.imMessageReceived(msg);
    }

    private void handleInsteonMessage(Msg msg) {
        try {
            if (msg.isReply()) {
                return;
            }
            InsteonAddress toAddr = msg.getAddress("toAddress");
            if (msg.isBroadcast() || isMsgForUs(toAddr)) {
                InsteonAddress fromAddr = msg.getAddress("fromAddress");
                listener.messageReceived(fromAddr, msg);
            }
        } catch (FieldException e) {
            logger.warn("got a bad Insteon message: {}", msg, e);
        }
    }

    private void handleX10Message(Msg msg) {
        try {
            if (msg.isReply()) {
                return;
            }
            int x10Flag = msg.getInt("X10Flag");
            int rawX10 = msg.getInt("rawX10");
            if (x10Flag == 0x80) { // actual command
                if (x10HouseUnit != -1) {
                    InsteonAddress fromAddr = new InsteonAddress((byte) x10HouseUnit);
                    listener.messageReceived(fromAddr, msg);
                }
            } else if (x10Flag == 0) {
                // what unit the next cmd will apply to
                x10HouseUnit = rawX10 & 0xFF;
            }
        } catch (FieldException e) {
            logger.warn("got a bad X10 message: {}", msg, e);
        }
    }
}
