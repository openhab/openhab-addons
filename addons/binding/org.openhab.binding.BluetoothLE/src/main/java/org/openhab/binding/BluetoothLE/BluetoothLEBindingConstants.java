/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.BluetoothLE;

import java.util.Set;
import java.util.UUID;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link BluetoothLE} class defines common constants, which are 
 * used across the whole binding.
 * 
 * @author Patrick Ammann - Initial contribution
 */
public class BluetoothLEBindingConstants {
	public static final String BINDING_ID = "BluetoothLE";
	
	public final static ThingTypeUID THING_TYPE_TemperatureSensor = new ThingTypeUID(BINDING_ID, "TemperatureSensor");
	// List all channels
	public static final String CHANNEL_TEMPERATURE = "temperature";
	public static final String CHANNEL_BATTERY     = "battery";
	
	
	public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(
			THING_TYPE_TemperatureSensor
			);
	
	
	// UUIDs (see https://developer.bluetooth.org/gatt/services/Pages/ServicesHome.aspx)
	public final static UUID UUID_HEALTH_THERMOMETER = new UUID(0, 0x1809);
	public final static UUID UUID_BATTERY            = new UUID(0, 0x180F);
}
