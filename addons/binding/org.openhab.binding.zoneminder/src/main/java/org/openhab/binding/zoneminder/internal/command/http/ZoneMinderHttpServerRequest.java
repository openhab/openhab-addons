package org.openhab.binding.zoneminder.internal.command.http;

import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.internal.command.ZoneMinderMessage.ZoneMinderRequestType;

public class ZoneMinderHttpServerRequest extends ZoneMinderHttpRequest {

    ZoneMinderHttpServerRequest(ZoneMinderRequestType _requestType, String id) {
        super(_requestType, ZoneMinderConstants.THING_TYPE_BRIDGE_ZONEMINDER_SERVER, id);
        // TODO Auto-generated constructor stub
    }

}
