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
 * Settigns used for layout.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class LayoutSettings {

    private final boolean drawLabels;
    private final boolean drawCorners;
    private final boolean drawOutline;
    private final boolean fillColor;

    public LayoutSettings(boolean drawLabels, boolean drawCorners, boolean drawOutline, boolean fillColor) {
        this.drawLabels = drawLabels;
        this.drawCorners = drawCorners;
        this.drawOutline = drawOutline;
        this.fillColor = fillColor;
    }

    public boolean shouldDrawLabels() {
        return drawLabels;
    }

    public boolean shouldDrawCorners() {
        return drawCorners;
    }

    public boolean shouldDrawOutline() {
        return drawOutline;
    }

    public boolean shouldFillWithColor() {
        return fillColor;
    }
}
