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
package org.openhab.binding.velux.test;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.velux.internal.bridge.slip.SCgetHouseStatus;
import org.openhab.binding.velux.internal.bridge.slip.SCgetProduct;
import org.openhab.binding.velux.internal.bridge.slip.SCgetProductStatus;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI;
import org.openhab.binding.velux.internal.things.VeluxProduct;
import org.openhab.binding.velux.internal.things.VeluxProductPosition;
import org.openhab.binding.velux.internal.things.VeluxProductType.ActuatorType;

/**
 * Test unit for vane position.
 *
 * @author Andrew Fiddian-Green - Initial contribution.
 */
@NonNullByDefault
class VanePositionTests {

    private static final byte PRODUCT_INDEX = 6;
    private static final int MAIN_POSITION_TARGET = 0xC800;
    private static final int VANE_POSITION_TARGET = 0x634f;
    private static final ActuatorType ACTUATOR_TYPE = ActuatorType.BLIND_17;

    private static byte[] toByteArray(String input) {
        String[] data = input.split(" ");
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = Integer.decode("0x" + data[i]).byteValue();
        }
        return result;
    }

    /**
     * Test the 'supportsVanePosition()' method for different types of products.
     */
    @Test
    void testSupportsVanePosition() {
        VeluxProduct product = new VeluxProduct();
        assertFalse(product.supportsVanePosition());
        product.setActuatorType(ACTUATOR_TYPE);
        assertTrue(product.supportsVanePosition());
    }

    /*
     * Test the correct parsing of a 'GW_STATUS_REQUEST_NTF' notification packet.
     */
    @Test
    void test_GW_STATUS_REQUEST_NTF() {
        // initialise the test parameters
        final String packet = "00 D8 01 06 00 01 01 02 00 C8 00 03 63 4F 00 00 00 00 00 00 00 00 00 00 00 00 00"
                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
        final short command = VeluxKLFAPI.Command.GW_STATUS_REQUEST_NTF.getShort();

        // initialise the BCP
        SCgetProductStatus bcp = new SCgetProductStatus();
        bcp.setProductId(PRODUCT_INDEX);

        // set the packet response
        bcp.setResponse(command, toByteArray(packet), false);

        // check BCP status
        assertTrue(bcp.isCommunicationSuccessful());
        assertTrue(bcp.isCommunicationFinished());

        // post initialise the product
        VeluxProduct product = bcp.getProduct();
        product.setActuatorType(ACTUATOR_TYPE);

        // check positive assertions
        assertTrue(bcp.isCommunicationSuccessful());
        assertTrue(bcp.isCommunicationFinished());
        assertEquals(MAIN_POSITION_TARGET, product.getCurrentPosition());
        assertEquals(VeluxProductPosition.VPP_VELUX_IGNORE, product.getTarget());
        assertEquals(PRODUCT_INDEX, product.getBridgeProductIndex().toInt());
        assertEquals(ACTUATOR_TYPE, product.getActuatorType());
        assertEquals(VANE_POSITION_TARGET, product.getVanePosition());
        assertTrue(bcp.isCommunicationSuccessful());
    }

    /*
     * Test the correct parsing of a 'GW_GET_NODE_INFORMATION_NTF' notification packet.
     */
    @Test
    void test_GW_GET_NODE_INFORMATION_NTF() {
        // initialise the test parameters
        final String packet = "06 00 06 00 48 6F 62 62 79 6B 61 6D 65 72 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                + " 00 00 00 00 00 01 04 40 00 00 00 00 00 00 00 00 00 00 00 00 00 2D C8 00 C8 00 F7 FF F7 FF 00 00 F7 FF 00"
                + " 00 4F 00 4A EA 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
        final short command = VeluxKLFAPI.Command.GW_GET_NODE_INFORMATION_NTF.getShort();

        // initialise the BCP
        SCgetProduct bcp = new SCgetProduct();
        bcp.setProductId(PRODUCT_INDEX);

        // set the packet response
        bcp.setResponse(command, toByteArray(packet), false);

        // check BCP status
        assertTrue(bcp.isCommunicationSuccessful());
        assertTrue(bcp.isCommunicationFinished());

        // post initialise the product
        VeluxProduct product = bcp.getProduct();
        product.setActuatorType(ACTUATOR_TYPE);

        // check positive assertions
        assertEquals(MAIN_POSITION_TARGET, product.getCurrentPosition());
        assertEquals(MAIN_POSITION_TARGET, product.getTarget());
        assertEquals(PRODUCT_INDEX, product.getBridgeProductIndex().toInt());
        assertEquals(ACTUATOR_TYPE, product.getActuatorType());
        assertEquals(VeluxProductPosition.VPP_VELUX_UNKNOWN, product.getVanePosition());

        // check negative assertions
        assertNotEquals(VANE_POSITION_TARGET, product.getVanePosition());
    }

    /*
     * Test the correct parsing of a 'GW_NODE_STATE_POSITION_CHANGED_NTF' notification packet.
     */
    @Test
    void test_GW_NODE_STATE_POSITION_CHANGED_NTF() {
        // initialise the test parameters
        final String packet = "06 2D C8 00 C8 00 F7 FF F7 FF 00 00 F7 FF 00 00 4A E5 00 00";
        final short command = VeluxKLFAPI.Command.GW_NODE_STATE_POSITION_CHANGED_NTF.getShort();

        // initialise the BCP
        SCgetHouseStatus bcp = new SCgetHouseStatus();

        // set the packet response
        bcp.setResponse(command, toByteArray(packet), false);

        // check BCP status
        assertTrue(bcp.isCommunicationSuccessful());
        assertTrue(bcp.isCommunicationFinished());

        // post initialise the product
        VeluxProduct product = bcp.getProduct();
        product.setActuatorType(ACTUATOR_TYPE);

        // check positive assertions
        assertEquals(MAIN_POSITION_TARGET, product.getCurrentPosition());
        assertEquals(MAIN_POSITION_TARGET, product.getTarget());
        assertEquals(PRODUCT_INDEX, product.getBridgeProductIndex().toInt());
        assertEquals(ACTUATOR_TYPE, product.getActuatorType());
        assertEquals(VeluxProductPosition.VPP_VELUX_UNKNOWN, product.getVanePosition());

        // check negative assertions
        assertNotEquals(VANE_POSITION_TARGET, product.getVanePosition());
    }
}
