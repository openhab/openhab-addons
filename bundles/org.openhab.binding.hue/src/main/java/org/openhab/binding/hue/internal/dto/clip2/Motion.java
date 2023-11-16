/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.hue.internal.dto.clip2;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for CLIP 2 motion sensor.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class Motion {
    private boolean motion;
    private @SerializedName("motion_valid") boolean motionValid;
    private @Nullable @SerializedName("motion_report") MotionReport motionReport;

    /**
     * The underlying field is deprecated in the CLIP 2 API.
     * Moved to motion_report/motion.
     * Should be used only as fallback for older firmwares.
     *
     * @return true if motion is detected
     */
    public boolean isMotion() {
        return motion;
    }

    /**
     * The underlying field is deprecated in the CLIP 2 API.
     * Motion is valid when motion_report property is present, invalid when absent.
     * Should be used only as fallback for older firmwares.
     */
    public boolean isMotionValid() {
        return motionValid;
    }

    public State getMotionState() {
        return motionValid ? OnOffType.from(motion) : UnDefType.UNDEF;
    }

    public State getMotionValidState() {
        return OnOffType.from(motionValid);
    }

    public @Nullable MotionReport getMotionReport() {
        return motionReport;
    }
}
