/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal.ws.services;

import org.openhab.binding.ihc.internal.ws.datatypes.WSTimeManagerSettings;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.internal.ws.http.IhcConnectionPool;

/**
 * Class to handle IHC / ELKO LS Controller's time service.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcTimeService extends IhcBaseService {

    public IhcTimeService(String host, int timeout, IhcConnectionPool ihcConnectionPool) {
        super(ihcConnectionPool);
        url = "https://" + host + "/ws/TimeManagerService";
        this.timeout = timeout;
        setConnectTimeout(timeout);
    }

    /**
     * Query time settings from the controller.
     *
     * @return time settings.
     * @throws IhcExecption
     */
    public synchronized WSTimeManagerSettings getTimeSettings() throws IhcExecption {
        String response = sendSoapQuery("getSettings", EMPTY_QUERY, timeout);
        return new WSTimeManagerSettings().parseXMLData(response);
    }
}
