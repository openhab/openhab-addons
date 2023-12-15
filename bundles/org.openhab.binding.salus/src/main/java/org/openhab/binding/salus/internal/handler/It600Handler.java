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
package org.openhab.binding.salus.internal.handler;

import static java.math.RoundingMode.HALF_EVEN;
import static org.openhab.binding.salus.internal.SalusBindingConstants.Channels.It600.*;
import static org.openhab.binding.salus.internal.SalusBindingConstants.It600Device.HoldType.*;
import static org.openhab.binding.salus.internal.SalusBindingConstants.SalusDevice.DSN;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatusDetail.*;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;

import org.apache.commons.lang3.StringUtils;
import org.openhab.binding.salus.internal.rest.DeviceProperty;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Grześlowski - Initial contribution
 */
public class It600Handler extends BaseThingHandler {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
    private static final Set<String> REQUIRED_CHANNELS = Set.of("ep_9:sIT600TH:LocalTemperature_x100",
            "ep_9:sIT600TH:HeatingSetpoint_x100", "ep_9:sIT600TH:SetHeatingSetpoint_x100", "ep_9:sIT600TH:HoldType",
            "ep_9:sIT600TH:SetHoldType");
    private final Logger logger;
    private String dsn;
    private CloudApi cloudApi;

    public It600Handler(Thing thing) {
        super(thing);
        logger = LoggerFactory.getLogger(It600Handler.class.getName() + "[" + thing.getUID().getId() + "]");
    }

    @Override
    public void initialize() {
        try {
            logger.debug("Initializing thing...");
            internalInitialize();
        } catch (Exception e) {
            logger.error("Error occurred while initializing Salus device!", e);
            updateStatus(OFFLINE, CONFIGURATION_ERROR,
                    "Error occurred while initializing Salus device! " + e.getLocalizedMessage());
        }
    }

    private void internalInitialize() {
        {
            var bridge = getBridge();
            if (bridge == null) {
                logger.debug("No bridge for thing with UID {}", thing.getUID());
                updateStatus(OFFLINE, BRIDGE_UNINITIALIZED,
                        "There is no bridge for this thing. Remove it and add it again.");
                return;
            }
            var bridgeHandler = bridge.getHandler();
            if (!(bridgeHandler instanceof CloudBridgeHandler cloudHandler)) {
                var bridgeHandlerClassName = Optional.ofNullable(bridgeHandler).map(BridgeHandler::getClass)
                        .map(Class::getSimpleName).orElse("null");
                logger.debug("Bridge is not instance of {}! Current bridge class {}, Thing UID {}",
                        CloudBridgeHandler.class.getSimpleName(), bridgeHandlerClassName, thing.getUID());
                updateStatus(OFFLINE, BRIDGE_UNINITIALIZED, "There is wrong type of bridge for cloud device!");
                return;
            }
            this.cloudApi = cloudHandler;
        }

        dsn = (String) getConfig().get(DSN);

        if (StringUtils.isEmpty(dsn)) {
            logger.debug("No {} for thing with UID {}", DSN, thing.getUID());
            updateStatus(OFFLINE, CONFIGURATION_ERROR,
                    "There is no " + DSN + " for this thing. Remove it and add it again.");
            return;
        }

        try {
            var device = this.cloudApi.findDevice(dsn);
            // no device in cloud
            if (device.isEmpty()) {
                var msg = "Device with DSN " + dsn + " not found!";
                logger.error(msg);
                updateStatus(OFFLINE, COMMUNICATION_ERROR, msg);
                return;
            }
            // device is not connected
            if (!device.get().isConnected()) {
                var msg = "Device with DSN " + dsn + " is not connected!";
                logger.error(msg);
                updateStatus(OFFLINE, COMMUNICATION_ERROR, msg);
                return;
            }
            // device is missing properties
            var deviceProperties = findDeviceProperties().stream().map(DeviceProperty::getName).toList();
            var result = new ArrayList<>(REQUIRED_CHANNELS);
            result.removeAll(deviceProperties);
            if (result.size() > 0) {
                var msg = "Device with DSN " + dsn + " is missing required properties: " + result;
                logger.error(msg);
                updateStatus(OFFLINE, CONFIGURATION_ERROR, msg);
                return;
            }
        } catch (Exception e) {
            var msg = "Error when connecting to Salus Cloud!";
            logger.error(msg, e);
            updateStatus(OFFLINE, COMMUNICATION_ERROR, msg);
            return;
        }

        // done
        updateStatus(ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Accepting command `{}` for channel `{}`", command, channelUID.getId());
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
    }

    private void handleCommandForTemperature(ChannelUID channelUID, Command command) {
        if (!(command instanceof RefreshType)) {
            // only refresh commands are supported for temp channel
            return;
        }

        findLongProperty("ep_9:sIT600TH:LocalTemperature_x100", "LocalTemperature_x100")
                .map(DeviceProperty.LongDeviceProperty::getValue).map(BigDecimal::new)
                .map(value -> value.divide(ONE_HUNDRED, new MathContext(5, HALF_EVEN))).map(DecimalType::new)
                .ifPresent(state -> updateState(channelUID, state));
    }

    private void handleCommandForExpectedTemperature(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            findLongProperty("ep_9:sIT600TH:HeatingSetpoint_x100", "HeatingSetpoint_x100")
                    .map(DeviceProperty.LongDeviceProperty::getValue).map(BigDecimal::new)
                    .map(value -> value.divide(ONE_HUNDRED, new MathContext(5, HALF_EVEN))).map(DecimalType::new)
                    .ifPresent(state -> updateState(channelUID, state));
            return;
        }

        if (command instanceof DecimalType || command instanceof QuantityType) {
            BigDecimal rawValue;
            if (command instanceof DecimalType typedCommand) {
                rawValue = typedCommand.toBigDecimal();
            } else {
                var typedCommand = (QuantityType<?>) command;
                rawValue = typedCommand.toBigDecimal();
            }

            var value = rawValue.multiply(ONE_HUNDRED).longValue();
            var property = findLongProperty("ep_9:sIT600TH:SetHeatingSetpoint_x100", "SetHeatingSetpoint_x100");
            if (property.isEmpty()) {
                return;
            }
            cloudApi.setValueForProperty(dsn, property.get().getName(), value);
            handleCommand(channelUID, REFRESH);
            return;
        }

        logger.debug("Does not know how to handle command `{}` ({}) on channel `{}`!", command,
                command.getClass().getSimpleName(), channelUID);
    }

