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
    public int power;
    public int luminance;
    public int temperature;
    public int r;
    public int g;
    public int b;
    public int a;

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
        state.power = power;
        state.luminance = luminance;
        state.temperature = temperature;
        state.r = r;
        state.g = g;
        state.b = b;
        state.a = a;

        if (thing.getStatus() == ThingStatus.OFFLINE) {
            fullRefresh(bridgeHandler, thingHandler);
            thingHandler.setOnline();

        } else if (powerDelta != 0 || luminanceDelta != 0 || temperatureDelta != 0 || rDelta != 0 || gDelta != 0 || bDelta != 0 || aDelta != 0) {
            logger.debug("{}: {}", deviceAddress, state);

            // Let the thing's channels know about the new state.

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
        return " power=" + (power != 0 ? "true" : "false")
            + " luminance=" + (luminance & 0xff)
            + " temperature=" + (temperature & 0xffff)
            + " r=" + r
            + " g=" + g
            + " b=" + b
            + " a=" + a;
    }
}
