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
package org.openhab.binding.wled.internal;

import static org.openhab.binding.wled.internal.WLedBindingConstants.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WLedDiscoveryService} Discovers and adds any Wled devices found.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class)
public class WLedDiscoveryService implements MDNSDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(WLedDiscoveryService.class);
    private final HttpClient httpClient;

    @Activate
    public WLedDiscoveryService(@Reference HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    private String sendGetRequest(String address, String url) {
        Request request = httpClient.newRequest(address + url);
        request.timeout(3, TimeUnit.SECONDS);
        request.method(HttpMethod.GET);
        request.header(HttpHeader.ACCEPT_ENCODING, "gzip");
        logger.trace("Sending WLED GET:{}", url);
        try {
            ContentResponse contentResponse = request.send();
            if (contentResponse.getStatus() == 200) {
                return contentResponse.getContentAsString();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException | ExecutionException e) {
            logger.debug(
                    "WLED discovery hit a TimeoutException | ExecutionException which may have blocked a device from getting discovered:{}",
                    e.getMessage());
        }
        return "";
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        String name = service.getName().toLowerCase();
        if (!name.contains("wled")) {
            return null;
        }
        String[] address = service.getURLs();
        if ((address == null) || address.length < 1) {
            logger.debug("WLED discovered with empty IP address-{}", service);
            return null;
        }
        String response = sendGetRequest(address[0], "/json");
        String label = WLedHelper.getValue(response, "\"name\":\"", "\"");
        if (label.isEmpty()) {
            label = "WLED @ " + address[0];
        }
        String macAddress = WLedHelper.getValue(response, "\"mac\":\"", "\"");
        if (!macAddress.isBlank()) {
            String firmware = WLedHelper.getValue(response, "\"ver\":\"", "\"");
            ThingUID thingUID = new ThingUID(THING_TYPE_JSON, macAddress);
            Map<String, Object> properties = Map.of(Thing.PROPERTY_MAC_ADDRESS, macAddress,
                    Thing.PROPERTY_FIRMWARE_VERSION, firmware, CONFIG_ADDRESS, address[0]);
            return DiscoveryResultBuilder.create(thingUID).withLabel(label).withProperties(properties)
                    .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS).build();
        }
        return null;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        return null;
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public String getServiceType() {
        return "_http._tcp.local.";
    }
}
