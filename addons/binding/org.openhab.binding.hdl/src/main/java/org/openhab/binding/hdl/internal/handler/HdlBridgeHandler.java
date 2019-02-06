/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.hdl.internal.handler;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.hdl.HdlBindingConstants;
import org.openhab.binding.hdl.internal.device.Device;
import org.openhab.binding.hdl.internal.device.DeviceConfiguration;
import org.openhab.binding.hdl.internal.device.DeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HdlBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 * Inspired by pulseaudio binding
 *
 * @author stigla - Initial contribution
 */
public class HdlBridgeHandler extends BaseBridgeHandler {
    private Logger logger = LoggerFactory.getLogger(HdlBridgeHandler.class);
    private Selector selector;
    private DatagramChannel datagramChannel = null;
    protected SelectionKey datagramChannelKey = null;
    protected DatagramChannel listenerChannel = null;
    protected SelectionKey listenerKey = null;
    private final Lock lock = new ReentrantLock();

    private ScheduledFuture<?> listeningJob;

    public static final int CONNECTION_REFRESH_INTERVAL = 100;
    public static final int MAX_PACKET_SIZE = 512;
    public static final int BUFFER_SIZE = 512;
    public static final int POLLING_REFRESH_INTERVAL = 5;
    public static final int REPORT_INTERVAL = 2000;

    public static String ipAddress;
    public static int portNr;
    private HashSet<String> lastActiveDevices = new HashSet<String>();
    private ArrayList<Device> devices = new ArrayList<Device>();
    private Set<DeviceStatusListener> deviceStatusListeners = new CopyOnWriteArraySet<>();

