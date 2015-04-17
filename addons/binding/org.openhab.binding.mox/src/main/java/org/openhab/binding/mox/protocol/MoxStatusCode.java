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
 * @author Sebastian Janzen (innoQ)
 * @since 2.0.0
 */
public enum MoxStatusCode implements MoxCode {

		POWER_ACTIVE(0x2, 0x306),
		POWER_REACTIVE(0x3, 0x306),
		POWER_APPARENT(0x4, 0x306),
		POWER_FACTOR(0x5, 0x306),
		POWER_ACTIVE_ENERGY(0x6, 0x306),

		LUMINOUS(0x1, 0x303),
		ONOFF(0x1, 0x303);
		

		private int low;
		private int high;
		
		MoxStatusCode(int low, int high) {
			this.low = low;
			this.high = high;
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
		
		public static MoxStatusCode valueOf(int low, int high) {
			for (MoxStatusCode code : MoxStatusCode.values()) {
				if (code.getLow() == low && code.getHigh() == high) {
					return code;
				}
			}
			//throw new IllegalArgumentException("There is no StatusCode for low=" + low + ", high=" + high);
			return null;
		}
		
	}
