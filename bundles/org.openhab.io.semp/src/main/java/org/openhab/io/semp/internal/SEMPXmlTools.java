/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.semp.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.openhab.io.semp.internal.SEMPConstants.SEMPMessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * XML Tools für device communication
 *
 * @author Markus Eckhardt - Initial Contribution
 *
 */
public class SEMPXmlTools {
    private final Logger logger = LoggerFactory.getLogger(SEMPXmlTools.class);

    public String createXMLMessage(List<SEMPMessageType> typeList, Map<String, SEMPConsumer> consumerMap,
            String deviceID) {
        DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder icBuilder = null;
        try {
            icBuilder = icFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            logger.error("{}", e);
            return null;
        }
        org.w3c.dom.Document doc = icBuilder.newDocument();
        Element mainRootElement = doc.createElementNS("http://www.sma.de/communication/schema/SEMP/v1", "Device2EM");
        doc.appendChild(mainRootElement);
        for (SEMPMessageType type : typeList) {
            switch (type) {
                case MSG_DEVICE_INFO:
                    for (SEMPConsumer consumer : consumerMap.values()) {
                        if (!consumer.getIdentification().getDeviceId().equals(deviceID) && !"".equals(deviceID)) {
                            continue;
                        }
                        Element deviceInfo = doc.createElement("DeviceInfo");
                        Element indentification = doc.createElement("Identification");
                        if (consumer.getIdentification().isDeviceIdSet()) {
                            indentification.appendChild(
                                    generateTextNode(doc, "DeviceId", consumer.getIdentification().getDeviceId()));
                        }
                        if (consumer.getIdentification().isDeviceNameSet()) {
                            indentification.appendChild(
                                    generateTextNode(doc, "DeviceName", consumer.getIdentification().getDeviceName()));
                        }
                        if (consumer.getIdentification().isDeviceTypeSet()) {
                            indentification.appendChild(
                                    generateTextNode(doc, "DeviceType", consumer.getIdentification().getDeviceType()));
                        }
                        if (consumer.getIdentification().isDeviceSerialSet()) {
                            indentification.appendChild(generateTextNode(doc, "DeviceSerial",
                                    consumer.getIdentification().getDeviceSerial()));
                        }
                        if (consumer.getIdentification().isDeviceVendorSet()) {
                            indentification.appendChild(generateTextNode(doc, "DeviceVendor",
                                    consumer.getIdentification().getDeviceVendor()));
                        }
                        deviceInfo.appendChild(indentification);
                        Element characteristics = doc.createElement("Characteristics");
                        if (consumer.getCharacteristics().isMaxPowerConsumptionSet()) {
                            characteristics.appendChild(generateTextNode(doc, "MaxPowerConsumption",
                                    String.valueOf(consumer.getCharacteristics().getMaxPowerConsumption())));
                        }
                        if (consumer.getCharacteristics().isMinOnTimeSet()) {
                            characteristics.appendChild(generateTextNode(doc, "MinOnTime",
                                    String.valueOf(consumer.getCharacteristics().getMinOnTime())));
                        }
                        if (consumer.getCharacteristics().isMinOffTimeSet()) {
                            characteristics.appendChild(generateTextNode(doc, "MinOffTime",
                                    String.valueOf(consumer.getCharacteristics().getMinOffTime())));
                        }
                        deviceInfo.appendChild(characteristics);
                        Element capabilities = doc.createElement("Capabilities");
                        if (consumer.getCapabilities().isMethodSet()) {
                            Element node = doc.createElement("CurrentPower");
                            node.appendChild(generateTextNode(doc, "Method", consumer.getCapabilities().getMethod()));
                            capabilities.appendChild(node);
                        }
                        if (consumer.getCapabilities().isAbsoluteTimestampsSet()) {
                            Element node = doc.createElement("Timestamps");
                            node.appendChild(generateTextNode(doc, "AbsoluteTimestamps",
                                    String.valueOf(consumer.getCapabilities().getAbsoluteTimestamps())));
                            capabilities.appendChild(node);
                        }
                        if (consumer.getCapabilities().isInterruptionsAllowedSet()) {
                            Element node = doc.createElement("Interruptions");
                            node.appendChild(generateTextNode(doc, "InterruptionsAllowed",
                                    String.valueOf(consumer.getCapabilities().getInterruptionsAllowed())));
                            capabilities.appendChild(node);
                        }
                        if (consumer.getCapabilities().isOptionalEnergySet()) {
                            Element node = doc.createElement("Requests");
                            node.appendChild(generateTextNode(doc, "OptionalEnergy",
                                    String.valueOf(consumer.getCapabilities().getOptionalEnergy())));
                            capabilities.appendChild(node);
                        }
                        deviceInfo.appendChild(capabilities);
                        mainRootElement.appendChild(deviceInfo);
                    }
                    break;
                case MSG_DEVICE_STATUS:
                    for (SEMPConsumer consumer : consumerMap.values()) {
                        if (!consumer.getIdentification().getDeviceId().equals(deviceID) && !"".equals(deviceID)) {
                            continue;
                        }
                        Element deviceStatus = doc.createElement("DeviceStatus");
                        if (consumer.getIdentification().isDeviceIdSet()) {
                            deviceStatus.appendChild(
                                    generateTextNode(doc, "DeviceId", consumer.getIdentification().getDeviceId()));
                        }
                        if (consumer.getDeviceStatus().isEMSignalsAcceptedSet()) {
                            deviceStatus.appendChild(generateTextNode(doc, "EMSignalsAccepted",
                                    String.valueOf(consumer.getDeviceStatus().getEMSignalsAccepted())));
                        }
                        if (consumer.getDeviceStatus().isStatusSet()) {
                            deviceStatus.appendChild(
                                    generateTextNode(doc, "Status", consumer.getDeviceStatus().getStatus()));
                        }
                        Element powerConsumption = doc.createElement("PowerConsumption");
                        if (!consumer.hasHistory) {
                            Element powerInfo = doc.createElement("PowerInfo");
                            if (consumer.getDeviceStatus().isAveragePowerSet()) {
                                powerInfo.appendChild(generateTextNode(doc, "AveragePower",
                                        String.valueOf(Math.round(consumer.getDeviceStatus().getAveragePower()))));
                            }
                            if (consumer.getDeviceStatus().isTimestampSet()) {
                                powerInfo.appendChild(generateTextNode(doc, "Timestamp",
                                        String.valueOf(consumer.getDeviceStatus().getTimestamp())));
                            }
                            if (consumer.getDeviceStatus().isAveragingIntervalSet()) {
                                powerInfo.appendChild(generateTextNode(doc, "AveragingInterval",
                                        String.valueOf(consumer.getDeviceStatus().getAveragingInterval())));
                            }
                            powerConsumption.appendChild(powerInfo);
                        } else {
                            for (int i = 0; i < consumer.getDeviceHistoryStatus().size(); i++) {
                                Element powerInfo = doc.createElement("PowerInfo");
                                if (consumer.getDeviceHistoryStatus().get(i).isAveragePowerSet()) {
                                    powerInfo.appendChild(generateTextNode(doc, "AveragePower", String.valueOf(
                                            Math.round(consumer.getDeviceHistoryStatus().get(i).getAveragePower()))));
                                }
                                if (consumer.getDeviceHistoryStatus().get(i).isMaxPowerSet()) {
                                    powerInfo.appendChild(generateTextNode(doc, "MaxPower", String.valueOf(
                                            Math.round(consumer.getDeviceHistoryStatus().get(i).getMaxPower()))));
                                }
                                if (consumer.getDeviceHistoryStatus().get(i).isMinPowerSet()) {
                                    powerInfo.appendChild(generateTextNode(doc, "MinPower", String.valueOf(
                                            Math.round(consumer.getDeviceHistoryStatus().get(i).getMinPower()))));
                                }
                                if (consumer.getDeviceHistoryStatus().get(i).isStdDevPowerSet()) {
                                    powerInfo.appendChild(generateTextNode(doc, "StdDevPower",
                                            String.valueOf(consumer.getDeviceHistoryStatus().get(i).getStdDevPower())));
                                }
                                if (consumer.getDeviceHistoryStatus().get(i).isTimestampSet()) {
                                    powerInfo.appendChild(generateTextNode(doc, "Timestamp",
                                            String.valueOf(consumer.getDeviceHistoryStatus().get(i).getTimestamp())));
                                }
                                if (consumer.getDeviceHistoryStatus().get(i).isAveragingIntervalSet()) {
                                    powerInfo.appendChild(generateTextNode(doc, "AveragingInterval", String
                                            .valueOf(consumer.getDeviceHistoryStatus().get(i).getAveragingInterval())));
                                }
                                powerConsumption.appendChild(powerInfo);
                            }
                        }
                        deviceStatus.appendChild(powerConsumption);
                        mainRootElement.appendChild(deviceStatus);
                    }
                    break;
                case MSG_TIMEFRAME:
                    for (SEMPConsumer consumer : consumerMap.values()) {
                        if (!consumer.getIdentification().getDeviceId().equals(deviceID) && !"".equals(deviceID)) {
                            continue;
                        }
                        Date currentDate = new Date();
                        long unixActTime = currentDate.getTime() / 1000;
                        if (!arePlaningRequestsAvailible(consumer, unixActTime)) {
                            mainRootElement.appendChild(doc.createComment("PlanningRequest element omitted"));
                        } else {
                            Element planningRequest = doc.createElement("PlanningRequest");
                            for (int i = 0; i < consumer.getTimeFrames().size(); i++) {
                                if (isRequestInPast(consumer, unixActTime, i)) {
                                    continue;
                                }
                                Element timeFrame = doc.createElement("Timeframe");
                                if (consumer.getIdentification().isDeviceIdSet()) {
                                    timeFrame.appendChild(generateTextNode(doc, "DeviceId",
                                            consumer.getIdentification().getDeviceId()));
                                }
                                if (consumer.getTimeFrames().get(i).isEarliestStartSet()) {
                                    timeFrame.appendChild(generateTextNode(doc, "EarliestStart",
                                            calculateEarliestStart(consumer, i, unixActTime)));
                                }
                                if (consumer.getTimeFrames().get(i).isLatestEndSet()) {
                                    timeFrame.appendChild(generateTextNode(doc, "LatestEnd",
                                            calculateLatestEnd(consumer, i, unixActTime)));
                                }
                                if (consumer.getTimeFrames().get(i).isMaxRunningTimeSet()) {
                                    timeFrame.appendChild(generateTextNode(doc, "MaxRunningTime",
                                            calculateMaxRunningTime(consumer, i, unixActTime)));
                                }
                                if (consumer.getTimeFrames().get(i).isMinRunningTimeSet()) {
                                    timeFrame.appendChild(generateTextNode(doc, "MinRunningTime",
                                            calculateMinRunningTime(consumer, i, unixActTime)));
                                }
                                planningRequest.appendChild(timeFrame);
                            }
                            mainRootElement.appendChild(planningRequest);
                        }
                    }
                    break;
                default:
                    break;
            }
        }

        // output DOM XML to console
        Transformer transformer = null;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        } catch (TransformerConfigurationException | TransformerFactoryConfigurationError e) {
            logger.error("{}", e);
            return null;
        }
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "0");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.VERSION, "1.0");

        DOMSource source = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        try {
            transformer.transform(source, new StreamResult(writer));
        } catch (TransformerException e) {
            logger.error("{}", e);
            return null;
        }
        return writer.toString();
    }

    private Element generateTextNode(org.w3c.dom.Document doc, String nodeName, String nodeValue) {
        Element node = doc.createElement(nodeName);
        node.appendChild(doc.createTextNode(nodeValue));
        return node;
    }

    public String getXMLValue(ByteArrayInputStream xmlStream, String xPath) {
        String value = null;
        org.w3c.dom.Document doc;
        DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder icBuilder = null;
        try {//
            icBuilder = icFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            logger.error("{}", e);
            return null;
        }
        try {
            doc = icBuilder.parse(xmlStream);
        } catch (SAXException | IOException e) {
            logger.error("{}", e);
            return null;
        }
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        try {
            value = (String) xpath.evaluate(xPath, doc, XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            logger.error("{}", e);
            return null;
        }
        return value;
    }

    private String calculateEarliestStart(SEMPConsumer consumer, int index, long unixActTime) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        long unixDayTime = startOfToday.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(Instant.now()));
        long earliestStart = consumer.getTimeFrames().get(index).getEarliestStart() + unixDayTime;
        if (earliestStart > unixActTime) {
            return String.valueOf(earliestStart);
        } else {
            return String.valueOf(unixActTime);
        }
    }

    private String calculateLatestEnd(SEMPConsumer consumer, int index, long unixActTime) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        long unixDayTime = startOfToday.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(Instant.now()));
        return String.valueOf(consumer.getTimeFrames().get(index).getLatestEnd() + unixDayTime);
    }

    private String calculateMaxRunningTime(SEMPConsumer consumer, int index, long unixActTime) {
        SEMPTimeFrame actTimeFrame = consumer.getCurrentTimeFrame(unixActTime);
        if (actTimeFrame == null || !actTimeFrame.isTimestampActivatedSet()
                || index != consumer.getTimeFrames().indexOf(actTimeFrame)) {
            return String.valueOf(consumer.getTimeFrames().get(index).getMaxRunningTime());
        }
        long remainingMaxRunningTime = consumer.getTimeFrames().get(index).getMaxRunningTime()
                + (actTimeFrame.getTimestampActivated() - unixActTime) - actTimeFrame.getCurrentRuntime();
        if (remainingMaxRunningTime > 0) {
            return String.valueOf(remainingMaxRunningTime);
        } else {
            return String.valueOf("0");
        }
    }

    private String calculateMinRunningTime(SEMPConsumer consumer, int index, long unixActTime) {
        SEMPTimeFrame actTimeFrame = consumer.getCurrentTimeFrame(unixActTime);
        if (actTimeFrame == null || !actTimeFrame.isTimestampActivatedSet()
                || index != consumer.getTimeFrames().indexOf(actTimeFrame)) {
            return String.valueOf(consumer.getTimeFrames().get(index).getMinRunningTime());
        }
        long remainingMinRunningTime = consumer.getTimeFrames().get(index).getMinRunningTime()
                + (actTimeFrame.getTimestampActivated() - unixActTime) - actTimeFrame.getCurrentRuntime();
        if (remainingMinRunningTime > 0) {
            return String.valueOf(remainingMinRunningTime);
        } else {
            return String.valueOf("0");
        }
    }

    private boolean arePlaningRequestsAvailible(SEMPConsumer consumer, long unixActTime) {
        if (consumer.getTimeFrames().isEmpty()) {
            return false;
        }
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        String dOW = startOfToday.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.US);
        if (!consumer.getDaysOfTheWeek().isEmpty() && !consumer.getDaysOfTheWeek().contains(dOW)) {
            return false;
        }
        long unixDayTime = startOfToday.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(Instant.now()));
        long latestEnd = consumer.getTimeFrames().get(consumer.getTimeFrames().size() - 1).getLatestEnd() + unixDayTime;
        if (latestEnd > unixActTime) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isRequestInPast(SEMPConsumer consumer, long unixActTime, int index) {
        if (consumer.getTimeFrames().isEmpty()) {
            return false;
        }
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        long unixDayTime = startOfToday.toEpochSecond(ZoneId.systemDefault().getRules().getOffset(Instant.now()));
        long latestEnd = consumer.getTimeFrames().get(index).getLatestEnd() + unixDayTime;
        if (latestEnd > unixActTime) {
            return false;
        } else {
            return true;
        }
    }
}
