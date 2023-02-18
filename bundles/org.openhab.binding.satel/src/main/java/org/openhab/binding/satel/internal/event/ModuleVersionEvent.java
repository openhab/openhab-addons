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
package org.openhab.binding.satel.internal.event;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Event class describing version of communication module.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class ModuleVersionEvent implements SatelEvent {

    private String version;
    private boolean extPayloadSupport;

    /**
     * Constructs new event class.
     *
     * @param version string describing version number and firmware revision
     * @param extPayloadSupport the module supports extended (32-bit) payload for zones/outputs
     */
    public ModuleVersionEvent(String version, boolean extPayloadSupport) {
        this.version = version;
        this.extPayloadSupport = extPayloadSupport;
    }

    /**
     * @return firmware version and date
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return <code>true</code> if the module supports extended (32-bit) payload for zones/outputs
     */
    public boolean hasExtPayloadSupport() {
        return this.extPayloadSupport;
    }

    @Override
    public String toString() {
        return String.format("ModuleVersionEvent: version = %s, extPayloadSupport = %b", this.version,
                this.extPayloadSupport);
    }
}
