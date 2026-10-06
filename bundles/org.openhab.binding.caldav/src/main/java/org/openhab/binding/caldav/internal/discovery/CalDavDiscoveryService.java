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
package org.openhab.binding.caldav.internal.discovery;

import static org.openhab.binding.caldav.internal.CalDavBindingConstants.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.caldav.internal.client.CalDavUris;
import org.openhab.binding.caldav.internal.client.CalendarCollection;
import org.openhab.binding.caldav.internal.handler.AccountHandler;
import org.openhab.core.config.core.ConfigUtil;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Discovery shares the serialized account worker and orders result publication against scan cancellation.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Asynchronous background discovery
 * @author Andreas Vilippus - One-time collection discovery
 * @author Andreas Vilippus - Atomic discovery publication
 * @author Andreas Vilippus - Collision-resistant discovery identities
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = CalDavDiscoveryService.class)
public class CalDavDiscoveryService extends AbstractThingHandlerDiscoveryService<AccountHandler>
        implements ThingHandlerService {
    private static final Set<ThingTypeUID> TYPES = Set.of(new ThingTypeUID(BINDING_ID, CALENDAR_THING_TYPE));
    private final AtomicLong generation = new AtomicLong();
    private final Object publicationLock = new Object();

    @Activate
    public CalDavDiscoveryService() {
        super(AccountHandler.class, TYPES, 300, true);
    }

    @Override
    public void startScan() {
        long current = nextGeneration();
        AccountHandler handler = thingHandler;
        handler.discover(collections -> {
            if (generation.get() != current) {
                return;
            }
            Map<String, String> acceptedIds = acceptedThingIds(handler);
            for (CalendarCollection collection : collections) {
                if (generation.get() != current) {
                    return;
                }
                String identity = canonicalUri(collection.uri());
                ThingUID uid = new ThingUID(new ThingTypeUID(BINDING_ID, CALENDAR_THING_TYPE),
                        handler.getThing().getUID(), acceptedIds.getOrDefault(identity, thingId(identity)));
                Map<String, Object> properties = new HashMap<>();
                properties.put("path", collection.uri().toString());
                DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(handler.getThing().getUID())
                        .withLabel(collection.name()).withProperties(properties).build();
                if (!publishIfCurrent(current, result)) {
                    return;
                }
            }
        });
    }

    @Override
    protected void startBackgroundDiscovery() {
        // Core registers the discovery service before initializing the account handler.
        thingHandler.registerDiscoveryService(this);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        thingHandler.unregisterDiscoveryService(this);
        nextGeneration();
    }

    @Override
    public void dispose() {
        stopScan();
        super.dispose();
    }

    @Override
    public void stopScan() {
        nextGeneration();
        super.stopScan();
    }

    private long nextGeneration() {
        synchronized (publicationLock) {
            return generation.incrementAndGet();
        }
    }

    private boolean publishIfCurrent(long current, DiscoveryResult result) {
        synchronized (publicationLock) {
            if (generation.get() != current) {
                return false;
            }
            thingDiscovered(result);
            return true;
        }
    }

    private Map<String, String> acceptedThingIds(AccountHandler handler) {
        Map<String, String> ids = new HashMap<>();
        for (Thing child : handler.getThing().getThings()) {
            if (!child.getThingTypeUID().equals(new ThingTypeUID(BINDING_ID, CALENDAR_THING_TYPE))) {
                continue;
            }
            try {
                Object path = ConfigUtil.resolveVariables(child.getConfiguration()).get("path");
                if (path instanceof String configuredPath) {
                    URI uri = CalDavUris.resolve(URI.create(handler.configuration().url), configuredPath);
                    String canonical = canonicalUri(uri);
                    String id = child.getUID().getId();
                    String legacy = "calendar-" + Integer.toUnsignedString(uri.toString().hashCode(), 36);
                    if (id.equals(legacy) || id.equals(thingId(uri.toString())) || id.equals(thingId(canonical))) {
                        // Preserve accepted discovery identities, links and storage keys across URI spelling changes.
                        ids.putIfAbsent(canonical, id);
                    }
                }
            } catch (IllegalArgumentException e) {
                // Invalid child configuration must not claim another collection's discovery identity.
            }
        }
        return ids;
    }

    private static String canonicalUri(URI uri) {
        String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
        int port = uri.getPort();
        String authority = uri.getHost().toLowerCase(Locale.ROOT)
                + (port < 0 || port == ("https".equals(scheme) ? 443 : 80) ? "" : ":" + port);
        String path = uri.getRawPath();
        String query = uri.getRawQuery();
        return CalDavUris.validate(URI.create(scheme + "://" + authority + normalizeEscapes(path.isEmpty() ? "/" : path)
                + (query == null ? "" : "?" + normalizeEscapes(query)))).toString();
    }

    private static String normalizeEscapes(String value) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char character = value.charAt(i);
            if (character == '%') {
                int code = Integer.parseInt(value.substring(i + 1, i + 3), 16);
                if (code >= 'a' && code <= 'z' || code >= 'A' && code <= 'Z' || code >= '0' && code <= '9'
                        || code == '-' || code == '.' || code == '_' || code == '~') {
                    result.append((char) code);
                } else {
                    result.append('%').append(value.substring(i + 1, i + 3).toUpperCase(Locale.ROOT));
                }
                i += 2;
            } else {
                result.append(character);
            }
        }
        return result.toString();
    }

    private static String thingId(String uri) {
        try {
            return "calendar-" + HexFormat.of()
                    .formatHex(MessageDigest.getInstance("SHA-256").digest(uri.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
