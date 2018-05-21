/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.hyperion.internal.protocol.ng;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link Priority} is a POJO for priority information in the Hyperion.ng server.
 *
 * @author Daniel Walters - Initial contribution
 */
public class Priority {

    @SerializedName("active")
    private Boolean active;

    @SerializedName("componentId")
    private String componentId;

    @SerializedName("duration_ms")
    private Integer durationMs;

    @SerializedName("origin")
    private String origin;

    @SerializedName("owner")
    private String owner;

    @SerializedName("priority")
    private Integer priority;

    @SerializedName("visible")
    private Boolean visible;

    @SerializedName("value")
    private Value value;

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public Integer getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Integer durationMs) {
        this.durationMs = durationMs;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean isVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

}
