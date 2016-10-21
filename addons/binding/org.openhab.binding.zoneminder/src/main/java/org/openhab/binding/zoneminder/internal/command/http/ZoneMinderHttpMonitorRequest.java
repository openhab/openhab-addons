package org.openhab.binding.zoneminder.internal.command.http;

import org.openhab.binding.zoneminder.ZoneMinderConstants;
import org.openhab.binding.zoneminder.internal.command.ZoneMinderMessage.ZoneMinderRequestType;

public class ZoneMinderHttpMonitorRequest extends ZoneMinderHttpRequest {

    public ZoneMinderHttpMonitorRequest(ZoneMinderRequestType _requestType, String _monitorId) {
        super(_requestType, ZoneMinderConstants.THING_TYPE_THING_ZONEMINDER_MONITOR, _monitorId);

    }

}
