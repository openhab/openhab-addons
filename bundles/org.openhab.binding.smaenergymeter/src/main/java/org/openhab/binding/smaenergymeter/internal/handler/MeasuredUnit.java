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
package org.openhab.binding.smaenergymeter.internal.handler;

/**
 * The {@link MeasuredUnit} enum defines what scaling is used for data transmission
 *
 * @author Lars Repenning - Initial contribution
 */
public enum MeasuredUnit {
    NONE(1),
    W(10),
    VA(10),
    VAr(10),
    kWh(360000),
    kVAh(3600000),
    kVArh(3600000),
    A(1000),
    V(1000),
    DEG(1000),
    Hz(1000);

    private final int factor;

    private MeasuredUnit(int factor) {
        this.factor = factor;
    }

    public int getFactor() {
        return factor;
    }
}
