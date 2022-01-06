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
package org.openhab.binding.loxone.internal.controls;

import org.openhab.binding.loxone.internal.types.LxUuid;

/**
 * A dimmer type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a dimmer control is:
 * <ul>
 * <li>a virtual input of dimmer type
 * </ul>
 *
 * @author Stephan Brunner - initial contribution
 *
 */
class LxControlDimmer extends LxControlEIBDimmer {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlDimmer(uuid);
        }

        @Override
        String getType() {
            return "dimmer";
        }
    }

    /**
     * States additionally to EIBDimmer
     */
    private static final String STATE_MIN = "min";
    private static final String STATE_MAX = "max";
    private static final String STATE_STEP = "step";

    private LxControlDimmer(LxUuid uuid) {
        super(uuid);
    }

    @Override
    Double getMin() {
        return getStateDoubleValue(STATE_MIN);
    }

    @Override
    Double getMax() {
        return getStateDoubleValue(STATE_MAX);
    }

    @Override
    Double getStep() {
        return getStateDoubleValue(STATE_STEP);
    }
}
