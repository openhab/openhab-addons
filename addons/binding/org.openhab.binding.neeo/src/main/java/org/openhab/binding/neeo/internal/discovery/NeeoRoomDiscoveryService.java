/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.discovery;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingRegistry;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.neeo.NeeoConstants;
import org.openhab.binding.neeo.NeeoUtil;
import org.openhab.binding.neeo.handler.NeeoBrainHandler;
import org.openhab.binding.neeo.internal.NeeoBrainApi;
import org.openhab.binding.neeo.internal.NeeoBrainConfig;
import org.openhab.binding.neeo.internal.models.NeeoBrain;
import org.openhab.binding.neeo.internal.models.NeeoRoom;
import org.openhab.binding.neeo.internal.type.NeeoTypeGenerator;
import org.openhab.binding.neeo.internal.type.UidUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link AbstractDiscoveryService} that will discover the rooms in a NEEO brain;
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true)
public class NeeoRoomDiscoveryService extends AbstractDiscoveryService {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoRoomDiscoveryService.class);

    /** The scanning task (not-null when connecting, null otherwise) */
    private final AtomicReference<@Nullable Future<?>> scan = new AtomicReference<>();

    /** The thing registry that we use */
    @NonNullByDefault({})
    private ThingRegistry thingRegistry;

    /** The type generator */
    @NonNullByDefault({})
    private NeeoTypeGenerator typeGenerator;

    /**
     * The thing types we discover. Since the room types are all dynamically generated, we simply return the binding
     * id plus a dummy thingtypeid. The dummy thingtypeid doesn't matter since we only want to be part of the NEEO
     * binding scan (which only uses the binding id part).
     *
     */
    private static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Collections
            .singleton(new ThingTypeUID(NeeoConstants.BINDING_ID, "rooms"));

    /**
     * Instantiates a new NEEO room discovery service.
     */
    public NeeoRoomDiscoveryService() {
        super(DISCOVERABLE_THING_TYPES_UIDS, NeeoConstants.ROOM_DISCOVERY_TIMEOUT);
    }

    /**
     * Set's the thing registry
     *
     * @param thingRegistry a possibly null {@link ThingRegistry}
     */
    @Reference(cardinality = ReferenceCardinality.MANDATORY, service = ThingRegistry.class, name = "ThingRegistry", policy = ReferencePolicy.DYNAMIC, unbind = "unsetThingRegistry")
    protected void setThingRegistry(ThingRegistry thingRegistry) {
        Objects.requireNonNull(thingRegistry, "thingRegistry cannot be null");

        this.thingRegistry = thingRegistry;
    }

    /**
     * Unsets thing registry.
     *
     * @param thingRegistry the thing registry (ignored)
     */
    protected void unsetThingRegistry(ThingRegistry thingRegistry) {
        this.thingRegistry = null;
    }

    /**
     * Set's the NEEO type generator
     *
     * @param typeGenerator a possibly null {@link NeeoTypeGenerator}
     */
    @Reference(cardinality = ReferenceCardinality.MANDATORY, service = NeeoTypeGenerator.class, name = "NeeoTypeGenerator", policy = ReferencePolicy.DYNAMIC, unbind = "unsetNeeoTypeGenerator")
    protected void setNeeoTypeGenerator(NeeoTypeGenerator typeGenerator) {
        Objects.requireNonNull(typeGenerator, "typeGenerator cannot be null");

        this.typeGenerator = typeGenerator;
    }

    /**
     * Unsets neeo type generator
     *
     * @param typeGenerator the neeo type provider (ignored)
     */
    protected void unsetNeeoTypeGenerator(NeeoTypeGenerator typeGenerator) {
        this.typeGenerator = null;
    }

    @Override
    public void deactivate() {
        logger.debug("Deactivating any active scans");
        NeeoUtil.cancel(scan.getAndSet(null));
    }

    @Override
    protected void startScan() {
        logger.debug("Starting NEEO Room scan");
        NeeoUtil.cancel(scan.getAndSet(scheduler.submit(() -> {
            try {
                scanForRooms();
            } catch (InterruptedException e) {
                logger.debug("NEEO Room scan interrupted: {}", e.getMessage(), e);
            }
        })));
    }

    /**
     * Starts the scan for rooms by looking through the {@link ThingRegistry} for any thing that has a
     * {@link NeeoBrainHandler} as it's handler. We then create {@link DiscoveryResult} for each room in the brain.
     *
     * @throws InterruptedException if our scan is interrupted
     */
    private void scanForRooms() throws InterruptedException {
        NeeoUtil.checkInterrupt();

        final ThingRegistry localThingRegistry = this.thingRegistry;
        if (localThingRegistry == null) {
            logger.debug("ThingRegistry is null - scan aborted");
            return;
        }

        final NeeoTypeGenerator localTypeGenerator = this.typeGenerator;
        if (localTypeGenerator == null) {
            logger.debug("TypeGenerator is null - scan aborted");
            return;
        }

        logger.debug("Scanning all things for NeeoBrainHandlers");
        for (final Thing thing : localThingRegistry.getAll()) {
            NeeoUtil.checkInterrupt();

            final ThingHandler handler = thing.getHandler();
            if (handler != null && handler instanceof NeeoBrainHandler) {
                final NeeoBrainHandler brainHandler = (NeeoBrainHandler) handler;

                final ThingUID brainUid = thing.getUID();
                final String brainId = brainUid.getId();

                final NeeoBrainApi api = brainHandler.getNeeoBrainApi();
                if (api == null) {
                    logger.debug("Brain API was not available for {} - skipping", brainId);
                    continue;
                }

                try {
                    // Important to re-retrieve new brain info in case rooms have changed
                    // since the NeeoBrainHandler was started...
                    final NeeoBrain brain = api.getBrain();

                    final NeeoBrainConfig config = thing.getConfiguration().as(NeeoBrainConfig.class);

                    final NeeoRoom[] rooms = brain.getRooms().getRooms();
                    if (rooms.length == 0) {
                        logger.debug("Brain {} ({}) found - but there were no rooms - skipping", brain.getName(),
                                brainId);
                    } else {
                        logger.debug("Brain {} ({}) found, scanning {} rooms in it", brain.getName(), brainId,
                                rooms.length);
                        for (NeeoRoom room : rooms) {
                            NeeoUtil.checkInterrupt();

                            final String roomKey = room.getKey();
                            if (roomKey == null || StringUtils.isEmpty(roomKey)) {
                                logger.debug("Room didn't have a room key: {}", room);
                                continue;
                            }

                            if (room.getDevices().getDevices().length == 0 && room.getRecipes().getRecipes().length == 0
                                    && !config.isDiscoverEmptyRooms()) {
                                logger.debug("Room {} ({}) found but has no devices or recipes, ignoring - {}",
                                        room.getKey(), brainId, room.getName());
                                continue;
                            }

                            logger.debug("Room {} ({}) found - {}", room.getKey(), brainId, room.getName());

                            logger.debug("Generating thing type for {}: {}", brainId, room);
                            localTypeGenerator.generate(brainId, room);

                            final ThingUID thingUID = new ThingUID(UidUtils.generateThingTypeUID(room), brainUid,
                                    "room");

                            final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                                    .withProperty(NeeoConstants.CONFIG_ROOMKEY, roomKey)
                                    .withProperty(NeeoConstants.CONFIG_EXCLUDE_THINGS, true).withBridge(brainUid)
                                    .withLabel(room.getName() + " (NEEO " + brainId + ")").build();
                            thingDiscovered(discoveryResult);
                        }

                    }
                } catch (IOException e) {
                    logger.debug("IOException occurred getting brain info ({}): {}", brainId, e.getMessage(), e);
                }
            }
        }
    }
}
