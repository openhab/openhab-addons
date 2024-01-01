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
package org.openhab.binding.bluetooth.daikinmadoka.handler;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothCharacteristic;
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
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.DisableCleanFilterIndicatorCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.EnterPrivilegedModeCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetCleanFilterIndicatorCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetEyeBrightnessCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetFanspeedCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetIndoorOutoorTemperatures;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetOperationHoursCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetOperationmodeCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetPowerstateCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetSetpointCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.GetVersionCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.ResetCleanFilterTimerCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.ResponseListener;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.SetEyeBrightnessCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.SetFanspeedCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.SetOperationmodeCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.SetPowerstateCommand;
import org.openhab.binding.bluetooth.daikinmadoka.internal.model.commands.SetSetpointCommand;
import org.openhab.core.common.NamedThreadFactory;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
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
 * The {@link DaikinMadokaHandler} is responsible for handling commands, which are
 * sent to one of the channels as well as updating channel values.
 *
 * @author Benjamin Lafois - Initial contribution
 */
@NonNullByDefault
public class DaikinMadokaHandler extends ConnectedBluetoothHandler implements ResponseListener {

    private final Logger logger = LoggerFactory.getLogger(DaikinMadokaHandler.class);

    private DaikinMadokaConfiguration config = new DaikinMadokaConfiguration();

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
            submitCommand(new GetCleanFilterIndicatorCommand());

            try {
                // As it is a complex operation - it has been extracted to a method.
                retrieveOperationHours();
            } catch (InterruptedException e) {
                // The thread wants to exit!
                return;
            }

