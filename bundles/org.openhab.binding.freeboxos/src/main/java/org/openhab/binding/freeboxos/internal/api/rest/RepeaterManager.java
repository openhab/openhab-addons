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

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.FreeboxException;
import org.openhab.binding.freeboxos.internal.api.Response;
import org.openhab.binding.freeboxos.internal.api.rest.LanBrowserManager.HostsResponse;
import org.openhab.binding.freeboxos.internal.api.rest.LanBrowserManager.LanHost;

import inet.ipaddr.mac.MACAddress;

/**
 * The {@link RepeaterManager} is the Java class used to handle api requests related to repeater
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class RepeaterManager extends ListableRest<RepeaterManager.Repeater, RepeaterManager.RepeaterResponse> {

    protected static class RepeaterResponse extends Response<Repeater> {
    }

    protected static class RepeaterLedResponse extends Response<RepeaterLed> {
    }

    public static record RepeaterLed(int id, boolean ledActivated) {
    }

    private static enum Connection {
        CONNECTED,
        DISCONNECTED,
        UNKNOWN;
    }

    private static enum Status {
        STARTING,
        RUNNING,
        REBOOTING,
        UPDATING,
        REBOOT_FAILURE,
        UPDATE_FAILURE,
        UNKNOWN;
    }

    public static enum Model {
        FBXWMR, // Répéteur Wifi
        UNKNOWN;
    }

    public static record Repeater(int id, boolean ledActivated, boolean enabled, MACAddress mainMac,
            Connection connection, ZonedDateTime bootTime, Status status, String name, String sn, String apiVer,
            ZonedDateTime lastSeen, Model model, String firmwareVersion) {

        public long getUptimeVal() {
            return Duration.between(bootTime, ZonedDateTime.now()).toSeconds();
        }
    }

    public RepeaterManager(FreeboxOsSession session) throws FreeboxException {
        super(session, LoginManager.Permission.NONE, RepeaterResponse.class,
                session.getUriBuilder().path(THING_REPEATER));
    }

    public List<LanHost> getRepeaterHosts(int id) throws FreeboxException {
        return get(HostsResponse.class, Integer.toString(id), THING_HOST);
    }

    public synchronized List<LanHost> getHosts() throws FreeboxException {
        List<LanHost> hosts = new ArrayList<>();
        for (Repeater rep : getDevices()) {
            if (Connection.CONNECTED.equals(rep.connection)) {
                hosts.addAll(getRepeaterHosts(rep.id));
            }
        }
        return hosts;
    }

    public Optional<LanHost> getHost(MACAddress mac) throws FreeboxException {
        return getHosts().stream().filter(host -> host.getMac().equals(mac)).findFirst();
    }

    public void reboot(int id) throws FreeboxException {
        post(Integer.toString(id), REBOOT_ACTION);
    }

    public Optional<RepeaterLed> led(int id, boolean enable) throws FreeboxException {
        RepeaterLed result = put(RepeaterLedResponse.class, new RepeaterLed(id, enable), Integer.toString(id));
        return Optional.ofNullable(result);
    }
}
