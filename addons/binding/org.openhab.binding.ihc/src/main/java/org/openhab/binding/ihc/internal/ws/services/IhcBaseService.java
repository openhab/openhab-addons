/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal.ws.services;

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
    protected final static String EMPTY_QUERY =
              "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
            + " <soapenv:Body>\n"
            + " </soapenv:Body>\n"
            + "</soapenv:Envelope>\n";
    // @formatter:on

    protected String url;
    protected int timeout;

    public IhcBaseService(IhcConnectionPool ihcConnectionPool) {
        super(ihcConnectionPool);
    }

    protected String sendSoapQuery(String soapAction, String query, int timeout) throws IhcExecption {
        openConnection(url);
        try {
            if (soapAction != null) {
                setRequestProperty("SOAPAction", soapAction);
            }
            return sendQuery(query, timeout);
        } finally {
            closeConnection();
        }
    }
}
