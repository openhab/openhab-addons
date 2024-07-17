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
package org.openhab.binding.tado.swagger.codegen.api.model;

import java.time.OffsetDateTime;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

/**
 * Static imported copy of the Java file originally created by Swagger Codegen.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class OverlayTerminationCondition {
    @SerializedName("type")
    private OverlayTerminationConditionType type = null;

    @SerializedName("projectedExpiry")
    private OffsetDateTime projectedExpiry = null;

    public OverlayTerminationCondition type(OverlayTerminationConditionType type) {
        this.type = type;
        return this;
    }

    public OverlayTerminationConditionType getType() {
        return type;
    }

    public void setType(OverlayTerminationConditionType type) {
        this.type = type;
    }

    public OffsetDateTime getProjectedExpiry() {
        return projectedExpiry;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OverlayTerminationCondition overlayTerminationCondition = (OverlayTerminationCondition) o;
        return Objects.equals(this.type, overlayTerminationCondition.type)
                && Objects.equals(this.projectedExpiry, overlayTerminationCondition.projectedExpiry);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, projectedExpiry);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OverlayTerminationCondition {\n");

        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    projectedExpiry: ").append(toIndentedString(projectedExpiry)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
