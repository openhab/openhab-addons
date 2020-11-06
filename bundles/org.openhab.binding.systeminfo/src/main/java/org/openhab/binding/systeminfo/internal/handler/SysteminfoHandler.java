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
package org.openhab.binding.systeminfo.internal.handler;

import static org.openhab.binding.systeminfo.internal.SysteminfoBindingConstants.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.systeminfo.internal.model.SysteminfoInterface;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.SmartHomeUnits;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SysteminfoHandler} is responsible for providing real time information about the system
 * (CPU, Memory, Storage, Display and others).
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Lyubomir Papzov - Separate the creation of the systeminfo object and its initialization
 * @author Wouter Born - Add null annotations
 * @author Alexander Falkenstern - Process information
 */
@NonNullByDefault
public class SysteminfoHandler extends BaseBridgeHandler {
    /**
     * Refresh interval for {@link #highPriorityChannels} in seconds.
     */
    private BigDecimal refreshIntervalHighPriority = BigDecimal.ONE;

    /**
     * Refresh interval for {@link #mediumPriorityChannels} in seconds.
     */
    private BigDecimal refreshIntervalMediumPriority = new BigDecimal(60);

    /**
     * Map containing channels grouped by update frequency
     * Channels with {@link #HIGH_PRIOIRITY} are updated periodically at
     * {@link #refreshIntervalHighPriority}, {@link #MEDIUM_PRIOIRITY} periodically at
     * {@link #refreshIntervalMediumPriority}. {@link #LOW_PRIOIRITY}are updated at
     * {@link #initialize()} only.
     */
    private final Map<String, @Nullable Set<ChannelUID>> channelGroups = new HashMap<>();

    /**
     * Wait time for the creation of Item-Channel links in seconds. This delay is needed, because the Item-Channel
     * links have to be created before the thing state is updated, otherwise item state will not be updated.
     */
    public static final int WAIT_TIME_CHANNEL_ITEM_LINK_INIT = 1;

    private @NonNullByDefault({}) SysteminfoInterface systeminfo;

    private @Nullable ScheduledFuture<?> highPriorityTasks;
    private @Nullable ScheduledFuture<?> mediumPriorityTasks;

    private final Logger logger = LoggerFactory.getLogger(SysteminfoHandler.class);

