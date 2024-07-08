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
package org.openhab.binding.hdpowerview.internal.discovery;

import static org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants.*;

import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.hdpowerview.internal.HDPowerViewWebTargets;
import org.openhab.binding.hdpowerview.internal.config.HDPowerViewHubConfiguration;
import org.openhab.binding.hdpowerview.internal.dto.Firmware;
import org.openhab.binding.hdpowerview.internal.dto.HubFirmware;
import org.openhab.binding.hdpowerview.internal.exceptions.HubException;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers HD PowerView hubs by means of mDNS
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component
public class HDPowerViewHubMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    public static final String LABEL_KEY_HUB = "discovery.hub.label";

    private final Logger logger = LoggerFactory.getLogger(HDPowerViewHubMDNSDiscoveryParticipant.class);
    private final HttpClient httpClient;

    @Activate
    public HDPowerViewHubMDNSDiscoveryParticipant(@Reference HttpClientFactory httpClientFactory) {
        httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_HUB);
    }

    @Override
    public String getServiceType() {
        return "_powerview._tcp.local.";
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        for (String host : service.getHostAddresses()) {
            if (VALID_IP_V4_ADDRESS.matcher(host).matches()) {
                ThingUID thingUID = new ThingUID(THING_TYPE_HUB, host.replace('.', '_'));
                String generation = this.getGeneration(host);
                DiscoveryResult hub = DiscoveryResultBuilder.create(thingUID)
                        .withProperty(HDPowerViewHubConfiguration.HOST, host)
                        .withRepresentationProperty(HDPowerViewHubConfiguration.HOST)
                        .withLabel(String.format("@text/%s [\"%s\", \"%s\"]", LABEL_KEY_HUB, generation, host)).build();
                logger.debug("mDNS discovered Gen {} hub on host '{}'", generation, host);
                return hub;
            }
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        for (String host : service.getHostAddresses()) {
            if (VALID_IP_V4_ADDRESS.matcher(host).matches()) {
                return new ThingUID(THING_TYPE_HUB, host.replace('.', '_'));
            }
        }
        return null;
    }

    private String getGeneration(String host) {
        var webTargets = new HDPowerViewWebTargets(httpClient, host);
        try {
            HubFirmware firmware = webTargets.getFirmwareVersions();
            Firmware mainProcessor = firmware.mainProcessor;
            if (mainProcessor != null) {
                return String.valueOf(mainProcessor.revision);
            }
        } catch (HubException e) {
            logger.debug("Failed to discover hub firmware versions", e);
        }
        return "1/2";
    }
}
