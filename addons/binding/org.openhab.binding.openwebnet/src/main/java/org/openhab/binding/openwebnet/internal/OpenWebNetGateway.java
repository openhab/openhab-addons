/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openwebnet.internal;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.openwebnet.internal.discovery.OpenWebNetDevice;
import org.openhab.binding.openwebnet.internal.exception.PortNotConnected;
import org.openhab.binding.openwebnet.internal.listener.ResponseListener;
import org.openhab.binding.openwebnet.internal.listener.ScanListener;
import org.openhab.binding.openwebnet.internal.listener.ThingStatusListener;
import org.openhab.binding.openwebnet.internal.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetGateway} class is the interface to the OpenWebNet bridge.
 *
 * @author Antoine Laydier
 *
 */
public class OpenWebNetGateway implements ResponseListener, AutoCloseable {

    private static final String KEEP_CONNECT = "*13*60*##";
    private static final String SUPERVISOR_MODE_ON = "*13*66*##";
    private static final String SUPERVISOR_MODE_OFF = "*13*67*##";
    private static final String GET_FIRMWARE_VERSION = "*#13*<where>*16##";
    private static final String GET_HARDWARE_VERSION = "*#13*<where>*17##";
    private static final String SCAN_NETWORK = "*13*65*##";
    private static final String GET_PRODUCT_INFO = "*#13**66*<index>##";
    private static final String SET_LIGHT = "*1*<value>*<where>#9##";
    private static final String GET_LIGHT = "*#1*<where>#9##";
    private static final String SET_AUTOMATION = "*2*<what>*<where>#9##";
    private static final String GET_AUTOMATION = "*#2*<where>#9*10##";
    private static final String MOVE_AUTOMATION = "*#2*<where>#9*#11*<level>##";

    private enum GatewayType {
        ZIGBEE_GATEWAY,
        SCS_GATEWAY,
        UNDEFINED
    };

    private enum Reply {
        ACK,
        NACK,
        BUSY_NACK,
        UNDEFINED
    }

    @SuppressWarnings("null")
    private final @NonNull Logger logger = LoggerFactory.getLogger(OpenWebNetGateway.class);

    @SuppressWarnings("null")
    // command Executor is used to ensure that all commands are only send in a sequential way.
    private final @NonNull ScheduledExecutorService commandExecutor = Executors.newSingleThreadScheduledExecutor();

    private @NonNull GatewayType type;
    private @Nullable InternalGateway gateway;
    private @NonNull Parser parser;
    private @NonNull ResponseHolder<Reply> response;
    private @NonNull ResponseHolder<Integer> scanResponse;
    private boolean ready;
    private @Nullable ResponseListener internalListener;
    private @NonNull String gatewayFirmwareVersion;
    private @NonNull String gatewayHardwareVersion;
    // REQUEST for light status doesn't send ACK, this is a workaround
    private int unlockWhereWorkAround;

    private @NonNull Map<Integer, ThingStatusListener> listeners;

    /**
     * Constructor
     */
    public OpenWebNetGateway() {
        this.type = GatewayType.UNDEFINED;
        this.gateway = null;
        this.parser = new Parser(this);
        this.response = new ResponseHolder<>(Reply.UNDEFINED);
        this.scanResponse = new ResponseHolder<>(0);
        this.ready = false;
        this.internalListener = null;
        this.gatewayFirmwareVersion = "Unknown";
        this.gatewayHardwareVersion = "Unknown";
        this.listeners = new HashMap<>();
        this.unlockWhereWorkAround = -1;
    }

    /**
     * Define the gateway as a Serial interface bridge
     *
     * @param serialPort serial port to use
     */
    @SuppressWarnings("null")
    public boolean defineAsZigbee(@NonNull String serialPort) {
        logger.debug("Gateway is now set as Serial {}", serialPort);
        this.type = GatewayType.ZIGBEE_GATEWAY;
        try {
            this.gateway = new SerialGateway(this.parser, serialPort);
            this.gateway.connect();
        } catch (Exception e) {
            logger.warn("Something gone wrong during connection");
            this.ready = false;
            return this.ready;
        }
        logger.debug("Checking connection...");
        Reply r = checkConnection();
        if (r != Reply.ACK) {
            // if not connected no interest to continue
            logger.debug("Check connection failed - May be not a correct interface");
            this.ready = false;
            return this.ready;
        }

        r = setSupervisorMode(true, true);
        if (r != Reply.ACK) {
            logger.info("Supervisor mode rejected. Update of Thing will not be available");
        } else {
            logger.debug("Supervisor mode on");
        }
        this.ready = true;
        return this.ready;
    }

    /**
     * Test connection to gateway.
     *
     * @return received reply
     */
    public @NonNull Reply checkConnection() {
        return writeSimpleMessage(KEEP_CONNECT, true, 0);
    }