            submitCommand(new GetEyeBrightnessCommand());
        }, new Random().nextInt(30), config.refreshInterval, TimeUnit.SECONDS); // We introduce a random start time, it
        // avoids when having multiple devices to
        // have the commands sent simultaneously.
    }

    private void retrieveOperationHours() throws InterruptedException {
        // This one is special - and MUST be ran twice, after being in priv mode
        // run it once an hour is sufficient... TODO
        submitCommand(new EnterPrivilegedModeCommand());
        submitCommand(new GetOperationHoursCommand());
        // a 1second+ delay is necessary
        Thread.sleep(1500);

        submitCommand(new GetOperationHoursCommand());
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
            case DaikinMadokaBindingConstants.CHANNEL_ID_CLEAN_FILTER_INDICATOR:
                OnOffType cleanFilterOrder = (OnOffType) command;
                if (cleanFilterOrder == OnOffType.OFF) {
                    resetCleanFilterIndicator();
                }
                break;
            case DaikinMadokaBindingConstants.CHANNEL_ID_SETPOINT:
                try {
                    QuantityType<Temperature> setpoint = (QuantityType<Temperature>) command;
                    submitCommand(new SetSetpointCommand(setpoint, setpoint));
                } catch (Exception e) {
                    logger.warn("Data received is not a valid temperature.", e);
                }
                break;
            case DaikinMadokaBindingConstants.CHANNEL_ID_EYE_BRIGHTNESS:
                try {
                    logger.debug("Set eye brightness with value {}, {}", command.getClass().getName(), command);
                    PercentType p = (PercentType) command;
                    submitCommand(new SetEyeBrightnessCommand(p));
                } catch (Exception e) {
                    logger.warn("Data received is not a valid Eye Brightness status", e);
                }
                break;
            case DaikinMadokaBindingConstants.CHANNEL_ID_ONOFF_STATUS:
                try {
                    OnOffType oot = (OnOffType) command;
                    submitCommand(new SetPowerstateCommand(oot));
                } catch (Exception e) {
                    logger.warn("Data received is not a valid on/off status", e);
                }
                break;
            case DaikinMadokaBindingConstants.CHANNEL_ID_FAN_SPEED:
                try {
                    DecimalType fanSpeed = (DecimalType) command;
                    FanSpeed fs = FanSpeed.valueOf(fanSpeed.intValue());
                    submitCommand(new SetFanspeedCommand(fs, fs));
                } catch (Exception e) {
                    logger.warn("Data received is not a valid FanSpeed status", e);
                }
                break;
            case DaikinMadokaBindingConstants.CHANNEL_ID_OPERATION_MODE:
                try {
                    StringType operationMode = (StringType) command;
                    OperationMode m = OperationMode.valueOf(operationMode.toFullString());

                    submitCommand(new SetOperationmodeCommand(m));
                } catch (Exception e) {
                    logger.warn("Data received is not a valid OPERATION MODE", e);
                }
                break;
            case DaikinMadokaBindingConstants.CHANNEL_ID_HOMEBRIDGE_MODE:
                try {
                    // Homebridge are discrete value different from Daikin
                    // 0 - Off
                    // 1 - Heating
                    // 2 - Cooling
                    // 3 - Auto
                    DecimalType homebridgeMode = (DecimalType) command;
                    switch (homebridgeMode.intValue()) {
                        case 0: // Off
                            submitCommand(new SetPowerstateCommand(OnOffType.OFF));
                            break;
                        case 1: // Heating
                            submitCommand(new SetOperationmodeCommand(OperationMode.HEAT));
                            if (madokaSettings.getOnOffState() == OnOffType.OFF) {
                                submitCommand(new SetPowerstateCommand(OnOffType.ON));
                            }
                            break;
                        case 2: // Cooling
                            submitCommand(new SetOperationmodeCommand(OperationMode.COOL));
                            if (madokaSettings.getOnOffState() == OnOffType.OFF) {
                                submitCommand(new SetPowerstateCommand(OnOffType.ON));
                            }
                            break;
                        case 3: // Auto
                            submitCommand(new SetOperationmodeCommand(OperationMode.AUTO));
                            if (madokaSettings.getOnOffState() == OnOffType.OFF) {
                                submitCommand(new SetPowerstateCommand(OnOffType.ON));
                            }
                            break;
                        default: // Invalid Value - in case of new FW
                            logger.warn("Invalid value received for channel {}. Ignoring.", channelUID);
                            break;
                    }
                } catch (Exception e) {
                    logger.warn("Data received is not a valid HOMEBRIDGE OPERATION MODE", e);
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

    /**
     * 2 actions need to be done: disable the notification AND reset the filter timer
     */
    private void resetCleanFilterIndicator() {
        logger.debug("[{}] resetCleanFilterIndicator()", super.thing.getUID().getId());
        submitCommand(new DisableCleanFilterIndicatorCommand());
        submitCommand(new ResetCleanFilterTimerCommand());
    }

    @Override
    public void onCharacteristicUpdate(BluetoothCharacteristic characteristic, byte[] msgBytes) {
        if (logger.isDebugEnabled()) {
            logger.debug("[{}] onCharacteristicUpdate({})", super.thing.getUID().getId(),
                    HexUtils.bytesToHex(msgBytes));
        }
        super.onCharacteristicUpdate(characteristic, msgBytes);

        // Check that arguments are valid.
        if (characteristic.getUuid() == null) {
            return;
        }

        // We are only interested in the Notify Characteristic of UART service
        if (!characteristic.getUuid().equals(DaikinMadokaBindingConstants.CHAR_NOTIF_UUID)) {
            return;
        }

        // A message cannot have a 0-byte length
        if (msgBytes.length == 0) {
            return;
        }

        this.uartProcessor.chunkReceived(msgBytes);
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

            if (device == null || device.getConnectionState() != ConnectionState.CONNECTED) {
                logger.debug("Unable to send command {} to device {}: not connected",
                        command.getClass().getSimpleName(), address);
                command.setState(BRC1HCommand.State.FAILED);
                return;
            }

            if (!device.isServicesDiscovered()) {
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

            // Commands can be composed of multiple chunks
            for (byte[] chunk : command.getRequest()) {
                command.setState(BRC1HCommand.State.ENQUEUED);
                for (int i = 0; i < DaikinMadokaBindingConstants.WRITE_CHARACTERISTIC_MAX_RETRIES; i++) {
                    try {
                        device.writeCharacteristic(charWrite, chunk).get(100, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ex) {
                        return;
                    } catch (ExecutionException ex) {
                        logger.debug("Error while writing message {}: {}", command.getClass().getSimpleName(),
                                ex.getMessage());
                        Thread.sleep(100);
                        continue;
                    } catch (TimeoutException ex) {
                        Thread.sleep(100);
                        continue;
                    }
                    command.setState(BRC1HCommand.State.SENT);
                    break;
                }
            }

            if (command.getState() == BRC1HCommand.State.SENT) {
                if (!command.awaitStateChange(config.commandTimeout, TimeUnit.MILLISECONDS,
                        BRC1HCommand.State.SUCCEEDED, BRC1HCommand.State.FAILED)) {
                    logger.debug("[{}] Command {} to device {} timed out", super.thing.getUID().getId(), command,
                            device.getAddress());
                    command.setState(BRC1HCommand.State.FAILED);
                }
            }
        } catch (Exception e) {
            currentCommand = null;
            // Let the exception bubble the stack!
            throw new RuntimeException(e);
        }

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
                command.handleResponse(scheduler, this, MadokaMessage.parse(response));
            } catch (MadokaParsingException e) {
                logger.debug("Response message could not be parsed correctly ({}): {}. Reason: {}",
                        command.getClass().getSimpleName(), HexUtils.bytesToHex(response), e.getMessage());
            }
        }
    }

    @Override
    public void receivedResponse(GetVersionCommand command) {
        String commCtrlVers = command.getCommunicationControllerVersion();
        if (commCtrlVers != null) {
            this.madokaSettings.setCommunicationControllerVersion(commCtrlVers);
            updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_COMMUNICATION_CONTROLLER_VERSION,
                    new StringType(commCtrlVers));
        }

        String remoteCtrlVers = command.getRemoteControllerVersion();
        if (remoteCtrlVers != null) {
            this.madokaSettings.setRemoteControllerVersion(remoteCtrlVers);
            updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_REMOTE_CONTROLLER_VERSION,
                    new StringType(remoteCtrlVers));
        }
    }

    @Override
    public void receivedResponse(GetFanspeedCommand command) {
        if (command.getCoolingFanSpeed() == null || command.getHeatingFanSpeed() == null) {
            return;
        }

        // We need the current operation mode to determine which Fan Speed we use (cooling or heating)
        OperationMode operationMode = this.madokaSettings.getOperationMode();
        if (operationMode == null) {
            return;
        }

        FanSpeed fs;

        switch (operationMode) {
            case AUTO:
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

        if (fs == null) {
            return;
        }

        // No need to re-set if it is the same value
        if (fs.equals(this.madokaSettings.getFanspeed())) {
            return;
        }

        this.madokaSettings.setFanspeed(fs);
        updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_FAN_SPEED, new DecimalType(fs.value()));
    }

    @Override
    public void receivedResponse(GetSetpointCommand command) {
        if (command.getCoolingSetpoint() == null || command.getHeatingSetpoint() == null) {
            return;
        }

        // We need the current operation mode to determine which Fan Speed we use (cooling or heating)
        OperationMode operationMode = this.madokaSettings.getOperationMode();
        if (operationMode == null) {
            return;
        }

        QuantityType<Temperature> sp;

        switch (operationMode) {
            case AUTO:
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

        if (sp == null) {
            return;
        }

        // No need to re-set if it is the same value
        if (sp.equals(this.madokaSettings.getSetpoint())) {
            return;
        }

        this.madokaSettings.setSetpoint(sp);

        QuantityType<Temperature> dt = this.madokaSettings.getSetpoint();
        if (dt != null) {
            updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_SETPOINT, dt);
        }
    }

    @Override
    public void receivedResponse(GetOperationmodeCommand command) {
        if (command.getOperationMode() == null) {
            logger.debug("OperationMode is null.");
            return;
        }

        OperationMode newMode = command.getOperationMode();

        if (newMode == null) {
            return;
        }

        this.madokaSettings.setOperationMode(newMode);

        updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_OPERATION_MODE, new StringType(newMode.name()));

        // For HomeKit channel, we need to map it to HomeKit supported strings
        OnOffType ooStatus = madokaSettings.getOnOffState();

        if (ooStatus != null && ooStatus == OnOffType.ON) {
            switch (newMode) {
                case COOL:
                    updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_CURRENT_HEATING_COOLING_MODE,
                            new StringType("Cooling"));
                    updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_HOMEBRIDGE_MODE, new DecimalType(2));
                    break;
                case HEAT:
                    updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_CURRENT_HEATING_COOLING_MODE,
                            new StringType("Heating"));
                    updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_HOMEBRIDGE_MODE, new DecimalType(1));
                    break;
                case AUTO:
                    updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_CURRENT_HEATING_COOLING_MODE,
                            new StringType("Auto"));
                    updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_HOMEBRIDGE_MODE, new DecimalType(3));
                default:
                    break;
            }
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

            if (ooStatus != null && ooStatus == OnOffType.ON) {
                this.madokaSettings.setHomekitTargetMode(newHomekitTargetStatus);
                updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_TARGET_HEATING_COOLING_MODE,
                        new StringType(newHomekitTargetStatus));
            } else if (ooStatus != null && ooStatus == OnOffType.OFF) {
                newHomekitTargetStatus = "Off";
                this.madokaSettings.setHomekitTargetMode(newHomekitTargetStatus);
                updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_TARGET_HEATING_COOLING_MODE,
                        new StringType(newHomekitTargetStatus));
            }

        }
    }

    @Override
    public void receivedResponse(GetPowerstateCommand command) {
        if (command.isPowerState() == null) {
            return;
        }

        OnOffType oot = OnOffType.from(command.isPowerState());

        this.madokaSettings.setOnOffState(oot);

        updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_ONOFF_STATUS, oot);

        if (oot.equals(OnOffType.OFF)) {
            updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_CURRENT_HEATING_COOLING_MODE,
                    new StringType("Off"));
            updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_TARGET_HEATING_COOLING_MODE,
                    new StringType("Off"));
            updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_HOMEBRIDGE_MODE, new DecimalType(0));
        }
    }

    @Override
    public void receivedResponse(GetIndoorOutoorTemperatures command) {
        QuantityType<Temperature> newIndoorTemp = command.getIndoorTemperature();
        if (newIndoorTemp != null) {
            updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_INDOOR_TEMPERATURE, newIndoorTemp);
            this.madokaSettings.setIndoorTemperature(newIndoorTemp);
        }

        QuantityType<Temperature> newOutdoorTemp = command.getOutdoorTemperature();
        if (newOutdoorTemp == null) {
            updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_OUTDOOR_TEMPERATURE, UnDefType.UNDEF);
        } else {
            this.madokaSettings.setOutdoorTemperature(newOutdoorTemp);
            updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_OUTDOOR_TEMPERATURE, newOutdoorTemp);
        }
    }

    @Override
    public void receivedResponse(GetEyeBrightnessCommand command) {
        PercentType eyeBrightnessTemp = command.getEyeBrightness();
        if (eyeBrightnessTemp != null) {
            this.madokaSettings.setEyeBrightness(eyeBrightnessTemp);
            updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_EYE_BRIGHTNESS, eyeBrightnessTemp);
            logger.debug("Notified {} channel with value {}", DaikinMadokaBindingConstants.CHANNEL_ID_EYE_BRIGHTNESS,
                    eyeBrightnessTemp);
        }
    }

    @Override
    public void receivedResponse(SetEyeBrightnessCommand command) {
        updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_EYE_BRIGHTNESS, command.getEyeBrightness());
        madokaSettings.setEyeBrightness(command.getEyeBrightness());
    }

    @Override
    public void receivedResponse(SetPowerstateCommand command) {
        updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_ONOFF_STATUS, command.getPowerState());

        madokaSettings.setOnOffState(command.getPowerState());

        if (command.getPowerState() == OnOffType.ON) {
            // Depending on the state

            OperationMode operationMode = madokaSettings.getOperationMode();
            if (operationMode == null) {
                return;
            }

            switch (operationMode) {
                case AUTO:
                    updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_CURRENT_HEATING_COOLING_MODE,
                            new StringType("Auto"));
                    updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_HOMEBRIDGE_MODE, new DecimalType(3));
                    break;
                case HEAT:
                    updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_CURRENT_HEATING_COOLING_MODE,
                            new StringType("Heating"));
                    updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_HOMEBRIDGE_MODE, new DecimalType(1));
                    break;
                case COOL:
                    updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_CURRENT_HEATING_COOLING_MODE,
                            new StringType("Cooling"));
                    updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_HOMEBRIDGE_MODE, new DecimalType(2));
                    break;
                default: // Other Modes are not [yet] supported
                    break;
            }
        } else {
            updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_HOMEKIT_CURRENT_HEATING_COOLING_MODE,
                    new StringType("Off"));
            updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_HOMEBRIDGE_MODE, new DecimalType(0));
        }
    }

    @Override
    public void receivedResponse(GetOperationHoursCommand command) {
        logger.debug("receivedResponse(GetOperationHoursCommand command)");

        QuantityType<Time> indoorPowerHours = command.getIndoorPowerHours();
        QuantityType<Time> indoorOperationHours = command.getIndoorOperationHours();
        QuantityType<Time> indoorFanHours = command.getIndoorFanHours();

        if (indoorPowerHours != null) {
            this.madokaSettings.setIndoorPowerHours(indoorPowerHours);
            updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_INDOOR_POWER_HOURS, indoorPowerHours);
            logger.debug("Notified {} channel with value {}",
                    DaikinMadokaBindingConstants.CHANNEL_ID_INDOOR_POWER_HOURS, indoorPowerHours);
        }

        if (indoorOperationHours != null) {
            this.madokaSettings.setIndoorOperationHours(indoorOperationHours);
            updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_INDOOR_OPERATION_HOURS, indoorOperationHours);
            logger.debug("Notified {} channel with value {}",
                    DaikinMadokaBindingConstants.CHANNEL_ID_INDOOR_OPERATION_HOURS, indoorOperationHours);
        }

        if (indoorFanHours != null) {
            this.madokaSettings.setIndoorFanHours(indoorFanHours);
            updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_INDOOR_FAN_HOURS, indoorFanHours);
            logger.debug("Notified {} channel with value {}", DaikinMadokaBindingConstants.CHANNEL_ID_INDOOR_FAN_HOURS,
                    indoorFanHours);
        }
    }

    @Override
    public void receivedResponse(SetSetpointCommand command) {
        // The update depends on the mode - so if not set - skip
        OperationMode operationMode = this.madokaSettings.getOperationMode();
        if (operationMode == null) {
            return;
        }

        switch (operationMode) {
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

        QuantityType<Temperature> dt = madokaSettings.getSetpoint();
        if (dt != null) {
            updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_SETPOINT, dt);
        }
    }

    @Override
    public void receivedResponse(GetCleanFilterIndicatorCommand command) {
        Boolean indicatorStatus = command.getCleanFilterIndicator();
        if (indicatorStatus != null) {
            this.madokaSettings.setCleanFilterIndicator(indicatorStatus);
            updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_CLEAN_FILTER_INDICATOR,
                    OnOffType.from(indicatorStatus));
        }
    }

    /**
     * Received response to "SetOperationmodeCommand" command
     */
    @Override
    public void receivedResponse(SetOperationmodeCommand command) {
        this.madokaSettings.setOperationMode(command.getOperationMode());
        updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_OPERATION_MODE,
                new StringType(command.getOperationMode().toString()));
    }

    /**
     * Received response to "SetFanSpeed" command
     */
    @Override
    public void receivedResponse(SetFanspeedCommand command) {
        // The update depends on the mode - so if not set - skip
        OperationMode operationMode = this.madokaSettings.getOperationMode();
        if (operationMode == null) {
            return;
        }

        FanSpeed fanSpeed;
        switch (operationMode) {
            case HEAT:
                fanSpeed = command.getHeatingFanSpeed();
                this.madokaSettings.setFanspeed(fanSpeed);
                break;
            case COOL:
                fanSpeed = command.getCoolingFanSpeed();
                this.madokaSettings.setFanspeed(fanSpeed);
                break;
            case AUTO:
                fanSpeed = command.getCoolingFanSpeed(); // Arbitrary cooling or heating... They are the same!
                this.madokaSettings.setFanspeed(fanSpeed);
                break;
            default:
                return;
        }

        updateStateIfLinked(DaikinMadokaBindingConstants.CHANNEL_ID_FAN_SPEED, new DecimalType(fanSpeed.value()));
    }

    private void updateStateIfLinked(String channelId, State state) {
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), channelId);
        if (isLinked(channelUID)) {
            updateState(channelUID, state);
        }
    }
}
