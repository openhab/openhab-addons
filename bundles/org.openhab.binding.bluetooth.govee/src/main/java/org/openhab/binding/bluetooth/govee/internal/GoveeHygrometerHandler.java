/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
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
 *
 */
@NonNullByDefault
public class GoveeHygrometerHandler extends ConnectedBluetoothHandler {

    private static final UUID SERVICE_UUID = UUID.fromString("494e5445-4c4c-495f-524f-434b535f4857");
    private static final UUID PROTOCOL_CHAR_UUID = UUID.fromString("494e5445-4c4c-495f-524f-434b535f2011");
    private static final UUID KEEP_ALIVE_CHAR_UUID = UUID.fromString("494e5445-4c4c-495f-524f-434b535f2012");

    private static final byte[] SCAN_HEADER = { (byte) 0xFF, (byte) 0x88, (byte) 0xEC };

    private final Logger logger = LoggerFactory.getLogger(GoveeHygrometerHandler.class);

    private final CommandSocket commandSocket = new CommandSocket();

    private GoveeHygrometerConfiguration config = new GoveeHygrometerConfiguration();
    private GoveeModel model = GoveeModel.H5074;// we use this as our default model

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

        Map<String, String> properties = thing.getProperties();
        String modelProp = properties.get(Thing.PROPERTY_MODEL_ID);
        model = GoveeModel.H5074;
        if (modelProp != null) {
            try {
                model = GoveeModel.valueOf(modelProp);
            } catch (IllegalArgumentException ex) {
                // ignore
            }
        }

        logger.debug("Initializing Govee Hygrometer {} model: {}", address, model);
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
        if (model.supportsWarningBroadcast()) {
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
        if (model.supportsWarningBroadcast()) {
            updateAlarm(CHANNEL_ID_TEMPERATURE_ALARM, tem, config.getTemperatureWarningSettings());
            updateAlarm(CHANNEL_ID_HUMIDITY_ALARM, hum, config.getHumidityWarningSettings());
        }
    }

    private <T extends Quantity<T>> void updateAlarm(String channelName, QuantityType<T> quantity,
            WarningSettingsDTO<T> settings) {
        boolean outOfRange = quantity.compareTo(settings.min) < 0 || settings.max.compareTo(quantity) < 0;
        updateState(channelName, OnOffType.from(outOfRange));
    }

    private int scanPacketSize() {
        switch (model) {
            case B5175:
            case B5178:
                return 10;
            case H5179:
                return 8;
            default:
                return 7;
        }
    }

    @Override
    public void onScanRecordReceived(BluetoothScanNotification scanNotification) {
        super.onScanRecordReceived(scanNotification);
        byte[] scanData = scanNotification.getData();
        int dataPacketSize = scanPacketSize();
        int recordIndex = indexOfTemHumRecord(scanData);
        if (recordIndex == -1 || recordIndex + dataPacketSize >= scanData.length) {
            return;
        }

        ByteBuffer data = ByteBuffer.wrap(scanData, recordIndex, dataPacketSize);

        short temperature;
        int humidity;
        int battery;
        int wifiLevel = 0;

        switch (model) {
            default:
                data.position(2);// we throw this away
                // fall through
            case H5072:
            case H5075:
                data.order(ByteOrder.BIG_ENDIAN);
                int l = data.getInt();
                l = l & 0xFFFFFF;

                boolean positive = (l & 0x800000) == 0;
                int tem = (short) ((l / 1000) * 10);
                if (!positive) {
                    tem = -tem;
                }
                temperature = (short) tem;
                humidity = (l % 1000) * 10;
                battery = data.get();
                break;
            case H5179:
                data.order(ByteOrder.LITTLE_ENDIAN);
                data.position(3);
                temperature = data.getShort();
                humidity = data.getShort();
                battery = Byte.toUnsignedInt(data.get());
                break;
            case H5051:
            case H5052:
            case H5071:
            case H5074:
                data.order(ByteOrder.LITTLE_ENDIAN);
                boolean hasWifi = data.get() == 0;
                temperature = data.getShort();
                humidity = Short.toUnsignedInt(data.getShort());
                battery = Byte.toUnsignedInt(data.get());
                wifiLevel = hasWifi ? Byte.toUnsignedInt(data.get()) : 0;
                break;
        }
        updateTemHumBattery(temperature, humidity, battery, wifiLevel);
    }

    private static int indexOfTemHumRecord(byte @Nullable [] scanData) {
        if (scanData == null || scanData.length != 62) {
            return -1;
        }
        int i = 0;
        while (i < 57) {
            int recordLength = scanData[i] & 0xFF;
            if (scanData[i + 1] == SCAN_HEADER[0]//
                    && scanData[i + 2] == SCAN_HEADER[1]//
                    && scanData[i + 3] == SCAN_HEADER[2]) {
                return i + 4;
            }

            i += recordLength + 1;
        }
        return -1;
    }

    private void updateTemHumBattery(short tem, int hum, int battery, int wifiLevel) {
        if (Short.toUnsignedInt(tem) == 0xFFFF || hum == 0xFFFF) {
            logger.trace("Govee device [{}] received invalid data", this.address);
            return;
        }

        logger.debug("Govee device [{}] received broadcast: tem = {}, hum = {}, battery = {}, wifiLevel = {}",
                this.address, tem, hum, battery, wifiLevel);

        if (tem == 0 && hum == 0 && battery == 0) {
            logger.trace("Govee device [{}] values are zero", this.address);
            return;
        }
        if (tem < -4000 || tem > 10000) {
            logger.trace("Govee device [{}] invalid temperature value: {}", this.address, tem);
            return;
        }
        if (hum > 10000) {
            logger.trace("Govee device [{}] invalid humidity valie: {}", this.address, hum);
            return;
        }

        TemHumDTO temhum = new TemHumDTO();
        temhum.temperature = new QuantityType<>(tem / 100.0, SIUnits.CELSIUS);
        temhum.humidity = new QuantityType<>(hum / 100.0, Units.PERCENT);
        updateTemperatureAndHumidity(temhum, null);

        updateBattery(new QuantityType<>(battery, Units.PERCENT), null);
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
}
