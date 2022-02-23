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
package org.openhab.binding.freeboxos.internal.api.lan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.lan.LanHost.LanHostsResponse;
import org.openhab.binding.freeboxos.internal.api.lan.LanInterface.LanInterfaceResponse;
import org.openhab.binding.freeboxos.internal.api.lan.LanInterface.LanInterfacesResponse;
import org.openhab.binding.freeboxos.internal.api.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.api.rest.ListableRest;

/**
 * The {@link LanBrowserManager} is the Java class used to handle api requests
 * related to lan
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LanBrowserManager extends ListableRest<LanInterface, LanInterfaceResponse, LanInterfacesResponse> {
    private static final String WOL_SUB_PATH = "wol";
    private static final String DEFAULT_INTF = "pub";
    private static final String INTERFACES_SUB_PATH = "interfaces";
    private static final String BROWSER_SUB_PATH = "browser";

    public LanBrowserManager(FreeboxOsSession session, UriBuilder uriBuilder) {
        super(session, LanInterfaceResponse.class, LanInterfacesResponse.class, uriBuilder, BROWSER_SUB_PATH);
        listSubPath = INTERFACES_SUB_PATH;
    }

    private List<LanHost> getInterfaceHosts(String lanInterface) throws FreeboxException {
        return getList(LanHostsResponse.class, lanInterface);
    }

    private synchronized List<LanHost> getHosts() throws FreeboxException {
        List<LanHost> hosts = new ArrayList<>();

        for (LanInterface intf : getDevices()) {
            String name = intf.getName();
            if (name != null) {
                List<LanHost> intfHosts = getInterfaceHosts(name);
                hosts.addAll(intfHosts);
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
        // TODO: default interface should be dynamically resolved
        post(wol, WOL_SUB_PATH, DEFAULT_INTF, host);
    }
}
