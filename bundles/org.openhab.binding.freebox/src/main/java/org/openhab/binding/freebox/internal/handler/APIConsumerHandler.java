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
package org.openhab.binding.freebox.internal.handler;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.BridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.freebox.internal.api.ApiManager;
import org.openhab.binding.freebox.internal.api.FreeboxException;
import org.openhab.binding.freebox.internal.config.APIConsumerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ServerHandler} handle common parts of Freebox bridges.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public abstract class APIConsumerHandler extends BaseThingHandler {
    final Logger logger = LoggerFactory.getLogger(APIConsumerHandler.class);
    private final ZoneId zoneId;
    private @NonNullByDefault({}) ScheduledFuture<?> globalJob;
    protected @NonNullByDefault({}) ServerHandler bridgeHandler;

    public APIConsumerHandler(Thing thing, ZoneId zoneId) {
        super(thing);
        this.zoneId = zoneId;
    }

    protected Map<String, String> discoverAttributes() throws FreeboxException {
        final Map<String, String> properties = new HashMap<>();
        properties.put(Thing.PROPERTY_VENDOR, "Freebox SAS");
        return properties;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for thing {}", getThing().getUID());
        APIConsumerConfiguration configuration = getConfigAs(APIConsumerConfiguration.class);

        if (thing.getProperties().isEmpty() && checkBridgeHandler()) {
            try {
                Map<String, String> properties = discoverAttributes();
                updateProperties(properties);
            } catch (FreeboxException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Error getting Freebox Server configuration items");
            }
        }

        if (globalJob == null || globalJob.isCancelled()) {
            logger.debug("Scheduling state update every {} seconds...", configuration.refreshInterval);
            globalJob = scheduler.scheduleWithFixedDelay(() -> {
                if (checkBridgeHandler()) {
                    try {
                        internalPoll();
                        updateStatus(ThingStatus.ONLINE);
                    } catch (FreeboxException e) {
                        logger.warn("Error polling thing {} : {}", this.getThing().getUID(), e.getMessage());
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
                logger.debug("Thing {}: unexpected command {} from channel {}", getThing().getUID(), command,
                        channelUID.getId());
            }
        } catch (FreeboxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private boolean checkBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                bridgeHandler = (ServerHandler) handler;
                updateStatus(ThingStatus.ONLINE);
                return true;
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
        if (globalJob != null && !globalJob.isCancelled()) {
            globalJob.cancel(true);
            globalJob = null;
        }
        super.dispose();
    }

    protected boolean internalHandleCommand(ChannelUID channelUID, Command command) throws FreeboxException {
        // By default, does nothing
        return false;
    }

    protected abstract void internalPoll() throws FreeboxException;

    protected void logCommandException(FreeboxException e, ChannelUID channelUID, Command command) {
        if (e.isMissingRights()) {
            logger.debug("Thing {}: missing right {} while handling command {} from channel {}", getThing().getUID(),
                    e.getResponse().getMissingRight(), command, channelUID.getId());
        } else {
            logger.debug("Thing {}: error while handling command {} from channel {}", getThing().getUID(), command,
                    channelUID.getId(), e);
        }
    }

    protected ZonedDateTime convertTimestamp(long timestamp) {
        Instant i = Instant.ofEpochSecond(timestamp);
        return ZonedDateTime.ofInstant(i, zoneId);
    }

    protected void updateChannelDateTimeState(String group, String channel, long timestamp) {
        updateState(new ChannelUID(getThing().getUID(), group, channel), new DateTimeType(convertTimestamp(timestamp)));
    }

    protected ApiManager getApiManager() {
        return bridgeHandler.getApiManager();
    }

    protected void updateChannelOnOff(String group, String channelId, boolean value) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        if (isLinked(id)) {
            updateState(id, OnOffType.from(value));
        }
    }

    protected void updateChannelString(String group, String channelId, String value) {
        ChannelUID id = new ChannelUID(getThing().getUID(), group, channelId);
        if (isLinked(id)) {
            updateState(id, new StringType(value));
        }
    }

    protected void updateChannelQuantity(String group, String channelId, QuantityType<?> qtty, Unit<?> unit) {
        updateChannelQuantity(group, channelId, qtty.toUnit(unit));
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

}