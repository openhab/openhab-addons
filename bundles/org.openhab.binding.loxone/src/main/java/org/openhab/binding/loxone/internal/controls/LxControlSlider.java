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
 * A slider type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, a slider control is a virtual input of slider type.
 * It behaves exactly the same as {@link LxControlUpDownAnalog}.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlSlider extends LxControlUpDownAnalog {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlSlider(uuid);
        }

        @Override
        String getType() {
            return "slider";
        }
    }

    private LxControlSlider(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxControlConfig config) {
        super.initialize(config, "Slider");
    }
}
