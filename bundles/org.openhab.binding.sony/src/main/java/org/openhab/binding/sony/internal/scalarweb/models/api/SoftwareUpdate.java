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

import java.util.List;

import org.apache.commons.lang.BooleanUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the information on a software update
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SoftwareUpdate {

    /** The updateable status (usually "true" or "false") */
    private @Nullable String isUpdatable;

    /** The update information */
    private @Nullable List<@Nullable SoftwareUpdateInfo> swInfo;

    /**
     * Constructor used for deserialization only
     */
    public SoftwareUpdate() {
    }

    /**
     * Get's the updateable status
     * 
     * @return the updateable statuc
     */
    public @Nullable String getIsUpdatable() {
        return isUpdatable;
    }

    /**
     * Attempt's to determine if there is a software update
     * 
     * @return true if an update, false otherwise
     */
    public boolean isUpdatable() {
        return BooleanUtils.toBooleanObject(isUpdatable) == Boolean.TRUE;
    }

    /**
     * Get's the software update information
     * 
     * @return the software update information
     */
    public @Nullable List<@Nullable SoftwareUpdateInfo> getSwInfo() {
        return swInfo;
    }

    @Override
    public String toString() {
        return "SoftwareUpdate [isUpdatable=" + isUpdatable + ", swInfo=" + swInfo + "]";
    }
}
