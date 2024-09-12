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
package org.openhab.binding.enocean.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.enocean.internal.discovery.EnOceanDeviceDiscoveryService;
import org.openhab.binding.enocean.internal.handler.EnOceanBaseActuatorHandler;
import org.openhab.binding.enocean.internal.handler.EnOceanBaseSensorHandler;
import org.openhab.binding.enocean.internal.handler.EnOceanBridgeHandler;
import org.openhab.binding.enocean.internal.handler.EnOceanClassicDeviceHandler;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingManager;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.link.ItemChannelLinkRegistry;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link EnOceanHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.enocean")
public class EnOceanHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(EnOceanBridgeHandler.SUPPORTED_THING_TYPES.stream(),
                    EnOceanBindingConstants.SUPPORTED_DEVICE_THING_TYPES_UIDS.stream())
            .collect(Collectors.toSet());

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    private final SerialPortManager serialPortManager;
    private final ThingManager thingManager;
    private final ItemChannelLinkRegistry itemChannelLinkRegistry;

    @Activate
    public EnOceanHandlerFactory(final @Reference SerialPortManager serialPortManager,
            final @Reference ThingManager thingManager,
            final @Reference ItemChannelLinkRegistry itemChannelLinkRegistry) {
        // Obtain references to thes service using an OSGi reference
        this.serialPortManager = serialPortManager;
        this.thingManager = thingManager;
        this.itemChannelLinkRegistry = itemChannelLinkRegistry;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (EnOceanBridgeHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            EnOceanBridgeHandler bridgeHandler = new EnOceanBridgeHandler((Bridge) thing, serialPortManager);
            registerDeviceDiscoveryService(bridgeHandler);
            return bridgeHandler;
        } else if (EnOceanBaseActuatorHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new EnOceanBaseActuatorHandler(thing, itemChannelLinkRegistry);
        } else if (EnOceanBaseSensorHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new EnOceanBaseSensorHandler(thing, itemChannelLinkRegistry);
        } else if (EnOceanClassicDeviceHandler.SUPPORTED_THING_TYPES.contains(thingTypeUID)) {
            return new EnOceanClassicDeviceHandler(thing, itemChannelLinkRegistry);
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
        if (serviceReg != null) {
            serviceReg.unregister();
            discoveryServiceRegs.remove(thingHandler.getThing().getUID());
        }
    }

    private void registerDeviceDiscoveryService(EnOceanBridgeHandler handler) {
        EnOceanDeviceDiscoveryService discoveryService = new EnOceanDeviceDiscoveryService(handler, thingManager);
        discoveryService.activate();
        this.discoveryServiceRegs.put(handler.getThing().getUID(),
                bundleContext.registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>()));
    }
}
