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
package org.openhab.binding.loxone.internal.controls;

import org.openhab.binding.loxone.internal.types.LxUuid;

/**
 * A LeftRightAnalog type of control on Loxone Miniserver.
 * <p>
 * According to Loxone API documentation, LeftRightAnalog control is a virtual input that is analog and has an input
 * type
 * up-down buttons. The analog buttons are simulated as a single analog number value. This control behaves exactly the
 * same as {@link LxControlUpDownAnalog} but has a different name.
 *
 * @author Pawel Pieczul - initial contribution
 *
 */
class LxControlLeftRightAnalog extends LxControlUpDownAnalog {

    static class Factory extends LxControlInstance {
        @Override
        LxControl create(LxUuid uuid) {
            return new LxControlLeftRightAnalog(uuid);
        }

        @Override
        String getType() {
            return "leftrightanalog";
        }
    }

    private LxControlLeftRightAnalog(LxUuid uuid) {
        super(uuid);
    }

    @Override
    public void initialize(LxControlConfig config) {
        super.initialize(config, "Left/Right Analog");
    }
}
