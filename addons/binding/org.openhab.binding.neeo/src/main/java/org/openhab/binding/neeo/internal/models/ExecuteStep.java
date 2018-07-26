/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.neeo.internal.models;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The model representing an execute setp (serialize/deserialize json use only)
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ExecuteStep {

    /** The duration of the step */
    private int duration;

    /** The text describing the step */
    @Nullable
    private String text;

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
    @Nullable
    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "ExecuteStep [duration=" + duration + ", text=" + text + "]";
    }
}
