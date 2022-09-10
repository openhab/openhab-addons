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
package org.openhab.binding.unifi.internal.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.unifi.internal.api.cache.UniFiControllerCache;
import org.openhab.binding.unifi.internal.api.dto.UnfiPortOverrideJsonElement;
import org.openhab.binding.unifi.internal.api.dto.UniFiClient;
import org.openhab.binding.unifi.internal.api.dto.UniFiDevice;
import org.openhab.binding.unifi.internal.api.dto.UniFiPortTuple;
import org.openhab.binding.unifi.internal.api.dto.UniFiSite;
import org.openhab.binding.unifi.internal.api.dto.UniFiUnknownClient;
import org.openhab.binding.unifi.internal.api.dto.UniFiWiredClient;
import org.openhab.binding.unifi.internal.api.dto.UniFiWirelessClient;
import org.openhab.binding.unifi.internal.api.dto.UniFiWlan;
import org.openhab.binding.unifi.internal.api.util.UnfiPortOverrideJsonElementDeserializer;
import org.openhab.binding.unifi.internal.api.util.UniFiClientDeserializer;
import org.openhab.binding.unifi.internal.api.util.UniFiClientInstanceCreator;
import org.openhab.binding.unifi.internal.api.util.UniFiDeviceInstanceCreator;
import org.openhab.binding.unifi.internal.api.util.UniFiSiteInstanceCreator;
import org.openhab.binding.unifi.internal.api.util.UniFiWlanInstanceCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link UniFiController} is the main communication point with an external instance of the Ubiquiti Networks
 * Controller Software.
 *
 * @author Matthew Bowman - Initial contribution
 * @author Patrik Wimnell - Blocking / Unblocking client support
 * @author Jacob Laursen - Fix online/blocked channels (broken by UniFi Controller 5.12.35)
 * @author Hilbrand Bouwkamp - Added POEPort support, moved generic cache related code to cache object
 */
@NonNullByDefault
public class UniFiController {

    private static final int INSIGHT_WITHIN_HOURS = 7 * 24; // scurb: Changed to 7 days.

    private final Logger logger = LoggerFactory.getLogger(UniFiController.class);

    private final HttpClient httpClient;
    private final UniFiControllerCache cache = new UniFiControllerCache();

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final boolean unifios;
    private final Gson gson;
    private final Gson poeGson;

    private String csrfToken;

    public UniFiController(final HttpClient httpClient, final String host, final int port, final String username,
            final String password, final boolean unifios) {
        this.httpClient = httpClient;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.unifios = unifios;
        this.csrfToken = "";
        final UniFiSiteInstanceCreator siteInstanceCreator = new UniFiSiteInstanceCreator(cache);
        final UniFiWlanInstanceCreator wlanInstanceCreator = new UniFiWlanInstanceCreator(cache);
        final UniFiDeviceInstanceCreator deviceInstanceCreator = new UniFiDeviceInstanceCreator(cache);
        final UniFiClientInstanceCreator clientInstanceCreator = new UniFiClientInstanceCreator(cache);
        this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(UniFiSite.class, siteInstanceCreator)
                .registerTypeAdapter(UniFiWlan.class, wlanInstanceCreator)
                .registerTypeAdapter(UniFiDevice.class, deviceInstanceCreator)
                .registerTypeAdapter(UniFiClient.class, new UniFiClientDeserializer())
                .registerTypeAdapter(UniFiUnknownClient.class, clientInstanceCreator)
                .registerTypeAdapter(UniFiWiredClient.class, clientInstanceCreator)
                .registerTypeAdapter(UniFiWirelessClient.class, clientInstanceCreator).create();
        this.poeGson = new GsonBuilder()
                .registerTypeAdapter(UnfiPortOverrideJsonElement.class, new UnfiPortOverrideJsonElementDeserializer())
                .create();
    }

    // Public API

    public void start() throws UniFiException {
        if (unifios) {
            obtainCsrfToken();
        }

        login();
    }

    public void stop() throws UniFiException {
        logout();
    }

