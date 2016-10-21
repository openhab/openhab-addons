/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.command;

import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.zoneminder.internal.connection.TelnetAction;

public class ZoneMinderTrigger extends ZoneMinderOutgoingRequest {
    protected static final Integer EXTERNALTRIGGER_DEFAULT_PRIORITY = 255;
    protected static final Integer EXTERNALTRIGGER_DEFAULT_TIMEOUT = 60;

    @Override
    public ZoneMinderRequestType getRequestType() {
        return ZoneMinderRequestType.MONITOR_TRIGGER;
    }

    protected ZoneMinderTrigger(String monitorId, Command command, String reason, String note, Integer timeout) {
        super(TelnetAction.getEnum(command.toString()), monitorId, EXTERNALTRIGGER_DEFAULT_PRIORITY, reason, note, null,
                timeout);

    }

    /*
     * public static ZoneMinderTrigger create(String monitorId, Command command, String reason) {
     * return new ZoneMinderTrigger(monitorId, command, reason, null, 0);
     * }
     */
    public static ZoneMinderTrigger create(String monitorId, Command command, String reason, Integer timeout) {
        return new ZoneMinderTrigger(monitorId, command, reason, null, timeout);
    }

    public static ZoneMinderTrigger create(String monitorId, Command command, String reason, String note,
            Integer timeout) {
        return new ZoneMinderTrigger(monitorId, command, reason, note, timeout);
    }

}
