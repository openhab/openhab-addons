/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.systeminfo.handler;

import static org.openhab.binding.systeminfo.SysteminfoBindingConstants.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.bidning.systeminfo.model.SysteminfoImpl;
import org.openhab.bidning.systeminfo.model.SysteminfoInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SysteminfoHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Svilen Valkanov - Initial contribution
 */
// TODO javadoc to all public methods

public class SysteminfoHandler extends BaseThingHandler {
    /**
     * Refresh interval for {@link #highPriorityChannels} in seconds.
     */
    private BigDecimal refreshIntervalHighPriority;

    /**
     * Refresh interval for {@link #mediumPriorityChannels} in seconds.
     */
    private BigDecimal refreshIntervalMediumPriority;

    /**
     * {@link #highPriorityChannels} are channels that usually need frequent update of the state like CPU load, or
     * information
     * about the free and used memory.
     * They are updated periodically at {@link #refreshIntervalHighPriority}.
     */
    private Set<ChannelUID> highPriorityChannels = new HashSet<ChannelUID>();

    /**
     * Medium priority channels are channels that usually need update of the state not so oft like battery capacity,
     * storage used and etc.
     * They are updated periodically at {@link #refreshIntervalMediumPriority}.
     */
    private Set<ChannelUID> mediumPriorityChannels = new HashSet<ChannelUID>();

    /**
     * Low priority channels usually need update only once. They represent static information or information that is
     * updated rare- e.g. CPU name, storage name and etc.
     * They are updated only at {@link #initialize()}.
     */
    private Set<ChannelUID> lowPriorityChannels = new HashSet<ChannelUID>();

    /**
     * Wait time for the creation of Item channel links in seconds. This delay is needed - if the links are not created
     * before the channel
     * state is updated, item state will not be updated.
     */
    private static final int WAIT_TIME_CHANNEL_ITEM_LINK_INIT = 1;

    private SysteminfoInterface system;

    private Logger logger = LoggerFactory.getLogger(SysteminfoHandler.class);

    public SysteminfoHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing !");

        // TODO Exceptions might occur here ! The binding might go to OFFLINE
        logger.debug("Start loading system libraries !");
        this.system = new SysteminfoImpl();

