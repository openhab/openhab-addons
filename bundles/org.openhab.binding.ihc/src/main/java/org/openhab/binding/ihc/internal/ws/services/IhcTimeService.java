/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
        super(ihcConnectionPool, timeout, host, "TimeManagerService");
    }

    /**
     * Query time settings from the controller.
     *
     * @return time settings.
     * @throws IhcExecption
     */
    public synchronized WSTimeManagerSettings getTimeSettings() throws IhcExecption {
        String response = sendSoapQuery("getSettings", EMPTY_QUERY);
        return new WSTimeManagerSettings().parseXMLData(response);
    }
}
