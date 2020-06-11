/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.util.HexUtils;
import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * Test for RFXCom-binding
 *
 * @author James Hewitt-Thomas - Initial contribution
 */
@NonNullByDefault
public class RFXComInvalidMessageTypeTest {

    @Test(expected = RFXComUnsupportedValueException.class)
    public void testMessage() throws RFXComException {
        byte[] message = HexUtils.hexToBytes("07CC01271356ECC0");
        RFXComMessageFactory.createMessage(message);
    }
}
