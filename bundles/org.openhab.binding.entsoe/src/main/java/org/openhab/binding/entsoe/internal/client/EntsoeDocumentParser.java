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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class EntsoeDocumentParser {
    private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private final Logger logger = LoggerFactory.getLogger(EntsoeDocumentParser.class);
    private final TreeMap<String, String> sequenceDurationMap = new TreeMap<>();
    private final TreeMap<String, TreeMap<Instant, SpotPrice>> sequencePriceMap = new TreeMap<>();
    private @Nullable String failureReason;
    private @Nullable Document document;

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

    public boolean isValid() {
        return failureReason == null;
    }

    public @Nullable String getFailureReason() {
        return failureReason;
    }

    public TreeMap<String, String> getSequences() {
        return sequenceDurationMap;
    }

    public TreeMap<Instant, SpotPrice> getPriceMap(String sequenceNumber) {
        TreeMap<Instant, SpotPrice> returnMap = sequencePriceMap.get(sequenceNumber);
        if (returnMap == null) {
            return new TreeMap<>();
        } else {
            return returnMap;
        }
    }

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

    private @Nullable Document parseDocument(String response)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(new InputSource(new StringReader(response)));
        document.getDocumentElement().normalize();
        return document;
    }

    private Instant calculateDateTime(Instant start, int iteration, String resolution) {
        Duration d = Duration.parse(resolution).multipliedBy(iteration);
        return start.plus(d);
    }
}
