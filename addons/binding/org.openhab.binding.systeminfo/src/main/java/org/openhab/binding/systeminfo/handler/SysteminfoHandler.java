/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.systeminfo.handler;

import static org.openhab.binding.systeminfo.SysteminfoBindingConstants.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
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
public class SysteminfoHandler extends BaseThingHandler {
    private static final int REFRESH_TIME_HIGH_PRIORITY = 1;
    private static final int REFRESH_TIME_MEDIUM_PRIORITY = 60;
    private Set<String> high_priority_channels = new HashSet<String>();
    private Set<String> medium_priority_channels = new HashSet<String>();
    private Set<String> low_priority_channels = new HashSet<String>();
    /**
     * Wait time for the creation of Item channel links in seconds
     */
    private static final int WAIT_TIME_CHANNEL_ITEM_LINK_INIT = 1;

    private SysteminfoInterface system;

    private Logger logger = LoggerFactory.getLogger(SysteminfoHandler.class);

    public SysteminfoHandler(Thing thing) {
        super(thing);
        this.system = new SysteminfoImpl();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO: No commands are supported, add message
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing !");

        sortChannelsByPriority();
        refresh();
        // TODO: When should the thing status be chaned to OFFLINE ?
        updateStatus(ThingStatus.ONLINE);
    }

    private void sortChannelsByPriority() {
        for (Channel channel : getThing().getChannels()) {
            String priority = (String) channel.getConfiguration().get("priority");

            switch (priority) {
                case "High":
                    high_priority_channels.add(channel.getUID().getAsString());
                    break;
                case "Medium":
                    medium_priority_channels.add(channel.getUID().getAsString());
                    break;
                case "Low":
                    low_priority_channels.add(channel.getUID().getAsString());
                    break;
            }
        }
    }

    private void refresh() {

        // TODO Start different schedulers for the different delays
        logger.debug("Schedule low priority tasks.");
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                publishData(low_priority_channels);
            }
        }, WAIT_TIME_CHANNEL_ITEM_LINK_INIT, TimeUnit.SECONDS);

        logger.debug("Schedule medium priority tasks.");
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                publishData(medium_priority_channels);
            }
        }, WAIT_TIME_CHANNEL_ITEM_LINK_INIT, REFRESH_TIME_MEDIUM_PRIORITY, TimeUnit.SECONDS);

        /**
         * scheduler.scheduleAtFixedRate(new Runnable() {
         *
         * @Override
         *           public void run() {
         *           publishData(high_priority_channels);
         *           }
         *           }, WAIT_TIME_CHANNEL_ITEM_LINK_INIT, REFRESH_TIME_HIGH_PRIORITY, TimeUnit.SECONDS);
         */
    }

    private void publishData(Set<String> channels) {
        Iterator<String> iter = channels.iterator();
        while (iter.hasNext()) {
            String channelID = iter.next();
            updateChannel(channelID);
        }
    }

    private void updateChannel(String channelID) {
        State state = null;

        int deviceIndex = 0;
        String channel = channelID.split("[:]")[3];
        String[] channelInfo = channel.split("[#]");
        String channelGroupTypeID = channelInfo[0];
        String channelTypeID = channelInfo[1];

        switch (channelGroupTypeID) {
            case CHANNEL_GROUP_OS: {
                switch (channelTypeID) {
                    case CHANNEL_FAMILY:
                        state = system.getOsFamily();
                        break;
                    case CHANNEL_MANUFACTURER:
                        state = system.getOsManufacturer();
                        break;
                    case CHANNEL_VERSION:
                        state = system.getOsVersion();
                        break;
                }
            }
            case CHANNEL_GROUP_DISPLAY: {
                switch (channelTypeID) {
                    case CHANNEL_EDID:
                        state = system.getDisplayInfo(deviceIndex);
                        break;
                }
            }
            case CHANNEL_GROUP_BATTERY: {
                switch (channelTypeID) {
                    case CHANNEL_NAME:
                        state = system.getBatteryName(deviceIndex);
                        break;
                    case CHANNEL_REMAINING_CAPACITY: {
                        state = system.getBatteryRemainingCapacity(deviceIndex);
                        break;
                    }
                    case CHANNEL_REMAINING_TIME: {
                        state = system.getBatteryRemainingTime(deviceIndex);
                        break;
                    }
                }
            }
            case CHANNEL_GROUP_SENSORS: {
                switch (channelTypeID) {
                    case CHANNEL_CPU_TEMPERATURE:
                        state = system.getSensorCpuTemp();
                        break;
                    case CHANNEL_CPU_VOLTAGE: {
                        state = system.getSensorCpuVoltage();
                        break;
                    }
                    case CHANNEL_FAN_SPEED: {
                        state = system.getSensorFanSpeed(deviceIndex);
                        break;
                    }
                }
            }
            case CHANNEL_GROUP_CPU: {
                switch (channelTypeID) {
                    case CHANNEL_CPU_LOAD:
                        state = system.getCpuLoad();
                        break;
                    case CHANNEL_CPU_PHYSICAL_CORES: {
                        state = system.getCpuPhysicalProcCount();
                        break;
                    }
                    case CHANNEL_CPU_LOGICAL_CORES: {
                        state = system.getCpuLogicalProcCount();
                        break;
                    }
                    case CHANNEL_DESCRIPTION: {
                        state = system.getCpuDescription();
                        break;
                    }
                }
            }
            case CHANNEL_GROUP_MEMORY: {
                switch (channelTypeID) {
                    case CHANNEL_AVAILABLE:
                        state = system.getMemoryAvailable();
                        break;
                    case CHANNEL_USED: {
                        state = system.getMemoryUsed();
                        break;
                    }
                    case CHANNEL_TOTAL: {
                        state = system.getMemoryTotal();
                        break;
                    }
                }
            }
            case CHANNEL_GROUP_STORAGE: {
                switch (channelTypeID) {
                    case CHANNEL_DESCRIPTION:
                        state = system.getStorageDescription(deviceIndex);
                        break;
                    case CHANNEL_AVAILABLE:
                        state = system.getStorageAvailable(deviceIndex);
                        break;
                    case CHANNEL_USED: {
                        state = system.getStorageUsed(deviceIndex);
                        break;
                    }
                    case CHANNEL_TOTAL: {
                        state = system.getStorageTotal(deviceIndex);
                        break;
                    }
                }
            }
            case CHANNEL_GROUP_NETWORK: {
                switch (channelTypeID) {
                    case CHANNEL_IP:
                        state = system.getNetworkIP(deviceIndex);
                        break;
                    case CHANNEL_ADAPTER_NAME: {
                        state = system.getNetworkAdapterName(deviceIndex);
                    }
                    case CHANNEL_NAME: {
                        state = system.getNetworkName(deviceIndex);
                        break;
                    }
                }
            }
        }
        if (state != null) {
            updateState(channel, state);
        } else {
            logger.debug("Channel update failed ID{} state{}", channelID, state);
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
