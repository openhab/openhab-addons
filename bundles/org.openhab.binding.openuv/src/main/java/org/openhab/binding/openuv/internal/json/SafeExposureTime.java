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
package org.openhab.binding.openuv.internal.json;

import java.math.BigInteger;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * Wrapper type around values reported by OpenUV safe exposure time.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class SafeExposureTime {
    public @Nullable BigInteger st1;
    public @Nullable BigInteger st2;
    public @Nullable BigInteger st3;
    public @Nullable BigInteger st4;
    public @Nullable BigInteger st5;
    public @Nullable BigInteger st6;

    public State getSafeExposure(int index) {
        BigInteger result;
        switch (index) {
            case 1:
                result = st1;
                break;
            case 2:
                result = st2;
                break;
            case 3:
                result = st3;
                break;
            case 4:
                result = st4;
                break;
            case 5:
                result = st5;
                break;
            case 6:
                result = st6;
                break;
            default:
                result = null;
        }
        return (result != null) ? new QuantityType<>(result, SmartHomeUnits.MINUTE) : UnDefType.NULL;
    }
}
