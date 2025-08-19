/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

    /**
     * Creates a copy of this configuration
     *
     * @param defaultOnLevel default on level value
     * @param defaultRampRate default ramp rate value
     * @return a new configuration instance
     */
    public InsteonChannelConfiguration copy(int defaultOnLevel, RampRate defaultRampRate) {
        InsteonChannelConfiguration config = new InsteonChannelConfiguration();
        config.group = group;
        config.onLevel = onLevel != -1 ? onLevel : defaultOnLevel;
        config.rampRate = rampRate != -1 ? rampRate : defaultRampRate.getTimeInSeconds();
        config.original = false;
        return config;
    }
}
