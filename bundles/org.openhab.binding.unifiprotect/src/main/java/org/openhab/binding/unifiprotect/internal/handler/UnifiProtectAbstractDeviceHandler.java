/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.unifiprotect.internal.handler;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.unifiprotect.internal.api.UniFiProtectApiClient;
import org.openhab.binding.unifiprotect.internal.api.dto.ApiValueEnum;
import org.openhab.binding.unifiprotect.internal.api.dto.Device;
import org.openhab.binding.unifiprotect.internal.api.dto.events.BaseEvent;
import org.openhab.binding.unifiprotect.internal.config.UnifiProtectContactConfiguration;
import org.openhab.binding.unifiprotect.internal.config.UnifiProtectDeviceConfiguration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract handler for all device types.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public abstract class UnifiProtectAbstractDeviceHandler<T extends Device> extends BaseThingHandler {
    protected final Logger logger = LoggerFactory.getLogger(UnifiProtectAbstractDeviceHandler.class);
    protected @Nullable T device;
    protected String deviceId = "";
    protected Map<String, State> stateCache = new HashMap<>();

    private Map<String, ScheduledFuture<?>> latchJobs = new ConcurrentHashMap<>();

    public enum WSEventType {
        ADD,
        UPDATE
    }

    public UnifiProtectAbstractDeviceHandler(Thing thing) {
        super(thing);
    }

    public void updateFromDevice(T device) {
        this.device = device;
    }

    public abstract void handleEvent(BaseEvent event, WSEventType eventType);

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        deviceId = getConfigAs(UnifiProtectDeviceConfiguration.class).deviceId;
    }

    @Override
    public void dispose() {
        latchJobs.forEach((k, v) -> v.cancel(true));
        latchJobs.clear();
    }

    // making public
    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }

    @Override
    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    public void updateState(String channelUID, State state) {
        super.updateState(channelUID, state);
        stateCache.put(channelUID, state);
    }

    public void markGone() {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "GONE");
    }

    protected void refreshState(String channelId) {
        State state = stateCache.get(channelId);
        if (state != null) {
            super.updateState(channelId, state);
        }
    }

    protected @Nullable UniFiProtectApiClient getApiClient() {
        Thing bridge = getBridge();
        if (bridge != null) {
            BaseThingHandler h = (BaseThingHandler) bridge.getHandler();
            if (h instanceof UnifiProtectNVRHandler) {
                return ((UnifiProtectNVRHandler) h).getApiClient();
            }
        }
        return null;
    }

    protected boolean hasChannel(String channelId) {
        return thing.getChannel(new ChannelUID(thing.getUID(), channelId)) != null;
    }

    protected void updateBooleanChannel(String channelId, @Nullable Boolean value) {
        updateState(channelId, value == null ? UnDefType.NULL : value ? OnOffType.ON : OnOffType.OFF);
    }

    protected void updateIntegerChannel(String channelId, @Nullable Integer value) {
        updateState(channelId, value == null ? UnDefType.NULL : new DecimalType(value));
    }

    protected void updateDimmerChannel(String channelId, @Nullable Integer value) {
        updateState(channelId, value == null ? UnDefType.NULL : new PercentType(value));
    }

    protected void updateStringChannel(String channelId, @Nullable String value) {
        updateState(channelId, value == null ? UnDefType.NULL : new StringType(value));
    }

    protected void updateApiValueChannel(String channelId, ApiValueEnum value) {
        updateStringChannel(channelId, value.getApiValue());
    }

    protected void updateDateTimeChannel(String channelId, long epochMillis) {
        updateState(channelId, new DateTimeType(Instant.ofEpochMilli(epochMillis)));
    }

    protected void updateDecimalChannel(String channelId, @Nullable Number value) {
        updateState(channelId,
                value == null ? UnDefType.NULL : new DecimalType(BigDecimal.valueOf(value.doubleValue())));
    }

    protected void updateTimeChannel(String channelId, @Nullable Long milliseconds) {
        updateState(channelId, milliseconds == null ? UnDefType.NULL
                : new QuantityType<Time>(milliseconds.longValue(), MetricPrefix.MILLI(Units.SECOND)));
    }

    protected void updateContactChannel(String channelId, State state) {
        if (getThing().getChannel(channelId) instanceof Channel channel) {
            if (state instanceof OpenClosedType openClosedType) {
                UnifiProtectContactConfiguration c = channel.getConfiguration()
                        .as(UnifiProtectContactConfiguration.class);
                ScheduledFuture<?> existing = latchJobs.remove(channelId);
                if (existing != null) {
                    existing.cancel(true);
                }
                if (openClosedType == OpenClosedType.OPEN) {
                    latchJobs.put(channelId, scheduler.schedule(() -> {
                        logger.debug("running close for channel: {}", channelId);
                        updateState(channelId, OpenClosedType.CLOSED);
                        latchJobs.remove(channelId);
                    }, c.motionLatchDelay, TimeUnit.MILLISECONDS));
                }
            }
            updateState(channelId, state);
        }
    }

    protected static @Nullable Long timeToMilliseconds(Type type) {
        if (type instanceof QuantityType<?> quantity) {
            QuantityType<?> milliseconds = quantity.toUnit(MetricPrefix.MILLI(Units.SECOND));
            if (milliseconds != null) {
                return milliseconds.longValue();
            }
        } else if (type instanceof Number number) {
            return number.longValue();
        }
        return null;
    }
}
