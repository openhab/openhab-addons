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
package org.openhab.binding.echonetlite.internal.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openhab.binding.echonetlite.internal.LangUtil.constantToVariable;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
class LangUtilTest {

    @Test
    void shouldConvertConstantToVariable() {
        assertEquals("operationStatus", constantToVariable("OPERATION_STATUS"));
    }
}
