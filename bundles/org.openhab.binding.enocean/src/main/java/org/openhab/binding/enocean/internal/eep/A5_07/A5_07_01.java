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
package org.openhab.binding.enocean.internal.eep.A5_07;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class A5_07_01 extends A5_07 {

    private static final int PIR_OFF = 0x7f;

    public A5_07_01(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected State getIllumination() {
        return UnDefType.UNDEF;
    }

    @Override
    protected State getMotion() {
        return OnOffType.from(PIR_OFF < getDB1Value());
    }

    @Override
    protected State getSupplyVoltage() {
        if (!getBit(getDB0Value(), 0)) {
            return UnDefType.UNDEF;
        }

        return getSupplyVoltage(getDB3Value());
    }
}
