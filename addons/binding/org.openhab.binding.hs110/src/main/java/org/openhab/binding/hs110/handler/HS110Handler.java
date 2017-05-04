/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hs110.handler;

import static org.openhab.binding.hs110.HS110BindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.config.core.status.ConfigStatusMessage;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.ConfigStatusThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.hs110.internal.HS110;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HS110Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Fischer - Initial contribution
 */
public class HS110Handler extends ConfigStatusThingHandler {

    private Logger logger = LoggerFactory.getLogger(HS110Handler.class);

    private HS110 plug;
    private static final int REFRESH_DEFAULT = 5;
    private BigDecimal refresh;
    private ScheduledFuture<?> refreshJob;

    private String sysinfoData;
    private String energyData;

    public HS110Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType) {
            if (channelUID.getId().equals(CHANNEL_SWITCH)) {
                logger.debug("Switching {} {}", thing.getUID().getAsString(), command.toFullString());
                try {
                    plug.sendSwitch((OnOffType) command);
                } catch (IOException e) {
                    logger.error("Failed sending command {} to {}", command.toFullString(),
                            thing.getUID().getAsString(), e);
                }
            }
        }

        if (command instanceof RefreshType) {
            boolean success = updateData();
            if (success) {
                switch (channelUID.getId()) {
                    case CHANNEL_WATTAGE:
                        updateState(channelUID, getWattage());
                        break;
                    case CHANNEL_TOTAL:
                        updateState(channelUID, getTotal());
                        break;
                    case CHANNEL_SWITCH:
                        updateState(channelUID, getState());
                        break;
                    case CHANNEL_SYSINFO:
                        updateState(channelUID, getSysinfo());
                        break;
                    default:
                        logger.debug("Command received for an unknown channel: {}", channelUID.getId());
                        break;
                }
            }
        } else {
            logger.debug("Command {} is not supported for channel: {}", command, channelUID.getId());
        }

    }

    @Override
    public void dispose() {
        refreshJob.cancel(true);
    }

    @Override
    public void initialize() {
        Configuration config = getThing().getConfiguration();

        String ip = (String) config.get(CONFIG_IP);
        logger.info("Initializing plug on ip {}", ip);
        plug = new HS110(ip);

        if (config.containsKey(CONFIG_REFRESH)) {
            refresh = (BigDecimal) config.get(CONFIG_REFRESH);
        } else {
            refresh = new BigDecimal(REFRESH_DEFAULT);
        }

        startAutomaticRefresh();

        updateStatus(ThingStatus.ONLINE);

    }

    private void startAutomaticRefresh() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                logger.debug("Refreshing {}", thing.getUID().getAsString());
                try {
                    boolean success = updateData();
                    if (success) {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_SWITCH), getState());
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_WATTAGE), getWattage());
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_TOTAL), getTotal());
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_SYSINFO), getSysinfo());
                    }
                } catch (Exception e) {
                    logger.debug("Exception occurred during execution", e);
                }
            }
        };

        refreshJob = scheduler.scheduleAtFixedRate(runnable, 0, refresh.intValue(), TimeUnit.SECONDS);
    }

    private synchronized boolean updateData() {
        logger.trace("Updating data for Plug type {}", thing.getThingTypeUID().getAsString());
        try {
            sysinfoData = plug.sendCommand(HS110.Command.SYSINFO);
            if (thing.getThingTypeUID().equals(THING_TYPE_HS110)) {
                energyData = plug.sendCommand(HS110.Command.ENERGY);
                logger.debug("Updated energy Data for {}: {}", thing.getUID(), energyData);
            }
            logger.debug("Updated sysinfo Data for {}: {}", thing.getUID(), sysinfoData);
            return true;

        } catch (IOException e) {
            logger.warn("Error accessing plug ", e);
            energyData = null;
            sysinfoData = null;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            return false;
        }
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {

        Collection<ConfigStatusMessage> configStatus = new ArrayList<>();

        try {
            InetAddress ip = InetAddress.getByName(plug.ip);
            if (!ip.isReachable(500)) {
                configStatus.add(
                        ConfigStatusMessage.Builder.error("offline").withMessageKeySuffix("ip unreachable").build());
            }
        } catch (IOException e) {
            logger.debug("Communication error ocurred reaching the device ", e);
        }

        return configStatus;
    }

    private State getWattage() {
        if (energyData != null) {
            BigDecimal wattage = HS110.parseWattage(energyData);
            if (wattage != null) {
                return new DecimalType(wattage);
            }
        }
        return UnDefType.UNDEF;
    }

    private State getTotal() {
        if (energyData != null) {
            BigDecimal total = HS110.parseTotal(energyData);
            if (total != null) {
                return new DecimalType(total);
            }
        }
        return UnDefType.UNDEF;
    }

    private State getSysinfo() {
        if (sysinfoData != null) {
            String sysinfo = HS110.parseSysinfo(sysinfoData);
            if (sysinfo != null) {
                return new StringType(sysinfo);
            }
        }
        return UnDefType.UNDEF;
    }

    private State getState() {
        if (sysinfoData != null) {
            OnOffType state = HS110.parseState(sysinfoData);
            if (state != null) {
                return state;
            }
        }
        return UnDefType.UNDEF;
    }
}
