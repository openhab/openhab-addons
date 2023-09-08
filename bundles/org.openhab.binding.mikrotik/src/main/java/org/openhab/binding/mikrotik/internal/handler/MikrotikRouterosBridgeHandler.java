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
package org.openhab.binding.mikrotik.internal.handler;

import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.types.RefreshType.REFRESH;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mikrotik.internal.MikrotikBindingConstants;
import org.openhab.binding.mikrotik.internal.config.RouterosThingConfig;
import org.openhab.binding.mikrotik.internal.model.RouterosDevice;
import org.openhab.binding.mikrotik.internal.model.RouterosRouterboardInfo;
import org.openhab.binding.mikrotik.internal.model.RouterosSystemResources;
import org.openhab.binding.mikrotik.internal.util.StateUtil;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.legrange.mikrotik.MikrotikApiException;

/**
 * The {@link MikrotikRouterosBridgeHandler} is a main binding class that wraps a {@link RouterosDevice} and
 * manages fetching data from RouterOS. It is also responsible for updating brindge thing properties and
 * handling commands, which are sent to one of the channels and emit channel updates whenever required.
 *
 * @author Oleg Vivtash - Initial contribution
 */
@NonNullByDefault
public class MikrotikRouterosBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(MikrotikRouterosBridgeHandler.class);
    private @Nullable RouterosThingConfig config;
    private @Nullable volatile RouterosDevice routeros;
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
        var cfg = getConfigAs(RouterosThingConfig.class);
        this.config = cfg;
        logger.debug("Initializing MikrotikRouterosBridgeHandler with config = {}", cfg);
        if (cfg.isValid()) {
            this.routeros = new RouterosDevice(cfg.host, cfg.port, cfg.login, cfg.password);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, String.format("Connecting to %s", cfg.host));
            scheduleRefreshJob();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Configuration is not valid");
        }
    }

    public @Nullable RouterosDevice getRouteros() {
        return routeros;
    }

    public @Nullable RouterosThingConfig getBridgeConfig() {
        return config;
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        if (status == ThingStatus.ONLINE
                || (status == ThingStatus.OFFLINE && statusDetail == ThingStatusDetail.COMMUNICATION_ERROR)) {
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
        cancelRefreshJob();
        var routeros = this.routeros;
        if (routeros != null) {
            routeros.stop();
            this.routeros = null;
        }
    }

    private void scheduleRefreshJob() {
        synchronized (this) {
            var cfg = this.config;
            if (refreshJob == null) {
                int refreshPeriod = 10;
                if (cfg != null) {
                    refreshPeriod = cfg.refresh;
                } else {
                    logger.warn("null config spotted in scheduleRefreshJob");
                }
                logger.debug("Scheduling refresh job every {}s", refreshPeriod);
                refreshJob = scheduler.scheduleWithFixedDelay(this::scheduledRun, 0, refreshPeriod, TimeUnit.SECONDS);
            }
        }
    }

    private void cancelRefreshJob() {
        synchronized (this) {
            var job = this.refreshJob;
            if (job != null) {
                logger.debug("Cancelling refresh job");
                job.cancel(true);
                this.refreshJob = null;
            }
        }
    }

    private void scheduledRun() {
        var routeros = this.routeros;
        if (routeros == null) {
            logger.error("RouterOS device is null in scheduledRun");
            return;
        }
        if (!routeros.isConnected()) {
            // Perform connection
            try {
                logger.debug("Starting routeros model");
                routeros.start();

                RouterosRouterboardInfo rbInfo = routeros.getRouterboardInfo();
                if (rbInfo != null) {
                    Map<String, String> bridgeProps = editProperties();
                    bridgeProps.put(MikrotikBindingConstants.PROPERTY_MODEL, rbInfo.getModel());
                    bridgeProps.put(MikrotikBindingConstants.PROPERTY_FIRMWARE, rbInfo.getFirmware());
                    bridgeProps.put(MikrotikBindingConstants.PROPERTY_SERIAL_NUMBER, rbInfo.getSerialNumber());
                    updateProperties(bridgeProps);
                } else {
                    logger.warn("Failed to set RouterBOARD properties for bridge {}", getThing().getUID());
                }
                updateStatus(ThingStatus.ONLINE);
            } catch (MikrotikApiException e) {
                logger.warn("Error while logging in to RouterOS {} | Cause: {}", getThing().getUID(), e, e.getCause());

                String errorMessage = e.getMessage();
                if (errorMessage == null) {
                    errorMessage = "Error connecting (UNKNOWN ERROR)";
                }
                if (errorMessage.contains("Command timed out") || errorMessage.contains("Error connecting")) {
                    routeros.stop();
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
                } else if (errorMessage.contains("Connection refused")) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Remote host refused to connect, make sure port is correct");
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, errorMessage);
                }
            }
        } else {
            // We're connected - do a usual polling cycle
            performRefresh();
        }
    }

    private void performRefresh() {
        var routeros = this.routeros;
        if (routeros == null) {
            logger.error("RouterOS device is null in performRefresh");
            return;
        }
        try {
            logger.debug("Refreshing RouterOS caches for {}", getThing().getUID());
            routeros.refresh();
            // refresh own channels
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
                if (handler instanceof MikrotikBaseThingHandler<?> thingHandler) {
                    thingHandler.refresh();
                }
            });
        } catch (ChannelUpdateException e) {
            logger.debug("Error updating channel! {}", e.getMessage(), e.getCause());
        } catch (MikrotikApiException e) {
            logger.error("RouterOS cache refresh failed in {} due to Mikrotik API error", getThing().getUID(), e);
            routeros.stop();
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
            RouterosDevice routeros = getRouteros();
            if (routeros != null) {
                if (command == REFRESH) {
                    refreshChannel(channelUID);
                } else {
                    logger.warn("Ignoring command = {} for channel = {} as it is not yet supported", command,
                            channelUID);
                }
            }
        }
    }

    protected void refreshChannel(ChannelUID channelUID) {
        RouterosDevice routerOs = getRouteros();
        String channelID = channelUID.getIdWithoutGroup();
        RouterosSystemResources rbRes = null;
        if (routerOs != null) {
            rbRes = routerOs.getSysResources();
        }
        State oldState = currentState.getOrDefault(channelID, UnDefType.NULL);
        State newState = oldState;

        if (rbRes == null) {
            newState = UnDefType.NULL;
        } else {
            switch (channelID) {
                case MikrotikBindingConstants.CHANNEL_UP_SINCE:
                    newState = StateUtil.timeOrNull(rbRes.getUptimeStart());
                    break;
                case MikrotikBindingConstants.CHANNEL_FREE_SPACE:
                    newState = StateUtil.qtyBytesOrNull(rbRes.getFreeSpace());
                    break;
                case MikrotikBindingConstants.CHANNEL_TOTAL_SPACE:
                    newState = StateUtil.qtyBytesOrNull(rbRes.getTotalSpace());
                    break;
                case MikrotikBindingConstants.CHANNEL_USED_SPACE:
                    newState = StateUtil.qtyPercentOrNull(rbRes.getSpaceUse());
                    break;
                case MikrotikBindingConstants.CHANNEL_FREE_MEM:
                    newState = StateUtil.qtyBytesOrNull(rbRes.getFreeMem());
                    break;
                case MikrotikBindingConstants.CHANNEL_TOTAL_MEM:
                    newState = StateUtil.qtyBytesOrNull(rbRes.getTotalMem());
                    break;
                case MikrotikBindingConstants.CHANNEL_USED_MEM:
                    newState = StateUtil.qtyPercentOrNull(rbRes.getMemUse());
                    break;
                case MikrotikBindingConstants.CHANNEL_CPU_LOAD:
                    newState = StateUtil.qtyPercentOrNull(rbRes.getCpuLoad());
                    break;
            }
        }

        if (!newState.equals(oldState)) {
            updateState(channelID, newState);
            currentState.put(channelID, newState);
        }
    }
}
