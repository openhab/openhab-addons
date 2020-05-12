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
package org.openhab.binding.avmfritz.internal.discovery;

import static org.eclipse.smarthome.core.thing.Thing.*;
import static org.openhab.binding.avmfritz.internal.BindingConstants.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerService;
import org.openhab.binding.avmfritz.internal.dto.AVMFritzBaseModel;
import org.openhab.binding.avmfritz.internal.dto.GroupModel;
import org.openhab.binding.avmfritz.internal.handler.AVMFritzBaseBridgeHandler;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaStatusListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discover all AHA (AVM Home Automation) devices connected to a FRITZ!Box device.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for groups
 */
@NonNullByDefault
public class AVMFritzDiscoveryService extends AbstractDiscoveryService
        implements FritzAhaStatusListener, DiscoveryService, ThingHandlerService {

    private final Logger logger = LoggerFactory.getLogger(AVMFritzDiscoveryService.class);
    /**
     * Handler of the bridge of which devices have to be discovered.
     */
    private @NonNullByDefault({}) AVMFritzBaseBridgeHandler bridgeHandler;

    public AVMFritzDiscoveryService() {
        super(Collections.unmodifiableSet(
                Stream.concat(SUPPORTED_DEVICE_THING_TYPES_UIDS.stream(), SUPPORTED_GROUP_THING_TYPES_UIDS.stream())
                        .collect(Collectors.toSet())),
                30);
    }

    @Override
    public void activate() {
        super.activate(null);
        bridgeHandler.registerStatusListener(this);
    }

    @Override
    public void deactivate() {
        bridgeHandler.unregisterStatusListener(this);
        super.deactivate();
    }

    @Override
    public void startScan() {
        logger.debug("Start manual scan on bridge {}", bridgeHandler.getThing().getUID());
        bridgeHandler.handleRefreshCommand();
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug("Stop manual scan on bridge {}", bridgeHandler.getThing().getUID());
        super.stopScan();
    }

    @Override
    public void setThingHandler(@NonNullByDefault({}) ThingHandler handler) {
        if (handler instanceof AVMFritzBaseBridgeHandler) {
            bridgeHandler = (AVMFritzBaseBridgeHandler) handler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return bridgeHandler;
    }

    @Override
    public void onDeviceAdded(AVMFritzBaseModel device) {
        ThingTypeUID thingTypeUID = new ThingTypeUID(BINDING_ID, bridgeHandler.getThingTypeId(device));
        if (getSupportedThingTypes().contains(thingTypeUID)) {
            ThingUID thingUID = new ThingUID(thingTypeUID, bridgeHandler.getThing().getUID(),
                    bridgeHandler.getThingName(device));
            onDeviceAddedInternal(thingUID, device);
        } else {
            logger.debug("Discovered unsupported device: {}", device);
        }
    }

    @Override
    public void onDeviceUpdated(ThingUID thingUID, AVMFritzBaseModel device) {
        onDeviceAddedInternal(thingUID, device);
    }

    @Override
    public void onDeviceGone(ThingUID thingUID) {
        // nothing to do
    }

    private void onDeviceAddedInternal(ThingUID thingUID, AVMFritzBaseModel device) {
        if (device.getPresent() == 1) {
            Map<String, Object> properties = new HashMap<>();
            properties.put(CONFIG_AIN, device.getIdentifier());
            properties.put(PROPERTY_VENDOR, device.getManufacturer());
            properties.put(PROPERTY_MODEL_ID, device.getDeviceId());
            properties.put(PROPERTY_SERIAL_NUMBER, device.getIdentifier());
            properties.put(PROPERTY_FIRMWARE_VERSION, device.getFirmwareVersion());
            if (device instanceof GroupModel && ((GroupModel) device).getGroupinfo() != null) {
                properties.put(PROPERTY_MASTER, ((GroupModel) device).getGroupinfo().getMasterdeviceid());
                properties.put(PROPERTY_MEMBERS, ((GroupModel) device).getGroupinfo().getMembers());
            }

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(CONFIG_AIN).withBridge(bridgeHandler.getThing().getUID())
                    .withLabel(device.getName()).build();

            thingDiscovered(discoveryResult);
        } else {
            thingRemoved(thingUID);
        }
    }
}
