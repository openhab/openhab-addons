/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonytv;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SonyTVBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Mikołaj Siedlarek - Initial contribution
 */
public class SonyTVBindingConstants {

    public static final String BINDING_ID = "sonytv";

    public final static ThingTypeUID THING_TYPE_BRAVIA = new ThingTypeUID(BINDING_ID, "bravia");

    public final static String CHANNEL_POWER = "power";
    public final static String CHANNEL_INPUT = "input";

}