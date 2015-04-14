/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mox.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Thomas Eichstaedt-Engelen (innoQ)
 * @since 2.0.0
 */
public class MoxSuboidMapping {

	private static Map<Integer, String> subOids = new HashMap<Integer, String>();

	static {
		subOids.put(17, "Channel 1"); // 0x11
		subOids.put(18, "Channel 2"); // 0x12
		subOids.put(19, "Channel 3"); // 0x13
		subOids.put(20, "Channel 4"); // 0x14
		subOids.put(21, "Channel 5"); // 0,15
		subOids.put(22, "Channel 6"); // 0x16
		subOids.put(23, "Channel 7"); // 0x17
		subOids.put(24, "Channel 8"); // 0x18
		subOids.put(49, "REM Data"); // 0x31
	}

}
