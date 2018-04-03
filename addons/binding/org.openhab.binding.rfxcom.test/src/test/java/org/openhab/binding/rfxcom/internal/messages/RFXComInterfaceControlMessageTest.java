/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.messages;

import static org.junit.Assert.assertArrayEquals;
import static org.openhab.binding.rfxcom.internal.messages.RFXComInterfaceMessage.TransceiverType._433_92MHZ_TRANSCEIVER;

import org.eclipse.smarthome.core.util.HexUtils;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.rfxcom.internal.config.RFXComBridgeConfiguration;
import org.openhab.binding.rfxcom.internal.exceptions.RFXComException;

/**
 * Test for RFXCom-binding
 *
 * @author Mike Jagdis
 * @since 2.1.0
 */
public class RFXComInterfaceControlMessageTest {
    private RFXComBridgeConfiguration configuration = new RFXComBridgeConfiguration();

    @Before
    public void resetConfig() {
        configuration.transmitPower = -18;
        configuration.enableUndecoded = false;
        configuration.enableImagintronixOpus = false;
        configuration.enableByronSX = false;
        configuration.enableRSL = false;
        configuration.enableLighting4 = false;
        configuration.enableFineOffsetViking = false;
        configuration.enableRubicson = false;
        configuration.enableAEBlyss = false;

        configuration.enableBlindsT1T2T3T4 = false;
        configuration.enableBlindsT0 = false;
        configuration.enableProGuard = false;
//      configuration.enableFS20Packets = false;
        configuration.enableLaCrosse = false;
        configuration.enableHidekiUPM = false;
        configuration.enableADLightwaveRF = false;
        configuration.enableMertik = false;

        configuration.enableVisonic = false;
        configuration.enableATI = false;
        configuration.enableOregonScientific = false;
        configuration.enableMeiantech = false;
        configuration.enableHomeEasyEU = false;
        configuration.enableAC = false;
        configuration.enableARC = false;
        configuration.enableX10 = false;
    }

    private void testMessage(RFXComInterfaceMessage.TransceiverType transceiverType, RFXComBridgeConfiguration configuration, String data)
            throws RFXComException {

        assertArrayEquals(HexUtils.hexToBytes(data),
            new RFXComInterfaceControlMessage(transceiverType, configuration).decodeMessage());
    }

    @Test
    public void testUndecodedMessage() throws RFXComException {
        configuration.enableUndecoded = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530080000000000000");
    }

    @Test
    public void testImagintronixOpusMessage() throws RFXComException {
        configuration.enableImagintronixOpus = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530040000000000000");
    }

    @Test
    public void testByronSXMessage() throws RFXComException {
        configuration.enableByronSX = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530020000000000000");
    }

    @Test
    public void testRSLMessage() throws RFXComException {
        configuration.enableRSL = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530010000000000000");
    }

    @Test
    public void testLighting4Message() throws RFXComException {
        configuration.enableLighting4 = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530008000000000000");
    }

    @Test
    public void testFineOffsetVikingMessage() throws RFXComException {
        configuration.enableFineOffsetViking = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530004000000000000");
    }

    @Test
    public void testRubicsonMessage() throws RFXComException {
        configuration.enableRubicson = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530002000000000000");
    }

    @Test
    public void testAEBlyssMessage() throws RFXComException {
        configuration.enableAEBlyss = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530001000000000000");
    }

    @Test
    public void testBlindsT1T2T3T4Message() throws RFXComException {
        configuration.enableBlindsT1T2T3T4 = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530000800000000000");
    }

    @Test
    public void testBlindsT0Message() throws RFXComException {
        configuration.enableBlindsT0 = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530000400000000000");
    }

    @Test
    public void testProGuardMessage() throws RFXComException {
        configuration.enableProGuard = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530000200000000000");
    }

//  @Test
//  public void testFS20PacketsMessage() throws RFXComException {
//      configuration.enableFS20Packets = true;
//      testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530000100000000000");
//  }

    @Test
    public void testLaCrosseMessage() throws RFXComException {
        configuration.enableLaCrosse = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530000080000000000");
    }

    @Test
    public void testHidekiUPMMessage() throws RFXComException {
        configuration.enableHidekiUPM = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530000040000000000");
    }

    @Test
    public void testLightwaverRFMessage() throws RFXComException {
        configuration.enableADLightwaveRF = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530000020000000000");
    }

    @Test
    public void testMertikMessage() throws RFXComException {
        configuration.enableMertik = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530000010000000000");
    }

    @Test
    public void testVisionicMessage() throws RFXComException {
        configuration.enableVisonic = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530000008000000000");
    }

    @Test
    public void testATIMessage() throws RFXComException {
        configuration.enableATI = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530000004000000000");
    }

    @Test
    public void testOregonScientificMessage() throws RFXComException {
        configuration.enableOregonScientific = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530000002000000000");
    }

    @Test
    public void testMeiantechMessage() throws RFXComException {
        configuration.enableMeiantech = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530000001000000000");
    }

    @Test
    public void testHomeEasyEUMessage() throws RFXComException {
        configuration.enableHomeEasyEU = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530000000800000000");
    }

    @Test
    public void testACMessage() throws RFXComException {
        configuration.enableAC = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530000000400000000");
    }

    @Test
    public void testARCMessage() throws RFXComException {
        configuration.enableARC = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530000000200000000");
    }

    @Test
    public void testX10Message() throws RFXComException {
        configuration.enableX10 = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530000000100000000");
    }

    @Test
    public void testTransmitPower() throws RFXComException {
        configuration.transmitPower = 0;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203531200000000000000");
    }

    @Test
    public void testPerSDK() throws RFXComException {
        configuration.enableUndecoded = true;
        configuration.enableLaCrosse = true;
        configuration.enableOregonScientific = true;
        configuration.enableAC = true;
        configuration.enableARC = true;
        configuration.enableX10 = true;
        testMessage(_433_92MHZ_TRANSCEIVER, configuration, "0D00000203530080082700000000");
    }
}
