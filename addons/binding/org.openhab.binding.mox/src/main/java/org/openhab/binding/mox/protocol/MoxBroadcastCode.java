/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mox.protocol;


/**
 * @author Thomas Eichstaedt-Engelen (innoQ)
 * @since 2.0.0
 */
public enum MoxBroadcastCode {
	
	ADDRESS(0x0, -1),
	SUBOID(0x2, -1),
	CMD_CODE(0x1, 0x406);
			
	private int low;
	private int high;
	
	MoxBroadcastCode(int low, int high) {
		this.low = low;
		this.high = high;
	}
	
	public int getLow() {
		return low;
	}
	
	public int getHigh() {
		return high;
	}
	
}
