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
public enum MoxCommandCode implements MoxCode {

		// REM
		GET_POWER_ACTIVE(0x2, 0x102, MoxSuboid.REM_DATA),
		GET_POWER_REACTIVE(0x3, 0x102, MoxSuboid.REM_DATA),
		GET_POWER_APARENT(0x4, 0x102, MoxSuboid.REM_DATA),
		GET_POWER_FACTOR(0x5, 0x102, MoxSuboid.REM_DATA),
		GET_POWER_ACTIVE_ENERGY(0x6, 0x102, MoxSuboid.REM_DATA),

		// Actor
		GET_ONOFF(0x1, 0x102, MoxSuboid.CHANNEL1),
		GET_LUMINOUS(0x3, 0x102, MoxSuboid.CHANNEL1),

		// Modify status
		SET_ONOFF(0x1, 0x203, MoxSuboid.CHANNEL1),
		SET_LUMINOUS(0x2, 0x206, MoxSuboid.CHANNEL1),
		INCREASE(0x1, 0x406, MoxSuboid.CHANNEL1),
		DECREASE(0x2, 0x406, MoxSuboid.CHANNEL1);
		
		private int low;
		private int high;
		private MoxSuboid suboid;
		
		MoxCommandCode(int low, int high, MoxSuboid suboid) {
			this.low = low;
			this.high = high;
			this.suboid = suboid;
		}
		
		/* (non-Javadoc)
		 * @see org.openhab.binding.mox.protocol.MoxCode#getLow()
		 */
		@Override
		public int getLow() {
			return low;
		}
		
		/* (non-Javadoc)
		 * @see org.openhab.binding.mox.protocol.MoxCode#getHigh()
		 */
		@Override
		public int getHigh() {
			return high;
		}
		
		public int getSuboid() {
			return suboid.getSuboid();
		}
		
		public static MoxCommandCode valueOf(int low, int high, int suboid) {
			for (MoxCommandCode code : MoxCommandCode.values()) {
				if (code.getLow() == low && code.getHigh() == high && code.getSuboid() == suboid) {
					return code;
				}
			}
			return null;
		}
		
	}
