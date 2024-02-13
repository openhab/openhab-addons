package org.openhab.binding.huesync.internal.discovery;

/**
 * Copyright (c) 2024-2024 Contributors to the openHAB project
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

import java.util.Collections;
import java.util.Dictionary;
import java.util.Objects;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.huesync.internal.HueSyncBindingConstants;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HueSyncDiscoveryParticipant} is responsible for discovering
 * the remote huesync.boxes using mDNS discovery service.
 *
 * @author Marco Kawon - Initial contribution
 * @author Patrik Gfeller - Integration into official repository, update to 4.x
 *         infrastructure
 * 
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class, configurationPid = "mdnsdiscovery.huesync")
public class HueSyncDiscoveryParticipant implements MDNSDiscoveryParticipant {
    @SuppressWarnings("null")
    private Logger logger = LoggerFactory.getLogger(HueSyncDiscoveryParticipant.class);

    /**
     *
     * Match the hostname + identifier of the discovered huesync-box.
     * Input is like "HueSyncBox-XXXXXXXXXXXX._huesync._tcp.local."
     * 
     * @see·<a·href=
     * "https://www.iana.org/assignments/service-names-port-numbers/service-names-port-numbers.xhtml?search=huesync">
     * Service·Name·and·Transport·Protocol·Port·Number·Registry</a>
     */
    private static final String SERVICE_TYPE = "_huesync._tcp.local.";
    private static final String DEVICE_INFO_ENDPOINT = "api/v1/device";

    // TODO: Implement SSL certificate validation
    private static final HttpClient httpClient = new HttpClient(new SslContextFactory.Client(true));
    // TODO: Get from configuration
    private boolean autoDiscoveryEnabled = true;

    protected final ThingRegistry thingRegistry;

    @Activate
    public HueSyncDiscoveryParticipant(final @Reference ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    @SuppressWarnings("null")
    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(HueSyncBindingConstants.THING_TYPE);
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        if (this.autoDiscoveryEnabled) {
            ThingUID uid = getThingUID(service);
            if (Objects.nonNull(uid)) {
                try {
                    String qualifiedName = service.getQualifiedName();

                    logger.debug("HueSync Device found: {}", qualifiedName);

                    String request = String.format("https://%s:%s/%s", service.getHostAddresses()[0], service.getPort(),
                            DEVICE_INFO_ENDPOINT);
                    ContentResponse response = HueSyncDiscoveryParticipant.httpClient.GET(request);

                    logger.debug("Device information for {}: {}", qualifiedName, response);
                } catch (Exception e) {
                    logger.error("Unable to query device information for {}: {}", service.getQualifiedName(), e);
                }
            }
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        String id = service.getName();
        String[] addressses = service.getHostAddresses();

        if (addressses.length == 0 || id == null || id.isBlank()) {
            logger.warn("Incomplete mDNS device discovery information - {} ignored.", id == null ? "[name: null]" : id);
            return null;
        }
        return new ThingUID(HueSyncBindingConstants.THING_TYPE, id);
    }

    @Activate
    protected void activate(ComponentContext componentContext) {
        try {
            httpClient.start();

            updateService(componentContext);
        } catch (Exception e) {
            logger.error("Unable to activate mDNS discovery participant: {}, Exception: {}", SERVICE_TYPE, e);
        }
    }

    @Modified
    protected void modified(ComponentContext componentContext) {
        updateService(componentContext);
    }

    @SuppressWarnings("null")
    private void updateService(ComponentContext componentContext) {
        Dictionary<String, @Nullable Object> properties = componentContext.getProperties();
        String autoDiscoveryPropertyValue = (String) properties
                .get(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY);

        if (autoDiscoveryPropertyValue != null && !autoDiscoveryPropertyValue.isBlank()) {
            boolean value = Boolean.valueOf(autoDiscoveryPropertyValue);
            if (value != this.autoDiscoveryEnabled) {
                logger.debug("{} update: {} ➡️ {}", DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY,
                        autoDiscoveryPropertyValue, value);
                this.autoDiscoveryEnabled = value;
            }
        }
    }
}
