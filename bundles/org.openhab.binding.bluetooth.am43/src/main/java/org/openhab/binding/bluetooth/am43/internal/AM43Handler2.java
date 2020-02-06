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

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.NamedThreadFactory;
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
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.core.util.HexUtils;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
import org.openhab.binding.bluetooth.BluetoothCompletionStatus;
import org.openhab.binding.bluetooth.BluetoothDevice.ConnectionState;
import org.openhab.binding.bluetooth.ConnectedBluetoothHandler;
import org.openhab.binding.bluetooth.am43.internal.command.AM43Command;
import org.openhab.binding.bluetooth.am43.internal.command.ControlCommand;
import org.openhab.binding.bluetooth.am43.internal.command.GetAllCommand;
import org.openhab.binding.bluetooth.am43.internal.command.GetBatteryLevelCommand;
import org.openhab.binding.bluetooth.am43.internal.command.GetLightLevelCommand;
import org.openhab.binding.bluetooth.am43.internal.command.GetPositionCommand;
import org.openhab.binding.bluetooth.am43.internal.command.GetSpeedCommand;
import org.openhab.binding.bluetooth.am43.internal.command.ResponseListener;
import org.openhab.binding.bluetooth.am43.internal.command.SetPositionCommand;
import org.openhab.binding.bluetooth.am43.internal.command.SetSettingsCommand;
import org.openhab.binding.bluetooth.am43.internal.data.ControlAction;
import org.openhab.binding.bluetooth.am43.internal.data.Direction;
import org.openhab.binding.bluetooth.am43.internal.data.MotorSettings;
import org.openhab.binding.bluetooth.am43.internal.data.OperationMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tec.uom.se.unit.MetricPrefix;