    public SysteminfoHandler(Bridge bridge, @Nullable SysteminfoInterface systeminfo) {
        super(bridge);

        Objects.requireNonNull(systeminfo, "Systeminfo may not be null");
        this.systeminfo = systeminfo;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command {} on channel {}", command, channelUID);

        Bridge bridge = getThing();
        if (ThingStatus.ONLINE != bridge.getStatus()) {
            logger.debug("Cannot handle command. Thing is not ONLINE.");
            return;
        }

        if (!(command instanceof RefreshType)) {
            logger.debug("Unsupported command {}. Supported commands: REFRESH.", command);
            return;
        }

        Thing child = bridge.getThing(channelUID.getThingUID());
        if (child != null) {
            ThingHandler handler = child.getHandler();
            if (handler != null) {
                handler.handleCommand(channelUID, command);
            }
        } else {
            int deviceIndex = getDeviceIndex(channelUID);
            try {
                if (BATTERY_GROUP_ID.equalsIgnoreCase(channelUID.getGroupId())) {
                    switch (channelUID.getIdWithoutGroup()) {
                        case CHANNEL_NAME: {
                            String state = systeminfo.getBatteryName(deviceIndex);
                            updateState(channelUID, new StringType(state));
                            break;
                        }
                        case CHANNEL_BATTERY_REMAINING_CAPACITY: {
                            BigDecimal state = systeminfo.getBatteryRemainingCapacity(deviceIndex);
                            updateState(channelUID, new QuantityType<>(state, SmartHomeUnits.PERCENT));
                            break;
                        }
                        case CHANNEL_BATTERY_REMAINING_TIME: {
                            BigDecimal state = systeminfo.getBatteryRemainingTime(deviceIndex);
                            updateState(channelUID,
                                    state != null ? new QuantityType<>(state, SmartHomeUnits.SECOND) : UnDefType.UNDEF);
                            break;
                        }
                        default: {
                            logger.debug("Channel with unknown ID: {}.", channelUID);
                            break;
                        }
                    }
                } else if (CPU_GROUP_ID.equalsIgnoreCase(channelUID.getGroupId())) {
                    switch (channelUID.getIdWithoutGroup()) {
                        case CHANNEL_DESCRIPTION: {
                            String state = systeminfo.getCpuDescription();
                            updateState(channelUID, new StringType(state));
                            break;
                        }
                        case CHANNEL_NAME: {
                            String state = systeminfo.getCpuName();
                            updateState(channelUID, new StringType(state));
                            break;
                        }
                        case CHANNEL_THREADS: {
                            BigDecimal state = systeminfo.getCpuThreads();
                            updateState(channelUID, new DecimalType(state));
                            break;
                        }
                        case CHANNEL_CPU_LOAD: {
                            BigDecimal state = systeminfo.getCpuLoad();
                            updateState(channelUID, state != null ? new QuantityType<>(state, SmartHomeUnits.PERCENT)
                                    : UnDefType.UNDEF);
                            break;
                        }
                        case CHANNEL_CPU_LOAD_1: {
                            BigDecimal state = systeminfo.getCpuLoad1();
                            updateState(channelUID, state != null ? new QuantityType<>(state, SmartHomeUnits.PERCENT)
                                    : UnDefType.UNDEF);
                            break;
                        }
                        case CHANNEL_CPU_LOAD_5: {
                            BigDecimal state = systeminfo.getCpuLoad5();
                            updateState(channelUID, state != null ? new QuantityType<>(state, SmartHomeUnits.PERCENT)
                                    : UnDefType.UNDEF);
                            break;
                        }
                        case CHANNEL_CPU_LOAD_15: {
                            BigDecimal state = systeminfo.getCpuLoad15();
                            updateState(channelUID, state != null ? new QuantityType<>(state, SmartHomeUnits.PERCENT)
                                    : UnDefType.UNDEF);
                            break;
                        }
                        case CHANNEL_CPU_TEMPERATURE: {
                            BigDecimal state = systeminfo.getSensorsCpuTemperature();
                            updateState(channelUID,
                                    state != null ? new QuantityType<>(state, SIUnits.CELSIUS) : UnDefType.UNDEF);
                            break;
                        }
                        case CHANNEL_CPU_VOLTAGE: {
                            BigDecimal state = systeminfo.getSensorsCpuVoltage();
                            updateState(channelUID,
                                    state != null ? new QuantityType<>(state, SmartHomeUnits.VOLT) : UnDefType.UNDEF);
                            break;
                        }
                        case CHANNEL_CPU_UPTIME: {
                            BigDecimal state = systeminfo.getCpuUptime();
                            updateState(channelUID, new QuantityType<>(state, SmartHomeUnits.SECOND));
                            break;
                        }
                        default: {
                            logger.debug("Channel with unknown ID: {}.", channelUID);
                            break;
                        }
                    }
                } else if (DISPLAY_GROUP_ID.equalsIgnoreCase(channelUID.getGroupId())) {
                    switch (channelUID.getIdWithoutGroup()) {
                        case CHANNEL_DISPLAY_INFORMATION: {
                            String state = systeminfo.getDisplayInformation(deviceIndex);
                            updateState(channelUID, new StringType(state));
                            break;
                        }
                        default: {
                            logger.debug("Channel with unknown ID: {}.", channelUID);
                            break;
                        }
                    }
                } else if (DRIVE_GROUP_ID.equalsIgnoreCase(channelUID.getGroupId())) {
                    switch (channelUID.getIdWithoutGroup()) {
                        case CHANNEL_DRIVE_MODEL: {
                            String state = systeminfo.getDriveModel(deviceIndex);
                            updateState(channelUID, new StringType(state));
                            break;
                        }
                        case CHANNEL_DRIVE_SERIAL: {
                            String state = systeminfo.getDriveSerialNumber(deviceIndex);
                            updateState(channelUID, new StringType(state));
                            break;
                        }
                        case CHANNEL_NAME: {
                            String state = systeminfo.getDriveName(deviceIndex);
                            updateState(channelUID, new StringType(state));
                            break;
                        }
                        default: {
                            logger.debug("Channel with unknown ID: {}.", channelUID);
                            break;
                        }
                    }
                } else if (FANS_GROUP_ID.equalsIgnoreCase(channelUID.getGroupId())) {
                    switch (channelUID.getIdWithoutGroup()) {
                        case CHANNEL_FAN_SPEED: {
                            BigDecimal state = systeminfo.getSensorsFanSpeed(deviceIndex);
                            updateState(channelUID, state != null ? new DecimalType(state) : UnDefType.UNDEF);
                            break;
                        }
                        default: {
                            logger.debug("Channel with unknown ID: {}.", channelUID);
                            break;
                        }
                    }
                } else if (MEMORY_GROUP_ID.equalsIgnoreCase(channelUID.getGroupId())) {
                    switch (channelUID.getIdWithoutGroup()) {
                        case CHANNEL_AVAILABLE: {
                            BigDecimal state = systeminfo.getMemoryAvailable();
                            updateState(channelUID,
                                    state != null ? new QuantityType<>(state, SmartHomeUnits.BYTE) : UnDefType.UNDEF);
                            break;
                        }
                        case CHANNEL_TOTAL: {
                            BigDecimal state = systeminfo.getMemoryTotal();
                            updateState(channelUID,
                                    state != null ? new QuantityType<>(state, SmartHomeUnits.BYTE) : UnDefType.UNDEF);
                            break;
                        }
                        case CHANNEL_USED: {
                            BigDecimal state = systeminfo.getMemoryUsed();
                            updateState(channelUID,
                                    state != null ? new QuantityType<>(state, SmartHomeUnits.BYTE) : UnDefType.UNDEF);
                            break;
                        }
                        case CHANNEL_AVAILABLE_PERCENT: {
                            BigDecimal state = systeminfo.getMemoryAvailablePercent();
                            updateState(channelUID, state != null ? new QuantityType<>(state, SmartHomeUnits.PERCENT)
                                    : UnDefType.UNDEF);
                            break;
                        }
                        case CHANNEL_USED_PERCENT: {
                            BigDecimal state = systeminfo.getMemoryUsedPercent();
                            updateState(channelUID, state != null ? new QuantityType<>(state, SmartHomeUnits.PERCENT)
                                    : UnDefType.UNDEF);
                            break;
                        }
                        default: {
                            logger.debug("Channel with unknown ID: {}.", channelUID);
                            break;
                        }
                    }
                } else if (NETWORK_GROUP_ID.equalsIgnoreCase(channelUID.getGroupId())) {
                    switch (channelUID.getIdWithoutGroup()) {
                        case CHANNEL_NAME: {
                            String state = systeminfo.getNetworkName(deviceIndex);
                            updateState(channelUID, new StringType(state));
                            break;
                        }
                        case CHANNEL_NETWORK_IP: {
                            String state = systeminfo.getNetworkIp(deviceIndex);
                            updateState(channelUID, new StringType(state));
                            break;
                        }
                        case CHANNEL_NETWORK_INTERFACE: {
                            String state = systeminfo.getNetworkDisplayName(deviceIndex);
                            updateState(channelUID, new StringType(state));
                            break;
                        }
                        case CHANNEL_NETWORK_MAC: {
                            String state = systeminfo.getNetworkMac(deviceIndex);
                            updateState(channelUID, new StringType(state));
                            break;
                        }
                        case CHANNEL_NETWORK_DATA_SENT: {
                            BigDecimal state = systeminfo.getNetworkDataSent(deviceIndex);
                            updateState(channelUID, new QuantityType<>(state, SmartHomeUnits.BYTE));
                            break;
                        }
                        case CHANNEL_NETWORK_DATA_RECEIVED: {
                            BigDecimal state = systeminfo.getNetworkDataReceived(deviceIndex);
                            updateState(channelUID, new QuantityType<>(state, SmartHomeUnits.BYTE));
                            break;
                        }
                        case CHANNEL_NETWORK_PACKETS_RECEIVED: {
                            BigDecimal state = systeminfo.getNetworkPacketsReceived(deviceIndex);
                            updateState(channelUID, new DecimalType(state));
                            break;
                        }
                        case CHANNEL_NETWORK_PACKETS_SENT: {
                            BigDecimal state = systeminfo.getNetworkPacketsSent(deviceIndex);
                            updateState(channelUID, new DecimalType(state));
                            break;
                        }
                        default: {
                            logger.debug("Channel with unknown ID: {}.", channelUID);
                            break;
                        }
                    }
                } else if (STORAGE_GROUP_ID.equalsIgnoreCase(channelUID.getGroupId())) {
                    switch (channelUID.getIdWithoutGroup()) {
                        case CHANNEL_AVAILABLE: {
                            BigDecimal state = systeminfo.getStorageAvailable(deviceIndex);
                            updateState(channelUID,
                                    state != null ? new QuantityType<>(state, SmartHomeUnits.BYTE) : UnDefType.UNDEF);
                            break;
                        }
                        case CHANNEL_DESCRIPTION: {
                            String state = systeminfo.getStorageDescription(deviceIndex);
                            updateState(channelUID, new StringType(state));
                            break;
                        }
                        case CHANNEL_NAME: {
                            String state = systeminfo.getStorageName(deviceIndex);
                            updateState(channelUID, new StringType(state));
                            break;
                        }
                        case CHANNEL_TOTAL: {
                            BigDecimal state = systeminfo.getStorageTotal(deviceIndex);
                            updateState(channelUID,
                                    state != null ? new QuantityType<>(state, SmartHomeUnits.BYTE) : UnDefType.UNDEF);
                            break;
                        }
                        case CHANNEL_USED: {
                            BigDecimal state = systeminfo.getStorageUsed(deviceIndex);
                            updateState(channelUID,
                                    state != null ? new QuantityType<>(state, SmartHomeUnits.BYTE) : UnDefType.UNDEF);
                            break;
                        }
                        case CHANNEL_AVAILABLE_PERCENT: {
                            BigDecimal state = systeminfo.getStorageAvailablePercent(deviceIndex);
                            updateState(channelUID, state != null ? new QuantityType<>(state, SmartHomeUnits.PERCENT)
                                    : UnDefType.UNDEF);
                            break;
                        }
                        case CHANNEL_USED_PERCENT: {
                            BigDecimal state = systeminfo.getStorageUsedPercent(deviceIndex);
                            updateState(channelUID, state != null ? new QuantityType<>(state, SmartHomeUnits.PERCENT)
                                    : UnDefType.UNDEF);
                            break;
                        }
                        case CHANNEL_STORAGE_TYPE: {
                            String state = systeminfo.getStorageType(deviceIndex);
                            updateState(channelUID, new StringType(state));
                            break;
                        }
                        default: {
                            logger.debug("Channel with unknown ID: {}.", channelUID);
                            break;
                        }
                    }
                } else if (SWAP_GROUP_ID.equalsIgnoreCase(channelUID.getGroupId())) {
                    switch (channelUID.getIdWithoutGroup()) {
                        case CHANNEL_AVAILABLE: {
                            BigDecimal state = systeminfo.getSwapAvailable();
                            updateState(channelUID,
                                    state != null ? new QuantityType<>(state, SmartHomeUnits.BYTE) : UnDefType.UNDEF);
                            break;
                        }
                        case CHANNEL_TOTAL: {
                            BigDecimal state = systeminfo.getSwapTotal();
                            updateState(channelUID,
                                    state != null ? new QuantityType<>(state, SmartHomeUnits.BYTE) : UnDefType.UNDEF);
                            break;
                        }
                        case CHANNEL_USED: {
                            BigDecimal state = systeminfo.getSwapUsed();
                            updateState(channelUID,
                                    state != null ? new QuantityType<>(state, SmartHomeUnits.BYTE) : UnDefType.UNDEF);
                            break;
                        }
                        case CHANNEL_AVAILABLE_PERCENT: {
                            BigDecimal state = systeminfo.getSwapAvailablePercent();
                            updateState(channelUID, state != null ? new QuantityType<>(state, SmartHomeUnits.PERCENT)
                                    : UnDefType.UNDEF);
                            break;
                        }
                        case CHANNEL_USED_PERCENT: {
                            BigDecimal state = systeminfo.getSwapUsedPercent();
                            updateState(channelUID, state != null ? new QuantityType<>(state, SmartHomeUnits.PERCENT)
                                    : UnDefType.UNDEF);
                            break;
                        }
                        default: {
                            logger.debug("Channel with unknown ID: {}.", channelUID);
                            break;
                        }
                    }
                } else {
                    logger.debug("Channel with unknown ID: {}.", channelUID);
                }
            } catch (IllegalArgumentException exception) {
                if (isLinked(channelUID)) {
                    logger.warn("No information for channel {} with device index {}.", channelUID, deviceIndex);
                }
                updateState(channelUID, UnDefType.UNDEF);
            } catch (Exception exception) {
                String message = exception.getMessage();
                logger.debug("Unexpected error occurred while getting system information {}.", message);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, message);
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing.");

        Bridge bridge = getThing();
        if (updateConfiguration(bridge) && updateProperties()) {
            for (Channel channel : bridge.getChannels()) {
                Configuration properties = channel.getConfiguration();
                changeChannelPriority(channel.getUID(), (String) properties.get(PARAMETER_PRIOIRITY));
            }

            if (highPriorityTasks == null) {
                logger.debug("Schedule high priority tasks at fixed rate {} s.", refreshIntervalHighPriority);
                highPriorityTasks = scheduler.scheduleWithFixedDelay(() -> {
                    Set<ChannelUID> group = null;
                    synchronized (channelGroups) {
                        group = channelGroups.get(HIGH_PRIOIRITY);
                    }
                    for (ChannelUID channel : group != null ? group : Collections.<ChannelUID> emptySet()) {
                        handleCommand(channel, RefreshType.REFRESH);
                    }
                }, WAIT_TIME_CHANNEL_ITEM_LINK_INIT, refreshIntervalHighPriority.intValue(), TimeUnit.SECONDS);
            }

            if (mediumPriorityTasks == null) {
                logger.debug("Schedule medium priority tasks at fixed rate {} s.", refreshIntervalMediumPriority);
                mediumPriorityTasks = scheduler.scheduleWithFixedDelay(() -> {
                    Set<ChannelUID> group = null;
                    synchronized (channelGroups) {
                        group = channelGroups.get(MEDIUM_PRIOIRITY);
                    }
                    for (ChannelUID channel : group != null ? group : Collections.<ChannelUID> emptySet()) {
                        handleCommand(channel, RefreshType.REFRESH);
                    }
                }, WAIT_TIME_CHANNEL_ITEM_LINK_INIT, refreshIntervalMediumPriority.intValue(), TimeUnit.SECONDS);
            }

            logger.debug("Thing is successfully initialized.");
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_INITIALIZING_ERROR,
                    "Thing cannot be initialized.");
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        ScheduledFuture<?> mediumTasks = mediumPriorityTasks;
        if (mediumTasks != null) {
            mediumTasks.cancel(true);
            logger.debug("Medium prioriy tasks will not run anymore.");
        }
        mediumPriorityTasks = null;

        ScheduledFuture<?> highTasks = highPriorityTasks;
        if (highTasks != null) {
            highTasks.cancel(true);
            logger.debug("High prioriy tasks will not run anymore.");
        }
        highPriorityTasks = null;
    }

    @Override
    public void thingUpdated(Thing thing) {
        if (updateConfiguration(thing)) {
            super.thingUpdated(thing);
        }
    }

    public void changeChannelPriority(ChannelUID channelUID, @Nullable String priority) {
        synchronized (channelGroups) {
            if ((priority != null) && !channelGroups.containsKey(priority)) {
                channelGroups.put(priority, new HashSet<ChannelUID>());
            }

            for (Set<ChannelUID> value : channelGroups.values()) {
                if (value != null) {
                    value.remove(channelUID);
                }
            }

            if ((priority != null) && channelGroups.containsKey(priority)) {
                Set<ChannelUID> group = channelGroups.get(priority);
                if (group != null) {
                    group.add(channelUID);
                } else {
                    logger.debug("Invalid priority configuration parameter for channel{}.", channelUID);
                }
            }
        }
        updateState(channelUID, UnDefType.UNDEF);
    }

    public @Nullable Set<ChannelUID> getHighPriorityChannels() {
        return channelGroups.get(HIGH_PRIOIRITY);
    }

    public @Nullable Set<ChannelUID> getMediumPriorityChannels() {
        return channelGroups.get(MEDIUM_PRIOIRITY);
    }

    public @Nullable Set<ChannelUID> getLowPriorityChannels() {
        return channelGroups.get(LOW_PRIOIRITY);
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        if (isLinked(channelUID)) {
            super.updateState(channelUID, state);
        }
    }

    private boolean updateConfiguration(Thing thing) {
        boolean result = false;
        try {
            final Configuration config = thing.getConfiguration();
            synchronized (refreshIntervalHighPriority) {
                BigDecimal value = (BigDecimal) config.get(HIGH_PRIORITY_REFRESH_TIME);
                if (value.intValue() <= 0) {
                    throw new IllegalArgumentException("High priority refresh interval must be positive number.");
                }
                refreshIntervalHighPriority = value;
                logger.debug("Refresh time for high priority channels set to {} s.", refreshIntervalHighPriority);
            }
            synchronized (refreshIntervalMediumPriority) {
                BigDecimal value = (BigDecimal) config.get(MEDIUM_PRIORITY_REFRESH_TIME);
                if (value.intValue() <= 0) {
                    throw new IllegalArgumentException("Medium priority refresh interval must be positive number.");
                }
                refreshIntervalMediumPriority = value;
                logger.debug("Refresh time for medium priority channels set to {} s.", refreshIntervalMediumPriority);
            }
            result = true;
        } catch (IllegalArgumentException exception) {
            logger.warn("Refresh time value is invalid. Please change the thing configuration.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, exception.getMessage());
        } catch (ClassCastException exception) {
            logger.debug("Channel configuration cannot be read.");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, exception.getMessage());
        }
        return result;
    }

    private boolean updateProperties() {
        Map<String, String> properties = editProperties();
        try {
            properties.put(PROPERTY_CPU_LOGICAL_CORES, systeminfo.getCpuLogicalCores().toString());
            properties.put(PROPERTY_CPU_PHYSICAL_CORES, systeminfo.getCpuPhysicalCores().toString());
            properties.put(PROPERTY_OS_FAMILY, systeminfo.getOsFamily().toString());
            properties.put(PROPERTY_OS_MANUFACTURER, systeminfo.getOsManufacturer().toString());
            properties.put(PROPERTY_OS_VERSION, systeminfo.getOsVersion().toString());
            updateProperties(properties);
            logger.debug("Properties updated.");
            return true;
        } catch (Exception exception) {
            logger.debug("Cannot get system properties: {}. Please try to restart the binding.",
                    exception.getMessage());
            return false;
        }
    }

    /**
     * The device index is an optional part of the channelID - the last characters of the groupID. It is used to
     * identify unique device, when more than one devices are available (e.g. local disks with names C:\, D:\, E"\ - the
     * first will have deviceIndex=0, the second deviceIndex=1 ant etc).
     * When no device index is specified, default value of 0 (first device in the list) is returned.
     *
     * @param channelID the ID of the channel
     * @return natural number (number >=0)
     */
    private int getDeviceIndex(ChannelUID channelUID) {
        int deviceIndex = 0;
        String channelGroupID = channelUID.getGroupId();
        if (channelGroupID != null) {
            char lastChar = channelGroupID.charAt(channelGroupID.length() - 1);
            if (Character.isDigit(lastChar)) {
                // All non-digits are deleted from the ID
                String deviceIndexPart = channelGroupID.replaceAll("\\D+", "");
                deviceIndex = Integer.parseInt(deviceIndexPart);
            }
        }
        return deviceIndex;
    }
}
