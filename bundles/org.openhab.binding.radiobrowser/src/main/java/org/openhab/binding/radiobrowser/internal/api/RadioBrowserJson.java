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
package org.openhab.binding.radiobrowser.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link RadioBrowserJson} DTO holds the state and GSON parsed replies from the Radio Stations API.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class RadioBrowserJson {
    public class Country {
        public String name = "";
        @SerializedName(value = "countryCode", alternate = { "iso_3166_1" }) // iso_3166_1 is used in json
        public String countryCode = "";
        public int stationcount;
    }

    public class Language {
        public String name = "";
        public int stationcount;
    }

    public class Station {
        public String name = "";
        public String stationuuid = "";
        public String url = "";
        public String favicon = "";
    }

    public class State {
        public String name = "";
        public int stationcount;
    }
}
