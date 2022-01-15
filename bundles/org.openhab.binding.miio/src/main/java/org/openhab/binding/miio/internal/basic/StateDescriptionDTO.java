/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal.basic;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Mapping properties from json for state descriptions
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class StateDescriptionDTO {

    @SerializedName("minimum")
    @Expose
    @Nullable
    private BigDecimal minimum;
    @SerializedName("maximum")
    @Expose
    @Nullable
    private BigDecimal maximum;
    @SerializedName("step")
    @Expose
    @Nullable
    private BigDecimal step;
    @SerializedName("pattern")
    @Expose
    @Nullable
    private String pattern;
    @SerializedName("readOnly")
    @Expose
    @Nullable
    private Boolean readOnly;
    @SerializedName("options")
    @Expose
    @Nullable
    public List<OptionsValueListDTO> options = null;

    @Nullable
    public BigDecimal getMinimum() {
        return minimum;
    }

    public void setMinimum(@Nullable BigDecimal minimum) {
        this.minimum = minimum;
    }

    @Nullable
    public BigDecimal getMaximum() {
        return maximum;
    }

    public void setMaximum(@Nullable BigDecimal maximum) {
        this.maximum = maximum;
    }

    @Nullable
    public BigDecimal getStep() {
        return step;
    }

    public void setStep(@Nullable BigDecimal step) {
        this.step = step;
    }

    @Nullable
    public String getPattern() {
        return pattern;
    }

    public void setPattern(@Nullable String pattern) {
        this.pattern = pattern;
    }

    @Nullable
    public Boolean getReadOnly() {
        return readOnly;
    }

    public void setReadOnly(@Nullable Boolean readOnly) {
        this.readOnly = readOnly;
    }

    @Nullable
    public List<OptionsValueListDTO> getOptions() {
        return options;
    }

    public void setOptions(@Nullable List<OptionsValueListDTO> options) {
        this.options = options;
    }
}
