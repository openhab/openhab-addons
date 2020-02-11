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
package org.openhab.binding.bluetooth.bluegiga.handler;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.io.transport.serial.PortInUseException;
import org.eclipse.smarthome.io.transport.serial.SerialPort;
import org.eclipse.smarthome.io.transport.serial.SerialPortIdentifier;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.eclipse.smarthome.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothDeviceListener;
import org.openhab.binding.bluetooth.BluetoothDiscoveryListener;
import org.openhab.binding.bluetooth.bluegiga.BlueGigaAdapterConstants;
import org.openhab.binding.bluetooth.bluegiga.BlueGigaBluetoothDevice;
import org.openhab.binding.bluetooth.bluegiga.internal.BlueGigaCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.BlueGigaEventListener;
import org.openhab.binding.bluetooth.bluegiga.internal.BlueGigaHandlerListener;
import org.openhab.binding.bluetooth.bluegiga.internal.BlueGigaResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.BlueGigaSerialHandler;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaAttributeWriteCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaAttributeWriteResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaFindInformationCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaFindInformationResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaReadByGroupTypeCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaReadByGroupTypeResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaReadByHandleCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaReadByHandleResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.command.connection.BlueGigaConnectionStatusEvent;
import org.openhab.binding.bluetooth.bluegiga.internal.command.connection.BlueGigaDisconnectCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.connection.BlueGigaDisconnectResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.command.connection.BlueGigaDisconnectedEvent;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaConnectDirectCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaConnectDirectResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaDiscoverCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaEndProcedureCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaEndProcedureResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaScanResponseEvent;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaSetModeCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaSetModeResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaSetScanParametersCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.system.BlueGigaAddressGetCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.system.BlueGigaAddressGetResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.command.system.BlueGigaGetConnectionsCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.system.BlueGigaGetConnectionsResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.command.system.BlueGigaGetInfoCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.system.BlueGigaGetInfoResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.BgApiResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.BluetoothAddressType;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.GapConnectableMode;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.GapDiscoverMode;
import org.openhab.binding.bluetooth.bluegiga.internal.enumeration.GapDiscoverableMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BlueGigaBridgeHandler} is responsible for interfacing to the BlueGiga Bluetooth adapter.
 * It provides a private interface for {@link BlueGigaBluetoothDevice}s to access the dongle and provides top
 * level adaptor functionality for scanning and arbitration.
 * <p>
 * The handler provides the serial interface to the dongle via the BlueGiga BG-API library.
 * <p>
 * In the BlueGiga dongle, we leave scanning enabled most of the time. Normally, it's just passive scanning, and active
 * scanning is enabled when we want to include new devices. Passive scanning is enough for us to receive beacons etc
 * that are transmitted periodically, and active scanning will get more information which may be useful when we are
 * including new devices.
 *
 * @author Chris Jackson - Initial contribution
 * @author Kai Kreuzer - Made handler implement BlueGigaHandlerListener
 */
