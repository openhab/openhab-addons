/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.thekeys.internal.smartlock;

import static org.openhab.binding.thekeys.internal.TheKeysBindingConstants.CONF_SMARTLOCK_LOCKID;
import static org.openhab.binding.thekeys.internal.TheKeysBindingConstants.THING_TYPE_SMARTLOCK;

import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.thekeys.internal.api.model.LockerDTO;
import org.openhab.binding.thekeys.internal.gateway.TheKeysGatewayHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service which uses TheKeys API to find all lock connected.
 *
 * @author Jordan Martin - Initial contribution
 */
@NonNullByDefault
public class TheKeysDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(TheKeysDiscoveryService.class);

    @Nullable
    private TheKeysGatewayHandler gatewayHandler;

    public TheKeysDiscoveryService() {
        super(Set.of(THING_TYPE_SMARTLOCK), 15, false);
    }

    @Override
    protected void startScan() {
        if (gatewayHandler == null) {
            return;
        }

        try {
            Stream<LockerDTO> locks = gatewayHandler.getApi().getLocks().stream();
            locks.map(this::createDiscoveryResult).forEach(this::thingDiscovered);
        } catch (Exception e) {
            logger.warn("Cannot start TheKeys discovery : {}", e.getMessage(), e);
        }
    }

    /**
     * Create discoveryResult from the lock
     *
     * @param lock The lock
     * @return The discoveryResult associated with the gateway
     */
    private DiscoveryResult createDiscoveryResult(LockerDTO lock) {
        if (gatewayHandler == null) {
            throw new IllegalStateException();
        }
        ThingUID gatewayThingUID = gatewayHandler.getThing().getUID();
        String label = gatewayHandler.getTranslationProvider().getText("discovery.thekeys.smartlock.label");
        return DiscoveryResultBuilder.create(createThingUID(lock)).withBridge(gatewayThingUID)
                .withLabel(label + " " + lock.getIdentifier()).withRepresentationProperty(CONF_SMARTLOCK_LOCKID)
                .withProperty(CONF_SMARTLOCK_LOCKID, lock.getIdentifier()).build();
    }

    /**
     * Create Smartlock thingUID from the lock
     *
     * @param lock The lock
     * @return The thingUID
     */
    private ThingUID createThingUID(LockerDTO lock) {
        if (gatewayHandler == null) {
            throw new IllegalStateException();
        }
        ThingUID gatewayThingUID = gatewayHandler.getThing().getUID();
        String lockId = String.valueOf(lock.getIdentifier());
        return new ThingUID(THING_TYPE_SMARTLOCK, gatewayThingUID, lockId);
    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return gatewayHandler;
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof TheKeysGatewayHandler) {
            gatewayHandler = (TheKeysGatewayHandler) handler;
        }
    }

    @Override
    public void deactivate() {
    }
}
