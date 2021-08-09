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
package org.openhab.binding.freeboxos.internal.handler;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiHandler;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.config.ApiConsumerConfiguration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
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
    protected static final Set<Command> TRUE_COMMANDS = Set.of(OnOffType.ON, UpDownType.UP, OpenClosedType.OPEN);
    protected static final Set<Class<?>> ON_OFF_CLASSES = Set.of(OnOffType.class, UpDownType.class,
            OpenClosedType.class);

    private final Logger logger = LoggerFactory.getLogger(ApiConsumerHandler.class);

    private final ZoneId zoneId;
    private @NonNullByDefault({}) ScheduledFuture<?> globalJob;
    protected @NonNullByDefault({}) ApiBridgeHandler bridgeHandler;

    ApiConsumerHandler(Thing thing, ZoneId zoneId) {
        super(thing);
        this.zoneId = zoneId;
    }

    public ApiHandler getApi() {
        return bridgeHandler.getApi();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for thing {}", getThing().getUID());
        ApiConsumerConfiguration configuration = getConfigAs(ApiConsumerConfiguration.class);

        if (globalJob == null || globalJob.isCancelled()) {
            logger.debug("Scheduling state update every {} seconds...", configuration.refreshInterval);
            globalJob = scheduler.scheduleWithFixedDelay(() -> {
                if (checkBridgeHandler()) {
                    try {
                        internalPoll();
                        updateStatus(ThingStatus.ONLINE);
                    } catch (FreeboxException e) {
                        logger.warn("Error polling thing {} : {}", getThing().getUID(), e);
                        updateStatus(ThingStatus.OFFLINE);
                    }
                }
            }, 5, configuration.refreshInterval, TimeUnit.SECONDS);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType || (getThing().getStatus() == ThingStatus.UNKNOWN || (getThing()
                .getStatus() == ThingStatus.OFFLINE
                && (getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE
                        || getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_UNINITIALIZED
                        || getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR)))) {
            return;
        }
        try {
            if (bridgeHandler == null || !internalHandleCommand(channelUID, command)) {
                logger.warn("Unexpected command {} on channel {}", command, channelUID.getId());
            }
        } catch (FreeboxException e) {
            logger.warn("Error handling command : {}", e);
        }
    }

    protected boolean checkBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler instanceof ApiBridgeHandler) {
                if (handler.getThing().getStatus() == ThingStatus.ONLINE) {
                    bridgeHandler = (ApiBridgeHandler) handler;
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

    protected void stopRefreshJob() {
        if (globalJob != null && !globalJob.isCancelled()) {
            globalJob.cancel(true);
            globalJob = null;
        }
    }

    protected boolean internalHandleCommand(ChannelUID channelUID, Command command) throws FreeboxException {
        return false;
    }

    protected abstract void internalPoll() throws FreeboxException;

    protected void updateChannelDateTimeState(String group, String channelId, long timestamp) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        if (isLinked(id)) {
            Instant i = Instant.ofEpochSecond(timestamp);
            updateState(id, timestamp == 0 ? UnDefType.NULL : new DateTimeType(ZonedDateTime.ofInstant(i, zoneId)));
        }
    }

    protected void updateChannelOnOff(String group, String channelId, boolean value) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        if (isLinked(id)) {
            updateState(id, OnOffType.from(value));
        }
    }

    protected void updateChannelString(String group, String channelId, @Nullable String value) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        if (isLinked(id)) {
            updateState(id, value != null ? new StringType(value) : UnDefType.NULL);
        }
    }

    protected void updateChannelQuantity(String group, String channelId, double d, Unit<?> unit) {
        updateChannelQuantity(group, channelId, new QuantityType<>(d, unit));
    }

    protected void updateChannelQuantity(String group, String channelId, @Nullable QuantityType<?> quantity) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        if (isLinked(id)) {
            updateState(id, quantity != null ? quantity : UnDefType.NULL);
        }
    }

    protected void updateChannelDecimal(String group, String channelId, int value) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        if (isLinked(id)) {
            updateState(id, new DecimalType(value));
        }
    }

    protected void updateChannelQuantity(String group, String channelId, QuantityType<?> qtty, Unit<?> unit) {
        updateChannelQuantity(group, channelId, qtty.toUnit(unit));
    }
}
