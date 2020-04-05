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
package org.openhab.binding.dwdpollenflug.internal.dto;

import static org.openhab.binding.dwdpollenflug.internal.DWDPollenflugBindingConstants.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * DTO for response of DWD request
 * 
 * @author Johannes DerOetzi Ott - Initial contribution
 */
@NonNullByDefault
public class DWDPollenflugJSON {
    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm";

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat(DATE_PATTERN);

    private String sender = EMPTY;

    private String name = EMPTY;

    @SerializedName("next_update")
    private @Nullable String nextUpdate;

    @SerializedName("last_update")
    private @Nullable String lastUpdate;

    private @Nullable Map<String, String> legend;

    @SerializedName("content")
    private @Nullable Set<DWDRegionJSON> regions;

    public String getName() {
        return name;
    }

    public String getSender() {
        return sender;
    }

    public @Nullable Set<DWDRegionJSON> getRegions() {
        return regions;
    }

    public @Nullable Date getNextUpdate() {
        return parseDate(nextUpdate);
    }

    public @Nullable Date getLastUpdate() {
        return parseDate(lastUpdate);
    }

    private synchronized @Nullable Date parseDate(@Nullable String date) {
        try {
            if (date == null) {
                return null;
            }

            return FORMATTER.parse(date.replace("Uhr", "").trim());
        } catch (ParseException e) {
            return null;
        }
    }

    public @Nullable Map<String, String> getLegend() {
        return Collections.unmodifiableMap(legend);
    }
}
