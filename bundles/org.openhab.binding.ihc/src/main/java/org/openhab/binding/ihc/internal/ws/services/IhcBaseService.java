/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.internal.ws.http.IhcConnectionPool;
import org.openhab.binding.ihc.internal.ws.http.IhcHttpsClient;

/**
 * Base class for all IHC / ELKO services.
 *
 *
 * @author Pauli Anttila - Initial contribution
 */
public abstract class IhcBaseService extends IhcHttpsClient {

    // @formatter:off
    protected static final String EMPTY_QUERY =
              "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
            + " <soapenv:Body>\n"
            + " </soapenv:Body>\n"
            + "</soapenv:Envelope>";
    // @formatter:on

    private String url;
    private int timeout;

    public IhcBaseService(IhcConnectionPool ihcConnectionPool, int timeout, String host, String service) {
        super(ihcConnectionPool);
        this.timeout = timeout;
        this.url = createUrl(host, service);
    }

    private String createUrl(String host, String service) {
        return "https://" + host + "/ws/" + service;
    }

    protected String sendSoapQuery(String soapAction, String query) throws IhcExecption {
        return sendSoapQuery(soapAction, query, timeout);
    }

    protected String sendSoapQuery(String soapAction, String query, int timeout) throws IhcExecption {
        Map<String, String> reqProperties = null;
        if (soapAction != null) {
            reqProperties = new HashMap<>();
            reqProperties.put("SOAPAction", soapAction);
        }
        return sendQuery(url, reqProperties, query, timeout);
    }

    protected int getTimeout() {
        return timeout;
    }
}
