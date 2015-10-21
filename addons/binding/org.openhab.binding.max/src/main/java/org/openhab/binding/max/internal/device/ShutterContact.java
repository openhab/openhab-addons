/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.device;

import org.eclipse.smarthome.core.library.types.OpenClosedType;

/**
 * MAX!Cube Shutter contact device.
 * 
 * @author Andreas Heil (info@aheil.de)
 * @author Marcel Verpaalen - OH2 update
 * @since 1.4.0
 */
public class ShutterContact extends Device {

	private OpenClosedType shutterState = null;

	public ShutterContact(DeviceConfiguration c) {
		super(c);
	}

	public void setShutterState(OpenClosedType shutterState) {
		if(this.shutterState != shutterState ) setUpdated (true);
		this.shutterState = shutterState;
	}

	public OpenClosedType getShutterState() {
		return shutterState;
	}

	@Override
	public DeviceType getType() {
		return DeviceType.ShutterContact;
	}

}
