/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.fileregexparser;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link FileRegexParserBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author meju25 - Initial contribution
 */
public class FileRegexParserBindingConstants {

    public static final String BINDING_ID = "fileregexparser";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_FILEREGEXPARSER = new ThingTypeUID(BINDING_ID, "filetoparse");

    // List of all Channel ids
    public final static String CHANNEL_GROUPCOUNT = "groupCount";
}
