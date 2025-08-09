/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.jellyfin.internal.api.generated.current.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class GroupStateUpdate.
 */
@JsonPropertyOrder({ GroupStateUpdate.JSON_PROPERTY_STATE, GroupStateUpdate.JSON_PROPERTY_REASON })

public class GroupStateUpdate {
    public static final String JSON_PROPERTY_STATE = "State";
    @org.eclipse.jdt.annotation.NonNull
    private GroupStateType state;

    public static final String JSON_PROPERTY_REASON = "Reason";
    @org.eclipse.jdt.annotation.NonNull
    private PlaybackRequestType reason;

    public GroupStateUpdate() {
    }

    public GroupStateUpdate state(@org.eclipse.jdt.annotation.NonNull GroupStateType state) {
        this.state = state;
        return this;
    }

    /**
     * Gets the state of the group.
     * 
     * @return state
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_STATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public GroupStateType getState() {
        return state;
    }

    @JsonProperty(JSON_PROPERTY_STATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setState(@org.eclipse.jdt.annotation.NonNull GroupStateType state) {
        this.state = state;
    }

    public GroupStateUpdate reason(@org.eclipse.jdt.annotation.NonNull PlaybackRequestType reason) {
        this.reason = reason;
        return this;
    }

    /**
     * Gets the reason of the state change.
     * 
     * @return reason
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_REASON)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PlaybackRequestType getReason() {
        return reason;
    }

    @JsonProperty(JSON_PROPERTY_REASON)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReason(@org.eclipse.jdt.annotation.NonNull PlaybackRequestType reason) {
        this.reason = reason;
    }

    /**
     * Return true if this GroupStateUpdate object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GroupStateUpdate groupStateUpdate = (GroupStateUpdate) o;
        return Objects.equals(this.state, groupStateUpdate.state)
                && Objects.equals(this.reason, groupStateUpdate.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, reason);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class GroupStateUpdate {\n");
        sb.append("    state: ").append(toIndentedString(state)).append("\n");
        sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
