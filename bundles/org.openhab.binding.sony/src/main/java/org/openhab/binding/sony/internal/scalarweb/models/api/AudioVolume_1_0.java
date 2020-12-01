/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import java.util.Objects;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the audio volume and is used for serialization
 *
 * Versions:
 * <ol>
 * <li>1.0: {"target":"string", "volume":"string"}</li>
 * </ol>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class AudioVolume_1_0 {

    /** The target of the volume */
    private final @Nullable String target;

    /** The volume level */
    private final @Nullable String volume;

    /**
     * Instantiates a new audio volume.
     *
     * @param target the non-null, non-empty target
     * @param volume the volume
     */
    public AudioVolume_1_0(final String target, final int volume) {
        this(target, String.valueOf(volume));
    }

    /**
     * Instantiates a new audio volume.
     *
     * @param target the non-null, possibly empty target
     * @param volume the non-null, non-empty volume
     */
    public AudioVolume_1_0(final String target, final String volume) {
        Objects.requireNonNull(target, "target cannot be empty");
        Validate.notEmpty(volume, "volume cannot be empty");

        this.target = target;
        this.volume = volume;
    }

    /**
     * Gets the volume.
     *
     * @return the volume
     */
    public @Nullable String getVolume() {
        return volume;
    }

    /**
     * Gets the target.
     *
     * @return the target
     */
    public @Nullable String getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "AudioVolume_1_0 [target=" + target + ", volume=" + volume + "]";
    }
}
