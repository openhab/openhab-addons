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

package org.openhab.binding.nanoleaf.internal.layout;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nanoleaf.internal.colors.NanoleafPanelColors;
import org.openhab.core.library.types.HSBType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores the state of the panels.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class LivePanelState implements PanelState {

    private static final Logger logger = LoggerFactory.getLogger(LivePanelState.class);
    private final NanoleafPanelColors panelColors;

    public LivePanelState(NanoleafPanelColors panelColors) {
        this.panelColors = panelColors;
    }

    @Override
    public HSBType getHSBForPanel(Integer panelId) {
        if (logger.isTraceEnabled()) {
            if (!panelColors.hasColor(panelId)) {
                logger.trace("Failed to get color for panel {}, falling back to black", panelId);
            }
        }

        return panelColors.getColor(panelId, HSBType.BLACK);
    }
}
