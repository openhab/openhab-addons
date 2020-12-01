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
 * This class represents the information for a specific software update
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SoftwareUpdateInfo {
    /** Estimated time to update */
    private @Nullable Integer estimatedTimeSec;

    /** Whether a forced update is required */
    private @Nullable String forcedUpdate;

    /** The target of the update */
    private @Nullable String target;

    /** The update version */
    private @Nullable String updatableVersion;

    /**
     * Constructor used for deserialization only
     */
    public SoftwareUpdateInfo() {
    }

    /**
     * Get's the estimated time to updated
     * 
     * @return the estimated time to updated
     */
    public @Nullable Integer getEstimatedTimeSec() {
        return estimatedTimeSec;
    }

    /**
     * Get's whether a forced update is required
     * 
     * @return whether a forced update is required
     */
    public @Nullable String getForcedUpdate() {
        return forcedUpdate;
    }

    /**
     * Get's the target of the update
     * 
     * @return the target of the update
     */
    public @Nullable String getTarget() {
        return target;
    }

    /**
     * Get's the update version
     * 
     * @return the update version
     */
    public @Nullable String getUpdatableVersion() {
        return updatableVersion;
    }

    @Override
    public String toString() {
        return "SoftwareUpdateInfo [estimatedTimeSec=" + estimatedTimeSec + ", forcedUpdate=" + forcedUpdate
                + ", target=" + target + ", updatableVersion=" + updatableVersion + "]";
    }
}
