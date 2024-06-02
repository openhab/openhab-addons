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
public class TimerTerminationConditionTemplate extends OverlayTerminationConditionTemplate {
    @SerializedName("durationInSeconds")
    private Integer durationInSeconds = null;

    public TimerTerminationConditionTemplate durationInSeconds(Integer durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
        return this;
    }

    public Integer getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(Integer durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TimerTerminationConditionTemplate timerTerminationConditionTemplate = (TimerTerminationConditionTemplate) o;
        return Objects.equals(this.durationInSeconds, timerTerminationConditionTemplate.durationInSeconds)
                && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(durationInSeconds, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TimerTerminationConditionTemplate {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    durationInSeconds: ").append(toIndentedString(durationInSeconds)).append("\n");
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
