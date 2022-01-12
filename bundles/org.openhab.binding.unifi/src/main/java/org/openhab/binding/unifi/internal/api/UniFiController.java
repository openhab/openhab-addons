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
import org.openhab.binding.unifi.internal.api.model.UnfiPortOverride;
import org.openhab.binding.unifi.internal.api.model.UniFiClient;
import org.openhab.binding.unifi.internal.api.model.UniFiControllerRequest;
import org.openhab.binding.unifi.internal.api.model.UniFiDevice;
import org.openhab.binding.unifi.internal.api.model.UniFiPortTable;
import org.openhab.binding.unifi.internal.api.model.UniFiSite;
import org.openhab.binding.unifi.internal.api.model.UniFiUnknownClient;
import org.openhab.binding.unifi.internal.api.model.UniFiWiredClient;
import org.openhab.binding.unifi.internal.api.model.UniFiWirelessClient;
import org.openhab.binding.unifi.internal.api.util.UniFiClientDeserializer;
import org.openhab.binding.unifi.internal.api.util.UniFiClientInstanceCreator;
import org.openhab.binding.unifi.internal.api.util.UniFiDeviceInstanceCreator;
import org.openhab.binding.unifi.internal.api.util.UniFiSiteInstanceCreator;
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

    private static final int INSIGHT_WITH_HOURS = 7 * 24; // scurb: Changed to 7 days.

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
        final UniFiDeviceInstanceCreator deviceInstanceCreator = new UniFiDeviceInstanceCreator(cache);
        final UniFiClientInstanceCreator clientInstanceCreator = new UniFiClientInstanceCreator(cache);
        this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(UniFiSite.class, siteInstanceCreator)
                .registerTypeAdapter(UniFiDevice.class, deviceInstanceCreator)
                .registerTypeAdapter(UniFiClient.class, new UniFiClientDeserializer())
                .registerTypeAdapter(UniFiUnknownClient.class, clientInstanceCreator)
                .registerTypeAdapter(UniFiWiredClient.class, clientInstanceCreator)
                .registerTypeAdapter(UniFiWirelessClient.class, clientInstanceCreator).create();
        this.poeGson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .excludeFieldsWithoutExposeAnnotation().create();
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
            refreshDevices(sites);
            refreshClients(sites);
            refreshInsights(sites);
        }
    }

    public @Nullable UniFiSite getSite(final @Nullable String id) {
        return cache.getSite(id);
    }

    public @Nullable UniFiClient getClient(@Nullable final String cid) {
        return cache.getClient(cid);
    }

    public Collection<UniFiClient> getClients() {
        return cache.getClients();
    }

    public @Nullable UniFiDevice getDevice(@Nullable final String id) {
        return cache.getDevice(id);
    }

    public @Nullable Map<Integer, UniFiPortTable> getSwitchPorts(@Nullable final String deviceId) {
        return cache.getSwitchPorts(deviceId);
    }

    public Collection<Map<Integer, UniFiPortTable>> getSwitchPorts() {
        return cache.getSwitchPorts();
    }

    public void block(final UniFiClient client, final boolean blocked) throws UniFiException {
        final UniFiControllerRequest<Void> req = newRequest(Void.class, HttpMethod.POST, gson);
        req.setAPIPath("/api/s/" + client.getSite().getName() + "/cmd/stamgr");
        req.setBodyParameter("cmd", blocked ? "block-sta" : "unblock-sta");
        req.setBodyParameter("mac", client.getMac());
        executeRequest(req);
        refresh();
    }

    public void reconnect(final UniFiClient client) throws UniFiException {
        final UniFiControllerRequest<Void> req = newRequest(Void.class, HttpMethod.POST, gson);
        req.setAPIPath("/api/s/" + client.getSite().getName() + "/cmd/stamgr");
        req.setBodyParameter("cmd", "kick-sta");
        req.setBodyParameter("mac", client.getMac());
        executeRequest(req);
        refresh();
    }

    public void poeMode(final UniFiDevice device, final Map<Integer, UnfiPortOverride> data) throws UniFiException {
        final UniFiControllerRequest<Void> req = newRequest(Void.class, HttpMethod.PUT, poeGson);
        req.setAPIPath("/api/s/" + device.getSite().getName() + "/rest/device/" + device.getId());
        req.setBodyParameter("port_overrides", data.values());
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

    private void refreshDevices(final Collection<UniFiSite> sites) throws UniFiException {
        for (final UniFiSite site : sites) {
            cache.putDevices(getDevices(site));
        }
    }

    private UniFiDevice @Nullable [] getDevices(final UniFiSite site) throws UniFiException {
        final UniFiControllerRequest<UniFiDevice[]> req = newRequest(UniFiDevice[].class, HttpMethod.GET, gson);
        req.setAPIPath("/api/s/" + site.getName() + "/stat/device");
        return executeRequest(req);
    }

    private void refreshClients(final Collection<UniFiSite> sites) throws UniFiException {
        for (final UniFiSite site : sites) {
            cache.putClients(getClients(site));
        }
    }

    private UniFiClient @Nullable [] getClients(final UniFiSite site) throws UniFiException {
        final UniFiControllerRequest<UniFiClient[]> req = newRequest(UniFiClient[].class, HttpMethod.GET, gson);
        req.setAPIPath("/api/s/" + site.getName() + "/stat/sta");
        return executeRequest(req);
    }

    private void refreshInsights(final Collection<UniFiSite> sites) throws UniFiException {
        for (final UniFiSite site : sites) {
            cache.putInsights(getInsights(site));
        }
    }

    private UniFiClient @Nullable [] getInsights(final UniFiSite site) throws UniFiException {
        final UniFiControllerRequest<UniFiClient[]> req = newRequest(UniFiClient[].class, HttpMethod.GET, gson);
        req.setAPIPath("/api/s/" + site.getName() + "/stat/alluser");
        req.setQueryParameter("within", INSIGHT_WITH_HOURS);
        return executeRequest(req);
    }
}
