/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rotelra1x;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link RotelRa1xBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marius Bjørnstad - Initial contribution
 */
public class RotelRa1xBindingConstants {

    public static final String BINDING_ID = "rotelra1x";

    public final static ThingTypeUID THING_TYPE_AMP = new ThingTypeUID(BINDING_ID, "amp");

}