    /**
     * Retrieve the firmware version of the gateway.
     *
     * @return the firmware version
     */
    // Needed for Maven
    @SuppressWarnings("null")
    public @NonNull String getFirmwareVersion() {
        writeSimpleMessage(GET_FIRMWARE_VERSION.replace("<where>", ""), true, 0);
        return gatewayFirmwareVersion;
    }

    /**
     * Retrieve the hardware version of the gateway.
     *
     * @return the hardware version
     */
    // Needed for Maven
    @SuppressWarnings("null")
    public @NonNull String getHardwareVersion() {
        writeSimpleMessage(GET_HARDWARE_VERSION.replace("<where>", ""), true, 0);
        return gatewayHardwareVersion;
    }

    public void setAutomation(int where, int what, boolean waitCompletion) {
        writeSimpleMessage(
                SET_AUTOMATION.replace("<where>", String.valueOf(where)).replace("<what>", String.valueOf(what)),
                waitCompletion, 0);
    }

    public void setPositionAutomation(int where, int percent, boolean waitCompletion) {
        writeSimpleMessage(
                MOVE_AUTOMATION.replace("<where>", String.valueOf(where)).replace("<level>", String.valueOf(percent)),
                waitCompletion, 0);
    }

    public void getAutomation(int where, boolean waitCompletion) {
        writeSimpleMessage(GET_AUTOMATION.replace("<where>", String.valueOf(where)), waitCompletion, 0);
    }

    public void setLight(int where, int state, boolean waitCompletion, int waitInMilliSec) {
        if (state == LightState.ERROR) {
            // invalid value
            logger.debug("Invalid light state requested on {}", where);
        } else if (state == LightState.REFRESH) {
            // Refresh state
            writeSimpleMessage(GET_LIGHT.replace("<where>", String.valueOf(where)), waitCompletion, waitInMilliSec);
        } else {
            writeSimpleMessage(
                    SET_LIGHT.replace("<where>", String.valueOf(where)).replace("<value>", String.valueOf(state)),
                    waitCompletion, waitInMilliSec);
        }
    }

    /**
     * start a scan in background.
     *
     * @param listener listener to call to inform
     */
    public void scanNetwork(@NonNull ScanListener listener) {
        class OneShotScan implements Runnable, ResponseListener {

            private @NonNull ScanListener listener;
            private @NonNull HashMap<Integer, OpenWebNetDevice> devices;

            public OneShotScan(@NonNull ScanListener listener) {
                this.listener = listener;
                devices = new HashMap<Integer, OpenWebNetDevice>();
            }

            @Override
            public void run() {
                logger.debug("OneShotScan begins");
                internalListener = this;
                try {
                    // first get how many device are available
                    write(SCAN_NETWORK);
                    // wait until the reply is received to avoid problem
                    Reply reply = response.get();
                    if (Reply.ACK.equals(reply)) {
                        // wait for the reply with number of device in network
                        int number = scanResponse.get();
                        logger.debug("{} thing(s) found", number);

                        // now get information for all of the devices
                        for (int i = 0; i < number; i++) {
                            write(GET_PRODUCT_INFO.replace("<index>", String.valueOf(i)));
                            reply = response.get();
                            if (!Reply.ACK.equals(reply)) {
                                logger.debug("Request to get product information at index {} returns {}", i, reply);
                            }
                        }
                        // get hardware & firmware version for found devices
                        for (Integer macAddress : devices.keySet()) {
                            write(GET_FIRMWARE_VERSION.replace("<where>", macAddress.toString() + "00#9"));
                            reply = response.get();
                            if (!Reply.ACK.equals(reply)) {
                                logger.debug("Request to get firmware version for MAC {} returns {}", macAddress,
                                        reply);
                            }
                            write(GET_HARDWARE_VERSION.replace("<where>", macAddress.toString() + "00#9"));
                            reply = response.get();
                            if (!Reply.ACK.equals(reply)) {
                                logger.debug("Request to get hardware version for MAC {} returns {}", macAddress,
                                        reply);
                            }
                        }
                        logger.debug("Scan achieved -> {} device(s) found", devices.size());

                        devices.forEach((mac, dev) -> {
                            logger.debug("Device found {}", dev.toString());
                            listener.onDeviceFound(mac, dev.getFirmwareVersion(), dev.getHardwareVersion(),
                                    dev.getChannels());
                        });

                        logger.debug("call onScanCompleted() -> {}", listener);
                        listener.onScanCompleted();
                    } else {
                        // we notify that the scan failed
                        logger.debug("call onScanError() -> {}", listener);
                        listener.onScanError();
                    }
                } catch (InterruptedException | IOException e) {
                    // we notify that the scan failed
                    logger.warn("exception {} call onScanError() -> {}", e.getLocalizedMessage(), listener);
                    listener.onScanError();
                } finally {
                    internalListener = null;
                    logger.debug("OneShotSCan finished.");
                }
            }

            // from ResponseListener
            @SuppressWarnings("null")
            @Override
            public void onProductInformation(int where, int index, int value) {
                logger.debug("Scan listener onProductInformation");
                int macAddress = where / 100;
                int port = where % 100;
                devices.putIfAbsent(macAddress, new OpenWebNetDevice(macAddress));
                @NonNull
                OpenWebNetDevice device = devices.get(macAddress);
                if (device.hasChannel(port)) {
                    logger.warn("Channel {} of type {} already exists at MAC {} ", port, value, macAddress);
                } else {
                    logger.debug("Channel {} at MAC {} of type {}", port, macAddress, value);
                    device.addChannel(port, value);
                }

            }

            @SuppressWarnings("null")
            @Override
            public void onHardwareVersion(int where, String version) {
                logger.debug("Scan listener onHardwareVersion");
                int macAddress = where / 100;
                @NonNull
                Optional<OpenWebNetDevice> opt = Optional.ofNullable(devices.get(macAddress));

                opt.ifPresent(dev -> {
                    dev.setHardwareVersion(version);
                });
                if (!opt.isPresent()) {
                    logger.debug("Hardware version {} for MAC {} not in map", version, macAddress);
                }
            }

            @SuppressWarnings("null")
            @Override
            public void onFirmwareVersion(int where, String version) {
                logger.debug("Scan listener onFirmwareVersion");
                int macAddress = where / 100;
                @NonNull
                Optional<OpenWebNetDevice> opt = Optional.ofNullable(devices.get(macAddress));

                opt.ifPresent(dev -> {
                    dev.setFirmwareVersion(version);
                });
                if (!opt.isPresent()) {
                    logger.warn("Firmware version {} for MAC {} not in map", version, macAddress);
                } else {
                    logger.debug("Firmware version {} for MAC {}  set", version, macAddress);
                }
            }

        }
        commandExecutor.schedule(new OneShotScan(listener), 0, TimeUnit.SECONDS);

    }

