/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.homepilot.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * @author Steffen Stundzig - Initial contribution
 */
public class HomePilotDevice {

	private final ThingTypeUID thingTypeUID;
	private final String deviceId;
	private final String name;
	private final String description;
	private final Integer position;

	public HomePilotDevice(ThingTypeUID thingTypeUID, Integer deviceId, String name, String description,
			Integer position) {
		this.position = position;
		this.thingTypeUID = thingTypeUID;
		this.deviceId = Integer.toString(deviceId);
		this.name = name;
		this.description = description;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Integer getPosition() {
		return position;
	}

	public ThingTypeUID getTypeUID() {
		return thingTypeUID;
	}
}
