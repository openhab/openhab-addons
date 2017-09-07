/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nibeuplink;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link NibeUplinkBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author afriese - Initial contribution
 */
public final class NibeUplinkBindingConstants {

    private static final String BINDING_ID = "nibeuplink";

    // List of main device types
<<<<<<< .mine
    public static final String DEVICE_VVM320 = "vvm320";
    public static final String DEVICE_VVM320_SENSORS = "vvm320-sensors";
    public static final String DEVICE_VVM320_SETTINGS = "vvm320-settings";
    public static final String DEVICE_VVM320_SPECIAL = "vvm320-special";
=======
    public static final String DEVICE_WEBIF = "web";
>>>>>>> .r378

    // List of all Thing Type UIDs
<<<<<<< .mine
    public static final ThingTypeUID THING_TYPE_VVM320 = new ThingTypeUID(BINDING_ID, DEVICE_VVM320);
    public static final ThingTypeUID THING_TYPE_VVM320_SENSORS = new ThingTypeUID(BINDING_ID, DEVICE_VVM320_SENSORS);
    public static final ThingTypeUID THING_TYPE_VVM320_SETTINGS = new ThingTypeUID(BINDING_ID, DEVICE_VVM320_SETTINGS);
    public static final ThingTypeUID THING_TYPE_VVM320_SPECIAL = new ThingTypeUID(BINDING_ID, DEVICE_VVM320_SPECIAL);
=======
    public static final ThingTypeUID THING_TYPE_WEBIF = new ThingTypeUID(BINDING_ID, DEVICE_WEBIF);
>>>>>>> .r378

    // List of all Channel ids ==> see UplinkDataChannels

    // URLs
    public static final String LOGIN_URL = "https://www.nibeuplink.com/LogIn";
    public static final String DATA_API_URL = "https://www.nibeuplink.com/PrivateAPI/Values";

    // login field names
    public static final String LOGIN_FIELD_PASSWORD = "Password";
    public static final String LOGIN_FIELD_EMAIL = "Email";
    public static final String LOGIN_FIELD_RETURN_URL = "returnUrl";

    // other field names
    public static final String DATA_API_FIELD_LAST_DATE = "currentWebDate";
    public static final String DATA_API_FIELD_LAST_DATE_DEFAULT_VALUE = "01.01.2017 13:37:42";
    public static final String DATA_API_FIELD_ID = "hpid";
    public static final String DATA_API_FIELD_DATA = "variables";
    public static final String DATA_API_FIELD_DATA_DEFAULT_VALUE = "0";

}
