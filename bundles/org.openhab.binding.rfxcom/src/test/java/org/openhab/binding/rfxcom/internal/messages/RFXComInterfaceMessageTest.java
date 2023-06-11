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
package org.openhab.binding.rfxcom.internal.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;
import org.openhab.binding.rfxcom.internal.messages.RFXComInterfaceMessage.Commands;
import org.openhab.binding.rfxcom.internal.messages.RFXComInterfaceMessage.FirmwareType;
import org.openhab.binding.rfxcom.internal.messages.RFXComInterfaceMessage.SubType;
import org.openhab.binding.rfxcom.internal.messages.RFXComInterfaceMessage.TransceiverType;
import org.openhab.core.util.HexUtils;

/**
 * Test for RFXCom-binding
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class RFXComInterfaceMessageTest {
    private RFXComInterfaceMessage testMessage(String hexMsg, SubType subType, int seqNbr, Commands command)
            throws RFXComException {
        RFXComInterfaceMessage msg = (RFXComInterfaceMessage) RFXComMessageFactoryImpl.INSTANCE
                .createMessage(HexUtils.hexToBytes(hexMsg));
        assertEquals(subType, msg.subType, "SubType");
        assertEquals(seqNbr, (short) (msg.seqNbr & 0xFF), "Seq Number");
        assertEquals(command, msg.command, "Command");

        return msg;
    }

    @Test
    public void testWelcomeCopyRightMessage() throws RFXComException {
        RFXComInterfaceMessage msg = testMessage("1401070307436F7079726967687420524658434F4D", SubType.START_RECEIVER,
                3, Commands.START_RECEIVER);

        assertEquals("Copyright RFXCOM", msg.text, "text");
    }

    @Test
    public void testRespondOnUnknownMessage() throws RFXComException {
        testMessage("0D01FF190053E2000C2701020000", SubType.UNKNOWN_COMMAND, 25, Commands.UNSUPPORTED_COMMAND);
    }

    private void testStatus(String message, TransceiverType transceiverType, FirmwareType firmwareType,
            int firewareVersion) throws RFXComException {
        RFXComInterfaceMessage msg = testMessage(message, SubType.RESPONSE, 1, Commands.GET_STATUS);

        assertEquals(transceiverType, msg.transceiverType, "transceiverType");
        assertEquals(firmwareType, msg.firmwareType, "firmwareType");
        assertEquals(firewareVersion, msg.firmwareVersion, "firmwareVersion");
    }

    @Test
    public void testStatus_Rfxtrx443_Ext_250() throws RFXComException {
        testStatus("0D0100010253FA0400070001031C", TransceiverType._433_92MHZ_TRANSCEIVER, FirmwareType.EXT, 250);
    }

    @Test
    public void testStatus_Rfxtrx443_Ext_251() throws RFXComException {
        testStatus("0D0100010253FB0400070001031C", TransceiverType._433_92MHZ_TRANSCEIVER, FirmwareType.EXT, 251);
    }

    @Test
    public void testStatus_Rfxtrx443_Ext_1001() throws RFXComException {
        testStatus("140100010253010400070001031C03000000000000", TransceiverType._433_92MHZ_TRANSCEIVER,
                FirmwareType.EXT, 1001);
    }

    @Test
    public void testStatus_Rfxtrx443_Pro1_1044() throws RFXComException {
        testStatus("1401000102532C04000700010300055D0000000000", TransceiverType._433_92MHZ_TRANSCEIVER,
                FirmwareType.PRO1, 1044);
    }

    @Test
    public void testStatus_Rfxtrx443_Type1_95() throws RFXComException {
        testStatus("0D01000102535F0000270001031C", TransceiverType._433_92MHZ_TRANSCEIVER, FirmwareType.TYPE1, 95);
    }

    @Test
    public void testStatus_Rfxtrx443_Type1_1024() throws RFXComException {
        testStatus("140100010253180000270001031C01000000000000", TransceiverType._433_92MHZ_TRANSCEIVER,
                FirmwareType.TYPE1, 1024);
    }

    @Test
    public void testStatus_Rfxtrx443_Type2_195() throws RFXComException {
        testStatus("0D0100010253C30080270001031C", TransceiverType._433_92MHZ_TRANSCEIVER, FirmwareType.TYPE2, 195);
    }

    @Test
    public void testStatus_Rfxtrx443_Type2_1022() throws RFXComException {
        testStatus("140100010253160080270001031C02000000000000", TransceiverType._433_92MHZ_TRANSCEIVER,
                FirmwareType.TYPE2, 1022);
    }

    @Test
    public void testStatus_Rfxtrx443_Ext2_1012() throws RFXComException {
        testStatus("1401000102530C0800270001031C04524658434F4D", TransceiverType._433_92MHZ_TRANSCEIVER,
                FirmwareType.EXT2, 1012);
    }

    @Test
    public void testStatus_Unknown_Ext2_1012() throws RFXComException {
        testStatus("1401000102AA0C0800270001031C04524658434F4D", TransceiverType._UNKNOWN, FirmwareType.EXT2, 1012);
    }

    @Test
    public void testStatus_Rfxtrx433_Unknown_1012() throws RFXComException {
        testStatus("1401000102530C0800270001031CAA524658434F4D", TransceiverType._433_92MHZ_TRANSCEIVER,
                FirmwareType.UNKNOWN, 1012);
    }
}
