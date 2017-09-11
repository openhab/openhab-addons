/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.handler;

import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.nest.NestBindingConstants;
import org.openhab.binding.nest.internal.NestUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Deals with the structures on the nest api, turning them into a thing in openhab.
 *
 * @author David Bennett - initial contribution
 * @author Martin van Wingerden - splitted of NestBaseHandler
 */
abstract class NestBaseHandler extends BaseThingHandler {
    private Logger logger = LoggerFactory.getLogger(NestBaseHandler.class);

    NestBaseHandler(Thing thing) {
        super(thing);
    }

    State getAsStringTypeOrNull(Object value) {
        if (value == null) {
            return UnDefType.NULL;
        }

        return new StringType(value.toString());
    }

    State getAsDateTimeTypeOrNull(Date date) {
        if (date == null) {
            return UnDefType.NULL;
        }
        Calendar cal;
        cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.setTime(date);
        return new DateTimeType(cal);
    }

    void addUpdateRequest(String updateUrl, String field, Object value) {
        String deviceId = getThing().getProperties().get(NestBindingConstants.PROPERTY_ID);
        NestBridgeHandler bridge = (NestBridgeHandler) getBridge();
        bridge.addUpdateRequest(new NestUpdateRequest.Builder()
                .withBaseUrl(updateUrl)
                .withIdentifier(deviceId)
                .withAdditionalValue(field, value)
                .build());
    }
}
