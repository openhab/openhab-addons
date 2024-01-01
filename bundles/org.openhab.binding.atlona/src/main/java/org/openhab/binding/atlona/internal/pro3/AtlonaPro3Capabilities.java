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
package org.openhab.binding.atlona.internal.pro3;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atlona.internal.handler.AtlonaCapabilities;

/**
 * The capabilities class for the Atlona PRO3 line. Each PRO3 model differs in the number of (output) ports that can be
 * powered, the number of audio ports there are and which (output) ports are HDMI ports.
 *
 * @author Tim Roberts - Initial contribution
 * @author Michael Lobstein - Add support for AT-PRO3HD66M
 */
@NonNullByDefault
public class AtlonaPro3Capabilities extends AtlonaCapabilities {

    /**
     * Number of power ports
     */
    private final int nbrPowerPorts;

    /**
     * Number of audio ports
     */
    private final int nbrAudioPorts;

    /**
     * The set of output ports that are HDMI ports
     */
    private final Set<Integer> hdmiPorts;

    /**
     * Indicates if the thing is a 4K/UHD model vs an older HD model
     */
    private final boolean isUHDModel;

    /**
     * Constructs the capabilities from the parms
     *
     * @param nbrPowerPorts a greater than 0 number of power ports
     * @param nbrAudioPorts a greater than 0 number of audio ports
     * @param hdmiPorts a non-null, non-empty set of hdmi ports
     */
    public AtlonaPro3Capabilities(int nbrPowerPorts, int nbrAudioPorts, Set<Integer> hdmiPorts, boolean isUHDModel) {
        super();

        if (hdmiPorts.isEmpty()) {
            throw new IllegalArgumentException("hdmiPorts cannot be empty");
        }

        this.nbrPowerPorts = nbrPowerPorts;
        this.nbrAudioPorts = nbrAudioPorts;
        this.hdmiPorts = Collections.unmodifiableSet(new HashSet<>(hdmiPorts));
        this.isUHDModel = isUHDModel;
    }

    /**
     * Returns the number of power ports
     *
     * @return a greater than 0 number of power ports
     */
    int getNbrPowerPorts() {
        return nbrPowerPorts;
    }

    /**
     * Returns the number of audio ports
     *
     * @return a greater than 0 number of audio ports
     */
    int getNbrAudioPorts() {
        return nbrAudioPorts;
    }

    /**
     * Returns the set of hdmi ports
     *
     * @return a non-null, non-empty immutable set of hdmi ports
     */
    Set<Integer> getHdmiPorts() {
        return hdmiPorts;
    }

    /**
     * Returns a flag indicating the model type
     *
     * @return boolean true if the thing is a 4K/UHD model
     */
    boolean isUHDModel() {
        return isUHDModel;
    }
}
