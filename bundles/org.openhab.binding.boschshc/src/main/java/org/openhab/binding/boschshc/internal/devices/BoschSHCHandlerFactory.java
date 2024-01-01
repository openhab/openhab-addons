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
package org.openhab.binding.boschshc.internal.devices;

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.*;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.bridge.BridgeHandler;
import org.openhab.binding.boschshc.internal.devices.camera.CameraHandler;
import org.openhab.binding.boschshc.internal.devices.climatecontrol.ClimateControlHandler;
import org.openhab.binding.boschshc.internal.devices.intrusion.IntrusionDetectionHandler;
import org.openhab.binding.boschshc.internal.devices.lightcontrol.LightControlHandler;
import org.openhab.binding.boschshc.internal.devices.motiondetector.MotionDetectorHandler;
import org.openhab.binding.boschshc.internal.devices.plug.PlugHandler;
import org.openhab.binding.boschshc.internal.devices.shuttercontrol.ShutterControlHandler;
import org.openhab.binding.boschshc.internal.devices.smartbulb.SmartBulbHandler;
import org.openhab.binding.boschshc.internal.devices.smokedetector.SmokeDetectorHandler;
import org.openhab.binding.boschshc.internal.devices.thermostat.ThermostatHandler;
import org.openhab.binding.boschshc.internal.devices.twinguard.TwinguardHandler;
import org.openhab.binding.boschshc.internal.devices.userdefinedstate.UserStateHandler;
import org.openhab.binding.boschshc.internal.devices.wallthermostat.WallThermostatHandler;
import org.openhab.binding.boschshc.internal.devices.windowcontact.WindowContact2Handler;
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
 * @author Christian Oeing - Added WallThermostatHandler
 * @author David Pace - Added cameras, intrusion detection system and smart plugs
 * @author Christian Oeing - Added smoke detector
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
            new ThingTypeHandlerMapping(THING_TYPE_SHC, thing -> new BridgeHandler((Bridge) thing)),
            new ThingTypeHandlerMapping(THING_TYPE_INWALL_SWITCH, LightControlHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_TWINGUARD, TwinguardHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_WINDOW_CONTACT, WindowContactHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_WINDOW_CONTACT_2, WindowContact2Handler::new),
            new ThingTypeHandlerMapping(THING_TYPE_MOTION_DETECTOR, MotionDetectorHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_SHUTTER_CONTROL, ShutterControlHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_THERMOSTAT, ThermostatHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_CLIMATE_CONTROL, ClimateControlHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_WALL_THERMOSTAT, WallThermostatHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_CAMERA_360, CameraHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_CAMERA_EYES, CameraHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_INTRUSION_DETECTION_SYSTEM, IntrusionDetectionHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_SMART_PLUG_COMPACT, PlugHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_SMART_BULB, SmartBulbHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_SMOKE_DETECTOR, SmokeDetectorHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_USER_DEFINED_STATE, UserStateHandler::new));

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
