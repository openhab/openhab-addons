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
import java.util.concurrent.CompletableFuture;
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
import org.openhab.core.io.net.mac.MacResolver;
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
public class HomekitMdnsDiscoveryParticipant extends AbstractDiscoveryService implements MDNSDiscoveryParticipant {

    private static final String SERVICE_TYPE = "_hap._tcp.local.";

    private final Logger logger = LoggerFactory.getLogger(HomekitMdnsDiscoveryParticipant.class);
    private final Storage<String> mappedThingIdStore;
    private final Map<String, String> mappedThingIdCache = new ConcurrentHashMap<>();
    private final Map<String, DiscoveryInfo> pendingDiscoveryResults = new ConcurrentHashMap<>();

    private final MacResolver macResolver;

    /**
     * Internal DTO holding all pre-parsed information needed for discovery. Used both for immediate
     * results and for pending asynchronous MAC resolution.
     * <p>
     * Note: the Thing UID is derived both before and after MAC resolution. The final decision is made
     * in buildDiscoveryResult() which always includes the MAC address. But if MAC is not yet known we
     * intentionally derive a UID that might wrongly be an accessory Thing instead of a Bridge. This is
     * acceptable because:
     * <ol>
     * <li>Most accessory Thing devices stay as such.</li>
     * <li>The mapping change (accessory Thing -> Bridge) is rare.</li>
     * <li>Even if it returns a temporarily wrong UID, OH re-discovery handles UID changes gracefully.</li>
     * </ol>
     */
    private final class DiscoveryInfo {
        final String ip;
        final int port;
        final String name;
        final String server;
        final String uniqueId;
        final AccessoryCategory category;
        final Map<String, String> properties;
        final @Nullable String mac;
        final ThingUID uid;

        DiscoveryInfo(String ip, int port, String name, String server, String uniqueId, AccessoryCategory category,
                Map<String, String> properties) {
            this(ip, port, name, server, uniqueId, category, properties, null);
        }

        DiscoveryInfo(String ip, int port, String name, String server, String uniqueId, AccessoryCategory category,
                Map<String, String> properties, @Nullable String mac) {
            this.ip = ip;
            this.port = port;
            this.name = name;
            this.server = server;
            this.uniqueId = uniqueId;
            this.category = category;
            this.properties = Collections.unmodifiableMap(new HashMap<>(properties));
            this.mac = (mac == null || mac.isBlank()) ? null : mac;
            this.uid = deriveUID(uniqueId, category, mac);
        }

        private ThingUID deriveUID(String uniqueId, AccessoryCategory category, @Nullable String mac) {
            boolean isBridge = AccessoryCategory.BRIDGE == category || isTypeMapped(uniqueId);
            if (!isBridge && mac != null) {
                isBridge = isTypeMapped(mac); // not yet checked if MAC not yet resolved
            }
            ThingTypeUID typeUID = isBridge ? THING_TYPE_BRIDGE : THING_TYPE_ACCESSORY;
            return new ThingUID(typeUID, uniqueId.replace(":", "").toLowerCase()); // e.g. "a1b2c3d4e5f6"
        }

        DiscoveryInfo withMac(String newMac) {
            return new DiscoveryInfo(this.ip, this.port, this.name, this.server, this.uniqueId, this.category,
                    this.properties, newMac);
        }
    }

    /**
     * Constructor. The discovery participant is activated by the OSGi framework when the bundle is
     * started. The {@link AbstractDiscoveryService} is initialized to be a non-scanning service having
     * an empty supported thing type set. The constructor also loads the persisted mapped thing Ids and
     * finally registers itself as a listener for MAC address resolution.
     */
    @Activate
    public HomekitMdnsDiscoveryParticipant(@Reference StorageService service, @Reference MacResolver resolver) {
        super(Collections.emptySet(), 0, false); // set up passive AbstractDiscoveryService
        mappedThingIdStore = service.getStorage(getClass().getName(), getClass().getClassLoader());
        for (String k : mappedThingIdStore.getKeys()) {
            String v = mappedThingIdStore.get(k);
            if (v != null) {
                mappedThingIdCache.put(k, v);
            }
        }
        macResolver = resolver;
    }

