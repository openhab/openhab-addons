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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.thing.ThingDiscoveryParticipant;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.neeo.NeeoConstants;
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
 * Implementation of {@link ThingDiscoveryParticipant} that will discover the devices in a NEEO room;
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
@Component(immediate = true)
public class NeeoDeviceDiscoveryService implements ThingDiscoveryParticipant {

    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(NeeoDeviceDiscoveryService.class);

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
    public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
        return DISCOVERABLE_THING_TYPES_UIDS;
    }

    @Override
    @Nullable
    public Collection<DiscoveryResult> createResults(Thing thing) {
        final NeeoTypeGenerator localTypeGenerator = this.typeGenerator;
        if (localTypeGenerator == null) {
            logger.debug("TypeGenerator is null - can't create results");
            return null;
        }

        final ThingHandler handler = thing.getHandler();
        if (handler == null || !(handler instanceof NeeoRoomHandler)) {
            return null;
        }

        final NeeoRoomHandler roomHandler = (NeeoRoomHandler) handler;

        final ThingUID roomUid = thing.getUID();
        final ThingTypeUID roomTypeUid = thing.getThingTypeUID();

        final String brainId = roomHandler.getNeeoBrainId();
        if (brainId == null || StringUtils.isEmpty(brainId)) {
            logger.debug("Unknown brain ID for roomHandler: {}", roomHandler);
            return null;
        }

        final NeeoBrainApi api = roomHandler.getNeeoBrainApi();
        if (api == null) {
            logger.debug("Brain API was not available for {} - skipping", brainId);
            return null;
        }

        final NeeoRoomConfig config = thing.getConfiguration().as(NeeoRoomConfig.class);
        final String roomKey = config.getRoomKey();
        if (roomKey == null || StringUtils.isEmpty(roomKey)) {
            logger.debug("RoomKey wasn't configured for {} - skipping", brainId);
            return null;
        }

        try {
            final NeeoRoom room = api.getRoom(roomKey);
            final NeeoDevice[] devices = room.getDevices().getDevices();

            if (devices.length == 0) {
                logger.debug("Room {} found - but there were no devices - skipping", room.getName());
                return null;
            }

            logger.debug("Room {} found, scanning {} devices in it", room.getName(), devices.length);
            final List<DiscoveryResult> results = new ArrayList<>();
            for (NeeoDevice device : devices) {
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

                final ThingUID thingUID = new ThingUID(UidUtils.generateThingTypeUID(device), roomUid, "device");

                results.add(
                        DiscoveryResultBuilder.create(thingUID).withProperty(NeeoConstants.CONFIG_DEVICEKEY, deviceKey)
                                .withBridge(roomUid).withLabel(device.getName() + " (NEEO " + brainId + ")").build());

            }

            return results;
        } catch (IOException e) {
            logger.debug("IOException occurred getting brain info ({}): {}", brainId, e.getMessage(), e);
            return null;
        }
    }

}
