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
package org.openhab.binding.ventaair.internal;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ventaair.internal.message.action.Action;
import org.openhab.binding.ventaair.internal.message.action.AllActions;
import org.openhab.binding.ventaair.internal.message.action.AutomaticAction;
import org.openhab.binding.ventaair.internal.message.action.BoostAction;
import org.openhab.binding.ventaair.internal.message.action.ChildLockAction;
import org.openhab.binding.ventaair.internal.message.action.FanAction;
import org.openhab.binding.ventaair.internal.message.action.HumidityAction;
import org.openhab.binding.ventaair.internal.message.action.PowerAction;
import org.openhab.binding.ventaair.internal.message.action.SleepModeAction;
import org.openhab.binding.ventaair.internal.message.action.TimerAction;
import org.openhab.binding.ventaair.internal.message.dto.DeviceInfoMessage;
import org.openhab.binding.ventaair.internal.message.dto.Header;
import org.openhab.binding.ventaair.internal.message.dto.Info;
import org.openhab.binding.ventaair.internal.message.dto.Measurements;
import org.openhab.core.library.dimension.Density;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VentaThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Stefan Triller - Initial contribution
 */
@NonNullByDefault
public class VentaThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(VentaThingHandler.class);

    private VentaAirDeviceConfiguration config = new VentaAirDeviceConfiguration();

    private @Nullable Communicator communicator;

    private Map<String, State> channelValueCache = new HashMap<>();

    public VentaThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command={} for channel={} with channelID={}", command, channelUID, channelUID.getId());
        if (command instanceof RefreshType) {
            refreshChannelFromCache(channelUID);
            return;
        }

        switch (channelUID.getId()) {
            case VentaAirBindingConstants.CHANNEL_POWER:
                if (command instanceof OnOffType) {
                    dispatchActionToDevice(new PowerAction(command == OnOffType.ON));
                }
                break;
            case VentaAirBindingConstants.CHANNEL_FAN_SPEED:
                if (command instanceof DecimalType decimalCommand) {
                    int fanStage = decimalCommand.intValue();
                    dispatchActionToDevice(new FanAction(fanStage));
                }
                break;
            case VentaAirBindingConstants.CHANNEL_TARGET_HUMIDITY:
                if (command instanceof DecimalType decimalCommand) {
                    int targetHumidity = decimalCommand.intValue();
                    dispatchActionToDevice(new HumidityAction(targetHumidity));
                }
                break;
            case VentaAirBindingConstants.CHANNEL_TIMER:
                if (command instanceof DecimalType decimalCommand) {
                    int timer = decimalCommand.intValue();
                    dispatchActionToDevice(new TimerAction(timer));
                }
                break;
            case VentaAirBindingConstants.CHANNEL_SLEEP_MODE:
                if (command instanceof OnOffType) {
                    dispatchActionToDevice(new SleepModeAction(command == OnOffType.ON));
                }
                break;
            case VentaAirBindingConstants.CHANNEL_BOOST:
                if (command instanceof OnOffType) {
                    dispatchActionToDevice(new BoostAction(command == OnOffType.ON));
                }
                break;
            case VentaAirBindingConstants.CHANNEL_CHILD_LOCK:
                if (command instanceof OnOffType) {
                    dispatchActionToDevice(new ChildLockAction(command == OnOffType.ON));
                }
                break;
            case VentaAirBindingConstants.CHANNEL_AUTOMATIC:
                if (command instanceof OnOffType) {
                    dispatchActionToDevice(new AutomaticAction(command == OnOffType.ON));
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(VentaAirDeviceConfiguration.class);

        updateStatus(ThingStatus.UNKNOWN);

        String configErrorMessage;
        if ((configErrorMessage = validateConfig()) != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configErrorMessage);
            return;
        }

        Header header = new Header(config.macAddress, config.deviceType.intValue(), config.hash.toString(), "openHAB");

        communicator = new Communicator(config.ipAddress, header, config.pollingTime, new StateUpdatedCallback());
        communicator.startPollDataFromDevice(scheduler);
    }

    private @Nullable String validateConfig() {
        if (config.ipAddress.isEmpty()) {
            return "IP address not set";
        }
        if (config.macAddress.isEmpty()) {
            return "Mac Address not set, use discovery to find the correct one";
        }
        if (BigDecimal.ZERO.equals(config.deviceType)) {
            return "Device Type not set, use discovery to find the correct one";
        }
        if (config.pollingTime.compareTo(BigDecimal.ZERO) <= 0) {
            return "Polling time has to be larger than 0 seconds";
        }

        return null;
    }

    private void dispatchActionToDevice(Action action) {
        Communicator localCommunicator = communicator;
        if (localCommunicator != null) {
            logger.debug("Dispatching Action={} to the device", action.getClass());
            try {
                localCommunicator.sendActionToDevice(action);
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                return;
            }
            localCommunicator.pollDataFromDevice();
        } else {
            logger.error("Should send action={} to device but communicator is not available.", action.getClass());
        }
    }

    private void refreshChannelFromCache(ChannelUID channelUID) {
        State cachedState = channelValueCache.get(channelUID.getId());
        if (cachedState != null) {
            updateState(channelUID, cachedState);
        }
    }

    private void updateProperties(Info info) {
        Thing thing = getThing();
        thing.setProperty("SWDisplay", info.getSwDisplay());
        thing.setProperty("SWPower", info.getSwPower());
        thing.setProperty("SWTouch", info.getSwTouch());
        thing.setProperty("SWWIFI", info.getSwWIFI());
    }

    @Override
    public void dispose() {
        Communicator localCommunicator = communicator;
        if (localCommunicator != null) {
            localCommunicator.stopPollDataFromDevice();
        }
        communicator = null;
    }

    class StateUpdatedCallback {
        /**
         * Method to pass the data received from the device to the handler
         *
         * @param message - message containing the parsed data from the device
         */
        public void stateUpdated(DeviceInfoMessage message) {
            if (messageIsEmpty(message)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                        "Please allow openHAB to access your device");
                return;
            }

            AllActions actions = message.getCurrentActions();

            Unit<Temperature> temperatureUnit = SIUnits.CELSIUS;

            if (actions != null) {
                OnOffType powerState = OnOffType.from(actions.isPower());
                updateState(VentaAirBindingConstants.CHANNEL_POWER, powerState);
                channelValueCache.put(VentaAirBindingConstants.CHANNEL_POWER, powerState);

                DecimalType fanspeedState = new DecimalType(actions.getFanSpeed());
                updateState(VentaAirBindingConstants.CHANNEL_FAN_SPEED, fanspeedState);
                channelValueCache.put(VentaAirBindingConstants.CHANNEL_FAN_SPEED, fanspeedState);

                DecimalType targetHumState = new DecimalType(actions.getTargetHum());
                updateState(VentaAirBindingConstants.CHANNEL_TARGET_HUMIDITY, targetHumState);
                channelValueCache.put(VentaAirBindingConstants.CHANNEL_TARGET_HUMIDITY, targetHumState);

                DecimalType timerState = new DecimalType(actions.getTimer());
                updateState(VentaAirBindingConstants.CHANNEL_TIMER, timerState);
                channelValueCache.put(VentaAirBindingConstants.CHANNEL_TIMER, timerState);

                OnOffType sleepModeState = OnOffType.from(actions.isSleepMode());
                updateState(VentaAirBindingConstants.CHANNEL_SLEEP_MODE, sleepModeState);
                channelValueCache.put(VentaAirBindingConstants.CHANNEL_SLEEP_MODE, sleepModeState);

                OnOffType boostState = OnOffType.from(actions.isBoost());
                updateState(VentaAirBindingConstants.CHANNEL_BOOST, boostState);
                channelValueCache.put(VentaAirBindingConstants.CHANNEL_BOOST, boostState);

                OnOffType childLockState = OnOffType.from(actions.isChildLock());
                updateState(VentaAirBindingConstants.CHANNEL_CHILD_LOCK, childLockState);
                channelValueCache.put(VentaAirBindingConstants.CHANNEL_CHILD_LOCK, childLockState);

                OnOffType automaticState = OnOffType.from(actions.isAutomatic());
                updateState(VentaAirBindingConstants.CHANNEL_AUTOMATIC, automaticState);
                channelValueCache.put(VentaAirBindingConstants.CHANNEL_AUTOMATIC, automaticState);

                temperatureUnit = actions.getTempUnit() == 0 ? SIUnits.CELSIUS : ImperialUnits.FAHRENHEIT;
            }

            Measurements measurements = message.getMeasurements();

            if (measurements != null) {
                QuantityType<Temperature> temperatureState = new QuantityType<>(measurements.getTemperature(),
                        temperatureUnit);
                updateState(VentaAirBindingConstants.CHANNEL_TEMPERATURE, temperatureState);
                channelValueCache.put(VentaAirBindingConstants.CHANNEL_TEMPERATURE, temperatureState);

                QuantityType<Dimensionless> humidityState = new QuantityType<>(measurements.getHumidity(),
                        Units.PERCENT);
                updateState(VentaAirBindingConstants.CHANNEL_HUMIDITY, humidityState);
                channelValueCache.put(VentaAirBindingConstants.CHANNEL_HUMIDITY, humidityState);

                QuantityType<Density> pm25State = new QuantityType<>(measurements.getDust(),
                        Units.MICROGRAM_PER_CUBICMETRE);
                updateState(VentaAirBindingConstants.CHANNEL_PM25, pm25State);
                channelValueCache.put(VentaAirBindingConstants.CHANNEL_PM25, pm25State);

                DecimalType waterLevelState = new DecimalType(measurements.getWaterLevel());
                updateState(VentaAirBindingConstants.CHANNEL_WATERLEVEL, waterLevelState);
                channelValueCache.put(VentaAirBindingConstants.CHANNEL_WATERLEVEL, waterLevelState);

                DecimalType fanRPMstate = new DecimalType(measurements.getFanRpm());
                updateState(VentaAirBindingConstants.CHANNEL_FAN_RPM, fanRPMstate);
                channelValueCache.put(VentaAirBindingConstants.CHANNEL_FAN_RPM, fanRPMstate);
            }

            Info info = message.getInfo();
            if (info != null) {
                int opHours = info.getOperationT() * 5 / 60;
                int discReplaceHours = info.getDiscIonT() * 5 / 60;
                int cleaningHours = info.getCleaningT() * 5 / 60;

                QuantityType<Time> opHoursState = new QuantityType<>(opHours, Units.HOUR);
                updateState(VentaAirBindingConstants.CHANNEL_OPERATION_TIME, opHoursState);
                channelValueCache.put(VentaAirBindingConstants.CHANNEL_OPERATION_TIME, opHoursState);

                QuantityType<Time> discReplaceHoursState = new QuantityType<>(2200 - discReplaceHours, Units.HOUR);
                updateState(VentaAirBindingConstants.CHANNEL_DISC_REPLACE_TIME, discReplaceHoursState);
                channelValueCache.put(VentaAirBindingConstants.CHANNEL_DISC_REPLACE_TIME, discReplaceHoursState);

                QuantityType<Time> cleaningHoursState = new QuantityType<>(4400 - cleaningHours, Units.HOUR);
                updateState(VentaAirBindingConstants.CHANNEL_CLEANING_TIME, cleaningHoursState);
                channelValueCache.put(VentaAirBindingConstants.CHANNEL_CLEANING_TIME, cleaningHoursState);

                OnOffType cleanModeState = OnOffType.from(info.isCleanMode());
                updateState(VentaAirBindingConstants.CHANNEL_CLEAN_MODE, cleanModeState);
                channelValueCache.put(VentaAirBindingConstants.CHANNEL_CLEAN_MODE, cleanModeState);

                QuantityType<Time> timerTimePassedState = new QuantityType<>(info.getTimerT(), Units.MINUTE);
                updateState(VentaAirBindingConstants.CHANNEL_TIMER_TIME_PASSED, timerTimePassedState);
                channelValueCache.put(VentaAirBindingConstants.CHANNEL_TIMER_TIME_PASSED, timerTimePassedState);

                QuantityType<Time> serviceTimeState = new QuantityType<>(info.getServiceT(), Units.MINUTE);
                updateState(VentaAirBindingConstants.CHANNEL_SERVICE_TIME, serviceTimeState);
                channelValueCache.put(VentaAirBindingConstants.CHANNEL_SERVICE_TIME, serviceTimeState);

                updateProperties(info);
            }

            updateStatus(ThingStatus.ONLINE);
        }

        private boolean messageIsEmpty(DeviceInfoMessage message) {
            return (message.getCurrentActions() == null && message.getInfo() == null
                    && message.getMeasurements() == null);
        }

        /**
         * Method to inform the handler about a communication issue
         */
        public void communicationProblem() {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }
}
