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
package org.openhab.binding.plclogo.internal;

import static org.openhab.binding.plclogo.internal.PLCLogoBindingConstants.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plclogo.internal.handler.PLCAnalogHandler;
import org.openhab.binding.plclogo.internal.handler.PLCBridgeHandler;
import org.openhab.binding.plclogo.internal.handler.PLCDateTimeHandler;
import org.openhab.binding.plclogo.internal.handler.PLCDigitalHandler;
import org.openhab.binding.plclogo.internal.handler.PLCMemoryHandler;
import org.openhab.binding.plclogo.internal.handler.PLCPulseHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link PLCLogoHandlerFactory} is responsible for creating things and
 * thing handlers supported by PLCLogo binding.
 *
 * @author Alexander Falkenstern - Initial contribution
 */
@NonNullByDefault
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.plclogo")
public class PLCLogoHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS;
    static {
        Set<ThingTypeUID> buffer = new HashSet<>();
        buffer.add(THING_TYPE_DEVICE);
        buffer.add(THING_TYPE_MEMORY);
        buffer.add(THING_TYPE_ANALOG);
        buffer.add(THING_TYPE_DIGITAL);
        buffer.add(THING_TYPE_DATETIME);
        buffer.add(THING_TYPE_PULSE);
        SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(buffer);
    }

    /**
     * Constructor.
     */
    public PLCLogoHandlerFactory() {
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        if (THING_TYPE_DEVICE.equals(thing.getThingTypeUID()) && (thing instanceof Bridge bridge)) {
            return new PLCBridgeHandler(bridge);
        } else if (THING_TYPE_ANALOG.equals(thing.getThingTypeUID())) {
            return new PLCAnalogHandler(thing);
        } else if (THING_TYPE_DIGITAL.equals(thing.getThingTypeUID())) {
            return new PLCDigitalHandler(thing);
        } else if (THING_TYPE_DATETIME.equals(thing.getThingTypeUID())) {
            return new PLCDateTimeHandler(thing);
        } else if (THING_TYPE_MEMORY.equals(thing.getThingTypeUID())) {
            return new PLCMemoryHandler(thing);
        } else if (THING_TYPE_PULSE.equals(thing.getThingTypeUID())) {
            return new PLCPulseHandler(thing);
        }

        return null;
    }
}
