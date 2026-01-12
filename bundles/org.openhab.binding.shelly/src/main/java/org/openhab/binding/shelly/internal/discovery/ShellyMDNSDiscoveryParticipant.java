/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.discovery;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.BINDING_ID;
import static org.openhab.binding.shelly.internal.ShellyDevices.SUPPORTED_THING_TYPES;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.getString;

import java.io.IOException;
import java.net.Inet4Address;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.binding.shelly.internal.util.ShellyCacheList;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class identifies Shelly devices by their mDNS service information.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@Component(service = MDNSDiscoveryParticipant.class)
public class ShellyMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    /**
     * For backwards compatibility with older Gen 1 devices and Gen 2 devices
     * with older firmware versions, <code>_http._tcp.local.</code> is used.
     * Newer firmware versions and Gen2+ devices advertise themselves as
     * <code>_shelly._tcp.local.</code> as well.
     */
    private static final String SERVICE_TYPE = "_http._tcp.local.";
    private static final long MDNS_CACHE_TIMEOUT_SEC = 15;

    private final Logger logger = LoggerFactory.getLogger(ShellyMDNSDiscoveryParticipant.class);
    private final ShellyBindingConfiguration bindingConfig = new ShellyBindingConfiguration();
    private final ShellyTranslationProvider messages;
    private final HttpClient httpClient;
    private final ConfigurationAdmin configurationAdmin;
    private final NetworkAddressService networkAddressService;

    public static final Pattern SHELLY_SERVICE_NAME_PATTERN = Pattern
            .compile("^([a-z0-9]*shelly[a-z0-9]*)-([a-z0-9]+)$", Pattern.CASE_INSENSITIVE);

    public static boolean isValidShellyServiceName(String serviceName) {
        return SHELLY_SERVICE_NAME_PATTERN.matcher(serviceName).matches();
    }

    private static final class MDNSCacheEntry {
        private final String ipAddress;

        MDNSCacheEntry(String ipAddress) {
            this.ipAddress = ipAddress;
        }
    }

    private final ShellyCacheList<String, MDNSCacheEntry> MDNSCache = new ShellyCacheList<>(MDNS_CACHE_TIMEOUT_SEC);

    @Activate
    public ShellyMDNSDiscoveryParticipant(@Reference ConfigurationAdmin configurationAdmin,
            @Reference NetworkAddressService networkAddressService, @Reference HttpClientFactory httpClientFactory,
            @Reference ShellyTranslationProvider translationProvider, ComponentContext componentContext) {
        logger.debug("Activating Shelly mDNS discovery service");
        this.configurationAdmin = configurationAdmin;
        this.networkAddressService = networkAddressService;
        this.messages = translationProvider;
        this.httpClient = httpClientFactory.getCommonHttpClient();
        updateBindingConfig(componentContext);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    /**
     * Process updates to Binding Config
     *
     * @param componentContext
     */
    @Modified
    protected void modified(final ComponentContext componentContext) {
        logger.debug("Shelly Binding Configuration refreshed");
        updateBindingConfig(componentContext);
    }

    private void updateBindingConfig(final ComponentContext componentContext) {
        synchronized (bindingConfig) {
            try {
                bindingConfig.updateFromProperties(componentContext.getProperties());
                // Get device settings
                Configuration serviceConfig = configurationAdmin.getConfiguration("binding." + BINDING_ID);
                if (serviceConfig.getProperties() != null) {
                    bindingConfig.updateFromProperties(serviceConfig.getProperties());
                }

                if (bindingConfig.localIP.isBlank()) {
                    String primary = networkAddressService.getPrimaryIpv4HostAddress();
                    if (primary != null && !primary.isBlank()) {
                        bindingConfig.localIP = primary;
                    }
                }
            } catch (IOException e) {
                logger.debug("ShellyMDNSDiscoveryParticipant: Unable to initialize bindingConfig");
            }
        }
    }

    @Override
    public @Nullable DiscoveryResult createResult(final ServiceInfo service) {
        // Shelly Duo: Name starts with "Shelly" rather than "shelly"
        String serviceName = service.getName().toLowerCase(Locale.ROOT);

        if (logger.isTraceEnabled()) {
            logger.trace("{}: mDNS Service Info: {}", serviceName, service.getNiceTextString());
        }
        if (!isValidShellyServiceName(serviceName)) {
            logger.trace("{}: serviceName doesn't match name pattern (e.g. Shelly device name), ignore", serviceName);
            return null;
        }

        String address = "";
        Inet4Address[] hostAddresses = service.getInet4Addresses();
        if (hostAddresses != null && hostAddresses.length > 0) {
            address = hostAddresses[0].getHostAddress();
        }
        if (address.isEmpty()) {
            logger.trace("{}: Shelly device discovered with empty IP address (service-name={})", serviceName, service);
            return null;
        }

        // Shelly might send multiple mDNS annoucements in a row, those trigger multiple (parallel) discoveries on the
        // OH side, which is inefficent and causes side effects -> ignore duplicates within MDNS_CACHE_TIMEOUT_SEC secs
        boolean newEntry = MDNSCache.putIfAbsent(serviceName, new MDNSCacheEntry(address),
                (oldV, newV) -> oldV.ipAddress.equals(newV.ipAddress));
        if (!newEntry) {
            logger.trace("{}: Discovered  device with IP address {} is already known (tname={})", serviceName, address,
                    Thread.currentThread().getName());
            return null;
        }

        logger.debug("{}: Shelly device with IP address {} discovered (tname={})", serviceName, address,
                Thread.currentThread().getName());

        try {
            // Get device settings
            String gen = getString(service.getPropertyString("gen"));
            boolean gen2 = "2".equals(gen) || "3".equals(gen) || "4".equals(gen)
                    || ShellyDeviceProfile.isGeneration2(serviceName);

            final ShellyThingConfiguration thingConfig;
            synchronized (bindingConfig) {
                thingConfig = ShellyBasicDiscoveryService.fillConfig(bindingConfig, address, serviceName);
            }

            return ShellyBasicDiscoveryService.createResult(gen2, serviceName, address, thingConfig, httpClient,
                    messages);
        } catch (Exception e) {
            logger.debug("{}: Exception on processing serviceInfo '{}'", serviceName, service.getNiceTextString(), e);
            return null;
        }
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        logger.trace("ServiceInfo {}", service);
        String serviceName = service.getName();
        if (serviceName == null) {
            return null;
        }
        if (!isValidShellyServiceName(serviceName)) {
            logger.debug("{} is not a valid Shelly service name", serviceName);
            return null;
        }
        return ShellyThingCreator.getThingUID(serviceName);
    }
}
