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
package org.openhab.binding.ahawastecollection.internal;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openhab.binding.ahawastecollection.internal.CollectionDate.WasteType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Schedule that returns the next collection dates from the aha website.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
final class AhaCollectionScheduleImpl implements AhaCollectionSchedule {

    private static final Pattern TIME_PATTERN = Pattern.compile("\\S\\S,\\s(\\d\\d.\\d\\d.\\d\\d\\d\\d)");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");
    private static final String WEBSITE_URL = "https://www.aha-region.de/abholtermine/abfuhrkalender/";

    private final Logger logger = LoggerFactory.getLogger(AhaCollectionScheduleImpl.class);
    private final String commune;
    private final String street;
    private final String houseNumber;
    private final String houseNumberAddon;
    private final String collectionPlace;

    /**
     * Creates an new {@link AhaCollectionScheduleImpl} for the given location.
     */
    public AhaCollectionScheduleImpl(final String commune, final String street, final String houseNumber,
            final String houseNumberAddon, final String collectionPlace) {
        this.commune = commune;
        this.street = street;
        this.houseNumber = houseNumber;
        this.houseNumberAddon = houseNumberAddon;
        this.collectionPlace = collectionPlace;
    }

    @Override
    public Map<WasteType, CollectionDate> getCollectionDates() throws IOException {
        final Document doc = Jsoup.connect(WEBSITE_URL) //
                .method(Method.POST) //
                .data("gemeinde", this.commune) //
                .data("strasse", this.street) //
                .data("hausnr", this.houseNumber) //
                .data("hausnraddon", this.houseNumberAddon) //
                .data("ladeort", this.collectionPlace) //
                .data("anzeigen", "Suchen") //
                .get();
        final Elements tableRows = doc.select("table").select("tr");
        if (tableRows.size() < 2) {
            this.logger.warn("No waste collection dates found.");
            return Collections.emptyMap();
        }

        // Skip first row, that contains the header
        // Than skip every second row, because it contains only the ical download buttons.
        final Map<WasteType, CollectionDate> result = new HashMap<>();
        for (int offset = 1; offset < tableRows.size(); offset += 2) {
            final Element row = tableRows.get(offset);
            final CollectionDate date = this.parseRow(row);
            if (date != null) {
                result.put(date.getType(), date);
            }
        }
        return result;
    }

    /**
     * Parses the {@link CollectionDate} from the given {@link Element table row}.
     *
     * @return The {@link CollectionDate} or <code>null</code> if no dates could be parsed.
     */
    private @Nullable CollectionDate parseRow(final Element row) {
        final Elements columns = row.select("td");
        if (columns.size() != 5) {
            this.logger.debug("Could not parse row: {}", row.toString());
            return null;
        }

        final WasteType wasteType = parseWasteType(columns.get(1));
        final List<Date> times = this.parseTimes(columns.get(3));

        if (times.isEmpty()) {
            return null;
        }

        return new CollectionDate(wasteType, times);
    }

    /**
     * Parses the waste types from the given {@link Element table cell}.
     */
    private static WasteType parseWasteType(final Element element) {
        String value = element.text().trim();
        final int firstSpace = value.indexOf(" ");
        if (firstSpace > 0) {
            value = value.substring(0, firstSpace);
        }
        return WasteType.parseValue(value);
    }

    /**
     * Parses the {@link CollectionDate} from the given {@link Element table cell}.
     */
    private List<Date> parseTimes(final Element element) {
        final List<Date> result = new ArrayList<>();
        final String value = element.text();
        final Matcher matcher = TIME_PATTERN.matcher(value);
        while (matcher.find()) {
            final String dateValue = matcher.group(1);
            try {
                result.add(DATE_FORMAT.parse(dateValue));
            } catch (final ParseException e) {
                this.logger.warn("Could not parse date: {}", dateValue);
            }
        }
        return result;
    }
}
