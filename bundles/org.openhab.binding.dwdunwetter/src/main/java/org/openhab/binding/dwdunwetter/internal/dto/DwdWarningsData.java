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
package org.openhab.binding.dwdunwetter.internal.dto;

import java.io.StringReader;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Contains the Data for all retrieved warnings for one thing.
 *
 * @author Martin Koehler - Initial contribution
 */
public class DwdWarningsData {

    private static final int MIN_REFRESH_WAIT_MINUTES = 5;

    private final Logger logger = LoggerFactory.getLogger(DwdWarningsData.class);

    private List<DwdWarningData> cityData = new LinkedList<>();

    private DwdWarningCache cache = new DwdWarningCache();

    private ExpiringCache<String> dataAccessCached;

    private DateTimeFormatter formatter = new DateTimeFormatterBuilder()
            // date/time
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            // offset (hh:mm - "+00:00" when it's zero)
            .optionalStart().appendOffset("+HH:MM", "+00:00").optionalEnd()
            // offset (hhmm - "+0000" when it's zero)
            .optionalStart().appendOffset("+HHMM", "+0000").optionalEnd()
            // offset (hh - "Z" when it's zero)
            .optionalStart().appendOffset("+HH", "Z").optionalEnd()
            // create formatter
            .toFormatter();

    public DwdWarningsData(String cellId) {
        DwdWarningDataAccess dataAccess = new DwdWarningDataAccess();
        this.dataAccessCached = new ExpiringCache<>(Duration.ofMinutes(MIN_REFRESH_WAIT_MINUTES),
                () -> dataAccess.getDataFromEndpoint(cellId));
    }

    private String getValue(XMLEventReader eventReader) throws XMLStreamException {
        XMLEvent event = eventReader.nextEvent();
        return event.asCharacters().getData();
    }

    private BigDecimal getBigDecimalValue(XMLEventReader eventReader) throws XMLStreamException {
        XMLEvent event = eventReader.nextEvent();
        try {
            return new BigDecimal(event.asCharacters().getData());
        } catch (NumberFormatException e) {
            logger.debug("Exception while parsing a BigDecimal", e);
            return BigDecimal.ZERO;
        }
    }

    private Instant getTimestampValue(XMLEventReader eventReader) throws XMLStreamException {
        XMLEvent event = eventReader.nextEvent();
        String dateTimeString = event.asCharacters().getData();
        try {
            OffsetDateTime dateTime = OffsetDateTime.parse(dateTimeString, formatter);
            return dateTime.toInstant();
        } catch (DateTimeParseException e) {
            logger.debug("Exception while parsing a DateTime", e);
            return Instant.MIN;
        }
    }

