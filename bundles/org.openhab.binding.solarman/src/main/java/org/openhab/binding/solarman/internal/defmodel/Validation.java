/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.solarman.internal.defmodel;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Catalin Sanda - Initial contribution
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@NonNullByDefault
public class Validation {
    private @Nullable Integer max;
    @JsonProperty("invalidate_all")
    private Object invalidateAll = new Object();
    private @Nullable Integer min;

    public @Nullable Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public Object getInvalidateAll() {
        return invalidateAll;
    }

    public void setInvalidateAll(Object invalidateAll) {
        this.invalidateAll = invalidateAll;
    }

    public @Nullable Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }
}
