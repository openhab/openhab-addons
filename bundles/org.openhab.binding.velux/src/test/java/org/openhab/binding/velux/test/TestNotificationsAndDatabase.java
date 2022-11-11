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
import org.openhab.binding.velux.internal.bridge.slip.FunctionalParameters;
import org.openhab.binding.velux.internal.bridge.slip.SCgetHouseStatus;
import org.openhab.binding.velux.internal.bridge.slip.SCgetProduct;
import org.openhab.binding.velux.internal.bridge.slip.SCgetProductStatus;
import org.openhab.binding.velux.internal.bridge.slip.SCrunProductCommand;
import org.openhab.binding.velux.internal.things.VeluxExistingProducts;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxProduct;
import org.openhab.binding.velux.internal.things.VeluxProduct.ProductBridgeIndex;
import org.openhab.binding.velux.internal.things.VeluxProduct.ProductState;
import org.openhab.binding.velux.internal.things.VeluxProductName;
import org.openhab.binding.velux.internal.things.VeluxProductPosition;
import org.openhab.binding.velux.internal.things.VeluxProductPosition.PositionType;
import org.openhab.binding.velux.internal.things.VeluxProductType.ActuatorType;
import org.openhab.core.library.types.PercentType;

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
    private static final byte PRODUCT_INDEX_A = 6;
    private static final byte PRODUCT_INDEX_B = 0;
    private static final int MAIN_POSITION_A = 0xC800;
    private static final int MAIN_POSITION_B = 0x4600;
    private static final int VANE_POSITION_A = 0x634f;
    private static final int TARGET_POSITION = 0xB800;
    private static final int STATE_SOMFY = 0x2D;

    private static final int UNKNOWN_POSITION = VeluxProductPosition.VPP_VELUX_UNKNOWN;
    private static final int IGNORE_POSITION = VeluxProductPosition.VPP_VELUX_IGNORE;
    private static final int STATE_DONE = VeluxProduct.ProductState.DONE.value;

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
        final Command command = VeluxKLFAPI.Command.GW_GET_NODE_INFORMATION_NTF;

        // initialise the BCP
        SCgetProduct bcp = new SCgetProduct();
        bcp.setProductId(PRODUCT_INDEX_A);

        // set the packet response
        bcp.setResponse(command.getShort(), toByteArray(packet), false);

        // check BCP status
        assertTrue(bcp.isCommunicationSuccessful());
        assertTrue(bcp.isCommunicationFinished());

        // initialise the product
        VeluxProduct product = bcp.getProduct();

        // check positive assertions
        assertEquals(STATE_SOMFY, product.getState());
        assertEquals(MAIN_POSITION_A, product.getCurrentPosition());
        assertEquals(MAIN_POSITION_A, product.getTarget());
        assertEquals(PRODUCT_INDEX_A, product.getBridgeProductIndex().toInt());
        assertEquals(ACTUATOR_TYPE_SOMFY, product.getActuatorType());
        assertEquals(UNKNOWN_POSITION, product.getVanePosition());
        assertNull(product.getFunctionalParameters());
        assertTrue(product.supportsVanePosition());
        assertTrue(product.isSomfyProduct());
        assertEquals(ProductState.DONE, product.getProductState());

        // check negative assertions
        assertNotEquals(VANE_POSITION_A, product.getVanePosition());

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
        VeluxProduct existing = existingProducts.get(new ProductBridgeIndex(PRODUCT_INDEX_A));
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
        VeluxProduct product = existingProducts.get(new ProductBridgeIndex(PRODUCT_INDEX_A));

        // confirm the product details
        assertEquals(STATE_SOMFY, product.getState());
        assertEquals(MAIN_POSITION_A, product.getCurrentPosition());
        assertEquals(MAIN_POSITION_A, product.getTarget());
        assertEquals(PRODUCT_INDEX_A, product.getBridgeProductIndex().toInt());
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
        final Command command = VeluxKLFAPI.Command.GW_STATUS_REQUEST_NTF;

        // initialise the BCP
        SCgetProductStatus bcp = new SCgetProductStatus();
        bcp.setProductId(PRODUCT_INDEX_A);

        // set the packet response
        bcp.setResponse(command.getShort(), toByteArray(packet), false);

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
        assertEquals(MAIN_POSITION_A, product.getCurrentPosition());
        assertEquals(IGNORE_POSITION, product.getTarget());
        assertEquals(PRODUCT_INDEX_A, product.getBridgeProductIndex().toInt());
        assertEquals(ACTUATOR_TYPE_SOMFY, product.getActuatorType());
        assertEquals(VANE_POSITION_A, product.getVanePosition());
        assertNotNull(product.getFunctionalParameters());
        assertEquals(ProductState.DONE, product.getProductState());

        // test updating the existing product in the database
        VeluxExistingProducts existingProducts = getExistingProducts();
        assertTrue(existingProducts.update(product));
        assertTrue(existingProducts.isDirty());

        // updating again with the same data should NOT set the dirty flag
        existingProducts.resetDirtyFlag();
        assertTrue(existingProducts.update(product));
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
        VeluxProduct product = existingProducts.get(new ProductBridgeIndex(PRODUCT_INDEX_A));

        // confirm the product details
        assertEquals(STATE_DONE, product.getState());
        assertEquals(MAIN_POSITION_A, product.getCurrentPosition());
        assertEquals(MAIN_POSITION_A, product.getTarget());
        assertEquals(PRODUCT_INDEX_A, product.getBridgeProductIndex().toInt());
        assertEquals(ACTUATOR_TYPE_SOMFY, product.getActuatorType());
        assertEquals(VANE_POSITION_A, product.getVanePosition());
        assertNotNull(product.getFunctionalParameters());
        assertEquals(ProductState.DONE, product.getProductState());
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
        assertEquals(MAIN_POSITION_A, product.getCurrentPosition());
        assertEquals(TARGET_POSITION, product.getTarget());
        assertEquals(PRODUCT_INDEX_A, product.getBridgeProductIndex().toInt());
        assertEquals(ACTUATOR_TYPE_SOMFY, product.getActuatorType());
        assertEquals(UNKNOWN_POSITION, product.getVanePosition());
        assertNull(product.getFunctionalParameters());

        // check negative assertions
        assertNotEquals(VANE_POSITION_A, product.getVanePosition());

        VeluxExistingProducts existingProducts = getExistingProducts();
        existingProducts.update(product);
    }

    /**
     * Confirm that the product in the existing database has the same values as the product created and updated to the
     * database in test 7.
     */
    @Test
    @Order(8)
    public void testExistingValidVanePositionWithNewTargetValue() {
        VeluxExistingProducts existingProducts = getExistingProducts();
        VeluxProduct product = existingProducts.get(new ProductBridgeIndex(PRODUCT_INDEX_A));

        // confirm the product details
        assertEquals(STATE_SOMFY, product.getState());
        assertEquals(MAIN_POSITION_A, product.getCurrentPosition());
        assertEquals(TARGET_POSITION, product.getTarget());
        assertEquals(PRODUCT_INDEX_A, product.getBridgeProductIndex().toInt());
        assertEquals(ACTUATOR_TYPE_SOMFY, product.getActuatorType());
        assertEquals(VANE_POSITION_A, product.getVanePosition());
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
        bcp.setProductId(PRODUCT_INDEX_B);

        // set the packet response
        bcp.setResponse(command, toByteArray(packet), false);

        // check BCP status
        assertTrue(bcp.isCommunicationSuccessful());
        assertTrue(bcp.isCommunicationFinished());

        // initialise the product
        VeluxProduct product = bcp.getProduct();

        // check positive assertions
        assertEquals(STATE_DONE, product.getState());
        assertEquals(MAIN_POSITION_B, product.getCurrentPosition());
        assertEquals(MAIN_POSITION_B, product.getTarget());
        assertEquals(PRODUCT_INDEX_B, product.getBridgeProductIndex().toInt());
        assertEquals(ACTUATOR_TYPE_VELUX, product.getActuatorType());
        assertEquals(UNKNOWN_POSITION, product.getVanePosition());
        assertNull(product.getFunctionalParameters());
        assertFalse(product.isSomfyProduct());

        // check negative assertions
        assertFalse(product.supportsVanePosition());
        assertNotEquals(VANE_POSITION_A, product.getVanePosition());

        // register in existing products database
        VeluxExistingProducts existingProducts = getExistingProducts();
        assertTrue(existingProducts.register(product));
        assertTrue(existingProducts.isRegistered(product));
        assertTrue(existingProducts.isRegistered(product.getBridgeProductIndex()));
        assertEquals(2, existingProducts.getNoMembers());

        // check that the product in the database is indeed the one just created
        VeluxProduct existing = existingProducts.get(new ProductBridgeIndex(PRODUCT_INDEX_B));
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
        assertEquals(STATE_SOMFY, product.getState());
        assertEquals(MAIN_POSITION_A, product.getCurrentPosition());
        assertEquals(TARGET_POSITION, product.getTarget());
        assertEquals(PRODUCT_INDEX_A, product.getBridgeProductIndex().toInt());
        assertEquals(ACTUATOR_TYPE_SOMFY, product.getActuatorType());
        assertEquals(VANE_POSITION_A, product.getVanePosition());
        assertTrue(product.isSomfyProduct());
        assertNotNull(product.getFunctionalParameters());

        // confirm the product details for the Velux product
        product = modified[1];
        assertEquals(STATE_DONE, product.getState());
        assertEquals(MAIN_POSITION_B, product.getCurrentPosition());
        assertEquals(MAIN_POSITION_B, product.getTarget());
        assertEquals(PRODUCT_INDEX_B, product.getBridgeProductIndex().toInt());
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
    public void testSCrunProductA() {
        final String expectedString = "02 1C 08 05 00 20 00 90 00 00 00 00 00 A0 00 00 00 00 00 00 00 00 00 00 00"
                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 06 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                + " 00 00 00 00 00 00 00 00 00";
        final byte[] expectedPacket = toByteArray(expectedString);
        final int targetMainPosition = 0x9000;
        final int targetVanePosition = 0xA000;

        // initialise the product to be commanded
        VeluxProduct product = new VeluxProduct(VeluxProductName.UNKNOWN, new ProductBridgeIndex(PRODUCT_INDEX_A), 0, 0,
                0, null, Command.UNDEFTYPE);
        product.setActuatorType(ACTUATOR_TYPE_SOMFY);
        product.setCurrentPosition(targetMainPosition);
        product.setVanePosition(targetVanePosition);

        // create the run product command, and initialise it from the test product's state values
        SCrunProductCommand bcp = new SCrunProductCommand();
        bcp.setNodeIdAndParameters(product.getBridgeProductIndex().toInt(),
                new VeluxProductPosition(product.getCurrentPosition()), product.getFunctionalParameters());

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

        // check the resulting updater product state is 'executing' with the new values
        product = bcp.getProduct();
        product.setActuatorType(ACTUATOR_TYPE_SOMFY);
        assertEquals(ProductState.EXECUTING, product.getProductState());
        assertEquals(targetMainPosition, product.getCurrentPosition());
        assertEquals(targetMainPosition, product.getTarget());
        assertEquals(targetMainPosition, product.getDisplayPosition());
        assertEquals(targetVanePosition, product.getVanePosition());
        assertEquals(targetVanePosition, product.getVaneDisplayPosition());
    }

    /**
     * Test the SCrunProduct command by creating a packet with some bad values, and checking the product is as expected.
     */
    @Test
    @Order(12)
    public void testSCrunProductB() {
        SCrunProductCommand bcp = new SCrunProductCommand();

        final int errorMainPosition = 0xffff;
        VeluxProduct product = new VeluxProduct(VeluxProductName.UNKNOWN, new ProductBridgeIndex(PRODUCT_INDEX_A), 0, 0,
                0, null, Command.UNDEFTYPE);
        product.setActuatorType(ACTUATOR_TYPE_SOMFY);
        product.setCurrentPosition(errorMainPosition);

        bcp.setNodeIdAndParameters(product.getBridgeProductIndex().toInt(),
                new VeluxProductPosition(product.getCurrentPosition()), product.getFunctionalParameters());
        product = bcp.getProduct();
        product.setActuatorType(ACTUATOR_TYPE_SOMFY);

        assertEquals(ProductState.EXECUTING, product.getProductState());
        assertEquals(IGNORE_POSITION, product.getCurrentPosition());
        assertEquals(IGNORE_POSITION, product.getTarget());
        assertEquals(UNKNOWN_POSITION, product.getDisplayPosition());
        assertEquals(UNKNOWN_POSITION, product.getVanePosition());
        assertEquals(UNKNOWN_POSITION, product.getVaneDisplayPosition());
    }

    /**
     * Test the actuator state.
     */
    @Test
    @Order(14)
    public void testActuatorState() {
        VeluxProduct product = new VeluxProduct(VeluxProductName.UNKNOWN, new ProductBridgeIndex(PRODUCT_INDEX_A), 0, 0,
                0, null, Command.UNDEFTYPE);
        int[] inputStates = { 0, 1, 2, 3, 4, 5, 0x2c, 0x2d, 0xff, 0x80 };
        ProductState[] expected = { ProductState.NON_EXECUTING, ProductState.ERROR, ProductState.NOT_USED,
                ProductState.WAITING_FOR_POWER, ProductState.EXECUTING, ProductState.DONE, ProductState.EXECUTING,
                ProductState.DONE, ProductState.UNKNOWN, ProductState.MANUAL };
        for (int i = 0; i < inputStates.length; i++) {
            product.setState(inputStates[i]);
            assertEquals(expected[i], product.getProductState());
        }
    }

    /**
     * Test actuator positions and display positions.
     */
    @Test
    @Order(15)
    public void testActuatorPositions() {
        VeluxProduct product = new VeluxProduct(VeluxProductName.UNKNOWN, new ProductBridgeIndex(PRODUCT_INDEX_A), 0, 0,
                0, null, Command.UNDEFTYPE);

        product.setCurrentPosition(MAIN_POSITION_A);
        product.setTarget(TARGET_POSITION);
        product.setActuatorType(ACTUATOR_TYPE_SOMFY);
        product.setVanePosition(VANE_POSITION_A);

        // state uninitialised
        assertEquals(MAIN_POSITION_A, product.getCurrentPosition());
        assertEquals(MAIN_POSITION_A, product.getDisplayPosition());
        assertEquals(TARGET_POSITION, product.getTarget());
        assertEquals(VANE_POSITION_A, product.getVaneDisplayPosition());
        assertEquals(VANE_POSITION_A, product.getVanePosition());

        // state = done
        product.setState(ProductState.DONE.value);
        assertEquals(MAIN_POSITION_A, product.getDisplayPosition());
        assertEquals(MAIN_POSITION_A, product.getCurrentPosition());
        assertEquals(TARGET_POSITION, product.getTarget());
        assertEquals(VANE_POSITION_A, product.getVaneDisplayPosition());
        assertEquals(VANE_POSITION_A, product.getVanePosition());

        // state = not used
        product.setState(ProductState.NOT_USED.value);
        assertEquals(MAIN_POSITION_A, product.getDisplayPosition());
        assertEquals(MAIN_POSITION_A, product.getCurrentPosition());
        assertEquals(TARGET_POSITION, product.getTarget());
        assertEquals(VANE_POSITION_A, product.getVaneDisplayPosition());
        assertEquals(VANE_POSITION_A, product.getVanePosition());

        // state = executing
        product.setState(ProductState.EXECUTING.value);
        assertEquals(TARGET_POSITION, product.getDisplayPosition());
        assertEquals(MAIN_POSITION_A, product.getCurrentPosition());
        assertEquals(TARGET_POSITION, product.getTarget());
        assertEquals(VANE_POSITION_A, product.getVaneDisplayPosition());
        assertEquals(VANE_POSITION_A, product.getVanePosition());

        // state = manual + excuting
        product.setState(ProductState.MANUAL.value + ProductState.EXECUTING.value);
        assertEquals(UNKNOWN_POSITION, product.getDisplayPosition());
        assertEquals(MAIN_POSITION_A, product.getCurrentPosition());
        assertEquals(TARGET_POSITION, product.getTarget());
        assertEquals(UNKNOWN_POSITION, product.getVaneDisplayPosition());
        assertEquals(VANE_POSITION_A, product.getVanePosition());

        // state = error
        product.setState(ProductState.ERROR.value);
        assertEquals(UNKNOWN_POSITION, product.getDisplayPosition());
        assertEquals(MAIN_POSITION_A, product.getCurrentPosition());
        assertEquals(TARGET_POSITION, product.getTarget());
        assertEquals(UNKNOWN_POSITION, product.getVaneDisplayPosition());
        assertEquals(VANE_POSITION_A, product.getVanePosition());
    }

    /**
     * Test the SCgetHouseStatus command by checking for the correct parsing of a 'GW_NODE_STATE_POSITION_CHANGED_NTF'
     * notification packet. Note: this packet is from a Velux roller shutter with main and vane position.
     */
    @Test
    @Order(16)
    public void testSCgetHouseStatusOnVelux() {
        // initialise the test parameters
        final String packet = "00 2D C8 00 B8 00 F7 FF F7 FF 00 00 F7 FF 00 00 4A E5 00 00";
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

        // check negative assertions
        assertNotEquals(VANE_POSITION_A, product.getVanePosition());

        // test updating the existing product in the database
        VeluxExistingProducts existingProducts = getExistingProducts();

        // process this as a receive only command for a Velux product => update IS applied
        existingProducts.resetDirtyFlag();
        assertTrue(existingProducts.update(product));
        assertTrue(existingProducts.isDirty());

        // process this as an information request command for a Velux product => update IS applied
        existingProducts.resetDirtyFlag();
        product.setCurrentPosition(MAIN_POSITION_B);
        assertTrue(existingProducts.update(product));
        assertTrue(existingProducts.isDirty());
    }

    /**
     * Test updating logic with various states applied.
     */
    @Test
    @Order(17)
    public void testUpdatingLogic() {
        VeluxExistingProducts existingProducts = getExistingProducts();
        ProductBridgeIndex index = new ProductBridgeIndex(PRODUCT_INDEX_A);
        VeluxProduct product = existingProducts.get(index).clone();

        assertEquals(ProductState.DONE, product.getProductState());

        // state = done
        assertEquals(MAIN_POSITION_A, product.getDisplayPosition());
        assertEquals(MAIN_POSITION_A, product.getCurrentPosition());
        assertEquals(TARGET_POSITION, product.getTarget());
        assertEquals(VANE_POSITION_A, product.getVaneDisplayPosition());
        assertEquals(VANE_POSITION_A, product.getVanePosition());

        // state = not used
        product.setState(ProductState.NOT_USED.value);
        product.setCurrentPosition(MAIN_POSITION_A - 1);
        product.setTarget(TARGET_POSITION - 1);
        product.setVanePosition(VANE_POSITION_A - 1);
        existingProducts.update(product);
        product = existingProducts.get(index).clone();
        assertEquals(MAIN_POSITION_A, product.getDisplayPosition());
        assertEquals(MAIN_POSITION_A, product.getCurrentPosition());
        assertEquals(TARGET_POSITION, product.getTarget());
        assertEquals(VANE_POSITION_A, product.getVaneDisplayPosition());
        assertEquals(VANE_POSITION_A, product.getVanePosition());

        // state = manual + excuting
        product.setState(ProductState.MANUAL.value + ProductState.EXECUTING.value);
        product.setCurrentPosition(MAIN_POSITION_A - 1);
        product.setTarget(TARGET_POSITION - 1);
        product.setVanePosition(VANE_POSITION_A - 1);
        existingProducts.update(product);
        product = existingProducts.get(index).clone();
        assertEquals(UNKNOWN_POSITION, product.getDisplayPosition());
        assertEquals(MAIN_POSITION_A, product.getCurrentPosition());
        assertEquals(TARGET_POSITION, product.getTarget());
        assertEquals(UNKNOWN_POSITION, product.getVaneDisplayPosition());
        assertEquals(VANE_POSITION_A, product.getVanePosition());

        // state = error
        product.setState(ProductState.ERROR.value);
        product.setCurrentPosition(MAIN_POSITION_A - 1);
        product.setTarget(TARGET_POSITION - 1);
        product.setVanePosition(VANE_POSITION_A - 1);
        existingProducts.update(product);
        product = existingProducts.get(index).clone();
        assertEquals(UNKNOWN_POSITION, product.getDisplayPosition());
        assertEquals(MAIN_POSITION_A, product.getCurrentPosition());
        assertEquals(TARGET_POSITION, product.getTarget());
        assertEquals(UNKNOWN_POSITION, product.getVaneDisplayPosition());
        assertEquals(VANE_POSITION_A, product.getVanePosition());

        // state = executing
        product.setState(ProductState.EXECUTING.value);
        product.setCurrentPosition(MAIN_POSITION_A - 1);
        product.setTarget(TARGET_POSITION - 1);
        product.setVanePosition(VANE_POSITION_A - 1);
        existingProducts.update(product);
        product = existingProducts.get(index).clone();
        assertNotEquals(TARGET_POSITION, product.getDisplayPosition());
        assertNotEquals(MAIN_POSITION_A, product.getCurrentPosition());
        assertNotEquals(TARGET_POSITION, product.getTarget());
        assertNotEquals(VANE_POSITION_A, product.getVaneDisplayPosition());
        assertNotEquals(VANE_POSITION_A, product.getVanePosition());

        // state = done
        product.setState(ProductState.EXECUTING.value);
        product.setCurrentPosition(MAIN_POSITION_A);
        product.setTarget(TARGET_POSITION);
        product.setVanePosition(VANE_POSITION_A);
        existingProducts.update(product);
        product = existingProducts.get(index).clone();
        assertEquals(TARGET_POSITION, product.getDisplayPosition());
        assertEquals(MAIN_POSITION_A, product.getCurrentPosition());
        assertEquals(TARGET_POSITION, product.getTarget());
        assertEquals(VANE_POSITION_A, product.getVaneDisplayPosition());
        assertEquals(VANE_POSITION_A, product.getVanePosition());
    }

    /**
     * Test updating the existing product in the database with special exceptions.
     */
    @Test
    @Order(18)
    public void testSpecialExceptions() {
        VeluxExistingProducts existingProducts = getExistingProducts();
        VeluxProduct existing = existingProducts.get(new ProductBridgeIndex(PRODUCT_INDEX_A));

        VeluxProduct product;

        // process this as a receive only command for a Somfy product => update IS applied
        product = new VeluxProduct(existing.getProductName(), existing.getBridgeProductIndex(), existing.getState(),
                existing.getCurrentPosition(), existing.getTarget(), existing.getFunctionalParameters(),
                Command.GW_OPENHAB_RECEIVEONLY);
        existingProducts.resetDirtyFlag();
        product.setState(ProductState.DONE.value);
        product.setCurrentPosition(MAIN_POSITION_B);
        assertTrue(existingProducts.update(product));
        assertTrue(existingProducts.isDirty());

        // process this as an information request command for a Somfy product => update IS applied
        product = new VeluxProduct(existing.getProductName(), existing.getBridgeProductIndex(), existing.getState(),
                existing.getCurrentPosition(), existing.getTarget(), existing.getFunctionalParameters(),
                Command.GW_GET_NODE_INFORMATION_REQ);
        existingProducts.resetDirtyFlag();
        product.setCurrentPosition(MAIN_POSITION_A);
        assertTrue(existingProducts.update(product));
        assertTrue(existingProducts.isDirty());

        // process this as a receive only command for a Somfy product with bad data => update NOT applied
        product = new VeluxProduct(existing.getProductName(), existing.getBridgeProductIndex(), existing.getState(),
                existing.getCurrentPosition(), existing.getTarget(), existing.getFunctionalParameters(),
                Command.GW_OPENHAB_RECEIVEONLY);
        existingProducts.resetDirtyFlag();
        product.setCurrentPosition(UNKNOWN_POSITION);
        product.setTarget(UNKNOWN_POSITION);
        assertTrue(existingProducts.update(product));
        assertFalse(existingProducts.isDirty());
    }

    /**
     * Test VeluxProductPosition
     */
    @Test
    @Order(19)
    public void testVeluxProductPosition() {
        VeluxProductPosition position;
        int target;

        // on and inside range limits
        assertTrue(new VeluxProductPosition(VeluxProductPosition.VPP_VELUX_MIN).isValid());
        assertTrue(new VeluxProductPosition(VeluxProductPosition.VPP_VELUX_MAX).isValid());
        assertTrue(new VeluxProductPosition(VeluxProductPosition.VPP_VELUX_MAX - 1).isValid());
        assertTrue(new VeluxProductPosition(VeluxProductPosition.VPP_VELUX_MIN + 1).isValid());

        // outside range limits
        assertFalse(new VeluxProductPosition(VeluxProductPosition.VPP_VELUX_MIN - 1).isValid());
        assertFalse(new VeluxProductPosition(VeluxProductPosition.VPP_VELUX_MAX + 1).isValid());
        assertFalse(new VeluxProductPosition(VeluxProductPosition.VPP_VELUX_IGNORE).isValid());
        assertFalse(new VeluxProductPosition(VeluxProductPosition.VPP_VELUX_DEFAULT).isValid());
        assertFalse(new VeluxProductPosition(VeluxProductPosition.VPP_VELUX_STOP).isValid());
        assertFalse(new VeluxProductPosition(VeluxProductPosition.VPP_VELUX_UNKNOWN).isValid());

        // 80% absolute position
        position = new VeluxProductPosition(new PercentType(80));
        assertEquals(0xA000, position.getPositionAsVeluxType());
        assertTrue(position.isValid());

        // 80% absolute position
        position = new VeluxProductPosition(new PercentType(80), false);
        assertEquals(0xA000, position.getPositionAsVeluxType());
        assertTrue(position.isValid());

        // 80% inverted absolute position (i.e. 20%)
        position = new VeluxProductPosition(new PercentType(80), true);
        assertEquals(0x2800, position.getPositionAsVeluxType());
        assertTrue(position.isValid());

        // 80% positive relative position
        target = VeluxProductPosition.VPP_VELUX_RELATIVE_ORIGIN
                + (VeluxProductPosition.VPP_VELUX_RELATIVE_RANGE * 8 / 10);
        position = new VeluxProductPosition(new PercentType(80)).overridePositionType(PositionType.OFFSET_POSITIVE);
        assertTrue(position.isValid());
        assertEquals(target, position.getPositionAsVeluxType());

        // 80% negative relative position
        target = VeluxProductPosition.VPP_VELUX_RELATIVE_ORIGIN
                - (VeluxProductPosition.VPP_VELUX_RELATIVE_RANGE * 8 / 10);
        position = new VeluxProductPosition(new PercentType(80)).overridePositionType(PositionType.OFFSET_NEGATIVE);
        assertTrue(position.isValid());
        assertEquals(target, position.getPositionAsVeluxType());
    }

    /**
     * Test SCrunProductResult results
     */
    @Test
    @Order(20)
    public void testSCrunProductResults() {
        SCrunProductCommand bcp = new SCrunProductCommand();

        // create a dummy product to get some functional parameters from
        VeluxProduct product = new VeluxProduct(VeluxProductName.UNKNOWN, new ProductBridgeIndex(PRODUCT_INDEX_A), 0, 0,
                0, null, Command.UNDEFTYPE);
        product.setActuatorType(ACTUATOR_TYPE_SOMFY);
        product.setVanePosition(VANE_POSITION_A);
        final FunctionalParameters functionalParameters = product.getFunctionalParameters();

        boolean ok;

        // test setting both main and vane position
        ok = bcp.setNodeIdAndParameters(PRODUCT_INDEX_A, new VeluxProductPosition(MAIN_POSITION_A),
                functionalParameters);
        assertTrue(ok);
        product = bcp.getProduct();
        product.setActuatorType(ACTUATOR_TYPE_SOMFY);
        assertEquals(MAIN_POSITION_A, product.getCurrentPosition());
        assertEquals(VANE_POSITION_A, product.getVanePosition());

        // test setting vane position only
        ok = bcp.setNodeIdAndParameters(PRODUCT_INDEX_A, null, functionalParameters);
        assertTrue(ok);
        product = bcp.getProduct();
        product.setActuatorType(ACTUATOR_TYPE_SOMFY);
        assertEquals(IGNORE_POSITION, product.getCurrentPosition());
        assertEquals(VANE_POSITION_A, product.getVanePosition());

        // test setting main position only
        ok = bcp.setNodeIdAndParameters(PRODUCT_INDEX_A, new VeluxProductPosition(MAIN_POSITION_A), null);
        assertTrue(ok);
        product = bcp.getProduct();
        product.setActuatorType(ACTUATOR_TYPE_SOMFY);
        assertEquals(MAIN_POSITION_A, product.getCurrentPosition());
        assertEquals(UNKNOWN_POSITION, product.getVanePosition());

        // test setting neither
        ok = bcp.setNodeIdAndParameters(PRODUCT_INDEX_A, null, null);
        assertFalse(ok);
    }

    /**
     * Test SCgetProductStatus exceptional error state processing.
     */
    @Test
    @Order(21)
    public void testErrorStateMapping() {
        // initialise the test parameters
        final String packet = "0F A3 01 06 01 00 01 02 00 9A 36 03 00 00 00 00 00 00 00 00 00 00 00 00 00"
                + " 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00"
                + " 00 00 00 00 00";
        final Command command = VeluxKLFAPI.Command.GW_STATUS_REQUEST_NTF;

        // initialise the BCP
        SCgetProductStatus bcp = new SCgetProductStatus();
        bcp.setProductId(PRODUCT_INDEX_A);

        // set the packet response
        bcp.setResponse(command.getShort(), toByteArray(packet), false);

        // check BCP status
        assertTrue(bcp.isCommunicationSuccessful());
        assertTrue(bcp.isCommunicationFinished());

        // check the product state
        assertEquals(ProductState.UNKNOWN.value, bcp.getProduct().getState());
    }
}
