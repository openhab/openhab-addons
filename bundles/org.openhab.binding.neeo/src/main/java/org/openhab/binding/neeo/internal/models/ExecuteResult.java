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
package org.openhab.binding.neeo.internal.models;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The model representing an execute result (serialize/deserialize json use only)
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ExecuteResult {

    /** The estimated duration */
    private int estimatedDuration;

    /** The name */
    @Nullable
    private String name;

    /** The start time */
    private long startTime;

    /** The steps */
    private ExecuteStep @Nullable [] steps;

    /** The type */
    @Nullable
    private String type;

    /**
     * Gets the estimated duration.
     *
     * @return the estimated duration
     */
    public int getEstimatedDuration() {
        return estimatedDuration;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Gets the steps.
     *
     * @return the steps
     */
    public ExecuteStep[] getSteps() {
        final ExecuteStep @Nullable [] localSteps = steps;
        return localSteps == null ? new ExecuteStep[0] : localSteps;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    @Nullable
    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ExecuteResult [estimatedDuration=" + estimatedDuration + ", name=" + name + ", startTime=" + startTime
                + ", steps=" + Arrays.toString(steps) + ", type=" + type + "]";
    }
}