    public void addThingStatusListener(int where, ThingStatusListener listener) {
        listeners.put(where, listener);
    }

    public void removeThingStatusListener(int where) {
        listeners.remove(where);
    }

    /* -- Private methods ---------------------------------------------------------------------------- */
    // Needed for Maven
    @SuppressWarnings("null")
    private @NonNull Reply setSupervisorMode(boolean supervisorMode, boolean waitCompletion) {
        @NonNull
        Reply result = writeSimpleMessage((supervisorMode ? SUPERVISOR_MODE_ON : SUPERVISOR_MODE_OFF), waitCompletion,
                0);

        logger.debug("{} request to {} supervisor mode is {}", this, supervisorMode ? "enter" : "leave", result);
        return result;
    }

    private @NonNull Reply writeSimpleMessage(String message, boolean waitCompletion, int delayInMilliSeconds) {
        class OneShotTask implements Runnable {

            private final @NonNull String messageToSend;

            public OneShotTask(@NonNull String messageToSend) {
                this.messageToSend = messageToSend;
            }

            @Override
            public void run() {
                try {
                    logger.debug("Notify begining of Task");
                    synchronized (this) {
                        this.notifyAll();
                    }

                    // Workaround
                    if (messageToSend.startsWith("*#1*")) {
                        unlockWhereWorkAround = Integer.valueOf(messageToSend.split("[\\*#]")[3]);
                        logger.debug("Initiate workaround for {}", unlockWhereWorkAround);
                    }

                    write(messageToSend);
                    // wait until the reply is received to avoid problem
                    response.get();
                    logger.debug("Response received");
                } catch (InterruptedException | IOException e) {
                    logger.warn("Catched -> force Undefined reply", e);
                    forceUndefinedReply();
                } finally {
                    logger.debug("End of Task");
                }
            }

        }
        if (message == null) {
            return Reply.NACK;
        }

        @NonNull
        OneShotTask task = new OneShotTask(message);
        commandExecutor.schedule(task, 50 + delayInMilliSeconds, TimeUnit.MILLISECONDS);
        @NonNull
        Reply reply = Reply.UNDEFINED;
        if (waitCompletion) {
            try {
                logger.debug("Waiting task to start...");
                // wait for task to start
                synchronized (task) {
                    task.wait();
                }
                logger.debug("Waiting task to provide response");
                // get the result of the task
                reply = this.response.get(Reply.UNDEFINED);
                logger.debug("Response received {}", reply);
            } catch (InterruptedException e) {
                // nothing to do
            }
        }
        return reply;
    }

