/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.fenecon.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GridPower} is a small helper class to convert the grid value.
 *
 * @author Philipp Schneider - Initial contribution
 */
@NonNullByDefault
public record GridPower(int sellTo, int buyFrom) {

    public static GridPower get(FeneconResponse response) {
        // Grid exchange power. Negative values for sell-to-grid; positive for buy-from-grid"
        Integer gridValue = Integer.valueOf(response.value());
        int selltoGridPower = 0;
        int buyFromGridPower = 0;
        if (gridValue < 0) {
            selltoGridPower = gridValue * -1;
        } else {
            buyFromGridPower = gridValue;
        }

        return new GridPower(selltoGridPower, buyFromGridPower);
    }
}
