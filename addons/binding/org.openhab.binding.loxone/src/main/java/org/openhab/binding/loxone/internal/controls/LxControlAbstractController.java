/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.loxone.internal.controls;

import org.openhab.binding.loxone.internal.LxServerHandlerApi;
import org.openhab.binding.loxone.internal.core.LxCategory;
import org.openhab.binding.loxone.internal.core.LxContainer;
import org.openhab.binding.loxone.internal.core.LxJsonApp3.LxJsonControl;
import org.openhab.binding.loxone.internal.core.LxUuid;

/**
 * A class that represents controllers, which contain sub-controls, which are also {@link LxControl}
 * For example: (@link LxControlLightController} or {@link LxControlLightControllerV2}
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
abstract class LxControlAbstractController extends LxControl {
    /**
     * Create controller object.
     *
     * @param handlerApi thing handler object representing the Miniserver
     * @param uuid       controller's UUID
     * @param json       JSON describing the control as received from the Miniserver
     * @param room       room to which controller belongs
     * @param category   category to which controller belongs
     */
    LxControlAbstractController(LxServerHandlerApi handlerApi, LxUuid uuid, LxJsonControl json, LxContainer room,
            LxCategory category) {
        super(handlerApi, uuid, json, room, category);
    }

    /**
     * Update Miniserver's controller in runtime.
     *
     * @param json     JSON describing the control as received from the Miniserver
     * @param room     New room that this control belongs to
     * @param category New category that this control belongs to
     */
    @Override
    public void update(LxJsonControl json, LxContainer room, LxCategory category) {
        super.update(json, room, category);
        getSubControls().values().forEach(c -> c.uuid.setUpdate(false));
        if (json.subControls != null) {
            for (LxJsonControl subControl : json.subControls.values()) {
                // recursively create a subcontrol as a new control
                subControl.room = json.room;
                subControl.cat = json.cat;
                LxUuid uuid = new LxUuid(subControl.uuidAction);
                if (getSubControls().containsKey(uuid)) {
                    getSubControls().get(uuid).update(subControl, room, category);
                } else {
                    LxControl control = LxControlFactory.createControl(handlerApi, uuid, subControl, room, category);
                    if (control != null) {
                        getSubControls().put(control.uuid, control);
                    }
                }
            }
        }
        getSubControls().entrySet().removeIf(e -> !e.getValue().uuid.getUpdate());
    }
}
