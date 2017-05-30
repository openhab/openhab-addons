/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.models;

import java.util.Arrays;

/**
 * The model representing an execute result (serialize/deserialize json use only)
 *
 * @author Tim Roberts - Initial contribution
 */
public class ExecuteResult {

    /** The estimated duration */
    private final int estimatedDuration;

    /** The name */
    private final String name;

    /** The start time */
    private final long startTime;

    /** The steps */
    private final ExecuteStep[] steps;

    /** The type */
    private final String type;

    /**
     * Instantiates a new execute result.
     */
    public ExecuteResult() {
        this(0, "", 0, new ExecuteStep[0], "");
    }

    /**
     * Instantiates a new execute result.
     *
     * @param estimatedDuration the estimated duration
     * @param name the name
     * @param startTime the start time
     * @param steps the steps
     * @param type the type
     */
    public ExecuteResult(int estimatedDuration, String name, long startTime, ExecuteStep[] steps, String type) {
        this.estimatedDuration = estimatedDuration;
        this.name = name;
        this.startTime = startTime;
        this.steps = steps;
        this.type = type;
    }

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
        return steps;
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "ExecuteResult [estimatedDuration=" + estimatedDuration + ", name=" + name + ", startTime=" + startTime
                + ", steps=" + Arrays.toString(steps) + ", type=" + type + "]";
    }

}
