/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.siemenshvac.internal.constants.SiemensHvacBindingConstants;
import org.openhab.binding.siemenshvac.internal.handler.SiemensHvacBridgeThingHandler;
import org.openhab.binding.siemenshvac.internal.metadata.SiemensHvacMetadataDevice;
import org.openhab.binding.siemenshvac.internal.metadata.SiemensHvacMetadataRegistry;
import org.openhab.binding.siemenshvac.internal.type.SiemensHvacException;
import org.openhab.binding.siemenshvac.internal.type.UidUtils;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
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
@NonNullByDefault
public class SiemensHvacDeviceDiscoveryService extends AbstractDiscoveryService
        implements DiscoveryService, ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(SiemensHvacDeviceDiscoveryService.class);

    private @Nullable SiemensHvacMetadataRegistry metadataRegistry;
    private @Nullable SiemensHvacBridgeThingHandler siemensHvacBridgeHandler;

    private static final int SEARCH_TIME = 10;

    public SiemensHvacDeviceDiscoveryService() {
        super(SiemensHvacBindingConstants.SUPPORTED_THING_TYPES, SEARCH_TIME);
    }

    @Reference
    public void setSiemensHvacMetadataRegistry(@Nullable SiemensHvacMetadataRegistry metadataRegistry) {
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
    }

    private @Nullable ThingUID getThingUID(ThingTypeUID thingTypeUID, String serial) {
        final SiemensHvacBridgeThingHandler lcSiemensHvacBridgeHandler = siemensHvacBridgeHandler;
        if (lcSiemensHvacBridgeHandler != null) {
            ThingUID localBridgeUID = lcSiemensHvacBridgeHandler.getThing().getUID();
            return new ThingUID(thingTypeUID, localBridgeUID, serial);
        }
        return null;
    }

    @Override
    public void startScan() {
        final SiemensHvacMetadataRegistry lcMetadataRegistry = metadataRegistry;
        final SiemensHvacBridgeThingHandler lcSiemensHvacBridgeHandler = siemensHvacBridgeHandler;
        logger.debug("call startScan()");

        if (lcMetadataRegistry != null) {
            try {
                lcMetadataRegistry.readMeta();
            } catch (SiemensHvacException ex) {
                logger.debug("Exception occurred during execution: {}", ex.getMessage(), ex);
                return;
            }

            ArrayList<SiemensHvacMetadataDevice> devices = lcMetadataRegistry.getDevices();

            if (devices == null) {
                return;
            }

            for (SiemensHvacMetadataDevice device : devices) {

                String name = device.getName();
                String type = device.getType();
                String addr = device.getAddr();
                String serialNr = device.getSerialNr();

                logger.debug("Find devices: {} / {} / {} / {}", name, type, addr, serialNr);

                String typeSn = UidUtils.sanetizeId(type);
                ThingTypeUID thingTypeUID = new ThingTypeUID(SiemensHvacBindingConstants.BINDING_ID, typeSn);

                ThingUID thingUID = getThingUID(thingTypeUID, serialNr);

                if (lcSiemensHvacBridgeHandler != null) {
                    ThingUID bridgeUID = lcSiemensHvacBridgeHandler.getThing().getUID();

                    if (thingUID != null) {
                        Map<String, Object> properties = new HashMap<>(4);
                        properties.put(Thing.PROPERTY_MODEL_ID, name);
                        properties.put("type", type);
                        properties.put("addr", addr);
                        properties.put(Thing.PROPERTY_SERIAL_NUMBER, serialNr);

                        DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                                .withProperties(properties).withBridge(bridgeUID).withLabel(name).build();

                        thingDiscovered(discoveryResult);
                    }
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
        if (handler instanceof SiemensHvacBridgeThingHandler siemensHvacBridgeHandler) {
            this.siemensHvacBridgeHandler = siemensHvacBridgeHandler;
            this.siemensHvacBridgeHandler.registerDiscoveryListener(this);
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return siemensHvacBridgeHandler;
    }

    @Override
    public void deactivate() {
    }
}