    private void handleCommandForWorkType(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            findLongProperty("ep_9:sIT600TH:HoldType", "HoldType").map(DeviceProperty.LongDeviceProperty::getValue)
                    .map(value -> switch (value.intValue()) {
                        case AUTO -> "AUTO";
                        case MANUAL -> "MANUAL";
                        case TEMPORARY_MANUAL -> "TEMPORARY_MANUAL";
                        case OFF -> "OFF";
                        default -> {
                            logger.warn("Unknown value {} for property HoldType!", value);
                            yield "AUTO";
                        }
                    }).map(StringType::new).ifPresent(state -> updateState(channelUID, state));
            return;
        }

        if (command instanceof StringType typedCommand) {
            long value;
            if (typedCommand.toString().equals("AUTO")) {
                value = AUTO;
            } else if (typedCommand.toString().equals("MANUAL")) {
                value = MANUAL;
            } else if (typedCommand.toString().equals("TEMPORARY_MANUAL")) {
                value = TEMPORARY_MANUAL;
            } else if (typedCommand.toString().equals("OFF")) {
                value = OFF;
            } else {
                logger.warn("Unknown value `{}` for property HoldType!", typedCommand);
                return;
            }
            var property = findLongProperty("ep_9:sIT600TH:SetHoldType", "SetHoldType");
            if (property.isEmpty()) {
                return;
            }
            cloudApi.setValueForProperty(dsn, property.get().getName(), value);
            handleCommand(channelUID, REFRESH);
            return;
        }

        logger.debug("Does not know how to handle command `{}` ({}) on channel `{}`!", command,
                command.getClass().getSimpleName(), channelUID);
    }

    private Optional<DeviceProperty.LongDeviceProperty> findLongProperty(String name, String shortName) {
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
            logger.warn("{} property not found!", shortName);
        }
        return property;
    }

    private SortedSet<DeviceProperty<?>> findDeviceProperties() {
        return this.cloudApi.findPropertiesForDevice(dsn);
    }
}
