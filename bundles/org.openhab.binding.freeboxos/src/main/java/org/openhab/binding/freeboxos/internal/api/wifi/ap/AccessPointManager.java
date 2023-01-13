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
package org.openhab.binding.freeboxos.internal.api.wifi.ap;

import static org.openhab.binding.freeboxos.internal.api.ApiConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.wifi.ap.AccessPointResponses.AccessPointResponse;
import org.openhab.binding.freeboxos.internal.api.wifi.ap.AccessPointResponses.AccessPointsResponse;
import org.openhab.binding.freeboxos.internal.api.wifi.ap.AccessPointResponses.ApHostsResponse;
import org.openhab.binding.freeboxos.internal.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.rest.ListableRest;

/**
 * The {@link AccessPointManager} is the Java class used to handle api requests related to wifi access points
 * provided by the Freebox Server
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AccessPointManager extends ListableRest<WifiAp, AccessPointResponse, AccessPointsResponse> {

    public AccessPointManager(FreeboxOsSession session, UriBuilder uriBuilder) {
        super(session, AccessPointResponse.class, AccessPointsResponse.class, uriBuilder, AP_SUB_PATH);
    }

    private List<WifiStation> getApStations(int apId) throws FreeboxException {
        return getList(ApHostsResponse.class, Integer.toString(apId), STATIONS_SUB_PATH);
    }

    public List<WifiStation> getStations() throws FreeboxException {
        List<WifiStation> hosts = new ArrayList<>();
        for (WifiAp ap : getDevices()) {
            hosts.addAll(getApStations(ap.getId()));
        }
        return hosts;
    }

    public Optional<WifiStation> getStation(String mac) throws FreeboxException {
        return getStations().stream().filter(host -> host.getMac().equalsIgnoreCase(mac)).findFirst();
    }
}
