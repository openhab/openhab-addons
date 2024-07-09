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
public class TimerTerminationCondition extends OverlayTerminationCondition {
    @SerializedName("durationInSeconds")
    private Integer durationInSeconds = null;

    @SerializedName("expiry")
    private OffsetDateTime expiry = null;

    @SerializedName("remainingTimeInSeconds")
    private Integer remainingTimeInSeconds = null;

    public TimerTerminationCondition durationInSeconds(Integer durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
        return this;
    }

    public Integer getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(Integer durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    public OffsetDateTime getExpiry() {
        return expiry;
    }

    public TimerTerminationCondition remainingTimeInSeconds(Integer remainingTimeInSeconds) {
        this.remainingTimeInSeconds = remainingTimeInSeconds;
        return this;
    }

    public Integer getRemainingTimeInSeconds() {
        return remainingTimeInSeconds;
    }

    public void setRemainingTimeInSeconds(Integer remainingTimeInSeconds) {
        this.remainingTimeInSeconds = remainingTimeInSeconds;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TimerTerminationCondition timerTerminationCondition = (TimerTerminationCondition) o;
        return Objects.equals(this.durationInSeconds, timerTerminationCondition.durationInSeconds)
                && Objects.equals(this.expiry, timerTerminationCondition.expiry)
                && Objects.equals(this.remainingTimeInSeconds, timerTerminationCondition.remainingTimeInSeconds)
                && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(durationInSeconds, expiry, remainingTimeInSeconds, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TimerTerminationCondition {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    durationInSeconds: ").append(toIndentedString(durationInSeconds)).append("\n");
        sb.append("    expiry: ").append(toIndentedString(expiry)).append("\n");
        sb.append("    remainingTimeInSeconds: ").append(toIndentedString(remainingTimeInSeconds)).append("\n");
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
