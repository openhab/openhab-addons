/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal.protocol.v1;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Priority} is a POJO for the priority information on the Hyperion server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class Priority {

    @SerializedName("priority")
    private Integer priority;

    @SerializedName("duration_ms")
    private Integer durationMs;

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Integer getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Integer durationMs) {
        this.durationMs = durationMs;
    }

}
