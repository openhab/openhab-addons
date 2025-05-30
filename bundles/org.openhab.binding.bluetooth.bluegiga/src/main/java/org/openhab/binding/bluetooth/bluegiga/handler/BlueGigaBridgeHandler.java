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
package org.openhab.binding.bluetooth.bluegiga.handler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.AbstractBluetoothBridgeHandler;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.binding.bluetooth.bluegiga.BlueGigaAdapterConstants;
import org.openhab.binding.bluetooth.bluegiga.BlueGigaBluetoothDevice;
import org.openhab.binding.bluetooth.bluegiga.internal.BlueGigaCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.BlueGigaConfiguration;
import org.openhab.binding.bluetooth.bluegiga.internal.BlueGigaEventListener;
import org.openhab.binding.bluetooth.bluegiga.internal.BlueGigaException;
import org.openhab.binding.bluetooth.bluegiga.internal.BlueGigaHandlerListener;
import org.openhab.binding.bluetooth.bluegiga.internal.BlueGigaResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.BlueGigaSerialHandler;
import org.openhab.binding.bluetooth.bluegiga.internal.BlueGigaTransactionManager;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaAttributeWriteCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaAttributeWriteResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaFindInformationCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaFindInformationResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaReadByGroupTypeCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaReadByGroupTypeResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaReadByHandleCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaReadByHandleResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaReadByTypeCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.attributeclient.BlueGigaReadByTypeResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.command.connection.BlueGigaConnectionStatusEvent;
import org.openhab.binding.bluetooth.bluegiga.internal.command.connection.BlueGigaDisconnectCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.connection.BlueGigaDisconnectResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.command.connection.BlueGigaDisconnectedEvent;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaConnectDirectCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaConnectDirectResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaDiscoverCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaDiscoverResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaEndProcedureCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaEndProcedureResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaScanResponseEvent;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaSetModeCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaSetModeResponse;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaSetScanParametersCommand;
import org.openhab.binding.bluetooth.bluegiga.internal.command.gap.BlueGigaSetScanParametersResponse;
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
import org.openhab.binding.bluetooth.util.RetryException;
import org.openhab.binding.bluetooth.util.RetryFuture;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.io.transport.serial.PortInUseException;
import org.openhab.core.io.transport.serial.SerialPort;
import org.openhab.core.io.transport.serial.SerialPortIdentifier;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.io.transport.serial.UnsupportedCommOperationException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
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
 * @author Pauli Anttila - Many improvements
 */
