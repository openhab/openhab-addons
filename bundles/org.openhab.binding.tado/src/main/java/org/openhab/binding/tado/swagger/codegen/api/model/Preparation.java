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
public class Preparation {
    @SerializedName("tadoMode")
    private TadoMode tadoMode = null;

    @SerializedName("end")
    private OffsetDateTime end = null;

    @SerializedName("setting")
    private GenericZoneSetting setting = null;

    public Preparation tadoMode(TadoMode tadoMode) {
        this.tadoMode = tadoMode;
        return this;
    }

    public TadoMode getTadoMode() {
        return tadoMode;
    }

    public void setTadoMode(TadoMode tadoMode) {
        this.tadoMode = tadoMode;
    }

    public Preparation end(OffsetDateTime end) {
        this.end = end;
        return this;
    }

    public OffsetDateTime getEnd() {
        return end;
    }

    public void setEnd(OffsetDateTime end) {
        this.end = end;
    }

    public Preparation setting(GenericZoneSetting setting) {
        this.setting = setting;
        return this;
    }

    public GenericZoneSetting getSetting() {
        return setting;
    }

    public void setSetting(GenericZoneSetting setting) {
        this.setting = setting;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Preparation preparation = (Preparation) o;
        return Objects.equals(this.tadoMode, preparation.tadoMode) && Objects.equals(this.end, preparation.end)
                && Objects.equals(this.setting, preparation.setting);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tadoMode, end, setting);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Preparation {\n");

        sb.append("    tadoMode: ").append(toIndentedString(tadoMode)).append("\n");
        sb.append("    end: ").append(toIndentedString(end)).append("\n");
        sb.append("    setting: ").append(toIndentedString(setting)).append("\n");
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
