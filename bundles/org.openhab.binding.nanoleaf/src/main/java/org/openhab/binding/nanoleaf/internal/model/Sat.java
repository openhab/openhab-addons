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
package org.openhab.binding.nanoleaf.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents saturation setting of the light panels
 *
 * @author Martin Raepple - Initial contribution
 */
@NonNullByDefault
public class Sat implements IntegerState {

    private int value;
    private @Nullable Integer max;
    private @Nullable Integer min;

    @Override
    public int getValue() {
        return value;
    }

    @Override
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
}
