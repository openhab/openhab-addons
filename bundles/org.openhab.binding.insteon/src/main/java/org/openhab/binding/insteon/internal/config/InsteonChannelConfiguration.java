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
package org.openhab.binding.insteon.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.RampRate;

/**
 *
 * The {@link InsteonChannelConfiguration} is the configuration for an insteon channel.
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class InsteonChannelConfiguration {

    private int group = -1;
    private int onLevel = -1;
    private double rampRate = -1;
    private boolean original = true;

    public int getGroup() {
        return group;
    }

    public int getOnLevel() {
        return onLevel;
    }

    public @Nullable RampRate getRampRate() {
        return rampRate != -1 ? RampRate.fromTime(rampRate) : null;
    }

    public boolean isOriginal() {
        return original;
    }

    @Override
    public String toString() {
        String s = "";
        if (group != -1) {
            s += " group=" + group;
        }
        if (onLevel != -1) {
            s += " onLevel=" + onLevel;
        }
        if (rampRate != -1) {
            s += " rampRate=" + rampRate;
        }
        return s;
    }

    public static InsteonChannelConfiguration copyOf(InsteonChannelConfiguration original, int onLevel,
            RampRate rampRate) {
        InsteonChannelConfiguration config = new InsteonChannelConfiguration();
        config.group = original.group;
        config.onLevel = original.onLevel != -1 ? original.onLevel : onLevel;
        config.rampRate = original.rampRate != -1 ? original.rampRate : rampRate.getTimeInSeconds();
        config.original = false;
        return config;
    }
}
