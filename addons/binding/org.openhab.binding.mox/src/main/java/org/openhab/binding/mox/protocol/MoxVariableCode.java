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
public enum MoxVariableCode {
	
	ADDRESS(0x1),
	SUBOID(0x11),
	CMD_SET_CODE(0x204),
	CMD_GET_CODE(0x102);
	
	private int code;
	
	private MoxVariableCode(int code) {
		this.code = code;
	}
	
	public int getCode() {
		return code;
	}
	
}