/**
 * The {@link AM43Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class AM43Handler2 extends ConnectedBluetoothHandler implements ResponseListener {

    private final Logger logger = LoggerFactory.getLogger(AM43Handler2.class);

    @Nullable
    private AM43Configuration config;

    @Nullable
    private volatile AM43Command currentCommand = null;

    @Nullable
    private ScheduledFuture<?> refreshJob;

    @Nullable
    private ExecutorService commandExecutor;

    @Nullable
    private MotorSettings motorSettings = null;

    public AM43Handler2(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        config = getConfigAs(AM43Configuration.class);

        commandExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory(thing.getUID().getAsString(), true));
        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            submitCommand(new GetAllCommand());
            submitCommand(new GetBatteryLevelCommand());
            submitCommand(new GetLightLevelCommand());
        }, 10, getAM43Config().refreshInterval, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (commandExecutor != null) {
            commandExecutor.shutdownNow();
            commandExecutor = null;
        }
        AM43Command command = currentCommand;
        if (command != null) {
            // even if it already completed it doesn't really matter.
            // on the off chance that the commandExecutor is waiting on the command, we can wake it up and cause it to
            // terminate
            command.setState(AM43Command.State.FAILED);
        }
        if (refreshJob != null) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
        motorSettings = null;
        super.dispose();
    }

    private Executor getCommandExecutor() {
        Executor executor = commandExecutor;
        if (executor == null) {
            throw new IllegalStateException("commandExecutor has not been initialized");
        }
        return executor;
    }

    private MotorSettings getMotorSettings() {
        MotorSettings settings = motorSettings;
        if (settings == null) {
            throw new IllegalStateException("motorSettings has not been initialized");
        }
        return settings;
    }

    private AM43Configuration getAM43Config() {
        AM43Configuration ret = config;
        if (ret == null) {
            throw new IllegalStateException("config has not been initialized");
        }
        return ret;
    }

    private void submitCommand(AM43Command command) {
        getCommandExecutor().execute(() -> processCommand(command));
    }

    private void processCommand(AM43Command command) {
        try {
            if (device.getConnectionState() != ConnectionState.CONNECTED) {
                logger.debug("Unable to send command {} to device {}: not connected", command, device.getAddress());
                command.setState(AM43Command.State.FAILED);
                return;
            }
            currentCommand = command;
            command.setState(AM43Command.State.ENQUEUED);
            BluetoothCharacteristic characteristic = device.getCharacteristic(AM43BindingConstants.CHARACTERISTIC_UUID);
            device.enableNotifications(characteristic);
            if (characteristic == null) {
                logger.warn("Unable to execute {}. Characteristic '{}' could not be found.", command,
                        AM43BindingConstants.CHARACTERISTIC_UUID);
                return;
            }

            characteristic.setValue(command.getRequest());

            device.writeCharacteristic(characteristic);
            if (!command.awaitStateChange(getAM43Config().commandTimeout, TimeUnit.MILLISECONDS,
                    AM43Command.State.SUCCEEDED, AM43Command.State.FAILED)) {
                logger.debug("Command {} to device {} timed out", command, device.getAddress());
            }
        } catch (InterruptedException e) {
            // do nothing
        } finally {
            logger.trace("Command final state: {}", command.getState());
            currentCommand = null;
        }
    }

    @Override
    public void onCharacteristicWriteComplete(BluetoothCharacteristic characteristic,
            BluetoothCompletionStatus status) {
        super.onCharacteristicWriteComplete(characteristic, status);

        byte[] request = characteristic.getByteValue();

        AM43Command command = currentCommand;

        if (command != null) {
            if (!Arrays.equals(request, command.getRequest())) {
                logger.debug("Write completed for unkmnown command");
                return;
            }
            switch (status) {
                case SUCCESS:
                    command.setState(AM43Command.State.SENT);
                    break;
                case ERROR:
                    command.setState(AM43Command.State.FAILED);
                    break;
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("No command found that matches request {}", HexUtils.bytesToHex(request));
            }
        }
    }

    @Override
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic) {
        super.onCharacteristicUpdate(characteristic);

        byte[] response = characteristic.getByteValue();

        AM43Command command = currentCommand;
        if (command == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No command present to handle response {}", HexUtils.bytesToHex(response));
            }
        } else if (!command.handleResponse(scheduler, this, response)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Command {} could not handle response {}", command, HexUtils.bytesToHex(response));
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case AM43BindingConstants.CHANNEL_ID_ELECTRIC:
                    submitCommand(new GetBatteryLevelCommand());
                    return;
                case AM43BindingConstants.CHANNEL_ID_LIGHT_LEVEL:
                    submitCommand(new GetLightLevelCommand());
                    return;
                case AM43BindingConstants.CHANNEL_ID_POSITION:
                    submitCommand(new GetPositionCommand());
                    return;
            }
            submitCommand(new GetAllCommand());
            return;
        }
        switch (channelUID.getId()) {
            case AM43BindingConstants.CHANNEL_ID_POSITION:
                if (command instanceof PercentType) {
                    PercentType percent = (PercentType) command;
                    int value = percent.intValue();
                    if (getAM43Config().invertPosition) {
                        value = 100 - value;
                    }
                    submitCommand(new SetPositionCommand(value));
                    return;
                }
                if (command instanceof StopMoveType) {
                    switch ((StopMoveType) command) {
                        case STOP:
                            submitCommand(new ControlCommand(ControlAction.STOP));
                            return;
                        case MOVE:
                            // do nothing
                            return;
                    }
                }
                if (command instanceof UpDownType) {
                    switch ((UpDownType) command) {
                        case UP:
                            submitCommand(new ControlCommand(ControlAction.OPEN));
                            return;
                        case DOWN:
                            submitCommand(new ControlCommand(ControlAction.CLOSE));
                            return;
                    }
                }
                return;
            case AM43BindingConstants.CHANNEL_ID_SPEED:
                if (command instanceof DecimalType) {
                    MotorSettings settings = motorSettings;
                    if (settings != null) {
                        DecimalType speedType = (DecimalType) command;
                        settings.setSpeed(speedType.intValue());
                        submitCommand(new SetSettingsCommand(settings));
                    } else {
                        logger.debug("Cannot set Speed before setting have been received");
                    }

                }
                return;
            case AM43BindingConstants.CHANNEL_ID_DIRECTION:
                if (command instanceof StringType) {
                    MotorSettings settings = motorSettings;
                    if (settings != null) {
                        settings.setDirection(Direction.valueOf(command.toString()));
                        submitCommand(new SetSettingsCommand(settings));
                    } else {
                        logger.debug("Cannot set Direction before setting have been received");
                    }
                }
                return;
            case AM43BindingConstants.CHANNEL_ID_OPERATION_MODE:
                if (command instanceof StringType) {
                    MotorSettings settings = motorSettings;
                    if (settings != null) {
                        settings.setOperationMode(OperationMode.valueOf(command.toString()));
                        submitCommand(new SetSettingsCommand(settings));
                    } else {
                        logger.debug("Cannot set OperationMode before setting have been received");
                    }
                }
                return;
        }

        super.handleCommand(channelUID, command);
    }

    @Override
    public void receivedResponse(GetLightLevelCommand command) {
        updateLightLevel(command.getLightLevel());
    }

    @Override
    public void receivedResponse(GetPositionCommand command) {
        updatePosition(command.getPosition());
    }

    @Override
    public void receivedResponse(GetSpeedCommand command) {
        updateSpeed(command.getSpeed());
    }

    @Override
    public void receivedResponse(GetAllCommand command) {
        motorSettings = new MotorSettings();

        updateDirection(command.getDirection());
        updateOperationMode(command.getOperationMode());
        updateTopLimitSet(command.getTopLimitSet());
        updateBottomLimitSet(command.getBottomLimitSet());
        updateHasLightSensor(command.getHasLightSensor());
        updateSpeed(command.getSpeed());
        updatePosition(command.getPosition());
        updateLength(command.getLength());
        updateDiameter(command.getDiameter());
        updateType(command.getType());
    }

    @Override
    public void receivedResponse(GetBatteryLevelCommand command) {
        updateBatteryLevel(command.getBatteryLevel());
    }

    private void updateDirection(Direction direction) {
        getMotorSettings().setDirection(direction);

        StringType directionType = new StringType(direction.toString());
        logger.debug("updating direction to: {}", directionType);
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_DIRECTION, directionType);
    }

    private void updateOperationMode(OperationMode opMode) {
        getMotorSettings().setOperationMode(opMode);

        StringType mode = new StringType(opMode.toString());
        logger.debug("updating operationMode to: {}", mode);
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_OPERATION_MODE, mode);
    }

    private void updateTopLimitSet(boolean bitValue) {
        OnOffType limitSet = OnOffType.from(bitValue);
        logger.debug("updating topLimitSet to: {}", bitValue);
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_TOP_LIMIT_SET, limitSet);
    }

    private void updateBottomLimitSet(boolean bitValue) {
        OnOffType limitSet = OnOffType.from(bitValue);
        logger.debug("updating bottomLimitSet to: {}", bitValue);
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_BOTTOM_LIMIT_SET, limitSet);
    }

    private void updateHasLightSensor(boolean bitValue) {
        OnOffType hasSensor = OnOffType.from(bitValue);
        logger.debug("updating hasLightSensor to: {}", bitValue);
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_HAS_LIGHT_SENSOR, hasSensor);
    }

    private void updateSpeed(int value) {
        getMotorSettings().setSpeed(value);

        DecimalType speed = new DecimalType(value);
        logger.debug("updating speed to: {}", speed);
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_SPEED, speed);
    }

    private void updatePosition(int value) {
        if (value >= 0 && value <= 100) {
            int percentValue = value;
            if (getAM43Config().invertPosition) {
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

    private void updateLength(int value) {
        getMotorSettings().setLength(value);
        QuantityType<Length> lengthType = QuantityType.valueOf(value, MetricPrefix.MILLI(SIUnits.METRE));
        logger.debug("updating length to: {}", lengthType);
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_LENGTH, lengthType);
    }

    private void updateDiameter(int value) {
        getMotorSettings().setDiameter(value);
        QuantityType<Length> diameter = QuantityType.valueOf(value, MetricPrefix.MILLI(SIUnits.METRE));
        logger.debug("updating diameter to: {}", diameter);
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_DIAMETER, diameter);
    }

    private void updateType(int value) {
        getMotorSettings().setType(value);
        DecimalType type = new DecimalType(value);
        logger.debug("updating type to: {}", type);
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_TYPE, type);
    }

    private void updateLightLevel(int value) {
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

}
