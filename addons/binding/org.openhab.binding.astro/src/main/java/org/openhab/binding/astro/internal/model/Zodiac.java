/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro.internal.model;

/**
 * Holds the sign of the zodiac.
 * 
 * @author Gerhard Riegler - Initial contribution
 */
public class Zodiac {
	private ZodiacSign sign;

	public Zodiac(ZodiacSign sign) {
		this.sign = sign;
	}

	/**
	 * Returns the sign of the zodiac.
	 */
	public ZodiacSign getSign() {
		return sign;
	}

}
