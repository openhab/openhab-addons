/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.models;

/**
 * The model representing an execute setp (serialize/deserialize json use only)
 *
 * @author Tim Roberts - Initial contribution
 */
public class ExecuteStep {

    /** The duration of the step */
    private final int duration;

    /** The text describing the step */
    private final String text;

    /**
     * Instantiates a new execute step.
     *
     * @param duration the duration
     * @param text the text
     */
    public ExecuteStep(int duration, String text) {
        this.duration = duration;
        this.text = text;
    }

    /**
     * Gets the duration of the step
     *
     * @return the duration
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Gets the text describing the step
     *
     * @return the text
     */
    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "ExecuteStep [duration=" + duration + ", text=" + text + "]";
    }
}
