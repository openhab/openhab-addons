/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.magnetpoesie;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link MagnetpoesieBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Yasemin Dogan - Initial contribution
 */
public class MagnetpoesieBindingConstants {

    public static final String BINDING_ID = "magnetpoesie";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "magnetpoesie");

    // List of all Channel ids
    public final static String SAVE = "finalSave";
    public final static String TAFEL = "tafel";

    public static final String ITEM_TYPE_STRING = "String";

    public static final String CATEGORIE_WHO = "who";
    public static final String CATEGORIE_WHAT = "what";
    public static final String CATEGORIE_WHERE = "where";
    public static final String CATEGORIE_WHEN = "when";
    public static final String CATEGORIE_GENERAL = "general";

    public static final String PROPERTY_WHO_TYPE = "whoType";
    public static final String PROPERTY_WHAT_TYPE = "whatType";
    public static final String PROPERTY_WHERE_TYPE = "whereType";
    public static final String PROPERTY_WHEN_TYPE = "whenType";
    public static final String PROPERTY_GENERAL_TYPE = "generalType";

}
