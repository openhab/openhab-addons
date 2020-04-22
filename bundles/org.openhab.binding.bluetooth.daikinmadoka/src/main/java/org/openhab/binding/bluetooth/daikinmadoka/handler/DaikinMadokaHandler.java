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
package org.openhab.binding.bluetooth.daikinmadoka.handler;

import java.util.Arrays;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.common.NamedThreadFactory;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
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
import org.openhab.binding.bluetooth.daikinmadoka.DaikinMadokaBindingConstants;
import org.openhab.binding.bluetooth.daikinmadoka.internal.BRC1HUartProcessor;
import org.openhab.binding.bluetooth.daikinmadoka.internal.DaikinMadokaConfiguration;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaMessage;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaParsingException;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaProperties.FanSpeed;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaProperties.OperationMode;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.MadokaSettings;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.BRC1HCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetFanspeedCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetIndoorOutoorTemperatures;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetOperationmodeCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetPowerstateCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetSetpointCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetVersionCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.ResponseListener;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.SetFanspeedCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.SetOperationmodeCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.SetPowerstateCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.SetSetpointCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DaikinMadokaHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Benjamin Lafois - Initial contribution
 */
@NonNullByDefault
public class DaikinMadokaHandler extends ConnectedBluetoothHandler implements ResponseListener {

    private final Logger logger = LoggerFactory.getLogger(DaikinMadokaHandler.class);

    private @Nullable DaikinMadokaConfiguration config;

    private @Nullable ExecutorService commandExecutor;

    private @Nullable ScheduledFuture<?> refreshJob;

    // UART Processor is in charge of reassembling chunks
    private BRC1HUartProcessor uartProcessor = new BRC1HUartProcessor(this);

    private volatile @Nullable BRC1HCommand currentCommand = null;

    private MadokaSettings madokaSettings = new MadokaSettings();

    public DaikinMadokaHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();

        logger.debug("[{}] Start initializing!", super.thing.getUID().getId());

        // Load Configuration
        config = getConfigAs(DaikinMadokaConfiguration.class);

        logger.debug("[{}] Parameter value [refreshInterval]: {}", super.thing.getUID().getId(),
                config.refreshInterval);
        logger.debug("[{}] Parameter value [commandTimeout]: {}", super.thing.getUID().getId(), config.commandTimeout);

        if (getBridge() == null) {
            logger.debug("[{}] Bridge is null. Exiting.", super.thing.getUID().getId());
            return;
        }

        this.commandExecutor = Executors
                .newSingleThreadExecutor(new NamedThreadFactory(thing.getUID().getAsString(), true));

