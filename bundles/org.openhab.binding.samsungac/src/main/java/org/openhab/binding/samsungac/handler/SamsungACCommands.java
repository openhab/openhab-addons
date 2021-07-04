/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.samsungac.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.Command;

/**
 *
 * The {@link SamsungACCommands} class defines the json commands for communication with Samsung Digital
 * Inverter
 *
 * @author Jan Grønlien - Initial contribution
 * @author Kai Kreuzer - Refactoring as preparation for openHAB contribution
 */

@NonNullByDefault
public class SamsungACCommands {

    public static JsonCommand createPowerCommand(Command command) {
        JsonCommand json = new JsonCommand();
        String state = "";
        switch (command.toString()) {
            case "OFF":
                state = "Off";
                break;
            case "ON":
                state = "On";
                break;
        }
        json.setPath("/devices/0");
        json.setJson(String.format("{\"Operation\": {\"power\": \"%s\"}}", state));
        return json;
    }

    public static JsonCommand createDesiredTemperatureCommand(Command command) {
        JsonCommand json = new JsonCommand();
        json.setPath("/devices/0/temperatures/0");
        json.setJson(String.format("{\"desired\": %s}", command.toString().split(" ")[0]));
        return json;
    }

    public static JsonCommand createDesiredWindSpeedCommand(Command command) {
        JsonCommand json = new JsonCommand();
        json.setPath("/devices/0/wind");
        json.setJson(String.format("{\"speedLevel\": %s}", command.toString()));
        return json;
    }

    public static JsonCommand createMaxWindSpeedCommand(Command command) {
        JsonCommand json = new JsonCommand();
        json.setPath("/devices/0/wind");
        json.setJson(String.format("{\"maxSpeedLevel\": %s}", command.toString()));
        return json;
    }

    public static JsonCommand createWindDirectionCommand(Command command) {
        JsonCommand json = new JsonCommand();
        json.setPath("/devices/0/wind");
        json.setJson(String.format("{\"direction\": \"%s\"}", command.toString()));
        return json;
    }

    public static JsonCommand createSetOperatingModeCommand(Command command) {
        JsonCommand json = new JsonCommand();
        json.setPath("/devices/0/mode");
        json.setJson(String.format("{\"modes\":[\"%s\"]}", command.toString()));
        return json;
    }

    public static JsonCommand createSetComodeCommand(Command command) {
        JsonCommand json = new JsonCommand();
        json.setPath("/devices/0/mode");
        json.setJson(String.format("{\"options\":[\"%s\"]}", command.toString()));
        return json;
    }

    public static JsonCommand createSetAutoCleanCommand(Command command) {
        JsonCommand json = new JsonCommand();
        json.setPath("/devices/0/mode");
        if ("ON".equals(command.toString())) {
            json.setJson("{\"options\":[\"Autoclean_On\"]}");
        } else {
            json.setJson("{\"options\":[\"Autoclean_Off\"]}");
        }
        return json;
    }

    public static JsonCommand createSetBeepCommand(Command command) {
        JsonCommand json = new JsonCommand();
        json.setPath("/devices/0/mode");
        if ("ON".equals(command.toString())) {
            json.setJson("{\"options\":[\"Volume_100\"]}");
        } else {
            json.setJson("{\"options\":[\"Volume_Mute\"]}");
        }
        return json;
    }

    public static JsonCommand createResetFilterCleanAlarm(Command command) {
        JsonCommand json = new JsonCommand();
        json.setPath("/devices/0/mode");
        json.setJson("{\"options\":[\"FilterCleanAlarm_1\"]}");
        return json;
    }
}
