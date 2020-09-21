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

import java.util.Arrays;
import java.util.Collection;

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
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link BoschSHCHandlerFactory} is responsible for creating things and
 * thing handlers.
 *
 * @author Stefan Kästle - Initial contribution
 * @author Christian Oeing - added Shutter Control and ThermostatHandler
 */
@NonNullByDefault
@Component(configurationPid = "binding.boschshc", service = ThingHandlerFactory.class)
public class BoschSHCHandlerFactory extends BaseThingHandlerFactory {

    // List of all supported Bosch devices.
    public static final Collection<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Arrays.asList(THING_TYPE_SHC,
            THING_TYPE_INWALL_SWITCH, THING_TYPE_TWINGUARD, THING_TYPE_WINDOW_CONTACT, THING_TYPE_MOTION_DETECTOR,
            THING_TYPE_SHUTTER_CONTROL, THING_TYPE_THERMOSTAT, THING_TYPE_CLIMATE_CONTROL);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_TYPE_SHC.equals(thingTypeUID)) {
            return new BoschSHCBridgeHandler((Bridge) thing);
        }

        else if (THING_TYPE_INWALL_SWITCH.equals(thingTypeUID)) {
            return new BoschInWallSwitchHandler(thing);
        }

        else if (THING_TYPE_TWINGUARD.equals(thingTypeUID)) {
            return new BoschTwinguardHandler(thing);
        }

        else if (THING_TYPE_WINDOW_CONTACT.equals(thingTypeUID)) {
            return new WindowContactHandler(thing);
        }

        else if (THING_TYPE_MOTION_DETECTOR.equals(thingTypeUID)) {
            return new MotionDetectorHandler(thing);
        }

        else if (THING_TYPE_SHUTTER_CONTROL.equals(thingTypeUID)) {
            return new ShutterControlHandler(thing);
        }

        else if (THING_TYPE_THERMOSTAT.equals(thingTypeUID)) {
            return new ThermostatHandler(thing);
        }

        else if (THING_TYPE_CLIMATE_CONTROL.equals(thingTypeUID)) {
            return new ClimateControlHandler(thing);
        }

        return null;
    }
}
