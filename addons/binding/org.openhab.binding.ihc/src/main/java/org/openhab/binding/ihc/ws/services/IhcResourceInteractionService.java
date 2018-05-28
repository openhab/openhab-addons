/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.ws.services;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.ihc.ws.datatypes.WSBaseDataType;
import org.openhab.binding.ihc.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.ws.http.IhcHttpsClient;
import org.openhab.binding.ihc.ws.resourcevalues.WSBooleanValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSDateValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSEnumValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSFloatingPointValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSIntegerValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSResourceValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSTimeValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSTimerValue;
import org.openhab.binding.ihc.ws.resourcevalues.WSWeekdayValue;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Class to handle IHC / ELKO LS Controller's resource interaction service.
 *
 * Service is used to fetch or update resource values from/to controller.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcResourceInteractionService extends IhcHttpsClient {

    private String url;
    private int timeout;

    public IhcResourceInteractionService(String host, int timeout) {
        url = "https://" + host + "/ws/ResourceInteractionService";
        this.timeout = timeout;
        super.setConnectTimeout(timeout);
    }

    private String sendSoapQuery(String query, int timeout) throws IhcExecption {
        openConnection(url);
        try {
            return sendQuery(query, timeout);
        } finally {
            closeConnection();
        }
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
                + "</soapenv:Envelope>\n";
        // @formatter:on

        String query = String.format(soapQuery, String.valueOf(resoureId));
        String response = sendSoapQuery(query, timeout);

        NodeList nodeList;
        try {
            nodeList = parseList(response, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getRuntimeValue2");

            if (nodeList != null && nodeList.getLength() == 1) {

                WSResourceValue val = parseResourceValue(nodeList.item(0), 2);

                if (val != null && val.getResourceID() == resoureId) {
                    return val;
                } else {
                    throw new IhcExecption("No resource id found");
                }

            } else {
                throw new IhcExecption("No resource value found");
            }

        } catch (XPathExpressionException e) {
            throw new IhcExecption(e);
        } catch (UnsupportedEncodingException e) {
            throw new IhcExecption(e);
        }
    }

    private NodeList parseList(String xml, String xpathExpression)
            throws XPathExpressionException, UnsupportedEncodingException {
        InputStream is = new ByteArrayInputStream(xml.getBytes("UTF8"));
        XPath xpath = XPathFactory.newInstance().newXPath();
        InputSource inputSource = new InputSource(is);

        xpath.setNamespaceContext(new NamespaceContext() {
            @Override
            public String getNamespaceURI(String prefix) {
                if (prefix == null) {
                    throw new IllegalArgumentException("Prefix argument can't be null");
                } else if ("SOAP-ENV".equals(prefix)) {
                    return "http://schemas.xmlsoap.org/soap/envelope/";
                } else if ("ns1".equals(prefix)) {
                    return "utcs";
                } else if ("ns2".equals(prefix)) {
                    return "utcs.values";
                }
                return null;
            }

            @Override
            public String getPrefix(String uri) {
                return null;
            }

            @Override
            @SuppressWarnings("rawtypes")
            public Iterator getPrefixes(String uri) {
                throw new UnsupportedOperationException();
            }
        });

        return (NodeList) xpath.evaluate(xpathExpression, inputSource, XPathConstants.NODESET);
    }

    private WSResourceValue parseResourceValue(Node n, int nameSpaceNumber) throws XPathExpressionException {

        // parse resource id
        String resourceId = getValue(n, "ns1:resourceID");

        if (StringUtils.isNotBlank(resourceId)) {
            int id = Integer.parseInt(resourceId);

            // Parse floating point value
            String value = getValue(n, "ns1:value/ns" + nameSpaceNumber + ":floatingPointValue");
            if (StringUtils.isNotBlank(value)) {
                WSFloatingPointValue val = new WSFloatingPointValue();
                val.setResourceID(id);
                val.setFloatingPointValue(Double.valueOf(value));
                value = getValue(n, "ns1:value/ns" + nameSpaceNumber + ":maximumValue");
                if (StringUtils.isNotBlank(value)) {
                    val.setMaximumValue(Double.valueOf(value));
                }
                value = getValue(n, "ns1:value/ns" + nameSpaceNumber + ":minimumValue");
                if (StringUtils.isNotBlank(value)) {
                    val.setMinimumValue(Double.valueOf(value));
                }
                return val;
            }

            // Parse boolean value
            value = getValue(n, "ns1:value/ns" + nameSpaceNumber + ":value");
            if (StringUtils.isNotBlank(value)) {
                WSBooleanValue val = new WSBooleanValue();
                val.setResourceID(id);
                val.setValue(Boolean.valueOf(value));
                return val;
            }

            // Parse integer value
            value = getValue(n, "ns1:value/ns" + nameSpaceNumber + ":integer");
            if (StringUtils.isNotBlank(value)) {
                WSIntegerValue val = new WSIntegerValue();
                val.setResourceID(id);
                val.setInteger(Integer.valueOf(value));
                value = getValue(n, "ns1:value/ns" + nameSpaceNumber + ":maximumValue");
                if (StringUtils.isNotBlank(value)) {
                    val.setMaximumValue(Integer.valueOf(value));
                }
                value = getValue(n, "ns1:value/ns" + nameSpaceNumber + ":minimumValue");
                if (StringUtils.isNotBlank(value)) {
                    val.setMinimumValue(Integer.valueOf(value));
                }
                return val;
            }

            // Parse timer value
            value = getValue(n, "ns1:value/ns" + nameSpaceNumber + ":milliseconds");
            if (StringUtils.isNotBlank(value)) {
                WSTimerValue val = new WSTimerValue();
                val.setResourceID(id);
                val.setMilliseconds(Integer.valueOf(value));
                return val;
            }

            // Parse time value
            value = getValue(n, "ns1:value/ns" + nameSpaceNumber + ":hours");
            if (StringUtils.isNotBlank(value)) {
                WSTimeValue val = new WSTimeValue();
                val.setResourceID(id);
                val.setHours(Integer.valueOf(value));
                value = getValue(n, "ns1:value/ns" + nameSpaceNumber + ":minutes");
                if (StringUtils.isNotBlank(value)) {
                    val.setMinutes(Integer.valueOf(value));
                }
                value = getValue(n, "ns1:value/ns" + nameSpaceNumber + ":seconds");
                if (StringUtils.isNotBlank(value)) {
                    val.setSeconds(Integer.valueOf(value));
                }
                return val;
            }

            // Parse date value
            value = getValue(n, "ns1:value/ns" + nameSpaceNumber + ":day");
            if (StringUtils.isNotBlank(value)) {
                WSDateValue val = new WSDateValue();
                val.setResourceID(id);
                val.setDay(Byte.valueOf(value));
                value = getValue(n, "ns1:value/ns" + nameSpaceNumber + ":month");
                if (StringUtils.isNotBlank(value)) {
                    val.setMonth(Byte.valueOf(value));
                }
                value = getValue(n, "ns1:value/ns" + nameSpaceNumber + ":year");
                if (StringUtils.isNotBlank(value)) {
                    val.setYear(Short.valueOf(value));
                }
                return val;
            }

            // Parse enum value
            value = getValue(n, "ns1:value/ns" + nameSpaceNumber + ":definitionTypeID");
            if (StringUtils.isNotBlank(value)) {
                WSEnumValue val = new WSEnumValue();
                val.setResourceID(id);
                val.setDefinitionTypeID(Integer.valueOf(value));
                value = getValue(n, "ns1:value/ns" + nameSpaceNumber + ":enumValueID");
                if (StringUtils.isNotBlank(value)) {
                    val.setEnumValueID(Integer.valueOf(value));
                }
                value = getValue(n, "ns1:value/ns" + nameSpaceNumber + ":enumName");
                if (StringUtils.isNotBlank(value)) {
                    val.setEnumName(value);
                }
                return val;
            }

            // Parse week day value
            value = getValue(n, "ns1:value/ns" + nameSpaceNumber + ":weekdayNumber");
            if (StringUtils.isNotBlank(value)) {
                WSWeekdayValue val = new WSWeekdayValue();
                val.setResourceID(id);
                val.setWeekdayNumber(Integer.valueOf(value));
                return val;
            }

            // Unknown value type
            throw new IllegalArgumentException("Unsupported value type");
        }
        return null;
    }

    private String getValue(Node n, String expr) throws XPathExpressionException {

        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new NamespaceContext() {

            @Override
            public String getNamespaceURI(String prefix) {

                if (prefix == null) {
                    throw new IllegalArgumentException("Prefix argument can't be null");
                } else if ("SOAP-ENV".equals(prefix)) {
                    return "http://schemas.xmlsoap.org/soap/envelope/";
                } else if ("ns1".equals(prefix)) {
                    return "utcs";
                }
                // else if ("ns2".equals(prefix)) return "utcs.values";
                return "utcs.values";
            }

            @Override
            public String getPrefix(String uri) {
                return null;
            }

            @Override
            @SuppressWarnings("rawtypes")
            public Iterator getPrefixes(String uri) {
                throw new UnsupportedOperationException();
            }
        });

        XPathExpression pathExpr = xpath.compile(expr);
        return (String) pathExpr.evaluate(n, XPathConstants.STRING);
    }

    /**
     * Update resource value to controller.
     *
     *
     * @param value
     *            Resource value.
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
                + "</soap:Envelope>\n";
        // @formatter:on

        String query = String.format(soapQuery, value.isValue() ? "true" : "false", value.getResourceID());
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
                + "</soap:Envelope>\n";
        // @formatter:on

        String query = String.format(soapQuery, value.getMaximumValue(), value.getMinimumValue(),
                value.getFloatingPointValue(), value.getResourceID());
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

        String query = String.format(soapQuery, value.getMaximumValue(), value.getMinimumValue(), value.getInteger(),
                value.getResourceID());
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

        String query = String.format(soapQuery, value.getMilliseconds(), value.getResourceID());
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

        String query = String.format(soapQuery, value.getWeekdayNumber(), value.getResourceID());
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

        String query = String.format(soapQuery, value.getDefinitionTypeID(), value.getEnumValueID(),
                value.getEnumName(), value.getResourceID());
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

        String query = String.format(soapQuery, value.getHours(), value.getMinutes(), value.getSeconds(),
                value.getResourceID());
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

        String query = String.format(soapQuery, value.getMonth(), value.getYear(), value.getDay(),
                value.getResourceID());
        return doResourceUpdate(query);
    }

    private boolean doResourceUpdate(String query) throws IhcExecption {
        String response = sendSoapQuery(query, timeout);
        return Boolean.parseBoolean(
                WSBaseDataType.parseXMLValue(response, "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:setResourceValue2"));
    }

    /**
     * Enable resources runtime value notifications.
     *
     * @param resourceIdList
     *            List of resource Identifiers.
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
        sendSoapQuery(query, timeout);
    }

    /**
     * Wait runtime value notifications.
     *
     * Runtime value notification should firstly be activated by
     * enableRuntimeValueNotifications function.
     *
     * @param timeoutInSeconds
     *            How many seconds to wait notifications.
     * @return List of received runtime value notifications.
     * @throws SocketTimeoutException
     */
    public List<WSResourceValue> waitResourceValueNotifications(int timeoutInSeconds)
            throws IhcExecption, SocketTimeoutException {

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
        String response = sendSoapQuery(query, timeout + timeoutInSeconds * 1000);
        List<WSResourceValue> resourceValueList = new ArrayList<WSResourceValue>();

        try {
            NodeList nodeList = parseList(response,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:waitForResourceValueChanges2/ns1:arrayItem");

            if (nodeList != null) {
                if (nodeList.getLength() == 1) {
                    String resourceId = getValue(nodeList.item(0), "ns1:resourceID");
                    if (resourceId == null || resourceId.isEmpty()) {
                        // IHC controller indicates timeout
                        throw new SocketTimeoutException();
                    }
                }

                for (int i = 0; i < nodeList.getLength(); i++) {
                    int nameSpaceNumber = i + 2;
                    WSResourceValue newVal = parseResourceValue(nodeList.item(i), nameSpaceNumber);
                    if (newVal != null) {
                        resourceValueList.add(newVal);
                    }
                }
            } else {
                throw new IhcExecption("Illegal resource value notification response received");
            }
            return resourceValueList;
        } catch (XPathExpressionException e) {
            throw new IhcExecption(e);
        } catch (UnsupportedEncodingException e) {
            throw new IhcExecption(e);
        }
    }
}
