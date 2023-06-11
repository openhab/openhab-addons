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
package org.openhab.binding.enocean.internal.eep.A5_08;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.enocean.internal.messages.ERP1Message;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class A5_08_01_FXBH extends A5_08 {

    public A5_08_01_FXBH(ERP1Message packet) {
        super(packet);
    }

    @Override
    protected double getScaledTemperatureMin() {
        return 0;
    }

    @Override
    protected double getScaledTemperatureMax() {
        return 1;
    }

    @Override
    protected double getScaledIlluminationMin() {
        return 0;
    }

    @Override
    protected double getScaledIlluminationMax() {
        return 2048;
    }
}