        this.refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            // It is useless to refresh version all the time ! Just once.
            if (this.madokaSettings.getCommunicationControllerVersion() == null
                    || this.madokaSettings.getRemoteControllerVersion() == null) {
                submitCommand(new GetVersionCommand());
            }
            submitCommand(new GetIndoorOutoorTemperatures());
            submitCommand(new GetOperationmodeCommand());
            submitCommand(new GetPowerstateCommand()); // always keep the "GetPowerState" aftern the "GetOperationMode"
            submitCommand(new GetSetpointCommand());
            submitCommand(new GetFanspeedCommand());
        }, 10, this.config.refreshInterval, TimeUnit.SECONDS);

    }

    @Override
    public void dispose() {

        logger.debug("[{}] dispose()", super.thing.getUID().getId());

        dispose(refreshJob);
        dispose(commandExecutor);
        dispose(currentCommand);

        // Unsubscribe to characteristic notifications
        if (this.device != null) {
            BluetoothCharacteristic charNotif = this.device
                    .getCharacteristic(DaikinMadokaBindingConstants.CHAR_NOTIF_UUID);

            if (charNotif != null) {
                @NonNull
                BluetoothCharacteristic c = charNotif;
                this.device.disableNotifications(c);
            }
        }

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

    private static void dispose(@Nullable BRC1HCommand command) {
        if (command != null) {
            // even if it already completed it doesn't really matter.
            // on the off chance that the commandExecutor is waiting on the command, we can wake it up and cause it to
            // terminate
            command.setState(BRC1HCommand.State.FAILED);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("[{}] Channel: {}, Command: {}", super.thing.getUID().getId(), channelUID, command);

        if (command instanceof RefreshType) {
            // The refresh commands are not supported in query mode.
            // The binding will notify updates on channels
            return;
        }

        switch (channelUID.getId()) {
            case DaikinMadokaBindingConstants.CHANNEL_ID_SETPOINT:
                try {
                    QuantityType setpoint = (QuantityType) command;
                    DecimalType dt = new DecimalType(setpoint.intValue());
                    submitCommand(new SetSetpointCommand(dt, dt));
                } catch (Exception e) {
                    logger.info("Data received is not a valid temperature", e);
                }
                break;
            case DaikinMadokaBindingConstants.CHANNEL_ID_ONOFF_STATUS:
                try {
                    OnOffType oot = (OnOffType) command;
                    submitCommand(new SetPowerstateCommand(oot));
                } catch (Exception e) {
                    logger.info("Data received is not a valid on/off status", e);
                }
                break;
            case DaikinMadokaBindingConstants.CHANNEL_ID_FAN_SPEED:
                try {
                    DecimalType fanSpeed = (DecimalType) command;
                    FanSpeed fs = FanSpeed.valueOf(fanSpeed.intValue());
                    submitCommand(new SetFanspeedCommand(fs, fs));
                } catch (Exception e) {
                    logger.info("Data received is not a valid FanSpeed status", e);
                }
                break;
            case DaikinMadokaBindingConstants.CHANNEL_ID_OPERATION_MODE:
                try {
                    StringType operationMode = (StringType) command;
                    OperationMode m = OperationMode.valueOf(operationMode.toFullString());

                    submitCommand(new SetOperationmodeCommand(m));
                } catch (Exception e) {
                    logger.info("Data received is not a valid OPERATION MODE", e);
                }
                break;
            case DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_TARGET_HEATING_COOLING_MODE:
                try {
                    StringType homekitOperationMode = (StringType) command;

                    switch (homekitOperationMode.toString()) {
                        case "Off":
                            submitCommand(new SetPowerstateCommand(OnOffType.OFF));
                            break;
                        case "CoolOn":
                            submitCommand(new SetOperationmodeCommand(OperationMode.COOL));
                            if (madokaSettings.getOnOffState() == OnOffType.OFF) {
                                submitCommand(new SetPowerstateCommand(OnOffType.ON));
                            }
                            break;
                        case "HeatOn":
                            submitCommand(new SetOperationmodeCommand(OperationMode.HEAT));
                            if (madokaSettings.getOnOffState() == OnOffType.OFF) {
                                submitCommand(new SetPowerstateCommand(OnOffType.ON));
                            }
                            break;
                        case "Auto":
                            submitCommand(new SetOperationmodeCommand(OperationMode.AUTO));
                            if (madokaSettings.getOnOffState() == OnOffType.OFF) {
                                submitCommand(new SetPowerstateCommand(OnOffType.ON));
                            }
                            break;
                        default:
                            break;
                    }

                } catch (Exception e) {
                    logger.info("Error while setting mode through HomeKIt received Mode");
                }
            default:
                break;

        }

    }

    @Override
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic) {
        super.onCharacteristicUpdate(characteristic);

        // Check that arguments are valid.
        if (characteristic.getUuid() == null) {
            return;
        }

        // We are only interested in the Notify Characteristic of UART service
        if (!characteristic.getUuid().equals(DaikinMadokaBindingConstants.CHAR_NOTIF_UUID)) {
            return;
        }

        // A message cannot be null or have a 0-byte length
        if (characteristic.getByteValue() == null || characteristic.getByteValue().length == 0) {
            return;
        }

        this.uartProcessor.chunkReceived(characteristic.getByteValue());

    }

    private void submitCommand(BRC1HCommand command) {
        Executor executor = commandExecutor;

        if (executor != null) {
            executor.execute(() -> processCommand(command));
        }
    }

    private void processCommand(BRC1HCommand command) {
        logger.debug("[{}] ProcessCommand {}", super.thing.getUID().getId(), command.getClass().getSimpleName());

        try {
            currentCommand = command;
            uartProcessor.abandon();

            if (command.getRequest() == null) {
                logger.debug("Unable to send command {} to device {}: Request NULL.",
                        command.getClass().getSimpleName(), device.getAddress());
                command.setState(BRC1HCommand.State.FAILED);
                return;
            }

            if (device == null || device.getConnectionState() != ConnectionState.CONNECTED) {
                logger.debug("Unable to send command {} to device {}: not connected",
                        command.getClass().getSimpleName(), address);
                command.setState(BRC1HCommand.State.FAILED);
                return;
            }

            if (!resolved) {
                logger.debug("Unable to send command {} to device {}: services not resolved",
                        command.getClass().getSimpleName(), device.getAddress());
                command.setState(BRC1HCommand.State.FAILED);
                return;
            }

            BluetoothCharacteristic charWrite = device
                    .getCharacteristic(DaikinMadokaBindingConstants.CHAR_WRITE_WITHOUT_RESPONSE_UUID);
            if (charWrite == null) {
                logger.warn("Unable to execute {}. Characteristic '{}' could not be found.",
                        command.getClass().getSimpleName(),
                        DaikinMadokaBindingConstants.CHAR_WRITE_WITHOUT_RESPONSE_UUID);
                command.setState(BRC1HCommand.State.FAILED);
                return;
            }

            BluetoothCharacteristic charNotif = this.device
                    .getCharacteristic(DaikinMadokaBindingConstants.CHAR_NOTIF_UUID);

            if (charNotif != null) {
                device.enableNotifications(charNotif);
            }

            charWrite.setValue(command.getRequest());
            command.setState(BRC1HCommand.State.ENQUEUED);
            device.writeCharacteristic(charWrite);

            if (!command.awaitStateChange(this.config.commandTimeout, TimeUnit.MILLISECONDS,
                    BRC1HCommand.State.SUCCEEDED, BRC1HCommand.State.FAILED)) {
                logger.debug("Command {} to device {} timed out", command, device.getAddress());
                command.setState(BRC1HCommand.State.FAILED);
            }

        } catch (Exception e) {
            logger.debug("Error", e);
        } finally {
            logger.debug("Command final state: {}", command.getState());
            currentCommand = null;
        }

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void onCharacteristicWriteComplete(BluetoothCharacteristic characteristic,
            BluetoothCompletionStatus status) {

        super.onCharacteristicWriteComplete(characteristic, status);

        byte[] request = characteristic.getByteValue();

        BRC1HCommand command = currentCommand;

        if (command != null) {
            if (!Arrays.equals(request, command.getRequest())) {
                logger.debug("Write completed for unknown command");
                return;
            }
            switch (status) {
                case SUCCESS:
                    command.setState(BRC1HCommand.State.SENT);
                    break;
                case ERROR:
                    command.setState(BRC1HCommand.State.FAILED);
                    break;
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("No command found that matches request {}", HexUtils.bytesToHex(request));
            }
        }

    }

    /**
     * When the method is triggered, it means that all message chunks have been received, re-assembled in the right
     * order and that the payload is ready to be processed.
     */
    @Override
    public void receivedResponse(byte[] response) {
        logger.debug("Received Response");
        BRC1HCommand command = currentCommand;

        if (command == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("No command present to handle response {}", HexUtils.bytesToHex(response));
            }
        } else {
            try {
                if ((!command.handleResponse(scheduler, this, MadokaMessage.parse(response)))) {
                    logger.debug("Command {} could not handle response {}", command.getClass().getSimpleName(),
                            HexUtils.bytesToHex(response));
                }
            } catch (MadokaParsingException e) {
                logger.debug("Response message could not be parsed correctly ({}): {}",
                        command.getClass().getSimpleName(), HexUtils.bytesToHex(response));
            }
        }
    }

    @Override
    public void receivedResponse(GetVersionCommand command) {
        String commCtrlVers = command.getCommunicationControllerVersion();
        if (commCtrlVers != null) {
            this.madokaSettings.setCommunicationControllerVersion(commCtrlVers);
            updateStateIfLinked(
                    new ChannelUID(getThing().getUID(),
                            DaikinMadokaBindingConstants.CHANNEL_ID_COMMUNICATION_CONTROLLER_VERSION),
                    new StringType(commCtrlVers));
        }

        String remoteCtrlVers = command.getRemoteControllerVersion();
        if (remoteCtrlVers != null) {
            this.madokaSettings.setRemoteControllerVersion(remoteCtrlVers);
            updateStateIfLinked(
                    new ChannelUID(getThing().getUID(),
                            DaikinMadokaBindingConstants.CHANNEL_ID_REMOTE_CONTROLLER_VERSION),
                    new StringType(remoteCtrlVers));
        }
    }

    @Override
    public void receivedResponse(GetFanspeedCommand command) {

        if (command.getCoolingFanSpeed() == null || command.getHeatingFanSpeed() == null) {
            return;
        }

        // We need the current operation mode to determine which Fan Speed we use (cooling or heating)
        if (this.madokaSettings.getOperationMode() == null) {
            return;
        }

        FanSpeed fs = null;

        switch (this.madokaSettings.getOperationMode()) {
            case AUTO:
                // TODO confirm it works in all conditions
                logger.debug("In AutoMode, CoolingFanSpeed = {}, HeatingFanSpeed = {}", command.getCoolingFanSpeed(),
                        command.getHeatingFanSpeed());
                fs = command.getHeatingFanSpeed();
                break;
            case HEAT:
                fs = command.getHeatingFanSpeed();
                break;
            case COOL:
                fs = command.getCoolingFanSpeed();
                break;
            default:
                return;
        }

        // No need to re-set if it is the same value
        if (fs.equals(this.madokaSettings.getFanspeed())) {
            return;
        }

        this.madokaSettings.setFanspeed(fs);
        updateStateIfLinked(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_FAN_SPEED),
                new DecimalType(fs.value()));

    }

    @Override
    public void receivedResponse(GetSetpointCommand command) {
        if (command.getCoolingSetpoint() == null || command.getHeatingSetpoint() == null) {
            return;
        }

        // We need the current operation mode to determine which Fan Speed we use (cooling or heating)
        if (this.madokaSettings.getOperationMode() == null) {
            return;
        }

        DecimalType sp = null;

        switch (this.madokaSettings.getOperationMode()) {
            case AUTO:
                // TODO confirm it works in all conditions
                logger.debug("In AutoMode, CoolingSetpoint = {}, HeatingSetpoint = {}", command.getCoolingSetpoint(),
                        command.getHeatingSetpoint());
                sp = command.getHeatingSetpoint();
                break;
            case HEAT:
                sp = command.getHeatingSetpoint();
                break;
            case COOL:
                sp = command.getCoolingSetpoint();
                break;
            default:
                return;
        }

        // No need to re-set if it is the same value
        if (sp.equals(this.madokaSettings.getSetpoint())) {
            return;
        }

        this.madokaSettings.setSetpoint(sp);

        DecimalType dt = this.madokaSettings.getSetpoint();
        if (dt != null) {
            updateStateIfLinked(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_SETPOINT),
                    dt);
        }

    }

    @Override
    public void receivedResponse(GetOperationmodeCommand command) {
        if (command.getOperationMode() == null) {
            return;
        }

        OperationMode newMode = command.getOperationMode();
        // If the mode has not changed - no need to refresh everything
        if (newMode.equals(this.madokaSettings.getOperationMode())) {
            return;
        }

        this.madokaSettings.setOperationMode(newMode);

        updateStateIfLinked(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_OPERATION_MODE),
                new StringType(newMode.name()));

        // For HomeKit channel, we need to map it to HomeKit supported strings
        switch (newMode) {
            case COOL:
                updateStateIfLinked(
                        new ChannelUID(getThing().getUID(),
                                DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_CURRENT_HEATING_COOLING_MODE),
                        new StringType("Cooling"));
                break;
            case HEAT:
                updateStateIfLinked(
                        new ChannelUID(getThing().getUID(),
                                DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_CURRENT_HEATING_COOLING_MODE),
                        new StringType("Heating"));
                break;
            default:
                break;
        }

        // If this is the first channel update - then we set target = current mode
        if (this.madokaSettings.getHomekitTargetMode() == null) {
            String newHomekitTargetStatus = null;

            // For HomeKit channel, we need to map it to HomeKit supported strings
            switch (newMode) {
                case COOL:
                    newHomekitTargetStatus = "CoolOn";
                    break;
                case HEAT:
                    newHomekitTargetStatus = "HeatOn";
                    break;
                default:
                    return;
            }
            this.madokaSettings.setHomekitTargetMode(newHomekitTargetStatus);

            updateStateIfLinked(
                    new ChannelUID(getThing().getUID(),
                            DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_TARGET_HEATING_COOLING_MODE),
                    new StringType(newHomekitTargetStatus));
        }

    }

    @Override
    public void receivedResponse(GetPowerstateCommand command) {
        if (command.isPowerState() == null) {
            return;
        }

        OnOffType oot = command.isPowerState() ? OnOffType.ON : OnOffType.OFF;

        if (oot.equals(this.madokaSettings.getOnOffState())) {
            return;
        }

        this.madokaSettings.setOnOffState(oot);

        updateStateIfLinked(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_ONOFF_STATUS),
                oot);

        if (oot.equals(OnOffType.OFF)) {
            updateStateIfLinked(
                    new ChannelUID(getThing().getUID(),
                            DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_CURRENT_HEATING_COOLING_MODE),
                    new StringType("Off"));
            updateStateIfLinked(
                    new ChannelUID(getThing().getUID(),
                            DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_TARGET_HEATING_COOLING_MODE),
                    new StringType("Off"));
        }

    }

    @Override
    public void receivedResponse(GetIndoorOutoorTemperatures command) {

        DecimalType newIndoorTemp = command.getIndoorTemperature();
        if (newIndoorTemp != null) {
            if (!newIndoorTemp.equals(this.madokaSettings.getIndoorTemperature())) {
                this.madokaSettings.setIndoorTemperature(newIndoorTemp);
                updateStateIfLinked(
                        new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_INDOOR_TEMPERATURE),
                        newIndoorTemp);
            }
        }

        DecimalType newOutdoorTemp = command.getOutdoorTemperature();
        if (newOutdoorTemp == null) {
            updateStateIfLinked(
                    new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_OUTDOOR_TEMPERATURE),
                    UnDefType.UNDEF);
        } else if (!newOutdoorTemp.equals(this.madokaSettings.getOutdoorTemperature())) {
            this.madokaSettings.setOutdoorTemperature(newOutdoorTemp);
            updateStateIfLinked(
                    new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_OUTDOOR_TEMPERATURE),
                    newOutdoorTemp);
        }

    }

    @Override
    public void receivedResponse(SetPowerstateCommand command) {

        updateStateIfLinked(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_ONOFF_STATUS),
                command.getPowerState());

        madokaSettings.setOnOffState(command.getPowerState());

        if (command.getPowerState() == OnOffType.ON) {
            // Depending on the state
            if (madokaSettings.getOperationMode() == null) {
                return;
            }

            switch (madokaSettings.getOperationMode()) {
                case AUTO:
                    updateStateIfLinked(
                            new ChannelUID(getThing().getUID(),
                                    DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_CURRENT_HEATING_COOLING_MODE),
                            new StringType("Auto"));
                    break;
                case HEAT:
                    updateStateIfLinked(
                            new ChannelUID(getThing().getUID(),
                                    DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_CURRENT_HEATING_COOLING_MODE),
                            new StringType("Heating"));
                    break;
                case COOL:
                    updateStateIfLinked(
                            new ChannelUID(getThing().getUID(),
                                    DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_CURRENT_HEATING_COOLING_MODE),
                            new StringType("Cooling"));
                    break;
                default: // Other Modes are not [yet] supported
                    break;
            }
        } else {
            updateStateIfLinked(
                    new ChannelUID(getThing().getUID(),
                            DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_CURRENT_HEATING_COOLING_MODE),
                    new StringType("Off"));

        }
    }

    @Override
    public void receivedResponse(SetSetpointCommand command) {

        // The update depends on the mode - so if not set - skip
        if (this.madokaSettings.getOperationMode() == null) {
            return;
        }

        switch (this.madokaSettings.getOperationMode()) {
            case HEAT:
                this.madokaSettings.setSetpoint(command.getHeatingSetpoint());
                break;
            case COOL:
                this.madokaSettings.setSetpoint(command.getCoolingSetpoint());
                break;
            case AUTO:
                // Here we don't really care if we are taking cooling or heating...
                this.madokaSettings.setSetpoint(command.getCoolingSetpoint());
                break;
            default:
                return;
        }

        DecimalType dt = madokaSettings.getSetpoint();
        if (dt != null) {
            updateStateIfLinked(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_SETPOINT),
                    dt);
        }
    }

    /**
     * Received response to "SetOperationmodeCommand" command
     */
    @Override
    public void receivedResponse(SetOperationmodeCommand command) {
        this.madokaSettings.setOperationMode(command.getOperationMode());
        updateStateIfLinked(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_OPERATION_MODE),
                new StringType(command.getOperationMode().toString()));
    }

    /**
     * Received response to "SetFanSpeed" command
     */
    @Override
    public void receivedResponse(SetFanspeedCommand command) {

        // The update depends on the mode - so if not set - skip
        if (this.madokaSettings.getOperationMode() == null) {
            return;
        }

        switch (this.madokaSettings.getOperationMode()) {
            case HEAT:
                this.madokaSettings.setFanspeed(command.getHeatingFanSpeed());
                break;
            case COOL:
                this.madokaSettings.setFanspeed(command.getCoolingFanSpeed());
                break;
            case AUTO:
                // TODO
                break;
            default:
                return;
        }

        updateStateIfLinked(new ChannelUID(getThing().getUID(), DaikinMadokaBindingConstants.CHANNEL_ID_FAN_SPEED),
                new DecimalType(madokaSettings.getFanspeed().value()));
    }

    private void updateStateIfLinked(ChannelUID channelUID, State state) {
        if (isLinked(channelUID)) {
            updateState(channelUID, state);
        }
    }

}
