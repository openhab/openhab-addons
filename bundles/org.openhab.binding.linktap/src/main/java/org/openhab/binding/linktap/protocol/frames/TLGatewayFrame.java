/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.linktap.protocol.frames;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link TLGatewayFrame} defines the common framing data, for requests and responses
 * from a Gateway device.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class TLGatewayFrame implements IPayloadValidator {

    public static final int DEFAULT_INT = -1;
    public static final String EMPTY_STRING = "";
    public static final String[] EMPTY_STRING_ARRAY = new String[0];

    protected static final Pattern DEVICE_ID_PATTERN = Pattern.compile("[a-zA-Z0-9]{0,16}");
    protected static final Pattern SUB_DEVICE_ID_PATTERN = Pattern.compile("[a-zA-Z0-9]{0,16}_[0-9]");

    public TLGatewayFrame() {
    }

    public TLGatewayFrame(final int command) {
        this.command = command;
    }

    public @Nullable Class<TLGatewayFrame> getResponseFrame() {
        return TLGatewayFrame.class;
    }

    /**
     * Defines the CMD identifier, that defines the payload type.
     * Possible values in constants CMD_*
     */
    @SerializedName("cmd")
    @Expose
    public int command = DEFAULT_INT;

    /**
     * Defines the gateway identifier.
     * Limited to the first 16 digits and letters of the Gateway ID
     */
    @SerializedName("gw_id")
    @Expose
    public String gatewayId = EMPTY_STRING;

    public Collection<ValidationError> getValidationErrors() {
        final ArrayList<ValidationError> errors = new ArrayList<>(0);
        if (command < CMD_HANDSHAKE || command > CMD_PAUSE_WATER_PLAN) {
            errors.add(new ValidationError("cmd", "not in range " + CMD_HANDSHAKE + " -> " + CMD_PAUSE_WATER_PLAN));
        }
        if (!DEVICE_ID_PATTERN.matcher(gatewayId).matches()) {
            errors.add(new ValidationError("gw_id", "not in range " + CMD_HANDSHAKE + " -> " + CMD_PAUSE_WATER_PLAN));
        }
        return errors;
    }

    // COMMAND Values

    /**
     * Command - 0. Handshake Message
     *
     * @direction GW->Broker->App
     * @description Handshake message. It is the first message after the Gateway connects to the MQTT Broker.
     *              </p>
     *              Gateway acquires its local time base from third-party application, and sends its
     *              end devices ID list to third-party application, through this message.
     */
    public static final int CMD_HANDSHAKE = 0;

    /**
     * Command - 1. Add / Register End Device
     *
     * @direction App->Broker->GW
     * @description Add's / Registers a new device to the Gateway
     *              (e.g., water timer) to the Gateway.
     */
    public static final int CMD_ADD_END_DEVICE = 1;

    /**
     * Command - 2. Delete End Device
     *
     * @direction App->Broker->GW
     * @description Removes / De-registers a device (e.g., water timer) from the Gateway.
     */
    public static final int CMD_REMOVE_END_DEVICE = 2;

    /**
     * Command - 3. Update Water Timer Status
     *
     * @direction App->Broker->GW
     * @description Update water timer’s status
     */
    public static final int CMD_UPDATE_WATER_TIMER_STATUS = 3;

    /**
     * Command - 103. Update Water Timer Status Unsolicited
     *
     * @direction GW->Broker->App
     * @description Update water timer’s status
     */
    public static final int CMD_UPDATE_WATER_TIMER_STATUS_UNSOLICITED = 103;

    /**
     * Command - 4. Send / Setup Water Plan
     *
     * @direction App->Broker->GW
     * @description Send / set up watering plan
     *              (The prerequisite for the correct execution of the watering plan is that
     *              the Gateway’s local time base has been properly set through
     *              CMD:0 (CMD_HANDSHAKE) or
     *              CMD:13)
     */
    public static final int CMD_SETUP_WATER_PLAN = 4;

    /**
     * Command - 5. Delete Water Plan
     *
     * @direction App->Broker->GW
     * @description Deletes the existing water plan
     */
    public static final int CMD_REMOVE_WATER_PLAN = 5;

    /**
     * Command - 6. Start Watering Immediately
     *
     * @direction App->Broker->GW
     * @description Start watering for the immediate duration irrelevant
     *              of water plan. (Gateway local time base is not required
     *              for the operation of this mode).
     */
    public static final int CMD_IMMEDIATE_WATER_START = 6;

    /**
     * Command - 7. Stop Watering Immediately
     *
     * @direction App->Broker->GW
     * @description Stop's watering immediately. The water plan will resume at
     *              the next point as setup.
     */
    public static final int CMD_IMMEDIATE_WATER_STOP = 7;

    /**
     * Command - 8. Fetch / Push Rainfall Data
     *
     * @direction GW->Broker->App
     * @description Request for Rainfall data
     * @direction App->Broker->GW
     * @description Push of Rainfall data
     */
    public static final int CMD_RAINFALL_DATA = 8;

    /**
     * Command - 9. Notificaiton of watering has been skipped
     *
     * @direction GW->Broker->App
     * @description Notification that a watering cycle has been skipped due to rainfall
     */
    public static final int CMD_NOTIFICATION_WATERING_SKIPPED = 9;

    /**
     * Command - 10. Alert Enablement / Disablement
     *
     * @direction App->Broker->GW
     * @description Enable or disablement of particular monitoring alerts
     */
    public static final int CMD_ALERT_ENABLEMENT = 10;

    /**
     * Command - 11. Dismiss Alert
     *
     * @direction App->Broker->GW
     * @description Dismisses the given alert
     */
    public static final int CMD_ALERT_DISMISS = 11;

    /**
     * Command - 12 Lockout state setup
     *
     * @direction App->Broker->GW
     * @description Setup lockout state for manual On/Off button (for G15 and G25 models only)
     */
    public static final int CMD_LOCKOUT_STATE = 12;

    /**
     * Command - 13 Gateways Date & Time Sync Request
     *
     * @direction GW->Broker->App
     * @description Request for the current date and time, for the Gateway to apply
     */
    public static final int CMD_DATETIME_SYNC = 13;

    /**
     * Command - 14 Read the Gateways Date & Time
     *
     * @direction App->Broker->Gw
     * @description Fetch Gateway's local datetime
     */
    public static final int CMD_DATETIME_READ = 14;

    /**
     * Command - 15 Test wireless performance of end device
     *
     * @direction App->Broker->Gw
     * @description Request a communications test between the Gateway and End Device
     */
    public static final int CMD_WIRELESS_CHECK = 15;

    /**
     * Command - 16 Get Gateway's configuration
     *
     * @direction App->Broker->Gw
     * @description Request the current Gateway's configuration
     */
    public static final int CMD_GET_CONFIGURATION = 16;

    /**
     * Command - 17 Set Gateway's configuration
     *
     * @direction App->Broker->Gw
     * @description Update the configuration for a device in the Gateway
     */
    public static final int CMD_SET_CONFIGURATION = 17;

    /**
     * Command - 18 Pause Water Plan
     *
     * @direction App->Broker->Gw
     * @description Pause the Water Plan for the given duration
     *              (0.1 to 240 hours)
     */
    public static final int CMD_PAUSE_WATER_PLAN = 18;
}
