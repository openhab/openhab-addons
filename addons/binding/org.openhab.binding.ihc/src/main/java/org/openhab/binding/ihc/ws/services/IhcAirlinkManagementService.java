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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.ihc.ws.datatypes.WSRFDevice;
import org.openhab.binding.ihc.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.ws.http.IhcHttpsClient;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Class to handle IHC / ELKO LS Controller's controller service.
 *
 * Controller service is used to fetch information from the controller.
 * E.g. Project file or controller status.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcAirlinkManagementService extends IhcHttpsClient {

    // @formatter:off
    private static String emptyQuery =
              "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
            + " <soapenv:Body>\n"
            + " </soapenv:Body>\n"
            + "</soapenv:Envelope>\n";
    // @formatter:on

    private String url;
    private int timeout;

    public IhcAirlinkManagementService(String host, int timeout) {
        url = "https://" + host + "/ws/AirlinkManagementService";
        this.timeout = timeout;
        super.setConnectTimeout(timeout);
    }

    private String sendSoapQuery(String SoapAction, String query, int timeout)
            throws org.openhab.binding.ihc.ws.exeptions.IhcExecption {
        openConnection(url);
        try {
            setRequestProperty("SOAPAction", SoapAction);
            return sendQuery(query, timeout);
        } finally {
            closeConnection();
        }
    }

    /**
     * Query system information from the controller.
     *
     * @return system information.
     * @throws IhcExecption
     */
    public synchronized List<WSRFDevice> getDetectedDeviceList() throws IhcExecption {
        String response = sendSoapQuery("getDetectedDeviceList", emptyQuery, timeout);

        List<WSRFDevice> resourceValueList = new ArrayList<WSRFDevice>();

        try {
            NodeList nodeList = parseList(response,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getDetectedDeviceList1/ns1:arrayItem");

            if (nodeList != null) {
                for (int i = 0; i < nodeList.getLength(); i++) {
                    WSRFDevice newVal = parseResourceValue(nodeList.item(i));
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

    private WSRFDevice parseResourceValue(Node n) throws XPathExpressionException {

        WSRFDevice dev = new WSRFDevice();

        String batteryLevel = getValue(n, "ns1:batteryLevel");
        if (StringUtils.isNotBlank(batteryLevel)) {
            dev.setBatteryLevel(Integer.parseInt(batteryLevel));
        }

        String deviceType = getValue(n, "ns1:deviceType");
        if (StringUtils.isNotBlank(deviceType)) {
            dev.setDeviceType(Integer.parseInt(deviceType));
        }

        String serialNumber = getValue(n, "ns1:serialNumber");
        if (StringUtils.isNotBlank(serialNumber)) {
            dev.setSerialNumber(Long.parseLong(serialNumber));
        }

        String signalStrength = getValue(n, "ns1:signalStrength");
        if (StringUtils.isNotBlank(signalStrength)) {
            dev.setSignalStrength(Integer.parseInt(signalStrength));
        }

        String version = getValue(n, "ns1:version");
        if (StringUtils.isNotBlank(version)) {
            dev.setVersion(Integer.parseInt(version));
        }

        String detected = getValue(n, "ns1:detected");
        if (StringUtils.isNotBlank(detected)) {
            dev.setdetected(Boolean.valueOf(detected));
        }

        return dev;
    }
}
