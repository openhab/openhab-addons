package org.openhab.binding.salus.internal.handler;

import org.apache.commons.lang3.StringUtils;
import org.openhab.binding.salus.internal.rest.DeviceProperty;
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

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Optional;
import java.util.SortedSet;
import java.util.concurrent.locks.ReentrantLock;

import static java.math.RoundingMode.HALF_EVEN;
import static java.util.Collections.emptySortedSet;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openhab.binding.salus.internal.SalusBindingConstants.Channels.It600.*;
import static org.openhab.binding.salus.internal.SalusBindingConstants.SalusDevice.DSN;
import static org.openhab.binding.salus.internal.SalusBindingConstants.SalusDevice.PROPERTY_CACHE;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatusDetail.*;

public class It600Handler extends BaseThingHandler {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
    private final Logger logger;
    private String dsn;
    private CloudBridgeHandler bridge;

    private final ReentrantLock devicePropertiesLock = new ReentrantLock();
    @NotNull
    private SortedSet<DeviceProperty<?>> deviceProperties = emptySortedSet();
    private long propertyCacheMilliSeconds;
    private long lastPropertyUpdateTime;

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
        var bridge = getBridge();
        if (bridge == null) {
            logger.debug("No bridge for thing with UID {}", thing.getUID());
            updateStatus(
                    OFFLINE,
                    BRIDGE_UNINITIALIZED,
                    "There is no bridge for this thing. Remove it and add it again.");
            return;
        }
        var bridgeHandler = bridge.getHandler();
        if (!(bridgeHandler instanceof CloudBridgeHandler cloudHandler)) {
            logger.debug("Bridge is not instance of {}! Current bridge class {}, Thing UID {}",
                    CloudBridgeHandler.class.getSimpleName(), CloudBridgeHandler.class.getSimpleName(), thing.getUID());
            updateStatus(OFFLINE, BRIDGE_UNINITIALIZED, "There is wrong type of bridge for cloud device!");
            return;
        }
        this.bridge = cloudHandler;

        {
            dsn = (String) getConfig().get(DSN);
            propertyCacheMilliSeconds = ((BigDecimal) getConfig().get(PROPERTY_CACHE)).longValue();
            if (propertyCacheMilliSeconds <= 0) {
                propertyCacheMilliSeconds = 5;
            }
            propertyCacheMilliSeconds = SECONDS.toMillis(propertyCacheMilliSeconds);
        }

        if (StringUtils.isEmpty(dsn)) {
            logger.debug("No {} for thing with UID {}", DSN, thing.getUID());
            updateStatus(
                    OFFLINE,
                    CONFIGURATION_ERROR,
                    "There is no " + DSN + " for this thing. Remove it and add it again.");
            return;
        }

        try {
            var device = this.bridge.findDevice(dsn);
            if (device.isEmpty()) {
                var msg = "Device with DSN " + dsn + " not found!";
                logger.error(msg);
                updateStatus(OFFLINE, COMMUNICATION_ERROR, msg);
                return;
            }
            if (!device.get().isConnected()) {
                var msg = "Device with DSN " + dsn + " is not connected!";
                logger.error(msg);
                updateStatus(OFFLINE, COMMUNICATION_ERROR, msg);
                return;
            }
            findDeviceProperties();
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
                .map(DeviceProperty.LongDeviceProperty::getValue)
                .map(BigDecimal::new)
                .map(value -> value.divide(ONE_HUNDRED, new MathContext(5, HALF_EVEN)))
                .map(DecimalType::new)
                .ifPresent(state -> updateState(channelUID, state));
    }

    private void handleCommandForExpectedTemperature(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            findLongProperty("ep_9:sIT600TH:HeatingSetpoint_x100", "HeatingSetpoint_x100")
                    .map(DeviceProperty.LongDeviceProperty::getValue)
                    .map(BigDecimal::new)
                    .map(value -> value.divide(ONE_HUNDRED, new MathContext(5, HALF_EVEN)))
                    .map(DecimalType::new)
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
            var result = bridge.setValueForProperty(dsn, property.get().getName(), value);
            if (result.isPresent()) {
                devicePropertiesLock.lock();
                try {
                    findLongProperty("ep_9:sIT600TH:HeatingSetpoint_x100", "HeatingSetpoint_x100")
                            .ifPresent(p -> p.setValue(value));
                } finally {
                    devicePropertiesLock.unlock();
                }
            }
            return;
        }

