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
package org.openhab.binding.modbus.sunspec.internal;

import static org.openhab.binding.modbus.sunspec.internal.SunSpecConstants.THING_TYPE_INVERTER_SINGLE_PHASE;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.modbus.sunspec.internal.handler.InverterHandler;
import org.openhab.io.transport.modbus.ModbusManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SunSpecHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Nagy Attila Gábor - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.sunspec", service = ThingHandlerFactory.class)
public class SunSpecHandlerFactory extends BaseThingHandlerFactory {

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(SunSpecHandlerFactory.class);

    /**
     * Reference to the modbus manager
     */
    private Optional<ModbusManager> manager = Optional.empty();

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();
    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_INVERTER_SINGLE_PHASE);
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (manager.isPresent()) {
            if (thingTypeUID.equals(THING_TYPE_INVERTER_SINGLE_PHASE)) {
                return new InverterHandler(thing, () -> manager.get());
            }
        } else {
            logger.debug("Modbus manager not found, can't continue");
            return null;
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
        this.manager = Optional.of(manager);
    }

    /**
     * Remove the modbus manager
     *
     * @param manager the modbus manager from org.openhab.io.transport.modbus package
     */
    public void unsetManager(ModbusManager manager) {
        this.manager = Optional.empty();
    }
}
