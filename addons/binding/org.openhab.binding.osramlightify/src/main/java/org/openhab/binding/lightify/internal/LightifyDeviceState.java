/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.osramlightify.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.handler.LightifyDeviceHandler;

/**
 * Track the state of Lightify devices.
 *
 * This allows us to tell when changes have been made via external means such
 * as the Lightify app or cloud and generate the appropriate update events
 * within openHAB.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifyDeviceState {

    private static final Logger logger = LoggerFactory.getLogger(LightifyDeviceState.class);

    /* The state as given in a {@link LightifyListPairedDevicesMessage} response. */
    public int reachable;
    public int power;
    public int luminance;
    public int temperature;
    public int r;
    public int g;
    public int b;
    public int a;
    public int timeSinceSeen; // in units of 5mins
    public int joining;

    public synchronized boolean received(LightifyBridgeHandler bridgeHandler, Thing thing, String deviceAddress) {
        LightifyDeviceHandler thingHandler = (LightifyDeviceHandler) thing.getHandler();

        LightifyDeviceState state = thingHandler.getLightifyDeviceState();

        // Figure out what changes we have.
        int powerDelta = power - state.power;
        int luminanceDelta = luminance - state.luminance;
        int temperatureDelta = temperature - state.temperature;
        int rDelta = r - state.r;
        int gDelta = g - state.g;
        int bDelta = b - state.b;
        int aDelta = a - state.a;

        // Set the new values.
        state.reachable = reachable;
        state.power = power;
        state.luminance = luminance;
        state.temperature = temperature;
        state.r = r;
        state.g = g;
        state.b = b;
        state.a = a;
        state.timeSinceSeen = timeSinceSeen;
        state.joining = joining;

        ThingStatus thingStatus = thing.getStatus();

        if (state.reachable == 2 && state.timeSinceSeen == 0) {
            if (thingStatus != ThingStatus.ONLINE) {
                logger.debug("{}: ONLINE", deviceAddress);
                thingHandler.setOnline();
                powerDelta = 1; // causes a full refresh below
            }
        } else if (state.reachable == 0 || state.timeSinceSeen > 1) {
            if (thingStatus != ThingStatus.OFFLINE) {
                logger.debug("{}: OFFLINE", deviceAddress);
                thingHandler.setStatus(ThingStatus.OFFLINE);
            }
        }

        if (thingStatus == ThingStatus.ONLINE
        && (powerDelta != 0 || luminanceDelta != 0 || temperatureDelta != 0 || rDelta != 0 || gDelta != 0 || bDelta != 0 || aDelta != 0)) {
            logger.debug("{}: {}", deviceAddress, state);

            // Let the thing's channels know about the new state.

            // FIXME: this is problematical. The ZLL spec says that an implementation (i.e. device)
            // responds to a colour change command by setting the closest colour supported by the
            // hardware. If there are multiple things linked to the same item they may not agree
            // on what colour has been set and the item itself may behave somewhat erratically.
            // On the other hand, if we do not do this then we are unable to track colour changes
            // made to devices via external means such as by the Lightify app.
            // Similar problems exist with temperature. Just because one device can't do a given
            // temperature and clips to its limit does not mean other linked devices are clipped.
            // Nor can a temperature% be assumed to represent the same thing if the range varies.

            if (powerDelta > 0) {
                fullRefresh(bridgeHandler, thingHandler);
            } else {
                if (powerDelta != 0) {
                    thingHandler.changedPower(getPower());
                }

                if (temperatureDelta != 0) {
                    thingHandler.changedTemperature(bridgeHandler, getTemperature());
                }

                if (luminanceDelta != 0) {
                    thingHandler.changedLuminance(getLuminance());
                }

                if (luminanceDelta != 0 || rDelta != 0 || gDelta != 0 || bDelta != 0 || aDelta != 0) {
                    thingHandler.changedColor(getColor());
                }
            }

            return true;
        }

        return false;
    }

    private void fullRefresh(LightifyBridgeHandler bridgeHandler, LightifyDeviceHandler thingHandler) {
        logger.debug("{}: refresh", thingHandler.getThing().getUID());

        thingHandler.changedPower(getPower());
        thingHandler.changedTemperature(bridgeHandler, getTemperature());

        if (power != 0) {
            thingHandler.changedColor(getColor());
            thingHandler.changedLuminance(getLuminance());
        }
    }

    public synchronized OnOffType getPower() {
        return (power != 0 ? OnOffType.ON : OnOffType.OFF);
    }

    public synchronized PercentType getLuminance() {
        return new PercentType(luminance);
    }

    public synchronized DecimalType getTemperature() {
        return new DecimalType(temperature);
    }

    public synchronized HSBType getColor() {
        HSBType hsb = HSBType.fromRGB(r, g, b);
        return new HSBType(hsb.getHue(), hsb.getSaturation(), getLuminance());
    }

    public String toString() {
        return "reachable=" + reachable
            + " power=" + (power != 0 ? "true" : "false")
            + " luminance=" + (luminance & 0xff)
            + " temperature=" + (temperature & 0xffff)
            + " r=" + r
            + " g=" + g
            + " b=" + b
            + " a=" + a
            + " time since seen=" + timeSinceSeen * 5 + "mins"
            + " joining=" + joining;
    }
}
