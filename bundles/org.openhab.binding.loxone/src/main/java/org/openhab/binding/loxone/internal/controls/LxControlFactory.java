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
package org.openhab.binding.loxone.internal.controls;

import java.util.HashMap;
import java.util.Map;

import org.openhab.binding.loxone.internal.controls.LxControl.LxControlInstance;
import org.openhab.binding.loxone.internal.types.LxUuid;

/**
 * A factory of controls of Loxone Miniserver.
 * It creates various types of control objects based on control type received from Miniserver.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlFactory {
    static {
        CONTROLS = new HashMap<>();
        add(new LxControlAlarm.Factory());
        add(new LxControlColorPickerV2.Factory());
        add(new LxControlDimmer.Factory());
        add(new LxControlEIBDimmer.Factory());
        add(new LxControlInfoOnlyAnalog.Factory());
        add(new LxControlInfoOnlyDigital.Factory());
        add(new LxControlIRoomControllerV2.Factory());
        add(new LxControlJalousie.Factory());
        add(new LxControlLeftRightAnalog.Factory());
        add(new LxControlLeftRightDigital.Factory());
        add(new LxControlLightController.Factory());
        add(new LxControlLightControllerV2.Factory());
        add(new LxControlMeter.Factory());
        add(new LxControlPushbutton.Factory());
        add(new LxControlRadio.Factory());
        add(new LxControlSauna.Factory());
        add(new LxControlSlider.Factory());
        add(new LxControlSwitch.Factory());
        add(new LxControlTextState.Factory());
        add(new LxControlTimedSwitch.Factory());
        add(new LxControlTracker.Factory());
        add(new LxControlUpDownAnalog.Factory());
        add(new LxControlUpDownDigital.Factory());
        add(new LxControlValueSelector.Factory());
        add(new LxControlWebPage.Factory());
    }

    private static final Map<String, LxControlInstance> CONTROLS;

    /**
     * Create a {@link LxControl} object for a control received from the Miniserver
     *
     * @param uuid UUID of the control to create
     * @param type control type
     * @return created control object or null if error
     */
    static LxControl createControl(LxUuid uuid, String type) {
        LxControlInstance control = CONTROLS.get(type.toLowerCase());
        if (control != null) {
            return control.create(uuid);
        }
        return null;
    }

    private static void add(LxControlInstance control) {
        CONTROLS.put(control.getType(), control);
    }
}
