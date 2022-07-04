/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.freeboxos.internal.handler;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.RestManager;
import org.openhab.binding.freeboxos.internal.config.ApiConsumerConfiguration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ServerHandler} handle common parts of Freebox bridges.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
abstract class ApiConsumerHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(ApiConsumerHandler.class);

    private @Nullable ScheduledFuture<?> globalJob;
    private @Nullable FreeboxOsHandler bridgeHandler;

    ApiConsumerHandler(Thing thing) {
        super(thing);
    }

    public <T extends RestManager> T getManager(Class<T> clazz) throws FreeboxException {
        FreeboxOsHandler handler = bridgeHandler;
        if (handler == null) {
            throw new FreeboxException("Bridge handler not yet defined");
        }
        return handler.getManager(clazz);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for thing {}", getThing().getUID());
        if (checkBridgeHandler()) {
            Map<String, String> properties = editProperties();
            if (properties.isEmpty()) {
                try {
                    internalGetProperties(properties);
                    updateProperties(properties);
                } catch (FreeboxException e) {
                    logger.warn("Error getting thing properties : {}", e.getMessage());
                }
            }
            startRefreshJob();
        }
    }

    abstract void internalGetProperties(Map<String, String> properties) throws FreeboxException;

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("Thing {}: bridge status changed to {}", getThing().getUID(), bridgeStatusInfo);
        if (checkBridgeHandler()) {
            startRefreshJob();
        } else {
            stopRefreshJob();
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType || getThing().getStatus() != ThingStatus.ONLINE) {
            return;
        }
        try {
            if (bridgeHandler == null || !internalHandleCommand(channelUID, command)) {
                logger.debug("Unexpected command {} on channel {}", command, channelUID.getId());
            }
        } catch (FreeboxException e) {
            logger.warn("Error handling command : {}", e.getMessage());
        }
    }

    private boolean checkBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler instanceof FreeboxOsHandler) {
                if (bridge.getStatus() == ThingStatus.ONLINE) {
                    bridgeHandler = (FreeboxOsHandler) handler;
                    updateStatus(ThingStatus.ONLINE);
                    return true;
                }
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
        return false;
    }

    @Override
    public void dispose() {
        logger.debug("Disposing handler for thing {}", getThing().getUID());
        stopRefreshJob();
        super.dispose();
    }

    private void startRefreshJob() {
        ScheduledFuture<?> job = globalJob;
        if (job == null || job.isCancelled()) {
            int refreshInterval = getConfigAs(ApiConsumerConfiguration.class).refreshInterval;
            logger.debug("Scheduling state update every {} seconds for thing {}...", refreshInterval,
                    getThing().getUID());
            ThingStatusDetail detail = thing.getStatusInfo().getStatusDetail();
            if (ThingStatusDetail.DUTY_CYCLE.equals(detail)) {
                boolean rebooting = true;
                while (rebooting) {
                    try {
                        internalPoll();
                        rebooting = false;
                    } catch (FreeboxException ignore) {
                        try {
                            Thread.sleep(20000);
                        } catch (InterruptedException e) {
                            rebooting = false;
                        }
                    }
                }
            }

            globalJob = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    internalPoll();
                } catch (FreeboxException e) {
                    logger.warn("Error polling thing {} : {}", getThing().getUID(), e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
            }, 0, refreshInterval, TimeUnit.SECONDS);
        }
    }

    protected void stopRefreshJob() {
        ScheduledFuture<?> job = globalJob;
        if (job != null && !job.isCancelled()) {
            logger.debug("Stop scheduled state update for thing {}", getThing().getUID());
            job.cancel(true);
            globalJob = null;
        }
    }

    protected boolean internalHandleCommand(ChannelUID channelUID, Command command) throws FreeboxException {
        return false;
    }

    protected abstract void internalPoll() throws FreeboxException;

    private void updateIfActive(String group, String channelId, State state) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        if (isLinked(id)) {
            updateState(id, state);
        }
    }

    protected void updateChannelDateTimeState(String group, String channelId, @Nullable ZonedDateTime timestamp) {
        updateIfActive(group, channelId, timestamp == null ? UnDefType.NULL : new DateTimeType(timestamp));
    }

    protected void updateChannelOnOff(String group, String channelId, boolean value) {
        updateIfActive(group, channelId, OnOffType.from(value));
    }

    protected void updateChannelString(String group, String channelId, @Nullable String value) {
        updateIfActive(group, channelId, value != null ? new StringType(value) : UnDefType.NULL);
    }

    protected void updateChannelQuantity(String group, String channelId, double d, Unit<?> unit) {
        updateChannelQuantity(group, channelId, new QuantityType<>(d, unit));
    }

    protected void updateChannelQuantity(String group, String channelId, @Nullable QuantityType<?> quantity) {
        updateIfActive(group, channelId, quantity != null ? quantity : UnDefType.NULL);
    }

    protected void updateChannelDecimal(String group, String channelId, @Nullable Integer value) {
        updateIfActive(group, channelId, value != null ? new DecimalType(value) : UnDefType.NULL);
    }

    protected void updateChannelQuantity(String group, String channelId, QuantityType<?> qtty, Unit<?> unit) {
        updateChannelQuantity(group, channelId, qtty.toUnit(unit));
    }
}
