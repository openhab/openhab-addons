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
package org.openhab.binding.atv.internal.discovery;

import static org.openhab.binding.atv.internal.AtvBindingConstants.*;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers Apple TV and AirPlay devices through mDNS.
 *
 * <p>
 * Every Apple TV and AirPlay device advertises {@code _airplay._tcp}, so a single participant
 * on that service type covers both Thing types. The device is classified from the {@code model}
 * TXT record, and the stable identifier (the AirPlay {@code deviceid}, a MAC) is used for the
 * ThingUID and representation property so the IP can change without losing identity.
 *
 * @author Dan Cunningham - Initial contribution
 */
@Component(configurationPid = "discovery.atv")
@NonNullByDefault
public class AtvDiscoveryParticipant implements MDNSDiscoveryParticipant {

    private static final String TXT_MODEL = "model";
    private static final String TXT_DEVICE_ID = "deviceid";
    private static final String APPLETV_MODEL_PREFIX = "AppleTV";

    /**
     * Apple computers advertise {@code _airplay._tcp} (macOS AirPlay Receiver) but are not usable
     * media Things. Their {@code model} follows {@code Mac16,12}, {@code MacBookPro18,1},
     * {@code iMac21,1}, etc. - a Mac/iMac prefix followed by {@code <generation>,<revision>}.
     */
    private static final Pattern APPLE_COMPUTER_MODEL = Pattern.compile("^i?Mac[A-Za-z]*\\d+,\\d+$");

    private final Logger logger = LoggerFactory.getLogger(AtvDiscoveryParticipant.class);

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return SUPPORTED_THING_TYPES_UIDS;
    }

    @Override
    public String getServiceType() {
        return MDNS_AIRPLAY;
    }

    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        String id = deviceId(service);
        if (id.isBlank()) {
            return null;
        }
        return new ThingUID(thingType(service), id.replace(":", "").toLowerCase(Locale.ROOT));
    }

    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        ThingUID uid = getThingUID(service);
        if (uid == null) {
            logger.trace("Ignoring AirPlay service {}: no '{}' TXT record (addresses={}, server={})", service.getName(),
                    TXT_DEVICE_ID, Arrays.toString(service.getHostAddresses()), service.getServer());
            return null;
        }
        String host = hostAddress(service);
        if (host.isBlank()) {
            logger.trace("Ignoring AirPlay device {} ({}): no resolved address (server={})", service.getName(),
                    deviceId(service), service.getServer());
            return null;
        }
        String mac = deviceId(service);
        String name = service.getName();
        String model = service.getPropertyString(TXT_MODEL);

        if (model != null && APPLE_COMPUTER_MODEL.matcher(model).matches()) {
            logger.trace("Ignoring Apple computer {} (model {})", name, model);
            return null;
        }
        logger.trace("Discovered AirPlay device {} ({}) at {}", name, mac, host);

        Map<String, Object> properties = new HashMap<>();
        properties.put(CONFIG_MAC, mac);
        properties.put(CONFIG_HOST, host);
        if (model != null) {
            properties.put(PROPERTY_MODEL, model);
        }

        String label = model != null && !model.isBlank() && !"Unknown".equals(model) && !name.contains(model)
                ? name + " (" + model + ")"
                : name;

        return DiscoveryResultBuilder.create(uid).withThingType(thingType(service)).withProperties(properties)
                .withRepresentationProperty(CONFIG_MAC).withLabel(label).build();
    }

    /** Apple TVs report a {@code model} starting with "AppleTV"; everything else is treated as a speaker. */
    private ThingTypeUID thingType(ServiceInfo service) {
        String model = service.getPropertyString(TXT_MODEL);
        return model != null && model.startsWith(APPLETV_MODEL_PREFIX) ? THING_TYPE_APPLETV : THING_TYPE_SPEAKER;
    }

    private String deviceId(ServiceInfo service) {
        String id = service.getPropertyString(TXT_DEVICE_ID);
        return id != null ? id : "";
    }

    /**
     * Resolves a connectable host, preferring IPv4, then a routable IPv6 address, then the hostname.
     * jmDNS often resolves Apple devices to IPv6 only, so requiring IPv4 would silently drop them.
     */
    private static String hostAddress(ServiceInfo service) {
        Inet4Address[] ipv4 = service.getInet4Addresses();
        if (ipv4.length > 0) {
            return ipv4[0].getHostAddress();
        }
        for (Inet6Address ipv6 : service.getInet6Addresses()) {
            if (!ipv6.isLinkLocalAddress()) {
                String address = ipv6.getHostAddress();
                int scope = address.indexOf('%');
                return scope >= 0 ? address.substring(0, scope) : address;
            }
        }
        String server = service.getServer();
        if (server != null && !server.isBlank()) {
            return server.endsWith(".") ? server.substring(0, server.length() - 1) : server;
        }
        return "";
    }
}
