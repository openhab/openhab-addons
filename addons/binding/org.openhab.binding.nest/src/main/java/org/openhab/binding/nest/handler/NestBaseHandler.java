/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.nest.internal.NestDeviceDataListener;
import org.openhab.binding.nest.internal.NestUpdateRequest;
import org.openhab.binding.nest.internal.config.NestDeviceConfiguration;
import org.openhab.binding.nest.internal.data.BaseNestDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deals with the structures on the Nest API, turning them into a thing in openHAB.
 *
 * @author David Bennett - initial contribution
 * @author Martin van Wingerden - Splitted of NestBaseHandler
 */
abstract class NestBaseHandler extends BaseThingHandler implements NestDeviceDataListener {
    private final Logger logger = LoggerFactory.getLogger(NestBaseHandler.class);

    NestBaseHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing handler for {}", getClass().getName());
        getNestBridgeHandler().map(b -> b.addDeviceDataListener(NestBaseHandler.this));

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Waiting for refresh");
    }

    @Override
    public void dispose() {
        getNestBridgeHandler().map(b -> b.removeDeviceDataListener(NestBaseHandler.this));
    }

    private String getDeviceId() {
        return getConfigAs(NestDeviceConfiguration.class).deviceId;
    }

    private Optional<NestBridgeHandler> getNestBridgeHandler() {
        return Optional.ofNullable(getBridge()).map(b -> (NestBridgeHandler) b.getHandler());
    }

    boolean isNotHandling(BaseNestDevice nestDevice) {
        return !(getDeviceId().equals(nestDevice.getDeviceId()));
    }

    State getAsStringTypeOrNull(Object value) {
        return value == null ? UnDefType.NULL :  new StringType(value.toString());
    }

    State getAsDateTimeTypeOrNull(Date date) {
        if (date == null) {
            return UnDefType.NULL;
        }
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(date);
        return new DateTimeType(cal);
    }

    void addUpdateRequest(String updateUrl, String field, Object value) {
        NestBridgeHandler bridge = (NestBridgeHandler) getBridge().getHandler();
        // @formatter:off
        bridge.addUpdateRequest(new NestUpdateRequest.Builder()
                .withBaseUrl(updateUrl)
                .withIdentifier(getDeviceId())
                .withAdditionalValue(field, value)
                .build());
        // @formatter:on
    }
}
