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
package org.openhab.binding.lgthinq.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DateTimeType;

/**
 * The LGThinQWasherDryerHandlerTest test class.
 *
 * @author Nemer Daud - Initial contribution
 */
class LGThinQWasherDryerHandlerTest {

    @Test
    void updateDeviceChannels() {
        String time = String.format("%02.0f:%02.0f", 0.00, 0.0);
        DateTimeType dt = new DateTimeType(time);
    }
}
