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
package org.openhab.binding.fineoffsetweatherstation.internal.domain.response;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.fineoffsetweatherstation.internal.domain.Measurand;
import org.openhab.core.types.State;

/**
 * A certain measured value.
 *
 * @author Andreas Berger - Initial contribution
 */
@NonNullByDefault
public class MeasuredValue {
    private final Measurand measurand;
    private final State state;

    public MeasuredValue(Measurand measurand, State state) {
        this.measurand = measurand;
        this.state = state;
    }

    public Measurand getMeasurand() {
        return measurand;
    }

    public State getState() {
        return state;
    }
}
