/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.edimax;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link EdimaxBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Falk Harnisch - Initial contribution
 */
public class EdimaxBindingConstants {

    public static final String BINDING_ID = "edimax";

    // List of all Thing Type UIDs
    public final static ThingTypeUID SP1101W = new ThingTypeUID(BINDING_ID, "sp1101w");
    public final static ThingTypeUID SP2101W = new ThingTypeUID(BINDING_ID, "sp2101w");

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(SP1101W, SP2101W);

    // List of all Channel ids
    public final static String SWITCH_CHANNEL = "switch-channel";
    public final static String ENERGY_CHANNEL = "energy-channel";
    public final static String POWER_CHANNEL = "power-channel";

}
