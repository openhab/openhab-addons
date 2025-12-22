/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.entsoe.internal.client;

import java.io.IOException;
import java.io.StringReader;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.entsoe.internal.exception.EntsoeResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class EntsoeDocumentParser implements ErrorHandler {
    private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private final Logger logger = LoggerFactory.getLogger(EntsoeDocumentParser.class);
    private final TreeMap<String, String> sequenceDurationMap = new TreeMap<>();
    private final TreeMap<String, TreeMap<Instant, SpotPrice>> sequencePriceMap = new TreeMap<>();
    private @Nullable String failureReason;
    private @Nullable Document document;

    public EntsoeDocumentParser() {
        failureReason = " Parser not initialized with response";
    }

    /**
     * Constructor. Parses the given XML response string.
     * Errors during parsing are captured and can be retrieved via {@link #isValid()} and {@link #getFailureReason()}.
     *
     * @param response The XML response string from EntsoE
     */
    public EntsoeDocumentParser(String response) {
        try {
            Document workingDocument = parseDocument(response);
            document = workingDocument;
            if (workingDocument != null) {
                if (workingDocument.getDocumentElement().getNodeName().equals("Acknowledgement_MarketDocument")) {
                    NodeList reasonOfRejection = workingDocument.getElementsByTagName("Reason");
                    Node reasonNode = reasonOfRejection.item(0);
                    Element reasonElement = (Element) reasonNode;
                    String reasonCode = reasonElement.getElementsByTagName("code").item(0).getTextContent();
                    String reasonText = reasonElement.getElementsByTagName("text").item(0).getTextContent();
                    failureReason = reasonCode + " - " + reasonText;
                } else {
                    parseXmlResponse(response);
                    if (sequencePriceMap.isEmpty()) {
                        failureReason = "No time series data found in response";
                    }
                }
            } else {
                failureReason = "Parsed document is null";
            }
            // in case of parsing errors log response at trace level
            if (!isValid()) {
                logger.trace("Error reading document from EntsoE: {} \n {}", failureReason, response);
            }
        } catch (EntsoeResponseException | ParserConfigurationException | SAXException | IOException e) {
            failureReason = e.getMessage();
        }
    }

    /**
     * Check if the parsing was successful.
     *
     * @return true if parsing was successful, false otherwise
     */
    public boolean isValid() {
        return failureReason == null;
    }

    /**
     * Get the failure reason if parsing was not successful.
     *
     * @return The failure reason, or null if parsing was successful
     */
    public @Nullable String getFailureReason() {
        return failureReason;
    }

    /**
     * Get the map of sequence numbers to their corresponding durations.
     *
     * @return A TreeMap with sequence numbers as keys and durations as values
     */
    public TreeMap<String, String> getSequences() {
        return sequenceDurationMap;
    }

    /**
     * Get the price map for a given sequence number.
     *
     * @param sequenceNumber The sequence number to retrieve the price map for
     * @return A TreeMap with Instants as keys and SpotPrices as values
     */
    public TreeMap<Instant, SpotPrice> getPriceMap(String sequenceNumber) {
        TreeMap<Instant, SpotPrice> returnMap = sequencePriceMap.get(sequenceNumber);
        if (returnMap == null) {
            return new TreeMap<>();
        } else {
            return returnMap;
        }
    }

    /**
     * Transform the price map of one sequence to a different resolution by averaging prices over the new time windows.
     *
     * @param sequenceNumber The sequence number to transform
     * @param targetResolution The target resolution in ISO 8601 duration format (e.g. "PT15M" for 15 minutes)
     * @return A TreeMap with the transformed prices
     * @throws EntsoeResponseException
     */
    public TreeMap<Instant, SpotPrice> transform(String sequenceNumber, Duration targetDuration)
            throws EntsoeResponseException {
        TreeMap<Instant, SpotPrice> priceMap = getPriceMap(sequenceNumber);
        TreeMap<Instant, SpotPrice> targetMap = new TreeMap<>();
        logger.debug("Transforming Duration {} to {}", getSequences().get(sequenceNumber), targetDuration);
        String duration = sequenceDurationMap.get(sequenceNumber);
        if (duration == null) {
            throw new EntsoeResponseException("No duration found for sequence " + sequenceNumber);
        }
        Instant timeWindowStart = priceMap.firstKey();
        Instant endTime = priceMap.lastKey().plus(Duration.parse(duration));
        SpotPrice investigationPrice = priceMap.firstEntry().getValue();

        while (timeWindowStart.isBefore(endTime)) {
            Instant timeWindowEnd = timeWindowStart.plus(targetDuration.toMinutes(), ChronoUnit.MINUTES);
            SortedMap<Instant, SpotPrice> subMap = priceMap.subMap(timeWindowStart, timeWindowEnd);
            SpotPrice averagePrice = average(timeWindowStart, timeWindowEnd, subMap, investigationPrice);
            targetMap.put(timeWindowStart, averagePrice);
            // shift time window and get starting price for next window
            timeWindowStart = timeWindowEnd;
            investigationPrice = priceMap.floorEntry(timeWindowStart).getValue();
        }
        return targetMap;
    }

    /**
     * Calculate the average price time weighted over the given time window.
     *
     * @param timeWindowStartTime The start time of the time window
     * @param timeWindowEnd The end time of the time window
     * @param subMap The sub map of prices within the time window
     * @param investigationPrice The price to use for periods without a price change
     * @return The average SpotPrice over the time window
     * @throws EntsoeResponseException
     */
    private SpotPrice average(Instant timeWindowStartTime, Instant timeWindowEnd, SortedMap<Instant, SpotPrice> subMap,
            SpotPrice investigationPrice) throws EntsoeResponseException {
        double investigationPriceDouble = investigationPrice.getPrice();
        Instant loopIterator = timeWindowStartTime;
        double averagePrice = 0.0;
        for (Map.Entry<Instant, SpotPrice> entry : subMap.entrySet()) {
            Instant step = entry.getKey();
            long durationMinutes = Duration.between(loopIterator, step).toMinutes();
            averagePrice += investigationPriceDouble * durationMinutes;
            investigationPriceDouble = entry.getValue().getPrice();
            loopIterator = step;
        }
        // add last segment
        long durationMinutes = Duration.between(loopIterator, timeWindowEnd).toMinutes();
        averagePrice += investigationPriceDouble * durationMinutes;
        averagePrice = averagePrice / Duration.between(timeWindowStartTime, timeWindowEnd).toMinutes();
        return new SpotPrice(investigationPrice.getCurrency(), "kWh", averagePrice);
    }

    /**
     * Parse the XML response and populate the sequenceDurationMap and sequencePriceMap.
     *
     * @param responseText The XML response text
     * @throws EntsoeResponseException If there is an error during parsing
     */
    private void parseXmlResponse(String responseText) throws EntsoeResponseException {
        // Get all "timeSeries" nodes from document
        Document workingDocument = document;
        if (workingDocument == null) {
            throw new EntsoeResponseException("Working document is null");
        }
        NodeList listOfTimeSeries = workingDocument.getElementsByTagName("TimeSeries");
        for (int i = 0; i < listOfTimeSeries.getLength(); i++) {
            Node timeSeriesNode = listOfTimeSeries.item(i);
            if (timeSeriesNode.getNodeType() == Node.ELEMENT_NODE) {
                Element timeSeriesElement = (Element) timeSeriesNode;

                /**
                 * SEQUENCES:
                 * response document can have
                 * - no classificationSequence_AttributeInstanceComponent (position) -> default to 0
                 * - 1 to n classificationSequence_AttributeInstanceComponent (position)
                 */
                Node sequenceNode = timeSeriesElement
                        .getElementsByTagName("classificationSequence_AttributeInstanceComponent.position").item(0);
                String sequenceNumber = "0";
                if (sequenceNode != null) {
                    sequenceNumber = sequenceNode.getTextContent();
                }

                // Evaluate currency and measure unit
                String currency = timeSeriesElement.getElementsByTagName("currency_Unit.name").item(0).getTextContent();
                String measureUnit = timeSeriesElement.getElementsByTagName("price_Measure_Unit.name").item(0)
                        .getTextContent();

                // get start time and resolution from period element
                NodeList listOfPeriod = timeSeriesElement.getElementsByTagName("Period");
                Node periodNode = listOfPeriod.item(0);
                Element periodElement = (Element) periodNode;
                String resolution = periodElement.getElementsByTagName("resolution").item(0).getTextContent();
                NodeList listOfTimeInterval = periodElement.getElementsByTagName("timeInterval");
                Node startTimeNode = listOfTimeInterval.item(0);
                Element startTimeElement = (Element) startTimeNode;
                String startTime = startTimeElement.getElementsByTagName("start").item(0).getTextContent();
                Instant startTimeInstant = ZonedDateTime.parse(startTime, DateTimeFormatter.ISO_ZONED_DATE_TIME)
                        .toInstant();

                // now we have sequence number and resolution so we can store it
                sequenceDurationMap.put(sequenceNumber, resolution);
                TreeMap<Instant, SpotPrice> responseMap = sequencePriceMap.get(sequenceNumber);
                if (responseMap == null) {
                    responseMap = new TreeMap<>();
                    sequencePriceMap.put(sequenceNumber, responseMap);
                }

                /*
                 * EntsoE changed their API on October 1 2024 so that they use the A03 curve type instead of A01. The
                 * difference between these curve types is that in A03 they don’t repeat an hour if it has the same
                 * price as the previous hour.
                 * No problem for timeseries if a state lasts more than time resolution
                 */
                NodeList listOfPoints = periodElement.getElementsByTagName("Point");
                for (int j = 0; j < listOfPoints.getLength(); j++) {
                    Node pointNode = listOfPoints.item(j);
                    Element pointElement = (Element) pointNode;
                    Node positionNode = pointElement.getElementsByTagName("position").item(0);
                    Node priceNode = pointElement.getElementsByTagName("price.amount").item(0);
                    if (positionNode != null && priceNode != null) {
                        int position = Integer.parseInt(positionNode.getTextContent()) - 1;
                        double price = Double.parseDouble(priceNode.getTextContent());
                        SpotPrice spotPrice = new SpotPrice(currency, measureUnit, price);
                        responseMap.put(calculateDateTime(startTimeInstant, position, resolution), spotPrice);
                    } else {
                        logger.warn("Missing position or price node in point element: {}", pointElement);
                    }
                }
            }
        }
    }

    /**
     * Parse the XML response string into a Document object.
     *
     * @param response The XML response string
     * @return The parsed Document object
     * @throws ParserConfigurationException If a DocumentBuilder cannot be created
     * @throws SAXException If any parse errors occur
     * @throws IOException If any IO errors occur
     */
    private @Nullable Document parseDocument(String response)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        documentBuilder.setErrorHandler(this);
        Document document = documentBuilder.parse(new InputSource(new StringReader(response)));
        document.getDocumentElement().normalize();
        return document;
    }

    /**
     * Calculate the date and time for a given iteration based on the start time and resolution.
     *
     * @param start The start time as an Instant
     * @param iteration The iteration number (0-based)
     * @param resolution The resolution in ISO 8601 duration format
     * @return The calculated Instant
     */
    private Instant calculateDateTime(Instant start, int iteration, String resolution) {
        Duration d = Duration.parse(resolution).multipliedBy(iteration);
        return start.plus(d);
    }

    @Override
    public void warning(@Nullable SAXParseException exception) throws SAXException {
        throwParserExcpetion("Unknown warning", exception);
    }

    @Override
    public void error(@Nullable SAXParseException exception) throws SAXException {
        throwParserExcpetion("Unknown error", exception);
    }

    @Override
    public void fatalError(@Nullable SAXParseException exception) throws SAXException {
        throwParserExcpetion("Unknown fatal error", exception);
    }

    private void throwParserExcpetion(String message, @Nullable SAXParseException exception) throws SAXException {
        if (exception != null) {
            throw exception;
        } else {
            throw new SAXException(message);
        }
    }
}
