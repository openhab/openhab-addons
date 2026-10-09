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
package org.openhab.binding.salus.internal.handler;

import static java.math.RoundingMode.HALF_EVEN;
import static org.openhab.binding.salus.internal.SalusBindingConstants.Channels.It600.*;
import static org.openhab.binding.salus.internal.SalusBindingConstants.It600Device.HoldType.*;
import static org.openhab.binding.salus.internal.SalusBindingConstants.SalusDevice.DSN;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatusDetail.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.salus.internal.rest.DeviceProperty;
import org.openhab.binding.salus.internal.rest.exceptions.AuthSalusApiException;
import org.openhab.binding.salus.internal.rest.exceptions.SalusApiException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Grześlowski - Initial contribution
 * @author Martin Grześlowski - AWS thermostat commands and temperature units
 * @author Martin Grześlowski - Thermostat settings and diagnostics
 */
@NonNullByDefault
public class It600Handler extends BaseThingHandler {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
    private static final Map<Long, String> SYSTEM_MODES = Map.of(0L, "OFF", 3L, "COOL", 4L, "HEAT");
    private static final Map<Long, String> DISPLAY_UNITS = Map.of(0L, "CELSIUS", 1L, "FAHRENHEIT");
    private static final Map<Long, String> CLOCK_FORMATS = Map.of(0L, "12H", 1L, "24H");
    private static final Pattern ERROR_PROPERTY = Pattern.compile("ep_?\\d+:sIT600TH:Error\\d{2}");
    private final Logger logger;
    @NonNullByDefault({})
    private String dsn;
    @NonNullByDefault({})
    private CloudApi cloudApi;
    private String channelPrefix = "";

    public It600Handler(Thing thing) {
        super(thing);
        logger = LoggerFactory.getLogger(It600Handler.class.getName() + "[" + thing.getUID().getId() + "]");
    }

    @Override
    public void initialize() {
        AbstractBridgeHandler<?> abstractBridgeHandler;
        {
            var bridge = getBridge();
            if (bridge == null) {
                updateStatus(BRIDGE_UNINITIALIZED, "@text/it600-handler.initialize.errors.no-bridge");
                return;
            }
            if (!(bridge.getHandler() instanceof AbstractBridgeHandler<?> cloudHandler)) {
                updateStatus(CONFIGURATION_ERROR, "@text/it600-handler.initialize.errors.bridge-wrong-type");
                return;
            }
            this.cloudApi = cloudHandler;
            abstractBridgeHandler = cloudHandler;
            channelPrefix = abstractBridgeHandler.channelPrefix();
        }

        dsn = (String) getConfig().get(DSN);

        if ("".equals(dsn)) {
            updateStatus(CONFIGURATION_ERROR, "@text/it600-handler.initialize.errors.no-dsn [\"" + DSN + "\"]");
            return;
        }

        try {
            var device = this.cloudApi.findDevice(dsn);
            // no device in cloud
            if (device.isEmpty()) {
                updateStatus(COMMUNICATION_ERROR,
                        "@text/it600-handler.initialize.errors.dsn-not-found [\"" + dsn + "\"]");
                return;
            }
            // device is not connected
            if (!device.get().connected()) {
                updateStatus(COMMUNICATION_ERROR,
                        "@text/it600-handler.initialize.errors.dsn-not-connected [\"" + dsn + "\"]");
                return;
            }
            // device is missing properties
            try {
                var deviceProperties = findDeviceProperties().stream().map(DeviceProperty::getName).toList();
                var result = new ArrayList<>(abstractBridgeHandler.it600RequiredChannels());
                result.removeAll(deviceProperties);
                if (!result.isEmpty()) {
                    updateStatus(CONFIGURATION_ERROR, "@text/it600-handler.initialize.errors.missing-channels [\"" + dsn
                            + "\", \"" + String.join(", ", result) + "\"]");
                    return;
                }
            } catch (SalusApiException ex) {
                updateStatus(COMMUNICATION_ERROR, ex.getLocalizedMessage());
                return;
            }
        } catch (Exception e) {
            updateStatus(COMMUNICATION_ERROR, "@text/it600-handler.initialize.errors.general-error");
            return;
        }

        // done
        updateStatus(ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            var id = channelUID.getId();
            switch (id) {
                case TEMPERATURE:
                    handleCommandForTemperature(channelUID, command);
                    break;
                case EXPECTED_TEMPERATURE:
                    handleCommandForExpectedTemperature(channelUID, command);
                    break;
                case WORK_TYPE:
                    handleCommandForWorkType(channelUID, command);
                    break;
                case RUNNING_STATE:
                    handleCommandForRunningState(channelUID, command);
                    break;
                case COOLING_SETPOINT:
                    handleTemperatureSetting(channelUID, command, "CoolingSetpoint_x100", 500, 3500);
                    break;
                case TEMPERATURE_OFFSET:
                    handleTemperatureSetting(channelUID, command, "TemperatureOffset", -300, 300);
                    break;
                case FROST_TEMPERATURE:
                    handleTemperatureSetting(channelUID, command, "FrostSetpoint_x100", 500, 1700);
                    break;
                case MINIMUM_SETPOINT:
                    handleTemperatureSetting(channelUID, command, "MinHeatSetpoint_x100", 500, 3500);
                    break;
                case MAXIMUM_SETPOINT:
                    handleTemperatureSetting(channelUID, command, "MaxHeatSetpoint_x100", 500, 3500);
                    break;
                case KEYPAD_LOCK:
                    handleSwitchSetting(channelUID, command, "LockKey");
                    break;
                case ALLOW_LOCAL_UNLOCK:
                    handleSwitchSetting(channelUID, command, "AllowUnlockFromDevice");
                    break;
                case VALVE_PROTECTION:
                    handleSwitchSetting(channelUID, command, "ValveProtection");
                    break;
                case DISPLAY_OFF:
                    handleSwitchSetting(channelUID, command, "ShutOffDisplay");
                    break;
                case SYSTEM_MODE:
                    handleEnumSetting(channelUID, command, "SystemMode", SYSTEM_MODES);
                    break;
                case DISPLAY_UNIT:
                    handleEnumSetting(channelUID, command, "TemperatureDisplayMode", DISPLAY_UNITS);
                    break;
                case CLOCK_FORMAT:
                    handleEnumSetting(channelUID, command, "TimeFormat24Hour", CLOCK_FORMATS);
                    break;
                case ACTIVE_ERRORS:
                    if (command instanceof RefreshType) {
                        refreshErrors(channelUID);
                    }
                    break;
                case RSSI, LQI:
                    if (command instanceof RefreshType) {
                        refreshRadio(channelUID);
                    }
                    break;
                default:
                    logger.warn("Unknown channel `{}` for command `{}`", id, command);
            }
        } catch (SalusApiException | AuthSalusApiException e) {
            logger.debug("Error while handling command `{}` on channel `{}`", command, channelUID, e);
            updateStatus(COMMUNICATION_ERROR, e.getLocalizedMessage());
        }
    }