    @Deactivate
    protected void deactivate() {
        pendingDiscoveryResults.clear();
        mappedThingIdCache.clear();
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
     * 
     * @param service the ServiceInfo object from which to create the DiscoveryResult.
     */
    @Override
    public @Nullable DiscoveryResult createResult(ServiceInfo service) {
        DiscoveryInfo info = createDiscoveryInfo(service);
        if (info == null) {
            return null;
        }

        // if we already have a pending entry, don't start another pipeline
        DiscoveryInfo existing = pendingDiscoveryResults.putIfAbsent(info.ip, info);
        if (existing != null) {
            return null;
        }

        CompletableFuture<@Nullable String> macFuture = macResolver.resolveMac(info.ip);
        // fast path: MAC already cached / future already completed
        if (macFuture.isDone()) {
            String mac = macFuture.getNow(null);
            pendingDiscoveryResults.remove(info.ip);
            if (mac != null) {
                DiscoveryResult result = buildDiscoveryResult(info.withMac(mac));
                if (result != null) {
                    logger.trace("{}", result);
                    return result;
                }
            }
            return null;
        }

        // slow path: complete later when MAC is resolved
        macFuture.whenComplete((mac, ex) -> {
            if (mac != null) {
                macAddressResolved(info.ip, mac);
            } else {
                pendingDiscoveryResults.remove(info.ip);
                logger.debug("IP {} did not resolve", info.ip);
            }
        });

        return null; // no immediate result
    }

    /**
     * Lambda method called from the MacResolver when a MAC address is resolved asynchronously. If the pending
     * discovery result still exists the discovery result is built and published as a Thing.
     * 
     * @param ip the IP address for which the MAC address was resolved.
     * @param mac the resolved MAC address.
     */
    private void macAddressResolved(String ip, String mac) {
        DiscoveryInfo info = pendingDiscoveryResults.remove(ip); // no longer pending
        if (info == null) {
            return;
        }
        DiscoveryResult result = buildDiscoveryResult(info.withMac(mac));
        if (result != null) {
            logger.trace("{}", result);
            thingDiscovered(result);
        }
    }

    /**
     * Builds a DiscoveryResult for the given {@link DiscoveryInfo}. This is used both for immediate
     * and deferred discovery when the MAC address has to resolved asynchronously. Returns null if
     * the discovery info does not contain a valid thing UID.
     * 
     * @param info the discovery info containing all necessary information to build the result.
     */
    private @Nullable DiscoveryResult buildDiscoveryResult(DiscoveryInfo info) {
        String mac = info.mac;
        if (mac == null) {
            return null; // safety
        }
        String hostIp = info.port > 0 ? info.ip + ":" + info.port : info.ip;

        DiscoveryResultBuilder builder = DiscoveryResultBuilder.create(info.uid);
        builder.withLabel(THING_LABEL_FMT.formatted(info.name, info.uniqueId)) //
                .withProperty(CONFIG_HTTP_HOST_HEADER, getHostName(info.server, info.port)) //
                .withProperty(CONFIG_IP_ADDRESS, hostIp) //
                .withProperty(CONFIG_UNIQUE_ID, info.uniqueId) //
                .withProperty(PROPERTY_ACCESSORY_CATEGORY, info.category.toString()) //
                .withProperty(Thing.PROPERTY_MAC_ADDRESS, mac) //
                .withRepresentationProperty(Thing.PROPERTY_MAC_ADDRESS);

        if (info.properties.get("md") instanceof String model) {
            builder.withProperty(Thing.PROPERTY_MODEL_ID, model);
        }
        if (info.properties.get("s#") instanceof String serial) {
            builder.withProperty(Thing.PROPERTY_SERIAL_NUMBER, serial);
        }
        if (info.properties.get("pv") instanceof String protocolVersion) {
            builder.withProperty(PROPERTY_PROTOCOL_VERSION, protocolVersion);
        }

        return builder.build();
    }

    /**
     * Extracts the ThingUID from the given ServiceInfo. Returns null if the service properties
     * do not contain a valid unique id or accessory category, or IP address. If the MAC address
     * is not yet resolved then the UID is derived based on the unique id and category, which
     * might temporarily be wrong. See {@link DiscoveryInfo} for comments on UID derivation.
     * 
     * @param service the ServiceInfo object from which to extract the ThingUID.
     */
    @Override
    public @Nullable ThingUID getThingUID(ServiceInfo service) {
        DiscoveryInfo info = createDiscoveryInfo(service);
        if (info == null) {
            return null;
        }
        String mac = macResolver.resolveMac(info.ip).getNow(null);
        if (mac != null) {
            info = info.withMac(mac);
        }
        return info.uid;
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
     * @param server the mDNS server name of the accessory.
     * @param port the TCP port of the accessory.
     * @return the HomeKit HTTP HAP Host header name.
     */
    private String getHostName(String server, int port) {
        String hostName = server.endsWith(".") ? server.substring(0, server.length() - 1) : server;
        hostName = hostName.replace(" ", "\\032");
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
        return (id != null) && (mappedThingIdCache.keySet().contains(id) || mappedThingIdCache.values().contains(id));
    }

    /**
     * Helper method to get the first IPv4 address from a ServiceInfo, or null if none found.
     */
    private @Nullable String getIp(ServiceInfo service) {
        return Arrays.stream(service.getInet4Addresses()).filter(Objects::nonNull).map(ipv4 -> ipv4.getHostAddress())
                .findFirst().orElse(null);
    }

    /**
     * Helper method to create a DiscoveryInfo from a ServiceInfo. Returns null if the service properties
     * do not contain a valid unique id or accessory category, or IP address.
     */
    protected @Nullable DiscoveryInfo createDiscoveryInfo(ServiceInfo service) {
        String ip = getIp(service);
        if (ip == null) {
            return null;
        }
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
        return new DiscoveryInfo(ip, service.getPort(), service.getName(), service.getServer(), uniqueId, category,
                properties);
    }

    /**
     * This {@link AbstractDiscoveryService} discovery participant does not perform active scanning, it
     * relies on the mDNS discovery service.
     */
    @Override
    protected void startScan() {
        stopScan(); // no active scanning
    }
}
