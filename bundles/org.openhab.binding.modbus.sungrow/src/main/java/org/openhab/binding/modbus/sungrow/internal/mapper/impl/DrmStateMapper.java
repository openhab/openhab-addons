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
package org.openhab.binding.modbus.sungrow.internal.mapper.impl;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.sungrow.internal.mapper.ToStringMapper;

/**
 * This mapper implements {@link ToStringMapper} and maps the integer codes of the sungrow modbus register to the human
 * readable DRM states.
 *
 * @author Tim Scholand - Initial contribution
 */
@NonNullByDefault
public class DrmStateMapper implements ToStringMapper {

    private static final DrmStateMapper INSTANCE = new DrmStateMapper();

    /**
     * @return a singleton instance of the mapper
     */
    public static DrmStateMapper instance() {
        return INSTANCE;
    }

    private DrmStateMapper() {
        // use instance()
    }

    @Override
    public String map(BigDecimal value) {
        return switch (value.intValue()) {
            // not sure, if the mapping is correct
            case 1 -> "DRM0";
            case 2 -> "DRM1";
            case 3 -> "DRM2";
            case 4 -> "DRM3";
            case 5 -> "DRM4";
            case 6 -> "DRM5";
            case 7 -> "DRM6";
            case 8 -> "DRM7";
            case 9 -> "DRM8";
            default -> "INVALID: " + value.toPlainString();
        };
    }
}
