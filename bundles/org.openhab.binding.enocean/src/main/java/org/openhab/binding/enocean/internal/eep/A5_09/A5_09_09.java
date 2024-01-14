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
package org.openhab.binding.enocean.internal.eep.A5_09;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Zhivka Dimova - Initial contribution
 */
@NonNullByDefault
public class A5_09_09 extends A5_09 {

    public A5_09_09(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected double getScaledCO2Max() {
        return 2000;
    }

    @Override
    protected int getUnscaledCO2Value() {
        return getDB1Value();
    }
}