    public void obtainCsrfToken() throws UniFiException {
        csrfToken = "";

        final UniFiControllerRequest<Void> req = newRequest(Void.class, HttpMethod.GET, gson);
        req.setPath("/");
        executeRequest(req);
    }

    public void login() throws UniFiException {
        final UniFiControllerRequest<Void> req = newRequest(Void.class, HttpMethod.POST, gson);
        req.setPath(unifios ? "/api/auth/login" : "/api/login");
        req.setBodyParameter("username", username);
        req.setBodyParameter("password", password);
        // scurb: Changed strict = false to make blocking feature work
        req.setBodyParameter("strict", false);
        req.setBodyParameter("remember", false);
        executeRequest(req, true);
    }

    public void logout() throws UniFiException {
        csrfToken = "";
        final UniFiControllerRequest<Void> req = newRequest(Void.class, HttpMethod.GET, gson);
        req.setPath(unifios ? "/api/auth/logout" : "/logout");
        executeRequest(req);
    }

    public void refresh() throws UniFiException {
        synchronized (this) {
            cache.clear();
            final Collection<UniFiSite> sites = refreshSites();
            refreshWlans(sites);
            refreshDevices(sites);
            refreshClients(sites);
            refreshInsights(sites);
        }
    }

    public UniFiControllerCache getCache() {
        return cache;
    }

    public @Nullable Map<Integer, UniFiPortTuple> getSwitchPorts(@Nullable final String deviceId) {
        return cache.getSwitchPorts(deviceId);
    }

    public void block(final UniFiClient client, final boolean blocked) throws UniFiException {
        final UniFiControllerRequest<Void> req = newRequest(Void.class, HttpMethod.POST, gson);
        req.setAPIPath(String.format("/api/s/%s/cmd/stamgr", client.getSite().getName()));
        req.setBodyParameter("cmd", blocked ? "block-sta" : "unblock-sta");
        req.setBodyParameter("mac", client.getMac());
        executeRequest(req);
        refresh();
    }

    public void reconnect(final UniFiClient client) throws UniFiException {
        final UniFiControllerRequest<Void> req = newRequest(Void.class, HttpMethod.POST, gson);
        req.setAPIPath(String.format("/api/s/%s/cmd/stamgr", client.getSite().getName()));
        req.setBodyParameter("cmd", "kick-sta");
        req.setBodyParameter("mac", client.getMac());
        executeRequest(req);
        refresh();
    }

    public boolean poeMode(final UniFiDevice device, final List<UnfiPortOverrideJsonElement> data)
            throws UniFiException {
        // Safety check to make sure no empty data is send to avoid corrupting override data on the device.
        if (data.isEmpty() || data.stream().anyMatch(p -> p.getJsonObject().entrySet().isEmpty())) {
            logger.info("Not overriding port for '{}', because port data contains empty json: {}", device.getName(),
                    poeGson.toJson(data));
            return false;
        } else {
            final UniFiControllerRequest<Void> req = newRequest(Void.class, HttpMethod.PUT, poeGson);
            req.setAPIPath(String.format("/api/s/%s/rest/device/%s", device.getSite().getName(), device.getId()));
            req.setBodyParameter("port_overrides", data);
            executeRequest(req);
            return true;
        }
    }

    public void poePowerCycle(final UniFiDevice device, final Integer portIdx) throws UniFiException {
        final UniFiControllerRequest<Void> req = newRequest(Void.class, HttpMethod.POST, gson);
        req.setAPIPath(String.format("/api/s/%s/cmd/devmgr", device.getSite().getName()));
        req.setBodyParameter("cmd", "power-cycle");
        req.setBodyParameter("mac", device.getMac());
        req.setBodyParameter("port_idx", portIdx);
        executeRequest(req);
        refresh();
    }

