/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.handler;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.Gson;

/***
 * The{@link ShellyTrigger} post events to the event channel of the device or component
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyTrigger {
    @Nullable
    private ShellyBaseHandler thingHandler;

    final ShellyTriggerGson   event;
    private Gson              gson = new Gson();

    public ShellyTrigger(ShellyBaseHandler thingHandler, String thingName, String category) {
        this.thingHandler = thingHandler;
        event = new ShellyTriggerGson(thingName, category);
    }

    public void setPayload(Map<String, String> args) {
        event.setPayload(args);
    }

    public String toJson() {
        return gson.toJson(event);
    }

    @SuppressWarnings("null")
    public void sendEvent() {
        Validate.notNull(thingHandler);
        Date date = new Date(); // this object contains the current date value
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        event.timestamp = formatter.format(date);
        thingHandler.triggerChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_EVENT_TRIGGER, toJson());
    }
}
