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
package org.openhab.binding.homeconnectdirect.internal.discovery;

import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.APPLIANCE_TYPE_COFFEE_MAKER;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.APPLIANCE_TYPE_COOKTOP;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.APPLIANCE_TYPE_COOKTOP_ALTERNATIVE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.APPLIANCE_TYPE_COOK_PROCESSOR;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.APPLIANCE_TYPE_DISHWASHER;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.APPLIANCE_TYPE_DRYER;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.APPLIANCE_TYPE_FRIDGE_FREEZER;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.APPLIANCE_TYPE_HOOD;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.APPLIANCE_TYPE_OVEN;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.APPLIANCE_TYPE_WARMING_DRAWER;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.APPLIANCE_TYPE_WASHER;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.APPLIANCE_TYPE_WASHER_AND_DRYER;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CONNECTION_TYPE_AES_PORT;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.CONNECTION_TYPE_TLS_PORT;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.LOCALE;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.PROPERTY_ADDRESS;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.PROPERTY_HOME_APPLIANCE_ID;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.SUPPORTED_THING_TYPES;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.THING_TYPE_COFFEE_MAKER;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.THING_TYPE_COOKTOP;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.THING_TYPE_COOK_PROCESSOR;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.THING_TYPE_DISHWASHER;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.THING_TYPE_DRYER;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.THING_TYPE_FRIDGE_FREEZER;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.THING_TYPE_GENERIC;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.THING_TYPE_HOOD;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.THING_TYPE_OVEN;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.THING_TYPE_WARMING_DRAWER;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.THING_TYPE_WASHER;
import static org.openhab.binding.homeconnectdirect.internal.HomeConnectDirectBindingConstants.THING_TYPE_WASHER_DRYER;
import static org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HomeConnectDirectMDNSDiscoveryParticipant} is responsible for discovering Home
 * Connect appliances in your network environment.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@Component(service = MDNSDiscoveryParticipant.class, configurationPid = "discovery.homeconnectdirect")
