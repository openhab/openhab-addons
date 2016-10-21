/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.command;

import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.openhab.binding.zoneminder.internal.connection.TelnetAction;

public class ZoneMinderTelnetEvent extends ZoneMinderMessage {
    private static final Integer IDX_UNKNOWN1 = 2;
    private static final Integer IDX_EVENTID = 3;

    private static final String COMMAND_UNKNOWN1 = "unknown1";
    private static final String COMMAND_EVENTID = "eventId";

    private String unknown1;
    private Integer eventId;

    public ZoneMinderTelnetEvent(String command) {
        super(command);
    }

    /**
     *
     * @return
     */
    public OpenClosedType getStateAsOpenClosedType() {
        return (getAction() == TelnetAction.ON) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
    }

    public OnOffType getStateAsOnOffType() {
        return (getAction() == TelnetAction.ON) ? OnOffType.ON : OnOffType.OFF;
    }

    @Override
    protected void onParseCommand(String[] commandParts) {
        unknown1 = commandParts[IDX_UNKNOWN1];
        eventId = Integer.parseInt(commandParts[IDX_EVENTID]);

    }

    @Override
    protected String onBuildSubCommandString(String command) {
        switch (command) {
            case COMMAND_UNKNOWN1:
                return unknown1;
            case COMMAND_EVENTID:
                return convertToString(eventId);
        }
        return null;
    }

    @Override
    protected List<String> getCommandArray() {
        return Arrays.asList(COMMAND_MONITORID, COMMAND_ACTION, COMMAND_UNKNOWN1, COMMAND_EVENTID);
    }

    public Integer getEventId() {
        return eventId;
    }

    @Override
    public ZoneMinderRequestType getRequestType() {
        // TODO Auto-generated method stub
        return ZoneMinderRequestType.MONITOR_EVENT;
    }

}
