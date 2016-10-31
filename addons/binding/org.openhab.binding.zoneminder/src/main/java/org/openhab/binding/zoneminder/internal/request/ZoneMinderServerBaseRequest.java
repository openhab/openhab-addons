/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.request;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * Class that contains requests to a ZoneMinder Monitor.
 *
 * @author Martin S. Eskildsen
 */
public abstract class ZoneMinderServerBaseRequest {

    private ZoneMinderRequestType requestType;
    private String id;
    private ThingTypeUID thingTypeUID;

    public String getId() {
        return id;
    }

    public ThingTypeUID getThingTypeUID() {
        return thingTypeUID;
    }

    public ZoneMinderRequestType getRequestType() {
        return requestType;
    }

    ZoneMinderServerBaseRequest(ZoneMinderRequestType _requestType, ThingTypeUID _thingTypeUID, String _id) {
        this.requestType = _requestType;
        this.id = _id;
        this.thingTypeUID = _thingTypeUID;
    }
}
