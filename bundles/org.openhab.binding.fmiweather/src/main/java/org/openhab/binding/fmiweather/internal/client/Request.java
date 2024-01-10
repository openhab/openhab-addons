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
package org.openhab.binding.fmiweather.internal.client;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Request class for FIM weather
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class Request {

    public static final String FMI_WFS_URL = "https://opendata.fmi.fi/wfs";

    public final QueryParameter location;
    public final long startEpoch;
    public final long endEpoch;
    public final long timestepMinutes;
    public final String storedQueryId;
    public final String[] parameters;

    private static ZoneId UTC = ZoneId.of("Z");
    private static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public Request(String storedQueryId, QueryParameter location, long startEpoch, long endEpoch,
            long timestepMinutes) {
        this(storedQueryId, location, startEpoch, endEpoch, timestepMinutes, new String[0]);
    }

    public Request(String storedQueryId, QueryParameter location, long startEpoch, long endEpoch, long timestepMinutes,
            String[] parameters) {
        this.storedQueryId = storedQueryId;
        this.location = location;
        this.startEpoch = startEpoch;
        this.endEpoch = endEpoch;
        this.timestepMinutes = timestepMinutes;
        this.parameters = parameters;
    }

    public String toUrl() {
        StringBuilder urlBuilder = new StringBuilder(FMI_WFS_URL)
                .append("?service=WFS&version=2.0.0&request=getFeature&storedquery_id=").append(storedQueryId)
                .append("&starttime=").append(epochToIsoDateTime(startEpoch)).append("&endtime=")
                .append(epochToIsoDateTime(endEpoch)).append("&timestep=").append(timestepMinutes);
        location.toRequestParameters().forEach(entry -> {
            urlBuilder.append("&").append(entry.getKey()).append("=").append(entry.getValue());
        });
        if (parameters.length > 0) {
            urlBuilder.append("&").append("parameters=").append(String.join(",", parameters));
        }
        return urlBuilder.toString();
    }

    /**
     * Convert epoch value (representing UTC time) to ISO formatted date time
     *
     * @param epoch
     * @return
     */
    private static String epochToIsoDateTime(long epoch) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(epoch), UTC).format(FORMATTER);
    }
}
