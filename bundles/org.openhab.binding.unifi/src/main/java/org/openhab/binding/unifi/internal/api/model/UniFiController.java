/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.unifi.internal.api.model;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.unifi.internal.api.UniFiException;
import org.openhab.binding.unifi.internal.api.UniFiExpiredSessionException;
import org.openhab.binding.unifi.internal.api.UniFiNotAuthorizedException;
import org.openhab.binding.unifi.internal.api.cache.UniFiClientCache;
import org.openhab.binding.unifi.internal.api.cache.UniFiDeviceCache;
import org.openhab.binding.unifi.internal.api.cache.UniFiSiteCache;
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
 */
@NonNullByDefault
public class UniFiController {

    private final Logger logger = LoggerFactory.getLogger(UniFiController.class);

    private UniFiSiteCache sitesCache = new UniFiSiteCache();

    private UniFiDeviceCache devicesCache = new UniFiDeviceCache();

    private UniFiClientCache clientsCache = new UniFiClientCache();

    private UniFiClientCache insightsCache = new UniFiClientCache();

    private final HttpClient httpClient;

    private final String host;

    private final int port;

    private final String username;

    private final String password;

    private final Gson gson;

    public UniFiController(HttpClient httpClient, String host, int port, String username, String password) {
        this.httpClient = httpClient;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        UniFiSiteInstanceCreator siteInstanceCreator = new UniFiSiteInstanceCreator(this);
        UniFiDeviceInstanceCreator deviceInstanceCreator = new UniFiDeviceInstanceCreator(this);
        UniFiClientInstanceCreator clientInstanceCreator = new UniFiClientInstanceCreator(this);
        this.gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(UniFiSite.class, siteInstanceCreator)
                .registerTypeAdapter(UniFiDevice.class, deviceInstanceCreator)
                .registerTypeAdapter(UniFiClient.class, new UniFiClientDeserializer())
                .registerTypeAdapter(UniFiUnknownClient.class, clientInstanceCreator)
                .registerTypeAdapter(UniFiWiredClient.class, clientInstanceCreator)
                .registerTypeAdapter(UniFiWirelessClient.class, clientInstanceCreator).create();
    }

    // Public API

    public void start() throws UniFiException {
        login();
    }

    public void stop() throws UniFiException {
        logout();
    }

    public void login() throws UniFiException {
        UniFiControllerRequest<Void> req = newRequest(Void.class);
        req.setPath("/api/login");
        req.setBodyParameter("username", username);
        req.setBodyParameter("password", password);
        // scurb: Changed strict = false to make blocking feature work
        req.setBodyParameter("strict", false);
        req.setBodyParameter("remember", false);
        executeRequest(req);
    }

    public void logout() throws UniFiException {
        UniFiControllerRequest<Void> req = newRequest(Void.class);
        req.setPath("/logout");
        executeRequest(req);
    }

    public void refresh() throws UniFiException {
        synchronized (this) {
            sitesCache = getSites();
            devicesCache = getDevices();
            clientsCache = getClients();
            insightsCache = getInsights();
        }
    }

    // Site API

    public @Nullable UniFiSite getSite(@Nullable String id) {
        UniFiSite site = null;
        if (StringUtils.isNotBlank(id)) {
            synchronized (this) {
                site = sitesCache.get(id);
            }
            if (site == null) {
                logger.debug("Could not find a matching site for id = '{}'", id);
            }
        }
        return site;
    }

    // Device API

    public @Nullable UniFiDevice getDevice(@Nullable String id) {
        UniFiDevice device = null;
        if (StringUtils.isNotBlank(id)) {
            synchronized (this) {
                device = devicesCache.get(id);
            }
            if (device == null) {
                logger.debug("Could not find a matching device for id = '{}'", id);
            }
        }
        return device;
    }

    // Client API

    public @Nullable UniFiClient getClient(@Nullable String id) {
        UniFiClient client = null;
        if (StringUtils.isNotBlank(id)) {
            synchronized (this) {
                // mgb: first check active clients and fallback to insights if not found
                client = clientsCache.get(id);
                if (client == null) {
                    client = insightsCache.get(id);
                }
            }
            if (client == null) {
                logger.debug("Could not find a matching client for id = {}", id);
            }
        }
        return client;
    }

    protected void block(UniFiClient client, boolean blocked) throws UniFiException {
        UniFiControllerRequest<Void> req = newRequest(Void.class);
        req.setPath("/api/s/" + client.getSite().getName() + "/cmd/stamgr");
        req.setBodyParameter("cmd", blocked ? "block-sta" : "unblock-sta");
        req.setBodyParameter("mac", client.getMac());
        executeRequest(req);
    }

