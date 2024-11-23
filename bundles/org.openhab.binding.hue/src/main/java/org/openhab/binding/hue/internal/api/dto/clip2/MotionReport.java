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
package org.openhab.binding.hue.internal.api.dto.clip2;

import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * DTO for CLIP 2 motion sensor report.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class MotionReport {
    private @NonNullByDefault({}) Instant changed;
    private boolean motion;

    /**
     * @return last time the value of this property is changed.
     */
    public Instant getLastChanged() {
        return changed;
    }

    /**
     * @return true if motion is detected
     */
    public boolean isMotion() {
        return motion;
    }
}