    private void handleTemperatureSetting(ChannelUID channelUID, Command command, String property, long minimum,
            long maximum) throws SalusApiException, AuthSalusApiException {
        boolean offset = TEMPERATURE_OFFSET.equals(channelUID.getId());
        if (command instanceof RefreshType) {
            var value = findThermostatValue(property).orElse(null);
            State state = value != null && value >= minimum && value <= maximum
                    ? new QuantityType<>(BigDecimal.valueOf(value, 2), CELSIUS)
                    : UnDefType.UNDEF;
            updateOptionalState(channelUID, state);
            return;
        }
        BigDecimal temperature = null;
        if (command instanceof QuantityType<?> quantity) {
            var converted = offset ? quantity.toUnitRelative(CELSIUS.toString()) : quantity.toUnit(CELSIUS);
            if (converted != null) {
                temperature = converted.toBigDecimal();
            }
        } else if (command instanceof DecimalType decimal) {
            temperature = decimal.toBigDecimal();
        }
        if (temperature == null) {
            return;
        }
        long value;
        try {
            // Allow unit-conversion rounding noise without truncating a thermostat step.
            value = temperature.multiply(ONE_HUNDRED).setScale(6, HALF_EVEN).longValueExact();
        } catch (ArithmeticException e) {
            logger.debug("Invalid temperature setting {} for {}", command, channelUID);
            return;
        }
        if (value < minimum || value > maximum || value % 50 != 0 || !withinSetpointLimits(property, value)) {
            logger.debug("Temperature setting {} is outside the supported limits for {}", command, channelUID);
            return;
        }
        writeSetting(property, value);
    }

    private boolean withinSetpointLimits(String property, long value) throws SalusApiException, AuthSalusApiException {
        if ("TemperatureOffset".equals(property)) {
            return true;
        }
        boolean cooling = "CoolingSetpoint_x100".equals(property);
        var minimum = findThermostatValue(cooling ? "MinCoolSetpoint_x100" : "MinHeatSetpoint_x100").orElse(null);
        var maximum = findThermostatValue(cooling ? "MaxCoolSetpoint_x100" : "MaxHeatSetpoint_x100").orElse(null);
        return ("MinHeatSetpoint_x100".equals(property) || minimum == null || value >= minimum)
                && ("MaxHeatSetpoint_x100".equals(property) || maximum == null || value <= maximum);
    }