        logger.debug("Does not know how to handle command `{}` ({}) on channel `{}`!",
                command, command.getClass().getSimpleName(), channelUID);
    }

    private void handleCommandForWorkType(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            findLongProperty("ep_9:sIT600TH:HoldType", "HoldType")
                    .map(DeviceProperty.LongDeviceProperty::getValue)
                    .map(value -> switch (value.intValue()) {
                        case 0 -> "AUTO";
                        case 2 -> "MANUAL";
                        case 1 -> "TEMPORARY_MANUAL";
                        case 7 -> "OFF";
                        default -> {
                            logger.warn("Unknown value {} for property HoldType!", value);
                            yield "AUTO";
                        }
                    })
                    .map(StringType::new)
                    .ifPresent(state -> updateState(channelUID, state));
            return;
        }

        if (command instanceof StringType typedCommand) {
            long value;
            if (typedCommand.toString().equals("AUTO")) {
                value = 0;
            } else if (typedCommand.toString().equals("MANUAL")) {
                value = 2;
            } else if (typedCommand.toString().equals("TEMPORARY_MANUAL")) {
                value = 1;
            } else if (typedCommand.toString().equals("OFF")) {
                value = 7;
            } else {
                logger.warn("Unknown value `{}` for property HoldType!", typedCommand);
                return;
            }
            var property = findLongProperty("ep_9:sIT600TH:SetHoldType", "SetHoldType");
            if (property.isEmpty()) {
                return;
            }
            var result = bridge.setValueForProperty(dsn, property.get().getName(), value);
            if (result.isPresent()) {
                devicePropertiesLock.lock();
                try {
                    findLongProperty("ep_9:sIT600TH:HoldType", "HoldType")
                            .ifPresent(p -> p.setValue(value));
                } finally {
                    devicePropertiesLock.unlock();
                }
            }
            return;
        }

        logger.debug("Does not know how to handle command `{}` ({}) on channel `{}`!",
                command, command.getClass().getSimpleName(), channelUID);
    }

    private Optional<DeviceProperty.LongDeviceProperty> findLongProperty(String name, String shortName) {
        var deviceProperties = findDeviceProperties();
        var property = deviceProperties.stream()
                .filter(p -> p.getName().equals(name))
                .filter(DeviceProperty.LongDeviceProperty.class::isInstance)
                .map(DeviceProperty.LongDeviceProperty.class::cast)
                .findAny();
        if (property.isEmpty()) {
            property = deviceProperties.stream()
                    .filter(p -> p.getName().contains(shortName))
                    .filter(DeviceProperty.LongDeviceProperty.class::isInstance)
                    .map(DeviceProperty.LongDeviceProperty.class::cast)
                    .findAny();
        }
        if (property.isEmpty()) {
            logger.warn("{} property not found!", shortName);
        }
        return property;
    }

    private SortedSet<DeviceProperty<?>> findDeviceProperties() {
        devicePropertiesLock.lock();
        try {
            var currentTimeMillis = System.currentTimeMillis();
            if (!deviceProperties.isEmpty() && currentTimeMillis < lastPropertyUpdateTime + propertyCacheMilliSeconds) {
                logger.trace("Using cached device properties. Next update in {} ms",
                        (lastPropertyUpdateTime + propertyCacheMilliSeconds) - currentTimeMillis);
                return deviceProperties;
            }
            logger.trace("Getting all properties and putting them in a cache");
            {
                var response = this.bridge.findPropertiesForDevice(dsn);
                if (!response.isEmpty()) {
                    this.deviceProperties = response;
                    lastPropertyUpdateTime = currentTimeMillis;
                } else {
                    logger.trace("Response was empty. No properties came from the server");
                }
            }
            return deviceProperties;
        } finally {
            devicePropertiesLock.unlock();
        }
    }
}
