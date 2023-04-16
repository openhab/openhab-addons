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
package org.openhab.io.imperihome.internal.model.device;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.openhab.core.items.Item;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.State;
import org.openhab.io.imperihome.internal.model.param.DeviceParam;
import org.openhab.io.imperihome.internal.model.param.ParamType;
import org.openhab.io.imperihome.internal.util.StringUtils;

/**
 * RGB light device.
 *
 * @author Pepijn de Geus - Initial contribution
 */
public class RgbLightDevice extends AbstractEnergyLinkDevice {

    public RgbLightDevice(Item item) {
        super(DeviceType.RGB_LIGHT, item);
    }

    @Override
    public void stateUpdated(Item item, State newState) {
        super.stateUpdated(item, newState);

        boolean status = false;
        int level = 0;
        String color = "00000000";

        State state = item.getStateAs(HSBType.class);
        boolean isHsbState = state instanceof HSBType;

        // State can be of UndefType, even with the getStateAs above
        if (isHsbState) {
            HSBType hsbState = (HSBType) state;
            PercentType[] rgb = hsbState.toRGB();

            // Set state to ON if any channel > 0
            boolean isOn = rgb[0].doubleValue() > 0 || rgb[1].doubleValue() > 0 || rgb[2].doubleValue() > 0;
            if (isOn) {
                status = true;
            }

            // Build hex string
            int r = convertPercentToByte(rgb[0]) & 0xFF;
            int g = convertPercentToByte(rgb[1]) & 0xFF;
            int b = convertPercentToByte(rgb[2]) & 0xFF;
            color = (isOn ? "FF" : "00") + toHex(r) + toHex(g) + toHex(b);
        }

        State pState = item.getStateAs(PercentType.class);
        if (pState instanceof PercentType) {
            level = ((PercentType) pState).intValue();
            if (level > 0) {
                status = true;
            }
        }

        addParam(new DeviceParam(ParamType.STATUS, status ^ isInverted() ? "1" : "0"));
        addParam(new DeviceParam(ParamType.LEVEL, String.valueOf(level)));
        addParam(new DeviceParam(ParamType.DIMMABLE, "0"));
        addParam(new DeviceParam(ParamType.WHITE_CHANNEL, "1"));
        addParam(new DeviceParam(ParamType.COLOR, color));
    }

    private String toHex(int value) {
        String hex = Integer.toHexString(value);
        return StringUtils.padLeft(hex, 2, "0");
    }

    private int convertPercentToByte(PercentType percent) {
        return percent.toBigDecimal().multiply(BigDecimal.valueOf(255))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP).intValue();
    }
}
