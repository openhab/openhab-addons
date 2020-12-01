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
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the request for the power sync (CEC) mode and is used for serialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class PowerSyncMode {

    /** The sink power off sync */
    private final @Nullable Boolean sinkPowerOffSync;

    /** The source power on sync */
    private final @Nullable Boolean sourcePowerOnSync;

    /**
     * Instantiates a new power sync mode
     *
     * @param sinkPowerOffSync the sink power off sync (null to not specify)
     * @param sourcePowerOnSync the source power on sync (null to not specify)
     */
    public PowerSyncMode(final @Nullable Boolean sinkPowerOffSync, final @Nullable Boolean sourcePowerOnSync) {
        this.sinkPowerOffSync = sinkPowerOffSync;
        this.sourcePowerOnSync = sourcePowerOnSync;
    }

    /**
     * Checks if is sink power off sync
     *
     * @return true, if is sink power off sync
     */
    public @Nullable Boolean isSinkPowerOffSync() {
        return sinkPowerOffSync;
    }

    /**
     * Checks if is source power on sync.
     *
     * @return true, if is source power on sync
     */
    public @Nullable Boolean isSourcePowerOnSync() {
        return sourcePowerOnSync;
    }

    @Override
    public String toString() {
        return "PowerSyncMode [sinkPowerOffSync=" + sinkPowerOffSync + ", sourcePowerOnSync=" + sourcePowerOnSync + "]";
    }
}
