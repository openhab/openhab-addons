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
package org.openhab.binding.nanoleaf.internal.colors;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nanoleaf.internal.handler.NanoleafPanelHandler;
import org.openhab.core.library.types.HSBType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores information about panels and their colors, while sending notifications to panels and controllers
 * about updated states.
 *
 * @author Jørgen Austvik - Initial contribution
 */
@NonNullByDefault
public class NanoleafPanelColors {

    private final Logger logger = LoggerFactory.getLogger(NanoleafPanelColors.class);

    // holds current color data per panel
    private final Map<Integer, HSBType> panelColors = new ConcurrentHashMap<>();
    private final Map<Integer, NanoleafPanelColorChangeListener> panelChangeListeners = new ConcurrentHashMap<>();
    private @Nullable NanoleafControllerColorChangeListener controllerListener;

    private boolean updatePanelColorNoController(Integer panelId, HSBType color) {
        boolean updatePanel = false;
        if (panelColors.containsKey(panelId)) {
            HSBType existingColor = panelColors.get(panelId);
            if (existingColor != null && !existingColor.equals(color)) {
                // Color change - update the panel thing
                updatePanel = true;
            }
        } else {
            // First time we see this panels color - update the panel thing
            updatePanel = true;
        }

        panelColors.put(panelId, color);

        if (updatePanel) {
            @Nullable
            NanoleafPanelColorChangeListener panelHandler = panelChangeListeners.get(panelId);
            if (panelHandler != null) {
                panelHandler.onPanelChangedColor(color);
            }
        }

        return updatePanel;
    }

    private void updatePanelColor(Integer panelId, HSBType color) {
        boolean updatePanel = updatePanelColorNoController(panelId, color);
        if (updatePanel) {
            notifyControllerListener();
        }
    }

    private void notifyControllerListener() {
        NanoleafControllerColorChangeListener privateControllerListener = controllerListener;
        if (privateControllerListener != null) {
            privateControllerListener.onPanelChangedColor();
        }
    }

    /**
     * Retrieves the color of the panel. Used by the panels to read their state.
     *
     * @param panelId The id of the panel
     * @return The color of the panel
     */
    public @Nullable HSBType getPanelColor(Integer panelId) {
        return panelColors.get(panelId);
    }

    /**
     * Called from panels to update the state.
     *
     * @param panelId The panel that received the update
     * @param color The new color of the panel
     */
    public void setPanelColor(Integer panelId, HSBType color) {
        updatePanelColor(panelId, color);
    }

    public void registerChangeListener(Integer panelId, NanoleafPanelHandler panelListener) {
        logger.trace("Adding color change listener for panel {}", panelId);
        panelChangeListeners.put(panelId, panelListener);
    }

    public void unregisterChangeListener(Integer panelId) {
        logger.trace("Removing color change listener for panel {}", panelId);
        panelChangeListeners.remove(panelId);
    }

    public void registerChangeListener(NanoleafControllerColorChangeListener controllerListener) {
        logger.trace("Setting color change listener for controller");
        this.controllerListener = controllerListener;
    }

    /**
     * Returns the color of a panel.
     * 
     * @param panelId The panel
     * @param defaultColor Default color if panel is missing color information
     * @return Color of the panel
     */
    public HSBType getColor(Integer panelId, HSBType defaultColor) {
        return panelColors.getOrDefault(panelId, defaultColor);
    }

    /**
     * Returns true if we have color information for the given panel.
     *
     * @param panelId The panel to check if has color
     * @return true if we have color information about the panel
     */
    public boolean hasColor(Integer panelId) {
        return panelColors.containsKey(panelId);
    }

    /**
     * Sets all panels to the same color. This will make controller repaint only once.
     * 
     * @param panelIds Panels to update
     * @param color The color for all panels
     */
    public void setMultiple(List<Integer> panelIds, HSBType color) {
        logger.debug("Setting all panels to color {}", color);
        boolean updatePanel = false;
        for (Integer panelId : panelIds) {
            updatePanel |= updatePanelColorNoController(panelId, color);
        }

        if (updatePanel) {
            notifyControllerListener();
        }
    }
}
