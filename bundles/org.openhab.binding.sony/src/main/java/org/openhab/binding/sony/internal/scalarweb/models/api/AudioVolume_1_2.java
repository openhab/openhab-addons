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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class represents the audio volume and is used for serialization
 *
 * Versions:
 * <ol>
 * <li>1.2: {"target":"string", "volume":"string", "ui":"string"}</li>
 * </ol>
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class AudioVolume_1_2 extends AudioVolume_1_0 {
    /**
     * Instantiates a new audio volume.
     *
     * @param target the non-null, possibly empty target
     * @param volume the volume
     */
    public AudioVolume_1_2(final String target, final int volume) {
        super(target, volume);
    }

    public AudioVolume_1_2(final String target, final String volume) {
        super(target, volume);
    }

    @Override
    public String toString() {
        return "AudioVolume_1_2 [target=" + getTarget() + ", volume=" + getVolume() + "]";
    }
}
