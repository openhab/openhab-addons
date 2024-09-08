/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.hdpowerview.internal.shade;

import static org.openhab.binding.bluetooth.hdpowerview.internal.ShadeBindingConstants.*;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BeaconBluetoothHandler;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCharacteristic.GattCharacteristic;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.binding.bluetooth.BluetoothService;
import org.openhab.binding.bluetooth.BluetoothUtils;
import org.openhab.binding.bluetooth.ConnectionException;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase;
import org.openhab.binding.hdpowerview.internal.database.ShadeCapabilitiesDatabase.Capabilities;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ShadeHandler} is a thing handler for Hunter Douglas Powerview Shades using Bluetooth Low Energy (BLE).
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShadeHandler extends BeaconBluetoothHandler {

    private static final String ENCRYPTION_KEY_HELP_URL = //
            "https://www.openhab.org/addons/bindings/bluetooth.hdpowerview/readme.html#encryption-key";

    private static final ShadeCapabilitiesDatabase CAPABILITIES_DATABASE = new ShadeCapabilitiesDatabase();
    private static final Map<Integer, String> HOME_ID_ENCRYPTION_KEYS = new ConcurrentHashMap<>();

    private final Logger logger = LoggerFactory.getLogger(ShadeHandler.class);
    private final List<Future<?>> readTasks = new ArrayList<>();
    private final Map<Instant, Future<?>> writeTasks = new ConcurrentHashMap<>();
    private final ShadeDataReader dataReader = new ShadeDataReader();

    private @Nullable Capabilities capabilities;
    private @Nullable Future<?> readBatteryTask;

    private byte[] cachedValue = new byte[0];
    private Instant activityTimeout = Instant.MIN;
    private ShadeConfiguration configuration = new ShadeConfiguration();
    private boolean propertiesLoaded = false;
    private byte writeSequence = Byte.MIN_VALUE;
    private int homeId;

    public ShadeHandler(Thing thing) {
        super(thing);
    }

    /**
     * Cancel the given task
     */
    private void cancelTask(@Nullable Future<?> task, boolean interrupt) {
        if (task != null) {
            task.cancel(interrupt);
        }
    }

    /**
     * Cancel all tasks
     */
    private void cancelTasks(boolean interrupt) {
        readTasks.forEach(task -> cancelTask(task, interrupt));
        writeTasks.values().forEach(task -> cancelTask(task, interrupt));
        cancelTask(readBatteryTask, interrupt);
        readBatteryTask = null;
        readTasks.clear();
        writeTasks.clear();
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
        if (CHANNEL_SHADE_BATTERY_LEVEL.equals(channelUID.getId())) {
            scheduleReadBattery();
        }
    }

    /**
     * Connect the device and download its services (if not already done). Blocks until the operation completes.
     */
    private void connectAndWait() throws TimeoutException, InterruptedException, ConnectionException {
        if (device.getConnectionState() != ConnectionState.CONNECTED) {
            if (device.getConnectionState() != ConnectionState.CONNECTING) {
                if (!device.connect()) {
                    throw new ConnectionException("Failed to start connecting");
                }
            }
            if (!device.awaitConnection(configuration.bleTimeout, TimeUnit.SECONDS)) {
                throw new TimeoutException("Connection attempt timeout");
            }
        }
        if (!device.isServicesDiscovered()) {
            device.discoverServices();
            if (!device.awaitServiceDiscovery(configuration.bleTimeout, TimeUnit.SECONDS)) {
                throw new TimeoutException("Service discovery timeout");
            }
        }
    }

    @Override
    public void dispose() {
        cancelTasks(true);
        super.dispose();
    }

    /**
     * Get the key for encrypting write commands. Uses either..
     *
     * <li>The key for this specific Thing via its own configuration properties, or</li>
     * <li>The key for any other Thing with the same homeId via the shared ENCRYPTION_KEYS map</li>
     */
    private @Nullable String getEncryptionKey() {
        String key = null;
        if (homeId != 0) {
            key = configuration.encryptionKey;
            key = key.isBlank() ? HOME_ID_ENCRYPTION_KEYS.get(homeId) : key;
            if (key == null || key.isBlank()) {
                logger.warn("Device '{}' requires an encryption key => see {}", device.getAddress(),
                        ENCRYPTION_KEY_HELP_URL);
            } else {
                HOME_ID_ENCRYPTION_KEYS.putIfAbsent(homeId, key);
                if (!configuration.encryptionKey.equals(key)) {
                    configuration.encryptionKey = key;
                    Configuration config = getConfig();
                    config.put(PROPERTY_ENCRYPTION_KEY, key);
                    updateConfiguration(config);
                }
            }
        }
        return key;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command commandArg) {
        super.handleCommand(channelUID, commandArg);

        if (commandArg == RefreshType.REFRESH) {
            switch (channelUID.getId()) {
                case CHANNEL_SHADE_BATTERY_LEVEL:
                    scheduleReadBattery();
                    break;

                default:
                    break;
            }
            return;
        }

        Command command = commandArg;

        // convert stop commands to (current) position commands
        if (command instanceof StopMoveType stopMove) {
            if (StopMoveType.STOP == stopMove) {
                switch (channelUID.getId()) {
                    case CHANNEL_SHADE_PRIMARY:
                        command = dataReader.getPrimary();
                        break;
                    case CHANNEL_SHADE_SECONDARY:
                        command = dataReader.getSecondary();
                        break;
                    case CHANNEL_SHADE_TILT:
                        command = dataReader.getTilt();
                        break;
                }
            }
        }

        // convert up/down commands to position command
        if (command instanceof UpDownType updown) {
            command = UpDownType.DOWN == updown ? PercentType.ZERO : PercentType.HUNDRED;
        }

        if (command instanceof PercentType percent) {
            Capabilities capabilities = this.capabilities;
            if (capabilities == null) {
                return;
            }

            try {
                switch (channelUID.getId()) {
                    case CHANNEL_SHADE_PRIMARY:
                        if (capabilities.supportsPrimary()) {
                            scheduleWritePosition(new ShadeDataWriter().withSequence(writeSequence++)
                                    .withPrimary(percent.doubleValue()).getEncrypted(getEncryptionKey()));
                        }
                        break;

                    case CHANNEL_SHADE_SECONDARY:
                        if (capabilities.supportsSecondary()) {
                            scheduleWritePosition(new ShadeDataWriter().withSequence(writeSequence++)
                                    .withSecondary(percent.doubleValue()).getEncrypted(getEncryptionKey()));
                        }
                        break;

                    case CHANNEL_SHADE_TILT:
                        if (capabilities.supportsTiltOnClosed() || capabilities.supportsTilt180()
                                || capabilities.supportsTiltAnywhere()) {
                            scheduleWritePosition(new ShadeDataWriter().withSequence(writeSequence++)
                                    .withTilt(percent.doubleValue()).getEncrypted(getEncryptionKey()));
                        }
                        break;
                }
            } catch (InvalidKeyException | IllegalArgumentException | NoSuchAlgorithmException | NoSuchPaddingException
                    | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException e) {
                logger.warn("handleCommand() device={} error={}", device.getAddress(), e.getMessage(),
                        logger.isDebugEnabled() ? e : null);
            }
        }
    }

    @Override
    public void initialize() {
        super.initialize();
        configuration = getConfigAs(ShadeConfiguration.class);
        try {
            new BluetoothAddress(configuration.address);
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            return;
        }
        updateProperty(PROPERTY_HOME_ID, Integer.toHexString(homeId).toUpperCase());
        activityTimeout = Instant.now().plusSeconds(configuration.pollingDelay * 2);

        cancelTasks(false);

        int initialDelaySeconds = 0;
        readTasks.add(scheduler.scheduleWithFixedDelay(() -> readThingStatus(), ++initialDelaySeconds,
                configuration.heartbeatDelay, TimeUnit.SECONDS));
        readTasks.add(scheduler.scheduleWithFixedDelay(() -> readProperties(), ++initialDelaySeconds,
                configuration.heartbeatDelay, TimeUnit.SECONDS));
        readTasks.add(scheduler.scheduleWithFixedDelay(() -> readBattery(), ++initialDelaySeconds,
                configuration.pollingDelay, TimeUnit.SECONDS));
    }

    @Override
    protected void onActivity() {
        super.onActivity();
        if (thing.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        }
        activityTimeout = Instant.now().plusSeconds(configuration.pollingDelay * 2);
    }

    /**
     * Process the scan record and update the channels.
     */
    @Override
    public void onScanRecordReceived(BluetoothScanNotification scanNotification) {
        super.onScanRecordReceived(scanNotification);
        onActivity();
        byte[] value = scanNotification.getManufacturerData();
        if (Arrays.equals(cachedValue, value)) {
            return;
        }
        cachedValue = value;
        if (logger.isDebugEnabled()) {
            logger.debug("onScanRecordReceived() device={} received value={}", device.getAddress(),
                    HexUtils.bytesToHex(value, ":"));
        }
        updatePosition(value);
    }

    @Override
    public void onServicesDiscovered() {
        super.onServicesDiscovered();
        scheduleReadBattery();
    }

    /**
     * Read the battery state. Blocks until the operation completes.
     */
    private void readBattery() {
        synchronized (this) {
            if (device.isServicesDiscovered()) {
                try {
                    connectAndWait();
                    for (BluetoothService service : device.getServices()) {
                        BluetoothCharacteristic characteristic = service
                                .getCharacteristic(GattCharacteristic.BATTERY_LEVEL.getUUID());
                        if (characteristic != null && characteristic.canRead()) {
                            byte[] value = device.readCharacteristic(characteristic).get(configuration.bleTimeout,
                                    TimeUnit.SECONDS);
                            if (logger.isDebugEnabled()) {
                                logger.debug("readBattery() device={} read uuid={}, value={}", device.getAddress(),
                                        characteristic.getUuid(), HexUtils.bytesToHex(value, ":"));
                            }
                            updateState(CHANNEL_SHADE_BATTERY_LEVEL,
                                    value.length > 0 ? QuantityType.valueOf(value[0], Units.PERCENT) : UnDefType.UNDEF);
                            onActivity();
                        }
                    }
                } catch (ConnectionException | TimeoutException | ExecutionException | InterruptedException e) {
                    // Bluetooth has frequent errors so we do not normally log them
                    logger.debug("readBattery() device={}, error={}", device.getAddress(), e.getMessage());
                }
            }
        }
    }

    /**
     * Read the thing properties. Blocks until the operation completes.
     */
    private void readProperties() {
        synchronized (this) {
            if (!propertiesLoaded && device.isServicesDiscovered()) {
                Map<String, String> properties = new HashMap<>();
                try {
                    connectAndWait();
                    for (BluetoothService service : device.getServices()) {
                        for (Entry<UUID, String> property : MAP_UID_PROPERTY_NAMES.entrySet()) {
                            BluetoothCharacteristic characteristic = service.getCharacteristic(property.getKey());
                            if (characteristic != null && characteristic.canRead()) {
                                byte[] value = device.readCharacteristic(characteristic).get(configuration.bleTimeout,
                                        TimeUnit.SECONDS);
                                if (logger.isDebugEnabled()) {
                                    logger.debug("readProperties() device={} read uuid={}, value={}",
                                            device.getAddress(), characteristic.getUuid(),
                                            HexUtils.bytesToHex(value, ":"));
                                }
                                String propertyName = property.getValue();
                                String propertyValue = BluetoothUtils.getStringValue(value, 0);
                                if (propertyValue != null) {
                                    properties.put(propertyName, propertyValue);
                                }
                            }
                        }
                    }
                } catch (ConnectionException | TimeoutException | ExecutionException | InterruptedException e) {
                    // Bluetooth has frequent errors so we do not normally log them
                    logger.debug("readProperties() device={}, error={}", device.getAddress(), e.getMessage());
                } finally {
                    if (!properties.isEmpty()) {
                        propertiesLoaded = true;
                        properties.put(Thing.PROPERTY_MAC_ADDRESS, device.getAddress().toString());
                        thing.setProperties(properties);
                        onActivity();
                    }
                }
            }
        }
    }

    /**
     * Read the Bluetooth services. Blocks until the operation completes.
     */
    private void readServices() {
        synchronized (this) {
            if (!device.isServicesDiscovered()) {
                try {
                    connectAndWait();
                    onActivity();
                } catch (ConnectionException | TimeoutException | InterruptedException e) {
                    // Bluetooth has frequent errors so we do not normally log them
                    logger.debug("readServices() device={}, error={}", device.getAddress(), e.getMessage());
                }
            }
        }
    }

    /**
     * Heartbeat task. Updates the online state and ensures that services are loaded.
     */
    private void readThingStatus() {
        if (thing.getStatus() == ThingStatus.ONLINE) {
            if (Instant.now().isAfter(activityTimeout)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            } else {
                readServices();
            }
        }
    }

    /**
     * Schedule a readBattery command
     */
    private void scheduleReadBattery() {
        cancelTask(readBatteryTask, false);
        readBatteryTask = scheduler.submit(() -> readBattery());
    }

    /**
     * Schedule a writePosition command with the given value
     */
    private void scheduleWritePosition(byte[] value) {
        Instant taskId = Instant.now();
        writeTasks.put(taskId, scheduler.submit(() -> writePosition(taskId, value)));
    }

    /**
     * Update homeId and if necessary update the encryption key.
     */
    private void updateHomeId(int newHomeId) {
        if (homeId != newHomeId) {
            homeId = newHomeId;
            updateProperty(PROPERTY_HOME_ID, Integer.toHexString(homeId).toUpperCase());
            getEncryptionKey();
        }
    }

    /**
     * Update the position channels
     */
    private void updatePosition(byte[] value) {
        logger.debug("updatePosition() device={}", device.getAddress());
        dataReader.setBytes(value);
        updateHomeId(dataReader.getHomeId());

        Capabilities capabilities = this.capabilities;
        if (capabilities == null) {
            capabilities = CAPABILITIES_DATABASE.getCapabilities(dataReader.getTypeId(), null);
            this.capabilities = capabilities;

            // remove unused channels
            List<Channel> removeChannels = new ArrayList<>();
            Channel channel;
            if (!capabilities.supportsPrimary()) {
                channel = thing.getChannel(CHANNEL_SHADE_PRIMARY);
                if (channel != null) {
                    removeChannels.add(channel);
                }
            }
            if (!capabilities.supportsSecondary()) {
                channel = thing.getChannel(CHANNEL_SHADE_SECONDARY);
                if (channel != null) {
                    removeChannels.add(channel);
                }
            }
            if (!(capabilities.supportsTilt180() || capabilities.supportsTiltAnywhere()
                    || capabilities.supportsTiltOnClosed())) {
                channel = thing.getChannel(CHANNEL_SHADE_TILT);
                if (channel != null) {
                    removeChannels.add(channel);
                }
            }
            if (!removeChannels.isEmpty()) {
                updateThing(editThing().withoutChannels(removeChannels).build());
            }
        }

        // update channel states
        if (capabilities.supportsPrimary()) {
            updateState(CHANNEL_SHADE_PRIMARY, dataReader.getPrimary());
        }
        if (capabilities.supportsSecondary()) {
            updateState(CHANNEL_SHADE_SECONDARY, dataReader.getSecondary());
        }
        if (capabilities.supportsTilt180() || capabilities.supportsTiltAnywhere()
                || capabilities.supportsTiltOnClosed()) {
            updateState(CHANNEL_SHADE_TILT, dataReader.getTilt());
        }
    }

    /**
     * Write position channel value task. Blocks until the operation completes.
     *
     * @param taskId identifies the task entry in the writeTasks map
     * @param value the data to write
     */
    private void writePosition(Instant taskId, byte[] value) {
        synchronized (this) {
            try {
                if (device.isServicesDiscovered()) {
                    connectAndWait();
                    BluetoothService shadeService = device.getServices(UUID_SERVICE_SHADE);
                    if (shadeService != null) {
                        BluetoothCharacteristic characteristic = shadeService
                                .getCharacteristic(UUID_CHARACTERISTIC_POSITION);
                        if (characteristic != null) {
                            device.writeCharacteristic(characteristic, value).get(configuration.bleTimeout,
                                    TimeUnit.SECONDS);
                            if (logger.isDebugEnabled()) {
                                logger.debug("writePosition() device={} sent uuid={}, value={}", device.getAddress(),
                                        characteristic.getUuid(), HexUtils.bytesToHex(value, ":"));
                            }
                            onActivity();
                        }
                    }
                }
            } catch (ConnectionException | TimeoutException | ExecutionException | InterruptedException e) {
                // Bluetooth has frequent errors so we do not normally log them
                logger.debug("writePosition() device={}, error={}", device.getAddress(), e.getMessage());
            } finally {
                writeTasks.remove(taskId);
            }
        }
    }
}
