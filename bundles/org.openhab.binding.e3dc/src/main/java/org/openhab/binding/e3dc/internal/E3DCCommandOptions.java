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
package org.openhab.binding.e3dc.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.CommandOption;

/**
 * The energies the user sets are limited by the battery the device reports, so their choices follow the
 * reported values rather than a fixed list: the reserve is bounded by the maximum the device accepts and the
 * manual charge by the energy the battery holds.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class E3DCCommandOptions {

    /** The reserve is a figure the user picks, so it steps in whole kilowatt hours. */
    private static final int RESERVE_STEP_WH = 1000;

    /** The manual charge is started for a rough amount of energy, so it steps in half kilowatt hours. */
    private static final int MANUAL_CHARGE_STEP_WH = 500;

    private E3DCCommandOptions() {
    }

    /** The reserve values from zero up to the energy the device accepts, in whole kilowatt hours. */
    public static List<CommandOption> reserve(double maxWh) {
        List<CommandOption> options = new ArrayList<>();
        for (int wh = 0; wh <= maxWh; wh += RESERVE_STEP_WH) {
            options.add(option(wh / 1000 + " kWh"));
        }
        return options;
    }

    /** The manual charge values from half a kilowatt hour up to the energy the battery holds. */
    public static List<CommandOption> manualCharge(double maxWh) {
        List<CommandOption> options = new ArrayList<>();
        for (int wh = MANUAL_CHARGE_STEP_WH; wh <= maxWh; wh += MANUAL_CHARGE_STEP_WH) {
            options.add(option(wh + " Wh"));
        }
        return options;
    }

    private static CommandOption option(String value) {
        return new CommandOption(value, value);
    }
}
