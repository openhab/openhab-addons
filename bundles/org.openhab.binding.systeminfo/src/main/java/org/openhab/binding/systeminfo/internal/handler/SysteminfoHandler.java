/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.systeminfo.internal.model.SysteminfoInterface;
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
     * Refresh interval for {@link #HIGH_PRIOIRITY} in seconds.
     */
    private BigDecimal refreshIntervalHighPriority = new BigDecimal(1);

    /**
     * Refresh interval for {@link #MEDIUM_PRIOIRITY} in seconds.
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

    private Logger logger = LoggerFactory.getLogger(SysteminfoHandler.class);

    public SysteminfoHandler(Bridge bridge, @Nullable SysteminfoInterface systeminfo) {
        super(bridge);

        Objects.requireNonNull(systeminfo, "Systeminfo may not be null");
        this.systeminfo = systeminfo;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handle command {} on channel {}", command, channelUID);

        Thing thing = getThing();
        if (ThingStatus.ONLINE != thing.getStatus()) {
            logger.debug("Cannot handle command. Thing is not ONLINE.");
            return;
        }

        if (!(command instanceof RefreshType)) {
            logger.debug("Unsupported command {}. Supported commands: REFRESH.", command);
            return;
        }

        Thing child = getThingByUID(channelUID.getThingUID());
        if (child != null) {
            ThingHandler handler = child.getHandler();
            if (handler != null) {
                handler.handleCommand(channelUID, command);
            }
        } else {
            if (isLinked(channelUID)) {
                updateState(channelUID, getInfoForChannel(channelUID));
            }
        }
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing.");

        final Thing thing = getThing();
        if (updateConfiguration(thing) && updateProperties()) {
            for (Channel channel : thing.getChannels()) {
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
                    for (ChannelUID channel : group != null ? group : Collections.<ChannelUID>emptySet()) {
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
                    for (ChannelUID channel : group != null ? group : Collections.<ChannelUID>emptySet()) {
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
        if (mediumPriorityTasks != null) {
            mediumPriorityTasks.cancel(true);
            mediumPriorityTasks = null;
            logger.debug("Medium prioriy tasks will not run anymore.");
        }
        if (highPriorityTasks != null) {
            highPriorityTasks.cancel(true);
            highPriorityTasks = null;
            logger.debug("High prioriy tasks will not run anymore.");
        }
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
     * This method gets the information for specific channel through the {@link SysteminfoInterface}. It uses the
     * channel ID to call the correct method from the {@link SysteminfoInterface} with deviceIndex parameter (in case of
     * multiple devices, for reference see {@link #getDeviceIndex(String)}})
     *
     * @param channelUID the UID of the channel
     * @return State object or null, if there is no information for the device with this index
     */
    private State getInfoForChannel(ChannelUID channelUID) {
        State state = UnDefType.UNDEF;
        int deviceIndex = getDeviceIndex(channelUID);
        try {
            if (BATTERY_GROUP_ID.equalsIgnoreCase(channelUID.getGroupId())) {
                switch (channelUID.getIdWithoutGroup()) {
                    case CHANNEL_NAME: {
                        state = systeminfo.getBatteryName(deviceIndex);
                        break;
                    }
                    case CHANNEL_BATTERY_REMAINING_CAPACITY: {
                        state = systeminfo.getBatteryRemainingCapacity(deviceIndex);
                        break;
                    }
                    case CHANNEL_BATTERY_REMAINING_TIME: {
                        state = systeminfo.getBatteryRemainingTime(deviceIndex);
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
                        state = systeminfo.getCpuDescription();
                        break;
                    }
                    case CHANNEL_NAME: {
                        state = systeminfo.getCpuName();
                        break;
                    }
                    case CHANNEL_THREADS: {
                        state = systeminfo.getCpuThreads();
                        break;
                    }
                    case CHANNEL_CPU_LOAD: {
                        state = systeminfo.getCpuLoad();
                        break;
                    }
                    case CHANNEL_CPU_LOAD_1: {
                        state = systeminfo.getCpuLoad1();
                        break;
                    }
                    case CHANNEL_CPU_LOAD_5: {
                        state = systeminfo.getCpuLoad5();
                        break;
                    }
                    case CHANNEL_CPU_LOAD_15: {
                        state = systeminfo.getCpuLoad15();
                        break;
                    }
                    case CHANNEL_CPU_TEMPERATURE: {
                        state = systeminfo.getSensorsCpuTemperature();
                        break;
                    }
                    case CHANNEL_CPU_VOLTAGE: {
                        state = systeminfo.getSensorsCpuVoltage();
                        break;
                    }
                    case CHANNEL_CPU_UPTIME: {
                        state = systeminfo.getCpuUptime();
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
                        state = systeminfo.getDisplayInformation(deviceIndex);
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
                        state = systeminfo.getDriveModel(deviceIndex);
                        break;
                    }
                    case CHANNEL_DRIVE_SERIAL: {
                        state = systeminfo.getDriveSerialNumber(deviceIndex);
                        break;
                    }
                    case CHANNEL_NAME: {
                        state = systeminfo.getDriveName(deviceIndex);
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
                        state = systeminfo.getSensorsFanSpeed(deviceIndex);
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
                        state = systeminfo.getMemoryAvailable();
                        break;
                    }
                    case CHANNEL_TOTAL: {
                        state = systeminfo.getMemoryTotal();
                        break;
                    }
                    case CHANNEL_USED: {
                        state = systeminfo.getMemoryUsed();
                        break;
                    }
                    case CHANNEL_AVAILABLE_PERCENT: {
                        state = systeminfo.getMemoryAvailablePercent();
                        break;
                    }
                    case CHANNEL_USED_PERCENT: {
                        state = systeminfo.getMemoryUsedPercent();
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
                        state = systeminfo.getNetworkName(deviceIndex);
                        break;
                    }
                    case CHANNEL_NETWORK_IP: {
                        state = systeminfo.getNetworkIp(deviceIndex);
                        break;
                    }
                    case CHANNEL_NETWORK_INTERFACE: {
                        state = systeminfo.getNetworkDisplayName(deviceIndex);
                        break;
                    }
                    case CHANNEL_NETWORK_MAC: {
                        state = systeminfo.getNetworkMac(deviceIndex);
                        break;
                    }
                    case CHANNEL_NETWORK_DATA_SENT: {
                        state = systeminfo.getNetworkDataSent(deviceIndex);
                        break;
                    }
                    case CHANNEL_NETWORK_DATA_RECEIVED: {
                        state = systeminfo.getNetworkDataReceived(deviceIndex);
                        break;
                    }
                    case CHANNEL_NETWORK_PACKETS_RECEIVED: {
                        state = systeminfo.getNetworkPacketsReceived(deviceIndex);
                        break;
                    }
                    case CHANNEL_NETWORK_PACKETS_SENT: {
                        state = systeminfo.getNetworkPacketsSent(deviceIndex);
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
                        state = systeminfo.getStorageAvailable(deviceIndex);
                        break;
                    }
                    case CHANNEL_DESCRIPTION: {
                        state = systeminfo.getStorageDescription(deviceIndex);
                        break;
                    }
                    case CHANNEL_NAME: {
                        state = systeminfo.getStorageName(deviceIndex);
                        break;
                    }
                    case CHANNEL_TOTAL: {
                        state = systeminfo.getStorageTotal(deviceIndex);
                        break;
                    }
                    case CHANNEL_USED: {
                        state = systeminfo.getStorageUsed(deviceIndex);
                        break;
                    }
                    case CHANNEL_AVAILABLE_PERCENT: {
                        state = systeminfo.getStorageAvailablePercent(deviceIndex);
                        break;
                    }
                    case CHANNEL_USED_PERCENT: {
                        state = systeminfo.getStorageUsedPercent(deviceIndex);
                        break;
                    }
                    case CHANNEL_STORAGE_TYPE: {
                        state = systeminfo.getStorageType(deviceIndex);
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
                        state = systeminfo.getSwapAvailable();
                        break;
                    }
                    case CHANNEL_TOTAL: {
                        state = systeminfo.getSwapTotal();
                        break;
                    }
                    case CHANNEL_USED: {
                        state = systeminfo.getSwapUsed();
                        break;
                    }
                    case CHANNEL_AVAILABLE_PERCENT: {
                        state = systeminfo.getSwapAvailablePercent();
                        break;
                    }
                    case CHANNEL_USED_PERCENT: {
                        state = systeminfo.getSwapUsedPercent();
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
            logger.warn("No information for channel {} with device index {}.", channelUID, deviceIndex);
        } catch (Exception exception) {
            logger.debug("Unexpected error occurred while getting system information.", exception);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, exception.getMessage());
        }
        return state;
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
