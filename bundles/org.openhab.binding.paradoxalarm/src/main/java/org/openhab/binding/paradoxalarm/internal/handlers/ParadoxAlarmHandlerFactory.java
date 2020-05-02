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
package org.openhab.binding.paradoxalarm.internal.handlers;

import static org.openhab.binding.paradoxalarm.internal.handlers.ParadoxAlarmBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.paradoxalarm.internal.discovery.ParadoxDiscoveryService;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ParadoxAlarmHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Konstantin Polihronov - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.paradoxalarm", service = ThingHandlerFactory.class)
public class ParadoxAlarmHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(ParadoxAlarmHandlerFactory.class);

    private final Map<ThingUID, @Nullable ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (COMMUNICATOR_THING_TYPE_UID.equals(thingTypeUID)) {
            logger.debug("createHandler(): ThingHandler created for {}", thingTypeUID);

            ParadoxIP150BridgeHandler paradoxIP150BridgeHandler = new ParadoxIP150BridgeHandler((Bridge) thing);
            registerDiscoveryService(paradoxIP150BridgeHandler);

            return paradoxIP150BridgeHandler;
        } else if (PANEL_THING_TYPE_UID.equals(thingTypeUID)) {
            logger.debug("createHandler(): ThingHandler created for {}", thingTypeUID);
            return new ParadoxPanelHandler(thing);
        } else if (PARTITION_THING_TYPE_UID.equals(thingTypeUID)) {
            logger.debug("createHandler(): ThingHandler created for {}", thingTypeUID);
            return new ParadoxPartitionHandler(thing);
        } else if (ZONE_THING_TYPE_UID.equals(thingTypeUID)) {
            logger.debug("createHandler(): ThingHandler created for {}", thingTypeUID);
            return new ParadoxZoneHandler(thing);
        } else {
            logger.warn("Handler implementation not found for Thing: {}", thing.getLabel());
        }
        return null;
    }

    private void registerDiscoveryService(ParadoxIP150BridgeHandler paradoxIP150BridgeHandler) {
        ParadoxDiscoveryService discoveryService = new ParadoxDiscoveryService(paradoxIP150BridgeHandler);
        ServiceRegistration<?> serviceRegistration = bundleContext.registerService(DiscoveryService.class.getName(),
                discoveryService, new Hashtable<>());
        this.discoveryServiceRegs.put(paradoxIP150BridgeHandler.getThing().getUID(), serviceRegistration);
    }

    @Override
    protected void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof ParadoxIP150BridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                serviceReg.unregister();
            }
        }
    }
}
