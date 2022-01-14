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
package org.openhab.binding.nest.internal.wwn.handler;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nest.internal.wwn.config.WWNDeviceConfiguration;
import org.openhab.binding.nest.internal.wwn.dto.WWNIdentifiable;
import org.openhab.binding.nest.internal.wwn.dto.WWNUpdateRequest;
import org.openhab.binding.nest.internal.wwn.listener.WWNThingDataListener;
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
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deals with the structures on the WWN API, turning them into a thing in openHAB.
 *
 * @author David Bennett - Initial contribution
 * @author Martin van Wingerden - Splitted of NestBaseHandler
 * @author Wouter Born - Add generic update data type
 *
 * @param <T> the type of update data
 */
@NonNullByDefault
public abstract class WWNBaseHandler<@NonNull T> extends BaseThingHandler
        implements WWNThingDataListener<T>, WWNIdentifiable {
    private final Logger logger = LoggerFactory.getLogger(WWNBaseHandler.class);

    private String deviceId = "";
    private Class<T> dataClass;

    WWNBaseHandler(Thing thing, Class<T> dataClass) {
        super(thing);
        this.dataClass = dataClass;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for {}", getClass().getName());

        WWNAccountHandler handler = getAccountHandler();
        if (handler != null) {
            boolean success = handler.addThingDataListener(dataClass, getId(), this);
            logger.debug("Adding {} with ID '{}' as device data listener, result: {}", getClass().getSimpleName(),
                    getId(), success);
        } else {
            logger.debug("Unable to add {} with ID '{}' as device data listener because bridge is null",
                    getClass().getSimpleName(), getId());
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Waiting for refresh");

        final @Nullable T lastUpdate = getLastUpdate();
        if (lastUpdate != null) {
            update(null, lastUpdate);
        }
    }

    @Override
    public void dispose() {
        WWNAccountHandler handler = getAccountHandler();
        if (handler != null) {
            handler.removeThingDataListener(dataClass, getId(), this);
        }
    }

    protected @Nullable T getLastUpdate() {
        WWNAccountHandler handler = getAccountHandler();
        if (handler != null) {
            return handler.getLastUpdate(dataClass, getId());
        }
        return null;
    }

    protected void addUpdateRequest(String updatePath, String field, Object value) {
        WWNAccountHandler handler = getAccountHandler();
        if (handler != null) {
            handler.addUpdateRequest(new WWNUpdateRequest.Builder() //
                    .withBasePath(updatePath) //
                    .withIdentifier(getId()) //
                    .withAdditionalValue(field, value) //
                    .build());
        }
    }

    @Override
    public String getId() {
        return getDeviceId();
    }

    protected String getDeviceId() {
        String localDeviceId = deviceId;
        if (localDeviceId.isEmpty()) {
            localDeviceId = getConfigAs(WWNDeviceConfiguration.class).deviceId;
            deviceId = localDeviceId;
        }
        return localDeviceId;
    }

    protected @Nullable WWNAccountHandler getAccountHandler() {
        Bridge bridge = getBridge();
        return bridge != null ? (WWNAccountHandler) bridge.getHandler() : null;
    }

    protected abstract State getChannelState(ChannelUID channelUID, T data);

    protected State getAsDateTimeTypeOrNull(@Nullable Date date) {
        if (date == null) {
            return UnDefType.NULL;
        }

        long offsetMillis = TimeZone.getDefault().getOffset(date.getTime());
        Instant instant = date.toInstant().plusMillis(offsetMillis);
        return new DateTimeType(ZonedDateTime.ofInstant(instant, TimeZone.getDefault().toZoneId()));
    }

    protected State getAsDecimalTypeOrNull(@Nullable Integer value) {
        return value == null ? UnDefType.NULL : new DecimalType(value);
    }

    protected State getAsOnOffTypeOrNull(@Nullable Boolean value) {
        return value == null ? UnDefType.NULL : value ? OnOffType.ON : OnOffType.OFF;
    }

    protected <U extends Quantity<U>> State getAsQuantityTypeOrNull(@Nullable Number value, Unit<U> unit) {
        return value == null ? UnDefType.NULL : new QuantityType<>(value, unit);
    }

    protected State getAsStringTypeOrNull(@Nullable Object value) {
        return value == null ? UnDefType.NULL : new StringType(value.toString());
    }

    protected State getAsStringTypeListOrNull(@Nullable Collection<@NonNull ?> values) {
        return values == null || values.isEmpty() ? UnDefType.NULL
                : new StringType(values.stream().map(value -> value.toString()).collect(Collectors.joining(",")));
    }

    protected boolean isNotHandling(WWNIdentifiable nestIdentifiable) {
        return !(getId().equals(nestIdentifiable.getId()));
    }

    protected void updateLinkedChannels(@Nullable T oldData, T data) {
        getThing().getChannels().stream().map(channel -> channel.getUID()).filter(this::isLinked)
                .forEach(channelUID -> {
                    State newState = getChannelState(channelUID, data);
                    if (oldData == null || !getChannelState(channelUID, oldData).equals(newState)) {
                        logger.debug("Updating {}", channelUID);
                        updateState(channelUID, newState);
                    }
                });
    }

    @Override
    public void onNewData(T data) {
        update(null, data);
    }

    @Override
    public void onUpdatedData(T oldData, T data) {
        update(oldData, data);
    }

    @Override
    public void onMissingData(String nestId) {
        thing.setStatusInfo(
                new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "Missing from streaming updates"));
    }

    protected abstract void update(@Nullable T oldData, T data);
}
