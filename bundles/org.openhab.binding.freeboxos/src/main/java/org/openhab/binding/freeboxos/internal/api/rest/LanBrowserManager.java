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
package org.openhab.binding.freeboxos.internal.api.rest;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.UriBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.rest.APManager.LanAccessPoint;
import org.openhab.binding.freeboxos.internal.api.rest.LanBrowserManager.InterfacesResponse;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import inet.ipaddr.mac.MACAddress;

/**
 * The {@link LanBrowserManager} is the Java class used to handle api requests related to lan
 *
 * https://dev.freebox.fr/sdk/os/system/#
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class LanBrowserManager extends ListableRest<LanBrowserManager.Interface, InterfacesResponse> {
    private static final IPAddress NULL_IP = new IPAddressString("0.0.0.0").getAddress();
    private static final String PATH = "browser";
    private static final String INTERFACES = "interfaces";
    private static final String WOL_ACTION = "wol";

    protected static class HostsResponse extends Response<LanHost> {
    }

    protected static class InterfacesResponse extends Response<Interface> {
    }

    public static enum Source {
        DHCP,
        NETBIOS,
        MDNS,
        MDNS_SRV,
        UPNP,
        WSD,
        UNKNOWN;
    }

    public record HostName(@Nullable String name, Source source) {
    }

    protected static record Interface(String name, int hostCount) {
    }

    private static record WakeOnLineData(String mac, String password) {
    }

    private static enum Type {
        MAC_ADDRESS,
        UNKNOWN;
    }

    private static record L2Ident(MACAddress id, Type type) {
    }

    private static record L3Connectivity(String addr, Af af, boolean active, boolean reachable,
            ZonedDateTime lastActivity, ZonedDateTime lastTimeReachable, String model) {

        private static enum Af {
            IPV4,
            IPV6,
            UNKNOWN;
        }

        public IPAddress getIPAddress() {
            if (af != Af.UNKNOWN) {
                return new IPAddressString(addr).getAddress();
            }
            return NULL_IP;
        }
    }

    public static record HostIntf(LanHost host, Interface intf) {
    }

    private static enum HostType {
        WORKSTATION,
        LAPTOP,
        SMARTPHONE,
        TABLET,
        PRINTER,
        VG_CONSOLE,
        TELEVISION,
        NAS,
        IP_CAMERA,
        IP_PHONE,
        FREEBOX_PLAYER,
        FREEBOX_HD,
        FREEBOX_CRYSTAL,
        FREEBOX_MINI,
        FREEBOX_DELTA,
        FREEBOX_ONE,
        FREEBOX_WIFI,
        FREEBOX_POP,
        NETWORKING_DEVICE,
        MULTIMEDIA_DEVICE,
        CAR,
        OTHER,
        UNKNOWN;
    }

    public static record LanHost(String id, @Nullable String primaryName, HostType hostType, boolean primaryNameManual,
            L2Ident l2ident, @Nullable String vendorName, boolean persistent, boolean reachable,
            @Nullable ZonedDateTime lastTimeReachable, boolean active, @Nullable ZonedDateTime lastActivity,
            @Nullable ZonedDateTime firstActivity, List<HostName> names, List<L3Connectivity> l3connectivities,
            @Nullable LanAccessPoint accessPoint) {

        public @Nullable LanAccessPoint accessPoint() {
            return accessPoint;
        }

        public String vendorName() {
            String localVendor = vendorName;
            return localVendor != null ? localVendor : "Unknown";
        }

        public Optional<String> getPrimaryName() {
            return Optional.ofNullable(primaryName);
        }

        public Optional<String> getUPnPName() {
            return names.stream().filter(name -> name.source == Source.UPNP).findFirst().map(name -> name.name);
        }

        public MACAddress getMac() {
            if (Type.MAC_ADDRESS.equals(l2ident.type)) {
                return l2ident.id;
            }
            throw new IllegalArgumentException("This host does not seem to have a Mac Address. Weird.");
        }

        public @Nullable IPAddress getIpv4() {
            return l3connectivities.stream().filter(L3Connectivity::reachable).map(L3Connectivity::getIPAddress)
                    .filter(ip -> !ip.equals(NULL_IP) && ip.isIPv4()).findFirst().orElse(null);
        }

        public @Nullable ZonedDateTime getLastSeen() {
            ZonedDateTime localLastActivity = lastActivity;
            if (lastTimeReachable == null && localLastActivity == null) {
                return null;
            }
            if (lastTimeReachable == null) {
                return lastActivity;
            }
            if (localLastActivity == null) {
                return lastTimeReachable;
            } else {
                return localLastActivity.isAfter(lastTimeReachable) ? lastActivity : lastTimeReachable;
            }
        }
    }

    private final List<Interface> interfaces = new ArrayList<>();

    public LanBrowserManager(FreeboxOsSession session, UriBuilder uriBuilder) throws FreeboxException {
        super(session, LoginManager.Permission.NONE, InterfacesResponse.class, uriBuilder.path(PATH));
        listSubPath = INTERFACES;
    }

    private List<LanHost> getInterfaceHosts(String lanInterface) throws FreeboxException {
        return get(HostsResponse.class, lanInterface);
    }

    private @Nullable LanHost getHost(String lanInterface, String hostId) throws FreeboxException {
        return getSingle(HostsResponse.class, lanInterface, hostId);
    }

    // As the list of interfaces on the box may not change, we cache the result
    private List<Interface> getInterfaces() throws FreeboxException {
        if (interfaces.isEmpty()) {
            interfaces.addAll(getDevices());
        }
        return interfaces;
    }

    public synchronized List<LanHost> getHosts() throws FreeboxException {
        List<LanHost> hosts = new ArrayList<>();

        for (Interface intf : getInterfaces()) {
            hosts.addAll(getInterfaceHosts(intf.name()));
        }
        return hosts;
    }

    public Optional<HostIntf> getHost(MACAddress searched) throws FreeboxException {
        for (Interface intf : getInterfaces()) {
            LanHost host = getHost(intf.name(), "ether-" + searched.toColonDelimitedString());
            if (host != null) {
                return Optional.of(new HostIntf(host, intf));
            }
        }
        return Optional.empty();
    }

    public boolean wakeOnLan(MACAddress mac, String password) throws FreeboxException {
        Optional<HostIntf> target = getHost(mac);
        if (target.isPresent()) {
            post(new WakeOnLineData(mac.toColonDelimitedString(), password), GenericResponse.class, WOL_ACTION,
                    target.get().intf.name);
            return true;
        }
        return false;
    }
}
