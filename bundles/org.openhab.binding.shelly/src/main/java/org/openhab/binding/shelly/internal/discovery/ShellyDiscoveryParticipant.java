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
package org.openhab.binding.shelly.internal.discovery;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;

import java.io.IOException;
import java.net.Inet4Address;
import java.util.Set;

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
public class ShellyDiscoveryParticipant implements MDNSDiscoveryParticipant {
    private final Logger logger = LoggerFactory.getLogger(ShellyDiscoveryParticipant.class);
    private final ShellyBindingConfiguration bindingConfig = new ShellyBindingConfiguration();
    private final ShellyTranslationProvider messages;
    private final HttpClient httpClient;
    private final ConfigurationAdmin configurationAdmin;

    @Activate
    public ShellyDiscoveryParticipant(@Reference ConfigurationAdmin configurationAdmin,
            @Reference HttpClientFactory httpClientFactory, @Reference LocaleProvider localeProvider,
            @Reference ShellyTranslationProvider translationProvider, ComponentContext componentContext) {
        logger.debug("Activating ShellyDiscovery service");
        this.configurationAdmin = configurationAdmin;
        this.messages = translationProvider;
        this.httpClient = httpClientFactory.getCommonHttpClient();
        bindingConfig.updateFromProperties(componentContext.getProperties());
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
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

    @Nullable
    @Override
    public DiscoveryResult createResult(final ServiceInfo service) {
        String name = service.getName().toLowerCase(); // Shelly Duo: Name starts with" Shelly" rather than "shelly"
        if (!name.startsWith("shelly")) {
            return null;
        }

        try {
            String address = "";
            Inet4Address[] hostAddresses = service.getInet4Addresses();
            if ((hostAddresses != null) && (hostAddresses.length > 0)) {
                address = substringAfter(hostAddresses[0].toString(), "/");
            }
            if (address.isEmpty()) {
                logger.trace("{}: Shelly device discovered with empty IP address (service-name={})", name, service);
                return null;
            }
            String thingType = service.getQualifiedName().contains(SERVICE_TYPE) && name.contains("-")
                    ? substringBeforeLast(name, "-")
                    : name;
            logger.debug("{}: Shelly device discovered: IP-Adress={}, type={}", name, address, thingType);

            // Get device settings
            Configuration serviceConfig = configurationAdmin.getConfiguration("binding.shelly");
            if (serviceConfig.getProperties() != null) {
                bindingConfig.updateFromProperties(serviceConfig.getProperties());
            }

            ShellyThingConfiguration config = new ShellyThingConfiguration();
            config.deviceIp = address;
            config.userId = bindingConfig.defaultUserId;
            config.password = bindingConfig.defaultPassword;

            String gen = getString(service.getPropertyString("gen"));
            boolean gen2 = "2".equals(gen) || "3".equals(gen) || ShellyDeviceProfile.isGeneration2(name);
            return ShellyBasicDiscoveryService.createResult(gen2, name, address, bindingConfig, httpClient, messages);
        } catch (IOException | NullPointerException e) {
            // maybe some format description was buggy
            logger.debug("{}: Exception on processing serviceInfo '{}'", name, service.getNiceTextString(), e);
        }
        return null;
    }

    @Nullable
    @Override
    public ThingUID getThingUID(@Nullable ServiceInfo service) throws IllegalArgumentException {
        logger.debug("ServiceInfo {}", service);
        if (service == null) {
            throw new IllegalArgumentException("service must not be null!");
        }
        String serviceName = service.getName();
        if (serviceName == null) {
            throw new IllegalArgumentException("serviceName must not be null!");
        }
        serviceName = serviceName.toLowerCase();
        if (!serviceName.contains(VENDOR.toLowerCase())) {
            logger.debug("Not a " + VENDOR + " device!");
            return null;
        }
        return ShellyThingCreator.getThingUID(serviceName, "", "", false);
    }
}