@NonNullByDefault
public class BlueGigaBridgeHandler extends BaseBridgeHandler
        implements BluetoothAdapter, BlueGigaEventListener, BlueGigaHandlerListener {

    private final Logger logger = LoggerFactory.getLogger(BlueGigaBridgeHandler.class);

    private final SerialPortManager serialPortManager;

    // The serial port.
    @Nullable
    private SerialPort serialPort;

    // The serial port input stream.
    @Nullable
    private InputStream inputStream;

    // The serial port output stream.
    @Nullable
    private OutputStream outputStream;

    // The BlueGiga API handler
    @Nullable
    private BlueGigaSerialHandler bgHandler;

    // The maximum number of connections this interface supports
    private int maxConnections = 0;

    private final int passiveScanInterval = 0x40;
    private final int passiveScanWindow = 0x08;

    private final int activeScanInterval = 0x40;
    private final int activeScanWindow = 0x20;

    // Our BT address
    @Nullable
    private BluetoothAddress address;

    // internal flag for the discovery configuration
    private boolean discoveryActive = true;

    // Map of Bluetooth devices known to this bridge.
    // This is all devices we have heard on the network - not just things bound to the bridge
    private final Map<BluetoothAddress, BluetoothDevice> devices = new ConcurrentHashMap<>();

    // Map of open connections
    private final Map<Integer, BluetoothAddress> connections = new ConcurrentHashMap<>();

    // Set of discovery listeners
    protected final Set<BluetoothDiscoveryListener> discoveryListeners = new CopyOnWriteArraySet<>();

    // List of device listeners
    protected final ConcurrentHashMap<BluetoothAddress, BluetoothDeviceListener> deviceListeners = new ConcurrentHashMap<>();

    public BlueGigaBridgeHandler(Bridge bridge, SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public ThingUID getUID() {
        // being a BluetoothAdapter, we use the UID of our bridge
        return getThing().getUID();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // No commands supported for the bridge
    }

    @Override
    public void initialize() {
        Object discovery = getConfig().get(BlueGigaAdapterConstants.PROPERTY_DISCOVERY);
        if (discovery != null && discovery.toString().equalsIgnoreCase(Boolean.FALSE.toString())) {
            discoveryActive = false;
            logger.debug("Deactivated discovery participation.");
        }

        final String portId = (String) getConfig().get(BlueGigaAdapterConstants.CONFIGURATION_PORT);

        if (portId == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Serial port must be configured!");
            return;
        }
        if (openSerialPort(portId, 115200)) {
            BlueGigaSerialHandler bgh = new BlueGigaSerialHandler(inputStream, outputStream);
            // Create and send the reset command to the dongle
            bgh.addEventListener(this);
            bgh.addHandlerListener(this);
            this.setBgHandler(bgh);

            updateStatus(ThingStatus.UNKNOWN);

            scheduler.submit(() -> {
                // Stop any procedures that are running
                bgStopProcedure();

                // Close all transactions
                BlueGigaCommand command = new BlueGigaGetConnectionsCommand();
                BlueGigaGetConnectionsResponse connectionsResponse = (BlueGigaGetConnectionsResponse) bgh
                        .sendTransaction(command);
                if (connectionsResponse != null) {
                    maxConnections = connectionsResponse.getMaxconn();
                }

                // Close all connections so we start from a known position
                for (int connection = 0; connection < maxConnections; connection++) {
                    bgDisconnect(connection);
                }

                // Get our Bluetooth address
                command = new BlueGigaAddressGetCommand();
                BlueGigaAddressGetResponse addressResponse = (BlueGigaAddressGetResponse) bgh.sendTransaction(command);
                if (addressResponse != null) {
                    address = new BluetoothAddress(addressResponse.getAddress());
                    updateStatus(ThingStatus.ONLINE);
                } else {
                    updateStatus(ThingStatus.OFFLINE);
                }

                command = new BlueGigaGetInfoCommand();
                BlueGigaGetInfoResponse infoResponse = (BlueGigaGetInfoResponse) bgh.sendTransaction(command);

                // Set mode to non-discoverable etc.
                // Not doing this will cause connection failures later
                bgSetMode();

                // Start passive scan
                bgStartScanning(false, passiveScanInterval, passiveScanWindow);

                Map<String, String> properties = editProperties();
                properties.put(BluetoothBindingConstants.PROPERTY_MAXCONNECTIONS, Integer.toString(maxConnections));
                properties.put(Thing.PROPERTY_FIRMWARE_VERSION,
                        String.format("%d.%d", infoResponse.getMajor(), infoResponse.getMinor()));
                properties.put(Thing.PROPERTY_HARDWARE_VERSION, Integer.toString(infoResponse.getHardware()));
                properties.put(BlueGigaAdapterConstants.PROPERTY_PROTOCOL,
                        Integer.toString(infoResponse.getProtocolVersion()));
                properties.put(BlueGigaAdapterConstants.PROPERTY_LINKLAYER,
                        Integer.toString(infoResponse.getLlVersion()));
                updateProperties(properties);
            });
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Failed opening serial port.");
        }
    }

    @Override
    public void dispose() {
        try {
            BlueGigaSerialHandler bgh = getBgHandler();
            bgh.removeEventListener(this);
            bgh.removeHandlerListener(this);
            bgh.close();
        } catch (IllegalStateException e) {
            // ignore if handler wasn't set at all
        }
        closeSerialPort();
    }

    private boolean openSerialPort(final String serialPortName, int baudRate) {
        logger.debug("Connecting to serial port '{}'", serialPortName);
        try {
            SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(serialPortName);
            if (portIdentifier == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Port does not exist");
                return false;
            }
            SerialPort sp = portIdentifier.open("org.openhab.binding.bluetooth.bluegiga", 2000);
            sp.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

            sp.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT);
            sp.enableReceiveThreshold(1);
            sp.enableReceiveTimeout(2000);

            // RXTX serial port library causes high CPU load
            // Start event listener, which will just sleep and slow down event loop
            sp.notifyOnDataAvailable(true);

            logger.info("Connected to serial port '{}'.", serialPortName);

            try {
                inputStream = new BufferedInputStream(sp.getInputStream());
                outputStream = sp.getOutputStream();
            } catch (IOException e) {
                logger.error("Error getting serial streams", e);
                return false;
            }
            this.serialPort = sp;
            return true;
        } catch (PortInUseException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Serial Error: Port in use");
            return false;
        } catch (UnsupportedCommOperationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "Serial Error: Unsupported operation");
            return false;
        }
    }

    private void closeSerialPort() {
        if (serialPort != null) {
            SerialPort sp = serialPort;
            try {
                sp.disableReceiveTimeout();
                sp.removeEventListener();
                if (outputStream != null) {
                    OutputStream os = outputStream;
                    os.flush();
                    os.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                logger.error("Error closing serial port.", e);
            } finally {
                sp.close();
                logger.debug("Closed serial port.");
                serialPort = null;
                inputStream = null;
                outputStream = null;
            }
        }
    }

    @SuppressWarnings({ "unused", "null" })
    @Override
    public void bluegigaEventReceived(@Nullable BlueGigaResponse event) {
        if (event instanceof BlueGigaScanResponseEvent) {
            BlueGigaScanResponseEvent scanEvent = (BlueGigaScanResponseEvent) event;

            // We use the scan event to add any devices we hear to the devices list
            // The device gets created, and then manages itself for discovery etc.
            BluetoothAddress sender = new BluetoothAddress(scanEvent.getSender());
            BlueGigaBluetoothDevice device;
            if (devices.get(sender) == null) {
                logger.debug("BlueGiga adding new device to adaptor {}: {}", address, sender);
                device = new BlueGigaBluetoothDevice(this, new BluetoothAddress(scanEvent.getSender()),
                        scanEvent.getAddressType());
                devices.put(sender, device);
                deviceDiscovered(device);
            }

            return;
        }

        if (event instanceof BlueGigaConnectionStatusEvent) {
            BlueGigaConnectionStatusEvent connectionEvent = (BlueGigaConnectionStatusEvent) event;
            connections.put(connectionEvent.getConnection(), new BluetoothAddress(connectionEvent.getAddress()));
        }

        if (event instanceof BlueGigaDisconnectedEvent) {
            BlueGigaDisconnectedEvent disconnectedEvent = (BlueGigaDisconnectedEvent) event;
            connections.remove(disconnectedEvent.getConnection());
        }
    }

    @Override
    public void scanStart() {
        // Stop the passive scan
        bgStopProcedure();

        // Start a active scan
        bgStartScanning(true, activeScanInterval, activeScanWindow);

        for (BluetoothDevice device : devices.values()) {
            deviceDiscovered(device);
        }
    }

    @Override
    public void scanStop() {
        // Stop the active scan
        bgStopProcedure();

        // Start a passive scan
        bgStartScanning(false, passiveScanInterval, passiveScanWindow);
    }

    @Override
    public BluetoothAddress getAddress() {
        BluetoothAddress addr = address;
        if (addr != null) {
            return addr;
        } else {
            throw new IllegalStateException("Adapter has not been initialized yet!");
        }
    }

    @SuppressWarnings({ "null", "unused" })
    @Override
    public BluetoothDevice getDevice(BluetoothAddress address) {
        BluetoothDevice device = devices.get(address);
        if (device == null) {
            // This method always needs to return a device, even if we don't currently know about it.
            device = new BlueGigaBluetoothDevice(this, address, BluetoothAddressType.UNKNOWN);
            devices.put(address, device);
        }
        return device;
    }

    /*
     * The following methods provide adaptor level functions for the BlueGiga interface. Typically these methods
     * are used by the device but are provided in the adapter to allow common knowledge and to support conflict
     * resolution.
     */

    public BlueGigaSerialHandler getBgHandler() {
        BlueGigaSerialHandler handler = bgHandler;
        if (handler != null) {
            return handler;
        } else {
            throw new IllegalStateException("bgHandler must not be null at that point!");
        }
    }

    public void setBgHandler(BlueGigaSerialHandler bgHandler) {
        this.bgHandler = bgHandler;
    }

    /**
     * Connects to a device.
     * <p>
     * If the device is already connected, or the attempt to connect failed, then we return false. If we have reached
     * the maximum number of connections supported by this dongle, then we return false.
     *
     * @param address the device {@link BluetoothAddress} to connect to
     * @param addressType the {@link BluetoothAddressType} of the device
     * @return true if the connection was started
     */
    public boolean bgConnect(BluetoothAddress address, BluetoothAddressType addressType) {
        // Check the connection to make sure we're not already connected to this device
        if (connections.containsValue(address)) {
            return false;
        }

        // FIXME: When getting here, I always found all connections to be already taken and thus the code never
        // proceeded. Relaxing this condition did not do any obvious harm, but now guaranteed that the services are
        // queried from the device.
        if (connections.size() == maxConnections + 1) {
            logger.debug("BlueGiga: Attempt to connect to {} but no connections available.", address);
            return false;
        }

        bgSetMode();

        // Connect...
        int connIntervalMin = 60;
        int connIntervalMax = 100;
        int latency = 0;
        int timeout = 100;

        BlueGigaConnectDirectCommand connect = new BlueGigaConnectDirectCommand();
        connect.setAddress(address.toString());
        connect.setAddrType(addressType);
        connect.setConnIntervalMin(connIntervalMin);
        connect.setConnIntervalMax(connIntervalMax);
        connect.setLatency(latency);
        connect.setTimeout(timeout);
        BlueGigaConnectDirectResponse connectResponse = (BlueGigaConnectDirectResponse) getBgHandler()
                .sendTransaction(connect);
        if (connectResponse.getResult() != BgApiResponse.SUCCESS) {
            return false;
        }

        return true;
    }

    /**
     * Close a connection using {@link BlueGigaDisconnectCommand}
     *
     * @param connectionHandle
     * @return
     */
    public boolean bgDisconnect(int connectionHandle) {
        BlueGigaDisconnectCommand command = new BlueGigaDisconnectCommand();
        command.setConnection(connectionHandle);
        BlueGigaDisconnectResponse response = (BlueGigaDisconnectResponse) getBgHandler().sendTransaction(command);

        return response.getResult() == BgApiResponse.SUCCESS;
    }

    /**
     * Device discovered. This simply passes the discover information to the discovery service for processing.
     */
    public void deviceDiscovered(BluetoothDevice device) {
        if (discoveryActive) {
            for (BluetoothDiscoveryListener listener : discoveryListeners) {
                listener.deviceDiscovered(device);
            }
        }
    }

    /**
     * Start a read of all primary services using {@link BlueGigaReadByGroupTypeCommand}
     *
     * @param connectionHandle
     * @return true if successful
     */
    public boolean bgFindPrimaryServices(int connectionHandle) {
        logger.debug("BlueGiga FindPrimary: connection {}", connectionHandle);
        BlueGigaReadByGroupTypeCommand command = new BlueGigaReadByGroupTypeCommand();
        command.setConnection(connectionHandle);
        command.setStart(1);
        command.setEnd(65535);
        command.setUuid(UUID.fromString("00002800-0000-0000-0000-000000000000"));
        BlueGigaReadByGroupTypeResponse response = (BlueGigaReadByGroupTypeResponse) getBgHandler()
                .sendTransaction(command);
        return response.getResult() == BgApiResponse.SUCCESS;
    }

    /**
     * Start a read of all characteristics using {@link BlueGigaFindInformationCommand}
     *
     * @param connectionHandle
     * @return true if successful
     */
    public boolean bgFindCharacteristics(int connectionHandle) {
        logger.debug("BlueGiga Find: connection {}", connectionHandle);
        BlueGigaFindInformationCommand command = new BlueGigaFindInformationCommand();
        command.setConnection(connectionHandle);
        command.setStart(1);
        command.setEnd(65535);
        BlueGigaFindInformationResponse response = (BlueGigaFindInformationResponse) getBgHandler()
                .sendTransaction(command);

        return response.getResult() == BgApiResponse.SUCCESS;
    }

    /**
     * Read a characteristic using {@link BlueGigaReadByHandleCommand}
     *
     * @param connectionHandle
     * @param handle
     * @return true if successful
     */
    public boolean bgReadCharacteristic(int connectionHandle, int handle) {
        logger.debug("BlueGiga Read: connection {}, handle {}", connectionHandle, handle);
        BlueGigaReadByHandleCommand command = new BlueGigaReadByHandleCommand();
        command.setConnection(connectionHandle);
        command.setChrHandle(handle);
        BlueGigaReadByHandleResponse response = (BlueGigaReadByHandleResponse) getBgHandler().sendTransaction(command);

        return response.getResult() == BgApiResponse.SUCCESS;
    }

    /**
     * Write a characteristic using {@link BlueGigaAttributeWriteCommand}
     *
     * @param connectionHandle
     * @param handle
     * @param value
     * @return true if successful
     */
    public boolean bgWriteCharacteristic(int connectionHandle, int handle, int[] value) {
        logger.debug("BlueGiga Write: connection {}, handle {}", connectionHandle, handle);
        BlueGigaAttributeWriteCommand command = new BlueGigaAttributeWriteCommand();
        command.setConnection(connectionHandle);
        command.setAttHandle(handle);
        command.setData(value);
        BlueGigaAttributeWriteResponse response = (BlueGigaAttributeWriteResponse) getBgHandler()
                .sendTransaction(command);

        return response.getResult() == BgApiResponse.SUCCESS;
    }

    /*
     * The following methods are private methods for handling the BlueGiga protocol
     */
    private boolean bgStopProcedure() {
        BlueGigaCommand command = new BlueGigaEndProcedureCommand();
        BlueGigaEndProcedureResponse response = (BlueGigaEndProcedureResponse) getBgHandler().sendTransaction(command);

        return response.getResult() == BgApiResponse.SUCCESS;
    }

    private boolean bgSetMode() {
        BlueGigaSetModeCommand command = new BlueGigaSetModeCommand();
        command.setConnect(GapConnectableMode.GAP_NON_CONNECTABLE);
        command.setDiscover(GapDiscoverableMode.GAP_NON_DISCOVERABLE);
        BlueGigaSetModeResponse response = (BlueGigaSetModeResponse) getBgHandler().sendTransaction(command);

        return response.getResult() == BgApiResponse.SUCCESS;
    }

    /**
     * Starts scanning on the dongle
     *
     * @param active true for active scanning
     */
    private void bgStartScanning(boolean active, int interval, int window) {
        BlueGigaSetScanParametersCommand scanCommand = new BlueGigaSetScanParametersCommand();
        scanCommand.setActiveScanning(active);
        scanCommand.setScanInterval(interval);
        scanCommand.setScanWindow(window);
        getBgHandler().sendTransaction(scanCommand);

        BlueGigaDiscoverCommand discoverCommand = new BlueGigaDiscoverCommand();
        discoverCommand.setMode(GapDiscoverMode.GAP_DISCOVER_OBSERVATION);
        getBgHandler().sendTransaction(discoverCommand);
    }

    /**
     * Add an event listener for the BlueGiga events
     *
     * @param listener the {@link BlueGigaEventListener} to add
     */
    public void addEventListener(BlueGigaEventListener listener) {
        getBgHandler().addEventListener(listener);
    }

    /**
     * Remove an event listener for the BlueGiga events
     *
     * @param listener the {@link BlueGigaEventListener} to remove
     */
    public void removeEventListener(BlueGigaEventListener listener) {
        getBgHandler().removeEventListener(listener);
    }

    @Override
    public void addDiscoveryListener(BluetoothDiscoveryListener listener) {
        discoveryListeners.add(listener);
    }

    @Override
    public void removeDiscoveryListener(@Nullable BluetoothDiscoveryListener listener) {
        discoveryListeners.remove(listener);
    }

    @Override
    public void bluegigaClosed(Exception reason) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason.getMessage());
    }

}
