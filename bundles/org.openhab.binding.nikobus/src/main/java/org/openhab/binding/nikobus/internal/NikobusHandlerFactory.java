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
package org.openhab.binding.nikobus.internal;

import static org.openhab.binding.nikobus.internal.NikobusBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikobus.internal.handler.NikobusDimmerModuleHandler;
import org.openhab.binding.nikobus.internal.handler.NikobusPcLinkHandler;
import org.openhab.binding.nikobus.internal.handler.NikobusPushButtonHandler;
import org.openhab.binding.nikobus.internal.handler.NikobusRollershutterModuleHandler;
import org.openhab.binding.nikobus.internal.handler.NikobusSwitchModuleHandler;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link NikobusHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.nikobus", service = ThingHandlerFactory.class)
public class NikobusHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(BRIDGE_TYPE_PCLINK, THING_TYPE_PUSH_BUTTON, THING_TYPE_SWITCH_MODULE,
                    THING_TYPE_DIMMER_MODULE, THING_TYPE_ROLLERSHUTTER_MODULE).collect(Collectors.toSet()));

    private @NonNullByDefault({}) SerialPortManager serialPortManager;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (BRIDGE_TYPE_PCLINK.equals(thingTypeUID)) {
            return new NikobusPcLinkHandler((Bridge) thing, serialPortManager);
        }

        if (THING_TYPE_PUSH_BUTTON.equals(thingTypeUID)) {
            return new NikobusPushButtonHandler(thing);
        }

        if (THING_TYPE_SWITCH_MODULE.equals(thingTypeUID)) {
            return new NikobusSwitchModuleHandler(thing);
        }

        if (THING_TYPE_DIMMER_MODULE.equals(thingTypeUID)) {
            return new NikobusDimmerModuleHandler(thing);
        }

        if (THING_TYPE_ROLLERSHUTTER_MODULE.equals(thingTypeUID)) {
            return new NikobusRollershutterModuleHandler(thing);
        }

        return null;
    }

    @Reference
    protected void setSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = serialPortManager;
    }

    protected void unsetSerialPortManager(final SerialPortManager serialPortManager) {
        this.serialPortManager = null;
    }
}
