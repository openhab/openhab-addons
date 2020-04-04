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

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * The {@link DWDPollenflug} class is internal DWD data structure.
 *
 * @author Johannes DerOetzi Ott - Initial contribution
 */
@NonNullByDefault
public class DWDPollenflug {
    private final static SimpleDateFormat FORMATTER = new SimpleDateFormat(DATE_PATTERN);

    private final Date CREATED = new Date();

    private final @Nullable Date nextUpdate;

    private final @Nullable Date lastUpdate;

    private final Map<String, String> properties;

    private final Map<String, State> channels = new HashMap<>();

    private final Map<Integer, DWDRegion> regions = new HashMap<>();

    public DWDPollenflug(DWDPollenflugJSON json) {
        nextUpdate = json.getNextUpdate();
        lastUpdate = json.getLastUpdate();

        properties = initProperties(json);

        for (DWDRegionJSON regionJSON : json.getRegions()) {
            DWDRegion region = new DWDRegion(regionJSON);
            regions.put(region.getRegionID(), region);
        }

        parseChannels(json);
    }

    private Map<String, String> initProperties(DWDPollenflugJSON json) {
        Map<String, String> map = new HashMap<>();

        map.put(PROPERTY_NAME, json.getName());
        map.put(PROPERTY_SENDER, json.getSender());
        map.put(PROPERTY_REFRESHED, FORMATTER.format(CREATED));

        if (nextUpdate != null) {
            map.put(PROPERTY_NEXT_UPDATE, FORMATTER.format(nextUpdate));
        }

        if (lastUpdate != null) {
            map.put(PROPERTY_LAST_UPDATE, FORMATTER.format(lastUpdate));
        }

        return Collections.unmodifiableMap(map);
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    private void parseChannels(DWDPollenflugJSON json) {
        createChannel(CHANNEL_REFRESHED, CREATED);
        createChannel(CHANNEL_NEXT_UPDATE, nextUpdate);
        createChannel(CHANNEL_LAST_UPDATE, lastUpdate);
    }

    private void createChannel(String subchannel, @Nullable Date date) {
        final String channelName = CHANNEL_UPDATES + "#" + subchannel;
        if (date == null) {
            channels.put(channelName, UnDefType.NULL);
        } else {
            ZonedDateTime zoned = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
            channels.put(channelName, new DateTimeType(zoned));
        }
    }

    public Map<String, State> getChannels() {
        return Collections.unmodifiableMap(channels);
    }

    public @Nullable Date getLastUpdate() {
        return lastUpdate;
    }

    public @Nullable DWDRegion getRegion(int regionID) {
        return regions.get(regionID);
    }
}