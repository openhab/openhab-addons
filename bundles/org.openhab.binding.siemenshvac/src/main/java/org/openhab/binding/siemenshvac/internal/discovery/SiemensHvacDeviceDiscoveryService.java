
/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.siemenshvac.internal.discovery;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.Metadata.SiemensHvacMetadata;
import org.openhab.binding.siemenshvac.internal.Metadata.SiemensHvacMetadataMenu;
import org.openhab.binding.siemenshvac.internal.Metadata.SiemensHvacMetadataRegistry;
import org.openhab.binding.siemenshvac.internal.constants.SiemensHvacBindingConstants;
import org.openhab.binding.siemenshvac.internal.handler.SiemensHvacBridgeBaseThingHandler;
import org.openhab.binding.siemenshvac.internal.network.SiemensHvacConnector;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SiemensHvacDeviceDiscoveryService} tracks for Siemens Hvac device connected to the bus.
 *
 * @author Laurent Arnal - Initial contribution
 */
// @Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.siemenshvac")
public class SiemensHvacDeviceDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    private static final Logger logger = LoggerFactory.getLogger(SiemensHvacDeviceDiscoveryService.class);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections
            .singleton(SiemensHvacBindingConstants.THING_TYPE_RVS41_813_327);

    private @Nullable SiemensHvacMetadataRegistry metadataRegistry;
    private @Nullable SiemensHvacBridgeBaseThingHandler siemensHvacBridgeHandler;
    private @Nullable SiemensHvacConnector hvacConnector;

    private static final int SEARCH_TIME = 10;

    public SiemensHvacDeviceDiscoveryService() {
        super(SUPPORTED_THING_TYPES, SEARCH_TIME);
    }

    @Reference
    public void setSiemensHvacMetadataRegistry(SiemensHvacMetadataRegistry metadataRegistry) {
        this.metadataRegistry = metadataRegistry;
    }

    public void unsetSiemensHvacMetadataRegistry(SiemensHvacMetadataRegistry metadataRegistry) {
        this.metadataRegistry = null;
    }

    @Override
    protected void startBackgroundDiscovery() {
    }

    @Override
    protected void stopBackgroundDiscovery() {
        // can be overridden
    }

    private @Nullable ThingUID getThingUID() {
        if (siemensHvacBridgeHandler != null) {
            ThingUID localBridgeUID = siemensHvacBridgeHandler.getThing().getUID();
            if (localBridgeUID != null) {
                ThingTypeUID thingTypeUID = SiemensHvacBindingConstants.THING_TYPE_RVS41_813_327;

                if (thingTypeUID != null && getSupportedThingTypes().contains(thingTypeUID)) {
                    return new ThingUID(thingTypeUID, localBridgeUID, "-1");
                }
            }
        }
        return null;
    }

    @Override
    public void startScan() {
        logger.debug("call startScan()");

        final SiemensHvacBridgeBaseThingHandler handler = siemensHvacBridgeHandler;

        if (metadataRegistry != null) {
            metadataRegistry.ReadMeta();

            SiemensHvacMetadataMenu rootMenu = metadataRegistry.getRoot();
            for (SiemensHvacMetadata child : rootMenu.getChilds().values()) {
                if (child.getLongDesc().indexOf("OZW672") >= 0) {
                    continue;
                }

                ThingUID thingUID = getThingUID();
                ThingUID bridgeUID = siemensHvacBridgeHandler.getThing().getUID();

                if (thingUID != null) {
                    Map<String, Object> properties = new HashMap<>(1);
                    properties.put("CatId", child.getCatId());
                    properties.put("GroupId", child.getGroupId());
                    properties.put("ItemId", child.getMenuId());
                    properties.put("LongDesc", child.getLongDesc());
                    properties.put("ShortDesc", child.getShortDesc());

                    DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                            .withBridge(bridgeUID).withLabel(child.getLongDesc()).build();

                    thingDiscovered(discoveryResult);
                }

            }
        }

    }

    @Override
    protected synchronized void stopScan() {
        super.stopScan();
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof SiemensHvacBridgeBaseThingHandler) {
            siemensHvacBridgeHandler = (SiemensHvacBridgeBaseThingHandler) handler;
            // bridgeUID = handler.getThing().getUID();
        }

    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return siemensHvacBridgeHandler;
    }

    @Override
    public void activate() {
        final SiemensHvacBridgeBaseThingHandler handler = siemensHvacBridgeHandler;
        if (handler != null) {
            handler.registerDiscoveryListener(this);
        }

    }

    @Override
    public void deactivate() {
        /*
         * removeOlderResults(new Date().getTime(), bridgeUID);
         * final HueBridgeHandler handler = hueBridgeHandler;
         * if (handler != null) {
         * handler.unregisterDiscoveryListener();
         * }
         */
    }

}
