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
package org.openhab.binding.salus.internal.handler;

import static java.math.RoundingMode.HALF_EVEN;
import static java.util.Objects.requireNonNull;
import static org.openhab.binding.salus.internal.SalusBindingConstants.Channels.It600.*;
import static org.openhab.binding.salus.internal.SalusBindingConstants.It600Device.HoldType.*;
import static org.openhab.binding.salus.internal.SalusBindingConstants.SalusDevice.DSN;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatusDetail.*;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Optional;
import java.util.SortedSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.salus.internal.rest.DeviceProperty;
import org.openhab.binding.salus.internal.rest.exceptions.AuthSalusApiException;
import org.openhab.binding.salus.internal.rest.exceptions.SalusApiException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class It600Handler extends BaseThingHandler {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
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
                updateStatus(OFFLINE, BRIDGE_UNINITIALIZED, "@text/it600-handler.initialize.errors.no-bridge");
                return;
            }
            if (!(bridge.getHandler() instanceof AbstractBridgeHandler<?> cloudHandler)) {
                updateStatus(OFFLINE, BRIDGE_UNINITIALIZED, "@text/it600-handler.initialize.errors.bridge-wrong-type");
                return;
            }
            this.cloudApi = cloudHandler;
            abstractBridgeHandler = cloudHandler;
            channelPrefix = abstractBridgeHandler.channelPrefix();
        }

        dsn = (String) getConfig().get(DSN);

        if ("".equals(dsn)) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR,
                    "@text/it600-handler.initialize.errors.no-dsn [\"" + DSN + "\"]");
            return;
        }

        try {
            var device = this.cloudApi.findDevice(dsn);
            // no device in cloud
            if (device.isEmpty()) {
                updateStatus(OFFLINE, COMMUNICATION_ERROR,
                        "@text/it600-handler.initialize.errors.dsn-not-found [\"" + dsn + "\"]");
                return;
            }
            // device is not connected
            if (!device.get().connected()) {
                updateStatus(OFFLINE, COMMUNICATION_ERROR,
                        "@text/it600-handler.initialize.errors.dsn-not-connected [\"" + dsn + "\"]");
                return;
            }
            // device is missing properties
            try {
                var deviceProperties = findDeviceProperties().stream().map(DeviceProperty::getName).toList();
                var result = new ArrayList<>(abstractBridgeHandler.it600RequiredChannels());
                result.removeAll(deviceProperties);
                if (!result.isEmpty()) {
                    updateStatus(OFFLINE, CONFIGURATION_ERROR,
                            "@text/it600-handler.initialize.errors.missing-channels [\"" + dsn + "\", \""
                                    + String.join(", ", result) + "\"]");
                    return;
                }
            } catch (SalusApiException ex) {
                updateStatus(OFFLINE, COMMUNICATION_ERROR, ex.getLocalizedMessage());
                return;
            }
        } catch (Exception e) {
            updateStatus(OFFLINE, COMMUNICATION_ERROR, "@text/it600-handler.initialize.errors.general-error");
            return;
        }

        // done
        updateStatus(ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command != REFRESH && cloudApi.isReadOnly()) {
            return;
        }
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
                default:
                    logger.warn("Unknown channel `{}` for command `{}`", id, command);
            }
        } catch (SalusApiException | AuthSalusApiException e) {
            logger.debug("Error while handling command `{}` on channel `{}`", command, channelUID, e);
            updateStatus(OFFLINE, COMMUNICATION_ERROR, e.getLocalizedMessage());
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
                .map(value -> value.divide(ONE_HUNDRED, new MathContext(5, HALF_EVEN))).map(DecimalType::new)
                .ifPresent(state -> {
                    updateState(channelUID, state);
                    updateStatus(ONLINE);
                });
    }

    private void handleCommandForExpectedTemperature(ChannelUID channelUID, Command command)
            throws SalusApiException, AuthSalusApiException {
        if (command instanceof RefreshType) {
            findLongProperty(channelPrefix + ":sIT600TH:HeatingSetpoint_x100", "HeatingSetpoint_x100")
                    .map(DeviceProperty.LongDeviceProperty::getValue).map(BigDecimal::new)
                    .map(value -> value.divide(ONE_HUNDRED, new MathContext(5, HALF_EVEN))).map(DecimalType::new)
                    .ifPresent(state -> {
                        updateState(channelUID, state);
                        updateStatus(ONLINE);
                    });
            return;
        }

        BigDecimal rawValue = null;
        if (command instanceof QuantityType<?> commandAsQuantityType) {
            rawValue = requireNonNull(commandAsQuantityType.toUnit(CELSIUS)).toBigDecimal();
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
                findLongProperty(channelPrefix + ":sIT600TH:HeatingSetpoint_x100", "HeatingSetpoint_x100")
                        .ifPresent(prop -> prop.setValue(value));
                findLongProperty(channelPrefix + ":sIT600TH:HoldType", "HoldType")
                        .ifPresent(prop -> prop.setValue((long) MANUAL));
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
                        case AUTO -> "AUTO";
                        case MANUAL -> "MANUAL";
                        case TEMPORARY_MANUAL -> "TEMPORARY_MANUAL";
                        case OFF -> "OFF";
                        default -> {
                            logger.warn("Unknown value {} for property HoldType!", value);
                            yield "AUTO";
                        }
                    }).map(StringType::new).ifPresent(state -> {
                        updateState(channelUID, state);
                        updateStatus(ONLINE);
                    });
            return;
        }

        if (command instanceof StringType typedCommand) {
            long value;
            if ("AUTO".equals(typedCommand.toString())) {
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
            cloudApi.setValueForProperty(dsn, property.get().getName(), value);
            updateStatus(ONLINE);
            return;
        }

        logger.debug("Does not know how to handle command `{}` ({}) on channel `{}`!", command,
                command.getClass().getSimpleName(), channelUID);
    }

    private Optional<DeviceProperty.LongDeviceProperty> findLongProperty(String name, String shortName)
            throws SalusApiException, AuthSalusApiException {
        var deviceProperties = findDeviceProperties();
        var property = deviceProperties.stream().filter(p -> p.getName().equals(name))
                .filter(DeviceProperty.LongDeviceProperty.class::isInstance)
                .map(DeviceProperty.LongDeviceProperty.class::cast).findAny();
        if (property.isEmpty()) {
            property = deviceProperties.stream().filter(p -> p.getName().contains(shortName))
                    .filter(DeviceProperty.LongDeviceProperty.class::isInstance)
                    .map(DeviceProperty.LongDeviceProperty.class::cast).findAny();
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
