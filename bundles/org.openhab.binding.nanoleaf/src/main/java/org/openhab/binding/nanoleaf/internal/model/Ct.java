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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Represents color temperature of the light panels
 *
 * @author Martin Raepple - Initial contribution
 */
@NonNullByDefault
public class Ct implements IntegerState {

    private int value;
    private @Nullable Integer max;
    private @Nullable Integer min;

    public @Nullable Integer getMax() {
        return max;
    }

    public void setMax(@Nullable Integer max) {
        this.max = max;
    }

    public @Nullable Integer getMin() {
        return min;
    }

    public void setMin(@Nullable Integer min) {
        this.min = min;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public void setValue(int value) {
        this.value = value;
    }
}
