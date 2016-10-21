/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.command;

import java.util.EventObject;
/*
public class ZoneMinderEvent extends ZoneMinderIncomming {

    public ZoneMinderEvent(String command) {
        super(command);
    }

    public Boolean isStateActive() {
        return (getAction() == TelnetAction.ON) ? true : false;
    }
}
*/

import org.eclipse.smarthome.core.thing.ThingTypeUID;

public class ZoneMinderEvent extends EventObject {

    private ZoneMinderMessage message = null;
    private ThingTypeUID thingTypeUID = null;

    /**
     * Constructor.
     *
     * @param source
     */
    public ZoneMinderEvent(ThingTypeUID thingTypeUID, Object source) {
        super(source);
        this.thingTypeUID = thingTypeUID;
        this.message = (ZoneMinderMessage) source;
    }

    public ThingTypeUID getThingTypeUID() {
        return thingTypeUID;
    }

    public String getZoneMinderId() {
        return message.getZoneMinderId();
    }

    /**
     * Adds the the received ZoneMinderMessage to the event.
     *
     * @param message
     */
    public void setZoneMinderMessage(ZoneMinderMessage message) {
        this.message = message;
    }

    /**
     * Returns the Message event from ZoneMinder.
     *
     * @return apiMessage
     */
    public ZoneMinderMessage getZoneMinderMessage() {
        return message;
    }
}