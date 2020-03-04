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
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
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
    private final TimeZoneProvider timeZoneProvider;
    private @NonNullByDefault({}) ScheduledFuture<?> globalJob;
    protected @NonNullByDefault({}) FreeboxAPIHandler bridgeHandler;

    public APIConsumerHandler(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
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
        if (bridgeHandler == null || !internalHandleCommand(channelUID, command)) {
            logger.debug("Thing {}: unexpected command {} from channel {}", getThing().getUID(), command,
                    channelUID.getId());
        }
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

        if (thing.getProperties().isEmpty()) {
            try {
                checkBridgeHandler();
                Map<String, String> properties = discoverAttributes();
                if (!properties.isEmpty()) {
                    updateProperties(properties);
                }
            } catch (FreeboxException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "Error getting Freebox Server configuration items");
            }
        }

        if (globalJob == null || globalJob.isCancelled()) {
            logger.debug("Scheduling state update every {} seconds...", configuration.refreshInterval);
            globalJob = scheduler.scheduleWithFixedDelay(() -> {
                if (checkBridgeHandler()) {
                    internalPoll();
                }
            }, 5, configuration.refreshInterval, TimeUnit.SECONDS);
        }
    }

    private boolean checkBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                bridgeHandler = (FreeboxAPIHandler) handler;
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

    protected boolean internalHandleCommand(ChannelUID channelUID, Command command) {
        // By default, does nothing
        return false;
    }

    protected abstract void internalPoll();

    protected void updateChannelStringState(String group, String channel, String state) {
        updateState(new ChannelUID(getThing().getUID(), group, channel), StringType.valueOf(state));
    }

    protected void updateChannelSwitchState(String group, String channel, boolean state) {
        updateState(new ChannelUID(getThing().getUID(), group, channel), OnOffType.from(state));
    }

    protected void updateChannelDecimalState(String group, String channel, int state) {
        updateState(new ChannelUID(getThing().getUID(), group, channel), new DecimalType(state));
    }

    protected void updateChannelDecimalState(String group, String channel, long state) {
        updateState(new ChannelUID(getThing().getUID(), group, channel), new DecimalType(state));
    }

    protected void updateChannelQuantityType(String group, String channelId, @Nullable QuantityType<?> quantity) {
        if (quantity != null) {
            updateState(new ChannelUID(getThing().getUID(), group, channelId), quantity);
        }
    }

    protected void logCommandException(FreeboxException e, ChannelUID channelUID, Command command) {
        if (e.isMissingRights()) {
            logger.debug("Thing {}: missing right {} while handling command {} from channel {}", getThing().getUID(),
                    e.getResponse().getMissingRight(), command, channelUID.getId());
        } else {
            logger.debug("Thing {}: error while handling command {} from channel {}", getThing().getUID(), command,
                    channelUID.getId(), e);
        }
    }

    protected void handleFreeboxException(FreeboxException e) {
        if (e.isMissingRights()) {
            logger.debug("Phone state job: missing right {}", e.getResponse().getMissingRight());
            updateStatus(ThingStatus.ONLINE);
        } else {
            logger.debug("Phone state job failed: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    protected ZonedDateTime convertTimestamp(long timestamp) {
        Instant i = Instant.ofEpochSecond(timestamp);
        return ZonedDateTime.ofInstant(i, timeZoneProvider.getTimeZone());
    }

    protected void updateChannelDateTimeState(String group, String channel, long timestamp) {
        updateState(new ChannelUID(getThing().getUID(), group, channel), new DateTimeType(convertTimestamp(timestamp)));
    }

}