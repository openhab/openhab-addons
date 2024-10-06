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
public class Overlay {
    @SerializedName("setting")
    private GenericZoneSetting setting = null;

    @SerializedName("termination")
    private OverlayTerminationCondition termination = null;

    public Overlay setting(GenericZoneSetting setting) {
        this.setting = setting;
        return this;
    }

    public GenericZoneSetting getSetting() {
        return setting;
    }

    public void setSetting(GenericZoneSetting setting) {
        this.setting = setting;
    }

    public Overlay termination(OverlayTerminationCondition termination) {
        this.termination = termination;
        return this;
    }

    public OverlayTerminationCondition getTermination() {
        return termination;
    }

    public void setTermination(OverlayTerminationCondition termination) {
        this.termination = termination;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Overlay overlay = (Overlay) o;
        return Objects.equals(this.setting, overlay.setting) && Objects.equals(this.termination, overlay.termination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(setting, termination);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Overlay {\n");

        sb.append("    setting: ").append(toIndentedString(setting)).append("\n");
        sb.append("    termination: ").append(toIndentedString(termination)).append("\n");
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
