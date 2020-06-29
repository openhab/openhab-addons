/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.daikinmadoka;

import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;

/**
 * The {@link DaikinMadokaBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Benjamin Lafois - Initial contribution
 */
@NonNullByDefault
public class DaikinMadokaBindingConstants {

    private DaikinMadokaBindingConstants() {
    }

    public static final ThingTypeUID THING_TYPE_BRC1H = new ThingTypeUID(BluetoothBindingConstants.BINDING_ID, "brc1h");

    public static final String CHANNEL_ID_ONOFF_STATUS = "onOffStatus";
    public static final String CHANNEL_ID_INDOOR_TEMPERATURE = "indoorTemperature";
    public static final String CHANNEL_ID_OUTDOOR_TEMPERATURE = "outdoorTemperature";
    public static final String CHANNEL_ID_COMMUNICATION_CONTROLLER_VERSION = "commCtrlVersion";
    public static final String CHANNEL_ID_REMOTE_CONTROLLER_VERSION = "remoteCtrlVersion";

    public static final String CHANNEL_ID_OPERATION_MODE = "operationMode";
    public static final String CHANNEL_ID_FAN_SPEED = "fanSpeed";
    public static final String CHANNEL_ID_SETPOINT = "setpoint";
    public static final String CHANNEL_ID_HOMEKIT_CURRENT_HEATING_COOLING_MODE = "homekitCurrentHeatingCoolingMode";
    public static final String CHANNEL_ID_HOMEKIT_TARGET_HEATING_COOLING_MODE = "homekitTargetHeatingCoolingMode";
    public static final String CHANNEL_ID_HOMEBRIDGE_MODE = "homebridgeMode";

    /**
     * BLUETOOTH UUID (service + chars)
     */
    public static final UUID SERVICE_UART_UUID = UUID.fromString("2141E110-213A-11E6-B67B-9E71128CAE77");
    public static final UUID CHAR_WRITE_WITHOUT_RESPONSE_UUID = UUID.fromString("2141E112-213A-11E6-B67B-9E71128CAE77");
    public static final UUID CHAR_NOTIF_UUID = UUID.fromString("2141E111-213A-11E6-B67B-9E71128CAE77");

}
