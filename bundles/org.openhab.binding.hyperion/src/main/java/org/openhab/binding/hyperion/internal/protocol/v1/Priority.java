/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
