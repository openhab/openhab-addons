/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal.api.responses._v3;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.api._v3.ShadePositionV3;

/**
 * Shade SSE event object as supplied an HD PowerView hub of Generation 3
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class SseShadeV3 {
    public @Nullable String evt;
    public @Nullable String isoDate;
    public int id;
    public @Nullable ShadePositionV3 currentPositions;
}
