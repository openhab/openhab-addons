/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.huesync.internal.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.huesync.internal.HueSyncConstants;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
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
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class, configurationPid = "mdnsdiscovery.huesync")
public class HueSyncDiscoveryParticipant implements MDNSDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(HueSyncDiscoveryParticipant.class);

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

    private boolean autoDiscoveryEnabled = true;

    protected final ThingRegistry thingRegistry;

    @Activate
    public HueSyncDiscoveryParticipant(final @Reference ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Collections.singleton(HueSyncConstants.THING_TYPE_UID);
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        if (this.autoDiscoveryEnabled) {
            ThingUID uid = getThingUID(service);
            if (uid != null) {
                try {
                    logger.debug("HDMI Sync Box {} discovered at {}:{}", service.getName(),
                            service.getHostAddresses()[0], service.getPort());

                    Map<String, Object> properties = new HashMap<>();

                    properties.put(HueSyncConstants.PARAMETER_HOST, service.getHostAddresses()[0]);
                    properties.put(HueSyncConstants.PARAMETER_PORT, service.getPort());

                    return DiscoveryResultBuilder.create(uid).withLabel(service.getName()).withProperties(properties)
                            .build();
                } catch (Exception e) {
                    logger.debug("Unable to query device information for {}: {}", service.getQualifiedName(),
                            e.getMessage());
                }
            }
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        String id = service.getName();
        String[] addresses = service.getHostAddresses();

        if (addresses.length == 0 || id == null || id.isBlank()) {
            logger.debug("Incomplete mDNS device discovery information - {} ignored.",
                    id == null ? "[name: null]" : id);
            return null;
        }

        return new ThingUID(HueSyncConstants.THING_TYPE_UID, id);
    }

    @Activate
    protected void activate(ComponentContext componentContext) {
        updateService(componentContext);
    }

    @Modified
    protected void modified(ComponentContext componentContext) {
        updateService(componentContext);
    }

    private void updateService(ComponentContext componentContext) {
        String autoDiscoveryPropertyValue = (String) componentContext.getProperties()
                .get(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY);

        if (autoDiscoveryPropertyValue != null && !autoDiscoveryPropertyValue.isBlank()) {
            boolean value = Boolean.parseBoolean(autoDiscoveryPropertyValue);
            if (value != this.autoDiscoveryEnabled) {
                logger.debug("{} update: {} - {}", DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY,
                        autoDiscoveryPropertyValue, value);
                this.autoDiscoveryEnabled = value;
            }
        }
    }
}
