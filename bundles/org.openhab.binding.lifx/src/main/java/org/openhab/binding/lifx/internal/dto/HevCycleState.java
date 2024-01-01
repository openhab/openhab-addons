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
package org.openhab.binding.lifx.internal.dto;

import java.time.Duration;

/**
 * The pending or current HEV cycle state.
 *
 * @author Wouter Born - Initial contribution
 */
public class HevCycleState {

    public static final HevCycleState OFF = new HevCycleState(false);
    public static final HevCycleState ON = new HevCycleState(true);

    private boolean enable;
    private Duration duration;

    public HevCycleState(boolean enable) {
        this.enable = enable;
        this.duration = Duration.ZERO;
    }

    public HevCycleState(boolean enable, Duration duration) {
        this.enable = enable;
        this.duration = duration;
    }

    public boolean isEnable() {
        return enable;
    }

    public Duration getDuration() {
        return duration;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((duration == null) ? 0 : duration.hashCode());
        result = prime * result + (enable ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        HevCycleState other = (HevCycleState) obj;
        if (duration == null) {
            if (other.duration != null) {
                return false;
            }
        } else if (!duration.equals(other.duration)) {
            return false;
        }
        return enable == other.enable;
    }

    @Override
    public String toString() {
        return "HevCycleState [enable=" + enable + ", duration=" + duration + "]";
    }
}
