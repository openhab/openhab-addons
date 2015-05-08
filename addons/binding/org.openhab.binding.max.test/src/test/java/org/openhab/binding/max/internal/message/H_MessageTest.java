/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.message;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests cases for {@link H_Message}.
 * 
 * @author Marcel Verpaalen - Initial Version
 * @since 2.0
 */
public class H_MessageTest {

	public final String rawData = "H:KEQ0565026,0b5951,0113,00000000,4eed6795,01,32,0f0113,0f34,03,0000";

	private H_Message message = null;

	@Before
	public void Before() {
		message = new H_Message(rawData);
	}

	@Test
	public void getMessageTypeTest() {

		MessageType messageType = ((Message) message).getType();

		assertEquals(MessageType.H, messageType);
	}

	@Test
	public void getRFAddressTest() {

		String rfAddress = message.getRFAddress();

		assertEquals("0b5951", rfAddress);
	}

	@Test
	public void getFirmwareTest() {

		String firmware = message.getFirmwareVersion();

		assertEquals("01.13", firmware);
	}

	@Test
	public void getConnectionIdTest() {

		String connectionId = message.getConnectionId();

		assertEquals("4eed6795", connectionId);
	}

	@Test
	public void getCubeTimeStateTest() {

		String cubeTimeState = message.getCubeTimeState();

		assertEquals("03", cubeTimeState);
	}
	
	@Test
	public void getNTPCounterTest() {

		String ntpCounter = message.getNTPCounter();

		assertEquals("0", ntpCounter);
	}
	
	@Test
	public void getSerialNumberTest() {
		String serialNumber = message.getSerialNumber();

		assertEquals("KEQ0565026", serialNumber);
	}
}