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
package org.openhab.binding.awtrixng.internal.dto;

/**
 * The frame published on {@code <prefix>/state/screen}: pixels are row by row from the top left, each one a decimal
 * {@code 0xRRGGBB} number.
 *
 * @author Keith T. Garner - Initial contribution
 */
public class ScreenFrame {
    public int width;
    public int height;
    // Without pixels the frame is empty, and not a null that would fail the rendering
    public int[] pixels = new int[0];
}
