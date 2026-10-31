/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.candy.internal.protocol;

import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Serializes normal Bianca commands using the Android application's wire names and defaults.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
@NonNullByDefault
public final class BiancaCommands {
    private BiancaCommands() {
    }

    public static String start(String name, Map<String, String> parameters) throws CandyProtocolException {
        String identifier = name.toUpperCase(Locale.ROOT);
        if (!name.matches("[A-Za-z0-9 _'()./-]{1,96}") || identifier.contains("MAINTENANCE")
                || identifier.contains("AUTOCLEAN") || identifier.contains("TUMBLING")) {
            throw new CandyProtocolException("@text/command-error-program");
        }
        int selector = number(parameters, "selector_position", 1, 255);
        int code = number(parameters, "pr_code", 1, 255);
        int temperature = number(parameters, "default_temperature", 0, 255);
        int spin = number(parameters, "default_spin_speed", 0, 2000);
        int minimumSoil = number(parameters, "minimum_soil_level", 0, 255);
        int maximumSoil = number(parameters, "maximum_soil_level", minimumSoil, 255);
        int soil = number(parameters, "default_soil_level", 0, 255);
        soil = soil >= minimumSoil && soil <= maximumSoil ? soil : minimumSoil;
        int steam = number(parameters, "steam", 0, 255);
        number(parameters, "dry", 0, 0);
        if ((temperature > 90 && temperature != 255) || (spin != 255 && spin % 100 != 0)) {
            throw new CandyProtocolException("@text/command-error-program");
        }
        StringBuilder body = new StringBuilder("Write=1&StSt=1&DelVl=0&PrNm=").append(selector).append("&PrCode=")
                .append(code).append("&PrStr=").append(name);
        if (temperature != 255) {
            body.append("&TmpTgt=").append(temperature);
        }
        // A 255 spin default becomes zero in Command.setProgram; masks start with no optional features selected.
        body.append("&SLevTgt=").append(soil == 255 ? 0 : soil).append("&SpdTgt=").append(spin == 255 ? 0 : spin / 100)
                .append("&OptMsk1=0&OptMsk2=0&Lang=1&Stm=").append(steam == 4 ? 1 : 0)
                .append("&Dry=0&ED=0&RecipeId=0&StartCheckUp=0&DispTestOn=1");
        return body.toString();
    }

    public static String stop(String programNumber, boolean delayed) throws CandyProtocolException {
        if (!programNumber.isEmpty()
                && (!programNumber.matches("[0-9]{1,3}") || Integer.parseInt(programNumber) > 255)) {
            throw new CandyProtocolException("@text/command-error-program");
        }
        return "Write=1&StSt=0" + (programNumber.isEmpty() ? "" : "&PrNm=" + programNumber)
                + (delayed ? "&DelVl=0" : "");
    }

    private static int number(Map<String, String> parameters, String name, int minimum, int maximum)
            throws CandyProtocolException {
        try {
            int value = Integer.parseInt(parameters.getOrDefault(name, ""));
            if (value >= minimum && value <= maximum) {
                return value;
            }
        } catch (NumberFormatException e) {
            // Missing and malformed defaults must not silently select another washing program.
        }
        throw new CandyProtocolException("@text/command-error-program");
    }
}
