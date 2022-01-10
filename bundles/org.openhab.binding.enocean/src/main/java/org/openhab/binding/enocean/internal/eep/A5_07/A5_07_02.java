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
package org.openhab.binding.enocean.internal.eep.A5_07;

import org.openhab.binding.enocean.internal.messages.ERP1Message;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class A5_07_02 extends A5_07 {

    public A5_07_02(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected State getIllumination() {
        return UnDefType.UNDEF;
    }

    @Override
    protected State getMotion() {
        return getBit(getDB_0Value(), 7) ? OnOffType.ON : OnOffType.OFF;
    }

    @Override
    protected State getSupplyVoltage() {
        return getSupplyVoltage(getDB_3Value());
    }
}
