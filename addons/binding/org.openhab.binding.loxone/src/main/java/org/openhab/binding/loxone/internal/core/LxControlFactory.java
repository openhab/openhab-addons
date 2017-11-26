/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.loxone.internal.core;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.loxone.internal.core.LxJsonApp3.LxJsonControl;

/**
 * A factory of controls of Loxone Miniserver.
 * It creates various types of control objects based on control type received from Miniserver.
 *
 * @author Pawel Pieczul
 *
 */
class LxControlFactory {
    static {
        controls = new HashMap<>();
        add(new LxControlDimmer());
        add(new LxControlInfoOnlyAnalog());
        add(new LxControlInfoOnlyDigital());
        add(new LxControlJalousie());
        add(new LxControlLightController());
        add(new LxControlLightControllerV2());
        add(new LxControlPushbutton());
        add(new LxControlRadio());
        add(new LxControlSwitch());
        add(new LxControlTextState());
        add(new LxControlTimedSwitch());
    }

    private static Map<String, LxControl> controls;

    /**
     * Create a {@link LxControl} object for a control received from the Miniserver
     *
     * @param client
     *            websocket client to facilitate communication with Miniserver
     * @param uuid
     *            UUID of the control to be created
     * @param json
     *            JSON describing the control as received from the Miniserver
     * @param room
     *            Room that this control belongs to
     * @param category
     *            Category that this control belongs to
     * @return
     *         created control object or null if error
     */
    static LxControl createControl(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room,
            LxCategory category) {
        if (json == null || json.type == null || json.name == null) {
            return null;
        }
        String type = json.type.toLowerCase();
        LxControl control = controls.get(type);
        if (control != null) {
            return control.create(client, uuid, json, room, category);
        }
        return null;
    }

    private static void add(LxControl control) {
        controls.put(control.getTypeName(), control);
    }
}
