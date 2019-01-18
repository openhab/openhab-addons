/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.unifi.internal.handler;

import static org.eclipse.smarthome.core.thing.ThingStatus.OFFLINE;
import static org.eclipse.smarthome.core.thing.ThingStatus.ONLINE;
import static org.eclipse.smarthome.core.thing.ThingStatusDetail.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingStatusInfoBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.unifi.internal.UniFiBindingConstants;
import org.openhab.binding.unifi.internal.UniFiControllerThingConfig;
import org.openhab.binding.unifi.internal.api.UniFiCommunicationException;
import org.openhab.binding.unifi.internal.api.UniFiController;
import org.openhab.binding.unifi.internal.api.UniFiException;
import org.openhab.binding.unifi.internal.api.UniFiInvalidCredentialsException;
import org.openhab.binding.unifi.internal.api.UniFiInvalidHostException;
import org.openhab.binding.unifi.internal.api.UniFiSSLException;
import org.openhab.binding.unifi.internal.api.model.UniFiClient;
import org.openhab.binding.unifi.internal.api.model.UniFiDevice;
import org.openhab.binding.unifi.internal.api.model.UniFiSite;
import org.openhab.binding.unifi.internal.api.model.UniFiWirelessClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link UniFiControllerThingHandler} is responsible for handling commands and status
 * updates for the UniFi Controller.
 *
 * @author Matthew Bowman - Initial contribution
 */
