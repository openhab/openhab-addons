/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.am43.internal;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Length;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jdt.annotation.DefaultLocation;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCompletionStatus;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.binding.bluetooth.am43.internal.data.Direction;
import org.openhab.binding.bluetooth.am43.internal.data.MotorSettings;
import org.openhab.binding.bluetooth.am43.internal.data.OperationMode;
import org.openhab.binding.bluetooth.ConnectedBluetoothHandler;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tec.uom.se.unit.MetricPrefix;

/**
 * The {@link AM43Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault({ DefaultLocation.PARAMETER, DefaultLocation.RETURN_TYPE, DefaultLocation.ARRAY_CONTENTS,
        DefaultLocation.TYPE_ARGUMENT, DefaultLocation.TYPE_BOUND, DefaultLocation.TYPE_PARAMETER })
public class AM43Handler extends ConnectedBluetoothHandler {

    private final Logger logger = LoggerFactory.getLogger(AM43Handler.class);

    private AM43Configuration configuration;

    protected volatile Boolean enabledNotifications = false;

    private ScheduledFuture<?> motorSettingsJob;
    private ScheduledFuture<?> refreshBatteryJob;
    private ScheduledFuture<?> refreshLightLevelJob;

    public AM43Handler(Thing thing) {
        super(thing);
    }

    private MotorSettings motorSettings = null;

    @Override
    public void initialize() {
        super.initialize();

        configuration = getConfigAs(AM43Configuration.class);

        motorSettingsJob = scheduler.scheduleWithFixedDelay(() -> {
            if (enableNotifications()) {
                sendFindSetCommand();
            }
        }, 0, configuration.refreshInterval, TimeUnit.SECONDS);

        refreshBatteryJob = scheduler.scheduleWithFixedDelay(() -> {
            if (enableNotifications()) {
                sendFindElectricCommand();
            }
        }, 5, configuration.refreshInterval, TimeUnit.SECONDS);

        refreshLightLevelJob = scheduler.scheduleWithFixedDelay(() -> {
            if (enableNotifications()) {
                sendFindLightLevelCommand();
            }
        }, 10, configuration.refreshInterval, TimeUnit.SECONDS);
    }

    private void cancelMotorSettingsJob() {
        if (motorSettingsJob != null) {
            motorSettingsJob.cancel(true);
            motorSettingsJob = null;
        }
    }

    private void cancelRefreshBatteryJob() {
        if (refreshBatteryJob != null) {
            refreshBatteryJob.cancel(true);
            refreshBatteryJob = null;
        }
    }

    private void cancelRefreshLightLevelJob() {
        if (refreshLightLevelJob != null) {
            refreshLightLevelJob.cancel(true);
            refreshLightLevelJob = null;
        }
    }

    @Override
    public void dispose() {
        cancelMotorSettingsJob();
        cancelRefreshBatteryJob();
        cancelRefreshLightLevelJob();
        super.dispose();
    }

    @Override
    public void onServicesDiscovered() {
        super.onServicesDiscovered();
        if (enableNotifications()) {
            sendFindSetCommand();
        }
    }

    private boolean isConnected() {
        return device != null && device.getConnectionState() == ConnectionState.CONNECTED;
    }

    private boolean enableNotifications() {
        if (!resolved || !isConnected()) {
            return false;
        }
        if (enabledNotifications) {
            return true;
        }
        BluetoothCharacteristic characteristic = device.getCharacteristic(AM43Constants.RX_CHAR_UUID);
        if (characteristic == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Failed to find service with characteristic: " + AM43Constants.RX_CHAR_UUID);
            device.disconnect();
            return false;
        }
        if (!device.enableNotifications(characteristic)) {
            logger.debug("failed to enable notifications for characteristic: {}", AM43Constants.RX_CHAR_UUID);
            return false;
        }
        enabledNotifications = true;
        return true;
    }

    @Override
    public void onConnectionStateChange(BluetoothConnectionStatusNotification connectionNotification) {
        super.onConnectionStateChange(connectionNotification);
        if (enabledNotifications && connectionNotification.getConnectionState() != ConnectionState.CONNECTED) {
            enabledNotifications = false;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!enableNotifications()) {
            return;
        }
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case AM43BindingConstants.CHANNEL_ID_ELECTRIC:
                    sendFindElectricCommand();
                    return;
                case AM43BindingConstants.CHANNEL_ID_LIGHT_LEVEL:
                    sendFindLightLevelCommand();
                    return;
            }
            sendFindSetCommand();
            return;
        }
        switch (channelUID.getId()) {
            case AM43BindingConstants.CHANNEL_ID_POSITION:
                if (command instanceof PercentType) {
                    PercentType percent = (PercentType) command;
                    int value = percent.intValue();
                    if (configuration.invertPosition) {
                        value = 100 - value;
                    }
                    sendControlPercentCommand(value);
                    return;
                }
                if (command instanceof StopMoveType) {
                    switch ((StopMoveType) command) {
                        case STOP:
                            sendControlCommand(AM43Constants.Command_Send_Content_Control_Stop);
                            return;
                        case MOVE:
                            // do nothing
                            return;
                    }
                }
                if (command instanceof UpDownType) {
                    switch ((UpDownType) command) {
                        case UP:
                            sendControlCommand(AM43Constants.Command_Send_Content_Control_Open);
                            return;
                        case DOWN:
                            sendControlCommand(AM43Constants.Command_Send_Content_Control_Close);
                            return;
                    }
                }
                break;
            case AM43BindingConstants.CHANNEL_ID_TOP_LIMIT_SET:
                // if (command instanceof OnOffType) {
                // switch ((OnOffType) command) {
                // case ON:
                // sendChangeLimitStateCommand(AM43Constants.Command_Send_Content_saveLimit, 0);
                // return;
                // case OFF:
                // sendResetLimitStateCommand();
                // updateBottomLimitSet(false);
                // return;
                // }
                // }
                break;
            case AM43BindingConstants.CHANNEL_ID_BOTTOM_LIMIT_SET:
                // if (command instanceof OnOffType) {
                // switch ((OnOffType) command) {
                // case ON:
                // sendChangeLimitStateCommand(AM43Constants.Command_Send_Content_saveLimit, 1);
                // return;
                // case OFF:
                // sendResetLimitStateCommand();
                // updateTopLimitSet(false);
                // return;
                // }
                // }
                break;
            case AM43BindingConstants.CHANNEL_ID_SPEED:
                if (command instanceof DecimalType) {
                    DecimalType speedType = (DecimalType) command;
                    motorSettings.setSpeed(speedType.intValue());
                    sendMotorSettingsCommand();
                    return;
                }
                break;
            case AM43BindingConstants.CHANNEL_ID_DIRECTION:
                if (command instanceof StringType) {
                    motorSettings.setDirection(Direction.valueOf(command.toString()));
                    sendMotorSettingsCommand();
                    return;
                }
                break;
        }

        super.handleCommand(channelUID, command);
    }

    @Override
    public void onCharacteristicWriteComplete(BluetoothCharacteristic characteristic,
            BluetoothCompletionStatus status) {
        super.onCharacteristicWriteComplete(characteristic, status);

        switch (status) {
            case ERROR:
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Failed to set characteristic "
                        + characteristic.getUuid() + " to value " + HexUtils.bytesToHex(characteristic.getByteValue()));
                device.disconnect();
                return;
            case SUCCESS:
                return;
        }
    }

    @Override
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic) {
        super.onCharacteristicUpdate(characteristic);
        if (!characteristic.getUuid().equals(AM43Constants.RX_CHAR_UUID)) {
            return;
        }
        byte[] data = characteristic.getByteValue();
        logger.debug("recieved {}", HexUtils.bytesToHex(data));

        byte headType = data[1];
        switch (headType) {
            case AM43Constants.Command_Head_Type_Control_Direct: {
                logger.debug("received control ack");
                if (data[3] == AM43Constants.Command_Notify_Content_Success) {
                    // direct command was successful
                }
                return;
            }
            case AM43Constants.Command_Notify_Head_Type_Move: {
                logger.debug("received movement notify");
                if (data.length < 5) {
                    return;
                }
                updatePosition(data[4]);
                break;
            }
            case AM43Constants.Command_Notify_Head_Type_Battery_Level: {
                if (data.length < 9) {
                    return;
                }
                updateBatteryLevel(data[7]);
                break;
            }
            case AM43Constants.Command_Notify_Head_Type_Light_Level: {
                if (data.length < 6) {
                    return;
                }
                // data[3] == 0 && data[4] == 0 when charging

                // boolean hasLightSensor = data[3] == 1;
                // if (hasLightSensor) {
                updateLightLevel(data[4]);
                // }
                break;
            }
            case AM43Constants.Command_Notify_Head_Type_Speed: {
                if (data.length < 5) {
                    return;
                }

                // updateDirection((data[3] & 2) > 0);
                // updateOperationMode((data[3] & 4) > 0);
                // updateHasLightSensor(((data[3] & 8)) > 0);
                updateSpeed(data[4]);
                break;
            }
            case AM43Constants.Command_Notify_Head_Type_Find_Normal: {
                if (data.length < 10) {
                    return;
                }

                motorSettings = new MotorSettings();

                updateDirection((data[3] & 1) > 0);
                updateOperationMode((data[3] & 2) > 0);
                updateTopLimitSet((data[3] & 4) > 0);
                updateBottomLimitSet((data[3] & 8) > 0);
                updateHasLightSensor(((data[3] & 16)) > 0);
                updateSpeed(data[4]);
                updatePosition(data[5]);
                updateLength(data[6], data[7]);
                updateDiameter(data[8]);
                updateType(Math.abs(data[9] >> 4));

                // cancelMotorSettingsJob();
                break;
            }
            case AM43Constants.Command_Notify_Head_Type_Find_Timing: {
                // if (data[2] == 0) {
                // bleSetNormalBean.getTimingList().clear();
                // return;
                // }
                //
                // int i3 = data[2] / 5;
                // bleSetNormalBean.getTimingList().clear();
                //
                // for (int i = 0; i < i3; i++) {
                // int i6 = i * 5;
                // int i7 = i6 + 5;
                // boolean[] bools = new boolean[7];
                // bools[0] = (data[i7] & 1) != 0;
                // bools[1] = (data[i7] & 2) != 0;
                // bools[2] = (data[i7] & 4) != 0;
                // bools[3] = (data[i7] & 8) != 0;
                // bools[4] = (data[i7] & 16) != 0;
                // bools[5] = (data[i7] & 32) != 0;
                // bools[6] = (data[i7] & 64) != 0;
                //
                // BleTimingBean bleTimingBean = new BleTimingBean(data[i6 + 3] == 1, Integer.valueOf(data[i6 + 4]),
                // arr,
                // data[i6 + 6], data[i6 + 7]);
                // ArrayList timingList = bleSetNormalBean.getTimingList();
                // timingList.add(bleTimingBean);
                // }
                return;
            }
            case AM43Constants.Command_Notify_Head_Type_Find_Season: {
                // if (data.length < 20) {
                // StringBuilder sb = new StringBuilder();
                // sb.append("错误的数据为：");
                // sb.append(CollectionsKt.joinToString$default(ByteUtils.Companion.formatHexStringList(bArr2), null,
                // null,
                // null, 0, null, null, 63, null));
                // Log.i("Error", sb.toString());
                // return;
                // }
                // bleSetNormalBean2.setSummerSeasonState(data[4] == 1);
                // bleSetNormalBean2.setSummerLightSeasonState(data[5]);
                // bleSetNormalBean2.setSummerLightLevel(data[6]);
                // bleSetNormalBean2.setSummerLightStartHour(data[7]);
                // bleSetNormalBean2.setSummerLightStartMinute(data[8]);
                // bleSetNormalBean2.setSummerLightEndHour(data[9]);
                // bleSetNormalBean2.setSummerLightEndMinute(data[10]);
                // if (data[12] != 1) {
                // z = false;
                // }
                // bleSetNormalBean2.setWinterSeasonState(z);
                // bleSetNormalBean2.setWinterLightSeasonState(data[13]);
                // bleSetNormalBean2.setWinterLightLevel(data[14]);
                // bleSetNormalBean2.setWinterLightStartHour(data[15]);
                // bleSetNormalBean2.setWinterLightStartMinute(data[16]);
                // bleSetNormalBean2.setWinterLightEndHour(data[17]);
                // bleSetNormalBean2.setWinterLightEndMinute(data[18]);
                return;
            }
            case AM43Constants.Command_Notify_Head_Type_Fault: {
                // sendBleNotifyAck(header);
                break;
            }

        }
        // scheduler.submit(() -> {
        // sendBleCommand(headType, true, AM43Constants.Command_Notify_Content_Success);
        // });
    }

    private void updateDirection(boolean bitValue) {
        Direction direction = Direction.valueOf(bitValue);
        motorSettings.setDirection(direction);

        StringType directionType = new StringType(direction.toString());
        logger.debug("updating direction to: {}", directionType);
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_DIRECTION, directionType);
    }

    private void updateOperationMode(boolean bitValue) {
        OperationMode opMode = OperationMode.valueOf(bitValue);
        motorSettings.setOperationMode(opMode);

        StringType mode = new StringType(opMode.toString());
        logger.debug("updating operationMode to: {}", mode);
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_OPERATION_MODE, mode);
    }

    private void updateTopLimitSet(boolean bitValue) {
        OnOffType limitSet = bitValue ? OnOffType.ON : OnOffType.OFF;
        logger.debug("updating topLimitSet to: {}", bitValue);
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_TOP_LIMIT_SET, limitSet);
    }

    private void updateBottomLimitSet(boolean bitValue) {
        OnOffType limitSet = bitValue ? OnOffType.ON : OnOffType.OFF;
        logger.debug("updating bottomLimitSet to: {}", bitValue);
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_BOTTOM_LIMIT_SET, limitSet);
    }

    private void updateHasLightSensor(boolean bitValue) {
        OnOffType hasSensor = bitValue ? OnOffType.ON : OnOffType.OFF;
        logger.debug("updating hasLightSensor to: {}", bitValue);
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_HAS_LIGHT_SENSOR, hasSensor);
    }

    private void updateSpeed(byte value) {
        motorSettings.setSpeed(value);

        DecimalType speed = new DecimalType(value);
        logger.debug("updating speed to: {}", speed);
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_SPEED, speed);
    }

    private void updatePosition(byte value) {
        if (value >= 0 && value <= 100) {
            int percentValue = value;
            if (configuration.invertPosition) {
                percentValue = 100 - percentValue;
            }
            PercentType position = new PercentType(percentValue);
            logger.debug("updating position to: {}", position);
            updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_POSITION, position);
        } else {
            logger.debug("updating position to: undef");
            updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_POSITION, UnDefType.UNDEF);
        }
    }

    private void updateLength(byte upper, byte lower) {
        motorSettings.setLength(upper << 8 | lower);
        QuantityType<Length> length = QuantityType.valueOf(motorSettings.getLength(),
                MetricPrefix.MILLI(SIUnits.METRE));
        logger.debug("updating length to: {}", length);
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_LENGTH, length);
    }

    private void updateDiameter(byte value) {
        motorSettings.setDiameter(value);
        QuantityType<Length> diameter = QuantityType.valueOf(value, MetricPrefix.MILLI(SIUnits.METRE));
        logger.debug("updating diameter to: {}", diameter);
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_DIAMETER, diameter);
    }

    private void updateType(int value) {
        motorSettings.setType(value);
        DecimalType type = new DecimalType(value);
        logger.debug("updating type to: {}", type);
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_TYPE, type);
    }

    private void updateLightLevel(byte value) {
        DecimalType lightLevel = new DecimalType(value);
        logger.debug("updating lightLevel to: {}", lightLevel);
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_LIGHT_LEVEL, lightLevel);
    }

    private void updateBatteryLevel(int value) {
        if (value >= 0 && value <= 100) {
            DecimalType deviceElectric = new DecimalType(value & 0xFF);
            logger.debug("updating battery level to: {}", deviceElectric);
            updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_ELECTRIC, deviceElectric);
        } else {
            logger.debug("Received invalid battery value {}. Updating battery level: undef", value);
            updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_ELECTRIC, UnDefType.UNDEF);
        }
    }

    /**
     * Update DecimalType channel state
     *
     * Update is not done when value is null.
     *
     * @param channelUID channel UID
     * @param value value to update
     * @return whether the value was present
     */
    private void updateStateIfLinked(String channelUID, State state) {
        if (isLinked(channelUID)) {
            updateState(channelUID, state);
        }
    }

    /**
     * The footer can be one of three values: the crc, a successflag, or a failure flag.
     */
    private static byte createFooter(@Nullable Boolean verificationFooter, byte[] contentByteArray) {
        if (verificationFooter == null) {
            return computeCrc(contentByteArray);
        } else if (verificationFooter) {
            return AM43Constants.Command_Foot_Verification_Success;
        } else {
            return AM43Constants.Command_Foot_Verification_Failure;
        }
    }

    private static byte computeCrc(byte[] data) {
        byte crc = data[0];
        for (int i = 1; i < data.length; i++) {
            crc ^= data[i];
        }
        return crc;
    }

    public static byte[] createBleCommand(byte commandType, @Nullable Boolean verificationFooter,
            byte... contentByteArray) {
        byte[] header = AM43Constants.Command_Head_Tag;
        byte[] value = ArrayUtils.EMPTY_BYTE_ARRAY;
        value = ArrayUtils.add(value, AM43Constants.Command_Head_Value);
        value = ArrayUtils.add(value, commandType);
        value = ArrayUtils.add(value, (byte) contentByteArray.length);
        value = ArrayUtils.addAll(value, contentByteArray);
        value = ArrayUtils.add(value, createFooter(verificationFooter, value));
        return ArrayUtils.addAll(header, value);
    }

    private void sendBleCommand(byte commandType, byte... contentByteArray) {
        sendBleCommand(commandType, null, contentByteArray);
    }

    private void sendBleCommand(byte commandType, @Nullable Boolean verificationFooter, byte... contentByteArray) {
        BluetoothCharacteristic characteristic = device.getCharacteristic(AM43Constants.TX_CHAR_UUID);
        characteristic.setValue(createBleCommand(commandType, verificationFooter, contentByteArray));
        device.writeCharacteristic(characteristic);
    }

    private void sendMotorSettingsCommand() {
        if (motorSettings == null) {
            throw new IllegalStateException("settings have not yet been retrieved from the motor");
        }

        @SuppressWarnings("null")
        int direction = motorSettings.getDirection().toByte();
        @SuppressWarnings("null")
        int operationMode = motorSettings.getOperationMode().toByte();
        int deviceType = motorSettings.getType();
        int deviceLength = motorSettings.getLength();
        int deviceSpeed = motorSettings.getSpeed();
        int deviceDiameter = motorSettings.getDiameter();

        int dataHead = ((direction & 1) << 1) | ((operationMode & 1) << 2) | (deviceType << 4);

        sendBleCommand(AM43Constants.Command_Head_Type_Setting_Frequently, (byte) dataHead, (byte) deviceSpeed,
                (byte) 0, (byte) ((deviceLength & 0xFF00) >> 8), (byte) (deviceLength & 0xFF), (byte) deviceDiameter);
    }

    @SuppressWarnings("unused")
    private void sendPasswordCommand(int i) {
        sendBleCommand(AM43Constants.Command_Head_Type_PassWord, (byte) ((i & 0xFF00) >> 8), (byte) (i & 0xFF));
    }

    @SuppressWarnings("unused")
    private void sendChangedPasswordCommand(int i) {
        sendBleCommand(AM43Constants.Command_Head_Type_PassWord_Change, (byte) ((i & 0xFF00) >> 8), (byte) (i & 0xFF));
    }

    private void sendControlCommand(byte command) {
        sendBleCommand(AM43Constants.Command_Head_Type_Control_Direct, command);
    }

    private void sendControlPercentCommand(int percent) {
        sendBleCommand(AM43Constants.Command_Head_Type_Control_Percent, (byte) percent);
    }

    @SuppressWarnings("unused")
    private void sendChangeLimitStateCommand(byte limitType, int limitMode) {
        byte[] data = { limitType, (byte) (1 << limitMode), 0 };
        sendBleCommand(AM43Constants.Command_Head_Type_LimitOrReset, data);
    }

    @SuppressWarnings("unused")
    private void sendResetLimitStateCommand() {
        byte[] data = { 0, 0, 1 };
        sendBleCommand(AM43Constants.Command_Head_Type_LimitOrReset, data);
    }

    @SuppressWarnings("unused")
    private void sendNewNameCommand(byte[] data) {
        sendBleCommand(AM43Constants.Command_Notify_Head_Type_NewName, data);
    }

    @SuppressWarnings("unused")
    private void sendChangeSeasonCommand(byte[] data) {
        sendBleCommand(AM43Constants.Command_Head_Type_Season, data);
    }

    @SuppressWarnings("unused")
    private void sendTimingCommand(byte[] data) {
        sendBleCommand(AM43Constants.Command_Head_Type_Timing, data);
    }

    @SuppressWarnings("unused")
    private void sendTimingSwitchCommand(int i, boolean z) {
        byte[] data = { (byte) i, 0, z ? (byte) 1 : 0, 0, 0, 0, 0 };
        sendBleCommand(AM43Constants.Command_Head_Type_Timing, data);
    }

    @SuppressWarnings("unused")
    private void sendCurrentTimeCommand() {
        Calendar instance = Calendar.getInstance();
        int hour = instance.get(Calendar.HOUR);
        if (instance.get(Calendar.AM_PM) != 0) {
            hour += 12;
        }
        int minute = instance.get(Calendar.MINUTE);
        int second = instance.get(Calendar.SECOND);
        int dayOfWeek = instance.get(Calendar.DAY_OF_WEEK) - 1;
        sendBleCommand(AM43Constants.Command_Head_Type_SendTime, (byte) dayOfWeek, (byte) hour, (byte) minute,
                (byte) second);
    }

    private void sendFindElectricCommand() {
        sendBleCommand(AM43Constants.Command_Head_Type_Battery_Level,
                AM43Constants.Command_Send_Content_findBatteryLevel);
    }

    private void sendFindSetCommand() {
        sendBleCommand(AM43Constants.Command_Head_Type_Setting_findAll,
                AM43Constants.Command_Send_Content_Type_Setting_findAll);
    }

    private void sendFindLightLevelCommand() {
        sendBleCommand(AM43Constants.Command_Head_Type_Light_Level, AM43Constants.Command_Send_Content_findLightLevel);
    }

}
