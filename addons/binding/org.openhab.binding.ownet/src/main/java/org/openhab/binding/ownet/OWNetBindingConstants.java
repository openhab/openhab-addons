/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ownet;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link OWNetBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dmitry Krasnov - Initial contribution
 */
public class OWNetBindingConstants {

    public static final String BINDING_ID = "ownet";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_TCP = new ThingTypeUID(BINDING_ID, "tcp");
    public final static ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    public final static ThingTypeUID THING_TYPE12 = new ThingTypeUID(BINDING_ID, "12");
    public final static ThingTypeUID THING_TYPE26 = new ThingTypeUID(BINDING_ID, "26");
    public final static ThingTypeUID THING_TYPE28 = new ThingTypeUID(BINDING_ID, "28");
    public final static ThingTypeUID THING_TYPE29 = new ThingTypeUID(BINDING_ID, "29");
    public final static ThingTypeUID THING_TYPE3A = new ThingTypeUID(BINDING_ID, "3A");

}