@NonNullByDefault
public class BlueGigaBridgeHandler extends AbstractBluetoothBridgeHandler<BlueGigaBluetoothDevice>
        implements BlueGigaEventListener, BlueGigaHandlerListener {

    private final Logger logger = LoggerFactory.getLogger(BlueGigaBridgeHandler.class);

    private static final int COMMAND_TIMEOUT_MS = 5000;
    private static final int INITIALIZATION_INTERVAL_SEC = 60;

    private final SerialPortManager serialPortManager;

    private final ScheduledExecutorService executor = ThreadPoolManager.getScheduledPool("BlueGiga");

    private BlueGigaConfiguration configuration = new BlueGigaConfiguration();

    // The serial port input stream.
    private Optional<InputStream> inputStream = Optional.empty();

    // The serial port output stream.
    private Optional<OutputStream> outputStream = Optional.empty();

    // The BlueGiga API handler
    private CompletableFuture<BlueGigaSerialHandler> serialHandler = CompletableFuture
            .failedFuture(new IllegalStateException("Uninitialized"));

    // The BlueGiga transaction manager
    @NonNullByDefault({})
    private CompletableFuture<BlueGigaTransactionManager> transactionManager = CompletableFuture
            .failedFuture(new IllegalStateException("Uninitialized"));

    // The maximum number of connections this interface supports
    private int maxConnections = 0;

    // Our BT address
    private @Nullable BluetoothAddress address;

    // Map of open connections
    private final Map<Integer, BluetoothAddress> connections = new ConcurrentHashMap<>();

    private volatile boolean initComplete = false;

    private CompletableFuture<SerialPort> serialPortFuture = CompletableFuture
            .failedFuture(new IllegalStateException("Uninitialized"));

    private @Nullable ScheduledFuture<?> removeInactiveDevicesTask;
    private @Nullable ScheduledFuture<?> discoveryTask;
    private @Nullable ScheduledFuture<?> initTask;

    private @Nullable Future<?> passiveScanIdleTimer;

    public BlueGigaBridgeHandler(Bridge bridge, SerialPortManager serialPortManager) {
        super(bridge);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void initialize() {
        super.initialize();
        updateStatus(ThingStatus.UNKNOWN);
        if (initTask == null) {
            initTask = scheduler.scheduleWithFixedDelay(this::checkInit, 0, 10, TimeUnit.SECONDS);
        }
    }

    protected void checkInit() {
        boolean init = false;
        try {
            if (!serialHandler.get().isAlive()) {
                logger.debug("BLE serial handler seems to be dead, reinitilize");
                stop();
                init = true;
            }
        } catch (InterruptedException e) {
            return;
        } catch (ExecutionException e) {
            init = true;
        }

        if (init) {
            logger.debug("Initialize BlueGiga");
            start();
        }
    }

    private void start() {
        Optional<BlueGigaConfiguration> cfg = Optional.of(getConfigAs(BlueGigaConfiguration.class));
        if (cfg.isPresent()) {
            initComplete = false;
            configuration = cfg.get();
            serialPortFuture = RetryFuture.callWithRetry(() -> {
                var localFuture = serialPortFuture;
                logger.debug("Using configuration: {}", configuration);

                String serialPortName = configuration.port;
                int baudRate = 115200;

                logger.debug("Connecting to serial port '{}'", serialPortName);
                try {
                    SerialPortIdentifier portIdentifier = serialPortManager.getIdentifier(serialPortName);
                    if (portIdentifier == null) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Port does not exist");
                        throw new RetryException(INITIALIZATION_INTERVAL_SEC, TimeUnit.SECONDS);
                    }
                    SerialPort sp = portIdentifier.open("org.openhab.binding.bluetooth.bluegiga", 2000);
                    sp.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
                            SerialPort.PARITY_NONE);

                    sp.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_OUT);
                    sp.enableReceiveThreshold(1);
                    sp.enableReceiveTimeout(2000);

                    // RXTX serial port library causes high CPU load
                    // Start event listener, which will just sleep and slow down event loop
                    sp.notifyOnDataAvailable(true);

                    logger.info("Connected to serial port '{}'.", serialPortName);

                    try {
                        inputStream = Optional.of(new BufferedInputStream(sp.getInputStream()));
                        outputStream = Optional.of(new BufferedOutputStream(sp.getOutputStream()));
                    } catch (IOException e) {
                        logger.error("Error getting serial streams", e);
                        throw new RetryException(INITIALIZATION_INTERVAL_SEC, TimeUnit.SECONDS);
                    }
                    // if this future has been cancelled while this was running, then we
                    // need to make sure that we close this port
                    localFuture.whenComplete((port, th) -> {
                        if (th != null) {
                            // we need to shut down the port now.
                            closeSerialPort(sp);
                        }
                    });

                    if (inputStream.isEmpty() || outputStream.isEmpty()) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                                "Serial Error: Communication stream not available");
                        throw new RetryException(INITIALIZATION_INTERVAL_SEC, TimeUnit.SECONDS);
                    }

                    return sp;
                } catch (PortInUseException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                            "Serial Error: Port in use");
                    throw new RetryException(INITIALIZATION_INTERVAL_SEC, TimeUnit.SECONDS);
                } catch (UnsupportedCommOperationException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                            "Serial Error: Unsupported operation");
                    throw new RetryException(INITIALIZATION_INTERVAL_SEC, TimeUnit.SECONDS);
                } catch (RuntimeException ex) {
                    logger.debug("Start failed", ex);
                    throw new RetryException(INITIALIZATION_INTERVAL_SEC, TimeUnit.SECONDS);
                }
            }, executor);

            serialHandler = serialPortFuture
                    .thenApply(sp -> new BlueGigaSerialHandler(getThing().getUID().getAsString(), inputStream.get(),
                            outputStream.get()));
            transactionManager = serialHandler.thenApply(sh -> {
                BlueGigaTransactionManager th = new BlueGigaTransactionManager(sh, executor);
                sh.addHandlerListener(this);
                th.addEventListener(this);
                return th;
            });
            transactionManager.thenRun(() -> {
                try {
                    // Stop any procedures that are running
                    bgEndProcedure();

                    // Set mode to non-discoverable etc.
                    bgSetMode();

                    // Get maximum parallel connections
                    maxConnections = readMaxConnections().getMaxconn();

                    // Close all connections so we start from a known position
                    for (int connection = 0; connection < maxConnections; connection++) {
                        sendCommandWithoutChecks(
                                new BlueGigaDisconnectCommand.CommandBuilder().withConnection(connection).build(),
                                BlueGigaDisconnectResponse.class);
                    }

                    // Get our Bluetooth address
                    address = new BluetoothAddress(readAddress().getAddress());

                    updateThingProperties();

                    initComplete = true;
                    updateStatus(ThingStatus.ONLINE);
                    startScheduledTasks();
                } catch (BlueGigaException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                            "Initialization of BlueGiga controller failed");
                }
            }).exceptionally(th -> {
                if (th instanceof CompletionException && th.getCause() instanceof CancellationException) {
                    // cancellation is a normal reason for failure, so no need to print it.
                    return null;
                }
                logger.warn("Error initializing bluegiga", th);
                return null;
            });

        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR);
        }
    }

    @Override
    public void dispose() {
        @Nullable
        ScheduledFuture<?> task = initTask;
        if (task != null) {
            task.cancel(true);
            task = null;
        }
        stop();
        super.dispose();
    }

    private void stop() {
        logger.info("Stop BlueGiga");
        transactionManager.thenAccept(tman -> {
            tman.removeEventListener(this);
            tman.close();
        });
        serialHandler.thenAccept(sh -> {
            sh.removeHandlerListener(this);
            sh.close();
        });
        address = null;
        initComplete = false;
        connections.clear();

        serialPortFuture.thenAccept(this::closeSerialPort);
        serialPortFuture.cancel(false);
        stopScheduledTasks();
    }

    private void schedulePassiveScan() {
        cancelScheduledPassiveScan();
        passiveScanIdleTimer = executor.schedule(() -> {
            if (!activeScanEnabled) {
                logger.debug("Activate passive scan");
                bgEndProcedure();
                bgStartScanning(false, configuration.passiveScanInterval, configuration.passiveScanWindow);
            } else {
                logger.debug("Ignore passive scan activation as active scan is active");
            }
        }, configuration.passiveScanIdleTime, TimeUnit.MILLISECONDS);
    }

    private void cancelScheduledPassiveScan() {
        @Nullable
        Future<?> scanTimer = passiveScanIdleTimer;
        if (scanTimer != null) {
            scanTimer.cancel(true);
        }
    }

    private void startScheduledTasks() {
        schedulePassiveScan();
        discoveryTask = scheduler.scheduleWithFixedDelay(this::refreshDiscoveredDevices, 0, 10, TimeUnit.SECONDS);
    }

    private void stopScheduledTasks() {
        cancelScheduledPassiveScan();
        @Nullable
        ScheduledFuture<?> removeTask = removeInactiveDevicesTask;
        if (removeTask != null) {
            removeTask.cancel(true);
            removeTask = null;
        }
        @Nullable
        ScheduledFuture<?> discoverTask = discoveryTask;
        if (discoverTask != null) {
            discoverTask.cancel(true);
            discoverTask = null;
        }
    }

    private BlueGigaGetConnectionsResponse readMaxConnections() throws BlueGigaException {
        return sendCommandWithoutChecks(new BlueGigaGetConnectionsCommand(), BlueGigaGetConnectionsResponse.class);
    }

    private BlueGigaAddressGetResponse readAddress() throws BlueGigaException {
        return sendCommandWithoutChecks(new BlueGigaAddressGetCommand(), BlueGigaAddressGetResponse.class);
    }

    private BlueGigaGetInfoResponse readInfo() throws BlueGigaException {
        return sendCommandWithoutChecks(new BlueGigaGetInfoCommand(), BlueGigaGetInfoResponse.class);
    }

    private void updateThingProperties() throws BlueGigaException {
        BlueGigaGetInfoResponse infoResponse = readInfo();

        Map<String, String> properties = editProperties();
        properties.put(BluetoothBindingConstants.PROPERTY_MAXCONNECTIONS, Integer.toString(maxConnections));
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION,
                String.format("%d.%d", infoResponse.getMajor(), infoResponse.getMinor()));
        properties.put(Thing.PROPERTY_HARDWARE_VERSION, Integer.toString(infoResponse.getHardware()));
        properties.put(BlueGigaAdapterConstants.PROPERTY_PROTOCOL, Integer.toString(infoResponse.getProtocolVersion()));
        properties.put(BlueGigaAdapterConstants.PROPERTY_LINKLAYER, Integer.toString(infoResponse.getLlVersion()));
        updateProperties(properties);
    }

    private void closeSerialPort(SerialPort sp) {
        sp.removeEventListener();
        try {
            sp.disableReceiveTimeout();
        } catch (Exception e) {
            // Ignore all as RXTX seems to send arbitrary exceptions when BlueGiga module is detached
        } finally {
            outputStream.ifPresent(output -> {
                try {
                    output.close();
                } catch (IOException e) {
                }
            });
            inputStream.ifPresent(input -> {
                try {
                    input.close();
                } catch (IOException e) {
                }
            });
            sp.close();
            logger.debug("Closed serial port.");
            inputStream = Optional.empty();
            outputStream = Optional.empty();
        }
    }

    @Override
    public void scanStart() {
        super.scanStart();
        logger.debug("Start active scan");
        // Stop the passive scan
        cancelScheduledPassiveScan();
        bgEndProcedure();

        // Start an active scan
        bgStartScanning(true, configuration.activeScanInterval, configuration.activeScanWindow);
    }

    @Override
    public void scanStop() {
        super.scanStop();
        logger.debug("Stop active scan");

        // Stop the active scan
        bgEndProcedure();

        // Start a passive scan after idle delay
        schedulePassiveScan();
    }

    @Override
    public @Nullable BluetoothAddress getAddress() {
        BluetoothAddress addr = address;
        if (addr != null) {
            return addr;
        } else {
            throw new IllegalStateException("Adapter has not been initialized yet!");
        }
    }

    @Override
    protected BlueGigaBluetoothDevice createDevice(BluetoothAddress address) {
        return new BlueGigaBluetoothDevice(this, address, BluetoothAddressType.UNKNOWN);
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

        logger.debug("BlueGiga Connect: address {}.", address);

        // @formatter:off
        BlueGigaConnectDirectCommand command = new BlueGigaConnectDirectCommand.CommandBuilder()
                .withAddress(address.toString())
                .withAddrType(addressType)
                .withConnIntervalMin(configuration.connIntervalMin)
                .withConnIntervalMax(configuration.connIntervalMax)
                .withLatency(configuration.connLatency)
                .withTimeout(configuration.connTimeout)
                .build();
        // @formatter:on
        try {
            return sendCommand(command, BlueGigaConnectDirectResponse.class, true).getResult() == BgApiResponse.SUCCESS;
        } catch (BlueGigaException e) {
            logger.debug("Error occurred when sending connect command to device {}, reason: {}.", address,
                    e.getMessage());
            return false;
        }
    }

    /**
     * Close a connection using {@link BlueGigaDisconnectCommand}
     *
     * @param connectionHandle
     * @return
     */
    public boolean bgDisconnect(int connectionHandle) {
        logger.debug("BlueGiga Disconnect: connection {}", connectionHandle);
        BlueGigaDisconnectCommand command = new BlueGigaDisconnectCommand.CommandBuilder()
                .withConnection(connectionHandle).build();

        try {
            return sendCommand(command, BlueGigaDisconnectResponse.class, true).getResult() == BgApiResponse.SUCCESS;
        } catch (BlueGigaException e) {
            logger.debug("Error occurred when sending disconnect command to device {}, reason: {}.", address,
                    e.getMessage());
            return false;
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
        // @formatter:off
        BlueGigaReadByGroupTypeCommand command = new BlueGigaReadByGroupTypeCommand.CommandBuilder()
                .withConnection(connectionHandle)
                .withStart(1)
                .withEnd(65535)
                .withUuid(UUID.fromString("00002800-0000-1000-8000-00805F9B34FB"))
                .build();
        // @formatter:on
        try {
            return sendCommand(command, BlueGigaReadByGroupTypeResponse.class, true)
                    .getResult() == BgApiResponse.SUCCESS;
        } catch (BlueGigaException e) {
            logger.debug("Error occurred when sending read primary services command to device {}, reason: {}.", address,
                    e.getMessage());
            return false;
        }
    }

    /**
     * Start a read of all characteristics using {@link BlueGigaFindInformationCommand}
     *
     * @param connectionHandle
     * @return true if successful
     */
    public boolean bgFindCharacteristics(int connectionHandle) {
        logger.debug("BlueGiga Find: connection {}", connectionHandle);
        // @formatter:off
        BlueGigaFindInformationCommand command = new BlueGigaFindInformationCommand.CommandBuilder()
                .withConnection(connectionHandle)
                .withStart(1)
                .withEnd(65535)
                .build();
        // @formatter:on
        try {
            return sendCommand(command, BlueGigaFindInformationResponse.class, true)
                    .getResult() == BgApiResponse.SUCCESS;
        } catch (BlueGigaException e) {
            logger.debug("Error occurred when sending read characteristics command to device {}, reason: {}.", address,
                    e.getMessage());
            return false;
        }
    }

    public boolean bgReadCharacteristicDeclarations(int connectionHandle) {
        logger.debug("BlueGiga Find: connection {}", connectionHandle);
        // @formatter:off
        BlueGigaReadByTypeCommand command = new BlueGigaReadByTypeCommand.CommandBuilder()
                .withConnection(connectionHandle)
                .withStart(1)
                .withEnd(65535)
                .withUUID(BluetoothBindingConstants.ATTR_CHARACTERISTIC_DECLARATION)
                .build();
        // @formatter:on
        try {
            return sendCommand(command, BlueGigaReadByTypeResponse.class, true).getResult() == BgApiResponse.SUCCESS;
        } catch (BlueGigaException e) {
            logger.debug("Error occurred when sending read characteristics command to device {}, reason: {}.", address,
                    e.getMessage());
            return false;
        }
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
        // @formatter:off
        BlueGigaReadByHandleCommand command = new BlueGigaReadByHandleCommand.CommandBuilder()
                .withConnection(connectionHandle)
                .withChrHandle(handle)
                .build();
        // @formatter:on
        try {
            return sendCommand(command, BlueGigaReadByHandleResponse.class, true).getResult() == BgApiResponse.SUCCESS;
        } catch (BlueGigaException e) {
            logger.debug("Error occurred when sending read characteristics command to device {}, reason: {}.", address,
                    e.getMessage());
            return false;
        }
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
        // @formatter:off
        BlueGigaAttributeWriteCommand command = new BlueGigaAttributeWriteCommand.CommandBuilder()
                .withConnection(connectionHandle)
                .withAttHandle(handle)
                .withData(value)
                .build();
        // @formatter:on
        try {
            return sendCommand(command, BlueGigaAttributeWriteResponse.class, true)
                    .getResult() == BgApiResponse.SUCCESS;
        } catch (BlueGigaException e) {
            logger.debug("Error occurred when sending write characteristics command to device {}, reason: {}.", address,
                    e.getMessage());
            return false;
        }
    }

    /*
     * The following methods are private methods for handling the BlueGiga protocol
     */
    private boolean bgEndProcedure() {
        try {
            return sendCommandWithoutChecks(new BlueGigaEndProcedureCommand(), BlueGigaEndProcedureResponse.class)
                    .getResult() == BgApiResponse.SUCCESS;
        } catch (BlueGigaException e) {
            logger.debug("Error occurred when sending end procedure command.");
            return false;
        }
    }

    private boolean bgSetMode() {
        try {
            // @formatter:off
            BlueGigaSetModeCommand command = new BlueGigaSetModeCommand.CommandBuilder()
                    .withConnect(GapConnectableMode.GAP_NON_CONNECTABLE)
                    .withDiscover(GapDiscoverableMode.GAP_NON_DISCOVERABLE)
                    .build();
            // @formatter:on
            return sendCommandWithoutChecks(command, BlueGigaSetModeResponse.class)
                    .getResult() == BgApiResponse.SUCCESS;
        } catch (BlueGigaException e) {
            logger.debug("Error occurred when sending set mode command, reason: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Starts scanning on the dongle
     *
     * @param active true for active scanning
     */
    private boolean bgStartScanning(boolean active, int interval, int window) {
        try {
            // @formatter:off
            BlueGigaSetScanParametersCommand scanCommand = new BlueGigaSetScanParametersCommand.CommandBuilder()
                    .withActiveScanning(active)
                    .withScanInterval(interval)
                    .withScanWindow(window)
                    .build();
            // @formatter:on
            if (sendCommand(scanCommand, BlueGigaSetScanParametersResponse.class, false)
                    .getResult() == BgApiResponse.SUCCESS) {
                BlueGigaDiscoverCommand discoverCommand = new BlueGigaDiscoverCommand.CommandBuilder()
                        .withMode(GapDiscoverMode.GAP_DISCOVER_OBSERVATION).build();
                if (sendCommand(discoverCommand, BlueGigaDiscoverResponse.class, false)
                        .getResult() == BgApiResponse.SUCCESS) {
                    logger.debug("{} scanning successfully started.", active ? "Active" : "Passive");
                    return true;
                }
            }
        } catch (BlueGigaException e) {
            logger.debug("Error occurred when sending start scan command, reason: {}", e.getMessage());
        }
        logger.debug("Scan start failed.");
        return false;
    }

    /**
     * Send command only if initialization phase is successfully done
     */
    private <T extends BlueGigaResponse> T sendCommand(BlueGigaCommand command, Class<T> expectedResponse,
            boolean schedulePassiveScan) throws BlueGigaException {
        if (!initComplete) {
            throw new BlueGigaException("BlueGiga not initialized");
        }

        if (schedulePassiveScan) {
            cancelScheduledPassiveScan();
        }
        try {
            return sendCommandWithoutChecks(command, expectedResponse);
        } finally {
            if (schedulePassiveScan) {
                schedulePassiveScan();
            }
        }
    }

    /**
     * Forcefully send command without any checks
     */
    private <T extends BlueGigaResponse> T sendCommandWithoutChecks(BlueGigaCommand command, Class<T> expectedResponse)
            throws BlueGigaException {
        BlueGigaTransactionManager manager = transactionManager.getNow(null);
        if (manager != null) {
            return manager.sendTransaction(command, expectedResponse, COMMAND_TIMEOUT_MS);
        } else {
            throw new BlueGigaException("Transaction manager missing");
        }
    }

    /**
     * Add an event listener for the BlueGiga events
     *
     * @param listener the {@link BlueGigaEventListener} to add
     */
    public void addEventListener(BlueGigaEventListener listener) {
        transactionManager.thenAccept(manager -> {
            manager.addEventListener(listener);
        });
    }

    /**
     * Remove an event listener for the BlueGiga events
     *
     * @param listener the {@link BlueGigaEventListener} to remove
     */
    public void removeEventListener(BlueGigaEventListener listener) {
        transactionManager.thenAccept(manager -> {
            manager.removeEventListener(listener);
        });
    }

    @Override
    public void bluegigaEventReceived(@Nullable BlueGigaResponse event) {
        if (event instanceof BlueGigaScanResponseEvent scanEvent) {
            if (initComplete) {
                // We use the scan event to add any devices we hear to the devices list
                // The device gets created, and then manages itself for discovery etc.
                BluetoothAddress sender = new BluetoothAddress(scanEvent.getSender());
                BlueGigaBluetoothDevice device = getDevice(sender);
                device.setAddressType(scanEvent.getAddressType());
                deviceDiscovered(device);
            } else {
                logger.trace("Ignore BlueGigaScanResponseEvent as initialization is not complete");
            }
            return;
        }

        if (event instanceof BlueGigaConnectionStatusEvent connectionEvent) {
            connections.put(connectionEvent.getConnection(), new BluetoothAddress(connectionEvent.getAddress()));
        }

        if (event instanceof BlueGigaDisconnectedEvent disconnectedEvent) {
            connections.remove(disconnectedEvent.getConnection());
        }
    }

    @Override
    public void bluegigaClosed(Exception reason) {
        logger.debug("BlueGiga connection closed, request reinitialization, reason: {}", reason.getMessage());
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason.getMessage());
    }
}
