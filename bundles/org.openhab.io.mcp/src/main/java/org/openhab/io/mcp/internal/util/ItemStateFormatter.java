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
package org.openhab.io.mcp.internal.util;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Formats openHAB item states into human-readable strings for AI consumption.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ItemStateFormatter {

    /**
     * Format an item state for LLM consumption.
     */
    public static String formatState(State state) {
        if (state instanceof UnDefType) {
            return state == UnDefType.NULL ? "no value" : "undefined";
        }
        if (state instanceof HSBType hsb) {
            return "hue: " + formatDecimal(hsb.getHue().toBigDecimal()) + "\u00B0, saturation: "
                    + formatDecimal(hsb.getSaturation().toBigDecimal()) + "%, brightness: "
                    + formatDecimal(hsb.getBrightness().toBigDecimal()) + "%";
        }
        if (state instanceof PercentType pct) {
            return formatDecimal(pct.toBigDecimal()) + "%";
        }
        if (state instanceof DateTimeType dt) {
            return dt.getInstant().toString();
        }
        return state.toString();
    }

    private static String formatDecimal(BigDecimal value) {
        BigDecimal stripped = value.stripTrailingZeros();
        return stripped.scale() < 0 ? stripped.setScale(0).toPlainString() : stripped.toPlainString();
    }
}
