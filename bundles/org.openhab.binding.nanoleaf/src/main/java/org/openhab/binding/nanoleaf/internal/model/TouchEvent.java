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
package org.openhab.binding.nanoleaf.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Nanoleaf Panel TouchEvent provided by controller
 *
 *
 * JSON
 * {"events":
 * [
 * { "panelId":48111,
 * "gesture":0},
 * { "panelId":48112,
 * * "gesture":1}
 * ]
 * }
 *
 *
 * @author Stefan Höhn - Initial contribution
 */
@NonNullByDefault
public class TouchEvent {

    private String panelId = "";
    private int gesture = -1;

    public String getPanelId() {
        return panelId;
    }

    public void setPanelId(String panelId) {
        this.panelId = panelId;
    }

    public int getGesture() {
        return gesture;
    }

    public void setGesture(int gesture) {
        this.gesture = gesture;
    }
}
