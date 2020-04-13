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
package org.openhab.binding.bluetooth.daikinmadoka.handler;

/**
 *
 * @author blafois
 *
 */
public class UARTQueueTester {

    // @Test
    // public void testExpectedMessageChunk6() {
    // DaikinMadokaHandler h = new DaikinMadokaHandler(new DummyThing());
    // BluetoothCharacteristic goodCharacteristic = new BluetoothCharacteristic(
    // UUID.fromString(DaikinMadokaBindingConstants.CHAR_NOTIF_UUID), 0);
    //
    // goodCharacteristic.setValue(new byte[] { 0x00, 38 });
    // h.onCharacteristicUpdate(goodCharacteristic);
    // goodCharacteristic.setValue(new byte[] { 0x02, 0x14 });
    // h.onCharacteristicUpdate(goodCharacteristic);
    // assertFalse(h.isMessageComplete());
    //
    // }
    //
    // @Test
    // public void testExpectedMessageChunk5() {
    // DaikinMadokaHandler h = new DaikinMadokaHandler(new DummyThing());
    // BluetoothCharacteristic goodCharacteristic = new BluetoothCharacteristic(
    // UUID.fromString(DaikinMadokaBindingConstants.CHAR_NOTIF_UUID), 0);
    //
    // goodCharacteristic.setValue(new byte[] { 0x00, 0x14 });
    // h.onCharacteristicUpdate(goodCharacteristic);
    // assertFalse(h.isMessageComplete());
    //
    // }
    //
    // @Test
    // public void testExpectedMessageChunks4() {
    // DaikinMadokaHandler h = new DaikinMadokaHandler(new DummyThing());
    // BluetoothCharacteristic goodCharacteristic = new BluetoothCharacteristic(
    // UUID.fromString(DaikinMadokaBindingConstants.CHAR_NOTIF_UUID), 0);
    //
    // goodCharacteristic.setValue(new byte[] { 0x00, 0x13 });
    // h.onCharacteristicUpdate(goodCharacteristic);
    // assertTrue(h.isMessageComplete());
    //
    // }
    //
    // @Test
    // public void testExpectedMessageChunks3() {
    // DaikinMadokaHandler h = new DaikinMadokaHandler(new DummyThing());
    // BluetoothCharacteristic goodCharacteristic = new BluetoothCharacteristic(
    // UUID.fromString(DaikinMadokaBindingConstants.CHAR_NOTIF_UUID), 0);
    //
    // goodCharacteristic.setValue(new byte[] { 0x00, 0x00 });
    // h.onCharacteristicUpdate(goodCharacteristic);
    // goodCharacteristic.setValue(new byte[] { 0x00, 0x14 });
    // h.onCharacteristicUpdate(goodCharacteristic);
    //
    // assertFalse(h.isMessageComplete());
    //
    // }
    //
    // @Test
    // public void testExpectedMessageChunks2() {
    // DaikinMadokaHandler h = new DaikinMadokaHandler(new DummyThing());
    // BluetoothCharacteristic goodCharacteristic = new BluetoothCharacteristic(
    // UUID.fromString(DaikinMadokaBindingConstants.CHAR_NOTIF_UUID), 0);
    //
    // goodCharacteristic.setValue(new byte[] { 0x01, 0x00 });
    // h.onCharacteristicUpdate(goodCharacteristic);
    // assertFalse(h.isMessageComplete());
    //
    // }
    //
    // @Test
    // public void testExpectedMessageChunks1() {
    // DaikinMadokaHandler h = new DaikinMadokaHandler(new DummyThing());
    // BluetoothCharacteristic goodCharacteristic = new BluetoothCharacteristic(
    // UUID.fromString(DaikinMadokaBindingConstants.CHAR_NOTIF_UUID), 0);
    //
    // // Insert 2 messages and check POP Order
    // goodCharacteristic.setValue(new byte[] { 0x00, 0x14 });
    // h.onCharacteristicUpdate(goodCharacteristic);
    // goodCharacteristic.setValue(new byte[] { 0x01, 0x00 });
    // h.onCharacteristicUpdate(goodCharacteristic);
    //
    // assertEquals(0, h.popQueueMessage()[0]);
    // assertEquals(1, h.popQueueMessage()[0]);
    //
    // goodCharacteristic.setValue(new byte[] { 0x01, 0x00 });
    // h.onCharacteristicUpdate(goodCharacteristic);
    // goodCharacteristic.setValue(new byte[] { 0x00, 0x14 });
    // h.onCharacteristicUpdate(goodCharacteristic);
    //
    // assertTrue(h.isMessageComplete());
    //
    // assertEquals(0, h.popQueueMessage()[0]);
    // assertEquals(1, h.popQueueMessage()[0]);
    //
    // }
    //
    // @Test
    // public void testExpectedMessageChunks0() {
    //
    // DaikinMadokaHandler h = new DaikinMadokaHandler(new DummyThing());
    //
    // // Test No Message has arrived yet
    // assertFalse(h.isMessageComplete());
    //
    // // Add a malformed message
    // h.onCharacteristicUpdate(new BluetoothCharacteristic(null, 0));
    //
    // // Add a message from a wrong characteristic
    // BluetoothCharacteristic badCharacteristic = new BluetoothCharacteristic(
    // UUID.fromString(DaikinMadokaBindingConstants.CHAR_WRITE_WITHOUT_RESPONSE_UUID), 0);
    // h.onCharacteristicUpdate(badCharacteristic);
    //
    // BluetoothCharacteristic goodCharacteristic = new BluetoothCharacteristic(
    // UUID.fromString(DaikinMadokaBindingConstants.CHAR_NOTIF_UUID), 0);
    // h.onCharacteristicUpdate(goodCharacteristic);
    //
    // goodCharacteristic.setValue(new byte[] { 0x00, 0x01 });
    // h.onCharacteristicUpdate(goodCharacteristic);
    //
    // assertEquals(0, h.popQueueMessage()[0]);
    //
    // }

}
