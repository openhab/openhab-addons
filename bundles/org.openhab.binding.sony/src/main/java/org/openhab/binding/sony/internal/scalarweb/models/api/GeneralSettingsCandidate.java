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

import org.apache.commons.lang.BooleanUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class describes the candidate parameters for a general setting. Please note that only specific fields are used
 * based on the overall setting (example: only a double slider will have min/max/step values whereas an enum will have
 * values)
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class GeneralSettingsCandidate {
    /** Whether the candidate is currently available */
    private @Nullable Boolean isAvailable;

    /** The minimum value */
    private @Nullable Double min;

    /** The maximum value */
    private @Nullable Double max;

    /** The step of the value */
    private @Nullable Double step;

    /** The candidate title */
    private @Nullable String title;

    /** The candidate title text id */
    private @Nullable String titleTextID;

    /** The value of the candidate */
    private @Nullable String value;

    /**
     * Constructor used for deserialization only
     */
    public GeneralSettingsCandidate() {
    }

    /**
     * Whether the candidate is available
     * 
     * @return true if available, false otherwise
     */
    public boolean isAvailable() {
        return isAvailable == null || BooleanUtils.isTrue(isAvailable);
    }

    /**
     * Gets the minimum value
     * 
     * @return the minimum value
     */
    public @Nullable Double getMin() {
        return min;
    }

    /**
     * Gets the maximum value
     * 
     * @return the maximum value
     */
    public @Nullable Double getMax() {
        return max;
    }

    /**
     * Gets the step value
     * 
     * @return the step value
     */
    public @Nullable Double getStep() {
        return step;
    }

    /**
     * Gets the title
     * 
     * @return the title
     */
    public @Nullable String getTitle() {
        return title;
    }

    /**
     * Gets the title text identifier
     * 
     * @return the title text identifier
     */
    public @Nullable String getTitleTextID() {
        return titleTextID;
    }

    /**
     * Gets the value
     * 
     * @return the value
     */
    public @Nullable String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "GeneralSettingsCandidate [isAvailable=" + isAvailable + ", max=" + max + ", min=" + min + ", step="
                + step + ", title=" + title + ", titleTextID=" + titleTextID + ", value=" + value + "]";
    }
}
