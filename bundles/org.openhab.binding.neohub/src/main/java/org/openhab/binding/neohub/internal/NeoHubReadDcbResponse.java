/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.neohub.internal;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.unit.ImperialUnits;
import org.eclipse.smarthome.core.library.unit.SIUnits;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

/**
 * A wrapper around the JSON response to the JSON READ_DCB 100 request
 *
 * @author Andrew Fiddian-Green - Initial contribution
 * 
 */
public class NeoHubReadDcbResponse {

    private static final Gson GSON = new Gson();

    @SerializedName("CORF")
    private String degreesCorF;

    public Unit<?> getTemperatureUnit() {
        return "F".equalsIgnoreCase(degreesCorF) ? ImperialUnits.FAHRENHEIT : SIUnits.CELSIUS;
    }

    /**
     * Create wrapper around the JSON response
     * 
     * @param response the JSON INFO request
     * @return a NeoHubReadDcbResponse wrapper around the JSON response
     * @throws JsonSyntaxException
     * 
     */
    public static @Nullable NeoHubReadDcbResponse createReadDcbResponse(String response) throws JsonSyntaxException {
        return GSON.fromJson(response, NeoHubReadDcbResponse.class);
    }
}
