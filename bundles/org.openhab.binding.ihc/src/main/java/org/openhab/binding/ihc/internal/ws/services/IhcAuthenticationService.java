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

import org.openhab.binding.ihc.internal.ws.datatypes.WSLoginResult;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.internal.ws.http.IhcConnectionPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle IHC / ELKO LS Controller's authentication service.
 *
 * Communication to controller need to be authenticated. On successful
 * authentication Controller returns session id which need to be used further
 * communication.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcAuthenticationService extends IhcBaseService {
    private final Logger logger = LoggerFactory.getLogger(IhcAuthenticationService.class);

    public IhcAuthenticationService(String host, int timeout, IhcConnectionPool ihcConnectionPool) {
        super(ihcConnectionPool, timeout, host, "AuthenticationService");
    }

    public WSLoginResult authenticate(String username, String password, String application) throws IhcExecption {
        logger.debug("Authenticate");

        // @formatter:off
        final String soapQuery =
                  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n"
                + " <soapenv:Body>\n"
                + "  <authenticate1 xmlns=\"utcs\">\n"
                + "   <password>%s</password>\n"
                + "   <username>%s</username>\n"
                + "   <application>%s</application>\n"
                + "  </authenticate1>\n"
                + " </soapenv:Body>\n"
                + "</soapenv:Envelope>";
        // @formatter:on

        String query = String.format(soapQuery, password, username, application);
        String response = sendSoapQuery(null, query);
        return new WSLoginResult().parseXMLData(response);
    }
}
