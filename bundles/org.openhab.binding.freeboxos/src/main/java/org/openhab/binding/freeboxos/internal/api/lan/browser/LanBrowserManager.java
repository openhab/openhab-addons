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
package org.openhab.binding.freeboxos.internal.api.lan.browser;

import static org.openhab.binding.freeboxos.internal.api.ApiConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.ApiConstants.Permission;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.rest.FreeboxOsSession;
import org.openhab.binding.freeboxos.internal.rest.ListableRest;

/**
 * The {@link LanBrowserManager} is the Java class used to handle api requests related to lan
 *
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LanBrowserManager extends ListableRest<LanInterface, LanBrowserManager.InterfaceResponse> {
    public static class HostsResponse extends Response<LanHost> {
    }

    public static class HostResponse extends Response<LanHost> {
    }

    public static class InterfaceResponse extends Response<LanInterface> {
    }

    private final List<LanInterface> interfaces = new ArrayList<>();

    public LanBrowserManager(FreeboxOsSession session, UriBuilder uriBuilder) throws FreeboxException {
        super(session, Permission.NONE, InterfaceResponse.class, uriBuilder.path(BROWSER_SUB_PATH));
        listSubPath = INTERFACES_SUB_PATH;
    }

    private List<LanHost> getInterfaceHosts(String lanInterface) throws FreeboxException {
        return get(HostsResponse.class, lanInterface);
    }

    private @Nullable LanHost getHost(String lanInterface, String hostId) throws FreeboxException {
        return getSingle(HostResponse.class, lanInterface, hostId);
    }

    // As the list of interfaces on the box does not change, we cache the result once for a while
    private List<LanInterface> getInterfaces() throws FreeboxException {
        if (interfaces.isEmpty()) {
            interfaces.addAll(getDevices());
        }
        return interfaces;
    }

    public synchronized List<LanHost> getHosts() throws FreeboxException {
        List<LanHost> hosts = new ArrayList<>();

        for (LanInterface intf : getInterfaces()) {
            hosts.addAll(getInterfaceHosts(intf.getName()));
        }
        return hosts;
    }

    public Optional<LanHost> getHost(String mac) throws FreeboxException {
        for (LanInterface intf : getInterfaces()) {
            LanHost host = getHost(intf.getName(), "ether-" + mac);
            if (host != null) {
                return Optional.of(host);
            }
        }
        return Optional.empty();
    }

    public void wakeOnLan(String mac) throws FreeboxException {
        for (LanInterface intf : getInterfaces()) {
            String name = intf.getName();
            LanHost host = getHost(name, "ether-" + mac);
            if (host != null) {
                post(new WakeOnLineData(mac), WOL_SUB_PATH, name, mac);
            }
        }
    }
}
