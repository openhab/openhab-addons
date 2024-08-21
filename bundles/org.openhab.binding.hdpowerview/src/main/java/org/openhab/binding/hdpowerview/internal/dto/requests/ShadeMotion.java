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
package org.openhab.binding.hdpowerview.internal.dto.requests;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A motion directive for a shade
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class ShadeMotion {

    public enum Type {
        STOP("stop"),
        JOG("jog"),
        CALIBRATE("calibrate");

        private String motion;

        Type(String motion) {
            this.motion = motion;
        }

        public String getMotion() {
            return this.motion;
        }
    }

    public String motion;

    public ShadeMotion(Type motionType) {
        this.motion = motionType.getMotion();
    }
}
