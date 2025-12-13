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
package org.openhab.binding.modbus.sungrow.internal.mapper;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Defines methods for mapping a given value (from the registers) to a corresponding String.
 *
 * @author Tim Scholand - Initial contribution
 */
@NonNullByDefault
public interface ToStringMapper {

    /**
     * Maps from the given {@link BigDecimal} to the {@link String} label.
     *
     * @param value the value from the register
     * @return the corresponding String value
     */
    String map(BigDecimal value);
}