@NonNullByDefault
public class UniFiControllerThingHandler extends BaseBridgeHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .of(UniFiBindingConstants.THING_TYPE_CONTROLLER).collect(Collectors.toSet());

    private static final String STATUS_DESCRIPTION_COMMUNICATION_ERROR = "Error communicating with the UniFi controller";

    private static final String STATUS_DESCRIPTION_SSL_ERROR = "Error establishing an SSL connection with the UniFi controller";

    private static final String STATUS_DESCRIPTION_INVALID_CREDENTIALS = "Invalid username and/or password - please double-check your configuration";

    private static final String STATUS_DESCRIPTION_INVALID_HOSTNAME = "Invalid hostname - please double-check your configuration";

    private static final String CACHE_KEY_PREFIX_MAC = "mac";

    private static final String CACHE_KEY_PREFIX_IP = "ip";

    private static final String CACHE_KEY_PREFIX_HOSTNAME = "hostname";

    private static final String CACHE_KEY_PREFIX_ALIAS = "alias";

    private static final List<String> CACHE_KEY_PREFIXES = Arrays.asList(CACHE_KEY_PREFIX_MAC, CACHE_KEY_PREFIX_IP,
            CACHE_KEY_PREFIX_HOSTNAME, CACHE_KEY_PREFIX_ALIAS);

    private static final String CACHE_KEY_SEPARATOR = ":";

    private final Logger logger = LoggerFactory.getLogger(UniFiControllerThingHandler.class);

    private @Nullable UniFiControllerThingConfig config;

    private @Nullable volatile UniFiController controller; /* mgb: volatile because accessed from multiple threads */

    private @Nullable ScheduledFuture<?> refreshJob;

    private Map<String, UniFiSite> sitesCache = Collections.emptyMap();

    private Map<String, UniFiDevice> devicesCache = Collections.emptyMap();

    private Map<String, UniFiClient> clientsCache = Collections.emptyMap();

    private Map<String, UniFiClient> insightsCache = Collections.emptyMap();

    private final HttpClient httpClient;

    public UniFiControllerThingHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    // Public API

    @Override
    public void initialize() {
        // mgb: called when the config changes
        cancelRefreshJob();
        config = getConfig().as(UniFiControllerThingConfig.class);
        logger.debug("Initializing the UniFi Controller Handler with config = {}", config);
        try {
            controller = new UniFiController(httpClient, config.getHost(), config.getPort(), config.getUsername(),
                    config.getPassword());
            controller.start();
            updateStatus(ONLINE);
        } catch (UniFiInvalidHostException e) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, STATUS_DESCRIPTION_INVALID_HOSTNAME);
        } catch (UniFiCommunicationException e) {
            updateStatus(OFFLINE, COMMUNICATION_ERROR, STATUS_DESCRIPTION_COMMUNICATION_ERROR);
        } catch (UniFiSSLException e) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, STATUS_DESCRIPTION_SSL_ERROR);
        } catch (UniFiInvalidCredentialsException e) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, STATUS_DESCRIPTION_INVALID_CREDENTIALS);
        } catch (UniFiException e) {
            logger.error("Unknown error while configuring the UniFi Controller", e);
            updateStatus(OFFLINE, CONFIGURATION_ERROR, e.getMessage());
        }
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        if (status == ONLINE || (status == OFFLINE && statusDetail == COMMUNICATION_ERROR)) {
            scheduleRefreshJob();
        } else if (status == OFFLINE && statusDetail == CONFIGURATION_ERROR) {
            cancelRefreshJob();
        }
        // mgb: update the status only if it's changed
        ThingStatusInfo statusInfo = ThingStatusInfoBuilder.create(status, statusDetail).withDescription(description)
                .build();
        if (!statusInfo.equals(getThing().getStatusInfo())) {
            super.updateStatus(status, statusDetail, description);
        }
    }

    @Override
    public void dispose() {
        cancelRefreshJob();
        if (controller != null) {
            try {
                controller.stop();
            } catch (UniFiException e) {
                // mgb: nop as we're in dispose
            }
            controller = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // nop - read-only binding
        logger.debug("Ignoring command = {} for channel = {} - the UniFi binding is read-only!", command, channelUID);
    }

    public int getRefreshInterval() {
        return config.getRefresh();
    }

    private void cachePut(Map<String, UniFiClient> cache, UniFiClient client) {
        synchronized (cache) {
            for (String prefix : CACHE_KEY_PREFIXES) {
                String suffix = null;
                switch (prefix) {
                    case CACHE_KEY_PREFIX_MAC:
                        suffix = client.getMac();
                        break;
                    case CACHE_KEY_PREFIX_IP:
                        suffix = client.getIp();
                        break;
                    case CACHE_KEY_PREFIX_HOSTNAME:
                        suffix = client.getHostname();
                        break;
                    case CACHE_KEY_PREFIX_ALIAS:
                        suffix = client.getAlias();
                        break;
                }
                if (StringUtils.isNotBlank(suffix)) {
                    String key = prefix + CACHE_KEY_SEPARATOR + suffix;
                    cache.put(key, client);
                }
            }
        }
    }

    private @Nullable UniFiClient cacheGet(Map<String, UniFiClient> cache, String cid) {
        UniFiClient client = null;
        synchronized (cache) {
            for (String prefix : CACHE_KEY_PREFIXES) {
                String key = prefix + CACHE_KEY_SEPARATOR + cid;
                if (cache.containsKey(key)) {
                    client = cache.get(key);
                    logger.debug("Found client '{}' = {}", key, client);
                    break;
                }
            }
        }
        return client;
    }

    public @Nullable UniFiClient getClient(String cid, String site) {
        // mgb: first check active clients and fallback to insights if not found
        UniFiClient client = null;

        // mgb: first check active clients and fallback to insights if not found
        client = cacheGet(clientsCache, cid);
        if (client == null) {
            client = cacheGet(insightsCache, cid);
        }

        // mgb: short circuit
        if (client == null || BooleanUtils.isNotTrue(client.isWireless()) || !belongsToSite(client, site)) {
            return null;
        }

        // mgb: instanceof check just for type / cast safety
        return (client instanceof UniFiWirelessClient ? (UniFiWirelessClient) client : null);
    }

    // Private API

    private void scheduleRefreshJob() {
        synchronized (this) {
            if (refreshJob == null) {
                logger.debug("Scheduling refresh job every {}s", config.getRefresh());
                refreshJob = scheduler.scheduleWithFixedDelay(this::run, 0, config.getRefresh(), TimeUnit.SECONDS);
            }
        }
    }

    private void cancelRefreshJob() {
        synchronized (this) {
            if (refreshJob != null) {
                logger.debug("Cancelling refresh job");
                refreshJob.cancel(true);
                refreshJob = null;
            }
        }
    }

    private void run() {
        try {
            logger.trace("Executing refresh job");
            refresh();
            updateStatus(ONLINE);
        } catch (UniFiCommunicationException e) {
            updateStatus(OFFLINE, COMMUNICATION_ERROR, STATUS_DESCRIPTION_COMMUNICATION_ERROR);
        } catch (UniFiInvalidCredentialsException e) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, STATUS_DESCRIPTION_INVALID_CREDENTIALS);
        } catch (Exception e) {
            logger.warn("Unhandled exception while refreshing the UniFi Controller {} - {}", getThing().getUID(),
                    e.getMessage());
            updateStatus(OFFLINE, COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void refresh() throws UniFiException {
        if (controller != null) {
            logger.debug("Refreshing the UniFi Controller {}", getThing().getUID());
            // mgb: refresh the controller thing
            synchronized (this) {
                sitesCache = getSites();
                devicesCache = getDevices();
                clientsCache = getClients();
                insightsCache = getInsights();
            }
            // mgb: then refresh all the client things
            getThing().getThings().forEach((thing) -> {
                if (thing.getHandler() instanceof UniFiClientThingHandler) {
                    ((UniFiClientThingHandler) thing.getHandler()).refresh();
                }
            });
        }
    }

    private Map<String, UniFiSite> getSites() throws UniFiException {
        Map<String, UniFiSite> siteMap = new HashMap<>();
        UniFiSite[] sites = controller.getSites();
        logger.debug("Found {} UniFi Site(s): {}", sites.length, lazyFormatAsList(sites));
        for (UniFiSite site : sites) {
            siteMap.put(site.getId(), site);
        }
        return siteMap;
    }

    private Map<String, UniFiDevice> getDevices() throws UniFiException {
        Map<String, UniFiDevice> deviceMap = new HashMap<>();
        Collection<UniFiSite> sites = sitesCache.values();
        for (UniFiSite site : sites) {
            Map<String, UniFiDevice> devices = getDevices(site);
            deviceMap.putAll(devices);
        }
        return deviceMap;
    }

    private Map<String, UniFiDevice> getDevices(UniFiSite site) throws UniFiException {
        Map<String, UniFiDevice> deviceMap = new HashMap<>();
        UniFiDevice[] devices = controller.getDevices(site);
        logger.debug("Found {} UniFi Device(s): {}", devices.length, lazyFormatAsList(devices));
        for (UniFiDevice device : devices) {
            device.setSite(site);
            deviceMap.put(device.getMac(), device);
        }
        return deviceMap;
    }

    private Map<String, UniFiClient> getClients() throws UniFiException {
        Map<String, UniFiClient> clientMap = new HashMap<>();
        Collection<UniFiSite> sites = sitesCache.values();
        for (UniFiSite site : sites) {
            Map<String, UniFiClient> siteClientMap = getClients(site);
            clientMap.putAll(siteClientMap);
        }
        return clientMap;
    }

    private Map<String, UniFiClient> getClients(UniFiSite site) throws UniFiException {
        Map<String, UniFiClient> clientMap = new HashMap<>();
        UniFiClient[] clients = controller.getClients(site);
        logger.debug("Found {} UniFi Client(s): {}", clients.length, lazyFormatAsList(clients));
        for (UniFiClient client : clients) {
            client.setDevice(devicesCache.get(client.getDeviceMac()));
            cachePut(clientMap, client);
        }
        return clientMap;
    }

    private Map<String, UniFiClient> getInsights() throws UniFiException {
        Map<String, UniFiClient> insightsMap = new HashMap<>();
        Collection<UniFiSite> sites = sitesCache.values();
        for (UniFiSite site : sites) {
            Map<String, UniFiClient> siteInsightsMap = getInsights(site);
            insightsMap.putAll(siteInsightsMap);
        }
        return insightsMap;
    }

    private Map<String, UniFiClient> getInsights(UniFiSite site) throws UniFiException {
        Map<String, UniFiClient> insightsMap = new HashMap<>();
        UniFiClient[] clients = controller.getInsights(site);
        logger.debug("Found {} UniFi Insights(s): {}", clients.length, lazyFormatAsList(clients));
        for (UniFiClient client : clients) {
            cachePut(insightsMap, client);
        }
        return insightsMap;
    }

    private boolean belongsToSite(UniFiClient client, String siteName) {
        boolean result = true; // mgb: assume true = proof by contradiction
        if (StringUtils.isNotEmpty(siteName)) {
            UniFiSite site = sitesCache.get(client.getSiteId());
            // mgb: if the 'site' can't be found or the name doesn't match...
            if (site == null || !site.matchesName(siteName)) {
                // mgb: ... then the client doesn't belong to this thing's configured 'site' and we 'filter' it
                result = false;
            }
        }
        return result;
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
