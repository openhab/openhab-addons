/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.govee.internal;

import static org.openhab.binding.bluetooth.govee.internal.GoveeBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.measure.Quantity;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BeaconBluetoothHandler;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.binding.bluetooth.BluetoothService;
import org.openhab.binding.bluetooth.govee.internal.command.hygrometer.GetBatteryCommand;
import org.openhab.binding.bluetooth.govee.internal.command.hygrometer.GetOrSetHumCaliCommand;
import org.openhab.binding.bluetooth.govee.internal.command.hygrometer.GetOrSetHumWarningCommand;
import org.openhab.binding.bluetooth.govee.internal.command.hygrometer.GetOrSetTemCaliCommand;
import org.openhab.binding.bluetooth.govee.internal.command.hygrometer.GetOrSetTemWarningCommand;
import org.openhab.binding.bluetooth.govee.internal.command.hygrometer.GetTemHumCommand;
import org.openhab.binding.bluetooth.govee.internal.command.hygrometer.GoveeCommand;
import org.openhab.binding.bluetooth.govee.internal.command.hygrometer.GoveeMessage;
import org.openhab.binding.bluetooth.govee.internal.command.hygrometer.TemHumDTO;
import org.openhab.binding.bluetooth.govee.internal.command.hygrometer.WarningSettingsDTO;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Connor Petty - Initial contribution
 * @author Matthias Bläsing - Fix reading advertisement data
 */
@NonNullByDefault
public class GoveeHygrometerHandler extends BeaconBluetoothHandler {

    private static final UUID PROTOCOL_CHAR_UUID = UUID.fromString("494e5445-4c4c-495f-524f-434b535f2011");
    private static final UUID UUID_SVC_GOVEE_AUTH = UUID.fromString("00010203-0405-0607-0809-0a0b0c0d1910");
    private static final UUID UUID_AUTH_NOTIFY = UUID.fromString("00010203-0405-0607-0809-0a0b0c0d2b10");
    private static final UUID UUID_AUTH_WRITE = UUID.fromString("00010203-0405-0607-0809-0a0b0c0d2b11");

    // the PSK is used for the initial handshake, which results in a session
    // key that is used for the further communication
    private static final byte[] PSK = "MakingLifeSmarte".getBytes(StandardCharsets.US_ASCII);

    private final Logger logger = LoggerFactory.getLogger(GoveeHygrometerHandler.class);

    private GoveeHygrometerConfiguration config = new GoveeHygrometerConfiguration();
    private @Nullable GoveeModel model = null;// we use this as our default model

    private Future<?> scanJob = CompletableFuture.completedFuture(null);

    private final Map<BluetoothCharacteristic, List<CompletableFuture<byte[]>>> characteristicListener = new ConcurrentHashMap<>();

    private final AtomicBoolean refreshRunning = new AtomicBoolean();

    public GoveeHygrometerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (thing.getStatus() == ThingStatus.OFFLINE) {
            // something went wrong in super.initialize() so we shouldn't initialize further here either
            return;
        }

        config = getConfigAs(GoveeHygrometerConfiguration.class);

