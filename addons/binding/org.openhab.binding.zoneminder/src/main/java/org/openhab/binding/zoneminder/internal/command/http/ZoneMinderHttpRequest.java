package org.openhab.binding.zoneminder.internal.command.http;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.zoneminder.internal.command.ZoneMinderMessage.ZoneMinderRequestType;

public abstract class ZoneMinderHttpRequest {

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

    ZoneMinderHttpRequest(ZoneMinderRequestType _requestType, ThingTypeUID _thingTypeUID, String _id) {
        this.requestType = _requestType;
        this.id = _id;
        this.thingTypeUID = _thingTypeUID;
    }
}
