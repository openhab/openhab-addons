/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.teslapowerwall.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Class for holding the set of parameters used to read the battery soe.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class BatterySOE {
    public double soe;

    private BatterySOE() {
    }

    public static BatterySOE parse(String response) {
        /* parse json string */
        JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
        BatterySOE info = new BatterySOE();
        info.soe = jsonObject.get("percentage").getAsDouble();
        return info;
    }
}
