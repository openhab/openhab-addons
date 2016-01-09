/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.passthru;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link PassthruBinding} class defines common constants, which are used
 * across the whole binding.
 *
 * @author J. Geyer - Initial contribution
 */
public class PassthruBindingConstants {

    public static final String BINDING_ID = "passthru";

    // List of all Thing Type UIDs
    public final static ThingTypeUID PASSTHRU_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "device");

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(PASSTHRU_THING_TYPE_UID);
}
