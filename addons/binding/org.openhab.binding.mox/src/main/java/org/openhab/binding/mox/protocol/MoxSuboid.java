/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mox.protocol;


/**
 * @author Sebastian Janzen (innoQ)
 * @since 2.0.0
 */
public enum MoxSuboid {

	CHANNEL1(0x11),
	CHANNEL2(0x12),
	CHANNEL3(0x11),
	CHANNEL4(0x11),
	CHANNEL5(0x11),
	CHANNEL6(0x11),
	CHANNEL7(0x11),
	CHANNEL8(0x11),

	REM_DATA( 0x31);

	private int suboid;

	MoxSuboid(int suboid) {
		this.suboid = suboid;
	}

	public int getSuboid() {
		return suboid;
	}
}
