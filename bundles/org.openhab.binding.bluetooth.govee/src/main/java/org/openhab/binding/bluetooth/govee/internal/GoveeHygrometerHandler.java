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

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.measure.Quantity;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.binding.bluetooth.ConnectedBluetoothHandler;
import org.openhab.binding.bluetooth.gattserial.MessageServicer;
import org.openhab.binding.bluetooth.gattserial.SimpleGattSocket;
import org.openhab.binding.bluetooth.govee.internal.command.hygrometer.GetBatteryCommand;
import org.openhab.binding.bluetooth.govee.internal.command.hygrometer.GetOrSetHumCaliCommand;
import org.openhab.binding.bluetooth.govee.internal.command.hygrometer.GetOrSetHumWarningCommand;
import org.openhab.binding.bluetooth.govee.internal.command.hygrometer.GetOrSetTemCaliCommand;
import org.openhab.binding.bluetooth.govee.internal.command.hygrometer.GetOrSetTemWarningCommand;
import org.openhab.binding.bluetooth.govee.internal.command.hygrometer.GetTemHumCommand;
import org.openhab.binding.bluetooth.govee.internal.command.hygrometer.GoveeMessage;
import org.openhab.binding.bluetooth.govee.internal.command.hygrometer.TemHumDTO;
import org.openhab.binding.bluetooth.govee.internal.command.hygrometer.WarningSettingsDTO;
import org.openhab.binding.bluetooth.notification.BluetoothScanNotification;
import org.openhab.binding.bluetooth.util.HeritableFuture;
import org.openhab.binding.bluetooth.util.RetryException;
import org.openhab.binding.bluetooth.util.RetryFuture;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Connor Petty - Initial contribution
 * @author Matthias Bläsing - Fix reading advertisement data
 */
@NonNullByDefault
public class GoveeHygrometerHandler extends ConnectedBluetoothHandler {

    private static final UUID SERVICE_UUID = UUID.fromString("494e5445-4c4c-495f-524f-434b535f4857");
    private static final UUID PROTOCOL_CHAR_UUID = UUID.fromString("494e5445-4c4c-495f-524f-434b535f2011");
    private static final UUID KEEP_ALIVE_CHAR_UUID = UUID.fromString("494e5445-4c4c-495f-524f-434b535f2012");

    private final Logger logger = LoggerFactory.getLogger(GoveeHygrometerHandler.class);

    private final CommandSocket commandSocket = new CommandSocket();

    private GoveeHygrometerConfiguration config = new GoveeHygrometerConfiguration();
    private @Nullable GoveeModel model = null;// we use this as our default model

    private CompletableFuture<?> initializeJob = CompletableFuture.completedFuture(null);// initially set to a dummy
                                                                                         // future
    private Future<?> scanJob = CompletableFuture.completedFuture(null);
    private Future<?> keepAliveJob = CompletableFuture.completedFuture(null);

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

        logger.debug("Initializing Govee Hygrometer {}", address);
        initializeJob = RetryFuture.composeWithRetry(this::createInitSettingsJob, scheduler)//
                .thenRun(() -> {
                    updateStatus(ThingStatus.ONLINE);
                });
        scanJob = scheduler.scheduleWithFixedDelay(() -> {
            try {
                if (initializeJob.isDone() && !initializeJob.isCompletedExceptionally()) {
                    logger.debug("refreshing temperature, humidity, and battery");
                    refreshBattery().join();
                    refreshTemperatureAndHumidity().join();
                    disconnect();
                    updateStatus(ThingStatus.ONLINE);
                }
            } catch (RuntimeException ex) {
                logger.warn("unable to refresh", ex);
            }
        }, 0, config.refreshInterval, TimeUnit.SECONDS);
        keepAliveJob = scheduler.scheduleWithFixedDelay(() -> {
            if (device.getConnectionState() == ConnectionState.CONNECTED) {
                try {
                    GoveeMessage message = new GoveeMessage((byte) 0xAA, (byte) 1, null);
                    writeCharacteristic(SERVICE_UUID, KEEP_ALIVE_CHAR_UUID, message.getPayload(), false);
                } catch (RuntimeException ex) {
                    logger.warn("unable to send keep alive", ex);
                }
            }
        }, 1, 2, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        initializeJob.cancel(false);
        scanJob.cancel(false);
        keepAliveJob.cancel(false);
        super.dispose();
    }

