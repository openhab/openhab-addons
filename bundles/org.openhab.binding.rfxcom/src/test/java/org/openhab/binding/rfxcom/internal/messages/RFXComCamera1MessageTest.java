/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.junit.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComMessageNotImplementedException;
import org.openhab.binding.rfxcom.internal.messages.RFXComBaseMessage.PacketType;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class RFXComCamera1MessageTest {

    @Test(expected = RFXComMessageNotImplementedException.class)
    public void checkNotImplemented() throws Exception {
        RFXComMessageFactory.createMessage(PacketType.CAMERA1);
    }
}