    /**
     * Force the reply in case of problem (exception) (must only be called the commandExecutor to avoid multiple
     * concurrent access)
     */
    private void forceUndefinedReply() {
        this.response.set(Reply.UNDEFINED, true);
    }

    /**
     * Write message to gateway (must only be called the commandExecutor to avoid multiple concurrent access)
     *
     * @param message
     * @throws IOException
     * @throws InterruptedException
     */
    // needed for Maven
    @SuppressWarnings("null")
    private void write(@Nullable String message) throws IOException, InterruptedException {
        @NonNull
        Reply result = Reply.UNDEFINED;
        this.response.set(result, false);
        if (this.gateway != null) {
            this.gateway.write(message);
        } else {
            logger.warn("Write attempt on an unconnected gateway ({}).", this.toString());
            throw new PortNotConnected("Attempt to write an unconnected gateway: " + this.toString());
        }
    }

    /* ------------------- from Object ---------------------------------------------------------------- */
    @SuppressWarnings("null")
    @Override
    public @NonNull String toString() {
        switch (this.type) {
            case ZIGBEE_GATEWAY:
                return "OpenWebNet ZigBee through " + gateway.toString();
            case SCS_GATEWAY:
                // TODO
                return "OpenWebNet SCS (* not implemented *)";
            default: /* UNDEFINED */
                return "Undefined OpenWebNet Gateway";
        }
    }

    /* ------------------- from Interface AutoCloseable ----------------------------------------------- */
    // needed for Maven
    @SuppressWarnings("null")
    @Override
    public void close() throws Exception {
        if (gateway != null && this.ready) {
            setSupervisorMode(false, true);
        }

        if (gateway != null) {
            this.gateway.close();
            logger.debug("{} closed.", this);
        }
    }

    /* ------------------- from Interface AnswerListener ---------------------------------------------- */
    @Override
    public void onAck() {
        logger.debug("{} -> ACK received", this);
        this.response.set(Reply.ACK, true);
    }

    @Override
    public void onNack() {
        logger.warn("{} -> NACK received", this);
        this.response.set(this.response.pick() == Reply.BUSY_NACK ? Reply.BUSY_NACK : Reply.NACK, true);
    }

    @Override
    public void onBusyNack() {
        logger.debug("{} -> Busy NACK received", this);
        this.response.set(Reply.BUSY_NACK, false);
    }

    @SuppressWarnings("null")
    @Override
    public void onLightStatusChange(int where, int state) {
        @Nullable
        ThingStatusListener listener = listeners.get(where);
        @NonNull
        LightState newState = new LightState(String.format("%02d", where % 100), state);
        if (listener != null) {
            listener.onStatusChange(newState);
        } else {
            logger.warn("Unknown light {} ' out of {}) receives change to {}", where, listeners.size(), newState);
            listeners.keySet().forEach(key -> {
                logger.debug("available key = {}", key);
            });
        }
        if (unlockWhereWorkAround == where) {
            // simulate an Ack
            unlockWhereWorkAround = -1;
            logger.warn("Workaround Ack !");
            onAck();
        }
    }

    /**
     * Product information provided
     */
    @Override
    public void onProductInformation(int where, int index, int value) {
        @Nullable
        ResponseListener internal = internalListener;
        if (internal != null) {
            // call the internal Listener
            internal.onProductInformation(where, index, value);
        } else {
            logger.debug("{} -> MAC/port {}, type {} at index {} without listener", this, where, value, index);
        }
    }

    @Override
    public void onHardwareVersion(int where, String version) {
        @Nullable
        ResponseListener internal = internalListener;
        if (where == 0) {
            // this is the gateway version
            gatewayHardwareVersion = version;
            logger.debug("Gateway hardware version {}", version);
        } else if (internal != null) {
            // call the internal Listener
            internal.onHardwareVersion(where, version);
        } else {
            logger.debug("{} -> MAC {}, hardware version {}  without listener", this, where / 100, version);
        }
    }

    @Override
    public void onFirmwareVersion(int where, String version) {
        @Nullable
        ResponseListener internal = internalListener;
        if (where == 0) {
            // this is the gateway version
            gatewayFirmwareVersion = version;
            logger.debug("Gateway firmware version {}", version);
        } else if (internal != null) {
            // call the internal Listener
            internal.onFirmwareVersion(where, version);
        } else {
            logger.debug("{} -> MAC {}, firmware version {}  without listener", this, where / 100, version);
        }
    }

    /**
     * New thing discovered
     */
    @Override
    public void onDiscoveredProductsNumber(int number) {
        scanResponse.set(number, true);
    }

    /**
     * Network has been joined
     */
    @Override
    public void onNetworkJoin(int where) {
        // when a new device is detected, the supervisor mode need to be re-activated to be taken into account by this
        // new device.
        setSupervisorMode(true, false);
    }
}
