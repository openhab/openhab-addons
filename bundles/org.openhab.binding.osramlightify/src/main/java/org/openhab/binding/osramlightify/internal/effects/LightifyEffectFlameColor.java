/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.osramlightify.internal.effects;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import org.eclipse.smarthome.core.library.types.HSBType;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.handler.LightifyDeviceHandler;

import org.openhab.binding.osramlightify.internal.LightifyDeviceState;

import org.openhab.binding.osramlightify.internal.messages.LightifySetEffectMessage;

/**
 * Set a "fire" effect on a device.
 *
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
public final class LightifyEffectFlameColor extends LightifyEffectFlame {

    private static final byte[] genericData = new byte[] {
        // Header
        (byte) 0x01, (byte) 0xff, (byte) 0x00, (byte) 0xff, (byte) 0x00, (byte) 0x3c, (byte) 0x00, (byte) 0x00, (byte) 0x3c,

        // 15 steps, each step is 4 bytes
        (byte) 0x0c, (byte) 0xff, (byte) 0xff, (byte) 0x01,
        (byte) 0x0b, (byte) 0xff, (byte) 0xff, (byte) 0x02,
        (byte) 0x06, (byte) 0xff, (byte) 0xff, (byte) 0x02,
        (byte) 0x0b, (byte) 0xff, (byte) 0xff, (byte) 0x01,
        (byte) 0x0e, (byte) 0xff, (byte) 0xff, (byte) 0x01,
        (byte) 0x09, (byte) 0xff, (byte) 0xff, (byte) 0x02,
        (byte) 0x0c, (byte) 0xff, (byte) 0xff, (byte) 0x01,
        (byte) 0x0a, (byte) 0xff, (byte) 0xff, (byte) 0x01,
        (byte) 0x0b, (byte) 0xff, (byte) 0xff, (byte) 0x02,
        (byte) 0x05, (byte) 0xff, (byte) 0xff, (byte) 0x02,
        (byte) 0x03, (byte) 0xff, (byte) 0xff, (byte) 0x02,
        (byte) 0x04, (byte) 0xff, (byte) 0xff, (byte) 0x01,
        (byte) 0x04, (byte) 0xff, (byte) 0xff, (byte) 0x01,
        (byte) 0x0d, (byte) 0xff, (byte) 0xff, (byte) 0x01,
        (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0x8c
    };

    private @Nullable ScheduledFuture<?> tickJob = null;

    private boolean background = false;

    private double hueMedian = 10.0;
    private double hueBelow = 10.0;
    private double hueAbove = 20.0;

    // Changes in hue below this threshold are not considered visible.
    private double hueChangeMin = 5;

    private double luminanceMedian = 192.0;
    private double luminanceBelow = -64.0;
    private double luminanceAbove = 63.0;

    // Changes in luminance below this threshold are not considered visible.
    private double luminanceChangeMin = 5;

    private double lastHue;
    private double lastLuminance;

    public LightifyEffectFlameColor(LightifyBridgeHandler bridgeHandler, LightifyDeviceHandler deviceHandler, String name) {
        super(bridgeHandler, deviceHandler, name, true, genericData);

        LightifyDeviceState state = deviceHandler.getLightifyDeviceState();

        luminanceMedian = state.luminance;
        hueMedian = HSBType.fromRGB(state.r, state.g, state.b).getHue().doubleValue();
    }

    @Override
    protected void param(String key, String value) {
        switch (key) {
            case "hue.median":
                hueMedian = parseHue(value);
                break;

            case "hue.below":
                hueBelow = parseHue(value);
                break;

            case "hue.above":
                hueAbove = parseHue(value);
                break;

            case "hue.change.min":
                hueChangeMin = Double.parseDouble(value);
                break;

            case "luminance.median":
                luminanceMedian = parseAbsoluteOrPercentage(value);
                break;

            case "luminance.below":
                luminanceBelow = parseAbsoluteOrPercentage(value);
                break;

            case "luminance.above":
                luminanceAbove = parseAbsoluteOrPercentage(value);
                break;

            case "luminance.change.min":
                luminanceChangeMin = Double.parseDouble(value);
                break;

            default:
                super.param(key, value);
                break;
        }
    }

    private synchronized void generate() {
        double hue = hueMedian;
        double luminance = luminanceMedian;

        int tickCount = 0;

        int step = 0;
        ticks = 1;

        while (step < NSTEPS-1) {
            generateStep();

            if (factor < 0.0) {
                hue = hueMedian + hueBelow * (factor < -1.0 ? -1.0 : factor);
                luminance = luminanceMedian + luminanceBelow * (factor < -1.0 ? -1.0 : factor);
            } else {
                hue = hueMedian + hueAbove * (factor > 1.0 ? 1.0 : factor);
                luminance = luminanceMedian + luminanceAbove * (factor > 1.0 ? 1.0 : factor);
            }

            if (step == 0 || Math.abs(hue - lastHue) >= hueChangeMin || Math.abs(luminance - lastLuminance) >= luminanceChangeMin || ticks >= nextFlicker || ticks == 255) {
                if (debug) { logger.debug("    step {}: hue = {}, luminance = {}, ticks = {}, flareCount = {}, nextFlicker = {}, factor = {}, dampingTicks = {}", step, hue, luminance, ticks, flareCount, nextFlicker, factor, dampingTicks); }

                setHue(step, hue);
                setLuminance(step, luminance);
                setDuration(step, ticks);

                lastFactor = factor;
                lastHue = hue;
                lastLuminance = luminance;
                tickCount += ticks;
                nextFlicker -= ticks;
                ticks = 1;
                step++;
            }
        }

        // The duration on the last step is really a checksum. It isn't clear if the state
        // values have any meaning so we'll do this just in case.
        setHue(step, hue);
        setLuminance(step, luminance);

        bridgeHandler.sendMessage(new LightifySetEffectMessage(deviceHandler, name, params, color, data, background));

        tickJob = bridgeHandler.getScheduler().schedule(() -> { generate(); }, (long) tickCount * 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public synchronized void start() {
        writeEnableData();

        generate();

        background = true;

        if (debugOnce) {
            debugOnce = false;
            debug = false;
        }
    }

    @Override
    public synchronized void stop() {
        if (tickJob != null) {
            tickJob.cancel(true);
            tickJob = null;
        }
    }
}
