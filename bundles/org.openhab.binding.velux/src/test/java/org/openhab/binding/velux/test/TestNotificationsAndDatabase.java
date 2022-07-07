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
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openhab.binding.velux.internal.bridge.slip.SCgetHouseStatus;
import org.openhab.binding.velux.internal.bridge.slip.SCgetProduct;
import org.openhab.binding.velux.internal.bridge.slip.SCgetProductStatus;
import org.openhab.binding.velux.internal.bridge.slip.SCrunProductCommand;
import org.openhab.binding.velux.internal.things.VeluxExistingProducts;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI;
import org.openhab.binding.velux.internal.things.VeluxProduct;
import org.openhab.binding.velux.internal.things.VeluxProduct.ProductBridgeIndex;
import org.openhab.binding.velux.internal.things.VeluxProductName;
import org.openhab.binding.velux.internal.things.VeluxProductPosition;
import org.openhab.binding.velux.internal.things.VeluxProductType.ActuatorType;

/**
 * JUnit test suite to check the proper parsing of actuator notification packets, and to confirm that the existing
 * products database is working correctly.
 *
 * @author Andrew Fiddian-Green - Initial contribution.
 */
@NonNullByDefault
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestNotificationsAndDatabase {
    // validation parameters
    private static final byte PRODUCT_INDEX_SOMFY = 6;
    private static final byte PRODUCT_INDEX_VELUX = 0;
    private static final int MAIN_POSITION_SOMFY = 0xC800;
    private static final int MAIN_POSITION_VELUX = 0x4600;
    private static final int VANE_POSITION_SOMFY = 0x634f;
    private static final int TARGET_POSITION_SOMFY = 0xB800;
    private static final Integer STATE_SOMFY = 45;

    private static final int UNKNOWN_POSITION = VeluxProductPosition.VPP_VELUX_UNKNOWN;
    private static final int IGNORE_POSITION = VeluxProductPosition.VPP_VELUX_IGNORE;
    private static final int STATE_DONE = VeluxProduct.State.DONE.value;

    private static final ActuatorType ACTUATOR_TYPE_SOMFY = ActuatorType.BLIND_17;
    private static final ActuatorType ACTUATOR_TYPE_VELUX = ActuatorType.WINDOW_4_1;
    private static final ActuatorType ACTUATOR_TYPE_UNDEF = ActuatorType.UNDEFTYPE;

    // existing products database
    private static final VeluxExistingProducts EXISTING_PRODUCTS = new VeluxExistingProducts();

    private static byte[] toByteArray(String input) {
        String[] data = input.split(" ");
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = Integer.decode("0x" + data[i]).byteValue();
        }
        return result;
    }

    private VeluxExistingProducts getExistingProducts() {
        return EXISTING_PRODUCTS;
    }

    /**
     * Confirm the existing products database is initialised.
     */
    @Test
    @Order(1)
    public void testInitialized() {
        assertEquals(0, getExistingProducts().getNoMembers());
    }

    /**
     * Test the 'supportsVanePosition()' method for two types of products.
     */
    @Test
    @Order(2)
    public void testSupportsVanePosition() {
        VeluxProduct product = new VeluxProduct();
        assertFalse(product.supportsVanePosition());
        product.setActuatorType(ACTUATOR_TYPE_SOMFY);
        assertTrue(product.supportsVanePosition());
    }

    /**
     * Test the SCgetProduct command by checking for the correct parsing of a 'GW_GET_NODE_INFORMATION_NTF' notification
     * packet. Note: this packet is from a Somfy roller shutter with main and vane position.
     */
    @Test
    @Order(3)
    public void testSCgetProduct() {
        // initialise the test parameters
        final String packet = "06 00 06 00 48 6F 62 62 79 6B 61 6D 65 72 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                + " 00 00 00 00 00 01 04 40 00 00 00 00 00 00 00 00 00 00 00 00 00 2D C8 00 C8 00 F7 FF F7 FF 00 00 F7 FF 00"
                + " 00 4F 00 4A EA 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
        final short command = VeluxKLFAPI.Command.GW_GET_NODE_INFORMATION_NTF.getShort();

        // initialise the BCP
        SCgetProduct bcp = new SCgetProduct();
        bcp.setProductId(PRODUCT_INDEX_SOMFY);

        // set the packet response
        bcp.setResponse(command, toByteArray(packet), false);

        // check BCP status
        assertTrue(bcp.isCommunicationSuccessful());
        assertTrue(bcp.isCommunicationFinished());

        // initialise the product
        VeluxProduct product = bcp.getProduct();

        // check positive assertions
        assertEquals(STATE_SOMFY, product.getState());
        assertEquals(MAIN_POSITION_SOMFY, product.getCurrentPosition());
        assertEquals(MAIN_POSITION_SOMFY, product.getTarget());
        assertEquals(PRODUCT_INDEX_SOMFY, product.getBridgeProductIndex().toInt());
        assertEquals(ACTUATOR_TYPE_SOMFY, product.getActuatorType());
        assertEquals(UNKNOWN_POSITION, product.getVanePosition());
        assertNull(product.getFunctionalParameters());
        assertTrue(product.supportsVanePosition());
        assertTrue(product.isSomfyProduct());

        // check negative assertions
        assertNotEquals(STATE_DONE, product.getState());
        assertNotEquals(VANE_POSITION_SOMFY, product.getVanePosition());

        // register in existing products database
        VeluxExistingProducts existingProducts = getExistingProducts();
        assertTrue(existingProducts.register(product));
        assertTrue(existingProducts.isRegistered(product));
        assertTrue(existingProducts.isRegistered(product.getBridgeProductIndex()));
        assertEquals(1, existingProducts.getNoMembers());

        // confirm that a dummy product is NOT in the database
        assertFalse(existingProducts.isRegistered(new ProductBridgeIndex(99)));

        // check dirty flag
        assertTrue(existingProducts.isDirty());
        existingProducts.resetDirtyFlag();
        assertFalse(existingProducts.isDirty());

        // re-registering the same product should return false
        assertFalse(existingProducts.register(product));

        // updating again with the same data should NOT set the dirty flag
        assertTrue(existingProducts.update(product));
        assertFalse(existingProducts.isDirty());

        // check that the product in the database is indeed the one just created
        VeluxProduct existing = existingProducts.get(new ProductBridgeIndex(PRODUCT_INDEX_SOMFY));
        assertEquals(product, existing);
        assertEquals(1, existingProducts.getNoMembers());
        assertTrue(existingProducts.isRegistered(product.getBridgeProductIndex()));
    }

    /**
     * Confirm that the product in the existing database has the same values as the product created and added in test 3.
     */
    @Test
    @Order(4)
    public void testExistingUnknownVanePosition() {
        VeluxExistingProducts existingProducts = getExistingProducts();
        VeluxProduct product = existingProducts.get(new ProductBridgeIndex(PRODUCT_INDEX_SOMFY));

        // confirm the product details
        assertEquals(STATE_DONE, product.getState());
        assertEquals(MAIN_POSITION_SOMFY, product.getCurrentPosition());
        assertEquals(MAIN_POSITION_SOMFY, product.getTarget());
        assertEquals(PRODUCT_INDEX_SOMFY, product.getBridgeProductIndex().toInt());
        assertEquals(ACTUATOR_TYPE_SOMFY, product.getActuatorType());
        assertEquals(UNKNOWN_POSITION, product.getVanePosition());
        assertNull(product.getFunctionalParameters());
    }

    /**
     * Test the SCgetProductStatus command by checking for the correct parsing of a 'GW_STATUS_REQUEST_NTF' notification
     * packet. Note: this packet is from a Somfy roller shutter with main and vane position.
     */
    @Test
    @Order(5)
    public void testSCgetProductStatus() {
        // initialise the test parameters
        final String packet = "00 D8 01 06 00 01 01 02 00 C8 00 03 63 4F 00 00 00 00 00 00 00 00 00 00 00 00 00"
                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
        final short command = VeluxKLFAPI.Command.GW_STATUS_REQUEST_NTF.getShort();

        // initialise the BCP
        SCgetProductStatus bcp = new SCgetProductStatus();
        bcp.setProductId(PRODUCT_INDEX_SOMFY);

        // set the packet response
        bcp.setResponse(command, toByteArray(packet), false);

        // check BCP status
        assertTrue(bcp.isCommunicationSuccessful());
        assertTrue(bcp.isCommunicationFinished());

        // initialise the product
        VeluxProduct product = bcp.getProduct();

        // change actuator type
        assertEquals(ACTUATOR_TYPE_UNDEF, product.getActuatorType());
        product.setActuatorType(ACTUATOR_TYPE_SOMFY);

        // check positive assertions
        assertEquals(STATE_DONE, product.getState());
        assertEquals(MAIN_POSITION_SOMFY, product.getCurrentPosition());
        assertEquals(IGNORE_POSITION, product.getTarget());
        assertEquals(PRODUCT_INDEX_SOMFY, product.getBridgeProductIndex().toInt());
        assertEquals(ACTUATOR_TYPE_SOMFY, product.getActuatorType());
        assertEquals(VANE_POSITION_SOMFY, product.getVanePosition());
        assertNotNull(product.getFunctionalParameters());

        // test updating the existing product in the database
        VeluxExistingProducts existingProducts = getExistingProducts();
        assertTrue(existingProducts.update(product));
        assertTrue(existingProducts.isDirty());

        // updating again with the same data should NOT set the dirty flag
        existingProducts.resetDirtyFlag();
        assertTrue(existingProducts.update(product));
        assertFalse(existingProducts.isDirty());

        // clean up
        existingProducts.resetDirtyFlag();
        assertFalse(existingProducts.isDirty());
    }

    /**
     * Confirm that the product in the existing database has the same values as the product created and updated to the
     * database in test 5.
     */
    @Test
    @Order(6)
    public void testExistingValidVanePosition() {
        VeluxExistingProducts existingProducts = getExistingProducts();
        VeluxProduct product = existingProducts.get(new ProductBridgeIndex(PRODUCT_INDEX_SOMFY));

        // confirm the product details
        assertEquals(STATE_DONE, product.getState());
        assertEquals(MAIN_POSITION_SOMFY, product.getCurrentPosition());
        assertEquals(MAIN_POSITION_SOMFY, product.getTarget());
        assertEquals(PRODUCT_INDEX_SOMFY, product.getBridgeProductIndex().toInt());
        assertEquals(ACTUATOR_TYPE_SOMFY, product.getActuatorType());
        assertEquals(VANE_POSITION_SOMFY, product.getVanePosition());
        assertNotNull(product.getFunctionalParameters());
    }

    /**
     * Test the SCgetHouseStatus command by checking for the correct parsing of a 'GW_NODE_STATE_POSITION_CHANGED_NTF'
     * notification packet. Note: this packet is from a Somfy roller shutter with main and vane position.
     */
    @Test
    @Order(7)
    public void testSCgetHouseStatus() {
        // initialise the test parameters
        final String packet = "06 2D C8 00 B8 00 F7 FF F7 FF 00 00 F7 FF 00 00 4A E5 00 00";
        final short command = VeluxKLFAPI.Command.GW_NODE_STATE_POSITION_CHANGED_NTF.getShort();

        // initialise the BCP
        SCgetHouseStatus bcp = new SCgetHouseStatus();

        // set the packet response
        bcp.setResponse(command, toByteArray(packet), false);

        // check BCP status
        assertTrue(bcp.isCommunicationSuccessful());
        assertTrue(bcp.isCommunicationFinished());

        // initialise the product
        VeluxProduct product = bcp.getProduct();

        // change actuator type
        assertEquals(ACTUATOR_TYPE_UNDEF, product.getActuatorType());
        product.setActuatorType(ACTUATOR_TYPE_SOMFY);

        // check positive assertions
        assertEquals(STATE_SOMFY, product.getState());
        assertEquals(MAIN_POSITION_SOMFY, product.getCurrentPosition());
        assertEquals(TARGET_POSITION_SOMFY, product.getTarget());
        assertEquals(PRODUCT_INDEX_SOMFY, product.getBridgeProductIndex().toInt());
        assertEquals(ACTUATOR_TYPE_SOMFY, product.getActuatorType());
        assertEquals(UNKNOWN_POSITION, product.getVanePosition());
        assertNull(product.getFunctionalParameters());

        // check negative assertions
        assertNotEquals(VANE_POSITION_SOMFY, product.getVanePosition());

        // test updating the existing product in the database
        VeluxExistingProducts existingProducts = getExistingProducts();
        assertTrue(existingProducts.update(product));
        assertTrue(existingProducts.isDirty());
    }

    /**
     * Confirm that the product in the existing database has the same values as the product created and updated to the
     * database in test 7.
     */
    @Test
    @Order(8)
    public void testExistingValidVanePositionWithNewTargetValue() {
        VeluxExistingProducts existingProducts = getExistingProducts();
        VeluxProduct product = existingProducts.get(new ProductBridgeIndex(PRODUCT_INDEX_SOMFY));

        // confirm the product details
        assertEquals(STATE_DONE, product.getState());
        assertEquals(MAIN_POSITION_SOMFY, product.getCurrentPosition());
        assertEquals(TARGET_POSITION_SOMFY, product.getTarget());
        assertEquals(PRODUCT_INDEX_SOMFY, product.getBridgeProductIndex().toInt());
        assertEquals(ACTUATOR_TYPE_SOMFY, product.getActuatorType());
        assertEquals(VANE_POSITION_SOMFY, product.getVanePosition());
        assertNotNull(product.getFunctionalParameters());
    }

    /**
     * Test the SCgetProduct by checking for the correct parsing of a 'GW_GET_NODE_INFORMATION_NTF' notification packet.
     * Note: this packet is from a Velux roof window without vane position.
     */
    @Test
    @Order(9)
    public void testSCgetProductOnVelux() {
        // initialise the test parameters
        final String packet = "00 00 00 00 53 68 65 64 20 57 69 6E 64 6F 77 00 00 00 00 00 00 00 00 00 00 00 00 00"
                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                + " 00 00 00 00 00 00 00 00 01 01 01 03 07 00 01 16 56 24 5C 26 14 19 00 FC 05 46 00 46 00 F7 FF F7"
                + " FF F7 FF F7 FF 00 00 4F 05 B3 5F 01 D8 03 B2 1C 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
        final short command = VeluxKLFAPI.Command.GW_GET_NODE_INFORMATION_NTF.getShort();

        // initialise the BCP
        SCgetProduct bcp = new SCgetProduct();
        bcp.setProductId(PRODUCT_INDEX_VELUX);

        // set the packet response
        bcp.setResponse(command, toByteArray(packet), false);

        // check BCP status
        assertTrue(bcp.isCommunicationSuccessful());
        assertTrue(bcp.isCommunicationFinished());

        // initialise the product
        VeluxProduct product = bcp.getProduct();

        // check positive assertions
        assertEquals(STATE_DONE, product.getState());
        assertEquals(MAIN_POSITION_VELUX, product.getCurrentPosition());
        assertEquals(MAIN_POSITION_VELUX, product.getTarget());
        assertEquals(PRODUCT_INDEX_VELUX, product.getBridgeProductIndex().toInt());
        assertEquals(ACTUATOR_TYPE_VELUX, product.getActuatorType());
        assertEquals(UNKNOWN_POSITION, product.getVanePosition());
        assertNull(product.getFunctionalParameters());
        assertFalse(product.isSomfyProduct());

        // check negative assertions
        assertFalse(product.supportsVanePosition());
        assertNotEquals(VANE_POSITION_SOMFY, product.getVanePosition());

        // register in existing products database
        VeluxExistingProducts existingProducts = getExistingProducts();
        assertTrue(existingProducts.register(product));
        assertTrue(existingProducts.isRegistered(product));
        assertTrue(existingProducts.isRegistered(product.getBridgeProductIndex()));
        assertEquals(2, existingProducts.getNoMembers());

        // check that the product in the database is indeed the one just created
        VeluxProduct existing = existingProducts.get(new ProductBridgeIndex(PRODUCT_INDEX_VELUX));
        assertEquals(product, existing);
        assertTrue(existingProducts.isRegistered(product.getBridgeProductIndex()));
    }

    /**
     * Confirm that the modified products list is functioning.
     */
    @Test
    @Order(10)
    public void testModifiedList() {
        VeluxExistingProducts existingProducts = getExistingProducts();
        VeluxProduct[] modified = existingProducts.valuesOfModified();

        // confirm that the list contains two entries
        assertEquals(2, modified.length);

        // confirm the product details for the Somfy product
        VeluxProduct product = modified[0];
        assertEquals(STATE_DONE, product.getState());
        assertEquals(MAIN_POSITION_SOMFY, product.getCurrentPosition());
        assertEquals(TARGET_POSITION_SOMFY, product.getTarget());
        assertEquals(PRODUCT_INDEX_SOMFY, product.getBridgeProductIndex().toInt());
        assertEquals(ACTUATOR_TYPE_SOMFY, product.getActuatorType());
        assertEquals(VANE_POSITION_SOMFY, product.getVanePosition());
        assertTrue(product.isSomfyProduct());
        assertNotNull(product.getFunctionalParameters());

        // confirm the product details for the Velux product
        product = modified[1];
        assertEquals(STATE_DONE, product.getState());
        assertEquals(MAIN_POSITION_VELUX, product.getCurrentPosition());
        assertEquals(MAIN_POSITION_VELUX, product.getTarget());
        assertEquals(PRODUCT_INDEX_VELUX, product.getBridgeProductIndex().toInt());
        assertEquals(ACTUATOR_TYPE_VELUX, product.getActuatorType());
        assertEquals(UNKNOWN_POSITION, product.getVanePosition());
        assertNull(product.getFunctionalParameters());
        assertFalse(product.isSomfyProduct());

        // reset the dirty flag
        existingProducts.resetDirtyFlag();
        assertFalse(existingProducts.isDirty());

        // confirm modified list is now empty again
        modified = existingProducts.valuesOfModified();
        assertEquals(0, modified.length);
    }

    /**
     * Test actuator type setting.
     */
    @Test
    @Order(11)
    public void testActuatorTypeSetting() {
        VeluxProduct product = new VeluxProduct();
        assertEquals(ACTUATOR_TYPE_UNDEF, product.getActuatorType());

        // set actuator type
        product.setActuatorType(ACTUATOR_TYPE_SOMFY);
        assertEquals(ACTUATOR_TYPE_SOMFY, product.getActuatorType());

        // try to set it again
        product.setActuatorType(ACTUATOR_TYPE_VELUX);
        assertNotEquals(ACTUATOR_TYPE_VELUX, product.getActuatorType());

        // try with a clean product
        product = new VeluxProduct();
        product.setActuatorType(ACTUATOR_TYPE_VELUX);
        assertEquals(ACTUATOR_TYPE_VELUX, product.getActuatorType());
    }

    /**
     * Test the SCrunProduct command by creating packet for a given main position and vane position, and checking the
     * created packet is as expected.
     */
    @Test
    @Order(12)
    public void testSCrunProduct() {
        final String expectedString = "02 1C 08 05 00 20 00 D2 00 00 00 00 00 A0 00 00 00 00 00 00 00 00 00 00 00"
                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 06 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                + " 00 00 00 00 00 00 00 00 00";
        final byte[] expectedPacket = toByteArray(expectedString);
        final int targetMainPosition = 0xD200;
        final int targetVanePosition = 0xA000;

        // initialise the product to be commanded
        VeluxProduct product = new VeluxProduct(new VeluxProductName("who cares"),
                new ProductBridgeIndex(PRODUCT_INDEX_SOMFY), 0, 0, 0, null);
        product.setActuatorType(ACTUATOR_TYPE_SOMFY);
        product.setCurrentPosition(targetMainPosition);
        product.setVanePosition(targetVanePosition);

        // create the run product command, and initialize it from the test product's state values
        SCrunProductCommand bcp = new SCrunProductCommand();
        bcp.setNodeIdAndParameters(product.getBridgeProductIndex().toInt(), product.getCurrentPosition(),
                product.getFunctionalParameters());

        // get the resulting data packet
        byte[] actualPacket = bcp.getRequestDataAsArrayOfBytes();

        // check the packet lengths are the same
        assertEquals(expectedPacket.length, actualPacket.length);

        // check the packet contents are identical (note start at i = 2 because session id won't match)
        boolean identical = true;
        for (int i = 2; i < expectedPacket.length; i++) {
            if (actualPacket[i] != expectedPacket[i]) {
                identical = false;
            }
        }
        assertTrue(identical);
    }
}
