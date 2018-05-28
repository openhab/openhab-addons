/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.ws.services;

import org.openhab.binding.ihc.ws.datatypes.WSSystemInfo;
import org.openhab.binding.ihc.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.ws.http.IhcHttpsClient;

/**
 * Class to handle IHC / ELKO LS Controller's controller service.
 *
 * Controller service is used to fetch information from the controller.
 * E.g. Project file or controller status.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcConfigurationService extends IhcHttpsClient {

    // @formatter:off
    private static String emptyQuery =
              "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
            + " <soapenv:Body>\n"
            + " </soapenv:Body>\n"
            + "</soapenv:Envelope>";
    // @formatter:on

    private String url;
    private int timeout;

    public IhcConfigurationService(String host, int timeout) {
        url = "https://" + host + "/ws/ConfigurationService";
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
    public synchronized WSSystemInfo getSystemInfo() throws IhcExecption {
        String response = sendSoapQuery("getSystemInfo", emptyQuery, timeout);
        return new WSSystemInfo().parseXMLData(response);
    }
}
