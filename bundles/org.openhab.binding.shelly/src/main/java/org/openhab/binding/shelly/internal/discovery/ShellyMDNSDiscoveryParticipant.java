/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.io.IOException;
import java.net.Inet4Address;
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
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
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

    private final Logger logger = LoggerFactory.getLogger(ShellyMDNSDiscoveryParticipant.class);
    private final ShellyBindingConfiguration bindingConfig = new ShellyBindingConfiguration();
    private final ShellyTranslationProvider messages;
    private final HttpClient httpClient;
    private final ConfigurationAdmin configurationAdmin;

    public static final Pattern SHELLY_SERVICE_NAME_PATTERN = Pattern
            .compile("^([a-z0-9]*shelly[a-z0-9]*)-([a-z0-9]+)$", Pattern.CASE_INSENSITIVE);

    public static boolean isValidShellyServiceName(String serviceName) {
        return SHELLY_SERVICE_NAME_PATTERN.matcher(serviceName).matches();
    }

    @Activate
    public ShellyMDNSDiscoveryParticipant(@Reference ConfigurationAdmin configurationAdmin,
            @Reference HttpClientFactory httpClientFactory, @Reference LocaleProvider localeProvider,
            @Reference ShellyTranslationProvider translationProvider, ComponentContext componentContext) {
        logger.debug("Activating Shelly mDNS discovery service");
        this.configurationAdmin = configurationAdmin;
        this.messages = translationProvider;
        this.httpClient = httpClientFactory.getCommonHttpClient();
        bindingConfig.updateFromProperties(componentContext.getProperties());
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
        bindingConfig.updateFromProperties(componentContext.getProperties());
    }

    @Override
    public @Nullable DiscoveryResult createResult(final ServiceInfo service) {
        String serviceName = service.getName().toLowerCase(); // Shelly Duo: Name starts with "Shelly" rather than
                                                              // "shelly"
        if (!isValidShellyServiceName(serviceName)) {
            return null;
        }

        String address = "";
        Inet4Address[] hostAddresses = service.getInet4Addresses();
        if ((hostAddresses != null) && (hostAddresses.length > 0)) {
            address = substringAfter(hostAddresses[0].toString(), "/");
        }
        if (address.isEmpty()) {
            logger.trace("{}: Shelly device discovered with empty IP address (service-name={})", serviceName, service);
            return null;
        }
        logger.debug("{}: Shelly device discovered: IP address={}", serviceName, address);

        try {
            // Get device settings
            Configuration serviceConfig = configurationAdmin.getConfiguration("binding." + BINDING_ID);
            if (serviceConfig.getProperties() != null) {
                bindingConfig.updateFromProperties(serviceConfig.getProperties());
            }

            ShellyThingConfiguration config = new ShellyThingConfiguration();
            config.deviceIp = address;
            config.userId = bindingConfig.defaultUserId;
            config.password = bindingConfig.defaultPassword;

            String gen = getString(service.getPropertyString("gen"));
            boolean gen2 = "2".equals(gen) || "3".equals(gen) || "4".equals(gen)
                    || ShellyDeviceProfile.isGeneration2(serviceName);
            return ShellyBasicDiscoveryService.createResult(gen2, serviceName, address, bindingConfig, httpClient,
                    messages);
        } catch (IOException e) {
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
