/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.nanoleaf.internal.model;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents global orientation settings of the light panels
 *
 * @author Martin Raepple - Initial contribution
 */
@NonNullByDefault
public class GlobalOrientation {

    private int value;
    private @Nullable Integer max;
    private @Nullable Integer min;

    public GlobalOrientation() {
    }

    public GlobalOrientation(Integer min, Integer max, int value) {
        this.min = min;
        this.max = max;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public @Nullable Integer getMax() {
        return max;
    }

    public void setMax(Integer max) {
        this.max = max;
    }

    public @Nullable Integer getMin() {
        return min;
    }

    public void setMin(Integer min) {
        this.min = min;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GlobalOrientation go = (GlobalOrientation) o;
        return (value == go.getValue()) && (Objects.equals(min, go.getMin())) && (Objects.equals(max, go.getMax()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        Integer x = max;
        Integer i = min;
        result = prime * result + value;
        result = prime * result + ((x == null) ? 0 : x.hashCode());
        result = prime * result + ((i == null) ? 0 : i.hashCode());
        return result;
    }
}
