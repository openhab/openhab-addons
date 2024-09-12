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
package org.openhab.binding.max.internal.discovery;

import java.time.Instant;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.max.internal.MaxBindingConstants;
import org.openhab.binding.max.internal.device.Device;
import org.openhab.binding.max.internal.handler.DeviceStatusListener;
import org.openhab.binding.max.internal.handler.MaxCubeBridgeHandler;
import org.openhab.core.config.discovery.AbstractThingHandlerDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MaxDeviceDiscoveryService} class is used to discover MAX! Cube
 * devices that are connected to the Lan gateway.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = MaxDeviceDiscoveryService.class)
@NonNullByDefault
public class MaxDeviceDiscoveryService extends AbstractThingHandlerDiscoveryService<MaxCubeBridgeHandler>
        implements DeviceStatusListener {

    private static final int SEARCH_TIME = 60;
    private final Logger logger = LoggerFactory.getLogger(MaxDeviceDiscoveryService.class);

    public MaxDeviceDiscoveryService() {
        super(MaxCubeBridgeHandler.class, MaxBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS, SEARCH_TIME, true);
    }

    @Override
    public void initialize() {
        thingHandler.registerDeviceStatusListener(this);
        super.initialize();
    }

    @Override
    public void dispose() {
        super.dispose();
        thingHandler.unregisterDeviceStatusListener(this);
        removeOlderResults(Instant.now().toEpochMilli(), thingHandler.getThing().getUID());
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return MaxBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS;
    }

    @Override
    public void onDeviceAdded(Bridge bridge, Device device) {
        logger.trace("Adding new MAX! {} with id '{}' to inbox", device.getType(), device.getSerialNumber());
        ThingUID thingUID = null;
        switch (device.getType()) {
            case WallMountedThermostat:
                thingUID = new ThingUID(MaxBindingConstants.WALLTHERMOSTAT_THING_TYPE, bridge.getUID(),
                        device.getSerialNumber());
                break;
            case HeatingThermostat:
                thingUID = new ThingUID(MaxBindingConstants.HEATINGTHERMOSTAT_THING_TYPE, bridge.getUID(),
                        device.getSerialNumber());
                break;
            case HeatingThermostatPlus:
                thingUID = new ThingUID(MaxBindingConstants.HEATINGTHERMOSTATPLUS_THING_TYPE, bridge.getUID(),
                        device.getSerialNumber());
                break;
            case ShutterContact:
                thingUID = new ThingUID(MaxBindingConstants.SHUTTERCONTACT_THING_TYPE, bridge.getUID(),
                        device.getSerialNumber());
                break;
            case EcoSwitch:
                thingUID = new ThingUID(MaxBindingConstants.ECOSWITCH_THING_TYPE, bridge.getUID(),
                        device.getSerialNumber());
                break;
            default:
                break;
        }
        if (thingUID != null) {
            String name = device.getName();
            if (name.isEmpty()) {
                name = device.getSerialNumber();
            }
            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                    .withProperty(Thing.PROPERTY_SERIAL_NUMBER, device.getSerialNumber()).withBridge(bridge.getUID())
                    .withLabel(device.getType() + ": " + name).withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER)
                    .build();
            thingDiscovered(discoveryResult);
        } else {
            logger.debug("Discovered MAX! device is unsupported: type '{}' with id '{}'", device.getType(),
                    device.getSerialNumber());
        }
    }

    @Override
    protected void startScan() {
        thingHandler.clearDeviceList();
        thingHandler.deviceInclusion();
    }

    @Override
    public void onDeviceStateChanged(ThingUID bridge, Device device) {
        // this can be ignored here
    }

    @Override
    public void onDeviceRemoved(MaxCubeBridgeHandler bridge, Device device) {
        // this can be ignored here
    }

    @Override
    public void onDeviceConfigUpdate(Bridge bridge, Device device) {
        // this can be ignored here
    }
}
