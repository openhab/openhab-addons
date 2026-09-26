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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.caldav.internal.client.CalendarCollection;
import org.openhab.binding.caldav.internal.handler.AccountHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Discovery shares the serialized account worker and never blocks a framework callback.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Asynchronous background discovery
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = CalDavDiscoveryService.class)
public class CalDavDiscoveryService extends AbstractThingHandlerDiscoveryService<AccountHandler>
        implements ThingHandlerService {
    private static final Set<ThingTypeUID> TYPES = Set.of(new ThingTypeUID(BINDING_ID, CALENDAR_THING_TYPE));
    private @Nullable ScheduledFuture<?> background;
    private final AtomicLong generation = new AtomicLong();

    @Activate
    public CalDavDiscoveryService() {
        super(AccountHandler.class, TYPES, 300, true);
    }

    @Override
    protected void startScan() {
        long current = generation.incrementAndGet();
        AccountHandler handler = thingHandler;
        handler.discover(collections -> {
            if (generation.get() != current) {
                return;
            }
            for (CalendarCollection collection : collections) {
                if (generation.get() != current) {
                    return;
                }
                ThingUID uid = new ThingUID(new ThingTypeUID(BINDING_ID, CALENDAR_THING_TYPE),
                        handler.getThing().getUID(), thingId(collection.uri()));
                Map<String, Object> properties = new HashMap<>();
                properties.put("calendarUid", collection.uri().toString());
                properties.put("path", collection.uri().toString());
                properties.put("calendarId", calendarId(collection.uri()));
                properties.put("enabled", true);
                thingDiscovered(DiscoveryResultBuilder.create(uid).withBridge(handler.getThing().getUID())
                        .withLabel(collection.name()).withProperties(properties)
                        .withRepresentationProperty("calendarUid").build());
            }
        });
    }

    @Override
    protected void startBackgroundDiscovery() {
        if (background == null) {
            background = scheduler.scheduleWithFixedDelay(this::startScan, 0, 600, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void stopBackgroundDiscovery() {
        generation.incrementAndGet();
        ScheduledFuture<?> job = background;
        background = null;
        if (job != null) {
            job.cancel(true);
        }
    }

    @Override
    public void stopScan() {
        generation.incrementAndGet();
        super.stopScan();
    }

    private String thingId(URI uri) {
        return "calendar-" + Integer.toUnsignedString(uri.toString().hashCode(), 36);
    }

    private String calendarId(URI uri) {
        String path = uri.getPath();
        if (path != null) {
            String normalized = path.replaceFirst("/+\\z", "");
            int separator = normalized.lastIndexOf('/');
            if (separator >= 0 && separator + 1 < normalized.length()) {
                return normalized.substring(separator + 1);
            }
        }
        return thingId(uri);
    }
}
