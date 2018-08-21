/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc2;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;

/**
 * The {@link Ihc2BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Niels Peter Enemark - Initial contribution
 */
@NonNullByDefault
public class Ihc2BindingConstants {

    /**
     *
     */
    private static final String BINDING_ID = "ihc2";
    private static final String CONTROLLER_NAME = "controller";
    private static final String CONTROLLER_ID = "1811";

    public static final ThingUID THE_IHC_CONTROLLER_UID = new ThingUID(BINDING_ID, CONTROLLER_NAME, CONTROLLER_ID);

    /**
     * Bridge Type UIDs
     */
    public static final ThingTypeUID THING_TYPE_IHCCONTROLLER = new ThingTypeUID(BINDING_ID, CONTROLLER_NAME);

    public static final ThingTypeUID THING_TYPE_NUMBER = new ThingTypeUID(BINDING_ID, "number");
    public static final ThingTypeUID THING_TYPE_STRING = new ThingTypeUID(BINDING_ID, "string");
    public static final ThingTypeUID THING_TYPE_DATETIME = new ThingTypeUID(BINDING_ID, "datetime");
    public static final ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");

    public static final ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");
    public static final ThingTypeUID THING_TYPE_CONTACT = new ThingTypeUID(BINDING_ID, "contact");

    public static final ThingTypeUID THING_TYPE_MULTI_CHANNEL = new ThingTypeUID(BINDING_ID, "multichannel");

    /**
     * List of all Channel ids (postCommand())
     */
    public static final String CHANNEL_SWITCH = "switchStatus";
    public static final String CHANNEL_CONTACT = "contactStatus";
    public static final String CHANNEL_PERCENT = "percentValue";

    public static final String CHANNEL_DATETIME = "dateTimeValue";
    public static final String CHANNEL_NUMBER = "numberValue";
    public static final String CHANNEL_STRING = "stringValue";

    /**
     * List of all Channel Types (<channel-type id="">)
     */
    public static final String CHANNEL_TYPE_SWITCH = "switchState";
    public static final String CHANNEL_TYPE_CONTACT = "contactState";
    public static final String CHANNEL_TYPE_PERCENT = "percent";

    public static final String CHANNEL_TYPE_DATETIME = "dateTime";
    public static final String CHANNEL_TYPE_NUMBER = "number";
    public static final String CHANNEL_TYPE_STRING = "string";

}
