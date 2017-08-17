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
    public boolean whiteMode = true;
    private boolean saved = false;

    public boolean received(LightifyBridgeHandler bridgeHandler, Thing thing, String deviceAddress) {
        boolean changes = false;

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

        // There is no way to tell whether we are in white or colour mode. We just
        // have to track it ourselves as best we can. It only changes if temperature
        // or colour change too. Under some circumstances (power on to white?) RGB
        // changes to 255,255,255 too (but only 20-30s after power on :-( )
        if (temperatureDelta != 0 || (r == 255 && g == 255 && b == 255)) {
            whiteMode = true;
        } else if (rDelta != 0 || gDelta != 0 || bDelta != 0 || aDelta != 0) {
            whiteMode = false;
        } else {
            whiteMode = state.whiteMode;
        }

        ThingStatus thingStatus = thing.getStatus();

        if (thingStatus != ThingStatus.ONLINE) {
            if (reachable == 2 && timeSinceSeen == 0) {
                // If we have state from before the device went offline we'll stay with it
                // otherwise the state is whatever the device is telling us.

                // setOnline may want to do some probes before we actually go online.
                if (thingHandler.setOnline(bridgeHandler)) {
                    logger.debug("{}: ONLINE {}", deviceAddress, this);
                    state.saved = false;
                    changes = true;
                } else {
                    // The handler wants to do some probes before going online. If we don't
                    // already have a saved state we save this (the power up state) so that
                    // we have something to restore once the probes are done and tell the
                    // linked items what we have done.
                    if (!state.saved) {
                        logger.debug("{}: INITIAL {}", deviceAddress, this);
                        state.copyFrom(this);
                        state.saved = true;
                        state.fullRefresh(bridgeHandler, thingHandler);
                    }
                }
            } else if (reachable != state.reachable) {
                logger.trace("{}: waiting {}", deviceAddress, this);
            }

        } else if (reachable != 2 || timeSinceSeen > 1) {
            // Always save state if we go unreachable with a time since seen of zero. This is
            // either a reboot (firmware upgrade?) or a fast (<5mins) power off/on. We will
            // restore the device state when it comes back.
            if (reachable == 0 && timeSinceSeen == 0) {
                logger.debug("{}: SAVED {}", deviceAddress, state);
                state.saved = true;
            }

            logger.debug("{}: OFFLINE {}", deviceAddress, this);
            thingHandler.setStatus(ThingStatus.OFFLINE);

            changes = true;
        } else {
            state.copyFrom(this);

            if (powerDelta != 0 || luminanceDelta != 0 || temperatureDelta != 0 || rDelta != 0 || gDelta != 0 || bDelta != 0 || aDelta != 0) {
                logger.debug("{}: CHANGED {}", deviceAddress, state);

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

                changes = true;
            }
        }

        return changes;
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

    private void copyFrom(LightifyDeviceState fromState) {
        reachable = fromState.reachable;
        power = fromState.power;
        luminance = fromState.luminance;
        temperature = fromState.temperature;
        r = fromState.r;
        g = fromState.g;
        b = fromState.b;
        a = fromState.a;
        timeSinceSeen = fromState.timeSinceSeen;
        joining = fromState.joining;
        whiteMode = fromState.whiteMode;
    }

    public OnOffType getPower() {
        return (power != 0 ? OnOffType.ON : OnOffType.OFF);
    }

    public PercentType getLuminance() {
        return new PercentType(luminance);
    }

    public DecimalType getTemperature() {
        return new DecimalType(temperature);
    }

    public int[] getRGBA() {
        int[] rgba = { r, g, b, a };
        return rgba;
    }

    public HSBType getColor() {
        HSBType hsb = HSBType.fromRGB(r, g, b);
        return new HSBType(hsb.getHue(), hsb.getSaturation(), getLuminance());
    }

    public boolean getWhiteMode() {
        return whiteMode;
    }

    public boolean isSaved() {
        return saved;
    }

    public String toString() {
        return "reachable=" + reachable
            + " power=" + (power != 0 ? "true" : "false")
            + " luminance=" + (luminance & 0xff)
            + " temperature=" + (temperature & 0xffff)
            + " white mode=" + (whiteMode ? "true" : "false")
            + " r=" + r
            + " g=" + g
            + " b=" + b
            + " a=" + a
            + " time since seen=" + timeSinceSeen * 5 + "mins"
            + " joining=" + joining;
    }
}
