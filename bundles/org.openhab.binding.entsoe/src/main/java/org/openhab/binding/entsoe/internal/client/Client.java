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
package org.openhab.binding.entsoe.internal.client;

import java.io.IOException;
import java.io.StringReader;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.entsoe.internal.exception.entsoeConfigurationException;
import org.openhab.binding.entsoe.internal.exception.entsoeResponseException;
import org.openhab.binding.entsoe.internal.exception.entsoeUnexpectedException;
import org.openhab.core.io.net.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author Miika Jukka - Initial contribution
 *
 */
@NonNullByDefault
public class Client {

    private final Logger logger = LoggerFactory.getLogger(Client.class);

    private DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    // private @Nullable DocumentBuilder documentBuilder;

    private Map<ZonedDateTime, Double> responseMap = new TreeMap<>();

    public Map<ZonedDateTime, Double> doGetRequest(Request request, int timeout)
            throws entsoeResponseException, entsoeUnexpectedException, entsoeConfigurationException {
        try {
            logger.debug("Sending GET request with parameters: {}", request);
            String url = request.toUrl();
            String responseText = HttpUtil.executeUrl("GET", url, timeout);
            if (responseText == null) {
                logger.error("GET request failed and returned null for request url: {}", url);
                throw new entsoeResponseException("Request failed");
            }
            logger.debug("{}", responseText);
            return parseXmlResponse(responseText);
        } catch (IOException e) {
            if (e.getMessage().contains("Authentication challenge without WWW-Authenticate header")) {
                throw new entsoeConfigurationException("Authentication failed. Please check your security token");
            }
            throw new entsoeResponseException(e);
        } catch (ParserConfigurationException | SAXException e) {
            throw new entsoeUnexpectedException(e);
        }
    }

    private Map<ZonedDateTime, Double> parseXmlResponse(String responseText) throws ParserConfigurationException,
            SAXException, IOException, entsoeResponseException, entsoeUnexpectedException {

        responseMap.clear();

        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(new InputSource(new StringReader(responseText)));
        document.getDocumentElement().normalize();

        // Check for rejection
        if (document.getDocumentElement().getNodeName().equals("Acknowledgement_MarketDocument")) {
            NodeList reasonOfRejection = document.getElementsByTagName("Reason");
            Node reasonNode = reasonOfRejection.item(0);
            Element reasonElement = (Element) reasonNode;
            String reasonCode = reasonElement.getElementsByTagName("code").item(0).getTextContent();
            String reasonText = reasonElement.getElementsByTagName("text").item(0).getTextContent();
            throw new entsoeResponseException(
                    String.format("Request failed with API response: Code %s, Text %s", reasonCode, reasonText));
        }

        // Get all "timeSeries" nodes from document
        NodeList listOfTimeSeries = document.getElementsByTagName("TimeSeries");
        for (int i = 0; i < listOfTimeSeries.getLength(); i++) {
            Node timeSeriesNode = listOfTimeSeries.item(i);
            if (timeSeriesNode.getNodeType() == Node.ELEMENT_NODE) {
                Element timeSeriesElement = (Element) timeSeriesNode;

                // Get start time from node
                NodeList listOfTimeInterval = timeSeriesElement.getElementsByTagName("timeInterval");
                Node startTimeNode = listOfTimeInterval.item(0);
                Element startTimeElement = (Element) startTimeNode;
                String startTime = startTimeElement.getElementsByTagName("start").item(0).getTextContent();
                ZonedDateTime zonedStartTime = ZonedDateTime.parse(startTime);

                logger.debug("\"timeSeries\" node: {}/{} with start time: {}", (i + 1), listOfTimeSeries.getLength(),
                        zonedStartTime);

                NodeList listOfPoints = timeSeriesElement.getElementsByTagName("Point");

                for (int p = 0; p < listOfPoints.getLength(); p++) {
                    Node pointNode = listOfPoints.item(p);
                    if (pointNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element pointElement = (Element) pointNode;
                        ZonedDateTime timeStamp = zonedStartTime.plusHours(p);
                        String price = pointElement.getElementsByTagName("price.amount").item(0).getTextContent();
                        Double priceAsDouble = Double.parseDouble(price);
                        responseMap.put(timeStamp, priceAsDouble);
                        logger.debug("\"Point\" node: {}/{} with values: {} - {} €/MWh", (p + 1),
                                listOfPoints.getLength(), timeStamp, priceAsDouble);
                    }
                }
            }
        }
        return responseMap;
    }
}
