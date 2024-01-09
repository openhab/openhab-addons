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
package org.openhab.binding.rfxcom.internal.messages;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author James Hewitt-Thomas - Initial contribution
 */
@NonNullByDefault
public class RFXComInvalidMessageTypeTest {

    @Test
    public void testMessage() {
        byte[] message = HexUtils.hexToBytes("07CC01271356ECC0");
        assertThrows(RFXComUnsupportedValueException.class,
                () -> RFXComMessageFactoryImpl.INSTANCE.createMessage(message));
    }
}
