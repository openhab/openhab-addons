/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.zoneminder.internal.request;

import org.openhab.binding.zoneminder.ZoneMinderConstants;

/**
 * Class that contains requests to a ZoneMinder Monitor.
 *
 * @author Martin S. Eskildsen
 */
public class ZoneMinderMonitorTriggerRequest extends ZoneMinderServerBaseRequest {

    private Boolean _activateState = false;
    private String _reason = "";
    private Integer _timeout = 0;
    private Integer _priority = 255;

    public ZoneMinderMonitorTriggerRequest(ZoneMinderRequestType requestType, Boolean activateState, String monitorId,
            Integer priority, String reason, Integer timeout) {
        super(requestType, ZoneMinderConstants.THING_TYPE_THING_ZONEMINDER_MONITOR, monitorId);

        _reason = reason;
        _priority = priority;
        _timeout = timeout;
        _activateState = activateState;
    }

    public Boolean getActivatedState() {
        return _activateState;
    }

    public Integer getTimeout() {
        return _timeout;
    }

    public Integer getPriority() {
        return _priority;
    }

    public String getReason() {
        return _reason;
    }
}
