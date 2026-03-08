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
package org.openhab.binding.bluelink.internal.dto;

import static org.openhab.core.library.unit.ImperialUnits.MILE;
import static org.openhab.core.library.unit.SIUnits.METRE;

import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Driving distance with unit.
 *
 * @author Marcus Better - Initial contribution
 */
public record DrivingRange(double value, int unit) {

    public State getRange() {
        return switch (unit) {
            case 1 -> new QuantityType<>(value * 1000, METRE);
            case 2, 3 -> new QuantityType<>(value, MILE);
            default -> UnDefType.UNDEF;
        };
    }
}
