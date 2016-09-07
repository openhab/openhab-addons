/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazondashbutton;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link AmazonDashButtonBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Oliver Libutzki <oliver@libutzki.de> - Initial contribution
 */
public class AmazonDashButtonBindingConstants {

    public static final String BINDING_ID = "amazondashbutton";

    // List of all Thing Type UIDs
    public final static ThingTypeUID DASH_BUTTON_THING_TYPE = new ThingTypeUID(BINDING_ID, "dashbutton");

    // List of all Channel ids
    public final static String PRESS = "press";

}
