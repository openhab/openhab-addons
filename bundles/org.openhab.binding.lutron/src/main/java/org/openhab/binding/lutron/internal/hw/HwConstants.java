/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.lutron.internal.hw;

import static org.openhab.binding.lutron.internal.LutronBindingConstants.BINDING_ID;

import org.openhab.core.thing.ThingTypeUID;

/**
 * Defines common constants, which are used across the whole binding.
 *
 * @author Andrew Shilliday - Initial contribution
 */
public class HwConstants {
    public static final ThingTypeUID THING_TYPE_HWSERIALBRIDGE = new ThingTypeUID(BINDING_ID, "hwserialbridge");
    public static final ThingTypeUID THING_TYPE_HWDIMMER = new ThingTypeUID(BINDING_ID, "hwdimmer");
}
