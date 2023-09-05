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
import org.openhab.binding.freeboxos.internal.api.rest.LanBrowserManager.LanHost;

import inet.ipaddr.mac.MACAddress;

/**
 * The {@link APManager} is the Java class used to handle api requests related to wifi access points
 * provided by the Freebox Server
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class APManager extends ListableRest<APManager.WifiAp, APManager.APResponse> {
    private static final String PATH = "ap";
    private static final String STATIONS_PATH = "stations";

    protected static record WifiInformation(String ssid, String band, int signal) { // Valid RSSI goes from -120 to 0
    }

    public static record LanAccessPoint(String mac, String type, String uid, @Nullable String connectivityType,
            long rxBytes, // received bytes (from station to Freebox)
            long txBytes, // transmitted bytes (from Freebox to station)
            long txRate, // reception data rate (in bytes/s)
            long rxRate, // transmission data rate (in bytes/s)
            WifiInformation wifiInformation) {

        public int getSignal() {
            return wifiInformation.signal();
        }

        public @Nullable String getSsid() {
            return wifiInformation().ssid();
        }
    }

    private enum State {
        ASSOCIATED,
        AUTHENTICATED,
        UNKNOWN
    }

    public static record Station(String id, MACAddress mac, String bssid, @Nullable String hostname, LanHost host,
            State state, int inactive, int connDuration, //
            long rxBytes, // received bytes (from station to Freebox)
            long txBytes, // transmitted bytes (from Freebox to station)
            long txRate, // reception data rate (in bytes/s)
            long rxRate, // transmission data rate (in bytes/s)
            int signal) { // signal attenuation (in dB)

        public @Nullable String getSsid() {
            LanAccessPoint accessPoint = host.accessPoint();
            return accessPoint != null ? accessPoint.getSsid() : null;
        }

        public @Nullable ZonedDateTime getLastSeen() {
            return host.getLastSeen();
        }
    }

    protected static record ApStatus(ApState state, int channelWidth, int primaryChannel, int secondaryChannel,
            int dfsCacRemainingTime, boolean dfsDisabled) {
        private enum ApState {
            SCANNING, // Ap is probing wifi channels
            NO_PARAM, // Ap is not configured
            BAD_PARAM, // Ap has an invalid configuration
            DISABLED, // Ap is permanently disabled
            DISABLED_PLANNING, // Ap is currently disabled according to planning
            NO_ACTIVE_BSS, // Ap has no active BSS
            STARTING, // Ap is starting
            ACS, // Ap is selecting the best available channel
            HT_SCAN, // Ap is scanning for other access point
            DFS, // Ap is performing dynamic frequency selection
            ACTIVE, // Ap is active
            FAILED, // Ap has failed to start
            UNKNOWN
        }
    }

    protected static record WifiAp(int id, String name, ApStatus status) {
    }

    private class ApHostsResponse extends Response<Station> {
    }

    protected class APResponse extends Response<WifiAp> {
    }

    public APManager(FreeboxOsSession session, UriBuilder uriBuilder) throws FreeboxException {
        super(session, LoginManager.Permission.NONE, APResponse.class, uriBuilder.path(PATH));
    }

    private List<Station> getApStations(int apId) throws FreeboxException {
        return get(ApHostsResponse.class, Integer.toString(apId), STATIONS_PATH);
    }

    public List<Station> getStations() throws FreeboxException {
        List<Station> hosts = new ArrayList<>();
        for (WifiAp ap : getDevices()) {
            hosts.addAll(getApStations(ap.id));
        }
        return hosts;
    }

    public Optional<Station> getStation(MACAddress mac) throws FreeboxException {
        return getStations().stream().filter(host -> host.mac().equals(mac)).findFirst();
    }
}
