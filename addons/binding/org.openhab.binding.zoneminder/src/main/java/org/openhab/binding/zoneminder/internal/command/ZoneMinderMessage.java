/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.command;

import java.util.List;
import java.util.regex.Pattern;

import org.openhab.binding.zoneminder.internal.connection.TelnetAction;

public abstract class ZoneMinderMessage {

    private static final String COMMAND_DELIMITER = "|";
    private static final String COMMAND_SUBDELIMITER = "+";
    private static final Integer IDX_MONITORID = 0;
    private static final Integer IDX_ACTION_AND_TIMEOUT = 1;

    protected static final String COMMAND_MONITORID = "monitorId";
    protected static final String COMMAND_ACTION = "action";
    private String[] commandParts;

    private String monitorId;
    private TelnetAction action;
    protected Integer timeout = null;

    public enum ZoneMinderRequestType {
        SERVER_LOW_PRIORITY_DATA,
        SERVER_HIGH_PRIORITY_DATA,

        MONITOR_THING,
        // Telnet
        MONITOR_EVENT,
        // Telnet
        MONITOR_TRIGGER;
    }

    public abstract ZoneMinderRequestType getRequestType();

    protected ZoneMinderMessage(String command) {
        commandParts = command.split(Pattern.quote(COMMAND_DELIMITER));
        parseCommand(commandParts);
    }

    protected ZoneMinderMessage(TelnetAction action, String monitorId, Integer timeout) {
        this.monitorId = monitorId;
        this.action = action;
        this.timeout = timeout;
    }

    public String getMonitorId() {
        return monitorId;
    }

    public String getZoneMinderId() {
        return monitorId;
    }

    protected TelnetAction getAction() {
        return action;
    }

    private void parseCommand(String[] commandParts) {

        String[] actionTimeoutParts = commandParts[IDX_ACTION_AND_TIMEOUT].split(Pattern.quote("+"));

        monitorId = commandParts[IDX_MONITORID];
        action = TelnetAction.getEnum(actionTimeoutParts[0]);

        if (actionTimeoutParts.length > 1) {
            timeout = Integer.parseInt(actionTimeoutParts[1]);
        }
        onParseCommand(commandParts);
    }

    protected String fixSpaces(String in) {

        if ((in != null) && (in.contains(" "))) {
            if (!in.startsWith("\"")) {
                in = String.format("\"%s", in);
            }
            if (!in.endsWith("\"")) {
                in = String.format("%s\"", in);
            }
        }
        return in;
    }

    protected abstract List<String> getCommandArray();

    protected abstract void onParseCommand(String[] commandParts);

    protected abstract String onBuildSubCommandString(String command);

    private String buildCommandString() {

        StringBuilder builder = new StringBuilder();
        for (String curCommand : getCommandArray()) {

            String commandValue = buildSubCommandString(curCommand);
            if (commandValue != null && !commandValue.isEmpty()) {
                if (builder.toString().length() > 0) {
                    builder.append(COMMAND_DELIMITER);
                }
                builder.append(commandValue);
            }
        }
        return builder.toString();
    }

    private String buildSubCommandString(String curCommand) {
        String strAction;
        switch (curCommand) {
            case COMMAND_MONITORID:
                return monitorId.toString();

            case COMMAND_ACTION:
                strAction = action.toString();
                if ((timeout != null) && (timeout > 0)) {
                    strAction += COMMAND_SUBDELIMITER + timeout.toString();
                }
                return strAction;
        }
        return onBuildSubCommandString(curCommand);
    }

    protected String convertToString(Integer val) {
        if (val == null) {
            return null;
        }

        return val.toString();
    }

    public String toCommandString() {
        return buildCommandString();
    }
}
