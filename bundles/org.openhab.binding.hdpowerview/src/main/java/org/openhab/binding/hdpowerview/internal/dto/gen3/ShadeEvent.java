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
package org.openhab.binding.hdpowerview.internal.dto.gen3;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * DTO for a shade SSE event object as supplied an HD PowerView Generation 3 Gateway.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class ShadeEvent {
    private int id;
    private @NonNullByDefault({}) ShadePosition currentPositions;

    public ShadePosition getCurrentPositions() {
        return currentPositions;
    }

    public int getId() {
        return id;
    }
}
