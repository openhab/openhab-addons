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
import org.openhab.binding.neeo.handler.NeeoRoomHandler;
import org.openhab.binding.neeo.internal.NeeoBrainApi;
import org.openhab.binding.neeo.internal.NeeoRoomConfig;
import org.openhab.binding.neeo.internal.models.NeeoDevice;
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
 * Implementation of {@link AbstractDiscoveryService} that will discover the devices in a NEEO room;
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true)
public class NeeoDeviceDiscoveryService extends AbstractDiscoveryService {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoDeviceDiscoveryService.class);

    /** The scanning task (not-null when connecting, null otherwise) */
    private final AtomicReference<@Nullable Future<?>> scan = new AtomicReference<>();

    /** The thing registry that we use */
    @NonNullByDefault({})
    private ThingRegistry thingRegistry;

    /** The type generator */
    @NonNullByDefault({})
    private NeeoTypeGenerator typeGenerator;

    /**
     * The thing types we discover. Since the device types are all dynamically generated, we simply return the binding
     * id plus a dummy thingtypeid. The dummy thingtypeid doesn't matter since we only want to be part of the NEEO
     * binding scan (which only uses the binding id part).
     *
     */
    private static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Collections
            .singleton(new ThingTypeUID(NeeoConstants.BINDING_ID, "devices"));

    /**
     * Instantiates a new NEEO room discovery service.
     */
    public NeeoDeviceDiscoveryService() {
        super(DISCOVERABLE_THING_TYPES_UIDS, NeeoConstants.DEVICE_DISCOVERY_TIMEOUT);
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
        logger.debug("Starting NEEO Device scan");
        NeeoUtil.cancel(scan.getAndSet(scheduler.submit(() -> {
            try {
                scanForDevices();
            } catch (InterruptedException e) {
                logger.debug("NEEO Device scan interrupted: {}", e.getMessage(), e);
            }
        })));
    }

    /**
     * Starts the scan for devices by looking through the {@link ThingRegistry} for any thing that has a
     * {@link NeeoRoomHandler} as it's handler. We then create {@link DiscoveryResult} for each device in the room.
     *
     * @throws InterruptedException if our scan is interrupted
     */
    private void scanForDevices() throws InterruptedException {
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

        logger.debug("Scanning all things for NeeoRoomHandlers");
        for (final Thing thing : localThingRegistry.getAll()) {
            NeeoUtil.checkInterrupt();

            final ThingHandler handler = thing.getHandler();
            if (handler != null && handler instanceof NeeoRoomHandler) {
                final NeeoRoomHandler roomHandler = (NeeoRoomHandler) handler;

                final String brainId = roomHandler.getNeeoBrainId();
                if (brainId == null || StringUtils.isEmpty(brainId)) {
                    logger.debug("Unknown brain ID for roomHandler: {}", roomHandler);
                    continue;
                }

                final NeeoBrainApi api = roomHandler.getNeeoBrainApi();
                if (api == null) {
                    logger.debug("Brain API was not available for {} - skipping", brainId);
                    continue;
                }

                final NeeoRoomConfig config = thing.getConfiguration().as(NeeoRoomConfig.class);
                final String roomKey = config.getRoomKey();
                if (roomKey == null || StringUtils.isEmpty(roomKey)) {
                    logger.debug("RoomKey wasn't configured for {} - skipping", brainId);
                    continue;
                }

                final ThingUID roomUid = thing.getUID();
                final ThingTypeUID roomTypeUid = thing.getThingTypeUID();

                try {
                    // Important to re-retrieve new room info in case devices have changed
                    // since the NeeoRoomHandler was started...
                    final NeeoRoom room = api.getRoom(roomKey);
                    final NeeoDevice[] devices = room.getDevices().getDevices();

                    if (devices.length == 0) {
                        logger.debug("Room {} found - but there were no devices - skipping", room.getName());
                    } else {
                        logger.debug("Room {} found, scanning {} devices in it", room.getName(), devices.length);
                        for (NeeoDevice device : devices) {
                            NeeoUtil.checkInterrupt();

                            final String deviceKey = device.getKey();
                            if (deviceKey == null || StringUtils.isEmpty(deviceKey)) {
                                logger.debug("Device key wasn't found for device: {}", device);
                                continue;
                            }

                            if (config.isExcludeThings() && UidUtils.isThing(device)) {
                                logger.debug("Found openHAB thing but ignoring per configuration: {}", device);
                                continue;
                            }

                            logger.debug("Device #{} found - {}", deviceKey, device.getName());

                            logger.debug("Generating thing type for {}, {}: {}", brainId, roomTypeUid, device);
                            localTypeGenerator.generate(brainId, roomTypeUid, device);

                            final ThingUID thingUID = new ThingUID(UidUtils.generateThingTypeUID(device), roomUid,
                                    "device");

                            final DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                                    .withProperty(NeeoConstants.CONFIG_DEVICEKEY, deviceKey).withBridge(roomUid)
                                    .withLabel(device.getName() + " (NEEO " + brainId + ")").build();

                            NeeoUtil.checkInterrupt();
                            thingDiscovered(discoveryResult);
                        }
                    }
                } catch (IOException e) {
                    logger.debug("IOException occurred getting brain info ({}) for room {}: {}", brainId,
                            config.getRoomKey(), e.getMessage(), e);
                }
            }
        }
    }
}
