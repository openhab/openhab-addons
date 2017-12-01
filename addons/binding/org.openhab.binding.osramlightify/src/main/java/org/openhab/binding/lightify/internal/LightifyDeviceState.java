/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.osramlightify.internal;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;

import static org.openhab.binding.osramlightify.LightifyBindingConstants.LIGHTIFY_DEVICE_TYPE_LIGHT_DIMMABLE;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.LIGHTIFY_DEVICE_TYPE_LIGHT_TUNABLE;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.LIGHTIFY_DEVICE_TYPE_LIGHT_SOFT_SWITCHABLE;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.LIGHTIFY_DEVICE_TYPE_LIGHT_RGBW;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.LIGHTIFY_DEVICE_TYPE_POWER;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.LIGHTIFY_DEVICE_TYPE_MOTION_SENSOR;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.LIGHTIFY_DEVICE_TYPE_SWITCH_2GANG;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.LIGHTIFY_DEVICE_TYPE_SWITCH_4GANG;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.handler.LightifyDeviceHandler;
import org.openhab.binding.osramlightify.handler.LightifyMotionSensorHandler;

import org.openhab.binding.osramlightify.internal.messages.LightifyMessage;
import org.openhab.binding.osramlightify.internal.messages.LightifyGetDeviceInfoMessage;

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

    private ScheduledFuture<?>[] transitionEndJob = new ScheduledFuture<?>[2];
    private Long[] transitionEndJobNanos = new Long[2];

    /* The state as given in a {@link LightifyListPairedDevicesMessage} response. */
    public int deviceType;
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

    public void setTransitionTimeNanos(LightifyDeviceHandler deviceHandler, int index, long transitionEndNanos, long transitionTimeNanos) {
        synchronized (this) {
            // If we had a transition already and we are doing an immediate (non-transition) change
            // we log the transition as complete.
            if (this.transitionEndJob[index] != null) {
                transitionEndJob[index].cancel(true);
                transitionEndJob[index] = null;

                if (transitionTimeNanos <= 0) {
                    logger.debug("{}: {} transition cancelled", deviceHandler.getDeviceAddress(), (index == 0 ? "luminance" : "colour"));
                }
            }

            if (transitionTimeNanos > 0) {
                transitionEndJobNanos[index] = transitionEndNanos;

                int otherIndex = (index == 0 ? 1 : 0);
                if (transitionEndJob[otherIndex] == null || transitionEndJobNanos[otherIndex] != transitionEndNanos) {
                    logger.debug("{}: schedule {} complete at {}", deviceHandler.getDeviceAddress(), (index == 0 ? "luminance" : "colour"), transitionEndNanos);

                    transitionEndJob[index] = deviceHandler.getScheduler().schedule(
                        () -> {
                            synchronized (this) {

                                transitionEndJob[index] = null;
                                deviceHandler.sendMessage(new LightifyGetDeviceInfoMessage(deviceHandler));
                            }
                         },
                        transitionTimeNanos, TimeUnit.NANOSECONDS);
                } else {
                    logger.debug("{}: {} completes with other transition at {}", deviceHandler.getDeviceAddress(), (index == 0 ? "luminance" : "colour"), transitionEndNanos);
                }
            }
        }
    }

    public boolean received(LightifyBridgeHandler bridgeHandler, Thing thing, long now, boolean knownCurrent) {
        boolean changes = false;

        LightifyDeviceHandler deviceHandler = (LightifyDeviceHandler) thing.getHandler();

        LightifyDeviceState state = deviceHandler.getLightifyDeviceState();

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
        // changes to 255,255,255 too. (But only ~30s after the fact. Presumably the
        // gateway gets state before the device finalizes it so the gateway only
        // sees it after the next (internally) scheduled poll, which appears to be
        // every 30s with firmware 1.1.3.53.)
        if (temperatureDelta != 0 || (r == 255 && g == 255 && b == 255)) {
            whiteMode = true;
        } else if (rDelta != 0 || gDelta != 0 || bDelta != 0 || aDelta != 0) {
            whiteMode = false;
        } else {
            whiteMode = state.whiteMode;
        }

        // If power changes then luminance, at least the the luminance we advertise
        // to linked items // anyway, must also change.
        if (luminanceDelta == 0) {
            luminanceDelta = powerDelta;
        }

        // If power is off luminance changes are hidden.
        if (power == 0) {
            luminanceDelta = 0;
        }

        ThingStatus thingStatus = thing.getStatus();

        if (thingStatus != ThingStatus.ONLINE) {
            if (reachable == 2 && timeSinceSeen == 0) {
                // If we have state from before the device went offline we'll stay with it
                // otherwise the state is whatever the device is telling us.

                // setOnline may want to do some probes before we actually go online.
                if (deviceHandler.setOnline(bridgeHandler)) {
                    logger.debug("{}: ONLINE {}", deviceHandler.getDeviceAddress(), this);
                    state.saved = false;
                    changes = true;
                } else {
                    // The handler wants to do some probes before going online. If we don't
                    // already have a saved state we save this (the power up state) so that
                    // we have something to restore once the probes are done and tell the
                    // linked items what we have done.
                    if (!state.saved) {
                        logger.debug("{}: INITIAL {}", deviceHandler.getDeviceAddress(), this);
                        state.copyFrom(this);
                        state.saved = true;
                        state.fullRefresh(bridgeHandler, deviceHandler);
                    }
                }
            } else if (reachable != state.reachable) {
                logger.trace("{}: waiting {}", deviceHandler.getDeviceAddress(), this);
            }

        // reachable can become 0 in a LIST_PAIRED state report if the device fails to respond
        // to the gateway's periodic poll. This is fairly easy to trigger by spamming a device
        // which has poor connectivity with GET_DEVICE_INFO requests which we do when collecting
        // transition stats. This is not an immediate problem so we'll wait and see.
        } else if (timeSinceSeen > 1 || (reachable != 2 && state.transitionEndJob[0] == null && state.transitionEndJob[1] == null)) {
            // Always save state if we go unreachable with a time since seen of zero. This is
            // either a reboot (firmware upgrade?) or a fast (<5mins) power off/on. We will
            // restore the device state when it comes back.
            if (reachable == 0 && timeSinceSeen == 0) {
                // If we thought we were in a transition we aren't any more and the state we
                // save may be up to 30s out of date since the gateway does not actively track
                // state as it changes during transitions - it simply appears to poll devices
                // periodically.
                synchronized (this) {
                    for (int i = 0; i < state.transitionEndJob.length; i++) {
                        if (state.transitionEndJob[i] != null) {
                            state.transitionEndJob[i].cancel(true);
                            state.transitionEndJob[i] = null;
                        }
                    }
                }

                logger.debug("{}: SAVED {}", deviceHandler.getDeviceAddress(), state);
                state.saved = true;
            }

            logger.debug("{}: OFFLINE {}", deviceHandler.getDeviceAddress(), this);
            deviceHandler.setStatus(ThingStatus.OFFLINE);

            changes = true;

        } else {
            synchronized (this) {
                // The gateway polls devices every 30s so if we are 30s beyond the expected end
                // of a transition we have post-transition state but lost the GET_DEVICE_INFO
                // for some reason.
                for (int i = 0; i < state.transitionEndJob.length; i++) {
                    if (state.transitionEndJobNanos[i] != null && ((knownCurrent && now - state.transitionEndJobNanos[i] >= 0) || now - state.transitionEndJobNanos[i] > 30000000000L)) {
                        logger.debug("{}: received final state for {} transition", deviceHandler.getDeviceAddress(), (i == 0 ? "luminance" : "colour"));
                        state.transitionEndJobNanos[i] = null;
                    }
                }

                // State changes during transitions (that we know about - i.e. initiated ourselves)
                // are ignored. This is because when we initiate a transition we see an immediate
                // state change showing the device moving towards the desired state and then
                // nothing until up to 30s after the transition has completed when we see the final
                // state. For transitions we know about we ignore state changes during the transition
                // and use a GET_DEVICE_INFO to ask the gateway to do an immediate state update after
                // the transition completes. We can't do anything for other transitions though so
                // they will generate updates every 30s with the update to the final state being
                // up to 30s late :-(.
                // N.B. 30s is the poll time for the gateway with firmware 1.1.3.53. It may change.

                if (state.transitionEndJobNanos[0] != null) {
                    power = state.power;
                    powerDelta = 0;
                    luminance = state.luminance;
                    luminanceDelta = 0;
                }
                if (state.transitionEndJobNanos[1] != null) {
                    r = state.r;
                    g = state.g;
                    b = state.b;
                    a = state.a;
                    temperature = state.temperature;
                    rDelta = 0;
                    gDelta = 0;
                    bDelta = 0;
                    aDelta = 0;
                    temperatureDelta = 0;
                }
            }

            state.copyFrom(this);

            if (powerDelta != 0 || luminanceDelta != 0 || temperatureDelta != 0 || rDelta != 0 || gDelta != 0 || bDelta != 0 || aDelta != 0) {
                logger.debug("{}: CHANGED {}", deviceHandler.getDeviceAddress(), state);

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

                // It might seem to make more sense to send an off-to-on change before everything
                // else and an on-to-off change after everything else and it might make more sense
                // for rules to see that order of events. However switches can be linked to the
                // luminance and colour channels so OnOff types need to be sent on them to update
                // the switches. But... openHAB maps OnOff to 0 and 100 on dimmer items. We don't
                // want the items bouncing around like that so we do power first and force
                // luminance and colour updates if a power update was done.
                if (powerDelta != 0) {
                    deviceHandler.changedPower(getPower());
                }

                if (deviceHandler instanceof LightifyMotionSensorHandler) {
                    // Motion sensors are quite unlike other devices.
                    LightifyMotionSensorHandler motionSensorHandler = (LightifyMotionSensorHandler) deviceHandler;

                    if (luminanceDelta != 0) {
                        motionSensorHandler.changedBattery(getBattery());
                    }

                    if (rDelta != 0) {
                        motionSensorHandler.changedEnabled(getEnabled());
                    }

                    if (gDelta != 0) {
                        motionSensorHandler.changedTriggered(getTriggered());
                    }

                } else {
                    if (temperatureDelta != 0) {
                        deviceHandler.changedTemperature(bridgeHandler, getTemperature());
                    }

                    if (luminanceDelta != 0) {
                        deviceHandler.changedLuminance(getLuminance());
                    }

                    if (luminanceDelta != 0 || rDelta != 0 || gDelta != 0 || bDelta != 0 || aDelta != 0) {
                        deviceHandler.changedColor(getColor());
                    }
                }

                changes = true;
            }
        }

        return changes;
    }

    private void fullRefresh(LightifyBridgeHandler bridgeHandler, LightifyDeviceHandler deviceHandler) {
        logger.debug("{}: refresh", deviceHandler.getThing().getUID());

        if (power != 0) {
            deviceHandler.changedPower(getPower());
        }

        if (deviceHandler instanceof LightifyMotionSensorHandler) {
            // Motion sensors are quite unlike other devices.
            LightifyMotionSensorHandler motionSensorHandler = (LightifyMotionSensorHandler) deviceHandler;

            motionSensorHandler.changedBattery(getBattery());
            motionSensorHandler.changedEnabled(getEnabled());
            motionSensorHandler.changedTriggered(getTriggered());
        } else {
            deviceHandler.changedTemperature(bridgeHandler, getTemperature());
            deviceHandler.changedColor(getColor());
            deviceHandler.changedLuminance(getLuminance());
        }

        if (power == 0) {
            deviceHandler.changedPower(getPower());
        }
    }

    private void copyFrom(LightifyDeviceState fromState) {
        deviceType = fromState.deviceType;
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
        return new PercentType(power == 0 ? 0 : luminance);
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

    public DecimalType getBattery() {
        return new DecimalType(luminance);
    }

    public OnOffType getEnabled() {
        return (r != 0 ? OnOffType.ON : OnOffType.OFF);
    }

    public OnOffType getTriggered() {
        return (g != 0 ? OnOffType.ON : OnOffType.OFF);
    }

    public boolean isSaved() {
        return saved;
    }

    public String toString() {
        StringBuilder result = new StringBuilder(" device type=" + deviceType
            + " reachable=" + reachable
            + " time since seen=" + timeSinceSeen * 5 + "mins"
            + " joining=" + joining
            + " power=" + (power != 0 ? "true" : "false"));

        switch (deviceType) {
            // Unknown device types simply display the state as if it was an RGBW light.
            // The labels are wrong, but the values contain everything we are told.
            //case LIGHTIFY_DEVICE_TYPE_SWITCH_2GANG:
            //case LIGHTIFY_DEVICE_TYPE_SWITCH_4GANG:
            default:

            // First section is light and power devices from most capable to least.
            // Note the fall throughs!
            case LIGHTIFY_DEVICE_TYPE_LIGHT_RGBW:
                result.append(" white mode=" + (whiteMode ? "true" : "false")
                    + " r=" + r
                    + " g=" + g
                    + " b=" + b
                    + " a=" + a);

            case LIGHTIFY_DEVICE_TYPE_LIGHT_TUNABLE:
                result.append(" temperature=" + (temperature & 0xffff));

            case LIGHTIFY_DEVICE_TYPE_LIGHT_DIMMABLE:
            case LIGHTIFY_DEVICE_TYPE_LIGHT_SOFT_SWITCHABLE:
                result.append(" luminance=" + (luminance & 0xff));

            case LIGHTIFY_DEVICE_TYPE_POWER:
                break;


            // Second section is motion sensors. The values need to be labelled differently.
            case LIGHTIFY_DEVICE_TYPE_MOTION_SENSOR:
                result.append(" battery=" + luminance + "%"
                    + " enabled=" + r
                    + " triggered=" + g
                    + " ["
                    + " b=" + b
                    + " a=" + a
                    + " temperature=" + temperature
                    + " ]"
                );
                break;
        }

        return result.toString();
    }
}
