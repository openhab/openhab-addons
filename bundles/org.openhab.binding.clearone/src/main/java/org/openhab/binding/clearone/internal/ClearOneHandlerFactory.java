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
package org.openhab.binding.clearone.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.clearone.internal.config.StackConfiguration;
import org.openhab.binding.clearone.internal.config.UnitConfiguration;
import org.openhab.binding.clearone.internal.config.ZoneConfiguration;
import org.openhab.binding.clearone.internal.handler.ClearOneStackHandler;
import org.openhab.binding.clearone.internal.handler.ClearOneUnitHandler;
import org.openhab.binding.clearone.internal.handler.ClearOneZoneHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link ClearOneHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Garry Mitchell - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.clearone", service = ThingHandlerFactory.class)
public class ClearOneHandlerFactory extends BaseThingHandlerFactory {

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegistrations = new HashMap<>();
    private @NonNullByDefault({}) Supplier<SerialPortManager> serialPortManagerSupplier = () -> null;

    @Nullable
    private ClearOneDynamicStateDescriptionProvider stateDescriptionProvider;

    @Nullable
    private ClearOneDynamicConfigOptionProvider optionDescriptionProvider;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return ClearOneBindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public @Nullable Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration,
            @Nullable ThingUID thingUID, @Nullable ThingUID bridgeUID) {
        if (ClearOneBindingConstants.THING_TYPE_STACK.equals(thingTypeUID)) {
            ThingUID stackBridgeUID = getStackUID(thingTypeUID, thingUID, configuration);
            return super.createThing(thingTypeUID, configuration, stackBridgeUID, null);
        } else if (ClearOneBindingConstants.THING_TYPE_UNIT.equals(thingTypeUID)) {
            ThingUID unitThingUID = getUnitUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, unitThingUID, bridgeUID);
        } else if (ClearOneBindingConstants.THING_TYPE_ZONE.equals(thingTypeUID)) {
            ThingUID zoneThingUID = getZoneUID(thingTypeUID, thingUID, configuration, bridgeUID);
            return super.createThing(thingTypeUID, configuration, zoneThingUID, bridgeUID);
        }

        throw new IllegalArgumentException(
                "createThing(): The thing type " + thingTypeUID + " is not supported by the ClearOne binding.");
    }

    /**
     * Get the Bridge Thing UID.
     *
     * @param thingTypeUID
     * @param thingUID
     * @param configuration
     * @return thingUID
     */
    private @Nullable ThingUID getStackUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            Configuration configuration) {
        if (thingUID == null) {
            String serialPort = (String) configuration.get(StackConfiguration.SERIAL_PORT);
            String bridgeID = serialPort.replace('.', '_');
            return new ThingUID(thingTypeUID, bridgeID);
        }
        return thingUID;
    }

    /**
     * Get the Unit Thing UID.
     *
     * @param thingTypeUID
     * @param thingUID
     * @param configuration
     * @param bridgeUID
     * @return thingUID
     */
    private @Nullable ThingUID getUnitUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            Configuration configuration, @Nullable ThingUID bridgeUID) {
        if (thingUID == null && bridgeUID != null) {
            String unitId = "unit" + (String) configuration.get(UnitConfiguration.UNIT_NUMBER);
            return new ThingUID(thingTypeUID, unitId, bridgeUID.getId());
        }
        return thingUID;
    }

    /**
     * Get the Zone Thing UID.
     *
     * @param thingTypeUID
     * @param thingUID
     * @param configuration
     * @param bridgeUID
     * @return thingUID
     */
    private @Nullable ThingUID getZoneUID(ThingTypeUID thingTypeUID, @Nullable ThingUID thingUID,
            Configuration configuration, @Nullable ThingUID bridgeUID) {
        if (thingUID == null && bridgeUID != null) {
            String zoneId = "zone" + (String) configuration.get(ZoneConfiguration.ZONE_NUMBER);
            return new ThingUID(thingTypeUID, zoneId, bridgeUID.getId());
        }
        return thingUID;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (ClearOneBindingConstants.THING_TYPE_STACK.equals(thingTypeUID)) {
            ClearOneStackHandler handler = new ClearOneStackHandler((Bridge) thing, serialPortManagerSupplier);
            registerStackDiscoveryService(handler);
            return handler;
        } else if (ClearOneBindingConstants.THING_TYPE_UNIT.equals(thingTypeUID)) {
            ClearOneUnitHandler handler = new ClearOneUnitHandler((Bridge) thing, stateDescriptionProvider,
                    optionDescriptionProvider);
            registerUnitDiscoveryService(handler);
            return handler;
            // return new ClearOneUnitHandler(thing);
        } else if (ClearOneBindingConstants.THING_TYPE_ZONE.equals(thingTypeUID)) {
            return new ClearOneZoneHandler(thing, stateDescriptionProvider, optionDescriptionProvider);
        }

        return null;
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        ServiceRegistration<?> discoveryServiceRegistration = discoveryServiceRegistrations
                .get(thingHandler.getThing().getUID());

        if (ClearOneBindingConstants.THING_TYPE_STACK.equals(thingHandler.getThing().getThingTypeUID())) {
            ClearOneStackDiscoveryService discoveryService = (ClearOneStackDiscoveryService) bundleContext
                    .getService(discoveryServiceRegistration.getReference());
            if (discoveryService != null) {
                discoveryService.deactivate();
            }
            discoveryServiceRegistration.unregister();
            discoveryServiceRegistration = null;
            discoveryServiceRegistrations.remove(thingHandler.getThing().getUID());
        } else if (ClearOneBindingConstants.THING_TYPE_UNIT.equals(thingHandler.getThing().getThingTypeUID())) {
            ClearOneUnitDiscoveryService discoveryService = (ClearOneUnitDiscoveryService) bundleContext
                    .getService(discoveryServiceRegistration.getReference());
            if (discoveryService != null) {
                discoveryService.deactivate();
            }
            discoveryServiceRegistration.unregister();
            discoveryServiceRegistration = null;
            discoveryServiceRegistrations.remove(thingHandler.getThing().getUID());
        }
        super.removeHandler(thingHandler);
    }

    /**
     * Register the Thing Discovery Service for a bridge.
     *
     * @param bridgeHandler
     */
    private void registerStackDiscoveryService(ClearOneStackHandler bridgeHandler) {
        ClearOneStackDiscoveryService discoveryService = new ClearOneStackDiscoveryService(bridgeHandler);
        discoveryService.activate();

        ServiceRegistration<?> discoveryServiceRegistration = bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>());
        discoveryServiceRegistrations.put(bridgeHandler.getThing().getUID(), discoveryServiceRegistration);
    }

    /**
     * Register the Thing Discovery Service for a bridge.
     *
     * @param bridgeHandler
     */
    private void registerUnitDiscoveryService(ClearOneUnitHandler bridgeHandler) {
        ClearOneUnitDiscoveryService discoveryService = new ClearOneUnitDiscoveryService(bridgeHandler);
        discoveryService.activate();

        ServiceRegistration<?> discoveryServiceRegistration = bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<>());
        discoveryServiceRegistrations.put(bridgeHandler.getThing().getUID(), discoveryServiceRegistration);
    }

    @Reference
    protected void setSerialPortManager(SerialPortManager serialPortManager) {
        this.serialPortManagerSupplier = () -> serialPortManager;
    }

    protected void unsetSerialPortManager(SerialPortManager serialPortManager) {
        this.serialPortManagerSupplier = () -> null;
    }

    @Reference
    protected void setDynamicStateDescriptionProvider(
            ClearOneDynamicStateDescriptionProvider stateDescriptionProvider) {
        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    protected void unsetDynamicStateDescriptionProvider(
            ClearOneDynamicStateDescriptionProvider stateDescriptionProvider) {
        this.stateDescriptionProvider = null;
    }

    @Reference
    protected void setDynamicConfigOptionProvider(ClearOneDynamicConfigOptionProvider configOptionProvider) {
        this.optionDescriptionProvider = configOptionProvider;
    }

    protected void unsetDynamicConfigOptionProvider(ClearOneDynamicConfigOptionProvider configOptionProvider) {
        this.optionDescriptionProvider = null;
    }
}
