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
package org.openhab.binding.nanoleaf.internal.layout;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Differentiates how shapes must be drawn
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public enum DrawingAlgorithm {
    NONE,
    SQUARE,
    TRIANGLE,
    HEXAGON,
    CORNER,
    LINE
}
