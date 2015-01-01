/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonos.internal;

/**
 * The {@link SonosZoneGroup} is data structure to describe
 * state of a Zone Player 
 * 
 * @author Karel Goderis - Initial contribution
 */
public class SonosZonePlayerState {

	public String transportState;
	public String volume;
	public String relTime;
	public SonosEntry entry;
	public long track;
	
}
