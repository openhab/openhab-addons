/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.nest.internal.config.NestDeviceConfiguration;
import org.openhab.binding.nest.internal.data.NestIdentifiable;
import org.openhab.binding.nest.internal.listener.NestThingDataListener;
import org.openhab.binding.nest.internal.rest.NestUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deals with the structures on the Nest API, turning them into a thing in openHAB.
 *
 * @author David Bennett - Initial contribution
 * @author Martin van Wingerden - Splitted of NestBaseHandler
 * @author Wouter Born - Add generic update data type
 *
 * @param <T> the type of update data
 */
@NonNullByDefault
abstract class NestBaseHandler<T> extends BaseThingHandler implements NestThingDataListener<T>, NestIdentifiable {
    private final Logger logger = LoggerFactory.getLogger(NestBaseHandler.class);
    private final Set<ChannelUID> linkedChannelUIDs = new CopyOnWriteArraySet<>();

    private @Nullable String deviceId;
    private Class<T> dataClass;

    NestBaseHandler(Thing thing, Class<T> dataClass) {
        super(thing);
        this.dataClass = dataClass;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for {}", getClass().getName());
        linkedChannelUIDs.clear();
        linkedChannelUIDs.addAll(this.getThing().getChannels().stream().filter(c -> isLinked(c.getUID()))
                .map(c -> c.getUID()).collect(Collectors.toSet()));

        NestBridgeHandler handler = getNestBridgeHandler();
        if (handler != null) {
            boolean success = handler.addThingDataListener(dataClass, getId(), this);
            logger.debug("Adding {} with ID '{}' as device data listener, result: {}", getClass().getSimpleName(),
                    getId(), success);
        } else {
            logger.debug("Unable to add {} with ID '{}' as device data listener because bridge is null");
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Waiting for refresh");

        T lastUpdate = getLastUpdate();
        if (lastUpdate != null) {
            update(null, lastUpdate);
        }
    }

    @Override
    public void dispose() {
        NestBridgeHandler handler = getNestBridgeHandler();
        if (handler != null) {
            handler.removeThingDataListener(dataClass, getId(), this);
        }
    }

    protected @Nullable T getLastUpdate() {
        NestBridgeHandler handler = getNestBridgeHandler();
        if (handler != null) {
            return handler.getLastUpdate(dataClass, getId());
        }
        return null;
    }

    protected void addUpdateRequest(String updatePath, String field, Object value) {
        NestBridgeHandler handler = getNestBridgeHandler();
        if (handler != null) {
            // @formatter:off
            handler.addUpdateRequest(new NestUpdateRequest.Builder()
                .withBasePath(updatePath)
                .withIdentifier(getId())
                .withAdditionalValue(field, value)
                .build());
            // @formatter:on
        }
    }

    protected <U extends Quantity<U>> QuantityType<U> commandToQuantityType(Command command, Unit<U> defaultUnit) {
        if (command instanceof QuantityType) {
            return (QuantityType<U>) command;
        }
        return new QuantityType<U>(new BigDecimal(command.toString()), defaultUnit);
    }

    @Override
    public String getId() {
        return getDeviceId();
    }

    protected String getDeviceId() {
        String localDeviceId = deviceId;
        if (localDeviceId == null) {
            localDeviceId = getConfigAs(NestDeviceConfiguration.class).deviceId;
            deviceId = localDeviceId;
        }
        return localDeviceId;
    }

    protected @Nullable NestBridgeHandler getNestBridgeHandler() {
        Bridge bridge = getBridge();
        return bridge != null ? (NestBridgeHandler) bridge.getHandler() : null;
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
        return value == null ? UnDefType.NULL : new QuantityType<U>(value, unit);
    }

    protected State getAsStringTypeOrNull(@Nullable Object value) {
        return value == null ? UnDefType.NULL : new StringType(value.toString());
    }

    protected State getAsStringTypeListOrNull(@Nullable Collection<? extends Object> values) {
        return values == null || values.isEmpty() ? UnDefType.NULL : new StringType(StringUtils.join(values, ","));
    }

    protected boolean isNotHandling(NestIdentifiable nestIdentifiable) {
        return !(getId().equals(nestIdentifiable.getId()));
    }

    protected void updateLinkedChannels(T oldData, T data) {
        linkedChannelUIDs.forEach(channelUID -> {
            State newState = getChannelState(channelUID, data);
            if (oldData == null || !getChannelState(channelUID, oldData).equals(newState)) {
                logger.debug("Updating {}", channelUID);
                updateState(channelUID, newState);
            }
        });
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);
        linkedChannelUIDs.add(channelUID);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        super.channelUnlinked(channelUID);
        linkedChannelUIDs.remove(channelUID);
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

    protected abstract void update(T oldData, T data);

}
