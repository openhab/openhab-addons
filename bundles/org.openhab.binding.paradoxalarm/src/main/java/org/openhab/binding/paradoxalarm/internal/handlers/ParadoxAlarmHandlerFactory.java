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
package org.openhab.binding.paradoxalarm.internal.handlers;

import static org.openhab.binding.paradoxalarm.internal.handlers.ParadoxAlarmBindingConstants.*;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.paradoxalarm.internal.discovery.ParadoxDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
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

    private final Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        ThingUID thingUID = thing.getUID();
        if (COMMUNICATOR_THING_TYPE_UID.equals(thingTypeUID)) {
            logger.debug("createHandler(): ThingHandler created for {}", thingUID);

            ParadoxIP150BridgeHandler paradoxIP150BridgeHandler = new ParadoxIP150BridgeHandler((Bridge) thing);
            registerDiscoveryService(paradoxIP150BridgeHandler);

            return paradoxIP150BridgeHandler;
        } else if (PANEL_THING_TYPE_UID.equals(thingTypeUID)) {
            logger.debug("createHandler(): ThingHandler created for {}", thingUID);
            return new ParadoxPanelHandler(thing);
        } else if (PARTITION_THING_TYPE_UID.equals(thingTypeUID)) {
            logger.debug("createHandler(): ThingHandler created for {}", thingUID);
            return new ParadoxPartitionHandler(thing);
        } else if (ZONE_THING_TYPE_UID.equals(thingTypeUID)) {
            logger.debug("createHandler(): ThingHandler created for {}", thingUID);
            return new ParadoxZoneHandler(thing);
        } else {
            logger.warn("Handler implementation not found for Thing: {}", thingUID);
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