    private CompletableFuture<@Nullable ?> createInitSettingsJob() {
        logger.debug("Initializing Govee Hygrometer {} settings", address);

        QuantityType<Temperature> temCali = config.getTemperatureCalibration();
        QuantityType<Dimensionless> humCali = config.getHumidityCalibration();
        WarningSettingsDTO<Temperature> temWarnSettings = config.getTemperatureWarningSettings();
        WarningSettingsDTO<Dimensionless> humWarnSettings = config.getHumidityWarningSettings();

        final CompletableFuture<@Nullable ?> parent = new HeritableFuture<>();
        CompletableFuture<@Nullable ?> future = parent;
        future.complete(null);

        if (temCali != null) {
            future = future.thenCompose(v -> {
                CompletableFuture<@Nullable QuantityType<Temperature>> caliFuture = parent.newIncompleteFuture();
                commandSocket.sendMessage(new GetOrSetTemCaliCommand(temCali, caliFuture));
                return caliFuture;
            });
        }
        if (humCali != null) {
            future = future.thenCompose(v -> {
                CompletableFuture<@Nullable QuantityType<Dimensionless>> caliFuture = parent.newIncompleteFuture();
                commandSocket.sendMessage(new GetOrSetHumCaliCommand(humCali, caliFuture));
                return caliFuture;
            });
        }
        if (getModel().supportsWarningBroadcast()) {
            future = future.thenCompose(v -> {
                CompletableFuture<@Nullable WarningSettingsDTO<Temperature>> temWarnFuture = parent
                        .newIncompleteFuture();
                commandSocket.sendMessage(new GetOrSetTemWarningCommand(temWarnSettings, temWarnFuture));
                return temWarnFuture;
            }).thenCompose(v -> {
                CompletableFuture<@Nullable WarningSettingsDTO<Dimensionless>> humWarnFuture = parent
                        .newIncompleteFuture();
                commandSocket.sendMessage(new GetOrSetHumWarningCommand(humWarnSettings, humWarnFuture));
                return humWarnFuture;
            });
        }

        // CompletableFuture.exceptionallyCompose isn't available yet so we have to compose it manually for now.
        CompletableFuture<@Nullable Void> retFuture = future.newIncompleteFuture();
        future.whenComplete((v, th) -> {
            if (th instanceof CompletionException) {
                th = th.getCause();
            }
            if (th instanceof RuntimeException) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Failed to initialize device: " + th.getMessage());
                retFuture.completeExceptionally(th);
            } else if (th != null) {
                logger.debug("Failure to initialize device: {}. Retrying in 30 seconds", th.getMessage());
                retFuture.completeExceptionally(new RetryException(30, TimeUnit.SECONDS));
            } else {
                retFuture.complete(null);
            }
        });
        return retFuture;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        super.handleCommand(channelUID, command);

        switch (channelUID.getId()) {
            case CHANNEL_ID_BATTERY:
                if (command == RefreshType.REFRESH) {
                    refreshBattery();
                }
                return;
            case CHANNEL_ID_TEMPERATURE:
            case CHANNEL_ID_HUMIDITY:
                if (command == RefreshType.REFRESH) {
                    refreshTemperatureAndHumidity();
                }
                return;
        }
    }

    private CompletableFuture<@Nullable ?> refreshBattery() {
        CompletableFuture<@Nullable QuantityType<Dimensionless>> future = new CompletableFuture<>();
        commandSocket.sendMessage(new GetBatteryCommand(future));
        future.whenCompleteAsync(this::updateBattery, scheduler);
        return future;
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

    private CompletableFuture<@Nullable ?> refreshTemperatureAndHumidity() {
        CompletableFuture<@Nullable TemHumDTO> future = new CompletableFuture<>();
        commandSocket.sendMessage(new GetTemHumCommand(future));
        future.whenCompleteAsync(this::updateTemperatureAndHumidity, scheduler);
        return future;
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
        commandSocket.receivePacket(value);
    }

    private class CommandSocket extends SimpleGattSocket<GoveeMessage> {

        @Override
        protected ScheduledExecutorService getScheduler() {
            return scheduler;
        }

        @Override
        public void sendMessage(MessageServicer<GoveeMessage, GoveeMessage> messageServicer) {
            logger.debug("sending message: {}", messageServicer.getClass().getSimpleName());
            super.sendMessage(messageServicer);
        }

        @Override
        protected void parsePacket(byte[] packet, Consumer<GoveeMessage> messageHandler) {
            messageHandler.accept(new GoveeMessage(packet));
        }

        @Override
        protected CompletableFuture<@Nullable Void> sendPacket(byte[] data) {
            return writeCharacteristic(SERVICE_UUID, PROTOCOL_CHAR_UUID, data, true);
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
