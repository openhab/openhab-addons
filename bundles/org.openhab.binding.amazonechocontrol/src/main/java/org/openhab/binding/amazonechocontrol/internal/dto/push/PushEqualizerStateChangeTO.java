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
package org.openhab.binding.amazonechocontrol.internal.dto.push;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The {@link PushEqualizerStateChangeTO} encapsulates PUSH_EQUALIZER_STATE_CHANGE messages
 *
 * @author Jan N. Klug - Initial contribution
 */
public class PushEqualizerStateChangeTO extends PushDeviceTO {
    public int bass;
    public int midrange;
    public int treble;

    @Override
    public @NonNull String toString() {
        return "PushEqualizerStateChangeTO{bass=" + bass + ", midrange=" + midrange + ", treble=" + treble
                + ", destinationUserId='" + destinationUserId + "', dopplerId=" + dopplerId + "}";
    }
}
