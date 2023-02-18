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
package org.openhab.binding.hdpowerview.internal.dto.requests;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.hdpowerview.internal.dto.ShadePosition;

/**
 * A request to set the position of a shade
 *
 * @author Andy Lintner - Initial contribution
 */
@NonNullByDefault
public class ShadeMove {

    public ShadePositions shade;

    public ShadeMove(ShadePosition position) {
        this.shade = new ShadePositions(position);
    }
}
