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

import java.util.Random;

import org.eclipse.jdt.annotation.NonNullByDefault;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;
import org.openhab.binding.osramlightify.handler.LightifyDeviceHandler;

/**
 * Set a "flame" effect on a device.
 *
 * @author Mike Jagdis - Initial contribution
 */
@NonNullByDefault
public abstract class LightifyEffectFlame extends LightifyEffect {

    private static final Random random = new Random();

    // Set the speed flickers should (randomly) occur.
    private double rate = 12.0;

    // Drop at least this proportion of the range below median at the start of a flare.
    private double dropMin = 0.5;
    private double dropStdDev = 0.3;

    // How many ticks we flare for.
    private int flareTicks = 5;

    // How aggressively we jump between min and max while flaring. Smaller values make
    // the jumps more agressive (i.e. are more likely to take us closer to the limits).
    private double flareStdDev = 0.8;

    private double dampingCoeff1 = 0.3;
    private double dampingOmega1 = Math.PI * 10 / 10;
    private double dampingCoeff2 = 0.3;
    private double dampingOmega2 = Math.PI * 10 / 40;

    private double burbleWeight = 0.5;
    private double burbleSize = 2.0;
    private double burbleSizeStdDev = 0.3;
    private double burbleTime = 20.0;
    private double burbleTimeStdDev = 1.0;

    protected double dampingTicks = -1.0;
    private double dampingThreshold = 0.1;
    protected int nextFlicker = 0;
    protected int flareCount;
    private double momentum;

    protected double factor;
    protected double lastFactor;
    protected int ticks;

    public LightifyEffectFlame(LightifyBridgeHandler bridgeHandler, LightifyDeviceHandler deviceHandler, String name, boolean color, byte[] genericData) {
        super(bridgeHandler, deviceHandler, name, color, genericData);
    }

    @Override
    protected void param(String key, String value) {
        switch (key) {
            case "rate":
                rate = Double.parseDouble(value);
                break;

            case "drop.min":
                dropMin = Double.parseDouble(value);
                break;

            case "drop.stddev":
                dropStdDev = Double.parseDouble(value);
                break;

            case "flare.ticks":
                flareTicks = Integer.parseInt(value);
                break;

            case "flare.stddev":
                flareStdDev = Double.parseDouble(value);
                break;

            case "damping.coeff1":
                dampingCoeff1 = Double.parseDouble(value);
                break;

            case "damping.freq1":
                dampingOmega1 = Math.PI * 10 / Double.parseDouble(value);
                break;

            case "damping.coeff2":
                dampingCoeff2 = Double.parseDouble(value);
                break;

            case "damping.freq2":
                dampingOmega2 = Math.PI * 10 / Double.parseDouble(value);
                break;

            case "burble.weight":
                burbleWeight = Double.parseDouble(value);
                break;

            case "burble.size":
                burbleSize = Double.parseDouble(value);
                break;

            case "burble.size.stddev":
                burbleSizeStdDev = Double.parseDouble(value);
                break;

            case "burble.time":
                burbleTime = Double.parseDouble(value);
                break;

            case "burble.time.stddev":
                burbleTimeStdDev = Double.parseDouble(value);
                break;

            default:
                super.param(key, value);
                break;
        }
    }

    protected void generateStep() {
        if (nextFlicker == 0) {
            // ***** Phase 1: initial drop

            if (debug) { logger.debug("    drop"); }
            factor = -dropMin - Math.abs(random.nextGaussian() * dropStdDev);

            flareCount = flareTicks;
            momentum = 1.0;
            dampingTicks = -1.0;

            // Schedule next flicker
            // This is more likely to occur sooner rather than later
            nextFlicker = 1 + (int) Math.abs(random.nextGaussian() * rate);

        } else if (flareCount != 0) {
            // ***** Phase 2: chaotic oscillations (flare)

            if (debug) { logger.debug("    flare"); }
            factor = momentum * (1 - Math.abs(random.nextGaussian() * flareStdDev));

            momentum = -momentum;
            flareCount--;

            if (flareCount == 0) {
                dampingTicks = 0.0;
            }

        } else if (dampingTicks >= 0.0) {
            // ***** Phase 3: damped harmonic oscillations

            double factor1 = Math.exp(-dampingTicks * dampingCoeff1) * Math.cos(dampingOmega1 * dampingTicks);
            double factor2 = Math.exp(-dampingTicks * dampingCoeff2) * Math.cos(dampingOmega2 * dampingTicks);

            factor = factor1 + factor2;

            if (Math.abs(factor1) + Math.abs(factor2) < dampingThreshold) {
                dampingTicks = -1.0;
            } else {
                dampingTicks++;
            }

        } else {
            // ***** Phase 4: background burbling

            factor = lastFactor + -lastFactor * burbleWeight + (burbleSize * random.nextGaussian() * burbleSizeStdDev) % burbleSize;
            ticks += (int) Math.abs((burbleTime * random.nextGaussian() * burbleTimeStdDev) % burbleTime);
        }
    }
}
