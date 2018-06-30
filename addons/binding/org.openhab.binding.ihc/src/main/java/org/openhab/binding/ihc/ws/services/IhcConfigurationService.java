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

/**
 * Class to handle IHC / ELKO LS Controller's configuration service.
 *
 * Controller service is used to fetch system information from the controller.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcConfigurationService extends IhcBaseService {

    public IhcConfigurationService(String host, int timeout) {
        url = "https://" + host + "/ws/ConfigurationService";
        this.timeout = timeout;
        setConnectTimeout(timeout);
    }

    /**
     * Query system information from the controller.
     *
     * @return system information.
     * @throws IhcExecption
     */
    public synchronized WSSystemInfo getSystemInfo() throws IhcExecption {
        String response = sendSoapQuery("getSystemInfo", EMPTY_QUERY, timeout);
        return new WSSystemInfo().parseXMLData(response);
    }
}