    public HdlBridgeHandler(Bridge br) {
        super(br);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing HDL Bridge handler.");
        Configuration config = getThing().getConfiguration();

        ipAddress = (String) config.get(HdlBindingConstants.PROPERTY_IP);
        portNr = ((BigDecimal) config.get(HdlBindingConstants.PROPERTY_PORT)).intValueExact();

        try {
            selector = Selector.open();
        } catch (IOException e) {
            logger.error("An exception occurred while registering the selector: '{}'", e.getMessage());
        }
        if (listenerChannel == null) {
            configureListener(portNr);
        }

        if (ipAddress != null && !ipAddress.equals("")) {
            // updateStatus(ThingStatus.ONLINE);
            establishConnection();
            // updateStatus(ThingStatus.ONLINE);

            if (listeningJob == null || listeningJob.isCancelled()) {
                try {
                    listeningJob = scheduler.scheduleWithFixedDelay(listeningRunnable, 0, CONNECTION_REFRESH_INTERVAL,
                            TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                            "An exception occurred while scheduling the connection job");
                }
            }
            onConnectionResumed();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "IP address or port number not set");
        }
    }

    protected void onRead(ByteBuffer byteBuffer, DatagramChannel datagramChannel) {
        String response = new String(byteBuffer.array(), 0, byteBuffer.limit());
        response = StringUtils.chomp(response);

        HdlPacket p = HdlPacket.parse(byteBuffer.array(), byteBuffer.position());

        try {
            if (p.sourcedeviceType != DeviceType.Invalid) {
                if (lastActiveDevices != null && lastActiveDevices.contains(p.serialNr)) {
                    // Device exist in devise list
                    Device dev = getDevice(p.serialNr);
                    if (dev != null) {
                        List<DeviceConfiguration> configurations = null;
                        Device.update(p, configurations, dev);

                        for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
                            try {
                                deviceStatusListener.onDeviceStateChanged(getThing().getUID(), dev);
                            } catch (Exception e) {
                                logger.error(
                                        "An exception occurred while calling the DeviceStatusListener , Exception msg {}",
                                        e.getMessage());
                                unregisterDeviceStatusListener(deviceStatusListener);
                            }
                        }
                    }
                } else { // Device does not exist need to make a new device add it to device list and add serial number
                         // to lastActiveDevices and add to status listner

                    DeviceConfiguration c = DeviceConfiguration.create(p.serialNr, p.sourceSubnetID, p.sourceDeviceID,
                            p.sourcedeviceType);

                    Device tempDevice = Device.create(c);

                    if (tempDevice != null) {
                        List<DeviceConfiguration> configurations = null;

                        Device.update(p, configurations, tempDevice);

                        devices.add(tempDevice);
                        lastActiveDevices.add(p.serialNr);

                        for (DeviceStatusListener deviceStatusListener : deviceStatusListeners) {
                            try {
                                // deviceStatusListener.onDeviceAdded(getThing(), tempDevice);
                                // tempDevice.setUpdated(true);
                                deviceStatusListener.onDeviceStateChanged(getThing().getUID(), tempDevice);
                            } catch (Exception e) {
                                logger.error(
                                        "An exception occurred while calling the DeviceStatusListener , Exception msg {}",
                                        e.getMessage());
                            }
                        }
                    }
                }
            } else {
                logger.debug("Subnet: {}, DeviceID: {}, Device Type is: Invalid for device nr: {}.", p.sourceSubnetID,
                        p.sourceDeviceID, p.sourceDevice);
            }
        } catch (Exception e) {
            logger.error(
                    "An exception Accured when trying to set info, Subnet: {} DeviceID: {} DeviceType: {}, Exception msg {}",
                    p.sourceSubnetID, p.sourceDeviceID, p.sourcedeviceType, e.getMessage());
        }
    }

    public void sendPacket(HdlPacket p) throws IOException {
        if (datagramChannel == null) {
            throw new IOException("server not started");
        }
        p.setReplyAddress(InetAddress.getByName(ipAddress));
        p.setSourceSubnetID(1);
        p.setSourceDeviceId(254);

        ByteBuffer bytes = ByteBuffer.wrap(p.getBytes());

        onWritable(bytes, datagramChannel);
    }

    @Override
    public void dispose() {
        try {
            selector.close();
        } catch (IOException e) {
            logger.error("An exception occurred while closing the selector: '{}'", e.getMessage());
        }

        try {
            datagramChannel.close();
        } catch (IOException e) {
            logger.warn("An exception occurred while closing the channel '{}': {}", datagramChannel, e.getMessage());
        }

        try {
            listenerChannel.close();
        } catch (IOException e) {
            logger.error("An exception occurred while closing the listener channel on port number {} ({})", portNr,
                    e.getMessage());
        }

        if (listeningJob != null && !listeningJob.isCancelled()) {
            listeningJob.cancel(true);
            listeningJob = null;
        }
        clearDeviceList();
        logger.debug("Handler disposed.");
    }

    protected void configureListener(int listenerPort) {
        // open the listener port
        try {
            listenerChannel = DatagramChannel.open();
            listenerChannel.socket().bind(new InetSocketAddress(listenerPort));
            listenerChannel.configureBlocking(false);

            logger.debug("Listening for incoming data on {}", listenerChannel.getLocalAddress());

            synchronized (selector) {
                selector.wakeup();
                try {
                    listenerKey = listenerChannel.register(selector, listenerChannel.validOps());
                } catch (ClosedChannelException e1) {
                    logger.error("An exception occurred while registering a selector: {}", e1.getMessage());
                }
            }
        } catch (Exception e2) {
            logger.error("An exception occurred while creating the Listener Channel on port number {} ({})",
                    listenerPort, e2.getMessage());
        }
    }

    protected ByteBuffer onReadable(DatagramChannel theChannel, int bufferSize, InetAddress permittedClientAddress) {
        lock.lock();
        try {
            SelectionKey theSelectionKey = theChannel.keyFor(selector);

            if (theSelectionKey != null) {
                synchronized (selector) {
                    try {
                        selector.selectNow();
                    } catch (IOException e) {
                        logger.error("An exception occurred while selecting: {}", e.getMessage());
                    }
                }

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey selKey = it.next();
                    it.remove();
                    if (selKey.isValid() && selKey.isReadable() && selKey.equals(theSelectionKey)) {
                        ByteBuffer readBuffer = ByteBuffer.allocate(bufferSize);
                        int numberBytesRead = 0;
                        boolean error = false;

                        if (selKey.equals(listenerKey)) {
                            try {
                                InetSocketAddress clientAddress = (InetSocketAddress) theChannel.receive(readBuffer);
                                if (clientAddress.getAddress().equals(permittedClientAddress)) {
                                    // logger.debug("Received {} on the listener port from {}",
                                    // new String(readBuffer.array()), clientAddress);
                                    numberBytesRead = readBuffer.position();
                                } else {
                                    logger.warn(
                                            "Received data from '{}' which is not the permitted remote address '{}'",
                                            clientAddress, permittedClientAddress);
                                    return null;
                                }
                            } catch (Exception e) {
                                logger.error("An exception occurred while receiving data on the listener port: '{}'",
                                        e.getMessage());
                                error = true;
                            }
                        } else {
                            try {
                                numberBytesRead = theChannel.read(readBuffer);
                            } catch (NotYetConnectedException e) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        "The remote host is not yet connected");
                                error = true;
                            } catch (PortUnreachableException e) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                                        "The remote host is probably not a KEBA EV Charging station");
                                error = true;
                            } catch (IOException e) {
                                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                        "An IO exception occurred");
                                error = true;
                            }
                        }

                        if (numberBytesRead == -1) {
                            error = true;
                        }

                        if (error) {
                            logger.debug("Disconnecting '{}' because of a socket error",
                                    getThing().getUID().toString());
                            try {
                                theChannel.close();
                            } catch (IOException e) {
                                logger.error("An exception occurred while closing the channel '{}': {}",
                                        datagramChannel, e.getMessage());
                            }
                            onConnectionLost();
                        } else {
                            return readBuffer;
                        }
                    }
                }
            }

            return null;
        } finally {
            lock.unlock();
        }
    }

    protected void onWritable(ByteBuffer buffer, DatagramChannel theChannel) {
        lock.lock();
        try {
            SelectionKey theSelectionKey = theChannel.keyFor(selector);

            if (theSelectionKey != null) {
                synchronized (selector) {
                    try {
                        selector.selectNow();
                    } catch (IOException e) {
                        logger.error("An exception occurred while selecting: {}", e.getMessage());
                    }
                }

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey selKey = it.next();
                    it.remove();
                    if (selKey.isValid() && selKey.isWritable() && selKey.equals(theSelectionKey)) {
                        boolean error = false;
                        buffer.rewind();

                        try {
                            logger.debug("Sending '{}' on the channel '{}'->'{}'",
                                    new Object[] { new String(buffer.array()), theChannel.getLocalAddress(),
                                            theChannel.getRemoteAddress() });
                            theChannel.write(buffer);
                        } catch (NotYetConnectedException e) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "The remote host is not yet connected");
                            error = true;
                        } catch (ClosedChannelException e) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "The connection to the remote host is closed");
                            error = true;
                        } catch (IOException e) {
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                    "An IO exception occurred");
                            error = true;
                        }

                        if (error) {
                            logger.debug("Disconnecting '{}' because of a socket error",
                                    getThing().getUID().toString());
                            try {
                                theChannel.close();
                            } catch (IOException e) {
                                logger.warn("An exception occurred while closing the channel '{}': {}", datagramChannel,
                                        e.getMessage());
                            }
                            onConnectionLost();
                        }
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void onConnectionLost() {
        establishConnection();
    }

    public void onConnectionResumed() {
        logger.debug("Bridge connected. Updating thing status to ONLINE.");
        updateStatus(ThingStatus.ONLINE);
    }

    private void establishConnection() {
        lock.lock();
        try {
            if (ipAddress != null && !ipAddress.equals("")) {
                try {
                    datagramChannel = DatagramChannel.open();
                } catch (Exception e2) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "An exception occurred while opening a datagram channel");
                }

                try {
                    datagramChannel.configureBlocking(false);
                } catch (IOException e2) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "An exception occurred while configuring a datagram channel");
                }

                synchronized (selector) {
                    selector.wakeup();
                    int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
                    try {
                        datagramChannelKey = datagramChannel.register(selector, interestSet);
                    } catch (ClosedChannelException e1) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "An exception occurred while registering a selector");
                    }

                    InetSocketAddress remoteAddress = new InetSocketAddress(ipAddress, portNr);

                    try {
                        logger.trace("Connecting the channel for {} ", remoteAddress);
                        datagramChannel.connect(remoteAddress);
                        // onConnectionResumed();
                    } catch (Exception e) {
                        logger.error("An exception occurred while connecting connecting to '{}:{}' : {}",
                                new Object[] { ipAddress, portNr, e.getMessage() });
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                                "An exception occurred while connecting");
                    }
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        getThing().getStatusInfo().getDescription());
            }
        } finally

        {
            lock.unlock();
        }
    }

    private Runnable listeningRunnable = new Runnable() {

        @Override
        public void run() {
            lock.lock();
            try {
                if (datagramChannel == null || !datagramChannel.isConnected()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "The connection is not yet initialized");
                    onConnectionLost();
                } else {
                    ByteBuffer buffer = onReadable(listenerChannel, BUFFER_SIZE, InetAddress.getByName(ipAddress));
                    if (buffer != null && buffer.remaining() > 0) {
                        onRead(buffer, datagramChannel);
                    }
                }
            } catch (Exception e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "An exception occurred while receiving event data from the HDL Bus");
            } finally {
                lock.unlock();
            }
        }
    };

    private Device getDevice(String serialNumber, ArrayList<Device> devices) {
        for (Device device : devices) {
            if (device.getSerialNr().toUpperCase().equals(serialNumber)) {
                return device;
            }
        }
        return null;
    }

    public Device getDevice(String serialNumber) {
        return getDevice(serialNumber, devices);
    }

    public boolean registerDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        if (deviceStatusListener == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null deviceStatusListener.");
        }
        boolean result = deviceStatusListeners.add(deviceStatusListener);

        return result;
    }

    public boolean unregisterDeviceStatusListener(DeviceStatusListener deviceStatusListener) {
        if (deviceStatusListener == null) {
            throw new IllegalArgumentException("It's not allowed to pass a null deviceStatusListener.");
        }
        boolean result = deviceStatusListeners.remove(deviceStatusListener);
        if (result) {
            clearDeviceList();
        }
        return result;
    }

    public void clearDeviceList() {
        lastActiveDevices.clear();
    }

    public void addTolastActiveDeviceList(String HdlHandlerSerialnr) {
        lastActiveDevices.add(HdlHandlerSerialnr);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }
}
