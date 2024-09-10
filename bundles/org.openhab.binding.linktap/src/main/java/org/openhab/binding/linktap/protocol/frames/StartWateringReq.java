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
package org.openhab.binding.linktap.protocol.frames;

import static org.openhab.binding.linktap.protocol.frames.ValidationError.Cause.USER;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link DismissAlertReq} defines the request to start watering immediately.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class StartWateringReq extends DeviceCmdReq {

    public StartWateringReq() {
    }

    public StartWateringReq(final int durationSecs, final int volume) {
        this.command = CMD_IMMEDIATE_WATER_START;
        this.duration = durationSecs;
        this.volume = volume;
    }

    /**
     * Defines the time duration in seconds to water for.
     * Minimum value 3
     * Maximum value 86340
     */
    @SerializedName("duration")
    @Expose
    public int duration = DEFAULT_INT;

    /**
     * Defines the volume of water to use
     * Units may be L or gal, depending on the units the device
     * is operating in.
     */
    @SerializedName("volume")
    @Expose
    public int volume = DEFAULT_INT;

    public Collection<ValidationError> getValidationErrors() {
        final Collection<ValidationError> errors = super.getValidationErrors();

        if (duration < 3 || duration > 86340) {
            errors.add(new ValidationError("duration", "not in range 3 -> 86340", USER));
        }

        return errors;
    }
}
