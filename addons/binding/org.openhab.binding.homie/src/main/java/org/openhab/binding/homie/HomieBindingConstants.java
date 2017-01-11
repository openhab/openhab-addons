/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homie;

import java.util.regex.Pattern;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link HomieBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Kolb - Initial contribution
 */
public class HomieBindingConstants {

    public static final String BINDING_ID = "homie";

    // List of all Thing Type UIDs
    public final static ThingTypeUID HOMIE_THING_TYPE = new ThingTypeUID(BINDING_ID, "homieNode");

    public final static int DISCOVERY_TIMEOUT_SECONDS = 5;

    public final static String BASETOPIC = "homie";

    public final static Pattern HOMIE_ID_REGEX = Pattern.compile("\\/([a-z0-9]([a-z0-9\\\\-]+[a-z0-9])?)\\/\\$");
}
