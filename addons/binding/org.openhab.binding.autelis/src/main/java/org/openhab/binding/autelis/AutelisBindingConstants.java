/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.autelis;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link AutelisBinding} class defines common constants, which are used
 * across the whole binding.
 * 
 * @author Dan Cunningham - Initial contribution
 */
public class AutelisBindingConstants {

	public static final String BINDING_ID = "autelis";

	// List of all Thing Type UIDs
	public final static ThingTypeUID POOLCONTROL_THING_TYPE_UID = new ThingTypeUID(
			BINDING_ID, "poolcontrol");

	public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet
			.of(POOLCONTROL_THING_TYPE_UID);
}
