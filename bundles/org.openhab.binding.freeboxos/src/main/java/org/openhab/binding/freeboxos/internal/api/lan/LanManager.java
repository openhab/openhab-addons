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
package org.openhab.binding.freeboxos.internal.api.lan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiHandler;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.ListResponse;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.RestManager;
import org.openhab.binding.freeboxos.internal.api.lan.LanConfig.NetworkMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LanManager} is the Java class used to handle api requests
 * related to lan
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LanManager extends RestManager {
    private final Logger logger = LoggerFactory.getLogger(LanManager.class);
    private final List<LanInterface> interfaces = new ArrayList<>();
    private @NonNullByDefault({}) NetworkMode networkMode;
    private final UriBuilder browserBuilder;

    public LanManager(ApiHandler apiHandler) {
        super(apiHandler, "lan");
        browserBuilder = getUriBuilder().path("browser");
    }

    public NetworkMode getNetworkMode() throws FreeboxException {
        if (networkMode == null) {
            this.networkMode = getLanConfig().getType();
        }
        return networkMode;
    }

    public LanConfig getLanConfig() throws FreeboxException {
        return get("config", LanConfigResponse.class, true);
    }

    /*
     * Interface list is not subject to frequent changes so I cache the result
     */
    private synchronized List<LanInterface> getLanInterfaces() throws FreeboxException {
        if (interfaces.isEmpty()) {
            UriBuilder myBuilder = browserBuilder.clone().path("interfaces");
            interfaces.addAll(getList(myBuilder.build(), LanInterfacesResponse.class, true));
        }
        return interfaces;
    }

    private List<LanHost> getInterfaceHosts(String lanInterface) throws FreeboxException {
        UriBuilder myBuilder = browserBuilder.clone().path(lanInterface);
        return getList(myBuilder.build(), LanHostsConfigResponse.class, true);
    }

    private synchronized List<LanHost> getHosts() throws FreeboxException {
        List<LanHost> hosts = new ArrayList<>();

        for (LanInterface intf : getLanInterfaces()) {
            try {
                List<LanHost> intfHosts = getInterfaceHosts(intf.getName());
                hosts.addAll(intfHosts);
            } catch (FreeboxException e) {
                logger.warn("Error getting hosts on interface '{}'. Will renew interface list : {}", intf.getName(), e);
                interfaces.clear();
                return getHosts();
            }
        }
        return hosts;
    }

    public Map<String, @Nullable LanHost> getHostsMap() throws FreeboxException {
        Map<String, @Nullable LanHost> result = new HashMap<>();
        getHosts().stream().forEach(host -> {
            String mac = host.getMac();
            if (mac != null) {
                result.put(mac, host);
            }
        });
        return result;
    }

    public void wakeOnLan(String host) throws FreeboxException {
        WakeOnLineData wol = new WakeOnLineData(host);
        post("wol/" + host, wol);
    }

    // Response classes and validity evaluations
    private static class LanConfigResponse extends Response<LanConfig> {
    }

    private static class LanInterfacesResponse extends ListResponse<LanInterface> {
    }

    private static class LanHostsConfigResponse extends ListResponse<LanHost> {
    }
}
