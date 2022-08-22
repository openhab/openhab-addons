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
package org.openhab.binding.smsmodem.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SMSModemBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gwendal ROULLEAU - Initial contribution
 */
@NonNullByDefault
public class SMSModemBindingConstants {

    private static final String BINDING_ID = "smsmodem";

    // List of all Thing Type UIDs
    public static final ThingTypeUID SMSCONVERSATION_THING_TYPE = new ThingTypeUID(BINDING_ID, "smsconversation");
    public static final ThingTypeUID SMSMODEMBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "smsmodembridge");
    public static final ThingTypeUID SMSMODEMREMOTEBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID,
            "smsmodemremotebridge");

    // List of all Channel ids
    public static final String CHANNEL_RECEIVED = "receive";
    public static final String CHANNEL_SEND = "send";
    public static final String CHANNEL_DELIVERYSTATUS = "deliverystatus";
    public static final String CHANNEL_TRIGGER_MODEM_RECEIVE = "receivetrigger";

    // parameter
    public static final String SMSCONVERSATION_PARAMETER_RECIPIENT = "recipient";

    // List of all properties
    public static final String PROPERTY_MANUFACTURER = Thing.PROPERTY_VENDOR;
    public static final String PROPERTY_MODEL = Thing.PROPERTY_MODEL_ID;
    public static final String PROPERTY_SWVERSION = Thing.PROPERTY_FIRMWARE_VERSION;
    public static final String PROPERTY_SERIALNO = Thing.PROPERTY_SERIAL_NUMBER;
    public static final String PROPERTY_IMSI = "imsi";
    public static final String PROPERTY_RSSI = "rssi";
    public static final String PROPERTY_MODE = "mode";
    public static final String PROPERTY_TOTALSENT = "sent";
    public static final String PROPERTY_TOTALFAILED = "failed";
    public static final String PROPERTY_TOTALRECEIVED = "received";
    public static final String PROPERTY_TOTALFAILURE = "failure";
}
