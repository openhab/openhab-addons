/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.openhab.binding.domintell.internal;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.i18n.UnitProvider;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.domintell.internal.discovery.DomintellDiscoveryService;
import org.openhab.binding.domintell.internal.handler.*;
import org.openhab.binding.domintell.internal.protocol.DomintellConnection;
import org.openhab.binding.domintell.internal.protocol.DomintellRegistry;
import org.openhab.binding.domintell.internal.protocol.model.group.ItemGroup;
import org.openhab.binding.domintell.internal.protocol.model.module.Module;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import static org.openhab.binding.domintell.internal.DomintellBindingConstants.*;

/**
 * The {@link DomintellHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Gabor Bicskei - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.domintell", configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class DomintellHandlerFactory extends BaseThingHandlerFactory {
    /**
     * Domintell logger. Uses a common category for all Domintell related logging.
     */
    private final Logger logger = LoggerFactory.getLogger(DomintellHandlerFactory.class);

    /**
     * Discovery service
     */
    private DomintellDiscoveryService discoveryService;

    /**
     * Domintell connection
     */
    private DomintellConnection connection;

    /**
     * Measurement unit
     */
    private boolean siUnit;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_MODULE_THING_TYPES_UIDS.contains(thingTypeUID) || BRIDGE_THING_TYPES_UIDS.contains(thingTypeUID) || SUPPORTED_GROUP_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    /**
     * By activating the component the bridge discovery service is also started.
     *
     * @param componentContext Component context.
     */
    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
        discoveryService.setBridgeUID(null);
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        if (BRIDGE_THING_TYPE.equals(thingTypeUID)) {
            DomintellBridgeHandler domintellBridgeHandler = new DomintellBridgeHandler((Bridge) thing, discoveryService);
            connection = domintellBridgeHandler.getConnection();
            return domintellBridgeHandler;
        } else {
            DomintellRegistry registry = connection.getRegistry();
            if (SUPPORTED_MODULE_THING_TYPES_UIDS.contains(thingTypeUID)) {
                DomintellModuleHandler handler = null;
                switch (thingTypeUID.getId()) {
                    case MODULE_BIR:
                    case MODULE_DMR:
                        handler = new DomintellReleyModuleHandler(thing, registry);
                        break;
                    case MODULE_IS4:
                    case MODULE_IS8:
                        handler = new DomintellContactModuleHandler(thing, registry);
                        break;
                    case MODULE_PBX:
                        handler = new DomintellPushButtonModuleHandler(thing, registry);
                        break;
                    case MODULE_TEX:
                        handler = new DomintellThermostatModuleHandler(thing, registry, siUnit);
                        break;
                    case MODULE_D10:
                    case MODULE_DIM:
                        handler = new DomintellDimmerModuleHandler(thing, registry);
                        break;
                }
                if (handler != null) {
                    discoveryService.removeModule(handler.getModule());
                }
                return handler;
            } else if (THING_TYPE_GROUP.equals(thingTypeUID)) {
                try {
                    DomintellItemGroupHandler handler = new DomintellVariableItemGroupHandler(thing, registry);
                    //remove from discovery
                    discoveryService.removeGroup(handler.getItemGroup());

                    return handler;
                } catch (IllegalArgumentException e) {
                    logger.debug("Unknown module type: {}", thingTypeUID.getId());
                }
            }
        }
        return null;
    }

    /**
     * Unregister module discovery service when the bridge handler is removed.
     *
     * @param thingHandler Bridge thing handler
     */
    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof DomintellBridgeHandler) {
            discoveryService.setBridgeUID(null);
        } else if (thingHandler instanceof DomintellItemGroupHandler) {
            ItemGroup itemGroup = ((DomintellItemGroupHandler) thingHandler).getItemGroup();
            itemGroup.setItemChangeListener(null);
            itemGroup.setStateChangeListener(null);
        } else if (thingHandler instanceof DomintellModuleHandler) {
            Module module= ((DomintellModuleHandler) thingHandler).getModule();
            module.setConfigChangeListener(null);
            module.setStateChangeListener(null);
        }
    }

    @Reference
    protected void setDomintellDiscoveryService(DomintellDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    protected void unsetDomintellDiscoveryService(DomintellDiscoveryService discoveryService) {
        this.discoveryService = null;
    }

    @Reference
    protected void setUnitProvider(UnitProvider unitProvider) {
        @Nullable Unit<Temperature> unit = unitProvider.getUnit(Temperature.class);
        siUnit = SIUnits.CELSIUS.equals(unit);
    }

    protected void unsetUnitProvider(UnitProvider unitProvider) {
    }
}
