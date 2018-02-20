/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.atlona.internal.pro3;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openhab.binding.atlona.handler.AtlonaCapabilities;

/**
 * The capabilities class for the Atlona PRO3 line. Each PRO3 model differs in the number of (output) ports that can be
 * powered, the number of audio ports there are and which (output) ports are HDMI ports.
 *
 * @author Tim Roberts
 */
public class AtlonaPro3Capabilities extends AtlonaCapabilities {

    /**
     * Number of power ports
     */
    private final int _nbrPowerPorts;

    /**
     * Number of audio ports
     */
    private final int _nbrAudioPorts;

    /**
     * The set of output ports that are HDMI ports
     */
    private final Set<Integer> _hdmiPorts;

    /**
     * Constructs the capabilities from the parms
     *
     * @param nbrPowerPorts a greater than 0 number of power ports
     * @param nbrAudioPorts a greater than 0 number of audio ports
     * @param hdmiPorts a non-null, non-empty set of hdmi ports
     */
    public AtlonaPro3Capabilities(int nbrPowerPorts, int nbrAudioPorts, Set<Integer> hdmiPorts) {
        super();

        if (nbrPowerPorts < 1) {
            throw new IllegalArgumentException("nbrPowerPorts must be greater than 0");
        }

        if (nbrAudioPorts < 1) {
            throw new IllegalArgumentException("nbrAudioPorts must be greater than 0");
        }

        if (hdmiPorts == null) {
            throw new IllegalArgumentException("hdmiPorts cannot be null");
        }

        if (hdmiPorts.size() == 0) {
            throw new IllegalArgumentException("hdmiPorts cannot be empty");
        }

        _nbrPowerPorts = nbrPowerPorts;
        _nbrAudioPorts = nbrAudioPorts;
        _hdmiPorts = Collections.unmodifiableSet(new HashSet<Integer>(hdmiPorts));
    }

    /**
     * Returns the number of power ports
     *
     * @return a greater than 0 number of power ports
     */
    int getNbrPowerPorts() {
        return _nbrPowerPorts;
    }

    /**
     * Returns the number of audio ports
     *
     * @return a greater than 0 number of audio ports
     */
    int getNbrAudioPorts() {
        return _nbrAudioPorts;
    }

    /**
     * Returns the set of hdmi ports
     *
     * @return a non-null, non-empty immutable set of hdmi ports
     */
    Set<Integer> getHdmiPorts() {
        return _hdmiPorts;
    }
}
