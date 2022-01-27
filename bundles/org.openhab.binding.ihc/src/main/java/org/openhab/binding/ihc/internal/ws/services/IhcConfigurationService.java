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

import org.openhab.binding.ihc.internal.ws.datatypes.WSSystemInfo;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.internal.ws.http.IhcConnectionPool;

/**
 * Class to handle IHC / ELKO LS Controller's configuration service.
 *
 * Controller service is used to fetch system information from the controller.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcConfigurationService extends IhcBaseService {

    public IhcConfigurationService(String host, int timeout, IhcConnectionPool ihcConnectionPool) {
        super(ihcConnectionPool, timeout, host, "ConfigurationService");
    }

    /**
     * Query system information from the controller.
     *
     * @return system information.
     * @throws IhcExecption
     */
    public synchronized WSSystemInfo getSystemInfo() throws IhcExecption {
        String response = sendSoapQuery("getSystemInfo", EMPTY_QUERY);
        return new WSSystemInfo().parseXMLData(response);
    }
}