    private void handleSwitchSetting(ChannelUID channelUID, Command command, String property)
            throws SalusApiException, AuthSalusApiException {
        if (command instanceof RefreshType) {
            var value = findThermostatValue(property).orElse(null);
            State state = value != null && (value == 0 || value == 1) ? OnOffType.from(value == 1) : UnDefType.UNDEF;
            updateOptionalState(channelUID, state);
        } else if (command instanceof OnOffType onOff) {
            writeSetting(property, onOff == OnOffType.ON ? 1 : 0);
        }
    }

    private void handleEnumSetting(ChannelUID channelUID, Command command, String property, Map<Long, String> options)
            throws SalusApiException, AuthSalusApiException {
        if (command instanceof RefreshType) {
            var value = findThermostatValue(property).orElse(null);
            String option = value == null ? null : options.get(value);
            updateOptionalState(channelUID, option == null ? UnDefType.UNDEF : new StringType(option));
        } else if (command instanceof StringType string) {
            for (var option : options.entrySet()) {
                if (option.getValue().equals(string.toString())) {
                    writeSetting(property, option.getKey());
                    return;
                }
            }
        }
    }

    private Optional<Long> findThermostatValue(String property) throws SalusApiException, AuthSalusApiException {
        return findLongProperty(channelPrefix + ":sIT600TH:" + property, property).map(DeviceProperty::getValue);
    }

    private void writeSetting(String property, long value) throws SalusApiException, AuthSalusApiException {
        var setter = findLongProperty(channelPrefix + ":sIT600TH:Set" + property, "Set" + property);
        if (setter.isPresent() && !setter.get().getReadOnly()
                && cloudApi.setValueForProperty(dsn, setter.get().getName(), value)) {
            updateStatus(ONLINE);
        }
    }

    private void refreshErrors(ChannelUID channelUID) throws SalusApiException, AuthSalusApiException {
        var errors = new TreeSet<String>();
        boolean reported = false;
        for (var property : findDeviceProperties()) {
            if (ERROR_PROPERTY.matcher(property.getName()).matches()) {
                if (!(property instanceof DeviceProperty.LongDeviceProperty number)) {
                    updateOptionalState(channelUID, UnDefType.UNDEF);
                    return;
                }
                var value = number.getValue();
                if (value == null || value < 0 || value > 1) {
                    updateOptionalState(channelUID, UnDefType.UNDEF);
                    return;
                }
                reported = true;
                if (value == 1) {
                    errors.add(property.getName().substring(property.getName().lastIndexOf(':') + 1));
                }
            }
        }
        updateOptionalState(channelUID,
                reported ? new StringType(errors.isEmpty() ? "NONE" : String.join(", ", errors)) : UnDefType.UNDEF);
    }

    private void refreshRadio(ChannelUID channelUID) throws SalusApiException, AuthSalusApiException {
        boolean rssi = RSSI.equals(channelUID.getId());
        String property = rssi ? "LastMessageRSSI_d" : "LastMessageLQI_d";
        var value = findLongProperty(channelPrefix + ":sIT600I:" + property, property).map(DeviceProperty::getValue)
                .orElse(null);
        State state = UnDefType.UNDEF;
        if (value != null) {
            state = rssi ? new QuantityType<>(value, Units.DECIBEL_MILLIWATTS) : new DecimalType(value);
        }
        updateOptionalState(channelUID, state);
    }

    private void updateOptionalState(ChannelUID channelUID, State state) {
        updateState(channelUID, state);
        if (state != UnDefType.UNDEF) {
            updateStatus(ONLINE);
        }
    }

    private void handleCommandForTemperature(ChannelUID channelUID, Command command)
            throws SalusApiException, AuthSalusApiException {
        if (!(command instanceof RefreshType)) {
            // only refresh commands are supported for temp channel
            return;
        }

        findLongProperty(channelPrefix + ":sIT600TH:LocalTemperature_x100", "LocalTemperature_x100")
                .map(DeviceProperty.LongDeviceProperty::getValue).map(BigDecimal::new)
                .map(value -> value.divide(ONE_HUNDRED, new MathContext(5, HALF_EVEN)))
                .map(value -> new QuantityType<>(value, CELSIUS)).ifPresent(state -> {
                    updateState(channelUID, state);
                    updateStatus(ONLINE);
                });
    }

