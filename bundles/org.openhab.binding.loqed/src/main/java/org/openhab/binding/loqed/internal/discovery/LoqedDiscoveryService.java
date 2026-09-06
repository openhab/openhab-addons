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
package org.openhab.binding.loqed.internal.discovery;

import static org.openhab.binding.loqed.internal.LoqedBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.loqed.internal.LoqedBridgeHandler;
import org.openhab.binding.loqed.internal.api.LoqedLockData;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Discovers all locks returned by a configured LOQED account.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
@Component(scope = ServiceScope.PROTOTYPE, service = LoqedDiscoveryService.class)
public class LoqedDiscoveryService extends AbstractThingHandlerDiscoveryService<LoqedBridgeHandler>
        implements ThingHandlerService {
    private static final int DISCOVERY_TIMEOUT_SECONDS = 20;
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_LOCK);

    @Activate
    public LoqedDiscoveryService() {
        super(LoqedBridgeHandler.class, SUPPORTED_THING_TYPES, DISCOVERY_TIMEOUT_SECONDS, false);
    }

    @Override
    protected void startScan() {
        ThingUID bridgeUID = thingHandler.getThing().getUID();
        for (LoqedLockData lock : thingHandler.refreshAndGetLocks()) {
            ThingUID thingUID = new ThingUID(THING_TYPE_LOCK, bridgeUID, uidSegment(lock.id));
            thingDiscovered(DiscoveryResultBuilder.create(thingUID).withBridge(bridgeUID).withLabel(lock.name)
                    .withProperties(Map.of(PROPERTY_LOCK_ID, lock.id, PROPERTY_MODEL, lock.modelName))
                    .withRepresentationProperty(PROPERTY_LOCK_ID).build());
        }
    }

    private String uidSegment(String lockId) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(lockId.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 8);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
