/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.ihc.internal.ws.services;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPathExpressionException;

import org.openhab.binding.ihc.internal.ws.datatypes.XPathUtils;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.internal.ws.http.IhcConnectionPool;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSBooleanValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSDateValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSEnumValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSFloatingPointValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSIntegerValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSResourceValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSTimeValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSTimerValue;
import org.openhab.binding.ihc.internal.ws.resourcevalues.WSWeekdayValue;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class to handle IHC / ELKO LS Controller's resource interaction service.
 *
 * Service is used to fetch or update resource values from/to controller.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcResourceInteractionService extends IhcBaseService {

    public IhcResourceInteractionService(String host, int timeout, IhcConnectionPool ihcConnectionPool) {
        super(ihcConnectionPool, timeout, host, "ResourceInteractionService");
    }

    /**
     * Query resource value from controller.
     *
     * @param resoureId Resource Identifier.
     * @return Resource value.
     */
    public WSResourceValue resourceQuery(int resoureId) throws IhcExecption {
        // @formatter:off
        final String soapQuery =
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
                + " <soapenv:Body>\n"
                + "  <ns1:getRuntimeValue1 xmlns:ns1=\"utcs\">%s</ns1:getRuntimeValue1>\n"
                + " </soapenv:Body>\n"
                + "</soapenv:Envelope>";
        // @formatter:on

        String query = String.format(soapQuery, String.valueOf(resoureId));
        String response = sendSoapQuery(null, query);
        NodeList nodeList;
        try {
            nodeList = XPathUtils.parseList(response, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getRuntimeValue2");

            if (nodeList != null && nodeList.getLength() == 1) {
                WSResourceValue val = parseResourceValue(nodeList.item(0));

                if (val != null && val.resourceID == resoureId) {
                    return val;
                } else {
                    throw new IhcExecption("No resource id found");
                }
            } else {
                throw new IhcExecption("No resource value found");
            }
        } catch (XPathExpressionException | NumberFormatException | IOException e) {
            throw new IhcExecption("Error occured during XML data parsing", e);
        }
    }

    private WSResourceValue parseResourceValue(Node n) throws XPathExpressionException, NumberFormatException {
        // parse resource id
        String resourceId = XPathUtils.getSpeficValueFromNode(n, "ns1:resourceID");

        if (resourceId != null && !resourceId.isBlank()) {
            int id = Integer.parseInt(resourceId);

            // Parse floating point value
            String floatingPointValue = getValue(n, "floatingPointValue");
            if (floatingPointValue != null && !floatingPointValue.isBlank()) {
                String min = getValue(n, "minimumValue");
                String max = getValue(n, "maximumValue");
                return new WSFloatingPointValue(id, Double.valueOf(floatingPointValue), Double.valueOf(min),
                        Double.valueOf(max));
            }

            // Parse boolean value
            String value = getValue(n, "value");
            if (value != null && !value.isBlank()) {
                return new WSBooleanValue(id, Boolean.valueOf(value));
            }

            // Parse integer value
            String integer = getValue(n, "integer");
            if (integer != null && !integer.isBlank()) {
                String min = getValue(n, "minimumValue");
                String max = getValue(n, "maximumValue");
                return new WSIntegerValue(id, Integer.valueOf(integer), Integer.valueOf(min), Integer.valueOf(max));
            }

            // Parse timer value
            String milliseconds = getValue(n, "milliseconds");
            if (milliseconds != null && !milliseconds.isBlank()) {
                return new WSTimerValue(id, Integer.valueOf(milliseconds));
            }

            // Parse time value
            String hours = getValue(n, "hours");
            if (hours != null && !hours.isBlank()) {
                String minutes = getValue(n, "minutes");
                String seconds = getValue(n, "seconds");
                return new WSTimeValue(id, Integer.valueOf(hours), Integer.valueOf(minutes), Integer.valueOf(seconds));
            }

            // Parse date value
            String year = getValue(n, "year");
            if (year != null && !year.isBlank()) {
                String month = getValue(n, "month");
                String day = getValue(n, "day");
                return new WSDateValue(id, Short.valueOf(year), Byte.valueOf(month), Byte.valueOf(day));
            }

            // Parse enum value
            String definitionTypeID = getValue(n, "definitionTypeID");
            if (definitionTypeID != null && !definitionTypeID.isBlank()) {
                String enumValueID = getValue(n, "enumValueID");
                String enumName = getValue(n, "enumName");
                return new WSEnumValue(id, Integer.valueOf(definitionTypeID), Integer.valueOf(enumValueID), enumName);
            }

            // Parse week day value
            value = getValue(n, "weekdayNumber");
            if (value != null && !value.isBlank()) {
                return new WSWeekdayValue(id, Integer.valueOf(value));
            }

            // Unknown value type
            throw new IllegalArgumentException("Unsupported value type");
        }
        return null;
    }

    private String getValue(Node n, String value) throws XPathExpressionException {
        return XPathUtils.getSpeficValueFromNode(n, "ns1:value/" + XPathUtils.createIgnoreNameSpaceSyntaxExpr(value));
    }

    /**
     * Update resource value to controller.
     *
     *
     * @param value Resource value.
     * @return True if value is successfully updated.
     */
    public boolean resourceUpdate(WSResourceValue value) throws IhcExecption {
        boolean retval = false;

        if (value instanceof WSFloatingPointValue) {
            retval = resourceUpdate((WSFloatingPointValue) value);
        } else if (value instanceof WSBooleanValue) {
            retval = resourceUpdate((WSBooleanValue) value);
        } else if (value instanceof WSIntegerValue) {
            retval = resourceUpdate((WSIntegerValue) value);
        } else if (value instanceof WSTimerValue) {
            retval = resourceUpdate((WSTimerValue) value);
        } else if (value instanceof WSWeekdayValue) {
            retval = resourceUpdate((WSWeekdayValue) value);
        } else if (value instanceof WSEnumValue) {
            retval = resourceUpdate((WSEnumValue) value);
        } else if (value instanceof WSTimeValue) {
            retval = resourceUpdate((WSTimeValue) value);
        } else if (value instanceof WSDateValue) {
            retval = resourceUpdate((WSDateValue) value);
        } else {
            throw new IhcExecption("Unsupported value type " + value.getClass().toString());
        }

        return retval;
    }

    public boolean resourceUpdate(WSBooleanValue value) throws IhcExecption {
        // @formatter:off
        final String soapQuery =
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
                + " <soap:Body>\n"
                + "  <setResourceValue1 xmlns=\"utcs\">\n"
                + "   <value xmlns:q1=\"utcs.values\" xsi:type=\"q1:WSBooleanValue\">\n"
                + "    <q1:value>%s</q1:value>\n"
                + "   </value>\n"
                + "   <resourceID>%s</resourceID>\n"
                + "   <isValueRuntime>true</isValueRuntime>\n"
                + "  </setResourceValue1>\n"
                + " </soap:Body>\n"
                + "</soap:Envelope>";
        // @formatter:on

        String query = String.format(soapQuery, value.value ? "true" : "false", value.resourceID);
        return doResourceUpdate(query);
    }

    public boolean resourceUpdate(WSFloatingPointValue value) throws IhcExecption {
        // @formatter:off
        final String soapQuery =
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
                + " <soap:Body>\n"
                + "  <setResourceValue1 xmlns=\"utcs\">\n"
                + "   <value xmlns:q1=\"utcs.values\" xsi:type=\"q1:WSFloatingPointValue\">\n"
                + "    <q1:maximumValue>%s</q1:maximumValue>\n"
                + "    <q1:minimumValue>%s</q1:minimumValue>\n"
                + "    <q1:floatingPointValue>%s</q1:floatingPointValue>\n"
                + "   </value>\n"
                + "   <resourceID>%s</resourceID>\n"
                + "   <isValueRuntime>true</isValueRuntime>\n"
                + "  </setResourceValue1>\n"
                + " </soap:Body>\n"
                + "</soap:Envelope>";
        // @formatter:on

        String query = String.format(soapQuery, value.maximumValue, value.minimumValue, value.value, value.resourceID);
        return doResourceUpdate(query);
    }

    public boolean resourceUpdate(WSIntegerValue value) throws IhcExecption {
        // @formatter:off
        final String soapQuery =
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
                + " <soap:Body>\n"
                + "  <setResourceValue1 xmlns=\"utcs\">\n"
                + "   <value xmlns:q1=\"utcs.values\" xsi:type=\"q1:WSIntegerValue\">\n"
                + "    <q1:maximumValue>%s</q1:maximumValue>\n"
                + "    <q1:minimumValue>%s</q1:minimumValue>\n"
                + "    <q1:integer>%s</q1:integer>\n"
                + "   </value>\n"
                + "   <resourceID>%s</resourceID>\n"
                + "   <isValueRuntime>true</isValueRuntime>\n"
                + "  </setResourceValue1>\n"
                + " </soap:Body>\n"
                + "</soap:Envelope>";
        // @formatter:on

        String query = String.format(soapQuery, value.maximumValue, value.minimumValue, value.value, value.resourceID);
        return doResourceUpdate(query);
    }

    public boolean resourceUpdate(WSTimerValue value) throws IhcExecption {
        // @formatter:off
        final String soapQuery =
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
                + " <soap:Body>\n"
                + "  <setResourceValue1 xmlns=\"utcs\">\n"
                + "   <value xmlns:q1=\"utcs.values\" xsi:type=\"q1:WSTimerValue\">\n"
                + "    <q1:milliseconds>%s</q1:milliseconds>\n"
                + "   </value>\n"
                + "   <resourceID>%s</resourceID>\n"
                + "   <isValueRuntime>true</isValueRuntime>\n"
                + "  </setResourceValue1>\n"
                + " </soap:Body>\n"
                + "</soap:Envelope>";
        // @formatter:on

        String query = String.format(soapQuery, value.milliseconds, value.resourceID);
        return doResourceUpdate(query);
    }

    public boolean resourceUpdate(WSWeekdayValue value) throws IhcExecption {
        // @formatter:off
        final String soapQuery =
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
                + " <soap:Body>\n"
                + "  <setResourceValue1 xmlns=\"utcs\">\n"
                + "   <value xmlns:q1=\"utcs.values\" xsi:type=\"q1:WSWeekdayValue\">\n"
                + "    <q1:weekdayNumber>%s</q1:weekdayNumber>\n"
                + "   </value>\n"
                + "   <resourceID>%s</resourceID>\n"
                + "   <isValueRuntime>true</isValueRuntime>\n"
                + "  </setResourceValue1>\n"
                + " </soap:Body>\n"
                + "</soap:Envelope>";
        // @formatter:on

        String query = String.format(soapQuery, value.weekdayNumber, value.resourceID);
        return doResourceUpdate(query);
    }

    public boolean resourceUpdate(WSEnumValue value) throws IhcExecption {
        // @formatter:off
        final String soapQuery =
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
                + " <soap:Body>\n"
                + "  <setResourceValue1 xmlns=\"utcs\">\n"
                + "   <value xmlns:q1=\"utcs.values\" xsi:type=\"q1:WSEnumValue\">\n"
                + "    <q1:definitionTypeID>%s</q1:definitionTypeID>\n"
                + "    <q1:enumValueID>%s</q1:enumValueID>\n"
                + "    <q1:enumName>%s</q1:enumName>\n"
                + "   </value>\n"
                + "   <resourceID>%s</resourceID>\n"
                + "   <isValueRuntime>true</isValueRuntime>\n"
                + "  </setResourceValue1>\n"
                + " </soap:Body>\n"
                + "</soap:Envelope>";
        // @formatter:on

        String query = String.format(soapQuery, value.definitionTypeID, value.enumValueID, value.enumName,
                value.resourceID);
        return doResourceUpdate(query);
    }

    public boolean resourceUpdate(WSTimeValue value) throws IhcExecption {
        // @formatter:off
        final String soapQuery =
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
                + " <soap:Body>\n"
                + "  <setResourceValue1 xmlns=\"utcs\">\n"
                + "   <value xmlns:q1=\"utcs.values\" xsi:type=\"q1:WSTimeValue\">\n"
                + "    <q1:hours>%s</q1:hours>\n"
                + "    <q1:minutes>%s</q1:minutes>\n"
                + "    <q1:seconds>%s</q1:seconds>\n"
                + "   </value>\n"
                + "   <resourceID>%s</resourceID>\n"
                + "   <isValueRuntime>true</isValueRuntime>\n"
                + "  </setResourceValue1>\n"
                + " </soap:Body>\n"
                + "</soap:Envelope>";
        // @formatter:on

        String query = String.format(soapQuery, value.hours, value.minutes, value.seconds, value.resourceID);
        return doResourceUpdate(query);
    }

    public boolean resourceUpdate(WSDateValue value) throws IhcExecption {
        // @formatter:off
        final String soapQuery =
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
                + " <soap:Body>\n"
                + "  <setResourceValue1 xmlns=\"utcs\">\n"
                + "   <value xmlns:q1=\"utcs.values\" xsi:type=\"q1:WSDateValue\">\n"
                + "    <q1:month>%s</q1:month>\n"
                + "    <q1:year>%s</q1:year>\n"
                + "    <q1:day>%s</q1:day>\n"
                + "   </value>\n"
                + "   <resourceID>%s</resourceID>\n"
                + "   <isValueRuntime>true</isValueRuntime>\n"
                + "  </setResourceValue1>\n"
                + " </soap:Body>\n"
                + "</soap:Envelope>";
        // @formatter:on

        String query = String.format(soapQuery, value.month, value.year, value.day, value.resourceID);
        return doResourceUpdate(query);
    }

    private boolean doResourceUpdate(String query) throws IhcExecption {
        String response = sendSoapQuery(null, query);
        try {
            return Boolean.parseBoolean(
                    XPathUtils.parseXMLValue(response, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:setResourceValue2"));
        } catch (IOException | XPathExpressionException e) {
            throw new IhcExecption(e);
        }
    }

    /**
     * Enable resources runtime value notifications.
     *
     * @param resourceIdList List of resource Identifiers.
     * @return True is connection successfully opened.
     */
    public void enableRuntimeValueNotifications(Set<Integer> resourceIdList) throws IhcExecption {
        // @formatter:off
        final String soapQueryPrefix =
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
                + " <soap:Body>\n"
                + "  <enableRuntimeValueNotifications1 xmlns=\"utcs\">\n";

        final String soapQuerySuffix =
                  "  </enableRuntimeValueNotifications1>\n"
                + " </soap:Body>\n"
                + "</soap:Envelope>";
        // @formatter:on

        String query = soapQueryPrefix;
        for (int i : resourceIdList) {
            query += "   <xsd:arrayItem>" + i + "</xsd:arrayItem>\n";
        }
        query += soapQuerySuffix;
        sendSoapQuery(null, query);
    }

    /**
     * Wait runtime value notifications.
     *
     * Runtime value notification should firstly be activated by
     * enableRuntimeValueNotifications function.
     *
     * @param timeoutInSeconds How many seconds to wait notifications.
     * @return List of received runtime value notifications.
     * @throws SocketTimeoutException
     * @throws IhcTimeoutExecption
     */
    public List<WSResourceValue> waitResourceValueNotifications(int timeoutInSeconds) throws IhcExecption {
        // @formatter:off
        final String soapQuery =
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:utcs=\"utcs\">\n"
                + " <soapenv:Body>\n"
                + "  <utcs:waitForResourceValueChanges1>%s</utcs:waitForResourceValueChanges1>\n"
                + " </soapenv:Body>\n"
                + "</soapenv:Envelope>";
        // @formatter:on

        String query = String.format(soapQuery, timeoutInSeconds);
        String response = sendSoapQuery(null, query, getTimeout() + timeoutInSeconds * 1000);
        List<WSResourceValue> resourceValueList = new ArrayList<>();

        try {
            NodeList nodeList = XPathUtils.parseList(response,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:waitForResourceValueChanges2/ns1:arrayItem");

            if (nodeList != null) {
                if (nodeList.getLength() == 1) {
                    String resourceId = XPathUtils.getSpeficValueFromNode(nodeList.item(0), "ns1:resourceID");
                    if (resourceId == null || resourceId.isEmpty()) {
                        // IHC controller indicates timeout, return empty list
                        return resourceValueList;
                    }
                }

                for (int i = 0; i < nodeList.getLength(); i++) {
                    WSResourceValue newVal = parseResourceValue(nodeList.item(i));
                    if (newVal != null) {
                        resourceValueList.add(newVal);
                    }
                }
            } else {
                throw new IhcExecption("Illegal resource value notification response received");
            }
            return resourceValueList;
        } catch (XPathExpressionException | NumberFormatException | IOException e) {
            throw new IhcExecption("Error occured during XML data parsing", e);
        }
    }
}
