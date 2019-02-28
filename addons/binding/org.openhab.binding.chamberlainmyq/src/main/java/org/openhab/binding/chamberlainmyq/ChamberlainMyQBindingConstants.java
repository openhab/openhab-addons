/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.chamberlainmyq;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ChamberlainMyQBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Scott Hanson - Initial contribution
 */
@NonNullByDefault
public class ChamberlainMyQBindingConstants {

    public static final String BINDING_ID = "chamberlainmyq";

    // bridge
    public static final ThingTypeUID THING_TYPE_MYQ_BRIDGE = new ThingTypeUID(BINDING_ID, "MyQGateway");

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_DOOR_OPENER = new ThingTypeUID(BINDING_ID, "MyQDoorOpener");
    public static final ThingTypeUID THING_TYPE_LIGHT = new ThingTypeUID(BINDING_ID, "MyQLight");

    // List of all Channel ids
    public static final String CHANNEL_LIGHT_STATE = "lightstate";
    public static final String CHANNEL_DOOR_STATE = "doorstate";
    public static final String CHANNEL_ROLLER_STATE = "rollerstate";
    public static final String CHANNEL_DOOR_STATUS = "doorstatus";
    public static final String CHANNEL_DOOR_OPEN = "dooropen";
    public static final String CHANNEL_DOOR_CLOSED = "doorclosed";

    public static final String CHANNEL_DESCRIPTION = "description";
    public static final String CHANNEL_SERIAL_NUMBER = "serialnumber";

    // Bridge config properties
    public static final String USER_NAME = "username";
    public static final String PASSWORD = "password";
    public static final String POLL_PERIOD = "pollPeriod";
    public static final String QUICK_POLL_PERIOD = "quickPollPeriod";
    public static final String TIME_OUT = "timeout";

    // Door Opener/Light config properties
    public static final String MYQ_ID = "MyQDeviceId";
    public static final String MYQ_TYPE = "MyQDeviceTypeName";
    public static final String MYQ_TYPEID = "MyQDeviceTypeId";
    public static final String MYQ_PARENT = "ParentMyQDeviceId";
    public static final String MYQ_SERIAL = "SerialNumber";
    public static final String MYQ_DESC = "desc";
    public static final String MYQ_ONLINE = "online";
    public static final String MYQ_STATE = "state";

    // API Information
    public static final String WEBSITE = "https://myqexternal.myqdevice.com";
    public static final String APP_ID = "OA9I/hgmPHFp9RYKJqCKfwnhh28uqLJzZ9KOJf1DXoo8N2XAaVX6A1wcLYyWsnnv";
    public static final String CULTURE = "en";
    public static final String BRANDID = "2";
    public static final String USERAGENT = "Chamberlain/3.73";
    public static final String APIVERSION = "4.1";
}