    protected void reconnect(UniFiClient client) throws UniFiException {
        UniFiControllerRequest<Void> req = newRequest(Void.class);
        req.setPath("/api/s/" + client.getSite().getName() + "/cmd/stamgr");
        req.setBodyParameter("cmd", "kick-sta");
        req.setBodyParameter("mac", client.getMac());
        executeRequest(req);
    }

    // Internal API

    private <T> UniFiControllerRequest<T> newRequest(Class<T> responseType) {
        return new UniFiControllerRequest<>(responseType, gson, httpClient, host, port);
    }

    private <T> @Nullable T executeRequest(UniFiControllerRequest<T> request) throws UniFiException {
        T result;
        try {
            result = request.execute();
        } catch (UniFiExpiredSessionException e) {
            login();
            result = executeRequest(request);
        } catch (UniFiNotAuthorizedException e) {
            logger.warn("Not Authorized! Please make sure your controller credentials have administrator rights");
            result = null;
        }
        return result;
    }

    private UniFiSiteCache getSites() throws UniFiException {
        UniFiControllerRequest<UniFiSite[]> req = newRequest(UniFiSite[].class);
        req.setPath("/api/self/sites");
        UniFiSite[] sites = executeRequest(req);
        UniFiSiteCache cache = new UniFiSiteCache();
        if (sites != null) {
            logger.debug("Found {} UniFi Site(s): {}", sites.length, lazyFormatAsList(sites));
            for (UniFiSite site : sites) {
                cache.put(site);
            }
        }
        return cache;
    }

    private UniFiDeviceCache getDevices() throws UniFiException {
        UniFiDeviceCache cache = new UniFiDeviceCache();
        Collection<UniFiSite> sites = sitesCache.values();
        for (UniFiSite site : sites) {
            cache.putAll(getDevices(site));
        }
        return cache;
    }

    private UniFiDeviceCache getDevices(UniFiSite site) throws UniFiException {
        UniFiControllerRequest<UniFiDevice[]> req = newRequest(UniFiDevice[].class);
        req.setPath("/api/s/" + site.getName() + "/stat/device");
        UniFiDevice[] devices = executeRequest(req);
        UniFiDeviceCache cache = new UniFiDeviceCache();
        if (devices != null) {
            logger.debug("Found {} UniFi Device(s): {}", devices.length, lazyFormatAsList(devices));
            for (UniFiDevice device : devices) {
                cache.put(device);
            }
        }
        return cache;
    }

    private UniFiClientCache getClients() throws UniFiException {
        UniFiClientCache cache = new UniFiClientCache();
        Collection<UniFiSite> sites = sitesCache.values();
        for (UniFiSite site : sites) {
            cache.putAll(getClients(site));
        }
        return cache;
    }

    private UniFiClientCache getClients(UniFiSite site) throws UniFiException {
        UniFiControllerRequest<UniFiClient[]> req = newRequest(UniFiClient[].class);
        req.setPath("/api/s/" + site.getName() + "/stat/sta");
        UniFiClient[] clients = executeRequest(req);
        UniFiClientCache cache = new UniFiClientCache();
        if (clients != null) {
            logger.debug("Found {} UniFi Client(s): {}", clients.length, lazyFormatAsList(clients));
            for (UniFiClient client : clients) {
                cache.put(client);
            }
        }
        return cache;
    }

    private UniFiClientCache getInsights() throws UniFiException {
        UniFiClientCache cache = new UniFiClientCache();
        Collection<UniFiSite> sites = sitesCache.values();
        for (UniFiSite site : sites) {
            cache.putAll(getInsights(site));
        }
        return cache;
    }

    private UniFiClientCache getInsights(UniFiSite site) throws UniFiException {
        UniFiControllerRequest<UniFiClient[]> req = newRequest(UniFiClient[].class);
        req.setPath("/api/s/" + site.getName() + "/stat/alluser");
        req.setQueryParameter("within", 168); // scurb: Changed to 7 days.
        UniFiClient[] clients = executeRequest(req);
        UniFiClientCache cache = new UniFiClientCache();
        if (clients != null) {
            logger.debug("Found {} UniFi Insights(s): {}", clients.length, lazyFormatAsList(clients));
            for (UniFiClient client : clients) {
                cache.put(client);
            }
        }
        return cache;
    }

    private static Object lazyFormatAsList(Object[] arr) {
        return new Object() {

            @Override
            public String toString() {
                String value = "";
                for (Object o : arr) {
                    value += "\n - " + o.toString();
                }
                return value;
            }

        };
    }

}
