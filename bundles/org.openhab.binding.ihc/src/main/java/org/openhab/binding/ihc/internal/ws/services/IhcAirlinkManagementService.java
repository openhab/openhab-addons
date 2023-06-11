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
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.openhab.binding.ihc.internal.ws.datatypes.WSRFDevice;
import org.openhab.binding.ihc.internal.ws.datatypes.XPathUtils;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.internal.ws.http.IhcConnectionPool;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class to handle IHC / ELKO LS Controller's airlink management service.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcAirlinkManagementService extends IhcBaseService {

    public IhcAirlinkManagementService(String host, int timeout, IhcConnectionPool ihcConnectionPool) {
        super(ihcConnectionPool, timeout, host, "AirlinkManagementService");
    }

    /**
     * Query system information from the controller.
     *
     * @return system information.
     * @throws IhcExecption
     */
    public synchronized List<WSRFDevice> getDetectedDeviceList() throws IhcExecption {
        String response = sendSoapQuery("getDetectedDeviceList", EMPTY_QUERY);

        List<WSRFDevice> resourceValueList = new ArrayList<>();

        try {
            NodeList nodeList = XPathUtils.parseList(response,
                    "/SOAP-ENV:Envelope/SOAP-ENV:Body/ns1:getDetectedDeviceList1/ns1:arrayItem");

            if (nodeList != null) {
                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    if (node != null) {
                        WSRFDevice dev = parseResourceValue(node);
                        if (dev != null) {
                            resourceValueList.add(dev);
                        }
                    }
                }
            } else {
                throw new IhcExecption("Illegal resource value notification response received");
            }
            return resourceValueList;
        } catch (IOException | XPathExpressionException | NumberFormatException e) {
            throw new IhcExecption("Error occured during XML data parsing", e);
        }
    }

    private WSRFDevice parseResourceValue(Node n) throws XPathExpressionException, NumberFormatException {
        try {
            int batteryLevel = Integer.parseInt(XPathUtils.getValueFromNode(n, "batteryLevel"));
            int deviceType = Integer.parseInt(XPathUtils.getValueFromNode(n, "deviceType"));
            long serialNumber = Long.parseLong(XPathUtils.getValueFromNode(n, "serialNumber"));
            int signalStrength = Integer.parseInt(XPathUtils.getValueFromNode(n, "signalStrength"));
            int version = Integer.parseInt(XPathUtils.getValueFromNode(n, "version"));
            boolean detected = Boolean.valueOf(XPathUtils.getValueFromNode(n, "detected"));
            return new WSRFDevice(batteryLevel, deviceType, serialNumber, signalStrength, version, detected);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