    private void handleCommandForExpectedTemperature(ChannelUID channelUID, Command command)
            throws SalusApiException, AuthSalusApiException {
        if (command instanceof RefreshType) {
            findLongProperty(channelPrefix + ":sIT600TH:HeatingSetpoint_x100", "HeatingSetpoint_x100")
                    .map(DeviceProperty.LongDeviceProperty::getValue).map(BigDecimal::new)
                    .map(value -> value.divide(ONE_HUNDRED, new MathContext(5, HALF_EVEN)))
                    .map(value -> new QuantityType<>(value, CELSIUS)).ifPresent(state -> {
                        updateState(channelUID, state);
                        updateStatus(ONLINE);
                    });
            return;
        }

        BigDecimal rawValue = null;
        if (command instanceof QuantityType<?> commandAsQuantityType) {
            var celsius = commandAsQuantityType.toUnit(CELSIUS);
            if (celsius != null) {
                rawValue = celsius.toBigDecimal();
            }
        } else if (command instanceof DecimalType commandAsDecimalType) {
            rawValue = commandAsDecimalType.toBigDecimal();
        }

        if (rawValue != null) {
            var value = rawValue.multiply(ONE_HUNDRED).longValue();
            var property = findLongProperty(channelPrefix + ":sIT600TH:SetHeatingSetpoint_x100",
                    "SetHeatingSetpoint_x100");
            if (property.isEmpty()) {
                return;
            }
            var wasSet = cloudApi.setValueForProperty(dsn, property.get().getName(), value);
            if (wasSet) {
                updateStatus(ONLINE);
            }
            return;
        }

        logger.debug("Does not know how to handle command `{}` ({}) on channel `{}`!", command,
                command.getClass().getSimpleName(), channelUID);
    }

    private void handleCommandForWorkType(ChannelUID channelUID, Command command)
            throws SalusApiException, AuthSalusApiException {
        if (command instanceof RefreshType) {
            findLongProperty(channelPrefix + ":sIT600TH:HoldType", "HoldType")
                    .map(DeviceProperty.LongDeviceProperty::getValue).map(value -> switch (value.intValue()) {
                        case AUTO -> "AUTOMATIC";
                        case MANUAL -> "MANUAL";
                        case TEMPORARY_MANUAL -> "TEMPORARY_MANUAL";
                        case OFF -> "OFF";
                        default -> {
                            logger.warn("Unknown value {} for property HoldType!", value);
                            yield "AUTOMATIC";
                        }
                    }).map(StringType::new).ifPresent(state -> {
                        updateState(channelUID, state);
                        updateStatus(ONLINE);
                    });
            return;
        }

        if (command instanceof StringType typedCommand) {
            long value;
            if ("AUTOMATIC".equals(typedCommand.toString()) || "AUTO".equals(typedCommand.toString())) {
                value = AUTO;
            } else if ("MANUAL".equals(typedCommand.toString())) {
                value = MANUAL;
            } else if ("TEMPORARY_MANUAL".equals(typedCommand.toString())) {
                value = TEMPORARY_MANUAL;
            } else if ("OFF".equals(typedCommand.toString())) {
                value = OFF;
            } else {
                logger.warn("Unknown value `{}` for property HoldType!", typedCommand);
                return;
            }
            var property = findLongProperty(channelPrefix + ":sIT600TH:SetHoldType", "SetHoldType");
            if (property.isEmpty()) {
                return;
            }
            if (cloudApi.setValueForProperty(dsn, property.get().getName(), value)) {
                updateStatus(ONLINE);
            }
            return;
        }

        logger.debug("Does not know how to handle command `{}` ({}) on channel `{}`!", command,
                command.getClass().getSimpleName(), channelUID);
    }

    private void handleCommandForRunningState(ChannelUID channelUID, Command command)
            throws SalusApiException, AuthSalusApiException {
        if (!(command instanceof RefreshType)) {
            return;
        }
        findLongProperty(channelPrefix + ":sIT600TH:RunningState", "RunningState")//
                .map(DeviceProperty::getValue)//
                .map(value -> value > 0)//
                .map(OnOffType::from)//
                .ifPresent(state -> {
                    updateState(channelUID, state);
                    updateStatus(ONLINE);
                });
    }

    private Optional<DeviceProperty.LongDeviceProperty> findLongProperty(String name, String shortName)
            throws SalusApiException, AuthSalusApiException {
        var deviceProperties = findDeviceProperties();
        var property = deviceProperties.stream()//
                .filter(p -> p.getName().equals(name))//
                .filter(DeviceProperty.LongDeviceProperty.class::isInstance)//
                .map(DeviceProperty.LongDeviceProperty.class::cast)//
                .findAny();
        if (property.isEmpty()) {
            property = deviceProperties.stream()//
                    .filter(p -> p.getName().endsWith(":" + shortName) || p.getName().equals(shortName))//
                    .filter(DeviceProperty.LongDeviceProperty.class::isInstance)//
                    .map(DeviceProperty.LongDeviceProperty.class::cast)//
                    .findAny();
        }
        if (property.isEmpty()) {
            logger.debug("{}/{} property not found!", name, shortName);
        }
        return property;
    }

    private SortedSet<DeviceProperty<?>> findDeviceProperties() throws SalusApiException, AuthSalusApiException {
        return this.cloudApi.findPropertiesForDevice(dsn);
    }
}
