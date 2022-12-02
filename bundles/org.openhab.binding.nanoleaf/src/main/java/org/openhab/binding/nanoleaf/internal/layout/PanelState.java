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

package org.openhab.binding.nanoleaf.internal.layout;

import static org.openhab.binding.nanoleaf.internal.NanoleafBindingConstants.CONFIG_PANEL_ID;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nanoleaf.internal.handler.NanoleafPanelHandler;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.thing.Thing;

/**
 * Stores the state of the panels.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class PanelState {

    private final Map<Integer, HSBType> panelStates = new HashMap<>();

    public PanelState(List<Thing> panels) {
        for (Thing panel : panels) {
            Integer panelId = Integer.valueOf(panel.getConfiguration().get(CONFIG_PANEL_ID).toString());
            NanoleafPanelHandler panelHandler = (NanoleafPanelHandler) panel.getHandler();
            if (panelHandler != null) {
                HSBType c = panelHandler.getColor();
                HSBType color = (c == null) ? HSBType.BLACK : c;
                panelStates.put(panelId, color);
            }
        }
    }

    public HSBType getHSBForPanel(Integer panelId) {
        return panelStates.getOrDefault(panelId, HSBType.BLACK);
    }
}
