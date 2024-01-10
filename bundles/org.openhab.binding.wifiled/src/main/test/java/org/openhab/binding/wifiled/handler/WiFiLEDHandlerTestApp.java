/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.wifiled.handler;

import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;

import java.io.IOException;

/**
 * Test app for the fading driver.
 *
 * @author Stefan Endrullis
 */
public class WiFiLEDHandlerTestApp {

	private static AbstractWiFiLEDDriver driver;

	public static void main(String[] args) throws IOException, InterruptedException {
		String ip = "192.168.178.91";
		Integer port = AbstractWiFiLEDDriver.DEFAULT_PORT;
		AbstractWiFiLEDDriver.Protocol protocol = AbstractWiFiLEDDriver.Protocol.LD686;

		boolean fadingDriver = false;

		System.out.println("start");

		driver = fadingDriver ?
				new FadingWiFiLEDDriver(ip, port, protocol, 0, 1) :
				new ClassicWiFiLEDDriver(this, ip, port, protocol);

		System.out.println("driver created");

		driver.init();

		System.out.println("driver initialized");

		testStateChanges();
		//testFrequentStateChanges();

		System.exit(0);
	}

	private static void testStateChanges() throws IOException, InterruptedException {
		driver.setPower(OnOffType.OFF);

		System.out.println("off");

		Thread.sleep(500);

		driver.setPower(OnOffType.ON);
		driver.setWhite(PercentType.HUNDRED);
		assertState("ON,0,0,0,100");

		Thread.sleep(4000);
		assertState("ON,0,0,0,100");

		driver.setColor(HSBType.BLUE);
		assertState("ON,240,100,100,100");

		Thread.sleep(4000);
		assertState("ON,240,100,100,100");

		driver.setWhite(PercentType.ZERO);
		assertState("ON,240,100,100,0");

		Thread.sleep(4000);
		assertState("ON,240,100,100,0");

		driver.setColor(HSBType.GREEN);
		driver.setWhite(PercentType.ZERO);
		System.out.println("g: " + driver.getLEDStateDTO());

		Thread.sleep(4000);
		System.out.println("g: " + driver.getLEDStateDTO());

		driver.setColor(HSBType.RED);
		driver.setWhite(PercentType.ZERO);
		System.out.println("r: " + driver.getLEDStateDTO());

		Thread.sleep(4000);
		System.out.println("r: " + driver.getLEDStateDTO());

		driver.setColor(HSBType.fromRGB(255, 32, 0));
		driver.setWhite(new PercentType(14));
		System.out.println("c: " + driver.getLEDStateDTO());

		Thread.sleep(4000);
		System.out.println("c: " + driver.getLEDStateDTO());

		driver.setPower(OnOffType.OFF);
		System.out.println("o: " + driver.getLEDStateDTO());

		Thread.sleep(4000);
		System.out.println("o: " + driver.getLEDStateDTO());
	}

	private static void testFrequentStateChanges() throws IOException, InterruptedException {
		driver.setPower(OnOffType.ON);
		driver.setWhite(PercentType.ZERO);

		for (int i = 0; i < 100; i++) {
			driver.setColor(HSBType.BLUE);
			Thread.sleep(100);
			driver.setColor(HSBType.RED);
			Thread.sleep(100);
		}
	}

	private static void assertState(String state) throws IOException {
		if (!driver.getLEDStateDTO().toString().equals(state + " [0,100]")) {
			//throw new RuntimeException("Expected: " + state + " [0,100]; actually: " + driver.getLEDStateDTO().toString());
		}
	}

}
