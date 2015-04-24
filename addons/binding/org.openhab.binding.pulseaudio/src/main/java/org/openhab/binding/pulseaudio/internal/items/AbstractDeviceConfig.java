/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pulseaudio.internal.items;

/**
 * Abstract root class for all items in an pulseaudio server. Every item in a
 * pulseaudio server has a name and a unique id which can be inherited by this
 * class.
 * 
 * @author Tobias Bräutigam
 * @since 1.2.0
 */
public abstract class AbstractDeviceConfig {

	protected int id;
	protected String name;
	
	public AbstractDeviceConfig(int id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * returns the internal id of this device
	 * @return
	 */
	public int getId() {
		return id;
	}

	public String getUIDName() {
		return name.replaceAll("[^A-Za-z0-9_]", "_");
	}
	public String getPaName() {
		return name;
	}
}
