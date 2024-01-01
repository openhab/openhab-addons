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
package org.openhab.binding.dwdpollenflug.internal.dto;

import static org.openhab.binding.dwdpollenflug.internal.DWDPollenflugBindingConstants.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link DWDPollenflug} class is internal DWD data structure.
 *
 * @author Johannes Ott - Initial contribution
 */
@NonNullByDefault
public class DWDPollenflug {
    private String sender = "";

    private String name = "";

    private final Date created = new Date();

    @SerializedName("next_update")
    private @Nullable String nextUpdate;

    @SerializedName("last_update")
    private @Nullable String lastUpdate;

    @SerializedName("content")
    private @Nullable Set<DWDRegion> regions;

    public Map<String, String> getProperties() {
        Map<String, String> map = new HashMap<>();

        map.put(PROPERTY_NAME, name);
        map.put(PROPERTY_SENDER, sender);

        return Collections.unmodifiableMap(map);
    }

    public Map<String, State> getChannelsStateMap() {
        Map<String, State> map = new HashMap<>();

        map.put(CHANNEL_UPDATES + "#" + CHANNEL_REFRESHED, parseDate(created));
        map.put(CHANNEL_UPDATES + "#" + CHANNEL_LAST_UPDATE, parseDate(lastUpdate));
        map.put(CHANNEL_UPDATES + "#" + CHANNEL_NEXT_UPDATE, parseDate(nextUpdate));

        return Collections.unmodifiableMap(map);
    }

    private State parseDate(final @Nullable String dateString) {
        try {
            if (dateString != null) {
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                Date date = formatter.parse(dateString.replace("Uhr", "").trim());
                return parseDate(date);
            }

            return UnDefType.NULL;
        } catch (ParseException e) {
            return UnDefType.NULL;
        }
    }

    private State parseDate(final @Nullable Date date) {
        if (date == null) {
            return UnDefType.NULL;
        } else {
            ZonedDateTime zoned = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            return new DateTimeType(zoned);
        }
    }

    public @Nullable DWDRegion getRegion(int key) {
        final Set<DWDRegion> localRegions = regions;
        if (localRegions != null) {
            for (DWDRegion region : localRegions) {
                if (region.getRegionID() == key) {
                    return region;
                }
            }
        }

        return null;
    }
}
