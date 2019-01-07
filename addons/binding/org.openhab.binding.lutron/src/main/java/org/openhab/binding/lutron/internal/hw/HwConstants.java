/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lutron.internal.hw;

import static org.openhab.binding.lutron.internal.LutronBindingConstants.BINDING_ID;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * Defines common constants, which are used across the whole binding.
 *
 * @author Andrew Shilliday - Initial contribution
 */
public class HwConstants {
    public static final ThingTypeUID THING_TYPE_HWSERIALBRIDGE = new ThingTypeUID(BINDING_ID, "hwserialbridge");
    public static final ThingTypeUID THING_TYPE_HWDIMMER = new ThingTypeUID(BINDING_ID, "hwdimmer");
}
