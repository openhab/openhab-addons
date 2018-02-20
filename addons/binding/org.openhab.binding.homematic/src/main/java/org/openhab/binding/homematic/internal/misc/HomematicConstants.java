/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homematic.internal.misc;

/**
 * Defines common constants, which are used across the Homematic implementation.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class HomematicConstants {
    public static final String DEVICE_TYPE_VIRTUAL = "HM-RCV-50";
    public static final String DEVICE_TYPE_VIRTUAL_WIRED = "HMW-RCV-50";
    public static final String DEVICE_TYPE_19_REMOTE_CONTROL = "HM-RC-19";
    public static final String DEVICE_TYPE_STATUS_DISPLAY = "HM-Dis-WM55";
    public static final String DEVICE_TYPE_EP_STATUS_DISPLAY = "HM-Dis-EP-WM55";

    public static final String DEVICE_TYPE_SHUTTER_CONTACT = "HM-Sec-SC";
    public static final String DEVICE_TYPE_SHUTTER_CONTACT_2 = "ZEL-STG-RM-FFK";
    public static final String DEVICE_TYPE_INCLINATION_SENSOR = "HM-Sec-TiS";
    public static final String DEVICE_TYPE_WIRED_IO_MODULE = "HMW-IO-12-Sw14-DR";
    public static final String DEVICE_TYPE_MAX_WINDOW_SENSOR = "BC-SC-Rd-WM";
    public static final String DEVICE_TYPE_SHUTTER_CONTACT_INTERFACE = "HM-SCI-3-FM";

    public static final String CHANNEL_TYPE_ALARMACTUATOR = "ALARMACTUATOR";
    public static final String CHANNEL_TYPE_SMOKE_DETECTOR = "SMOKE_DETECTOR";
    public static final String CHANNEL_TYPE_WATERDETECTIONSENSOR = "WATERDETECTIONSENSOR";
    public static final String CHANNEL_TYPE_RAINDETECTOR = "RAINDETECTOR";
    public static final String CHANNEL_TYPE_POWERMETER = "POWERMETER";
    public static final String CHANNEL_TYPE_SHUTTER_CONTACT = "SHUTTER_CONTACT";
    public static final String CHANNEL_TYPE_SENSOR = "SENSOR";
    public static final String CHANNEL_TYPE_BLIND = "BLIND";
    public static final String CHANNEL_TYPE_WINMATIC = "WINMATIC";
    public static final String CHANNEL_TYPE_AKKU = "AKKU";
    public static final String CHANNEL_TYPE_JALOUSIE = "JALOUSIE";
    public static final String CHANNEL_TYPE_SHUTTER_TRANSMITTER = "SHUTTER_TRANSMITTER";
    public static final String CHANNEL_TYPE_SHUTTER_VIRTUAL_RECEIVER = "SHUTTER_VIRTUAL_RECEIVER";

    public static final String DATAPOINT_NAME_CONFIG_PENDING = "CONFIG_PENDING";
    public static final String DATAPOINT_NAME_UPDATE_PENDING = "UPDATE_PENDING";
    public static final String DATAPOINT_NAME_UNREACH = "UNREACH";
    public static final String DATAPOINT_NAME_DEVICE_IN_BOOTLOADER = "DEVICE_IN_BOOTLOADER";
    public static final String DATAPOINT_NAME_INSTALL_TEST = "INSTALL_TEST";
    public static final String DATAPOINT_NAME_BATTERY_TYPE = "BATTERY_TYPE";
    public static final String DATAPOINT_NAME_LOWBAT = "LOWBAT";
    public static final String DATAPOINT_NAME_STATE = "STATE";
    public static final String DATAPOINT_NAME_HUMIDITY = "HUMIDITY";
    public static final String DATAPOINT_NAME_TEMPERATURE = "TEMPERATURE";
    public static final String DATAPOINT_NAME_MOTION = "MOTION";
    public static final String DATAPOINT_NAME_AIR_PRESSURE = "AIR_PRESSURE";
    public static final String DATAPOINT_NAME_WIND_SPEED = "WIND_SPEED";
    public static final String DATAPOINT_NAME_RAIN = "RAIN";
    public static final String DATAPOINT_NAME_BOOT = "BOOT";
    public static final String DATAPOINT_NAME_FREQUENCY = "FREQUENCY";
    public static final String DATAPOINT_NAME_SENSOR = "SENSOR";
    public static final String DATAPOINT_NAME_LEVEL = "LEVEL";
    public static final String DATAPOINT_NAME_SUBMIT = "SUBMIT";
    public static final String DATAPOINT_NAME_BEEP = "BEEP";
    public static final String DATAPOINT_NAME_BACKLIGHT = "BACKLIGHT";
    public static final String DATAPOINT_NAME_UNIT = "UNIT";
    public static final String DATAPOINT_NAME_TEXT = "TEXT";
    public static final String DATAPOINT_NAME_ON_TIME = "ON_TIME";
    public static final String DATAPOINT_NAME_STOP = "STOP";
    public static final String DATAPOINT_NAME_RSSI_DEVICE = "RSSI_DEVICE";
    public static final String DATAPOINT_NAME_RSSI_PEER = "RSSI_PEER";
    public static final String DATAPOINT_NAME_AES_KEY = "AES_KEY";
    public static final String DATAPOINT_NAME_VALUE = "VALUE";
    public static final String DATAPOINT_NAME_CALIBRATION = "CALIBRATION";
    public static final String DATAPOINT_NAME_LOWBAT_IP = "LOW_BAT";
    public static final String DATAPOINT_NAME_CHANNEL_FUNCTION = "CHANNEL_FUNCTION";

    public static final String VIRTUAL_DATAPOINT_NAME_BATTERY_TYPE = "BATTERY_TYPE";
    public static final String VIRTUAL_DATAPOINT_NAME_DELETE_DEVICE_MODE = "DELETE_DEVICE_MODE";
    public static final String VIRTUAL_DATAPOINT_NAME_DELETE_DEVICE = "DELETE_DEVICE";
    public static final String VIRTUAL_DATAPOINT_NAME_DISPLAY_OPTIONS = "DISPLAY_OPTIONS";
    public static final String VIRTUAL_DATAPOINT_NAME_FIRMWARE = "FIRMWARE";
    public static final String VIRTUAL_DATAPOINT_NAME_INSTALL_MODE = "INSTALL_MODE";
    public static final String VIRTUAL_DATAPOINT_NAME_INSTALL_MODE_DURATION = "INSTALL_MODE_DURATION";
    public static final String VIRTUAL_DATAPOINT_NAME_ON_TIME_AUTOMATIC = "ON_TIME_AUTOMATIC";
    public static final String VIRTUAL_DATAPOINT_NAME_RELOAD_ALL_FROM_GATEWAY = "RELOAD_ALL_FROM_GATEWAY";
    public static final String VIRTUAL_DATAPOINT_NAME_RELOAD_FROM_GATEWAY = "RELOAD_FROM_GATEWAY";
    public static final String VIRTUAL_DATAPOINT_NAME_RELOAD_RSSI = "RELOAD_RSSI";
    public static final String VIRTUAL_DATAPOINT_NAME_RSSI = "RSSI";
    public static final String VIRTUAL_DATAPOINT_NAME_STATE_CONTACT = "STATE_CONTACT";
    public static final String VIRTUAL_DATAPOINT_NAME_SIGNAL_STRENGTH = "SIGNAL_STRENGTH";
    public static final String VIRTUAL_DATAPOINT_NAME_PRESS = "PRESS";

    public static final String RPC_METHODNAME_EVENT = "event";
    public static final String RPC_METHODNAME_LIST_DEVICES = "listDevices";
    public static final String RPC_METHODNAME_UPDATE_DEVICE = "updateDevice";
    public static final String RPC_METHODNAME_DELETE_DEVICES = "deleteDevices";
    public static final String RPC_METHODNAME_NEW_DEVICES = "newDevices";
    public static final String RPC_METHODNAME_SYSTEM_LISTMETHODS = "system.listMethods";
    public static final String RPC_METHODNAME_SYSTEM_MULTICALL = "system.multicall";
    public static final String RPC_METHODNAME_SET_CONFIG_READY = "setReadyConfig";
}
