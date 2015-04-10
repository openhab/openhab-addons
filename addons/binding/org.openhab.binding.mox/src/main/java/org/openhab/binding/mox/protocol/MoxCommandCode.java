/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mox.protocol;

/**
 * @author Thomas Eichstaedt-Engelen (innoQ) - Initial contribution
 * @since 2.0.0
 */
public enum MoxCommandCode {

//		GET_POWER_ACTIVE(0x2, 0x102),
//		GET_POWER_REACTIVE(0x3, 0x102),
//		GET_POWER_APARENT(0x4, 0x102),
//		GET_POWER_FACTOR(0x5, 0x102),
//		GET_POWER_ACTIVE_ENERGY(0x6, 0x102),

		POWER_ACTIVE(0x2, 0x306),
		POWER_REACTIVE(0x3, 0x306),
		POWER_APPARENT(0x4, 0x306),
		POWER_FACTOR(0x5, 0x306),
		POWER_ACTIVE_ENERGY(0x6, 0x306),

		// Query and receive actor status
//		GET(0x1, 0x102),
		LUMINOUS_GET(0x3, 0x304), // Docs fail: says 0x102
		STATUS(0x1, 0x303),

		// Modify status
		ONOFF(0x1, 0x203),
		LUMINOUS_SET(0x2, 0x206),
		INCREASE(0x1, 0x406),
		DECREASE(0x2, 0x406);
		
		private int low;
		private int high;
		
		MoxCommandCode(int low, int high) {
			this.low = low;
			this.high = high;
		}
		
		public int getLow() {
			return low;
		}
		
		public int getHigh() {
			return high;
		}
		
		public static MoxCommandCode valueOf(int low, int high) {
			for (MoxCommandCode code : MoxCommandCode.values()) {
				if (code.getLow() == low && code.getHigh() == high) {
					return code;
				}
			}
			throw new IllegalArgumentException("There is no CommandCode for low=" + low + ", high=" + high);
		}
		
	}
