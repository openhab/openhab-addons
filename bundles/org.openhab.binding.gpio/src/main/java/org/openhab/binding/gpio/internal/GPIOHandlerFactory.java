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
package org.openhab.binding.gpio.internal;

import static org.openhab.binding.gpio.internal.GPIOBindingConstants.THING_TYPE_DIGITAL_INPUT;
import static org.openhab.binding.gpio.internal.GPIOBindingConstants.THING_TYPE_DIGITAL_OUTPUT;
import static org.openhab.binding.gpio.internal.GPIOBindingConstants.THING_TYPE_PIGPIO_REMOTE_BRIDGE;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gpio.internal.handler.GPIODigitalInputHandler;
import org.openhab.binding.gpio.internal.handler.GPIODigitalOutputHandler;
import org.openhab.binding.gpio.internal.handler.PigpioRemoteBridgeHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link gpioHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Nils Bauer - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.gpio", service = ThingHandlerFactory.class)
public class GPIOHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_DIGITAL_INPUT,
            THING_TYPE_DIGITAL_OUTPUT, THING_TYPE_PIGPIO_REMOTE_BRIDGE);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_DIGITAL_INPUT)) {
            return new GPIODigitalInputHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_DIGITAL_OUTPUT)) {
            return new GPIODigitalOutputHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_PIGPIO_REMOTE_BRIDGE)) {
            return new PigpioRemoteBridgeHandler((Bridge) thing);
        }

        return null;
    }
}
