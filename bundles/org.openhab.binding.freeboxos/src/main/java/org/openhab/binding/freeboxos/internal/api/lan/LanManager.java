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
import java.util.Optional;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.FreeboxOsSession;
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
    private static final String INTERFACES_SUB_PATH = "interfaces";
    private static final String LAN_SUB_PATH = "lan";
    private static final String BROWSER_SUB_PATH = "browser";

    private final Logger logger = LoggerFactory.getLogger(LanManager.class);
    private final List<LanInterface> interfaces = new ArrayList<>();
    private final NetworkMode networkMode;
    private final UriBuilder browserUriBuilder;

    public LanManager(FreeboxOsSession session) throws FreeboxException {
        super(LAN_SUB_PATH, session);
        browserUriBuilder = getUriBuilder().path(BROWSER_SUB_PATH);
        this.networkMode = getLanConfig().getMode();
    }

    public NetworkMode getNetworkMode() throws FreeboxException {
        return networkMode;
    }

    public LanConfig getLanConfig() throws FreeboxException {
        return get(LanConfigResponse.class, CONFIG_SUB_PATH);
    }

    /*
     * Interface list is not subject to frequent changes so I cache the result
     */
    private synchronized List<LanInterface> getLanInterfaces() throws FreeboxException {
        if (interfaces.isEmpty()) {
            UriBuilder myBuilder = browserUriBuilder.clone().path(INTERFACES_SUB_PATH);
            interfaces.addAll(getList(LanInterfacesResponse.class, myBuilder.build()));
        }
        return interfaces;
    }

    private List<LanHost> getInterfaceHosts(String lanInterface) throws FreeboxException {
        UriBuilder myBuilder = browserUriBuilder.clone().path(lanInterface);
        return getList(LanHostsConfigResponse.class, myBuilder.build());
    }

    private synchronized List<LanHost> getHosts() throws FreeboxException {
        List<LanHost> hosts = new ArrayList<>();

        for (LanInterface intf : getLanInterfaces()) {
            try {
                List<LanHost> intfHosts = getInterfaceHosts(intf.getName());
                hosts.addAll(intfHosts);
            } catch (FreeboxException e) {
                logger.warn("Error getting hosts on interface '{}'. Will renew interface list : {}", intf.getName(),
                        e.getMessage());
                interfaces.clear();
                return getHosts();
            }
        }
        return hosts;
    }

    public Map<String, LanHost> getHostsMap() throws FreeboxException {
        Map<String, LanHost> result = new HashMap<>();
        getHosts().stream().forEach(host -> {
            String mac = host.getMac();
            if (mac != null) {
                result.put(mac, host);
            }
        });
        return result;
    }

    public Optional<LanHost> getHost(String mac) throws FreeboxException {
        return Optional.ofNullable(getHostsMap().get(mac));
    }

    public void wakeOnLan(String host) throws FreeboxException {
        WakeOnLineData wol = new WakeOnLineData(host);
        post("wol/" + host, wol);
    }

    // Response classes
    private static class LanConfigResponse extends Response<LanConfig> {
    }

    private static class LanInterfacesResponse extends Response<List<LanInterface>> {
    }

    private static class LanHostsConfigResponse extends Response<List<LanHost>> {
    }
}
