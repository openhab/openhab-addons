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
package org.openhab.binding.hue.internal.discovery;

import static org.openhab.binding.hue.internal.HueBindingConstants.*;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hue.internal.dto.clip2.MetaData;
import org.openhab.binding.hue.internal.dto.clip2.Resource;
import org.openhab.binding.hue.internal.dto.clip2.ResourceReference;
import org.openhab.binding.hue.internal.dto.clip2.enums.Archetype;
import org.openhab.binding.hue.internal.dto.clip2.enums.ResourceType;
import org.openhab.binding.hue.internal.exceptions.ApiException;
import org.openhab.binding.hue.internal.exceptions.AssetNotLoadedException;
import org.openhab.binding.hue.internal.handler.Clip2BridgeHandler;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discovery service to find resource things on a Hue Bridge that is running CLIP 2.
 *
 * @author Andrew Fiddian-Green - Initial Contribution
 */
@NonNullByDefault
public class Clip2ThingDiscoveryService extends AbstractDiscoveryService implements ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(Clip2ThingDiscoveryService.class);

    private static final int DISCOVERY_TIMEOUT_SECONDS = 20;
    private static final int DISCOVERY_INTERVAL_SECONDS = 600;

    /**
     * Map of resource types and respective thing types that shall be discovered.
     */
    private static final Map<ResourceType, ThingTypeUID> DISCOVERY_TYPES = Map.of( //
            ResourceType.DEVICE, THING_TYPE_DEVICE, //
            ResourceType.ROOM, THING_TYPE_ROOM, //
            ResourceType.ZONE, THING_TYPE_ZONE, //
            ResourceType.BRIDGE_HOME, THING_TYPE_ZONE);

    private @Nullable Clip2BridgeHandler bridgeHandler;
    private @Nullable ScheduledFuture<?> discoveryTask;

    public Clip2ThingDiscoveryService() {
        super(Set.of(THING_TYPE_DEVICE, THING_TYPE_ROOM, THING_TYPE_ZONE), DISCOVERY_TIMEOUT_SECONDS, true);
    }

    @Override
    public void activate() {
        Clip2BridgeHandler bridgeHandler = this.bridgeHandler;
        if (Objects.nonNull(bridgeHandler)) {
            bridgeHandler.registerDiscoveryService(this);
        }
        super.activate(null);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        Clip2BridgeHandler bridgeHandler = this.bridgeHandler;
        if (Objects.nonNull(bridgeHandler)) {
            bridgeHandler.unregisterDiscoveryService();
            removeOlderResults(new Date().getTime(), bridgeHandler.getThing().getBridgeUID());
            this.bridgeHandler = null;
        }
    }

    /**
     * If the bridge is online, then query it to get all resource types within it, which are allowed to be instantiated
     * as OH things, and announce those respective things by calling the core 'thingDiscovered()' method.
     */
    private synchronized void discoverThings() {
        Clip2BridgeHandler bridgeHandler = this.bridgeHandler;
        if (Objects.nonNull(bridgeHandler) && bridgeHandler.getThing().getStatus() == ThingStatus.ONLINE) {
            try {
                ThingUID bridgeUID = bridgeHandler.getThing().getUID();
                for (Entry<ResourceType, ThingTypeUID> entry : DISCOVERY_TYPES.entrySet()) {
                    for (Resource resource : bridgeHandler.getResources(new ResourceReference().setType(entry.getKey()))
                            .getResources()) {

                        MetaData metaData = resource.getMetaData();
                        if (Objects.nonNull(metaData) && (metaData.getArchetype() == Archetype.BRIDGE_V2)) {
                            // the bridge device is handled by a bridge thing handler
                            continue;
                        }

                        String resourceId = resource.getId();
                        String idv1 = resource.getIdV1();
                        String resourceType = resource.getType().toString();
                        String resourceName = resource.getName();
                        String thingId = resourceId;
                        String thingLabel = resourceName;
                        String legacyThingUID = null;

                        // special zone 'all lights'
                        if (resource.getType() == ResourceType.BRIDGE_HOME) {
                            thingLabel = bridgeHandler.getLocalizedText(ALL_LIGHTS_KEY);
                        }

                        Optional<Thing> legacyThingOptional = bridgeHandler.getLegacyThing(idv1);
                        if (legacyThingOptional.isPresent()) {
                            Thing legacyThing = legacyThingOptional.get();
                            legacyThingUID = legacyThing.getUID().getAsString();
                            thingId = legacyThing.getUID().getId();
                            String legacyLabel = legacyThing.getLabel();
                            thingLabel = Objects.nonNull(legacyLabel) ? legacyLabel : thingLabel;
                        }

                        DiscoveryResultBuilder builder = DiscoveryResultBuilder
                                .create(new ThingUID(entry.getValue(), bridgeUID, thingId)) //
                                .withBridge(bridgeUID) //
                                .withLabel(thingLabel) //
                                .withProperty(PROPERTY_RESOURCE_ID, resourceId)
                                .withProperty(PROPERTY_RESOURCE_TYPE, resourceType)
                                .withProperty(PROPERTY_RESOURCE_NAME, resourceName)
                                .withRepresentationProperty(PROPERTY_RESOURCE_ID);

                        if (Objects.nonNull(legacyThingUID)) {
                            builder = builder.withProperty(PROPERTY_LEGACY_THING_UID, legacyThingUID);
                        }
                        thingDiscovered(builder.build());
                    }
                }
            } catch (ApiException | AssetNotLoadedException e) {
                logger.debug("discoverThings() bridge is offline or in a bad state");
            } catch (InterruptedException e) {
            }
        }
        stopScan();
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void setThingHandler(ThingHandler handler) {
        if (handler instanceof Clip2BridgeHandler) {
            bridgeHandler = (Clip2BridgeHandler) handler;
        }
    }

    @Override
    protected void startBackgroundDiscovery() {
        ScheduledFuture<?> discoveryTask = this.discoveryTask;
        if (Objects.isNull(discoveryTask) || discoveryTask.isCancelled()) {
            this.discoveryTask = scheduler.scheduleWithFixedDelay(this::discoverThings, 0, DISCOVERY_INTERVAL_SECONDS,
                    TimeUnit.SECONDS);
        }
    }

    @Override
    protected void startScan() {
        scheduler.execute(this::discoverThings);
    }

    @Override
    protected void stopBackgroundDiscovery() {
        ScheduledFuture<?> discoveryTask = this.discoveryTask;
        if (Objects.nonNull(discoveryTask)) {
            discoveryTask.cancel(true);
            this.discoveryTask = null;
        }
    }
}
