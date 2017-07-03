/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sony.internal.scalarweb.models.api;

// TODO: Auto-generated Javadoc
/**
 * The Class PowerSyncMode.
 * 
 * @author Tim Roberts - Initial contribution
 */
public class PowerSyncMode {

    /** The sink power off sync. */
    private final Boolean sinkPowerOffSync;

    /** The source power on sync. */
    private final Boolean sourcePowerOnSync;

    /**
     * Instantiates a new power sync mode.
     *
     * @param sinkPowerOffSync the sink power off sync
     * @param sourcePowerOnSync the source power on sync
     */
    public PowerSyncMode(Boolean sinkPowerOffSync, Boolean sourcePowerOnSync) {
        super();
        this.sinkPowerOffSync = sinkPowerOffSync;
        this.sourcePowerOnSync = sourcePowerOnSync;
    }

    /**
     * Checks if is sink power off sync.
     *
     * @return true, if is sink power off sync
     */
    public boolean isSinkPowerOffSync() {
        return sinkPowerOffSync;
    }

    /**
     * Checks if is source power on sync.
     *
     * @return true, if is source power on sync
     */
    public boolean isSourcePowerOnSync() {
        return sourcePowerOnSync;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "PowerSyncMode [sinkPowerOffSync=" + sinkPowerOffSync + ", sourcePowerOnSync=" + sourcePowerOnSync + "]";
    }
}
