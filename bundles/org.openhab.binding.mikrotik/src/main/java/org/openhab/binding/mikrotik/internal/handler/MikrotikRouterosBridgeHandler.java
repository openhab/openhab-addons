/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.mikrotik.internal.handler;

import me.legrange.mikrotik.MikrotikApiException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.mikrotik.internal.MikrotikBindingConstants;
import org.openhab.binding.mikrotik.internal.config.RouterosThingConfig;
import org.openhab.binding.mikrotik.internal.model.RouterosInstance;
import org.openhab.binding.mikrotik.internal.model.RouterosRouterboardInfo;
import org.openhab.binding.mikrotik.internal.model.RouterosSystemResources;
import org.openhab.binding.mikrotik.internal.util.StateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.eclipse.smarthome.core.thing.ThingStatus.ONLINE;
import static org.eclipse.smarthome.core.types.RefreshType.REFRESH;
import static org.openhab.binding.mikrotik.internal.MikrotikBindingConstants.*;


/**
 * The {@link MikrotikRouterosBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class MikrotikRouterosBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(MikrotikRouterosBridgeHandler.class);
    private @Nullable RouterosThingConfig config;
    private @Nullable volatile RouterosInstance routeros;
    private @Nullable ScheduledFuture<?> refreshJob;
    private Map<String, State> currentState = new HashMap<>();

    public static boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return MikrotikBindingConstants.THING_TYPE_ROUTEROS.equals(thingTypeUID);
    }

    public MikrotikRouterosBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        cancelRefreshJob();
        config = getConfigAs(RouterosThingConfig.class);

        logger.debug("Initializing MikrotikRouterosBridgeHandler with config = {}", config);
        routeros = new RouterosInstance(config.host, config.port, config.login,
                config.password);

        updateStatus(ThingStatus.INITIALIZING);
        scheduler.execute(() -> {
            try {
                logger.debug("Starting routeros model");
                routeros.start();

                @Nullable RouterosRouterboardInfo rbInfo = routeros.getRouterboardInfo();
                if(rbInfo != null){
                    Map<String, String> bridgeProps = editProperties();
                    bridgeProps.put(PROPERTY_MODEL, rbInfo.getModel());
                    bridgeProps.put(PROPERTY_FIRMWARE, rbInfo.getFirmware());
                    bridgeProps.put(PROPERTY_SERIAL_NUMBER, rbInfo.getSerialNumber());
                    updateProperties(bridgeProps);
                } else {
                    logger.warn("Failed to set RouterBOARD properties to bridge {}", getThing().getUID());
                }

                updateStatus(ThingStatus.ONLINE);
            } catch (MikrotikApiException e) {
                logger.warn("Error while logging in to RouterOS", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            }
        });
        logger.debug("Finished initializing Mikrotik binding!");
    }


    public @Nullable RouterosInstance getRouteros() { return routeros; }
    public @Nullable RouterosThingConfig getBridgeConfig() {
        return config;
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        logger.trace("Attempt updating {} status to {}; detail = {}", getThing().getUID(), status, statusDetail);
        if (status == ThingStatus.ONLINE || (status == ThingStatus.OFFLINE && statusDetail == ThingStatusDetail.COMMUNICATION_ERROR)) {
            scheduleRefreshJob();
        } else if (status == ThingStatus.OFFLINE && statusDetail == ThingStatusDetail.CONFIGURATION_ERROR) {
            cancelRefreshJob();
        }
        // update the status only if it's changed
        ThingStatusInfo statusInfo = ThingStatusInfoBuilder.create(status, statusDetail).withDescription(description)
                .build();
        if (!statusInfo.equals(getThing().getStatusInfo())) {
            super.updateStatus(status, statusDetail, description);
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing RouterOS bridge");
        cancelRefreshJob();
        if (routeros != null) {
            try {
                routeros.stop();
            } catch (MikrotikApiException e) {
                logger.debug("Error during bridge dispose", e);
            }
            routeros = null;
        }
    }

    private void scheduleRefreshJob() {
        synchronized (this) {
            if (refreshJob == null) {
                logger.debug("Scheduling refresh job every {}s", config.refresh);
                refreshJob = scheduler.scheduleWithFixedDelay(this::scheduledRun, 0, config.refresh, TimeUnit.SECONDS);
            }
        }
    }

    private void cancelRefreshJob() {
        synchronized (this) {
            if (refreshJob != null) {
                logger.debug("Cancelling refresh job");
                refreshJob.cancel(true);
                refreshJob = null;
            }
        }
    }

    private void scheduledRun() {
        try {
            if (routeros != null && routeros.isConnected()) {
                logger.debug("Refreshing RouterOS caches for {}", getThing().getUID());
                routeros.refresh();
                    //refresh own channels
                    for (Channel channel : getThing().getChannels()) {
                        try {
                            refreshChannel(channel.getUID());
                        } catch (RuntimeException e) {
                            throw new ChannelUpdateException(getThing().getUID(), channel.getUID(), e);
                        }
                    }
                    // refresh all the client things below
                    getThing().getThings().forEach(thing -> {
                        ThingHandler handler = thing.getHandler();
                        if (handler instanceof MikrotikBaseThingHandler) {
                            ((MikrotikBaseThingHandler) handler).refresh();
                        }
                    });
            } else {
                logger.debug("Skipping RouterOS cache update as routeros {} is not available", getThing().getUID());
                updateStatus(ThingStatus.OFFLINE);
            }
        } catch (ChannelUpdateException e) {
            logger.error("Error updating channel! {}", e.getMessage(), e.getInnerException());
        } catch (MikrotikApiException e) {
            logger.error("Failed to refresh RouterOS cache in {}", getThing().getUID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (Exception e) {
            logger.error("Unhandled exception while refreshing the {} RouterOS model", getThing().getUID(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }


    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Handling command = {} for channel = {}", command, channelUID);
        if (getThing().getStatus() == ONLINE) {
            @Nullable RouterosInstance routeros = getRouteros();
            if (routeros != null) {
                if (command == REFRESH) {
                    refreshChannel(channelUID);
                } else {
                    logger.warn("Ignoring command = {} for channel = {} as it is not yet supported", command, channelUID);
                }
            }
        }
    }

    protected void refreshChannel(ChannelUID channelUID) {
        String channelID = channelUID.getIdWithoutGroup();
        RouterosSystemResources rbRes = getRouteros().getSysResources();
        State oldState = currentState.getOrDefault(channelID, UnDefType.NULL);
        State newState = oldState;

        if(rbRes == null){
            newState = UnDefType.NULL;
        } else {
            switch (channelID) {
                case CHANNEL_UP_TIME:
                    newState = StateUtil.stringOrNull(rbRes.getUptime());
                    break;
                case CHANNEL_FREE_SPACE:
                    newState = StateUtil.intOrNull(rbRes.getFreeSpace());
                    break;
                case CHANNEL_TOTAL_SPACE:
                    newState = StateUtil.intOrNull(rbRes.getTotalSpace());
                    break;
                case CHANNEL_USED_SPACE:
                    newState = StateUtil.intOrNull(rbRes.getSpaceUse());
                    break;
                case CHANNEL_FREE_MEM:
                    newState = StateUtil.intOrNull(rbRes.getFreeMem());
                    break;
                case CHANNEL_TOTAL_MEM:
                    newState = StateUtil.intOrNull(rbRes.getTotalMem());
                    break;
                case CHANNEL_USED_MEM:
                    newState = StateUtil.intOrNull(rbRes.getMemUse());
                    break;
                case CHANNEL_CPU_LOAD:
                    newState = StateUtil.stringOrNull(rbRes.getCpuLoad());
                    break;
            }
        }

        logger.trace("About to update state on channel {} for thing {}: newState({}) = {}, oldState = {}", channelUID,
                getThing().getUID(), newState.getClass().getSimpleName(), newState, oldState);
        if(newState != oldState){
            updateState(channelID, newState);
            currentState.put(channelID, newState);
        }
    }

}