@NonNullByDefault
public class HomeConnectDirectMDNSDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private static final String SERVICE_TYPE = "_homeconnect._tcp.local.";
    private static final String PROPERTY_ID = "id";
    private static final String PROPERTY_BRAND = "brand";
    private static final String PROPERTY_TYPE = "type";
    private static final String PROPERTY_VIB = "vib";
    private static final String PROPERTY_MAC = "mac";

    private final Logger logger;
    private final ThingRegistry thingRegistry;

    @Activate
    public HomeConnectDirectMDNSDiscoveryParticipant(@Reference ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
        logger = LoggerFactory.getLogger(HomeConnectDirectMDNSDiscoveryParticipant.class);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES;
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    @Override
    @Nullable
    public DiscoveryResult createResult(final ServiceInfo serviceInfo) {
        var ipv4List = List.of(serviceInfo.getInet4Addresses());
        var haId = serviceInfo.getPropertyString(PROPERTY_ID);
        var type = serviceInfo.getPropertyString(PROPERTY_TYPE);
        var brand = serviceInfo.getPropertyString(PROPERTY_BRAND);
        var port = serviceInfo.getPort();
        var thingUID = mapThingUID(serviceInfo);

        logger.trace("Found appliance. haId={}, addresses={}, port={}", haId, ipv4List, port);
        if (!ipv4List.isEmpty() && isNotBlank(haId)
                && (port == CONNECTION_TYPE_AES_PORT || port == CONNECTION_TYPE_TLS_PORT) && isNotBlank(type)
                && isNotBlank(brand) && thingUID != null && !isAlreadyRegisteredThing(haId)) {
            var thingTypeUID = mapType(type);
            var friendlyName = getLabel(thingTypeUID, capitalize(brand.toLowerCase(LOCALE)));

            Map<String, Object> properties = Map.of(PROPERTY_HOME_APPLIANCE_ID, haId, PROPERTY_ADDRESS,
                    ipv4List.getFirst().getHostAddress());

            return DiscoveryResultBuilder.create(thingUID).withProperties(properties).withLabel(friendlyName)
                    .withRepresentationProperty(PROPERTY_HOME_APPLIANCE_ID).build();
        } else {
            logger.trace("Ignore device due to missing attributes or already registered (haId={}).", haId);
            return null;
        }
    }

    @Override
    @Nullable
    public ThingUID getThingUID(ServiceInfo serviceInfo) {
        return mapThingUID(serviceInfo);
    }

    @Nullable
    private ThingUID mapThingUID(ServiceInfo serviceInfo) {
        var haId = serviceInfo.getPropertyString(PROPERTY_ID);
        var type = serviceInfo.getPropertyString(PROPERTY_TYPE);
        var mac = serviceInfo.getPropertyString(PROPERTY_MAC);
        var vib = serviceInfo.getPropertyString(PROPERTY_VIB);
        var brand = serviceInfo.getPropertyString(PROPERTY_BRAND);

        if (isNotBlank(haId) && isNotBlank(brand) && isNotBlank(type) && isNotBlank(mac) && isNotBlank(vib)) {
            var idProposal = brand.toLowerCase(LOCALE) + "-" + vib.toLowerCase(LOCALE) + "-" + mac.toLowerCase(LOCALE);
            return new ThingUID(mapType(type), idProposal);
        } else {
            return null;
        }
    }

    private boolean isAlreadyRegisteredThing(String haId) {
        return thingRegistry.stream().filter(thing -> SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID()))
                .filter(thing -> thing.getConfiguration().containsKey(PROPERTY_HOME_APPLIANCE_ID))
                .anyMatch(thing -> equalsIgnoreCase(
                        String.valueOf(thing.getConfiguration().get(PROPERTY_HOME_APPLIANCE_ID)), haId));
    }

    private ThingTypeUID mapType(String type) {
        if (equalsIgnoreCase(type, APPLIANCE_TYPE_WASHER)) {
            return THING_TYPE_WASHER;
        } else if (equalsIgnoreCase(type, APPLIANCE_TYPE_DRYER)) {
            return THING_TYPE_DRYER;
        } else if (equalsIgnoreCase(type, APPLIANCE_TYPE_WASHER_AND_DRYER)) {
            return THING_TYPE_WASHER_DRYER;
        } else if (equalsIgnoreCase(type, APPLIANCE_TYPE_DISHWASHER)) {
            return THING_TYPE_DISHWASHER;
        } else if (equalsIgnoreCase(type, APPLIANCE_TYPE_COFFEE_MAKER)) {
            return THING_TYPE_COFFEE_MAKER;
        } else if (equalsIgnoreCase(type, APPLIANCE_TYPE_COOK_PROCESSOR)) {
            return THING_TYPE_COOK_PROCESSOR;
        } else if (equalsIgnoreCase(type, APPLIANCE_TYPE_OVEN)) {
            return THING_TYPE_OVEN;
        } else if (equalsIgnoreCase(type, APPLIANCE_TYPE_WARMING_DRAWER)) {
            return THING_TYPE_WARMING_DRAWER;
        } else if (equalsIgnoreCase(type, APPLIANCE_TYPE_HOOD)) {
            return THING_TYPE_HOOD;
        } else if (equalsIgnoreCase(type, APPLIANCE_TYPE_COOKTOP_ALTERNATIVE)
                || equalsIgnoreCase(type, APPLIANCE_TYPE_COOKTOP)) {
            return THING_TYPE_COOKTOP;
        } else if (equalsIgnoreCase(type, APPLIANCE_TYPE_FRIDGE_FREEZER)) {
            return THING_TYPE_FRIDGE_FREEZER;
        } else {
            return THING_TYPE_GENERIC;
        }
    }

    private String getLabel(ThingTypeUID thingTypeUID, String brand) {
        if (THING_TYPE_WASHER.equals(thingTypeUID)) {
            return "@text/appliance.washer.label [\"" + brand + "\"]";
        } else if (THING_TYPE_DRYER.equals(thingTypeUID)) {
            return "@text/appliance.dryer.label [\"" + brand + "\"]";
        } else if (THING_TYPE_WASHER_DRYER.equals(thingTypeUID)) {
            return "@text/appliance.washerdryer.label [\"" + brand + "\"]";
        } else if (THING_TYPE_DISHWASHER.equals(thingTypeUID)) {
            return "@text/appliance.dishwasher.label [\"" + brand + "\"]";
        } else if (THING_TYPE_COFFEE_MAKER.equals(thingTypeUID)) {
            return "@text/appliance.coffeemaker.label [\"" + brand + "\"]";
        } else if (THING_TYPE_COOK_PROCESSOR.equals(thingTypeUID)) {
            return "@text/appliance.cookprocessor.label [\"" + brand + "\"]";
        } else if (THING_TYPE_OVEN.equals(thingTypeUID)) {
            return "@text/appliance.oven.label [\"" + brand + "\"]";
        } else if (THING_TYPE_WARMING_DRAWER.equals(thingTypeUID)) {
            return "@text/appliance.warmingdrawer.label [\"" + brand + "\"]";
        } else if (THING_TYPE_HOOD.equals(thingTypeUID)) {
            return "@text/appliance.hood.label [\"" + brand + "\"]";
        } else if (THING_TYPE_COOKTOP.equals(thingTypeUID)) {
            return "@text/appliance.cooktop.label [\"" + brand + "\"]";
        } else if (THING_TYPE_FRIDGE_FREEZER.equals(thingTypeUID)) {
            return "@text/appliance.fridgefreezer.label [\"" + brand + "\"]";
        } else {
            return "@text/appliance.generic.label [\"" + brand + "\"]";
        }
    }
}
