/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.digitalstrom.internal;

import org.openhab.binding.digitalstrom.internal.lib.structure.scene.constants.SceneTypes;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link DigitalSTROMBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Ochel - Initial contribution
 * @author Mathias Siegele - Initial contribution
 */
public class DigitalSTROMBindingConstants {

    public static final String BINDING_ID = "digitalstrom";

    // List of all Thing Type IDs
    public static final String THING_TYPE_ID_DSS_BRIDGE = "dssBridge";
    public static final String THING_TYPE_ID_ZONE_TEMERATURE_CONTROL = "zoneTemperatureControl";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_DSS_BRIDGE = new ThingTypeUID(BINDING_ID, THING_TYPE_ID_DSS_BRIDGE);
    public static final ThingTypeUID THING_TYPE_ZONE_TEMERATURE_CONTROL = new ThingTypeUID(BINDING_ID,
            THING_TYPE_ID_ZONE_TEMERATURE_CONTROL);

    public static final ThingTypeUID THING_TYPE_APP_SCENE = new ThingTypeUID(BINDING_ID, SceneTypes.APARTMENT_SCENE);
    public static final ThingTypeUID THING_TYPE_ZONE_SCENE = new ThingTypeUID(BINDING_ID, SceneTypes.ZONE_SCENE);
    public static final ThingTypeUID THING_TYPE_GROUP_SCENE = new ThingTypeUID(BINDING_ID, SceneTypes.GROUP_SCENE);
    public static final ThingTypeUID THING_TYPE_NAMED_SCENE = new ThingTypeUID(BINDING_ID, SceneTypes.NAMED_SCENE);

    // scene
    public static final String CHANNEL_ID_SCENE = "scene";

    // sensor
    public static final String CHANNEL_ID_TOTAL_ACTIVE_POWER = "totalActivePower";
    public static final String CHANNEL_ID_TOTAL_ELECTRIC_METER = "totalElectricMeter";

    // options combined switches
    public static final String OPTION_COMBINED_BOTH_OFF = "0";
    public static final String OPTION_COMBINED_BOTH_ON = "200";
    public static final String OPTION_COMBINED_FIRST_ON = "90";
    public static final String OPTION_COMBINED_SECOND_ON = "130";

    /* Bridge config properties */

    public static final String HOST = "dSSAddress";
    public static final String USER_NAME = "userName";
    public static final String PASSWORD = "password";
    public static final String APPLICATION_TOKEN = "applicationToken";
    public static final String DS_ID = "dSID";
    public static final String DS_NAME = "dS-Installation-Name";
    public static final String SENSOR_DATA_UPDATE_INTERVAL = "sensorDataUpdateInterval";
    public static final String TOTAL_POWER_UPDATE_INTERVAL = "totalPowerUpdateInterval";
    public static final String DEFAULT_TRASH_DEVICE_DELETE_TIME_KEY = "defaultTrashBinDeleteTime";
    public static final String SENSOR_WAIT_TIME = "sensorWaitTime";

    public static final String SERVER_CERT = "serverCert";

    /* Device info properties */

    public static final String DEVICE_UID = "dSUID";
    public static final String DEVICE_NAME = "deviceName";
    public static final String DEVICE_DSID = "dSID";
    public static final String DEVICE_HW_INFO = "hwInfo";
    public static final String DEVICE_ZONE_ID = "zoneID";
    public static final String DEVICE_GROUPS = "groups";
    public static final String DEVICE_OUTPUT_MODE = "outputmode";
    public static final String DEVICE_FUNCTIONAL_COLOR_GROUP = "funcColorGroup";
    public static final String DEVICE_METER_ID = "meterDSID";
    public static final String DEVICE_BINARAY_INPUTS = "binarayInputs";

    // Device properties scene
    public static final String DEVICE_SCENE = "scene"; // + number of scene

    // Sensor data channel properties
    public static final String ACTIVE_POWER_REFRESH_PRIORITY = "activePowerRefreshPriority";
    public static final String ELECTRIC_METER_REFRESH_PRIORITY = "electricMeterRefreshPriority";
    public static final String OUTPUT_CURRENT_REFRESH_PRIORITY = "outputCurrentRefreshPriority";

    /* Scene config */
    public static final String ZONE_ID = "zoneID";
    public static final String GROUP_ID = "groupID";
    public static final String SCENE_ID = "sceneID";

    // circuit properties
    public static final String HW_NAME = "hwName";
    public static final String HW_VERSION = "hwVersion";
    public static final String SW_VERSION = "swVersion";
    public static final String API_VERSION = "apiVersion";
    public static final String DSP_SW_VERSION = "armSwVersion";
    public static final String ARM_SW_VERSION = "dspSwVersion";
}
