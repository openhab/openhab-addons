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
package org.openhab.binding.homekit.internal.discovery;

import static org.openhab.binding.homekit.internal.HomekitBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.homekit.internal.enums.AccessoryCategory;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.config.discovery.mdns.MDNSDiscoveryParticipant;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovers new HomeKit server devices.
 * <p>
 * HomeKit devices advertise themselves using mDNS with the service type "_hap._tcp.local.".
 * Each device is identified by its unique id, which is included in the mDNS properties.
 * The device category is also included, allowing differentiation between bridges and accessories.
 * The discovery participant creates a ThingUID based on the unique id and device category.
 * <p>
 * Discovered devices are published as Things of type
 * {@link org.openhab.binding.homekit.internal.HomekitBindingConstants#THING_TYPE_ACCESSORY}
 * or {@link org.openhab.binding.homekit.internal.HomekitBindingConstants#THING_TYPE_BRIDGE}.
 * Discovered Things include properties such as model name, protocol version, and IP address.
 * <p>
 * This class does not perform active scanning; instead, it relies on the central mDNS discovery
 * service to notify it of new services.
 * <p>
 * To prevent mistaken discovery of the same device (e.g. when an accessory is migrated to
 * a bridge) the participant maintains a set of mapped unique Id's and MAC addresses. When a
 * unique Id or MAC is mapped then discovery results for that id are created as a Bridge rather
 * than an accessory Thing. The mapping list is persisted across restarts.
 * <p>
 * This class also registers itself as a {@link DiscoveryService}. This means that it can
 * discover things whose MAC address is not yet known, and persist those things in a pending
 * list until the MAC is resolved by a {@link MacResolver}, and when that happens it can
 * asynchronously notify the discovery of the Thing later.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
@Component(service = { MDNSDiscoveryParticipant.class, DiscoveryService.class }, immediate = true, property = {
        "class.id=homekit" })
public class HomekitMdnsDiscoveryParticipant extends AbstractDiscoveryService
        implements MDNSDiscoveryParticipant, MacResolverListener {

    private static final String SERVICE_TYPE = "_hap._tcp.local.";

    private final Logger logger = LoggerFactory.getLogger(HomekitMdnsDiscoveryParticipant.class);
    private final Storage<String> mappedThingIdStore;
    private final Map<String, String> mappedThingIdCache = new ConcurrentHashMap<>();
    private final Map<String, ServiceInfo> pendingDiscoveryResults = new ConcurrentHashMap<>();

    private final MacResolver macResolver;

    /**
     * Constructor. The discovery participant is activated by the OSGi framework when the bundle is
     * started. The {@link AbstractDiscoveryService} is initialized to be a non-scanning service having
     * an empty supported thing type set. The constructor also loads the persisted mapped thing Ids and
     * finally registers itself as a listener for MAC address resolution.
     */
    @Activate
    public HomekitMdnsDiscoveryParticipant(@Reference StorageService service, @Reference MacResolver resolver) {
        super(Collections.emptySet(), 0, false);
        mappedThingIdStore = service.getStorage(getClass().getName(), getClass().getClassLoader());
        for (String k : mappedThingIdStore.getKeys()) {
            String v = mappedThingIdStore.get(k);
            if (v != null) {
                mappedThingIdCache.put(k, v);
            }
        }
        macResolver = resolver;
        macResolver.addMacResolverListener(this);
    }

    @Deactivate
    protected void deactivate() {
        macResolver.removeMacResolverListener(this);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return Set.of(THING_TYPE_BRIDGE, THING_TYPE_ACCESSORY);
    }

    @Override
    public String getServiceType() {
        return SERVICE_TYPE;
    }

    /**
     * Creates a DiscoveryResult for the given ServiceInfo. Returns null if the service properties do not contain a
     * valid unique id or accessory category, or IP address. If {@link MacResolver} does not provide a MAC address
     * immediately the discovery result is deferred until the {@link MacResolver} resolves it asynchronously.
     */
    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        ThingUID uid = getThingUID(service);
        if (uid == null) {
            return null;
        }

        String ip = getIp(service);
        if (ip == null) {
            return null;
        }

        String mac = macResolver.resolveMac(ip);
        if (mac == null) {
            pendingDiscoveryResults.put(ip, service);
            return null;
        }

        DiscoveryResult result = buildResult(service, mac);
        if (result != null) {
            logger.trace("Synchronous {}", result);
        }
        return result;
    }

    /**
     * Callback from the MacResolver when a MAC address is resolved asynchronously. The discovery result
     * is built and published as a Thing if the pending discovery result still exists.
     */
    @Override
    public void macAddressResolved(String ip, String mac) {
        ServiceInfo service = pendingDiscoveryResults.remove(ip);
        if (service == null) {
            return;
        }
        DiscoveryResult result = buildResult(service, mac);
        if (result != null) {
            logger.trace("Asynchronous {}", result);
            thingDiscovered(result);
        }
    }

    /**
     * Builds a DiscoveryResult for the given service and MAC address. Returns null if the service properties
     * do not contain a valid unique id, accessory category, or IP address.
     */
    private @Nullable DiscoveryResult buildResult(ServiceInfo service, String mac) {
        Map<String, String> properties = getProperties(service);

        String uniqueId = properties.get("id"); // unique id
        if (uniqueId == null) {
            return null;
        }

        AccessoryCategory category;
        try {
            String ci = properties.getOrDefault("ci", ""); // accessory category
            category = AccessoryCategory.from(Integer.parseInt(ci));
        } catch (IllegalArgumentException e) {
            return null;
        }

        String ip = getIp(service);
        if (ip == null) {
            return null;
        }

        int port = service.getPort();
        if (port > 0) {
            ip = ip + ":" + port;
        }

        ThingUID uid = getThingUID(service);
        if (uid == null) {
            return null;
        }

        DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(uid);
        builder.withLabel(THING_LABEL_FMT.formatted(service.getName(), uniqueId)) //
                .withProperty(CONFIG_HTTP_HOST_HEADER, getHostName(service)) //
                .withProperty(CONFIG_IP_ADDRESS, ip) //
                .withProperty(CONFIG_UNIQUE_ID, uniqueId) //
                .withProperty(PROPERTY_ACCESSORY_CATEGORY, category.toString()) //
                .withProperty(Thing.PROPERTY_MAC_ADDRESS, mac) //
                .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS);

        if (properties.get("md") instanceof String model) {
            builder.withProperty(Thing.PROPERTY_MODEL_ID, model);
        }
        if (properties.get("s#") instanceof String serial) {
            builder.withProperty(Thing.PROPERTY_SERIAL_NUMBER, serial);
        }
        if (properties.get("pv") instanceof String protocolVersion) {
            builder.withProperty(PROPERTY_PROTOCOL_VERSION, protocolVersion);
        }

        return builder.build();
    }

    /**
     * Extracts the ThingUID from the given ServiceInfo. Returns null if the service properties do not contain a valid
     * unique id, accessory category, or ip address.
     */
    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        Map<String, String> properties = getProperties(service);

        String uniqueId = properties.get("id");
        if (uniqueId == null) {
            return null;
        }

        AccessoryCategory category;
        try {
            String ci = properties.getOrDefault("ci", "");
            category = AccessoryCategory.from(Integer.parseInt(ci));
        } catch (IllegalArgumentException e) {
            return null;
        }

        String ip = getIp(service);
        if (ip == null) {
            return null;
        }

        /*
         * As a general rule the thing type is determined by the accessory category, but to prevent duplicate
         * discovery of the same device (e.g. when an accessory is migrated to a bridge) the unique id and (if
         * available) the MAC address are checked against the type mapping cache to determine if the accessory
         * Thing type should be mapped to Bridge instead.
         */
        ThingTypeUID uid = (AccessoryCategory.BRIDGE == category) || isTypeMapped(uniqueId)
                || isTypeMapped(macResolver.resolveMac(ip)) ? THING_TYPE_BRIDGE : THING_TYPE_ACCESSORY;

        return new ThingUID(uid, uniqueId.replace(":", "").toLowerCase()); // e.g. "a1b2c3d4e5f6"
    }

    /**
     * The JmDNS library getProperties() method has a bug whereby it fails to return any properties
     * in the case that the TXT record contains zero length parts. This is a drop in replacement.
     * Fixed upstream by https://github.com/jmdns/jmdns/pull/355
     */
    private Map<String, String> getProperties(ServiceInfo service) {
        Map<String, String> map = new HashMap<>();
        byte[] bytes = service.getTextBytes();
        int i = 0;
        while (i < bytes.length) {
            int len = bytes[i++] & 0xFF;
            if (len == 0) { // skip zero length parts
                continue;
            }
            String[] parts = new String(bytes, i, len, StandardCharsets.UTF_8).split("=");
            map.put(parts[0], parts.length < 2 ? "" : parts[1].replaceFirst("\\u0000$", "")); // strip zero endings
            i += len;
        }
        return map;
    }

    /**
     * Returns the HomeKit host name. This is used in the 'Host' header in HAP HTTP requests. The name is based on the
     * accessory's mDNS server name. Duplicate accessories will disambiguate their names via a '-' suffix according to
     * the mDNS RFC. Spaces are escaped as '\032'. Any '.' suffix after the 'local' is trimmed. And if the port is
     * neither '0' nor the default 80 then a port suffix is added. For example: my\032accessory-2.local:12345
     *
     * @param service the ServiceInfo object.
     * @return the HomeKit HTTP HAP Host header name.
     */
    private String getHostName(ServiceInfo service) {
        String hostName = service.getServer();
        hostName = hostName.endsWith(".") ? hostName.substring(0, hostName.length() - 1) : hostName;
        hostName = hostName.replace(" ", "\\032");
        int port = service.getPort();
        if (port != 80 && port != 0) {
            hostName += ":" + port;
        }
        return hostName;
    }

    /**
     * Enables/disables mapping of discovered accessory Things to Bridges. When accessory Things are auto-
     * migrated to Bridges then we need to re-map re-discovery to create Bridges rather than accessory Things.
     * <p>
     * NOTE: In prior versions the lookup table was a key-value pair with both fields being the unique id,
     * however in newer versions the value is now the MAC address.
     * 
     * @param enable true to enable mapping, false to disable it
     * @param uniqueId the Thing uniqueId property for which discovery mapping shall be enabled
     * @param mac the MAC address of the device for which discovery mapping shall be enabled
     */
    public void setTypeMapping(boolean enable, String uniqueId, @Nullable String mac) {
        if (enable) {
            mappedThingIdCache.put(uniqueId, mac != null ? mac : uniqueId); // cache for quick lookup
            mappedThingIdStore.put(uniqueId, mac != null ? mac : uniqueId); // persist across restarts
        } else {
            mappedThingIdCache.remove(uniqueId);
            mappedThingIdStore.remove(uniqueId);
        }
    }

    /**
     * Helper method to check if the given id is currently associated with discovery thing type mapping. Checks
     * both unique Id and MAC address.
     */
    private boolean isTypeMapped(@Nullable String id) {
        return id != null && (mappedThingIdCache.keySet().contains(id) || mappedThingIdCache.values().contains(id));
    }

    /**
     * Helper method to get the first IPv4 address from a ServiceInfo, or null if none found.
     */
    private @Nullable String getIp(ServiceInfo service) {
        return Arrays.stream(service.getInet4Addresses()).filter(Objects::nonNull).map(ipv4 -> ipv4.getHostAddress())
                .findFirst().orElse(null);
    }

    /**
     * This discovery participant does not perform active scanning, it relies on the mDNS discovery service.
     */
    @Override
    protected void startScan() {
        stopScan(); // no active scanning
    }
}
