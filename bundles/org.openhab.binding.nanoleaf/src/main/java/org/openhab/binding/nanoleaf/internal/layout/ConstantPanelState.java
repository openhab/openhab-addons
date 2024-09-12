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
import org.openhab.core.library.types.HSBType;

/**
 * Always returns the same color for all panels.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class ConstantPanelState implements PanelState {
    private final HSBType color;

    public ConstantPanelState(HSBType color) {
        this.color = color;
    }

    @Override
    public HSBType getHSBForPanel(Integer panelId) {
        return color;
    }
}