        scanJob = scheduler.scheduleWithFixedDelay(() -> {
            try {
                refresh();
            } catch (RuntimeException ex) {
                logger.info("Refresh failed for Govee Device {}", address, ex);
            }
        }, 20, config.refreshInterval, TimeUnit.SECONDS);
    }

    private void refresh() {
        // Refresh does in sequence:
        //
        // - fetch temperature and humidity
        // - fetch battery state
        // - set calibration data (temperature and humidity correction values)
        // - set warning level data for temperature and humidiy
        //
        // The connection handshake was observered to be not 100% stable. The
        // measurement data is also send as bluetooth notifications, which seem
        // the be more stable The code for that can be found in
        // #onScanRecordReceived
        boolean wasRunning = refreshRunning.compareAndExchange(false, true);
        if (wasRunning) {
            logger.debug("Refresh is already running - skipping execution");
            return;
        }
        try {
            BluetoothDevice device = this.device;
            if (device == null) {
                logger.debug("Device not present - skipping execution");
                return;
            }
            logger.info("Refreshing Govee Hygrometer {}", address);
            device.connect();
            device.awaitConnection(30, TimeUnit.SECONDS);
            if (device.getConnectionState() != ConnectionState.CONNECTED) {
                logger.info("Failed to establish connection");
                return;
            }
            device.discoverServices();
            device.awaitServiceDiscovery(10, TimeUnit.SECONDS);

            EncryptionHelper encryptionHelper = encryptionHandshake();

            BluetoothCharacteristic characteristic = device.getCharacteristic(PROTOCOL_CHAR_UUID);

            QuantityType<Temperature> temCali = config.getTemperatureCalibration();
            QuantityType<Dimensionless> humCali = config.getHumidityCalibration();
            WarningSettingsDTO<Temperature> temWarnSettings = config.getTemperatureWarningSettings();
            WarningSettingsDTO<Dimensionless> humWarnSettings = config.getHumidityWarningSettings();

            if (characteristic != null) {
                logger.debug("Before enable notifications");
                CompletableFuture<?> future = device.enableNotifications(characteristic).thenCompose(v -> {
                    CompletableFuture<@Nullable TemHumDTO> resultHandler = new CompletableFuture<>();
                    logger.debug("Execute GetTemHumCommand");
                    executeCommand(encryptionHelper, characteristic, new GetTemHumCommand(resultHandler));
                    logger.debug("Executed GetTemHumCommand");
                    return resultHandler;
                }).handle((dto, th) -> {
                    logger.debug("Received temperature and humidity data: {}", dto, th);
                    updateTemperatureAndHumidity(dto, th);
                    logger.debug("Update of temperature and humidity is done");
                    return dto;
                }).thenCompose(v -> {
                    CompletableFuture<@Nullable QuantityType<Dimensionless>> resultHandler = new CompletableFuture<>();
                    logger.debug("Execute GetBatteryCommand");
                    executeCommand(encryptionHelper, characteristic, new GetBatteryCommand(resultHandler));
                    logger.debug("Executed GetBatteryCommand");
                    return resultHandler;
                }).handle((dto, th) -> {
                    logger.debug("Received battery data: {}", dto);
                    updateBattery(dto, th);
                    logger.debug("Update of battery data is done");
                    return dto;
                });
                if (temCali != null) {
                    future = future.thenCompose(v -> {
                        CompletableFuture<@Nullable QuantityType<Temperature>> caliFuture = new CompletableFuture<>();
                        logger.debug("Execute GetOrSetTemCaliCommand");
                        executeCommand(encryptionHelper, characteristic,
                                new GetOrSetTemCaliCommand(temCali, caliFuture));
                        logger.debug("Executed GetOrSetTemCaliCommand");
                        return caliFuture;
                    });
                }
                if (humCali != null) {
                    future = future.thenCompose(v -> {
                        CompletableFuture<@Nullable QuantityType<Dimensionless>> caliFuture = new CompletableFuture<>();
                        logger.debug("Execute GetOrSetHumCaliCommand");
                        executeCommand(encryptionHelper, characteristic,
                                new GetOrSetHumCaliCommand(humCali, caliFuture));
                        logger.debug("Executed GetOrSetHumCaliCommand");
                        return caliFuture;
                    });
                }
                if (getModel().supportsWarningBroadcast()) {
                    future = future.thenCompose(v -> {
                        CompletableFuture<@Nullable WarningSettingsDTO<Temperature>> temWarnFuture = new CompletableFuture<>();
                        logger.debug("Execute GetOrSetTemWarningCommand");
                        executeCommand(encryptionHelper, characteristic,
                                new GetOrSetTemWarningCommand(temWarnSettings, temWarnFuture));
                        logger.debug("Executed GetOrSetTemWarningCommand");
                        return temWarnFuture;
                    });
                    future = future.thenCompose(v -> {
                        CompletableFuture<@Nullable WarningSettingsDTO<Dimensionless>> humWarnFuture = new CompletableFuture<>();
                        logger.debug("Execute GetOrSetHumWarningCommand");
                        executeCommand(encryptionHelper, characteristic,
                                new GetOrSetHumWarningCommand(humWarnSettings, humWarnFuture));
                        logger.debug("Executed GetOrSetHumWarningCommand");
                        return humWarnFuture;
                    });
                }
                future = future.thenCompose(v -> {
                    logger.debug("Execute disableNotifications");
                    CompletableFuture<@Nullable Void> result = device.disableNotifications(characteristic);
                    logger.debug("Executed disableNotifications");
                    return result;
                });
                future.get();
                logger.debug("Refresh done");
            }
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            logger.warn("Failed to run refresh", ex);
        } finally {
            if (device.getConnectionState() == ConnectionState.CONNECTED) {
                device.disconnect();
            }
            logger.debug("Refresh done (finally-block)");
            refreshRunning.set(false);
        }
    }

    private CompletableFuture<@Nullable Void> executeCommand(EncryptionHelper encryptionHelper,
            BluetoothCharacteristic characteristic, GoveeCommand command) {
        // Register the listener for the reply to the command
        // this will wait for max. 5 seconds for the reply
        // from the device.
        AtomicReference<CompletableFuture<@Nullable Void>> ref = new AtomicReference<>();
        ref.set(listenForCharacteristicUpdate(characteristic).orTimeout(5, TimeUnit.SECONDS).handle((data, th) -> {
            logger.debug("executeCommand, running notification listener: {}, {}", ref.get(), data, th);
            try {
                if (data != null) {
                    byte[] decryptedData = encryptionHelper.decrypt(data);
                    GoveeMessage reply = new GoveeMessage(decryptedData);
                    if (command.matches(reply)) {
                        command.handleResponse(reply.getData(), th);
                    }
                }
                if (th != null) {
                    command.handleResponse(null, th);
                } else {
                    command.handleResponse(null, new IllegalArgumentException("Received data did not match command "
                            + command + ": " + HexFormat.ofDelimiter(" ").formatHex(data)));
                }
            } catch (Exception ex) {
                logger.debug("executeCommand, notification listener raised exception", ex);
                command.handleResponse(null, ex);
            }
            return null;
        }));
        logger.debug("Resolve completable future: {}", ref.get());
        return device
                .writeCharacteristic(characteristic, encryptionHelper.encrypt(command.createMessage().getPayload()))
                .thenRun(() -> logger.debug("Write executed: {}", characteristic.getUuid()))
                .thenCombine(ref.get(), (a, b) -> null);
    }

    @Override
    public void dispose() {
        scanJob.cancel(false);
        super.dispose();
    }

    /**
     * Handle the encryption handshake required to communicate with the device.
     * Newer Govee devices require the communication over the
     * {@link #PROTOCOL_CHAR_UUID} characteristic to be encrypted. This method
     * does the handshake and yields a helper that handles the encryption. If
     * it is determined, that this device does not support encryption, a NOOP
     * implementation is returned.
     *
     * @return EncryptionHelper to use for this session. This is never {@code null}.
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    private EncryptionHelper encryptionHandshake() throws InterruptedException, ExecutionException, TimeoutException {
        BluetoothService service = device.getServices(UUID_SVC_GOVEE_AUTH);
        // For devices that don't support/require encryption and thus don't offer
        // the authentication service, return a replacement, that just copys the
        // input
        if (service == null) {
            return EncryptionHelper.NOOP;
        }

        BluetoothCharacteristic authNotifyInput = service.getCharacteristic(UUID_AUTH_NOTIFY);
        BluetoothCharacteristic authWrite = service.getCharacteristic(UUID_AUTH_WRITE);

        if (authNotifyInput == null || authWrite == null) {
            return EncryptionHelper.NOOP;
        }

        BluetoothCharacteristic authNotify = authNotifyInput;

        EncryptionHelper pskEncryption = new EncryptionHelper(PSK);

        byte[] tx1 = createTxPacket((byte) 1);
        byte[] encryptedTx1 = pskEncryption.encrypt(tx1);

        byte[] tx2 = createTxPacket((byte) 2);
        byte[] encryptedTx2 = pskEncryption.encrypt(tx2);

        logger.debug("Enable notifications for {}", authNotify);
        device.enableNotifications(authNotify).get();

        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Sending TX1: {}", HexFormat.ofDelimiter(" ").formatHex(tx1));
            }
            CompletableFuture<byte[]> result = listenForCharacteristicUpdate(authNotify);
            device.writeCharacteristic(authWrite, encryptedTx1);
            byte[] rx1 = pskEncryption.decrypt(result.get(30, TimeUnit.SECONDS));
            if (logger.isDebugEnabled()) {
                logger.debug("Received as RX1: {}", HexFormat.ofDelimiter(" ").formatHex(rx1));
            }
            if (rx1[0] != ((byte) 0xE7) || rx1[1] != ((byte) 0x01)) {
                throw new IllegalStateException("Encryption Handshake failed");
            }
            byte[] sessionKey = Arrays.copyOfRange(rx1, 2, 18);
            if (logger.isDebugEnabled()) {
                logger.debug("Sending TX2: {}", HexFormat.ofDelimiter(" ").formatHex(tx2));
            }
            result = listenForCharacteristicUpdate(authNotify);
            device.writeCharacteristic(authWrite, encryptedTx2);
            byte[] rx2 = pskEncryption.decrypt(result.get(30, TimeUnit.SECONDS));
            if (logger.isDebugEnabled()) {
                logger.debug("Received as RX2: {}", HexFormat.ofDelimiter(" ").formatHex(rx2));
            }
            if (rx2[0] != ((byte) 0xE7) || rx2[1] != ((byte) 0x02)) {
                throw new IllegalStateException("Encryption Handshake failed (2)");
            }
            return new EncryptionHelper(sessionKey);
        } finally {
            device.disableNotifications(authNotify).get();
        }
    }

    private byte[] createTxPacket(byte phase) {
        byte[] tx = new byte[20];
        tx[0] = (byte) 0xE7;
        tx[1] = phase;
        for (int i = 0; i < (tx.length - 1); i++) {
            tx[19] ^= tx[i];
        }
        return tx;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        switch (channelUID.getId()) {
            case CHANNEL_ID_BATTERY, CHANNEL_ID_TEMPERATURE, CHANNEL_ID_HUMIDITY -> {
                if (command == RefreshType.REFRESH) {
                    refresh();
                }
            }
        }
    }

    private void updateBattery(@Nullable QuantityType<Dimensionless> result, @Nullable Throwable th) {
        if (th != null) {
            logger.debug("Failed to get battery: {}", th.getMessage());
        }
        if (result == null) {
            return;
        }
        updateState(CHANNEL_ID_BATTERY, result);
    }

    private void updateTemperatureAndHumidity(@Nullable TemHumDTO result, @Nullable Throwable th) {
        if (th != null) {
            logger.debug("Failed to get temperature/humidity: {}", th.getMessage());
        }
        if (result == null) {
            return;
        }
        QuantityType<Temperature> tem = result.temperature;
        QuantityType<Dimensionless> hum = result.humidity;
        if (tem == null || hum == null) {
            return;
        }
        updateState(CHANNEL_ID_TEMPERATURE, tem);
        updateState(CHANNEL_ID_HUMIDITY, hum);
        if (getModel().supportsWarningBroadcast()) {
            updateAlarm(CHANNEL_ID_TEMPERATURE_ALARM, tem, config.getTemperatureWarningSettings());
            updateAlarm(CHANNEL_ID_HUMIDITY_ALARM, hum, config.getHumidityWarningSettings());
        }
    }

    private <T extends Quantity<T>> void updateAlarm(String channelName, QuantityType<T> quantity,
            WarningSettingsDTO<T> settings) {
        boolean outOfRange = quantity.compareTo(settings.min) < 0 || settings.max.compareTo(quantity) < 0;
        updateState(channelName, OnOffType.from(outOfRange));
    }

    @Override
    public void onScanRecordReceived(BluetoothScanNotification scanNotification) {
        super.onScanRecordReceived(scanNotification);
        byte[] scanData = scanNotification.getManufacturerData();
        GoveeModel.ManufacturerDataSet manufacturerDataset = getModel().parseManufacturerData(scanData);

        if (manufacturerDataset != null) {
            updateTemHumBattery(manufacturerDataset);
        }
    }

    private void updateTemHumBattery(GoveeModel.ManufacturerDataSet manufacturerDataset) {
        if (Short.toUnsignedInt(manufacturerDataset.temperature()) == 0xFFFF
                || manufacturerDataset.humidity() == 0xFFFF) {
            logger.trace("Govee device [{}] received invalid data", this.address);
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Govee device [{}] received broadcast: tem = {}, hum = {}, battery = {}, wifiLevel = {}",
                    this.address, manufacturerDataset.temperature(), manufacturerDataset.humidity(),
                    manufacturerDataset.battery(), manufacturerDataset.wifiLevel());
        }

        if (manufacturerDataset.temperature() == 0 && manufacturerDataset.humidity() == 0
                && manufacturerDataset.battery() == 0) {
            logger.trace("Govee device [{}] values are zero", this.address);
            return;
        }
        if (manufacturerDataset.temperature() < -4000 || manufacturerDataset.temperature() > 10000) {
            logger.trace("Govee device [{}] invalid temperature value: {}", this.address,
                    manufacturerDataset.temperature());
            return;
        }
        if (manufacturerDataset.humidity() > 10000) {
            logger.trace("Govee device [{}] invalid humidity valie: {}", this.address, manufacturerDataset.humidity());
            return;
        }

        TemHumDTO temhum = new TemHumDTO();
        temhum.temperature = new QuantityType<>(manufacturerDataset.temperature() / 100.0, SIUnits.CELSIUS);
        temhum.humidity = new QuantityType<>(manufacturerDataset.humidity() / 100.0, Units.PERCENT);
        updateTemperatureAndHumidity(temhum, null);

        updateBattery(new QuantityType<>(manufacturerDataset.battery(), Units.PERCENT), null);
    }

    @Override
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic, byte[] value) {
        super.onCharacteristicUpdate(characteristic, value);
        if (logger.isDebugEnabled()) {
            logger.debug("Notification: {} // {}", characteristic.getUuid(),
                    HexFormat.ofDelimiter(" ").formatHex(value));
        }
        characteristicListener.getOrDefault(characteristic, List.of()).forEach(cf -> {
            logger.debug("Delivering to {}", cf);
            cf.complete(value);
        });
    }

    private CompletableFuture<byte[]> listenForCharacteristicUpdate(BluetoothCharacteristic characteristic) {
        CompletableFuture<byte[]> result = new CompletableFuture<>();
        result.handle((innerResult, throwable) -> {
            if (logger.isDebugEnabled()) {
                logger.debug("Removing characteristicListener for {}, {}", characteristic.getUuid(),
                        HexFormat.ofDelimiter(" ").formatHex(innerResult), throwable);
            }
            Optional.ofNullable(characteristicListener.get(characteristic)).ifPresent(list -> list.remove(result));
            return innerResult;
        });
        logger.debug("listenForCharacteristicUpdate future: {}", result);
        characteristicListener
                .computeIfAbsent(characteristic, c -> new CopyOnWriteArrayList<CompletableFuture<byte[]>>())
                .add(result);
        return result;
    }

    @Override
    public void onConnectionStateChange(BluetoothConnectionStatusNotification connectionNotification) {
        super.onConnectionStateChange(connectionNotification);
        if (connectionNotification.getConnectionState() == ConnectionState.DISCONNECTED) {
            characteristicListener.values().stream().flatMap(l -> l.stream()).forEach(cf -> {
                logger.debug("Canceling: {}", cf);
                cf.cancel(true);
            });
            characteristicListener.clear();
        }
    }

    private GoveeModel getModel() {
        GoveeModel result = model;
        if (result == null) {
            BluetoothDevice dev = device;
            if (dev == null) {
                result = GoveeModel.H5074;
            } else {
                GoveeModel model = GoveeModel.getGoveeModel(dev);
                if (model != null) {
                    this.model = model;
                    result = model;
                } else {
                    result = GoveeModel.H5074;
                }
            }
        }
        return result;
    }
}
