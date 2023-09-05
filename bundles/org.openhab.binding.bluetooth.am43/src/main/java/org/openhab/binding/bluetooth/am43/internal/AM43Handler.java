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
package org.openhab.binding.bluetooth.am43.internal;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Length;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
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
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AM43Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public class AM43Handler extends ConnectedBluetoothHandler implements ResponseListener {

    private final Logger logger = LoggerFactory.getLogger(AM43Handler.class);

    private @Nullable AM43Configuration config;

    private volatile @Nullable AM43Command currentCommand = null;

    private @Nullable ScheduledFuture<?> refreshJob;

    private @Nullable ExecutorService commandExecutor;

    private @Nullable MotorSettings motorSettings = null;

    public AM43Handler(Thing thing) {
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
        dispose(commandExecutor);
        dispose(currentCommand);
        dispose(refreshJob);

        commandExecutor = null;
        currentCommand = null;
        refreshJob = null;
        motorSettings = null;
        super.dispose();
    }

    private static void dispose(@Nullable ExecutorService executor) {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private static void dispose(@Nullable ScheduledFuture<?> future) {
        if (future != null) {
            future.cancel(true);
        }
    }

    private static void dispose(@Nullable AM43Command command) {
        if (command != null) {
            // even if it already completed it doesn't really matter.
            // on the off chance that the commandExecutor is waiting on the command, we can wake it up and cause it to
            // terminate
            command.setState(AM43Command.State.FAILED);
        }
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
        Executor executor = commandExecutor;
        if (executor != null) {
            executor.execute(() -> processCommand(command));
        }
    }

    private void processCommand(AM43Command command) {
        try {
            currentCommand = command;
            if (device.getConnectionState() != ConnectionState.CONNECTED) {
                logger.debug("Unable to send command {} to device {}: not connected", command, device.getAddress());
                command.setState(AM43Command.State.FAILED);
                return;
            }
            if (!device.isServicesDiscovered()) {
                logger.debug("Unable to send command {} to device {}: services not resolved", command,
                        device.getAddress());
                command.setState(AM43Command.State.FAILED);
                return;
            }
            BluetoothCharacteristic characteristic = device.getCharacteristic(AM43BindingConstants.CHARACTERISTIC_UUID);
            if (characteristic == null) {
                logger.warn("Unable to execute {}. Characteristic '{}' could not be found.", command,
                        AM43BindingConstants.CHARACTERISTIC_UUID);
                command.setState(AM43Command.State.FAILED);
                return;
            }
            // there is no consequence to calling this as much as we like
            device.enableNotifications(characteristic);

            command.setState(AM43Command.State.ENQUEUED);
            device.writeCharacteristic(characteristic, command.getRequest()).whenComplete((v, t) -> {
                if (t != null) {
                    logger.debug("Failed to send command {}: {}", command.getClass().getSimpleName(), t.getMessage());
                    command.setState(AM43Command.State.FAILED);
                } else {
                    command.setState(AM43Command.State.SENT);
                }
            });

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
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic, byte[] response) {
        super.onCharacteristicUpdate(characteristic, response);

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
                if (command instanceof PercentType percentCommand) {
                    MotorSettings settings = motorSettings;
                    if (settings == null) {
                        logger.warn("Cannot set position before settings have been received.");
                        return;
                    }
                    if (!settings.isTopLimitSet() || !settings.isBottomLimitSet()) {
                        logger.warn("""
                                Cannot set position of blinds. Top or bottom limits have not been set. \
                                Please configure manually.\
                                """);
                        return;
                    }
                    int value = percentCommand.intValue();
                    if (getAM43Config().invertPosition) {
                        value = 100 - value;
                    }
                    submitCommand(new SetPositionCommand(value));
                    return;
                }
                if (command instanceof StopMoveType stopMoveCommand) {
                    switch (stopMoveCommand) {
                        case STOP:
                            submitCommand(new ControlCommand(ControlAction.STOP));
                            return;
                        case MOVE:
                            // do nothing
                            return;
                    }
                }
                if (command instanceof UpDownType upDownCommand) {
                    switch (upDownCommand) {
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
                if (command instanceof DecimalType decimalCommand) {
                    MotorSettings settings = motorSettings;
                    if (settings != null) {
                        settings.setSpeed(decimalCommand.intValue());
                        submitCommand(new SetSettingsCommand(settings));
                    } else {
                        logger.warn("Cannot set Speed before setting have been received");
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
                        logger.warn("Cannot set Direction before setting have been received");
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
                        logger.warn("Cannot set OperationMode before setting have been received");
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
        getMotorSettings().setSpeed(command.getSpeed());
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

        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_DIRECTION, new StringType(direction.toString()));
    }

    private void updateOperationMode(OperationMode opMode) {
        getMotorSettings().setOperationMode(opMode);

        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_OPERATION_MODE, new StringType(opMode.toString()));
    }

    private void updateTopLimitSet(boolean bitValue) {
        getMotorSettings().setTopLimitSet(bitValue);

        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_TOP_LIMIT_SET, OnOffType.from(bitValue));
    }

    private void updateBottomLimitSet(boolean bitValue) {
        getMotorSettings().setBottomLimitSet(bitValue);

        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_BOTTOM_LIMIT_SET, OnOffType.from(bitValue));
    }

    private void updateHasLightSensor(boolean bitValue) {
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_HAS_LIGHT_SENSOR, OnOffType.from(bitValue));
    }

    private void updateSpeed(int value) {
        getMotorSettings().setSpeed(value);

        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_SPEED, new DecimalType(value));
    }

    private void updatePosition(int value) {
        if (value >= 0 && value <= 100) {
            int percentValue = value;
            if (getAM43Config().invertPosition) {
                percentValue = 100 - percentValue;
            }
            updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_POSITION, new PercentType(percentValue));
        } else {
            updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_POSITION, UnDefType.UNDEF);
        }
    }

    private void updateLength(int value) {
        getMotorSettings().setLength(value);

        QuantityType<Length> lengthType = QuantityType.valueOf(value, MetricPrefix.MILLI(SIUnits.METRE));
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_LENGTH, lengthType);
    }

    private void updateDiameter(int value) {
        getMotorSettings().setDiameter(value);

        QuantityType<Length> diameter = QuantityType.valueOf(value, MetricPrefix.MILLI(SIUnits.METRE));
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_DIAMETER, diameter);
    }

    private void updateType(int value) {
        getMotorSettings().setType(value);

        DecimalType type = new DecimalType(value);
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_TYPE, type);
    }

    private void updateLightLevel(int value) {
        DecimalType lightLevel = new DecimalType(value);
        updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_LIGHT_LEVEL, lightLevel);
    }

    private void updateBatteryLevel(int value) {
        if (value >= 0 && value <= 100) {
            DecimalType deviceElectric = new DecimalType(value & 0xFF);
            updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_ELECTRIC, deviceElectric);
        } else {
            updateStateIfLinked(AM43BindingConstants.CHANNEL_ID_ELECTRIC, UnDefType.UNDEF);
        }
    }

    private void updateStateIfLinked(String channelUID, State state) {
        if (isLinked(channelUID)) {
            updateState(channelUID, state);
        }
    }
}
