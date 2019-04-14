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

        List<WSRFDevice> resourceValueList = new ArrayList<WSRFDevice>();

        try {
            NodeList nodeList = XPathUtils.parseList(response,
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
        } catch (IOException | XPathExpressionException | NumberFormatException e) {
            throw new IhcExecption("Error occured during XML data parsing", e);
        }
    }

    private WSRFDevice parseResourceValue(Node n) throws XPathExpressionException, NumberFormatException {
        String batteryLevel = XPathUtils.getValueFromNode(n, "batteryLevel");
        String deviceType = XPathUtils.getValueFromNode(n, "deviceType");
        String serialNumber = XPathUtils.getValueFromNode(n, "serialNumber");
        String signalStrength = XPathUtils.getValueFromNode(n, "signalStrength");
        String version = XPathUtils.getValueFromNode(n, "version");
        String detected = XPathUtils.getValueFromNode(n, "detected");
        return new WSRFDevice(Integer.parseInt(batteryLevel), Integer.parseInt(deviceType),
                Long.parseLong(serialNumber), Integer.parseInt(signalStrength), Integer.parseInt(version),
                Boolean.valueOf(detected));
    }
}
