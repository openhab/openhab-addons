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
package org.openhab.binding.loxone.internal.core;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.openhab.binding.loxone.internal.core.LxJsonApp3.LxJsonControl;

/**
 * A Color Picker V2 type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a color picker control covers:
 * <ul>
 * <li>Color (Hue/Saturation/Brightness)
 * </ul>
 *
 * @author Michael Mattan - initial contribution
 *
 */
public class LxControlColorPickerV2 extends LxControl {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
            return new LxControlColorPickerV2(client, uuid, json, room, category);
        }

        @Override
        String getType() {
            return TYPE_NAME;
        }
    }

    /**
     * Color state
     */
    public static final String STATE_COLOR = "color";

    private static final String TYPE_NAME = "colorpickerv2";

    LxControlColorPickerV2(LxWsClient client, LxUuid uuid, LxJsonControl json, LxContainer room, LxCategory category) {
        super(client, uuid, json, room, category);
    }

    public HSBType getColor() {
        String hsvColor = getStateTextValue(STATE_COLOR);
        HSBType color = new HSBType(hsvColor.substring(hsvColor.indexOf("(") + 1, hsvColor.length() - 1));
        return color;
    }

    public void setColor(HSBType hsb) {
        socketClient.sendAction(uuid, "hsv(" + hsb.toString() + ")");
    }

}
