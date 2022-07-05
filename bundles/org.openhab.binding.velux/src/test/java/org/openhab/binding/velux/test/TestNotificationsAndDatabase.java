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
import org.openhab.binding.velux.internal.things.VeluxExistingProducts;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI;
import org.openhab.binding.velux.internal.things.VeluxProduct;
import org.openhab.binding.velux.internal.things.VeluxProduct.ProductBridgeIndex;
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
    private static final byte TEST_PRODUCT_INDEX = 6;
    private static final int TEST_MAIN_POSITION = 0xC800;
    private static final int TEST_VANE_POSITION = 0x634f;
    private static final int TEST_TARGET_POSITION = 0xB800;
    private static final int TEST_UNKNOWN_POSITION = VeluxProductPosition.VPP_VELUX_UNKNOWN;
    private static final int TEST_IGNORE_POSITION = VeluxProductPosition.VPP_VELUX_IGNORE;

    private static final ActuatorType TEST_ACTUATOR_TYPE = ActuatorType.BLIND_17;

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
        product.setActuatorType(TEST_ACTUATOR_TYPE);
        assertTrue(product.supportsVanePosition());
    }

    /**
     * Test the correct parsing of a 'GW_GET_NODE_INFORMATION_NTF' notification packet.
     * Note: this packet is from a Somfy roller shutter with main and vane position.
     */
    @Test
    @Order(3)
    public void test_GW_GET_NODE_INFORMATION_NTF() {
        // initialise the test parameters
        final String packet = "06 00 06 00 48 6F 62 62 79 6B 61 6D 65 72 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                + " 00 00 00 00 00 01 04 40 00 00 00 00 00 00 00 00 00 00 00 00 00 2D C8 00 C8 00 F7 FF F7 FF 00 00 F7 FF 00"
                + " 00 4F 00 4A EA 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
        final short command = VeluxKLFAPI.Command.GW_GET_NODE_INFORMATION_NTF.getShort();

        // initialise the BCP
        SCgetProduct bcp = new SCgetProduct();
        bcp.setProductId(TEST_PRODUCT_INDEX);

        // set the packet response
        bcp.setResponse(command, toByteArray(packet), false);

        // check BCP status
        assertTrue(bcp.isCommunicationSuccessful());
        assertTrue(bcp.isCommunicationFinished());

        // post initialise the product
        VeluxProduct product = bcp.getProduct();
        product.setActuatorType(TEST_ACTUATOR_TYPE);

        // check positive assertions
        assertEquals(TEST_MAIN_POSITION, product.getCurrentPosition());
        assertEquals(TEST_MAIN_POSITION, product.getTarget());
        assertEquals(TEST_PRODUCT_INDEX, product.getBridgeProductIndex().toInt());
        assertEquals(TEST_ACTUATOR_TYPE, product.getActuatorType());
        assertEquals(TEST_UNKNOWN_POSITION, product.getVanePosition());
        assertNull(product.getFunctionalParameters());

        // check negative assertions
        assertNotEquals(TEST_VANE_POSITION, product.getVanePosition());

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

        // check that the product in the database is indeed the one just created
        VeluxProduct existing = existingProducts.get(new ProductBridgeIndex(TEST_PRODUCT_INDEX));
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
        VeluxProduct product = existingProducts.get(new ProductBridgeIndex(TEST_PRODUCT_INDEX));

        // confirm the product details
        assertEquals(TEST_MAIN_POSITION, product.getCurrentPosition());
        assertEquals(TEST_MAIN_POSITION, product.getTarget());
        assertEquals(TEST_PRODUCT_INDEX, product.getBridgeProductIndex().toInt());
        assertEquals(TEST_ACTUATOR_TYPE, product.getActuatorType());
        assertEquals(TEST_UNKNOWN_POSITION, product.getVanePosition());
        assertNull(product.getFunctionalParameters());
    }

    /**
     * Test the correct parsing of a 'GW_STATUS_REQUEST_NTF' notification packet.
     * Note: this packet is from a Somfy roller shutter with main and vane position.
     */
    @Test
    @Order(5)
    public void test_GW_STATUS_REQUEST_NTF() {
        // initialise the test parameters
        final String packet = "00 D8 01 06 00 01 01 02 00 C8 00 03 63 4F 00 00 00 00 00 00 00 00 00 00 00 00 00"
                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";
        final short command = VeluxKLFAPI.Command.GW_STATUS_REQUEST_NTF.getShort();

        // initialise the BCP
        SCgetProductStatus bcp = new SCgetProductStatus();
        bcp.setProductId(TEST_PRODUCT_INDEX);

        // set the packet response
        bcp.setResponse(command, toByteArray(packet), false);

        // check BCP status
        assertTrue(bcp.isCommunicationSuccessful());
        assertTrue(bcp.isCommunicationFinished());

        // post initialise the product
        VeluxProduct product = bcp.getProduct();
        product.setActuatorType(TEST_ACTUATOR_TYPE);

        // check positive assertions
        assertEquals(TEST_MAIN_POSITION, product.getCurrentPosition());
        assertEquals(TEST_IGNORE_POSITION, product.getTarget());
        assertEquals(TEST_PRODUCT_INDEX, product.getBridgeProductIndex().toInt());
        assertEquals(TEST_ACTUATOR_TYPE, product.getActuatorType());
        assertEquals(TEST_VANE_POSITION, product.getVanePosition());
        assertNotNull(product.getFunctionalParameters());

        // test updating the existing product in the database
        VeluxExistingProducts existingProducts = getExistingProducts();
        assertTrue(existingProducts.update(product));
        assertTrue(existingProducts.isDirty());

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
        VeluxProduct product = existingProducts.get(new ProductBridgeIndex(TEST_PRODUCT_INDEX));

        // confirm the product details
        assertEquals(TEST_MAIN_POSITION, product.getCurrentPosition());
        assertEquals(TEST_MAIN_POSITION, product.getTarget());
        assertEquals(TEST_PRODUCT_INDEX, product.getBridgeProductIndex().toInt());
        assertEquals(TEST_ACTUATOR_TYPE, product.getActuatorType());
        assertEquals(TEST_VANE_POSITION, product.getVanePosition());
        assertNotNull(product.getFunctionalParameters());
    }

    /**
     * Test the correct parsing of a 'GW_NODE_STATE_POSITION_CHANGED_NTF' notification packet.
     * Note: this packet is from a Somfy roller shutter with main and vane position.
     */
    @Test
    @Order(7)
    public void test_GW_NODE_STATE_POSITION_CHANGED_NTF() {
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

        // post initialise the product
        VeluxProduct product = bcp.getProduct();
        product.setActuatorType(TEST_ACTUATOR_TYPE);

        // check positive assertions
        assertEquals(TEST_MAIN_POSITION, product.getCurrentPosition());
        assertEquals(TEST_TARGET_POSITION, product.getTarget());
        assertEquals(TEST_PRODUCT_INDEX, product.getBridgeProductIndex().toInt());
        assertEquals(TEST_ACTUATOR_TYPE, product.getActuatorType());
        assertEquals(TEST_UNKNOWN_POSITION, product.getVanePosition());
        assertNull(product.getFunctionalParameters());

        // check negative assertions
        assertNotEquals(TEST_VANE_POSITION, product.getVanePosition());

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
        VeluxProduct product = existingProducts.get(new ProductBridgeIndex(TEST_PRODUCT_INDEX));

        // confirm the product details
        assertEquals(TEST_MAIN_POSITION, product.getCurrentPosition());
        assertEquals(TEST_TARGET_POSITION, product.getTarget());
        assertEquals(TEST_PRODUCT_INDEX, product.getBridgeProductIndex().toInt());
        assertEquals(TEST_ACTUATOR_TYPE, product.getActuatorType());
        assertEquals(TEST_VANE_POSITION, product.getVanePosition());
        assertNotNull(product.getFunctionalParameters());
    }

    /**
     * Confirm that the modified products list is functioning.
     */
    @Test
    @Order(9)
    public void testModifiedList() {
        VeluxExistingProducts existingProducts = getExistingProducts();
        VeluxProduct[] modified = existingProducts.valuesOfModified();

        // confirm that the list contains one entry
        assertEquals(1, modified.length);

        // confirm the product details
        VeluxProduct product = modified[0];
        assertEquals(TEST_MAIN_POSITION, product.getCurrentPosition());
        assertEquals(TEST_TARGET_POSITION, product.getTarget());
        assertEquals(TEST_PRODUCT_INDEX, product.getBridgeProductIndex().toInt());
        assertEquals(TEST_ACTUATOR_TYPE, product.getActuatorType());
        assertEquals(TEST_VANE_POSITION, product.getVanePosition());
        assertNotNull(product.getFunctionalParameters());

        // reset the dirty flag
        existingProducts.resetDirtyFlag();
        assertFalse(existingProducts.isDirty());

        // confirm modified list is now empty again
        modified = existingProducts.valuesOfModified();
        assertEquals(0, modified.length);
    }
}
