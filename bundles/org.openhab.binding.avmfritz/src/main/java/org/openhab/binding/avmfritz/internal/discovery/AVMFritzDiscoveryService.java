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
package org.openhab.binding.avmfritz.internal.discovery;

import static org.openhab.binding.avmfritz.internal.AVMFritzBindingConstants.*;
import static org.openhab.core.thing.Thing.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.avmfritz.internal.dto.AVMFritzBaseModel;
import org.openhab.binding.avmfritz.internal.dto.GroupModel;
import org.openhab.binding.avmfritz.internal.handler.AVMFritzBaseBridgeHandler;
import org.openhab.binding.avmfritz.internal.hardware.FritzAhaStatusListener;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Discover all AHA (AVM Home Automation) devices connected to a FRITZ!Box device.
 *
 * @author Robert Bausdorf - Initial contribution
 * @author Christoph Weitkamp - Added support for groups
 */
@Component(scope = ServiceScope.PROTOTYPE, service = AVMFritzDiscoveryService.class)
@NonNullByDefault
public class AVMFritzDiscoveryService extends AbstractThingHandlerDiscoveryService<AVMFritzBaseBridgeHandler>
        implements FritzAhaStatusListener, DiscoveryService {
    private final Logger logger = LoggerFactory.getLogger(AVMFritzDiscoveryService.class);

    public AVMFritzDiscoveryService() {
        super(AVMFritzBaseBridgeHandler.class, Stream
                .of(SUPPORTED_LIGHTING_THING_TYPES, SUPPORTED_BUTTON_THING_TYPES_UIDS, SUPPORTED_HEATING_THING_TYPES,
                        SUPPORTED_DEVICE_THING_TYPES_UIDS, SUPPORTED_GROUP_THING_TYPES_UIDS)
                .flatMap(Set::stream).collect(Collectors.toUnmodifiableSet()), 30);
    }

    @Override
    public void initialize() {
        thingHandler.registerStatusListener(this);
        super.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
        thingHandler.unregisterStatusListener(this);
    }

    @Override
    public void startScan() {
        logger.debug("Start manual scan on bridge {}", thingHandler.getThing().getUID());
        thingHandler.handleRefreshCommand();
    }

    @Override
    protected synchronized void stopScan() {
        logger.debug("Stop manual scan on bridge {}", thingHandler.getThing().getUID());
        super.stopScan();
    }

    @Override
    public void onDeviceAdded(AVMFritzBaseModel device) {
        String id = thingHandler.getThingTypeId(device);
        ThingTypeUID thingTypeUID = id.isEmpty() ? null : new ThingTypeUID(BINDING_ID, id);
        if (thingTypeUID != null && getSupportedThingTypes().contains(thingTypeUID)) {
            ThingUID thingUID = new ThingUID(thingTypeUID, thingHandler.getThing().getUID(),
                    thingHandler.getThingName(device));
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
            properties.put(PRODUCT_NAME, device.getProductName());
            properties.put(PROPERTY_SERIAL_NUMBER, device.getIdentifier());
            properties.put(PROPERTY_FIRMWARE_VERSION, device.getFirmwareVersion());
            if (device instanceof GroupModel model && model.getGroupinfo() != null) {
                properties.put(PROPERTY_MASTER, model.getGroupinfo().getMasterdeviceid());
                properties.put(PROPERTY_MEMBERS, model.getGroupinfo().getMembers());
            }

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID).withProperties(properties)
                    .withRepresentationProperty(CONFIG_AIN).withBridge(thingHandler.getThing().getUID())
                    .withLabel(device.getName()).build();

            thingDiscovered(discoveryResult);
        } else {
            thingRemoved(thingUID);
        }
    }
}
