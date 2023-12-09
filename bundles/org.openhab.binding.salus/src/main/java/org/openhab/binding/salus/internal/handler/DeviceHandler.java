package org.openhab.binding.salus.internal.handler;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.salus.internal.rest.DeviceProperty;
import org.openhab.core.library.types.*;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import static java.math.RoundingMode.HALF_EVEN;
import static java.util.Collections.emptySortedSet;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.openhab.binding.salus.internal.SalusBindingConstants.BINDING_ID;
import static org.openhab.binding.salus.internal.SalusBindingConstants.Channels.*;
import static org.openhab.binding.salus.internal.SalusBindingConstants.SalusDevice.DSN;
import static org.openhab.binding.salus.internal.SalusBindingConstants.SalusDevice.PROPERTY_CACHE;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatusDetail.*;

public class DeviceHandler extends BaseThingHandler {
    public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);
    private final Logger logger;
    private String dsn;
    private CloudBridgeHandler bridge;
    private final Map<String, String> channelUidMap = new HashMap<>();
    private final Map<String, String> channelX100UidMap = new HashMap<>();

    private final ReentrantLock devicePropertiesLock = new ReentrantLock();
    @NotNull
    private SortedSet<DeviceProperty<?>> deviceProperties = emptySortedSet();
    private long propertyCacheMilliSeconds;
    private long lastPropertyUpdateTime;

    public DeviceHandler(Thing thing) {
        super(thing);
        logger = LoggerFactory.getLogger(DeviceHandler.class.getName() + "[" + thing.getUID().getId() + "]");
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
            var channels = findDeviceProperties()
                    .stream()
                    .map(this::buildChannel)
                    .toList();
            if (channels.isEmpty()) {
                updateStatus(
                        OFFLINE,
                        CONFIGURATION_ERROR,
                        "There are no channels for " + dsn + ".");
                return;
            }
            updateChannels(channels);
        } catch (Exception e) {
            logger.error("Error when loading IO device from Salus Cloud!", e);
            updateStatus(OFFLINE, COMMUNICATION_ERROR, "Error when loading IO device from Salus Cloud!");
            return;
        }

        // done
        updateStatus(ONLINE);
    }

    private Channel buildChannel(DeviceProperty<?> property) {
        String channelId;
        String acceptedItemType;
        if (property instanceof DeviceProperty.BooleanDeviceProperty booleanProperty) {
            channelId = inOrOut(property.getDirection(), GENERIC_INPUT_BOOL_CHANNEL, GENERIC_OUTPUT_BOOL_CHANNEL);
            acceptedItemType = "Switch";
        } else if (property instanceof DeviceProperty.LongDeviceProperty longDeviceProperty) {
            if (TEMPERATURE_CHANNELS.contains(longDeviceProperty.getName())) {
                // a temp channel
                channelId = inOrOut(property.getDirection(), TEMPERATURE_INPUT_NUMBER_CHANNEL, TEMPERATURE_OUTPUT_NUMBER_CHANNEL);
            } else {
                channelId = inOrOut(property.getDirection(), GENERIC_INPUT_NUMBER_CHANNEL, GENERIC_OUTPUT_NUMBER_CHANNEL);
            }
            acceptedItemType = "Number";
        } else if (property instanceof DeviceProperty.StringDeviceProperty stringDeviceProperty) {
            channelId = inOrOut(property.getDirection(), GENERIC_INPUT_CHANNEL, GENERIC_OUTPUT_CHANNEL);
            acceptedItemType = "String";
        } else {
            throw new UnsupportedOperationException("Property class " + property.getClass().getSimpleName() + " is not supported!");
        }

        var channelUid = new ChannelUID(thing.getUID(), buildChannelUid(property.getName()));
        var channelTypeUID = new ChannelTypeUID(BINDING_ID, channelId);
        return ChannelBuilder
                .create(channelUid, acceptedItemType)
                .withType(channelTypeUID)
                .withLabel(buildChannelDisplayName(property.getDisplayName()))
                .build();
    }

    private String buildChannelUid(final String name) {
        String uid = name;
        var map = channelUidMap;
        if (name.contains("x100")) {
            map = channelX100UidMap;
            uid = removeX100(uid);
        }
        uid = uid.replaceAll("[^[\\w-]*]", "_");
        final var firstUid = uid;
        var idx = 1;
        while (map.containsKey(uid)) {
            uid = firstUid + "_" + idx++;
        }
        map.put(uid, name);
        return uid;
    }

    private String buildChannelDisplayName(final String displayName) {
        if (displayName.contains("x100")) {
            return removeX100(displayName);
        }
        return displayName;
    }

    private static String removeX100(String name) {
        var withoutSuffix = name.replaceAll("_x100", "").replaceAll("x100", "");
        if (withoutSuffix.endsWith("_")) {
            withoutSuffix = withoutSuffix.substring(0, withoutSuffix.length() - 2);
        }
        return withoutSuffix;
    }


    private String inOrOut(String direction, String in, String out) {
        if ("output".equalsIgnoreCase(direction)) {
            return out;
        }
        if ("input".equalsIgnoreCase(direction)) {
            return in;
        }

        logger.warn("Direction [{}] is unknown!", direction);
        return out;
    }

    private void updateChannels(final List<Channel> channels) {
        var thingBuilder = editThing();
        thingBuilder.withChannels(channels);
        updateThing(thingBuilder.build());
    }

    @Override
    public void handleCommand(@NonNullByDefault ChannelUID channelUID, @NonNullByDefault Command command) {
        logger.debug("Accepting command {} for channel {}", command, channelUID.getId());
        try {
            if (command instanceof RefreshType) {
                handleRefreshCommand(channelUID);
            } else if (command instanceof OnOffType typedCommand) {
                handleBoolCommand(channelUID, typedCommand == OnOffType.ON);
            } else if (command instanceof UpDownType typedCommand) {
                handleBoolCommand(channelUID, typedCommand == UpDownType.UP);
            } else if (command instanceof OpenClosedType typedCommand) {
                handleBoolCommand(channelUID, typedCommand == OpenClosedType.OPEN);
            } else if (command instanceof PercentType typedCommand) {
                handleDecimalCommand(channelUID, typedCommand.as(DecimalType.class));
            } else if (command instanceof DecimalType typedCommand) {
                handleDecimalCommand(channelUID, typedCommand);
            } else if (command instanceof StringType typedCommand) {
                handleStringCommand(channelUID, typedCommand);
            } else {
                logger.warn("Does not know how to handle command `{}` ({}) on channel `{}`!",
                        command, command.getClass().getSimpleName(), channelUID);
            }
        } catch (Exception ex) {
            logger.error("Error occurred while handling command `{}` ({}) on channel `{}`!",
                    command, command.getClass().getSimpleName(), channelUID.getId(), ex);
        }
    }

    private void handleRefreshCommand(ChannelUID channelUID) {
        var id = channelUID.getId();
        String salusId;
        boolean isX100;
        if (channelUidMap.containsKey(id)) {
            salusId = channelUidMap.get(id);
            isX100 = false;
        } else if (channelX100UidMap.containsKey(id)) {
            salusId = channelX100UidMap.get(id);
            isX100 = true;
        } else {
            logger.warn("Channel {} not found in channelUidMap and channelX100UidMap!", id);
            return;
        }

        var propertyOptional = findDeviceProperties()
                .stream()
                .filter(property -> property.getName().equals(salusId))
                .findFirst();
        if (propertyOptional.isEmpty()) {
            logger.warn("Property {} not found in response!", salusId);
            return;
        }
        var property = propertyOptional.get();
        State state;
        if (property instanceof DeviceProperty.BooleanDeviceProperty booleanProperty) {
            state = booleanProperty.getValue() ? OnOffType.ON : OnOffType.OFF;
        } else if (property instanceof DeviceProperty.LongDeviceProperty longDeviceProperty) {
            if (isX100) {
                state = new DecimalType(new BigDecimal(longDeviceProperty.getValue()).divide(ONE_HUNDRED, new MathContext(5, HALF_EVEN)));
            } else {
                state = new DecimalType(longDeviceProperty.getValue());
            }
        } else if (property instanceof DeviceProperty.StringDeviceProperty stringDeviceProperty) {
            state = new StringType(stringDeviceProperty.getValue());
        } else {
            logger.warn("Property class {} is not supported!", property.getClass().getSimpleName());
            return;
        }
        logger.debug("Setting value {}{} for channel {}", state, isX100 ? " (x100)" : "", channelUID);
        updateState(channelUID, state);
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

    private void handleBoolCommand(ChannelUID channelUID, boolean command) {
        var id = channelUID.getId();
        String salusId;
        if (channelUidMap.containsKey(id)) {
            salusId = channelUidMap.get(id);
        } else {
            logger.warn("Channel {} not found in channelUidMap!", id);
            return;
        }
        var result = bridge.setValueForProperty(dsn, salusId, command);
        if(result.isPresent()) {
            devicePropertiesLock.lock();
            try {
                deviceProperties.stream()
                        .filter(property -> property.getName().equals(salusId))
                        .filter(DeviceProperty.BooleanDeviceProperty.class::isInstance)
                        .map(DeviceProperty.BooleanDeviceProperty.class::cast)
                        .findFirst()
                        .ifPresent(property -> property.setValue(command));
            } finally {
                devicePropertiesLock.unlock();
            }
        }
    }


    private void handleDecimalCommand(ChannelUID channelUID, DecimalType command) {
        var id = channelUID.getId();
        String salusId;
        long value;
        if (channelUidMap.containsKey(id)) {
            salusId = channelUidMap.get(id);
            value = command.toBigDecimal().longValue();
        } else if (channelX100UidMap.containsKey(id)) {
            salusId = channelX100UidMap.get(id);
            value = command.toBigDecimal().multiply(ONE_HUNDRED).longValue();
        } else {
            logger.warn("Channel {} not found in channelUidMap and channelX100UidMap!", id);
            return;
        }
        var result = bridge.setValueForProperty(dsn, salusId, value);
        if(result.isPresent()) {
            devicePropertiesLock.lock();
            try {
                deviceProperties.stream()
                        .filter(property -> property.getName().equals(salusId))
                        .filter(DeviceProperty.LongDeviceProperty.class::isInstance)
                        .map(DeviceProperty.LongDeviceProperty.class::cast)
                        .findFirst()
                        .ifPresent(property -> property.setValue(value));
            } finally {
                devicePropertiesLock.unlock();
            }
        }
    }

    private void handleStringCommand(ChannelUID channelUID, StringType command) {
        var id = channelUID.getId();
        String salusId;
        if (channelUidMap.containsKey(id)) {
            salusId = channelUidMap.get(id);
        } else {
            logger.warn("Channel {} not found in channelUidMap!", id);
            return;
        }
        var value = command.toFullString();
        var result = bridge.setValueForProperty(dsn, salusId, value);
        if(result.isPresent()) {
            devicePropertiesLock.lock();
            try {
                deviceProperties.stream()
                        .filter(property -> property.getName().equals(salusId))
                        .filter(DeviceProperty.StringDeviceProperty.class::isInstance)
                        .map(DeviceProperty.StringDeviceProperty.class::cast)
                        .findFirst()
                        .ifPresent(property -> property.setValue(value));
            } finally {
                devicePropertiesLock.unlock();
            }
        }
    }

}
