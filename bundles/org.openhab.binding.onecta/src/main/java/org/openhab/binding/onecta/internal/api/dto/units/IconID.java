/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.onecta.internal.api.dto.units;

/**
 * @author Alexander Drent - Initial contribution
 */
public class IconID {
    private boolean settable;
    private Float value;
    private Float maxValue;
    private Float minValue;
    private Float stepValue;
    private String unit;

    public boolean isSettable() {
        return settable;
    }

    public Float getValue() {
        return value;
    }

    public Float getMaxValue() {
        return maxValue;
    }

    public Float getMinValue() {
        return minValue;
    }

    public Float getStepValue() {
        return stepValue;
    }

    public String getUnit() {
        return unit;
    }
}
