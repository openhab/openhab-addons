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
public class FanSpeedFixed {
    private boolean settable;
    private Integer value;
    private Integer maxValue;
    private Integer minValue;
    private Integer stepValue;
    private String unit;

    public boolean isSettable() {
        return settable;
    }

    public Integer getValue() {
        return value;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public Integer getMinValue() {
        return minValue;
    }

    public Integer getStepValue() {
        return stepValue;
    }

    public String getUnit() {
        return unit;
    }
}