        if (isConfigurationValid()) {
            sortChannelsByPriority();
            refresh();
            // TODO message
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }

    }

    private boolean isConfigurationValid() {
        Configuration config = getConfig();
        try {
            refreshIntervalMediumPriority = (BigDecimal) config.get(MEDIUM_PRIORITY_REFRESH_TIME);
            logger.debug("Refresh time for medium priority channels set to {}", refreshIntervalMediumPriority);

            refreshIntervalHighPriority = (BigDecimal) config.get(HIGH_PRIORITY_REFRESH_TIME);
            logger.debug("Refresh time for high priority channels set to {}", refreshIntervalHighPriority);

            if (refreshIntervalHighPriority.intValue() <= 0 || refreshIntervalMediumPriority.intValue() <= 0) {
                throw new IllegalArgumentException("Refresh time must be positive value !");
            }
            return true;
        } catch (Exception e) {
            logger.error("Refresh time value is invalid!. Please change the thing configuration!", e);
            return false;
        }
    }

    /**
     * Channels are sorted according to their priority set in the configuration.
     */
    private void sortChannelsByPriority() {
        for (Channel channel : getThing().getChannels()) {
            String priority = (String) channel.getConfiguration().get("priority");

            switch (priority) {
                case "High":
                    highPriorityChannels.add(channel.getUID());
                    break;
                case "Medium":
                    mediumPriorityChannels.add(channel.getUID());
                    break;
                case "Low":
                    lowPriorityChannels.add(channel.getUID());
                    break;
            }
        }
    }

    private void refresh() {
        logger.debug("Schedule high priority tasks at fixed rate {} s.", refreshIntervalHighPriority);
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                // TODO rename, javadoc
                publishData(highPriorityChannels);
            }
        }, WAIT_TIME_CHANNEL_ITEM_LINK_INIT, refreshIntervalHighPriority.intValue(), TimeUnit.SECONDS);

        logger.debug("Schedule medium priority tasks at fixed rate {} s.", refreshIntervalMediumPriority);
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                publishData(mediumPriorityChannels);
            }
        }, WAIT_TIME_CHANNEL_ITEM_LINK_INIT, refreshIntervalMediumPriority.intValue(), TimeUnit.SECONDS);

        logger.debug("Schedule low priority tasks.");
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                publishData(lowPriorityChannels);
            }
        }, WAIT_TIME_CHANNEL_ITEM_LINK_INIT, TimeUnit.SECONDS);

    }

    private void publishData(Set<ChannelUID> channels) {
        Iterator<ChannelUID> iter = channels.iterator();
        while (iter.hasNext()) {
            ChannelUID channeUlID = iter.next();
            updateChannelState(system, channeUlID);
        }
    }

    /**
     * What is this method doing and why it is needed ?
     * This method is responsible for choosing the correct method from {@link SysteminfoInterface}}. In order to do that
     * the method has to check if
     *
     * @param system
     * @param channelUID
     */
    private void updateChannelState(SysteminfoInterface system, ChannelUID channelUID) {
        State state = null;
        int deviceIndex = 0;
        // TODO example + dynamic channels do not exist +link to issue
        String channelID = channelUID.getId();
        // The convention used here is - if the last character is a digit, this digit represents the index of the device
        // (in case of multiple devices possible)
        char lastChar = channelID.charAt(channelID.length() - 1);
        // TODO bug when number 10 is reached
        if (Character.isDigit(lastChar)) {
            deviceIndex = Character.getNumericValue(lastChar);
            channelID = channelID.substring(0, channelID.length() - 1);
        }

        switch (channelID) {
            case CHANNEL_OS_FAMILY:
                state = system.getOsFamily();
                break;
            case CHANNEL_OS_MANUFACTURER:
                state = system.getOsManufacturer();
                break;
            case CHANNEL_OS_VERSION:
                state = system.getOsVersion();
                break;
            case CHANNEL_DISPLAY_INFORMATION:
                state = system.getDisplayInformation(deviceIndex);
                break;
            case CHANNEL_BATTERY_NAME:
                state = system.getBatteryName(deviceIndex);
                break;
            case CHANNEL_BATTERY_REMAINING_CAPACITY:
                state = system.getBatteryRemainingCapacity(deviceIndex);
                break;
            case CHANNEL_BATTERY_REMAINING_TIME:
                state = system.getBatteryRemainingTime(deviceIndex);
                break;
            case CHANNEL_SENSORS_CPU_TEMPERATURE:
                state = system.getSensorsCpuTemperature();
                break;
            case CHANNEL_SENOSRS_CPU_VOLTAGE:
                state = system.getSensorsCpuVoltage();
                break;
            case CHANNEL_SENSORS_FAN_SPEED:
                state = system.getSensorsFanSpeed(deviceIndex);
                break;
            case CHANNEL_CPU_LOAD:
                state = system.getCpuLoad();
                break;
            case CHANNEL_CPU_PHYSICAL_CORES:
                state = system.getCpuPhysicalCores();
                break;
            case CHANNEL_CPU_LOGICAL_CORES:
                state = system.getCpuLogicalCores();
                break;
            case CHANNEL_CPU_DESCRIPTION:
                state = system.getCpuDescription();
                break;
            case CHANNEL_CPU_NAME:
                state = system.getCpuName();
                break;
            case CHANNEL_MEMORY_AVAILABLE:
                state = system.getMemoryAvailable();
                break;
            case CHANNEL_MEMORY_USED:
                state = system.getMemoryUsed();
                break;
            case CHANNEL_MEMORY_TOTAL:
                state = system.getMemoryTotal();
                break;
            case CHANNEL_STORAGE_NAME:
                state = system.getStorageName(deviceIndex);
                break;
            case CHANNEL_STORAGE_DESCRIPTION:
                state = system.getStorageDescription(deviceIndex);
                break;
            case CHANNEL_STORAGE_AVAILABLE:
                state = system.getStorageAvailable(deviceIndex);
                break;
            case CHANNEL_STORAGE_USED:
                state = system.getStorageUsed(deviceIndex);
                break;
            case CHANNEL_STORAGE_TOTAL:
                state = system.getStorageTotal(deviceIndex);
                break;
            case CHANNEL_NETWORK_IP:
                state = system.getNetworkIp(deviceIndex);
                break;
            case CHANNEL_NETWORK_ADAPTER_NAME:
                state = system.getNetworkAdapterName(deviceIndex);
                break;
            case CHANNEL_NETWORK_NAME:
                state = system.getNetworkName(deviceIndex);
                break;
        }
        if (state != null) {
            updateState(channelID, state);
        } else {
            logger.debug("Channel update failed ID {} state {}", channelID, state);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO what happens with concurrency ?
        if (command instanceof RefreshType) {
            logger.debug("Refreshing channel {} !", channelUID);
            // FIXME Final variable is needed
            final ChannelUID channUID = channelUID;
            scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    updateChannelState(system, channUID);
                }
            }, WAIT_TIME_CHANNEL_ITEM_LINK_INIT, TimeUnit.SECONDS);
        } else {
            logger.debug("Unsuported command {}! Supported commands: REFRESH", command);
        }
    }

    /**
     * private void addNewChannel(String group, String channel, int channelNumber, String channelType) {
     * try {
     * String channelIDString = "%s#%s";
     * String channelID = String.format(channelIDString, group, channel);
     * ChannelTypeUID channelTypeUID = getThing().getChannel(channelID).getChannelTypeUID();
     * ThingBuilder thingBuilder = editThing();
     * ThingUID thingUID = getThing().getUID();
     * ChannelUID channelUID = new ChannelUID(thingUID, channelID + channelNumber);
     * Channel newChannel = ChannelBuilder.create(channelUID, channelType).withType(channelTypeUID).build();
     * thingBuilder.withChannels(newChannel);
     * updateThing(thingBuilder.build());
     * } catch (Exception e) {
     * logger.debug("Could not add channels to device ", e);
     * }
     *
     * }
     **/
}