    public void enableWifi(final UniFiWlan wlan, final boolean enable) throws UniFiException {
        final UniFiControllerRequest<Void> req = newRequest(Void.class, HttpMethod.PUT, poeGson);
        req.setAPIPath(String.format("/api/s/%s/rest/wlanconf/%s", wlan.getSite().getName(), wlan.getId()));
        req.setBodyParameter("_id", wlan.getId());
        req.setBodyParameter("enabled", enable ? "true" : "false");
        executeRequest(req);
        refresh();
    }

    // Internal API

    private <T> UniFiControllerRequest<T> newRequest(final Class<T> responseType, final HttpMethod method,
            final Gson gson) {
        return new UniFiControllerRequest<>(responseType, gson, httpClient, method, host, port, csrfToken, unifios);
    }

    private <T> @Nullable T executeRequest(final UniFiControllerRequest<T> request) throws UniFiException {
        return executeRequest(request, false);
    }

    private <T> @Nullable T executeRequest(final UniFiControllerRequest<T> request, final boolean fromLogin)
            throws UniFiException {
        T result;
        try {
            result = request.execute();
            csrfToken = request.getCsrfToken();
        } catch (final UniFiExpiredSessionException e) {
            if (fromLogin) {
                // if this exception is thrown from a login attempt something is wrong, because the login should init
                // the session.
                throw new UniFiCommunicationException(e);
            } else {
                login();
                result = executeRequest(request);
            }
        } catch (final UniFiNotAuthorizedException e) {
            logger.warn("Not Authorized! Please make sure your controller credentials have administrator rights");
            result = null;
        }
        return result;
    }

    private List<UniFiSite> refreshSites() throws UniFiException {
        final UniFiControllerRequest<UniFiSite[]> req = newRequest(UniFiSite[].class, HttpMethod.GET, gson);
        req.setAPIPath("/api/self/sites");
        return cache.setSites(executeRequest(req));
    }

    private void refreshWlans(final Collection<UniFiSite> sites) throws UniFiException {
        for (final UniFiSite site : sites) {
            cache.putWlans(getWlans(site));
        }
    }

    private UniFiWlan @Nullable [] getWlans(final UniFiSite site) throws UniFiException {
        final UniFiControllerRequest<UniFiWlan[]> req = newRequest(UniFiWlan[].class, HttpMethod.GET, gson);
        req.setAPIPath(String.format("/api/s/%s/rest/wlanconf", site.getName()));
        return executeRequest(req);
    }

    private void refreshDevices(final Collection<UniFiSite> sites) throws UniFiException {
        for (final UniFiSite site : sites) {
            cache.putDevices(getDevices(site));
        }
    }

    private UniFiDevice @Nullable [] getDevices(final UniFiSite site) throws UniFiException {
        final UniFiControllerRequest<UniFiDevice[]> req = newRequest(UniFiDevice[].class, HttpMethod.GET, gson);
        req.setAPIPath(String.format("/api/s/%s/stat/device", site.getName()));
        return executeRequest(req);
    }

    private void refreshClients(final Collection<UniFiSite> sites) throws UniFiException {
        for (final UniFiSite site : sites) {
            cache.putClients(getClients(site));
        }
    }

    private UniFiClient @Nullable [] getClients(final UniFiSite site) throws UniFiException {
        final UniFiControllerRequest<UniFiClient[]> req = newRequest(UniFiClient[].class, HttpMethod.GET, gson);
        req.setAPIPath(String.format("/api/s/%s/stat/sta", site.getName()));
        return executeRequest(req);
    }

    private void refreshInsights(final Collection<UniFiSite> sites) throws UniFiException {
        for (final UniFiSite site : sites) {
            cache.putInsights(getInsights(site));
        }
    }

    private UniFiClient @Nullable [] getInsights(final UniFiSite site) throws UniFiException {
        final UniFiControllerRequest<UniFiClient[]> req = newRequest(UniFiClient[].class, HttpMethod.GET, gson);
        req.setAPIPath(String.format("/api/s/%s/stat/alluser", site.getName()));
        req.setQueryParameter("within", INSIGHT_WITHIN_HOURS);
        return executeRequest(req);
    }
}
