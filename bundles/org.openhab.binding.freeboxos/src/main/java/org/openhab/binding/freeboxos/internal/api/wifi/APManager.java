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
package org.openhab.binding.freeboxos.internal.api.wifi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.rest.ListableRest;
import org.openhab.binding.freeboxos.internal.api.wifi.AccessPoint.AccessPointResponse;
import org.openhab.binding.freeboxos.internal.api.wifi.AccessPoint.AccessPointsResponse;
import org.openhab.binding.freeboxos.internal.api.wifi.AccessPointHost.AccessPointHostsResponse;

/**
 * The {@link APManager} is the Java class used to handle api requests
 * related to access points
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class APManager extends ListableRest<AccessPoint, AccessPointResponse, AccessPointsResponse> {
    private static final String STATIONS_SUB_PATH = "stations";
    private static final String AP_SUB_PATH = "ap";

    public APManager(FreeboxOsSession session, UriBuilder uriBuilder) {
        super(session, AccessPointResponse.class, AccessPointsResponse.class, uriBuilder, AP_SUB_PATH);
    }

    private @Nullable List<AccessPointHost> getAccessPointHosts(int apId) throws FreeboxException {
        return getList(AccessPointHostsResponse.class, Integer.toString(apId), STATIONS_SUB_PATH);
    }

    public Map<String, AccessPointHost> getHostsMap() throws FreeboxException {
        Map<String, AccessPointHost> result = new HashMap<>();
        getHosts().stream().forEach(host -> {
            String mac = host.getMac();
            result.put(mac, host);
        });
        return result;
    }

    private List<AccessPointHost> getHosts() throws FreeboxException {
        List<AccessPointHost> hosts = new ArrayList<>();
        for (AccessPoint ap : getDevices()) {
            List<AccessPointHost> apHosts = getAccessPointHosts(ap.getId());
            if (apHosts != null) {
                hosts.addAll(apHosts);
            }
        }
        return hosts;
    }

    public Optional<AccessPointHost> getHost(String mac) throws FreeboxException {
        return getHosts().stream().filter(host -> host.getMac().equalsIgnoreCase(mac)).findFirst();
    }
}
