/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openuv.json;

import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;

/**
 * Wrapper type around values reported by OpenUV safe exposure time.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class OpenUVSafeExposureTime {

    private int st1;
    private int st2;
    private int st3;
    private int st4;
    private int st5;
    private int st6;

    public QuantityType<?> getSafeExposure(int index) {
        int result;
        switch (index) {
            case 1:
                result = st1;
            case 2:
                result = st2;
            case 3:
                result = st3;
            case 4:
                result = st4;
            case 5:
                result = st5;
            default:
                result = st6;
        }
        return new QuantityType<>(result, SmartHomeUnits.MINUTE);
    }
}
