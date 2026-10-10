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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.caldav.internal.client.CalDavUris;
import org.openhab.binding.caldav.internal.client.CalendarCollection;
import org.openhab.binding.caldav.internal.client.DavPrivilege;
import org.openhab.binding.caldav.internal.handler.AccountHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
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
 * @author Andreas Vilippus - Central URI identity and origin-relative discovery paths
 * @author Andreas Vilippus - Collection metadata and account-wide discovery labels
 * @author Andreas Vilippus - Cancel pending discovery retries with the scan generation
 * @author Andreas Vilippus - Cancel result preparation when the account session ends
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
            URI accountUrl = URI.create(handler.configuration().url);
            Map<URI, CalendarCollection> canonicalCollections = new LinkedHashMap<>();
            for (CalendarCollection collection : collections) {
                if (generation.get() != current) {
                    return;
                }
                URI canonicalCollection = CalDavUris
                        .canonicalize(CalDavUris.resolve(accountUrl, collection.uri().toString()));
                canonicalCollections.putIfAbsent(canonicalCollection, collection);
            }
            Map<URI, String> labels = CalDavDiscoveryLabels.create(canonicalCollections);
            for (var entry : canonicalCollections.entrySet()) {
                if (generation.get() != current) {
                    return;
                }
                URI uri = entry.getKey();
                CalendarCollection collection = entry.getValue();
                Map<String, Object> properties = new HashMap<>();
                properties.put("path", CalDavUris.originRelative(accountUrl, uri));
                if (!collection.description().isBlank()) {
                    properties.put("calendarDescription", collection.description());
                }
                if (!collection.color().isBlank()) {
                    properties.put("calendarColor", collection.color());
                }
                if (!collection.privileges().isEmpty()) {
                    properties.put("calendarPrivileges", String.join(",",
                            collection.privileges().stream().map(DavPrivilege::externalName).sorted().toList()));
                }
                ThingUID uid = new ThingUID(new ThingTypeUID(BINDING_ID, CALENDAR_THING_TYPE),
                        handler.getThing().getUID(), "calendar-" + CalDavDiscoveryLabels.digest(uri));
                DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(handler.getThing().getUID())
                        .withLabel(Objects.requireNonNull(labels.get(uri))).withProperties(properties).build();
                if (!publishIfCurrent(current, result)) {
                    return;
                }
            }
        }, () -> generation.get() == current, () -> cancelScan(current));
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

    private void cancelScan(long current) {
        synchronized (publicationLock) {
            generation.compareAndSet(current, current + 1);
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
}
