/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.modbus.sunspec.internal;

import static org.openhab.binding.modbus.sunspec.internal.SunSpecBindingConstants.*;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.modbus.sunspec.internal.handler.InverterHandler;
import org.openhab.binding.modbus.sunspec.internal.handler.MeterHandler;
import org.openhab.io.transport.modbus.ModbusManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SunSpecHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Nagy Attila Gabor - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.sunspec", configurationPolicy = ConfigurationPolicy.OPTIONAL)
public class SunSpecHandlerFactory extends BaseThingHandlerFactory {

    /**
     * Logger instance
     */
    @NonNull
    private final Logger logger = LoggerFactory.getLogger(SunSpecHandlerFactory.class);

    /**
     * Reference to the modbus manager
     */
    private ModbusManager manager;

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();
    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_INVERTER_SINGLE_PHASE);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_INVERTER_SPLIT_PHASE);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_INVERTER_THREE_PHASE);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_METER_SINGLE_PHASE);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_METER_SPLIT_PHASE);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_METER_WYE_PHASE);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_METER_DELTA_PHASE);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (manager == null) {
            logger.debug("Modbus manager not found, can't continue");
            return null;
        }
        if (thingTypeUID.equals(THING_TYPE_INVERTER_SINGLE_PHASE)
                || thingTypeUID.equals(THING_TYPE_INVERTER_SPLIT_PHASE)
                || thingTypeUID.equals(THING_TYPE_INVERTER_THREE_PHASE)) {
            return new InverterHandler(thing, () -> manager);
        } else if (thingTypeUID.equals(THING_TYPE_METER_SINGLE_PHASE)
                || thingTypeUID.equals(THING_TYPE_METER_SPLIT_PHASE) || thingTypeUID.equals(THING_TYPE_METER_WYE_PHASE)
                || thingTypeUID.equals(THING_TYPE_METER_DELTA_PHASE)) {
            return new MeterHandler(thing, () -> manager);
        }

        return null;
    }

    /**
     * Setter to accept the ModbusManager
     *
     * @param manager the modbus manager from org.openhab.io.transport.modbus package
     */
    @Reference(service = ModbusManager.class, cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "unsetManager")
    public void setManager(ModbusManager manager) {
        logger.debug("Setting manager: {}", manager);
        this.manager = manager;
    }

    /**
     * Remove the modbus manager
     *
     * @param manager the modbus manager from org.openhab.io.transport.modbus package
     */
    public void unsetManager(ModbusManager manager) {
        this.manager = null;
    }
}
