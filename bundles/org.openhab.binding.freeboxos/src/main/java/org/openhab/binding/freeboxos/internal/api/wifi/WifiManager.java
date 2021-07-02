/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
import org.openhab.binding.freeboxos.internal.api.ApiHandler;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.ListResponse;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.RestManager;

/**
 * The {@link WifiManager} is the Java class used to handle api requests
 * related to wifi
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class WifiManager extends RestManager {
    private List<AccessPoint> accessPoints = new ArrayList<>();
    private final UriBuilder apBuilder;

    public WifiManager(ApiHandler apiHandler) {
        super(apiHandler, "wifi");
        apBuilder = getUriBuilder().path("ap");
    }

    public boolean getStatus() throws FreeboxException {
        return get("config", WifiGlobalConfigResponse.class, true).isEnabled();
    }

    public boolean setStatus(boolean enable) throws FreeboxException {
        WifiConfig config = new WifiConfig();
        return put("config", config, WifiGlobalConfigResponse.class).isEnabled();
    }

    private synchronized List<AccessPoint> getAccessPoints() throws FreeboxException {
        if (accessPoints.isEmpty()) {
            accessPoints.addAll(getList(apBuilder.build(), AccessPointsResponse.class, true));
        }
        return accessPoints;
    }

    private @Nullable List<AccessPointHost> getAccessPointHosts(int apId) throws FreeboxException {
        UriBuilder myBuilder = apBuilder.clone().path(Integer.toString(apId)).path("stations");
        return getList(myBuilder.build(), AccessPointHostsResponse.class, true);
    }

    public Map<String, @Nullable AccessPointHost> getHostsMap() throws FreeboxException {
        Map<String, @Nullable AccessPointHost> result = new HashMap<>();
        getHosts().stream().forEach(host -> {
            String mac = host.getMac();
            result.put(mac, host);
        });
        return result;
    }

    public List<AccessPointHost> getHosts() throws FreeboxException {
        List<AccessPointHost> hosts = new ArrayList<>();
        for (AccessPoint ap : getAccessPoints()) {
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

    // Response classes and validity evaluations
    private static class WifiGlobalConfigResponse extends Response<WifiConfig> {
    }

    private class AccessPointHostsResponse extends ListResponse<AccessPointHost> {
    }

    private class AccessPointsResponse extends ListResponse<AccessPoint> {
    }
}
