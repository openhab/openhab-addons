/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.folding;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link FoldingBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author FaMaKe - Initial contribution
 */
public class FoldingBindingConstants {

    public static final String BINDING_ID = "folding";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_CLIENT = new ThingTypeUID(BINDING_ID, "client");
    public final static ThingTypeUID THING_TYPE_SLOT = new ThingTypeUID(BINDING_ID, "slot");

    public final static String PARAM_SLOT_ID = "id";

    // List of all Channel ids
    public final static String CHANNEL_STATUS = "status";

}
