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
package org.openhab.binding.boschshc.internal.devices;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_CLIMATE_CONTROL;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_INWALL_SWITCH;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_MOTION_DETECTOR;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_SHC;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_SHUTTER_CONTROL;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_THERMOSTAT;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_TWINGUARD;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_WINDOW_CONTACT;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.bridge.BoschSHCBridgeHandler;
import org.openhab.binding.boschshc.internal.devices.climatecontrol.ClimateControlHandler;
import org.openhab.binding.boschshc.internal.devices.inwallswitch.BoschInWallSwitchHandler;
import org.openhab.binding.boschshc.internal.devices.motiondetector.MotionDetectorHandler;
import org.openhab.binding.boschshc.internal.devices.shuttercontrol.ShutterControlHandler;
import org.openhab.binding.boschshc.internal.devices.thermostat.ThermostatHandler;
import org.openhab.binding.boschshc.internal.devices.twinguard.BoschTwinguardHandler;
import org.openhab.binding.boschshc.internal.devices.windowcontact.WindowContactHandler;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link BoschSHCHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Stefan Kästle - Initial contribution
 * @author Christian Oeing - Added Shutter Control and ThermostatHandler; refactored handler mapping
 */
@NonNullByDefault
@Component(configurationPid = "binding.boschshc", service = ThingHandlerFactory.class)
public class BoschSHCHandlerFactory extends BaseThingHandlerFactory {

    private static class ThingTypeHandlerMapping {
        public ThingTypeUID thingTypeUID;
        public Function<Thing, BaseThingHandler> handlerSupplier;

        public ThingTypeHandlerMapping(ThingTypeUID thingTypeUID, Function<Thing, BaseThingHandler> handlerSupplier) {
            this.thingTypeUID = thingTypeUID;
            this.handlerSupplier = handlerSupplier;
        }
    }

    private static final Collection<ThingTypeHandlerMapping> SUPPORTED_THING_TYPES = List.of(
            new ThingTypeHandlerMapping(THING_TYPE_SHC, thing -> new BoschSHCBridgeHandler((Bridge) thing)),
            new ThingTypeHandlerMapping(THING_TYPE_INWALL_SWITCH, BoschInWallSwitchHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_TWINGUARD, BoschTwinguardHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_WINDOW_CONTACT, WindowContactHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_MOTION_DETECTOR, MotionDetectorHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_SHUTTER_CONTROL, ShutterControlHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_THERMOSTAT, ThermostatHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_CLIMATE_CONTROL, ClimateControlHandler::new));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES.stream().anyMatch(mapping -> mapping.thingTypeUID.equals(thingTypeUID));
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        // Search for mapping for thing type and return handler for it if found. Otherwise return null.
        return SUPPORTED_THING_TYPES.stream().filter(mapping -> mapping.thingTypeUID.equals(thingTypeUID)).findFirst()
                .<@Nullable BaseThingHandler> map(mapping -> mapping.handlerSupplier.apply(thing)).orElse(null);
    }
}
