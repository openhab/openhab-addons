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

import java.util.Objects;

import com.google.gson.annotations.SerializedName;

/**
 * Static imported copy of the Java file originally created by Swagger Codegen.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
public class OverlayTemplate {
    @SerializedName("terminationCondition")
    private OverlayTerminationConditionTemplate terminationCondition = null;

    public OverlayTemplate terminationCondition(OverlayTerminationConditionTemplate terminationCondition) {
        this.terminationCondition = terminationCondition;
        return this;
    }

    public OverlayTerminationConditionTemplate getTerminationCondition() {
        return terminationCondition;
    }

    public void setTerminationCondition(OverlayTerminationConditionTemplate terminationCondition) {
        this.terminationCondition = terminationCondition;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OverlayTemplate overlayTemplate = (OverlayTemplate) o;
        return Objects.equals(this.terminationCondition, overlayTemplate.terminationCondition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(terminationCondition);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OverlayTemplate {\n");

        sb.append("    terminationCondition: ").append(toIndentedString(terminationCondition)).append("\n");
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
