/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import javax.xml.bind.DatatypeConverter;

import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComUnsupportedValueException;

/**
 * Test for RFXCom-binding
 *
 * @author James Hewitt-Thomas
 * @since 1.9.0
 */
public class RFXComInvalidMessageTypeTest {

    @Test(expected = RFXComUnsupportedValueException.class)
    public void testMessage() throws RFXComException {
        byte[] message = DatatypeConverter.parseHexBinary("07CC01271356ECC0");
        final RFXComMessage msg = RFXComMessageFactory.createMessage(message);
    }
}
