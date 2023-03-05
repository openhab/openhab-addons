/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.Iterator;
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
     * Creates a new {@link AhaCollectionScheduleImpl} for the given location.
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

        final Elements table = doc.select("table");

        if (table.size() == 0) {
            logger.warn("No result table found.");
            return Collections.emptyMap();
        }

        final Iterator<Element> rowIt = table.get(0).getElementsByTag("tr").iterator();
        final Map<WasteType, CollectionDate> result = new HashMap<>();

        while (rowIt.hasNext()) {
            final Element currentRow = rowIt.next();
            if (!currentRow.tagName().equals("tr")) {
                continue;
            }
            // Skip header, empty and download button rows.
            if (isHeader(currentRow) || isDelimiterOrDownloadRow(currentRow)) {
                continue;
            }

            // If no following row is present, no collection dates can be parsed
            if (!rowIt.hasNext()) {
                logger.warn("No row with collection dates found.");
                break;
            }
            final Element collectionDatesRow = rowIt.next();

            final CollectionDate date = this.parseRows(currentRow, collectionDatesRow);
            if (date != null) {
                result.put(date.getType(), date);
            }
        }
        return result;
    }

    /**
     * Parses the row with the waste type and the following row with the collection dates.
     * 
     * @param wasteTypeRow Row that contains the waste type information
     * @param collectionDatesRow Row that contains the collection date informations.
     * @return The parsed {@link CollectionDate} or <code>null</code> if information could not be parsed.
     */
    @Nullable
    private CollectionDate parseRows(Element wasteTypeRow, Element collectionDatesRow) {
        // Try to extract the waste Type from the first row
        final Elements wasteTypeElement = wasteTypeRow.select("td").select("strong");
        if (wasteTypeElement.size() != 1) {
            this.logger.warn("Could not parse waste type row: {}", wasteTypeRow.toString());
            return null;
        }
        final WasteType wasteType = parseWasteType(wasteTypeElement.get(0));

        // Try to extract the collection dates from the second row
        final Elements collectionDatesColumns = collectionDatesRow.select("td");
        if (collectionDatesColumns.size() != 3) {
            this.logger.warn("collection dates row could not be parsed.");
            return null;
        }

        final Element collectionDatesColumn = collectionDatesColumns.get(1);
        final List<Date> collectionDates = parseTimes(collectionDatesColumn);

        if (!collectionDates.isEmpty()) {
            return new CollectionDate(wasteType, collectionDates);
        } else {
            return null;
        }
    }

    /**
     * Returns <code>true</code> if the row is an (empty) delimiter row or if its a row that contains the download
     * buttons for ical.
     */
    private boolean isDelimiterOrDownloadRow(Element currentRow) {
        final Elements columns = currentRow.select("td");
        return columns.size() == 1 && columns.get(0).text().isBlank() || !columns.select("form").isEmpty();
    }

    private boolean isHeader(Element currentRow) {
        return !currentRow.select("th").isEmpty();
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
                synchronized (DATE_FORMAT) {
                    result.add(DATE_FORMAT.parse(dateValue));
                }
            } catch (final ParseException e) {
                this.logger.warn("Could not parse date: {}", dateValue);
            }
        }
        return result;
    }
}
