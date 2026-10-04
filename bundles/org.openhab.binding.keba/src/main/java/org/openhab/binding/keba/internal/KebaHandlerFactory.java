/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.keba.internal;

import static org.openhab.binding.keba.internal.KebaBindingConstants.THING_TYPE_KECONTACT;
import static org.openhab.binding.keba.internal.KebaBindingConstants.THING_TYPE_KECONTACT_MODBUS;
import static org.openhab.binding.keba.internal.KebaBindingConstants.THING_TYPE_KECONTACT_REST;
import static org.openhab.binding.keba.internal.KebaBindingConstants.THING_TYPE_KECONTACT_UDP;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.keba.internal.handler.KeContactCombinedHandler;
import org.openhab.binding.keba.internal.handler.modbus.KeContactModbusHandler;
import org.openhab.binding.keba.internal.handler.rest.KeContactRestHandler;
import org.openhab.binding.keba.internal.handler.udp.KeContactHandler;
import org.openhab.binding.keba.internal.handler.udp.KeContactTransceiver;
import org.openhab.core.io.transport.modbus.ModbusManager;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link KebaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Karel Goderis - Initial contribution
 * @author Karel Goderis - Added Modbus TCP support
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.keba")
@NonNullByDefault
public class KebaHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();

    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_KECONTACT);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_KECONTACT_MODBUS);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_KECONTACT_REST);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_KECONTACT_UDP);
    }

    private final KeContactTransceiver transceiver = new KeContactTransceiver();

    private @NonNullByDefault({}) ModbusManager modbusManager;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_KECONTACT)) {
            return new KeContactCombinedHandler(thing, Objects.requireNonNull(modbusManager), transceiver);
        } else if (thingTypeUID.equals(THING_TYPE_KECONTACT_UDP)) {
            return new KeContactHandler(thing, transceiver);
        } else if (thingTypeUID.equals(THING_TYPE_KECONTACT_MODBUS)) {
            return new KeContactModbusHandler(thing, Objects.requireNonNull(modbusManager));
        } else if (thingTypeUID.equals(THING_TYPE_KECONTACT_REST)) {
            return new KeContactRestHandler(thing);
        }

        return null;
    }

    @Reference
    public void setModbusManager(ModbusManager modbusManager) {
        this.modbusManager = modbusManager;
    }

    public void unsetModbusManager(ModbusManager modbusManager) {
        this.modbusManager = null;
    }
}
