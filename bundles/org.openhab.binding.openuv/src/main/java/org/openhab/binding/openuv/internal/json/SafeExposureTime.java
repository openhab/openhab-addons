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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * Wrapper type around values reported by OpenUV safe exposure time.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class SafeExposureTime {
    private BigInteger st1 = BigInteger.ZERO;
    private BigInteger st2 = BigInteger.ZERO;
    private BigInteger st3 = BigInteger.ZERO;
    private BigInteger st4 = BigInteger.ZERO;
    private BigInteger st5 = BigInteger.ZERO;
    private BigInteger st6 = BigInteger.ZERO;

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
            default:
                result = st6;
        }
        return result != BigInteger.ZERO ? new QuantityType<>(result, SmartHomeUnits.MINUTE) : UnDefType.NULL;
    }
}
