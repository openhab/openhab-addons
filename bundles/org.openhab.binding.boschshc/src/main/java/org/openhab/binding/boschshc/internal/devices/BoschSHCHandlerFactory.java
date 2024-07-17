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

import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_CAMERA_360;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_CAMERA_EYES;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_CLIMATE_CONTROL;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_DIMMER;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_INTRUSION_DETECTION_SYSTEM;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_INWALL_SWITCH;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_LIGHT_CONTROL_2;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_MOTION_DETECTOR;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_SHC;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_SHUTTER_CONTROL;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_SHUTTER_CONTROL_2;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_SMART_BULB;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_SMART_PLUG_COMPACT;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_SMOKE_DETECTOR;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_SMOKE_DETECTOR_2;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_THERMOSTAT;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_TWINGUARD;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_UNIVERSAL_SWITCH;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_UNIVERSAL_SWITCH_2;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_USER_DEFINED_STATE;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_WALL_THERMOSTAT;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_WATER_DETECTOR;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_WINDOW_CONTACT;
import static org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants.THING_TYPE_WINDOW_CONTACT_2;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.boschshc.internal.devices.bridge.BridgeHandler;
import org.openhab.binding.boschshc.internal.devices.camera.CameraHandler;
import org.openhab.binding.boschshc.internal.devices.climatecontrol.ClimateControlHandler;
import org.openhab.binding.boschshc.internal.devices.intrusion.IntrusionDetectionHandler;
import org.openhab.binding.boschshc.internal.devices.lightcontrol.DimmerHandler;
import org.openhab.binding.boschshc.internal.devices.lightcontrol.LightControl2Handler;
import org.openhab.binding.boschshc.internal.devices.lightcontrol.LightControlHandler;
import org.openhab.binding.boschshc.internal.devices.motiondetector.MotionDetectorHandler;
import org.openhab.binding.boschshc.internal.devices.plug.PlugHandler;
import org.openhab.binding.boschshc.internal.devices.shuttercontrol.ShutterControl2Handler;
import org.openhab.binding.boschshc.internal.devices.shuttercontrol.ShutterControlHandler;
import org.openhab.binding.boschshc.internal.devices.smartbulb.SmartBulbHandler;
import org.openhab.binding.boschshc.internal.devices.smokedetector.SmokeDetector2Handler;
import org.openhab.binding.boschshc.internal.devices.smokedetector.SmokeDetectorHandler;
import org.openhab.binding.boschshc.internal.devices.thermostat.ThermostatHandler;
import org.openhab.binding.boschshc.internal.devices.twinguard.TwinguardHandler;
import org.openhab.binding.boschshc.internal.devices.universalswitch.UniversalSwitch2Handler;
import org.openhab.binding.boschshc.internal.devices.universalswitch.UniversalSwitchHandler;
import org.openhab.binding.boschshc.internal.devices.userdefinedstate.UserStateHandler;
import org.openhab.binding.boschshc.internal.devices.wallthermostat.WallThermostatHandler;
import org.openhab.binding.boschshc.internal.devices.waterleakage.WaterLeakageSensorHandler;
import org.openhab.binding.boschshc.internal.devices.windowcontact.WindowContact2Handler;
import org.openhab.binding.boschshc.internal.devices.windowcontact.WindowContactHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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

    private TimeZoneProvider timeZoneProvider;

    @Activate
    public BoschSHCHandlerFactory(final @Reference TimeZoneProvider timeZoneProvider) {
        this.timeZoneProvider = timeZoneProvider;
    }

    private static class ThingTypeHandlerMapping {
        public ThingTypeUID thingTypeUID;
        public Function<Thing, BaseThingHandler> handlerSupplier;

        public ThingTypeHandlerMapping(ThingTypeUID thingTypeUID, Function<Thing, BaseThingHandler> handlerSupplier) {
            this.thingTypeUID = thingTypeUID;
            this.handlerSupplier = handlerSupplier;
        }
    }

    private final Collection<ThingTypeHandlerMapping> supportedThingTypes = List.of(
            new ThingTypeHandlerMapping(THING_TYPE_SHC, thing -> new BridgeHandler((Bridge) thing)),
            new ThingTypeHandlerMapping(THING_TYPE_INWALL_SWITCH, LightControlHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_TWINGUARD, TwinguardHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_WINDOW_CONTACT, WindowContactHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_WINDOW_CONTACT_2, WindowContact2Handler::new),
            new ThingTypeHandlerMapping(THING_TYPE_MOTION_DETECTOR, MotionDetectorHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_SHUTTER_CONTROL, ShutterControlHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_SHUTTER_CONTROL_2, ShutterControl2Handler::new),
            new ThingTypeHandlerMapping(THING_TYPE_THERMOSTAT, ThermostatHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_CLIMATE_CONTROL, ClimateControlHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_WALL_THERMOSTAT, WallThermostatHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_CAMERA_360, CameraHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_CAMERA_EYES, CameraHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_INTRUSION_DETECTION_SYSTEM, IntrusionDetectionHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_SMART_PLUG_COMPACT, PlugHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_SMART_BULB, SmartBulbHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_SMOKE_DETECTOR, SmokeDetectorHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_USER_DEFINED_STATE, UserStateHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_UNIVERSAL_SWITCH,
                    thing -> new UniversalSwitchHandler(thing, timeZoneProvider)),
            new ThingTypeHandlerMapping(THING_TYPE_UNIVERSAL_SWITCH_2,
                    thing -> new UniversalSwitch2Handler(thing, timeZoneProvider)),
            new ThingTypeHandlerMapping(THING_TYPE_SMOKE_DETECTOR_2, SmokeDetector2Handler::new),
            new ThingTypeHandlerMapping(THING_TYPE_LIGHT_CONTROL_2, LightControl2Handler::new),
            new ThingTypeHandlerMapping(THING_TYPE_DIMMER, DimmerHandler::new),
            new ThingTypeHandlerMapping(THING_TYPE_WATER_DETECTOR, WaterLeakageSensorHandler::new));

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return supportedThingTypes.stream().anyMatch(mapping -> mapping.thingTypeUID.equals(thingTypeUID));
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        // Search for mapping for thing type and return handler for it if found. Otherwise return null.
        return supportedThingTypes.stream().filter(mapping -> mapping.thingTypeUID.equals(thingTypeUID)).findFirst()
                .<@Nullable BaseThingHandler> map(mapping -> mapping.handlerSupplier.apply(thing)).orElse(null);
    }
}