    /**
     * Refreshes the Warnings Data
     */
    public boolean refresh() {
        String rawData = dataAccessCached.getValue();
        if (rawData == null || rawData.isEmpty()) {
            logger.debug("No Data from Endpoint");
            return false;
        }

        cityData.clear();

        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader reader = inputFactory.createXMLStreamReader(new StringReader(rawData));
            XMLEventReader eventReader = inputFactory.createXMLEventReader(reader);
            DwdWarningData gemeindeData = new DwdWarningData();
            boolean insideGemeinde = false;
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                if (!insideGemeinde && event.isStartElement()) {
                    DwdXmlTag xmlTag = DwdXmlTag.getDwdXmlTag(event.asStartElement().getName().getLocalPart());
                    switch (xmlTag) {
                        case WARNUNGEN_GEMEINDEN:
                            gemeindeData = new DwdWarningData();
                            insideGemeinde = true;
                            break;
                        default:
                            break;
                    }
                } else if (insideGemeinde && event.isStartElement()) {
                    DwdXmlTag xmlTag = DwdXmlTag.getDwdXmlTag(event.asStartElement().getName().getLocalPart());
                    switch (xmlTag) {
                        case SEVERITY:
                            gemeindeData.setSeverity(Severity.getSeverity(getValue(eventReader)));
                            break;
                        case DESCRIPTION:
                            gemeindeData.setDescription(getValue(eventReader));
                            break;
                        case EFFECTIVE:
                            gemeindeData.setEffective(getTimestampValue(eventReader));
                            break;
                        case EXPIRES:
                            gemeindeData.setExpires(getTimestampValue(eventReader));
                            break;
                        case EVENT:
                            gemeindeData.setEvent(getValue(eventReader));
                            break;
                        case STATUS:
                            gemeindeData.setStatus(getValue(eventReader));
                            break;
                        case MSGTYPE:
                            gemeindeData.setMsgType(getValue(eventReader));
                            break;
                        case HEADLINE:
                            gemeindeData.setHeadline(getValue(eventReader));
                            break;
                        case ONSET:
                            gemeindeData.setOnset(getTimestampValue(eventReader));
                            break;
                        case ALTITUDE:
                            gemeindeData.setAltitude(getBigDecimalValue(eventReader));
                            break;
                        case CEILING:
                            gemeindeData.setCeiling(getBigDecimalValue(eventReader));
                            break;
                        case IDENTIFIER:
                            gemeindeData.setId(getValue(eventReader));
                            break;
                        case INSTRUCTION:
                            gemeindeData.setInstruction(getValue(eventReader));
                            break;
                        case URGENCY:
                            gemeindeData.setUrgency(Urgency.getUrgency(getValue(eventReader)));
                            break;
                        default:
                            break;
                    }
                } else if (insideGemeinde && event.isEndElement()) {
                    DwdXmlTag xmlTag = DwdXmlTag.getDwdXmlTag(event.asEndElement().getName().getLocalPart());
                    switch (xmlTag) {
                        case WARNUNGEN_GEMEINDEN:
                            if (!gemeindeData.isTest() && !gemeindeData.isCancel()) {
                                cityData.add(gemeindeData);
                            }
                            insideGemeinde = false;
                            break;
                        default:
                            break;
                    }
                }
            }
        } catch (XMLStreamException e) {
            logger.warn("Exception occurred while parsing the XML response: {}", e.getMessage());
            logger.debug("Exception trace", e);
            return false;
        }

        Collections.sort(cityData, new SeverityComparator());
        return true;
    }

    private DwdWarningData getGemeindeData(int number) {
        return cityData.size() <= number ? null : cityData.get(number);
    }

    public State getWarning(int number) {
        DwdWarningData data = getGemeindeData(number);
        return data == null ? OnOffType.OFF : OnOffType.ON;
    }

    public State getSeverity(int number) {
        DwdWarningData data = getGemeindeData(number);
        return data == null ? UnDefType.UNDEF : StringType.valueOf(data.getSeverity().getText());
    }

    public State getDescription(int number) {
        DwdWarningData data = getGemeindeData(number);
        return data == null ? UnDefType.UNDEF : StringType.valueOf(data.getDescription());
    }

    public State getEffective(int number) {
        DwdWarningData data = getGemeindeData(number);
        if (data == null) {
            return UnDefType.UNDEF;
        }
        ZonedDateTime zoned = ZonedDateTime.ofInstant(data.getEffective(), ZoneId.systemDefault());
        return new DateTimeType(zoned);
    }

    public State getExpires(int number) {
        DwdWarningData data = getGemeindeData(number);
        if (data == null) {
            return UnDefType.UNDEF;
        }
        ZonedDateTime zoned = ZonedDateTime.ofInstant(data.getExpires(), ZoneId.systemDefault());
        return new DateTimeType(zoned);
    }

    public State getOnset(int number) {
        DwdWarningData data = getGemeindeData(number);
        if (data == null) {
            return UnDefType.UNDEF;
        }
        ZonedDateTime zoned = ZonedDateTime.ofInstant(data.getOnset(), ZoneId.systemDefault());
        return new DateTimeType(zoned);
    }

    public State getEvent(int number) {
        DwdWarningData data = getGemeindeData(number);
        return data == null ? UnDefType.UNDEF : StringType.valueOf(data.getEvent());
    }

    public State getHeadline(int number) {
        DwdWarningData data = getGemeindeData(number);
        return data == null ? UnDefType.UNDEF : StringType.valueOf(data.getHeadline());
    }

    public State getAltitude(int number) {
        DwdWarningData data = getGemeindeData(number);
        if (data == null) {
            return UnDefType.UNDEF;
        }
        return new QuantityType<>(data.getAltitude(), ImperialUnits.FOOT);
    }

    public State getCeiling(int number) {
        DwdWarningData data = getGemeindeData(number);
        if (data == null) {
            return UnDefType.UNDEF;
        }
        return new QuantityType<>(data.getCeiling(), ImperialUnits.FOOT);
    }

    public State getInstruction(int number) {
        DwdWarningData data = getGemeindeData(number);
        return data == null ? UnDefType.UNDEF : StringType.valueOf(data.getInstruction());
    }

    public State getUrgency(int number) {
        DwdWarningData data = getGemeindeData(number);
        return data == null ? UnDefType.UNDEF : StringType.valueOf(data.getUrgency().getText());
    }

    public boolean isNew(int number) {
        DwdWarningData data = getGemeindeData(number);
        if (data == null) {
            return false;
        }
        return cache.addEntry(data);
    }

    public void updateCache() {
        cache.deleteOldEntries();
    }

    /**
     * Only for Tests
     */
    protected void setDataAccess(DwdWarningDataAccess dataAccess) {
        dataAccessCached = new ExpiringCache<>(Duration.ofMinutes(MIN_REFRESH_WAIT_MINUTES),
                () -> dataAccess.getDataFromEndpoint(""));
    }
}
